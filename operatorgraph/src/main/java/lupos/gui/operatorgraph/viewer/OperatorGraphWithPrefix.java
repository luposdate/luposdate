package lupos.gui.operatorgraph.viewer;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefixNonEditable;
import lupos.gui.operatorgraph.prefix.Prefix;
import xpref.datatypes.BooleanDatatype;

public class OperatorGraphWithPrefix extends OperatorGraph {
	private static final long serialVersionUID = 902876317608720839L;

	private Prefix prefix = null;

	public OperatorGraphWithPrefix() {
		super();
		try {
			this.prefix = new ViewerPrefix(BooleanDatatype.getValues(
					"operatorGraph_usePrefixes").get(0).booleanValue());
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public OperatorGraphWithPrefix(final ViewerPrefix prefix) {
		super();
		try {
			this.prefix = prefix;
			this.prefix.setStatus(BooleanDatatype.getValues(
					"operatorGraph_usePrefixes").get(0).booleanValue());
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public void setPrefixStatus(final boolean status) {
		this.prefix.setStatus(status);
	}

	public Prefix getPrefix() {
		return this.prefix;
	}

	@Override
	public synchronized void arrange(final boolean flipX, final boolean flipY,
			final boolean rotate,
			final Arrange arrange) {
		final GraphWrapper prefixGW = new GraphWrapperPrefixNonEditable(this.prefix);

		if (this.boxes.containsKey(prefixGW)) {
			final GraphBox oldBox = this.boxes.remove(prefixGW);
			this.remove(oldBox.getElement());
		}

		super.arrange(flipX, flipY, rotate, arrange);

		if (this.prefix == null
				|| !this.prefix.isActive()
				|| (this.rootList.size() > 0 && !this.rootList.get(0)
						.usePrefixesActive())) {
			return;
		}

		final GraphBox prefixBox = new GraphBox(this, prefixGW);
		prefixBox.setX(2 * (int) Math.ceil(this.PADDING));
		prefixBox.setY(this.getMaxY()
				+ (int) Math.ceil(this.SPACING));
		prefixBox.arrange(flipX, flipY, rotate, arrange);

		this.boxes.put(prefixGW, prefixBox);

		this.updateSize();
	}
}