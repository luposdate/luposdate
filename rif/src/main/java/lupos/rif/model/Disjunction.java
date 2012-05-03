package lupos.rif.model;

import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.builtin.BooleanLiteral;

import com.google.common.collect.Multimap;

public class Disjunction extends AbstractExpressionContainer {
	public void addExpr(IExpression expr) {
		if (expr instanceof Disjunction)
			for (IExpression obj : ((AbstractExpressionContainer) expr).exprs)
				addExpr(obj);
		else if (expr != null) {
			if (!exprs.contains(expr)) {
				expr.setParent(this);
				exprs.add(expr);
			}
		}
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException {
		return visitor.visit(this, arg);
	}

	public String getLabel() {
		return "Or";
	}

	public Object evaluate(Bindings binding) {
		return evaluate(binding, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult) {
		return evaluate(binding, optionalResult, null);
	}

	public Object evaluate(Bindings binding, Object optionalResult, Multimap<IExpression, IExpression> equalities) {
		for (IExpression expr : exprs) {
			Object result = expr.evaluate(binding);
			if (result instanceof BooleanLiteral) {
				if (((BooleanLiteral) result).value)
					return result;
			} else
				throw new RIFException(
				"In Disjunction, only Boolean Resulttypes are allowed!");
		}
		return BooleanLiteral.create(false);
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		// In jedem Element muss die Variable gebunden sein
		for (IExpression expr : exprs) {
			if (!expr.isBound(var, boundVars))
				return false;
		}
		return true;
	}

	public boolean isPossibleAssignment() {
		return false;
	}
}
