package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.Dimension;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

public class AnnotationPanel<T> extends AbstractGuiComponent<T> {
	private static final long serialVersionUID = 1L;

	public AnnotationPanel(VisualGraph<T> parent, GraphWrapper gw, T operator, T child) {
		super(parent, gw, operator, false);

		this.parentOp = operator;
		this.child = child;

		int dimension = Double.valueOf(10 * parent.getZoomFactor()).intValue();

		this.setPreferredSize(new Dimension(dimension, dimension));
	}

	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		return true;
	}
}