package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

public class InMemoryListDistinct extends Distinct {

	LinkedList<Bindings> bindings = new LinkedList<Bindings>();

	public QueryResult process(final QueryResult _bindings, final int operandID) {
		for (final Bindings b : _bindings) {
			if (!bindings.contains(b)) {
				bindings.add(b);
			}
		}
		return null;
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		for (final Bindings b : bindings) {
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(b);
			}
		}
		bindings.clear();
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		preProcessMessage(new EndOfEvaluationMessage());
		return msg;
	}
	
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		final QueryResult qr = QueryResult.createInstance();
		for (final Bindings b : bindings)
			qr.add(b);
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		bindings.clear();
		return msg;
	}
}