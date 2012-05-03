package lupos.engine.operators.index;

import java.util.Collection;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class EmptyIndex extends BasicIndex {

	public EmptyIndex(final OperatorIDTuple succeedingOperator, final Collection<TriplePattern> triplePattern, final lupos.engine.operators.index.IndexCollection indexCollection) {
		super(indexCollection);
		this.succeedingOperators = new LinkedList<OperatorIDTuple>();
		if (succeedingOperator != null) {
			this.succeedingOperators.add(succeedingOperator);
		}
		if(triplePattern!=null)
			this.triplePatterns = triplePattern;
		else this.triplePatterns = new LinkedList<TriplePattern>();
	}

	@Override
	public QueryResult process(final int opt, final Dataset dataset) {
		final QueryResult queryResult = QueryResult.createInstance();
		for (final OperatorIDTuple succOperator : succeedingOperators) {

			((Operator) succOperator.getOperator()).processAll(queryResult, succOperator.getId());
		}
		return queryResult;
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		// TODO Auto-generated method stub
		return null;
	}

}
