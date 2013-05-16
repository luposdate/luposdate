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

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.codemap.CodeMapLiteral;
import lupos.datastructures.items.literal.string.StringLiteral;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;

public class LuposObjectOutputStream extends ObjectOutputStream {

	protected OutputStream os;

	public LuposObjectOutputStream() throws IOException {
	}

	public LuposObjectOutputStream(final OutputStream arg0) throws IOException {
		super(arg0);
		this.os = arg0;
	}

	protected Literal 	lastSubject = null,
						lastPredicate = null,
						lastObject = null;

	protected byte[] lastString = null;

	public void writeLuposObject(final Object arg0) throws IOException {
		Registration.serializeWithoutId(arg0, this);
	}

	protected void writeLuposVarBucket(final VarBucket vb) throws IOException {
		this.writeLuposInt(vb.selectivityOfInterval.size());
		if (vb.minimum == null) {
			if (vb.maximum == null) {
				this.writeLuposByte((byte) 0);
			} else {
				this.writeLuposByte((byte) 1);
				LiteralFactory.writeLuposLiteral(vb.maximum, this);
			}
		} else {
			if (vb.maximum == null) {
				this.writeLuposByte((byte) 2);
				LiteralFactory.writeLuposLiteral(vb.minimum, this);
			} else {
				this.writeLuposByte((byte) 3);
				LiteralFactory.writeLuposLiteral(vb.minimum, this);
				LiteralFactory.writeLuposLiteral(vb.maximum, this);
			}
		}
		for (final lupos.optimizations.logical.statistics.Entry entry : vb.selectivityOfInterval) {
			this
					.writeLuposLong(Double
							.doubleToLongBits(entry.distinctLiterals));
			this.writeLuposLong(Double.doubleToLongBits(entry.selectivity));
			LiteralFactory.writeLuposLiteral(entry.literal, this);
		}
	}

	public void writeLuposTriplePattern(final TriplePattern tp)
			throws IOException {
		int id;
		final Integer alreadyid = LuposObjectInputStream.triplePatternHashMapID
				.get(tp);
		if (alreadyid != null) {
			id = alreadyid.byteValue();
		} else {
			id = LuposObjectInputStream.triplePatternID;
			LuposObjectInputStream.triplePatternID++;
			LuposObjectInputStream.triplePatternHashMapID.put(tp, id);
			LuposObjectInputStream.triplePatternHashMap.put(id, tp);
		}
		this.writeLuposByte((byte) id);
	}

	protected Bindings previousBindings = null;

	public void writeLuposTriple(final Triple t) throws IOException {
		int diff = 0;
		if (this.lastSubject == null
				|| t.getSubject() instanceof LazyLiteral
				&& !t.getSubject().equals(this.lastSubject)
				|| !(t.getSubject() instanceof LazyLiteral)
				&& t.getSubject().originalString().compareTo(
						this.lastSubject.originalString()) != 0) {
			diff = 1;
			this.lastSubject = t.getSubject();
		}
		if (this.lastPredicate == null
				|| t.getPredicate() instanceof LazyLiteral
				&& !t.getPredicate().equals(this.lastPredicate)
				|| !(t.getPredicate() instanceof LazyLiteral)
				&& t.getPredicate().originalString().compareTo(
						this.lastPredicate.originalString()) != 0) {
			diff += 2;
			this.lastPredicate = t.getPredicate();
		}
		if (this.lastObject == null
				|| t.getObject() instanceof LazyLiteral
				&& !t.getObject().equals(this.lastObject)
				|| !(t.getObject() instanceof LazyLiteral)
				&& t.getObject().originalString().compareTo(
						this.lastObject.originalString()) != 0) {
			diff += 4;
			this.lastObject = t.getObject();
		}
		this.os.write(diff);
		if (diff % 2 == 1) {
			this.writeLuposObject(t.getSubject());
		}
		if ((diff / 2) % 2 == 1) {
			this.writeLuposObject(t.getPredicate());
		}
		if ((diff / 4) % 2 == 1) {
			this.writeLuposObject(t.getObject());
		}
	}

