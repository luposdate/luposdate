package lupos.engine.operators.rdfs.index;

import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TripleConsumer;

public class RDFSPutIntoIndicesCyclicComputation extends RDFSPutIntoIndices
		implements TripleConsumer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4752511368314625259L;
	private final Set<Triple> newTriplesSet;

	public RDFSPutIntoIndicesCyclicComputation(final Set<Triple> newTriplesSet,
			final Indices indices) {
		super(indices);
		this.newTriplesSet = newTriplesSet;
	}

	public void newTripleProcessing() {
		newTriplesSet.clear();
	}

	public boolean getNewTriples() {
		return newTriplesSet.size() > 0;
	}

	@Override
	public void consume(final Triple triple) {
		if (!newTriplesSet.contains(triple) && !indices.contains(triple)) {
			newTriplesSet.add(triple);
		}
	}
}
