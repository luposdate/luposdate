package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.awt.Color;
import java.util.LinkedHashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.QueryGraph;

public class TripleContainer extends OperatorContainer {
	public TripleContainer() { // needed for insertOperator()...
		super();
	}

	public TripleContainer(LinkedHashSet<Operator> ops) {
		super(ops);
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		return this.drawPanel(gw, (QueryGraph) parent, Color.GREEN);
	}
}