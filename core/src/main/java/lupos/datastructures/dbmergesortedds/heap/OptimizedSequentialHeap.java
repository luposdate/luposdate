package lupos.datastructures.dbmergesortedds.heap;

public class OptimizedSequentialHeap<E extends Comparable<E>> extends
		SequentialHeap<E> {

	public OptimizedSequentialHeap(final int height) {
		super(height < 2 ? 2 : height);
	}

	public OptimizedSequentialHeap(final Object[] content, final int size) {
		super(content, (content[0] == null && size > 0) ? size + 1 : size);
	}

	public OptimizedSequentialHeap(final int length_or_height,
			final boolean length) {
		super(length_or_height < 3 ? 3 : length_or_height, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#isFull()
	 */
	@Override
	public boolean isFull() {
		return arr[0] != null && size() >= arr.length;
	}

	@Override
	public E peek() {
		if (arr[0] == null) {
			final E e1 = get(1);
			final E e2 = get(2);
			return (e2 == null || e2.compareTo(e1) > 0) ? e1 : e2;
		} else
			return super.peek();
	}

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
