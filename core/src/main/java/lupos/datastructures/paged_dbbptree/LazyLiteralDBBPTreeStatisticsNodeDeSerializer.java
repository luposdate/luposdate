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
package lupos.datastructures.paged_dbbptree;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.misc.BitVector;
import lupos.misc.Tuple;

public class LazyLiteralDBBPTreeStatisticsNodeDeSerializer extends
		LazyLiteralNodeDeSerializer implements
		NodeDeSerializer<TripleKey, Triple> {

	private final static String UnsupportedOperationExceptionMessage = "Currently no updates allowed on DBBPTree for fast histogram computation!";

	public LazyLiteralDBBPTreeStatisticsNodeDeSerializer(
			final RDF3XIndexScan.CollationOrder order) {
		super(order);
	}

	@Override
	public Tuple<TripleKey, Integer> getNextInnerNodeEntry(
			final TripleKey lastKey, final LuposObjectInputStream<Triple> in) {
		try {
			BitVector bits;
			try {
				bits = new BitVector(in, 7);
			} catch (final EOFException e) {
				return null;
			}
			if (!bits.get(0)) {

				final boolean subjectDifferentFromPreviousTriple = bits.get(1);
				final boolean predicateDifferentFromPreviousTriple = bits
						.get(2);
				final boolean objectDifferentFromPreviousTriple = bits.get(3);
				int index = 3;
				final int filenameSize = getIntSize(bits, index, in);
				index = (index + 2) % 8;
				final int numberOfTriplesSize = getIntSize(bits, index, in);
				index = (index + 2) % 8;
				final int numberDistinctSubjectsSize = getIntSize(bits, index,
						in);
				index = (index + 2) % 8;
				final int numberDistinctPredicatesSize = getIntSize(bits,
						index, in);
				index = (index + 2) % 8;
				final int numberDistinctObjectsSize = getIntSize(bits, index,
						in);
				index = (index + 2) % 8;
				final int fileName = getInt(filenameSize + 1, in);
				final int numberOfTriples = getInt(numberOfTriplesSize + 1, in);
				final int numberDistinctSubjects = getInt(
						numberDistinctSubjectsSize + 1, in);
				final int numberDistinctPredicates = getInt(
						numberDistinctPredicatesSize + 1, in);
				final int numberDistinctObjects = getInt(
						numberDistinctObjectsSize + 1, in);
				return new Tuple<TripleKey, Integer>(null, fileName);
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

			int index = 7;
			for (int i = 0; i < numberDifferent; i++) {
				numberBytesForInt[i] = getIntSize(bits, index, in);
				index = (index + 2) % 8;
			}
			int codeForOriginalContent = 0;
			int originalContentSize = 0;
			if (objectIsLazyLiteralOriginalContent) {
				originalContentSize = getIntSize(bits, index, in);
				index = (index + 2) % 8;
			}

			final int filenameSize = getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int numberOfTriplesSize = getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int numberDistinctSubjectsSize = getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int numberDistinctPredicatesSize = getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int numberDistinctObjectsSize = getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int fileName = getInt(filenameSize + 1, in);
			final int numberOfTriples = getInt(numberOfTriplesSize + 1, in);
			final int numberDistinctSubjects = getInt(
					numberDistinctSubjectsSize + 1, in);
			final int numberDistinctPredicates = getInt(
					numberDistinctPredicatesSize + 1, in);
			final int numberDistinctObjects = getInt(
					numberDistinctObjectsSize + 1, in);

			if (objectIsLazyLiteralOriginalContent) {
				codeForOriginalContent = getInt(originalContentSize + 1, in);
			}

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

	@Override
	public void writeInnerNodeEntry(final int fileName, final TripleKey key,
			final LuposObjectOutputStream out, final TripleKey lastKey)
			throws IOException {
		// TODO
		throw new UnsupportedOperationException(
				UnsupportedOperationExceptionMessage);
	}

	@Override
	public void writeInnerNodeEntry(final int fileName,
			final LuposObjectOutputStream out) throws IOException {
		// TODO
		throw new UnsupportedOperationException(
				UnsupportedOperationExceptionMessage);
	}
}
