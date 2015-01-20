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

	public RuleOperator(){
	}

	public RuleOperator(final String name, final JSONObject loadObject) throws JSONException {
		this.documentName = loadObject.getString("DOCUMENTNAME");
		this.ruleName = name;
	}

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

	@Override
	public void prefixAdded() {}

	@Override
	public void prefixModified(final String arg0, final String arg1) {}

	@Override
	public void prefixRemoved(final String arg0, final String arg1) {}

	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper arg0,
			final VisualGraph<Operator> arg1) {
		this.panel = new RuleOperatorPanel(arg1, arg0, this, true);
		return this.panel;
	}

	@Override
	public StringBuffer serializeOperator() {
		return this.getSerializedOperator();
	}

	@Override
	public StringBuffer serializeOperatorAndTree(final HashSet<Operator> arg0) {
		return this.getSerializedOperator();
	}

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

	@Override
	public boolean variableInUse(final String arg0, final HashSet<Operator> arg1) {
		return false;
	}

	public String getRuleName() {
		return this.ruleName;
	}

	public void setRuleName(final String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleLabelName() {
		return this.ruleLabelName;
	}

	public void setRuleLabelName(final String ruleLabelName) {
		this.ruleLabelName = ruleLabelName;
		System.out.println("New RuleLabel Name: "+ruleLabelName);
		this.panel.revalidate();
		this.panel.repaint();
	}

	public StringBuffer getSerializedOperator() {
		return this.serializedOperator;
	}

	public void setSerializedOperator(final StringBuffer serializedOperator) {
		this.serializedOperator = serializedOperator;
	}

	@Override
	public boolean validateOperator(final boolean showErrors, final HashSet<Operator> visited, final Object data) {
		return true;
	}

	public RuleOperatorPanel getRuleOperatorPanel(){
		return (RuleOperatorPanel) this.panel;
	}

	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	public boolean isInitRule() {
		return this.initRule;
	}

	public void setInitRule(final boolean initRule) {
		this.initRule = initRule;
	}

	public String getDocumentName() {
		return this.documentName;
	}

	public void setDocumentName(final String documentName) {
		this.documentName = documentName;
	}

	public void setRulePanel(final RulePanel rulePanel) {
		this.rulePanel = rulePanel;
	}

	public RulePanel getRulePanel(){
		return this.rulePanel;
	}

	// rif -> visualRif
	public void setUnVisitedObject(final Rule obj) {
		this.object = obj;

	}

	public Rule getUnVisitedObject(){
		return this.object;
	}

	public boolean isInitRulePanel() {
		return this.initRulePanel;
	}

	public void setInitRulePanel(final boolean b) {
		this.initRulePanel = b;
	}
}
