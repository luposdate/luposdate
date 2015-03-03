
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.AnnotationPanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator;
public class ConnectionContainer {
	private AbstractRuleOperator parent = null;
	private AbstractRuleOperator child = null;
	private Boolean isActive = false;
	private Integer opID = -1;
	private String opLabel = "";
	private ModeEnum mode = null;

	/**
	 * <p>Constructor for ConnectionContainer.</p>
	 *
	 * @param ap a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.AnnotationPanel} object.
	 */
	public ConnectionContainer(AnnotationPanel ap) {
		this.parent = (AbstractRuleOperator) ap.getOperator();
		this.child = (AbstractRuleOperator) ap.getChild();
		this.isActive = ap.isActive();
		this.opID = ap.getOpID();
		this.opLabel = ap.getOpLabel();
		this.mode = ap.getMode();
	}

	/**
	 * <p>hashCode.</p>
	 *
	 * @return a int.
	 */
	public int hashCode() {
		long hashCode = (long) this.parent.getName().hashCode() + (long) this.child.getName().hashCode() + this.isActive.hashCode() + this.mode.hashCode();

		if(this.isActive) {
			hashCode += this.opID.hashCode();
			hashCode += this.opLabel.hashCode();
		}

		return (int) (hashCode % Integer.MAX_VALUE);
	}

	/** {@inheritDoc} */
	public boolean equals(Object element) {
		if(element instanceof ConnectionContainer) {
			ConnectionContainer conn = (ConnectionContainer) element;

			if(!this.parent.getName().equals(conn.parent.getName())) {
				return false;
			}

			if(!this.child.getName().equals(conn.child.getName())) {
				return false;
			}

			if(this.isActive != conn.isActive) {
				return false;
			}

			if(this.isActive == true && this.opID != conn.opID && !this.opLabel.equals(conn.opLabel)) {
				return false;
			}

			if(this.mode != conn.mode) {
				return false;
			}

			return true;
		}

		return false;
	}

	/**
	 * <p>Getter for the field <code>parent</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator} object.
	 */
	public AbstractRuleOperator getParent() {
		return this.parent;
	}

	/**
	 * <p>Getter for the field <code>child</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.operators.AbstractRuleOperator} object.
	 */
	public AbstractRuleOperator getChild() {
		return this.child;
	}

	/**
	 * <p>Getter for the field <code>isActive</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getIsActive() {
		return this.isActive;
	}

	/**
	 * <p>Getter for the field <code>opID</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	public Integer getOpID() {
		return this.opID;
	}

	/**
	 * <p>getOpIDLabel.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOpIDLabel() {
		return this.opLabel;
	}

	/**
	 * <p>Getter for the field <code>mode</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ModeEnum} object.
	 */
	public ModeEnum getMode() {
		return this.mode;
	}
}
