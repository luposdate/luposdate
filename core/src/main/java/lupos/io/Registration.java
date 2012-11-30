/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.smallerinmemorylargerondisk.CollectionImplementation;
import lupos.engine.operators.multiinput.join.InnerNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.LeafNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.NodeInPartitionTree;
import lupos.optimizations.logical.statistics.VarBucket;

public class Registration {

	public interface DeSerializer<T> {
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
		public void serialize(T t, LuposObjectOutputStream out)
		throws IOException;

		/**
		 * derialization method
		 * 
		 * @param in
		 *            the input stream form which is object is deserialized
		 * @return the deserialized object
		 */
		public T deserialize(LuposObjectInputStream<T> in) throws IOException,
		ClassNotFoundException, URISyntaxException;
	}

	public abstract static class DeSerializerConsideringSubClasses<T>
	implements DeSerializer<T> {
		public boolean exactClass() {
			return false;
		}

		public abstract boolean instanceofTest(Object o);
	}

	public abstract static class DeSerializerExactClass<T> implements
	DeSerializer<T> {
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
			if (!d.exactClass())
				subclasses++;
		}
		final DeSerializer[] zdeSerializerForId = new DeSerializer[deSerializerForId.length
		                                                           + deserializer.length];
		if (zdeSerializerForId.length > 255)
			System.err
			.println("Too many registered (De-)Serializer! Only up to 255 are allowed! De-/Serialization will probably not work correctly!");
		System.arraycopy(deSerializerForId, 0, zdeSerializerForId, 0,
				deSerializerForId.length);
		System.arraycopy(deserializer, 0, zdeSerializerForId,
				deSerializerForId.length, deserializer.length);

