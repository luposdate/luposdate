package lupos.gui.operatorgraph.visualeditor.queryeditor.operators;

import java.util.HashSet;
import java.util.LinkedList;

import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.RetrieveDataPanel;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.util.OperatorIDTuple;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.SimpleNode;

public abstract class RetrieveDataWithProjectionAndSolutionModifier extends RetrieveDataWithSolutionModifier {
	protected LinkedList<String> projectionElements = new LinkedList<String>();

	protected RetrieveDataWithProjectionAndSolutionModifier(Prefix prefix) {
		super(prefix);
	}


	protected abstract SimpleNode parseProjectionElement(String projectionElement) throws Throwable;

	public void addProjectionElement(String projectionElement) throws ModificationException {
		try {
			SimpleNode node = this.parseProjectionElement(projectionElement);

			if(node instanceof ASTVar) {
				this.projectionElements.add(((ASTVar) node).toQueryString());
			}
			else if(node instanceof ASTQuotedURIRef) {
				this.projectionElements.add(((ASTQuotedURIRef) node).toQueryString());
			}
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	public void setProjectionElement(int index, String projectionElement) throws ModificationException {
		try {
			// new element...
			if(this.projectionElements.size() == index) {
				this.projectionElements.add("");
			}

			// parse new value
			SimpleNode node = this.parseProjectionElement(projectionElement);

			// remove old value...
			if(this.projectionElements.get(index) != null) {
				this.projectionElements.remove(index);
			}

			// add new value...
			if(node instanceof ASTVar) {
				this.projectionElements.add(index, ((ASTVar) node).toQueryString());
			}
			else if(node instanceof ASTQuotedURIRef) {
				this.projectionElements.add(index, ((ASTQuotedURIRef) node).toQueryString());
			}
		}
		catch(Throwable t) {
			this.handleParseError(t);
		}
	}

	public void removeProjectionElement(int index) {
		if(index == this.projectionElements.size()) {
			return;
		}

		this.projectionElements.remove(index);
	}

	public LinkedList<String> getProjectionElements() {
		return this.projectionElements;
	}

	public void clearProjectionList() {
		this.projectionElements = new LinkedList<String>();
	}


	public StringBuffer serializeProjections() {
		StringBuffer ret = new StringBuffer();

		if(this.projectionElements.size() == 0 || !((RetrieveDataPanel) this.panel).getElementStatus("projection")) {
			ret.append("*");
		}
		else {
			for(String projectionElement : this.projectionElements) {
				ret.append(projectionElement + " ");
			}
		}

		ret.append("\n");

		return ret;
	}


	public boolean variableInUse(String variable, HashSet<Operator> visited) {
		if(visited.contains(this)) {
			return false;
		}

		visited.add(this);


		if(super.variableInUse(variable, visited)) {
			return true;
		}

		for(String projElement : this.projectionElements) {
			if(projElement.equalsIgnoreCase(variable)) {
				return true;
			}
		}

		for(OperatorIDTuple<Operator> opIDT : this.succeedingOperators) {
			if(opIDT.getOperator().variableInUse(variable, visited)) {
				return true;
			}
		}

		return false;
	}
}