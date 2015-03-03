
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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements;

import java.awt.BorderLayout;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import org.json.JSONException;
import org.json.JSONObject;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.ScrollPane;
public class GroupPanel extends JTabbedPane {


	private static final long serialVersionUID = 4058860501486111604L;
	
	
	private GroupPanel that = this;
	private VisualRifEditor visualRifEditor;
	private TreePath groupPath;
	private String tabTitle, groupName;
	
	//visual representation
	private GroupEditorPane groupEditorPane = null;


	private LinkedList<String> listOfRules;
	
	// Constructor
	/**
	 * <p>Constructor for GroupPanel.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public GroupPanel(VisualRifEditor visualRifEditor, String name) {
		this(visualRifEditor, name, null);
	}
	
	// Constructor
	/**
	 * <p>Constructor for GroupPanel.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 * @param name a {@link java.lang.String} object.
	 * @param loadObject a {@link org.json.JSONObject} object.
	 */
	public GroupPanel(VisualRifEditor visualRifEditor, String name,
			JSONObject loadObject) {
		super();

		this.setListOfRules(new LinkedList<String>());
		this.setVisualRifEditor(visualRifEditor);
		this.setGroupName(name);

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
	
	
	//visual representation
		private JPanel getVisualRepresentationTab(JSONObject loadObject) {
			
				this.groupEditorPane = new GroupEditorPane(this.visualRifEditor.getStatusBar());
				this.groupEditorPane.setVisualRifEditor(this.visualRifEditor);
				this.groupEditorPane.setGroupName(this.groupName);
				
				
				JPanel topPanel = new JPanel(new BorderLayout());
				topPanel.add(new ScrollPane(this.groupEditorPane.getVisualGraphs().get(0)));

				JPanel bottomPanel = new JPanel(new BorderLayout());
				bottomPanel.add(new ScrollPane(new JTextField()));

				JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				splitPane.setContinuousLayout(true);
				splitPane.setOneTouchExpandable(true);
				splitPane.setResizeWeight(0.8);
				splitPane.setTopComponent(topPanel);
				splitPane.setBottomComponent(this.groupEditorPane.buildBottomPane());
				
				JPanel visualPanel = new JPanel(new BorderLayout());
				visualPanel.add(this.groupEditorPane.buildMenuBar(), BorderLayout.NORTH);
				
				JPanel topToolbar = null;

				if(loadObject != null) {
					try {
						topToolbar = this.groupEditorPane.createTopToolBar(loadObject.getJSONObject("top toolbar"));
					}
					catch(JSONException e) {
						e.printStackTrace();
					}
				}else
				{
					topToolbar = this.groupEditorPane.createTopToolBar(null);
				}
					
				JPanel innerPanel = new JPanel(new BorderLayout());
				innerPanel.add(topToolbar, BorderLayout.NORTH);
				innerPanel.add(splitPane, BorderLayout.CENTER);
				
				visualPanel.add(innerPanel, BorderLayout.CENTER);



				return visualPanel;
			}
	
		/**
		 * <p>cancelModi.</p>
		 */
		public void cancelModi() {
			if (this.groupEditorPane != null) {
				this.groupEditorPane.cancelModi();
			}
		}	
	
	
		/**
		 * <p>toString.</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String toString(){
			return this.groupName;
		}
		
		
	/* *************** **
	 * Getter + Setter **
	 * *************** */

	/**
	 * <p>Getter for the field <code>that</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public GroupPanel getThat() {
		return that;
	}
	
	/**
	 * <p>Setter for the field <code>that</code>.</p>
	 *
	 * @param that a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 */
	public void setThat(GroupPanel that) {
		this.that = that;
	}
	
	/**
	 * <p>Getter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}
	
	/**
	 * <p>Setter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}
	

	
	/**
	 * <p>Getter for the field <code>tabTitle</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTabTitle() {
		return tabTitle;
	}
	
	/**
	 * <p>Setter for the field <code>tabTitle</code>.</p>
	 *
	 * @param tabTitle a {@link java.lang.String} object.
	 */
	public void setTabTitle(String tabTitle) {
		this.tabTitle = tabTitle;
	}
	
	/**
	 * <p>Getter for the field <code>groupName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupName() {
		return groupName;
	}
	
	/**
	 * <p>Setter for the field <code>groupName</code>.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	/**
	 * <p>getSerialversionuid.</p>
	 *
	 * @return a long.
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * <p>Getter for the field <code>groupEditorPane</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupEditorPane} object.
	 */
	public GroupEditorPane getGroupEditorPane() {
		return groupEditorPane;
	}

	/**
	 * <p>Setter for the field <code>groupEditorPane</code>.</p>
	 *
	 * @param groupEditorPane a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupEditorPane} object.
	 */
	public void setGroupEditorPane(GroupEditorPane groupEditorPane) {
		this.groupEditorPane = groupEditorPane;
	}

	/**
	 * <p>Getter for the field <code>listOfRules</code>.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<String> getListOfRules() {
		return listOfRules;
	}

	/**
	 * <p>Setter for the field <code>listOfRules</code>.</p>
	 *
	 * @param listOfRules a {@link java.util.LinkedList} object.
	 */
	public void setListOfRules(LinkedList<String> listOfRules) {
		this.listOfRules = listOfRules;
	}

	/**
	 * <p>Getter for the field <code>groupPath</code>.</p>
	 *
	 * @return a {@link javax.swing.tree.TreePath} object.
	 */
	public TreePath getGroupPath() {
		return groupPath;
	}

	/**
	 * <p>Setter for the field <code>groupPath</code>.</p>
	 *
	 * @param groupPath a {@link javax.swing.tree.TreePath} object.
	 */
	public void setGroupPath(TreePath groupPath) {
		this.groupPath = groupPath;
	}
	

}
