package lupos.engine.operators.messages;

import java.util.Collection;
import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;

public class BoundVariablesMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6641732433881606823L;

	private Collection<Variable> variables = new HashSet<Variable>();

	public BoundVariablesMessage() {
	}

	public BoundVariablesMessage(final Message msg) {
		super(msg);
		// variables.addAll(((BoundVariablesMessage)msg).variables);
	}

	@Override
	public Message postProcess(final BasicOperator op) {
		return op.postProcessMessage(this);
	}

	@Override
	public Message preProcess(final BasicOperator op) {
		return op.preProcessMessage(this);
	}

	@Override
	public Message merge(final Collection<Message> msgs, final BasicOperator op) {
		return op.mergeMessages(msgs, this);
	}

	public Collection<Variable> getVariables() {
		return variables;
	}

	public void setVariables(final Collection<Variable> variables) {
		this.variables = variables;
	}

	@Override
	public String toString() {
		return super.toString() + " " + variables;
	}

	@Override
	public Message clone() {
		final BoundVariablesMessage msg = new BoundVariablesMessage(this);
		msg.variables = new HashSet<Variable>();
		msg.variables.addAll(variables);
		msg.visited = (HashSet<BasicOperator>) visited.clone();
		return msg;
	}

}
