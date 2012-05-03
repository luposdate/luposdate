package lupos.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayPresortingNumbers;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.Entry;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.LiteralFactory.MapType;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;

public class LuposObjectInputStream<E> extends ObjectInputStream {

	public static final int LITERAL = 0;
	public static final int URILITERAL = 1;
	public static final int LANGUAGETAGGEDLITERAL = 2;
	public static final int TYPEDLITERAL = 3;
	public static final int ANONYMOUSLITERAL = 4;
	public static final int LAZYLITERAL = 5;
	public static final int LAZYLITERALMATERIALIZED = 6;
	public static final int LAZYLITERALORIGINALCONTENT = 7;
	public static final int LAZYLITERALORIGINALCONTENTMATERIALIZED = 8;
	public static final int PLAINSTRINGLITERAL = 9;

	// public static final int STRING = 0;
	// public static final int SORTEDTRIPLEELEMENT = 1;
	// // public static final int COMMONPREFIXESORTEDTRIPLEELEMENT = 2;
	// public static final int SIMPLESORTEDTRIPLEELEMENT = 3;
	// // public static final int COMMONPREFIXSIMPLESORTEDTRIPLEELEMENT = 4;
	// public static final int TRIPLE = 5;
	// public static final int SUPERLITERAL = 6;
	// public static final int ENTRY = 7;
	// public static final int BOOLEAN = 8;
	// public static final int INT = 9;
	// public static final int LONG = 10;
	// public static final int BINDINGS = 11;
	// public static final int DBSORTEDSET = 12;
	// public static final int MEMORYSORTEDSET = 13;
	// public static final int OPTIMIZEDDBBPTREEGENERATION = 14;
	// public static final int DBBPTREE = 15;
	// public static final int MAPENTRY = 16;
	// public static final int TRIPLEKEY = 17;
	// public static final int DBMERGESORTEDMAP = 18;
	// public static final int OBJECT = 19;
	// public static final int NODEINPARTITIONTREE = 20;
	// public static final int LITERALSTRING = 21;

	protected InputStream is;
	protected Class<? extends E> classOfElements;

	public LuposObjectInputStream() throws IOException {
	}

	public LuposObjectInputStream(final InputStream arg0,
			final Class<? extends E> classOfElements) throws IOException,
			EOFException {
		super(arg0);
		is = arg0;
		this.classOfElements = classOfElements;
	}

