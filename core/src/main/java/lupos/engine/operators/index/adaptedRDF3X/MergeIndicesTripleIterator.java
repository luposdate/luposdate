package lupos.engine.operators.index.adaptedRDF3X;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.SequentialHeap;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;

public class MergeIndicesTripleIterator implements Iterator<Triple> {

	private final Comparator<Triple> comparator;
	private final Heap<HeapEntry> heap;
	private int idOfLastElement;

	public MergeIndicesTripleIterator(final IndicesTripleIterator[] itia,
			final RDF3XIndex.CollationOrder collationOrder) {
		comparator = new TripleComparator(collationOrder);
		heap = new SequentialHeap<HeapEntry>(itia.length, true);
		for (int i = 0; i < itia.length; i++) {
			if (itia[i] != null) {
				final Triple t = itia[i].next();
				if (t != null) {
					heap.add(new HeapEntry(t, itia[i]));
				}
			}
		}
	}

	public boolean hasNext() {
		return !(heap.isEmpty());
	}

	public Triple next() {
		if (heap.isEmpty())
			return null;
		final HeapEntry next = heap.pop();
		if (next != null) {
			final Triple t = next.itt.next();
			if (t != null)
				heap.add(new HeapEntry(t, next.itt));
			idOfLastElement = next.itt.getId();
			return next.t;
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public int getIdOfLastElement() {
		return idOfLastElement;
	}

	private class HeapEntry implements Comparable<HeapEntry>, Serializable {

		Triple t;
		IndicesTripleIterator itt;

		public HeapEntry(final Triple t, final IndicesTripleIterator itt) {
			this.t = t;
			this.itt = itt;
		}

		public HeapEntry(final IndicesTripleIterator itt) {
			this.itt = itt;
			this.t = itt.next();
		}

		public int compareTo(final HeapEntry arg0) {
			return comparator.compare(this.t, arg0.t);
		}
	}

	public int getMaxId() {
		return heap.maxLength();
	}
}
