package lupos.engine.operators.singleinput;

import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.rdf.Prefix;

public class ComputeBindings extends SingleInputOperator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6315017556187823149L;

	protected QueryResult queryResult;

	public ComputeBindings(QueryResult qr) {
		queryResult = qr;
	}

	/**
	 * saving the QueryResult
	 * 
	 * @param QueryResult
	 * @param int
	 * @return QueryResult
	 */
	public synchronized QueryResult process(final QueryResult bindings, final int operandID) {
		return queryResult;
	}

	/**
	 * Sets the intersection variables and the union variables for the given
	 * query result and returns a message with all used variables
	 * 
	 * @param BoundVariablesMessage
	 *            msg
	 * @return Message
	 */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		BoundVariablesMessage msgResult = new BoundVariablesMessage(msg);
		final HashSet<Variable> variables = new HashSet<Variable>();
		if (!queryResult.isEmpty()) {
			variables.addAll(queryResult.iterator().next().getVariableSet());
		}
		for (final Bindings b : queryResult) {
			variables.retainAll(b.getVariableSet());
		}
		intersectionVariables = variables;
		unionVariables = queryResult.getVariableSet();
		msgResult.setVariables(variables);
		return msgResult;
	}

	public String toString() {
		return super.toString() + " " + queryResult;
	}

	public String toString(Prefix prefixInstance) {
		return super.toString() + " " + queryResult.toString(prefixInstance);
	}
}
