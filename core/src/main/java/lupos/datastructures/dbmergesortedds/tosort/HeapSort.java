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
package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.misc.util.ImmutableIterator;
public class HeapSort<E extends Comparable<E>> extends ToSort<E> {

	private final Heap<E> heap;

	/**
	 * <p>Constructor for HeapSort.</p>
	 *
	 * @param height a int.
	 */
	public HeapSort(final int height) {
		this.heap = Heap.createInstance(height);
	}

	/**
	 * <p>Constructor for HeapSort.</p>
	 *
	 * @param length_or_height a int.
	 * @param length a boolean.
	 */
	public HeapSort(final int length_or_height, final boolean length) {
		this.heap = Heap.createInstance(length_or_height, length);
	}

	/** {@inheritDoc} */
	@Override
	public void add(final E elem) {
		this.heap.add(elem);
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.heap.clear();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<E> emptyDatastructure() {
		// return heap.emptyDatastructure();
		return new ImmutableIterator<E>() {

			@Override
			public boolean hasNext() {
				return !HeapSort.this.heap.isEmpty();
			}

			@Override
			public E next() {
				if (!HeapSort.this.heap.isEmpty()) {
					return HeapSort.this.heap.pop();
				} else {
					return null;
				}
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.heap.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isFull() {
		return this.heap.isFull();
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		this.heap.release();
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.heap.size();
	}
}
