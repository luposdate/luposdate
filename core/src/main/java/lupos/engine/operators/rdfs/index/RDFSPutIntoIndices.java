package lupos.engine.operators.rdfs.index;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleOperator;

public class RDFSPutIntoIndices extends TripleOperator implements
		TripleConsumer {

	protected final Indices indices;

	public RDFSPutIntoIndices(final Indices indices) {
		this.indices = indices;
	}

	@Override
	public void consume(final Triple triple) {
		indices.add(triple);
	}
}
