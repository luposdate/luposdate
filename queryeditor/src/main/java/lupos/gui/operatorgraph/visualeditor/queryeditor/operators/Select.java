package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.RetrieveDataPanel;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class Select extends RetrieveDataWithProjectionAndSolutionModifier {
	public DistinctState distinctState = DistinctState.NO_DISTINCT;

	public enum DistinctState {
		DISTINCT, REDUCED, NO_DISTINCT;
	}


	public Select(Prefix prefix) {
		super(prefix);
	}


	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new RetrieveDataPanel(gw, this, parent, "Select");
		((RetrieveDataPanel) this.panel).addProjections(true);
		((RetrieveDataPanel) this.panel).addDatasetClause();
		((RetrieveDataPanel) this.panel).addSolutionModifier();
		((RetrieveDataPanel) this.panel).finalize();

		return this.panel;
	}


	protected SimpleNode parseProjectionElement(String projectionElement) throws Throwable {
		return SPARQL1_1Parser.parseVar(projectionElement);
	}


	public StringBuffer serializeOperator() {
		StringBuffer ret = super.serializeOperator();

		if(this.distinctState != DistinctState.NO_DISTINCT)
			ret.append(this.distinctState.toString() + " ");

		ret.append(this.serializeProjections());
		ret.append(this.serializeDatasetClause());
		ret.append(this.serializeSolutionModifier());

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = super.serializeOperator();

		if(this.distinctState != DistinctState.NO_DISTINCT)
			ret.append(this.distinctState.toString() + " ");

		ret.append(this.serializeProjections());
		ret.append(this.serializeDatasetClauseAndWhereClause(visited));
		ret.append(this.serializeSolutionModifier());

		return ret;
	}
}