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
package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.heap.Heap;

public abstract class ToSort<E extends Comparable<E>> {

	public enum TOSORT {
		NONE, MERGESORT, PARALLELMERGESORT, QUICKSORT, HEAPSORT
	};

	private static TOSORT tosort = TOSORT.NONE;

	private static int height = 16;

	public abstract void clear();

	public abstract boolean isFull();

	public abstract boolean isEmpty();

	public abstract void add(final E elem);

	public abstract Iterator<E> emptyDatastructure();

	public void release() {
	}

	public static<T extends Comparable<T>> ToSort<T> createInstance(final int height_param) {
		return ToSort.createInstance(ToSort.tosort, height_param);
	}

	public static<T extends Comparable<T>> ToSort<T> createInstance(final TOSORT tosort, final int height) {
		switch (tosort) {
		case MERGESORT:
			return new JavaMergeSort(1 << (height + 1));
		case PARALLELMERGESORT:
			return new ParallelMergeSort(1 << (height + 1));
		case QUICKSORT:
			return new QuickSort(1 << (height + 1));
		case HEAPSORT:
			return new HeapSort(height);
		default:
			return null;
		}
	}

	public static<T extends Comparable<T>> ToSort<T> createInstance() {
		return ToSort.createInstance(ToSort.tosort, ToSort.height);
	}

	public static<T extends Comparable<T>> ToSort<T> cloneInstance(final ToSort tosort) {
		if (tosort instanceof Heap) {
			return Heap.cloneInstance((Heap) tosort);
		} else
			return tosort;
	}

	public static int getHeight() {
		return height;
	}

	public static void setHeight(final int height) {
		ToSort.height = height;
	}

	public static TOSORT getTosort() {
		return ToSort.tosort;
	}

	public static void setTosort(final TOSORT tosort) {
		ToSort.tosort = tosort;
	}
}
