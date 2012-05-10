package lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;

public class PredicatePanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 5428234388837014129L;

	public PredicatePanel(final VisualGraphOperatorWithPrefix parent, final RDFTerm operator, final RDFTerm child) {
		super(parent, new GraphWrapperOperator(operator), operator, false);

		this.parentOp = operator;
		this.child = child;

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, 0);

		for(int i = 0; i < operator.getPredicates(child).size(); ++i) { // walk through predicates and add them...
			if(i == operator.getPredicates(child).size()-1) {
				gbc.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING);
			}

			this.add(this.createPredicateElement(parent.prefix.add(operator.getPredicates(child).get(i).toString())), gbc);

			gbc.gridx++;
		}
	}

	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		return true;
	}

	private JLabel createPredicateElement(String predicate) {
		JLabel label = new JLabel(predicate, SwingConstants.CENTER);
		label.setFont(this.parent.getFONT());
		label.setBorder(new LineBorder(Color.BLACK));

		Dimension size = new Dimension(label.getPreferredSize().width + (int) parent.PADDING, label.getPreferredSize().height + (int) parent.PADDING);
		label.setPreferredSize(size);
		label.setSize(size);

		return label;
	}
}