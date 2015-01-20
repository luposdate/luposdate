/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import java.util.HashMap;

import lupos.io.helper.LengthHelper;
import lupos.io.serializer.BINDINGS;
import lupos.io.serializer.BOOLEAN;
import lupos.io.serializer.COLLECTION;
import lupos.io.serializer.COMPARATOR;
import lupos.io.serializer.DBSORTEDSET;
import lupos.io.serializer.DISKCOLLECTION;
import lupos.io.serializer.ENTRY;
import lupos.io.serializer.INT;
import lupos.io.serializer.LONG;
import lupos.io.serializer.MAPENTRY;
import lupos.io.serializer.MEMORYSORTEDSET;
import lupos.io.serializer.NODEDESERIALIZER;
import lupos.io.serializer.NODEINPARTITIONTREE;
import lupos.io.serializer.PAGEDCOLLECTION;
import lupos.io.serializer.SETIMPLEMENTATION;
import lupos.io.serializer.SORTEDMAP;
import lupos.io.serializer.STRING;
import lupos.io.serializer.SUPERLITERAL;
import lupos.io.serializer.TRIPLE;
import lupos.io.serializer.TRIPLEKEY;
import lupos.io.serializer.VARBUCKET;
import lupos.io.serializer.VARBUCKETARRAY;
import lupos.misc.Tuple;

