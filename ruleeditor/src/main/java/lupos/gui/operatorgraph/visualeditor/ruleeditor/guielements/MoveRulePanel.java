/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import lupos.gui.operatorgraph.visualeditor.ruleeditor.RuleEditor;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.AssociationsContainer;
import lupos.gui.operatorgraph.visualeditor.ruleeditor.util.Tree;

public class MoveRulePanel extends JPanel {
	private static final long serialVersionUID = 5162659603147323428L;

	private MoveRulePanel that = this;

	private JButton upButton = null;
	private JButton downButton = null;

	private Tree tree = null;
	private DefaultTreeModel treeModel = null;
	private DefaultMutableTreeNode treeNode = null;

	private AssociationsContainer associationsContainer = null;

	public MoveRulePanel(AssociationsContainer associationsContainer, Tree tree) {
		super(new FlowLayout(FlowLayout.CENTER));

		this.associationsContainer = associationsContainer;
		this.tree = tree;
		this.treeModel = (DefaultTreeModel) this.tree.getModel();

		this.upButton = new JButton(new ImageIcon(RuleEditor.class.getResource("/arrow_up.png")));
		this.upButton.setEnabled(false);
		this.upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				that.moveNode(-1);
			}
		});

		this.downButton = new JButton(new ImageIcon(RuleEditor.class.getResource("/arrow_down.png")));
		this.downButton.setEnabled(false);
		this.downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				that.moveNode(1);
			}
		});

		this.add(this.upButton);
		this.add(this.downButton);
	}

	public void disableButtons() {
		this.upButton.setEnabled(false);
		this.downButton.setEnabled(false);
	}

	public void update(TreeNode treeNode, int index, int count) {
		this.treeNode = (DefaultMutableTreeNode) treeNode;

		this.disableButtons();

		if(count > 1) {
			if(index == 0) {
				this.downButton.setEnabled(true);
			}
			else if(index == count-1) {
				this.upButton.setEnabled(true);
			}
			else {
				this.upButton.setEnabled(true);
				this.downButton.setEnabled(true);
			}
		}
	}

	private void moveNode(int difference) {
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.treeNode.getParent();
		int index = parentNode.getIndex(this.treeNode);

		this.treeModel.removeNodeFromParent(this.treeNode);

		this.treeModel.insertNodeInto(this.treeNode, parentNode, index + difference);
		this.tree.setSelectionPath(new TreePath(this.treeNode.getPath()));

		this.associationsContainer.moveRule(parentNode.getUserObject().toString(), index, difference);
	}
}