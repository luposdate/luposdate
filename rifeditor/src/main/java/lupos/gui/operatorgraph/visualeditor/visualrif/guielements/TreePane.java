
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


import java.awt.Component;

import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.DocumentContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.ScrollPane;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Tree;
public class TreePane extends JTabbedPane  {

	private static final long serialVersionUID = 3803257060245483206L;

	private TreePane that = this;
	private VisualRifEditor visualRifEditor = null;
	private DocumentContainer documentContainer = null;
	private RuleContainer ruleContainer = null;
	private Tree tree_documents = null;
	private Tree currentTree = null;
	private int count;
	private int index;


	// Constructor
	/**
	 * <p>Constructor for TreePane.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 * @param documentContainer a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.DocumentContainer} object.
	 */
	public TreePane(final VisualRifEditor visualRifEditor,final DocumentContainer documentContainer){


		this.setVisualRifEditor(visualRifEditor);
		this.setDocumentContainer(documentContainer);

		this.add("Explorer",new ScrollPane(this.generateComponent()));

	}



	private Component generateComponent() {
		this.tree_documents = new Tree(this.documentContainer, this.that.visualRifEditor);

		this.tree_documents.addTreeSelectionListener(new TreeSelectionListener() {


			@Override
			public void valueChanged(final TreeSelectionEvent tse) {

				if(tse.isAddedPath()) {
//					that.tree_unassigned.deSelect();
					TreePane.this.that.setCurrentTree(TreePane.this.that.tree_documents);
//					that.ruleContainer.cancelModi();
				}
				else {
					return;
				}

				// get selected item...
				final TreePath treePath = tse.getPath();
				final TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();


				if(treePath.getPathCount() == 3) {
					final TreeNode parentNode = treeNode.getParent();
					TreePane.this.setCount(parentNode.getChildCount());
					TreePane.this.setIndex(parentNode.getIndex(treeNode));

//					that.moveRulePanel.update(treeNode, index, count);

					TreePane.this.that.visualRifEditor.getRuleContainer().showRule(treeNode.toString());
					TreePane.this.that.visualRifEditor.getDocumentContainer().setActiveDocument(TreePane.this.that.visualRifEditor.getDocumentContainer().getDocumentByName(parentNode.toString()));

				}
				else {
//					that.moveRulePanel.disableButtons();
					TreePane.this.that.documentContainer.showDocument(treeNode.toString());
				}


			}


		});



		return this.tree_documents;

	}

	/**
	 *
	 * stores the name (string) of the document in the documents tree
	 *
	 * @param newDocument a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.DocumentPanel} object.
	 */
	public void addNewDocument(final DocumentPanel newDocument) {

		this.tree_documents.add(newDocument.toString());

	}

	/**
	 * <p>addNewRule.</p>
	 *
	 * @param newRulePanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 * @param documentName a {@link java.lang.String} object.
	 */
	public void addNewRule(final RulePanel newRulePanel, final String documentName) {
		this.tree_documents.add(newRulePanel.toString(),documentName);
		newRulePanel.setRulePath(this.tree_documents.getTreePath());

	}

	/**
	 * <p>remove.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param documentName a {@link java.lang.String} object.
	 */
	public void remove(final String name, final String documentName){
		this.tree_documents.remove(name, documentName);
	}

	/**
	 * <p>addNewGroup.</p>
	 *
	 * @param newGroupPanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.GroupPanel} object.
	 * @param documentName a {@link java.lang.String} object.
	 */
	public void addNewGroup(final GroupPanel newGroupPanel, final String documentName){
		this.tree_documents.add(newGroupPanel.toString(), documentName);
		newGroupPanel.setGroupPath(this.tree_documents.getTreePath());
	}

