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
package lupos.gui.operatorgraph.visualeditor.visualrif.operators;

import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.RuleOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleIdentifier;
import lupos.rif.model.Rule;


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
//	private Object visitedObjectLeft;
//	private Object visitedObjectRight;

	public RuleOperator(){
	}
		
	public RuleOperator(String name, JSONObject loadObject) throws JSONException {
		this.documentName = loadObject.getString("DOCUMENTNAME");
		this.ruleName = name;
	}
	

	public void initRule() {
		
		if (this.getDocumentName() == null) this.setDocumentName(this.visualRifEditor.getDocumentContainer().getNameOfActiveElement());
		
		RuleContainer ruleContainer = this.visualRifEditor.getRuleContainer();
		
		RulePanel rp = ruleContainer.createNewRule(this.getDocumentName(),this.getRuleName());
		
		this.setRulePanel(rp);
	
		this.setRuleName(rp.getRuleName());
		
		this.setInitRule(true);
		
//		this.getVisualRifEditor().getRuleContainer().getRulePanelList().add(new RuleIdentifier(rulePanel.getRuleName(), rulePanel,  this, rulePanel.getRulePath(),this.getDocumentName()));
//		
//		this.addComponentListener(new ComponentAdapter() {
//			public void componentResized(ComponentEvent e) {
//				
//				updateSize();
//			}
//
//		});
		
		
		
	}




	@Override
	public void prefixAdded() {}

	@Override
	public void prefixModified(String arg0, String arg1) {}

	@Override
	public void prefixRemoved(String arg0, String arg1) {}

	@Override
	public AbstractGuiComponent<Operator> draw(GraphWrapper arg0,
			VisualGraph<Operator> arg1) {

		this.panel = new RuleOperatorPanel(arg1, arg0, this, true);
		return this.panel;
	}

	@Override
	public StringBuffer serializeOperator() {
		return this.getSerializedOperator();
	}

	@Override
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> arg0) {

		return this.getSerializedOperator();
	}

	public JSONObject toJSON(JSONObject connectionsObject) throws JSONException {
		JSONObject saveObject = new JSONObject();

		Point position = ((RuleOperatorPanel) this.panel).getPositionAndDimension().getFirst();

		saveObject.put("OP TYPE", this.getClass().getSimpleName());

		saveObject.put("POSITION", new double[]{position.getX(), position.getY()});

		if(!(this.getPrecedingOperators() == null) && this.getPrecedingOperators().size() > 0)
		saveObject.put("ANNOTATION", ((AnnotationOperator) this.getPrecedingOperators().get(0)).toJSON());
		// --- handle connections - begin ---
//		JSONArray connectionsArray = new JSONArray();

//		for(Operator child : this.annotationLabels.keySet()) {
//			AbstractRuleOperator childOp = (AbstractRuleOperator) child;
//			AnnotationPanel ap = (AnnotationPanel) this.annotationLabels.get(child);
//
//			JSONObject childConnectionObject = new JSONObject();
//			childConnectionObject.put("to", childOp.getName());
//			childConnectionObject.put("active", ap.isActive());
//			childConnectionObject.put("id", ap.getOpID());
//			childConnectionObject.put("id label", ap.getOpLabel());
//			childConnectionObject.put("mode", ap.getMode().name());
//
//			connectionsArray.put(childConnectionObject);
//		}
//
//		if(connectionsArray.length() > 0) {
//			connectionsObject.put(this.getName(), connectionsArray);
//		}
		// --- handle connections - end ---

		return saveObject;
	}

	
	
	@Override
	public boolean variableInUse(String arg0, HashSet<Operator> arg1) {
		return false;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	
	public String getRuleLabelName() {
		return ruleLabelName;
	}

	public void setRuleLabelName(String ruleLabelName) {
		this.ruleLabelName = ruleLabelName;
		System.out.println("New RuleLabel Name: "+ruleLabelName);
		this.panel.revalidate();
		this.panel.repaint();
	}

	
	public StringBuffer getSerializedOperator() {
		return serializedOperator;
	}

	public void setSerializedOperator(StringBuffer serializedOperator) {
		this.serializedOperator = serializedOperator;
	}

	public boolean validateOperator(boolean showErrors, HashSet<Operator> visited, Object data) {
	

		return true;
	}
	
	public RuleOperatorPanel getRuleOperatorPanel(){
		return (RuleOperatorPanel) this.panel;
	}
	

	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
		
	}

	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}



	public boolean isInitRule() {
		return initRule;
	}



	public void setInitRule(boolean initRule) {
		this.initRule = initRule;
	}



	public String getDocumentName() {
		return documentName;
	}



	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}



	
	public void setRulePanel(RulePanel rulePanel) {
		this.rulePanel = rulePanel;
		
	}
	
	public RulePanel getRulePanel(){
		return this.rulePanel;
	}



	
	// rif -> visualRif
	public void setUnVisitedObject(Rule obj) {
		this.object = obj;
		
	}

	public Rule getUnVisitedObject(){
		return this.object;
	}



	public boolean isInitRulePanel() {
		// TODO Auto-generated method stub
		return this.initRulePanel;
	}



	public void setInitRulePanel(boolean b) {
		this.initRulePanel = b;
		
	}


	
//	public void setVisitedObjectLeft(Object left) {
//		this.visitedObjectLeft = left;
//		
//	}
//
//	public void setVisitedObjectRight(Object right) {
//		this.visitedObjectRight = right;
//		
//	}
//	
//	public Object getVisitedObejectLeft(){
//		return this.visitedObjectLeft;
//	}
//	
//	public Object getVisitedObjectRight(){
//		return this.visitedObjectRight;
//	}

	
}
