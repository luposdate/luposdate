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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

import org.json.JSONException;
import org.json.JSONObject;


import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.DocumentPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel;


public class RuleContainer implements ITree {
	
	
	private VisualRifEditor visualRifEditor;
	private RulePanel activeRule;
	private HashMap<String,RulePanel> rules = new HashMap<String,RulePanel>();
	private LinkedList<RuleIdentifier> rulePanelList;


	
	
	
	
	//Constructor
	public RuleContainer(VisualRifEditor visualRifEditor){
		this.visualRifEditor = visualRifEditor;
		this.setRulePanelList(new LinkedList<RuleIdentifier>());
	}
	
	
	
	public void showRule(String ruleName) {
		this.activeRule = this.rules.get(ruleName);
//		this.activeRule.updateRule();

		this.visualRifEditor.setRightComponent(this.activeRule);
	}
	
	
	public RulePanel getRuleByName(String ruleName){
		return this.rules.get(ruleName);
	}
	
	
	public RulePanel createNewRule(String documentName){

		String name = this.checkName("Rule", "Rule", 0);
		
		this.activeRule = new RulePanel(this.visualRifEditor, name);
		this.rules.put(name, this.activeRule);
		visualRifEditor.getTreePane().addNewRule(this.activeRule,documentName);
		printRules();

		TreePath path = this.activeRule.getRulePath();
		visualRifEditor.getDocumentContainer().getActiveDocument().getListOfRules().add(name);
		visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleNameInVisualGraphsComponentArray(name, name);
//		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleLabelName(name, name);
		
		return this.activeRule;
	}
	
	public RulePanel createNewRule(String documentName, String ruleName){

		String name = this.checkName(ruleName, ruleName, 0);
		
		this.activeRule = new RulePanel(this.visualRifEditor, name);
		this.rules.put(name, this.activeRule);
		visualRifEditor.getTreePane().addNewRule(this.activeRule,documentName);
		printRules();

		TreePath path = this.activeRule.getRulePath();
		visualRifEditor.getDocumentContainer().getActiveDocument().getListOfRules().add(name);
		visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleNameInVisualGraphsComponentArray(name, name);
//		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleLabelName(name, name);
		
		return this.activeRule;
	}
	
	
	public void deleteRule(String ruleName){
		//delete visual Component on Canvas
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().deleteRule(ruleName);
		visualRifEditor.getTreePane().remove(ruleName,visualRifEditor.getDocumentContainer().getNameOfActiveElement());
		this.rules.remove(ruleName);
		
		// delete RulePanel
		for (int i = 0; i < this.rulePanelList.size(); i++) {
			if (this.rulePanelList.get(i).getRuleName().equals(ruleName)){
				this.rulePanelList.remove(i);
				}
		}
		
		// set RuleCnt
//		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().setRulesCnt(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getRulesCnt()-1);
		
		
	}
	

	public void deleteAllRules(String documentName){
		LinkedList<String> blackList = new LinkedList<String>();
		
		for (Entry<String, RulePanel> entry : this.rules.entrySet()) {
			visualRifEditor.getTreePane().remove(entry.getKey(),visualRifEditor.getDocumentContainer().getNameOfActiveElement());
		
			
			// delete RulePanel
			for (int i = 0; i < this.rulePanelList.size(); i++) {
				if (rulePanelList.get(i).getDocumentName().equals(visualRifEditor.getDocumentContainer().getNameOfActiveElement())){
				if (this.rulePanelList.get(i).getRuleName().equals(entry.getKey())){
					this.rulePanelList.remove(i);
					blackList.add(entry.getKey());
					}
			}
			}
		}
		
		for (int i = 0; i < blackList.size(); i++) {
			this.rules.remove(blackList.get(i));
		}
		
//		this.rules = new HashMap<String,RulePanel>();
	}
	
	
	/**
	 * Checks whether the name of 
	 * the new rule is already used.
	 * @param basename
	 * @param newname
	 * @param index
	 * @return a new auto-generated name for the new rule
	 */
	public String checkName(String basename, String newname, int index) {
		boolean exists = false;

		for(String documentName : this.rules.keySet()) {
			if(newname.equalsIgnoreCase(documentName)) {
				newname = basename + index;
				index += 1;
				exists = true;

				break;
			}
		}

		if(exists) {
			newname = this.checkName(basename, newname, index);
		}

		return newname;
	}
	

	
	/**
	 * Loads the RulePanel and shows it on the right side of the GUI
	 * @param ruleName
	 */
	public void showDocument(String ruleName){
		
		this.activeRule = this.rules.get(ruleName);
//		this.activeDocument.updateDocument();
		
		this.visualRifEditor.setRightComponent(this.activeRule);
		
	}
	


