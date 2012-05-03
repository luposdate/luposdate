package lupos.optimizations.logical.rules.generated;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.optimizations.logical.rules.generated.runtime.RulePackage;
import lupos.optimizations.logical.rules.generated.runtime.RulePackageWithStartNodeMap;

public class LogicalOptimizationForStreamEngineRulePackage extends RulePackage {
    public LogicalOptimizationForStreamEngineRulePackage() {
        this.rules = new Rule[] {
		        new FactoroutANDinFilterRule(),
		        new PushFilterRule(),
		        new BinaryJoinRule(),
		        new ConstantPropagationofFilterinTriplePatternRule()
        };
    }
}
