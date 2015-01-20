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
package lupos.gui.operatorgraph.visualeditor.queryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.SPARQLParser;
import lupos.gui.debug.ShowResult;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefixNonEditable;
import lupos.gui.operatorgraph.util.JTableButtonRenderer;
import lupos.gui.operatorgraph.viewer.ViewerPrefix;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.comboItemDisabler.ComboItem;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.QueryGraph;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionFrame;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionPanel;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionRowPanel;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm;
import lupos.gui.operatorgraph.util.ButtonEditor;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.ASTConstructQuery;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.ASTLimit;
import lupos.sparql1_1.ASTOffset;
import lupos.sparql1_1.ASTOrderConditions;
import lupos.sparql1_1.ASTQuery;
import lupos.sparql1_1.ASTSelectQuery;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;

public class AdvancedQueryEditor extends QueryEditor {
	private static final long serialVersionUID = 4969382663465698208L;
	protected LuposJTextPane tp_queryInput;
	protected JPanel resultpanel = null;
	private ViewerPrefix prefixInstance = null;
	private Boolean usePrefixes = true;
	private JSplitPane splitPane_result;
	private JButton buttonForward;
	private JButton buttonBackward;
	private JFrame frame;

	private final LinkedList<String> backwardList = new LinkedList<String>();
	private final LinkedList<String> forwardList = new LinkedList<String>();

	private String currentBrowserQuery;

	public enum EDITORTYPE {
		ADVANCED, VISUALQUERY, BROWSERLIKE, TEXTUALQUERY
	};

	private EDITORTYPE editorType;

	public AdvancedQueryEditor(final String query, final String data,
			final IQueryEditor component, final Image image) {
		this(query, data, component, image, EDITORTYPE.ADVANCED);
	}

	public AdvancedQueryEditor(final String query, final String data,
			final IQueryEditor component, final Image image,
			final EDITORTYPE editorType) {
		super(false);
		this.initAdvancedQueryEditor(query, data, component, image, editorType);
	}

