package lupos.gui.operatorgraph;

import java.io.Serializable;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

public class GraphWrapperIDTuple implements Serializable {
	private static final long serialVersionUID = 1290984783247779070L;
	private GraphWrapper op;
	private int id;

	public GraphWrapperIDTuple(GraphWrapper op, int id) {
		this.op = op;
		this.id = id;
	}

	public String toString() {
		return id + ": " + op;
	}

	public GraphWrapper getOperator() {
		return this.op;
	}

	public int getId() {
		return this.id;
	}
}