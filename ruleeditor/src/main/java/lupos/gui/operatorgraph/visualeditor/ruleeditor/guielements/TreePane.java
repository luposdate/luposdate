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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RuleContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.RulePackageContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.ScrollPane;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.Tree;

public class TreePane extends JSplitPane {
	private static final long serialVersionUID = 4608035754670555581L;

	private TreePane that = this;

	private RuleEditor editor = null;
	private RuleContainer ruleContainer = null;
	private RulePackageContainer rulePackageContainer = null;

	private int splitPaneHeight = 300;

	private Tree tree_rulePackages = null;
	private Tree tree_unassigned = null;
	private Tree currentTree = null;

	private MoveRulePanel moveRulePanel = null;

	public TreePane(RuleEditor editor, RuleContainer ruleContainer, RulePackageContainer rulePackageContainer) {
		super(JSplitPane.VERTICAL_SPLIT);

		this.editor = editor;
		this.ruleContainer = ruleContainer;
		this.rulePackageContainer = rulePackageContainer;

		this.setContinuousLayout(true);
		this.setOneTouchExpandable(true);
		this.setResizeWeight(0.5);
		this.setTopComponent(this.generateTopComponent());
		this.setBottomComponent(this.generateBottomComponent());
		this.setDividerLocation(this.splitPaneHeight);
	}


	private JPanel generateTopComponent() {
		this.tree_rulePackages = new Tree(this.rulePackageContainer, that.editor);
		this.tree_rulePackages.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				if(tse.isAddedPath()) {
					that.tree_unassigned.deSelect();
					that.currentTree = that.tree_rulePackages;
					that.ruleContainer.cancelModi();
				}
				else {
					return;
				}

				// get selected item...
				TreePath treePath = tse.getPath();
				TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();

				if(treePath.getPathCount() == 3) {
					TreeNode parentNode = treeNode.getParent();
					int count = parentNode.getChildCount();
					int index = parentNode.getIndex(treeNode);

					that.moveRulePanel.update(treeNode, index, count);
					that.ruleContainer.showRule(treeNode.toString());
				}
				else {
					that.moveRulePanel.disableButtons();
					that.rulePackageContainer.showRulePackage(treeNode.toString());
				}
			}
		});

		this.moveRulePanel = new MoveRulePanel(this.editor.getAssociationsContainer(), this.tree_rulePackages);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.BOTH;


		JPanel topComp = new JPanel(new GridBagLayout());

		topComp.add(this.moveRulePanel, gbc);
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 0, 0, 0);

		topComp.add(new ScrollPane(this.tree_rulePackages), gbc);

		return topComp;
	}

	public void addNewRulePackage(RulePackagePanel newRulePackage) {
		this.tree_rulePackages.add(newRulePackage.toString());
	}

	public void clearTopComponent() {
		this.tree_rulePackages.clear();
	}


	private JPanel generateBottomComponent() {
		this.tree_unassigned = new Tree(this.ruleContainer, that.editor);
		this.tree_unassigned.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				if(tse.isAddedPath()) {
					that.tree_rulePackages.deSelect();
					that.moveRulePanel.disableButtons();
					that.currentTree = that.tree_unassigned;
					that.ruleContainer.cancelModi();
				}
				else {
					return;
				}

				// get selected item...
				TreeNode treeNode = (TreeNode) tse.getPath().getLastPathComponent();

				that.ruleContainer.showRule(treeNode.toString());
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0;
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.BOTH;


		JPanel bottomComp = new JPanel(new GridBagLayout());

		bottomComp.add(new JLabel("not associated rules"), gbc);
		gbc.gridy++;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 0, 0, 0);

		bottomComp.add(new ScrollPane(this.tree_unassigned), gbc);

		return bottomComp;
	}

	public void addNewRule(RulePanel newRule) {
		this.tree_unassigned.add(newRule.toString());
	}

	public void clearBottomComponent() {
		this.tree_unassigned.clear();
	}


	public void renameElement() {
		this.currentTree.edit();
	}

	public void deleteElement() {
		this.currentTree.delete();
	}


	public void addAssociation(String rulePackageName, String ruleName) {
		this.addAssociation(rulePackageName, ruleName, false);
	}

	public void addAssociation(String rulePackageName, String ruleName, boolean select) {
		this.tree_unassigned.remove(ruleName, "root");
		lupos.gui.operatorgraph.visualeditor.ruleeditor.util.TreeNode node = this.tree_rulePackages.add(ruleName, rulePackageName);

		if(select) {
			this.tree_rulePackages.addSelectionPath(new TreePath(node.getPath()));
		}
	}

	public void removeAssociation(String rulePackageName, String ruleName) {
		boolean ret = this.tree_rulePackages.remove(ruleName, rulePackageName);

		if(!ret) {
			this.tree_unassigned.add(ruleName, "root");
		}
	}


	public void updateTopComponent(String oldRuleName, String newRuleName) {
		DefaultMutableTreeNode rootNode = this.tree_rulePackages.getRootNode();

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


	public MoveRulePanel getMoveRulePanel() {
		return this.moveRulePanel;
	}
}