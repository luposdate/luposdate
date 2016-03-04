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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.dbmergesortedds.DiskCollection;
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
import lupos.io.Registration;
import lupos.optimizations.logical.statistics.VarBucket;
public final class LengthHelper {
	private LengthHelper() {
	}

	/**
	 * <p>lengthLuposTriple.</p>
	 *
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @param previousTriple a {@link lupos.datastructures.items.Triple} object.
	 * @return a int.
	 */
	public final static int lengthLuposTriple(final Triple t, final Triple previousTriple) {
		int result = 1;
		if (previousTriple.getSubject() == null
				|| t.getSubject() instanceof LazyLiteral
				&& !t.getSubject().equals(previousTriple.getSubject())
				|| !(t.getSubject() instanceof LazyLiteral)
				&& t.getSubject().originalString().compareTo(
						previousTriple.getSubject().originalString()) != 0) {
			result += LengthHelper.lengthLuposLiteral(t.getSubject());
		}
		if (previousTriple.getPredicate() == null
				|| t.getPredicate() instanceof LazyLiteral
				&& !t.getPredicate().equals(previousTriple.getPredicate())
				|| !(t.getPredicate() instanceof LazyLiteral)
				&& t.getPredicate().originalString().compareTo(
						previousTriple.getPredicate().originalString()) != 0) {
			result += LengthHelper.lengthLuposLiteral(t.getPredicate());
		}
		if (previousTriple.getObject() == null
				|| t.getObject() instanceof LazyLiteral
				&& !t.getObject().equals(previousTriple.getObject())
				|| !(t.getObject() instanceof LazyLiteral)
				&& t.getObject().originalString().compareTo(
						previousTriple.getObject().originalString()) != 0) {
			result += LengthHelper.lengthLuposLiteral(t.getObject());
		}
		return result;
	}

	/**
	 * <p>lengthLuposTriple.</p>
	 *
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @return a int.
	 */
	public final static int lengthLuposTriple(final Triple t) {
		return 	LengthHelper.lengthLuposLiteral(t.getSubject()) +
				LengthHelper.lengthLuposLiteral(t.getPredicate()) +
				LengthHelper.lengthLuposLiteral(t.getObject());
	}

	/**
	 * <p>lengthLuposString.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param previousString a {@link java.lang.String} object.
	 * @return a int.
	 */
	public final static int lengthLuposString(final String s, final String previousString) {
		try {
			return LengthHelper.lengthLuposDifferenceString(s, previousString.getBytes(LuposObjectInputStream.UTF8));
		} catch (final UnsupportedEncodingException e) {
			System.err.println(e);
			e.printStackTrace();
			return 0;
		}
	}


