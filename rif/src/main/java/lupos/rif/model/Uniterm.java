package lupos.rif.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lupos.rdf.Prefix;
import lupos.rif.IExpression;

public abstract class Uniterm extends AbstractRuleNode implements IExpression {
	public IExpression termName;
	public List<IExpression> termParams = new ArrayList<IExpression>();

	public Uniterm() {
		super();
	}

	public boolean containsOnlyVariables() {
		return false;
	}

	public Set<RuleVariable> getVariables() {
		Set<RuleVariable> vars = new HashSet<RuleVariable>();
		vars.addAll(termName.getVariables());
		for (IExpression expr : termParams)
			vars.addAll(expr.getVariables());
		return vars;
	}

	public List<Uniterm> getPredicates() {
		return Arrays.asList(this);
	}

	public String getLabel() {
		final StringBuffer str = new StringBuffer();
		str.append(termName.toString()).append("(");
		for (int idx = 0; idx < termParams.size(); idx++) {
			str.append(termParams.get(idx).toString());
			if (idx < termParams.size() - 1)
				str.append(", ");
			else
				str.append(")");
		}
		if (str.substring(str.length()) != ")")
			str.append(")");
		return str.toString();
	}

	public String toString(Prefix prefixInstance) {
		final StringBuffer str = new StringBuffer();
		str.append(termName.toString(prefixInstance)).append("(");
		for (int idx = 0; idx < termParams.size(); idx++) {
			str.append(termParams.get(idx).toString(prefixInstance));
			if (idx < termParams.size() - 1)
				str.append(", ");
			else
				str.append(")");
		}
		if (str.substring(str.length()) != ")")
			str.append(")");
		return str.toString();
	}

	public abstract boolean equalsDataStructure(Object obj);
}
