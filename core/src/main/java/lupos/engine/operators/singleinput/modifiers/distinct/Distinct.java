package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.*;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class Distinct extends SingleInputOperator {
	public QueryResult process(QueryResult bindings, int operandID) {
		throw(new UnsupportedOperationException("This Operator("+this+") should have been replaced before being used."));
	}
	
	public boolean remainsSortedData(Collection<Variable> sortCriterium){
		return true;
	}
}