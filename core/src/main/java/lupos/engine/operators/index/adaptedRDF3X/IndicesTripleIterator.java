package lupos.engine.operators.index.adaptedRDF3X;

import java.util.Iterator;

import lupos.datastructures.items.Triple;

public class IndicesTripleIterator implements Iterator<Triple> {

	private final Iterator<Triple> it;
	private final int id;

	public IndicesTripleIterator(final Iterator<Triple> it, final int id) {
		this.it = it;
		this.id = id;
	}

	public boolean hasNext() {
		return it.hasNext();
	}

	public Triple next() {
		return it.next();
	}

	public void remove() {
		it.remove();
	}

	public int getId() {
		return id;
	}

}