	public E readLuposObject() throws IOException, ClassNotFoundException,
			URISyntaxException {
		return Registration.deserializeWithoutId(classOfElements, this);
		// // if (classOfElements ==
		// CommonPrefixSimpleSortedTripleElement.class) {
		// // try {
		// // return (E) readCommonPrefixSimpleSortedTripleElement();
		// // } catch (final URISyntaxException e) {
		// // throw new IOException(
		// // "Expected URI in InputStream, but it is not an URI!");
		// // }
		// // } else
		// if (classOfElements == SimpleSortedTripleElement.class) {
		// try {
		// return (E) readSimpleSortedTripleElement();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI in InputStream, but it is not an URI!");
		// }
		// // } else if (classOfElements ==
		// // CommonPrefixeSortedTripleElement.class) {
		// // try {
		// // return (E) readCommonPrefixeSortedTripleElement();
		// // } catch (final URISyntaxException e) {
		// // throw new IOException(
		// // "Expected URI in InputStream, but it is not an URI!");
		// // }
		// } else if (classOfElements == SortedTripleElement.class) {
		// try {
		// return (E) readSortedTripleElement();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI in InputStream, but it is not an URI!");
		// }
		// } else if (classOfElements == Triple.class) {
		// try {
		// return (E) readTriple();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI in InputStream, but it is not an URI!");
		// }
		// } else if (classOfElements == Literal.class) {
		// return (E) LiteralFactory.readLuposLiteral(this);
		// } else if (classOfElements == String.class) {
		// return (E) readLuposString();
		// } else if (classOfElements == Integer.class) {
		// return (E) readLuposInteger();
		// } else if (classOfElements == Long.class) {
		// return (E) readLuposLong();
		// } else if (classOfElements ==
		// lupos.datastructures.dbmergesortedds.MapEntry.class) {
		// return (E) readLuposMapEntry();
		// } else if (classOfElements == Bindings.class
		// || classOfElements == BindingsArray.class
		// || classOfElements == BindingsMap.class
		// || classOfElements == BindingsArrayPresortingNumbers.class
		// || classOfElements == BindingsArrayVarMinMax.class
		// || classOfElements == BindingsArrayReadTriples.class) {
		// return (E) readLuposBindings();
		// } else if (classOfElements == TripleKey.class) {
		// return (E) readLuposTripleKey();
		// } else if (classOfElements == Collection.class
		// || classOfElements == CollectionImplementation.class
		// || classOfElements == DiskCollection.class
		// || classOfElements == LinkedList.class
		// || classOfElements == ArrayList.class) {
		// final int size = is.read();
		// if (size == 255)
		// return (E) DiskCollection.readAndCreateLuposObject(this);
		// else {
		// final LinkedList ll = new LinkedList();
		// final int type = is.read();
		// for (int i = 0; i < size; i++) {
		// try {
		// ll.add(readType(type));
		// } catch (final URISyntaxException e) {
		// e.printStackTrace();
		// throw new IOException(e.getMessage());
		// }
		// }
		// return (E) ll;
		// }
		// } else if (classOfElements == TreeSet.class) {
		// return readLuposTreeSet();
		// } else if (classOfElements == DBMergeSortedSet.class) {
		// return readLuposSortedSet();
		// } else if (classOfElements == OptimizedDBBPTreeGeneration.class
		// || classOfElements == DBBPTree.class
		// || classOfElements == DBMergeSortedMap.class
		// || classOfElements == SortedMapSecondLevel.class) {
		// try {
		// return readLuposOptimizedDBBPTreeGeneration();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI, but did not read URI from InputStream!");
		// }
		// } else if (classOfElements == NodeInPartitionTree.class
		// || classOfElements == LeafNodeInPartitionTree.class
		// || classOfElements == InnerNodeInPartitionTree.class) {
		// final byte type = readLuposByte();
		// switch (type) {
		// case 1:
		// return (E) new LeafNodeInPartitionTree(new QueryResult(
		// DiskCollection.readAndCreateLuposObject(this)));
		// default:
		// case 2:
		// return (E) new InnerNodeInPartitionTree(DiskCollection
		// .readAndCreateLuposObject(this));
		// }
		// } else if (classOfElements == VarBucket[].class) {
		// final byte number = readLuposByte();
		// final byte nulls = readLuposByte();
		// final VarBucket[] vba = new VarBucket[number];
		// int counter = 1;
		// for (int i = 0; i < number; i++) {
		// if ((nulls / counter) % 2 == 0)
		// vba[i] = this.readLuposVarBucket();
		// counter *= 2;
		// }
		// return (E) vba;
		// } else if (classOfElements == VarBucket.class) {
		// return (E) this.readLuposVarBucket();
		// } else
		// return (E) readObject();
	}

	/*
	 * public Object readLazyCollection() throws IOException{ // "lazy loading":
	 * this requires that the collection is the last object in the file! final
	 * int size=readLuposInt(); final int type=(size>0)?is.read():-1; return new
	 * Collection<E>(){ >>>>>>> 1.5 public boolean add(final Object o) { throw
	 * new UnsupportedOperationException( "This operation is unsupported!"); }
	 * 
	 * public boolean addAll(final Collection c) { throw new
	 * UnsupportedOperationException( "This operation is unsupported!"); }
	 * 
	 * public void clear() { throw new UnsupportedOperationException(
	 * "This operation is unsupported!"); }
	 * 
	 * public boolean contains(final Object o) { for (final Object co : this) {
	 * if (co.equals(o)) return true; } return false; }
	 * 
	 * public boolean containsAll(final Collection c) { for (final Object o : c)
	 * { if (!contains(o)) return false; } return true; }
	 * 
	 * public boolean isEmpty() { return size > 0; } <<<<<<<
	 * LuposObjectInputStream.java
	 * 
	 * public Iterator iterator() { return new Iterator() { int current = 0;
	 * 
	 * ======= public Iterator<E> iterator() { return new Iterator<E>(){ int
	 * current=0; >>>>>>> 1.5 public boolean hasNext() { return current < size;
	 * } <<<<<<< LuposObjectInputStream.java
	 * 
	 * public Object next() { if (!hasNext()) ======= public E next() {
	 * if(!hasNext()) >>>>>>> 1.5 return null; try { return (E) readType(type);
	 * } catch (final IOException e) { e.printStackTrace(); } catch (final
	 * URISyntaxException e) { e.printStackTrace(); } catch (final
	 * ClassNotFoundException e) { e.printStackTrace(); } return null; }
	 * 
	 * public void remove() { throw new UnsupportedOperationException(
	 * "This operation is unsupported!"); } }; }
	 * 
	 * public boolean remove(final Object o) { throw new
	 * UnsupportedOperationException( "This operation is unsupported!"); }
	 * 
	 * public boolean removeAll(final Collection c) { throw new
	 * UnsupportedOperationException( "This operation is unsupported!"); }
	 * 
	 * public boolean retainAll(final Collection c) { throw new
	 * UnsupportedOperationException( "This operation is unsupported!"); }
	 * 
	 * public int size() { return size; }
	 * 
	 * public Object[] toArray() { throw new UnsupportedOperationException(
	 * "This operation is unsupported!"); } <<<<<<< LuposObjectInputStream.java
	 * 
	 * public Object[] toArray(final Object[] a) { throw new
	 * UnsupportedOperationException( "This operation is unsupported!"); } }; }
	 * 
	 * public Object readLuposObject(final Class<?> classOfElements) throws
	 * IOException, ClassNotFoundException { if (classOfElements ==
	 * SortedTripleElement.class) { ======= public E[] toArray(final Object[] a)
	 * { throw new
	 * UnsupportedOperationException("This operation is unsupported!"); } }; }
	 */

