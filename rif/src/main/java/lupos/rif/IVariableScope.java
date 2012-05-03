package lupos.rif;

import java.util.Set;

import lupos.rif.model.RuleVariable;

public interface IVariableScope extends IRuleNode {

	Set<RuleVariable> getDeclaredVariables();

	void addVariable(RuleVariable var);

}
