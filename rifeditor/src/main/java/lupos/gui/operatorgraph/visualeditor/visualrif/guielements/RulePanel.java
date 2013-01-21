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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import org.json.JSONException;
import org.json.JSONObject;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.ScrollPane;

public class RulePanel extends JTabbedPane {
	private static final long serialVersionUID = 1776324661493797131L;


	private RulePanel that = this;
	private VisualRifEditor visualRifEditor;
	private String ruleName;
	private TreePath rulePath;
	private String tabTitle;

	//visual representation
	private RuleEditorPane ruleEditorPane = null;

	// Constructor
	public RulePanel(VisualRifEditor visualRifEditor, String name) {
		this(visualRifEditor, name, null);
	}

	// Constructor
	public RulePanel(VisualRifEditor visualRifEditor, String name,
			JSONObject loadObject) {

		super();

		this.visualRifEditor = visualRifEditor;

		this.setRuleName(name);

		if (loadObject == null) {
			this.add("Visual Representation",
					this.getVisualRepresentationTab(null));
		}

		this.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent ce) {
				JTabbedPane tabSource = (JTabbedPane) ce.getSource();
				setTabTitle(tabSource.getTitleAt(tabSource
						.getSelectedIndex()));

			}
		});
	}

	// visual representation
	private JPanel getVisualRepresentationTab(JSONObject loadObject) {
		this.ruleEditorPane = new RuleEditorPane(this.visualRifEditor.getStatusBar(),this.visualRifEditor);
		this.ruleEditorPane.setVisualRifEditor(this.visualRifEditor);

		JPanel leftPanel = new JPanel(new BorderLayout());
		// this.documentEditorPane.getVisualGraphs().get(0)
		leftPanel.add(new ScrollPane(this.ruleEditorPane.getVisualGraphs().get(
				0)));

		JPanel rightPanel = new JPanel(new BorderLayout());
		// this.documentEditorPane.getVisualGraphs().get(1)
		 rightPanel.add(new
		 ScrollPane(this.ruleEditorPane.getVisualGraphs().get(1)));

		 
		 
		JSplitPane splitPaneCanvas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneCanvas.setContinuousLayout(true);
		splitPaneCanvas.setOneTouchExpandable(true);
		splitPaneCanvas.setResizeWeight(0.5);
		splitPaneCanvas.setLeftComponent(leftPanel);
		splitPaneCanvas.setRightComponent(rightPanel);
		
		

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.8);
		splitPane.setTopComponent(splitPaneCanvas);
		splitPane.setBottomComponent(this.ruleEditorPane.buildBottomPane());
		

		JPanel visualPanel = new JPanel(new BorderLayout());

		visualPanel.add(this.ruleEditorPane.buildMenuBar(), BorderLayout.NORTH);

		JPanel topToolbar = null;

		if (loadObject != null) {
			try {
				topToolbar = this.ruleEditorPane.createTopToolBar(loadObject
						.getJSONObject("TOP TOOLBAR"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			topToolbar = this.ruleEditorPane.createTopToolBar(null);
		}

		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(topToolbar, BorderLayout.NORTH);
		innerPanel.add(splitPane, BorderLayout.CENTER);
		visualPanel.add(innerPanel, BorderLayout.CENTER);

		// this.documentEditorPane.fromJSON(loadObject);

		return visualPanel;
	}

	public void cancelModi() {
		if (this.ruleEditorPane != null) {
			this.ruleEditorPane.cancelModi();
		}
	}

	public JSONObject toJSON(String documentName) {
		JSONObject saveObject = new JSONObject();
		try {
			saveObject.put("DOCUMENTNAME", documentName);
			saveObject.put("CANVASINFO", this.visualRifEditor.getDocumentContainer().getDocumentByName(documentName).getDocumentEditorPane().getDocumentGraph().ruleToJSON(ruleName));
			saveObject.put("RULEEDITORPANE", this.ruleEditorPane.toJSON());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return saveObject;
	}
	

	
	
	public String toString() {
		return this.ruleName;
	}


	/* *************** **
	 * Getter + Setter **
	 * *************** */
	
	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public TreePath getRulePath() {

		return this.rulePath;
	}

	public void setRulePath(TreePath rulePath) {
		this.rulePath = rulePath;
	}

	public RuleEditorPane getRuleEditorPane() {
		return ruleEditorPane;
	}

	public void setRuleEditorPane(RuleEditorPane ruleEditorPane) {
		this.ruleEditorPane = ruleEditorPane;
	}

	public RulePanel getThat() {
		return that;
	}
	
	public void setThat(RulePanel that) {
		this.that = that;
	}

	public String getTabTitle() {
		return tabTitle;
	}

	public void setTabTitle(String tabTitle) {
		this.tabTitle = tabTitle;
	}


}
