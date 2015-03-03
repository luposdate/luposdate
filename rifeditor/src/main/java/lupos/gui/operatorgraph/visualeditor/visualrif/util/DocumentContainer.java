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
import java.util.Map.Entry;

import javax.swing.JPanel;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.DocumentPanel;

import org.json.JSONException;
import org.json.JSONObject;

public class DocumentContainer implements ITree {




	private final VisualRifEditor visualRifEditor;
	private DocumentPanel activeDocument;
	private HashMap<String,DocumentPanel> documents = new HashMap<String,DocumentPanel>();
	private String jsonDocumentName;



	// Constructor
	public DocumentContainer(final VisualRifEditor visualRifEditor) {

		this.visualRifEditor = visualRifEditor;
	}


	/**
	 * Creates an new {@link DocumentPanel} and stores it in a HashMap
	 * see checkName
	 * @return An active {@link DocumentPanel}
	 */
	public DocumentPanel createNewDocument(){

		final String name = this.checkName("Document", "Document", 0);

		this.activeDocument = new DocumentPanel(this.visualRifEditor, name);
		this.documents.put(name, this.activeDocument);


		return this.activeDocument;
	}


	/**
	 * Checks whether the name of
	 * the new document is already used.
	 * @param basename
	 * @param newname
	 * @param index
	 * @return a new auto-generated name for the new document
	 */
	public String checkName(final String basename, String newname, int index) {
		boolean exists = false;

		for(final String documentName : this.documents.keySet()) {
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
	 * Loads the DocumentPanel and shows it on the right side of the GUI
	 * @param documentName
	 */
	public void showDocument(final String documentName){

		this.activeDocument = this.documents.get(documentName);
//		this.activeDocument.updateDocument();

		this.visualRifEditor.setRightComponent(this.activeDocument);

	}


	public void deleteDocument(final String documentName){
		// delete all rules in the document
		for (int i = 0; i < this.visualRifEditor.getRuleContainer().getRulePanelList().size(); i++) {
			for (int j = 0; j < this.getDocumentByName(documentName).getListOfRules().size(); j++) {
				if(this.visualRifEditor.getRuleContainer().getRulePanelList().get(i).getRuleName().equals(this.getDocumentByName(documentName).getListOfRules().get(j))){
					System.out.println(this.visualRifEditor.getRuleContainer().getRulePanelList().get(i).getRuleName());
					this.visualRifEditor.getRuleContainer().deleteRule(this.visualRifEditor.getRuleContainer().getRulePanelList().get(i).getRuleName());
				}
			}
		}


	}


	@Override
	@SuppressWarnings("unchecked")
	public void removeElement(final String elem, final TreeNode parentNode) {
		final DocumentPanel ret = this.documents.remove(elem);

//		if(ret == null) {
//			this.visualRifEditor.getAssociationsContainer().remove(parentNode.getUserObject().toString(), elem);
//		}
//		else {
//			LinkedList<String> rules = this.editor.getAssociationsContainer().check(this.activeRulePackage.toString());

//			for(String documentName : (LinkedList<String>) documents.clone()) {
//				this.visualRifEditor.getAssociationsContainer().remove(elem, this.editor.getRuleContainer().getRule(ruleName).toString());
//			}

			this.activeDocument = null;
//	}

		this.visualRifEditor.setRightComponent(new JPanel());
	}


	@Override
	public boolean nameChanged(final TypeEnum e, final String oldName, final String newName) {

		if( e == TypeEnum.Rule ) {
			return this.visualRifEditor.getRuleContainer().nameChanged(e, oldName, newName);
		}
//		else
//			if( e == TypeEnum.Group )
//				return this.visualRifEditor.getGroupContainer().nameChanged(e, oldName, newName);
		else {
			String tmpName = this.checkName(newName, newName, 0);

			if(!tmpName.equalsIgnoreCase(newName)) {
				return false;
			}

			tmpName = this.visualRifEditor.getDocumentContainer().checkName(newName, newName, 0);

			if(!tmpName.equalsIgnoreCase(newName)) {
				return false;
			}

			this.activeDocument = this.documents.get(oldName);
			this.activeDocument.setDocumentName(newName);

			this.documents.remove(oldName);
			this.documents.put(newName, this.activeDocument);

//			this.activeDocument.getAssociationsPanel().setElementName(newName);
//			this.visualRifEditor.getAssociationsContainer().update(TypeEnum.RulePackage, oldName, newName);
//			this.activeRulePackage.getAssociationsPanel().rebuild(TypeEnum.Rule, this.editor.getRules());


//			this.activeDocument.getDocumentationPanel().setElementName(newName);

//			for(JTabbedPane document : this.visualRifEditor.getDocuments()) {
//				((DocumentPanel) document).getDocumentationPanel().updateRulePackageName(oldName, newName);
//			}

//			for(DocumentPanel rulePackage : this.rulePackages.values()) {
//				rulePackage.getDocumentationPanel().updateRulePackageName(oldName, newName);
//			}
//
//			this.activeRulePackage.getDocumentationPanel().rebuild(this.editor.getRules(), this.editor.getRulePackages());



			return true;
		}
	}


	/*
	 * Load + Save
	 */

	public JSONObject toJSON() throws JSONException {
		final JSONObject saveObject = new JSONObject();

		for(final DocumentPanel document : this.documents.values()) {


			saveObject.put(document.toString(), document.toJSON());
		}

		return saveObject;
	}

	@SuppressWarnings("unchecked")
	public void fromJSON(final JSONObject loadObject) throws JSONException {

		final Iterator<String> keyIt = loadObject.keys();


		// while there are documents
		while(keyIt.hasNext()) {
			this.jsonDocumentName = keyIt.next();

			this.jsonDocumentName = this.checkName(this.jsonDocumentName, this.jsonDocumentName, 0);

			this.activeDocument = new DocumentPanel(this.visualRifEditor, this.jsonDocumentName, loadObject.getJSONObject(this.jsonDocumentName));
			this.documents.put(this.jsonDocumentName, this.activeDocument);

//			this.editor.getTreePane().addNewRulePackage(this.activeRulePackage);
			this.visualRifEditor.getTreePane().addNewDocument(this.activeDocument);
		}

		this.visualRifEditor.enableMenus(true);

}



	/* *************** **
	 * Getter + Setter **
	 * *************** */

	@Override
	public String getNameOfActiveElement() {

		return this.activeDocument.toString();
	}



	public  DocumentPanel getDocumentByName(final String documentName){

		DocumentPanel tmp = null;

		for (final Entry<String, DocumentPanel> panel : this.documents.entrySet()) {

			if(panel.getKey().equals(documentName)){
				tmp = panel.getValue();
				return tmp;
			}
		}
		return tmp;
	}

	public DocumentPanel getActiveDocument() {
		return this.activeDocument;
	}

	public void setActiveDocument(final DocumentPanel activeDocument) {
		this.activeDocument = activeDocument;
	}

	public HashMap<String, DocumentPanel> getDocuments() {
		return this.documents;
	}

	public void setDocuments(final HashMap<String, DocumentPanel> documents) {
		this.documents = documents;
	}






}
