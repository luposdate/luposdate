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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.buffermanager.PageInputStream;
import lupos.datastructures.buffermanager.PageOutputStream;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.datastructures.items.TripleComparator.COMPARE;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LazyLiteralOriginalContent;
import lupos.datastructures.paged_dbbptree.node.DBBPTreeEntry;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.LazyLiteralDBBPTreeStatisticsNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.LazyLiteralNodeDeSerializer;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.BitVector;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.VarBucket;

public class LazyLiteralTripleKeyDBBPTreeStatistics extends DBBPTree<TripleKey, Triple> {

	protected final RDF3XIndexScan.CollationOrder order;

	public LazyLiteralTripleKeyDBBPTreeStatistics(
			final Comparator<? super TripleKey> comparator, final int k,
			final int k_, final RDF3XIndexScan.CollationOrder order)
			throws IOException {
		super(comparator, k, k_, new LazyLiteralDBBPTreeStatisticsNodeDeSerializer(order));
		this.order = order;
	}

	protected final static int[][] map = { { 0, 1, 2 }, // SPO
			{ 0, 2, 1 }, // SOP
			{ 1, 0, 2 }, // PSO
			{ 1, 2, 0 }, // POS
			{ 2, 0, 1 }, // OSP
			{ 2, 1, 0 } // OPS
	};

	public void writeInnerNodeEntry(final int fileName, final TripleKey key,
			final OutputStream out, final TripleKey lastKey,
			final int numberOfTriples, final int numberDistinctSubjects,
			final int numberDistinctPredicates,
			final int numberDistinctObjects,
			final boolean subjectDifferentFromPreviousTriple,
			final boolean predicateDifferentFromPreviousTriple,
			final boolean objectDifferentFromPreviousTriple) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.set(0);
		final Triple lastValue = (lastKey == null) ? null : lastKey.getTriple();
		final Triple v = key.getTriple();
		final boolean mustWriteLazyLiteralOriginalContent = (v.getObject() instanceof LazyLiteralOriginalContent)
				&& (lastValue == null || !(lastValue.getObject().equals(v
						.getObject())));
		if (mustWriteLazyLiteralOriginalContent) {
			bits.set(1);
		} else {
			bits.clear(1);
		}
		int value = 0;
		if (lastValue == null) {
			value = 3;
		} else {
			for (int i = 0; i < 3; i++) {
				if (v.getPos(map[this.order.ordinal()][i]).equals(
						lastValue.getPos(map[this.order.ordinal()][i]))) {
					value++;
				} else {
					break;
				}
			}
		}
		if (value <= 1) {
			bits.clear(2);
		} else {
			bits.set(2);
		}
		if (value % 2 == 0) {
			bits.clear(3);
		} else {
			bits.set(3);
		}

		if (subjectDifferentFromPreviousTriple) {
			bits.set(4);
		} else {
			bits.clear(4);
		}
		if (predicateDifferentFromPreviousTriple) {
			bits.set(5);
		} else {
			bits.clear(5);
		}
		if (objectDifferentFromPreviousTriple) {
			bits.set(6);
		} else {
			bits.clear(6);
		}

		int index = 7;

		for (int i = 0; i < 3; i++) {
			if (lastValue == null
					|| !v.getPos(map[this.order.ordinal()][i]).equals(
							lastValue.getPos(map[this.order.ordinal()][i]))) {
				if (lastValue != null) {
					// determine difference
					final int diff = ((LazyLiteral) v.getPos(map[this.order
							.ordinal()][i])).getCode()
							- ((LazyLiteral) lastValue.getPos(map[this.order
									.ordinal()][i])).getCode();
					index = LazyLiteralNodeDeSerializer
							.determineNumberOfBytesForRepresentation(diff,
									bits, index, out);
				}
				for (int j = i + ((value == 3) ? 0 : 1); j < 3; j++) {
					// deal with the "rest"
					index = LazyLiteralNodeDeSerializer
							.determineNumberOfBytesForRepresentation(
									((LazyLiteral) v
											.getPos(map[this.order.ordinal()][j]))
											.getCode(), bits, index, out);
				}
				break;
			}
		}
		if (mustWriteLazyLiteralOriginalContent) {
			index = LazyLiteralNodeDeSerializer
					.determineNumberOfBytesForRepresentation(
							((LazyLiteralOriginalContent) v.getObject())
									.getCodeOriginalContent(), bits, index, out);
		}
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(fileName, bits, index,
						out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(numberOfTriples, bits,
						index, out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(
						numberDistinctSubjects, bits, index, out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(
						numberDistinctPredicates, bits, index, out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(numberDistinctObjects,
						bits, index, out);
		bits.writeWithoutSize(out);

		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(fileName, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberOfTriples, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberDistinctSubjects, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberDistinctPredicates, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberDistinctObjects, out);

		if (mustWriteLazyLiteralOriginalContent) {
			LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
					((LazyLiteralOriginalContent) v.getObject())
							.getCodeOriginalContent(), out);
		}

		for (int i = 0; i < 3; i++) {
			if (lastValue == null
					|| !v.getPos(map[this.order.ordinal()][i]).equals(
							lastValue.getPos(map[this.order.ordinal()][i]))) {
				if (lastValue != null) {
					// determine difference
					final int diff = ((LazyLiteral) v.getPos(map[this.order
							.ordinal()][i])).getCode()
							- ((LazyLiteral) lastValue.getPos(map[this.order
									.ordinal()][i])).getCode();
					LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
							diff, out);
				}
				for (int j = i + ((value == 3) ? 0 : 1); j < 3; j++) {
					// deal with the "rest"
					LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
							((LazyLiteral) v.getPos(map[this.order.ordinal()][j]))
									.getCode(), out);
				}
				break;
			}
		}
	}

