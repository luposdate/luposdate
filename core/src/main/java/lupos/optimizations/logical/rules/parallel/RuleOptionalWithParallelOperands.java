package lupos.optimizations.logical.rules.parallel;

import java.util.HashMap;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.multiinput.optional.Optional;

public class RuleOptionalWithParallelOperands extends
		RuleJoinWithParallelOperands {
	@Override
	protected void init() {
		final Operator a = new Optional();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(a, "join");

		startNode = a;
	}

	@Override
	public String getName() {
		return "OptionalWithParallelOperands";
	}
}
