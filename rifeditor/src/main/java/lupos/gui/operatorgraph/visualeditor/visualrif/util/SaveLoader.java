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


import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map.Entry;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.DocumentPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel;

import org.json.JSONException;
import org.json.JSONObject;
public class SaveLoader{
	private VisualRifEditor visualRifEditor = null;

	/**
	 * <p>Constructor for SaveLoader.</p>
	 *
	 * @param editor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public SaveLoader(VisualRifEditor editor) {
		this.visualRifEditor = editor;
	}

	/**
	 * <p>save.</p>
	 *
	 * @param saveFileName a {@link java.lang.String} object.
	 */
	public void save(String saveFileName) {
		System.out.println("Starting to save...");

		try {
			JSONObject saveObject = new JSONObject();
			saveObject.put("DOCUMENTS", this.visualRifEditor.getDocumentContainer().toJSON());
			saveObject.put("RULES", this.visualRifEditor.getRuleContainer().toJSON());


			File.writeFile(saveFileName, saveObject.toString(2));
		}
		catch(JSONException e) {
			e.printStackTrace();
		}

		System.out.println("DONE");
	}

	/**
	 * <p>load.</p>
	 *
	 * @param loadFileName a {@link java.lang.String} object.
	 */
	public void load(String loadFileName) {
	
		
		this.visualRifEditor.getDocumentContainer().setActiveDocument(null);
		this.visualRifEditor.getDocumentContainer().getDocuments().clear();

		
		this.visualRifEditor.getRuleContainer().setActiveRule(null);
		this.visualRifEditor.getRuleContainer().getRules().clear();
		
		this.visualRifEditor.getTreePane().clearTopComponent();
		
		System.out.println("Starting to load...");
		
		try {
			JSONObject loadObject = new JSONObject(File.readFile(loadFileName));

			this.visualRifEditor.getDocumentContainer().fromJSON( loadObject.getJSONObject("DOCUMENTS") );
	
			this.visualRifEditor.getRuleContainer().fromJSON( loadObject.getJSONObject("RULES") );

			
			for (Entry<String, DocumentPanel> entry : this.visualRifEditor.getDocumentContainer().getDocuments().entrySet()) {
				entry.getValue().getDocumentEditorPane().updateSize();
			} 
			
			for (Entry<String,RulePanel> entry : this.visualRifEditor.getRuleContainer().getRules().entrySet()) {
				entry.getValue().getRuleEditorPane().updateSize();
			}
			
		}
		catch(JSONException e) {
			e.printStackTrace();
		}

		System.out.println("DONE");
	}

	/**
	 * <p>export.</p>
	 *
	 * @param fileName a {@link java.lang.String} object.
	 */
	public void export(String fileName) {
		if (!(this.visualRifEditor.getDocumentContainer().getActiveDocument() == null)) {
			String rif = this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getRifCodeEditor().getTp_rifInput().getText();
			PrintWriter pw = null;
			try {
				Writer fw = new FileWriter(fileName);
				Writer bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
				pw.append(rif);
			} catch (IOException e) {
				System.out.println("Konnte Datei nicht erstellen");
			} finally {
				if (pw != null)
					pw.close();
			}
		} else {
			DocumentPanel newDocument = visualRifEditor.getDocumentContainer().createNewDocument();
			visualRifEditor.getTreePane().addNewDocument(newDocument);
			visualRifEditor.setRightComponent(newDocument);
			visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
			visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().evaluate();
			visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().generateRif();
			this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getConsole().getTextArea().setText("You can not export an empty document. Please create a valid rif document");
			this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getBottomPane().setSelectedIndex(1);
		}
	}
	
	
	

}
