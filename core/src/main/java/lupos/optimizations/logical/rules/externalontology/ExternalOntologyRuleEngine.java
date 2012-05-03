package lupos.optimizations.logical.rules.externalontology;

import lupos.optimizations.logical.rules.Rule;
import lupos.optimizations.logical.rules.RuleEngine;
import lupos.optimizations.logical.rules.RuleMakeBinaryJoin;
import lupos.optimizations.logical.rules.RulePushFilter;
import lupos.optimizations.logical.rules.RuleReplaceConstantOfFilterInTriplePattern;
import lupos.optimizations.logical.rules.rdfs.RuleDeleteNotConnectedToResultOperator;
import lupos.optimizations.logical.rules.rdfs.RuleEliminateFilterUnequalAfter2XAdd;

public class ExternalOntologyRuleEngine extends RuleEngine {

	public ExternalOntologyRuleEngine() {
		createRules();
	}

	@Override
	protected void createRules() {
		// rules.add(new RuleFindAllGenerates());
		// separate the following rules from the rest because this rule would
		// add again
		// infinity loops which are previously eliminated by the rule
		// RuleEliminateInfinityLoop
		rules = new Rule[] { new RuleFactorOutUnionInJoin(),
				new RulePushFilter(),
				new RuleEliminateFilterUnequalAfter2XAdd(),
				new RuleMakeBinaryJoin(), new RuleFactorOutUnionInGenerate(),
				new RuleConstantPropagationOverJoin(),
				new RuleDeleteTriggerOneTimeJoin(),
				new RuleReplaceConstantOfFilterInTriplePattern(),
				new RuleEliminateUnsatisfiableAddFilterSequence(),
				new RuleConstantPropagationFromAddToGenerate(),
				new RuleEliminateUnsatisfiableFilterAfterAdd(),
				new RuleDeleteNotConnectedToResultOperator() };
	}

}
