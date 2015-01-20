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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleIdentifier;

public class RuleOperatorPanel extends AbstractGuiComponent<Operator>{

	private static final long serialVersionUID = -4782440727633760278L;

	protected GridBagConstraints gbc = null;
	private RuleOperator ruleOperator;
	private JLabel label;

	private VisualRifEditor visualRifEditor;

	public RuleOperatorPanel(final VisualGraph<Operator> parent, final GraphWrapper gw,
			final RuleOperator operator, final boolean movable) {
		super(parent, gw, operator, movable);

		this.setRuleOperator(operator);

		this.setVisualRifEditor(this.ruleOperator.getVisualRifEditor());

		if (!this.ruleOperator.isInitRulePanel()) {
			this.initRulePanel();
			this.ruleOperator.setInitRulePanel(true);
		}

		if (!this.ruleOperator.isInitRule()) {
			this.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(final ComponentEvent e) {
					RuleOperatorPanel.this.updateSize();
				}

			});
			this.ruleOperator.setInitRule(true);
		}

		this.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(final MouseEvent e){
				if (e.getClickCount() == 2){

					final RuleOperatorPanel rop = (RuleOperatorPanel) e.getComponent();
					final String ruleName = rop.getRuleName();

					RuleOperatorPanel.this.visualRifEditor.getTreePane().getTree_documents().setSelectionPath( RuleOperatorPanel.this.visualRifEditor.getRuleContainer().getRuleByName(ruleName).getRulePath());
					RuleOperatorPanel.this.visualRifEditor.getRuleContainer().showRule(ruleName);
				}
			}
		} );



		this.parent = parent;

		final Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.setBorder(raisedbevel);

		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTHWEST;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;

		this.gbc.insets = new Insets((int) parent.PADDING,
				(int) parent.PADDING, (int) parent.PADDING,
				(int) parent.PADDING);

		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;

		this.label = new JLabel(this.ruleOperator.getRuleName());
		this.label.setFont(parent.getFONT());

		this.add(this.label,this.gbc);
	}

	private void initRulePanel() {

	this.getVisualRifEditor().getRuleContainer().getRulePanelList().add(new RuleIdentifier(this.ruleOperator.getRulePanel().getRuleName(),this.ruleOperator.getRulePanel(),  this, this.ruleOperator.getRulePanel().getRulePath(),this.ruleOperator.getDocumentName()));

	this.addComponentListener(new ComponentAdapter() {
		@Override
		public void componentResized(final ComponentEvent e) {
			RuleOperatorPanel.this.updateSize();
		}
	});
}

	@Override
	public boolean validateOperatorPanel(final boolean arg0, final Object arg1) {
		return false;
	}

	public RuleOperator getRuleOperator() {
		return this.ruleOperator;
	}

	public void setRuleOperator(final RuleOperator ruleOperator) {
		this.ruleOperator = ruleOperator;
	}

	public String getRuleName() {
		return this.ruleOperator.getRuleName();
	}

	public void setRuleName(final String ruleName) {
		this.ruleOperator.setRuleName(ruleName);
	}

	public String getRuleLabelName() {
		return this.ruleOperator.getRuleLabelName();
	}

	@Override
	public void updateSize(){
		this.setMinimumSize(this.label.getSize());
		this.setSize(this.getPreferredSize());
		this.revalidate();
	}

	public void setRuleLabelName(final String ruleLabelName) {
		this.ruleOperator.setRuleLabelName(ruleLabelName);
		this.remove(this.label);
		this.label = new JLabel(this.getRuleLabelName());
		this.label.setFont(this.parent.getFONT());
		this.add(this.label,this.gbc);
		this.updateSize();

	}

	public void setSerializedOperator(final StringBuffer serializedOperator){
		this.ruleOperator.setSerializedOperator(serializedOperator);
	}

	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}
}
