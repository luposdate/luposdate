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
package lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
public class PredicatePanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 5428234388837014129L;

	/**
	 * <p>Constructor for PredicatePanel.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix} object.
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.operators.RDFTerm} object.
	 * @param child a {@link lupos.gui.operatorgraph.visualeditor.operators.RDFTerm} object.
	 */
	public PredicatePanel(final VisualGraphOperatorWithPrefix parent, final RDFTerm operator, final RDFTerm child) {
		super(parent, new GraphWrapperOperator(operator), operator, false);

		this.parentOp = operator;
		this.child = child;

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gbc.gridy = 0;
		gbc.weightx = gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, 0);

		for(int i = 0; i < operator.getPredicates(child).size(); ++i) { // walk through predicates and add them...
			if(i == operator.getPredicates(child).size()-1) {
				gbc.insets = new Insets((int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING, (int) parent.PADDING);
			}

			this.add(this.createPredicateElement(parent.prefix.add(operator.getPredicates(child).get(i).toString())), gbc);

			gbc.gridx++;
		}
	}

	/** {@inheritDoc} */
	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		return true;
	}

	private JLabel createPredicateElement(String predicate) {
		JLabel label = new JLabel(predicate, SwingConstants.CENTER);
		label.setFont(this.parent.getFONT());
		label.setBorder(new LineBorder(Color.BLACK));

		Dimension size = new Dimension(label.getPreferredSize().width + (int) parent.PADDING, label.getPreferredSize().height + (int) parent.PADDING);
		label.setPreferredSize(size);
		label.setSize(size);

		return label;
	}
}
