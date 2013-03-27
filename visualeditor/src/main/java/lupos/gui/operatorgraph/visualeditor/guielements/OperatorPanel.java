/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.operators.JTFOperator;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.FocusThread;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;

public class OperatorPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 1L;
	protected JTextFieldResizing textField;
	protected JLabel label;

	public OperatorPanel(JTFOperator operator, GraphWrapper gw, VisualGraph<Operator> parent, String text) {
		this(operator, gw, parent, text, "");
	}

	public OperatorPanel(JTFOperator operator, GraphWrapper gw, VisualGraph<Operator> parent, String text, String label) {
		super(parent, gw, operator, true);

		init(operator, parent.PADDING, parent.getFONT(), text, label);
	}

	public OperatorPanel(JTFOperator operator, GraphWrapper gw, double PADDING, Font font, String text, String label) {
		super(null, gw, operator, true);

		init(operator, PADDING, font, text, label);
	}

	private void init(final JTFOperator operator, double PADDING, Font font, String text, String label) {
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets((int) PADDING, (int) PADDING, (int) PADDING, (int) PADDING);
		gbc.fill = GridBagConstraints.BOTH;

		this.textField = new JTextFieldResizing(text, font, this);
		this.textField.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if(parent.visualEditor instanceof Suggester && 
						((Suggester<Operator>) parent.visualEditor).isInSuggestionMode(operator)) {
					makeSuggestions();
				}
				else {
					handleConnectionMode();
				}
			}
		});
		this.textField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent fe) {}

			public void focusLost(FocusEvent fe) {
				try {
					operator.applyChange(textField.getText());
				}
				catch(ModificationException me) {
					int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

					if(n == JOptionPane.YES_OPTION) {
						(new FocusThread(textField)).start();
					}
				}
			}
		});

		if(!label.equals("")) {
			JLabel textLabel = new JLabel(label + " ");
			textLabel.setFont(parent.getFONT());

			this.label = textLabel;

			gbc.weightx = 0;
			gbc.insets = new Insets((int) PADDING, (int) PADDING, (int) PADDING, 0);

			this.add(textLabel, gbc);

			gbc.gridx++;
			gbc.weightx = 1;
			gbc.insets = new Insets((int) PADDING, 0, (int) PADDING, (int) PADDING);
		}

		this.add(this.textField, gbc);

		int width = this.textField.getPreferredSize().width + (int) (2 * PADDING);
		final int height = this.textField.getPreferredSize().height + (int) (2 * PADDING);

		if(!label.equals("")) {
			width = this.getPreferredSize().width + (int) (2 * PADDING);
		}

		this.setPreferredSize(new Dimension(width, height));
	}

	public void setValue(String value) {
		this.textField.setText(value);
	}

	public String getValue() {
		return this.textField.getText();
	}

	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		try {
			((JTFOperator) this.operator).applyChange(this.textField.getText());

			return true;
		}
		catch(ModificationException me) {
			if(showErrors) {
				JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

				this.textField.grabFocus();
			}

			return false;
		}
	}

	public void updateSize() {
		int objWidth = this.textField.getPreferredSize().width + (int) (2 * this.parent.PADDING);
		int objHeight = this.textField.getPreferredSize().height + (int) (2 * this.parent.PADDING);

		if(this.label != null) {
			objWidth += this.label.getPreferredSize().width;
		}

		// if the needed size of the content of the panel is not equal with the
		// current size of it...
		if(objWidth != this.getPreferredSize().width || objHeight != this.getPreferredSize().height) {
			// update size of the panel...
			Dimension d = new Dimension(objWidth, objHeight);

			this.setPreferredSize(d);
			this.setSize(d);
			this.setMinimumSize(d);

			// update size of the GraphBox...
			this.getBox().width = objWidth;
			this.getBox().height = objHeight;
		}

		this.parent.revalidate();
		this.parent.repaint();
	}
}