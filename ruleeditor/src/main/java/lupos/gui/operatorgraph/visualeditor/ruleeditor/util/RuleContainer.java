/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePackagePanel;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePanel;
import lupos.misc.Triple;

import org.json.JSONException;
import org.json.JSONObject;

public class RuleContainer implements ITree {
	private RuleEditor editor = null;
	private RulePanel activeRule = null;
	private HashMap<String, RulePanel> rules = new HashMap<String, RulePanel>();

	public RuleContainer(RuleEditor editor) {
		this.editor = editor;
	}

	public RulePanel createNewRule() {
		String name = this.checkName("new rule", "new rule", 0);

		this.activeRule = new RulePanel(this.editor, name);
		this.rules.put(name, this.activeRule);

		return this.activeRule;
	}

	public String checkName(String basename, String newname, int index) {
		boolean exists = false;

		for(String ruleName : this.rules.keySet()) {
			if(newname.equalsIgnoreCase(ruleName)) {
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

	public void showRule(String ruleName) {
		this.activeRule = this.rules.get(ruleName);
		this.activeRule.updateRule();

		this.editor.setRightComponent(this.activeRule);
	}

	public void removeElement(String elem, TreeNode parentNode) {
		this.activeRule = null;
		this.rules.remove(elem);
		this.editor.setRightComponent(new JPanel());
	}

	public String getNameOfActiveElement() {
		return this.activeRule.toString();
	}

	public boolean nameChanged(TypeEnum e, String oldName, String newName) {
		String tmpName = this.checkName(newName, newName, 0);

		if(!tmpName.equalsIgnoreCase(newName)) {
			this.activeRule.setRuleName(oldName);

			return false;
		}

		tmpName = this.editor.getRulePackageContainer().checkName(newName, newName, 0);

		if(!tmpName.equalsIgnoreCase(newName)) {
			return false;
		}

		this.activeRule = this.rules.get(oldName);
		this.activeRule.setRuleName(newName);

		this.rules.remove(oldName);
		this.rules.put(newName, this.activeRule);

		this.activeRule.getAssociationsPanel().setElementName(newName);
		this.editor.getAssociationsContainer().update(TypeEnum.Rule, oldName, newName);
		this.activeRule.getAssociationsPanel().rebuildRulePackages(this.editor.getRulePackages());


		this.activeRule.getDocumentationPanel().setElementName(newName);

		for(RulePanel rule : this.rules.values()) {
			rule.getDocumentationPanel().updateRuleName(oldName, newName);
		}

		for(JTabbedPane rulePackage : this.editor.getRulePackages()) {
			((RulePackagePanel) rulePackage).getDocumentationPanel().updateRuleName(oldName, newName);
		}

		this.activeRule.getDocumentationPanel().rebuild(this.editor.getRules(), this.editor.getRulePackages());

		this.editor.getTreePane().updateTopComponent(oldName, newName);

		return true;
	}

	public void cancelModi() {
		if(this.activeRule != null) {
			this.activeRule.cancelModi();
		}
	}

	public LinkedList<RulePanel> getRules() {
		return new LinkedList<RulePanel>(this.rules.values());
	}

	public RulePanel getRule(String ruleName) {
		return this.rules.get(ruleName);
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();

		for(RulePanel rule : this.rules.values()) {
			System.out.print(":: validating rule '" + rule + "'... ");

			Triple<Boolean, HashMap<String, VariableContainer>, HashMap<String, VariableContainer>> resultTriple = rule.getRuleEditorPane().validateGraphs();

			if(!resultTriple.getFirst()) {
				System.err.println("FAILED");

				continue;
			}
			else {
				System.out.println("OK");
			}

			System.out.println(":: saving rule '" + rule + "'...");

			saveObject.put(rule.toString(), rule.toJSON());
		}

		return saveObject;
	}

	@SuppressWarnings("unchecked")
	public void fromJSON(JSONObject loadObject) throws JSONException {
		this.activeRule = null;
		this.rules.clear();
		this.editor.getTreePane().clearBottomComponent();

		Iterator<String> keyIt = loadObject.keys();

		while(keyIt.hasNext()) {
			String ruleName = keyIt.next();

			System.out.println(":: loading rule '" + ruleName + "'...");

			ruleName = this.checkName(ruleName, ruleName, 0);

			this.activeRule = new RulePanel(this.editor, ruleName, loadObject.getJSONObject(ruleName));
			this.rules.put(ruleName, this.activeRule);

			this.editor.getTreePane().addNewRule(this.activeRule);
			this.editor.setRightComponent(this.activeRule);
		}

		this.editor.enableMenus(true);
	}
}