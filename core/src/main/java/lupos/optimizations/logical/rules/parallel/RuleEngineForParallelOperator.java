package lupos.optimizations.logical.rules.parallel;

import lupos.optimizations.logical.rules.Rule;
import lupos.optimizations.logical.rules.RuleEngine;

public class RuleEngineForParallelOperator extends RuleEngine {

	public RuleEngineForParallelOperator() {
		createRules();
	}

	private static boolean lastJoin;

	@Override
	protected void createRules() {
		if (lastJoin) {
			rules = new Rule[] { new RuleJoinLastParallelOperands(),
					new RuleOptionalLastParallelOperands() };
		} else {
			rules = new Rule[] { new RuleJoinWithParallelOperands(),
					new RuleOptionalWithParallelOperands() };
		}
	}

	public static boolean isLastJoin() {
		return lastJoin;
	}

	public static void setLastJoin(final boolean lastJoin) {
		RuleEngineForParallelOperator.lastJoin = lastJoin;
	}

}
