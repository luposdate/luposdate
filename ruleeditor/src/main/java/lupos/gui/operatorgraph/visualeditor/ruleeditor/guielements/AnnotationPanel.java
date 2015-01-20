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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.JumpOverOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ModeEnum;
import lupos.gui.operatorgraph.visualeditor.util.FocusThread;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.JCheckBoxOwnIcon;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
import lupos.misc.Triple;

public class AnnotationPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 1L;
	private final AnnotationPanel that = this;
	private JLabel jL_id = null;
	private JLabel jL_opID = null;
	private JCheckBoxOwnIcon jCB_activate = null;
	private JTextField jTF_id = null;
	private JRadioButton jRB_mode_exists = null;
	private JRadioButton jRB_mode_all_preceding = null;
	private JRadioButton jRB_mode_only_preceding = null;
	private JRadioButton jRB_mode_all_succeeding = null;
	private JRadioButton jRB_mode_only_succeeding = null;
	private JRadioButton jRB_mode_only_preceding_and_succeeding = null;

	public AnnotationPanel(final VisualGraph<Operator> parent, final AbstractRuleOperator operator, final AbstractRuleOperator child, final Triple<Boolean, String, ModeEnum> data) {
		super(parent, new GraphWrapperOperator(operator), operator, false);

		this.parentOp = operator;
		this.child = child;

		boolean active = false;
		String opID = "";
		ModeEnum mode = ModeEnum.EXISTS;

		if(data != null) {
			active = data.getFirst();

			if(!data.getSecond().equals("-1")) {
				opID = data.getSecond();
			}

			mode = data.getThird();
		}

		this.setLayout(new GridBagLayout());

		final GridBagConstraints mainGBC = new GridBagConstraints();
		mainGBC.gridwidth = 3;
		mainGBC.gridheight = 1;
		mainGBC.gridx = mainGBC.gridy = 0;
		mainGBC.fill = GridBagConstraints.NONE;
		mainGBC.anchor = GridBagConstraints.WEST;
		mainGBC.weighty = 1.0;
		// mainGBC.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING);

		this.jTF_id = new JTextField(opID, 2);
		this.jTF_id.setFont(parent.getFONT());
		this.jTF_id.setEnabled(active);
		this.jTF_id.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent fe) {}

			@Override
			public void focusLost(final FocusEvent fe) {
				final String content = AnnotationPanel.this.that.jTF_id.getText();

				if(!content.equals("")) {
					try {
						child.setOpID(content, AnnotationPanel.this.that.isActive());
						operator.setChildOpID(child, content);
					}
					catch(final ModificationException me) {
						final int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

						if(n == JOptionPane.YES_OPTION) {
							(new FocusThread(AnnotationPanel.this.that.jTF_id)).start();
						}
					}
				}
			}
		});

		this.jCB_activate = new JCheckBoxOwnIcon("activate", active, parent.getFONT());
		this.jCB_activate.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent ie) {
				// get new state...
				final boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

				AnnotationPanel.this.that.jTF_id.setEnabled(selected);
				operator.setActiveConnection(child, selected);
			}
		});

		this.jRB_mode_exists = new JRadioButton(ModeEnum.EXISTS.toString(), mode == ModeEnum.EXISTS);
		this.jRB_mode_exists.setFont(parent.getFONT());
		this.jRB_mode_exists.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				if(AnnotationPanel.this.that.jRB_mode_exists.isSelected()) {
					operator.setMode(child, ModeEnum.EXISTS);
				}
			}
		});

		this.jRB_mode_all_preceding = new JRadioButton(ModeEnum.ALL_PRECEDING.toString(), mode == ModeEnum.ALL_PRECEDING);
		this.jRB_mode_all_preceding.setFont(parent.getFONT());
		this.jRB_mode_all_preceding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				if(AnnotationPanel.this.that.jRB_mode_all_preceding.isSelected()) {
					operator.setMode(child, ModeEnum.ALL_PRECEDING);
				}
			}
		});

		this.jRB_mode_only_preceding = new JRadioButton(ModeEnum.ONLY_PRECEDING.toString(), mode == ModeEnum.ONLY_PRECEDING);
		this.jRB_mode_only_preceding.setFont(parent.getFONT());
		this.jRB_mode_only_preceding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				if(AnnotationPanel.this.that.jRB_mode_only_preceding.isSelected()) {
					operator.setMode(child, ModeEnum.ONLY_PRECEDING);
				}
			}
		});

		this.jRB_mode_all_succeeding = new JRadioButton(ModeEnum.ALL_SUCCEEDING.toString(), mode == ModeEnum.ALL_SUCCEEDING);
		this.jRB_mode_all_succeeding.setFont(parent.getFONT());
		this.jRB_mode_all_succeeding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				if(AnnotationPanel.this.that.jRB_mode_all_succeeding.isSelected()) {
					operator.setMode(child, ModeEnum.ALL_SUCCEEDING);
				}
			}
		});

		this.jRB_mode_only_succeeding = new JRadioButton(ModeEnum.ONLY_SUCCEEDING.toString(), mode == ModeEnum.ONLY_SUCCEEDING);
		this.jRB_mode_only_succeeding.setFont(parent.getFONT());
		this.jRB_mode_only_succeeding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				if(AnnotationPanel.this.that.jRB_mode_only_succeeding.isSelected()) {
					operator.setMode(child, ModeEnum.ONLY_SUCCEEDING);
				}
			}
		});

		this.jRB_mode_only_preceding_and_succeeding = new JRadioButton(ModeEnum.ONLY_PRECEDING_AND_SUCCEEDING.toString(), mode == ModeEnum.ONLY_PRECEDING_AND_SUCCEEDING);
		this.jRB_mode_only_preceding_and_succeeding.setFont(parent.getFONT());
		this.jRB_mode_only_preceding_and_succeeding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				if(AnnotationPanel.this.that.jRB_mode_only_preceding_and_succeeding.isSelected()) {
					operator.setMode(child, ModeEnum.ONLY_PRECEDING_AND_SUCCEEDING);
				}
			}
		});

		final ButtonGroup group = new ButtonGroup();
		group.add(this.jRB_mode_exists);
		group.add(this.jRB_mode_all_preceding);
		group.add(this.jRB_mode_only_preceding);
		group.add(this.jRB_mode_all_succeeding);
		group.add(this.jRB_mode_only_succeeding);
		group.add(this.jRB_mode_only_preceding_and_succeeding);

		if(operator.getClass() == JumpOverOperator.class || child.getClass() == JumpOverOperator.class) {
			this.jRB_mode_only_preceding_and_succeeding.setSelected(true);
			operator.setMode(child, ModeEnum.ONLY_PRECEDING_AND_SUCCEEDING);

			this.jRB_mode_exists.setEnabled(false);
			this.jRB_mode_all_preceding.setEnabled(false);
			this.jRB_mode_only_preceding.setEnabled(false);
			this.jRB_mode_all_succeeding.setEnabled(false);
			this.jRB_mode_only_succeeding.setEnabled(false);
			this.jRB_mode_only_preceding_and_succeeding.setEnabled(false);
		}


		this.jL_id = new JLabel("ID:");
		this.jL_id.setFont(parent.getFONT());

		this.jL_opID = new JLabel("OperandID");
		this.jL_opID.setFont(parent.getFONT());

		this.add(this.jL_opID, mainGBC);
		mainGBC.gridy++;
		mainGBC.gridx = 0;
		mainGBC.gridwidth = 1;

		this.add(this.jCB_activate, mainGBC);
		mainGBC.gridx++;

		this.add(this.jL_id, mainGBC);
		mainGBC.gridx++;

		this.add(this.jTF_id, mainGBC);
		mainGBC.gridy++;
		mainGBC.gridx = 0;
		mainGBC.gridwidth = 1;

		this.add(this.jRB_mode_exists, mainGBC);
		mainGBC.gridx++;

		this.add(this.jRB_mode_only_preceding, mainGBC);
		mainGBC.gridx++;

		this.add(this.jRB_mode_all_preceding, mainGBC);
		mainGBC.gridy++;
		mainGBC.gridx = 1;

		this.add(this.jRB_mode_only_succeeding, mainGBC);
		mainGBC.gridx++;

		this.add(this.jRB_mode_all_succeeding, mainGBC);
		mainGBC.gridy++;
		mainGBC.gridx = 1;
		mainGBC.gridwidth = 2;

		this.add(this.jRB_mode_only_preceding_and_succeeding, mainGBC);

		this.setBackground(new Color(255, 165, 0));
	}

	@Override
	public void updateSize() {
		final int objWidth = (int) (4*this.parent.PADDING) + this.jCB_activate.getPreferredSize().width + this.jL_id.getPreferredSize().width + this.jTF_id.getPreferredSize().width;
		final int objHeight = (int) (3*this.parent.PADDING) + this.jL_opID.getPreferredSize().height + this.jTF_id.getPreferredSize().height;

		// if the needed size of the content of the panel is not equal with the
		// current size of it...
		if(objWidth != this.getPreferredSize().width || objHeight != this.getPreferredSize().height) {
			// update size of the panel...
			final Dimension d = new Dimension(objWidth, objHeight);

			final int oldCenter = this.getX() + (this.getPreferredSize().width / 2);

			this.setPreferredSize(d);
			this.setSize(d);
			this.setMinimumSize(d);

			final int newCenter = this.getX() + (d.width / 2);

			final int xDiff = (newCenter - oldCenter) / 2;

			final int newX = this.getX() - xDiff;

			this.setLocation(newX, this.getY());

			this.revalidate();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		try {
			final String idText = this.jTF_id.getText();

			final AbstractRuleOperator ruleOp = (AbstractRuleOperator) this.operator;
			final AbstractRuleOperator ruleChildOp = (AbstractRuleOperator) this.child;

			ruleOp.setActiveConnection(ruleChildOp, this.jCB_activate.isSelected());
			final boolean ret = ruleChildOp.setOpID(idText, this.isActive());

			if(ret) {
				ruleOp.setChildOpID(ruleChildOp, idText);
			}

			ruleOp.setMode(ruleChildOp, this.getMode());

			if(!idText.equals("")) {
				try {
					Integer.parseInt(idText);
				}
				catch(final NumberFormatException nfe) {
					final HashMap<String, Operator> names = (HashMap<String, Operator>) data;

					if(names.containsKey(idText)) {
						throw new ModificationException("Name already in use!", this.operator);
					}
					else {
						names.put(idText, this.operator);
					}
				}
			}
		}
		catch(final ModificationException me) {
			if(showErrors) {
				JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

				this.jTF_id.grabFocus();
			}

			return false;
		}

		return true;
	}

	public boolean isActive() {
		return this.jCB_activate.isSelected();
	}

	public int getOpID() {
		try {
			return Integer.parseInt(this.jTF_id.getText());
		}
		catch(final NumberFormatException nfe) {
			return -1;
		}
	}

	public String getOpLabel() {
		final String labelText = this.jTF_id.getText();

		try {
			Integer.parseInt(labelText);

			return "";
		}
		catch(final NumberFormatException nfe) {
			return labelText;
		}
	}

	public ModeEnum getMode() {
		if(this.jRB_mode_exists.isSelected()) {
			return ModeEnum.EXISTS;
		}
		else if(this.jRB_mode_all_preceding.isSelected()) {
			return ModeEnum.ALL_PRECEDING;
		}
		else if(this.jRB_mode_all_succeeding.isSelected()) {
			return ModeEnum.ALL_SUCCEEDING;
		}
		else if(this.jRB_mode_only_preceding.isSelected()) {
			return ModeEnum.ONLY_PRECEDING;
		}
		else if(this.jRB_mode_only_succeeding.isSelected()) {
			return ModeEnum.ONLY_SUCCEEDING;
		}
		else if(this.jRB_mode_only_preceding_and_succeeding.isSelected()) {
			return ModeEnum.ONLY_PRECEDING_AND_SUCCEEDING;
		}
		else {
			return null;
		}
	}

	@Override
	public void setBackground(final Color bg){
		super.setBackground(bg);
		if(this.jRB_mode_exists!=null){
			this.jRB_mode_exists.setBackground(bg);
			this.jRB_mode_all_preceding.setBackground(bg);
			this.jRB_mode_only_preceding.setBackground(bg);
			this.jRB_mode_all_succeeding.setBackground(bg);
			this.jRB_mode_only_succeeding.setBackground(bg);
			this.jRB_mode_only_preceding_and_succeeding.setBackground(bg);
			this.jCB_activate.setBackground(bg);
		}
	}
}