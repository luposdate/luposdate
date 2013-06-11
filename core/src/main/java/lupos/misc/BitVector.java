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
package lupos.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Iterator;

import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.util.ImmutableIterator;

public final class BitVector implements Comparable<BitVector>, Iterable<Boolean> {

	public byte[] bits;
	private int size;

	public BitVector(final LuposObjectInputStream in, final int n) throws IOException {
		this.readWithoutSize(in, n);
	}

	public BitVector(final InputStream in, final int n) throws IOException {
		this.readWithoutSize(in, n);
	}

	/** Constructs a vector for storing n bits */
	public BitVector(final int n) {
		this.size = n;
		this.bits = new byte[(this.size >> 3) + 1];
	}

	/** Sets a bit */
	public final void set(final int bit) {
		this.bits[bit >> 3] |= 1 << (bit & 7);
	}

	/** Sets or clears a bit according to a boolean value */
	public final void set(final int bit, final boolean value) {
		if (value){
			this.set(bit);
		} else {
			this.clear(bit);
		}
	}

	/** Clears a bit */
	public final void clear(final int bit) {
		this.bits[bit >> 3] &= ~(1 << (bit & 7));
	}

	/**
	 * Returns true if the bit is set or false if it is cleared
	 */
	public final boolean get(final int bit) {
		return (this.bits[bit >> 3] & (1 << (bit & 7))) != 0;
	}

	/**
	 * Returns the number of bits in this vector (one greater than
	 * the number of the largest valid bit number)
	 */
	public final int size() {
		return this.size;
	}

	/**
	 * Check whether this and the given bit vector have at least one bit in
	 * common by performing a logical AND operation on each pair of bits. If
	 * both vectors have different size and the smallest vector has size n, then
	 * only the first n bits of both vectors are compared.
	 *
	 * @param vectorParam
	 * @return true if both vectors have at least one bit in common
	 */
	public final boolean oneBitInCommon(final BitVector vectorParam) {
		final int minSize = (this.bits.length < vectorParam.bits.length ? this.bits.length
				: vectorParam.bits.length);

		for (int i = 0; i < minSize; i++) {
			if ((this.bits[i] & vectorParam.bits[i]) != 0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @return the number of bits, which are set
	 */
	public final int count() {
		int count = 0;
		for(final Boolean bit: this){
			if(bit){
				count++;
			}
		}
		return count;
	}


	public int getNextBit(int start) {
		while (start % 8 != 0) {
			if (start > this.size) {
				return -1;
			}
			if (this.get(start)) {
				return start;
			}
			start++;
		}
		int bytenr = start >> 3;
		if (bytenr >= this.bits.length) {
			return -1;
		}
		while (this.bits[bytenr] == 0) {
			bytenr++;
			if (bytenr >= this.bits.length) {
				return -1;
			}
		}
		int result = (bytenr << 3);
		while (true) {
			if (result >= this.size) {
				return -1;
			}
			if (this.get(result)) {
				return result;
			}
			result++;
		}
	}

	public final void writeWithoutSize(final LuposObjectOutputStream out) throws IOException {
		for (int i = 0; i < this.bits.length; i++){
			OutHelper.writeLuposByte(this.bits[i], out.os);
		}
	}

	public final void readWithoutSize(final LuposObjectInputStream in, final int n) throws IOException {
		this.size = n;
		this.bits = new byte[(this.size >> 3) + 1];
		for (int i = 0; i < this.bits.length; i++){
			this.bits[i] = InputHelper.readLuposByte(in.is);
		}
	}

	public final void writeWithoutSize(final OutputStream out) throws IOException {
		for (int i = 0; i < this.bits.length; i++){
			OutHelper.writeLuposByte(this.bits[i], out);
		}
	}

	public final void readWithoutSize(final InputStream in, final int n) throws IOException {
		this.size = n;
		this.bits = new byte[(this.size >> 3) + 1];
		for (int i = 0; i < this.bits.length; i++){
			this.bits[i] = InputHelper.readLuposByte(in);
		}
	}

	@Override
	public String toString() {
		String s = "";
		for (final boolean bit: this) {
			s += bit ? "1" : "0";
		}
		return s;
	}

	public long getLong() {
		long result = 0;
		for (int i = this.size - 1; i >= 0; i--) {
			result <<= 1;
			if (this.get(i)) {
				result += 1;
			}
		}
		return result;
	}

	public void setAccordingToLongValue(long value) {
		int bit = 0;
		while (value > 0 && bit < this.size) {
			this.set(bit, value % 2 == 1);
			bit++;
			value >>= 1;
		}
	}

	@Override
	public int compareTo(final BitVector o) {
		final int max = Math.max(o.size, this.size);
		for (int j = max; j >= 0; j--) {
			final boolean b1 = (this.size >= j) ? this.get(j) : false;
			final boolean b2 = (o.size >= j) ? o.get(j) : false;
			if (b1 != b2) {
				return b1 ? 1 : -1;
			}
		}
		return 0;
	}

	public BigInteger getBigInteger() {
		BigInteger result = BigInteger.ZERO;
		for (int i = this.size - 1; i >= 0; i--) {
			result = result.shiftLeft(1);
			if (this.get(i)) {
				result = result.add(BigInteger.ONE);
			}
		}
		return result;
	}

	public static BitVector getBitVector(BigInteger bigInt,
			final int numberOfBits) {
		final BigInteger TWO = BigInteger.valueOf(2);
		final BitVector bv = new BitVector(numberOfBits);
		for (int i = 0; i < numberOfBits; i++) {
			if (bigInt.mod(TWO).compareTo(BigInteger.ZERO) > 0) {
				bv.set(i);
			}
			bigInt = bigInt.shiftRight(1);
		}
		return bv;
	}

	@Override
	public Iterator<Boolean> iterator() {
		return new ImmutableIterator<Boolean>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < BitVector.this.size;
			}

			@Override
			public Boolean next() {
				if (this.hasNext()) {
					return BitVector.this.get(this.index++);
				} else {
					return null;
				}
			}
		};
	}
}
