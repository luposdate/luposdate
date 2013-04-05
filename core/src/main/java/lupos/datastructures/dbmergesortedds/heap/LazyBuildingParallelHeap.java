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
package lupos.datastructures.dbmergesortedds.heap;

public class LazyBuildingParallelHeap<E extends Comparable<E>> extends
		ParallelHeap<E> {

	private boolean phase1 = true;

	public LazyBuildingParallelHeap(final int height) {
		super(height);
	}

	public LazyBuildingParallelHeap(final int height,
			final int localFirstHeapHeight) {
		super(height, localFirstHeapHeight);
	}

	public LazyBuildingParallelHeap(final Object[] content, final int size) {
		super(content, size);
	}

	public LazyBuildingParallelHeap(final Object[] content, final int size,
			final int localFirstHeapHeight) {
		super(content, size, localFirstHeapHeight);
	}

	public LazyBuildingParallelHeap(final int length_or_height,
			final boolean length) {
		super(length_or_height, length);
	}

	public LazyBuildingParallelHeap(final int length_or_height,
			final boolean length, final int localFirstHeapHeight) {
		super(length_or_height, length, localFirstHeapHeight);
	}

	@Override
	protected void initParallelHeaps() {
		for (int i = (1 << localFirstHeapHeight) - 1; i < arr.length; i++) {
			final InnerParallelHeap<E> ihe_new = new InnerParallelHeap<E>(
					new LazyBuildingSequentialHeap<E>(this.height
							- localFirstHeapHeight));
			this.arr[i] = ihe_new;
			ihe_new.start();
		}
	}

	@Override
	protected Object[] getContent() {
		buildHeap();
		return super.getContent();
	}

	@Override
	protected void buildHeap() {
		if (phase1) {
			for (int i = (1 << localFirstHeapHeight) - 1; i < arr.length; i++) {
				final InnerParallelHeap<E> ihe = (InnerParallelHeap<E>) arr[i];
				ihe.buildHeap();
			}
			for (int i = Math.min(length, (1 << localFirstHeapHeight) - 2); i >= 0; i--) {
				bubbleDown(i);
			}
			phase1 = false;
		}
	}

	@Override
	public void clear() {
		super.clear();
		phase1 = true;
	}

	@Override
	public E peek() {
		buildHeap();
		return super.peek();
	}

	@Override
	public E pop() {
		buildHeap();
		return super.pop();
	}

	@Override
	public String toString() {
		buildHeap();
		return super.toString();
	}

	@Override
	public void add(final E elem) {
		if (phase1) {
			if (length < (1 << localFirstHeapHeight) - 1) {
				arr[length++] = elem;
			} else {
				final int[] ia = getPathToElement(length);
				for (int index = ia[0]; index > 0; index--) {
					// for (int i = 0; i < ia.length; i++)
					// System.out.print(ia[i] + " ");
					// System.out.println();
					// System.out.println(Arrays.toString(arr));
					final int i = ia[index];
					if (arr[i] instanceof InnerParallelHeap) {
						length++;
						final InnerParallelHeap<E> ihe = (InnerParallelHeap<E>) arr[i];
						ihe.sh.arr[ihe.sh.length++] = elem;
						break;
					}
				}
			}
		} else
			super.add(elem);
	}
}
