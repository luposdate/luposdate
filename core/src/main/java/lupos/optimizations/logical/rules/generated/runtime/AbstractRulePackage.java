package lupos.optimizations.logical.rules.generated.runtime;

import java.util.List;

import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.rdf.Prefix;

public abstract class AbstractRulePackage {
	protected Rule[] rules;

	public abstract void applyRules(BasicOperator rootOp);
	public abstract List<DebugContainer<BasicOperatorByteArray>> applyRulesDebugByteArray(BasicOperator rootOp, Prefix prefixInstance);
}