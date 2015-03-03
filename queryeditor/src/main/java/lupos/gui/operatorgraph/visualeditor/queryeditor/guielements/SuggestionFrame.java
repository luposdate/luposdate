
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
package lupos.gui.operatorgraph.visualeditor.queryeditor.guielements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.AdvancedQueryEditor;
import lupos.gui.operatorgraph.visualeditor.queryeditor.QueryEditor;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboItem;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm;
import lupos.gui.operatorgraph.visualeditor.queryeditor.util.QueryConnection;
import lupos.gui.operatorgraph.visualeditor.util.Connection;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
public class SuggestionFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private final SuggestionFrame myself = this;
	private final QueryEditor visualEditor;
	private final QueryGraph queryGraph;
	protected final QueryResult queryResult_preceding;
	protected final QueryResult queryResult_succeeding;
	protected final QueryRDFTerm op;
	protected final Variable varSO;
	protected final Variable varPred;
	protected SuggestionPanel subjectPanel;
	protected SuggestionPanel predicatePanelS;
	protected SuggestionPanel predicatePanelO;
	protected SuggestionPanel objectPanel;

	/**
	 * <p>Constructor for SuggestionFrame.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.QueryEditor} object.
	 * @param queryGraph a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.QueryGraph} object.
	 * @param queryResult_preceding a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param queryResult_succeeding a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param op a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm} object.
	 * @param varSOName a {@link java.lang.String} object.
	 * @param varPredName a {@link java.lang.String} object.
	 */
	public SuggestionFrame(final QueryEditor parent,
			final QueryGraph queryGraph,
			final QueryResult queryResult_preceding,
			final QueryResult queryResult_succeeding, final QueryRDFTerm op,
			final String varSOName, final String varPredName) {
		super(); // call constructor of JFrame

		// set variables...
		this.visualEditor = parent;
		this.queryGraph = queryGraph;
		this.queryResult_preceding = queryResult_preceding;
		this.queryResult_succeeding = queryResult_succeeding;
		this.op = op;
		this.varSO = new Variable(varSOName.substring(1));
		this.varPred = new Variable(varPredName.substring(1));

		final JPanel contentPanel = this.createContentPanel(
				new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						addPrecedingSuggestion(subjectPanel, predicatePanelS);
					}
				}, new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						addSucceedingSuggestion(predicatePanelO, objectPanel);
					}
				}); // get contentPanel

		// create main window...
		this.setTitle("Suggestions");
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.getContentPane().add(contentPanel);
		this.pack();
		this.setLocationRelativeTo(parent);
		this.setVisible(true);
	}

	/**
	 * <p>Constructor for SuggestionFrame.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.AdvancedQueryEditor} object.
	 * @param queryGraph a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.QueryGraph} object.
	 * @param queryResult_preceding a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param queryResult_succeeding a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param op a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm} object.
	 * @param varSOName a {@link java.lang.String} object.
	 * @param varPredName a {@link java.lang.String} object.
	 */
	public SuggestionFrame(final AdvancedQueryEditor parent,
			final QueryGraph queryGraph,
			final QueryResult queryResult_preceding,
			final QueryResult queryResult_succeeding, final QueryRDFTerm op,
			final String varSOName, final String varPredName) {
		super(); // call constructor of JFrame

		// set variables...
		this.visualEditor = parent;
		this.queryGraph = queryGraph;
		this.queryResult_preceding = queryResult_preceding;
		this.queryResult_succeeding = queryResult_succeeding;
		this.op = op;
		this.varSO = new Variable(varSOName);
		this.varPred = new Variable(varPredName);

		final JPanel contentPanel = this.createContentPanel(
				new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						final Item subjectItem = subjectPanel
								.getSelectedElement();
						final Item predicateItem = predicatePanelS
								.getSelectedElement();
						final String uniqueS = subjectItem.isVariable() ? "?"
								+ parent.getUniqueVariableName(subjectItem)
								: subjectItem.toString();
						final String uniqueP = predicateItem.isVariable() ? "?"
								+ parent.getUniqueVariableName(predicateItem)
								: predicateItem.toString();

						String vars = "";
						if (subjectItem.isVariable())
							vars += uniqueS + " ";
						if (predicateItem.isVariable())
							vars += uniqueP + " ";
						parent.getStatusBar().setText(
								"add preceding suggestion...");
						parent.updateAll(parent.determineNewQuery(parent
								.getCurrentBrowserQuery(), uniqueS + " "
								+ uniqueP + " " + op.toString() + ".", vars));
						parent.getStatusBar().clear();
					}
				}, new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						final Item predicateItem = predicatePanelO
								.getSelectedElement();
						final Item objectItem = objectPanel
								.getSelectedElement();
						final String uniqueO = objectItem.isVariable() ? "?"
								+ parent.getUniqueVariableName(objectItem)
								: objectItem.toString();
						final String uniqueP = predicateItem.isVariable() ? "?"
								+ parent.getUniqueVariableName(predicateItem)
								: predicateItem.toString();

						String vars = "";
						if (objectItem.isVariable())
							vars += uniqueO + " ";
						if (predicateItem.isVariable())
							vars += uniqueP + " ";
						parent.getStatusBar().setText(
								"add succeeding suggestion...");
						parent.updateAll(parent.determineNewQuery(parent
								.getCurrentBrowserQuery(), op.toString() + " "
								+ uniqueP + " " + uniqueO + ".", vars));
						parent.getStatusBar().clear();
					}
				}); // get contentPanel

		// create main window...
		this.setTitle("Suggestions");
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.getContentPane().add(contentPanel);
		this.pack();
		this.setLocationRelativeTo(parent);
		this.setVisible(true);
	}

	private JPanel createContentPanel(
			final ActionListener addPrecedingSuggestionAction,
			final ActionListener addSucceedingSuggestionAction) {
		final JPanel contentPanel = new JPanel(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weighty = 1.0;

		if (!this.queryResult_preceding.isEmpty()) {
			contentPanel
					.add(
							this
									.createPrecedingSuggestionsPanel(addPrecedingSuggestionAction),
							gbc);

			gbc.gridy++;
		}

		if (!this.queryResult_succeeding.isEmpty()) {
			if (!this.queryResult_preceding.isEmpty()) {
				contentPanel.add(new JSeparator(), gbc);

				gbc.gridy++;
			}

			contentPanel
					.add(
							this
									.createSucceedingSuggestionsPanel(addSucceedingSuggestionAction),
							gbc);

			gbc.gridy++;
		}

		final JButton btn_close = new JButton("Close");
		btn_close.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				myself.setVisible(false);
			}
		});

		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(5, 5, 5, 5);

		contentPanel.add(btn_close, gbc);

		return contentPanel;
	}

	/**
	 * <p>createPrecedingSuggestionsPanel.</p>
	 *
	 * @param addPrecedingSuggestionAction a {@link java.awt.event.ActionListener} object.
	 * @return a {@link javax.swing.JPanel} object.
	 */
	protected JPanel createPrecedingSuggestionsPanel(
			final ActionListener addPrecedingSuggestionAction) {
		System.out.println("creating preceding suggestions...");

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weighty = 1.0;

		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder(new LineBorder(Color.BLACK),
				"Preceding Suggestions"));

		final SuggestionRowPanel rowPanel = new SuggestionRowPanel(
				this.queryResult_preceding, this.varSO, this.varPred);
		rowPanel.setLayout(null);

		// --- subject - begin ---
		final LinkedList<ComboItem> variablesSubj = new LinkedList<ComboItem>();
		variablesSubj.add(new ComboItem(this.varSO));

		final HashSet<Literal> soLiterals = new HashSet<Literal>();

		for (final Bindings b : this.queryResult_preceding)
			soLiterals.add(b.get(this.varSO));

		final LinkedList<Literal> soSortedLiterals = new LinkedList<Literal>();
		soSortedLiterals.addAll(soLiterals);

		Collections.sort(soSortedLiterals);

		for (final Literal l : soSortedLiterals)
			variablesSubj.add(new ComboItem(l));

		QueryRDFTerm queryRDFTerm = new QueryRDFTerm(this.queryGraph.prefix);
		
		subjectPanel = new SuggestionPanel(this.queryGraph, queryRDFTerm, variablesSubj, rowPanel);

		final int subjectX = (int) this.queryGraph.PADDING;
		final int subjectY = (int) this.queryGraph.PADDING;
		final int subjectWidth = subjectPanel.getPreferredSize().width;
		final int subjectHeight = subjectPanel.getPreferredSize().height;

		subjectPanel.setBounds(subjectX, subjectY, subjectWidth, subjectHeight);

		rowPanel.add(subjectPanel);
		// --- subject - end ---

		// --- predicate - begin ---
		final LinkedList<ComboItem> variablesPred = new LinkedList<ComboItem>();
		variablesPred.add(new ComboItem(this.varPred));

		final HashSet<Literal> predLiterals = new HashSet<Literal>();

		for (final Bindings b : this.queryResult_preceding)
			predLiterals.add(b.get(this.varPred));

		final LinkedList<Literal> predSortedLiterals = new LinkedList<Literal>();
		predSortedLiterals.addAll(predLiterals);

		Collections.sort(predSortedLiterals);

		for (final Literal l : predSortedLiterals)
			variablesPred.add(new ComboItem(l));

		predicatePanelS = new SuggestionPanel(this.queryGraph,
				new QueryRDFTerm(this.queryGraph.prefix), variablesPred,
				rowPanel);

		final int predicateX = subjectX + subjectWidth
				+ (int) this.queryGraph.getSPACING_X();
		final int predicateY = (int) this.queryGraph.PADDING;
		final int predicateWidth = predicatePanelS.getPreferredSize().width;
		final int predicateHeight = predicatePanelS.getPreferredSize().height;

		predicatePanelS.setBounds(predicateX, predicateY, predicateWidth,
				predicateHeight);

		rowPanel.add(predicatePanelS);
		// --- predicate - end ---

		// --- object - begin ---
		final AbstractGuiComponent<Operator> objectPanel = this.op.draw(
				new GraphWrapperOperator(this.op), this.queryGraph);
		((JTextField) objectPanel.getComponent(0)).setEditable(false);
		objectPanel.setMovable(false);

		final int objectX = predicateX + predicateWidth
				+ (int) this.queryGraph.getSPACING_X();
		final int objectY = (int) this.queryGraph.PADDING;
		final int objectWidth = objectPanel.getPreferredSize().width;
		final int objectHeight = objectPanel.getPreferredSize().height;

		objectPanel.setBounds(objectX, objectY, objectWidth, objectHeight);

		rowPanel.add(objectPanel);
		// --- object - end ---

		rowPanel.setPreferredSize(new Dimension(objectX + objectWidth
				+ (int) this.queryGraph.PADDING, predicateHeight
				+ (int) (2 * this.queryGraph.PADDING)));

		panel.add(rowPanel, gbc);

		gbc.gridy++;

		final JButton btn_add = new JButton("Add preceding suggestion");
		btn_add.addActionListener(addPrecedingSuggestionAction);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(5, 5, 5, 5);

		panel.add(btn_add, gbc);

		return panel;
	}

	/**
	 * <p>createSucceedingSuggestionsPanel.</p>
	 *
	 * @param addSucceedingSuggestionAction a {@link java.awt.event.ActionListener} object.
	 * @return a {@link javax.swing.JPanel} object.
	 */
	protected JPanel createSucceedingSuggestionsPanel(
			final ActionListener addSucceedingSuggestionAction) {
		System.out.println("creating succeeding suggestions...");

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.gridx = gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weighty = 1.0;

		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder(new LineBorder(Color.BLACK),
				"Succeeding Suggestions"));

		final SuggestionRowPanel rowPanel = new SuggestionRowPanel(
				this.queryResult_succeeding, this.varSO, this.varPred);
		rowPanel.setLayout(null);

		// --- subject - begin ---
		final AbstractGuiComponent<Operator> subjectPanel = this.op.draw(
				new GraphWrapperOperator(this.op), this.queryGraph);
		((JTextField) subjectPanel.getComponent(0)).setEditable(false);
		subjectPanel.setMovable(false);

		final int subjectX = (int) this.queryGraph.PADDING;
		final int subjectY = (int) this.queryGraph.PADDING;
		final int subjectWidth = subjectPanel.getPreferredSize().width;
		final int subjectHeight = subjectPanel.getPreferredSize().height;

		subjectPanel.setBounds(subjectX, subjectY, subjectWidth, subjectHeight);

		rowPanel.add(subjectPanel);
		// --- subject - end ---

		// --- predicate - begin ---
		final LinkedList<ComboItem> variablesPred = new LinkedList<ComboItem>();
		variablesPred.add(new ComboItem(this.varPred));

		final HashSet<Literal> predLiterals = new HashSet<Literal>();

		for (final Bindings b : this.queryResult_succeeding)
			predLiterals.add(b.get(this.varPred));

		final LinkedList<Literal> predSortedLiterals = new LinkedList<Literal>();
		predSortedLiterals.addAll(predLiterals);

		Collections.sort(predSortedLiterals);

		for (final Literal l : predSortedLiterals)
			variablesPred.add(new ComboItem(l));

		predicatePanelO = new SuggestionPanel(this.queryGraph, this.op,
				variablesPred, rowPanel);

		final int predicateX = subjectX + subjectWidth
				+ (int) this.queryGraph.getSPACING_X();
		final int predicateY = (int) this.queryGraph.PADDING;
		final int predicateWidth = predicatePanelO.getPreferredSize().width;
		final int predicateHeight = predicatePanelO.getPreferredSize().height;

		predicatePanelO.setBounds(predicateX, predicateY, predicateWidth,
				predicateHeight);

		rowPanel.add(predicatePanelO);
		// --- predicate - end ---

		// --- object - begin ---
		final LinkedList<ComboItem> variablesObj = new LinkedList<ComboItem>();
		variablesObj.add(new ComboItem(this.varSO));

		final HashSet<Literal> soLiterals = new HashSet<Literal>();

		for (final Bindings b : this.queryResult_succeeding)
			soLiterals.add(b.get(this.varSO));

		final LinkedList<Literal> soSortedLiterals = new LinkedList<Literal>();
		soSortedLiterals.addAll(soLiterals);

		Collections.sort(soSortedLiterals);

		for (final Literal l : soSortedLiterals)
			variablesObj.add(new ComboItem(l));

		QueryRDFTerm queryRDFTerm = new QueryRDFTerm(this.queryGraph.prefix);
		
		objectPanel = new SuggestionPanel(this.queryGraph, queryRDFTerm, variablesObj, rowPanel);

		final int objectX = predicateX + predicateWidth
				+ (int) this.queryGraph.getSPACING_X();
		final int objectY = (int) this.queryGraph.PADDING;
		final int objectWidth = objectPanel.getPreferredSize().width;
		final int objectHeight = objectPanel.getPreferredSize().height;

		objectPanel.setBounds(objectX, objectY, objectWidth, objectHeight);

		rowPanel.add(objectPanel);
		// --- object - end ---

		rowPanel.setPreferredSize(new Dimension(objectX + objectWidth
				+ (int) this.queryGraph.PADDING, predicateHeight
				+ (int) (2 * this.queryGraph.PADDING)));

		panel.add(rowPanel, gbc);

		gbc.gridy++;

		final JButton btn_add = new JButton("Add succeeding suggestion");
		btn_add.addActionListener(addSucceedingSuggestionAction);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(5, 5, 5, 5);

		panel.add(btn_add, gbc);

		return panel;
	}

	/**
	 * <p>addPrecedingSuggestion.</p>
	 *
	 * @param subjectPanel a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionPanel} object.
	 * @param predicatePanel a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionPanel} object.
	 */
	protected void addPrecedingSuggestion(final SuggestionPanel subjectPanel,
			final SuggestionPanel predicatePanel) {
		// get items...
		final Item subjectItem = subjectPanel.getSelectedElement();
		final Item predicateItem = predicatePanel.getSelectedElement();

		// add the subject operator...
		this.visualEditor.prepareOperatorForAdd(QueryRDFTerm.class);
		final Operator subjectOp = this.queryGraph.addOperator(5, 5,
				subjectItem);

		// create the connection between subject and object operator with the
		// right predicate item...
		final Connection<Operator> connectionMode = new QueryConnection(
				this.visualEditor);
		connectionMode.setConnectionContent(predicateItem);
		connectionMode.addOperator(subjectOp);
		connectionMode.addOperator(this.op);

		// sort elements of the outer reference...
		final OperatorContainer opContainer = (OperatorContainer) this.queryGraph.outerReference
				.getOperator();

		if (opContainer.getOperators().contains(this.op))
			opContainer.removeOperator(this.op);

		opContainer.addOperator(subjectOp);

		this.queryGraph.arrange(Arrange.values()[0]);
	}

	/**
	 * <p>addSucceedingSuggestion.</p>
	 *
	 * @param predicatePanel a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionPanel} object.
	 * @param objectPanel a {@link lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionPanel} object.
	 */
	protected void addSucceedingSuggestion(
			final SuggestionPanel predicatePanel,
			final SuggestionPanel objectPanel) {
		// get items...
		final Item predicateItem = predicatePanel.getSelectedElement();
		final Item objectItem = objectPanel.getSelectedElement();

		// add the object operator...
		this.visualEditor.prepareOperatorForAdd(QueryRDFTerm.class);
		final Operator objectOp = this.queryGraph.addOperator(5, 5, objectItem);

		// create the connection between subject and object operator with the
		// right predicate item...
		final Connection<Operator> connectionMode = new QueryConnection(
				this.visualEditor);
		connectionMode.setConnectionContent(predicateItem);
		connectionMode.addOperator(this.op);
		connectionMode.addOperator(objectOp);

		this.queryGraph.arrange(Arrange.values()[0]);
	}
}
