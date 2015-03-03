
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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.operators;

import java.util.HashSet;

import javax.swing.JOptionPane;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.JumpOverOperatorPanel;

import org.json.JSONException;
import org.json.JSONObject;
public class JumpOverOperator extends AbstractRuleOperator {
	private String cardinality;
	private String conditions;

	/**
	 * <p>Constructor for JumpOverOperator.</p>
	 */
	public JumpOverOperator() {
		super();
		this.cardinality = "*";
		this.conditions = "";
	}

	/**
	 * <p>Constructor for JumpOverOperator.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JumpOverOperator(final String name, final JSONObject loadObject) throws JSONException {
		super(name, loadObject);
	}

	/** {@inheritDoc} */
	@Override
	protected void fromJSON(final JSONObject loadObject) throws JSONException {
		this.cardinality = loadObject.getString("cardinality");
		this.conditions = loadObject.getString("conditions");
	}

	/** {@inheritDoc} */
	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper gw, final VisualGraph<Operator> parent) {
		this.panel = new JumpOverOperatorPanel(parent, gw, this, this.classType, this.determineNameForDrawing(), this.alsoSubClasses, this.cardinality);

		return this.panel;
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject toJSON(final JSONObject connectionsObject) throws JSONException {
		final JSONObject saveObject = this.internalToJSON(connectionsObject);

		saveObject.put("cardinality", this.cardinality);
		saveObject.put("conditions", this.conditions);

		return saveObject;
	}

	/**
	 * <p>Setter for the field <code>cardinality</code>.</p>
	 *
	 * @param cardinality a {@link java.lang.String} object.
	 */
	public void setCardinality(final String cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * <p>Getter for the field <code>cardinality</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCardinality() {
		return this.cardinality;
	}

	/**
	 * <p>Getter for the field <code>conditions</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getConditions() {
		return this.conditions;
	}

	/**
	 * <p>Setter for the field <code>conditions</code>.</p>
	 *
	 * @param conditions a {@link java.lang.String} object.
	 */
	public void setConditions(final String conditions) {
		this.conditions = conditions;
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperator(final boolean showErrors, final HashSet<Operator> visited, final Object data) {
		final boolean ret = super.validateOperator(showErrors, visited, data);

		if(!ret) {
			return ret;
		}

		if(this.getPrecedingOperators().size() == 0 && this.getSucceedingOperators().size() == 0) {
			if(showErrors) {
				JOptionPane.showOptionDialog(this.panel.getParentQG().visualEditor, "A JumpOverOperator must have preceding or succeeding elements!", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
			}

			return false;
		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String getXPrefID(){
		return "ruleEditorPane_style_jumpoveroperator";
	}
}
