package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.OperatorPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.JTFOperator;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class Filter extends JTFOperator {
	private String filterExpression = "";

	public Filter(Prefix prefix) {
		super(prefix);
	}

	public Filter(Prefix prefix, String filterExpression) {
		super(prefix);

		this.filterExpression = filterExpression;
	}

	public String toString() {
		return this.filterExpression;
	}

	public void applyChange(String value) throws ModificationException {
		try {
			SPARQL1_1Parser.parseFilter( value, prefix.getPrefixNames());

			this.filterExpression = value;
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
		this.panel = new OperatorPanel(this, gw, parent, this.prefix.add(this.toString()), "Filter");

		return this.panel;
	}

	public StringBuffer serializeOperator() {
		StringBuffer ret = new StringBuffer();
		ret.append("FILTER(" + this.filterExpression + ") .\n");

		return ret;
	}

	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer ret = this.serializeOperator();

		for(OperatorIDTuple<Operator> opIDt : this.succeedingOperators)
			ret.append(opIDt.getOperator().serializeOperatorAndTree(visited));

		return ret;
	}

	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this))
			return false;

		visited.add(this);

		try {
			HashSet<Variable> variables = new HashSet<Variable>();

			SimpleNode node = SPARQL1_1Parser.parseFilter("FILTER(" + this.filterExpression + ")", this.prefix.getPrefixNames());

			Operator.computeUsedVariables(node, variables);

			if(variables.contains(new Variable(variable)))
				return true;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators)
			if(opIDT.getOperator().variableInUse(variable, visited))
				return true;

		return false;
	}
	
	@Override
	public String getXPrefID(){
		return "queryEditor_style_filter";
	}
}