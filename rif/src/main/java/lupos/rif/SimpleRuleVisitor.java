package lupos.rif;

import java.util.ArrayList;

import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RuleVariable;
import lupos.rif.model.RulePredicate;

public abstract class SimpleRuleVisitor implements
		IRuleVisitor<IRuleNode, IRuleNode> {

	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		for (Rule rule : obj.getRules())
			rule.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Rule obj, IRuleNode arg) throws RIFException {
		obj.getHead().accept(this, obj);
		obj.getBody().accept(this, obj);
		return obj;
	}

	public IRuleNode visit(ExistExpression obj, IRuleNode arg)
			throws RIFException {
		obj.expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Conjunction obj, IRuleNode arg) throws RIFException {
		for (IExpression expr : new ArrayList<IExpression>(obj.exprs))
			expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Disjunction obj, IRuleNode arg) throws RIFException {
		for (IExpression expr : new ArrayList<IExpression>(obj.exprs))
			expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(RulePredicate obj, IRuleNode arg)
			throws RIFException {
		obj.termName.accept(this, obj);
		for (IExpression params : obj.termParams)
			params.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Equality obj, IRuleNode arg) throws RIFException {
		obj.leftExpr.accept(this, obj);
		obj.rightExpr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(External obj, IRuleNode arg) throws RIFException {
		obj.termName.accept(this, obj);
		for (IExpression params : obj.termParams)
			params.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(RuleList obj, IRuleNode arg) throws RIFException {
		for (IExpression expr : obj.getItems())
			expr.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(RuleVariable obj, IRuleNode arg) throws RIFException {
		return obj;
	}

	public IRuleNode visit(Constant obj, IRuleNode arg) throws RIFException {
		return obj;
	}
}
