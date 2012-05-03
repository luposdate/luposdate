package lupos.rif.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;

import com.google.common.collect.Multimap;

/**
 * Represents a Variable, as used in a Rule. Internally a Lupos Variable is
 * used.
 * 
 * @author jenskluttig
 * 
 */
public class RuleVariable extends AbstractRuleNode implements IExpression {
	private Variable internalVariable;

	public RuleVariable(String name) {
		internalVariable = new Variable(name);
	}

	public String getName() {
		return internalVariable.getName();
	}

	public Variable getVariable() {
		return internalVariable;
	}

	public void setVariable(String name) {
		internalVariable = new Variable(name);
	}

	@Override
	public int hashCode() {
		return getVariable().hashCode();
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	public String getLabel() {
		return internalVariable.toString();
	}

	public boolean containsOnlyVariables() {
		return true;
	}

	public Set<RuleVariable> getVariables() {
		Set<RuleVariable> ret = new HashSet<RuleVariable>();
		ret.add(this);
		return ret;
	}

	public List<Uniterm> getPredicates() {
		return Arrays.asList();
	}

	public Object evaluate(Bindings binding) {
		Object ret = binding.get(internalVariable);
		if (ret == null)
			return getVariable();
		else
			return ret;
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		return false;
	}

	public boolean isPossibleAssignment() {
		return false;
	}

	public String toString(Prefix prefixInstance) {
		return toString();
	}

	public boolean equals(Object expr) {
		if (expr instanceof RuleVariable)
			return ((RuleVariable) expr).getVariable().equals(internalVariable);
		else
			return false;
	}

	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding);
	}

	public Object evaluate(Bindings binding, Object optionalResult, Multimap<IExpression, IExpression> equalities) {
		return evaluate(binding);
	}
}
