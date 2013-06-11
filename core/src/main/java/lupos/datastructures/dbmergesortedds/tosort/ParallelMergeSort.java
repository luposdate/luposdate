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
package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import lupos.datastructures.dbmergesortedds.StandardComparator;
import lupos.misc.util.ImmutableIterator;

public class ParallelMergeSort<E extends Comparable<E>> extends ArraySort<E> {

	protected final static int NUMBEROFTHREADS = 8;
	protected Semaphore threadsPoolRemaining;
	protected Comparator<E> comparator;

	public ParallelMergeSort(final int length, final Comparator<E> comparator) {
		super(length);
		this.comparator = comparator;
	}

	public ParallelMergeSort(final int length) {
		super(length);
		this.comparator = new StandardComparator<E>();
	}

	public ParallelMergeSort(final Object[] elements) {
		super(0);
		this.elements = elements;
		this.length = elements.length;
		this.comparator = new StandardComparator<E>();
	}

	public ParallelMergeSort(final Object[] elements, final int length) {
		super(0);
		this.elements = elements;
		this.length = length;
		this.comparator = new StandardComparator<E>();
	}

	public ParallelMergeSort(final Object[] elements, final int length,
			final Comparator<E> comparator) {
		super(0);
		this.elements = elements;
		this.length = length;
		this.comparator = comparator;
	}

	@Override
	public void sort() {
		this.elements = this.sortAndReturn();
	}

	@Override
	public Iterator<E> emptyDatastructure() {
		final int lengthSorted = this.length;
		final Object[] sorted = this.sortAndReturn();
		// System.out.println(lengthSorted + "<->" + sorted.length);
		return new ImmutableIterator<E>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < lengthSorted;
			}

			@Override
			public E next() {
				if (this.index < lengthSorted) {
					return (E) sorted[this.index++];
				} else {
					return null;
				}
			}
		};
	}

	public static int M = 25; // length of start sequences sorted with insertion

	public Object[] sortAndReturn() {
		this.threadsPoolRemaining = new Semaphore(NUMBEROFTHREADS);
		final Sorter sorter = new Sorter(this.elements, 0, this.length);
		sorter.run();
		return sorter.getSorted();
	}

	public class Sorter extends Thread {
		private final Object[] tosort;
		private final int start;
		private final int length;

		private Object[] sorted = null;

		public Sorter(final Object[] tosort, final int start, final int length) {
			this.tosort = tosort;
			this.start = start;
			this.length = length;
		}

		@Override
		public void run() {
			if (this.length <= M) {
				this.sorted = this.getSorted(this.tosort, this.start, this.length);
			} else {
				final int m = this.length / 2;
				final Sorter sorter1 = new Sorter(this.tosort, this.start, m);
				if (ParallelMergeSort.this.threadsPoolRemaining.tryAcquire()) {
					sorter1.start();
				} else {
					sorter1.run();
				}
				final Sorter sorter2 = new Sorter(this.tosort, this.start + m, this.length - m);
				sorter2.run();
				try {
					sorter1.join();
					this.sorted = this.merge(sorter1.getSorted(), sorter2.getSorted());
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}

		}

		private Object[] getSorted(final Object[] elements, final int min,
				final int length) {
			final Object[] alreadySorted = new Object[length];
			System.arraycopy(elements, min, alreadySorted, 0, length);
			// insertionsort on the copied sequence!
			for (int i = 0 + 1; i < length; ++i) {
				final E tmp2 = (E) alreadySorted[i];
				int j;
				for (j = i - 1; j >= 0
						&& ParallelMergeSort.this.comparator.compare(tmp2, (E) alreadySorted[j]) < 0; --j) {
					alreadySorted[j + 1] = alreadySorted[j];
				}
				alreadySorted[j + 1] = tmp2;
			}
			return alreadySorted;
		}

		private Object[] merge(final Object[] a, final Object[] b) {
			final Object[] s = new Object[a.length + b.length];
			int index_a = 0;
			int index_b = 0;
			int index_s = 0;
			while (index_s < s.length) {
				if (index_a >= a.length) {
					// rest from b
					System
							.arraycopy(b, index_b, s, index_s, b.length
									- index_b);
					// s[index_s++] = b[index_b++];
					return s;
				} else if (index_b >= b.length) {
					// rest from a
					System
							.arraycopy(a, index_a, s, index_s, a.length
									- index_a);
					// s[index_s++] = a[index_a++];
					return s;
				} else {
					s[index_s++] = (ParallelMergeSort.this.comparator.compare((E) a[index_a],
							(E) b[index_b]) <= 0) ? a[index_a++] : b[index_b++];
				}
			}
			return s;
		}

		public Object[] getSorted() {
			return this.sorted;
		}
	}

}
