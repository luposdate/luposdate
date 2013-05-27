/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayPresortingNumbers;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.Entry;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.LanguageTaggedLiteralOriginalLanguage;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.items.literal.TypedLiteralOriginalContent;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.codemap.CodeMapURILiteral;
import lupos.datastructures.items.literal.string.PlainStringLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.smallerinmemorylargerondisk.CollectionImplementation;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;
import lupos.engine.operators.multiinput.join.InnerNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.LeafNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.NodeInPartitionTree;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.VarBucket;

public class Registration {

	public interface DeSerializer<T> {

		public int length(T t);
		public int length(T t, T previousValue);
		public int serialize(T t, byte[] byteArray) throws IOException;
		public int serialize(T t, T previousValue, byte[] byteArray) throws IOException;
		public int serialize(T t, byte[] byteArray, int offset) throws IOException;
		public int serialize(T t, T previousValue, byte[] byteArray, int offset) throws IOException;
		public void serialize(T t, OutputStream out) throws IOException;
		public void serialize(T t, T previousValue, OutputStream out) throws IOException;

		public T deserialize(InputStream in) throws IOException, URISyntaxException, ClassNotFoundException;
		public T deserialize(T previousValue, InputStream in) throws IOException, URISyntaxException, ClassNotFoundException;
		public T deserialize(byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException;
		public Tuple<T, Integer> deserializeAndLength(byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException;
		public T deserialize(T previousValue, byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException;
		public Tuple<T, Integer> deserializeAndLength(T previousValue, byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException;
		public T deserialize(byte[] byteArray, int offset) throws IOException, URISyntaxException, ClassNotFoundException;
		public Tuple<T, Integer> deserializeAndNewOffset(byte[] byteArray, int offset) throws IOException, URISyntaxException, ClassNotFoundException;
		public T deserialize(T previousValue, byte[] byteArray, int offset) throws IOException, URISyntaxException, ClassNotFoundException;
		public Tuple<T, Integer> deserializeAndNewOffset(T previousValue, byte[] byteArray, int offset) throws IOException, URISyntaxException, ClassNotFoundException;

		/**
		 * The registered class for which the (de-) serialization should take
		 * place
		 *
		 * @return the class to be serialized
		 */
		public Class<? extends T>[] getRegisteredClasses();

		/**
		 * whether or not LUPOSDATE should use this DeSerializer for only exact
		 * classes (as the registered class) or also for sublasses of the
		 * registered class
		 *
		 * @return
		 */
		public boolean exactClass();

		/**
		 * serialization method
		 *
		 * @param t
		 *            the object to be serialized
		 * @param out
		 *            the output stream into which the object should be
		 *            serialized
		 */
		public void serialize(T t, LuposObjectOutputStream out) throws IOException;

		/**
		 * deserialization method
		 *
		 * @param in
		 *            the input stream from which the object is deserialized
		 * @return the deserialized object
		 */
		public T deserialize(LuposObjectInputStream<T> in) throws IOException, ClassNotFoundException, URISyntaxException;
	}

	/**
	 * This class is just for implementing some methods if the previous value is not used to compress the value to be serialized.
	 * If the value to be serialized is compressed based on the previous value, these methods must be overridden.
	 * Further there are some methods based on the others implemented.
	 *
	 * @param <T> The class the objects of which are (de-) serialized
	 */
	public abstract static class DeSerializerSuperClass<T> implements DeSerializer<T> {

		@Override
		public int length(final T t, final T previousValue){
			return this.length(t);
		}

		@Override
		public void serialize(final T t, final T previousValue, final OutputStream out) throws IOException {
			this.serialize(t, out);
		}

		@Override
		public int serialize(final T t, final byte[] byteArray) throws IOException {
			return this.serialize(t, byteArray, 0);
		}

		@Override
		public int serialize(final T t, final T previousValue, final byte[] byteArray) throws IOException {
			return this.serialize(t, previousValue, byteArray, 0);
		}

		@Override
		public int serialize(final T t, final byte[] byteArray, final int offset) throws IOException {
			final ExistingByteArrayOutputStream out = new ExistingByteArrayOutputStream(byteArray, offset);
			this.serialize(t, out);
			return out.getOffset();
		}

		@Override
		public int serialize(final T t, final T previousValue, final byte[] byteArray, final int offset) throws IOException {
			final ExistingByteArrayOutputStream out = new ExistingByteArrayOutputStream(byteArray, offset);
			this.serialize(t, previousValue, out);
			return out.getOffset();
		}

		@Override
		public T deserialize(final T previousValue, final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return this.deserialize(in);
		}

		@Override
		public T deserialize(final byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException {
			return this.deserialize(byteArray, 0);
		}

		@Override
		public Tuple<T, Integer> deserializeAndLength(final byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException {
			return this.deserializeAndNewOffset(byteArray, 0);
		}

		@Override
		public T deserialize(final T previousValue, final byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException {
			return this.deserialize(previousValue, byteArray, 0);
		}

		@Override
		public Tuple<T, Integer> deserializeAndLength(final T previousValue, final byte[] byteArray) throws IOException, URISyntaxException, ClassNotFoundException {
			return this.deserializeAndNewOffset(previousValue, byteArray, 0);
		}

		@Override
		public T deserialize(final byte[] byteArray, final int offset) throws IOException, URISyntaxException, ClassNotFoundException {
			final ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, byteArray.length - offset);
			return this.deserialize(in);
		}

		@Override
		public Tuple<T, Integer> deserializeAndNewOffset(final byte[] byteArray, final int offset) throws IOException, URISyntaxException, ClassNotFoundException {
			final ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, byteArray.length - offset);
			return new Tuple<T, Integer>(this.deserialize(in), byteArray.length - in.available());
		}

		@Override
		public T deserialize(final T previousValue, final byte[] byteArray, final int offset) throws IOException, URISyntaxException, ClassNotFoundException {
			final ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, byteArray.length - offset);
			return this.deserialize(previousValue, in);
		}

		@Override
		public Tuple<T, Integer> deserializeAndNewOffset(final T previousValue, final byte[] byteArray, final int offset) throws IOException, URISyntaxException, ClassNotFoundException {
			final ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, byteArray.length - offset);
			return new Tuple<T, Integer>(this.deserialize(previousValue, in), byteArray.length - in.available());
		}

		@Override
		public void serialize(final T t, final LuposObjectOutputStream out) throws IOException {
			this.serialize(t, out.os);
		}

		@Override
		public T deserialize(final LuposObjectInputStream<T> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return this.deserialize(in.is);
		}
	}

	public abstract static class DeSerializerConsideringSubClasses<T> extends DeSerializerSuperClass<T> {
		@Override
		public boolean exactClass() {
			return false;
		}

		/**
		 * Should be overridden to provide an instanceof test with the considered class
		 * @param o the object to be checked with instanceof
		 * @return (o instanceof T)
		 */
		public abstract boolean instanceofTest(Object o);
	}

	public abstract static class DeSerializerExactClass<T> extends DeSerializerSuperClass<T> {
		@Override
		public boolean exactClass() {
			return true;
		}
	}

	private static class Container<T> {
		public int id;
		public DeSerializer<T> deserializer;

		public Container(final int id, final DeSerializer<T> deserializer) {
			this.id = id;
			this.deserializer = deserializer;
		}
	}

	private static HashMap<Class<?>, Container> deSerializerForClass = new HashMap<Class<?>, Container>();
	private static DeSerializer[] deSerializerForId = new DeSerializer[0];
	private static int[] considerSubClasses = new int[0];

	/**
	 * This method adds a (de-) serializer to be used within LUPOSDATE while
	 * de-/serializing objects
	 *
	 * @param deserializer
	 *            the (de-) serializer to be registered
	 */
	public static void addDeSerializer(final DeSerializer... deserializer) {
		int subclasses = 0;
		for (final DeSerializer d : deserializer) {
			if (!d.exactClass()){
				subclasses++;
			}
		}
		final DeSerializer[] zdeSerializerForId = new DeSerializer[deSerializerForId.length
		                                                           + deserializer.length];
		if (zdeSerializerForId.length > 255){
			System.err.println("Too many registered (De-)Serializer! Only up to 255 are allowed! De-/Serialization will probably not work correctly!");
		}
		System.arraycopy(deSerializerForId, 0, zdeSerializerForId, 0, deSerializerForId.length);
		System.arraycopy(deserializer, 0, zdeSerializerForId, deSerializerForId.length, deserializer.length);

		final int[] zconsiderSubClasses = new int[considerSubClasses.length
		                                          + subclasses];
		System.arraycopy(considerSubClasses, 0, zconsiderSubClasses, subclasses, considerSubClasses.length);
		int id = deSerializerForId.length;
		int subclassindex = 0;
		for (final DeSerializer d : deserializer) {
			final Container container = new Container(id, d);
			for (final Class c : d.getRegisteredClasses()){
				deSerializerForClass.put(c, container);
			}
			if (!d.exactClass()) {
				zconsiderSubClasses[subclassindex] = id;
				subclassindex++;
			}
			id++;
		}
		considerSubClasses = zconsiderSubClasses;
		deSerializerForId = zdeSerializerForId;
	}

	public static void serializeWithoutId(final Object o, final LuposObjectOutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					deSerializerForId[id].serialize(o, out);
					return;
				}
			}
		} else {
			container.deserializer.serialize(o, out);
			return;
		}
		System.err.println("No DeSerializer for class " + o.getClass()
				+ " found! Using Java standard de-/serialization!");
		out.writeObject(o);
	}

