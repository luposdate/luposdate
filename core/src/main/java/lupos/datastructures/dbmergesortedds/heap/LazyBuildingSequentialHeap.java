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
