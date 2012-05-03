package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import lupos.datastructures.dbmergesortedds.StandardComparator;

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
		elements = sortAndReturn();
	}

	@Override
	public Iterator<E> emptyDatastructure() {
		final int lengthSorted = length;
		final Object[] sorted = sortAndReturn();
		// System.out.println(lengthSorted + "<->" + sorted.length);
		return new Iterator<E>() {
			private int index = 0;

			public boolean hasNext() {
				return index < lengthSorted;
			}

			public E next() {
				if (index < lengthSorted)
					return (E) sorted[index++];
				else
					return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static int M = 25; // length of start sequences sorted with insertion

	public Object[] sortAndReturn() {
		threadsPoolRemaining = new Semaphore(NUMBEROFTHREADS);
		final Sorter sorter = new Sorter(elements, 0, length);
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
			if (length <= M)
				sorted = getSorted(tosort, start, length);
			else {
				final int m = length / 2;
				final Sorter sorter1 = new Sorter(tosort, start, m);
				if (threadsPoolRemaining.tryAcquire())
					sorter1.start();
				else
					sorter1.run();
				final Sorter sorter2 = new Sorter(tosort, start + m, length - m);
				sorter2.run();
				try {
					sorter1.join();
					sorted = merge(sorter1.getSorted(), sorter2.getSorted());
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
						&& comparator.compare(tmp2, (E) alreadySorted[j]) < 0; --j)
					alreadySorted[j + 1] = alreadySorted[j];
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
					s[index_s++] = (comparator.compare((E) a[index_a],
							(E) b[index_b]) <= 0) ? a[index_a++] : b[index_b++];
				}
			}
			return s;
		}

		public Object[] getSorted() {
			return sorted;
		}
	}

}
