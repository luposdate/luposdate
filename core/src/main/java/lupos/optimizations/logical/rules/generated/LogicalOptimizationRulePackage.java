package lupos.optimizations.logical.rules.generated;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.optimizations.logical.rules.generated.runtime.RulePackage;
import lupos.optimizations.logical.rules.generated.runtime.RulePackageWithStartNodeMap;

public class LogicalOptimizationRulePackage extends RulePackage {
    public LogicalOptimizationRulePackage() {
        this.rules = new Rule[] {
		        new FactoroutANDinFilterRule(),
		        new PushFilterRule(),
		        new ConstantPropagationofFilterinIndexRule(),
		        new CombineUnionsRule(),
		        new VariablePropagationRule(),
		        new ProjectionOverSortRule(),
		        new SortLimitoverSortRule(),
		        new BinaryJoinRule(),

        };
    }
}
