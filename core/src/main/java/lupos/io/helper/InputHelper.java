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
package lupos.io.helper;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.dbmergesortedds.DiskCollection;
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
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.smallerinmemorylargerondisk.PagedCollection;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;
import lupos.engine.operators.multiinput.join.InnerNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.LeafNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.NodeInPartitionTree;
import lupos.io.LuposObjectInputStream;
import lupos.io.Registration;
import lupos.optimizations.logical.statistics.VarBucket;
public final class InputHelper {

	/**
	 * <p>readLuposString.</p>
	 *
	 * @param lastString a {@link java.lang.String} object.
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public final static String readLuposString(final String lastString, final InputStream is) throws IOException {
		final byte[] result = InputHelper.readLuposDifferenceString((lastString==null)?null:lastString.getBytes(LuposObjectInputStream.UTF8), is);
		if(result==null){
			return null;
		} else {
			return new String(result, LuposObjectInputStream.UTF8);
		}
	}

	/**
	 * <p>readLuposDifferenceString.</p>
	 *
	 * @param lastString an array of byte.
	 * @param is a {@link java.io.InputStream} object.
	 * @return an array of byte.
	 * @throws java.io.IOException if any.
	 */
	public final static byte[] readLuposDifferenceString(final byte[] lastString, final InputStream is) throws IOException {
		if(lastString==null){
			return InputHelper.readLuposStringAsByteArray(is);
		}
		final Integer common = InputHelper.readLuposIntVariableBytes(is);
		if(common==null){
			return null;
		}
		final Integer length = InputHelper.readLuposIntVariableBytes(is);
		if(length==null || length<0){
			return null;
		}
		// copy the common prefix with the last stored string!
		final byte[] bytesOfResult = new byte[common + length];
		System.arraycopy(lastString, 0, bytesOfResult, 0, common);

		// now read only difference string
		is.read(bytesOfResult, common, length);
		return bytesOfResult;
	}

	/**
	 * <p>readLuposString.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.String} object.
	 * @throws java.io.IOException if any.
	 */
	public final static String readLuposString(final InputStream is) throws IOException {
		final byte[] result = InputHelper.readLuposStringAsByteArray(is);
		if(result==null){
			return null;
		} else {
			return new String(result, LuposObjectInputStream.UTF8);
		}
	}

	/**
	 * <p>readLuposStringAsByteArray.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return an array of byte.
	 * @throws java.io.IOException if any.
	 */
	public final static byte[] readLuposStringAsByteArray(final InputStream is) throws IOException {
		final Integer length = InputHelper.readLuposIntVariableBytes(is);
		if(length==null || length<0){
			return null;
		}
		final byte[] bytesOfResult = new byte[length];
		is.read(bytesOfResult);
		return bytesOfResult;
	}

	/**
	 * <p>readLuposEntry.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @param <E> a E object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static<E> Entry<E> readLuposEntry(final InputStream is) throws IOException, ClassNotFoundException {
		E e = null;
		try {
			e = Registration.deserializeWithId(is);
		} catch (final URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (e == null) {
			return null;
		}
		return new Entry<E>(e);
	}

	/**
	 * <p>readLuposEntry.</p>
	 *
	 * @param is a {@link lupos.io.LuposObjectInputStream} object.
	 * @param <E> a E object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static<E> Entry<E> readLuposEntry(final LuposObjectInputStream<E> is) throws IOException, ClassNotFoundException {
		E e = null;
		try {
			e = Registration.deserializeWithId(is);
		} catch (final URISyntaxException e1) {
			e1.printStackTrace();
		}
		if (e == null) {
			return null;
		}
		return new Entry<E>(e);
	}

	/**
	 * <p>readLuposBoolean.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a boolean.
	 * @throws java.io.IOException if any.
	 */
	public final static boolean readLuposBoolean(final InputStream is) throws IOException {
		final int i = is.read();
		return (i == 0);
	}

	/**
	 * <p>readLuposInteger1Byte.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.Integer} object.
	 * @throws java.io.IOException if any.
	 */
	public final static Integer readLuposInteger1Byte(final InputStream is) throws IOException {
		final int i1 = is.read();
		if (i1 < 0) {
			return null;
		}
		return i1;
	}