	protected void writeStringKey(final String s) throws IOException {
		if (s.startsWith(this.lastSubject.toString())) {
			if (s.compareTo(this.lastSubject.toString() + this.lastPredicate.toString()
					+ this.lastObject.toString()) == 0) {
				this.os.write(1);
				return;
			} else if (s.compareTo(this.lastSubject.toString()
					+ this.lastObject.toString() + this.lastPredicate.toString()) == 0) {
				this.os.write(2);
				return;
			}
		} else if (s.startsWith(this.lastPredicate.toString())) {
			if (s.compareTo(this.lastPredicate.toString() + this.lastSubject.toString()
					+ this.lastObject.toString()) == 0) {
				this.os.write(3);
				return;
			} else if (s.compareTo(this.lastPredicate.toString()
					+ this.lastObject.toString() + this.lastSubject.toString()) == 0) {
				this.os.write(4);
				return;
			}
		} else if (s.startsWith(this.lastObject.toString())) {
			if (s.compareTo(this.lastObject.toString() + this.lastSubject.toString()
					+ this.lastPredicate.toString()) == 0) {
				this.os.write(5);
				return;
			} else if (s.compareTo(this.lastObject.toString()
					+ this.lastPredicate.toString() + this.lastSubject.toString()) == 0) {
				this.os.write(6);
				return;
			}
		}
		this.os.write(0);
		this.writeLuposString(s);
	}

	public void writeLuposDifferenceString(final String s) throws IOException {
		if (s == null){
			return;
		}
		if(this.lastString==null){
			this.lastString = s.getBytes(LuposObjectInputStream.UTF8);
			this.writeLuposString(s);
			return;
		}
		final byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
		// determine common prefix of new string and last stored string
		int common = 0;
		while(common<bytesOfS.length && common < this.lastString.length && bytesOfS[common]==this.lastString[common]){
			common++;
		}
		this.writeLuposIntVariableBytes(common);

		// now write only difference string
		final int length = bytesOfS.length;
		this.writeLuposIntVariableBytes(length-common);
		this.os.write(bytesOfS, common, length-common);
		this.lastString = bytesOfS;
	}

	public void writeLuposString(final String s) throws IOException {
		if (s == null){
			return;
		}
		final byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
		final int length = bytesOfS.length;
		this.writeLuposIntVariableBytes(length);
		this.os.write(bytesOfS);
	}

	public void writeLuposBoolean(final boolean flag) throws IOException {
		if (flag){
			this.os.write(0);
		} else {
			this.os.write(1);
		}
	}

	public void writeLuposInt(final int i) throws IOException {
		int remaining = i;
		this.os.write((byte) remaining);
		remaining>>>=8;
		this.os.write((byte) remaining);
		remaining>>>=8;
		this.os.write((byte) remaining);
		remaining>>>=8;
		this.os.write((byte) remaining);
	}

	public void writeLuposBigInteger(final BigInteger value, final int numberOfBits) throws IOException {
		int remainingBits = numberOfBits;
		BigInteger remainingValue = value;
		final BigInteger BYTE = BigInteger.valueOf(256);
		while(remainingBits>0){
			final BigInteger[] result = remainingValue.divideAndRemainder(BYTE);
			remainingValue = result[0];
			this.os.write((byte) result[1].intValue());
			remainingBits-=8;
		}
	}

	public void writeLuposInt1Byte(final int i) throws IOException {
		this.os.write((byte) i);
	}

	public void writeLuposInt2Bytes(final int i) throws IOException {
		int remaining = i;
		this.os.write((byte) remaining);
		remaining>>>=8;
		this.os.write((byte) remaining);
	}

	public void writeLuposInt3Bytes(final int i) throws IOException {
		int remaining = i;
		this.os.write((byte) remaining);
		remaining>>>=8;
		this.os.write((byte) remaining);
		remaining>>>=8;
		this.os.write((byte) remaining);
	}

	public void writeLuposLong(final long l) throws IOException {
		this.writeLuposInt((int) l);
		this.writeLuposInt((int) (l >>> 32));
	}

	public void writeLuposByte(final byte b) throws IOException {
		this.os.write(b);
	}

	public void writeLuposIntVariableBytes(final int i_par) throws IOException {
		int i = i_par;
		if (i <= 251) {
			this.writeLuposInt1Byte(i);
		} else {
			i -= 251;
			if (i >= 256) {
				if (i >>> 8 >= 256) {
					if (i >>> 16 >= 256) {
						this.writeLuposInt1Byte(255);
					} else {
						this.writeLuposInt1Byte(254);
					}
				} else {
					this.writeLuposInt1Byte(253);
				}
			} else {
				this.writeLuposInt1Byte(252);
			}
			while (i > 0) {
				this.writeLuposInt1Byte((byte)i);
				i >>>= 8;
			}
		}
	}

	public static void writeLuposInt(final int i_par, final ObjectOutput out) throws IOException {
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

	public static void writeLuposLiteral(final Literal literal,
			final ObjectOutput out) throws IOException {
		if (literal instanceof StringLiteral) {
			out.writeObject(((StringLiteral) literal).originalString());
		} else {
			LuposObjectOutputStream.writeLuposInt(((CodeMapLiteral) literal).getCode(), out);
		}
	}
}