	public <TT> TT readLuposObject(final Class classOfElements)
			throws IOException, ClassNotFoundException, URISyntaxException {
		return (TT) Registration.deserializeWithoutId(classOfElements, this);
		// // if (classOfElements ==
		// CommonPrefixSimpleSortedTripleElement.class) {
		// // try {
		// // return readCommonPrefixSimpleSortedTripleElement();
		// // } catch (final URISyntaxException e) {
		// // throw new IOException(
		// // "Expected URI in InputStream, but it is not an URI!");
		// // }
		// // } else
		// if (classOfElements == SimpleSortedTripleElement.class) {
		// try {
		// return readSimpleSortedTripleElement();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI in InputStream, but it is not an URI!");
		// }
		// // } else if (classOfElements ==
		// // CommonPrefixeSortedTripleElement.class) {
		// // try {
		// // return readCommonPrefixeSortedTripleElement();
		// // } catch (final URISyntaxException e) {
		// // throw new IOException(
		// // "Expected URI in InputStream, but it is not an URI!");
		// // }
		// } else if (classOfElements == SortedTripleElement.class) {
		// try {
		// return readSortedTripleElement();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI in InputStream, but it is not an URI!");
		// }
		// } else if (classOfElements == Triple.class) {
		// try {
		// return readTriple();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI in InputStream, but it is not an URI!");
		// }
		// } else if (classOfElements == Literal.class) {
		// return LiteralFactory.readLuposLiteral(this);
		// } else if (classOfElements == String.class) {
		// return readLuposString();
		// } else if (classOfElements == Integer.class) {
		// return readLuposInteger();
		// } else if (classOfElements == Long.class) {
		// return readLuposLong();
		// } else if (classOfElements ==
		// lupos.datastructures.dbmergesortedds.MapEntry.class) {
		// return readLuposMapEntry();
		// } else if (classOfElements == Bindings.class
		// || classOfElements == BindingsArray.class
		// || classOfElements == BindingsMap.class
		// || classOfElements == BindingsArrayPresortingNumbers.class
		// || classOfElements == BindingsArrayVarMinMax.class
		// || classOfElements == BindingsArrayReadTriples.class) {
		// return readLuposBindings();
		// } else if (classOfElements == TripleKey.class) {
		// return readLuposTripleKey();
		// } else if (classOfElements == Collection.class
		// || classOfElements == CollectionImplementation.class
		// || classOfElements == DiskCollection.class
		// || classOfElements == LinkedList.class
		// || classOfElements == ArrayList.class) {
		// final int size = is.read();
		// if (size == 255)
		// return DiskCollection.readAndCreateLuposObject(this);
		// else {
		// final LinkedList ll = new LinkedList();
		// final int type = is.read();
		// for (int i = 0; i < size; i++) {
		// try {
		// ll.add(readType(type));
		// } catch (final URISyntaxException e) {
		// e.printStackTrace();
		// throw new IOException(e.getMessage());
		// }
		// }
		// return ll;
		// }
		// } else if (classOfElements == TreeSet.class) {
		// return readLuposTreeSet();
		// } else if (classOfElements == DBMergeSortedSet.class) {
		// return readLuposSortedSet();
		// } else if (classOfElements == OptimizedDBBPTreeGeneration.class
		// || classOfElements == DBBPTree.class
		// || classOfElements == DBMergeSortedMap.class
		// || classOfElements == SortedMapSecondLevel.class) {
		// try {
		// return readLuposOptimizedDBBPTreeGeneration();
		// } catch (final URISyntaxException e) {
		// throw new IOException(
		// "Expected URI, but did not read URI from InputStream!");
		// }
		// } else if (classOfElements == NodeInPartitionTree.class
		// || classOfElements == LeafNodeInPartitionTree.class
		// || classOfElements == InnerNodeInPartitionTree.class) {
		// final byte type = readLuposByte();
		// switch (type) {
		// case 1:
		// return new LeafNodeInPartitionTree(new QueryResult(
		// DiskCollection.readAndCreateLuposObject(this)));
		// default:
		// case 2:
		// return new InnerNodeInPartitionTree(DiskCollection
		// .readAndCreateLuposObject(this));
		// }
		// } else if (classOfElements == VarBucket[].class) {
		// final byte number = readLuposByte();
		// final byte nulls = readLuposByte();
		// final VarBucket[] vba = new VarBucket[number];
		// int counter = 1;
		// for (int i = 0; i < number; i++) {
		// if ((nulls / counter) % 2 == 0)
		// vba[i] = this.readLuposVarBucket();
		// counter *= 2;
		// }
		// return vba;
		// } else if (classOfElements == VarBucket.class) {
		// return this.readLuposVarBucket();
		// } else
		// return readObject();
	}

