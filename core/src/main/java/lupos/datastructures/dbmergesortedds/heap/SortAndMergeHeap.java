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
package lupos.datastructures.dbmergesortedds.heap;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.tosort.ArraySort;
import lupos.datastructures.dbmergesortedds.tosort.ParallelMergeSort;
import lupos.datastructures.dbmergesortedds.tosort.QuickSort;

public class SortAndMergeHeap<E extends Comparable<E>> extends Heap<E> {

	protected boolean useQuickSort = true;

	protected final int maxLength;

	protected int start = 0;
	protected int lengthAlreadySorted = 0;
	protected volatile int merged = 0;

	protected Object[] alreadySorted = null;
	protected ArraySort<E> toSort;

	public SortAndMergeHeap(final int length, final boolean useQuickSort) {
		this.useQuickSort = useQuickSort;
		newSort(length);
		maxLength = length;
	}

	public SortAndMergeHeap(final Object[] content, final int capacity,
			final boolean useQuickSort) {
		this.useQuickSort = useQuickSort;
		// newSort(0);
		// if (content != null)
		// toSort.setElements(content, content.length);
		if (content != null)
			alreadySorted = content;
		else
			alreadySorted = new Object[0];
		lengthAlreadySorted = alreadySorted.length;
		merged = lengthAlreadySorted + 1;
		toSort = null;
		maxLength = capacity;
	}

	public SortAndMergeHeap(final int length) {
		newSort(length);
		maxLength = length;
	}

	public SortAndMergeHeap(final Object[] content, final int capacity) {
		// newSort(0);
		// if (content != null)
		// toSort.setElements(content, content.length);
		if (content != null)
			alreadySorted = content;
		else
			alreadySorted = new Object[0];
		lengthAlreadySorted = alreadySorted.length;
		merged = lengthAlreadySorted + 1;
		toSort = null;
		maxLength = capacity;
	}

	public void newSort(final int length) {
		if (useQuickSort)
			toSort = new QuickSort<E>(length);
		else
			toSort = new ParallelMergeSort<E>(length);
	}

	@Override
	protected Object[] getContent() {
		if (size() == 0)
			return null;
		sort();
		while (merged < this.lengthAlreadySorted + 1)
			;
		// System.out.println("waiting for merger to end");
		if (start > 0 || this.lengthAlreadySorted < alreadySorted.length) {
			final Object[] result = new Object[this.lengthAlreadySorted - start];
			System.arraycopy(alreadySorted, start, result, 0,
					this.lengthAlreadySorted);
			return result;
		} else
			return alreadySorted;
	}

	@Override
	public int maxLength() {
		return maxLength;
	}

	@Override
	public E peek() {
		if (size() == 0)
			return null;
		sort();
		while (start >= merged - 1)
			;
		// System.out.println("busy loop");
		return (E) alreadySorted[start];
	}

	@Override
	public E pop() {
		if (size() == 0)
			return null;
		sort();
		while (start >= merged - 1)
			;
		// System.out.println("busy loop");
		return (E) alreadySorted[start++];
	}

	private void sort() {
		if (toSort != null) {
			toSort.sort();
			if (alreadySorted == null || start >= this.lengthAlreadySorted) {
				alreadySorted = toSort.getElements();
				this.lengthAlreadySorted = toSort.size();
				merged = this.lengthAlreadySorted + 1;
			} else {
				while (merged < this.lengthAlreadySorted + 1)
					;
				// System.out.println("waiting for merger to end:" + merged
				// + "<" + this.lengthAlreadySorted + 1);
				// System.out.println("start:" + start
				// + " this.lengthAlreadySorted "
				// + this.lengthAlreadySorted + " merging "
				// + (this.lengthAlreadySorted - start) + "<->"
				// + toSort.size());
				final Object[] intermediate = this.alreadySorted;
				alreadySorted = new Object[this.lengthAlreadySorted - start
						+ toSort.size()];
				final Merger merger = new Merger(toSort.getElements(), toSort
						.size(), intermediate, start, lengthAlreadySorted
						- start);
				merger.start();
				this.lengthAlreadySorted = alreadySorted.length;
			}
			start = 0;
			toSort = null;
		}
	}

	public class Merger extends Thread {
		Object[] elements;
		int size;
		Object[] alreadySorted;
		int start;
		int length;

		public Merger(final Object[] elements, final int size,
				final Object[] alreadySorted, final int start, final int length) {
			this.elements = elements;
			this.size = size;
			this.alreadySorted = alreadySorted;
			this.start = start;
			this.length = length;
			merged = 0;
		}

		@Override
		public void run() {
			merge(this.elements, 0, this.size, this.alreadySorted, this.start,
					this.length);
		}
	}

	private void merge(final Object[] a, final int start_a, final int length_a,
			final Object[] b, final int start_b, final int length_b) {
		int index_a = start_a;
		final int end_a = start_a + length_a;
		int index_b = start_b;
		final int end_b = start_b + length_b;
		try {
			while (merged < alreadySorted.length) {
				if (index_a >= end_a) {
					// rest from b
					System.arraycopy(b, index_b, alreadySorted, merged, end_b
							- index_b);
					// s[index_s++] = b[index_b++];
					merged = alreadySorted.length + 1;
					return;
				} else if (index_b >= end_b) {
					// rest from a
					System.arraycopy(a, index_a, alreadySorted, merged, end_a
							- index_a);
					// s[index_s++] = a[index_a++];
					merged = alreadySorted.length + 1;
					return;
				} else {
					alreadySorted[merged] = (((E) a[index_a])
							.compareTo((E) b[index_b]) <= 0) ? a[index_a++]
							: b[index_b++];
					merged++;
				}
			}
		} catch (final ArrayIndexOutOfBoundsException e) {
			System.out.println("index_a " + index_a + " end_a " + end_a
					+ " index_b " + index_b + " merged " + merged + " end_b "
					+ end_b + " end_b - index_b " + (end_b - index_b)
					+ " b.length " + b.length + " alreadySorted "
					+ alreadySorted.length + " length_a " + length_a
					+ " length_b " + length_b);
		}
		merged = alreadySorted.length + 1;
	}

	@Override
	public int size() {
		int size = (alreadySorted == null) ? 0 : lengthAlreadySorted - start;
		size += (toSort == null) ? 0 : toSort.size();
		return size;
	}

	@Override
	public String toString() {
		String s = "";
		if (toSort != null)
			s += toSort.toString();
		if (alreadySorted != null)
			s += alreadySorted.toString();
		return s;
	}

	@Override
	public void add(final E elem) {
		if (toSort == null) {
			newSort(maxLength - size());
		}
		toSort.add(elem);
	}

	@Override
	public void clear() {
		if (toSort != null) {
			toSort.clear();
			toSort = null;
		}
		start = 0;
	}

	@Override
	public void release() {
		if (toSort != null) {
			toSort.release();
			toSort = null;
		}
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean isFull() {
		if (toSort == null) {
			if (alreadySorted != null)
				return this.lengthAlreadySorted - start >= maxLength;
			else
				return false;
		}
		return toSort.isFull();
	}
}
