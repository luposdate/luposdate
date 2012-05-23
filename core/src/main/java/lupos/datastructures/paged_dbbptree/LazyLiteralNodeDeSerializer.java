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
package lupos.datastructures.paged_dbbptree;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.misc.BitVector;
import lupos.misc.Tuple;

public class LazyLiteralNodeDeSerializer implements
		NodeDeSerializer<TripleKey, Triple> {

	protected final RDF3XIndex.CollationOrder order;

	protected final static int[][] map = { { 0, 1, 2 }, // SPO
			{ 0, 2, 1 }, // SOP
			{ 1, 0, 2 }, // PSO
			{ 1, 2, 0 }, // POS
			{ 2, 0, 1 }, // OSP
			{ 2, 1, 0 } // OPS
	};

	public LazyLiteralNodeDeSerializer(
			final RDF3XIndex.CollationOrder order) {
		this.order = order;
	}

	public void writeInnerNodeEntry(final int fileName, final TripleKey key,
			final LuposObjectOutputStream out, final TripleKey lastKey)
			throws IOException {
		final BitVector bits = new BitVector(7);
		bits.set(0);
		final Triple lastValue = (lastKey == null) ? null : lastKey.getTriple();
		final Triple v = key.getTriple();
		final boolean mustWriteLazyLiteralOriginalContent = (v.getObject() instanceof LazyLiteralOriginalContent)
				&& (lastValue == null || !(lastValue.getObject().equals(v
						.getObject())));
		if (mustWriteLazyLiteralOriginalContent)
			bits.set(1);
		else
			bits.clear(1);
		int value = 0;
		if (lastValue == null)
			value = 3;
		else {
			for (int i = 0; i < 3; i++) {
				if (lastValue != null
						&& v.getPos(map[order.ordinal()][i]).equals(
								lastValue.getPos(map[order.ordinal()][i])))
					value++;
				else
					break;
			}
		}
		if (value <= 1) {
			bits.clear(2);
		} else
			bits.set(2);
		if (value % 2 == 0) {
			bits.clear(3);
		} else
			bits.set(3);

		int index = 3;

		for (int i = 0; i < 3; i++) {
			if (lastValue == null
					|| !v.getPos(map[order.ordinal()][i]).equals(
							lastValue.getPos(map[order.ordinal()][i]))) {
				if (lastValue != null) {
					// determine difference
					final int diff = ((LazyLiteral) v.getPos(map[order
							.ordinal()][i])).getCode()
							- ((LazyLiteral) lastValue.getPos(map[order
									.ordinal()][i])).getCode();
					index = determineNumberOfBytesForRepresentation(diff, bits,
							index, out);
				}
				for (int j = i + ((value == 3) ? 0 : 1); j < 3; j++) {
					// deal with the "rest"
					index = determineNumberOfBytesForRepresentation(
							((LazyLiteral) v.getPos(map[order.ordinal()][j]))
									.getCode(), bits, index, out);
				}
				break;
			}
		}
		if (mustWriteLazyLiteralOriginalContent)
			index = determineNumberOfBytesForRepresentation(
					((LazyLiteralOriginalContent) v.getObject())
							.getCodeOriginalContent(), bits, index, out);
		index = determineNumberOfBytesForRepresentation(fileName, bits, index,
				out);
		bits.writeWithoutSize(out);
		writeIntWithoutLeadingZeros(fileName, out);

		if (mustWriteLazyLiteralOriginalContent)
			writeIntWithoutLeadingZeros(((LazyLiteralOriginalContent) v
					.getObject()).getCodeOriginalContent(), out);

		for (int i = 0; i < 3; i++) {
			if (lastValue == null
					|| !v.getPos(map[order.ordinal()][i]).equals(
							lastValue.getPos(map[order.ordinal()][i]))) {
				if (lastValue != null) {
					// determine difference
					final int diff = ((LazyLiteral) v.getPos(map[order
							.ordinal()][i])).getCode()
							- ((LazyLiteral) lastValue.getPos(map[order
									.ordinal()][i])).getCode();
					writeIntWithoutLeadingZeros(diff, out);
				}
				for (int j = i + ((value == 3) ? 0 : 1); j < 3; j++) {
					// deal with the "rest"
					writeIntWithoutLeadingZeros(((LazyLiteral) v
							.getPos(map[order.ordinal()][j])).getCode(), out);
				}
				break;
			}
		}
	}

	public void writeInnerNodeEntry(final int fileName,
			final LuposObjectOutputStream out) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.clear(0);
		determineNumberOfBytesForRepresentation(fileName, bits, 0, out);
		bits.writeWithoutSize(out);
		writeIntWithoutLeadingZeros(fileName, out);
	}

	public void writeLeafEntry(final TripleKey k, final Triple v,
			final LuposObjectOutputStream out, final TripleKey lastKey,
			final Triple lastValue) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.set(0);
		final boolean mustWriteLazyLiteralOriginalContent = (v.getObject() instanceof LazyLiteralOriginalContent)
				&& (lastValue == null || !(lastValue.getObject().equals(v
						.getObject())));
		if (mustWriteLazyLiteralOriginalContent)
			bits.set(1);
		else
			bits.clear(1);
		int value = 0;
		if (lastValue == null)
			value = 3;
		else {
			for (int i = 0; i < 3; i++) {
					if(v.getPos(map[order.ordinal()][i]).equals(
								lastValue.getPos(map[order.ordinal()][i])))
						value++;
					else break;
			}
		}
		if (value <= 1) {
			bits.clear(2);
		} else
			bits.set(2);
		if (value % 2 == 0) {
			bits.clear(3);
		} else
			bits.set(3);

		int index = 3;

		for (int i = 0; i < 3; i++) {
			if (lastValue == null
					|| !v.getPos(map[order.ordinal()][i]).equals(
							lastValue.getPos(map[order.ordinal()][i]))) {
				if (lastValue != null) {
					// determine difference
					final int diff = ((LazyLiteral) v.getPos(map[order
							.ordinal()][i])).getCode()
							- ((LazyLiteral) lastValue.getPos(map[order
									.ordinal()][i])).getCode();
					index = determineNumberOfBytesForRepresentation(diff, bits,
							index, out);
				}
				for (int j = i + ((value == 3) ? 0 : 1); j < 3; j++) {
					// deal with the "rest"
					index = determineNumberOfBytesForRepresentation(
							((LazyLiteral) v.getPos(map[order.ordinal()][j]))
									.getCode(), bits, index, out);
				}
				break;
			}
		}
		if (mustWriteLazyLiteralOriginalContent)
			index = determineNumberOfBytesForRepresentation(
					((LazyLiteralOriginalContent) v.getObject())
							.getCodeOriginalContent(), bits, index, out);
		bits.writeWithoutSize(out);

		if (mustWriteLazyLiteralOriginalContent)
			writeIntWithoutLeadingZeros(((LazyLiteralOriginalContent) v
					.getObject()).getCodeOriginalContent(), out);

		for (int i = 0; i < 3; i++) {
			if (lastValue == null
					|| !v.getPos(map[order.ordinal()][i]).equals(
							lastValue.getPos(map[order.ordinal()][i]))) {
				if (lastValue != null) {
					// determine difference
					final int diff = ((LazyLiteral) v.getPos(map[order
							.ordinal()][i])).getCode()
							- ((LazyLiteral) lastValue.getPos(map[order
									.ordinal()][i])).getCode();
					writeIntWithoutLeadingZeros(diff, out);
				}
				for (int j = i + ((value == 3) ? 0 : 1); j < 3; j++) {
					// deal with the "rest"
					writeIntWithoutLeadingZeros(((LazyLiteral) v
							.getPos(map[order.ordinal()][j])).getCode(), out);
				}
				break;
			}
		}
	}

	public void writeLeafEntryNextFileName(final int filename,
			final LuposObjectOutputStream out) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.clear(0);
		determineNumberOfBytesForRepresentation(filename, bits, 0, out);
		bits.writeWithoutSize(out);
		writeIntWithoutLeadingZeros(filename, out);
	}

	protected static void writeIntWithoutLeadingZeros(final int diff,
			final LuposObjectOutputStream out) throws IOException {
		switch (determineNumberOfBytesForRepresentation(diff)) {
		case 0:
			out.writeLuposInt1Byte(diff);
			break;
		case 1:
			out.writeLuposInt2Bytes(diff);
			break;
		case 2:
			out.writeLuposInt3Bytes(diff);
			break;
		default:
		case 3:
			out.writeLuposInt(diff);
			break;
		}
	}

	protected static int determineNumberOfBytesForRepresentation(
			final int diff, final BitVector bits, int index,
			final LuposObjectOutputStream out) throws IOException {
		final int number = determineNumberOfBytesForRepresentation(diff);
		index++;
		if (index == 8) {
			bits.writeWithoutSize(out);
			index = 0;
		}
		if (number >= 2) {
			bits.set(index);
		} else
			bits.clear(index);
		index++;
		if (number % 2 == 0) {
			bits.clear(index);
		} else
			bits.set(index);
		return index;
	}

	protected static int determineNumberOfBytesForRepresentation(int diff) {
		if (diff < 0)
			diff *= -1;
		int number = 0;
		while (diff >= 256) {
			diff /= 256;
			number++;
		}
		return number;
	}

	protected static int readInt(final BitVector bv, final int index,
			final LuposObjectInputStream<Triple> in) throws IOException {
		if (bv.get(index)) {
			if (bv.get(index + 1)) {
				return in.readLuposInteger();
			} else {
				return in.readLuposInteger3Bytes();
			}
		} else {
			if (bv.get(index + 1)) {
				return in.readLuposInteger2Bytes();

			} else {
				return in.readLuposInteger1Byte();
			}
		}
	}

	protected static int getIntSize(final BitVector bits, int index,
			final LuposObjectInputStream<Triple> in) throws IOException {
		index++;
		if (index == 8) {
			bits.readWithoutSize(in, 7);
			index = 0;
		}
		int number = 0;
		if (bits.get(index)) {
			number = 2;
		}
		if (bits.get(index + 1))
			number += 1;
		return number;
	}

	protected static int getInt(final int number,
			final LuposObjectInputStream<Triple> in) throws IOException {
		switch (number) {
		case 0:
			return 0;
		case 1:
			return in.readLuposInteger1Byte();
		case 2:
			return in.readLuposInteger2Bytes();
		case 3:
			return in.readLuposInteger3Bytes();
		default:
		case 4:
			return in.readLuposInteger();
		}
	}

	public DBBPTreeEntry<TripleKey, Triple> getNextLeafEntry(
			final LuposObjectInputStream<Triple> in, final TripleKey lastKey,
			final Triple lastValue) {
		return getNextLeafEntry(lastValue, in);
	}

	private synchronized DBBPTreeEntry<TripleKey, Triple> getNextLeafEntry(
			final Triple lastTriple, final LuposObjectInputStream<Triple> in) {
		try {
			BitVector bits;
			try {
				bits = new BitVector(in, 7);
			} catch (final EOFException e) {
				return null;
			}
			if (!bits.get(0)) {
				return new DBBPTreeEntry<TripleKey, Triple>(null, null,
						readInt(bits, 1, in));
			}
			final int filenameOfNextLeafNode = -1;

			final boolean objectIsLazyLiteralOriginalContent = bits.get(1);
			int whereDifferentLiteral = 0;
			if (bits.get(2))
				whereDifferentLiteral = 2;
			if (bits.get(3))
				whereDifferentLiteral += 1;
			final Triple t = new Triple();
			if(whereDifferentLiteral==3 && lastTriple!=null){
				for(int i=0; i<3; i++)
					t.setPos(i, lastTriple.getPos(i));
			} else {
				final int numberDifferent = (whereDifferentLiteral==3)?3:3 - whereDifferentLiteral;
				final int[] numberBytesForInt = new int[numberDifferent];
				int index = 3;
				for (int i = 0; i < numberDifferent; i++) {
					numberBytesForInt[i] = getIntSize(bits, index, in);
					index = (index + 2) % 8;
				}
				int codeForOriginalContent = 0;
				if (objectIsLazyLiteralOriginalContent) {
					codeForOriginalContent = getInt(
							getIntSize(bits, index, in) + 1, in);
				}

				int index2 = 0;
				for (int i = 0; i < 3; i++) {
					if (i < whereDifferentLiteral && whereDifferentLiteral != 3) {
						t.setPos(map[order.ordinal()][i], lastTriple
								.getPos(map[order.ordinal()][i]));
					} else {
						if (whereDifferentLiteral != 3) {
							final int diff = getInt(
									numberBytesForInt[index2++] + 1, in);
							t.setPos(map[order.ordinal()][i], getLiteral(diff
									+ ((LazyLiteral) lastTriple.getPos(map[order
											.ordinal()][i])).getCode(), map[order
									.ordinal()][i], codeForOriginalContent,
									objectIsLazyLiteralOriginalContent));
						}
						for (int j = i + ((whereDifferentLiteral != 3) ? 1 : 0); j < 3; j++) {
							final int code = getInt(
									numberBytesForInt[index2++] + 1, in);
							t.setPos(map[order.ordinal()][j], getLiteral(code,
									map[order.ordinal()][j],
									codeForOriginalContent,
									objectIsLazyLiteralOriginalContent));
						}
						break;
					}
				}
			}
			return new DBBPTreeEntry<TripleKey, Triple>(new TripleKey(t,
					new TripleComparator(order)), t, filenameOfNextLeafNode);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
		}
		return null;
	}

	public synchronized Tuple<TripleKey, Integer> getNextInnerNodeEntry(
			final TripleKey lastKey, final LuposObjectInputStream<Triple> in) {
		try {
			BitVector bits;
			try {
				bits = new BitVector(in, 7);
			} catch (final EOFException e) {
				return null;
			}
			if (!bits.get(0)) {
				return new Tuple<TripleKey, Integer>(null, readInt(bits, 1, in));
			}
			final Triple lastTriple = (lastKey == null) ? null : lastKey
					.getTriple();

			final boolean objectIsLazyLiteralOriginalContent = bits.get(1);
			int whereDifferentLiteral = 0;
			if (bits.get(2))
				whereDifferentLiteral = 2;
			if (bits.get(3))
				whereDifferentLiteral += 1;
			final int numberDifferent = (whereDifferentLiteral == 3) ? 3
					: 3 - whereDifferentLiteral;
			final int[] numberBytesForInt = new int[numberDifferent];
			int index = 3;
			for (int i = 0; i < numberDifferent; i++) {
				numberBytesForInt[i] = getIntSize(bits, index, in);
				index = (index + 2) % 8;
			}
			int codeForOriginalContent = 0;
			if (objectIsLazyLiteralOriginalContent) {
				codeForOriginalContent = getInt(
						getIntSize(bits, index, in) + 1, in);
				index = (index + 2) % 8;
			}

			final int fileName = getInt(getIntSize(bits, index, in) + 1, in);

			final Triple t = new Triple();
			int index2 = 0;
			for (int i = 0; i < 3; i++) {
				if (i < whereDifferentLiteral && whereDifferentLiteral != 3) {
					t.setPos(map[order.ordinal()][i], lastTriple
							.getPos(map[order.ordinal()][i]));
				} else {
					if (whereDifferentLiteral != 3) {
						final int diff = getInt(
								numberBytesForInt[index2++] + 1, in);
						t.setPos(map[order.ordinal()][i], getLiteral(diff
								+ ((LazyLiteral) lastTriple.getPos(map[order
										.ordinal()][i])).getCode(), map[order
								.ordinal()][i], codeForOriginalContent,
								objectIsLazyLiteralOriginalContent));
					}
					for (int j = i + ((whereDifferentLiteral != 3) ? 1 : 0); j < 3; j++) {
						final int code = getInt(
								numberBytesForInt[index2++] + 1, in);
						t.setPos(map[order.ordinal()][j], getLiteral(code,
								map[order.ordinal()][j],
								codeForOriginalContent,
								objectIsLazyLiteralOriginalContent));
					}
					break;
				}
			}
			return new Tuple<TripleKey, Integer>(new TripleKey(t,
					new TripleComparator(order)), fileName);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
		}
		return null;
	}

	protected static Literal getLiteral(final int code, final int pos,
			final int codeForOriginalContent,
			final boolean objectIsLazyLiteralOriginalContent) {
		if (!objectIsLazyLiteralOriginalContent || pos != 2) {
			return new LazyLiteral(code);
		} else {
			return new LazyLiteralOriginalContent(code, codeForOriginalContent);
		}
	}
}
