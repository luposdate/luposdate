
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ScrollPane;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.TypeEnum;

import org.json.JSONException;
import org.json.JSONObject;
public class RulePanel extends JTabbedPane {
	private static final long serialVersionUID = -7897238149968316491L;

	// --- basic variables - begin ---
	private RulePanel that = this;
	private RuleEditor editor;
	private String ruleName;
	// --- basic variables - end ---

	// --- visual representation variables - begin ---
	private RuleEditorPane editorPane = null;
	// --- visual representation variables - end ---

	// --- associations variables - begin ---
	private AssociationsPanel assotiationsPanel = null;
	// --- associations variables - end ---

	// --- documentation variables - begin ---
	private DocumentationPanel documentationPanel = null;
	// --- documentation variables - end ---

	// --- implementation variables - begin ---
	private ImplementationPanel implementationPanel = null;
	// --- implementation variables - end ---


	/**
	 * <p>Constructor for RulePanel.</p>
	 *
	 * @param editor a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor} object.
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public RulePanel(RuleEditor editor, String ruleName) {
		this(editor, ruleName, null);
	}

	/**
	 * <p>Constructor for RulePanel.</p>
	 *
	 * @param editor a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor} object.
	 * @param ruleName a {@link java.lang.String} object.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 */
	public RulePanel(RuleEditor editor, String ruleName, JSONObject loadObject) {
		super();

		this.editor = editor;
		this.ruleName = ruleName;

		if(loadObject == null) {
			this.add("Visual Representation", this.getVisualRepresentationTab(null));
			this.add("Documentation", this.getDocumentationTab(null));
			this.add("Implementation", this.getImplementationTab(null));
		}
		else {
			try {
				this.add("Visual Representation", this.getVisualRepresentationTab(loadObject.getJSONObject("visual representation")));
				this.add("Documentation", this.getDocumentationTab(loadObject.getJSONObject("documentation")));
				this.add("Implementation", this.getImplementationTab(loadObject.getJSONObject("implementation")));
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}

		this.add("Associations", this.getAssociationsTab());

		this.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				JTabbedPane tabSource = (JTabbedPane) ce.getSource();
				String tabTitle = tabSource.getTitleAt(tabSource.getSelectedIndex());

				if(tabTitle.equals("Associations")) {
					that.assotiationsPanel.rebuildRulePackages(that.editor.getRulePackages());
				}
				else if(tabTitle.equals("Documentation")) {
					that.documentationPanel.rebuild(that.editor.getRules(), that.editor.getRulePackages());
				}
			}
		});
	}


	// --- basic functions - begin ---
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return this.ruleName;
	}

	/**
	 * <p>Setter for the field <code>ruleName</code>.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * <p>updateRule.</p>
	 */
	public void updateRule() {
		this.assotiationsPanel.rebuildRulePackages(this.editor.getRulePackages());
		this.documentationPanel.rebuild(this.editor.getRules(), this.editor.getRulePackages());
	}
	// --- basic functions - end ---


	// --- visual representation - begin ---
	private JPanel getVisualRepresentationTab(JSONObject loadObject) {
		this.editorPane = new RuleEditorPane(this.editor.getStatusBar());

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(new ScrollPane(this.editorPane.getVisualGraphs().get(0)));

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new ScrollPane(this.editorPane.getVisualGraphs().get(1)));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.5);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
		
		JPanel visualPanel = new JPanel(new BorderLayout());
		
		visualPanel.add(this.editorPane.buildMenuBar(), BorderLayout.NORTH);
		
		JPanel topToolbar = null;

		if(loadObject != null) {
			try {
				topToolbar = this.editorPane.createTopToolBar(loadObject.getJSONObject("top toolbar"));
			}
			catch(JSONException e) {
				e.printStackTrace();
			}
		}
		else {
			topToolbar = this.editorPane.createTopToolBar(null);
		}
		
		JPanel innerPanel=new JPanel(new BorderLayout());
		innerPanel.add(topToolbar, BorderLayout.NORTH);
		innerPanel.add(splitPane, BorderLayout.CENTER);
		visualPanel.add(innerPanel, BorderLayout.CENTER);

		this.editorPane.fromJSON(loadObject);

		return visualPanel;
	}

	/**
	 * <p>cancelModi.</p>
	 */
	public void cancelModi() {
		if(this.editorPane != null) {
			this.editorPane.cancelModi();
		}
	}

	/**
	 * <p>getRuleEditorPane.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.RuleEditorPane} object.
	 */
	public RuleEditorPane getRuleEditorPane() {
		return this.editorPane;
	}
	// --- visual representation - end ---


	// --- documentation - begin ---
	private ScrollPane getDocumentationTab(JSONObject loadObject) {
		this.documentationPanel = new DocumentationPanel(TypeEnum.Rule, this.ruleName, this.editorPane, loadObject);
		this.documentationPanel.rebuild(this.editor.getRules(), this.editor.getRulePackages());

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(this.documentationPanel, BorderLayout.CENTER);

		return new ScrollPane(panel);
	}

	/**
	 * <p>Getter for the field <code>documentationPanel</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.DocumentationPanel} object.
	 */
	public DocumentationPanel getDocumentationPanel() {
		return this.documentationPanel;
	}
	// --- documentation - end ---


	// --- implementation - begin ---
	private JPanel getImplementationTab(JSONObject loadObject) {
		this.implementationPanel = new ImplementationPanel(loadObject);
		return this.implementationPanel;
	}

	/**
	 * <p>Getter for the field <code>implementationPanel</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.ImplementationPanel} object.
	 */
	public ImplementationPanel getImplementationPanel() {
		return this.implementationPanel;
	}
	// --- implementation - end ---


	// --- associations - begin ---
	private JPanel getAssociationsTab() {
		this.assotiationsPanel = new AssociationsPanel(this.toString(), this.editor.getAssociationsContainer());
		this.assotiationsPanel.rebuildRulePackages(this.editor.getRulePackages());

		JPanel tmpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmpPanel.add(this.assotiationsPanel);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(new JLabel("Associated rule packages:"), BorderLayout.NORTH);
		panel.add(new ScrollPane(tmpPanel), BorderLayout.CENTER);

		return panel;
	}

	/**
	 * <p>getAssociationsPanel.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.guielements.AssociationsPanel} object.
	 */
	public AssociationsPanel getAssociationsPanel() {
		return this.assotiationsPanel;
	}
	// --- associations - end ---

	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();

		saveObject.put("visual representation", this.editorPane.toJSON());
		saveObject.put("documentation", this.documentationPanel.toJSON());
		saveObject.put("implementation", this.implementationPanel.toJSON());

		return saveObject;
	}
}
