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

import java.awt.Point;
import java.util.HashSet;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.RuleOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleContainer;
import lupos.rif.model.Rule;

import org.json.JSONException;
import org.json.JSONObject;
public class RuleOperator extends Operator  {

	private String ruleName = "Rule";
	private String ruleLabelName = "Rule";
	private StringBuffer serializedOperator = null;
	private VisualRifEditor visualRifEditor;
	private boolean initRule = false;
	private boolean initRulePanel = false;
	private String documentName;
	private RulePanel rulePanel;

	// rif -> visualRif
	private Rule object;

	/**
	 * <p>Constructor for RuleOperator.</p>
	 */
	public RuleOperator(){
	}

	/**
	 * <p>Constructor for RuleOperator.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public RuleOperator(final String name, final JSONObject loadObject) throws JSONException {
		this.documentName = loadObject.getString("DOCUMENTNAME");
		this.ruleName = name;
	}

	/**
	 * <p>initRule.</p>
	 */
	public void initRule() {
		if (this.getDocumentName() == null) {
			this.setDocumentName(this.visualRifEditor.getDocumentContainer().getNameOfActiveElement());
		}
		final RuleContainer ruleContainer = this.visualRifEditor.getRuleContainer();
		final RulePanel rp = ruleContainer.createNewRule(this.getDocumentName(),this.getRuleName());
		this.setRulePanel(rp);
		this.setRuleName(rp.getRuleName());
		this.setInitRule(true);
	}

	/** {@inheritDoc} */
	@Override
	public void prefixAdded() {}

	/** {@inheritDoc} */
	@Override
	public void prefixModified(final String arg0, final String arg1) {}

	/** {@inheritDoc} */
	@Override
	public void prefixRemoved(final String arg0, final String arg1) {}

	/** {@inheritDoc} */
	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper arg0,
			final VisualGraph<Operator> arg1) {
		this.panel = new RuleOperatorPanel(arg1, arg0, this, true);
		return this.panel;
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperator() {
		return this.getSerializedOperator();
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperatorAndTree(final HashSet<Operator> arg0) {
		return this.getSerializedOperator();
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @param connectionsObject a {@link org.json.JSONObject} object.
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSON(final JSONObject connectionsObject) throws JSONException {
		final JSONObject saveObject = new JSONObject();
		final Point position = ((RuleOperatorPanel) this.panel).getPositionAndDimension().getFirst();
		saveObject.put("OP TYPE", this.getClass().getSimpleName());
		saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
		if(!(this.getPrecedingOperators() == null) && this.getPrecedingOperators().size() > 0) {
			saveObject.put("ANNOTATION", ((AnnotationOperator) this.getPrecedingOperators().get(0)).toJSON());
		}
		// --- handle connections - begin ---
		// --- handle connections - end ---
		return saveObject;
	}

	/** {@inheritDoc} */
	@Override
	public boolean variableInUse(final String arg0, final HashSet<Operator> arg1) {
		return false;
	}

	/**
	 * <p>Getter for the field <code>ruleName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRuleName() {
		return this.ruleName;
	}

	/**
	 * <p>Setter for the field <code>ruleName</code>.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void setRuleName(final String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * <p>Getter for the field <code>ruleLabelName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRuleLabelName() {
		return this.ruleLabelName;
	}

	/**
	 * <p>Setter for the field <code>ruleLabelName</code>.</p>
	 *
	 * @param ruleLabelName a {@link java.lang.String} object.
	 */
	public void setRuleLabelName(final String ruleLabelName) {
		this.ruleLabelName = ruleLabelName;
		System.out.println("New RuleLabel Name: "+ruleLabelName);
		this.panel.revalidate();
		this.panel.repaint();
	}

	/**
	 * <p>Getter for the field <code>serializedOperator</code>.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer getSerializedOperator() {
		return this.serializedOperator;
	}

	/**
	 * <p>Setter for the field <code>serializedOperator</code>.</p>
	 *
	 * @param serializedOperator a {@link java.lang.StringBuffer} object.
	 */
	public void setSerializedOperator(final StringBuffer serializedOperator) {
		this.serializedOperator = serializedOperator;
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperator(final boolean showErrors, final HashSet<Operator> visited, final Object data) {
		return true;
	}

	/**
	 * <p>getRuleOperatorPanel.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.RuleOperatorPanel} object.
	 */
	public RuleOperatorPanel getRuleOperatorPanel(){
		return (RuleOperatorPanel) this.panel;
	}

	/**
	 * <p>Setter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
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
	 * <p>isInitRule.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isInitRule() {
		return this.initRule;
	}

	/**
	 * <p>Setter for the field <code>initRule</code>.</p>
	 *
	 * @param initRule a boolean.
	 */
	public void setInitRule(final boolean initRule) {
		this.initRule = initRule;
	}

	/**
	 * <p>Getter for the field <code>documentName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDocumentName() {
		return this.documentName;
	}

	/**
	 * <p>Setter for the field <code>documentName</code>.</p>
	 *
	 * @param documentName a {@link java.lang.String} object.
	 */
	public void setDocumentName(final String documentName) {
		this.documentName = documentName;
	}

	/**
	 * <p>Setter for the field <code>rulePanel</code>.</p>
	 *
	 * @param rulePanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public void setRulePanel(final RulePanel rulePanel) {
		this.rulePanel = rulePanel;
	}

	/**
	 * <p>Getter for the field <code>rulePanel</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel getRulePanel(){
		return this.rulePanel;
	}

	// rif -> visualRif
	/**
	 * <p>setUnVisitedObject.</p>
	 *
	 * @param obj a {@link lupos.rif.model.Rule} object.
	 */
	public void setUnVisitedObject(final Rule obj) {
		this.object = obj;

	}

	/**
	 * <p>getUnVisitedObject.</p>
	 *
	 * @return a {@link lupos.rif.model.Rule} object.
	 */
	public Rule getUnVisitedObject(){
		return this.object;
	}

	/**
	 * <p>isInitRulePanel.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isInitRulePanel() {
		return this.initRulePanel;
	}

	/**
	 * <p>Setter for the field <code>initRulePanel</code>.</p>
	 *
	 * @param b a boolean.
	 */
	public void setInitRulePanel(final boolean b) {
		this.initRulePanel = b;
	}
}