	/**
	 * <p>updateTopComponent.</p>
	 *
	 * @param oldRuleName a {@link java.lang.String} object.
	 * @param newRuleName a {@link java.lang.String} object.
	 */
	public void updateTopComponent(final String oldRuleName, final String newRuleName) {
		final DefaultMutableTreeNode rootNode = this.tree_documents.getRootNode();

		for(int i = 0; i < rootNode.getChildCount(); i += 1) {
			final TreeNode tmpNode = rootNode.getChildAt(i);

			for(int j = 0; j < tmpNode.getChildCount(); j += 1) {
				final DefaultMutableTreeNode tmpNode2 = (DefaultMutableTreeNode) tmpNode.getChildAt(j);

				if(tmpNode2.toString().equalsIgnoreCase(oldRuleName)) {
					tmpNode2.setUserObject(newRuleName);

					break;
				}
			}
		}
	}

	/**
	 * <p>renameElement.</p>
	 */
	public void renameElement() {
		this.currentTree.edit();
	}

	/**
	 * <p>deleteElement.</p>
	 */
	public void deleteElement() {

		this.currentTree.delete();
	}

	/**
	 * <p>clearTopComponent.</p>
	 */
	public void clearTopComponent() {
		this.tree_documents.clear();
	}


	/* *************** **
	 * Getter + Setter **
	 * *************** */


	/**
	 * <p>Getter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	/**
	 * <p>Setter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	/**
	 * <p>Getter for the field <code>documentContainer</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.DocumentContainer} object.
	 */
	public DocumentContainer getDocumentContainer() {
		return this.documentContainer;
	}

	/**
	 * <p>Setter for the field <code>documentContainer</code>.</p>
	 *
	 * @param documentContainer a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.DocumentContainer} object.
	 */
	public void setDocumentContainer(final DocumentContainer documentContainer) {
		this.documentContainer = documentContainer;
	}

	/**
	 * <p>Getter for the field <code>that</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.TreePane} object.
	 */
	public TreePane getThat() {
		return this.that;
	}

	/**
	 * <p>Setter for the field <code>that</code>.</p>
	 *
	 * @param that a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.TreePane} object.
	 */
	public void setThat(final TreePane that) {
		this.that = that;
	}

	/**
	 * <p>getMoveRulePanel.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getMoveRulePanel() {
		return null;
	}

	/**
	 * <p>Getter for the field <code>currentTree</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Tree} object.
	 */
	public Tree getCurrentTree() {
		return this.currentTree;
	}

	/**
	 * <p>Setter for the field <code>currentTree</code>.</p>
	 *
	 * @param currentTree a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Tree} object.
	 */
	public void setCurrentTree(final Tree currentTree) {
		this.currentTree = currentTree;
	}

	/**
	 * <p>Getter for the field <code>ruleContainer</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleContainer} object.
	 */
	public RuleContainer getRuleContainer() {
		return this.ruleContainer;
	}

	/**
	 * <p>Setter for the field <code>ruleContainer</code>.</p>
	 *
	 * @param ruleContainer a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleContainer} object.
	 */
	public void setRuleContainer(final RuleContainer ruleContainer) {
		this.ruleContainer = ruleContainer;
	}

	/**
	 * <p>Getter for the field <code>tree_documents</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Tree} object.
	 */
	public Tree getTree_documents() {
		return this.tree_documents;
	}

	/**
	 * <p>Setter for the field <code>tree_documents</code>.</p>
	 *
	 * @param tree_documents a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Tree} object.
	 */
	public void setTree_documents(final Tree tree_documents) {
		this.tree_documents = tree_documents;
	}

	/**
	 * <p>Getter for the field <code>count</code>.</p>
	 *
	 * @return a int.
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 * <p>Setter for the field <code>count</code>.</p>
	 *
	 * @param count a int.
	 */
	public void setCount(final int count) {
		this.count = count;
	}

	/**
	 * <p>Getter for the field <code>index</code>.</p>
	 *
	 * @return a int.
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * <p>Setter for the field <code>index</code>.</p>
	 *
	 * @param index a int.
	 */
	public void setIndex(final int index) {
		this.index = index;
	}





}
