package lupos.engine.operators.singleinput.readtriplesdistinct;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class ReadTriplesDistinct extends SingleInputOperator {
	public QueryResult process(QueryResult bindings, int operandID) {
		throw(new UnsupportedOperationException("This Operator("+this+") should have been replaced before being used."));
	}
}