	// public static Class<?> getClassFromByte(final byte b) {
	// switch (b) {
	// case STRING:
	// return String.class;
	// case SORTEDTRIPLEELEMENT:
	// return SortedTripleElement.class;
	// case SIMPLESORTEDTRIPLEELEMENT:
	// return SimpleSortedTripleElement.class;
	// case TRIPLE:
	// return Triple.class;
	// case SUPERLITERAL:
	// return Literal.class;
	// case ENTRY:
	// return lupos.datastructures.dbmergesortedds.Entry.class;
	// case BOOLEAN:
	// return Boolean.class;
	// case INT:
	// return Integer.class;
	// case LONG:
	// return Long.class;
	// case BINDINGS:
	// return Bindings.class;
	// case DBSORTEDSET:
	// return DBMergeSortedSet.class;
	// case MEMORYSORTEDSET:
	// return TreeSet.class;
	// case OPTIMIZEDDBBPTREEGENERATION:
	// return OptimizedDBBPTreeGeneration.class;
	// case DBBPTREE:
	// return DBBPTree.class;
	// case MAPENTRY:
	// return MapEntry.class;
	// case TRIPLEKEY:
	// return TripleKey.class;
	// case DBMERGESORTEDMAP:
	// return DBMergeSortedMap.class;
	// case OBJECT:
	// return Object.class;
	// case NODEINPARTITIONTREE:
	// return NodeInPartitionTree.class;
	// case LITERALSTRING:
	// return LiteralString.class;
	// }
	// return null;
	// }

	public lupos.datastructures.dbmergesortedds.MapEntry<Object, Object> readLuposMapEntry()
			throws IOException, ClassNotFoundException {
		final Class type1 = Registration.deserializeId(this)[0];
		if (type1 == null)
			return null;
		final Class type2 = Registration.deserializeId(this)[0];
		final Object key, value;
		try {
			if (type1 == String.class
					&& (type2 == Triple.class
					)) {
				value = Registration.deserializeWithoutId(type2, this);
				final int compressed = is.read();
				switch (compressed) {
				case 1:
					key = new String(lastSubject.toString()
							+ lastPredicate.toString() + lastObject.toString());
					break;
				case 2:
					key = new String(lastSubject.toString()
							+ lastObject.toString() + lastPredicate.toString());
					break;
				case 3:
					key = new String(lastPredicate.toString()
							+ lastSubject.toString() + lastObject.toString());
					break;
				case 4:
					key = new String(lastPredicate.toString()
							+ lastObject.toString() + lastSubject.toString());
					break;
				case 5:
					key = new String(lastObject.toString()
							+ lastSubject.toString() + lastPredicate.toString());
					break;
				case 6:
					key = new String(lastObject.toString()
							+ lastPredicate.toString() + lastSubject.toString());
					break;
				default:
					key = Registration.deserializeWithoutId(type1, this);
				}
			} else {
				value = Registration.deserializeWithoutId(type2, this);
				key = Registration.deserializeWithoutId(type1, this);
			}
		} catch (final URISyntaxException e) {
			throw new IOException(
					"Expected URI, but did not read URI from InputStream!");
		}
		return new lupos.datastructures.dbmergesortedds.MapEntry<Object, Object>(
				key, value);
	}

