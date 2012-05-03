package lupos.optimizations.logical.rules.generated;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.optimizations.logical.rules.generated.runtime.RulePackage;
import lupos.optimizations.logical.rules.generated.runtime.RulePackageWithStartNodeMap;

public class AfterPhysicalOptimizationRulePackage extends RulePackage {
    public AfterPhysicalOptimizationRulePackage() {
        this.rules = new Rule[] {
		        new PushFilterRule()
        };
    }
}
