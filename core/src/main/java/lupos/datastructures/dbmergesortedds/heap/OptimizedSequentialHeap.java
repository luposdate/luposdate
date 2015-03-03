
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.datastructures.dbmergesortedds.heap;
public class OptimizedSequentialHeap<E extends Comparable<E>> extends
		SequentialHeap<E> {

	/**
	 * <p>Constructor for OptimizedSequentialHeap.</p>
	 *
	 * @param height a int.
	 */
	public OptimizedSequentialHeap(final int height) {
		super(height < 2 ? 2 : height);
	}

	/**
	 * <p>Constructor for OptimizedSequentialHeap.</p>
	 *
	 * @param content an array of {@link java.lang.Object} objects.
	 * @param size a int.
	 */
	public OptimizedSequentialHeap(final Object[] content, final int size) {
		super(content, (content[0] == null && size > 0) ? size + 1 : size);
	}

	/**
	 * <p>Constructor for OptimizedSequentialHeap.</p>
	 *
	 * @param length_or_height a int.
	 * @param length a boolean.
	 */
	public OptimizedSequentialHeap(final int length_or_height,
			final boolean length) {
		super(length_or_height < 3 ? 3 : length_or_height, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#isFull()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFull() {
		return arr[0] != null && size() >= arr.length;
	}

	/** {@inheritDoc} */
	@Override
	public E peek() {
		if (arr[0] == null) {
			final E e1 = get(1);
			final E e2 = get(2);
			return (e2 == null || e2.compareTo(e1) > 0) ? e1 : e2;
		} else
			return super.peek();
	}

	/** {@inheritDoc} */
	@Override
	public E pop() {
		if (arr[0] == null) {
			if (length == 0 || length == 1) {
				length = 0;
				return null;
			}
			arr[0] = arr[--length];
			arr[length] = null;
			bubbleDown(0);
		}
		final E e = get(0);
		arr[0] = null;
		if (length == 1)
			length = 0;
		return e;
	}

	/** {@inheritDoc} */
	@Override
	public void add(final E elem) {
		if (arr[0] == null) {
			if (length == 0)
				length++;
			arr[0] = elem;
			bubbleDown(0);
		} else
			super.add(elem);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		if (length == 0)
			return 0;
		else if (arr[0] == null)
			return length - 1;
		else
			return length;
	}
}
