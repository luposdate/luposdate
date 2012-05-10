package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.RetrieveDataPanel;
import lupos.misc.util.OperatorIDTuple;

public class Ask extends RetrieveData {
	public Ask(Prefix prefix) {
		super(prefix);
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new RetrieveDataPanel(gw, this, parent, "Ask");
		((RetrieveDataPanel) this.panel).addDatasetClause();
		((RetrieveDataPanel) this.panel).finalize();

		return this.panel;
	}

	public StringBuffer serializeOperator() {
		StringBuffer ret = super.serializeOperator();
		ret.append(this.serializeDatasetClause());

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = super.serializeOperator();
		ret.append(this.serializeDatasetClauseAndWhereClause(visited));

		return ret;
	}

	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this))
			return false;

		visited.add(this);


		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getOperator().variableInUse(variable, visited))
				return true;

		return false;
	}
}