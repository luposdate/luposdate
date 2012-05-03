package lupos.misc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Iterator;

import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public final class BitVector implements Comparable<BitVector>, Iterable<Boolean> {
	public byte[] bits;
	private int size;
	private int count = -1;

	public BitVector(final LuposObjectInputStream in, final int n) throws IOException {
		readWithoutSize(in, n);
	}

	/** Constructs a vector capable of holding <code>n</code> bits. */
	public BitVector(final int n) {
		size = n;
		bits = new byte[(size >> 3) + 1];
	}

	/** Sets the value of <code>bit</code> to one. */
	public final void set(final int bit) {
		bits[bit >> 3] |= 1 << (bit & 7);
		count = -1;
	}

	/** Sets the value of <code>bit</code> according to <code>value</code>. */
	public final void set(final int bit, final boolean value) {
		if (value)
			set(bit);
		else
			clear(bit);
	}

	/** Sets the value of <code>bit</code> to zero. */
	public final void clear(final int bit) {
		bits[bit >> 3] &= ~(1 << (bit & 7));
		count = -1;
	}

	/**
	 * Returns <code>true</code> if <code>bit</code> is one and
	 * <code>false</code> if it is zero.
	 */
	public final boolean get(final int bit) {
		return (bits[bit >> 3] & (1 << (bit & 7))) != 0;
	}

	/**
	 * Returns the number of bits in this vector. This is also one greater than
	 * the number of the largest valid bit number.
	 */
	public final int size() {
		return size;
	}

	/**
	 * Perform bitwise OR between this vector and the given one. Neither of the
	 * two vectors is altered during the operation.
	 * 
	 * @param vectorParam
	 * @return resulting vector of the bitwise OR operation
	 */
	public final BitVector OR(final BitVector vectorParam) {
		final int minBitSize = (this.size < vectorParam.size ? this.size
				: vectorParam.size);
		final int minByteSize = minBitSize >> 3;
		final BitVector result = new BitVector(minBitSize);

		for (int i = 0; i < minByteSize; i++) {
			result.bits[i] = (byte) (this.bits[i] | vectorParam.bits[i]);
		}

		return result;
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
			if ((bits[i] & vectorParam.bits[i]) != 0)
				return true;
		}

		return false;
	}

	/**
	 * Set all bits of this bit vector to one.
	 * 
	 */
	public final void setAllOne() {
		for (int i = 0; i < bits.length; i++) {
			bits[i] |= ~0;
		}
		count = -1;
	}

	/**
	 * Returns the total number of one bits in this vector. This is efficiently
	 * computed and cached, so that, if the vector is not changed, no
	 * recomputation is done for repeated calls.
	 */
	public final int count() {
		if (count == -1) {
			int c = 0;
			final int end = bits.length;
			for (int i = 0; i < end; i++)
				c += BYTE_COUNTS[bits[i] & 0xFF]; // sum bits per byte
			count = c;
		}
		return count;
	}

	private static final byte[] BYTE_COUNTS = { // table of bits/byte
		0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2,
		3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4,
		5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 1, 2, 2, 3, 2,
		3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4,
		5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3,
		4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3, 3,
		4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4,
		5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4,
		5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3,
		4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6,
		7, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5,
		6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8 };

	public int getNextBit(int start) {
		while (start % 8 != 0) {
			if (start > size)
				return -1;
			if (get(start))
				return start;
			start++;
		}
		int bytenr = start >> 3;
		if (bytenr >= bits.length)
			return -1;
		while (bits[bytenr] == 0) {
			bytenr++;
			if (bytenr >= bits.length)
				return -1;
		}
		int result = (bytenr << 3);
		while (true) {
			if (result >= size)
				return -1;
			if (get(result))
				return result;
			result++;
		}
	}

	/**
	 * Writes this vector to the file <code>name</code> in Directory
	 * <code>d</code>, in a format that can be read by the constructor
	 * {@link #BitVector(Directory, String)}.
	 */
	public final void write(final String name) throws IOException {
		final ObjectOutputStream output = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(name)));
		try {
			output.writeInt(size()); // write size
			output.writeInt(count()); // write count
			output.write(bits); // write bits
		} finally {
			output.close();
		}
	}

	public final void writeWithoutSize(final LuposObjectOutputStream out)
	throws IOException {
		for (int i = 0; i < bits.length; i++)
			out.writeLuposByte(bits[i]);
	}

	public final void readWithoutSize(final LuposObjectInputStream in,
			final int n) throws IOException {
		size = n;
		bits = new byte[(size >> 3) + 1];
		for (int i = 0; i < bits.length; i++)
			bits[i] = in.readLuposByte();
	}

	public final void writeWithoutSize(final OutputStream out)
	throws IOException {
		for (int i = 0; i < bits.length; i++)
			out.write(bits[i] + 128);
	}

	public final void readWithoutSize(final InputStream in, final int n)
	throws IOException {
		size = n;
		bits = new byte[(size >> 3) + 1];
		for (int i = 0; i < bits.length; i++)
			bits[i] = (byte) (in.read() - 128);
	}

	/**
	 * Constructs a bit vector from the file <code>name</code> in Directory
	 * <code>d</code>, as written by the {@link #write} method.
	 */
	public BitVector(final String name) throws IOException {
		final ObjectInputStream input = new ObjectInputStream(
				new BufferedInputStream(new FileInputStream(name)));
		try {
			size = input.readInt(); // read size
			count = input.readInt(); // read count
			bits = new byte[(size >> 3) + 1]; // allocate bits
			input.read(bits); // read bits
		} finally {
			input.close();
		}
	}

	@Override
	public String toString() {
		String s = "";
		for (int i = 0; i < size; i++) {
			s += get(i) ? "1" : "0";
		}
		return s;
	}

	public long getLong() {
		long result = 0;
		for (int i = this.size - 1; i >= 0; i--) {
			result <<= 1;
			if (get(i)) {
				result += 1;
			}
		}
		return result;
	}

	public void setAccordingToLongValue(long value) {
		int bit = 0;
		while (value > 0 && bit < size) {
			set(bit, value % 2 == 1);
			bit++;
			value >>= 1;
		}
	}

	public int compareTo(final BitVector o) {
		final int max = Math.max(o.size, size);
		for (int j = max; j >= 0; j--) {
			final boolean b1 = (size >= j) ? get(j) : false;
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
			if (get(i)) {
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
			if (bigInt.mod(TWO).compareTo(BigInteger.ZERO) > 0)
				bv.set(i);
			bigInt = bigInt.shiftRight(1);
		}
		return bv;
	}

	public Iterator<Boolean> iterator() {
		return new Iterator<Boolean>() {
			int index = 0;

			public boolean hasNext() {
				return index < size;
			}

			public Boolean next() {
				if (hasNext())
					return get(index++);
				else
					return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
