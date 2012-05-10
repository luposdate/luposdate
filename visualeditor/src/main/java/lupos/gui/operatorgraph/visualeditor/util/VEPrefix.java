package lupos.gui.operatorgraph.visualeditor.util;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.PrefixPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;

public class VEPrefix extends Prefix {
	public VEPrefix(boolean active) {
		super(active);
	}

	public VEPrefix(boolean active, Prefix prefixReference) {
		super(active, prefixReference);
	}

	@SuppressWarnings("unchecked")
	public AbstractSuperGuiComponent draw(GraphWrapper gw, OperatorGraph parent) {
		this.panel = new PrefixPanel(this, gw, (VisualGraph<Operator>) parent);

		return (AbstractSuperGuiComponent) this.panel;
	}
}
