package lupos.gui.operatorgraph.visualeditor.dataeditor.guielements;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefix;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefixNonEditable;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;

public class DataGraph extends VisualGraphOperatorWithPrefix {
	private static final long serialVersionUID = 69926446707446031L;

	public DataGraph(final VisualEditor<Operator> visualEditor, final Prefix prefix) {
		super(visualEditor, prefix);

		System.out.println(">" + (this.prefix == null));
	}

	@Override
	protected void handleAddOperator(final Operator newOp) {}

	@Override
	protected boolean validateAddOperator(final int x, final int y, final String newClassName) {
		return true;
	}

	@Override
	public String serializeGraph() {
		final String object = super.serializeSuperGraph();

		final StringBuffer ret = this.prefix.getPrefixString("@prefix ", " .");
		ret.append(object);

		return ret.toString();
	}

	@Override
	public synchronized void arrange(final boolean flipX, final boolean flipY,
			final boolean rotate,
			final Arrange arrange) {
		final GraphWrapper prefixGW = new GraphWrapperPrefix(this.prefix);

		if(this.boxes.containsKey(prefixGW)) {
			final GraphBox oldBox = this.boxes.remove(prefixGW);
			this.remove(oldBox.getElement());
		}

		super.arrange(flipX, flipY, rotate, arrange);

		if(this.prefix == null || !this.prefix.isActive() || (this.rootList.size() == 1 && this.rootList.get(0) instanceof GraphWrapperPrefixNonEditable)) {
			return;
		}

		final GraphBox prefixBox = new GraphBox(this, prefixGW);
		prefixBox.setX(2* (int) Math.ceil(this.PADDING));
		prefixBox.setY(this.getMaxY() + (int) Math.ceil(this.SPACING));
		prefixBox.arrange(flipX, flipY, rotate, arrange);

		this.boxes.put(prefixGW, prefixBox);

		this.updateSize();
	}

	@Override
	public VisualGraphOperatorWithPrefix newInstance(
			VisualEditor<Operator> visualEditor, Prefix prefix) {
		return new DataGraph(visualEditor, prefix);
	}
}
