/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

	/**
	 * <p>Constructor for ConfirmationDialog.</p>
	 *
	 * @param parentFrame a {@link javax.swing.JFrame} object.
	 * @param contentPanel a {@link javax.swing.JPanel} object.
	 * @param title a {@link java.lang.String} object.
	 * @param confirmationText a {@link java.lang.String} object.
	 */
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
