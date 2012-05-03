package lupos.rif.visitor;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
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

public class ResolveListsRuleVisitor implements
		IRuleVisitor<IRuleNode, IRuleNode> {
	private String aliasString = "ALIASVAR_";
	private IVariableScope currentVariableScope = null;
	private int listCtr = 0;

	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		for (Rule rule : obj.getRules())
			if (rule.isImplication())
				rule.accept(this, obj);
		return obj;
	}

	public IRuleNode visit(Rule obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		currentVariableScope = obj;
		obj.setBody((IExpression) obj.getBody().accept(this, obj));
		currentVariableScope = null;
		return obj;
	}

	public IRuleNode visit(ExistExpression obj, IRuleNode arg)
			throws RIFException {
		obj.setParent(arg);
		IVariableScope temp = currentVariableScope;
		currentVariableScope = obj;
		obj.expr = (IExpression) obj.expr.accept(this, obj);
		currentVariableScope = temp;
		return obj;
	}

	public IRuleNode visit(Conjunction obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		List<IExpression> items = new ArrayList<IExpression>(obj.exprs);
		obj.exprs.clear();
		for (IExpression expr : items)
			obj.addExpr((IExpression) expr.accept(this, obj));
		return obj;
	}

	public IRuleNode visit(Disjunction obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		List<IExpression> items = new ArrayList<IExpression>(obj.exprs);
		obj.exprs.clear();
		for (IExpression expr : items)
			obj.addExpr((IExpression) expr.accept(this, obj));
		return obj;
	}

	public IRuleNode visit(RulePredicate obj, IRuleNode arg)
			throws RIFException {
		obj.setParent(arg);
		Conjunction conjunction = null;
		List<IExpression> items = new ArrayList<IExpression>(obj.termParams);
		obj.termParams.clear();
		for (IExpression expr : items) {
			final IRuleNode result = expr.accept(this, obj);
			if (result instanceof Conjunction) {
				conjunction = conjunction == null ? new Conjunction()
						: conjunction;
				for (IExpression item : ((Conjunction) result).exprs)
					if (item instanceof RulePredicate)
						conjunction.addExpr(item);
					else if (item instanceof RuleVariable)
						obj.termParams.add(item);
			} else
				obj.termParams.add((IExpression) result);
		}
		if (conjunction != null)
			conjunction.addExpr(obj);
		return conjunction == null ? obj : conjunction;
	}

	public IRuleNode visit(RuleList obj, IRuleNode arg) throws RIFException {
		try {
			//Wenn Liste leer
			if (obj.getItems().isEmpty()) {
				return new Constant(
						LiteralFactory
								.createURILiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>"),
						arg);
			}
			Conjunction conjunction = new Conjunction();
			conjunction.setParent(arg);
			int iteration = 0;
			//Listenidentifikator erstellen
			String baseName = aliasString + "list" + listCtr++ + "it";
			conjunction.addExpr(new RuleVariable(baseName + iteration));
			int ctr = obj.getItems().size();
			for (IExpression expr : obj.getItems()) {
				ctr--;
				IRuleNode result = expr.accept(this, conjunction);
				final String itVar = baseName + iteration++;
				if (result instanceof Conjunction) {
					for (IExpression item : new ArrayList<IExpression>(
							((Conjunction) result).exprs))
						if (item instanceof RulePredicate)
							conjunction.addExpr(item);
						else if (item instanceof RuleVariable)
							result = item;
				}
				final RulePredicate item = new RulePredicate(
						new RuleVariable(itVar),
						new Constant(
								LiteralFactory
										.createURILiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#first>"),
								arg), (IExpression) result);
				conjunction.addExpr(item);
				final RulePredicate next = new RulePredicate(
						new RuleVariable(itVar),
						new Constant(
								LiteralFactory
										.createURILiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#rest>"),
								arg),
						ctr == 0 ? new Constant(
								LiteralFactory
										.createURILiteral("<http://www.w3.org/1999/02/22-rdf-syntax-ns#nil>"),
								arg)
								: new RuleVariable(baseName + iteration));
				conjunction.addExpr(next);
				currentVariableScope.addVariable(new RuleVariable(itVar));
			}
			return conjunction;
		} catch (URISyntaxException e) {
			throw new RIFException(e.getMessage());
		}
	}

	public IRuleNode visit(RuleVariable obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		return obj;
	}

	public IRuleNode visit(Constant obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		return obj;
	}

	public IRuleNode visit(Equality obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		return obj;
	}

	public IRuleNode visit(External obj, IRuleNode arg) throws RIFException {
		obj.setParent(arg);
		return obj;
	}
}
