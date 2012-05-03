package lupos.optimizations.logical.rules.rdfs;

import lupos.optimizations.logical.rules.Rule;
import lupos.optimizations.logical.rules.RuleEngine;
import lupos.optimizations.logical.rules.RulePushFilter;

public class RDFSRuleEngine1 extends RuleEngine {

	public RDFSRuleEngine1() {
		this.createRules();
	}

	@Override
	protected void createRules() {
		rules = new Rule[] { new RuleEliminateFilterUnequalAfter2XAdd(),new RuleEliminateInfinityLoop(),
				new RuleDeleteNotConnectedToResultOperator(),
				new RuleDeleteOperatorWithNoSuccs(), new RuleSplitGenerate(),
				new RuleEliminateUnnecessaryGenerate(),
				new RuleReplaceGenPat(), new RuleDeleteEmptyReplaceLit(),
				new RuleDeleteEmptyReplaceVar(),
				new RuleDeleteUseLessProjection(), new RulePushFilter(),
				new RuleReplaceVarUnderJoin(), new RuleMergeTwoProjection(),
				new RuleOptimizeReplaceByPat(),
				new RuleReplaceVarUnderReplaceLit(),
				new RuleReplaceVarUnderTriplePattern() };
	}
}
