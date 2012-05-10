package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.RetrieveDataPanel;
import lupos.misc.util.OperatorIDTuple;

public class Construct extends RetrieveDataWithSolutionModifier {
	public Construct(Prefix prefix) {
		super(prefix);
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new RetrieveDataPanel(gw, this, parent, "Construct");
		((RetrieveDataPanel) this.panel).addDatasetClause();
		((RetrieveDataPanel) this.panel).addSolutionModifier();
		((RetrieveDataPanel) this.panel).finalize();

		return this.panel;
	}

	public StringBuffer serializeOperator() {
		StringBuffer ret = super.serializeOperator();
		ret.append(this.serializeDatasetClause());
		ret.append(this.serializeSolutionModifier());

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = super.serializeOperator();
		ret.append(this.serializeDatasetClauseAndWhereClause(visited));
		ret.append(this.serializeSolutionModifier());

		return ret;
	}

	public StringBuffer serializeDatasetClauseAndWhereClause(HashSet<Operator> visited) {
		StringBuffer ret = this.serializeDatasetClause();

		ret.append("{\n");

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			Operator op = opIDT.getOperator();

			if(op instanceof ConstructTemplateContainer) {
				ret.append(op.serializeOperatorAndTree(visited));
			}
		}

		ret.append("}\n");

		// WHERE
		ret.append("WHERE {\n");

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			Operator op = opIDT.getOperator();

			if(!(op instanceof ConstructTemplateContainer)) {
				ret.append(opIDT.getOperator().serializeOperatorAndTree(visited));
			}
		}

		ret.append("}\n");

		return ret;
	}

	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this))
			return false;

		visited.add(this);


		if(super.variableInUse(variable, visited))
			return true;

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getOperator().variableInUse(variable, visited))
				return true;

		return false;
	}

	public boolean canAddSucceedingOperator() {
		return (this.succeedingOperators.size() < 2);
	}
}