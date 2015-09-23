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

import javax.swing.JPanel;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer;
public class ContainerPanel extends lupos.gui.operatorgraph.visualeditor.guielements.ContainerPanel {

	private static final long serialVersionUID = 5993459978976260745L;

	private RuleGraph ruleGraph = null;

	private AbstractContainer operatorContainer;
	private VisualRifEditor visualRifEditor;

	// Constructor
	/**
	 * <p>Constructor for ContainerPanel.</p>
	 *
	 * @param operatorContainer a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer} object.
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param graphPanel a {@link javax.swing.JPanel} object.
	 * @param ruleGraph a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public ContainerPanel(final AbstractContainer operatorContainer, final GraphWrapper gw, final JPanel graphPanel, final RuleGraph ruleGraph, final RuleGraph parent, final VisualRifEditor visualRifEditor) {
		super(operatorContainer, gw, graphPanel, ruleGraph, parent);
		this.operatorContainer = operatorContainer;
		this.setMinimumSize(null);
		// this.graphSP.setMinimumSize(null);
	}

	/**
	 * <p>Getter for the field <code>ruleGraph</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 */
	public RuleGraph getRuleGraph(){
		return this.ruleGraph;
	}

	/**
	 * <p>Setter for the field <code>ruleGraph</code>.</p>
	 *
	 * @param ruleGraph a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 */
	public void setRuleGraph(final RuleGraph ruleGraph){
		this.ruleGraph = ruleGraph;
		this.qg = ruleGraph;
	}

	/**
	 * <p>Getter for the field <code>operatorContainer</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer} object.
	 */
	public AbstractContainer getOperatorContainer() {
		return this.operatorContainer;
	}

	/**
	 * <p>Setter for the field <code>operatorContainer</code>.</p>
	 *
	 * @param operatorContainer a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer} object.
	 */
	public void setOperatorContainer(final AbstractContainer operatorContainer) {
		this.operatorContainer = operatorContainer;
	}

	/**
	 * <p>Getter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	/**
	 * <p>Setter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
		this.operatorContainer.setVisualRifEditor(this.visualRifEditor);
	}
}
