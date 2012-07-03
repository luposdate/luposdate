/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.JavaScanner;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.JumpOverOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum;

public class JumpOverOperatorPanel extends AbstractRuleOperatorPanel {
	private static final long serialVersionUID = 3254499503232644390L;
	private JumpOverOperatorPanel that = this;
	private JRadioButton cardinality_oneORnone = null;
	private JRadioButton cardinality_oneORmore = null;
	private JRadioButton cardinality_noneORmore = null;

	public JumpOverOperatorPanel(final VisualGraph<Operator> parent, GraphWrapper gw, final JumpOverOperator operator, RuleEnum classType, String name, boolean alsoSubClasses, String cardinality) {
		super(parent, gw, operator, classType, name, alsoSubClasses);

		if(this.getParentQG() == ((RuleEditorPane) this.getParentQG().visualEditor).getVisualGraphs().get(0)) {
			JButton jB_conditions = new JButton("Conditions");
			jB_conditions.setFont(parent.getFONT());
			jB_conditions.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JPanel panel = new JPanel();
					final JFrame frame = parent.visualEditor.createSmallFrame(panel, "JumpOverOperator conditions");

					LuposDocument document = new LuposDocument();
					final JTextPane tp = new LuposJTextPane(document);
					document.init(JavaScanner.createILuposParser(new LuposDocumentReader(document)), true);

					tp.addKeyListener(parent.visualEditor.getKeyListener(frame));
					tp.setFont(new Font("Courier New", Font.PLAIN, 12));
					tp.setText(operator.getConditions());

					// create OK button, which starts query evaluation...
					JButton bt_ok = new JButton("OK");
					bt_ok.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							operator.setConditions(tp.getText());
							frame.setVisible(false); // hide query input frame
						}
					});

					JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
					buttonPanel.add(bt_ok);

					// create main panel and add components to it...
					panel.setLayout(new BorderLayout());
					panel.add(new JScrollPane(tp), BorderLayout.CENTER);
					panel.add(buttonPanel, BorderLayout.SOUTH);

					frame.setVisible(true);
				}
			});

			this.add(jB_conditions, this.gbc);
			this.gbc.gridx++;


			JLabel cardinalityLabel = new JLabel("Cardinality: ");
			cardinalityLabel.setFont(parent.getFONT());

			this.add(cardinalityLabel, this.gbc);
			this.gbc.gridx++;


			this.cardinality_oneORnone = new JRadioButton("? ", cardinality.equals("?"));
			this.cardinality_oneORnone.setFont(parent.getFONT());
			this.cardinality_oneORnone.setOpaque(false);
			this.cardinality_oneORnone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if(that.cardinality_oneORnone.isSelected()) {
						operator.setCardinality("?");
					}
				}
			});

			this.cardinality_oneORmore = new JRadioButton("+ ", cardinality.equals("+"));
			this.cardinality_oneORmore.setFont(parent.getFONT());
			this.cardinality_oneORmore.setOpaque(false);
			this.cardinality_oneORmore.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if(that.cardinality_oneORmore.isSelected()) {
						operator.setCardinality("+");
					}
				}
			});

			this.cardinality_noneORmore = new JRadioButton("* ", cardinality.equals("*"));
			this.cardinality_noneORmore.setFont(parent.getFONT());
			this.cardinality_noneORmore.setOpaque(false);
			this.cardinality_noneORmore.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if(that.cardinality_noneORmore.isSelected()) {
						operator.setCardinality("*");
					}
				}
			});

			ButtonGroup group = new ButtonGroup();
			group.add(this.cardinality_oneORnone);
			group.add(this.cardinality_oneORmore);
			group.add(this.cardinality_noneORmore);

			JPanel cardinalityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			cardinalityPanel.setOpaque(false);
			cardinalityPanel.add(this.cardinality_oneORnone);
			cardinalityPanel.add(this.cardinality_oneORmore);
			cardinalityPanel.add(this.cardinality_noneORmore);

			this.add(cardinalityPanel, this.gbc);
		}
	}

	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		if(!super.validateOperatorPanel(showErrors, data)) {
			return false;
		}

		if(this.getParentQG() == ((RuleEditorPane) this.getParentQG().visualEditor).getVisualGraphs().get(0)) {
			JumpOverOperator jumpOp = (JumpOverOperator) this.operator;

			if(this.cardinality_oneORnone.isSelected()) {
				jumpOp.setCardinality("?");
			}
			else if(this.cardinality_oneORmore.isSelected()) {
				jumpOp.setCardinality("+");
			}
			else if(this.cardinality_noneORmore.isSelected()) {
				jumpOp.setCardinality("*");
			}
		}

		return true;
	}
}