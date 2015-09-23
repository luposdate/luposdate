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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class AssociationsContainer {
	private HashMap<String, LinkedList<String>> associations = new HashMap<String, LinkedList<String>>();

	private RuleEditor editor = null;

	/**
	 * <p>Constructor for AssociationsContainer.</p>
	 *
	 * @param editor a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor} object.
	 */
	public AssociationsContainer(RuleEditor editor) {
		this.editor = editor;
	}

	/**
	 * <p>add.</p>
	 *
	 * @param rulePackageName a {@link java.lang.String} object.
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void add(String rulePackageName, String ruleName) {
		if(!this.associations.containsKey(rulePackageName)) {
			this.associations.put(rulePackageName, new LinkedList<String>());
		}

		this.associations.get(rulePackageName).add(ruleName);

		this.editor.getTreePane().addAssociation(rulePackageName, ruleName);
	}

	/**
	 * <p>remove.</p>
	 *
	 * @param rulePackageName a {@link java.lang.String} object.
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void remove(String rulePackageName, String ruleName) {
		if(!this.associations.containsKey(rulePackageName)) {
			return;
		}

		this.associations.get(rulePackageName).remove(ruleName);

		if(this.associations.get(rulePackageName).size() == 0) {
			this.associations.remove(rulePackageName);
		}

		this.editor.getTreePane().removeAssociation(rulePackageName, ruleName);
	}

	/**
	 * <p>check.</p>
	 *
	 * @param rulePackageName a {@link java.lang.String} object.
	 * @param ruleName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean check(String rulePackageName, String ruleName) {
		boolean status = false;

		if(this.associations.containsKey(rulePackageName)) {
			if(this.associations.get(rulePackageName).contains(ruleName)) {
				status = true;
			}
		}

		return status;
	}

	/**
	 * <p>check.</p>
	 *
	 * @param rulePackageName a {@link java.lang.String} object.
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<String> check(String rulePackageName) {
		LinkedList<String> ret = this.associations.get(rulePackageName);

		if(ret == null) {
			return new LinkedList<String>();
		}
		else {
			return ret;
		}
	}

	/**
	 * <p>update.</p>
	 *
	 * @param e a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.TypeEnum} object.
	 * @param oldName a {@link java.lang.String} object.
	 * @param newName a {@link java.lang.String} object.
	 */
	public void update(TypeEnum e, String oldName, String newName) {
		if(e == TypeEnum.Rule) { // Rule name changed...
			for(LinkedList<String> rules : this.associations.values()) {
				if(rules.contains(oldName)) {
					rules.remove(oldName);
					rules.add(newName);
				}
			}
		}
		else { // RulePackage name changed...
			if(this.associations.containsKey(oldName)) {
				this.associations.put(newName, this.associations.get(oldName));
				this.associations.remove(oldName);
			}
		}
	}

	/**
	 * <p>getAssociationsToRulePackage.</p>
	 *
	 * @param rulePackageName a {@link java.lang.String} object.
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<String> getAssociationsToRulePackage(String rulePackageName) {
		if(this.associations.containsKey(rulePackageName)) {
			return this.associations.get(rulePackageName);
		}
		else {
			return new LinkedList<String>();
		}
	}

	/**
	 * <p>moveRule.</p>
	 *
	 * @param rulePackageName a {@link java.lang.String} object.
	 * @param index a int.
	 * @param difference a int.
	 */
	public void moveRule(String rulePackageName, int index, int difference) {
		String ruleName = this.associations.get(rulePackageName).remove(index);

		this.associations.get(rulePackageName).add(index + difference, ruleName);
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link java.util.HashMap} object.
	 */
	public HashMap<String, LinkedList<String>> toJSON() {
		System.out.println(":: saving associations...");

		return this.associations;
	}

	/**
	 * <p>fromJSON.</p>
	 *
	 * @param loadObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	@SuppressWarnings("unchecked")
	public void fromJSON(JSONObject loadObject) throws JSONException {
		System.out.println(":: loading associations...");

		this.associations.clear();

		if(loadObject == null) {
			return;
		}

		Iterator<String> keyIt = loadObject.keys();

		while(keyIt.hasNext()) {
			String rulePackageName = keyIt.next();

			JSONArray array = loadObject.getJSONArray(rulePackageName);

			if(array.length() > 0) {
				LinkedList<String> associations = new LinkedList<String>();

				for(int i = 0; i < array.length(); i += 1) {
					String ruleName = array.getString(i);

					associations.add(ruleName);
					this.editor.getTreePane().addAssociation(rulePackageName, ruleName, true);
				}

				this.associations.put(rulePackageName, associations);

			}
		}
	}
}
