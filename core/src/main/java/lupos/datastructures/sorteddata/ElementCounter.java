package lupos.datastructures.sorteddata;

import java.util.LinkedList;
import java.util.List;

public class ElementCounter<E> {
	protected final LinkedList<E> elements = new LinkedList<E>();

	public ElementCounter(final E element) {
		this.elements.add(element);
	}

	public void add(final E element) {
		this.elements.add(element);
	}

	public List<E> getElements() {
		return elements;
	}
}
