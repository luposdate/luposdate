
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
package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.heap.Heap;
public abstract class ToSort<E extends Comparable<E>> {

	public enum TOSORT {
		NONE, MERGESORT, PARALLELMERGESORT, QUICKSORT, HEAPSORT,
		LSDRADIXSORT // Note: LSADRADIXSORT sorts only strings!
	};

	private static TOSORT tosort = TOSORT.NONE;

	private static int height = 16;

	/**
	 * <p>clear.</p>
	 */
	public abstract void clear();

	/**
	 * <p>isFull.</p>
	 *
	 * @return a boolean.
	 */
	public abstract boolean isFull();

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public abstract boolean isEmpty();

	/**
	 * <p>size.</p>
	 *
	 * @return a int.
	 */
	public abstract int size();

	/**
	 * <p>add.</p>
	 *
	 * @param elem a E object.
	 */
	public abstract void add(final E elem);

	/**
	 * <p>emptyDatastructure.</p>
	 *
	 * @return a {@link java.util.Iterator} object.
	 */
	public abstract Iterator<E> emptyDatastructure();

	/**
	 * <p>release.</p>
	 */
	public void release() {
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param height_param a int.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort} object.
	 */
	public static<T extends Comparable<T>> ToSort<T> createInstance(final int height_param) {
		return ToSort.createInstance(ToSort.tosort, height_param);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param tosort a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT} object.
	 * @param height a int.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort} object.
	 */
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
		case LSDRADIXSORT:
			return (ToSort<T>) new LSDRadixSort(1 << (height + 1));
		default:
			return null;
		}
	}

	/**
	 * <p>createInstanceWithGivenNumberOfElements.</p>
	 *
	 * @param tosort a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT} object.
	 * @param numberOfElements a int.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort} object.
	 */
	public static<T extends Comparable<T>> ToSort<T> createInstanceWithGivenNumberOfElements(final TOSORT tosort, final int numberOfElements) {
		switch (tosort) {
		case MERGESORT:
			return new JavaMergeSort(numberOfElements);
		case PARALLELMERGESORT:
			return new ParallelMergeSort(numberOfElements);
		case QUICKSORT:
			return new QuickSort(numberOfElements);
		case HEAPSORT:
			return new HeapSort(numberOfElements, true);
		case LSDRADIXSORT:
			return (ToSort<T>) new LSDRadixSort(numberOfElements);
		default:
			return null;
		}
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort} object.
	 */
	public static<T extends Comparable<T>> ToSort<T> createInstance() {
		return ToSort.createInstance(ToSort.tosort, ToSort.height);
	}

	/**
	 * <p>cloneInstance.</p>
	 *
	 * @param tosort a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort} object.
	 * @param <T> a T object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort} object.
	 */
	public static<T extends Comparable<T>> ToSort<T> cloneInstance(final ToSort tosort) {
		if (tosort instanceof Heap) {
			return Heap.cloneInstance((Heap) tosort);
		} else {
			return tosort;
		}
	}

	/**
	 * <p>Getter for the field <code>height</code>.</p>
	 *
	 * @return a int.
	 */
	public static int getHeight() {
		return height;
	}

	/**
	 * <p>Setter for the field <code>height</code>.</p>
	 *
	 * @param height a int.
	 */
	public static void setHeight(final int height) {
		ToSort.height = height;
	}

	/**
	 * <p>Getter for the field <code>tosort</code>.</p>
	 *
	 * @return a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT} object.
	 */
	public static TOSORT getTosort() {
		return ToSort.tosort;
	}

	/**
	 * <p>Setter for the field <code>tosort</code>.</p>
	 *
	 * @param tosort a {@link lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT} object.
	 */
	public static void setTosort(final TOSORT tosort) {
		ToSort.tosort = tosort;
	}
}
