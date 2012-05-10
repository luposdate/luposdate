package lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;

public class MultiElementsPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = -5247920987996477146L;
	private JComboBox comboBox = null;

	public MultiElementsPanel(CondensedRDFTerm rdfTerm, GraphWrapper gw, VisualGraph<Operator> parent) {
		super(parent, gw, rdfTerm, true);

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 1.0;
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING);

		if(rdfTerm.getItems().size() > 1) {
			this.comboBox = new JComboBox(rdfTerm.getItems().toArray());
			this.comboBox.setFont(parent.getFONT());

			this.add(this.comboBox, gbc);
		}
		else {
			JLabel label = new JLabel(((VisualGraphOperatorWithPrefix) parent).prefix.add(rdfTerm.getItems().iterator().next().toString()));
			label.setFont(parent.getFONT());

			this.add(label, gbc);
		}
	}

	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		return true;
	}

	public String getSelectedItem() {
		return this.comboBox.getSelectedItem().toString();
	}
}