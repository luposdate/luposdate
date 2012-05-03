package lupos.engine.operators.singleinput.generate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SingleInputOperator;

public class GenerateAddEnv extends SingleInputOperator {
	private HashMap<Variable, Literal> constants;
	private HashMap<Variable, Literal> conditions;

	public GenerateAddEnv() {
	}

	public HashMap<Variable, Literal> getConstants() {
		return constants;
	}

	public HashMap<Variable, Literal> getConditions() {
		return conditions;
	}

	public GenerateAddEnv(final HashMap<Variable, Literal> conditions,
			final HashMap<Variable, Literal> constants) {
		this.constants = constants;
		this.conditions = conditions;
	}

	// bindings should contain exactly one element!
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final QueryResult result = QueryResult.createInstance();

		final Iterator<Bindings> itb = bindings.iterator();
		Bindings bind1;
		if (itb.hasNext())
			bind1 = itb.next();
		else
			return null;

		boolean conditionFulfilled = true;

		// check conditions...
		Iterator<Variable> it = conditions.keySet().iterator();

		while (it.hasNext()) {
			final Variable elem = it.next();
			if (!conditions.get(elem).valueEquals(bind1.get(elem))) {
				conditionFulfilled = false;
			}
		}

		if (conditionFulfilled) {
			final Bindings bnew = bind1.clone();

			it = constants.keySet().iterator();
			while (it.hasNext()) {
				final Variable elem = it.next();
				bind1.add(elem, constants.get(elem));
			}
			result.add(bnew);
		}

		result.add(bind1);
		return result;
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		msg.getVariables().addAll(constants.keySet());
		intersectionVariables = new LinkedList<Variable>();
		intersectionVariables.addAll(msg.getVariables());
		unionVariables = new LinkedList<Variable>();
		unionVariables.addAll(msg.getVariables());
		return msg;
	}

	@Override
	public String toString() {
		return super.toString() + conditions + "," + constants;
	}
}