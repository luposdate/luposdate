package lupos.engine.operators.singleinput;

import lupos.datastructures.queryresult.QueryResult;

public class SeveralSucceedingOperators extends SingleInputOperator
{
	public SeveralSucceedingOperators(){
	}
	
	public QueryResult process(QueryResult bindings, int operandID){
		return bindings;
	}
}