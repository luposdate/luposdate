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
package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveData;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveDataWithProjectionAndSolutionModifier;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveDataWithSolutionModifier;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Select;
import lupos.gui.operatorgraph.visualeditor.queryeditor.util.SortContainer;
import lupos.gui.operatorgraph.visualeditor.util.FocusThread;
import lupos.gui.operatorgraph.visualeditor.util.JCheckBoxOwnIcon;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.util.ModificationException;
public class RetrieveDataPanel extends AbstractGuiComponent<Operator> {
	private static final long serialVersionUID = 1L;
	private RetrieveData operator;
	private LinkedList<JTextFieldResizing> textElements = new LinkedList<JTextFieldResizing>();
	private LinkedList<JTextFieldResizing> projElementsList = new LinkedList<JTextFieldResizing>();
	private LinkedList<JLabel> projElementsDelLabelsList = new LinkedList<JLabel>();
	private LinkedList<JTextFieldResizing> fromElementsList = new LinkedList<JTextFieldResizing>();
	private LinkedList<JLabel> fromElementsDelLabelsList = new LinkedList<JLabel>();
	private LinkedList<JTextFieldResizing> fromNamedElementsList = new LinkedList<JTextFieldResizing>();
	private LinkedList<JLabel> fromNamedElementsDelLabelsList = new LinkedList<JLabel>();
	private LinkedList<JComboBox> orderByComboBoxesList = new LinkedList<JComboBox>();
	private LinkedList<JTextFieldResizing> orderByElementsList = new LinkedList<JTextFieldResizing>();
	private LinkedList<JLabel> orderByElementsDelLabelsList = new LinkedList<JLabel>();
	private JTextFieldResizing limitTF = new JTextFieldResizing("", this.parent.getFONT(), this);
	private JTextFieldResizing offsetTF = new JTextFieldResizing("", this.parent.getFONT(), this);
	private HashMap<String, Boolean> elementStatus = new HashMap<String, Boolean>();