	private void initAdvancedQueryEditor(final String query, final String data,
			final IQueryEditor component, final Image image,
			final EDITORTYPE editorType) {
		this.editorType = editorType;
		if (editorType == EDITORTYPE.ADVANCED
				|| editorType == EDITORTYPE.VISUALQUERY) {
			init(query, data, component, image);
			if (!query.equals("")) {
				if (this.tp_queryInput != null)
					this.tp_queryInput.setText(query);
				this.queryAndUpdateBrowserContent(query);
			} else
				try {
					final String queryFromVisualQuery = this
					.getQueryFromVisualQuery();
					if (this.tp_queryInput != null)
						this.tp_queryInput.setText(queryFromVisualQuery);
					this.queryAndUpdateBrowserContent(queryFromVisualQuery);
				} catch (final Exception e) {
					if (this.tp_queryInput != null)
						this.tp_queryInput.setText(query);
					this.queryAndUpdateBrowserContent(query);
				}
		} else {
			this.component = component;

			// create main window...
			this.frame = this.createMainWindowSingleGraph("LUPOSDATE-QEdit", true, image);

			if (!query.equals("")) {
				this.setVisible(true);
			} else {
				if (this.standAlone) {
					System.exit(0);
				} else {
					this.setVisible(false);
				}
			}
			if (!data.equals("")) {
				this.dataFile = data;

				this.statusBar.setText("Loading N3 data ...");

				try {
					final URILiteral rdfURL = LiteralFactory
					.createURILiteralWithoutLazyLiteral("<inlinedata:"
							+ this.dataFile + ">");

					final Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
					defaultGraphs.add(rdfURL);

					this.evaluator = MemoryIndexQueryEvaluator.class.newInstance();
					this.evaluator.setupArguments();
					this.evaluator.getArgs().set("result",
							QueryResult.TYPE.MEMORY);
					this.evaluator.getArgs().set("codemap",
							LiteralFactory.MapType.HASHMAP);
					this.evaluator.getArgs().set("type", "N3");
					this.evaluator.getArgs().set("datastructure",
							Indices.DATA_STRUCT.HASHMAP);
					this.evaluator.init();
					this.evaluator.prepareInputData(defaultGraphs,
							new LinkedList<URILiteral>());

					if (editorType == EDITORTYPE.BROWSERLIKE) {
						this.queryAndUpdateBrowserContent(query);
					}

				} catch (final RuntimeException re) {
					this.dataFile = "";

					JOptionPane.showOptionDialog(this,
							"Error while loading data file:\n"
							+ re.getMessage(), "Error",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE, null, null, null);
				} catch (final Exception e) {
					this.dataFile = "";

					e.printStackTrace();
				}

				this.statusBar.clear();
			}

			this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

			this.frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(final WindowEvent we) {
					frame.setVisible(false);
				}
			});
		}
	}

	@Override
	protected JFrame createMainWindowSingleGraph(final String title, final boolean showTopToolBar, final Image image) {
		JFrame frame;

		switch (editorType) {
		default:
		case ADVANCED:
			final JSplitPane splitPaneMain = new JSplitPane();
			splitPaneMain.setOneTouchExpandable(true);
			splitPaneMain.setContinuousLayout(true);
			splitPaneMain.setResizeWeight(0.5);

			final JSplitPane splitPaneQuery = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT);
			splitPaneQuery.setOneTouchExpandable(true);
			splitPaneQuery.setContinuousLayout(true);
			splitPaneQuery.setResizeWeight(0.8);

			//			frame = new VisualEditorFrame(editor, title, image, showTopToolBar)
			frame = this.createMainWindow(title, image);

			splitPaneQuery.setTopComponent(this.getVisualQueryComponent(
					showTopToolBar, true));
			splitPaneQuery.setBottomComponent(this
					.getTextualQueryComponent(true));
			splitPaneMain.setBottomComponent(splitPaneQuery);
			splitPaneMain.setTopComponent(this.getBrowserComponent());

			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(splitPaneMain, BorderLayout.CENTER);
			frame.getContentPane().add(this.statusBar, BorderLayout.SOUTH);
			break;
		case VISUALQUERY:
			frame = this.createMainWindow(title, image);
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(
					this.getVisualQueryComponent(showTopToolBar, false),
					BorderLayout.CENTER);
			frame.getContentPane().add(this.statusBar, BorderLayout.SOUTH);
			break;
		case BROWSERLIKE:
			frame = this.createMainWindow(title, image);
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(this.getBrowserComponent(),
					BorderLayout.CENTER);
			frame.getContentPane().add(this.statusBar, BorderLayout.SOUTH);
			break;
		case TEXTUALQUERY:
			frame = this.createMainWindow(title, image);
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(this.getTextualQueryComponent(false),
					BorderLayout.CENTER);
			frame.getContentPane().add(this.statusBar, BorderLayout.SOUTH);
			break;
		}

		return frame;
	}

	protected JFrame createMainWindow(final String title, final Image image) {
		final JFrame frame = new JFrame();

		if (this.standAlone) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (final Exception e) {
				e.printStackTrace();
			}

			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		} else {
			frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		}

		if (image != null) {
			frame.setIconImage(image);
		}

		frame.setTitle(title);

		frame.setSize(1000, 600);
		frame.setLocationRelativeTo(null);

		return frame;
	}

	protected JComponent getVisualQueryComponent(final boolean showTopToolBar,
			final boolean applyButton) {
		final JPanel mainPanel = new JPanel(new BorderLayout());
		final JPanel childPanel = new JPanel(new BorderLayout());

		final JScrollPane pane = new JScrollPane(this.visualGraphs.get(0));
		childPanel.add(pane, BorderLayout.CENTER);

		final JPanel childPanelMenuToolBar = new JPanel(new BorderLayout());
		final JComponent menu = this.buildMenuBar();
		childPanelMenuToolBar.add(menu, BorderLayout.NORTH);
		if (showTopToolBar) {
			childPanelMenuToolBar.add(this.createTopToolBar(),
					BorderLayout.CENTER);
		}

		mainPanel.add(childPanel, BorderLayout.CENTER);
		mainPanel.add(childPanelMenuToolBar, BorderLayout.NORTH);
		if (applyButton) {
			final JButton applyVisualQuery = new JButton("Apply");
			applyVisualQuery.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					try {
						final String query = getQueryFromVisualQuery();
						tp_queryInput.setText(query);
						queryAndUpdateBrowserContent(query);
					} catch (final Exception ex) {
					}
				}
			});
			mainPanel.add(applyVisualQuery, BorderLayout.SOUTH);
		}

		return mainPanel;
	}

	protected JComponent getTextualQueryComponent(final boolean applyButton) {
		final JPanel mainPanel = new JPanel(new BorderLayout());

		final LuposDocument document = new LuposDocument();
		this.tp_queryInput = new LuposJTextPane(document);
		document.init(SPARQLParser.createILuposParser(new LuposDocumentReader(document)), true, 100);

		final JScrollPane scrollpane = new JScrollPane(this.tp_queryInput);
		scrollpane.getViewport().setBackground(Color.WHITE);

		mainPanel.add(scrollpane, BorderLayout.CENTER);

		if (applyButton) {
			final JButton applyVisualQuery = new JButton("Apply");
			applyVisualQuery.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					final String query = tp_queryInput.getText();
					evaluateQuery(query);
					queryAndUpdateBrowserContent(query);
				}
			});
			mainPanel.add(applyVisualQuery, BorderLayout.SOUTH);
		}

		return mainPanel;
	}

	protected String getQueryFromVisualQuery() throws Exception {
		this.statusBar.setText("Validating query ...");

		final boolean valid = this.visualGraphs.get(0).validateGraph(true, null);

		this.statusBar.clear();

		if (!valid) {
			throw new Exception("Visual query not valid!");
		}

		this.statusBar.setText("Serializing query ...");

		final String serializedQuery = this.visualGraphs.get(0)
		.serializeGraph();

		this.statusBar.clear();
		return serializedQuery;
	}

	private Component getBrowserComponent() {
		this.resultpanel = new JPanel(new BorderLayout());
		return this.resultpanel;
	}

	private void updateBrowserContent(final QueryResult resultQueryEvaluator) {
		if (this.resultpanel == null)
			return;
		this.resultpanel.removeAll();
		if (prefixInstance == null)
			prefixInstance = new ViewerPrefix(usePrefixes);

		if (resultQueryEvaluator instanceof BooleanResult) {

			System.out.println(resultQueryEvaluator.toString());

			final JLabel l_noResult = new JLabel("Result: "
					+ resultQueryEvaluator.toString());

			final JPanel noPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			noPanel.add(l_noResult);

			final JPanel navigationPanel = new JPanel(new BorderLayout());
			this.addNavigationButtons(navigationPanel);

			final JPanel mainPanel = new JPanel(new BorderLayout());

			mainPanel.add(navigationPanel, BorderLayout.NORTH);
			mainPanel.add(noPanel, BorderLayout.CENTER);

			this.resultpanel.add(mainPanel);
		} else if (((resultQueryEvaluator == null || resultQueryEvaluator
				.isEmpty()) && !(resultQueryEvaluator instanceof GraphResult))
				|| ((resultQueryEvaluator instanceof GraphResult) && (((GraphResult) resultQueryEvaluator)
						.getGraphResultTriples() == null || ((GraphResult) resultQueryEvaluator)
						.getGraphResultTriples().size() == 0))) {
			System.out.println("no result");

			final JLabel l_noResult = new JLabel();
			l_noResult.setText("Result: no result");

			final JPanel noPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			noPanel.add(l_noResult);

			final JPanel navigationPanel = new JPanel(new BorderLayout());
			this.addNavigationButtons(navigationPanel);

			final JPanel mainPanel = new JPanel(new BorderLayout());

			mainPanel.add(navigationPanel, BorderLayout.NORTH);
			mainPanel.add(noPanel, BorderLayout.CENTER);

			this.resultpanel.add(mainPanel, BorderLayout.NORTH);
		} else if (resultQueryEvaluator instanceof GraphResult) {
			final GraphResult gr = (GraphResult) resultQueryEvaluator;

			final String[] tableHead = { "Subject", "Predicate", "Object" };

			final Object[][] rows = new Object[gr.getGraphResultTriples()
			                                   .size()][];

			int i = 0;

			for (final Triple t : gr.getGraphResultTriples()) {
				final Object[] row = {
						t.getSubject().toString(this.prefixInstance),
						t.getPredicate().toString(this.prefixInstance),
						t.getObject().toString(this.prefixInstance) };

				rows[i++] = row;
			}

			this.outputTableResult(rows, tableHead, resultQueryEvaluator);
		} else {
			final HashSet<Variable> variables = new HashSet<Variable>();

			// get variables...
			for (final Bindings ba : resultQueryEvaluator) {
				variables.addAll(ba.getVariableSet());
			}

			// --- generate table head - begin ---
			final String[] tableHead = new String[variables.size()];

			int i = 0;

			// result order is defined...
			// if (this.resultOrder.size() > 0) {
			// for (final String s : this.resultOrder) {
			// if (variables.contains(new Variable(s))) {
			// tableHead[i++] = "?" + s;
			// }
			// }
			// } else {
			// result order is not defined...
			for (final Variable v : variables) {
				tableHead[i++] = v.toString();
			}
			// }
			// --- generate table head - end ---

			// --- generate table rows - begin ---
			final Object[][] rows = new Object[resultQueryEvaluator.size() + 4][];

			final Object[] rowButtonsRename = new Object[variables.size()];
			for (int j = 0; j < rowButtonsRename.length; j++) {
				final int row = j;
				final JButton buttonRename = new JButton("Rename");
				buttonRename.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final Variable toBeRenamed = (Variable) (variables
								.toArray()[row]);

						final String newName = JOptionPane.showInputDialog(
								AdvancedQueryEditor.this,
								"Please enter a new variable name for "
								+ toBeRenamed.toString() + ":",
								toBeRenamed.toString());

						statusBar.setText("Rename variable " + toBeRenamed
								+ "...");
						final String newQuery = determineNewQueryRenamedVariable(
								currentBrowserQuery, toBeRenamed, newName);
						System.out.println(newQuery);
						updateAll(newQuery);
						statusBar.clear();
					}
				});

				final JPanel panelRename = new JPanel(new FlowLayout());
				panelRename.add(buttonRename);
				panelRename.setBackground(Color.WHITE);

				rowButtonsRename[j] = panelRename;
			}
			rows[0] = rowButtonsRename;

			final Object[] rowButtonsSort = new Object[variables.size()];
			for (int j = 0; j < rowButtonsSort.length; j++) {
				final int row = j;
				final JButton buttonSort = new JButton("Sort");
				buttonSort.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final Variable toBeSorted = (Variable) (variables
								.toArray()[row]);
						statusBar.setText("Sort according to variable "
								+ toBeSorted + "...");
						final String newQuery = determineNewQuerySortAccordingToVariable(
								currentBrowserQuery, toBeSorted);
						System.out.println(newQuery);
						updateAll(newQuery);
						statusBar.clear();
					}
				});

				final JPanel panelSort = new JPanel(new FlowLayout());
				panelSort.add(buttonSort);
				panelSort.setBackground(Color.WHITE);

				rowButtonsSort[j] = panelSort;
			}
			rows[1] = rowButtonsSort;

			final Object[] rowButtonsExclude = new Object[variables.size()];
			for (int j = 0; j < rowButtonsExclude.length; j++) {
				final int row = j;
				final JButton buttonExclude = new JButton("Exclude");
				buttonExclude.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final Variable toBeExcluded = (Variable) (variables
								.toArray()[row]);
						statusBar.setText("Exclude variable " + toBeExcluded
								+ "...");
						final String newQuery = determineNewQueryProjectionWithoutVariable(
								currentBrowserQuery, toBeExcluded);
						System.out.println(newQuery);
						updateAll(newQuery);
						statusBar.clear();
					}
				});

				final JPanel panelExclude = new JPanel(new FlowLayout());
				panelExclude.add(buttonExclude);
				panelExclude.setBackground(Color.WHITE);

				rowButtonsExclude[j] = panelExclude;
			}
			rows[2] = rowButtonsExclude;

			final Object[] rowButtonsRefine = new Object[variables.size()];
			for (int j = 0; j < rowButtonsRefine.length; j++) {
				final int row = j;
				final JButton buttonRefine = new JButton("Refine");
				buttonRefine.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						final Variable toBeRefined = (Variable) (variables
								.toArray()[row]);
						statusBar.setText("Refine variable " + toBeRefined
								+ "...");
						refineQuery(currentBrowserQuery, toBeRefined);
						statusBar.clear();
					}
				});

				final JPanel panelRefine = new JPanel(new FlowLayout());
				panelRefine.add(buttonRefine);
				panelRefine.setBackground(Color.WHITE);

				rowButtonsRefine[j] = panelRefine;
			}
			rows[3] = rowButtonsRefine;

			i = 4;

			for (final Bindings ba : resultQueryEvaluator) {
				final Object[] row = new Object[variables.size()];

				int j = 0;

				// result order is defined...
				// if (this.resultOrder.size() > 0) {
				// for (final String s : this.resultOrder) {
				// if (variables.contains(new Variable(s))) {
				// final Literal literal = ba.get(new Variable(s));
				// String value = "";
				//
				// if (literal != null) {
				// value = literal
				// .toString(this.prefixInstance);
				// }
				//
				// row[j++] = value;
				// }
				// }
				// } else { // result order is not defined...
				for (final Variable variable : variables) {
					final Literal literal = ba.get(variable);
					String value = "";

					if (literal != null) {
						value = literal.toString(this.prefixInstance);
					}

					final JPanel panel = new JPanel(new BorderLayout());
					final LuposDocument document = new LuposDocument();
					final LuposJTextPane textPane = new LuposJTextPane(document);
					document.init(SPARQLParser.createILuposParser(new LuposDocumentReader(document)), false, 100);

					textPane.setText(value);
					textPane.setEditable(false);
					final JScrollPane scrollpane = new JScrollPane(textPane);
					scrollpane.getViewport().setBackground(Color.WHITE);
					panel.add(scrollpane);
					// panel.add(textPane);

					final JButton button = new JButton("Filter");
					button.addActionListener(new ActionListener() {
						public void actionPerformed(final ActionEvent e) {
							statusBar.setText("Adding filter to query...");
							final String newQuery = determineNewQuery(
									currentBrowserQuery, "FILTER(" + variable
									+ "=" + literal + ")");
							System.out.println(newQuery);
							updateAll(newQuery);
							statusBar.clear();
						}
					});

					final JPanel buttonPanel = new JPanel(new FlowLayout());
					buttonPanel.add(button);
					buttonPanel.setBackground(Color.WHITE);

					panel.add(buttonPanel, BorderLayout.EAST);

					row[j++] = panel;
				}
				// }

				rows[i++] = row;
			}
			// --- generate table rows - begin ---

			final HashSet<String> allVariables = new HashSet<String>();
			try {
				determineVariables(SPARQL1_1Parser
						.parse(this.currentBrowserQuery), allVariables);
				for (final Variable v : variables) {
					allVariables.remove(v.getName());
				}
				if (allVariables.size() > 0) {
					this.outputTableResult(rows, tableHead,
							resultQueryEvaluator, allVariables);
					return;
				}
			} catch (final ParseException e) {
				System.out.println(e);
				e.printStackTrace();
			}

			this.outputTableResult(rows, tableHead, resultQueryEvaluator);
		}
	}

	private JPanel generatePrefixCheckBox(final QueryResult resultQueryEvaluator, final Container toValidate) {
		// create CheckBox for line colors, add actionListener and add it to
		// Applet...
		final JCheckBox cb_prefixes = new JCheckBox("Use prefixes", true);
		cb_prefixes.setSelected(this.usePrefixes);
		cb_prefixes.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent e) {
				// set boolean flag...
				if (e.getStateChange() == ItemEvent.SELECTED) {
					usePrefixes = true;
					prefixInstance.setStatus(true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					usePrefixes = false;
					prefixInstance.setStatus(false);
				}

				resultpanel.removeAll();
				updateBrowserContent(resultQueryEvaluator);
				if(toValidate!=null){
					toValidate.validate();
				}
			}
		});

		final JPanel prefixesPanel = new JPanel(new BorderLayout());
		prefixesPanel.add(cb_prefixes, BorderLayout.WEST);

		addNavigationButtons(prefixesPanel);

		return prefixesPanel;
	}

	private void addNavigationButtons(final JPanel prefixesPanel) {
		final JButton button = new JButton("Query for all data");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				statusBar.setText("query for all data...");
				updateAll("SELECT ?s ?p ?o\nWHERE {\n\t?s ?p ?o.\n}");
				statusBar.clear();
			}
		});
		prefixesPanel.add(button, BorderLayout.EAST);

		final JPanel navigatePanel = new JPanel(new FlowLayout());
		buttonBackward = new JButton("<");
		buttonBackward.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				statusBar.setText("previous query...");
				final String newQuery = backwardList.removeLast();
				forwardList.addFirst(currentBrowserQuery);
				updateAfterNavigation(newQuery);
				statusBar.clear();
			}
		});
		buttonBackward.setEnabled(backwardList.size() > 0);
		navigatePanel.add(buttonBackward);

		buttonForward = new JButton(">");
		buttonForward.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				statusBar.setText("next query...");
				final String newQuery = forwardList.remove(0);
				backwardList.addLast(currentBrowserQuery);
				updateAfterNavigation(newQuery);
				statusBar.clear();
			}
		});
		buttonForward.setEnabled(forwardList.size() > 0);
		navigatePanel.add(buttonForward);

		prefixesPanel.add(navigatePanel, BorderLayout.CENTER);
	}

	private void updateAfterNavigation(final String newQuery) {
		buttonBackward.setEnabled(backwardList.size() > 0);
		buttonForward.setEnabled(forwardList.size() > 0);
		updateAll(newQuery, false);
	}

	private void outputTableResult(final Object[][] rows,
			final String[] tableHead, final QueryResult resultQueryEvaluator,
			final HashSet<String> allVariables) {
		final JPanel addVariablePanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
		final String[] variables = new String[allVariables.size()];
		int i = 0;
		for (final String var : allVariables) {
			variables[i++] = "?" + var;
		}
		final JComboBox comboBox = new JComboBox(variables);
		addVariablePanel.add(comboBox);
		final JButton button = new JButton("Add Variable");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				statusBar.setText("add variable...");
				updateAll(determineNewQueryProjectionWithVariable(
						currentBrowserQuery, (String) comboBox
						.getSelectedItem()));
				statusBar.clear();
			}
		});
		addVariablePanel.add(button);
		addVariablePanel.add(new JLabel("  "));
		generateEliminateDuplicatesCheckBox(addVariablePanel);
		outputTableResult(rows, tableHead, resultQueryEvaluator,
				addVariablePanel);
	}

	private JPanel generateEliminateDuplicatesCheckBox(JPanel panel) {
		final int distinctQuery = isDistinctQuery(this.currentBrowserQuery);
		if (distinctQuery >= 0) {
			final JCheckBox checkBox = new JCheckBox("Eliminate Duplicates");
			checkBox.setSelected(distinctQuery == 1);
			checkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						statusBar.setText("eliminate duplicates...");
						updateAll(determineNewQueryChangeDistinct(
								currentBrowserQuery, true));
						statusBar.clear();
					} else {
						statusBar.setText("allow duplicates...");
						updateAll(determineNewQueryChangeDistinct(
								currentBrowserQuery, false));
						statusBar.clear();
					}
				}
			});
			if (panel == null)
				panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(checkBox);
			return panel;
		}
		return panel;
	}

	private void outputTableResult(final Object[][] rows,
			final String[] tableHead, final QueryResult resultQueryEvaluator) {
		final JPanel panel = generateEliminateDuplicatesCheckBox(null);
		outputTableResult(rows, tableHead, resultQueryEvaluator, panel);
	}

	private void outputTableResult(final Object[][] rows,
			final String[] tableHead, final QueryResult resultQueryEvaluator,
			final JPanel toBeAddedBeforeTable) {
		this.resultpanel.add(this.generatePrefixCheckBox(resultQueryEvaluator, this.resultpanel), BorderLayout.NORTH);

		if (this.usePrefixes) {
			final JPanel pPanel = new JPanel(new BorderLayout());

			final JLabel info = new JLabel("Prefixes:");

			pPanel.add(info, BorderLayout.NORTH);

			final LuposDocument document = new LuposDocument();
			final LuposJTextPane textPane = new LuposJTextPane(document);
			document.init(SPARQLParser.createILuposParser(new LuposDocumentReader(document)), false, 100);

			final StringBuffer prefixes = this.prefixInstance.getPrefixString("", "");
			textPane.setText(prefixes.substring(0, prefixes.length() - 1));
			textPane.setEditable(false);
			final JScrollPane scroll = new JScrollPane(textPane);
			scroll.getViewport().setBackground(Color.WHITE);

			pPanel.add(scroll);

			this.splitPane_result = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			this.splitPane_result.setOneTouchExpandable(true);
			this.splitPane_result.setContinuousLayout(true);
			this.splitPane_result.setTopComponent(pPanel);
			this.splitPane_result.setResizeWeight(0.15);

			this.resultpanel.add(this.splitPane_result, BorderLayout.CENTER);
		}

		// --- output result table - begin ---
		this.generateTable(rows, tableHead, toBeAddedBeforeTable);
		// --- output result table - end ---
	}

	private void generateTable(final Object[][] rows, final String[] tableHead,
			final JPanel toBeAddedBeforeTable) {
		final JTable resultTable = new JTable(new JTableButtonModel(rows,
				tableHead));
		resultTable.setDefaultRenderer(JButton.class, new JTableButtonRenderer(
				resultTable.getDefaultRenderer(JButton.class)));
		resultTable.setDefaultEditor(JButton.class, new ButtonEditor(
				new JCheckBox()));
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		ShowResult.updateTable(resultTable);

		final JPanel tPanel = new JPanel(new BorderLayout());
		tPanel.setLayout(new BoxLayout(tPanel, BoxLayout.Y_AXIS));
		tPanel.add(resultTable.getTableHeader());

		tPanel.add(resultTable);

		final JPanel t2Panel = new JPanel(new BorderLayout());

		final JLabel info = new JLabel("Result table:");
		if (toBeAddedBeforeTable == null) {
			t2Panel.add(info, BorderLayout.NORTH);
		} else {
			final JPanel iPanel = new JPanel(new BorderLayout());
			iPanel.add(toBeAddedBeforeTable, BorderLayout.NORTH);
			iPanel.add(info, BorderLayout.SOUTH);
			t2Panel.add(iPanel, BorderLayout.NORTH);
		}

		t2Panel.add(tPanel);

		final JScrollPane sp_result = new JScrollPane(t2Panel);

		if (this.usePrefixes) {
			this.splitPane_result.setBottomComponent(sp_result);
		} else {
			this.resultpanel.add(sp_result, BorderLayout.CENTER);
		}
	}

	private class JTableButtonModel extends AbstractTableModel {
		private static final long serialVersionUID = -9125639738828485256L;

		private final Object[][] __rows;

		private final String[] __columns;

		public JTableButtonModel(final Object[][] rows, final String[] tableHead) {
			this.__rows = rows;
			this.__columns = tableHead;
		}

		@Override
		public String getColumnName(final int column) {
			return __columns[column];
		}

		public int getRowCount() {
			return __rows.length;
		}

		public int getColumnCount() {
			return __columns.length;
		}

		public Object getValueAt(final int row, final int column) {
			return __rows[row][column];
		}

		@Override
		public boolean isCellEditable(final int row, final int column) {
			return true;
		}

		@Override
		public Class<?> getColumnClass(final int column) {
			return JButton.class;
			// return getValueAt(0, column).getClass();
		}
	}


	private void queryAndUpdateBrowserContent(final String query) {
		updateNavigation();
		this.currentBrowserQuery = query;
		final QueryResult queryResult = this.queryForSuggestions(query);
		updateBrowserContent(queryResult);
	}

	public void updateAll(final String query) {
		updateAll(query, true);
	}

	public void updateAll(final String query, final boolean updateLists) {
		if (updateLists
				&& (editorType == EDITORTYPE.ADVANCED || editorType == EDITORTYPE.BROWSERLIKE)) {
			updateNavigation();
		}
		if ((editorType == EDITORTYPE.ADVANCED || editorType == EDITORTYPE.TEXTUALQUERY))
			this.tp_queryInput.setText(query);
		if ((editorType == EDITORTYPE.ADVANCED || editorType == EDITORTYPE.VISUALQUERY))
			this.evaluateQuery(query);
		this.currentBrowserQuery = query;
		if ((editorType == EDITORTYPE.ADVANCED || editorType == EDITORTYPE.BROWSERLIKE))
			this.updateBrowserContent(this.queryForSuggestions(query));
	}

	private void updateNavigation() {
		if (this.currentBrowserQuery != null)
			backwardList.addLast(this.currentBrowserQuery);
		if (buttonBackward != null)
			buttonBackward.setEnabled(true);
		forwardList.clear();
		if (buttonForward != null)
			buttonForward.setEnabled(false);
	}

	private static String determineNewQuery(final String oldQuery,
			final SPARQLParserVisitorImplementationDumper spvid) {
		try {
			// get root node...
			final SimpleNode root = SPARQL1_1Parser.parse(oldQuery);
			return (String) spvid.visit(root);
		} catch (final ParseException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return "";
	}

	private static int isDistinctQuery(final String currentBrowserQuery2) {
		try {
			// get root node...
			return isDistinctQuery(SPARQL1_1Parser.parse(currentBrowserQuery2));
		} catch (final ParseException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return -1;
	}

	private static String determineNewQuery(final String oldQuery,
			final String toBeAddedInWhereClause) {
		return determineNewQuery(oldQuery,
				new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTGroupConstraint node) {
				String ret = tab + "{\n";
				tab += "\t";
				final String retEnd = "}\n";
				if (node.jjtGetParent() instanceof ASTSelectQuery
						|| node.jjtGetParent() instanceof ASTConstructQuery
						|| node.jjtGetParent() instanceof ASTQuery) {
					ret = "WHERE {\n" + toBeAddedInWhereClause + "\n";
				}
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					ret += visitChild(node, i);
				}
				tab = tab.replace("\t", "");
				return ret + retEnd;
			}
		});
	}

	protected static String determineNewQueryChangeDistinct(
			final String oldQuery, final boolean distinct) {
		return determineNewQuery(oldQuery,
				new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTSelectQuery node) {
				String ret = "SELECT ";
				int i = 0;
				if (distinct) {
					ret += "DISTINCT ";
				}
				if (node.isSelectAll()) {
					ret += "*";
				} else {
					while (i < node.jjtGetNumChildren()
							&& node.jjtGetChild(i) instanceof ASTVar) {
						ret += visitChild(node, i++) + " ";
					}
				}
				ret += "\n";
				while (i < node.jjtGetNumChildren()) {
					ret += visitChild(node, i++);
				}
				return ret;
			}
		});
	}

	private static String determineNewQueryProjectionWithoutVariable(
			final String oldQuery, final Variable v) {
		try {
			final SimpleNode root = SPARQL1_1Parser.parse(oldQuery);
			return determineNewQuery(oldQuery,
					new SPARQLParserVisitorImplementationDumper() {
				@Override
				public String visit(final ASTSelectQuery node) {
					String ret = "SELECT ";
					int i = 0;
					if (node.isDistinct()) {
						ret += "DISTINCT ";
					}
					if (node.isReduced()) {
						ret += "REDUCED ";
					}
					boolean varInUse = false;
					if (node.isSelectAll()) {
						final HashSet<String> variables = new HashSet<String>();
						determineVariables(root, variables);
						variables.remove(v.getName());
						for (final String var : variables) {
							ret += "?" + var + " ";
							varInUse = true;
						}
					} else {
						while (i < node.jjtGetNumChildren()
								&& node.jjtGetChild(i) instanceof ASTVar) {
							if (((ASTVar) node.jjtGetChild(i))
									.getName().compareTo(v.getName()) != 0) {
								ret += visitChild(node, i++)
								+ " ";
								varInUse = true;
							} else
								i++;
						}
					}
					if (!varInUse) {
						final HashSet<String> variables = new HashSet<String>();
						determineVariables(root, variables);
						String varNotOccuring = "a";
						while (variables.contains(varNotOccuring))
							varNotOccuring += "a";
						ret += "?" + varNotOccuring;
					}
					ret += "\n";
					while (i < node.jjtGetNumChildren()) {
						ret += visitChild(node, i++);
					}
					return ret;
				}
			});
		} catch (final ParseException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return "";
	}

	private static String determineNewQueryProjectionWithVariable(
			final String oldQuery, final String var) {
		return determineNewQuery(oldQuery,
				new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTSelectQuery node) {
				String ret = "SELECT ";
				int i = 0;
				if (node.isDistinct()) {
					ret += "DISTINCT ";
				}
				if (node.isReduced()) {
					ret += "REDUCED ";
				}
				if (node.isSelectAll()) {
					ret += "*";
				} else {
					while (i < node.jjtGetNumChildren()
							&& node.jjtGetChild(i) instanceof ASTVar) {
						ret += visitChild(node, i++) + " ";
					}
					ret += var;
				}
				ret += "\n";
				while (i < node.jjtGetNumChildren()) {
					ret += visitChild(node, i++);
				}
				return ret;
			}
		});
	}

	private String refineQuery(final String oldQuery, final Variable toBeRefined) {
		try {
			final SimpleNode root = SPARQL1_1Parser.parse(oldQuery);
			final HashSet<String> variables = new HashSet<String>();
			determineVariables(root, variables);
			String varSO = "SO";
			while (variables.contains(varSO))
				varSO += "a";
			String varP = "P";
			while (variables.contains(varP))
				varP += "a";

			final String succeedingTriplePatterns = determineQueryForRefinement(
					oldQuery, toBeRefined.toString() + " ?" + varP + " ?"
					+ varSO + ".", varP, varSO);
			final String precedingTriplePatterns = determineQueryForRefinement(
					oldQuery, "?" + varSO + " ?" + varP + " "
					+ toBeRefined.toString() + ".", varP, varSO);
			final QueryResult succeedingTriplePatternsQR = this
			.queryForSuggestions(succeedingTriplePatterns);
			final QueryResult precedingTriplePatternsQR = this
			.queryForSuggestions(precedingTriplePatterns);

			if (succeedingTriplePatternsQR.isEmpty()
					&& precedingTriplePatternsQR.isEmpty()) {
				JOptionPane.showOptionDialog(this, "No suggestions available!",
						"Error", JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE, null, null, null);

				return oldQuery;
			}
			final QueryRDFTerm queryRDFTerm = new QueryRDFTerm(prefixInstance, toBeRefined);
			
			if (editorType == EDITORTYPE.BROWSERLIKE) {
				new SuggestionFrameForBrowserLike(this,
						precedingTriplePatternsQR, succeedingTriplePatternsQR,
						queryRDFTerm, varSO, varP);

			} else {
				new SuggestionFrame(this, (QueryGraph) visualGraphs.get(0),
						precedingTriplePatternsQR, succeedingTriplePatternsQR,
						queryRDFTerm, varSO, varP);
			}

		} catch (final ParseException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return "";
	}

	private String determineQueryForRefinement(final String oldQuery,
			final String toBeAddedInWhereClause, final String varP,
			final String varSO) {
		return determineNewQuery(oldQuery,
				new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTSelectQuery node) {
				String ret = "SELECT DISTINCT ?" + varP + " ?" + varSO
				+ "\n";
				int i = 0;
				if (node.isSelectAll()) {
				} else {
					while (i < node.jjtGetNumChildren()
							&& node.jjtGetChild(i) instanceof ASTVar) {
						i++;
					}
				}
				while (i < node.jjtGetNumChildren()) {
					ret += visitChild(node, i++);
				}
				return ret;
			}

			@Override
			public String visit(final ASTGroupConstraint node) {
				String ret = tab + "{\n";
				tab += "\t";
				final String retEnd = "}\n";
				if (node.jjtGetParent() instanceof ASTSelectQuery
						|| node.jjtGetParent() instanceof ASTConstructQuery
						|| node.jjtGetParent() instanceof ASTQuery) {
					ret = "WHERE {\n" + toBeAddedInWhereClause + "\n";
				}
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					ret += visitChild(node, i);
				}
				tab = tab.replace("\t", "");
				return ret + retEnd;
			}
		});
	}

	public String determineNewQuery(final String oldQuery,
			final String toBeAddedInWhereClause, final String var) {
		return determineNewQuery(oldQuery,
				new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTSelectQuery node) {
				String ret = "SELECT ";
				int i = 0;
				if (node.isDistinct()) {
					ret += "DISTINCT ";
				}
				if (node.isReduced()) {
					ret += "REDUCED ";
				}
				if (node.isSelectAll()) {
					ret += "*";
				} else {
					while (i < node.jjtGetNumChildren()
							&& node.jjtGetChild(i) instanceof ASTVar) {
						ret += visitChild(node, i++) + " ";
					}
					ret += var;
				}
				ret += "\n";
				while (i < node.jjtGetNumChildren()) {
					ret += visitChild(node, i++);
				}
				return ret;
			}

			@Override
			public String visit(final ASTGroupConstraint node) {
				String ret = tab + "{\n";
				tab += "\t";
				final String retEnd = "}\n";
				if (node.jjtGetParent() instanceof ASTSelectQuery
						|| node.jjtGetParent() instanceof ASTConstructQuery
						|| node.jjtGetParent() instanceof ASTQuery) {
					ret = "WHERE {\n" + toBeAddedInWhereClause + "\n";
				}
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					ret += visitChild(node, i);
				}
				tab = tab.replace("\t", "");
				return ret + retEnd;
			}
		});
	}

	protected String determineNewQuerySortAccordingToVariable(
			final String oldQuery, final Variable toBeSorted) {
		return determineNewQuery(oldQuery,
				new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTSelectQuery node) {
				String ret = "SELECT ";
				int i = 0;
				if (node.isDistinct()) {
					ret += "DISTINCT ";
				}
				if (node.isReduced()) {
					ret += "REDUCED ";
				}
				if (node.isSelectAll()) {
					ret += "*";
				} else {
					while (i < node.jjtGetNumChildren()
							&& node.jjtGetChild(i) instanceof ASTVar) {
						ret += visitChild(node, i++) + " ";
					}
				}
				ret += "\n";
				final String orderByClause = " ORDER BY "
					+ toBeSorted.toString() + "\n";
				boolean ORDERBYADDED = false;
				while (i < node.jjtGetNumChildren()) {
					final Node child = node.jjtGetChild(i);
					if (!ORDERBYADDED
							&& (child instanceof ASTOrderConditions
									|| child instanceof ASTLimit || child instanceof ASTOffset)) {
						ORDERBYADDED = true;
						ret += orderByClause;
						i++;
					} else
						ret += visitChild(node, i++);
				}
				if (!ORDERBYADDED)
					ret += orderByClause;
				return ret;
			}
		});
	}

	private static HashSet<String> determineVariables(final String query) {
		try {
			final SimpleNode root = SPARQL1_1Parser.parse(query);
			final HashSet<String> variables = new HashSet<String>();
			determineVariables(root, variables);
			return variables;
		} catch (final ParseException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return new HashSet<String>();
	}

	private static String determineNewQueryRenamedVariable(
			final String currentBrowserQuery, final Variable toBeRenamed,
			String newName) {
		try {
			final SimpleNode root = SPARQL1_1Parser.parse(currentBrowserQuery);

			try {
				SPARQL1_1Parser.parseVar(newName);
			} catch (final Throwable e) {
				try {
					SPARQL1_1Parser.parseVar("?" + newName);
				} catch (final Throwable e1) {
					JOptionPane.showOptionDialog(null,
							"This is not a valid variable name!", "Error",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE, null, null, null);

					return currentBrowserQuery;
				}
				newName = "?" + newName;
			}

			renameVariable(root, toBeRenamed.getName(), newName.substring(1));

			final SPARQLParserVisitorImplementationDumper spvid = new SPARQLParserVisitorImplementationDumper();
			return spvid.visit(root);

		} catch (final ParseException e) {
			System.out.println(e);
			e.printStackTrace();
			return "";
		}
	}

	private static void renameVariable(final Node root,
			final String toBeRenamed, final String newName) {
		if (root instanceof ASTVar) {
			if (((ASTVar) root).getName().compareTo(toBeRenamed) == 0)
				((ASTVar) root).setName(newName);
		}
		for (int i = 0; i < root.jjtGetNumChildren(); i++) {
			renameVariable(root.jjtGetChild(i), toBeRenamed, newName);
		}
	}

	private static void determineVariables(final Node root,
			final Set<String> variables) {
		if (root instanceof ASTSelectQuery) {
			for (int i = 0; i < root.jjtGetNumChildren(); i++) {
				final Node node = root.jjtGetChild(i);
				if (node instanceof ASTGroupConstraint)
					determineVariables(node, variables);
			}
		} else {
			if (root instanceof ASTVar) {
				variables.add(((ASTVar) root).getName());
			}
			for (int i = 0; i < root.jjtGetNumChildren(); i++) {
				determineVariables(root.jjtGetChild(i), variables);
			}
		}
	}

	private static int isDistinctQuery(final Node root) {
		if (root instanceof ASTSelectQuery) {
			if (((ASTSelectQuery) root).isDistinct())
				return 1;
			else
				return 0;
		} else {
			for (int i = 0; i < root.jjtGetNumChildren(); i++) {
				final int result = isDistinctQuery(root.jjtGetChild(i));
				if (result >= 0)
					return result;
			}
		}
		return -1;
	}

	public String getCurrentBrowserQuery() {
		return this.currentBrowserQuery;
	}

	public String getUniqueVariableName(final Item item) {
		if (!item.isVariable())
			return null;
		final String var = ((Variable) item).getName();
		final HashSet<String> variables = determineVariables(this.currentBrowserQuery);
		if (!variables.contains(var))
			return var;
		int i = 0;
		while (variables.contains(var + i))
			i++;
		return var + i;
	}

	private class SuggestionFrameForBrowserLike extends SuggestionFrame {
		private static final long serialVersionUID = 483192455033993111L;
		private final static int PADDING = 5;
		private final static int SPACING = 60;

		public SuggestionFrameForBrowserLike(final AdvancedQueryEditor parent,
				final QueryResult queryResult_preceding,
				final QueryResult queryResult_succeeding,
				final QueryRDFTerm op, final String varSOName,
				final String varPredName) {
			super(parent, null, queryResult_preceding, queryResult_succeeding,
					op, varSOName, varPredName);
		}

		@Override
		protected void addPrecedingSuggestion(
				final SuggestionPanel subjectPanel,
				final SuggestionPanel predicatePanel) {
			// get items...
			final Item subjectItem = subjectPanel.getSelectedElement();
			final Item predicateItem = predicatePanel.getSelectedElement();

			final String oldquery = getCurrentBrowserQuery();
			statusBar.setText("set refined query...");
			updateAll(oldquery.substring(0, oldquery.length() - 1)
					+ subjectItem + " " + predicateItem + " " + this.op + ".}");
			statusBar.clear();
		}

		@Override
		protected void addSucceedingSuggestion(
				final SuggestionPanel predicatePanel,
				final SuggestionPanel objectPanel) {
			// get items...
			final Item predicateItem = predicatePanel.getSelectedElement();
			final Item objectItem = objectPanel.getSelectedElement();

			final String oldquery = getCurrentBrowserQuery();
			statusBar.setText("set refined query...");
			updateAll(oldquery.substring(0, oldquery.length() - 1) + this.op
					+ " " + predicateItem + " " + objectItem + ".}");
			statusBar.clear();
		}

		@Override
		protected JPanel createPrecedingSuggestionsPanel(
				final ActionListener addPrecedingSuggestionAction) {
			final Font font = new Font("Sans Serif", Font.PLAIN, 12);
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

			QueryRDFTerm queryRDFTerm = new QueryRDFTerm(prefixInstance);
			// just in order to set panel of QueryRDFTerm...
			queryRDFTerm.draw(new GraphWrapperOperator(queryRDFTerm), AdvancedQueryEditor.this.visualGraphs.get(0));
			
			subjectPanel = new SuggestionPanel(PADDING, queryRDFTerm, variablesSubj, rowPanel);

			final int subjectX = PADDING;
			final int subjectY = PADDING;
			final int subjectWidth = subjectPanel.getPreferredSize().width;
			final int subjectHeight = subjectPanel.getPreferredSize().height;

			subjectPanel.setBounds(subjectX, subjectY, subjectWidth,
					subjectHeight);

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

			QueryRDFTerm queryRDFTerm2 = new QueryRDFTerm(prefixInstance);
			// just in order to set panel of QueryRDFTerm...
			queryRDFTerm2.draw(new GraphWrapperOperator(queryRDFTerm2), AdvancedQueryEditor.this.visualGraphs.get(0));
			
			predicatePanelS = new SuggestionPanel(PADDING, queryRDFTerm2, variablesPred, rowPanel);

			final int predicateX = subjectX + subjectWidth + SPACING;
			final int predicateY = PADDING;
			final int predicateWidth = predicatePanelS.getPreferredSize().width;
			final int predicateHeight = predicatePanelS.getPreferredSize().height;

			predicatePanelS.setBounds(predicateX, predicateY, predicateWidth,
					predicateHeight);

			rowPanel.add(predicatePanelS);
			// --- predicate - end ---

			// --- object - begin ---
			final AbstractGuiComponent<Operator> objectPanel = this.op.draw(
					new GraphWrapperOperator(this.op), PADDING, font);
			((JTextField) objectPanel.getComponent(0)).setEditable(false);
			objectPanel.setMovable(false);

			final int objectX = predicateX + predicateWidth + SPACING;
			final int objectY = PADDING;
			final int objectWidth = objectPanel.getPreferredSize().width;
			final int objectHeight = objectPanel.getPreferredSize().height;

			objectPanel.setBounds(objectX, objectY, objectWidth, objectHeight);

			rowPanel.add(objectPanel);
			// --- object - end ---

			rowPanel.setPreferredSize(new Dimension(objectX + objectWidth
					+ PADDING, predicateHeight + 2 * PADDING));

			panel.add(rowPanel, gbc);

			gbc.gridy++;

			final JButton btn_add = new JButton("Add preceding suggestion");
			btn_add.addActionListener(addPrecedingSuggestionAction);

			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets(5, 5, 5, 5);

			panel.add(btn_add, gbc);

			return panel;
		}

		@Override
		protected JPanel createSucceedingSuggestionsPanel(
				final ActionListener addSucceedingSuggestionAction) {
			final Font font = new Font("Sans Serif", Font.PLAIN, 12);
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
					new GraphWrapperOperator(this.op), PADDING, font);
			((JTextField) subjectPanel.getComponent(0)).setEditable(false);
			subjectPanel.setMovable(false);

			final int subjectX = PADDING;
			final int subjectY = PADDING;
			final int subjectWidth = subjectPanel.getPreferredSize().width;
			final int subjectHeight = subjectPanel.getPreferredSize().height;

			subjectPanel.setBounds(subjectX, subjectY, subjectWidth,
					subjectHeight);

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

			predicatePanelO = new SuggestionPanel(PADDING, this.op,
					variablesPred, rowPanel);

			final int predicateX = subjectX + subjectWidth + SPACING;
			final int predicateY = PADDING;
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

			QueryRDFTerm queryRDFTerm = new QueryRDFTerm(prefixInstance);
			
			objectPanel = new SuggestionPanel(PADDING, queryRDFTerm, variablesObj, rowPanel);

			final int objectX = predicateX + predicateWidth + SPACING;
			final int objectY = PADDING;
			final int objectWidth = objectPanel.getPreferredSize().width;
			final int objectHeight = objectPanel.getPreferredSize().height;

			objectPanel.setBounds(objectX, objectY, objectWidth, objectHeight);

			rowPanel.add(objectPanel);
			// --- object - end ---

			rowPanel.setPreferredSize(new Dimension(objectX + objectWidth
					+ PADDING, predicateHeight + 2 * PADDING));

			panel.add(rowPanel, gbc);

			gbc.gridy++;

			final JButton btn_add = new JButton("Add succeeding suggestion");
			btn_add.addActionListener(addSucceedingSuggestionAction);

			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets(5, 5, 5, 5);

			panel.add(btn_add, gbc);

			return panel;
		}
	}

	public JFrame getFrame() {
		return this.frame;
	}

	@Override
	public String getXPrefPrefix() {
		return this.getClass().getSuperclass().getSimpleName();
	}
}
