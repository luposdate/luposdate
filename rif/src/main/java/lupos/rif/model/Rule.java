package lupos.rif.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.IVariableScope;
import lupos.rif.RIFException;

public class Rule extends AbstractRuleNode implements IVariableScope {
	private final Set<Rule> recursiveConnections = Sets.newHashSet();
	private final Set<RuleVariable> vars = Sets.newHashSet();
	private IExpression head;
	private IExpression body;

	@Deprecated
	// nur ein prädikat im kopf
	public Set<IExpression> getHeadExpressions() {
		final Set<IExpression> result = new HashSet<IExpression>();
		if (head instanceof Conjunction)
			result.addAll(((Conjunction) head).exprs);
		else
			result.add(head);
		return result;
	}

	public Set<RuleVariable> getDeclaredVariables() {
		return vars;
	}

	public void addVariable(RuleVariable var) {
		var.setParent(this);
		vars.add(var);
	}

	public boolean isImplication() {
		return body != null;
	}

	public void setHead(IExpression head) {
		this.head = head;
	}

	public IExpression getHead() {
		return head;
	}

	public void setBody(IExpression body) {
		this.body = body;
	}

	public IExpression getBody() {
		return body;
	}

	public Set<Rule> getRecursiveConnections() {
		return recursiveConnections;
	}

	@Override
	public List<IRuleNode> getChildren() {
		List<IRuleNode> ret = new ArrayList<IRuleNode>();
		ret.addAll(vars);
		ret.add(head);
		if (isImplication())
			ret.add(body);
		return ret;
	}

	public boolean containsRecursion(IExpression conclusion, Set<Rule> visited) {
		if (conclusion instanceof RulePredicate
				&& getHead() instanceof RulePredicate) {
			final RulePredicate predConclusion = (RulePredicate) conclusion;
			final RulePredicate predHead = (RulePredicate) getHead();
			if (predConclusion.termParams.size() == predHead.termParams.size()) {
				boolean breaking = false;
				if (predConclusion.termName instanceof Constant
						&& predHead.termName instanceof Constant
						&& !predHead.termName.equals(predConclusion.termName))
					breaking = true;
				if (!breaking) {
					for (int i = 0; i < predConclusion.termParams.size(); i++)
						if (predConclusion.termParams.get(i) instanceof Constant
								&& predHead.termParams.get(i) instanceof Constant
								&& !predHead.termParams.get(i).equals(
										predConclusion.termParams.get(i)))
							breaking = true;
				}
				if (!breaking)
					return true;
			}
		} else if (conclusion instanceof Equality
				&& getHead() instanceof Equality)
			// Alle Regeln welche zur erzeugung einer Equality führen drinn
			// lassen
			return true;
		visited.add(this);
		for (final Rule rule : recursiveConnections)
			if (!visited.contains(rule)
					&& rule.containsRecursion(conclusion, visited))
				return true;
		return false;
	}

	@Override
	public String toString() {
		return getHead().toString();
	}

	public String getLabel() {
		return isImplication() ? "Rule" : "Fact";
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg)
			throws RIFException {
		return visitor.visit(this, arg);
	}
}
