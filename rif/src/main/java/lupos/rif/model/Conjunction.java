package lupos.rif.model;

import java.util.Collection;

import lupos.datastructures.bindings.Bindings;
import lupos.rif.IExpression;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
import lupos.rif.RIFException;
import lupos.rif.builtin.BooleanLiteral;

import com.google.common.collect.Multimap;

public class Conjunction extends AbstractExpressionContainer {
	public void addExpr(IExpression expr) {
		if (expr instanceof Conjunction)
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
		return "And";
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
				if (!((BooleanLiteral) result).value)
					return result;
			} else
				throw new RIFException(
				"In Conjunction, only Boolean Resulttypes are allowed!");
		}
		return BooleanLiteral.create(true);
	}

	public boolean isBound(RuleVariable var, Collection<RuleVariable> boundVars) {
		// mind. ein element darf kein scope sein und die variable muss gebunden
		// sein
		for (IExpression expr : exprs) {
			if (!(expr instanceof IVariableScope)
					&& expr.isBound(var, boundVars))
				return true;
		}
		return false;
	}

	public boolean isPossibleAssignment() {
		return false;
	}
}
