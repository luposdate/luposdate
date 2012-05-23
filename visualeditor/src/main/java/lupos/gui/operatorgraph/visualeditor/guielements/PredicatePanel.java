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
package lupos.gui.operatorgraph.visualeditor.guielements;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.FocusThread;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;

public class PredicatePanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 1L;
	private LinkedList<JTextFieldResizing> predicateElementsList = new LinkedList<JTextFieldResizing>();
	private Prefix prefix;

	private JLabel addLabel = new JLabel();

	public PredicatePanel(VisualGraph<Operator> parent, final RDFTerm operator, final RDFTerm child, Prefix prefix) {
		super(parent, new GraphWrapperOperator(operator), operator, false);

		this.parentOp = operator;
		this.child = child;
		this.prefix = prefix;

		final JPanel rowPanel = new JPanel(new GridBagLayout());
		rowPanel.setOpaque(false);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);

		// walk through predicates and add them ...
		for (int i = 0; i < operator.getPredicates(child).size(); ++i) {
			rowPanel.add(this.createPredicateElement(i, this.prefix.add(operator.getPredicates(child).get(i).toString())), gbc);

			gbc.insets = new Insets(0, (int) this.parent.PADDING, 0, (int) this.parent.PADDING);
			gbc.gridx++;
		}

		this.setLayout(new GridBagLayout());

		final GridBagConstraints mainGBC = new GridBagConstraints();
		mainGBC.gridwidth = mainGBC.gridheight = 1;
		mainGBC.gridx = mainGBC.gridy = 0;
		mainGBC.fill = GridBagConstraints.NONE;
		mainGBC.anchor = GridBagConstraints.WEST;
		mainGBC.weighty = 1.0;
		mainGBC.insets = new Insets((int) this.parent.PADDING, (int) this.parent.PADDING, (int) this.parent.PADDING, 0);

		this.add(rowPanel, mainGBC);

		mainGBC.gridx++;
		mainGBC.insets = new Insets((int) this.parent.PADDING, (int) this.parent.PADDING, (int) this.parent.PADDING, (int) this.parent.PADDING);

		// create add button...
		this.addLabel = new JLabel(this.parent.addIcon);
		this.addLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				// create new predicate element panel...
				JPanel predicateElement = createPredicateElement(operator.getPredicates(child).size(), "");

				// add predicate element panel to the right panel...
				rowPanel.add(predicateElement, gbc);

				gbc.gridx++;

				updateSize();
			}
		});

		this.add(this.addLabel, mainGBC);
	}

	private JPanel createPredicateElement(final int tmpIndex, String predicateString) {
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);

		final JTextFieldResizing jtf = new JTextFieldResizing(predicateString, this.parent.getFONT(), this);
		jtf.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent fe) {}

			public void focusLost(FocusEvent fe) {
				String content = jtf.getText();

				if(!content.equals("")) {
					try {
						((RDFTerm) operator).setPredicate((RDFTerm) child, content, tmpIndex);
					}
					catch(ModificationException me) {
						int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

						if(n == JOptionPane.YES_OPTION) {
							(new FocusThread(jtf)).start();
						}
					}
				}
			}
		});

		if(predicateString.equals("")) {
			jtf.setPreferredSize(new Dimension(20, jtf.getPreferredSize().height));
		}

		jtf.setMinimumSize(jtf.getPreferredSize());

		this.predicateElementsList.add(jtf); // add TextField to predicates TextFields list

		final JLabel delLabel = new JLabel(this.parent.delIcon);
		delLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent me) {
				if (delLabel.isEnabled()) { // only process click if label is
					// enabled...
					final int returnValue = JOptionPane.showOptionDialog(
							parent.visualEditor,
							"Do you really want to delete this predicate?",
							"Delete predicate", JOptionPane.YES_NO_OPTION,
							JOptionPane.INFORMATION_MESSAGE, null,
							new String[] { "YES", "NO" }, 0);

					if (returnValue == 0) {
						predicateElementsList.remove(jtf);

						((RDFTerm) operator).deletePredicate((RDFTerm) child,
								tmpIndex); // remove predicate element from
						// operator

						final JPanel parentPanel = (JPanel) panel.getParent(); // get
						// parent
						// panel
						// of
						// element
						// panel
						parentPanel.remove(panel); // remove element panel from
						// parent panel

						updateSize();
					}
				}
			}
		});

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, (int) this.parent.PADDING);

		panel.add(jtf, gbc);

		gbc.gridx++;

		panel.add(delLabel, gbc);

		panel.setPreferredSize(new Dimension(panel.getPreferredSize().width,
				jtf.getPreferredSize().height));

		return panel;
	}

	public void updateSize() {
		int objWidth = (int) this.parent.PADDING;
		int objHeight = this.predicateElementsList.get(0).getPreferredSize().height + (int) (2 * this.parent.PADDING);

		// the top panel with the other elements
		Container c = (Container) this.getComponent(0);

		// the element panels
		for(int i = 0; i < c.getComponentCount(); i += 1) {
			Container con = (Container) c.getComponent(i);

			// JTextField...
			objWidth += con.getComponent(0).getPreferredSize().width;
			objWidth += (int) (2* this.parent.PADDING);

			// del-label...
			objWidth += con.getComponent(1).getPreferredSize().width;
			objWidth += (int) (2* this.parent.PADDING);
		}

		// the add-label...
		objWidth += this.getComponent(1).getPreferredSize().width;
		objWidth += (int) this.parent.PADDING;

		// if the needed size of the content of the panel is not equal with the
		// current size of it...
		if(objWidth != this.getPreferredSize().width || objHeight != this.getPreferredSize().height) {
			// update size of the panel...
			Dimension d = new Dimension(objWidth, objHeight);

			int oldCenter = this.getX() + (this.getPreferredSize().width / 2);

			this.setPreferredSize(d);
			this.setSize(d);
			this.setMinimumSize(d);

			int newCenter = this.getX() + (d.width / 2);

			int xDiff = (newCenter - oldCenter) / 2;

			int newX = this.getX() - xDiff;

			this.setLocation(newX, this.getY());

			// this.parent.revalidate();
			this.revalidate();
		}
	}

	public void prefixAdded() {
		for (final JTextField jtf : this.predicateElementsList) {
			jtf.setText(this.prefix.add(jtf.getText()));
		}
	}

	public void prefixRemoved(final String prefix, final String namespace) {
		for (final JTextField jtf : this.predicateElementsList) {
			final String replacement = jtf.getText().replaceFirst(prefix + ":",
					namespace);

			if (!replacement.equals(jtf.getText())) {
				jtf.setText("<" + replacement + ">");
			}
		}
	}

	public void prefixModified(final String oldPrefix, final String newPrefix) {
		for (final JTextField jtf : this.predicateElementsList) {
			jtf.setText(jtf.getText().replaceFirst(oldPrefix + ":",
					newPrefix + ":"));
		}
	}

	public boolean validateOperatorPanel(final boolean showErrors, Object data) {
		for (int i = 0; i < this.predicateElementsList.size(); ++i) {
			final JTextField jtf = this.predicateElementsList.get(i);

			try {
				((RDFTerm) this.operator).setPredicate((RDFTerm) this.child,
						this.prefix.add(jtf.getText()), i);
			} catch (final ModificationException me) {
				if (showErrors) {
					JOptionPane.showOptionDialog(this.parent.visualEditor, me
							.getMessage(), "Error", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE, null, null, null);

					jtf.grabFocus();
				}

				return false;
			}
		}

		return true;
	}
}