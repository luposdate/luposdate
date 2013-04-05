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

public class LazyBuildingSequentialHeap<E extends Comparable<E>> extends
		OptimizedSequentialHeap<E> {

	private boolean phase1 = true;

	public LazyBuildingSequentialHeap(final int height) {
		super(height);
	}

	public LazyBuildingSequentialHeap(final Object[] arr, final int length) {
		super(arr, length);
	}

	public LazyBuildingSequentialHeap(final int length_or_height,
			final boolean length) {
		super(length_or_height, length);
	}

	@Override
	protected Object[] getContent() {
		buildHeap();
		return super.getContent();
	}

	@Override
	protected void buildHeap() {
		if (phase1) {
			super.buildHeap();
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
			arr[length++] = elem;
		} else
			super.add(elem);
	}
}
