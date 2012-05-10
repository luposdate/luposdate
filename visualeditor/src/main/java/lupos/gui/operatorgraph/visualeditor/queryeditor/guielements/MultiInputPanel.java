package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.MultiInputOperator;

public class MultiInputPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 1L;

	public MultiInputPanel(MultiInputOperator operator, GraphWrapper gw, VisualGraph<Operator> parent, String string) {
		this(operator, gw, parent, string, true);
	}

	public MultiInputPanel(MultiInputOperator operator, GraphWrapper gw, VisualGraph<Operator> parent, String string, boolean movable) {
		super(parent, gw, operator, movable);

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING);
		gbc.fill = GridBagConstraints.BOTH;

		JLabel textLabel = new JLabel(string);
		textLabel.setFont(parent.getFONT());

		this.add(textLabel, gbc);
	}

	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		return true;
	}
}