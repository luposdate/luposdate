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
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

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
		os = arg0;
	}

	protected Literal lastSubject = null;
	protected Literal lastPredicate = null, lastObject = null;

	public void writeLuposObject(final Object arg0) throws IOException {
		Registration.serializeWithoutId(arg0, this);
	}

	protected void writeLuposVarBucket(final VarBucket vb) throws IOException {
		writeLuposInt(vb.selectivityOfInterval.size());
		if (vb.minimum == null) {
			if (vb.maximum == null) {
				writeLuposByte((byte) 0);
			} else {
				writeLuposByte((byte) 1);
				LiteralFactory.writeLuposLiteral(vb.maximum, this);
			}
		} else {
			if (vb.maximum == null) {
				writeLuposByte((byte) 2);
				LiteralFactory.writeLuposLiteral(vb.minimum, this);
			} else {
				writeLuposByte((byte) 3);
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
		writeLuposByte((byte) id);
	}

	protected Bindings previousBindings = null;

	public void writeLuposTriple(final Triple t) throws IOException {
		int diff = 0;
		if (lastSubject == null
				|| t.getSubject() instanceof LazyLiteral
				&& !t.getSubject().equals(lastSubject)
				|| !(t.getSubject() instanceof LazyLiteral)
				&& t.getSubject().originalString().compareTo(
						lastSubject.originalString()) != 0) {
			diff = 1;
			lastSubject = t.getSubject();
		}
		if (lastPredicate == null
				|| t.getPredicate() instanceof LazyLiteral
				&& !t.getPredicate().equals(lastPredicate)
				|| !(t.getPredicate() instanceof LazyLiteral)
				&& t.getPredicate().originalString().compareTo(
						lastPredicate.originalString()) != 0) {
			diff += 2;
			lastPredicate = t.getPredicate();
		}
		if (lastObject == null
				|| t.getObject() instanceof LazyLiteral
				&& !t.getObject().equals(lastObject)
				|| !(t.getObject() instanceof LazyLiteral)
				&& t.getObject().originalString().compareTo(
						lastObject.originalString()) != 0) {
			diff += 4;
			lastObject = t.getObject();
		}
		os.write(diff);
		if (diff % 2 == 1)
			writeLuposObject(t.getSubject());
		if ((diff / 2) % 2 == 1)
			writeLuposObject(t.getPredicate());
		if ((diff / 4) % 2 == 1)
			writeLuposObject(t.getObject());
	}

	protected void writeStringKey(final String s) throws IOException {
		if (s.startsWith(lastSubject.toString())) {
			if (s.compareTo(lastSubject.toString() + lastPredicate.toString()
					+ lastObject.toString()) == 0) {
				os.write(1);
				return;
			} else if (s.compareTo(lastSubject.toString()
					+ lastObject.toString() + lastPredicate.toString()) == 0) {
				os.write(2);
				return;
			}
		} else if (s.startsWith(lastPredicate.toString())) {
			if (s.compareTo(lastPredicate.toString() + lastSubject.toString()
					+ lastObject.toString()) == 0) {
				os.write(3);
				return;
			} else if (s.compareTo(lastPredicate.toString()
					+ lastObject.toString() + lastSubject.toString()) == 0) {
				os.write(4);
				return;
			}
		} else if (s.startsWith(lastObject.toString())) {
			if (s.compareTo(lastObject.toString() + lastSubject.toString()
					+ lastPredicate.toString()) == 0) {
				os.write(5);
				return;
			} else if (s.compareTo(lastObject.toString()
					+ lastPredicate.toString() + lastSubject.toString()) == 0) {
				os.write(6);
				return;
			}
		}
		os.write(0);
		writeLuposString(s);
	}

	public void writeLuposString(final String s) throws IOException {
		if (s == null){
			return;
		}
		byte[] bytesOfS = s.getBytes(LuposObjectInputStream.UTF8);
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
		final int i1 = i % 256;
		final int i2 = (i / 256) % 256;
		final int i3 = (i / (256 * 256)) % 256;
		final int i4 = (i / (256 * 256 * 256)) % 256;
		this.os.write(i1);
		this.os.write(i2);
		this.os.write(i3);
		this.os.write(i4);
	}
	
	public void writeLuposBigInteger(final BigInteger value, final int numberOfBits) throws IOException {
		int remainingBits = numberOfBits;
		BigInteger remainingValue = value;
		final BigInteger BYTE = BigInteger.valueOf(256);
		while(remainingBits>0){
			BigInteger[] result = remainingValue.divideAndRemainder(BYTE);
			remainingValue = result[0];
			this.os.write(result[1].intValue()%256);
			remainingBits-=8;
		}
	}
	
	public void writeLuposInt1Byte(final int i) throws IOException {
		os.write(i % 256);
	}

	public void writeLuposInt2Bytes(final int i) throws IOException {
		final int i1 = i % 256;
		final int i2 = (i / 256) % 256;
		os.write(i1);
		os.write(i2);
	}

	public void writeLuposInt3Bytes(final int i) throws IOException {
		final int i1 = i % 256;
		final int i2 = (i / 256) % 256;
		final int i3 = (i / (256 * 256)) % 256;
		os.write(i1);
		os.write(i2);
		os.write(i3);
	}

	public void writeLuposLong(final long l) throws IOException {
		writeLuposInt((int) (l % ((long) 256 * 256 * 256 * 256)));
		writeLuposInt((int) (l / ((long) 256 * 256 * 256 * 256)));
	}

	public void writeLuposByte(final byte b) throws IOException {
		os.write(b);
	}
	
	public void writeLuposIntVariableBytes(int i_par) throws IOException {
		int i = i_par; 
		if (i <= 251) {
			this.writeLuposInt1Byte(i);
		} else {
			i -= 251;
			if (i >= 256) {
				if (i / 256 >= 256) {
					if (i / (256 * 256) >= 256) {
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
				this.writeLuposInt1Byte(i % 256);
				i /= 256;
			}
		}
	}

	public static void writeLuposInt(int i_par, final ObjectOutput out) throws IOException {
		int i = i_par;
		if (i <= 251) {
			out.writeByte(i);
		} else {
			i -= 251;
			if (i >= 256) {
				if (i / 256 >= 256) {
					if (i / (256 * 256) >= 256) {
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
				out.writeByte(i % 256);
				i /= 256;
			}
		}
	}

	public static void writeLuposLiteral(final Literal literal,
			final ObjectOutput out) throws IOException {
		if (literal instanceof StringLiteral)
			out.writeObject(((StringLiteral) literal).originalString());
		else
			LuposObjectOutputStream.writeLuposInt(((CodeMapLiteral) literal).getCode(), out);
	}

	private static Collection<String> splitStringInto64KBUTFBlocks(
			final String value) throws IOException {
		String remaining = value;
		final LinkedList<String> result = new LinkedList<String>();
		final int len = value.length();
		long sum = 0;
		int i = 0;
		while (i < remaining.length()) {
			final char c = remaining.charAt(i);
			if (c >= '\u0001' && c <= '\u007f')
				sum += 1;
			else if (c == '\u0000' || (c >= '\u0080' && c <= '\u07ff'))
				sum += 2;
			else
				sum += 3;
			if (sum >= 65500) {
				result.add(remaining.substring(0, i));
				remaining = remaining.substring(i);
				sum = 0;
				i = 0;
			}
			++i;
		}

		if (remaining.length() > 0)
			result.add(remaining);
		return result;
	}
}
