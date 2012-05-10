package lupos.gui.operatorgraph.visualeditor.queryeditor.util;

import java.util.HashSet;

import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Construct;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.ConstructTemplateContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveData;
import lupos.gui.operatorgraph.visualeditor.util.Connection;

public class QueryConnection extends Connection<Operator> {
	public QueryConnection(VisualEditor<Operator> visualEditor) {
		super(visualEditor);
	}

	protected String validateConnection() {
		String errorString = "";

		// we can't connect two operators on different layers...
		if(((Operator) this.firstOp.getElement()).getParentContainer() != ((Operator) this.secondOp.getElement()).getParentContainer()) {
			errorString = "You can only connect two operators in the same layer!";
		}

		if(this.secondOp.getElement() instanceof ConstructTemplateContainer && !(this.firstOp.getElement() instanceof Construct)) {
			errorString = "A ConstructTemplateContainer can only be a child of a CONSTRUCT operator!";
		}

		if(this.secondOp.getElement() instanceof RetrieveData) {
			errorString = "RetrieveData operators (ASK, CONSTRUCT, DESCRIBE, SELECT) can't have preceeding elements!";
		}

		if(((Operator) this.firstOp.getElement()).getParentContainer() == null) {
			this.firstOp.addSucceedingElement(new GraphWrapperIDTuple(this.secondOp, 0));

			if(this.hasCircle(this.firstOp, new HashSet<GraphWrapper>())) {
				errorString = "Circles are not allowed outside a Container!";
			}

			this.firstOp.removeSucceedingElement(this.secondOp);
		}

		return errorString;
	}

	private boolean hasCircle(GraphWrapper operator, HashSet<GraphWrapper> visited) {
		if(visited.contains(operator)) {
			return true;
		}

		visited.add(operator);

		for(GraphWrapperIDTuple opidt : operator.getSucceedingElements()) {
			if(this.hasCircle(opidt.getOperator(), visited) == true) {
				return true;
			}
		}

		return false;
	}
}