@SuppressWarnings("rawtypes")
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

	public static void serializeWithoutId(final Object o, final Object previousValue, final OutputStream out) throws IOException {
		if(previousValue==null){
			serializeWithoutId(o, out);
			return;
		}
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					deSerializerForId[id].serialize(o, previousValue, out);
					return;
				}
			}
		} else {
			container.deserializer.serialize(o, previousValue, out);
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

	public static int serializeWithoutId(final Object o, final Object previousValue, final byte[] page, final int offset) throws IOException {
		if(previousValue==null){
			return serializeWithoutId(o, page, offset);
		}
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					return deSerializerForId[id].serialize(o, previousValue, page, offset);
				}
			}
		} else {
			return container.deserializer.serialize(o, previousValue, page, offset);
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

	public static int lengthSerializeWithoutId(final Object o, final Object previousValue) {
		if(previousValue==null){
			return lengthSerializeWithoutId(o);
		}
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id]).instanceofTest(o)) {
					return deSerializerForId[id].length(o, previousValue);
				}
			}
		} else {
			return container.deserializer.length(o, previousValue);
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

	public static void serializeWithId(final Object o, final Object previousValue, final OutputStream out) throws IOException {
		if(previousValue==null){
			serializeWithId(o, out);
			return;
		}
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					out.write(id);
					deSerializerForId[id].serialize(o, previousValue, out);
					return;
				}
			}
		} else {
			out.write(container.id);
			container.deserializer.serialize(o, previousValue, out);
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

	public static int serializeWithId(final Object o, final Object previousValue, final byte[] page, int offset) throws IOException {
		if(previousValue==null){
			return serializeWithId(o, page, offset);
		}
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id])
						.instanceofTest(o)) {
					page[offset++] = (byte) id;
					return deSerializerForId[id].serialize(o, previousValue, page, offset);
				}
			}
		} else {
			page[offset++] = (byte) container.id;
			return container.deserializer.serialize(o, previousValue, page, offset);
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


	public static int lengthSerializeWithId(final Object o, final Object previousValue){
		if(previousValue==null){
			return lengthSerializeWithId(o);
		}
		final Container container = deSerializerForClass.get(o.getClass());
		if (container == null) {
			for (final int id : considerSubClasses) {
				if (((DeSerializerConsideringSubClasses) deSerializerForId[id]).instanceofTest(o)) {
					return LengthHelper.lengthLuposByte() + deSerializerForId[id].length(o, previousValue);
				}
			}
		} else {
			return LengthHelper.lengthLuposByte() + container.deserializer.length(o, previousValue);
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

	public static<T> T deserializeWithoutId(
			final Class<? extends T> registeredClass,
			final LuposObjectInputStream<T> in) throws IOException,
			ClassNotFoundException, URISyntaxException {
		final Container<T> container = deSerializerForClass.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserialize(in);
		} else {
			System.err.println("No DeSerializer for class " + registeredClass + " found! Using Java standard de-/serialization!");
			return (T) in.readObject();
		}
	}

	public static<T> T deserializeWithoutId(
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
			final Class<? extends T> registeredClass, final T previousValue,
			final InputStream in) throws IOException,
			ClassNotFoundException, URISyntaxException {
		if(previousValue==null){
			return deserializeWithoutId(registeredClass, in);
		}
		final Container<T> container = deSerializerForClass.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserialize(previousValue, in);
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

	public static<T> T deserializeWithoutId(
			final Class<? extends T> registeredClass, final T previousValue,
			final byte[] page, final int offset) throws IOException,
			ClassNotFoundException, URISyntaxException {
		if(previousValue==null){
			return deserializeWithoutId(registeredClass, page, offset);
		}
		final Container<T> container = deSerializerForClass.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserialize(previousValue, page, offset);
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

	public static<T> Tuple<T, Integer> deserializeWithoutIdAndNewOffset(
			final Class<? extends T> registeredClass, final T previousValue,
			final byte[] page, final int offset) throws IOException,
			ClassNotFoundException, URISyntaxException {
		if(previousValue==null){
			return deserializeWithoutIdAndNewOffset(registeredClass, page, offset);
		}
		final Container<T> container = deSerializerForClass.get(registeredClass);
		if (container != null) {
			return container.deserializer.deserializeAndNewOffset(previousValue, page, offset);
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

	public static<T> T deserializeWithId(final InputStream in) throws IOException, ClassNotFoundException, URISyntaxException {
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

	public static<T> T deserializeWithId(final T previousValue, final InputStream in) throws IOException, ClassNotFoundException, URISyntaxException {
		if(previousValue==null){
			return deserializeWithId(in);
		}
		final int index = in.read();
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return (T) deSerializerForId[index].deserialize(previousValue, in);
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


	public static<T> T deserializeWithId(final T previousValue, final byte[] page, int offset) throws IOException, ClassNotFoundException, URISyntaxException {
		if(previousValue==null){
			return deserializeWithId(page, offset);
		}
		final int index = 0xFF & page[offset];
		offset++;
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return (T) deSerializerForId[index].deserialize(previousValue, page, offset);
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

	public static<T> Tuple<T, Integer> deserializeWithIdAndNewOffset(final T previousValue, final byte[] page, int offset) throws IOException, ClassNotFoundException, URISyntaxException {
		if(previousValue==null){
			return deserializeWithIdAndNewOffset(page, offset);
		}
		final int index = 0xFF & page[offset];
		offset++;
		if (index == -1) {
			return null;
		}
		if (index >= 0 && index < deSerializerForId.length) {
			return deSerializerForId[index].deserializeAndNewOffset(previousValue, page, offset);
		} else {
			System.err.println("No DeSerializer for id " + index + " found! Returning null!");
			return null;
		}
	}

	/**
	 * Register the standard LUPOSDATE classes for serialization...
	 */
	static {
		addDeSerializer(
				new TRIPLE(),
				new SUPERLITERAL(),
				new ENTRY(),
				new STRING(),
				new MAPENTRY(),
				new BINDINGS(),
				new TRIPLEKEY(),
				new DISKCOLLECTION(),
				new PAGEDCOLLECTION(),
				new COLLECTION(),
				new INT(),
				new LONG(),
				new BOOLEAN(),
				new MEMORYSORTEDSET(),
				new DBSORTEDSET(),
				new SETIMPLEMENTATION(),
				new SORTEDMAP(),
				new NODEINPARTITIONTREE(),
				new VARBUCKET(),
				new VARBUCKETARRAY(),
				new COMPARATOR(),
				new NODEDESERIALIZER());
	}
}
