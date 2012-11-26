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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
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
	
	public static final String UTF8 = "UTF-8";

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

	protected InputStream is;
	protected Class<? extends E> classOfElements;

	public LuposObjectInputStream() throws IOException {
	}

	public LuposObjectInputStream(final InputStream arg0, final Class<? extends E> classOfElements) throws IOException, EOFException {
		super(arg0);
		this.is = arg0;
		this.classOfElements = classOfElements;
	}

	public E readLuposObject() throws IOException, ClassNotFoundException, URISyntaxException {
		return Registration.deserializeWithoutId(this.classOfElements, this);
	}

	public <TT> TT readLuposObject(final Class classOfElements)
			throws IOException, ClassNotFoundException, URISyntaxException {
		return (TT) Registration.deserializeWithoutId(classOfElements, this);
	}

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
			Serializable value;
			Serializable key;
			try {
				value = (Serializable) Registration.deserializeWithoutId(typeValue, this);
				key = (Serializable) Registration.deserializeWithoutId(typeKey, this);
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
	
	public BigInteger readLuposBigInteger(final int numberOfBits) throws IOException {
		BigInteger result = BigInteger.ZERO;
		BigInteger factor = BigInteger.ONE;
		final BigInteger BYTE = BigInteger.valueOf(256);
		int remainingBits = numberOfBits;
		while(remainingBits>0){
			final int currentValueByte = this.is.read();
			if(currentValueByte<0){
				// EOF reached!
				return null;
			}
			final BigInteger currentValue = BigInteger.valueOf(currentValueByte);
			result = result.add(currentValue.multiply(factor));
			factor = factor.multiply(BYTE);
			remainingBits-=8;
		}
		return result;
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
			final Map<Variable, Integer> hm = BindingsArray.getPosVariables();
			BigInteger usedVars = readLuposBigInteger(hm.size());
			if (usedVars == null)
				return null;
			BigInteger differentFromPreviousBindings = readLuposBigInteger(hm.size());
			final Bindings b = Bindings.createNewInstance();
			final BigInteger TWO = BigInteger.valueOf(2);
			for (final Variable v : hm.keySet()) {				
				if (usedVars.mod(TWO).compareTo(BigInteger.ONE)==0) {
					if (this.previousBindings == null || differentFromPreviousBindings.mod(TWO).compareTo(BigInteger.ONE)==0) {
						Literal lit;
						lit = readLiteral();
						b.add(v, lit);
					} else {
						b.add(v, this.previousBindings.get(v));
					}
				}
				usedVars = usedVars.shiftRight(1);
				differentFromPreviousBindings = differentFromPreviousBindings.shiftRight(1);
			}
			this.previousBindings = b;
			if (b instanceof BindingsArrayReadTriples) {
				final int number = readLuposInt();
				if (number == 0)
					return b;
				if (number < 0)
					return null;
				for (int j = 0; j < number; j++) {
					try {
						Triple t = readTriple();
						b.addTriple(t);
					} catch (final URISyntaxException e) {
						System.out.println(e);
						e.printStackTrace();
						return b;
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
		final Integer length = this.readLuposIntVariableBytes();
		if(length==null || length<0){
			return null;
		}
		final byte[] bytesOfResult = new byte[length];
		this.is.read(bytesOfResult);
		return new String(bytesOfResult, LuposObjectInputStream.UTF8);
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

	public Integer readLuposIntVariableBytes() throws IOException {
		final Integer i0 = this.readLuposInteger1Byte();
		if(i0==null){
			return null;
		}
		if (i0 <= 251){
			return i0;
		}
		int result = 251;
		int offset = 1;
		for (int i = 0; i < i0 - 251; i++) {
			result += this.readLuposInteger1Byte() * offset;
			offset *= 256;
		}
		return result;
	}
	
	public static int readLuposInt(final ObjectInput in) throws IOException {
		final int i0 = in.read();
		if (i0 <= 251){
			return i0;
		}
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
