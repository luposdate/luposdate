package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.OperatorPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.JTFOperator;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.DummyItem;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class Graph extends JTFOperator {
	private Item graphItem;

	public Graph(Prefix prefix) {
		super(prefix);

		this.graphItem = new DummyItem();
	}

	public Graph(Prefix prefix, Item graphItem) {
		super(prefix);

		this.graphItem = graphItem;
	}

	public String toString() {
		return this.graphItem.toString();
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new OperatorPanel(this, gw, parent, this.prefix.add(this.toString()), "Graph");

		return this.panel;
	}

	public void applyChange(String value) throws ModificationException {
		try {
			SimpleNode node = SPARQL1_1Parser.parseVarOrBlankNodeOrIRIref(value, this.prefix.getPrefixNames());

			this.graphItem = this.getItem(node);
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	public StringBuffer serializeOperator() {
		StringBuffer ret = new StringBuffer();
		ret.append("GRAPH " + this.graphItem.toString() + "{\n");

		if(this.succeedingOperators.size() > 0)
			ret.append(this.succeedingOperators.get(0).getOperator().serializeOperator());

		ret.append("}");

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = new StringBuffer();
		ret.append("GRAPH " + this.graphItem.toString() + "{\n");

		if(this.succeedingOperators.size() > 0)
			ret.append(this.succeedingOperators.get(0).getOperator().serializeOperatorAndTree(visited));

		ret.append("}");

		return ret;
	}

	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this))
			return false;

		visited.add(this);

		if(this.graphItem instanceof Variable && this.graphItem.toString().equalsIgnoreCase(variable))
			return true;

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getOperator().variableInUse(variable, visited))
				return true;

		return false;
	}
}