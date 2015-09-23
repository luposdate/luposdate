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
package lupos.gui.operatorgraph.visualeditor.visualrif.operators;

import java.util.HashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.GroupOperatorPanel;
public class GroupOperator extends Operator {

	private String groupName = "Group";
	private String groupLabelName = "Group";

	
	/**
	 * <p>prefixAdded.</p>
	 */
	public void prefixAdded() {}

	/** {@inheritDoc} */
	public void prefixModified(String arg0, String arg1) {}

	/** {@inheritDoc} */
	public void prefixRemoved(String arg0, String arg1) {}

	/** {@inheritDoc} */
	public AbstractGuiComponent<Operator> draw(GraphWrapper arg0,
			VisualGraph<Operator> arg1) {
		this.panel = new GroupOperatorPanel(arg1, arg0, this, true);
		return this.panel;
	}

	
	/**
	 * <p>serializeOperator.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeOperator() {
		return null;
	}

	
	/** {@inheritDoc} */
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> arg0) {
		return null;
	}

	
	/** {@inheritDoc} */
	public boolean variableInUse(String arg0, HashSet<Operator> arg1) {
		return false;
	}

	/**
	 * <p>Getter for the field <code>groupLabelName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupLabelName() {

		return this.groupLabelName;
	}

	/**
	 * <p>Setter for the field <code>groupLabelName</code>.</p>
	 *
	 * @param groupLabelName a {@link java.lang.String} object.
	 */
	public void setGroupLabelName(String groupLabelName) {
		this.groupLabelName = groupLabelName;
		
	}

	/**
	 * <p>Getter for the field <code>groupName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * <p>Setter for the field <code>groupName</code>.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	


}
