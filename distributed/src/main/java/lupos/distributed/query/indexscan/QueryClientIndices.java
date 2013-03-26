package lupos.distributed.query.indexscan;

import java.io.IOException;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class QueryClientIndices extends Indices {

	public QueryClientIndices(URILiteral uriLiteral) {
		this.rdfName = uriLiteral;
	}

	public QueryResult evaluateTriplePattern(final TriplePattern triplePattern){
		// TODO
		return null;
	}
	
	@Override
	public boolean add(Triple t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Triple t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Triple t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init(DATA_STRUCT ds) {
	}

	@Override
	public void constructCompletely() {
	}

	@Override
	public void writeOutAllModifiedPages() throws IOException {
	}

	@Override
	public int numberOfTriples() {
		// TODO Auto-generated method stub
		return 0;
	}
}
