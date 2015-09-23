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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.ScrollPane;

import org.json.JSONException;
import org.json.JSONObject;
public class RulePanel extends JTabbedPane {
	private static final long serialVersionUID = 1776324661493797131L;


	private RulePanel that = this;
	private final VisualRifEditor visualRifEditor;
	private String ruleName;
	private TreePath rulePath;
	private String tabTitle;

	//visual representation
	private RuleEditorPane ruleEditorPane = null;

	// Constructor
	/**
	 * <p>Constructor for RulePanel.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public RulePanel(final VisualRifEditor visualRifEditor, final String name) {
		this(visualRifEditor, name, null);
	}

	// Constructor
	/**
	 * <p>Constructor for RulePanel.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 * @param name a {@link java.lang.String} object.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 */
	public RulePanel(final VisualRifEditor visualRifEditor, final String name,
			final JSONObject loadObject) {

		super();

		this.visualRifEditor = visualRifEditor;

		this.setRuleName(name);

		if (loadObject == null) {
			this.add("Visual Representation",
					this.getVisualRepresentationTab(null));
		}

		this.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent ce) {
				final JTabbedPane tabSource = (JTabbedPane) ce.getSource();
				RulePanel.this.setTabTitle(tabSource.getTitleAt(tabSource
						.getSelectedIndex()));

			}
		});
	}

	// visual representation
	private JPanel getVisualRepresentationTab(final JSONObject loadObject) {
		this.ruleEditorPane = new RuleEditorPane(this.visualRifEditor.getStatusBar(),this.visualRifEditor);
		this.ruleEditorPane.setVisualRifEditor(this.visualRifEditor);

		final JPanel leftPanel = new JPanel(new BorderLayout());
		// this.documentEditorPane.getVisualGraphs().get(0)
		leftPanel.add(new ScrollPane(this.ruleEditorPane.getVisualGraphs().get(
				0)));

		final JPanel rightPanel = new JPanel(new BorderLayout());
		// this.documentEditorPane.getVisualGraphs().get(1)
		 rightPanel.add(new
		 ScrollPane(this.ruleEditorPane.getVisualGraphs().get(1)));



		final JSplitPane splitPaneCanvas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneCanvas.setContinuousLayout(true);
		splitPaneCanvas.setOneTouchExpandable(true);
		splitPaneCanvas.setResizeWeight(0.5);
		splitPaneCanvas.setLeftComponent(leftPanel);
		splitPaneCanvas.setRightComponent(rightPanel);



		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.8);
		splitPane.setTopComponent(splitPaneCanvas);
		splitPane.setBottomComponent(this.ruleEditorPane.buildBottomPane());


		final JPanel visualPanel = new JPanel(new BorderLayout());

		visualPanel.add(this.ruleEditorPane.buildMenuBar(), BorderLayout.NORTH);

		JPanel topToolbar = null;

		if (loadObject != null) {
			try {
				topToolbar = this.ruleEditorPane.createTopToolBar(loadObject
						.getJSONObject("TOP TOOLBAR"));
			} catch (final JSONException e) {
				e.printStackTrace();
			}
		} else {
			topToolbar = this.ruleEditorPane.createTopToolBar(null);
		}

		final JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(topToolbar, BorderLayout.NORTH);
		innerPanel.add(splitPane, BorderLayout.CENTER);
		visualPanel.add(innerPanel, BorderLayout.CENTER);

		// this.documentEditorPane.fromJSON(loadObject);

		return visualPanel;
	}

	/**
	 * <p>cancelModi.</p>
	 */
	public void cancelModi() {
		if (this.ruleEditorPane != null) {
			this.ruleEditorPane.cancelModi();
		}
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @param documentName a {@link java.lang.String} object.
	 * @return a {@link org.json.JSONObject} object.
	 */
	public JSONObject toJSON(final String documentName) {
		final JSONObject saveObject = new JSONObject();
		try {
			saveObject.put("DOCUMENTNAME", documentName);
			saveObject.put("CANVASINFO", this.visualRifEditor.getDocumentContainer().getDocumentByName(documentName).getDocumentEditorPane().getDocumentGraph().ruleToJSON(this.ruleName));
			saveObject.put("RULEEDITORPANE", this.ruleEditorPane.toJSON());
		} catch (final JSONException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return saveObject;
	}




	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.ruleName;
	}


	/* *************** **
	 * Getter + Setter **
	 * *************** */

	/**
	 * <p>Getter for the field <code>ruleName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRuleName() {
		return this.ruleName;
	}

	/**
	 * <p>Setter for the field <code>ruleName</code>.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void setRuleName(final String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * <p>Getter for the field <code>rulePath</code>.</p>
	 *
	 * @return a {@link javax.swing.tree.TreePath} object.
	 */
	public TreePath getRulePath() {

		return this.rulePath;
	}

	/**
	 * <p>Setter for the field <code>rulePath</code>.</p>
	 *
	 * @param rulePath a {@link javax.swing.tree.TreePath} object.
	 */
	public void setRulePath(final TreePath rulePath) {
		this.rulePath = rulePath;
	}

	/**
	 * <p>Getter for the field <code>ruleEditorPane</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RuleEditorPane} object.
	 */
	public RuleEditorPane getRuleEditorPane() {
		return this.ruleEditorPane;
	}

	/**
	 * <p>Setter for the field <code>ruleEditorPane</code>.</p>
	 *
	 * @param ruleEditorPane a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RuleEditorPane} object.
	 */
	public void setRuleEditorPane(final RuleEditorPane ruleEditorPane) {
		this.ruleEditorPane = ruleEditorPane;
	}

	/**
	 * <p>Getter for the field <code>that</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel getThat() {
		return this.that;
	}

	/**
	 * <p>Setter for the field <code>that</code>.</p>
	 *
	 * @param that a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public void setThat(final RulePanel that) {
		this.that = that;
	}

	/**
	 * <p>Getter for the field <code>tabTitle</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTabTitle() {
		return this.tabTitle;
	}

	/**
	 * <p>Setter for the field <code>tabTitle</code>.</p>
	 *
	 * @param tabTitle a {@link java.lang.String} object.
	 */
	public void setTabTitle(final String tabTitle) {
		this.tabTitle = tabTitle;
	}


}
