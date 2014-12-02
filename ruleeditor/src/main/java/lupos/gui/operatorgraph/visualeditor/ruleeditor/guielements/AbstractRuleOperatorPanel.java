/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleMouseListener;
import lupos.gui.operatorgraph.visualeditor.util.FocusThread;
import lupos.gui.operatorgraph.visualeditor.util.JCheckBoxOwnIcon;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;

public class AbstractRuleOperatorPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 2525787949536056772L;
	private final AbstractRuleOperatorPanel that = this;
	protected GridBagConstraints gbc = null;
	private JComboBox enumCoBo = null;
	private JTextField textField = null;
	private JCheckBoxOwnIcon cB_subClasses = null;

	public AbstractRuleOperatorPanel(final VisualGraph<Operator> parent, final GraphWrapper gw, final AbstractRuleOperator operator, final RuleEnum classType, final String name, final boolean alsoSubClasses) {
		super(parent, gw, operator, true);

		// build drop down menu for class names...
		this.enumCoBo = new JComboBox(RuleEnum.class.getEnumConstants());
		this.enumCoBo.setSelectedIndex(classType.ordinal());
		this.enumCoBo.setFont(parent.getFONT());
		this.enumCoBo.addMouseListener(new RuleMouseListener(this, this.enumCoBo));
		this.enumCoBo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				operator.setClassType((RuleEnum) AbstractRuleOperatorPanel.this.that.enumCoBo.getSelectedItem());
			}
		});


		// build label for name...
		final JLabel nameLabel = new JLabel("Name:");
		nameLabel.setFont(this.parent.getFONT());


		// build text field for object name...
		this.textField = new JTextField(name);
		this.textField.setFont(parent.getFONT());
		this.textField.setPreferredSize(new Dimension(this.textField.getPreferredSize().width + 100, this.textField.getPreferredSize().height));
		this.textField.addMouseListener(new RuleMouseListener(this, this.textField));
		this.textField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent fe) {}

			@Override
			public void focusLost(final FocusEvent fe) {
				try {
					operator.applyChange(AbstractRuleOperatorPanel.this.that.textField.getText());
				}
				catch(final ModificationException me) {
					final int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

					if(n == JOptionPane.YES_OPTION) {
						(new FocusThread(AbstractRuleOperatorPanel.this.that.textField)).start();
					}
				}
			}
		});


		// arrange elements...
		this.setLayout(new GridBagLayout());

		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTHWEST;
		this.gbc.gridwidth = 2;
		this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1;
		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING);
		this.gbc.fill = GridBagConstraints.BOTH;

		this.add(this.enumCoBo, this.gbc);
		this.gbc.gridx += 2;
		this.gbc.gridwidth = 1;

		this.add(nameLabel, this.gbc);
		this.gbc.gridx++;

		this.add(this.textField, this.gbc);
		this.gbc.gridy++;
		this.gbc.gridx = 0;

		if(this.getParentQG() == ((RuleEditorPane) this.getParentQG().visualEditor).getVisualGraphs().get(0)) {
			this.cB_subClasses = new JCheckBoxOwnIcon("also Subclasses", alsoSubClasses, this.parent.getFONT());
			this.cB_subClasses.addMouseListener(new RuleMouseListener(this, this.cB_subClasses));
			this.cB_subClasses.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent ie) {
					// get new state...
					final boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

					operator.setAlsoSubClasses(selected);
				}
			});

			this.add(this.cB_subClasses, this.gbc);
			this.gbc.gridx++;
		}
		this.setBackground(new Color(211, 211, 211));
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		((AbstractRuleOperator) this.operator).setClassType((RuleEnum) this.enumCoBo.getSelectedItem());

		try {
			final String newName = this.textField.getText();

			((AbstractRuleOperator) this.operator).applyChange(newName);

			if(!newName.equals("")) {
				final HashMap<String, Operator> names = (HashMap<String, Operator>) data;
				final Operator tmp = names.get(newName);

				if(tmp != null && !tmp.equals(this.operator)) {
					throw new ModificationException("Name already in use!", this.operator);
				}
				else {
					names.put(newName, this.operator);
				}
			}

			return true;
		}
		catch(final ModificationException me) {
			if(showErrors) {
				JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null , null);

				this.textField.grabFocus();
			}

			return false;
		}
	}

	@Override
	public void setBackground(final Color bg){
		super.setBackground(bg);
		if(this.enumCoBo!=null){
			this.enumCoBo.setBackground(bg);
		}
		if(this.cB_subClasses!=null){
			this.cB_subClasses.setBackground(bg);
		}
	}
}