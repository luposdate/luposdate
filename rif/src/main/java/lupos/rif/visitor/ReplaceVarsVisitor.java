package lupos.rif.visitor;

import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;
import lupos.rif.model.Conjunction;
import lupos.rif.model.Constant;
import lupos.rif.model.Disjunction;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.ExistExpression;
import lupos.rif.model.External;
import lupos.rif.model.Rule;
import lupos.rif.model.RuleList;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;

public class ReplaceVarsVisitor implements IRuleVisitor<IRuleNode, IRuleNode> {
	public Bindings bindings;

	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	public IRuleNode visit(Rule obj, IRuleNode arg) throws RIFException {
		throw new UnsupportedOperationException();
	}

	public IRuleNode visit(ExistExpression obj, IRuleNode arg)
	throws RIFException {
		throw new UnsupportedOperationException();
	}

	public IRuleNode visit(Conjunction obj, IRuleNode arg) throws RIFException {
		final Conjunction result = new Conjunction();
		result.setParent(arg);
		List<IExpression> exprs = new ArrayList<IExpression>(obj.exprs);
		for (IExpression expr : exprs)
			result.exprs.add((IExpression) expr.accept(this, result));
		return result;
	}

	public IRuleNode visit(Disjunction obj, IRuleNode arg) throws RIFException {
		final Disjunction result = new Disjunction();
		result.setParent(arg);
		List<IExpression> exprs = new ArrayList<IExpression>(obj.exprs);
		for (IExpression expr : exprs)
			result.exprs.add((IExpression) expr.accept(this, result));
		return result;
	}

	public IRuleNode visit(RulePredicate obj, IRuleNode arg) throws RIFException {
		final RulePredicate result = new RulePredicate(obj.isTriple());
		result.setParent(arg);
		result.termName = (IExpression) obj.termName.accept(this, result);
		List<IExpression> exprs = new ArrayList<IExpression>(obj.termParams);
		for (IExpression expr : exprs)
			result.termParams.add((IExpression) expr.accept(this, result));
		return result;
	}

	public IRuleNode visit(Equality obj, IRuleNode arg) throws RIFException {
		final Equality result = new Equality();
		result.leftExpr = (IExpression) obj.leftExpr.accept(this, result);
		result.rightExpr = (IExpression) obj.rightExpr.accept(this, result);
		return result;
	}

	public IRuleNode visit(External obj, IRuleNode arg) throws RIFException {
		final External result = new External();
		result.setParent(arg);
		result.termName = (IExpression) obj.termName.accept(this, result);
		List<IExpression> exprs = new ArrayList<IExpression>(obj.termParams);
		for (IExpression expr : exprs)
			result.termParams.add((IExpression) expr.accept(this, result));
		return result;
	}

	public IRuleNode visit(RuleList obj, IRuleNode arg) throws RIFException {
		final RuleList result = new RuleList();
		result.setParent(arg);
		result.isOpen = obj.isOpen;
		List<IExpression> exprs = new ArrayList<IExpression>(obj.getItems());
		for (IExpression expr : exprs)
			result.getItems().add((IExpression) expr.accept(this, result));
		return result;
	}

	public IRuleNode visit(RuleVariable obj, IRuleNode arg) throws RIFException {
		return new Constant(bindings.get(obj.getVariable()), arg);
	}

	public IRuleNode visit(Constant obj, IRuleNode arg) throws RIFException {
		return new Constant(obj.getLiteral(), arg);
	}
}
