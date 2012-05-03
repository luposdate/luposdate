package lupos.rif;

import java.util.List;

public interface IRuleNode {
	IRuleNode getParent();

	void setParent(IRuleNode parent);

	List<IRuleNode> getChildren();
	
	String getLabel();

	<R, A> R accept(IRuleVisitor<R, A> visitor, A arg) throws RIFException;
}
