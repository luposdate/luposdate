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

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.Entry;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.datastructures.smallerinmemorylargerondisk.SetImplementation;
import lupos.engine.operators.multiinput.join.InnerNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.LeafNodeInPartitionTree;
import lupos.engine.operators.multiinput.join.NodeInPartitionTree;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.io.Registration;
import lupos.optimizations.logical.statistics.VarBucket;
public final class OutHelper {

	/**
	 * <p>writeLuposTriple.</p>
	 *
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @param previousTriple a {@link lupos.datastructures.items.Triple} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposTriple(final Triple t, final Triple previousTriple, final OutputStream os) throws IOException {
		int diff = 0;
		if (previousTriple.getSubject() == null
				|| t.getSubject() instanceof LazyLiteral
				&& !t.getSubject().equals(previousTriple.getSubject())
				|| !(t.getSubject() instanceof LazyLiteral)
				&& t.getSubject().originalString().compareTo(
						previousTriple.getSubject().originalString()) != 0) {
			diff = 1;
		}
		if (previousTriple.getPredicate() == null
				|| t.getPredicate() instanceof LazyLiteral
				&& !t.getPredicate().equals(previousTriple.getPredicate())
				|| !(t.getPredicate() instanceof LazyLiteral)
				&& t.getPredicate().originalString().compareTo(
						previousTriple.getPredicate().originalString()) != 0) {
			diff += 2;
		}
		if (previousTriple.getObject() == null
				|| t.getObject() instanceof LazyLiteral
				&& !t.getObject().equals(previousTriple.getObject())
				|| !(t.getObject() instanceof LazyLiteral)
				&& t.getObject().originalString().compareTo(
						previousTriple.getObject().originalString()) != 0) {
			diff += 4;
		}
		os.write(diff);
		if (diff % 2 == 1) {
			OutHelper.writeLuposLiteral(t.getSubject(), os);
		}
		if ((diff / 2) % 2 == 1) {
			OutHelper.writeLuposLiteral(t.getPredicate(), os);
		}
		if ((diff / 4) % 2 == 1) {
			OutHelper.writeLuposLiteral(t.getObject(), os);
		}
	}

	/**
	 * <p>writeLuposTriple.</p>
	 *
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposTriple(final Triple t, final OutputStream os) throws IOException {
		OutHelper.writeLuposLiteral(t.getSubject(), os);
		OutHelper.writeLuposLiteral(t.getPredicate(), os);
		OutHelper.writeLuposLiteral(t.getObject(), os);
	}

	/**
	 * <p>writeLuposDifferenceString.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param previousString an array of byte.
	 * @param os a {@link java.io.OutputStream} object.
	 * @return the byte array of the just written string (to be kept and used for next call to this method)
	 * @throws java.io.IOException if any.
	 */
	public final static byte[] writeLuposDifferenceString(final String s, final byte[] previousString, final OutputStream os) throws IOException {
		if (s == null){
			return null;
		}
		if(previousString==null){
			OutHelper.writeLuposString(s, os);
			return s.getBytes(LuposObjectInputStream.UTF8);
		}
		final byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
		// determine common prefix of new string and last stored string
		int common = 0;
		while(common<bytesOfS.length && common < previousString.length && bytesOfS[common]==previousString[common]){
			common++;
		}
		OutHelper.writeLuposIntVariableBytes(common, os);

		// now write only difference string
		final int length = bytesOfS.length;
		OutHelper.writeLuposIntVariableBytes(length-common, os);
		os.write(bytesOfS, common, length-common);
		return bytesOfS;
	}

