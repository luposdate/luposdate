package lupos.optimizations.logical.rules.rdfs;

import java.util.LinkedList;

import lupos.engine.operators.singleinput.generate.Generate;
import lupos.optimizations.logical.rules.Rule;
import lupos.optimizations.logical.rules.RuleEngine;

public class RDFSRuleEngine0 extends RuleEngine {

	public static LinkedList<Generate> generates = null;
	private final boolean doNotConnectInferenceRules;

	public RDFSRuleEngine0(final boolean doNotConnectInferenceRules) {
		this.doNotConnectInferenceRules = doNotConnectInferenceRules;
		createRules();
		generates = new LinkedList<Generate>();
	}

	@Override
	protected void createRules() {
		// rules.add(new RuleFindAllGenerates());
		// separate the following rules from the rest because this rule would
		// add again
		// infinity loops which are previously eliminated by the rule
		// RuleEliminateInfinityLoop
		rules = new Rule[] { new RuleConnectGenPat(
				this.doNotConnectInferenceRules) };
	}

}
