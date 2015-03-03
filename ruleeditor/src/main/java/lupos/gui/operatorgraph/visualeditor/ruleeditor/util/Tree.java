
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
package lupos.gui.operatorgraph.visualeditor.ruleeditor.util;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
public class Tree extends JTree {
	private static final long serialVersionUID = 5428177608456900685L;

	private Tree that = this;
	private ITree comp = null;
	private RuleEditor editor = null;
	private DefaultMutableTreeNode rootNode = null;
	private DefaultTreeModel model = null;
	private TreePath activePath = null;

	/**
	 * <p>Constructor for Tree.</p>
	 *
	 * @param comp a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ITree} object.
	 * @param editor a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor} object.
	 */
	public Tree(ITree comp, RuleEditor editor) {
		super(new TreeNode("root"));

		this.comp = comp;
		this.editor = editor;

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
				TypeEnum e = (that.activePath.getPathCount() == 3) ? TypeEnum.Rule : TypeEnum.RulePackage;

				TreeNode node = (TreeNode) tme.getChildren()[0];
				String newName = node.toString();
				String oldName = ((TreeNode) that.activePath.getLastPathComponent()).updateCurrentName(newName);

				if(newName.equals("")) {
					((DefaultMutableTreeNode) that.activePath.getLastPathComponent()).setUserObject(oldName);
					((TreeNode) that.activePath.getLastPathComponent()).updateCurrentName(oldName);

					JOptionPane.showOptionDialog(that.editor, "This name is not allowed!", "Error",
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

					return;
				}

				boolean success = that.comp.nameChanged(e, oldName, newName);

				if(!success) {
					((DefaultMutableTreeNode) that.activePath.getLastPathComponent()).setUserObject(oldName);
					((TreeNode) that.activePath.getLastPathComponent()).updateCurrentName(oldName);

					JOptionPane.showOptionDialog(that.editor, "This name is already in use!", "Error",
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

	/**
	 * <p>add.</p>
	 *
	 * @param element a {@link java.lang.String} object.
	 */
	public void add(String element) {
		DefaultMutableTreeNode childNode = this.add(element, this.rootNode);

		if(childNode != null) {
			this.setSelectionPath(new TreePath(childNode.getPath()));
		}
	}

	/**
	 * <p>add.</p>
	 *
	 * @param element a {@link java.lang.String} object.
	 * @param rootNodeName a {@link java.lang.String} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.TreeNode} object.
	 */
	public TreeNode add(String element, String rootNodeName) {
		return this.add(element, this.getNodeByName(rootNodeName, this.rootNode));
	}

	/**
	 * <p>add.</p>
	 *
	 * @param element a {@link java.lang.String} object.
	 * @param rootNode a {@link javax.swing.tree.DefaultMutableTreeNode} object.
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.ruleeditor.util.TreeNode} object.
	 */
	public TreeNode add(String element, DefaultMutableTreeNode rootNode) {
		if(this.getNodeByName(element, this.rootNode) != null) {
			return null;
		}

		TreeNode childNode = new TreeNode(element);
		this.model.insertNodeInto(childNode, rootNode, rootNode.getChildCount());
		this.scrollPathToVisible(new TreePath(childNode.getPath()));

		return childNode;
	}

	/** {@inheritDoc} */
	public void setSelectionPath(TreePath path) {
		this.activePath = path;

		super.setSelectionPath(path);
	}

	/**
	 * <p>deSelect.</p>
	 */
	public void deSelect() {
		super.removeSelectionPath(this.activePath);

		this.activePath = null;
	}

	/**
	 * <p>edit.</p>
	 */
	public void edit() {
		if(this.activePath != null) {
			this.startEditingAtPath(this.activePath);
		}
	}

	/**
	 * <p>delete.</p>
	 */
	public void delete() {
		if(this.activePath != null) {
			int returnValue = JOptionPane.showOptionDialog(
					this.editor, "Do you really want to delete the selected element?",
					"Delete selected element", JOptionPane.YES_NO_OPTION,
					JOptionPane.INFORMATION_MESSAGE, null,
					new String[] { "YES", "NO" }, 0);

			if (returnValue == 0) {
				this.editor.getTreePane().getMoveRulePanel().disableButtons();

				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) this.activePath.getLastPathComponent();
				TreeNode parentNode = (TreeNode) currentNode.getParent();

				this.comp.removeElement(currentNode.getUserObject().toString(), parentNode);

				if(currentNode.getParent() != null) {
					this.model.removeNodeFromParent(currentNode);
				}

				if(this.rootNode.getChildCount() == 0) {
					this.editor.getRuleEditMenu().setEnabled(false);
					this.editor.getGenerationMenu().setEnabled(false);
				}
			}
		}
	}

	/**
	 * <p>remove.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 * @param rulePackageName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean remove(String ruleName, String rulePackageName) {
		DefaultMutableTreeNode rootNode = this.getNodeByName(rulePackageName, this.rootNode);
		DefaultMutableTreeNode node = this.getNodeByName(ruleName, rootNode);

		if(node != null) {
			this.model.removeNodeFromParent(node);
		}

		return this.getNodeByName(ruleName, this.rootNode, true) != null;
	}

	/**
	 * <p>Getter for the field <code>rootNode</code>.</p>
	 *
	 * @return a {@link javax.swing.tree.DefaultMutableTreeNode} object.
	 */
	public DefaultMutableTreeNode getRootNode() {
		return this.rootNode;
	}

	/**
	 * <p>clear.</p>
	 */
	public void clear() {
		this.rootNode.removeAllChildren();
		this.model.reload();
	}
}