	/**
	 * <p>writeLuposString.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param previousString a {@link java.lang.String} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposString(final String s, final String previousString, final OutputStream os) throws IOException {
		OutHelper.writeLuposDifferenceString(s, (previousString==null)? null: previousString.getBytes(LuposObjectInputStream.UTF8), os);
	}

	/**
	 * <p>writeLuposString.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposString(final String s, final OutputStream os) throws IOException {
		if (s == null){
			return;
		}
		final byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
		final int length = bytesOfS.length;
		OutHelper.writeLuposIntVariableBytes(length, os);
		os.write(bytesOfS);
	}

	/**
	 * <p>writeLuposBoolean.</p>
	 *
	 * @param flag a boolean.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposBoolean(final boolean flag, final OutputStream os) throws IOException {
		if (flag){
			os.write(0);
		} else {
			os.write(1);
		}
	}

	/**
	 * <p>writeLuposInt.</p>
	 *
	 * @param i a int.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposInt(final int i, final OutputStream os) throws IOException {
		int remaining = i;
		os.write((byte) remaining);
		remaining>>>=8;
		os.write((byte) remaining);
		remaining>>>=8;
		os.write((byte) remaining);
		remaining>>>=8;
		os.write((byte) remaining);
	}

	/**
	 * <p>writeLuposBigInteger.</p>
	 *
	 * @param value a {@link java.math.BigInteger} object.
	 * @param numberOfBits a int.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposBigInteger(final BigInteger value, final int numberOfBits, final OutputStream os) throws IOException {
		int remainingBits = numberOfBits;
		BigInteger remainingValue = value;
		final BigInteger BYTE = BigInteger.valueOf(256);
		while(remainingBits>0){
			final BigInteger[] result = remainingValue.divideAndRemainder(BYTE);
			remainingValue = result[0];
			os.write((byte) result[1].intValue());
			remainingBits-=8;
		}
	}

	/**
	 * <p>writeLuposInt1Byte.</p>
	 *
	 * @param i a int.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposInt1Byte(final int i, final OutputStream os) throws IOException {
		os.write((byte) i);
	}

	/**
	 * <p>writeLuposInt2Bytes.</p>
	 *
	 * @param i a int.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static  void writeLuposInt2Bytes(final int i, final OutputStream os) throws IOException {
		int remaining = i;
		os.write((byte) remaining);
		remaining>>>=8;
		os.write((byte) remaining);
	}

	/**
	 * <p>writeLuposInt3Bytes.</p>
	 *
	 * @param i a int.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposInt3Bytes(final int i, final OutputStream os) throws IOException {
		int remaining = i;
		os.write((byte) remaining);
		remaining>>>=8;
		os.write((byte) remaining);
		remaining>>>=8;
		os.write((byte) remaining);
	}

	/**
	 * <p>writeLuposLong.</p>
	 *
	 * @param l a long.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposLong(final long l, final OutputStream os) throws IOException {
		OutHelper.writeLuposInt((int) l, os);
		OutHelper.writeLuposInt((int) (l >>> 32), os);
	}

	/**
	 * <p>writeLuposByte.</p>
	 *
	 * @param b a byte.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposByte(final byte b, final OutputStream os) throws IOException {
		os.write(b);
	}

	/**
	 * <p>writeLuposIntVariableBytes.</p>
	 *
	 * @param i_par a int.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposIntVariableBytes(final int i_par, final OutputStream os) throws IOException {
		int i = i_par;
		if (i <= 251) {
			OutHelper.writeLuposInt1Byte(i, os);
		} else {
			i -= 251;
			if (i >= 256) {
				if (i >>> 8 >= 256) {
					if (i >>> 16 >= 256) {
						OutHelper.writeLuposInt1Byte(255, os);
					} else {
						OutHelper.writeLuposInt1Byte(254, os);
					}
				} else {
					OutHelper.writeLuposInt1Byte(253, os);
				}
			} else {
				OutHelper.writeLuposInt1Byte(252, os);
			}
			while (i > 0) {
				OutHelper.writeLuposInt1Byte((byte) i, os);
				i >>>= 8;
			}
		}
	}

	/**
	 * <p>writeLuposInt.</p>
	 *
	 * @param i_par a int.
	 * @param out a {@link java.io.ObjectOutput} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposInt(final int i_par, final ObjectOutput out) throws IOException {
		int i = i_par;
		if (i <= 251) {
			out.writeByte(i);
		} else {
			i -= 251;
			if (i >= 256) {
				if (i >>> 8 >= 256) {
					if (i >>> 16 >= 256) {
						out.writeByte(255);
					} else {
						out.writeByte(254);
					}
				} else {
					out.writeByte(253);
				}
			} else {
				out.writeByte(252);
			}
			while (i > 0) {
				out.writeByte((byte) i);
				i >>>= 8;
			}
		}
	}

	/**
	 * <p>writeLuposLiteral.</p>
	 *
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param out a {@link java.io.ObjectOutput} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposLiteral(final Literal literal, final ObjectOutput out) throws IOException {
		if (literal instanceof StringLiteral) {
			out.writeObject(((StringLiteral) literal).originalString());
		} else {
			OutHelper.writeLuposInt(((CodeMapLiteral) literal).getCode(), out);
		}
	}

	/**
	 * <p>writeLuposLiteral.</p>
	 *
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposLiteral(final Literal literal, final OutputStream os) throws IOException {
		LiteralFactory.writeLuposLiteral(literal, os);
	}

	/**
	 * <p>writeLuposEntry.</p>
	 *
	 * @param e a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @param <T> a T object.
	 * @throws java.io.IOException if any.
	 */
	public final static<T> void writeLuposEntry(final Entry<T> e, final OutputStream os) throws IOException {
		Registration.serializeWithId(e.e, os);
	}

