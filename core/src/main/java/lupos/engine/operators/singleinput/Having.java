package lupos.engine.operators.singleinput;

import lupos.datastructures.queryresult.QueryResult;
import lupos.sparql1_1.ASTFilterConstraint;

public class Having extends Filter {

	public Having(ASTFilterConstraint astFilterConstraint) {
		super(astFilterConstraint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8734474236903049575L;

	/**
	 * Modified Filter for HAVING statement so that the query results aren't
	 * merged
	 * 
	 * @param QueryResult
	 * @param int
	 * @return QueryResult
	 */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (aggregationFunctions == null) {
			return super.process(bindings, operandID);
		} else {
			return getQueryResultForAggregatedFilter(this.getNodePointer(), bindings, aggregationFunctions, this.getUsedEvaluationVisitor());
		}
	}

}
