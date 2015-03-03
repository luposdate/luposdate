
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
package lupos.gui.operatorgraph.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lupos.engine.operators.BasicOperator;
import lupos.gui.Browser;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperator;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperatorByteArray;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
public class DebugContainerToolBar<T> extends JPanel {
	private static final long serialVersionUID = -6983017259818782269L;
	private final DebugContainerToolBar<T> that = this;
	private final Viewer operatorGraphViewer;
	private final List<DebugContainer<T>> debugContainerList;
	private int curContainerPos;
	private JButton firstButton;
	private JButton prevButton;
	private JComboBox ruleCombo;
	private JButton infoButton;
	private JButton nextButton;
	private JButton lastButton;

	/**
	 * <p>Constructor for DebugContainerToolBar.</p>
	 *
	 * @param operatorGraphViewer a {@link lupos.gui.operatorgraph.viewer.Viewer} object.
	 * @param debugContainerList a {@link java.util.List} object.
	 * @param fromJar a boolean.
	 */
	public DebugContainerToolBar(final Viewer operatorGraphViewer, final List<DebugContainer<T>> debugContainerList, final boolean fromJar) {
		super();

		this.operatorGraphViewer = operatorGraphViewer;
		this.debugContainerList = debugContainerList;
		this.curContainerPos = debugContainerList.size() - 1;

		this.generateFirstButton();
		this.generatePrevButton();
		this.generateRuleCombo();
		this.generateInfoButton(fromJar);
		this.generateNextButton();
		this.generateLastButton();

		this.update();
	}

	private void generateFirstButton() {
		this.firstButton = new JButton("<<");
		this.firstButton.setToolTipText("first graph");
		this.firstButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DebugContainerToolBar.this.that.curContainerPos = 0;

				DebugContainerToolBar.this.that.reloadGraphViewer();
			}
		});

		this.add(this.firstButton);
	}

	private void generatePrevButton() {
		this.prevButton = new JButton("<");
		this.prevButton.setToolTipText("previous graph");
		this.prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DebugContainerToolBar.this.that.curContainerPos -= 1;

				DebugContainerToolBar.this.that.reloadGraphViewer();
			}
		});

		this.add(this.prevButton);
	}

	private void generateRuleCombo() {
		final String[] ruleNames = new String[this.debugContainerList.size()];

		for(int i = 0; i < this.debugContainerList.size(); i += 1) {
			ruleNames[i] = (i + 1) + ". " + this.debugContainerList.get(i).getRuleName();
		}

		this.ruleCombo = new JComboBox(ruleNames);
		this.ruleCombo.setSelectedIndex(this.curContainerPos);
		this.ruleCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DebugContainerToolBar.this.that.curContainerPos = ((JComboBox) e.getSource()).getSelectedIndex();

				DebugContainerToolBar.this.that.reloadGraphViewer();
			}
		});

		this.add(this.ruleCombo);
	}

	private void generateInfoButton(final boolean fromJar) {
		this.infoButton = new JButton("?");
		this.infoButton.setToolTipText("additional information about this rule...");
		this.infoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final DebugContainer<T> dc = DebugContainerToolBar.this.debugContainerList.get(DebugContainerToolBar.this.curContainerPos);

				if(dc == null || dc.getDescription() == null) {
					JOptionPane.showMessageDialog(DebugContainerToolBar.this.that, "No information about this rule available!", "Error", JOptionPane.ERROR_MESSAGE);
				}
				else {
					final JFrame browserFrame = new Browser("http://www.ifis.uni-luebeck.de/~groppe/tutorial_demo/ruledoc/" + dc.getDescription() + ".html", "Rule information", false);

					if(fromJar) {
						browserFrame.setIconImage(new ImageIcon(DebugContainerToolBar.class.getResource("/demo.gif")).getImage());
					}
					else {
						browserFrame.setIconImage(new ImageIcon("data" + File.separator + "demo.gif").getImage());
					}
				}
			}
		});

		this.add(this.infoButton);
	}

	private void generateNextButton() {
		this.nextButton = new JButton(">");
		this.nextButton.setToolTipText("next graph");
		this.nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DebugContainerToolBar.this.that.curContainerPos += 1;

				DebugContainerToolBar.this.that.reloadGraphViewer();
			}
		});

		this.add(this.nextButton);
	}

	private void generateLastButton() {
		this.lastButton = new JButton(">>");
		this.lastButton.setToolTipText("last graph");
		this.lastButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DebugContainerToolBar.this.that.curContainerPos = DebugContainerToolBar.this.debugContainerList.size() - 1;

				DebugContainerToolBar.this.that.reloadGraphViewer();
			}
		});

		this.add(this.lastButton);
	}

	private void reloadGraphViewer() {
		final T root = this.debugContainerList.get(this.curContainerPos).getRoot();

		if(root instanceof BasicOperator) {
			this.operatorGraphViewer.createGraphElement(new GraphWrapperBasicOperator((BasicOperator) root));
		}
		else {
			this.operatorGraphViewer.createGraphElement(new GraphWrapperBasicOperatorByteArray((BasicOperatorByteArray) root));
		}

		this.update();
	}

	private void update() {
		if(this.curContainerPos == 0) {
			this.firstButton.setEnabled(false);
			this.prevButton.setEnabled(false);
		}
		else {
			this.firstButton.setEnabled(true);
			this.prevButton.setEnabled(true);
		}

		this.ruleCombo.setSelectedIndex(this.curContainerPos);

		if(this.curContainerPos == this.debugContainerList.size() - 1) {
			this.nextButton.setEnabled(false);
			this.lastButton.setEnabled(false);
		}
		else {
			this.nextButton.setEnabled(true);
			this.lastButton.setEnabled(true);
		}

		// deal with context menus
		this.operatorGraphViewer.setupContextMenus();
	}
}
