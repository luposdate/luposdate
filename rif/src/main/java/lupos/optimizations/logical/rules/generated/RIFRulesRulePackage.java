package lupos.optimizations.logical.rules.generated;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.optimizations.logical.rules.generated.runtime.RulePackage;
import lupos.optimizations.logical.rules.generated.runtime.RulePackageWithStartNodeMap;

public class RIFRulesRulePackage extends RulePackage {
    public RIFRulesRulePackage() {
        this.rules = new Rule[] {
		        new RemoveEmptyIndexRule(),
		        new RemoveUnnecessaryConstructRule(),
		        new RemoveUnnecessaryConstructPredicateRule(),
		        new ConstructToGenerateRule(),
		        new SplitGenerateRule(),
		        new SplitPredicatePatternRule(),
		        new SplitConstructPredicateRule(),
		        new ReplaceGeneratePatRule(),
		        new GeneratePatConstructPredicateRule(),
		        new RemoveUnionRule(),
		        new RemoveDistinctRule(),
		        new FactoroutANDinFilterRule(),
		        new ReplaceFilterWithRuleFilterRule(),
		        new ReplaceVarUnderIndexRule(),
		        new MergeMemoryIndexesRule(),
		        new PushRuleFilterRule()
        };
    }
}
