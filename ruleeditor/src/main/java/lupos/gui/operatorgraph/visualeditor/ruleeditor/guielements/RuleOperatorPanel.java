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

import javax.swing.JLabel;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleEnum;

public class RuleOperatorPanel extends AbstractRuleOperatorPanel {
	private static final long serialVersionUID = -7897238149968316491L;
	private JLabel startNodeLabel;

	public RuleOperatorPanel(final VisualGraph<Operator> parent, GraphWrapper gw, RuleOperator operator, RuleEnum classType, String name, boolean startNode, boolean alsoSubClasses) {
		super(parent, gw, operator, classType, name, alsoSubClasses);

		if(this.getParentQG() == ((RuleEditorPane) this.getParentQG().visualEditor).getVisualGraphs().get(0)) {
			this.startNodeLabel = new JLabel("start node");
			this.startNodeLabel.setFont(this.parent.getFONT());
			this.startNodeLabel.setVisible(startNode);

			this.gbc.gridx++;
			this.gbc.gridwidth = 2;
			this.add(this.startNodeLabel, this.gbc);

			if(startNode) {
				((RuleEditorPane) this.getParentQG().visualEditor).setStartNode(operator);
			}
		}
	}

	public void setAsStartNode(boolean state) {
		this.startNodeLabel.setVisible(state);
	}
}