package lupos.engine.operators.tripleoperator;

import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;

public class TriggerOneTime extends TripleOperator implements TripleConsumer {

	protected boolean firstTime = true;
	protected final boolean addEmptyBindings;

	public TriggerOneTime() {
		this(true);
	}

	public TriggerOneTime(boolean addEmptyBindings) {
		unionVariables = new HashSet<Variable>();
		intersectionVariables = unionVariables;
		this.addEmptyBindings = addEmptyBindings;
	}

	@Override
	public void consume(final Triple triple) {
		trigger();
	}
	
	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		firstTime=true;
		return msg;
	}
	
	public Message postProcessMessage(final StartOfEvaluationMessage msg) {
		trigger();
		return msg;
	}
	
	public void trigger(){
		if (firstTime) {
			final QueryResult ll = QueryResult.createInstance();
			if(addEmptyBindings)
				ll.add(Bindings.createNewInstance());
			for (final OperatorIDTuple op : getSucceedingOperators()) {
				((Operator) op.getOperator()).processAll(ll, op.getId());
			}
			firstTime = false;
		}
	}

	@Override
	public String toString(){
		return super.toString()+" "+((addEmptyBindings)?"1 empty bindings":"1 empty queryresult");
	}
}