	public VarBucket readLuposVarBucket() throws IOException {
		final VarBucket vb = new VarBucket();
		final int size = readLuposInt();
		final byte minMax = readLuposByte();
		if (minMax >= 2)
			vb.minimum = LiteralFactory.readLuposLiteral(this);
		if (minMax % 2 == 1)
			vb.maximum = LiteralFactory.readLuposLiteral(this);
		for (int i = 0; i < size; i++) {
			final lupos.optimizations.logical.statistics.Entry entry = new lupos.optimizations.logical.statistics.Entry();
			entry.distinctLiterals = Double.longBitsToDouble(readLuposLong());
			entry.selectivity = Double.longBitsToDouble(readLuposLong());
			entry.literal = LiteralFactory.readLuposLiteral(this);
			vb.selectivityOfInterval.add(entry);
		}
		return vb;
	}

	public E readLuposOptimizedDBBPTreeGeneration() throws IOException,
			URISyntaxException, ClassNotFoundException {
		final int type = readLuposByte();
		if (type == 1) {
			return readLuposDBMergeSortedMapBasic();
		} else {
			System.err.println("LuposObjectInputStream: Not supported!");
			return null;
		}
	}

	private final static int memoryLimit = 10000;

	public E readLuposDBMergeSortedMap() throws IOException,
			ClassNotFoundException {
		final int type = readLuposByte();
		if (type == 1) {
			return readLuposDBMergeSortedMapBasic();
		} else {
			System.err.println("LuposObjectInputStream: Not supported!");
			return null;
		} 
	}