	/**
	 * <p>Constructor for RetrieveDataPanel.</p>
	 *
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveData} object.
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public RetrieveDataPanel(GraphWrapper gw, RetrieveData operator, VisualGraph<Operator> parent, String name) {
		super(parent, gw, operator, true);

		this.operator = operator;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
		nameLabel.setFont(parent.getFONT());

		this.add(nameLabel);
		this.add(Box.createRigidArea(new Dimension(10, 3)));
	}

	/**
	 * <p>addProjections.</p>
	 *
	 * @param distinct a boolean.
	 */
	public void addProjections(boolean distinct) {
		final RetrieveDataWithProjectionAndSolutionModifier operator = (RetrieveDataWithProjectionAndSolutionModifier) this.operator;

		JPanel projectionPanel = this.getRowPanel(); // get panel for row

		if(distinct == true) {
			// add ComboBox to panel row...
			projectionPanel.add(this.createDistinctCoBo(operator));
			projectionPanel.add(Box.createRigidArea(new Dimension(10, 3))); // spacer
		}

		// determine initial state of projection CheckBox...
		boolean projection = operator.getProjectionElements().size() > 0;

		// create projection CheckBox...
		final JCheckBoxOwnIcon projCB = new JCheckBoxOwnIcon("Projection", projection, this.parent.getFONT());
		projCB.setOpaque(false);

		elementStatus.put("projection", projection);

		projectionPanel.add(projCB); // add projection CheckBox to row panel

		final JPanel projElementsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		projElementsPanel.setOpaque(false);

		// walk through projection elements of the SELECT-Operator...
		for(int i = 0; i < operator.getProjectionElements().size(); ++i) {
			// get current projection element...
			String projString = operator.getProjectionElements().get(i);

			// create new projection element panel for the current projection
			// element and set the initial state...
			JPanel projectionElement = this.createProjectionElement(operator, projection, i, projString);

			// add projection element panel to the right panel...
			projElementsPanel.add(projectionElement);
		}

		projectionPanel.add(projElementsPanel); // add panel for projection
		// elements to row panel

		final LinkedList<JComponent> needsEnableList = new LinkedList<JComponent>();

		if(operator.getProjectionElements().size() == 0) {
			// create new projection element panel...
			JPanel projectionElement = createProjectionElement(operator, true, operator.getProjectionElements().size(), "");
			projectionElement.setVisible(projection);

			projElementsPanel.add(projectionElement); // add projection element
			// panel to the right
			// panel

			needsEnableList.add(projectionElement);
		}

		// create add button...
		final JLabel addLabel = new JLabel(this.parent.addIcon);
		addLabel.setVisible(projection);
		addLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent me) {
				// create new projection element panel...
				JPanel projectionElement = createProjectionElement(operator, true, operator.getProjectionElements().size(), "");

				// add projection element panel to the right panel...
				projElementsPanel.add(projectionElement);

				updateSize(); // update the width of the SelectPanel
			}
		});

		projectionPanel.add(addLabel); // add add-button to row panel

		needsEnableList.add(addLabel);

		// define listener for projection CheckBox...
		projCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				// get new state...
				boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

				elementStatus.put("projection", selected);

				// walk through projection TextFields...
				for(JTextField jtf : projElementsList) {
					jtf.setEnabled(selected); // set new state to current
					// TextField

					if(selected) { // if state is selected and field is not
						// empty...
						try {
							if(!jtf.getText().equals("")) {
								// add current element to projection list of SelectOP...
								operator.addProjectionElement(jtf.getText());
							}
						}
						catch(ModificationException me) {
							final int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

							if(n == JOptionPane.YES_OPTION) {
								(new FocusThread(jtf)).start();
							}
						}
					}
				}

				// walk through Delete-Labels...
				for(JLabel delLabel : projElementsDelLabelsList) {
					delLabel.setEnabled(selected); // set new state of current
					// Delete-Label
				}

				if(!selected) { // if state is not selected...
					operator.clearProjectionList(); // clear projection list of
					// SelectOP
					addLabel.setVisible(false);
				}
				else {
					// if state is selected...
					for(JComponent jc : needsEnableList) {
						jc.setVisible(true);
					}
				}

				updateSize(); // update the size of the SelectPanel
			}
		});

		this.add(projectionPanel);
	}

	/**
	 * <p>addDatasetClause.</p>
	 */
	public void addDatasetClause() {
		this.createFromElements();
		this.createFromNamedElements();
	}

	/**
	 * <p>addSolutionModifier.</p>
	 */
	public void addSolutionModifier() {
		this.createOrderByElements();
		this.createLimitOffsetElements();
	}

	/**
	 * <p>finalize.</p>
	 */
	public void finalize() {
		this.setPreferredSize(new Dimension(this.getPreferredSize().width + 10, this.getPreferredSize().height + 10));
	}

	private JComboBox createDistinctCoBo(RetrieveDataWithProjectionAndSolutionModifier op) {
		final Select operator = (Select) op;

		// create new ComboBox...
		final JComboBox distinctCoBo = new JComboBox(new String[] { "Distinct", "Reduced", "No Distinct" });
		distinctCoBo.setFont(this.parent.getFONT());

		// select right element of ComboBox...
		if(operator.distinctState == Select.DistinctState.DISTINCT) {
			distinctCoBo.setSelectedIndex(0);
		}
		else if(operator.distinctState == Select.DistinctState.REDUCED) {
			distinctCoBo.setSelectedIndex(1);
		}
		else {
			distinctCoBo.setSelectedIndex(2);
		}

		// define listener for distinct ComboBox...
		distinctCoBo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int chosen = distinctCoBo.getSelectedIndex(); // get new
				// state

				// set distinctState in selectOP...
				if(chosen == 0) {
					operator.distinctState = Select.DistinctState.DISTINCT;
				}
				else if(chosen == 1) {
					operator.distinctState = Select.DistinctState.REDUCED;
				}
				else if(chosen == 2) {
					operator.distinctState = Select.DistinctState.NO_DISTINCT;
				}
			}
		});

		return distinctCoBo;
	}

	private void createFromElements() {
		JPanel fromPanel = this.getRowPanel(); // get panel for row

		// determine initial state of from CheckBox...
		boolean from = this.operator.getFromList().size() > 0;

		// create from CheckBox...
		JCheckBoxOwnIcon fromCB = new JCheckBoxOwnIcon("From", from, this.parent.getFONT());
		fromCB.setOpaque(false);

		elementStatus.put("from", from);

		fromPanel.add(fromCB); // add from CheckBox to row panel

		final JPanel fromElementsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fromElementsPanel.setOpaque(false);

		// walk through from elements of the select operator...
		for(int i = 0; i < this.operator.getFromList().size(); ++i) {
			String fromString = this.operator.getFromList().get(i);

			// create new from element panel for the current from element and
			// set the initial state...
			JPanel fromElement = this.createFromElement(from, i, ((VisualGraphOperatorWithPrefix) this.parent).prefix.add(fromString));

			fromElementsPanel.add(fromElement); // add from element panel to row
			// panel
		}

		fromPanel.add(fromElementsPanel); // add panel for from elements to row
		// panel

		final LinkedList<JComponent> needsEnableList = new LinkedList<JComponent>();

		if(this.operator.getFromList().size() == 0) {
			// create new from element panel...
			JPanel fromElement = createFromElement(true, this.operator.getFromList().size(), "");
			fromElement.setVisible(from);

			fromElementsPanel.add(fromElement); // add from element panel to row
			// panel

			needsEnableList.add(fromElement);
		}

		// create add button...
		final JLabel addLabel = new JLabel(this.parent.addIcon);
		addLabel.setVisible(from);
		addLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(final MouseEvent me) {
				// create new from element panel...
				JPanel fromElement = createFromElement(true, operator.getFromList().size(), "");

				fromElementsPanel.add(fromElement); // add from element panel to
				// row panel

				updateSize(); // update the size of the SelectPanel
			}
		});

		fromPanel.add(addLabel); // add add-button to row panel

		needsEnableList.add(addLabel);

		// define listener for from CheckBox...
		fromCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				// get new state...
				boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

				elementStatus.put("from", selected);

				// walk through from TextFields...
				for(JTextField jtf : fromElementsList) {
					jtf.setEnabled(selected); // set new state to current
					// TextField

					if(selected) { // if state is selected and field is not
						// empty...
						try {
							if(!jtf.getText().equals("")) {
								operator.addFromItem(jtf.getText()); // add
								// current
								// element
								// to
								// from
								// list
								// of
								// SelectOP
							}
						}
						catch(ModificationException me) {
							int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

							if(n == JOptionPane.YES_OPTION) {
								(new FocusThread(jtf)).start();
							}
						}
					}
				}

				// walk through Delete-Labels...
				for(JLabel delLabel : fromElementsDelLabelsList) {
					delLabel.setEnabled(selected); // set new state of current
					// Delete-Label
				}

				if(!selected) {// if state is not selected...
					operator.clearFromList(); // clear from list of SelectOP
					addLabel.setVisible(false);
				}
				else {
					for(JComponent jc : needsEnableList) {
						jc.setVisible(true);
					}
				}

				updateSize(); // update the size of the SelectPanel
			}
		});

		this.add(fromPanel);
	}

	private void createFromNamedElements() {
		JPanel fromNamedPanel = this.getRowPanel(); // get panel for row

		// determine initial state of fromNamed CheckBox...
		boolean fromNamed = this.operator.getFromNamedList().size() > 0;

		// create fromNamed CheckBox...
		JCheckBoxOwnIcon fromNamedCB = new JCheckBoxOwnIcon("From Named", fromNamed, this.parent.getFONT());
		fromNamedCB.setOpaque(false);

		elementStatus.put("fromNamed", fromNamed);

		fromNamedPanel.add(fromNamedCB); // add fromNamed CheckBox to row panel

		// create panel for fromNamed elements...
		final JPanel fromNamedElementsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fromNamedElementsPanel.setOpaque(false);

		// walk through fromNamed elements of the select operator...
		for(int i = 0; i < this.operator.getFromNamedList().size(); ++i) {
			String fromNamedString = this.operator.getFromNamedList().get(i);

			// create new fromNamed element panel for the current fromNamed
			// element and set the initial state...
			JPanel fromNamedElement = this.createFromNamedElement(fromNamed, i, ((VisualGraphOperatorWithPrefix) this.parent).prefix.add(((VisualGraphOperatorWithPrefix) this.parent).prefix.add(fromNamedString)));

			fromNamedElementsPanel.add(fromNamedElement); // add fromNamed
			// element panel to
			// row panel
		}

		fromNamedPanel.add(fromNamedElementsPanel); // add panel for fromNamed
		// elements to row panel

		final LinkedList<JComponent> needsEnableList = new LinkedList<JComponent>();

		if(this.operator.getFromNamedList().size() == 0) {
			// create new JTextField...
			JPanel fromNamedElement = createFromNamedElement(true, operator.getFromNamedList().size(), "");
			fromNamedElement.setVisible(fromNamed);

			fromNamedElementsPanel.add(fromNamedElement); // add fromNamed
			// element panel to
			// row panel

			needsEnableList.add(fromNamedElement);
		}

		// create add button...
		final JLabel addLabel = new JLabel(this.parent.addIcon);
		addLabel.setVisible(fromNamed);
		addLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				// create new fromNamed element panel...
				JPanel fromNamedElement = createFromNamedElement(true, operator.getFromNamedList().size(), "");

				fromNamedElementsPanel.add(fromNamedElement); // add fromNamed
				// element panel
				// to row panel

				updateSize(); // update the size of the SelectPanel
			}
		});

		fromNamedPanel.add(addLabel); // add add-button to row panel

		needsEnableList.add(addLabel);

		// define listener for fromNamed CheckBox...
		fromNamedCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				// get new state...
				boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

				elementStatus.put("fromNamed", selected);

				// walk through fromNamed TextFields...
				for(JTextField jtf : fromNamedElementsList) {
					jtf.setEnabled(selected); // set new state to current
					// TextField

					if(selected) { // if state is selected and field is not
						// empty...
						try {
							if(!jtf.getText().equals("")) {
								operator.addFromNamedItem(jtf.getText()); // add
								// current
								// element
								// to
								// fromNamed
								// list
								// of
								// SelectOP
							}
						}
						catch(ModificationException me) {
							int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

							if(n == JOptionPane.YES_OPTION) {
								(new FocusThread(jtf)).start();
							}
						}
					}
				}

				// walk through Delete-Labels...
				for(JLabel delLabel : fromNamedElementsDelLabelsList) {
					delLabel.setEnabled(selected); // set new state of current
					// Delete-Label
				}

				if(!selected) { // if state is not selected...
					operator.clearFromNamedList(); // clear fromNamed list of
					// SelectOP
					addLabel.setVisible(false);
				}
				else {
					for(JComponent jc : needsEnableList) {
						jc.setVisible(true);
					}
				}

				updateSize(); // update the size of the SelectPanel
			}
		});

		this.add(fromNamedPanel);
	}

	private void createOrderByElements() {
		final RetrieveDataWithSolutionModifier operator = (RetrieveDataWithSolutionModifier) this.operator;

		JPanel orderByPanel = this.getRowPanel(); // get panel for row

		// determine initial state of orderBy CheckBox...
		boolean orderBy = operator.getOrderByList().size() > 0;

		// create orderBy CheckBox...
		JCheckBoxOwnIcon orderByCB = new JCheckBoxOwnIcon("Order By", orderBy, this.parent.getFONT());
		orderByCB.setOpaque(false);

		elementStatus.put("orderBy", orderBy);

		orderByPanel.add(orderByCB); // add orderBy CheckBox to row panel

		// create panel for orderBy elements...
		final JPanel orderByElementsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		orderByElementsPanel.setOpaque(false);

		// walk through orderBy elements of the select operator...
		for(int i = 0; i < operator.getOrderByList().size(); ++i) {
			SortContainer sortContainer = operator.getOrderByList().get(i);

			// add spacer if this is not the first element...
			if(this.orderByElementsList.size() > 0) {
				orderByElementsPanel.add(Box.createRigidArea(new Dimension(10, 3)));
			}

			// determine right selectedIndex for ComboBox...
			int selectedIndex = (sortContainer.isDesc()) ? 1 : 0;

			// create new orderBy element panel and set the initial state...
			JPanel orderByElement = this.createOrderByElement(orderBy, i, sortContainer, selectedIndex, sortContainer.getSortString());

			orderByElementsPanel.add(orderByElement); // add orderBy element
			// panel to row panel
		}

		orderByPanel.add(orderByElementsPanel); // add panel for orderBy
		// elements to row panel

		final LinkedList<JComponent> needsEnableList = new LinkedList<JComponent>();

		if(operator.getOrderByList().size() == 0) {
			SortContainer sortContainer = null;

			try {
				sortContainer = new SortContainer(((VisualGraphOperatorWithPrefix) this.parent).prefix, false, "");
				sortContainer.setOperator(operator);
			}
			catch(ModificationException me) {
				me.printStackTrace();
			}

			// create new orderBy element panel and set the initial state...
			JPanel orderByElement = this.createOrderByElement(true, operator.getOrderByList().size(), sortContainer, 0, "");
			orderByElement.setVisible(orderBy);

			orderByElementsPanel.add(orderByElement); // add orderBy element
			// panel to row panel

			needsEnableList.add(orderByElement);
		}

		// create add button...
		final JLabel addLabel = new JLabel(this.parent.addIcon);
		addLabel.setVisible(orderBy);
		addLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				// add spacer if this is not the first element...
				if(orderByElementsList.size() > 0) {
					orderByElementsPanel.add(Box.createRigidArea(new Dimension(10, 3)));
				}

				SortContainer sortContainer = null;

				try {
					sortContainer = new SortContainer(((VisualGraphOperatorWithPrefix) parent).prefix, false, "");
					sortContainer.setOperator(operator);
				}
				catch(ModificationException mex) {
					mex.printStackTrace();
				}

				operator.addOrderByElement(sortContainer);

				// create new orderBy element panel and set the initial state...
				JPanel orderByElement = createOrderByElement(true, operator.getOrderByList().size(), sortContainer, 0, "");

				orderByElementsPanel.add(orderByElement); // add orderBy element
				// panel to row
				// panel

				updateSize(); // update the size of the RetrieveDataPanel
			}
		});

		orderByPanel.add(addLabel); // add add button to row panel

		needsEnableList.add(addLabel);

		// define listener for orderBy CheckBox...
		orderByCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				// get new state...
				boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

				elementStatus.put("orderBy", selected);

				// walk through fromNamed TextFields...
				for(int i = 0; i < orderByComboBoxesList.size(); ++i) {
					JComboBox jCoBo = orderByComboBoxesList.get(i);
					JTextField jTF = orderByElementsList.get(i);

					// set new state to current elements...
					jCoBo.setEnabled(selected);
					jTF.setEnabled(selected);

					if(selected) { // if state is selected and field is not
						// empty...
						// create SortContainer for current elements...
						SortContainer sc = null;

						try {
							sc = new SortContainer(((VisualGraphOperatorWithPrefix) parent).prefix, (jCoBo.getSelectedIndex() == 1), jTF.getText());
							sc.setOperator(operator);
						}
						catch(ModificationException me) {
							int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

							if(n == JOptionPane.YES_OPTION) {
								(new FocusThread(jTF)).start();
							}
						}

						// add current element to orderBy list of SelectOP...
						operator.addOrderByElement(sc);
					}
				}

				// walk through Delete-Labels...
				for(JLabel delLabel : orderByElementsDelLabelsList) {
					delLabel.setEnabled(selected); // set new state of current Delete-Label
				}

				if(!selected) { // if state is not selected...
					operator.setNewOrderByList(new LinkedList<SortContainer>()); // clear
					// orderBy
					// list
					// of
					// SelectOP
					addLabel.setVisible(false);
				}
				else {
					for(JComponent jc : needsEnableList) {
						jc.setVisible(true);
					}
				}

				updateSize(); // update the size of the SelectPanel
			}
		});

		this.add(orderByPanel);
	}

	private void createLimitOffsetElements() {
		final RetrieveDataWithSolutionModifier operator = (RetrieveDataWithSolutionModifier) this.operator;

		JPanel limitOffsetPanel = this.getRowPanel(); // get panel for row

		// create limit CheckBox...
		JCheckBoxOwnIcon limitCB = new JCheckBoxOwnIcon("Limit", (operator.getLimitValue() > -1), this.parent.getFONT());
		limitCB.setOpaque(false);

		elementStatus.put("limit", (operator.getLimitValue() > -1));

		limitOffsetPanel.add(limitCB); // add limit CheckBox to row panel

		this.limitTF.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				String content = limitTF.getText();

				if(!content.equals("")) {
					try {
						operator.setLimitValue(content);
					}
					catch(ModificationException me) {
						int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

						if(n == JOptionPane.YES_OPTION) {
							(new FocusThread(limitTF)).start();
						}
					}
				}
			}
		});

		if(operator.getLimitValue() > -1) { // if there is a limit value...
			this.limitTF.setText("" + operator.getLimitValue()); // set it
		}
		else { // if there is no limit value...
			this.limitTF.setText("-1"); // set "0" as default value
			this.limitTF.setEnabled(false); // disable the limit TextField
		}

		// set new preferred size of the limit TextField...
		this.limitTF.setPreferredSize(new Dimension(this.limitTF.getPreferredSize().width + 20, this.limitTF.getPreferredSize().height));

		// define listener for limit CheckBox...
		limitCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				// get new state...
				boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

				elementStatus.put("limit", selected);

				limitTF.setEnabled(selected); // enable or disable limit
				// TextField according to new
				// state

				// set limit value in selectOP...
				try {
					if(!selected) {
						operator.setLimitValue("-1");
					}
					else if(selected) {
						operator.setLimitValue(limitTF.getText());
					}
				}
				catch(ModificationException me) {
					int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

					if(n == JOptionPane.YES_OPTION) {
						(new FocusThread(limitTF)).start();
					}
				}
			}
		});

		limitOffsetPanel.add(this.limitTF); // add limit TextField to row panel

		limitOffsetPanel.add(Box.createRigidArea(new Dimension(10, 3))); // spacer

		// create offset CheckBox...
		JCheckBoxOwnIcon offsetCB = new JCheckBoxOwnIcon("Offset", (operator.getOffsetValue() > -1), this.parent.getFONT());
		offsetCB.setOpaque(false);

		elementStatus.put("offset", (operator.getOffsetValue() > -1));

		limitOffsetPanel.add(offsetCB); // add offset CheckBox to row panel

		this.offsetTF.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				String content = offsetTF.getText();

				if(!content.equals("")) {
					try {
						operator.setOffsetValue(content);
					}
					catch(ModificationException me) {
						int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

						if(n == JOptionPane.YES_OPTION) {
							(new FocusThread(offsetTF)).start();
						}
					}
				}
			}
		});

		if(operator.getOffsetValue() > -1) { // if there is an offset value...
			this.offsetTF.setText("" + operator.getOffsetValue()); // set it
		}
		else { // if there is no offset value...
			this.offsetTF.setText("0"); // set "0" as default value
			this.offsetTF.setEnabled(false); // disable the offset TextField
		}

		// set new preferred size of the offset TextField...
		this.offsetTF.setPreferredSize(new Dimension(this.offsetTF.getPreferredSize().width + 20, this.offsetTF.getPreferredSize().height));

		// define listener for offset CheckBox...
		offsetCB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				// get new state...
				boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);

				elementStatus.put("offset", selected);

				offsetTF.setEnabled(selected); // enable or disable offset
				// TextField according to new
				// state

				// set offset value in selectOP...
				try {
					if(!selected) {
						operator.setOffsetValue("-1");
					}
					else if(selected) {
						operator.setOffsetValue(offsetTF.getText());
					}
				}
				catch(ModificationException me) {
					int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

					if(n == JOptionPane.YES_OPTION) {
						(new FocusThread(offsetTF)).start();
					}
				}
			}
		});

		limitOffsetPanel.add(this.offsetTF); // add offset TextField to row
		// panel

		this.add(limitOffsetPanel);
	}

	private JPanel createProjectionElement(final RetrieveDataWithProjectionAndSolutionModifier operator, boolean active, final int tmpIndex, String projString) {
		final JPanel panel = new JPanel();
		panel.setOpaque(false);

		final JTextFieldResizing jtf = new JTextFieldResizing(projString, this.parent.getFONT(), this);
		jtf.setEnabled(active);
		jtf.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				String content = jtf.getText();

				if(!content.equals("")) {
					try {
						operator.setProjectionElement(tmpIndex, content);
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

		if(projString.equals("")) {
			jtf.setPreferredSize(new Dimension(20, jtf.getPreferredSize().height));
		}

		this.projElementsList.add(jtf); // add TextField to projections
		// TextFields list

		final JLabel delLabel = new JLabel(this.parent.delIcon);
		delLabel.setEnabled(active);
		delLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if(delLabel.isEnabled()) { // only process click if label is
					// enabled...
					projElementsList.remove(jtf);
					projElementsDelLabelsList.remove(delLabel);
					textElements.remove(jtf);

					operator.removeProjectionElement(tmpIndex); // remove
					// projection
					// element from
					// operator

					JPanel parentPanel = (JPanel) panel.getParent(); // get
					// parent
					// panel
					// of
					// element
					// panel
					parentPanel.remove(panel); // remove element panel from
					// parent panel

					updateSize(); // update the size of the RetrieveDataPanel
				}
			}
		});

		this.projElementsDelLabelsList.add(delLabel); // add Delete-Label to
		// projections
		// Delete-Labels list

		panel.add(jtf);
		panel.add(delLabel);

		return panel;
	}

	private JPanel createFromElement(boolean active, final int tmpIndex, String fromString) {
		final JPanel panel = new JPanel();
		panel.setOpaque(false);

		final JTextFieldResizing jtf = new JTextFieldResizing(((VisualGraphOperatorWithPrefix) this.parent).prefix.add(fromString), this.parent.getFONT(), this);
		jtf.setEnabled(active);
		jtf.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				String content = jtf.getText();

				if(!content.equals("")) {
					try {
						operator.setFromItem(tmpIndex, content);
					}
					catch(ModificationException me) {
						final int n = AbstractGuiComponent.showCorrectIgnoreOptionDialog(parent, me.getMessage());

						if(n == JOptionPane.YES_OPTION) {
							(new FocusThread(jtf)).start();
						}
					}
				}
			}
		});

		if(fromString.equals("")) {
			jtf.setPreferredSize(new Dimension(20, jtf.getPreferredSize().height));
		}

		this.textElements.add(jtf);
		this.fromElementsList.add(jtf); // add TextField to from TextFields list

		final JLabel delLabel = new JLabel(this.parent.delIcon);
		delLabel.setEnabled(active);
		delLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if(delLabel.isEnabled()) { // only process click if label is
					// enabled...
					fromElementsList.remove(jtf);
					fromElementsDelLabelsList.remove(delLabel);
					textElements.remove(jtf);

					operator.removeFromItem(tmpIndex); // remove from element
					// from operator

					JPanel parentPanel = (JPanel) panel.getParent(); // get
					// parent
					// panel
					// of
					// element
					// panel
					parentPanel.remove(panel); // remove element panel from
					// parent panel

					updateSize(); // update the size of the RetrieveDataPanel
				}
			}
		});

		this.fromElementsDelLabelsList.add(delLabel); // add Delete-Label to
		// from Delete-Labels
		// list

		panel.add(jtf);
		panel.add(delLabel);

		return panel;
	}

	private JPanel createFromNamedElement(boolean active, final int tmpIndex, String fromNamedString) {
		final JPanel panel = new JPanel();
		panel.setOpaque(false);

		final JTextFieldResizing jtf = new JTextFieldResizing(((VisualGraphOperatorWithPrefix) this.parent).prefix.add(fromNamedString), this.parent.getFONT(), this);
		jtf.setEnabled(active);
		jtf.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				String content = jtf.getText();

				if(!content.equals("")) {
					try {
						operator.setFromNamedItem(tmpIndex, content);
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

		if(fromNamedString.equals("")) {
			jtf.setPreferredSize(new Dimension(20, jtf.getPreferredSize().height));
		}

		this.textElements.add(jtf);
		this.fromNamedElementsList.add(jtf); // add TextField to fromNamed
		// TextFields list

		final JLabel delLabel = new JLabel(this.parent.delIcon);
		delLabel.setEnabled(active);
		delLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if(delLabel.isEnabled()) { // only process click if label is
					// enabled...
					fromNamedElementsList.remove(jtf);
					fromNamedElementsDelLabelsList.remove(delLabel);
					textElements.remove(jtf);

					operator.removeFromNamedItem(tmpIndex); // remove fromNamed
					// element from
					// operator

					final JPanel parentPanel = (JPanel) panel.getParent(); // get
					// parent
					// panel
					// of
					// element
					// panel
					parentPanel.remove(panel); // remove element panel from
					// parent panel

					updateSize(); // update the size of the RetrieveDataPanel
				}
			}
		});

		this.fromNamedElementsDelLabelsList.add(delLabel); // add Delete-Label
		// to fromNamed
		// Delete-Labels
		// list

		panel.add(jtf);
		panel.add(delLabel);

		return panel;
	}

	private JPanel createOrderByElement(boolean active, final int tmpIndex, final SortContainer sortContainer, int selectedIndex, String orderByString) {
		final RetrieveDataWithSolutionModifier operator = (RetrieveDataWithSolutionModifier) this.operator;

		final JPanel panel = new JPanel();
		panel.setOpaque(false);

		final JComboBox orderByCoBo = new JComboBox(new String[] { "ASC", "DESC" });
		orderByCoBo.setFont(this.parent.getFONT());
		orderByCoBo.setSelectedIndex(selectedIndex);
		orderByCoBo.setEnabled(active);
		orderByCoBo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int chosen = orderByCoBo.getSelectedIndex(); // get new
				// state

				// update sortContainer of current element...
				sortContainer.setDesc((chosen == 1));

				// add current element to orderBy list of SelectOP...
				operator.setOrderByElement(tmpIndex, sortContainer);
			}
		});

		this.orderByComboBoxesList.add(orderByCoBo); // add ComboBox to orderBy
		// ComboBoxes list

		final JTextFieldResizing jtf = new JTextFieldResizing(orderByString, this.parent.getFONT(), this);
		jtf.setEnabled(active);
		jtf.addFocusListener(new FocusAdapter() {
			public void focusLost(final FocusEvent fe) {
				String content = jtf.getText();

				if(!content.equals("")) {
					try {
						// update sortContainer of current element...
						sortContainer.setSortString(jtf.getText());

						// add current element to orderBy list of SelectOP...
						operator.setOrderByElement(tmpIndex, sortContainer);
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

		if(orderByString.equals("")) {
			jtf.setPreferredSize(new Dimension(20, jtf.getPreferredSize().height));
		}

		this.textElements.add(jtf);
		this.orderByElementsList.add(jtf); // add TextField to orderBy
		// TextFields list

		final JLabel delLabel = new JLabel(this.parent.delIcon);
		delLabel.setEnabled(active);
		delLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if(delLabel.isEnabled()) { // only process click if label is
					// enabled...
					orderByComboBoxesList.remove(orderByCoBo);
					orderByElementsList.remove(jtf);
					orderByElementsDelLabelsList.remove(delLabel);
					textElements.remove(jtf);

					operator.removeOrderByElement(tmpIndex); // remove orderBy
					// element from
					// operator

					JPanel parentPanel = (JPanel) panel.getParent(); // get
					// parent
					// panel
					// of
					// element
					// panel
					parentPanel.remove(panel); // remove element panel from
					// parent panel

					updateSize(); // update the size of the RetrieveDataPanel
				}
			}
		});

		this.orderByElementsDelLabelsList.add(delLabel); // add Delete-Label to
		// orderBy
		// Delete-Labels
		// list

		panel.add(orderByCoBo);
		panel.add(jtf);
		panel.add(delLabel);

		return panel;
	}

	private JPanel getRowPanel() {
		// create panel with FlowLayout and set right background...
		JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rowPanel.setOpaque(false);

		return rowPanel; // return panel
	}

	/**
	 * <p>updateSize.</p>
	 */
	public void updateSize() {
		int objWidth = 0;
		int objHeight = 0;

		// calculate needed size of the content of the RetrieveDataPanel...
		for(int i = 0; i < this.getComponentCount(); ++i) {
			objWidth = Math.max(objWidth, this.getComponent(i).getPreferredSize().width);
			objHeight += this.getComponent(i).getPreferredSize().height;
		}

		// if the needed size of the content of the RetrieveDataPanel is not
		// equal with the current size of it...
		if(objWidth != this.getPreferredSize().width || objHeight != this.getPreferredSize().height) {
			// update size of the RetrieveDataPanel...
			Dimension d = new Dimension(objWidth, objHeight);

			this.setPreferredSize(d);
			this.setSize(d);

			// update size of the GraphBox...
			this.getBox().width = objWidth;
			this.getBox().height = objHeight;
		}

		this.parent.revalidate();
		this.parent.repaint();
	}

	/**
	 * <p>prefixAdded.</p>
	 */
	public void prefixAdded() {
		for(JTextField jtf : this.textElements) {
			jtf.setText(((VisualGraphOperatorWithPrefix) this.parent).prefix.add(jtf.getText()));
		}
	}

	/**
	 * <p>prefixRemoved.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @param namespace a {@link java.lang.String} object.
	 */
	public void prefixRemoved(String prefix, final String namespace) {
		for(JTextField jtf : this.textElements) {
			String replacement = jtf.getText().replaceFirst(prefix + ":", namespace);

			if(!replacement.equals(jtf.getText())) {
				jtf.setText("<" + replacement + ">");
			}
		}
	}

	/**
	 * <p>prefixModified.</p>
	 *
	 * @param oldPrefix a {@link java.lang.String} object.
	 * @param newPrefix a {@link java.lang.String} object.
	 */
	public void prefixModified(String oldPrefix, String newPrefix) {
		for(JTextField jtf : this.textElements) {
			jtf.setText(jtf.getText().replaceFirst(oldPrefix + ":", newPrefix + ":"));
		}
	}

	/** {@inheritDoc} */
	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		// Projections...
		for(int i = 0; i < this.projElementsList.size(); ++i) {
			JTextField jtf = this.projElementsList.get(i);

			try {
				if(!jtf.getText().equals("")) {
					((RetrieveDataWithProjectionAndSolutionModifier) this.operator).setProjectionElement(i, jtf.getText());
				}
			}
			catch(ModificationException me) {
				if(showErrors) {
					JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

					jtf.grabFocus();
				}

				return false;
			}
		}

		// FROM...
		for(int i = 0; i < this.fromElementsList.size(); ++i) {
			JTextField jtf = this.fromElementsList.get(i);

			try {
				if(!jtf.getText().equals("")) {
					this.operator.setFromItem(i, jtf.getText());
				}
			}
			catch(ModificationException me) {
				if(showErrors) {
					JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

					jtf.grabFocus();
				}

				return false;
			}
		}

		// FROM NAMED...
		for(int i = 0; i < this.fromNamedElementsList.size(); ++i) {
			JTextField jtf = this.fromNamedElementsList.get(i);

			try {
				if(!jtf.getText().equals("")) {
					this.operator.setFromNamedItem(i, jtf.getText());
				}
			}
			catch(ModificationException me) {
				if(showErrors) {
					JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

					jtf.grabFocus();
				}

				return false;
			}
		}

		if(this.operator instanceof RetrieveDataWithSolutionModifier) {
			RetrieveDataWithSolutionModifier operator = (RetrieveDataWithSolutionModifier) this.operator;

			// ORDER BY...
			for(int i = 0; i < this.orderByElementsList.size(); ++i) {
				JTextField jtf = this.orderByElementsList.get(i);

				if(jtf.getText().equals("")) {
					continue;
				}

				SortContainer sortContainer = operator.getOrderByList().get(i);

				try {
					// update sortContainer of current element...
					sortContainer.setSortString(jtf.getText());

					// add current element to orderBy list of SelectOP...
					operator.setOrderByElement(i, sortContainer);
				}
				catch(ModificationException me) {
					if(showErrors) {
						JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

						jtf.grabFocus();
					}

					return false;
				}
			}

			// LIMIT...
			try {
				operator.setLimitValue(this.limitTF.getText());
			}
			catch(ModificationException me) {
				if(showErrors) {
					JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

					this.limitTF.grabFocus();
				}

				return false;
			}

			// OFFSET
			try {
				operator.setOffsetValue(this.offsetTF.getText());
			}
			catch(ModificationException me) {
				if(showErrors) {
					JOptionPane.showOptionDialog(this.parent.visualEditor, me.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

					this.offsetTF.grabFocus();
				}

				return false;
			}
		}

		return true;
	}

	/**
	 * <p>Getter for the field <code>elementStatus</code>.</p>
	 *
	 * @param elementName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean getElementStatus(String elementName) {
		return this.elementStatus.get(elementName);
	}
}