	@SuppressWarnings("unchecked")
	public void removeElement(String elem, TreeNode parentNode) {
		RulePanel ret = this.rules.remove(elem);

//		if(ret == null) {
//			this.visualRifEditor.getAssociationsContainer().remove(parentNode.getUserObject().toString(), elem);
//		}
//		else {
//			LinkedList<String> rules = this.editor.getAssociationsContainer().check(this.activeRulePackage.toString());

//			for(String documentName : (LinkedList<String>) documents.clone()) {
//				this.visualRifEditor.getAssociationsContainer().remove(elem, this.editor.getRuleContainer().getRule(ruleName).toString());
//			}

			this.activeRule = null;
//	}

		this.visualRifEditor.setRightComponent(new JPanel());
	}


	public boolean nameChanged(TypeEnum e, String oldName, String newName) {
		String tmpName = this.checkName(newName, newName, 0);

		if(!tmpName.equalsIgnoreCase(newName)) {
			this.activeRule.setRuleName(oldName);

			return false;
		}

		tmpName = this.visualRifEditor.getDocumentContainer().checkName(newName, newName, 0);

		if(!tmpName.equalsIgnoreCase(newName)) {
			return false;
		}

		this.activeRule = this.rules.get(oldName);
		this.activeRule.setRuleName(newName);

		this.rules.remove(oldName);
		this.rules.put(newName, this.activeRule);
		
		this.updateNameInRulePanelList(oldName, newName);
		
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleNameInVisualGraphsComponentArray(oldName, newName);
		
//		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleLabelName(oldName, newName);

//		this.activeRule.getAssociationsPanel().setElementName(newName);
//		this.editor.getAssociationsContainer().update(TypeEnum.Rule, oldName, newName);
//		this.activeRule.getAssociationsPanel().rebuild(TypeEnum.RulePackage, this.editor.getRulePackages());


//		this.activeRule.getDocumentationPanel().setElementName(newName);

//		for(RulePanel rule : this.rules.values()) {
//			rule.getDocumentationPanel().updateRuleName(oldName, newName);
//		}

//		for(JTabbedPane rulePackage : this.editor.getRulePackages()) {
//			((RulePackagePanel) rulePackage).getDocumentationPanel().updateRuleName(oldName, newName);
//		}
//
//		this.activeRule.getDocumentationPanel().rebuild(this.editor.getRules(), this.editor.getRulePackages());

		this.visualRifEditor.getTreePane().updateTopComponent(oldName, newName);

//		this.setNewRuleNameForCanvas(oldName, newName);
//		visualRifEditor.getDocumentContainer().getActiveDocument().updateRuleName(oldName, newName);
		

		
		return true;
	}

//	public void setNewRuleNameForCanvas(String oldName,String newName){
//	visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleName(oldName, newName);
//	}


	@Override
	public String getNameOfActiveElement() {
		// TODO Auto-generated method stub
		return this.activeRule.toString();
	}

	private void updateNameInRulePanelList(String oldName, String newName){
		
		for(int i = 0 ; i < this.rulePanelList.size() ; i++){
			if(this.rulePanelList.get(i).getRuleName().equals(oldName)){
				this.rulePanelList.get(i).setRuleName(newName);
			}
		}
		
	}

	public void cancelModi() {
		if(this.activeRule != null) {
			this.activeRule.cancelModi();
		}
	}
	
	/**
	 * Test
	 */
	private void printRules(){
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		for(String name : this.rules.keySet()) {
			System.out.println(name);
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}
	
	
	/* *************** **
	 * Getter + Setter **
	 * *************** */
	
	
	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}

	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	public HashMap<String,RulePanel> getRules() {
		return rules;
	}

	public void setRules(HashMap<String,RulePanel> rules) {
		this.rules = rules;
	}

	public RulePanel getActiveRule() {
		return activeRule;
	}

	public void setActiveRule(RulePanel activeRule) {
		this.activeRule = activeRule;
	}



	public LinkedList<RuleIdentifier> getRulePanelList() {
		return rulePanelList;
	}



	public void setRulePanelList(LinkedList<RuleIdentifier> rulePanelList) {
		this.rulePanelList = rulePanelList;
	}



	
	
	public String getDocumentNameByRuleName(String ruleName){
		for (int i = 0; i < this.rulePanelList.size(); i++) {
			if(this.rulePanelList.get(i).getRuleName().equals(ruleName)){
				return this.rulePanelList.get(i).getDocumentName();
			}
				
		}
		return "";
		
	}
	
	
	public void fromJSON(JSONObject jsonObject)  throws JSONException {

		@SuppressWarnings("unchecked")
		Iterator<String> keyIt = jsonObject.keys();
		

		
		while(keyIt.hasNext()) {
			String ruleName = keyIt.next();
			JSONObject ruleObj = jsonObject.getJSONObject(ruleName);
			String documentName = ruleObj.getString("DOCUMENTNAME");
		
			
			this.visualRifEditor.getDocumentContainer().getDocuments().get(documentName).getDocumentEditorPane().getDocumentGraph().ruleFromJSON(ruleObj, documentName, ruleName);
			
			
			
		}
	}



	public JSONObject toJSON() {
		JSONObject saveObject = new JSONObject();
		
		for(RulePanel rule : this.rules.values()) {

			try {
				saveObject.put(rule.toString(), rule.toJSON(this.getDocumentNameByRuleName(rule.getRuleName())));
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}
		return saveObject;



	}




}