	/**
	 * <p>writeLuposEntry.</p>
	 *
	 * @param e a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 * @param os a {@link lupos.io.LuposObjectOutputStream} object.
	 * @param <T> a T object.
	 * @throws java.io.IOException if any.
	 */
	public final static<T> void writeLuposEntry(final Entry<T> e, final LuposObjectOutputStream os) throws IOException {
		Registration.serializeWithId(e.e, os);
	}

	/**
	 * <p>writeLuposMapEntry.</p>
	 *
	 * @param t a {@link lupos.datastructures.dbmergesortedds.MapEntry} object.
	 * @param os a {@link lupos.io.LuposObjectOutputStream} object.
	 * @param <K> a K object.
	 * @param <V> a V object.
	 * @throws java.io.IOException if any.
	 */
	public final static<K, V> void writeLuposMapEntry(final lupos.datastructures.dbmergesortedds.MapEntry<K, V> t, final LuposObjectOutputStream os) throws IOException {
		Registration.serializeId(t.getKey(), os);
		Registration.serializeId(t.getValue(), os);
		if (t.getKey() instanceof String && (t.getValue() instanceof Triple)) {
			Registration.serializeWithoutId(t.getValue(), os);
			OutHelper.writeStringKey((String) t.getKey(), (Triple) t.getValue(), os);
		} else {
			Registration.serializeWithoutId(t.getValue(), os);
			Registration.serializeWithoutId(t.getKey(), os);
		}
	}

	/**
	 * <p>writeLuposMapEntry.</p>
	 *
	 * @param t a {@link lupos.datastructures.dbmergesortedds.MapEntry} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @param <K> a K object.
	 * @param <V> a V object.
	 * @throws java.io.IOException if any.
	 */
	public final static<K, V> void writeLuposMapEntry(final lupos.datastructures.dbmergesortedds.MapEntry<K, V> t, final OutputStream os) throws IOException {
		Registration.serializeId(t.getKey(), os);
		Registration.serializeId(t.getValue(), os);
		if (t.getKey() instanceof String && (t.getValue() instanceof Triple)) {
			Registration.serializeWithoutId(t.getValue(), os);
			OutHelper.writeStringKey((String) t.getKey(), (Triple) t.getValue(), os);
		} else {
			Registration.serializeWithoutId(t.getValue(), os);
			Registration.serializeWithoutId(t.getKey(), os);
		}
	}