	public static void serializeWithoutId(final Object o, final OutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					deSerializerForId[id].serialize(o, out);
					return;
				}
			}
		} else {
			container.deserializer.serialize(o, out);
			return;
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		throw new IOException(errorText);
	}

	public static int serializeWithoutId(final Object o, final byte[] page, final int offset) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					return deSerializerForId[id].serialize(o, page, offset);
				}
			}
		} else {
			return container.deserializer.serialize(o, page, offset);
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		throw new IOException(errorText);
	}

	public static int lengthSerializeWithoutId(final Object o) {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id]).instanceofTest(o)) {
					return deSerializerForId[id].length(o);
				}
			}
		} else {
			return container.deserializer.length(o);
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		return 0;
	}

	public static void serializeWithId(final Object o, final LuposObjectOutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					out.os.write(id);
					deSerializerForId[id].serialize(o, out);
					return;
				}
			}
		} else {
			out.os.write(container.id);
			container.deserializer.serialize(o, out);
			return;
		}
		System.err.println("No DeSerializer for class " + o.getClass()
				+ " found! Using Java standard de-/serialization!");
		out.os.write(deSerializerForId.length);
		out.writeObject(o);
	}

	public static void serializeWithId(final Object o, final OutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					out.write(id);
					deSerializerForId[id].serialize(o, out);
					return;
				}
			}
		} else {
			out.write(container.id);
			container.deserializer.serialize(o, out);
			return;
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		throw new IOException(errorText);
	}

	public static int serializeWithId(final Object o, final byte[] page, int offset) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					page[offset++] = (byte) id;
					return deSerializerForId[id].serialize(o, page, offset);
				}
			}
		} else {
			page[offset++] = (byte) container.id;
			return container.deserializer.serialize(o, page, offset);
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		throw new IOException(errorText);
	}

	public static int lengthSerializeWithId(final Object o){
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id]).instanceofTest(o)) {
					return LengthHelper.lengthLuposByte() + deSerializerForId[id].length(o);
				}
			}
		} else {
			return LengthHelper.lengthLuposByte() + container.deserializer.length(o);
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		return 0;
	}

	public final static int lengthSerializeId() {
		return LengthHelper.lengthLuposByte();
	}

	public static void serializeId(final Object o, final LuposObjectOutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					out.os.write(id);
					return;
				}
			}
		} else {
			out.os.write(container.id);
			return;
		}
		System.err.println("No DeSerializer for class " + o.getClass()
				+ " found! Using Java standard de-/serialization!");
		out.os.write(deSerializerForId.length);
	}

	public static void serializeId(final Object o, final OutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					out.write(id);
					return;
				}
			}
		} else {
			out.write(container.id);
			return;
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		throw new IOException(errorText);
	}

	public static int serializeId(final Object o, final byte[] page, int offset) throws IOException {
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					page[offset++] = (byte) id;
					return offset;
				}
			}
		} else {
			page[offset++] = (byte) container.id;
			return offset;
		}
		final String errorText = "No DeSerializer for class " + o.getClass() + " found!";
		System.err.println(errorText);
		throw new IOException(errorText);
	}

	public static void serializeClass(final Class<?> o, final LuposObjectOutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o);
		if (container == null) {
			System.err.println("No DeSerializer for class " + o.getClass()
					+ " found! Using Java standard de-/serialization!");
			out.os.write(deSerializerForId.length);
		} else {
			out.os.write(container.id);
		}
	}

	public static void serializeClass(final Class<?> o, final OutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o);
		if (container == null) {
			final String errorText = "No DeSerializer for class " + o.getClass()
					+ " found! No Java standard de-/serialization here supported!";
			System.err.println(errorText);
			throw new IOException(errorText);
		} else {
			out.write(container.id);
		}
	}

	public static int serializeClass(final Class<?> o, final byte[] page, int offset) throws IOException {
		final Container container = deSerializerForClass.get(o);
		if (container == null) {
			final String errorText = "No DeSerializer for class " + o.getClass()
					+ " found! No Java standard de-/serialization here supported!";
			System.err.println(errorText);
			throw new IOException(errorText);
		} else {
			page[offset++] = (byte) container.id;
			return offset;
		}
	}

	public static Class<?>[] deserializeId(final LuposObjectInputStream in) throws IOException {
		final int index = in.is.read();
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return deSerializerForId[index].getRegisteredClasses();
		} else {
			System.err.println("No DeSerializer for id " + index
					+ " found! Using Java standard de-/serialization!");
			return new Class[] { Object.class };
		}
	}

	public static Class<?>[] deserializeId(final InputStream in) throws IOException {
		final int index = in.read();
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return deSerializerForId[index].getRegisteredClasses();
		} else {
			final String errorText = "No DeSerializer for id " + index
					+ " found! Using Java standard de-/serialization!";
			System.err.println(errorText);
			throw new IOException(errorText);
		}
	}

	public static Class<?>[] deserializeId(final byte[] page, final int offset) throws IOException {
		final int index = 0xFF & page[offset];
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return deSerializerForId[index].getRegisteredClasses();
		} else {
			final String errorText = "No DeSerializer for id " + index
					+ " found! Using Java standard de-/serialization!";
			System.err.println(errorText);
			throw new IOException(errorText);
		}
	}

	public static <T> T deserializeWithoutId(
			final Class<? extends T> registeredClass,
			final LuposObjectInputStream<T> in) throws IOException,
			ClassNotFoundException, URISyntaxException {
		final Container<T> container = deSerializerForClass
		.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserialize(in);
		} else {
			System.err.println("No DeSerializer for class " + registeredClass
					+ " found! Using Java standard de-/serialization!");
			return (T) in.readObject();
		}
	}

	public static <T> T deserializeWithoutId(
			final Class<? extends T> registeredClass,
			final InputStream in) throws IOException,
			ClassNotFoundException, URISyntaxException {
		final Container<T> container = deSerializerForClass.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserialize(in);
		} else {
			final String errorText = "No DeSerializer for class " + registeredClass + " found!";
			System.err.println(errorText);
			throw new IOException(errorText);
		}
	}

	public static<T> T deserializeWithoutId(
			final Class<? extends T> registeredClass,
			final byte[] page, final int offset) throws IOException,
			ClassNotFoundException, URISyntaxException {
		final Container<T> container = deSerializerForClass.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserialize(page, offset);
		} else {
			final String errorText = "No DeSerializer for class " + registeredClass + " found!";
			System.err.println(errorText);
			throw new IOException(errorText);
		}
	}

	public static<T> Tuple<T, Integer> deserializeWithoutIdAndNewOffset(
			final Class<? extends T> registeredClass,
			final byte[] page, final int offset) throws IOException,
			ClassNotFoundException, URISyntaxException {
		final Container<T> container = deSerializerForClass.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserializeAndNewOffset(page, offset);
		} else {
			final String errorText = "No DeSerializer for class " + registeredClass + " found!";
			System.err.println(errorText);
			throw new IOException(errorText);
		}
	}

	public static<T> T deserializeWithId(final LuposObjectInputStream<T> in) throws IOException, ClassNotFoundException, URISyntaxException {
		final int index = in.is.read();
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return (T) deSerializerForId[index].deserialize(in);
		} else {
			System.err.println("No DeSerializer for id " + index
					+ " found! Using Java standard de-/serialization!");
			return (T) in.readObject();
		}
	}

	public static <T> T deserializeWithId(final InputStream in) throws IOException, ClassNotFoundException, URISyntaxException {
		final int index = in.read();
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return (T) deSerializerForId[index].deserialize(in);
		} else {
			System.err.println("No DeSerializer for id " + index
					+ " found! Returning null!");
			return null;
		}
	}

	public static<T> T deserializeWithId(final byte[] page, int offset) throws IOException, ClassNotFoundException, URISyntaxException {
		final int index = 0xFF & page[offset];
		offset++;
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return (T) deSerializerForId[index].deserialize(page, offset);
		} else {
			System.err.println("No DeSerializer for id " + index + " found! Returning null!");
			return null;
		}
	}

	public static<T> Tuple<T, Integer> deserializeWithIdAndNewOffset(final byte[] page, int offset) throws IOException, ClassNotFoundException, URISyntaxException {
		final int index = 0xFF & page[offset];
		offset++;
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return deSerializerForId[index].deserializeAndNewOffset(page, offset);
		} else {
			System.err.println("No DeSerializer for id " + index + " found! Returning null!");
			return null;
		}
	}

