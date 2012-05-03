package lupos.rif.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.rdf.Prefix;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;

public abstract class AbstractExpressionContainer extends AbstractRuleNode
implements IExpression {

	public List<IExpression> exprs = new ArrayList<IExpression>();

	public AbstractExpressionContainer() {
		super();
	}

	public abstract void addExpr(IExpression expr);

	public boolean isEmpty() {
		return exprs.isEmpty();
	}

	public boolean containsOnlyVariables() {
		for (IExpression expr : exprs)
			if (!expr.containsOnlyVariables())
				return false;
		return true;
	}

	public Set<RuleVariable> getVariables() {
		Set<RuleVariable> variables = new HashSet<RuleVariable>();
		for (IExpression expr : exprs)
			variables.addAll(expr.getVariables());
		return variables;
	}

	public List<Uniterm> getPredicates() {
		List<Uniterm> terms = new ArrayList<Uniterm>();
		for (IExpression expr : exprs)
			terms.addAll(expr.getPredicates());
		return terms;
	}

	public List<IRuleNode> getChildren() {
		return new ArrayList<IRuleNode>(exprs);
	}
	
	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder(getLabel()).append("(");
		for(final IExpression expr : exprs)
			str.append(expr.toString()).append(" ");
		return str.append(")").toString();
	}
	
	@Override
	public String toString(final Prefix prefixInstance) {
		final StringBuilder str = new StringBuilder(getLabel()).append("(");
		for(final IExpression expr : exprs)
			str.append(expr.toString(prefixInstance)).append(" ");
		return str.append(")").toString();
	}
}