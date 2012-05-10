package lupos.gui.operatorgraph.viewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.OperatorGraph;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;

public class ElementPanel extends AbstractSuperGuiComponent {
	private static final long serialVersionUID = -8331563992779078372L;

	public ElementPanel(OperatorGraph parent, GraphWrapper gw) {
		super(parent, gw, true);

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.NORTHWEST;

		String text = gw.toString();

		if(parent instanceof OperatorGraphWithPrefix) {
			text = gw.toString(((OperatorGraphWithPrefix) parent).getPrefix());
		}

		int width = 0;
		int height = 0;

		for(String textPart : text.split("\n")) {
			JLabel textLabel = new JLabel(textPart);
			textLabel.setFont(this.parent.getFONT());
			textLabel.setVerticalAlignment(JLabel.BOTTOM);
			textLabel.setPreferredSize(new Dimension(textLabel.getPreferredSize().width, textLabel.getPreferredSize().height));

			this.add(textLabel, gbc);

			gbc.gridy += 1;

			width = Math.max(width, textLabel.getPreferredSize().width);
			height += textLabel.getPreferredSize().height;
		}

		this.setPreferredSize(new Dimension(width + (int) (2 * parent.PADDING), height + (int) (2 * parent.PADDING)));
	}
}