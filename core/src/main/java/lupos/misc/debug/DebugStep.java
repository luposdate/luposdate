package lupos.misc.debug;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.messages.Message;

public interface DebugStep {
	/**
	 * This method is called whenever an intermediate result is transmitted
	 * between two operators
	 */
	public void step(BasicOperator from, BasicOperator to, Bindings bindings);

	/**
	 * This method is called whenever an intermediate result to be deleted is
	 * transmitted between two operators
	 */
	public void stepDelete(BasicOperator from, BasicOperator to,
			Bindings bindings);

	/**
	 * This method is called whenever a triple is transmitted between two
	 * operators
	 */
	public void step(BasicOperator from, BasicOperator to, Triple triple);

	/**
	 * This method is called whenever a triple to be deleted is transmitted
	 * between two operators
	 */
	public void stepDelete(BasicOperator from, BasicOperator to, Triple triple);

	/**
	 * This method is called whenever an event for deleting all intermediate
	 * results is transmitted between two operators
	 */
	public void stepDeleteAll(BasicOperator from, BasicOperator to);

	/**
	 * This method is called whenever a message is transmitted between two
	 * operators
	 */
	public void stepMessage(BasicOperator from, BasicOperator to, Message msg);

	/**
	 * This method is called after the evaluation of the query has ended
	 */
	public void endOfEvaluation();
}