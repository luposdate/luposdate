package lupos.engine.operators.singleinput;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

public class GroupByAddComputedBinding extends AddComputedBinding {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1787633855746419591L;

	/**
	 * Modified method of AddComputedBinding which doesn't merge the bindings of
	 * the QueryResult
	 * 
	 * @param QueryResult
	 * @param int
	 * @return QueryResult
	 */
	public QueryResult process(final QueryResult bindings, final int operandID) {
		return getQueryResultForAggregatedFilter(bindings);
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		return msg;
	}
	
	public Message preProcessMessageDebug(final ComputeIntermediateResultMessage msg, final DebugStep debugstep) {
		return msg;
	}
}