	public void writeInnerNodeEntry(final int fileName,
			final int numberOfTriples, final int numberDistinctSubjects,
			final int numberDistinctPredicates,
			final int numberDistinctObjects,
			final boolean subjectDifferentFromPreviousTriple,
			final boolean predicateDifferentFromPreviousTriple,
			final boolean objectDifferentFromPreviousTriple,
			final OutputStream out) throws IOException {
		final BitVector bits = new BitVector(7);
		bits.clear(0);
		if (subjectDifferentFromPreviousTriple) {
			bits.set(1);
		} else {
			bits.clear(1);
		}
		if (predicateDifferentFromPreviousTriple) {
			bits.set(2);
		} else {
			bits.clear(2);
		}
		if (objectDifferentFromPreviousTriple) {
			bits.set(3);
		} else {
			bits.clear(3);
		}
		int index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(fileName, bits, 3, out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(numberOfTriples, bits,
						index, out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(
						numberDistinctSubjects, bits, index, out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(
						numberDistinctPredicates, bits, index, out);
		index = LazyLiteralNodeDeSerializer
				.determineNumberOfBytesForRepresentation(numberDistinctObjects,
						bits, index, out);
		bits.writeWithoutSize(out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(fileName, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberOfTriples, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberDistinctSubjects, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberDistinctPredicates, out);
		LazyLiteralNodeDeSerializer.writeIntWithoutLeadingZeros(
				numberDistinctObjects, out);
	}

	protected class InnerNodeEntry {
		final TripleKey key;
		final int fileName;
		final int numberOfTriples;
		final int numberDistinctSubjects;
		final int numberDistinctPredicates;
		final int numberDistinctObjects;
		final boolean subjectDifferentFromPreviousTriple;
		final boolean predicateDifferentFromPreviousTriple;
		final boolean objectDifferentFromPreviousTriple;

		public InnerNodeEntry(final TripleKey key, final int fileName,
				final int numberOfTriples, final int numberDistinctSubjects,
				final int numberDistinctPredicates,
				final int numberDistinctObjects,
				final boolean subjectDifferentFromPreviousTriple,
				final boolean predicateDifferentFromPreviousTriple,
				final boolean objectDifferentFromPreviousTriple) {
			this.key = key;
			this.fileName = fileName;
			this.numberOfTriples = numberOfTriples;
			this.numberDistinctSubjects = numberDistinctSubjects;
			this.numberDistinctPredicates = numberDistinctPredicates;
			this.numberDistinctObjects = numberDistinctObjects;
			this.subjectDifferentFromPreviousTriple = subjectDifferentFromPreviousTriple;
			this.predicateDifferentFromPreviousTriple = predicateDifferentFromPreviousTriple;
			this.objectDifferentFromPreviousTriple = objectDifferentFromPreviousTriple;
		}
	}

	public InnerNodeEntry getNextInnerNodeEntryStatistics(
			final TripleKey lastKey, final InputStream in) {
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
				final int filenameSize = LazyLiteralNodeDeSerializer
						.getIntSize(bits, index, in);
				index = (index + 2) % 8;
				final int numberOfTriplesSize = LazyLiteralNodeDeSerializer
						.getIntSize(bits, index, in);
				index = (index + 2) % 8;
				final int numberDistinctSubjectsSize = LazyLiteralNodeDeSerializer
						.getIntSize(bits, index, in);
				index = (index + 2) % 8;
				final int numberDistinctPredicatesSize = LazyLiteralNodeDeSerializer
						.getIntSize(bits, index, in);
				index = (index + 2) % 8;
				final int numberDistinctObjectsSize = LazyLiteralNodeDeSerializer
						.getIntSize(bits, index, in);
				index = (index + 2) % 8;
				final int fileName = LazyLiteralNodeDeSerializer.getInt(
						filenameSize + 1, in);
				final int numberOfTriples = LazyLiteralNodeDeSerializer.getInt(
						numberOfTriplesSize + 1, in);
				final int numberDistinctSubjects = LazyLiteralNodeDeSerializer
						.getInt(numberDistinctSubjectsSize + 1, in);
				final int numberDistinctPredicates = LazyLiteralNodeDeSerializer
						.getInt(numberDistinctPredicatesSize + 1, in);
				final int numberDistinctObjects = LazyLiteralNodeDeSerializer
						.getInt(numberDistinctObjectsSize + 1, in);
				return new InnerNodeEntry(null, fileName, numberOfTriples,
						numberDistinctSubjects, numberDistinctPredicates,
						numberDistinctObjects,
						subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple);
			}
			final Triple lastTriple = (lastKey == null) ? null : lastKey
					.getTriple();

			final boolean objectIsLazyLiteralOriginalContent = bits.get(1);
			int whereDifferentLiteral = 0;
			if (bits.get(2)) {
				whereDifferentLiteral = 2;
			}
			if (bits.get(3)) {
				whereDifferentLiteral += 1;
			}
			final boolean subjectDifferentFromPreviousTriple = bits.get(4);
			final boolean predicateDifferentFromPreviousTriple = bits.get(5);
			final boolean objectDifferentFromPreviousTriple = bits.get(6);

			final int numberDifferent = (whereDifferentLiteral == 3) ? 3
					: 3 - whereDifferentLiteral;
			final int[] numberBytesForInt = new int[numberDifferent];
			int index = 7;
			for (int i = 0; i < numberDifferent; i++) {
				numberBytesForInt[i] = LazyLiteralNodeDeSerializer.getIntSize(
						bits, index, in);
				index = (index + 2) % 8;
			}

			int originalContentSize = 0;
			if (objectIsLazyLiteralOriginalContent) {
				originalContentSize = LazyLiteralNodeDeSerializer.getIntSize(
						bits, index, in);
				index = (index + 2) % 8;
			}

			final int filenameSize = LazyLiteralNodeDeSerializer.getIntSize(
					bits, index, in);
			index = (index + 2) % 8;
			final int numberOfTriplesSize = LazyLiteralNodeDeSerializer
					.getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int numberDistinctSubjectsSize = LazyLiteralNodeDeSerializer
					.getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int numberDistinctPredicatesSize = LazyLiteralNodeDeSerializer
					.getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int numberDistinctObjectsSize = LazyLiteralNodeDeSerializer
					.getIntSize(bits, index, in);
			index = (index + 2) % 8;
			final int fileName = LazyLiteralNodeDeSerializer.getInt(
					filenameSize + 1, in);
			final int numberOfTriples = LazyLiteralNodeDeSerializer.getInt(
					numberOfTriplesSize + 1, in);
			final int numberDistinctSubjects = LazyLiteralNodeDeSerializer
					.getInt(numberDistinctSubjectsSize + 1, in);
			final int numberDistinctPredicates = LazyLiteralNodeDeSerializer
					.getInt(numberDistinctPredicatesSize + 1, in);
			final int numberDistinctObjects = LazyLiteralNodeDeSerializer
					.getInt(numberDistinctObjectsSize + 1, in);

			int codeForOriginalContent = 0;
			if (objectIsLazyLiteralOriginalContent) {
				codeForOriginalContent = LazyLiteralNodeDeSerializer.getInt(
						originalContentSize + 1, in);
			}

			final Triple t = new Triple();
			int index2 = 0;
			for (int i = 0; i < 3; i++) {
				if (i < whereDifferentLiteral && whereDifferentLiteral != 3) {
					t.setPos(map[this.order.ordinal()][i], lastTriple
							.getPos(map[this.order.ordinal()][i]));
				} else {
					if (whereDifferentLiteral != 3) {
						final int diff = LazyLiteralNodeDeSerializer.getInt(
								numberBytesForInt[index2++] + 1, in);
						t
								.setPos(
										map[this.order.ordinal()][i],
										LazyLiteralNodeDeSerializer
												.getLiteral(
														diff
																+ ((LazyLiteral) lastTriple
																		.getPos(map[this.order
																				.ordinal()][i]))
																		.getCode(),
														map[this.order.ordinal()][i],
														codeForOriginalContent,
														objectIsLazyLiteralOriginalContent));
					}
					for (int j = i + ((whereDifferentLiteral != 3) ? 1 : 0); j < 3; j++) {
						final int code = LazyLiteralNodeDeSerializer.getInt(
								numberBytesForInt[index2++] + 1, in);
						t.setPos(map[this.order.ordinal()][j],
								LazyLiteralNodeDeSerializer.getLiteral(code,
										map[this.order.ordinal()][j],
										codeForOriginalContent,
										objectIsLazyLiteralOriginalContent));
					}
					break;
				}
			}
			return new InnerNodeEntry(new TripleKey(t, new TripleComparator(
					this.order)), fileName, numberOfTriples, numberDistinctSubjects,
					numberDistinctPredicates, numberDistinctObjects,
					subjectDifferentFromPreviousTriple,
					predicateDifferentFromPreviousTriple,
					objectDifferentFromPreviousTriple);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
		}
		return null;
	}

	public VarBucket getVarBucket(final TripleKey key, final TripleKey minimum,
			final TripleKey maximum, final int pos) {
		final VarBucket result = new VarBucket();
		final int distance = (minimum == null) ? this.getDistance(key, key)
				: (maximum == null) ? this.getDistance(minimum, minimum)
						: this.getDistance(minimum, maximum);
		if (distance < 0) {
			return null;
		}
		if (distance == 0) {
			// special treatment: only one element!
			final ParallelIterator<Triple> it = (minimum == null) ? (ParallelIterator<Triple>) this
					.prefixSearch(key)
					: (ParallelIterator<Triple>) this.prefixSearch(minimum);
			final Triple t = it.next();
			it.close();
			if (t == null) {
				return null;
			}
			result.maximum = t.getPos(pos);
			result.minimum = result.maximum;
			final lupos.optimizations.logical.statistics.Entry entry = new lupos.optimizations.logical.statistics.Entry();
			entry.literal = result.maximum;
			entry.distinctLiterals = 1;
			entry.selectivity = 1.0;
			result.selectivityOfInterval.add(entry);
			return result;
		}
		final int MAXNUMBERBUCKETS = BasicIndexScan.getMaxNumberBuckets();
		double step = (double) distance / MAXNUMBERBUCKETS;
		if (step < 1.0) {
			step = 1.0;
		}
		final PrefixSearchIteratorMaxMinDistanceJump it = (minimum == null) ? new PrefixSearchIteratorMaxMinDistanceJump(
				key)
				: new PrefixSearchIteratorMaxMinDistanceJump(key, minimum,
						maximum);
		lupos.optimizations.logical.statistics.Entry entry = it.next(step, pos);
		// double currentDistance = distance;
		while (entry != null) {
			// currentDistance -= entry.selectivity;
			// adaptively change the step width:
			// if (result.selectivityOfInterval.size() >= MAXNUMBERBUCKETS) {
			// step = Double.POSITIVE_INFINITY;
			// } else
			// step = currentDistance
			// / (MAXNUMBERBUCKETS - (result.selectivityOfInterval
			// .size() + 1));
			result.selectivityOfInterval.add(entry);
			entry = it.next(step, pos);
		}
		result.maximum = (maximum == null) ? null : maximum.getTriple().getPos(
				pos);
		result.minimum = (minimum == null) ? null : minimum.getTriple().getPos(
				pos);
		return result;
	}

	private class PrefixSearchIteratorMaxMinDistanceJump {
		protected TripleKey largest = null;
		protected Triple next = null;
		final private TripleKey arg0;
		private final List<Tuple<TripleKey, InputStream>> innerNodes;
		private InputStream currentLeafIn;
		private Triple lastTriple;

		public PrefixSearchIteratorMaxMinDistanceJump(final TripleKey arg0,
				final TripleKey smallest, final TripleKey largest) {
			this(arg0, smallest);
			this.largest = largest;
			if (this.next != null && largest != null) {
				if (largest.compareTo(this.next) < 0) {
					this.next = null;
				}
			}
		}

		public lupos.optimizations.logical.statistics.Entry next(
				final double distance, final int pos) {
			final lupos.optimizations.logical.statistics.Entry entry = new lupos.optimizations.logical.statistics.Entry();
			if (this.next == null) {
				return null;
			}
			this.next = this.getNext(distance, pos, entry);
			if (this.next != null && this.largest != null) {
				if (this.largest.compareTo(this.next) < 0) {
					this.next = null;
					return entry;
				}
			}
			return entry;
		}

		public PrefixSearchIteratorMaxMinDistanceJump(final TripleKey arg0) {
			this.arg0 = arg0;
			this.lastTriple = null;
			this.innerNodes = new LinkedList<Tuple<TripleKey, InputStream>>();
			this.next = this.getFirst(LazyLiteralTripleKeyDBBPTreeStatistics.this.rootPage);
		}

		public PrefixSearchIteratorMaxMinDistanceJump(final TripleKey arg0,
				final TripleKey smallest) {
			this.arg0 = arg0;
			this.lastTriple = null;
			this.innerNodes = new LinkedList<Tuple<TripleKey, InputStream>>();
			this.next = this.getFirst(LazyLiteralTripleKeyDBBPTreeStatistics.this.rootPage, smallest);
		}

		private Triple getFirst(final int filename) {
			if (filename < 0) {
				return null;
			}
			final InputStream fis;
			try {
				final InputStream in = new PageInputStream(filename, LazyLiteralTripleKeyDBBPTreeStatistics.this.pageManager);
				final boolean leaf =  InputHelper.readLuposBoolean(in);
				if (leaf) { // leaf node reached!
					while (true) {
						final DBBPTreeEntry<TripleKey, Triple> e = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextLeafEntry(
								in, (this.lastTriple == null) ? null
										: new TripleKey(this.lastTriple, LazyLiteralTripleKeyDBBPTreeStatistics.this.order),
								this.lastTriple);
						if (e == null || e.key == null) {
							this.currentLeafIn = in;
							this.close();
							return null;
						}
						if (e.value != null) {
							this.lastTriple = e.value;
						}
						final TripleKey key = e.key;
						final int compare = LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(key, this.arg0);
						if (compare == 0) {
							this.currentLeafIn = in;
							return e.value;
						} else if (compare > 0) {
							this.currentLeafIn = in;
							this.close();
							return null;
						}
					}
				} else {
					TripleKey lastKey = null;
					while (true) {
						final Tuple<TripleKey, Integer> nextEntry = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextInnerNodeEntry(
								lastKey, in);
						if (nextEntry == null || nextEntry.getSecond() == 0
								|| nextEntry.getSecond() < 0) {
							in.close();
							this.close();
							return null;
						}
						if (nextEntry.getFirst() == null) {
							this.innerNodes
									.add(new Tuple<TripleKey, InputStream>(null, in));
							return this.getFirst(nextEntry.getSecond());
						}
						final int compare = LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(nextEntry
								.getFirst(), this.arg0);
						if (compare >= 0) {
							this.innerNodes
									.add(new Tuple<TripleKey, InputStream>(nextEntry.getFirst(), in));
							return this.getFirst(nextEntry.getSecond());
						}
						lastKey = nextEntry.getFirst();
					}
				}

			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
			return null;
		}

		private Triple getFirst(final int filename, final TripleKey triplekey) {
			if (filename < 0) {
				return null;
			}
			try {
				final InputStream in =  new PageInputStream(filename, LazyLiteralTripleKeyDBBPTreeStatistics.this.pageManager);
				final boolean leaf = InputHelper.readLuposBoolean(in);
				this.lastTriple = null;
				if (leaf) { // leaf node reached!
					while (true) {
						final DBBPTreeEntry<TripleKey, Triple> e = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextLeafEntry(
								in, (this.lastTriple == null) ? null
										: new TripleKey(this.lastTriple, LazyLiteralTripleKeyDBBPTreeStatistics.this.order),
								this.lastTriple);
						if (e == null || e.key == null) {
							this.currentLeafIn = in;
							this.close();
							return null;
						}
						if (e.value != null) {
							this.lastTriple = e.value;
						}
						final TripleKey key = e.key;
						final int compare = LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(key, triplekey);
						if (compare == 0) {
							this.currentLeafIn = in;
							return e.value;
						} else if (compare > 0) {
							if (LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(key, this.arg0) > 0) {
								this.currentLeafIn = in;
								this.close();
								return null;
							} else {
								this.currentLeafIn = in;
								return e.value;
							}
						}
					}
				} else {
					TripleKey lastKey = null;
					while (true) {
						final Tuple<TripleKey, Integer> nextEntry = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextInnerNodeEntry(
								lastKey, in);
						if (nextEntry == null || nextEntry.getSecond() <= 0) {
							in.close();
							this.close();
							return null;
						}
						lastKey = nextEntry.getFirst();
						if (nextEntry.getFirst() == null) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(null, in));
							return this.getFirst(nextEntry.getSecond(), triplekey);
						}
						final int compare = LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(nextEntry
								.getFirst(), triplekey);
						if (compare >= 0) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(nextEntry.getFirst(), in));
							return this.getFirst(nextEntry.getSecond(), triplekey);
						}
					}
				}

			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
			return null;
		}

		protected Triple useInnerNodes(final double distance, final int pos,
				final lupos.optimizations.logical.statistics.Entry entry,
				final int index) {
			if (index < 0) {
				return null;
			}
			double countDown = distance;
			final Tuple<TripleKey, InputStream> innerNode = this.innerNodes.get(index);
			if (innerNode.getFirst() == null) {
				try {
					innerNode.getSecond().close();
				} catch (final IOException e) {
					System.out.println(e);
					e.printStackTrace();
				}
				this.innerNodes.remove(index);
				return this.useInnerNodes(distance, pos, entry, index - 1);
			}
			while (countDown > 0.0) {
				final InnerNodeEntry ine = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextInnerNodeEntryStatistics(
						innerNode.getFirst(), innerNode.getSecond());
				if (ine == null) {
					try {
						innerNode.getSecond().close();
					} catch (final IOException e) {
						System.out.println(e);
						e.printStackTrace();
					}
					this.innerNodes.remove(index);
					return this.useInnerNodes(distance, pos, entry, index - 1);
				}
				innerNode.setFirst(ine.key);
				if (countDown - ine.numberOfTriples < 0.0) {
					switch (pos) {
					case 0:
						if (!ine.subjectDifferentFromPreviousTriple) {
							entry.distinctLiterals--;
						}
						break;
					case 1:
						if (!ine.predicateDifferentFromPreviousTriple) {
							entry.distinctLiterals--;
						}
						break;
					case 2:
						if (!ine.objectDifferentFromPreviousTriple) {
							entry.distinctLiterals--;
						}
						break;
					}
					// now go down this child node
					return this.useInnerNodesGoDown(countDown, pos, entry,
							ine.fileName);
				}
				countDown -= ine.numberOfTriples;
				entry.selectivity += ine.numberOfTriples;
				switch (pos) {
				case 0:
					entry.distinctLiterals += ine.numberDistinctSubjects;
					if (!ine.subjectDifferentFromPreviousTriple) {
						entry.distinctLiterals--;
					}
					break;
				case 1:
					entry.distinctLiterals += ine.numberDistinctPredicates;
					if (!ine.predicateDifferentFromPreviousTriple) {
						entry.distinctLiterals--;
					}
					break;
				case 2:
					entry.distinctLiterals += ine.numberDistinctObjects;
					if (!ine.objectDifferentFromPreviousTriple) {
						entry.distinctLiterals--;
					}
					break;
				}
				// last node accessed?
				if (ine.key == null) {
					try {
						innerNode.getSecond().close();
					} catch (final IOException e) {
						System.out.println(e);
						e.printStackTrace();
					}
					this.innerNodes.remove(index);
					return this.useInnerNodes(distance, pos, entry, index - 1);
				}
			}
			// this should never be reached!
			return innerNode.getFirst().getTriple();
		}

		private Triple useInnerNodesGoDown(double countDown, final int pos,
				final lupos.optimizations.logical.statistics.Entry entry,
				final int fileName) {
			if (fileName < 0) {
				return null;
			}
			try {
				final InputStream in = new PageInputStream(fileName, LazyLiteralTripleKeyDBBPTreeStatistics.this.pageManager);
				final boolean leaf = InputHelper.readLuposBoolean(in);
				this.lastTriple = null;
				if (leaf) { // leaf node reached!
					this.currentLeafIn = in;
					while (countDown >= 0.0) {
						final DBBPTreeEntry<TripleKey, Triple> e = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextLeafEntry(
								in, (this.lastTriple == null) ? null
										: new TripleKey(this.lastTriple, LazyLiteralTripleKeyDBBPTreeStatistics.this.order),
								this.lastTriple);
						if (e == null) {
							this.close();
							this.next = null;
							return null;
						}
						if (e.key == null) {
							// next leaf node, but we do big jumps by going over
							// the inner nodes!
							// this case should never happen!
							if (e.filenameOfNextLeafNode >= 0) {
								this.currentLeafIn.close();
								return this.useInnerNodes(countDown, pos,
										entry, this.innerNodes.size() - 1);
							} else {
								// should never happen!
								this.currentLeafIn.close();
								this.close();
								this.next = null;
								return null;
							}
						}
						if (LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(e.key, this.arg0) != 0) {
							this.close();
							this.next = null;
							return null;
						}
						if (this.largest != null) {
							if (LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(e.key, this.largest) > 0) {
								this.close();
								this.next = null;
								return null;
							}
						}
						entry.selectivity += 1.0;
						countDown -= 1.0;
						if (this.lastTriple == null
								|| this.lastTriple
										.getPos(pos)
										.compareToNotNecessarilySPARQLSpecificationConform(
												e.value.getPos(pos)) != 0) {
							entry.distinctLiterals += 1.0;
						}
						this.lastTriple = e.value;
						entry.literal = this.lastTriple.getPos(pos);
					}
					// read over the triples with the same literal at position
					// pos
					return this.readOver(this.lastTriple, entry, pos);
				} else {
					TripleKey lastKey = null;
					while (true) {
						final InnerNodeEntry nextEntry = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextInnerNodeEntryStatistics(
								lastKey, in);
						if (nextEntry == null || nextEntry.fileName <= 0) {
							in.close();
							this.close();
							this.next = null;
							return null;
						}
						lastKey = nextEntry.key;
						if (nextEntry.key == null) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(null, in));
							return this.useInnerNodesGoDown(countDown, pos, entry, nextEntry.fileName);
						}
						if (countDown - nextEntry.numberOfTriples < 0.0) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(nextEntry.key, in));
							return this.useInnerNodesGoDown(countDown, pos, entry,
									nextEntry.fileName);
						}
						if (this.largest == null
								|| LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(nextEntry.key, this.largest) > 0) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(nextEntry.key, in));
							return this.useInnerNodesGoDown(countDown, pos, entry,
									nextEntry.fileName);
						} else {
							countDown -= nextEntry.numberOfTriples;
							entry.selectivity += nextEntry.numberOfTriples;
							switch (pos) {
							case 0:
								entry.distinctLiterals += nextEntry.numberDistinctSubjects;
								if (!nextEntry.subjectDifferentFromPreviousTriple) {
									entry.distinctLiterals--;
								}
								break;
							case 1:
								entry.distinctLiterals += nextEntry.numberDistinctPredicates;
								if (!nextEntry.predicateDifferentFromPreviousTriple) {
									entry.distinctLiterals--;
								}
								break;
							case 2:
								entry.distinctLiterals += nextEntry.numberDistinctObjects;
								if (!nextEntry.objectDifferentFromPreviousTriple) {
									entry.distinctLiterals--;
								}
								break;
							}
						}
					}
				}
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
			return null;
		}

		protected Triple getNext(final double distance, final int pos,
				final lupos.optimizations.logical.statistics.Entry entry) {
			try {
				this.lastTriple = this.next;
				entry.selectivity = 1.0;
				entry.distinctLiterals = 1.0;
				entry.literal = this.lastTriple.getPos(pos);
				double countDown = distance - 1.0;
				while (countDown > 0.0) {
					final DBBPTreeEntry<TripleKey, Triple> e = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextLeafEntry(
							this.currentLeafIn, (this.lastTriple == null) ? null
									: new TripleKey(this.lastTriple, LazyLiteralTripleKeyDBBPTreeStatistics.this.order),
							this.lastTriple);
					if (e == null) {
						this.currentLeafIn.close();
						return this.useInnerNodes(countDown, pos, entry,
								this.innerNodes.size() - 1);
						// close();
						// next = null;
						// return null;
					}
					if (e.key == null) {
						// next leaf node, but we do big jumps by going over the
						// inner nodes!
						if (e.filenameOfNextLeafNode >= 0) {
							this.currentLeafIn.close();
							return this.useInnerNodes(countDown, pos, entry,
									this.innerNodes.size() - 1);
						} else {
							// should never happen!
							this.currentLeafIn.close();
							this.close();
							this.next = null;
							return null;
						}
					}
					if (LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(e.key, this.arg0) != 0) {
						this.close();
						this.next = null;
						return null;
					}
					if (this.largest != null) {
						if (LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(e.key, this.largest) > 0) {
							this.close();
							this.next = null;
							return null;
						}
					}
					entry.selectivity += 1.0;
					countDown -= 1.0;
					if (this.lastTriple.getPos(pos)
							.compareToNotNecessarilySPARQLSpecificationConform(
									e.value.getPos(pos)) != 0) {
						entry.distinctLiterals += 1.0;
					}
					this.lastTriple = e.value;
					entry.literal = this.lastTriple.getPos(pos);
				}
				// read over the triples with the same literal at position pos
				return this.readOver(this.lastTriple, entry, pos);
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
			this.next = this.lastTriple;
			return this.lastTriple;
		}

		private Triple readOver(Triple lastTriple,
				final lupos.optimizations.logical.statistics.Entry entry,
				final int pos) {
			while (true) {
				final DBBPTreeEntry<TripleKey, Triple> e = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextLeafEntry(
						this.currentLeafIn, (lastTriple == null) ? null
								: new TripleKey(lastTriple, LazyLiteralTripleKeyDBBPTreeStatistics.this.order), lastTriple);
				if (e == null) {
					this.close();
					this.next = null;
					return null;
				}
				if (e.key == null) {
					// next leaf node (accessed over the inner nodes)!
					return this.useInnerNodesReadOver(entry, pos, lastTriple,
							this.innerNodes.size() - 1);
				} else {
					if (LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(e.key, this.arg0) != 0) {
						this.close();
						this.next = null;
						return null;
					}
					if (this.largest != null) {
						if (LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(e.key, this.largest) > 0) {
							this.close();
							this.next = null;
							return null;
						}
					}
					if (lastTriple == null
							|| lastTriple
									.getPos(pos)
									.compareToNotNecessarilySPARQLSpecificationConform(
											e.value.getPos(pos)) != 0) {
						lastTriple = e.value;
						this.next = lastTriple;
						return this.next;
					}
					entry.selectivity += 1.0;
					lastTriple = e.value;
				}
			}
		}

		protected Triple useInnerNodesReadOver(
				final lupos.optimizations.logical.statistics.Entry entry,
				final int pos, final Triple lastTriple, final int index) {
			if (index < 0) {
				return null;
			}
			final Tuple<TripleKey, InputStream> innerNode = this.innerNodes
					.get(index);
			if (innerNode.getFirst() == null) {
				try {
					innerNode.getSecond().close();
				} catch (final IOException e) {
					System.out.println(e);
					e.printStackTrace();
				}
				this.innerNodes.remove(index);
				return this.useInnerNodesReadOver(entry, pos, lastTriple, index - 1);
			} else {
				final COMPARE primary = innerNode.getFirst()
						.getTripleComparator().getPrimary();
				COMPARE secondary = COMPARE.NONE;
				COMPARE tertiary = COMPARE.NONE;
				if (primary.ordinal() == pos + 1) {
					secondary = innerNode.getFirst().getTripleComparator()
							.getSecondary();
					if (secondary.ordinal() == pos + 1) {
						tertiary = innerNode.getFirst().getTripleComparator()
								.getTertiary();
					}
				}
				final TripleKey key = new TripleKey(lastTriple,
						new TripleComparator(primary, secondary, tertiary));
				while (innerNode.getFirst().compareTo(key) < 0) {
					final InnerNodeEntry ine = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextInnerNodeEntryStatistics(
							innerNode.getFirst(), innerNode.getSecond());
					if (ine == null) {
						try {
							innerNode.getSecond().close();
						} catch (final IOException e) {
							System.out.println(e);
							e.printStackTrace();
						}
						this.innerNodes.remove(index);
						return this.useInnerNodesReadOver(entry, pos, lastTriple,
								index - 1);
					}
					innerNode.setFirst(ine.key);
					if (ine.key.compareTo(key) < 0) {
						// now go down this child node
						return this.useInnerNodesGoDownReadOver(entry, pos,
								lastTriple, ine.fileName);
					}
					entry.selectivity += ine.numberOfTriples;
					switch (pos) {
					case 0:
						entry.distinctLiterals += ine.numberDistinctSubjects;
						if (!ine.subjectDifferentFromPreviousTriple) {
							entry.distinctLiterals--;
						}
						break;
					case 1:
						entry.distinctLiterals += ine.numberDistinctPredicates;
						if (!ine.predicateDifferentFromPreviousTriple) {
							entry.distinctLiterals--;
						}
						break;
					case 2:
						entry.distinctLiterals += ine.numberDistinctObjects;
						if (!ine.objectDifferentFromPreviousTriple) {
							entry.distinctLiterals--;
						}
						break;
					}
					// last node accessed?
					if (ine.key == null) {
						try {
							innerNode.getSecond().close();
						} catch (final IOException e) {
							System.out.println(e);
							e.printStackTrace();
						}
						this.innerNodes.remove(index);
						return this.useInnerNodesReadOver(entry, pos, lastTriple,
								index - 1);
					}
				}
			}
			// this should never be reached!
			return innerNode.getFirst().getTriple();
		}

		private Triple useInnerNodesGoDownReadOver(
				final lupos.optimizations.logical.statistics.Entry entry,
				final int pos, Triple lastTriple, final int fileName) {
			if (fileName < 0) {
				return null;
			}
			try {
				final InputStream in = new PageInputStream(fileName, LazyLiteralTripleKeyDBBPTreeStatistics.this.pageManager);
				final boolean leaf = InputHelper.readLuposBoolean(in);
				lastTriple = null;
				if (leaf) { // leaf node reached!
					this.currentLeafIn = in;
					return this.readOver(lastTriple, entry, pos);
				} else {
					TripleKey lastKey = null;
					InnerNodeEntry nextEntry = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextInnerNodeEntryStatistics(
							lastKey, in);
					final COMPARE primary = nextEntry.key.getTripleComparator()
							.getPrimary();
					final COMPARE secondary = COMPARE.NONE;
					final COMPARE tertiary = COMPARE.NONE;
					final TripleKey key = new TripleKey(lastTriple,
							new TripleComparator(primary, secondary, tertiary));
					while (true) {
						if (nextEntry == null || nextEntry.fileName <= 0) {
							in.close();
							this.close();
							this.next = null;
							return null;
						}
						lastKey = nextEntry.key;
						if (nextEntry.key == null) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(null, in));
							return this.useInnerNodesGoDownReadOver(entry, pos,
									lastTriple, nextEntry.fileName);
						}
						if (nextEntry.key.compareTo(key) < 0) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(nextEntry.key, in));
							return this.useInnerNodesGoDownReadOver(entry, pos,
									lastTriple, nextEntry.fileName);
						}
						if (this.largest == null
								|| LazyLiteralTripleKeyDBBPTreeStatistics.this.comparator.compare(nextEntry.key, this.largest) > 0) {
							this.innerNodes.add(new Tuple<TripleKey, InputStream>(nextEntry.key, in));
							return this.useInnerNodesGoDownReadOver(entry, pos,
									lastTriple, nextEntry.fileName);
						} else {
							entry.selectivity += nextEntry.numberOfTriples;
							switch (pos) {
							case 0:
								entry.distinctLiterals += nextEntry.numberDistinctSubjects;
								if (!nextEntry.subjectDifferentFromPreviousTriple) {
									entry.distinctLiterals--;
								}
								break;
							case 1:
								entry.distinctLiterals += nextEntry.numberDistinctPredicates;
								if (!nextEntry.predicateDifferentFromPreviousTriple) {
									entry.distinctLiterals--;
								}
								break;
							case 2:
								entry.distinctLiterals += nextEntry.numberDistinctObjects;
								if (!nextEntry.objectDifferentFromPreviousTriple) {
									entry.distinctLiterals--;
								}
								break;
							}
						}
						nextEntry = LazyLiteralTripleKeyDBBPTreeStatistics.this.getNextInnerNodeEntryStatistics(lastKey, in);
					}
				}
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
			return null;
		}

		public void close() {
			for (final Tuple<TripleKey, InputStream> tuple : this.innerNodes) {
				try {
					tuple.getSecond().close();
				} catch (final IOException e) {
				}
			}
			this.innerNodes.clear();
			try {
				this.currentLeafIn.close();
			} catch (final IOException e) {
			}
		}
	}

	public int getDistance(final TripleKey minimum, final TripleKey maximum) {
		return this.getDistance(this.rootPage, minimum, maximum);
	}

	private int getDistance(final int filename, final TripleKey minimum,
			final TripleKey maximum) {
		if (filename < 0) {
			return -1;
		}
		try {
			final InputStream in = new PageInputStream(filename, this.pageManager);
			final boolean leaf = InputHelper.readLuposBoolean(in);
			Triple lastTriple = null;
			if (leaf) { // leaf node reached!
				while (true) {
					DBBPTreeEntry<TripleKey, Triple> e = this.getNextLeafEntry(in,
							(lastTriple == null) ? null : new TripleKey(
									lastTriple, this.order), lastTriple);
					if (e == null || e.key == null) {
						in.close();
						return -1;
					}
					if (e.value != null) {
						lastTriple = e.value;
					}
					final TripleKey key = e.key;
					final int compare = this.comparator.compare(key, minimum);
					if (compare >= 0) {
						// now find maximum already here or reach the end of
						// this leaf node...
						int dist = -1;
						while (e != null && e.key != null
								&& this.comparator.compare(e.key, maximum) <= 0) {
							dist++;
							e = this.getNextLeafEntry(in,
									(lastTriple == null) ? null
											: new TripleKey(lastTriple, this.order),
									lastTriple);
							if (e!=null && e.value != null) {
								lastTriple = e.value;
							}
						}
						in.close();
						return dist;
					}
				}
			} else {
				TripleKey lastKey = null;
				while (true) {
					InnerNodeEntry nextEntry = this.getNextInnerNodeEntryStatistics(
							lastKey, in);
					if (nextEntry == null || nextEntry.fileName <= 0) {
						in.close();
						return -1;
					}
					lastKey = nextEntry.key;
					if (nextEntry.key == null) {
						// the maximum is already found and the distance has
						// been calculated
						// or there are no elements between minimum and maximum
						// or for finding the maximum, ancestor inner nodes must
						// be considered
						// as this inner node has already ended...
						return this.getDistance(nextEntry.fileName, minimum, maximum);
					}
					final int compare = this.comparator.compare(nextEntry.key,
							minimum);
					if (compare >= 0) {
						int dist = this.getDistance(nextEntry.fileName, minimum,
								maximum);
						if (dist < 0) {
							return -1;
						} else {
							// now find maximum (already in this inner
							// node?)
							if (this.comparator.compare(nextEntry.key, maximum) <= 0) {
								while (this.comparator.compare(nextEntry.key,
										maximum) <= 0) {
									nextEntry = this.getNextInnerNodeEntryStatistics(
											lastKey, in);
									if (nextEntry == null
											|| nextEntry.fileName <= 0) {
										in.close();
										return dist;
									}
									lastKey = nextEntry.key;
									if (nextEntry.key == null) {
										in.close();
										return dist;
									}
									if (this.comparator.compare(nextEntry.key,
											maximum) <= 0) {
										dist += nextEntry.numberOfTriples;
									}
								}
								// check remaining triples in this child
								// node and find maximum...
								return dist
										+ this.getDistanceToFindMaximum(
												nextEntry.fileName, maximum, 0);
							} else {
								return dist;
							}
						}
					}
				}
			}

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
		return -1;
	}

	private int getDistanceToFindMaximum(final int filename,
			final TripleKey arg0, int dist) {
		if (filename < 0) {
			return dist;
		}
		final InputStream fis;
		try {
			final InputStream in = new PageInputStream(filename, this.pageManager);
			final boolean leaf = InputHelper.readLuposBoolean(in);
			if (leaf) { // leaf node reached!
				Triple lastTriple = null;
				while (true) {
					final DBBPTreeEntry<TripleKey, Triple> e = this.getNextLeafEntry(
							in, (lastTriple == null) ? null : new TripleKey(
									lastTriple, this.order), lastTriple);
					if (e == null || e.key == null) {
						in.close();
						return dist;
					}
					final TripleKey key = e.key;
					final int compare = this.comparator.compare(key, arg0);
					if (compare > 0) {
						in.close();
						return dist;
					}
					if (e.value != null) {
						lastTriple = e.value;
						dist++;
					}
				}
			} else {
				TripleKey lastKey = null;
				int lastFilename = -1;
				int lastDistance = 0;
				while (true) {
					final InnerNodeEntry nextEntry = this.getNextInnerNodeEntryStatistics(
							lastKey, in);
					if (nextEntry == null || nextEntry.fileName <= 0) {
						in.close();
						return dist + lastDistance;
					}
					if (nextEntry.key == null) {
						in.close();
						return this.getDistanceToFindMaximum(nextEntry.fileName,
								arg0, dist + lastDistance);
					}
					final int compare = this.comparator.compare(nextEntry.key, arg0);
					if (compare > 0) {
						in.close();
						final int interDist = this.getDistanceToFindMaximum(
								nextEntry.fileName, arg0, 0);
						if (interDist > 0) {
							return dist + lastDistance + interDist;
						}
						if (lastFilename > 0) {
							return this.getDistanceToFindMaximum(lastFilename, arg0,
									dist);
						} else {
							return dist;
						}
					}
					lastKey = nextEntry.key;
					if (compare <= 0) {
						lastFilename = nextEntry.fileName;
					}
					dist += lastDistance;
					lastDistance = nextEntry.numberOfTriples;
				}
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
		return dist;
	}

	@Override
	public void generateDBBPTree(final SortedMap<TripleKey, Triple> sortedMap) {
		final LinkedList<Container> innerNodes = new LinkedList<Container>();
		this.size = sortedMap.size();
		final Container leaf = new Container(this.size, this.k_, true);
		this.firstLeafPage = leaf.getFileName();
		if (sortedMap.comparator() != null) {
			this.comparator = sortedMap.comparator();
		}
		final Iterator<Entry<TripleKey, Triple>> it = sortedMap.entrySet()
				.iterator();

		final Triple previousTriple = null;
		while (it.hasNext()) {
			final Entry<TripleKey, Triple> entry = it.next();
			if (leaf.newNodeForNextEntry()) {
				boolean subjectDifferentFromPreviousTriple = true;
				boolean predicateDifferentFromPreviousTriple = true;
				boolean objectDifferentFromPreviousTriple = true;
				if (previousTriple != null) {
					if (previousTriple.getPos(0)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(0)) == 0) {
						subjectDifferentFromPreviousTriple = false;
					} else {
						subjectDifferentFromPreviousTriple = true;
					}
					if (previousTriple.getPos(1)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(1)) == 0) {
						predicateDifferentFromPreviousTriple = false;
					} else {
						predicateDifferentFromPreviousTriple = true;
					}
					if (previousTriple.getPos(2)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(2)) == 0) {
						objectDifferentFromPreviousTriple = false;
					} else {
						objectDifferentFromPreviousTriple = true;
					}
				}
				leaf.closeNode(innerNodes, subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple);
			}
			leaf.storeInLeafNode(entry, previousTriple);
		}
		if (it instanceof ParallelIterator) {
			((ParallelIterator) it).close();
		}
		leaf.close();
		Container previous = leaf;
		for (final Container container : innerNodes) {
			container.storeInInnerNode(previous.filename,
					previous.numberOfTriples, previous.numberDistinctSubjects,
					previous.numberDistinctPredicates,
					previous.numberDistinctObjects,
					previous.subjectDifferentFromPreviousTriple,
					previous.predicateDifferentFromPreviousTriple,
					previous.objectDifferentFromPreviousTriple,
					previous.lastStoredEntry);
			container.close();
			previous = container;
		}
		this.rootPage = previous.filename;
	}

	public void generateDBBPTree(final Set<Map.Entry<TripleKey, Triple>> set) throws IOException {
		final LinkedList<Container> innerNodes = new LinkedList<Container>();
		this.size = set.size();
		final Container leaf = new Container(this.size, this.k_, true);
		this.firstLeafPage = leaf.getFileName();
		final Iterator<Entry<TripleKey, Triple>> it = set.iterator();

		Triple previousTriple = null;
		while (it.hasNext()) {
			final Entry<TripleKey, Triple> entry = it.next();
			if (leaf.newNodeForNextEntry()) {
				boolean subjectDifferentFromPreviousTriple = true;
				boolean predicateDifferentFromPreviousTriple = true;
				boolean objectDifferentFromPreviousTriple = true;
				if (previousTriple != null) {
					if (previousTriple.getPos(0)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(0)) == 0) {
						subjectDifferentFromPreviousTriple = false;
					} else {
						subjectDifferentFromPreviousTriple = true;
					}
					if (previousTriple.getPos(1)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(1)) == 0) {
						predicateDifferentFromPreviousTriple = false;
					} else {
						predicateDifferentFromPreviousTriple = true;
					}
					if (previousTriple.getPos(2)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(2)) == 0) {
						objectDifferentFromPreviousTriple = false;
					} else {
						objectDifferentFromPreviousTriple = true;
					}
				}
				leaf.closeNode(innerNodes, subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple);
			}
			leaf.storeInLeafNode(entry, previousTriple);
			previousTriple = entry.getValue();
		}
		if (it instanceof ParallelIterator) {
			((ParallelIterator) it).close();
		}
		leaf.close();
		Container previous = leaf;
		for (final Container container : innerNodes) {
			container.storeInInnerNode(previous.filename,
					previous.numberOfTriples, previous.numberDistinctSubjects,
					previous.numberDistinctPredicates,
					previous.numberDistinctObjects,
					previous.subjectDifferentFromPreviousTriple,
					previous.predicateDifferentFromPreviousTriple,
					previous.objectDifferentFromPreviousTriple,
					previous.lastStoredEntry);
			container.close();
			previous = container;
		}
		this.rootPage = previous.filename;
		this.pageManager.writeAllModifiedPages();
	}

	protected class Container {
		private OutputStream out = null;
		private int filename;
		private int currentEntry = 0;
		private final double factor;
		private Entry<TripleKey, Triple> lastStoredEntry;
		private final long numberOfNodes;
		private double limitNextNode;
		private final boolean leaf;
		private TripleKey lastKey = null;
		private Triple lastValue = null;
		private int numberOfTriples = 0;
		private int numberDistinctSubjects = 0;
		private int numberDistinctPredicates = 0;
		private int numberDistinctObjects = 0;

		private boolean subjectDifferentFromPreviousTriple;
		private boolean predicateDifferentFromPreviousTriple;
		private boolean objectDifferentFromPreviousTriple;

		public Container(final long numberOfEntries, final int kk_,
				final boolean leaf) {
			this.leaf = leaf;
			this.filename = LazyLiteralTripleKeyDBBPTreeStatistics.this.newFilename();
			this.init(null);
			this.numberOfNodes = Math.round(Math
					.ceil((double) numberOfEntries / kk_));
			this.factor = (double) numberOfEntries / this.numberOfNodes;
			this.limitNextNode = this.factor;
		}

		protected void init(final Triple leftKey) {
			try {
				if (this.out != null) {
					this.out.close();
				}
				this.out = new PageOutputStream(this.filename,
						LazyLiteralTripleKeyDBBPTreeStatistics.this.pageManager, true);
				OutHelper.writeLuposBoolean(this.leaf, this.out);
				this.lastKey = null;
				this.lastValue = null;
				this.numberOfTriples = 0;
				this.numberDistinctSubjects = 0;
				this.numberDistinctPredicates = 0;
				this.numberDistinctObjects = 0;
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public int getFileName() {
			return this.filename;
		}

		public boolean newNodeForNextEntry() {
			if (this.currentEntry + 1 > this.limitNextNode) {
				return true;
			} else {
				return false;
			}
		}

		public void storeInLeafNode(final Entry<TripleKey, Triple> entry,
				final Triple previous) {
			this.lastStoredEntry = entry;
			this.currentEntry++;
			LazyLiteralTripleKeyDBBPTreeStatistics.this.keyClass = (Class<? super TripleKey>) entry.getKey().getClass();
			LazyLiteralTripleKeyDBBPTreeStatistics.this.valueClass = (Class<? super Triple>) entry.getValue().getClass();
			if (this.numberOfTriples == 0) {
				if (previous == null) {
					this.subjectDifferentFromPreviousTriple = true;
					this.predicateDifferentFromPreviousTriple = true;
					this.objectDifferentFromPreviousTriple = true;
				} else {
					if (previous.getPos(0)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(0)) == 0) {
						this.subjectDifferentFromPreviousTriple = false;
					} else {
						this.subjectDifferentFromPreviousTriple = true;
					}
					if (previous.getPos(1)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(1)) == 0) {
						this.predicateDifferentFromPreviousTriple = false;
					} else {
						this.predicateDifferentFromPreviousTriple = true;
					}
					if (previous.getPos(2)
							.compareToNotNecessarilySPARQLSpecificationConform(
									entry.getValue().getPos(2)) == 0) {
						this.objectDifferentFromPreviousTriple = false;
					} else {
						this.objectDifferentFromPreviousTriple = true;
					}
				}
			}
			try {
				LazyLiteralTripleKeyDBBPTreeStatistics.this.writeLeafEntry(entry.getKey(), entry.getValue(), this.out, this.lastKey,
						this.lastValue);
				this.numberOfTriples++;
				if (this.lastValue == null
						|| entry
								.getValue()
								.getSubject()
								.compareToNotNecessarilySPARQLSpecificationConform(
										this.lastValue.getSubject()) != 0) {
					this.numberDistinctSubjects++;
				}
				if (this.lastValue == null
						|| entry
								.getValue()
								.getPredicate()
								.compareToNotNecessarilySPARQLSpecificationConform(
										this.lastValue.getPredicate()) != 0) {
					this.numberDistinctPredicates++;
				}
				if (this.lastValue == null
						|| entry
								.getValue()
								.getObject()
								.compareToNotNecessarilySPARQLSpecificationConform(
										this.lastValue.getObject()) != 0) {
					this.numberDistinctObjects++;
				}
				this.lastKey = entry.getKey();
				this.lastValue = entry.getValue();
				// System.out.println("leaf "+ filename
				// +" ("+entry.getKey()+", "+entry.getValue()+")");
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void storeInInnerNode(final int fileName,
				final Entry<TripleKey, Triple> entry,
				final int numberOfTriples, final int numberDistinctSubjects,
				final int numberDistinctPredicates,
				final int numberDistinctObjects,
				final boolean subjectDifferentFromPreviousTriple,
				final boolean predicateDifferentFromPreviousTriple,
				final boolean objectDifferentFromPreviousTriple) {
			if (this.numberOfTriples == 0) {
				this.subjectDifferentFromPreviousTriple = subjectDifferentFromPreviousTriple;
				this.predicateDifferentFromPreviousTriple = predicateDifferentFromPreviousTriple;
				this.objectDifferentFromPreviousTriple = objectDifferentFromPreviousTriple;
			}
			this.currentEntry++;
			try {
				this.lastStoredEntry = entry;
				this.lastValue = entry.getValue();
				LazyLiteralTripleKeyDBBPTreeStatistics.this.writeInnerNodeEntry(fileName, entry.getKey(), this.out, this.lastKey,
						numberOfTriples, numberDistinctSubjects,
						numberDistinctPredicates, numberDistinctObjects,
						subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple);
				this.numberOfTriples += numberOfTriples;
				if (this.lastKey != null
						&& this.lastKey
								.getTriple()
								.getSubject()
								.compareToNotNecessarilySPARQLSpecificationConform(
										entry.getKey().getTriple().getSubject()) == 0) {
					this.numberDistinctSubjects += numberDistinctSubjects - 1;
				} else {
					this.numberDistinctSubjects += numberDistinctSubjects;
				}
				if (this.lastKey != null
						&& this.lastKey
								.getTriple()
								.getPredicate()
								.compareToNotNecessarilySPARQLSpecificationConform(
										entry.getKey().getTriple()
												.getPredicate()) == 0) {
					this.numberDistinctPredicates += numberDistinctPredicates - 1;
				} else {
					this.numberDistinctPredicates += numberDistinctPredicates;
				}
				if (this.lastKey != null
						&& this.lastKey
								.getTriple()
								.getObject()
								.compareToNotNecessarilySPARQLSpecificationConform(
										entry.getKey().getTriple().getObject()) == 0) {
					this.numberDistinctObjects += numberDistinctObjects - 1;
				} else {
					this.numberDistinctObjects += numberDistinctObjects;
				}
				this.lastKey = entry.getKey();
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		public void storeInInnerNode(final int fileName,
				final int numberOfTriples, final int numberDistinctSubjects,
				final int numberDistinctPredicates,
				final int numberDistinctObjects,
				final boolean subjectDifferentFromPreviousTriple,
				final boolean predicateDifferentFromPreviousTriple,
				final boolean objectDifferentFromPreviousTriple,
				final Entry<TripleKey, Triple> entry) {
			try {
				if (this.numberOfTriples == 0) {
					this.subjectDifferentFromPreviousTriple = subjectDifferentFromPreviousTriple;
					this.predicateDifferentFromPreviousTriple = predicateDifferentFromPreviousTriple;
					this.objectDifferentFromPreviousTriple = objectDifferentFromPreviousTriple;
				}
				this.lastStoredEntry = entry;
				this.lastValue = entry.getValue();
				LazyLiteralTripleKeyDBBPTreeStatistics.this.writeInnerNodeEntry(fileName, numberOfTriples,
						numberDistinctSubjects, numberDistinctPredicates,
						numberDistinctObjects,
						subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple, this.out);
				this.numberOfTriples += numberOfTriples;
				if (this.lastKey != null
						&& this.lastKey
								.getTriple()
								.getSubject()
								.compareToNotNecessarilySPARQLSpecificationConform(
										entry.getKey().getTriple().getSubject()) == 0) {
					this.numberDistinctSubjects += numberDistinctSubjects - 1;
				} else {
					this.numberDistinctSubjects += numberDistinctSubjects;
				}
				if (this.lastKey != null
						&& this.lastKey
								.getTriple()
								.getPredicate()
								.compareToNotNecessarilySPARQLSpecificationConform(
										entry.getKey().getTriple()
												.getPredicate()) == 0) {
					this.numberDistinctPredicates += numberDistinctPredicates - 1;
				} else {
					this.numberDistinctPredicates += numberDistinctPredicates;
				}
				if (this.lastKey != null
						&& this.lastKey
								.getTriple()
								.getObject()
								.compareToNotNecessarilySPARQLSpecificationConform(
										entry.getKey().getTriple().getObject()) == 0) {
					this.numberDistinctObjects += numberDistinctObjects - 1;
				} else {
					this.numberDistinctObjects += numberDistinctObjects;
				}
				this.lastKey = entry.getKey();
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void closeNode(final LinkedList<Container> innerNodes,
				final boolean subjectDifferentFromPreviousTriple,
				final boolean predicateDifferentFromPreviousTriple,
				final boolean objectDifferentFromPreviousTriple) {
			this.addToInnerNodes(innerNodes, 0, this, this.filename,
					this.lastStoredEntry, this.numberOfTriples,
					this.numberDistinctSubjects, this.numberDistinctPredicates,
					this.numberDistinctObjects, subjectDifferentFromPreviousTriple,
					predicateDifferentFromPreviousTriple,
					objectDifferentFromPreviousTriple);
			this.filename = LazyLiteralTripleKeyDBBPTreeStatistics.this.newFilename();
			LazyLiteralTripleKeyDBBPTreeStatistics.this.writeLeafEntryNextFileName(this.filename, this.out);

			this.init(this.lastStoredEntry.getValue());
			this.limitNextNode = this.currentEntry + this.factor;
		}

		public void close() {
			try {
				this.out.close();
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void addToInnerNodes(final LinkedList<Container> innerNodes,
				final int position, Container previous, final int filename,
				final Entry<TripleKey, Triple> lastStoredEntry,
				final int numberOfTriples, final int numberDistinctSubjects,
				final int numberDistinctPredicates,
				final int numberDistinctObjects,
				final boolean subjectDifferentFromPreviousTriple,
				final boolean predicateDifferentFromPreviousTriple,
				final boolean objectDifferentFromPreviousTriple) {
			while (innerNodes.size() < position + 1) {
				final Container container = new Container(this.numberOfNodes - 1, LazyLiteralTripleKeyDBBPTreeStatistics.this.k,
						false);
				previous = container;
				innerNodes.add(container);
			}
			final Container container = innerNodes.get(position);
			if (container.newNodeForNextEntry()) {
				container.storeInInnerNode(filename, numberOfTriples,
						numberDistinctSubjects, numberDistinctPredicates,
						numberDistinctObjects,
						subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple, lastStoredEntry);
				this.addToInnerNodes(innerNodes, position + 1, container,
						container.filename, lastStoredEntry,
						container.numberOfTriples,
						container.numberDistinctSubjects,
						container.numberDistinctPredicates,
						container.numberDistinctObjects,
						subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple);
				container.filename = LazyLiteralTripleKeyDBBPTreeStatistics.this.newFilename();
				container.init(lastStoredEntry.getValue());
				container.limitNextNode = container.currentEntry
						+ container.factor;
			} else {
				container.storeInInnerNode(filename, lastStoredEntry,
						numberOfTriples, numberDistinctSubjects,
						numberDistinctPredicates, numberDistinctObjects,
						subjectDifferentFromPreviousTriple,
						predicateDifferentFromPreviousTriple,
						objectDifferentFromPreviousTriple);
			}
		}
	}

	@Override
	public void writeLuposObject(final OutputStream loos)
			throws IOException {
		this.pageManager.writeAllModifiedPages();
		OutHelper.writeLuposInt(this.currentID, loos);
		OutHelper.writeLuposInt(this.k, loos);
		OutHelper.writeLuposInt(this.k_, loos);
		OutHelper.writeLuposInt(this.size, loos);
		Registration.serializeWithoutId(this.comparator, loos);
		OutHelper.writeLuposInt(this.rootPage, loos);
		OutHelper.writeLuposInt(this.firstLeafPage, loos);
		Registration.serializeClass(this.keyClass, loos);
		Registration.serializeClass(this.valueClass, loos);
		OutHelper.writeLuposByte((byte) this.order.ordinal(), loos);
	}

	public static LazyLiteralTripleKeyDBBPTreeStatistics readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		final int currentID = InputHelper.readLuposInt(lois);
		final int k = InputHelper.readLuposInt(lois);
		final int k_ = InputHelper.readLuposInt(lois);
		final int size = InputHelper.readLuposInt(lois);
		final Comparator comp = Registration.deserializeWithoutId(Comparator.class, lois);
		final int rootFilename = InputHelper.readLuposInt(lois);
		final int firstLeafFileName = InputHelper.readLuposInt(lois);
		final Class keyClass = Registration.deserializeId(lois)[0];
		final Class valueClass = Registration.deserializeId(lois)[0];
		final byte b = InputHelper.readLuposByte(lois);
		final RDF3XIndexScan.CollationOrder order = RDF3XIndexScan.CollationOrder.values()[b];
		final LazyLiteralTripleKeyDBBPTreeStatistics dbbptree = new LazyLiteralTripleKeyDBBPTreeStatistics(
				k, k_, size, comp, rootFilename, firstLeafFileName, keyClass,
				valueClass, order, currentID);
		return dbbptree;
	}

	/**
	 * This constructor is only used for creating a DBBPTree after reading it
	 * from file!
	 *
	 * @param k
	 * @param k_
	 * @param size
	 * @param comp
	 * @param folder
	 * @param rootFilename
	 * @param firstLeafFileName
	 * @param keyClass
	 * @param valueClass
	 */
	public LazyLiteralTripleKeyDBBPTreeStatistics(final int k, final int k_,
			final int size, final Comparator comp, final int rootFilename,
			final int firstLeafFileName, final Class keyClass,
			final Class valueClass,
			final RDF3XIndexScan.CollationOrder order, final int currentID)
			throws IOException {
		super(k, k_, size, comp, rootFilename, firstLeafFileName, keyClass,
				valueClass, currentID,
				new LazyLiteralDBBPTreeStatisticsNodeDeSerializer(order));
		this.order = order;
	}
}
