package lupos.datastructures.dbmergesortedds.tosort;

public abstract class ArraySort<E extends Comparable<E>> extends ToSort<E> {

	protected Object[] elements;
	protected int length = 0;

	public ArraySort(final int length) {
		elements = new Object[length];
	}

	@Override
	public void add(final E elem) {
		if (!isFull())
			elements[length++] = elem;
	}

	@Override
	public void clear() {
		length = 0;
	}

	@Override
	public boolean isEmpty() {
		return (length == 0);
	}

	@Override
	public boolean isFull() {
		return (length == elements.length);
	}

	public Object[] getElements() {
		return elements;
	}

	public void setElements(final Object[] elements, final int length) {
		this.elements = elements;
		this.length = length;
	}

	public int size() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public abstract void sort();
}
