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
			final RDF3XIndexScan.CollationOrder collationOrder) {
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
