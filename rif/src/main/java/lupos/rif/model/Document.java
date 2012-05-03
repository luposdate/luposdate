package lupos.rif.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lupos.rif.IExpression;
import lupos.rif.IRuleNode;
import lupos.rif.IRuleVisitor;
import lupos.rif.RIFException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Document extends AbstractRuleNode {
	private String baseNamespace;
	private final Map<String, String> prefixMap = Maps.newHashMap();
	private final Collection<IExpression> facts = Lists.newArrayList();
	private final Collection<Rule> rules = Lists.newArrayList();
	private IExpression conclusion;

	@Override
	public List<IRuleNode> getChildren() {
		return new ArrayList<IRuleNode>(rules);
	}

	public void setBaseNamespace(String baseNamespace) {
		this.baseNamespace = baseNamespace;
	}

	public String getBaseNamespace() {
		return baseNamespace;
	}

	public Map<String, String> getPrefixMap() {
		return prefixMap;
	}

	public Collection<Rule> getRules() {
		return rules;
	}

	public void setConclusion(IExpression conclusion) {
		this.conclusion = conclusion;
	}

	public IExpression getConclusion() {
		return conclusion;
	}

	public Collection<IExpression> getFacts() {
		return facts;
	}

	public <R, A> R accept(IRuleVisitor<R, A> visitor, A arg)
			throws RIFException {
		return visitor.visit(this, arg);
	}

	public String getLabel() {
		StringBuilder str = new StringBuilder();
		str.append("Document").append("\n");
		if (baseNamespace != null)
			str.append("Base: ").append(baseNamespace).append("\n");
		for (Map.Entry<String, String> entry : prefixMap.entrySet())
			str.append("Prefix: ").append(entry.getKey()).append(" - ")
					.append(entry.getValue()).append("\n");
		return str.toString();
	}
}
