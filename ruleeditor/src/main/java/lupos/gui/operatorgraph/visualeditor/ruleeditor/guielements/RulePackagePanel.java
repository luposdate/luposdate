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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ScrollPane;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.TypeEnum;

import org.json.JSONException;
import org.json.JSONObject;

public class RulePackagePanel extends JTabbedPane {
	private static final long serialVersionUID = 1870189692371247906L;

	// --- basic variables - begin ---
	private RulePackagePanel that = this;
	private RuleEditor editor = null;
	private String rulePackageName = "";
	// --- basic variables - end ---

	// --- associations variables - begin ---
	private AssociationsPanel assotiationsPanel = null;
	// --- associations variables - end ---

	// --- documentation variables - begin ---
	private DocumentationPanel documentationPanel = null;
	// --- documentation variables - end ---


	public RulePackagePanel(RuleEditor editor, String rulePackageName) {
		this(editor, rulePackageName, null);
	}

	public RulePackagePanel(RuleEditor editor, String rulePackageName, JSONObject loadObject) {
		super();

		this.editor = editor;
		this.rulePackageName = rulePackageName;

		this.add("Associations", this.getAssociationsTab());

		if(loadObject == null) {
			this.add("Documentation", this.getDocumentationTab(null));
		}
		else {
			try {
				this.add("Documentation", this.getDocumentationTab(loadObject.getJSONObject("documentation")));
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}

		this.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				JTabbedPane tabSource = (JTabbedPane) ce.getSource();
				String tabTitle = tabSource.getTitleAt(tabSource.getSelectedIndex());

				if(tabTitle.equals("Associations")) {
					that.assotiationsPanel.rebuildRules(that.editor.getRules());
				}
				else if(tabTitle.equals("Documentation")) {
					that.documentationPanel.rebuild(that.editor.getRules(), that.editor.getRulePackages());
				}
			}
		});
	}


	// --- basic functions - begin ---
	public String toString() {
		return this.rulePackageName;
	}

	public void setRulePackageName(String rulePackageName) {
		this.rulePackageName = rulePackageName;
	}

	public void updateRulePackage() {
		this.assotiationsPanel.rebuildRules(this.editor.getRules());
		this.documentationPanel.rebuild(this.editor.getRules(), this.editor.getRulePackages());
	}
	// --- basic functions - end ---


	// --- associations - begin ---
	private JPanel getAssociationsTab() {
		this.assotiationsPanel = new AssociationsPanel(this.toString(), this.editor.getAssociationsContainer());
		this.assotiationsPanel.rebuildRules(this.editor.getRules());

		JPanel tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(this.assotiationsPanel);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(new JLabel("Associated rules:"), BorderLayout.NORTH);
		panel.add(new ScrollPane(tmpPanel), BorderLayout.CENTER);

		return panel;
	}

	public AssociationsPanel getAssociationsPanel() {
		return this.assotiationsPanel;
	}

	public LinkedList<RulePanel> getAssociatedRules() {
		LinkedList<String> associations = this.assotiationsPanel.getAssociationsContainer().getAssociationsToRulePackage(this.rulePackageName);
		LinkedList<RulePanel> allRules = this.editor.getRules();
		LinkedList<RulePanel> associatedRules = new LinkedList<RulePanel>();

		for(RulePanel pane : allRules) {
			if(associations.contains(pane.toString())) {
				associatedRules.add(pane);
			}
		}

		return associatedRules;
	}
	// --- associations - end ---


	// --- documentation - begin ---
	private ScrollPane getDocumentationTab(JSONObject loadObject) {
		this.documentationPanel = new DocumentationPanel(TypeEnum.RulePackage, this.rulePackageName, null, loadObject);
		this.documentationPanel.rebuild(this.editor.getRules(), this.editor.getRulePackages());

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(this.documentationPanel, BorderLayout.CENTER);

		return new ScrollPane(panel);
	}

	public DocumentationPanel getDocumentationPanel() {
		return this.documentationPanel;
	}
	// --- documentation - end ---

	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();

		saveObject.put("documentation", this.documentationPanel.toJSON());

		return saveObject;
	}
}