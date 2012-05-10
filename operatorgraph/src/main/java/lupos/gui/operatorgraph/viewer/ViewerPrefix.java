package lupos.gui.operatorgraph.viewer;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;

public class ViewerPrefix extends Prefix {
	public ViewerPrefix(boolean active) {
		super(active);
	}

	public ViewerPrefix(boolean active, Prefix prefixReference) {
		super(active, prefixReference);
	}

	public AbstractSuperGuiComponent draw(GraphWrapper gw, OperatorGraph parent) {
		return new ElementPanel(parent, gw);
	}
}