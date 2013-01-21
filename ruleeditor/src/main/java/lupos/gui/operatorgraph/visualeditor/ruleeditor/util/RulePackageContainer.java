/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import org.json.JSONException;
import org.json.JSONObject;

public class RulePackageContainer implements ITree {
	private RuleEditor editor;
	private RulePackagePanel activeRulePackage = null;
	private HashMap<String, RulePackagePanel> rulePackages = new HashMap<String, RulePackagePanel>();

	public RulePackageContainer(RuleEditor editor) {
		this.editor = editor;
	}

	public RulePackagePanel createNewRulePackage() {
		String name = this.checkName("new rule package", "new rule package", 0);

		this.activeRulePackage = new RulePackagePanel(this.editor, name);
		this.rulePackages.put(name, this.activeRulePackage);

		return this.activeRulePackage;
	}

	public String checkName(String basename, String newname, int index) {
		boolean exists = false;

		for(String ruleName : this.rulePackages.keySet()) {
			if(newname.equals(ruleName)) {
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

	public void showRulePackage(String rulePackageName) {
		this.activeRulePackage = this.rulePackages.get(rulePackageName);
		this.activeRulePackage.updateRulePackage();

		this.editor.setRightComponent(this.activeRulePackage);
	}

	@SuppressWarnings("unchecked")
	public void removeElement(String elem, TreeNode parentNode) {
		RulePackagePanel ret = this.rulePackages.remove(elem);

		if(ret == null) {
			this.editor.getAssociationsContainer().remove(parentNode.getUserObject().toString(), elem);
		}
		else {
			LinkedList<String> rules = this.editor.getAssociationsContainer().check(this.activeRulePackage.toString());

			for(String ruleName : (LinkedList<String>) rules.clone()) {
				this.editor.getAssociationsContainer().remove(elem, this.editor.getRuleContainer().getRule(ruleName).toString());
			}

			this.activeRulePackage = null;
		}

		this.editor.setRightComponent(new JPanel());
	}

	public boolean nameChanged(TypeEnum e, String oldName, String newName) {
		if(e == TypeEnum.Rule) {
			return this.editor.getRuleContainer().nameChanged(e, oldName, newName);
		}
		else {
			String tmpName = this.checkName(newName, newName, 0);

			if(!tmpName.equalsIgnoreCase(newName)) {
				return false;
			}

			tmpName = this.editor.getRuleContainer().checkName(newName, newName, 0);

			if(!tmpName.equalsIgnoreCase(newName)) {
				return false;
			}

			this.activeRulePackage = this.rulePackages.get(oldName);
			this.activeRulePackage.setRulePackageName(newName);

			this.rulePackages.remove(oldName);
			this.rulePackages.put(newName, this.activeRulePackage);

			this.activeRulePackage.getAssociationsPanel().setElementName(newName);
			this.editor.getAssociationsContainer().update(TypeEnum.RulePackage, oldName, newName);
			this.activeRulePackage.getAssociationsPanel().rebuildRules(this.editor.getRules());


			this.activeRulePackage.getDocumentationPanel().setElementName(newName);

			for(JTabbedPane rule : this.editor.getRules()) {
				((RulePanel) rule).getDocumentationPanel().updateRulePackageName(oldName, newName);
			}

			for(RulePackagePanel rulePackage : this.rulePackages.values()) {
				rulePackage.getDocumentationPanel().updateRulePackageName(oldName, newName);
			}

			this.activeRulePackage.getDocumentationPanel().rebuild(this.editor.getRules(), this.editor.getRulePackages());



			return true;
		}
	}

	public String getNameOfActiveElement() {
		return this.activeRulePackage.toString();
	}

	public LinkedList<RulePackagePanel> getRulePackages() {
		return new LinkedList<RulePackagePanel>(this.rulePackages.values());
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();

		for(RulePackagePanel rulePackage : this.rulePackages.values()) {
			System.out.println(":: saving rule package '" + rulePackage + "'...");

			saveObject.put(rulePackage.toString(), rulePackage.toJSON());
		}

		return saveObject;
	}

	@SuppressWarnings("unchecked")
	public void fromJSON(JSONObject loadObject) throws JSONException {
		this.activeRulePackage = null;
		this.rulePackages.clear();
		this.editor.getTreePane().clearTopComponent();

		Iterator<String> keyIt = loadObject.keys();

		while(keyIt.hasNext()) {
			String rulePackageName = keyIt.next();

			System.out.println(":: loading rule package '" + rulePackageName + "'...");

			rulePackageName = this.checkName(rulePackageName, rulePackageName, 0);

			this.activeRulePackage = new RulePackagePanel(this.editor, rulePackageName, loadObject.getJSONObject(rulePackageName));
			this.rulePackages.put(rulePackageName, this.activeRulePackage);

			this.editor.getTreePane().addNewRulePackage(this.activeRulePackage);
		}

		this.editor.enableMenus(true);
	}
}