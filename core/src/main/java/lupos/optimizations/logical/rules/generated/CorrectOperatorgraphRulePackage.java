package lupos.optimizations.logical.rules.generated;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.optimizations.logical.rules.generated.runtime.RulePackage;
import lupos.optimizations.logical.rules.generated.runtime.RulePackageWithStartNodeMap;

public class CorrectOperatorgraphRulePackage extends RulePackage {
    public CorrectOperatorgraphRulePackage() {
        this.rules = new Rule[] {
		        new BoundVariableinOptionalRule(),
		        new BoundVariableinUnionRule()
        };
    }
}