// ------------------------------------------ The De-(Serialization) classes for standard classes -----------------------------------------------

	public static class TRIPLE extends DeSerializerConsideringSubClasses<Triple> {

		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Triple;
		}

		@Override
		public Triple deserialize(final LuposObjectInputStream<Triple> in)throws IOException, URISyntaxException, ClassNotFoundException {
			return in.readLuposTriple();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Triple>[] getRegisteredClasses() {
			return new Class[] { Triple.class };
		}

		@Override
		public void serialize(final Triple t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposTriple(t);
		}

		@Override
		public int length(final Triple t) {
			return LengthHelper.lengthLuposTriple(t);
		}

		@Override
		public void serialize(final Triple t, final OutputStream out) throws IOException {
			OutHelper.writeLuposTriple(t, out);
		}

		@Override
		public Triple deserialize(final InputStream in) throws ClassNotFoundException, IOException {
			return InputHelper.readLuposTriple(in);
		}

		@Override
		public int length(final Triple t, final Triple previousTriple) {
			return LengthHelper.lengthLuposTriple(t, previousTriple);
		}

		@Override
		public void serialize(final Triple t, final Triple previousTriple, final OutputStream out) throws IOException {
			OutHelper.writeLuposTriple(t, previousTriple, out);
		}

		@Override
		public Triple deserialize(final Triple previousTriple, final InputStream in) throws IOException, ClassNotFoundException {
			return InputHelper.readLuposTriple(previousTriple, in);
		}
	}

	public static class SUPERLITERAL extends DeSerializerConsideringSubClasses<Literal> {

		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Literal;
		}

		@Override
		public Literal deserialize(final LuposObjectInputStream<Literal> in) throws IOException {
			return LiteralFactory.readLuposLiteral(in);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Literal>[] getRegisteredClasses() {
			return new Class[] { Literal.class,
					AnonymousLiteral.class,
					LanguageTaggedLiteral.class,
					LanguageTaggedLiteralOriginalLanguage.class,
					LazyLiteral.class,
					LazyLiteralOriginalContent.class,
					TypedLiteral.class,
					TypedLiteralOriginalContent.class,
					URILiteral.class,
					CodeMapLiteral.class,
					CodeMapURILiteral.class,
					PlainStringLiteral.class,
					StringLiteral.class,
					StringURILiteral.class};
		}

		@Override
		public int length(final Literal t) {
			return LengthHelper.lengthLuposLiteral(t);
		}

		@Override
		public void serialize(final Literal t, final OutputStream out) throws IOException {
			LiteralFactory.writeLuposLiteral(t, out);
		}

		@Override
		public Literal deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return LiteralFactory.readLuposLiteral(in);
		}
	}

	@SuppressWarnings("rawtypes")
	public static class ENTRY extends DeSerializerConsideringSubClasses<lupos.datastructures.dbmergesortedds.Entry> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof lupos.datastructures.dbmergesortedds.Entry;
		}

		@Override
		public void serialize(final Entry t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposEntry(t);
		}

		@Override
		public Entry deserialize(final LuposObjectInputStream in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposEntry();
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		public Class<Entry>[] getRegisteredClasses() {
			return new Class[] { lupos.datastructures.dbmergesortedds.Entry.class };
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		public void serialize(final Entry t, final OutputStream out) throws IOException {
			OutHelper.writeLuposEntry(t, out);
		}

		@Override
		public Entry deserialize(final InputStream in) throws IOException, ClassNotFoundException, URISyntaxException {
			return InputHelper.readLuposEntry(in);
		}

		@Override
		public int length(final Entry t) {
			return Registration.lengthSerializeWithId(t.e);
		}
	}

	public static class STRING extends DeSerializerConsideringSubClasses<String> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof String;
		}

		@Override
		public String deserialize(final LuposObjectInputStream<String> in) throws IOException {
			return in.readLuposDifferenceString();
		}

		@Override
		public Class<String>[] getRegisteredClasses() {
			return new Class[] { String.class };
		}

		@Override
		public void serialize(final String t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposDifferenceString(t);
		}

		@Override
		public int length(final String t) {
			return LengthHelper.lengthLuposString(t);
		}

		@Override
		public void serialize(final String t, final OutputStream out) throws IOException {
			OutHelper.writeLuposString(t, out);
		}

		@Override
		public String deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposString(in);
		}

		@Override
		public int length(final String t, final String previousString) {
			return LengthHelper.lengthLuposString(t, previousString);
		}

		@Override
		public void serialize(final String t, final String previousString, final OutputStream out) throws IOException {
			OutHelper.writeLuposString(t, previousString, out);
		}

		@Override
		public String deserialize(final String previousString, final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposString(previousString, in);
		}
	}

	@SuppressWarnings("rawtypes")
	public static class MAPENTRY extends DeSerializerConsideringSubClasses<lupos.datastructures.dbmergesortedds.MapEntry> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof MapEntry;
		}

		@Override
		public MapEntry deserialize(final LuposObjectInputStream<MapEntry> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return InputHelper.readLuposMapEntry(in);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<MapEntry>[] getRegisteredClasses() {
			return new Class[] { MapEntry.class };
		}

		@SuppressWarnings("unchecked")
		@Override
		public void serialize(final MapEntry t, final LuposObjectOutputStream out) throws IOException {
			OutHelper.writeLuposMapEntry(t, out);
		}

		@Override
		public MapEntry deserialize(final InputStream in) throws IOException, ClassNotFoundException, URISyntaxException {
			return InputHelper.readLuposMapEntry(in);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void serialize(final MapEntry t, final OutputStream out) throws IOException {
			OutHelper.writeLuposMapEntry(t, out);
		}

		@SuppressWarnings("unchecked")
		@Override
		public int length(final MapEntry t) {
			return LengthHelper.lengthLuposMapEntry(t);
		}
	}

	public static class BINDINGS extends DeSerializerConsideringSubClasses<Bindings> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Bindings;
		}

		@Override
		public Bindings deserialize(final LuposObjectInputStream<Bindings> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposBindings();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Bindings>[] getRegisteredClasses() {
			return new Class[] { Bindings.class, BindingsArray.class,
					BindingsMap.class, BindingsArrayPresortingNumbers.class,
					BindingsArrayVarMinMax.class,
					BindingsArrayReadTriples.class };
		}

		@Override
		public void serialize(final Bindings t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposBindings(t);
		}

		@Override
		public int length(final Bindings t) {
			return LengthHelper.lengthLuposBindings(t);
		}

		@Override
		public int length(final Bindings t, final Bindings previousBindings) {
			return LengthHelper.lengthLuposBindings(t, previousBindings);
		}

		@Override
		public void serialize(final Bindings t, final OutputStream out) throws IOException {
			OutHelper.writeLuposBindings(t, out);
		}

		@Override
		public void serialize(final Bindings t, final Bindings previousBindings, final OutputStream out) throws IOException {
			OutHelper.writeLuposBindings(t, previousBindings, out);
		}

		@Override
		public Bindings deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposBindings(in);
		}

		@Override
		public Bindings deserialize(final Bindings previousBindings, final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposBindings(previousBindings, in);
		}
	}

	public static class TRIPLEKEY extends DeSerializerConsideringSubClasses<TripleKey> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof TripleKey;
		}

		@Override
		public TripleKey deserialize(final LuposObjectInputStream<TripleKey> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposTripleKey();
		}

		@Override
		public Class<TripleKey>[] getRegisteredClasses() {
			return new Class[] { TripleKey.class };
		}

		@Override
		public void serialize(final TripleKey t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposTripleKey(t);
		}

		@Override
		public int length(final TripleKey t) {
			return LengthHelper.lengthLuposTripleKey(t);
		}

		@Override
		public int length(final TripleKey t, final TripleKey prevousTripleKey) {
			return LengthHelper.lengthLuposTripleKey(t, prevousTripleKey);
		}

		@Override
		public void serialize(final TripleKey t, final OutputStream out) throws IOException {
			OutHelper.writeLuposTripleKey(t, out);
		}

		@Override
		public void serialize(final TripleKey t, final TripleKey previousTripleKey, final OutputStream out) throws IOException {
			OutHelper.writeLuposTripleKey(t, previousTripleKey, out);
		}

		@Override
		public TripleKey deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposTripleKey(in);
		}

		@Override
		public TripleKey deserialize(final TripleKey previousTripleKey, final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposTripleKey(previousTripleKey, in);
		}
	}

	public static class DISKCOLLECTION extends DeSerializerConsideringSubClasses<DiskCollection> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof DiskCollection;
		}

		@Override
		public DiskCollection deserialize(final LuposObjectInputStream<DiskCollection> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposDiskCollection();
		}

		@Override
		public Class<DiskCollection>[] getRegisteredClasses() {
			return new Class[] { DiskCollection.class };
		}

		@Override
		public void serialize(final DiskCollection t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposDiskCollection(t);
		}

		@Override
		public int length(final DiskCollection t) {
			return t.lengthLuposObject();
		}

		@Override
		public void serialize(final DiskCollection t, final OutputStream out) throws IOException {
			t.writeLuposObject(out);
		}

		@Override
		public DiskCollection deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return DiskCollection.readAndCreateLuposObject(in);
		}
	}

	@SuppressWarnings("rawtypes")
	public static class COLLECTION extends DeSerializerConsideringSubClasses<Collection> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Collection;
		}

		@Override
		public Collection deserialize(final LuposObjectInputStream<Collection> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposCollection();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Collection>[] getRegisteredClasses() {
			return new Class[] {
					Collection.class,
					CollectionImplementation.class,
					LinkedList.class,
					ArrayList.class };
		}

		@Override
		public void serialize(final Collection t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposCollection(t);
		}

		@Override
		public int length(final Collection t) {
			return LengthHelper.lengthLuposCollection(t);
		}

		@Override
		public void serialize(final Collection t, final OutputStream out) throws IOException {
			OutHelper.writeLuposCollection(t, out);
		}

		@Override
		public Collection deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposCollection(in);
		}
	}

	public static class INT extends DeSerializerConsideringSubClasses<Integer> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Integer;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Integer>[] getRegisteredClasses() {
			return new Class[] { Integer.class };
		}

		@Override
		public int length(final Integer t) {
			return LengthHelper.lengthLuposInt();
		}

		@Override
		public void serialize(final Integer t, final OutputStream out) throws IOException {
			OutHelper.writeLuposInt(t, out);
		}

		@Override
		public Integer deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposInteger(in);
		}

	}

	public static class LONG extends DeSerializerConsideringSubClasses<Long> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Long;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Long>[] getRegisteredClasses() {
			return new Class[] { Long.class };
		}

		@Override
		public int length(final Long t) {
			return LengthHelper.lengthLuposLong();
		}

		@Override
		public void serialize(final Long t, final OutputStream out) throws IOException {
			OutHelper.writeLuposLong(t, out);
		}

		@Override
		public Long deserialize(final InputStream in) throws IOException,
				URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposLong(in);
		}

	}

	public static class BOOLEAN extends DeSerializerConsideringSubClasses<Boolean> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Boolean;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Boolean>[] getRegisteredClasses() {
			return new Class[] { Boolean.class };
		}

		@Override
		public int length(final Boolean t) {
			return LengthHelper.lengthLuposBoolean();
		}

		@Override
		public void serialize(final Boolean t, final OutputStream out) throws IOException {
			OutHelper.writeLuposBoolean(t, out);
		}

		@Override
		public Boolean deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposBoolean(in);
		}
	}

	@SuppressWarnings("rawtypes")
	public static class MEMORYSORTEDSET extends DeSerializerConsideringSubClasses<TreeSet> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof TreeSet;
		}

		@Override
		public TreeSet deserialize(final LuposObjectInputStream<TreeSet> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposTreeSet();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<TreeSet>[] getRegisteredClasses() {
			return new Class[] { TreeSet.class };
		}

		@Override
		public void serialize(final TreeSet t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposTreeSet(t);
		}

		@Override
		public int length(final TreeSet t) {
			throw new UnsupportedOperationException("TreeSet cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

		@Override
		public void serialize(final TreeSet t, final OutputStream out) throws IOException {
			throw new UnsupportedOperationException("TreeSet cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

		@Override
		public TreeSet deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			throw new UnsupportedOperationException("TreeSet cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

	}

	@SuppressWarnings("rawtypes")
	public static class DBSORTEDSET extends DeSerializerConsideringSubClasses<DBMergeSortedSet> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof DBMergeSortedSet;
		}

		@Override
		public DBMergeSortedSet deserialize(final LuposObjectInputStream<DBMergeSortedSet> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposSortedSet();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<DBMergeSortedSet>[] getRegisteredClasses() {
			return new Class[] { DBMergeSortedSet.class };
		}

		@Override
		public void serialize(final DBMergeSortedSet t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposSortedSet(t);
		}

		@Override
		public int length(final DBMergeSortedSet t) {
			throw new UnsupportedOperationException("DBSortedSet cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

		@Override
		public void serialize(final DBMergeSortedSet t, final OutputStream out) throws IOException {
			throw new UnsupportedOperationException("DBSortedSet cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

		@Override
		public DBMergeSortedSet deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			throw new UnsupportedOperationException("DBSortedSet cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

	}

	@SuppressWarnings("rawtypes")
	public static class SETIMPLEMENTATION extends DeSerializerConsideringSubClasses<SetImplementation> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof SetImplementation;
		}

		@Override
		public SetImplementation deserialize(final LuposObjectInputStream<SetImplementation> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposSetImplementation();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<SetImplementation>[] getRegisteredClasses() {
			return new Class[] { SetImplementation.class };
		}

		@Override
		public void serialize(final SetImplementation t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposSetImplementation(t);
		}

		@Override
		public int length(final SetImplementation t) {
			return LengthHelper.lengthLuposSet(t);
		}

		@Override
		public void serialize(final SetImplementation t, final OutputStream out)
				throws IOException {
			OutHelper.writeLuposSet(t, out);
		}

		@Override
		public SetImplementation deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposSet(in);
		}
	}

	@SuppressWarnings("rawtypes")
	public static class SORTEDMAP extends DeSerializerConsideringSubClasses<SortedMap> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof SortedMap;
		}

		@Override
		public SortedMap deserialize(final LuposObjectInputStream<SortedMap> in) throws IOException, ClassNotFoundException, URISyntaxException {
			try {
				return in.readLuposSortedMap();
			} catch (final URISyntaxException e) {
				throw new IOException("Expected URI, but did not read URI from InputStream!");
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<SortedMap>[] getRegisteredClasses() {
			return new Class[] { SortedMap.class, DBMergeSortedMap.class};
		}

		@Override
		public void serialize(final SortedMap t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposSortedMap(t);
		}

		@Override
		public int length(final SortedMap t) {
			throw new UnsupportedOperationException("SortedMap cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

		@Override
		public void serialize(final SortedMap t, final OutputStream out) throws IOException {
			throw new UnsupportedOperationException("SortedMap cannot be (de-)serialized with lupos i/o because of the comparator!");
		}

		@Override
		public SortedMap deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			throw new UnsupportedOperationException("SortedMap cannot be (de-)serialized with lupos i/o because of the comparator!");
		}
	}

	public static class NODEINPARTITIONTREE extends DeSerializerConsideringSubClasses<NodeInPartitionTree> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof NodeInPartitionTree;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<NodeInPartitionTree>[] getRegisteredClasses() {
			return new Class[] { NodeInPartitionTree.class,
					LeafNodeInPartitionTree.class,
					InnerNodeInPartitionTree.class };
		}

		@Override
		public int length(final NodeInPartitionTree t) {
			return LengthHelper.lengthLuposNodeInPartitionTree(t);
		}

		@Override
		public void serialize(final NodeInPartitionTree t, final OutputStream out) throws IOException {
			OutHelper.writeLuposNodeInPartitionTree(t, out);
		}

		@Override
		public NodeInPartitionTree deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposNodeInPartitionTree(in);
		}
	}

	public static class VARBUCKET extends DeSerializerConsideringSubClasses<VarBucket> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof VarBucket;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<VarBucket>[] getRegisteredClasses() {
			return new Class[] { VarBucket.class };
		}

		@Override
		public int length(final VarBucket t) {
			return LengthHelper.lengthLuposVarBucket(t);
		}

		@Override
		public void serialize(final VarBucket t, final OutputStream out) throws IOException {
			OutHelper.writeLuposVarBucket(t, out);
		}

		@Override
		public VarBucket deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposVarBucket(in);
		}

	}

	public static class VARBUCKETARRAY extends DeSerializerConsideringSubClasses<VarBucket[]> {
		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof VarBucket[];
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<VarBucket[]>[] getRegisteredClasses() {
			return new Class[] { VarBucket[].class };
		}

		@Override
		public int length(final VarBucket[] t) {
			return LengthHelper.lengthLuposVarBucketArray(t);
		}

		@Override
		public void serialize(final VarBucket[] t, final OutputStream out) throws IOException {
			OutHelper.writeLuposVarBucketArray(t, out);
		}

		@Override
		public VarBucket[] deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
			return InputHelper.readLuposVarBucketArray(in);
		}
	}

	/**
	 * Register the standard LUPOSDATE classes for serialization...
	 */
	static {
		addDeSerializer(new TRIPLE(),
				new SUPERLITERAL(), new ENTRY(), new STRING(), new MAPENTRY(),
				new BINDINGS(), new TRIPLEKEY(), new DISKCOLLECTION(),
				new COLLECTION(), new INT(), new LONG(), new BOOLEAN(),
				new MEMORYSORTEDSET(), new DBSORTEDSET(), new SETIMPLEMENTATION(),
				new SORTEDMAP(),
				new NODEINPARTITIONTREE(), new VARBUCKET(),
				new VARBUCKETARRAY());
	}
}
