package lupos.datastructures.bindings;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex;
import lupos.rdf.Prefix;

public class BindingsArrayReadTriples extends BindingsArray {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5634651341313907266L;

	protected List<Triple> readTriples = new LinkedList<Triple>();

	@Override
	public BindingsArrayReadTriples clone() {
		final BindingsArrayReadTriples other = new BindingsArrayReadTriples();
		// System.arraycopy(this.literals, 0, other.literals, 0,
		// this.literals.length);
		other.cloneLiterals(getLiterals());
		other.readTriples.addAll(readTriples);

		return other;
	}

	/**
	 * This method adds a triple to the internal list of read triples for these
	 * bindings
	 */
	@Override
	public void addTriple(final Triple triple) {
		readTriples.add(triple);
	}

	/**
	 * This method adds all triples to the internal list of read triples for
	 * these bindings. This method must be overridden by Bindings-subclasses,
	 * which support this feature, e.g. BindingsArrayReadTriples
	 */
	@Override
	public void addAllTriples(final Collection<Triple> triples) {
		if (triples != null)
			readTriples.addAll(triples);
	}

	/**
	 * This method adds all triples of a given Bindings to the internal list of
	 * read triples for these bindings. This method must be overridden by
	 * Bindings-subclasses, which support this feature, e.g.
	 * BindingsArrayReadTriples
	 */
	@Override
	public void addAllTriples(final Bindings bindings) {
		addAllTriples(bindings.getTriples());
	}

	/**
	 * This method returns the internal list of read triples for these bindings.
	 */
	@Override
	public List<Triple> getTriples() {
		return readTriples;
	}

	@Override
	public String toString() {
		return super.toString() + " read triples:" + readTriples + "\n";
	}
	
	@Override
	public String toString(Prefix prefix) {
		String result = super.toString(prefix) + " read triples: [";
		boolean firstTime=true;
		for(Triple t: readTriples){
			if(firstTime)
				firstTime=false;
			else result+=", ";
			result+=t.toString(prefix);
		}
		return result+"]";
	}


	@Override
	public void init() {
		super.init();
		readTriples = new LinkedList<Triple>();
	}

	public void sortReadTriples() {
		final SortedSet<Triple> sst = new TreeSet<Triple>(new TripleComparator(
				RDF3XIndex.CollationOrder.SPO));
		sst.addAll(readTriples);
		readTriples.clear();
		readTriples.addAll(sst);
	}
}
