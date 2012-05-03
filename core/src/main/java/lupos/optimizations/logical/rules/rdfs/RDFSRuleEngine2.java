package lupos.optimizations.logical.rules.rdfs;

import lupos.optimizations.logical.rules.Rule;
import lupos.optimizations.logical.rules.RuleEngine;

public class RDFSRuleEngine2 extends RuleEngine {

	public RDFSRuleEngine2() {
		this.createRules();
	}

	@Override
	protected void createRules() {
		rules = new Rule[] { new RuleInsertGenerateAddEnv(),
				new RuleGenerateAddOverJoin(),
				new RuleGenerateAddOverOptional(),
				new RuleGenerateAddOverUnion(),
				new RuleGenerateAddOverFilter(),
				new RuleGenerateAddOverProjection(),
				new RuleDeleteEmptyGenerateAdd(), new RuleReplaceLitOverJoin(),
				new RuleReplaceLitOverOptional(),
				new RuleReplaceLitOverUnion(), new RuleReplaceLitOverFilter(),
				new RuleReplaceLitOverProjection(),
				new RuleSplitJoinOperandsWithSameId() };
	}

}