	/**
	 * <p>readLuposInteger2Bytes.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.Integer} object.
	 * @throws java.io.IOException if any.
	 */
	public final static Integer readLuposInteger2Bytes(final InputStream is) throws IOException {
		final int i1 = is.read();
		if (i1 < 0) {
			return null;
		}
		final int i2 = is.read();
		if (i2 < 0) {
			return null;
		}
		return (0xFF & i1) | (0xFF & i2) << 8;
	}

	/**
	 * <p>readLuposInteger3Bytes.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.Integer} object.
	 * @throws java.io.IOException if any.
	 */
	public final static Integer readLuposInteger3Bytes(final InputStream is) throws IOException {
		final int i1 = is.read();
		if (i1 < 0) {
			return null;
		}
		final int i2 = is.read();
		if (i2 < 0) {
			return null;
		}
		final int i3 = is.read();
		if (i3 < 0) {
			return null;
		}
		return (0xFF & i1) | ((0xFF & i2) | (0xFF & i3) << 8) << 8;
	}

	/**
	 * <p>readLuposInteger.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.Integer} object.
	 * @throws java.io.IOException if any.
	 */
	public final static Integer readLuposInteger(final InputStream is) throws IOException {
		final int i1 = is.read();
		if (i1 < 0) {
			return null;
		}
		final int i2 = is.read();
		if (i2 < 0) {
			return null;
		}
		final int i3 = is.read();
		if (i3 < 0) {
			return null;
		}
		final int i4 = is.read();
		if (i4 < 0) {
			return null;
		}
		return (0xFF & i1) | ((0xFF & i2) | ((0xFF & i3) | (0xFF & i4) << 8) << 8) << 8;
	}

	/**
	 * <p>readLuposLong.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.Long} object.
	 * @throws java.io.IOException if any.
	 */
	public final static Long readLuposLong(final InputStream is) throws IOException {
		final Integer a = InputHelper.readLuposInteger(is);
		final Integer b = InputHelper.readLuposInteger(is);
		if (a == null || b == null) {
			return null;
		}
		return (long) a | ((long) b) << 32;
	}

	/**
	 * <p>readLuposInt.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a int.
	 * @throws java.io.IOException if any.
	 */
	public final static int readLuposInt(final InputStream is) throws IOException {
		final int i1 = is.read();
		if (i1 < 0) {
			return i1;
		}
		final int i2 = is.read();
		if (i2 < 0) {
			return i2;
		}
		final int i3 = is.read();
		if (i3 < 0) {
			return i3;
		}
		final int i4 = is.read();
		if (i4 < 0) {
			return i4;
		}
		return (0xFF & i1) | ((0xFF & i2) | ((0xFF & i3) | (0xFF & i4) << 8) << 8) << 8;
	}

	/**
	 * <p>readLuposByte.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a byte.
	 * @throws java.io.IOException if any.
	 */
	public final static byte readLuposByte(final InputStream is) throws IOException {
		final int value = is.read();
		if (value < 0) {
			throw new EOFException();
		}
		return (byte) value;
	}

	/**
	 * <p>readLuposIntVariableBytes.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.lang.Integer} object.
	 * @throws java.io.IOException if any.
	 */
	public final static Integer readLuposIntVariableBytes(final InputStream is) throws IOException {
		final Integer i0 = InputHelper.readLuposInteger1Byte(is);
		if(i0==null){
			return null;
		}
		if (i0 <= 251){
			return i0;
		}
		int result = 251;
		int offset = 1;
		for (int i = 1; i <= i0 - 251; i++) {
			result += InputHelper.readLuposInteger1Byte(is) * offset;
			offset <<= 8;
		}
		return result;
	}

	/**
	 * <p>readLuposInt.</p>
	 *
	 * @param in a {@link java.io.ObjectInput} object.
	 * @return a int.
	 * @throws java.io.IOException if any.
	 */
	public final static int readLuposInt(final ObjectInput in) throws IOException {
		final int i0 = in.read();
		if (i0 <= 251){
			return i0;
		}
		int result = 251;
		int offset = 1;
		for (int i = 1; i <= i0 - 251; i++) {
			result += in.read() * offset;
			offset <<= 8;
		}
		return result;
	}

