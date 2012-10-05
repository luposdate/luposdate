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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.DocumentPanel;




public class Tree extends JTree{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2077633661764490052L;
	private Tree that = this;
	private DefaultMutableTreeNode rootNode = null;
	private DefaultTreeModel model = null;
	
	private ITree comp = null;
	private VisualRifEditor visualRifEditor = null;
	private TreePath activePath = null;
	private TreePath rulePath = null;
	
	// Constructor
	public Tree(ITree comp, VisualRifEditor visualRifEditor) {
		super(new TreeNode("root"));

		this.comp = comp;
		this.visualRifEditor = visualRifEditor;

		this.setEditable(true);
		this.setRootVisible(false);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		this.addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent ke) {}
			public void keyPressed(KeyEvent ke) {}

			public void keyReleased(KeyEvent ke) {
				if(ke.getKeyCode() == KeyEvent.VK_DELETE || ke.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					that.delete();

				}
			}
		});

		this.model = (DefaultTreeModel) this.getModel();
		
		this.model.addTreeModelListener(new TreeModelListener() {
			
			public void treeNodesChanged(TreeModelEvent tme) {
				
				TypeEnum e = (that.activePath.getPathCount() == 3) ? TypeEnum.Rule : TypeEnum.Document;

				TreeNode node = (TreeNode) tme.getChildren()[0];
				String newName = node.toString();
				String oldName = ((TreeNode) that.activePath.getLastPathComponent()).updateCurrentName(newName);

				if(newName.equals("")) {
					((DefaultMutableTreeNode) that.activePath.getLastPathComponent()).setUserObject(oldName);
					((TreeNode) that.activePath.getLastPathComponent()).updateCurrentName(oldName);

					JOptionPane.showOptionDialog(that.visualRifEditor, "This name is not allowed!", "Error",
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

					return;
				}

				boolean success = that.comp.nameChanged(e, oldName, newName);

				if(!success) {
					((DefaultMutableTreeNode) that.activePath.getLastPathComponent()).setUserObject(oldName);
					((TreeNode) that.activePath.getLastPathComponent()).updateCurrentName(oldName);

					JOptionPane.showOptionDialog(that.visualRifEditor, "This name is already in use!", "Error",
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
				}
			}

			public void treeNodesInserted(TreeModelEvent e) {}
			public void treeNodesRemoved(TreeModelEvent e) {}
			public void treeStructureChanged(TreeModelEvent e) {}
		});

		this.rootNode = (DefaultMutableTreeNode) this.model.getRoot();
	}

	
	
	private DefaultMutableTreeNode getNodeByName(String name, DefaultMutableTreeNode rootNode) {
		return this.getNodeByName(name, rootNode, false);
	}

	
	
	private DefaultMutableTreeNode getNodeByName(String name, DefaultMutableTreeNode rootNode, boolean recursive) {
		if(name == "root") {
			return this.rootNode;
		}

		DefaultMutableTreeNode node = null;

		for(int i = 0; i < rootNode.getChildCount(); i += 1) {
			DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);

			if(tmpNode.toString().equals(name)) {
				node = tmpNode;

				break;
			}

			if(recursive && !tmpNode.isLeaf()) {
				DefaultMutableTreeNode tmpNode2 = this.getNodeByName(name, tmpNode, recursive);

				if(tmpNode2 != null) {
					node = tmpNode2;

					break;
				}
			}
		}

		return node;
	}

	public void add(String element) {
		DefaultMutableTreeNode childNode = this.add(element, this.rootNode);

		if(childNode != null) {
			this.setSelectionPath(new TreePath(childNode.getPath()));
		}
	}

	public TreeNode add(String element, String rootNodeName) {
		return this.add(element, this.getNodeByName(rootNodeName, this.rootNode));
	}

	public TreeNode add(String element, DefaultMutableTreeNode rootNode) {
		if(this.getNodeByName(element, this.rootNode) != null) {
			return null;
		}

		TreeNode childNode = new TreeNode(element);
		
		this.model.insertNodeInto(childNode, rootNode, rootNode.getChildCount());
		this.scrollPathToVisible(new TreePath(childNode.getPath()));
//		this.setSelectionPath(new TreePath(childNode.getPath())); // !
		this.setTreePath(new TreePath(childNode.getPath()));
		return childNode;
	}
	
	private void setTreePath(TreePath path){
		this.rulePath = path;
	}
	
	
	public TreePath getTreePath(){
		
		return this.rulePath;
	}
	

	public void setSelectionPath(TreePath path) {
		this.activePath = path;
		super.setSelectionPath(path);
	}

	public void deSelect() {
		super.removeSelectionPath(this.activePath);

		this.activePath = null;
	}

	public void edit() {
		if(this.activePath != null) {
			this.startEditingAtPath(this.activePath);
		}
	}

	public void delete() {
		if(this.activePath != null) {
			int returnValue = JOptionPane.showOptionDialog(
					this.visualRifEditor, "Do you really want to delete the selected element?",
					"Delete selected element", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null,
					new String[] { "YES", "NO" }, 0);

			if (returnValue == 0) {
//				this.visualRifEditor.getTreePane().getMoveRulePanel().disableButtons();
				
				 
				
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) this.activePath.getLastPathComponent();
				TreeNode parentNode = (TreeNode) currentNode.getParent();

				int nodeLevel = currentNode.getLevel();
				
				
				// in case the current node is a document... 
				if(nodeLevel == 1){
					visualRifEditor.getDocumentContainer().deleteDocument(currentNode.toString());
				}
				
				// in case the current node is a rule... 
				if(nodeLevel == 2){

					this.visualRifEditor.getRuleContainer().deleteRule(currentNode.toString());
					this.setSelectionPath(new TreePath(parentNode.getPath()));
				}
				
				this.comp.removeElement(currentNode.getUserObject().toString(), parentNode);

				if(currentNode.getParent() != null) {
					this.model.removeNodeFromParent(currentNode);
				}
				if(this.rootNode.getChildCount() == 0) {
//					this.visualRifEditor.getRuleEditMenu().setEnabled(false);
//					this.visualRifEditor.getGenerationMenu().setEnabled(false);
				}
				// in case the current node is a rule... 
				if(nodeLevel == 2){
				visualRifEditor.getDocumentContainer().showDocument(parentNode.toString());
				}
			}
		}
	}

	public boolean remove(String ruleName, String rulePackageName) {
		DefaultMutableTreeNode rootNode = this.getNodeByName(rulePackageName, this.rootNode);
		DefaultMutableTreeNode node = this.getNodeByName(ruleName, rootNode);
		System.out.println("Tree.remove(): Name der gelöschten Regel: "+rulePackageName+"." +ruleName+"");
		if(node != null) {
			this.model.removeNodeFromParent(node);
		}

		return this.getNodeByName(ruleName, this.rootNode, true) != null;
	}

	public DefaultMutableTreeNode getRootNode() {
		return this.rootNode;
	}

	public void clear() {
		this.rootNode.removeAllChildren();
		this.model.reload();
	}
	
	
	
	/* *************** **
	 * Getter + Setter **
	 * *************** */
	

	public Tree getThat() {
		return that;
	}



	public void setThat(Tree that) {
		this.that = that;
	}




	
	
}
