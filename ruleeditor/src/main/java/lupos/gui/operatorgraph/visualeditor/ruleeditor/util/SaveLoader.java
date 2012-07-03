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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;

import org.json.JSONException;
import org.json.JSONObject;

public class SaveLoader {
	private RuleEditor editor = null;

	public SaveLoader(RuleEditor editor) {
		this.editor = editor;
	}

	public void save(String saveFileName) {
		System.out.println("Starting to save...");

		try {
			JSONObject saveObject = new JSONObject();
			saveObject.put("associations", this.editor.getAssociationsContainer().toJSON());
			saveObject.put("rule packages", this.editor.getRulePackageContainer().toJSON());
			saveObject.put("rules", this.editor.getRuleContainer().toJSON());

			File.writeFile(saveFileName, saveObject.toString(2));
		}
		catch(JSONException e) {
			e.printStackTrace();
		}

		System.out.println("DONE");
	}

	public void load(String loadFileName) {
		System.out.println("Starting to load...");

		try {
			JSONObject loadObject = new JSONObject(File.readFile(loadFileName));

			this.editor.getRulePackageContainer().fromJSON(loadObject.getJSONObject("rule packages"));
			this.editor.getRuleContainer().fromJSON(loadObject.getJSONObject("rules"));
			this.editor.getAssociationsContainer().fromJSON(loadObject.getJSONObject("associations"));
			
			for(lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RulePanel rulePanel: this.editor.getRuleContainer().getRules()){
				rulePanel.getRuleEditorPane().updateSize();
			}
		}
		catch(JSONException e) {
			e.printStackTrace();
		}

		System.out.println("DONE");
	}
}