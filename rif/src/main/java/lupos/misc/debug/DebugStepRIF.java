package lupos.misc.debug;

import lupos.engine.operators.BasicOperator;
import lupos.rif.datatypes.RuleResult;

public interface DebugStepRIF extends DebugStep {
	/**
	 * This method is called whenever an intermediate result with predicates is
	 * transmitted between two operators
	 */
	public void step(BasicOperator from, BasicOperator to, RuleResult rr);

	/**
	 * This method is called whenever an intermediate result with predicates to
	 * be deleted is transmitted between two operators
	 */
	public void stepDelete(BasicOperator from, BasicOperator to, RuleResult rr);

}
