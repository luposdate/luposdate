package lupos.gui.operatorgraph.visualeditor.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class StatusBar extends JPanel {
	private static final long serialVersionUID = 4602066192309533345L;
	private JLabel label = new JLabel();

	public StatusBar() {
		this(" ");
	}

	public StatusBar(String text) {
		super(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = gbc.gridwidth = 1;
		gbc.weightx = gbc.weighty = 1.0;
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets(0, 1, 0, 1);

		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
		this.add(this.label, gbc);

		this.setText(text);
	}

	public void setText(String text) {
		this.label.setText(text);

		this.revalidate();
	}

	public void clear() {
		this.setText(" ");
	}
}