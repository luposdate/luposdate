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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel;

import org.json.JSONException;
import org.json.JSONObject;
public class RuleContainer implements ITree {

	private VisualRifEditor visualRifEditor;
	private RulePanel activeRule;
	private HashMap<String,RulePanel> rules = new HashMap<String,RulePanel>();
	private LinkedList<RuleIdentifier> rulePanelList;

	//Constructor
	/**
	 * <p>Constructor for RuleContainer.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public RuleContainer(final VisualRifEditor visualRifEditor){
		this.visualRifEditor = visualRifEditor;
		this.setRulePanelList(new LinkedList<RuleIdentifier>());
	}

	/**
	 * <p>showRule.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void showRule(final String ruleName) {
		this.activeRule = this.rules.get(ruleName);
		this.visualRifEditor.setRightComponent(this.activeRule);
	}

	/**
	 * <p>getRuleByName.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel getRuleByName(final String ruleName){
		return this.rules.get(ruleName);
	}

	/**
	 * <p>createNewRule.</p>
	 *
	 * @param documentName a {@link java.lang.String} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel createNewRule(final String documentName){
		final String name = this.checkName("Rule", "Rule", 0);
		this.activeRule = new RulePanel(this.visualRifEditor, name);
		this.rules.put(name, this.activeRule);
		this.visualRifEditor.getTreePane().addNewRule(this.activeRule,documentName);
		this.printRules();
		final TreePath path = this.activeRule.getRulePath();
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getListOfRules().add(name);
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleNameInVisualGraphsComponentArray(name, name);
		return this.activeRule;
	}

	/**
	 * <p>createNewRule.</p>
	 *
	 * @param documentName a {@link java.lang.String} object.
	 * @param ruleName a {@link java.lang.String} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel createNewRule(final String documentName, final String ruleName){
		final String name = this.checkName(ruleName, ruleName, 0);
		this.activeRule = new RulePanel(this.visualRifEditor, name);
		this.rules.put(name, this.activeRule);
		this.visualRifEditor.getTreePane().addNewRule(this.activeRule,documentName);
		this.printRules();
		final TreePath path = this.activeRule.getRulePath();
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getListOfRules().add(name);
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().updateRuleNameInVisualGraphsComponentArray(name, name);
		return this.activeRule;
	}

	/**
	 * <p>deleteRule.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void deleteRule(final String ruleName){
		//delete visual Component on Canvas
		this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().deleteRule(ruleName);
		this.visualRifEditor.getTreePane().remove(ruleName,this.visualRifEditor.getDocumentContainer().getNameOfActiveElement());
		this.rules.remove(ruleName);
		// delete RulePanel
		for (int i = 0; i < this.rulePanelList.size(); i++) {
			if (this.rulePanelList.get(i).getRuleName().equals(ruleName)){
				this.rulePanelList.remove(i);
			}
		}
	}

	/**
	 * <p>deleteAllRules.</p>
	 *
	 * @param documentName a {@link java.lang.String} object.
	 */
	public void deleteAllRules(final String documentName){
		final LinkedList<String> blackList = new LinkedList<String>();
		for (final Entry<String, RulePanel> entry : this.rules.entrySet()) {
			this.visualRifEditor.getTreePane().remove(entry.getKey(),this.visualRifEditor.getDocumentContainer().getNameOfActiveElement());
			// delete RulePanel
			for (int i = 0; i < this.rulePanelList.size(); i++) {
				if (this.rulePanelList.get(i).getDocumentName().equals(this.visualRifEditor.getDocumentContainer().getNameOfActiveElement())){
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
	}

	/**
	 * Checks whether the name of
	 * the new rule is already used.
	 *
	 * @param basename a {@link java.lang.String} object.
	 * @param newname a {@link java.lang.String} object.
	 * @param index a int.
	 * @return a new auto-generated name for the new rule
	 */
	public String checkName(final String basename, String newname, int index) {
		boolean exists = false;
		for(final String documentName : this.rules.keySet()) {
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
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void showDocument(final String ruleName){
		this.activeRule = this.rules.get(ruleName);
		this.visualRifEditor.setRightComponent(this.activeRule);

	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void removeElement(final String elem, final TreeNode parentNode) {
		final RulePanel ret = this.rules.remove(elem);
		this.activeRule = null;
		this.visualRifEditor.setRightComponent(new JPanel());
	}


	/** {@inheritDoc} */
	@Override
	public boolean nameChanged(final TypeEnum e, final String oldName, final String newName) {
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
		this.visualRifEditor.getTreePane().updateTopComponent(oldName, newName);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String getNameOfActiveElement() {
		return this.activeRule.toString();
	}

	private void updateNameInRulePanelList(final String oldName, final String newName){
		for(int i = 0 ; i < this.rulePanelList.size() ; i++){
			if(this.rulePanelList.get(i).getRuleName().equals(oldName)){
				this.rulePanelList.get(i).setRuleName(newName);
			}
		}
	}

	/**
	 * <p>cancelModi.</p>
	 */
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
		for(final String name : this.rules.keySet()) {
			System.out.println(name);
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */
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
	}

	/**
	 * <p>Getter for the field <code>rules</code>.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<String,RulePanel> getRules() {
		return this.rules;
	}

	/**
	 * <p>Setter for the field <code>rules</code>.</p>
	 *
	 * @param rules a {@link java.util.HashMap} object.
	 */
	public void setRules(final HashMap<String,RulePanel> rules) {
		this.rules = rules;
	}

	/**
	 * <p>Getter for the field <code>activeRule</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel getActiveRule() {
		return this.activeRule;
	}

	/**
	 * <p>Setter for the field <code>activeRule</code>.</p>
	 *
	 * @param activeRule a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public void setActiveRule(final RulePanel activeRule) {
		this.activeRule = activeRule;
	}

	/**
	 * <p>Getter for the field <code>rulePanelList</code>.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<RuleIdentifier> getRulePanelList() {
		return this.rulePanelList;
	}

	/**
	 * <p>Setter for the field <code>rulePanelList</code>.</p>
	 *
	 * @param rulePanelList a {@link java.util.LinkedList} object.
	 */
	public void setRulePanelList(final LinkedList<RuleIdentifier> rulePanelList) {
		this.rulePanelList = rulePanelList;
	}

	/**
	 * <p>getDocumentNameByRuleName.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getDocumentNameByRuleName(final String ruleName){
		for (int i = 0; i < this.rulePanelList.size(); i++) {
			if(this.rulePanelList.get(i).getRuleName().equals(ruleName)){
				return this.rulePanelList.get(i).getDocumentName();
			}

		}
		return "";
	}

	/**
	 * <p>fromJSON.</p>
	 *
	 * @param jsonObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public void fromJSON(final JSONObject jsonObject)  throws JSONException {
		@SuppressWarnings("unchecked")
		final
		Iterator<String> keyIt = jsonObject.keys();
		while(keyIt.hasNext()) {
			final String ruleName = keyIt.next();
			final JSONObject ruleObj = jsonObject.getJSONObject(ruleName);
			final String documentName = ruleObj.getString("DOCUMENTNAME");
			this.visualRifEditor.getDocumentContainer().getDocuments().get(documentName).getDocumentEditorPane().getDocumentGraph().ruleFromJSON(ruleObj, documentName, ruleName);
		}
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 */
	public JSONObject toJSON() {
		final JSONObject saveObject = new JSONObject();
		for(final RulePanel rule : this.rules.values()) {
			try {
				saveObject.put(rule.toString(), rule.toJSON(this.getDocumentNameByRuleName(rule.getRuleName())));

			} catch (final JSONException e) {
				e.printStackTrace();
			}
		}
		return saveObject;
	}
}
