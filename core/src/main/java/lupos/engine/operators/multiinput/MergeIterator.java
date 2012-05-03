package lupos.engine.operators.multiinput;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.SequentialHeap;
import lupos.datastructures.queryresult.ParallelIterator;

public class MergeIterator<E> implements ParallelIterator<E> {
	protected Heap<HeapEntry> heap;
	protected Comparator<? super E> comparator;

	protected MergeIterator() {
	}

	public MergeIterator(final Collection<Iterator<E>> operandResults,
			final Comparator<? super E> comparator) {
		init(operandResults, comparator);
	}

	protected void init(final Collection<Iterator<E>> operandResults,
			final Comparator<? super E> comparator) {
		this.comparator = comparator;
		heap = new SequentialHeap<HeapEntry>(operandResults.size(), true);
		for (final Iterator<E> itb : operandResults) {
			if (itb != null) {
				final E b = itb.next();
				if (b != null) {
					heap.add(new HeapEntry(b, itb));
				}
			}
		}
	}

	public MergeIterator(final Iterator<E>[] operandResults,
			final Comparator<? super E> comparator) {
		init(operandResults, comparator);
	}

	public MergeIterator(final Comparator<? super E> comparator,
			final Iterator<E>... operandResults) {
		init(operandResults, comparator);
	}

	protected void init(final Iterator<E>[] operandResults,
			final Comparator<? super E> comparator) {
		this.comparator = comparator;
		heap = new SequentialHeap<HeapEntry>(operandResults.length, true);
		for (final Iterator<E> itb : operandResults) {
			if (itb != null) {
				if (itb.hasNext()) {
					final E b = itb.next();
					if (b != null) {
						heap.add(new HeapEntry(b, itb));
					}
				}
			}
		}
	}

	public boolean hasNext() {
		return !heap.isEmpty();
	}

	public E next() {
		if (heap.isEmpty())
			return null;
		final HeapEntry next = heap.pop();
		if (next != null) {
			if (next.itb.hasNext()) {
				final E b = next.itb.next();
				if (b != null)
					heap.add(new HeapEntry(b, next.itb));
				else if (next.itb instanceof ParallelIterator)
					((ParallelIterator) next.itb).close();
			} else if (next.itb instanceof ParallelIterator)
				((ParallelIterator) next.itb).close();
			return next.b;
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	private class HeapEntry implements Comparable<HeapEntry>, Serializable {

		E b;
		Iterator<E> itb;

		public HeapEntry(final E b, final Iterator<E> itb) {
			this.b = b;
			this.itb = itb;
		}

		public HeapEntry(final Iterator<E> itb) {
			this.itb = itb;
			this.b = itb.next();
		}

		public int compareTo(final HeapEntry arg0) {
			return comparator.compare(this.b, arg0.b);
		}
	}

	public void close() {
		while (!heap.isEmpty()) {
			final HeapEntry next = heap.pop();
			if (next.itb instanceof ParallelIterator)
				((ParallelIterator) next.itb).close();
		}
	}
}