	private E readLuposDBMergeSortedMapBasic() throws IOException,
			ClassNotFoundException {
		final Comparator comparator = (Comparator) readObject();
		final int size = readLuposInt();
		if (size < 0)
			return null;
		final DBMergeSortedMap ms = new DBMergeSortedMap(2, comparator, null);
		if (size == 0)
			return (E) ms;
		final Class typeKey = Registration.deserializeId(this)[0];
		final Class typeValue = Registration.deserializeId(this)[0];
		for (int i = 0; i < size; i++) {
			Object value;
			Object key;
			try {
				value = Registration.deserializeWithoutId(typeValue, this);
				key = Registration.deserializeWithoutId(typeKey, this);
				ms.put(key, value);
			} catch (final URISyntaxException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		return (E) ms;
	}

	public E readLuposSortedSet() throws IOException, ClassNotFoundException {
		final Comparator comparator = (Comparator) readObject();
		final int size = readLuposInt();
		if (size < 0)
			return null;
		if (size == 0)
			return (E) new DBMergeSortedSet(2, comparator, null);
		final Class type = Registration.deserializeId(this)[0];
		final SortedSet ms;
		if (size < memoryLimit) {
			if (type == Triple.class)
				ms = new TreeSet<Triple>(comparator);
			else
				ms = new TreeSet(comparator);
		} else {
			if (type == Triple.class)
				ms = new DBMergeSortedSet(2, comparator, Triple.class);
			else
				ms = new DBMergeSortedSet(2, comparator, null);
		}
		for (int i = 0; i < size; i++) {
			try {
				ms.add(Registration.deserializeWithoutId(type, this));
			} catch (final URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		}
		return (E) ms;
	}

	public E readLuposTreeSet() throws IOException, ClassNotFoundException {
		final Comparator comparator = (Comparator) readObject();
		final int size = readLuposInt();
		if (size == 0)
			return (E) new TreeSet(comparator);
		final Class c = Registration.deserializeId(this)[0];
		final TreeSet ts = new TreeSet(comparator);
		for (int i = 0; i < size; i++) {
			try {
				ts.add(Registration.deserializeWithoutId(c, this));
			} catch (final URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		}
		return (E) ts;
	}

	public Bindings readLuposBindings() throws IOException {
		if (Bindings.instanceClass == BindingsMap.class) {
			final Bindings b = Bindings.createNewInstance();
			final int number = readLuposInt();
			if (number < 0)
				return null;
			for (int i = 0; i < number; i++) {
				final String varName = readLuposString();
				final Variable v = new Variable(varName);
				final Literal l = readLiteral();
				b.add(v, l);
			}
			return b;
		} else {
			final int usedVars = readLuposInt();
			if (usedVars < 0)
				return null;
			final int differentFromPreviousBindings = readLuposInt();
			final Map<Variable, Integer> hm = BindingsArray.getPosVariables();
			final Bindings b = Bindings.createNewInstance();
			int i = 1;
			for (final Variable v : hm.keySet()) {
				if ((usedVars / i) % 2 == 1) {
					if (previousBindings == null
							|| (differentFromPreviousBindings / i) % 2 == 1) {
						Literal lit;
						lit = readLiteral();
						b.add(v, lit);
					} else {
						b.add(v, previousBindings.get(v));
					}
				}
				i *= 2;
			}
			previousBindings = b;
			if (b instanceof BindingsArrayReadTriples) {
				final int number = readLuposInt();
				if (number == 0)
					return b;
				if (number < 0)
					return null;
				final int tripleType = is.read();
				for (int j = 0; j < number; j++) {
					Triple t;
					if (tripleType == 1) {
						try {
							t = readTriple();
						} catch (final URISyntaxException e) {
							System.out.println(e);
							e.printStackTrace();
							return b;
						}
						b.addTriple(t);
					} 
				}
			}
			if (b instanceof BindingsArrayVarMinMax) {
				((BindingsArrayVarMinMax) b).readPresortingNumbers(this);
			}
			return b;
		}
	}

	protected static int triplePatternID = 0;
	protected static HashMap<Integer, TriplePattern> triplePatternHashMap = new HashMap<Integer, TriplePattern>();
	protected static HashMap<TriplePattern, Integer> triplePatternHashMapID = new HashMap<TriplePattern, Integer>();

	public TriplePattern readLuposTriplePattern() throws IOException {
		final int id = readLuposByte();
		return triplePatternHashMap.get(id);
	}

	private Bindings previousBindings = null;

	private Literal lastSubject = null, lastPredicate = null,
			lastObject = null;

	public Triple readTriple() throws IOException, URISyntaxException {
		final int diff = is.read();
		if (diff < 0)
			return null;
		final Literal subject = (diff % 2 == 1) ? readLiteral() : lastSubject;
		final Literal predicate = ((diff / 2) % 2 == 1) ? readLiteral()
				: lastPredicate;
		final Literal object = ((diff / 4) % 2 == 1) ? readLiteral()
				: lastObject;
		if (subject == null || predicate == null || object == null)
			return null;
		lastSubject = subject;
		lastPredicate = predicate;
		lastObject = object;
		return new Triple(subject, predicate, object);
	}

	public Literal readLiteral() throws IOException {
		return LiteralFactory.readLuposLiteral(this);
	}

	public String readLuposString() throws IOException {
		int firstByte = is.read();
		if (firstByte < 0)
			return null;
		boolean flag;
		if (firstByte < 32) {
			flag = true;
		} else {
			flag = false;
			firstByte -= 32;
		}
		Integer length;
		switch (firstByte) {
		case 0:
			length = readLuposInteger1Byte();
			break;
		case 1:
			length = readLuposInteger2Bytes();
			break;
		case 2:
			length = readLuposInteger3Bytes();
			break;
		default:
		case 3:
			length = readLuposInt();
			break;
		}
		if (length == null || length < 0)
			return null;
		final byte[] ba = new byte[flag ? length : length * 2];
		is.read(ba);
		if (flag) {
			return new String(ba);
		} else {
			final ByteBuffer buf = ByteBuffer.wrap(ba);
			final StringBuffer sb = new StringBuffer();
			for (int i = 0; i < length; i++) {
				sb.append(buf.getChar());
			}
			return sb.toString();
		}
	}

	public Entry<E> readLuposEntry() throws IOException, ClassNotFoundException {
		E e = null;
		try {
			e = Registration.deserializeWithId(this);
		} catch (final URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (e == null)
			return null;
		return new Entry<E>(e);
	}

	public boolean readLuposBoolean() throws IOException {
		final int i = is.read();
		return (i == 0);
	}

	public Integer readLuposInteger1Byte() throws IOException {
		final int i1 = is.read();
		if (i1 < 0)
			return null;
		return i1;
	}

	public Integer readLuposInteger2Bytes() throws IOException {
		final int i1 = is.read();
		if (i1 < 0)
			return null;
		final int i2 = is.read();
		if (i2 < 0)
			return null;
		return i1 + 256 * i2;
	}

	public Integer readLuposInteger3Bytes() throws IOException {
		final int i1 = is.read();
		if (i1 < 0)
			return null;
		final int i2 = is.read();
		if (i2 < 0)
			return null;
		final int i3 = is.read();
		if (i3 < 0)
			return null;
		return i1 + 256 * (i2 + 256 * i3);
	}

	public Integer readLuposInteger() throws IOException {
		final int i1 = is.read();
		if (i1 < 0)
			return null;
		final int i2 = is.read();
		if (i2 < 0)
			return null;
		final int i3 = is.read();
		if (i3 < 0)
			return null;
		final int i4 = is.read();
		if (i4 < 0)
			return null;
		return (i1 + 256 * (i2 + 256 * (i3 + 256 * i4)));
	}

	public Long readLuposLong() throws IOException {
		final Integer a = readLuposInteger();
		final Integer b = readLuposInteger();
		if (a == null || b == null)
			return null;
		return (long) a + (long) b * ((long) 256 * 256 * 256 * 256);
	}

	public int readLuposInt() throws IOException {
		final int i1 = is.read();
		if (i1 < 0)
			return i1;
		final int i2 = is.read();
		if (i2 < 0)
			return i2;
		final int i3 = is.read();
		if (i3 < 0)
			return i3;
		final int i4 = is.read();
		if (i4 < 0)
			return i4;
		return (i1 + 256 * (i2 + 256 * (i3 + 256 * i4)));
	}

	// public int readLuposInt(final int maxValue) throws IOException {
	// final int i1 = is.read();
	// if (i1 < 0)
	// return i1;
	// int i2 = 0;
	// int i3 = 0;
	// int i4 = 0;
	// if (maxValue >= 256) {
	// i2 = is.read();
	// if (i2 < 0)
	// return i2;
	// }
	// if (maxValue >= 256 * 256) {
	// i3 = is.read();
	// if (i3 < 0)
	// return i3;
	// }
	// if (maxValue >= 256 * 256 * 256) {
	// i4 = is.read();
	// if (i4 < 0)
	// return i4;
	// }
	// return (i1 + 256 * (i2 + 256 * (i3 + 256 * i4)));
	// }

	public byte readLuposByte() throws IOException {
		final int value = is.read();
		if (value < 0)
			throw new EOFException();
		return (byte) value;
	}

	public TripleKey readLuposTripleKey() throws IOException {
		// it is expected that the triple key contains the key computed from the
		// last read triple
		final int order = is.read();
		if (order < 0)
			return null;
		Triple t;
		if (lastSubject == null) {
			final Literal subject = readLiteral();
			final Literal predicate = readLiteral();
			final Literal object = readLiteral();
			t = new Triple(subject, predicate, object);
		} else
			t = new Triple(lastSubject, lastPredicate, lastObject);
		return new TripleKey(t, new TripleComparator((byte) order));
	}

	public static int readLuposInt(final ObjectInput in) throws IOException {
		final int i0 = in.read();
		if (i0 <= 251)
			return i0;
		int result = 251;
		int offset = 1;
		for (int i = 0; i < i0 - 251; i++) {
			result += in.read() * offset;
			offset *= 256;
		}
		return result;
	}

	public static Literal readLuposLiteral(final ObjectInput in)
			throws IOException, ClassNotFoundException {
		if (LiteralFactory.getMapType() == MapType.NOCODEMAP
				|| LiteralFactory.getMapType() == MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				|| LiteralFactory.getMapType() == MapType.PREFIXCODEMAP)
			return new StringLiteral((String) in.readObject());
		else
			return new CodeMapLiteral(LuposObjectInputStream.readLuposInt(in));
	}

}
