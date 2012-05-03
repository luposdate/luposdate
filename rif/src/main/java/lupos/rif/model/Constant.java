package lupos.rif.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;
import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;

import com.google.common.collect.Multimap;

public class Constant extends AbstractRuleNode implements IExpression {
	private Literal value;

	public Constant(Literal value, IRuleNode parent) {
		super(parent);
		this.value = value;
	}

	public Literal getLiteral() {
		return value;
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	public String getLabel() {
		return value.toString();
	}

	public boolean containsOnlyVariables() {
		return false;
	}

	public Set<RuleVariable> getVariables() {
		return new HashSet<RuleVariable>();
	}

	public List<Uniterm> getPredicates() {
		return Arrays.asList();
	}

	public Object evaluate(Bindings binding) {
		return evaluate(binding, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding, optionalResult, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult, Multimap<IExpression, IExpression> equalities) {
		return value;
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		return false;
	}

	public boolean isPossibleAssignment() {
		return false;
	}

	public String toString(Prefix prefixInstance) {
		return value.toString(prefixInstance);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Constant)
			return ((Constant) obj).getLiteral().equals(getLiteral());
		else
			return false;
	}

	@Override
	public int hashCode() {
		return getLiteral().hashCode();
	}
}
