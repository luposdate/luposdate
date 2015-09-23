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
package lupos.datastructures.paged_dbbptree.node.nodedeserializer;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.misc.BitVector;
import lupos.misc.Tuple;
public class IntArrayDBBPTreeStatisticsNodeDeSerializer extends
		IntArrayNodeDeSerializer implements
		NodeDeSerializer<int[], int[]> {

	private final static String UnsupportedOperationExceptionMessage = "Currently no updates allowed on DBBPTree for fast histogram computation!";

	/**
	 * <p>Constructor for LazyLiteralDBBPTreeStatisticsNodeDeSerializer.</p>
	 *
	 * @param order a {@link lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder} object.
	 */
	public IntArrayDBBPTreeStatisticsNodeDeSerializer(
			final RDF3XIndexScan.CollationOrder order) {
		super(order);
	}

	/** {@inheritDoc} */
	@Override
	public Tuple<int[], Integer> getNextInnerNodeEntry(
			final int[] lastKey, final InputStream in) {
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
				return new Tuple<int[], Integer>(null, fileName);
			}
			final int[] lastTriple = lastKey;

			final boolean objectIsLazyLiteralOriginalContent = bits.get(1);
			int whereDifferentLiteral = 0;
			if (bits.get(2)) {
				whereDifferentLiteral = 2;
			}
			if (bits.get(3)) {
				whereDifferentLiteral += 1;
			}
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

			final int[] t = new int[3];
			int index2 = 0;
			for (int i = 0; i < 3; i++) {
				if (i < whereDifferentLiteral && whereDifferentLiteral != 3) {
					t[map[this.order.ordinal()][i]] = lastTriple[map[this.order.ordinal()][i]];
				} else {
					if (whereDifferentLiteral != 3) {
						final int diff = getInt(numberBytesForInt[index2++] + 1, in);
						t[map[this.order.ordinal()][i]] = diff + lastTriple[map[this.order.ordinal()][i]];
					}
					for (int j = i + ((whereDifferentLiteral != 3) ? 1 : 0); j < 3; j++) {
						final int code = getInt(
								numberBytesForInt[index2++] + 1, in);
						t[map[this.order.ordinal()][j]] = code;
					}
					break;
				}
			}
			return new Tuple<int[], Integer>(t, fileName);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void writeInnerNodeEntry(final int fileName, final int[] key,
			final OutputStream out, final int[] lastKey)
			throws IOException {
		// TODO
		throw new UnsupportedOperationException(
				UnsupportedOperationExceptionMessage);
	}

	/** {@inheritDoc} */
	@Override
	public void writeInnerNodeEntry(final int fileName,
			final OutputStream out) throws IOException {
		// TODO
		throw new UnsupportedOperationException(
				UnsupportedOperationExceptionMessage);
	}
}
