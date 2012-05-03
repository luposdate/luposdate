package lupos.datastructures.dbmergesortedds.tosort;

import java.util.Iterator;

public abstract class InPlaceSort<E extends Comparable<E>> extends ArraySort<E> {

	public InPlaceSort(final int length) {
		super(length);
	}

	@Override
	public Iterator<E> emptyDatastructure() {
		sort(0, length - 1);
		return new Iterator<E>() {
			private int index = 0;

			public boolean hasNext() {
				return index < length;
			}

			public E next() {
				return (E) elements[index++];
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public abstract void sort(final int unten, final int oben);
}
