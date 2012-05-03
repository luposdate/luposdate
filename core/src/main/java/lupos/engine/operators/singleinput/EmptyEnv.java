package lupos.engine.operators.singleinput;

import lupos.datastructures.queryresult.QueryResult;

public class EmptyEnv extends SingleInputOperator {

	public QueryResult process(QueryResult bindings, int operandID){
		return QueryResult.createInstance();
	}
}