		final int[] zconsiderSubClasses = new int[considerSubClasses.length
		                                          + subclasses];
		System.arraycopy(considerSubClasses, 0, zconsiderSubClasses,
				subclasses, considerSubClasses.length);
		int id = deSerializerForId.length;
		int subclassindex = 0;
		for (final DeSerializer d : deserializer) {
			final Container container = new Container(id, d);
			for (final Class c : d.getRegisteredClasses())
				deSerializerForClass.put(c, container);
			if (!d.exactClass()) {
				zconsiderSubClasses[subclassindex] = id;
				subclassindex++;
			}
			id++;
		}
		considerSubClasses = zconsiderSubClasses;
		deSerializerForId = zdeSerializerForId;
	}

	public static void serializeWithoutId(final Object o,
			final LuposObjectOutputStream out) throws IOException {
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

	public static void serializeWithId(final Object o,
			final LuposObjectOutputStream out) throws IOException {
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

	public static void serializeId(final Object o,
			final LuposObjectOutputStream out) throws IOException {
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

	public static void serializeClass(final Class<?> o,
			final LuposObjectOutputStream out) throws IOException {
		final Container container = deSerializerForClass.get(o);
		if (container == null) {
			System.err.println("No DeSerializer for class " + o.getClass()
					+ " found! Using Java standard de-/serialization!");
			out.os.write(deSerializerForId.length);
		} else {
			out.os.write(container.id);
		}
	}

	public static Class<?>[] deserializeId(final LuposObjectInputStream in)
	throws IOException {
		final int index = in.is.read();
		if (index == -1)
			return null;
		if (index >= 0 && index < deSerializerForId.length) {
			return deSerializerForId[index].getRegisteredClasses();
		} else {
			System.err.println("No DeSerializer for id " + index
					+ " found! Using Java standard de-/serialization!");
			return new Class[] { Object.class };
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

	public static <T> T deserializeWithId(final LuposObjectInputStream<T> in)
	throws IOException, ClassNotFoundException, URISyntaxException {
		final int index = in.is.read();
		if (index == -1)
			return null;
		if (index >= 0 && index < deSerializerForId.length) {
			return (T) deSerializerForId[index].deserialize(in);
		} else {
			System.err.println("No DeSerializer for id " + index
					+ " found! Using Java standard de-/serialization!");
			return (T) in.readObject();
		}
	}


	public static class TRIPLE extends
	DeSerializerConsideringSubClasses<Triple> {

		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Triple;
		}

		public Triple deserialize(final LuposObjectInputStream<Triple> in)throws IOException, URISyntaxException {
			return in.readTriple();
		}

		public Class<Triple>[] getRegisteredClasses() {
			return new Class[] { Triple.class };
		}

		public void serialize(final Triple t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposTriple(t);
		}

	}

	public static class SUPERLITERAL extends
	DeSerializerConsideringSubClasses<Literal> {

		@Override
		public boolean instanceofTest(final Object o) {
			return o instanceof Literal;
		}

		public Literal deserialize(final LuposObjectInputStream<Literal> in) throws IOException {
			return LiteralFactory.readLuposLiteral(in);
		}

		public Class<Literal>[] getRegisteredClasses() {
			return new Class[] { Literal.class };
		}

		public void serialize(final Literal t, final LuposObjectOutputStream out) throws IOException {
			LiteralFactory.writeLuposLiteral(t, out);
		}
	}

	public static class ENTRY extends DeSerializerConsideringSubClasses<lupos.datastructures.dbmergesortedds.Entry> {
		public boolean instanceofTest(final Object o) {
			return o instanceof lupos.datastructures.dbmergesortedds.Entry;
		}

		public Entry deserialize(final LuposObjectInputStream<Entry> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposEntry();
		}

		public Class<Entry>[] getRegisteredClasses() {
			return new Class[] { lupos.datastructures.dbmergesortedds.Entry.class };
		}

		public void serialize(final Entry t, final LuposObjectOutputStream out) throws IOException {
			serializeWithId(t.e, out);
		}

	}

	public static class STRING extends DeSerializerConsideringSubClasses<String> {
		public boolean instanceofTest(final Object o) {
			return o instanceof String;
		}

		public String deserialize(final LuposObjectInputStream<String> in) throws IOException {
			return in.readLuposDifferenceString();
		}

		public Class<String>[] getRegisteredClasses() {
			return new Class[] { String.class };
		}

		public void serialize(final String t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposDifferenceString(t);
		}
	}

	public static class MAPENTRY extends
	DeSerializerConsideringSubClasses<lupos.datastructures.dbmergesortedds.MapEntry> {
		public boolean instanceofTest(final Object o) {
			return o instanceof MapEntry;
		}

		public MapEntry deserialize(final LuposObjectInputStream<MapEntry> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposMapEntry();
		}

		public Class<MapEntry>[] getRegisteredClasses() {
			return new Class[] { MapEntry.class };
		}

		public void serialize(final MapEntry t, final LuposObjectOutputStream out) throws IOException {
			serializeId(t.getKey(), out);
			serializeId(t.getValue(), out);
			if (t.getKey() instanceof String
					&& (t.getValue() instanceof Triple)) {
				serializeWithoutId(t.getValue(), out);
				out.writeStringKey((String) t.getKey());
			} else {
				serializeWithoutId(t.getValue(), out);
				serializeWithoutId(t.getKey(), out);
			}
		}

	}

	public static class BINDINGS extends DeSerializerConsideringSubClasses<Bindings> {
		public boolean instanceofTest(final Object o) {
			return o instanceof Bindings;
		}

		public Bindings deserialize(final LuposObjectInputStream<Bindings> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposBindings();
		}

		public Class<Bindings>[] getRegisteredClasses() {
			return new Class[] { Bindings.class, BindingsArray.class,
					BindingsMap.class, BindingsArrayPresortingNumbers.class,
					BindingsArrayVarMinMax.class,
					BindingsArrayReadTriples.class };
		}

		public void serialize(final Bindings t, final LuposObjectOutputStream out) throws IOException {
			if (t instanceof BindingsArray) {
				final BindingsArray ba = (BindingsArray) t;
				final Map<Variable, Integer> hm = BindingsArray.getPosVariables();
				BigInteger usedVars = BigInteger.ZERO;
				BigInteger differentFromPreviousBindings = BigInteger.ZERO;
				BigInteger i = BigInteger.ONE;
				BigInteger TWO = BigInteger.valueOf(2);
				for (final Variable v : hm.keySet()) {
					if (ba.get(v) != null) {
						usedVars = usedVars.add(i);
						if (out.previousBindings == null
								|| out.previousBindings.get(v) == null){
							differentFromPreviousBindings = differentFromPreviousBindings.add(i);
						} else if (ba.get(v) instanceof LazyLiteralOriginalContent) {
							if (!(out.previousBindings.get(v) instanceof LazyLiteralOriginalContent)
									|| ((LazyLiteralOriginalContent) ba.get(v))
									.getCodeOriginalContent() != ((LazyLiteralOriginalContent) out.previousBindings
											.get(v)).getCodeOriginalContent()) {
								differentFromPreviousBindings = differentFromPreviousBindings.add(i);
							}
						} else if (ba.get(v) instanceof LazyLiteral) {
							if (!(out.previousBindings.get(v) instanceof LazyLiteral)
									|| ((LazyLiteral) ba.get(v)).getCode() != ((LazyLiteral) out.previousBindings
											.get(v)).getCode()) {
								differentFromPreviousBindings = differentFromPreviousBindings.add(i);
							}
						} else if (ba.get(v).originalString().compareTo(
								out.previousBindings.get(v).originalString()) != 0) {
							differentFromPreviousBindings = differentFromPreviousBindings.add(i);
						}
					}
					i = i.multiply(TWO);
				}
				out.writeLuposBigInteger(usedVars, hm.size());
				out.writeLuposBigInteger(differentFromPreviousBindings, hm.size());
				for (final Variable v : hm.keySet()) {
					if (ba.get(v) != null) {
						if (out.previousBindings == null
								|| out.previousBindings.get(v) == null)
							serializeWithoutId(ba.get(v), out);
						else if (ba.get(v) instanceof LazyLiteralOriginalContent) {
							if (!(out.previousBindings.get(v) instanceof LazyLiteralOriginalContent)
									|| ((LazyLiteralOriginalContent) ba.get(v))
									.getCodeOriginalContent() != ((LazyLiteralOriginalContent) out.previousBindings
											.get(v)).getCodeOriginalContent()) {
								serializeWithoutId(ba.get(v), out);
							}
						} else if (ba.get(v) instanceof LazyLiteral) {
							if (!(out.previousBindings.get(v) instanceof LazyLiteral)
									|| ((LazyLiteral) ba.get(v)).getCode() != ((LazyLiteral) out.previousBindings
											.get(v)).getCode()) {
								serializeWithoutId(ba.get(v), out);
							}
						} else if (ba.get(v).originalString().compareTo(
								out.previousBindings.get(v).originalString()) != 0) {
							serializeWithoutId(ba.get(v), out);
						}
					}
				}
				out.previousBindings = ba;
				if (t instanceof BindingsArrayReadTriples) {
					out.writeLuposInt(ba.getTriples().size());
					for (final Triple tt : ba.getTriples()) {
						serializeWithoutId(tt, out);
					}
				}
				if (t instanceof BindingsArrayVarMinMax) {
					((BindingsArrayVarMinMax) t).writePresortingNumbers(out);
				}
			} else {
				final Set<Variable> vars = t.getVariableSet();
				out.writeLuposInt(vars.size());
				for (final Variable v : vars) {
					out.writeLuposString(v.getName());
					serializeWithoutId(t.get(v), out);
				}
			}
		}

	}

	public static class TRIPLEKEY extends DeSerializerConsideringSubClasses<TripleKey> {
		public boolean instanceofTest(final Object o) {
			return o instanceof TripleKey;
		}

		public TripleKey deserialize(final LuposObjectInputStream<TripleKey> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposTripleKey();
		}

		public Class<TripleKey>[] getRegisteredClasses() {
			return new Class[] { TripleKey.class };
		}

		public void serialize(final TripleKey t, final LuposObjectOutputStream out) throws IOException {
			out.os.write(t.getTripleComparator().getBytePattern());
			if (out.lastSubject == null) {
				LiteralFactory.writeLuposLiteral(t.getTriple().getSubject(),
						out);
				LiteralFactory.writeLuposLiteral(t.getTriple().getPredicate(),
						out);
				LiteralFactory
				.writeLuposLiteral(t.getTriple().getObject(), out);
				out.lastSubject = null;
				out.lastPredicate = null;
				out.lastObject = null;
			}
		}
	}

	public static class DISKCOLLECTION extends DeSerializerConsideringSubClasses<DiskCollection> {
		public boolean instanceofTest(final Object o) {
			return o instanceof DiskCollection;
		}

		public DiskCollection deserialize(final LuposObjectInputStream<DiskCollection> in)
		throws IOException, ClassNotFoundException, URISyntaxException {
			return DiskCollection.readAndCreateLuposObject(in);
		}

		public Class<DiskCollection>[] getRegisteredClasses() {
			return new Class[] { DiskCollection.class };
		}

		public void serialize(final DiskCollection t, final LuposObjectOutputStream out) throws IOException {
			t.writeLuposObject(out);
		}
	}

	public static class COLLECTION extends DeSerializerConsideringSubClasses<Collection> {
		public boolean instanceofTest(final Object o) {
			return o instanceof Collection;
		}

		public Collection deserialize(final LuposObjectInputStream<Collection> in) throws IOException, ClassNotFoundException, URISyntaxException {
			final int size = in.is.read();
			if (size == 255)
				return DiskCollection.readAndCreateLuposObject(in);
			else {
				final LinkedList ll = new LinkedList();
				final Class type = deserializeId(in)[0];
				for (int i = 0; i < size; i++) {
					try {
						ll.add(deserializeWithoutId(type, in));
					} catch (final URISyntaxException e) {
						e.printStackTrace();
						throw new IOException(e.getMessage());
					}
				}
				return ll;
			}
		}

		public Class<Collection>[] getRegisteredClasses() {
			return new Class[] { Collection.class,
					CollectionImplementation.class, LinkedList.class,
					ArrayList.class };
		}

		public void serialize(final Collection t, final LuposObjectOutputStream out) throws IOException {
			if (t.size() > 200) {
				out.os.write(255);
				final DiskCollection dc;
				if (t.size() > 0) {
					dc = new DiskCollection(t.iterator().next().getClass());
				} else
					dc = new DiskCollection(Object.class);
				dc.addAll(t);
				dc.writeLuposObject(out);
			} else {
				out.os.write(t.size());
				if (t.size() > 0) {
					serializeId(t.iterator().next(), out);
					for (final Object o : t) {
						serializeWithoutId(o, out);
					}
				}
			}
		}
	}

	public static class INT extends DeSerializerConsideringSubClasses<Integer> {
		public boolean instanceofTest(final Object o) {
			return o instanceof Integer;
		}

		public Integer deserialize(final LuposObjectInputStream<Integer> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposInteger();
		}

		public Class<Integer>[] getRegisteredClasses() {
			return new Class[] { Integer.class };
		}

		public void serialize(final Integer t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposInt(t);
		}

	}

	public static class LONG extends DeSerializerConsideringSubClasses<Long> {
		public boolean instanceofTest(final Object o) {
			return o instanceof Long;
		}

		public Long deserialize(final LuposObjectInputStream<Long> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposLong();
		}

		public Class<Long>[] getRegisteredClasses() {
			return new Class[] { Long.class };
		}

		public void serialize(final Long t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposLong(t);
		}

	}

	public static class BOOLEAN extends DeSerializerConsideringSubClasses<Boolean> {
		public boolean instanceofTest(final Object o) {
			return o instanceof Boolean;
		}

		public Boolean deserialize(final LuposObjectInputStream<Boolean> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposBoolean();
		}

		public Class<Boolean>[] getRegisteredClasses() {
			return new Class[] { Boolean.class };
		}

		public void serialize(final Boolean t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposBoolean(t);
		}

	}

	public static class MEMORYSORTEDSET extends DeSerializerConsideringSubClasses<TreeSet> {
		public boolean instanceofTest(final Object o) {
			return o instanceof TreeSet;
		}

		public TreeSet deserialize(final LuposObjectInputStream<TreeSet> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposTreeSet();
		}

		public Class<TreeSet>[] getRegisteredClasses() {
			return new Class[] { TreeSet.class };
		}

		public void serialize(final TreeSet t, final LuposObjectOutputStream out) throws IOException {
			out.writeObject(t.comparator());
			out.writeLuposInt(t.size());
			if (t.size() > 0)
				serializeId(t.iterator().next(), out);
			for (final Object o : t) {
				serializeWithoutId(o, out);
			}
		}

	}

	public static class DBSORTEDSET extends DeSerializerConsideringSubClasses<DBMergeSortedSet> {
		public boolean instanceofTest(final Object o) {
			return o instanceof DBMergeSortedSet;
		}

		public DBMergeSortedSet deserialize(final LuposObjectInputStream<DBMergeSortedSet> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposSortedSet();
		}

		public Class<DBMergeSortedSet>[] getRegisteredClasses() {
			return new Class[] { DBMergeSortedSet.class };
		}

		public void serialize(final DBMergeSortedSet t, final LuposObjectOutputStream out) throws IOException {
			out.writeObject(t.comparator());
			out.writeLuposInt(t.size());
			if (t.size() > 0)
				serializeId(t.iterator().next(), out);
			for (final Object o : t) {
				serializeWithoutId(o, out);
			}
		}

	}

	public static class SORTEDMAP extends DeSerializerConsideringSubClasses<SortedMap> {
		public boolean instanceofTest(final Object o) {
			return o instanceof SortedMap;
		}

		public SortedMap deserialize(final LuposObjectInputStream<SortedMap> in) throws IOException, ClassNotFoundException, URISyntaxException {
			try {
				return in.readLuposOptimizedDBBPTreeGeneration();
			} catch (final URISyntaxException e) {
				throw new IOException("Expected URI, but did not read URI from InputStream!");
			}
		}

		public Class<SortedMap>[] getRegisteredClasses() {
			return new Class[] { SortedMap.class, DBMergeSortedMap.class};
		}

		public void serialize(final SortedMap t, final LuposObjectOutputStream out) throws IOException {
				out.writeLuposByte((byte) 1);
				out.writeObject(t.comparator());
				out.writeLuposInt(t.size());
				if (t.size() == 0)
					return;
				boolean flag = true;
				for (final Object mapentry : t.entrySet()) {
					final Object value = ((Map.Entry<Object, Object>) mapentry)
					.getValue();
					final Object key = ((Map.Entry<Object, Object>) mapentry)
					.getKey();

					if (flag) {
						flag = false;
						serializeId(key, out);
						serializeId(value, out);
					}

					serializeWithoutId(value, out);
					serializeWithoutId(key, out);
				}
		}
	}

	public static class NODEINPARTITIONTREE extends DeSerializerConsideringSubClasses<NodeInPartitionTree> {
		public boolean instanceofTest(final Object o) {
			return o instanceof NodeInPartitionTree;
		}

		public NodeInPartitionTree deserialize(final LuposObjectInputStream<NodeInPartitionTree> in) throws IOException, ClassNotFoundException, URISyntaxException {
			final byte type = in.readLuposByte();
			switch (type) {
			case 1:
				return new LeafNodeInPartitionTree(new QueryResult(
						DiskCollection.readAndCreateLuposObject(in)));
			default:
			case 2:
				return new InnerNodeInPartitionTree(DiskCollection
						.readAndCreateLuposObject(in));
			}
		}

		public Class<NodeInPartitionTree>[] getRegisteredClasses() {
			return new Class[] { NodeInPartitionTree.class,
					LeafNodeInPartitionTree.class,
					InnerNodeInPartitionTree.class };
		}

		public void serialize(final NodeInPartitionTree t, final LuposObjectOutputStream out) throws IOException {
			if (t instanceof LeafNodeInPartitionTree) {
				out.writeLuposByte((byte) 1);
				serializeWithoutId(((LeafNodeInPartitionTree) t).partition
						.getCollection(), out);
			} else if (t instanceof InnerNodeInPartitionTree) {
				out.writeLuposByte((byte) 2);
				serializeWithoutId(((InnerNodeInPartitionTree) t).nodes, out);
			}
		}
	}

	public static class VARBUCKET extends DeSerializerConsideringSubClasses<VarBucket> {
		public boolean instanceofTest(final Object o) {
			return o instanceof VarBucket;
		}

		public VarBucket deserialize(final LuposObjectInputStream<VarBucket> in) throws IOException, ClassNotFoundException, URISyntaxException {
			return in.readLuposVarBucket();
		}

		public Class<VarBucket>[] getRegisteredClasses() {
			return new Class[] { VarBucket.class };
		}

		public void serialize(final VarBucket t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposVarBucket(t);
		}

	}

	public static class VARBUCKETARRAY extends DeSerializerConsideringSubClasses<VarBucket[]> {
		public boolean instanceofTest(final Object o) {
			return o instanceof VarBucket[];
		}

		public VarBucket[] deserialize(final LuposObjectInputStream<VarBucket[]> in)
		throws IOException, ClassNotFoundException, URISyntaxException {
			final byte number = in.readLuposByte();
			final byte nulls = in.readLuposByte();
			final VarBucket[] vba = new VarBucket[number];
			int counter = 1;
			for (int i = 0; i < number; i++) {
				if ((nulls / counter) % 2 == 0)
					vba[i] = in.readLuposVarBucket();
				counter *= 2;
			}
			return vba;
		}

		public Class<VarBucket[]>[] getRegisteredClasses() {
			return new Class[] { VarBucket[].class };
		}

		public void serialize(final VarBucket[] t, final LuposObjectOutputStream out) throws IOException {
			out.writeLuposByte((byte) t.length);
			int nulls = 0;
			int counter = 1;
			for (final VarBucket vb : t) {
				if (vb == null)
					nulls += counter;
				counter *= 2;
			}
			out.writeLuposByte((byte) nulls);
			for (final VarBucket vb : t) {
				if (vb != null)
					out.writeLuposVarBucket(vb);
			}
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
				new MEMORYSORTEDSET(), new DBSORTEDSET(), new SORTEDMAP(),
				new NODEINPARTITIONTREE(), new VARBUCKET(),
				new VARBUCKETARRAY());
	}
}
