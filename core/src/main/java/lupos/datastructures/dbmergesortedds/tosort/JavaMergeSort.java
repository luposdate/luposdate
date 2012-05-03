package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Arrays;

public class JavaMergeSort<E extends Comparable<E>> extends InPlaceSort<E> {

	public JavaMergeSort(final int length) {
		super(length);
	}

	@Override
	public void sort() {
		sort(0, length - 1);
	}

	@Override
	public void sort(final int unten, final int oben) {
		Arrays.sort(elements, unten, oben - unten + 1);
	}
}