	/**
	 * <p>readLuposBigInteger.</p>
	 *
	 * @param numberOfBits a int.
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link java.math.BigInteger} object.
	 * @throws java.io.IOException if any.
	 */
	public final static BigInteger readLuposBigInteger(final int numberOfBits, final InputStream is) throws IOException {
		BigInteger result = BigInteger.ZERO;
		BigInteger factor = BigInteger.ONE;
		final BigInteger BYTE = BigInteger.valueOf(256);
		int remainingBits = numberOfBits;
		while(remainingBits>0){
			final int currentValueByte = is.read();
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

	/**
	 * <p>readLuposLiteral.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static Literal readLuposLiteral(final InputStream is) throws IOException, ClassNotFoundException {
		return LiteralFactory.readLuposLiteral(is);
	}

	/**
	 * <p>readLuposLiteral.</p>
	 *
	 * @param in a {@link java.io.ObjectInput} object.
	 * @return a {@link lupos.datastructures.items.literal.Literal} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static Literal readLuposLiteral(final ObjectInput in) throws IOException, ClassNotFoundException {
		if (LiteralFactory.getMapType() == MapType.NOCODEMAP
				|| LiteralFactory.getMapType() == MapType.LAZYLITERAL
				|| LiteralFactory.getMapType() == MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP
				|| LiteralFactory.getMapType() == MapType.PREFIXCODEMAP) {
			return new StringLiteral((String) in.readObject());
		} else {
			return new CodeMapLiteral(InputHelper.readLuposInt(in));
		}
	}

	/**
	 * <p>readLuposTriple.</p>
	 *
	 * @param previousTriple a {@link lupos.datastructures.items.Triple} object.
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static Triple readLuposTriple(final Triple previousTriple, final InputStream is) throws IOException, ClassNotFoundException {
		final int diff = is.read();
		if (diff < 0) {
			return null;
		}
		final Literal subject = (diff % 2 == 1) ? InputHelper.readLuposLiteral(is) : previousTriple.getSubject();
		final Literal predicate = ((diff / 2) % 2 == 1) ? InputHelper.readLuposLiteral(is) : previousTriple.getPredicate();
		final Literal object = ((diff / 4) % 2 == 1) ? InputHelper.readLuposLiteral(is) : previousTriple.getObject();
		if (subject == null || predicate == null || object == null) {
			return null;
		}
		return new Triple(subject, predicate, object);
	}

	/**
	 * <p>readLuposTriple.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.items.Triple} object.
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java.io.IOException if any.
	 */
	public final static Triple readLuposTriple(final InputStream is) throws ClassNotFoundException, IOException {
		return new Triple(InputHelper.readLuposLiteral(is), InputHelper.readLuposLiteral(is), InputHelper.readLuposLiteral(is));
	}

	/**
	 * <p>readLuposMapEntry.</p>
	 *
	 * @param in a {@link lupos.io.LuposObjectInputStream} object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.MapEntry} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static lupos.datastructures.dbmergesortedds.MapEntry<Object, Object> readLuposMapEntry(final LuposObjectInputStream in) throws IOException, ClassNotFoundException {
		final Class type1 = Registration.deserializeId(in)[0];
		if (type1 == null) {
			return null;
		}
		final Class type2 = Registration.deserializeId(in)[0];
		final Object key, value;
		try {
			if (type1 == String.class && (type2 == Triple.class)) {
				value = Registration.deserializeWithoutId(type2, in);
				final Triple t = (Triple) value;
				final int compressed = in.read();
				switch (compressed) {
				case 1:
					key = new String(t.getSubject().toString() + t.getPredicate().toString() + t.getObject().toString());
					break;
				case 2:
					key = new String(t.getSubject().toString() + t.getObject().toString() + t.getPredicate().toString());
					break;
				case 3:
					key = new String(t.getPredicate().toString() + t.getSubject().toString() + t.getObject().toString());
					break;
				case 4:
					key = new String(t.getPredicate().toString() + t.getObject().toString() + t.getSubject().toString());
					break;
				case 5:
					key = new String(t.getObject().toString() + t.getSubject().toString() + t.getPredicate().toString());
					break;
				case 6:
					key = new String(t.getObject().toString() + t.getPredicate().toString() + t.getSubject().toString());
					break;
				default:
					key = Registration.deserializeWithoutId(type1, in);
				}
			} else {
				value = Registration.deserializeWithoutId(type2, in);
				key = Registration.deserializeWithoutId(type1, in);
			}
		} catch (final URISyntaxException e) {
			throw new IOException(
					"Expected URI, but did not read URI from InputStream!");
		}
		return new lupos.datastructures.dbmergesortedds.MapEntry<Object, Object>(key, value);
	}

	/**
	 * <p>readLuposMapEntry.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.MapEntry} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static lupos.datastructures.dbmergesortedds.MapEntry<Object, Object> readLuposMapEntry(final InputStream in) throws IOException, ClassNotFoundException {
		final Class type1 = Registration.deserializeId(in)[0];
		if (type1 == null) {
			return null;
		}
		final Class type2 = Registration.deserializeId(in)[0];
		final Object key, value;
		try {
			if (type1 == String.class && (type2 == Triple.class)) {
				value = Registration.deserializeWithoutId(type2, in);
				final Triple t = (Triple) value;
				final int compressed = in.read();
				switch (compressed) {
				case 1:
					key = new String(t.getSubject().toString() + t.getPredicate().toString() + t.getObject().toString());
					break;
				case 2:
					key = new String(t.getSubject().toString() + t.getObject().toString() + t.getPredicate().toString());
					break;
				case 3:
					key = new String(t.getPredicate().toString() + t.getSubject().toString() + t.getObject().toString());
					break;
				case 4:
					key = new String(t.getPredicate().toString() + t.getObject().toString() + t.getSubject().toString());
					break;
				case 5:
					key = new String(t.getObject().toString() + t.getSubject().toString() + t.getPredicate().toString());
					break;
				case 6:
					key = new String(t.getObject().toString() + t.getPredicate().toString() + t.getSubject().toString());
					break;
				default:
					key = Registration.deserializeWithoutId(type1, in);
				}
			} else {
				value = Registration.deserializeWithoutId(type2, in);
				key = Registration.deserializeWithoutId(type1, in);
			}
		} catch (final URISyntaxException e) {
			throw new IOException(
					"Expected URI, but did not read URI from InputStream!");
		}
		return new lupos.datastructures.dbmergesortedds.MapEntry<Object, Object>(key, value);
	}

	/**
	 * <p>readLuposBindings.</p>
	 *
	 * @param previousBindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static Bindings readLuposBindings(final Bindings previousBindings, final InputStream in) throws IOException, ClassNotFoundException {
		if (Bindings.instanceClass == BindingsMap.class) {
			return InputHelper.readLuposBindingsMap(in);
		} else {
			final BindingsFactory bindingsFactory = (previousBindings==null) ? readLuposBindingsFactory(in) : ((BindingsArray) previousBindings).getBindingsFactory();
			final Map<Variable, Integer> hm = bindingsFactory.getPosVariables();
			BigInteger usedVars = InputHelper.readLuposBigInteger(hm.size(), in);
			if (usedVars == null) {
				return null;
			}
			BigInteger differentFromPreviousBindings = InputHelper.readLuposBigInteger(hm.size(), in);
			final Bindings b = bindingsFactory.createInstance();
			final BigInteger TWO = BigInteger.valueOf(2);
			for (final Variable v : hm.keySet()) {
				if (usedVars.mod(TWO).compareTo(BigInteger.ONE)==0) {
					if (previousBindings == null || differentFromPreviousBindings.mod(TWO).compareTo(BigInteger.ONE)==0) {
						Literal lit;
						lit = InputHelper.readLuposLiteral(in);
						b.add(v, lit);
					} else {
						b.add(v, previousBindings.get(v));
					}
				}
				usedVars = usedVars.shiftRight(1);
				differentFromPreviousBindings = differentFromPreviousBindings.shiftRight(1);
			}
			InputHelper.addSpecialInformationToBindings(b, in);
			return b;
		}
	}

	/**
	 * <p>readLuposBindings.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.bindings.Bindings} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static Bindings readLuposBindings(final InputStream in) throws IOException, ClassNotFoundException {
		if (Bindings.instanceClass == BindingsMap.class) {
			return InputHelper.readLuposBindingsMap(in);
		} else {
			final BindingsFactory bindingsFactory = readLuposBindingsFactory(in);
			final Map<Variable, Integer> hm = bindingsFactory.getPosVariables();
			BigInteger usedVars = InputHelper.readLuposBigInteger(hm.size(), in);
			if (usedVars == null) {
				return null;
			}
			final Bindings b = bindingsFactory.createInstance();
			final BigInteger TWO = BigInteger.valueOf(2);
			for (final Variable v : hm.keySet()) {
				if (usedVars.mod(TWO).compareTo(BigInteger.ONE)==0) {
					final Literal lit = InputHelper.readLuposLiteral(in);
					b.add(v, lit);
				}
				usedVars = usedVars.shiftRight(1);
			}
			InputHelper.addSpecialInformationToBindings(b, in);
			return b;
		}
	}

	private final static BindingsMap readLuposBindingsMap(final InputStream in) throws IOException, ClassNotFoundException {
		if (Bindings.instanceClass == BindingsMap.class) {
			final Bindings b = new BindingsMap();
			final int number = InputHelper.readLuposIntVariableBytes(in);
			if (number < 0) {
				return null;
			}
			for (int i = 0; i < number; i++) {
				final String varName = InputHelper.readLuposString(in);
				final Variable v = new Variable(varName);
				final Literal l = InputHelper.readLuposLiteral(in);
				b.add(v, l);
			}
			return (BindingsMap) b;
		}
		return null;
	}

	private final static BindingsFactory readLuposBindingsFactory(final InputStream in) throws IOException {
		final int number = InputHelper.readLuposIntVariableBytes(in);
		if (number < 0) {
			return null;
		}
		final Variable[] vars = new Variable[number];
		for(int i=0; i<number; i++){
			vars[i] = InputHelper.readLuposVariable(in);
		}
		return BindingsFactory.createBindingsFactory(vars);
	}

	private final static Variable readLuposVariable(final InputStream in) throws IOException {
		final String name = readLuposString(in);
		if(name==null){
			return null;
		} else {
			return new Variable(name);
		}
	}

	private final static void addSpecialInformationToBindings(final Bindings b, final InputStream in) throws IOException, ClassNotFoundException {
		if (b instanceof BindingsArrayReadTriples) {
			final int number = InputHelper.readLuposIntVariableBytes(in);
			if (number == 0) {
				return;
			}
			if (number < 0) {
				return;
			}
			for (int j = 0; j < number; j++) {
				final Triple t = InputHelper.readLuposTriple(in);
				b.addTriple(t);
			}
		}
		if (b instanceof BindingsArrayVarMinMax) {
			((BindingsArrayVarMinMax) b).readPresortingNumbers(in);
		}
	}

	/**
	 * <p>readLuposTripleKey.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.items.TripleKey} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static TripleKey readLuposTripleKey(final InputStream in) throws IOException, ClassNotFoundException {
		final int order = in.read();
		if (order < 0) {
			return null;
		}
		final Triple t = InputHelper.readLuposTriple(in);
		return new TripleKey(t, new TripleComparator((byte) order));
	}

	/**
	 * <p>readLuposTripleKey.</p>
	 *
	 * @param previousTripleKey a {@link lupos.datastructures.items.TripleKey} object.
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.items.TripleKey} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static TripleKey readLuposTripleKey(final TripleKey previousTripleKey, final InputStream in) throws IOException, ClassNotFoundException {
		return InputHelper.readLuposTripleKey(previousTripleKey.getTriple(), in);
	}

	/**
	 * <p>readLuposTripleKey.</p>
	 *
	 * @param previousTriple a {@link lupos.datastructures.items.Triple} object.
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.items.TripleKey} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static TripleKey readLuposTripleKey(final Triple previousTriple, final InputStream in) throws IOException, ClassNotFoundException {
		final int order = in.read();
		if (order < 0) {
			return null;
		}
		final Triple t = InputHelper.readLuposTriple(previousTriple, in);
		return new TripleKey(t, new TripleComparator((byte) order));
	}

	/**
	 * <p>readLuposCollection.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @param <T> a T object.
	 * @return a {@link java.util.Collection} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	@SuppressWarnings("unchecked")
	public final static<T> Collection<T> readLuposCollection(final InputStream in) throws IOException, ClassNotFoundException {
		final int size = in.read();
		if (size == 255) {
			return DiskCollection.readAndCreateLuposObject(in);
		} else {
			final LinkedList<T> ll = new LinkedList<T>();
			@SuppressWarnings("rawtypes")
			final Class type = Registration.deserializeId(in)[0];
			for (int i = 0; i < size; i++) {
				try {
					ll.add((T) Registration.deserializeWithoutId(type, in));
				} catch (final URISyntaxException e) {
					e.printStackTrace();
					throw new IOException(e.getMessage());
				}
			}
			return ll;
		}
	}

	/**
	 * <p>readLuposSet.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.smallerinmemorylargerondisk.SetImplementation} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static SetImplementation readLuposSet(final InputStream in) throws IOException, ClassNotFoundException {
		final int size = InputHelper.readLuposInt(in);
		if (size < 0){
			return null;
		}
		final SetImplementation set = new SetImplementation();
		if(size==0){
			return set;
		}
		final Class type = Registration.deserializeId(in)[0];
		for (int i = 0; i < size; i++) {
			try {
				set.add((Serializable)Registration.deserializeWithoutId(type, in));
			} catch (final URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
		}
		return set;
	}

	/**
	 * <p>readLuposNodeInPartitionTree.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @return a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 */
	public final static NodeInPartitionTree readLuposNodeInPartitionTree(final InputStream in) throws IOException, ClassNotFoundException {
		final byte type = InputHelper.readLuposByte(in);
		switch (type) {
		case 1:
			final Collection<Bindings> coll = PagedCollection.readAndCreateLuposObject(in);
			return new LeafNodeInPartitionTree(new QueryResult(coll));
		default:
		case 2:
			final PagedCollection<NodeInPartitionTree> pcoll = PagedCollection.readAndCreateLuposObject(in);
			return new InnerNodeInPartitionTree(pcoll);
		}
	}

	/**
	 * <p>readLuposVarBucket.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return a {@link lupos.optimizations.logical.statistics.VarBucket} object.
	 * @throws java.io.IOException if any.
	 */
	public final static VarBucket readLuposVarBucket(final InputStream is) throws IOException {
		final VarBucket vb = new VarBucket();
		final int size = InputHelper.readLuposIntVariableBytes(is);
		final byte minMax = InputHelper.readLuposByte(is);
		if (minMax >= 2) {
			vb.minimum = LiteralFactory.readLuposLiteral(is);
		}
		if (minMax % 2 == 1) {
			vb.maximum = LiteralFactory.readLuposLiteral(is);
		}
		for (int i = 0; i < size; i++) {
			final lupos.optimizations.logical.statistics.Entry entry = new lupos.optimizations.logical.statistics.Entry();
			entry.distinctLiterals = Double.longBitsToDouble(InputHelper.readLuposLong(is));
			entry.selectivity = Double.longBitsToDouble(InputHelper.readLuposLong(is));
			entry.literal = LiteralFactory.readLuposLiteral(is);
			vb.selectivityOfInterval.add(entry);
		}
		return vb;
	}

	/**
	 * <p>readLuposVarBucketArray.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @return an array of {@link lupos.optimizations.logical.statistics.VarBucket} objects.
	 * @throws java.io.IOException if any.
	 */
	public final static VarBucket[] readLuposVarBucketArray(final InputStream is) throws IOException {
		final int number = InputHelper.readLuposIntVariableBytes(is);
		final int nulls = InputHelper.readLuposIntVariableBytes(is);
		final VarBucket[] vba = new VarBucket[number];
		int counter = 1;
		for (int i = 0; i < number; i++) {
			if ((nulls / counter) % 2 == 0) {
				vba[i] = InputHelper.readLuposVarBucket(is);
			}
			counter *= 2;
		}
		return vba;
	}
}