	/**
	 * <p>writeStringKey.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	protected final static void writeStringKey(final String s, final Triple t, final OutputStream os) throws IOException {
		if (s.startsWith(t.getSubject().toString())) {
			if (s.compareTo(t.getSubject().toString() + t.getPredicate().toString() + t.getObject().toString()) == 0) {
				os.write(1);
				return;
			} else if (s.compareTo(t.getSubject().toString() + t.getObject().toString() + t.getPredicate().toString()) == 0) {
				os.write(2);
				return;
			}
		} else if (s.startsWith(t.getPredicate().toString())) {
			if (s.compareTo(t.getPredicate().toString() + t.getSubject().toString() + t.getObject().toString()) == 0) {
				os.write(3);
				return;
			} else if (s.compareTo(t.getPredicate().toString() + t.getObject().toString() + t.getSubject().toString()) == 0) {
				os.write(4);
				return;
			}
		} else if (s.startsWith(t.getObject().toString())) {
			if (s.compareTo(t.getObject().toString() + t.getSubject().toString() + t.getPredicate().toString()) == 0) {
				os.write(5);
				return;
			} else if (s.compareTo(t.getObject().toString() + t.getPredicate().toString() + t.getSubject().toString()) == 0) {
				os.write(6);
				return;
			}
		}
		os.write(0);
		OutHelper.writeLuposString(s, os);
	}

	/**
	 * <p>writeLuposBindings.</p>
	 *
	 * @param t a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param previousBindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposBindings(final Bindings t, final Bindings previousBindings, final OutputStream os) throws IOException {
		if (t instanceof BindingsArray) {
			final BindingsArray ba = (BindingsArray) t;
			if(previousBindings==null){
				writeLuposBindingsFactory(ba, os);
			}
			final Map<Variable, Integer> hm = ba.getBindingsFactory().getPosVariables();
			BigInteger usedVars = BigInteger.ZERO;
			BigInteger differentFromPreviousBindings = BigInteger.ZERO;
			BigInteger i = BigInteger.ONE;
			final BigInteger TWO = BigInteger.valueOf(2);
			for (final Variable v : hm.keySet()) {
				if (ba.get(v) != null) {
					usedVars = usedVars.add(i);
					if (previousBindings == null
							|| previousBindings.get(v) == null){
						differentFromPreviousBindings = differentFromPreviousBindings.add(i);
					} else if (ba.get(v) instanceof LazyLiteralOriginalContent) {
						if (!(previousBindings.get(v) instanceof LazyLiteralOriginalContent)
								|| ((LazyLiteralOriginalContent) ba.get(v))
								.getCodeOriginalContent() != ((LazyLiteralOriginalContent) previousBindings
										.get(v)).getCodeOriginalContent()) {
							differentFromPreviousBindings = differentFromPreviousBindings.add(i);
						}
					} else if (ba.get(v) instanceof LazyLiteral) {
						if (!(previousBindings.get(v) instanceof LazyLiteral)
								|| ((LazyLiteral) ba.get(v)).getCode() != ((LazyLiteral) previousBindings
										.get(v)).getCode()) {
							differentFromPreviousBindings = differentFromPreviousBindings.add(i);
						}
					} else if (ba.get(v).originalString().compareTo(
							previousBindings.get(v).originalString()) != 0) {
						differentFromPreviousBindings = differentFromPreviousBindings.add(i);
					}
				}
				i = i.multiply(TWO);
			}
			OutHelper.writeLuposBigInteger(usedVars, hm.size(), os);
			OutHelper.writeLuposBigInteger(differentFromPreviousBindings, hm.size(), os);
			for (final Variable v : hm.keySet()) {
				if (ba.get(v) != null) {
					if (previousBindings == null
							|| previousBindings.get(v) == null) {
						Registration.serializeWithoutId(ba.get(v), os);
					} else if (ba.get(v) instanceof LazyLiteralOriginalContent) {
						if (!(previousBindings.get(v) instanceof LazyLiteralOriginalContent)
								|| ((LazyLiteralOriginalContent) ba.get(v))
								.getCodeOriginalContent() != ((LazyLiteralOriginalContent) previousBindings
										.get(v)).getCodeOriginalContent()) {
							Registration.serializeWithoutId(ba.get(v), os);
						}
					} else if (ba.get(v) instanceof LazyLiteral) {
						if (!(previousBindings.get(v) instanceof LazyLiteral)
								|| ((LazyLiteral) ba.get(v)).getCode() != ((LazyLiteral) previousBindings
										.get(v)).getCode()) {
							Registration.serializeWithoutId(ba.get(v), os);
						}
					} else if (ba.get(v).originalString().compareTo(
							previousBindings.get(v).originalString()) != 0) {
						Registration.serializeWithoutId(ba.get(v), os);
					}
				}
			}
			OutHelper.writeAdditionalInformationOfBindings(ba, os);
		} else {
			OutHelper.writeOtherBindingsTypes(t, os);
		}
	}

	/**
	 * <p>writeLuposBindings.</p>
	 *
	 * @param t a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposBindings(final Bindings t, final OutputStream os) throws IOException {
		if (t instanceof BindingsArray) {
			final BindingsArray ba = (BindingsArray) t;
			writeLuposBindingsFactory(ba, os);
			final Map<Variable, Integer> hm = ba.getBindingsFactory().getPosVariables();
			BigInteger usedVars = BigInteger.ZERO;
			BigInteger i = BigInteger.ONE;
			final BigInteger TWO = BigInteger.valueOf(2);
			for (final Variable v : hm.keySet()) {
				if (ba.get(v) != null) {
					usedVars = usedVars.add(i);
				}
				i = i.multiply(TWO);
			}
			OutHelper.writeLuposBigInteger(usedVars, hm.size(), os);
			for (final Variable v : hm.keySet()) {
				if (ba.get(v) != null) {
					Registration.serializeWithoutId(ba.get(v), os);
				}
			}
			OutHelper.writeAdditionalInformationOfBindings(ba, os);
		} else {
			OutHelper.writeOtherBindingsTypes(t, os);
		}
	}

	private final static void writeLuposBindingsFactory(final BindingsArray t, final OutputStream os) throws IOException{
		final Variable[] vars = t.getBindingsFactory().getVariablesInOrder();
		writeLuposIntVariableBytes(vars.length, os);
		for(final Variable v: vars){
			writeLuposString(v.getName(), os);
		}
	}

	private final static void writeOtherBindingsTypes(final Bindings t, final OutputStream os) throws IOException {
		final Set<Variable> vars = t.getVariableSet();
		OutHelper.writeLuposIntVariableBytes(vars.size(), os);
		for (final Variable v : vars) {
			OutHelper.writeLuposString(v.getName(), os);
			Registration.serializeWithoutId(t.get(v), os);
		}
	}

	private final static void writeAdditionalInformationOfBindings(final Bindings ba, final OutputStream os) throws IOException {
		if (ba instanceof BindingsArrayReadTriples) {
			OutHelper.writeLuposIntVariableBytes(ba.getTriples().size(), os);
			for (final Triple tt : ba.getTriples()) {
				Registration.serializeWithoutId(tt, os);
			}
		}
		if (ba instanceof BindingsArrayVarMinMax) {
			((BindingsArrayVarMinMax) ba).writePresortingNumbers(os);
		}
	}

	/**
	 * <p>writeLuposTripleKey.</p>
	 *
	 * @param tk a {@link lupos.datastructures.items.TripleKey} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposTripleKey(final TripleKey tk, final OutputStream os) throws IOException {
		os.write(tk.getTripleComparator().getBytePattern());
		OutHelper.writeLuposTriple(tk.getTriple(), os);
	}

	/**
	 * <p>writeLuposTripleKey.</p>
	 *
	 * @param tk a {@link lupos.datastructures.items.TripleKey} object.
	 * @param previousTripleKey a {@link lupos.datastructures.items.TripleKey} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposTripleKey(final TripleKey tk, final TripleKey previousTripleKey, final OutputStream os) throws IOException {
		OutHelper.writeLuposTripleKey(tk,  previousTripleKey.getTriple(), os);
	}

	/**
	 * <p>writeLuposTripleKey.</p>
	 *
	 * @param tk a {@link lupos.datastructures.items.TripleKey} object.
	 * @param previousTriple a {@link lupos.datastructures.items.Triple} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposTripleKey(final TripleKey tk, final Triple previousTriple, final OutputStream os) throws IOException {
		os.write(tk.getTripleComparator().getBytePattern());
		OutHelper.writeLuposTriple(tk.getTriple(), previousTriple, os);
	}

	/**
	 * <p>writeLuposCollection.</p>
	 *
	 * @param t a {@link java.util.Collection} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final static void writeLuposCollection(final Collection t, final OutputStream os) throws IOException {
		if (t.size() > 200) {
			os.write(255);
			final DiskCollection dc;
			if (!t.isEmpty()) {
				dc = new DiskCollection(t.iterator().next().getClass());
			} else {
				dc = new DiskCollection(Object.class);
			}
			dc.addAll(t);
			dc.writeLuposObject(os);
		} else {
			os.write(t.size());
			if (!t.isEmpty()) {
				Registration.serializeId(t.iterator().next(), os);
				for (final Object o : t) {
					Registration.serializeWithoutId(o, os);
				}
			}
		}
	}

	/**
	 * <p>writeLuposSet.</p>
	 *
	 * @param t a {@link lupos.datastructures.smallerinmemorylargerondisk.SetImplementation} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	@SuppressWarnings("rawtypes")
	public final static void writeLuposSet(final SetImplementation t, final OutputStream os) throws IOException{
		OutHelper.writeLuposInt(t.size(), os);
		if (!t.isEmpty()){
			Registration.serializeId(t.iterator().next(), os);
		}
		for (final Object o : t) {
			Registration.serializeWithoutId(o, os);
		}
	}

	/**
	 * <p>writeLuposNodeInPartitionTree.</p>
	 *
	 * @param t a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposNodeInPartitionTree(final NodeInPartitionTree t, final OutputStream os) throws IOException {
		if (t instanceof LeafNodeInPartitionTree) {
			OutHelper.writeLuposByte((byte) 1, os);
			Registration.serializeWithoutId(((LeafNodeInPartitionTree) t).partition.getCollection(), os);
		} else if (t instanceof InnerNodeInPartitionTree) {
			OutHelper.writeLuposByte((byte) 2, os);
			Registration.serializeWithoutId(((InnerNodeInPartitionTree) t).nodes, os);
		}
	}

	/**
	 * <p>writeLuposVarBucket.</p>
	 *
	 * @param vb a {@link lupos.optimizations.logical.statistics.VarBucket} object.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposVarBucket(final VarBucket vb, final OutputStream os) throws IOException {
		OutHelper.writeLuposIntVariableBytes(vb.selectivityOfInterval.size(), os);
		if (vb.minimum == null) {
			if (vb.maximum == null) {
				OutHelper.writeLuposByte((byte) 0, os);
			} else {
				OutHelper.writeLuposByte((byte) 1, os);
				LiteralFactory.writeLuposLiteral(vb.maximum, os);
			}
		} else {
			if (vb.maximum == null) {
				OutHelper.writeLuposByte((byte) 2, os);
				LiteralFactory.writeLuposLiteral(vb.minimum, os);
			} else {
				OutHelper.writeLuposByte((byte) 3, os);
				LiteralFactory.writeLuposLiteral(vb.minimum, os);
				LiteralFactory.writeLuposLiteral(vb.maximum, os);
			}
		}
		for (final lupos.optimizations.logical.statistics.Entry entry : vb.selectivityOfInterval) {
			OutHelper.writeLuposLong(Double.doubleToLongBits(entry.distinctLiterals), os);
			OutHelper.writeLuposLong(Double.doubleToLongBits(entry.selectivity), os);
			LiteralFactory.writeLuposLiteral(entry.literal, os);
		}
	}

	/**
	 * <p>writeLuposVarBucketArray.</p>
	 *
	 * @param t an array of {@link lupos.optimizations.logical.statistics.VarBucket} objects.
	 * @param os a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public final static void writeLuposVarBucketArray(final VarBucket[] t, final OutputStream os) throws IOException {
		OutHelper.writeLuposIntVariableBytes(t.length, os);
		int nulls = 0;
		int counter = 1;
		for (final VarBucket vb : t) {
			if (vb == null) {
				nulls += counter;
			}
			counter *= 2;
		}
		OutHelper.writeLuposIntVariableBytes(nulls, os);
		for (final VarBucket vb : t) {
			if (vb != null) {
				OutHelper.writeLuposVarBucket(vb, os);
			}
		}
	}
}
