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
