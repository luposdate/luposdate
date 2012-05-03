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

	public static ToSort createInstance(final int height) {
		switch (tosort) {
		case MERGESORT:
			return new JavaMergeSort(1 << height);
		case PARALLELMERGESORT:
			return new ParallelMergeSort(1 << height);
		case QUICKSORT:
			return new QuickSort(1 << height);
		case HEAPSORT:
			return new HeapSort(height);
		default:
			return null;
		}
	}

	public static ToSort createInstance(final TOSORT tosort, final int height) {
		switch (tosort) {
		case MERGESORT:
			return new JavaMergeSort(1 << height);
		case PARALLELMERGESORT:
			return new ParallelMergeSort(1 << height);
		case QUICKSORT:
			return new QuickSort(1 << height);
		case HEAPSORT:
			return new HeapSort(height);
		default:
			return null;
		}
	}

	public static ToSort createInstance() {
		switch (tosort) {
		case MERGESORT:
			return new JavaMergeSort(1 << height);
		case PARALLELMERGESORT:
			return new ParallelMergeSort(1 << height);
		case QUICKSORT:
			return new QuickSort(1 << height);
		case HEAPSORT:
			return new HeapSort(height);
		default:
			return null;
		}
	}

	public static ToSort cloneInstance(final ToSort tosort) {
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
