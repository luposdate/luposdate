package lupos.optimizations.logical.rules.generated.runtime;

import java.util.LinkedList;
import java.util.List;

import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.rdf.Prefix;

public class RulePackage extends AbstractRulePackage {
	public void applyRules(BasicOperator rootOp) {
		while(true) {
			boolean end = true;

			for(Rule rule : this.rules) {
				if(rule.apply(rootOp)) {
					end = false;

					break;
				}
			}

			if(end) {
				break;
			}
		}
	}

	public List<DebugContainer<BasicOperatorByteArray>> applyRulesDebugByteArray(BasicOperator rootOp, Prefix prefixInstance) {
		List<DebugContainer<BasicOperatorByteArray>> debug = new LinkedList<DebugContainer<BasicOperatorByteArray>>();

		while(true) {
			boolean end = true;
			for(Rule rule : this.rules) {
				if(rule.apply(rootOp)) {
					debug.add(new DebugContainer<BasicOperatorByteArray>(rule.toString(), rule.getClass().getSimpleName().replace(" ", "").toLowerCase() + "Rule", BasicOperatorByteArray.getBasicOperatorByteArray(rootOp.deepClone(), prefixInstance)));

					end = false;

					break;
				}
			}
			if(end) {
				break;
			}
		}
		return debug;
	}
}