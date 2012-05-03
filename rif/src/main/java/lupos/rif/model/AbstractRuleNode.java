package lupos.rif.model;

import java.util.Arrays;
import java.util.List;

import lupos.rif.IRuleNode;

public abstract class AbstractRuleNode implements IRuleNode {
	protected IRuleNode parent;

	public AbstractRuleNode() {
	}

	public AbstractRuleNode(IRuleNode parent) {
		this();
		setParent(parent);
	}

	public IRuleNode getParent() {
		return parent;
	}

	public void setParent(IRuleNode parent) {
		this.parent = parent;
	}

	public List<IRuleNode> getChildren() {
		return Arrays.asList();
	}

	@Override
	public String toString() {
		return getLabel();
	}
}
