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

import javax.swing.JLabel;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.GroupOperator;
public class GroupOperatorPanel extends AbstractGuiComponent<Operator> {
	protected GridBagConstraints gbc = null;
	private GroupOperator groupOperator;
	private JLabel label;
	private static final long serialVersionUID = -9204167792670922418L;

	// Constructor
	/**
	 * <p>Constructor for GroupOperatorPanel.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.GroupOperator} object.
	 * @param movable a boolean.
	 */
	public GroupOperatorPanel(final VisualGraph<Operator> parent, final GraphWrapper gw,
			final GroupOperator operator, final boolean movable) {
		super(parent, gw, operator, movable);

		this.parent = parent;

		this.setGroupOperator(operator);

		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTHWEST;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;

		this.gbc.insets = new Insets((int) parent.PADDING,
				(int) parent.PADDING, (int) parent.PADDING,
				(int) parent.PADDING);

		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;


		this.label = new JLabel(this.getGroupLabelName());
		this.label.setFont(parent.getFONT());

		this.add(this.label,this.gbc);
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		return false;
	}

	/**
	 * <p>Getter for the field <code>groupOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.GroupOperator} object.
	 */
	public GroupOperator getGroupOperator() {
		return this.groupOperator;
	}

	/**
	 * <p>Setter for the field <code>groupOperator</code>.</p>
	 *
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.GroupOperator} object.
	 */
	public void setGroupOperator(final GroupOperator operator) {
		this.groupOperator = operator;
	}

	/** {@inheritDoc} */
	@Override
	public void updateSize(){
		this.setMinimumSize(this.label.getSize());
		this.setSize(this.getPreferredSize());
		this.revalidate();
	}

	/**
	 * <p>getGroupLabelName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupLabelName() {
		return this.groupOperator.getGroupLabelName();
	}

	/**
	 * <p>setGroupLabelName.</p>
	 *
	 * @param groupLabelName a {@link java.lang.String} object.
	 */
	public void setGroupLabelName(final String groupLabelName) {
		this.groupOperator.setGroupLabelName(groupLabelName);

		this.remove(this.label);

		this.label = new JLabel(this.getGroupLabelName());
		this.label.setFont(this.parent.getFONT());
		this.add(this.label,this.gbc);
		this.updateSize();
	}

	/**
	 * <p>setGroupName.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 */
	public void setGroupName(final String groupName){
		this.groupOperator.setGroupName(groupName);
	}

	/**
	 * <p>getGroupName.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupName(){
		return this.groupOperator.getGroupName();
	}
}