	/**
	 * <p>lengthLuposDifferenceString.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param previousString an array of byte.
	 * @return the byte array of the just written string (to be kept and used for next call to this method)
	 */
	public final static int lengthLuposDifferenceString(final String s, final byte[] previousString) {
		if (s == null){
			return 0;
		}
		if(previousString==null){
			return LengthHelper.lengthLuposString(s);
		}
		try {
			final byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
			// determine common prefix of new string and last stored string
			int common = 0;
			while(common<bytesOfS.length && common < previousString.length && bytesOfS[common]==previousString[common]){
				common++;
			}

			// now write only difference string
			final int length = bytesOfS.length;
			return LengthHelper.lengthLuposIntVariableBytes(common) + LengthHelper.lengthLuposIntVariableBytes(length-common) + (length-common);
		} catch(final Exception e){
			System.err.println(e);
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * <p>lengthLuposString.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return a int.
	 */
	public final static int lengthLuposString(final String s) {
		if (s == null){
			return 0;
		}
		try {
			final byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
			final int length = bytesOfS.length;
			return LengthHelper.lengthLuposIntVariableBytes(length) + length;
		} catch(final Exception e){
			System.err.println(e);
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * <p>lengthLuposBoolean.</p>
	 *
	 * @return a int.
	 */
	public final static int lengthLuposBoolean() {
		return 1;
	}

	/**
	 * <p>lengthLuposInt.</p>
	 *
	 * @return a int.
	 */
	public final static int lengthLuposInt() {
		return 4;
	}

	/**
	 * <p>lengthLuposBigInteger.</p>
	 *
	 * @param numberOfBits a int.
	 * @return a int.
	 */
	public final static int lengthLuposBigInteger(final int numberOfBits) {
		return (numberOfBits / 8) + ((numberOfBits%8==0)?0:1);
	}

	/**
	 * <p>lengthLuposInt1Byte.</p>
	 *
	 * @return a int.
	 */
	public final static int lengthLuposInt1Byte() {
		return 1;
	}

	/**
	 * <p>lengthLuposInt2Bytes.</p>
	 *
	 * @return a int.
	 */
	public final static int lengthLuposInt2Bytes() {
		return 2;
	}

	/**
	 * <p>lengthLuposInt3Bytes.</p>
	 *
	 * @return a int.
	 */
	public final static int lengthLuposInt3Bytes() {
		return 3;	}

	/**
	 * <p>lengthLuposLong.</p>
	 *
	 * @return a int.
	 */
	public final static int lengthLuposLong() {
		return 8;
	}

	/**
	 * <p>lengthLuposByte.</p>
	 *
	 * @return a int.
	 */
	public final static int lengthLuposByte() {
		return 1;
	}

	/**
	 * <p>lengthLuposIntVariableBytes.</p>
	 *
	 * @param i_par a int.
	 * @return a int.
	 */
	public final static int lengthLuposIntVariableBytes(final int i_par) {
		int i = i_par;
		if (i <= 251) {
			return LengthHelper.lengthLuposInt1Byte();
		} else {
			int result = 1;
			while (i > 0) {
				result += LengthHelper.lengthLuposInt1Byte();
				i >>>= 8;
			}
			return result;
		}
	}

	/**
	 * <p>lengthLuposInt.</p>
	 *
	 * @param i_par a int.
	 * @return a int.
	 */
	public final static int lengthLuposInt(final int i_par) {
		int i = i_par;
		if (i <= 251) {
			return 1;
		} else {
			int result = 1;
			while (i > 0) {
				result++;
				i >>>= 8;
			}
			return result;
		}
	}

	/**
	 * <p>lengthLuposLiteralDataOutputStream.</p>
	 *
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a int.
	 */
	public final static int lengthLuposLiteralDataOutputStream(final Literal literal) {
		if (literal instanceof StringLiteral) {
			return LengthHelper.lengthLuposString(((StringLiteral) literal).originalString());
		} else {
			return LengthHelper.lengthLuposInt(((CodeMapLiteral) literal).getCode());
		}
	}

	/**
	 * <p>lengthLuposLiteral.</p>
	 *
	 * @param literal a {@link lupos.datastructures.items.literal.Literal} object.
	 * @return a int.
	 */
	public final static int lengthLuposLiteral(final Literal literal) {
		return LiteralFactory.lengthLuposLiteral(literal);
	}

	/**
	 * <p>lengthLuposMapEntry.</p>
	 *
	 * @param t a {@link lupos.datastructures.dbmergesortedds.MapEntry} object.
	 * @param <K> a K object.
	 * @param <V> a V object.
	 * @return a int.
	 */
	public final static<K, V> int lengthLuposMapEntry(final lupos.datastructures.dbmergesortedds.MapEntry<K, V> t) {
		final int result = 2*LengthHelper.lengthLuposByte();
		if (t.getKey() instanceof String && (t.getValue() instanceof Triple)) {
			return result + Registration.lengthSerializeWithoutId(t.getValue())+ LengthHelper.lengthStringKey((String) t.getKey(), (Triple) t.getValue());
		} else {
			return result + Registration.lengthSerializeWithoutId(t.getValue()) + Registration.lengthSerializeWithoutId(t.getKey());
		}
	}

	/**
	 * <p>lengthStringKey.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param t a {@link lupos.datastructures.items.Triple} object.
	 * @return a int.
	 */
	protected final static int lengthStringKey(final String s, final Triple t) {
		if (s.startsWith(t.getSubject().toString())) {
			if (s.compareTo(t.getSubject().toString() + t.getPredicate().toString() + t.getObject().toString()) == 0) {
				return 1;
			} else if (s.compareTo(t.getSubject().toString() + t.getObject().toString() + t.getPredicate().toString()) == 0) {
				return 1;
			}
		} else if (s.startsWith(t.getPredicate().toString())) {
			if (s.compareTo(t.getPredicate().toString() + t.getSubject().toString() + t.getObject().toString()) == 0) {
				return 1;
			} else if (s.compareTo(t.getPredicate().toString() + t.getObject().toString() + t.getSubject().toString()) == 0) {
				return 1;
			}
		} else if (s.startsWith(t.getObject().toString())) {
			if (s.compareTo(t.getObject().toString() + t.getSubject().toString() + t.getPredicate().toString()) == 0) {
				return 1;
			} else if (s.compareTo(t.getObject().toString() + t.getPredicate().toString() + t.getSubject().toString()) == 0) {
				return 1;
			}
		}
		return 1 + LengthHelper.lengthLuposString(s);
	}

	/**
	 * <p>lengthLuposBindings.</p>
	 *
	 * @param t a {@link lupos.datastructures.bindings.Bindings} object.
	 * @param previousBindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a int.
	 */
	public final static int lengthLuposBindings(final Bindings t, final Bindings previousBindings) {
		if (t instanceof BindingsArray) {
			final BindingsArray ba = (BindingsArray) t;
			int result = (ba==null)? lengthLuposBindingsFactory(ba) : 0;
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
			result += 2 * LengthHelper.lengthLuposBigInteger(hm.size());
			for (final Variable v : hm.keySet()) {
				if (ba.get(v) != null) {
					if (previousBindings == null
							|| previousBindings.get(v) == null) {
						result += Registration.lengthSerializeWithoutId(ba.get(v));
					} else if (ba.get(v) instanceof LazyLiteralOriginalContent) {
						if (!(previousBindings.get(v) instanceof LazyLiteralOriginalContent)
								|| ((LazyLiteralOriginalContent) ba.get(v))
								.getCodeOriginalContent() != ((LazyLiteralOriginalContent) previousBindings
										.get(v)).getCodeOriginalContent()) {
							result += Registration.lengthSerializeWithoutId(ba.get(v));
						}
					} else if (ba.get(v) instanceof LazyLiteral) {
						if (!(previousBindings.get(v) instanceof LazyLiteral)
								|| ((LazyLiteral) ba.get(v)).getCode() != ((LazyLiteral) previousBindings
										.get(v)).getCode()) {
							result += Registration.lengthSerializeWithoutId(ba.get(v));
						}
					} else if (ba.get(v).originalString().compareTo(
							previousBindings.get(v).originalString()) != 0) {
						result += Registration.lengthSerializeWithoutId(ba.get(v));
					}
				}
			}
			return result + LengthHelper.lengthAdditionalInformationOfBindings(ba);
		} else {
			return LengthHelper.lengthOtherBindingsTypes(t);
		}
	}

	/**
	 * <p>lengthLuposBindings.</p>
	 *
	 * @param t a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a int.
	 */
	public final static int lengthLuposBindings(final Bindings t) {
		if (t instanceof BindingsArray) {
			final BindingsArray ba = (BindingsArray) t;
			int result = lengthLuposBindingsFactory(ba);
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
			result += LengthHelper.lengthLuposBigInteger(hm.size());

			for (final Variable v : hm.keySet()) {
				if (ba.get(v) != null) {
					result += Registration.lengthSerializeWithoutId(ba.get(v));
				}
			}
			return result + LengthHelper.lengthAdditionalInformationOfBindings(ba);
		} else {
			return LengthHelper.lengthOtherBindingsTypes(t);
		}
	}

	private final static int lengthLuposBindingsFactory(final BindingsArray t){
		final Set<Variable> vars = t.getBindingsFactory().getPosVariables().keySet();
		int result = lengthLuposIntVariableBytes(vars.size());
		for(final Variable v: vars){
			result += lengthLuposString(v.getName());
		}
		return result;
	}

	private final static int lengthOtherBindingsTypes(final Bindings t) {
		final Set<Variable> vars = t.getVariableSet();
		int result = LengthHelper.lengthLuposInt(vars.size());
		for (final Variable v : vars) {
			result += LengthHelper.lengthLuposString(v.getName());
			result += Registration.lengthSerializeWithoutId(t.get(v));
		}
		return result;
	}

	private final static int lengthAdditionalInformationOfBindings(final Bindings ba) {
		int result = 0;
		if (ba instanceof BindingsArrayReadTriples) {
			result += LengthHelper.lengthLuposInt(ba.getTriples().size());
			for (final Triple tt : ba.getTriples()) {
				result += Registration.lengthSerializeWithoutId(tt);
			}
		}
		if (ba instanceof BindingsArrayVarMinMax) {
			result += ((BindingsArrayVarMinMax) ba).lengthPresortingNumbers();
		}
		return result;
	}

	/**
	 * <p>lengthLuposTripleKey.</p>
	 *
	 * @param tk a {@link lupos.datastructures.items.TripleKey} object.
	 * @return a int.
	 */
	public final static int lengthLuposTripleKey(final TripleKey tk){
		return LengthHelper.lengthLuposByte() + LengthHelper.lengthLuposTriple(tk.getTriple());
	}

	/**
	 * <p>lengthLuposTripleKey.</p>
	 *
	 * @param tk a {@link lupos.datastructures.items.TripleKey} object.
	 * @param previousTripleKey a {@link lupos.datastructures.items.TripleKey} object.
	 * @return a int.
	 */
	public final static int lengthLuposTripleKey(final TripleKey tk, final TripleKey previousTripleKey){
		return LengthHelper.lengthLuposByte() + LengthHelper.lengthLuposTriple(tk.getTriple(), previousTripleKey.getTriple());
	}

	/**
	 * <p>lengthLuposCollection.</p>
	 *
	 * @param t a {@link java.util.Collection} object.
	 * @return a int.
	 */
	@SuppressWarnings("rawtypes")
	public final static int lengthLuposCollection(final Collection t){
		int result = 1;
		if (t.size() > 200) {
			// be careful: this sometimes leads to errors if between
			// this determination of the length and really writing
			// the collection other collections are written...
			// locking is in these cases needed
			result += DiskCollection.lengthLuposObjectOfNextDiskCollection();
		} else {
			if (!t.isEmpty()) {
				result += Registration.lengthSerializeId();
				for (final Object o : t) {
					result += Registration.lengthSerializeWithoutId(o);
				}
			}
		}
		return result;
	}

	/**
	 * <p>lengthLuposSet.</p>
	 *
	 * @param t a {@link lupos.datastructures.smallerinmemorylargerondisk.SetImplementation} object.
	 * @return a int.
	 */
	@SuppressWarnings("rawtypes")
	public final static int lengthLuposSet(final SetImplementation t){
		int result = LengthHelper.lengthLuposInt();
		if (!t.isEmpty()){
			result += Registration.lengthSerializeId();
		}
		for (final Object o : t) {
			result += Registration.lengthSerializeWithoutId(o);
		}
		return result;
	}

	/**
	 * <p>lengthLuposNodeInPartitionTree.</p>
	 *
	 * @param t a {@link lupos.engine.operators.multiinput.join.NodeInPartitionTree} object.
	 * @return a int.
	 */
	public final static int lengthLuposNodeInPartitionTree(final NodeInPartitionTree t){
		int result = LengthHelper.lengthLuposByte();
		if (t instanceof LeafNodeInPartitionTree) {
			result += Registration.lengthSerializeWithoutId(((LeafNodeInPartitionTree) t).partition.getCollection());
		} else if (t instanceof InnerNodeInPartitionTree) {
			result += Registration.lengthSerializeWithoutId(((InnerNodeInPartitionTree) t).nodes);
		}
		return result;
	}

	/**
	 * <p>lengthLuposVarBucket.</p>
	 *
	 * @param vb a {@link lupos.optimizations.logical.statistics.VarBucket} object.
	 * @return a int.
	 */
	public final static int lengthLuposVarBucket(final VarBucket vb) {
		int result = LengthHelper.lengthLuposInt(vb.selectivityOfInterval.size());
		if (vb.minimum == null) {
			result += LengthHelper.lengthLuposByte();
			if (vb.maximum != null) {
				result += LiteralFactory.lengthLuposLiteral(vb.maximum);
			}
		} else {
			result += LengthHelper.lengthLuposByte();
			if (vb.maximum == null) {
				result += LiteralFactory.lengthLuposLiteral(vb.minimum);
			} else {
				result += LiteralFactory.lengthLuposLiteral(vb.minimum);
				result += LiteralFactory.lengthLuposLiteral(vb.maximum);
			}
		}
		for (final lupos.optimizations.logical.statistics.Entry entry : vb.selectivityOfInterval) {
			result += 2* LengthHelper.lengthLuposLong() + LiteralFactory.lengthLuposLiteral(entry.literal);
		}
		return result;
	}

	/**
	 * <p>lengthLuposVarBucketArray.</p>
	 *
	 * @param t an array of {@link lupos.optimizations.logical.statistics.VarBucket} objects.
	 * @return a int.
	 */
	public final static int lengthLuposVarBucketArray(final VarBucket[] t) {
		int result = LengthHelper.lengthLuposIntVariableBytes(t.length);
		int nulls = 0;
		int counter = 1;
		for (final VarBucket vb : t) {
			if (vb == null) {
				nulls += counter;
			}
			counter *= 2;
		}
		result += LengthHelper.lengthLuposIntVariableBytes(nulls);
		for (final VarBucket vb : t) {
			if (vb != null) {
				result += LengthHelper.lengthLuposVarBucket(vb);
			}
		}
		return result;
	}
}
