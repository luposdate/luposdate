package lupos.rif.visitor;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.RIFException;
import lupos.rif.SimpleRuleVisitor;
import lupos.rif.model.Constant;
import lupos.rif.model.Document;
import lupos.rif.model.Equality;
import lupos.rif.model.Rule;
import lupos.rif.model.RulePredicate;
import lupos.rif.model.RuleVariable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class RuleDependencyGraphVisitor extends SimpleRuleVisitor {
	private final Multimap<String, Rule> predicateMap = HashMultimap.create();
	private Rule currentRule = null;

	@Override
	public IRuleNode visit(Document obj, IRuleNode arg) throws RIFException {
		predicateMap.clear();
		for (final Rule rule : obj.getRules())
		{
			rule.getRecursiveConnections().clear();
			if (rule.getHead() instanceof RulePredicate) {
				final IExpression expr = (IExpression) ((RulePredicate) rule
						.getHead()).termName;
				if (expr instanceof Constant)
					predicateMap.put(expr.toString(), rule);
				else if (expr instanceof RuleVariable)
					predicateMap.put("?", rule);
			} else if (rule.getHead() instanceof Equality)
				predicateMap.put("=", rule);
		}
		return super.visit(obj, arg);
	}

	@Override
	public IRuleNode visit(Rule obj, IRuleNode arg) throws RIFException {
		currentRule = obj;
		if (obj.getBody() != null)
			return obj.getBody().accept(this, arg);
		else
			return null;
	}

	@Override
	public IRuleNode visit(RulePredicate obj, IRuleNode arg)
			throws RIFException {
		obj.setRecursive(false);
		final IExpression expr = (IExpression) obj.termName;
		if (expr instanceof Constant)
			for (final Rule rule : predicateMap.get(expr.toString())) {
				obj.setRecursive(true);
				rule.getRecursiveConnections().add(currentRule);
			}
		else if (expr instanceof RuleVariable)
			for (final Rule rule : predicateMap.get("?")) {
				obj.setRecursive(true);
				rule.getRecursiveConnections().add(currentRule);
			}
		return null;
	}

	@Override
	public IRuleNode visit(Equality obj, IRuleNode arg) throws RIFException {
		for (final Rule rule : predicateMap.get("="))
			rule.getRecursiveConnections().add(currentRule);
		return null;
	}

}
