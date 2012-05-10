package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.JComboBox;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboItem;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboListener;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboRenderer;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;

public class SuggestionPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 1L;
	private final LinkedList<ComboItem> elements;
	private final JComboBox jCoBo;

	public SuggestionPanel(final VisualGraph<Operator> parent,
			final Operator op, final LinkedList<ComboItem> elements,
			final SuggestionRowPanel rowPanel) {
		super(parent, new GraphWrapperOperator(op), op, false);

		this.elements = elements;

		for (final MouseListener ml : this.getMouseListeners())
			this.removeMouseListener(ml);

		this.jCoBo = new JComboBox(elements.toArray());
		this.jCoBo.setRenderer(new ComboRenderer());
		this.jCoBo.addActionListener(new ComboListener(this.jCoBo, rowPanel));

		this.add(this.jCoBo);

		this.setPreferredSize(new Dimension(this.jCoBo.getPreferredSize().width
				+ (int) (2 * parent.PADDING),
				this.jCoBo.getPreferredSize().height
				+ (int) (2 * parent.PADDING)));
	}

	public SuggestionPanel(final int PADDING, final Operator op,
			final LinkedList<ComboItem> elements,
			final SuggestionRowPanel rowPanel) {
		super(null, new GraphWrapperOperator(op), op, false);

		this.elements = elements;

		for (final MouseListener ml : this.getMouseListeners())
			this.removeMouseListener(ml);

		this.jCoBo = new JComboBox(elements.toArray());
		this.jCoBo.setRenderer(new ComboRenderer());
		this.jCoBo.addActionListener(new ComboListener(this.jCoBo, rowPanel));

		this.add(this.jCoBo);

		this.setPreferredSize(new Dimension(this.jCoBo.getPreferredSize().width
				+ 2 * PADDING, this.jCoBo.getPreferredSize().height + 2
				* PADDING));
	}

	public LinkedList<ComboItem> getElements() {
		return this.elements;
	}

	public Item getSelectedElement() {
		return (Item) ((ComboItem) this.jCoBo.getSelectedItem()).getObject();
	}

	public boolean validateOperatorPanel(final boolean showErrors, Object data) {
		return true;
	}
}