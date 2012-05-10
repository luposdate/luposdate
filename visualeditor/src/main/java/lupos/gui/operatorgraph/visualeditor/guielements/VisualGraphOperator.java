package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.Dimension;
import java.util.HashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;

public abstract class VisualGraphOperator extends VisualGraph<Operator> {
	private static final long serialVersionUID = 7449846681888858372L;

	public VisualGraphOperator(VisualEditor<Operator> visualEditor) {
		super(visualEditor);
	}

	public GraphWrapperEditable createGraphWrapper(Operator op) {
		return new GraphWrapperOperator(op);
	}

	public Operator createDummyOperator() {
		return new DummyOperator();
	}

	public void clear() {
		super.clearAll();
	}

	private class DummyOperator extends Operator {
		public AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent) {
			this.panel = new AnnotationPanel<Operator>(parent, gw, this, null);
			this.panel.setPreferredSize(new Dimension(0, 0));

			return this.panel;
		}

		public StringBuffer serializeOperator() {
			return new StringBuffer();
		}

		public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
			return new StringBuffer();
		}

		public boolean variableInUse(String variable, HashSet<Operator> visited) {
			return false;
		}

		public void prefixAdded() {}
		public void prefixModified(String oldPrefix, String newPrefix) {}
		public void prefixRemoved(String prefix, String namespace) {}
	}
}