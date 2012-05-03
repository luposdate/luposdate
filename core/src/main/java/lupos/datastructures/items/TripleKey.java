package lupos.datastructures.items;

import java.io.Serializable;

import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex;

public class TripleKey implements Comparable<TripleKey>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6205248978579168911L;

	protected Triple triple;
	protected TripleComparator comp;

	public TripleKey() {
	}

	public TripleKey(final Triple triple, final TripleComparator comp) {
		this.triple = triple;
		this.comp = comp;
	}

	public TripleKey(final Triple triple, final RDF3XIndex.CollationOrder order) {
		this.triple = triple;
		this.comp = new TripleComparator(order);
		this.comp.makeNoneForNull(triple);
	}

	public int compareTo(final TripleKey arg0) {
		return comp.compare(triple, arg0.triple);
	}

	public int compareTo(final Triple arg0) {
		return comp.compare(triple, arg0);
	}

	public Triple getTriple() {
		return triple;
	}

	public void setTriple(final Triple triple) {
		this.triple = triple;
	}

	public TripleComparator getTripleComparator() {
		return comp;
	}

	public void setTripleComparator(final TripleComparator comp) {
		this.comp = comp;
	}

	@Override
	public String toString() {
		return "TripleKey of " + triple.toString() + ", " + comp.toString();
	}
}
