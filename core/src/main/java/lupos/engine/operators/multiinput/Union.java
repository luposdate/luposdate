package lupos.engine.operators.multiinput;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;

public class Union extends MultiInputOperator {

	public Union() {
		super();
	}

	public QueryResult process(final QueryResult bindings, final int operandID) {
		return bindings;
	}
	
	@Override
	public Message preProcessMessage(BoundVariablesMessage msg) {
		BoundVariablesMessage msg_result = new BoundVariablesMessage(msg); 
		msg_result.setVariables(intersectionVariables);
		return msg_result;
	}
}
