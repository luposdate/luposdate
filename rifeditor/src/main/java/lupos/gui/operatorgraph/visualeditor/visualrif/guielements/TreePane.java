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
	public TreePane(VisualRifEditor visualRifEditor,DocumentContainer documentContainer){

	
		this.setVisualRifEditor(visualRifEditor);
		this.setDocumentContainer(documentContainer);
		
		this.add("Explorer",new ScrollPane(this.generateComponent()));

	}


	
	private Component generateComponent() {
		this.tree_documents = new Tree(this.documentContainer, that.visualRifEditor);
		
		this.tree_documents.addTreeSelectionListener(new TreeSelectionListener() {
		

			public void valueChanged(TreeSelectionEvent tse) {
				
				if(tse.isAddedPath()) {
//					that.tree_unassigned.deSelect();
					that.setCurrentTree(that.tree_documents);
//					that.ruleContainer.cancelModi();
				}
				else {
					return;
				}

				// get selected item...
				TreePath treePath = tse.getPath();
				TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();


				if(treePath.getPathCount() == 3) {
					TreeNode parentNode = treeNode.getParent();
					setCount(parentNode.getChildCount());
					setIndex(parentNode.getIndex(treeNode));

//					that.moveRulePanel.update(treeNode, index, count);
					
					that.visualRifEditor.getRuleContainer().showRule(treeNode.toString());
					that.visualRifEditor.getDocumentContainer().setActiveDocument(that.visualRifEditor.getDocumentContainer().getDocumentByName(parentNode.toString()));
					
				}
				else {
//					that.moveRulePanel.disableButtons();
					that.documentContainer.showDocument(treeNode.toString());
				}
				
				
			}

			
		});

		
		
		return this.tree_documents;
		
	}

	/***
	 * stores the name (string) of the document in the documents tree
	 * @param newDocument
	 */
	public void addNewDocument(DocumentPanel newDocument) {
		
		this.tree_documents.add(newDocument.toString());
		
	}

	public void addNewRule(RulePanel newRulePanel, String documentName) {
		this.tree_documents.add(newRulePanel.toString(),documentName);
		newRulePanel.setRulePath(this.tree_documents.getTreePath());
		
	}
	
	public void remove(String name, String documentName){
		this.tree_documents.remove(name, documentName);
	}
	
	public void addNewGroup(GroupPanel newGroupPanel, String documentName){
		this.tree_documents.add(newGroupPanel.toString(), documentName);
		newGroupPanel.setGroupPath(this.tree_documents.getTreePath());
	}

	public void updateTopComponent(String oldRuleName, String newRuleName) {
		DefaultMutableTreeNode rootNode = this.tree_documents.getRootNode();

		for(int i = 0; i < rootNode.getChildCount(); i += 1) {
			TreeNode tmpNode = rootNode.getChildAt(i);

			for(int j = 0; j < tmpNode.getChildCount(); j += 1) {
				DefaultMutableTreeNode tmpNode2 = (DefaultMutableTreeNode) tmpNode.getChildAt(j);

				if(tmpNode2.toString().equalsIgnoreCase(oldRuleName)) {
					tmpNode2.setUserObject(newRuleName);

					break;
				}
			}
		}
	}

	public void renameElement() {
		this.currentTree.edit();
	}

	public void deleteElement() {

		this.currentTree.delete();
	}
	
	public void clearTopComponent() {
		this.tree_documents.clear();
	}

	
	/* *************** **
	 * Getter + Setter **
	 * *************** */
	
	
	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}

	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	public DocumentContainer getDocumentContainer() {
		return documentContainer;
	}

	public void setDocumentContainer(DocumentContainer documentContainer) {
		this.documentContainer = documentContainer;
	}

	public TreePane getThat() {
		return that;
	}

	public void setThat(TreePane that) {
		this.that = that;
	}

	public Object getMoveRulePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	public Tree getCurrentTree() {
		return currentTree;
	}

	public void setCurrentTree(Tree currentTree) {
		this.currentTree = currentTree;
	}

	public RuleContainer getRuleContainer() {
		return ruleContainer;
	}

	public void setRuleContainer(RuleContainer ruleContainer) {
		this.ruleContainer = ruleContainer;
	}

	public Tree getTree_documents() {
		return tree_documents;
	}

	public void setTree_documents(Tree tree_documents) {
		this.tree_documents = tree_documents;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}




	
}
