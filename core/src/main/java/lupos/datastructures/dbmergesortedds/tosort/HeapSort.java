package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.heap.Heap;

public class HeapSort<E extends Comparable<E>> extends ToSort<E> {

	private final Heap<E> heap;

	public HeapSort(final int height) {
		heap = Heap.createInstance(height);
	}

	@Override
	public void add(final E elem) {
		heap.add(elem);
	}

	@Override
	public void clear() {
		heap.clear();
	}

	@Override
	public Iterator<E> emptyDatastructure() {
		// return heap.emptyDatastructure();
		return new Iterator<E>() {

			public boolean hasNext() {
				return !heap.isEmpty();
			}

			public E next() {
				if (!heap.isEmpty())
					return heap.pop();
				else
					return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	public boolean isEmpty() {
		return heap.isEmpty();
	}

	@Override
	public boolean isFull() {
		return heap.isFull();
	}

	@Override
	public void release() {
		heap.release();
	}
}
