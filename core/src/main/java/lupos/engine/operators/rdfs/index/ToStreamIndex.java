package lupos.engine.operators.rdfs.index;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class ToStreamIndex extends BasicIndex {

	public ToStreamIndex (final IndexCollection indexCollection){
		super(indexCollection);
	}

	public ToStreamIndex (final OperatorIDTuple succeedingOperator, final Collection<TriplePattern> triplePattern, final IndexCollection indexCollection)
	{
		super(indexCollection);
		this.succeedingOperators = new LinkedList<OperatorIDTuple>();

		if (succeedingOperator != null)
		{
			this.succeedingOperators.add(succeedingOperator);
		}
		this.triplePatterns = triplePattern;
	}


	/**
	 * Joins the triple pattern using the index maps and returns the result.<br>
	 * The succeeding operators are passed to the operator pipe to be processed.
	 * @param triplePattern - the triple pattern to be joined
	 * @param succeedingOperators - the succeeding operators to be passed
	 * @return the result of the performed join
	 */
	protected QueryResult process (	final Collection<TriplePattern> triplePattern,
									final List<OperatorIDTuple> succeedingOperators){
		throw new UnsupportedOperationException("join(	final Collection<TriplePattern> triplePattern, final List<OperatorIDTuple> succeedingOperators) is not supported by ToStreamIndex");
	}

	@Override
	public QueryResult join(final Indices indices, final Bindings bindings) {
		throw new UnsupportedOperationException(
				"join(Indices indices, Bindings bindings) is not supported by ToStreamIndex");
	}
}
