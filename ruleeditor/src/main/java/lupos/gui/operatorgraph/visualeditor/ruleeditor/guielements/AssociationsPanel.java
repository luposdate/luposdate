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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.AssociationsContainer;

public class AssociationsPanel extends JPanel {
	private static final long serialVersionUID = 2631033545142339598L;

	private AssociationsPanel that = this;
	private GridBagConstraints gbc;
	private String elementName;
	private AssociationsContainer associationsContainer = null;

	public AssociationsPanel(String elementName, AssociationsContainer associationsContainer) {
		super(new GridBagLayout());

		this.elementName = elementName;
		this.associationsContainer = associationsContainer;

		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTH;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;
		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.insets = new Insets(0, 0, 0, 0);
		this.gbc.fill = GridBagConstraints.BOTH;
	}

	
	public void rebuildRules(LinkedList<RulePanel> elements) {
		this.removeAll();
		this.gbc.gridy = 0;

		for(RulePanel rule : elements) {
			final String ruleName = rule.toString();
			boolean state = this.associationsContainer.check(this.elementName, ruleName);

			JCheckBox jCheckBox = new JCheckBox(ruleName, state);
			jCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == ItemEvent.SELECTED) {
						that.associationsContainer.add(that.elementName, ruleName);
					}
					else {
						that.associationsContainer.remove(that.elementName, ruleName);
					}
				}
			});

			this.add(jCheckBox, this.gbc);

			this.gbc.gridy++;
		}

	}
	
	
	public void rebuildRulePackages(LinkedList<RulePackagePanel> elements) {
		this.removeAll();
		this.gbc.gridy = 0;

		for(RulePackagePanel rulePackage : elements) {
			final String rulePackageName = rulePackage.toString();
			boolean state = this.associationsContainer.check(rulePackageName, this.elementName);

			JCheckBox jCheckBox = new JCheckBox(rulePackageName, state);
			jCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					if(ie.getStateChange() == ItemEvent.SELECTED) {
						that.associationsContainer.add(rulePackageName, that.elementName);
					}
					else {
						that.associationsContainer.remove(rulePackageName, that.elementName);
					}
				}
			});

			this.add(jCheckBox, this.gbc);

			this.gbc.gridy++;
		}
	}

	public void setElementName(String newName) {
		this.elementName = newName;
	}

	public AssociationsContainer getAssociationsContainer() {
		return this.associationsContainer;
	}
}