/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.tosort.ToSort;

public abstract class Heap<E extends Comparable<E>> extends ToSort<E> {

	public enum HEAPTYPE {
		DEFAULT, SEQUENTIAL, OPTIMIZEDSEQUENTIAL, PARALLEL, PARALLEL8, PARALLEL16, PARALLEL32, LAZYOPTIMIZEDSEQUENTIAL, LAZYPARALLEL, LAZYPARALLEL8, LAZYPARALLEL16, LAZYPARALLEL32, SORTANDMERGEHEAP, SORTANDMERGEHEAPUSINGMERGESORT
	};

	protected static HEAPTYPE heapType = HEAPTYPE.SEQUENTIAL;

	public abstract int maxLength();

	public abstract E peek();

	public abstract E pop();

	public abstract int size();

	@Override
	public Iterator<E> emptyDatastructure() {
		return new Iterator<E>() {

			public boolean hasNext() {
				return !isEmpty();
			}

			public E next() {
				return pop();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	protected abstract Object[] getContent();

	@Override
	public abstract String toString();

	public static Heap createInstance(final int height) {
		return createInstance(height, Heap.heapType);
	}

	public static Heap createInstance(final int height, final HEAPTYPE heapType) {
		switch (heapType) {
		default:
		case SEQUENTIAL:
			return new SequentialHeap(height);
		case OPTIMIZEDSEQUENTIAL:
			return new OptimizedSequentialHeap(height);
		case PARALLEL:
			return new ParallelHeap(height);
		case PARALLEL8:
			return new ParallelHeap(height, 3);
		case PARALLEL16:
			return new ParallelHeap(height, 4);
		case PARALLEL32:
			return new ParallelHeap(height, 5);
		case LAZYOPTIMIZEDSEQUENTIAL:
			return new LazyBuildingSequentialHeap(height);
		case LAZYPARALLEL:
			return new LazyBuildingParallelHeap(height);
		case LAZYPARALLEL8:
			return new LazyBuildingParallelHeap(height, 3);
		case LAZYPARALLEL16:
			return new LazyBuildingParallelHeap(height, 4);
		case LAZYPARALLEL32:
			return new LazyBuildingParallelHeap(height, 5);
		case SORTANDMERGEHEAP:
			return new SortAndMergeHeap(1 << height);
		case SORTANDMERGEHEAPUSINGMERGESORT:
			return new SortAndMergeHeap(1 << height, false);
		}
	}

	public static Heap createInstance(final int length_or_height,
			final boolean length) {
		return createInstance(length_or_height, length, Heap.heapType);
	}

	public static Heap createInstance(final int length_or_height,
			final boolean length, final HEAPTYPE heapType) {
		switch (heapType) {
		default:
		case SEQUENTIAL:
			return new SequentialHeap(length_or_height, length);
		case OPTIMIZEDSEQUENTIAL:
			return new OptimizedSequentialHeap(length_or_height, length);
		case PARALLEL:
			return new ParallelHeap(length_or_height, length);
		case PARALLEL8:
			return new ParallelHeap(length_or_height, length, 3);
		case PARALLEL16:
			return new ParallelHeap(length_or_height, length, 4);
		case PARALLEL32:
			return new ParallelHeap(length_or_height, length, 5);
		case LAZYOPTIMIZEDSEQUENTIAL:
			return new LazyBuildingSequentialHeap(length_or_height, length);
		case LAZYPARALLEL:
			return new LazyBuildingParallelHeap(length_or_height, length);
		case LAZYPARALLEL8:
			return new LazyBuildingParallelHeap(length_or_height, length, 3);
		case LAZYPARALLEL16:
			return new LazyBuildingParallelHeap(length_or_height, length, 4);
		case LAZYPARALLEL32:
			return new LazyBuildingParallelHeap(length_or_height, length, 5);
		case SORTANDMERGEHEAP:
			return new SortAndMergeHeap(length ? length_or_height
					: 1 << length_or_height);
		case SORTANDMERGEHEAPUSINGMERGESORT:
			return new SortAndMergeHeap(length ? length_or_height
					: 1 << length_or_height, false);
		}
	}

	public static Heap cloneInstance(final Heap heap) {
		return cloneInstance(heap, Heap.heapType);
	}

	public static Heap cloneInstance(final Heap heap, final HEAPTYPE heapType) {
		switch (heapType) {
		default:
		case SEQUENTIAL:
			return new SequentialHeap(heap.getContent(), heap.size());
		case OPTIMIZEDSEQUENTIAL:
			return new OptimizedSequentialHeap(heap.getContent(), heap.size());
		case PARALLEL:
			return new ParallelHeap(heap.getContent(), heap.size());
		case PARALLEL8:
			return new ParallelHeap(heap.getContent(), heap.size(), 3);
		case PARALLEL16:
			return new ParallelHeap(heap.getContent(), heap.size(), 4);
		case PARALLEL32:
			return new ParallelHeap(heap.getContent(), heap.size(), 5);
		case LAZYOPTIMIZEDSEQUENTIAL:
			return new LazyBuildingSequentialHeap(heap.getContent(), heap
					.size());
		case LAZYPARALLEL:
			return new LazyBuildingParallelHeap(heap.getContent(), heap.size());
		case LAZYPARALLEL8:
			return new LazyBuildingParallelHeap(heap.getContent(), heap.size(),
					3);
		case LAZYPARALLEL16:
			return new LazyBuildingParallelHeap(heap.getContent(), heap.size(),
					4);
		case LAZYPARALLEL32:
			return new LazyBuildingParallelHeap(heap.getContent(), heap.size(),
					5);
		case SORTANDMERGEHEAP:
			return new SortAndMergeHeap(heap.getContent(), heap.maxLength());
		case SORTANDMERGEHEAPUSINGMERGESORT:
			return new SortAndMergeHeap(heap.getContent(), heap.maxLength(),
					false);
		}
	}

	public static HEAPTYPE getHeapType() {
		return heapType;
	}

	public static void setHeapType(final HEAPTYPE heapType) {
		Heap.heapType = heapType;
	}
}