package lupos.gui.operatorgraph.visualeditor.util;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ConfirmationDialog extends JDialog {
	private static final long serialVersionUID = 7715900088284584423L;
	private int returnValue = 0; // 0 = cancel; 1 = set data and close; 2 = ignore data and close

	public ConfirmationDialog(JFrame parentFrame, JPanel contentPanel, String title, String confirmationText) {
		super(parentFrame, title, true);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.weightx = gbc.weighty = 1;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.add(contentPanel, gbc);

		gbc.gridy++;
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.weighty = 0;

		mainPanel.add(new JLabel(confirmationText, SwingConstants.CENTER), gbc);

		gbc.gridy++;

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton bt_yes = new JButton("yes");
		bt_yes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				returnValue = 1;
				setVisible(false);
			}
		});

		JButton bt_no = new JButton("no");
		bt_no.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				returnValue = 2;
				setVisible(false);
			}
		});

		JButton bt_cancel = new JButton("cancel");
		bt_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				returnValue = 0;
				setVisible(false);
			}
		});

		buttonPanel.add(bt_yes);
		buttonPanel.add(bt_no);
		buttonPanel.add(bt_cancel);

		mainPanel.add(buttonPanel, gbc);

		this.getContentPane().add(mainPanel);
		this.pack();
		this.setSize(794, 400);
		this.setLocationRelativeTo(this);
	}

	/**
	 * Gets the return value of the dialog.
	 * 0 = cancel; 1 = set data and close; 2 = ignore data and close
	 * 
	 * @return return value
	 */
	public int getReturnValue() {
		return this.returnValue;
	}
}