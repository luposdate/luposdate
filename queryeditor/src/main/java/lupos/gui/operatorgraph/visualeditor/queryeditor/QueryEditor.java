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
package lupos.gui.operatorgraph.visualeditor.queryeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE;
import lupos.gui.anotherSyntaxHighlighting.LinePainter;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.SPARQLParser;
import lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser;
import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.Suggester;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.guielements.ContainerPanel;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.QueryGraph;
import lupos.gui.operatorgraph.visualeditor.queryeditor.guielements.SuggestionFrame;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Ask;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Construct;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.ConstructTemplateContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Describe;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Filter;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Graph;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Join;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Optional;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.QueryRDFTerm;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.RetrieveData;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Select;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.TripleContainer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.operators.Union;
import lupos.gui.operatorgraph.visualeditor.queryeditor.parsing.VisualQueryGenerator;
import lupos.gui.operatorgraph.visualeditor.queryeditor.util.QueryConnection;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.misc.util.OperatorIDTuple;
import lupos.gui.operatorgraph.visualeditor.util.SimpleOperatorGraphVisitor;
import lupos.gui.operatorgraph.visualeditor.util.VEPrefix;
import lupos.misc.FileHelper;
import lupos.optimizations.sparql2core_sparql.SPARQL2CoreSPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SPARQL1_1ParserVisitor;
import lupos.sparql1_1.SimpleNode;
import lupos.sparql1_1.operatorgraph.SPARQLCoreParserVisitorImplementation;

/**
 * This is an editor to build visual queries for SPARQL.
 */
public class QueryEditor extends VisualEditor<Operator> implements Suggester<Operator> {
	private static final long serialVersionUID = 1L;
	private JMenuItem suggestionMI;
	private boolean isInSuggestionMode = false;
	protected QueryEvaluator evaluator;
	protected String dataFile = "";
	protected IQueryEditor component;
	private JFrame frame;

	/**
	 * This Methods creates the GUI for the visual query editor.
	 */
	public QueryEditor(final Image image) {
		super(true);

		LANGUAGE.SEMANTIC_WEB.setStyles();

		this.create("", image);
	}

	public QueryEditor(final boolean standalone) {
		super(standalone);

		LANGUAGE.SEMANTIC_WEB.setStyles();
	}

	public QueryEditor(final String query, final String data,
			final IQueryEditor component, final Image image) {
		super(false);

		LANGUAGE.SEMANTIC_WEB.setStyles();

		init(query, data, component, image);
	}

	protected void init(final String query, final String data,
			final IQueryEditor component, final Image image) {

		this.component = component;

		this.frame = this.create(query, image);

		if (!data.equals("")) {
			this.dataFile = data;

			this.loadData();
		}

		this.suggestionMI.setEnabled(true);

		this.frame
		.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				closeWindow();
			}
		});
	}

	protected JFrame create(final String query, final Image image) {
		this.visualGraphs.add(new QueryGraph(this, new VEPrefix(true)));

		// create main window...
		final JFrame frame = this.createMainWindowSingleGraph(
				"LUPOSDATE-QEdit", true, image);
		frame.setVisible(false);

		if (!query.equals("")) {
			this.evaluateQuery(query);
			this.activateGraphMenus();
			frame.setVisible(true);
		} else {
			if (this.askForQueryTemplate("The query string is empty. ")) {
				frame.setVisible(true);
			} else {
				if (this.standAlone) {
					System.exit(0);
				} else {
					frame.setVisible(false);
				}
			}
		}

		return frame;
	}

	private void generateQuery(final boolean closeWindow) {
		if (this.standAlone) {
			if (closeWindow) {
				this.frame.setVisible(false);

				System.exit(0);
			} else {
				this.statusBar.setText("Validating query ...");

				final boolean valid = this.visualGraphs.get(0).validateGraph(true, null);

				this.statusBar.clear();

				if (!valid) {
					return;
				}

				this.statusBar.setText("Serializing query ...");

				final String serializedQuery = this.visualGraphs.get(0).serializeGraph();

				this.statusBar.clear();

				this.createShowTextFrame("Serialized Query", serializedQuery, SPARQLParser.class);
			}
		} else {
			this.statusBar.setText("Validating query ...");

			final boolean valid = this.visualGraphs.get(0).validateGraph(!closeWindow, null);

			this.statusBar.clear();

			if (valid) {
				this.statusBar.setText("Serializing query ...");

				final String serializedQuery = this.visualGraphs.get(0)
				.serializeGraph();

				this.statusBar.clear();

				final int returnValue = this.createShowTextDialog(
						"Generated SPARQL Query", serializedQuery,
						"Take over query into calling application?", true);

				if (returnValue == 1 || returnValue == 2) {
					if (returnValue == 1) {
						this.component.setSerializedQuery(serializedQuery);
					}

					if (closeWindow) {
						this.frame.setVisible(false);
					}
				}
			} else if (closeWindow) {
				final int returnValue = JOptionPane.showOptionDialog(this,
						"An error has occured during validation",
						"Validation error", JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE, null, new Object[] {
						"Correct Input", "Close Program" }, 0);

				if (returnValue == 1) {
					this.frame.setVisible(false);
				}
			}
		}
	}

	void closeWindow() {
		this.generateQuery(true);
	}

	private boolean askForQueryTemplate(final String start) {
		final int returnValue = JOptionPane.showOptionDialog(this, start
				+ "Please choose a query template.", "Choose a query head",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
				null, new Object[] { "SELECT", "ASK", "CONSTRUCT", "DESCRIBE",
		"No query template" }, 0);

		switch (returnValue) {
		case -1:
			return false;
		default:
			this.clearCanvas();

			switch (returnValue) {
			case 0:
				this.evaluateQuery("SELECT * WHERE {?s ?p ?o.}");
				break;
			case 1:
				this.evaluateQuery("ASK{?s ?p ?o.}");
				break;
			case 2:
				this.evaluateQuery("CONSTRUCT{?s ?p ?o.}WHERE{?s ?p ?o.}");
				break;
			case 3:
				this.evaluateQuery("DESCRIBE * WHERE{?s ?p ?o.}");
				break;
			}
		}

		if (returnValue != 4) {
			this.activateGraphMenus();
		}

		return true;
	}

	/**
	 * This is the method, that creates the JMenuBar for the GUI.
	 * 
	 * @return JMenuBar The created JMenuBar
	 */
	@Override
	public JMenuBar buildMenuBar() {
		final JMenuBar menuBar = this.createMenuBar();
		menuBar.add(this.buildFileMenu());
		menuBar.add(this.buildEditMenu());
		menuBar.add(this.buildGraphMenu());
		menuBar.add(this.buildOperatorMenu());

		return menuBar; // return the MenuBar
	}

	private JMenu buildFileMenu() {
		// create JMenuItem to start an empty query...
		final JMenuItem emptyQueryMI = new JMenuItem("New empty query");
		emptyQueryMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				askForQueryTemplate("");
			}
		});

		// create JMenuItem to load a new query from text...
		final JMenuItem newQueryTMI = new JMenuItem("Load query from text");
		newQueryTMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				createQueryInputFrame();
			}
		});

		// create JMenuItem to load a new query from a file...
		final JMenuItem newQueryFMI = new JMenuItem("Load query from a file");
		newQueryFMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter(
						"Query Files", "txt"));

				if (fileChooser.showOpenDialog(myself) == JFileChooser.APPROVE_OPTION) {
					try {
						// evaluate query and show graph...
						evaluateQuery(FileHelper.readFile(fileChooser
								.getSelectedFile().getAbsolutePath()));

						repaint();
						activateGraphMenus();
					} catch (final Throwable t) {
						t.printStackTrace();

						JOptionPane.showOptionDialog(myself, t.getMessage(),
								"Error", JOptionPane.DEFAULT_OPTION,
								JOptionPane.ERROR_MESSAGE, null, null, null);
					}
				}
			}
		});

		// create JMenuItem to load data from a file...
		final JMenuItem loadDataMI = new JMenuItem("Load data");
		loadDataMI.setEnabled(false);
		loadDataMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				loadDataDialog();
			}
		});

		this.jGraphMenus.add(loadDataMI);

		// create JMenuItem to show the loaded data...
		final JMenuItem showDataMI = new JMenuItem("Show loaded data");
		showDataMI.setEnabled(false);
		showDataMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				boolean suggestionsEnabled = true;

				if (dataFile.equals("")) {
					suggestionsEnabled = askLoadDataDialog();
				}

				if (suggestionsEnabled) {
					createShowTextFrame("Loaded data", dataFile, TurtleParser.class);
				}
			}
		});

		this.jGraphMenus.add(showDataMI);

		// create JMenuItem to end the program...
		final JMenuItem endMI = new JMenuItem("End program");
		endMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				closeWindow();
			}
		});

		// create File Menu and add components to it...
		final JMenu fileMenu = new JMenu("File");
		fileMenu.add(emptyQueryMI);

		if (this.standAlone) {
			fileMenu.add(newQueryTMI);
			fileMenu.add(newQueryFMI);
			fileMenu.addSeparator();
			fileMenu.add(loadDataMI);
			fileMenu.add(showDataMI);
		}

		fileMenu.addSeparator();
		fileMenu.add(endMI);

		return fileMenu;
	}

	@Override
	public JMenu buildEditMenu() {
		final JMenu editMenu = super.buildEditMenu();

		// create JMenuItem to show suggestions...
		this.suggestionMI = new JMenuItem("Make suggestions");
		this.suggestionMI.setEnabled(false);
		this.suggestionMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				boolean suggestionsEnabled = true;

				if (dataFile.equals("")) {
					if (component != null) {
						final int returnValue = JOptionPane
						.showOptionDialog(
								QueryEditor.this,
								"No data so far. Take over data from calling application?",
								"No Data", JOptionPane.YES_NO_OPTION,
								JOptionPane.INFORMATION_MESSAGE, null,
								new String[] { "YES", "NO" }, 0);

						if (returnValue == 0) {
							dataFile = component.getData();

							if (dataFile.equals("")) {
								suggestionsEnabled = false;

								JOptionPane
								.showOptionDialog(
										QueryEditor.this,
										"The calling application also does not have any data.",
										"No Data",
										JOptionPane.OK_OPTION,
										JOptionPane.ERROR_MESSAGE,
										null, new String[] { "OK" }, 0);
							} else {
								loadData();

								if (dataFile.equals("")) {
									suggestionsEnabled = false;
								}
							}
						} else {
							suggestionsEnabled = false;
						}
					} else {
						suggestionsEnabled = askLoadDataDialog();
					}
				}

				if (suggestionsEnabled) {
					isInSuggestionMode = true;

					statusBar
					.setText("SuggestionMode: Click on the RDFTerm you want to get suggestions for.");
				}
			}
		});

		editMenu.addSeparator();
		editMenu.add(this.suggestionMI);

		return editMenu;
	}

	private JMenu buildGraphMenu() {
		// create JMenuItem to rearrange the QueryGraph...
		final JMenuItem rearrangeMI = new JMenuItem("Arrange Graph");
		rearrangeMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				statusBar.setText("Arranging query ...");

				visualGraphs.get(0).arrange(false, false, false,
						Arrange.values()[0]);
				visualGraphs.get(0).arrange(false, false, false,
						Arrange.values()[0]);

				statusBar.clear();
			}
		});

		// create JMenuItem to serialize the QueryGraph...
		final JMenuItem serializeMI = new JMenuItem("Generate SPARQL query");
		serializeMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				generateQuery(false);
			}
		});

		// create Graph menu and add components to it...
		final JMenu graphMenu = new JMenu("Graph");
		graphMenu.setEnabled(false);
		graphMenu.add(rearrangeMI);
		graphMenu.addSeparator();
		graphMenu.add(serializeMI);

		this.jGraphMenus.add(graphMenu);

		return graphMenu;
	}

	private JMenu buildOperatorMenu() {
		// create JMenuItem to add a connection between two Operators...
		final JMenuItem addConnectionMI = new JMenuItem(
		"Add connection between two operators");
		addConnectionMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				connectionMode = new QueryConnection(myself);
			}
		});

		// create JMenuItem to add Filter-Operator...
		final JMenuItem filterOpMI = new JMenuItem("FILTER");
		filterOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Filter.class);
			}
		});

		// create JMenuItem to add Graph-Operator...
		final JMenuItem graphOpMI = new JMenuItem("GRAPH");
		graphOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Graph.class);
			}
		});

		// create JMenuItem to add RDFTerm-Operator...
		final JMenuItem rdfTermOpMI = new JMenuItem("RDFTerm");
		rdfTermOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(QueryRDFTerm.class);
			}
		});

		// create JMenuItem to add an OperatorContainer...
		final JMenuItem opContainerMI = new JMenuItem("TripleContainer");
		opContainerMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(TripleContainer.class);
			}
		});

		// create Operator menu and add components to it...
		final JMenu operatorMenu = new JMenu("Add");
		operatorMenu.add(addConnectionMI);
		operatorMenu.addSeparator();
		operatorMenu.add(filterOpMI);
		operatorMenu.add(graphOpMI);
		operatorMenu.add(this.buildMultiInputOperatorMenu());
		operatorMenu.add(opContainerMI);
		operatorMenu.add(rdfTermOpMI);
		operatorMenu.add(this.buildRetrieveDataOperatorMenu());

		return operatorMenu;
	}

	private JMenu buildRetrieveDataOperatorMenu() {
		// create JMenuItem to add Ask-Operator...
		final JMenuItem askOpMI = new JMenuItem("ASK");
		askOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Ask.class);
			}
		});

		// create JMenuItem to add Construct-Operator...
		final JMenuItem constructOpMI = new JMenuItem("CONSTRUCT");
		constructOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Construct.class);
			}
		});

		// create JMenuItem to add a ConstructContainer...
		final JMenuItem constructContainerMI = new JMenuItem(
		"ConstructTemplateContainer");
		constructContainerMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(ConstructTemplateContainer.class);
			}
		});

		// create JMenuItem to add Describe-Operator...
		final JMenuItem describeOpMI = new JMenuItem("DESCRIBE");
		describeOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Describe.class);
			}
		});

		// create JMenuItem to add Select-Operator...
		final JMenuItem selectOpMI = new JMenuItem("SELECT");
		selectOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Select.class);
			}
		});

		// create RetrieveData Operator menu and add components to it...
		final JMenu retrieveDataOperatorM = new JMenu("Query heads");
		retrieveDataOperatorM.add(askOpMI);
		retrieveDataOperatorM.add(constructOpMI);
		retrieveDataOperatorM.add(constructContainerMI);
		retrieveDataOperatorM.add(describeOpMI);
		retrieveDataOperatorM.add(selectOpMI);

		return retrieveDataOperatorM;
	}

	private JMenu buildMultiInputOperatorMenu() {
		// create JMenuItem to add Join-Operator...
		final JMenuItem joinOpMI = new JMenuItem("JOIN");
		joinOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Join.class);
			}
		});

		// create JMenuItem to add Optional-Operator...
		final JMenuItem optionalOpMI = new JMenuItem("OPTIONAL");
		optionalOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Optional.class);
			}
		});

		// create JMenuItem to add Union-Operator...
		final JMenuItem unionOpMI = new JMenuItem("UNION");
		unionOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(Union.class);
			}
		});

		// create MultiInput Operator menu and add components to it...
		final JMenu multiInputOperatorM = new JMenu("Multiinput Operators");
		multiInputOperatorM.add(joinOpMI);
		multiInputOperatorM.add(optionalOpMI);
		multiInputOperatorM.add(unionOpMI);

		return multiInputOperatorM;
	}

	/**
	 * This method parses the given query and creates the VisualQuery.
	 * 
	 * @param query
	 *            the query to parse
	 */
	protected void evaluateQuery(final String query) {
		try {
			final Operator rootNode = this.parseQuery(query); // parse the query

			// generate QueryGraph...
			final JPanel graphPanel = this.visualGraphs.get(0).createGraph(
					new GraphWrapperOperator(rootNode), false, false, false,
					Arrange.values()[0]);

			this.visualGraphs.get(0).updateMainPanel(graphPanel);
		} catch (final Throwable e) {
			this.statusBar.clear();

			JOptionPane.showOptionDialog(this, e.getMessage(), "Error",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
					null, null, null);
		}
	}

	private Operator parseQuery(final String query) throws Throwable {
		this.statusBar.setText("Parsing query ...");

		SimpleNode root = SPARQL1_1Parser.parse(query); // get root node...

		((VisualGraphOperatorWithPrefix) this.visualGraphs.get(0)).prefix
		.registerElementsInPrefixInstance(root);

		// get coreSPARQL query...
		final boolean[] rules = { true, true, true, true, true, true, true,
				true, true, true, true, true, true, true, false , true, true, true};

		final SPARQL2CoreSPARQLParserVisitorImplementationDumper spvid = SPARQL2CoreSPARQLParserVisitorImplementationDumper.createInstance(rules);
		final String corequery = spvid.visit(root);

		// get root node of coreSPARQL query...
		root = SPARQL1_1Parser.parse(corequery);

		// generate VisualQuery and get root element of it...
		final VisualQueryGenerator vqg = new VisualQueryGenerator(
				((VisualGraphOperatorWithPrefix) this.visualGraphs.get(0)).prefix);

		final Operator rootNode = (Operator) root.jjtAccept(
				(SPARQL1_1ParserVisitor) vqg, null);
		rootNode.setParents();

		this.statusBar.clear();

		return rootNode;
	}

	private boolean askLoadDataDialog() {
		final int ret = JOptionPane.showOptionDialog(this, "No data loaded!",
				"Error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,
				null, new Object[] { "Load data", "Cancel" }, 0);

		if (ret == 0) { // load data...
			this.loadDataDialog();

			return true;
		} else {
			return false;
		}
	}

	private void loadDataDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser
		.setFileFilter(new FileNameExtensionFilter("N3 Files", "n3"));

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			this.dataFile = FileHelper.readFile(fileChooser.getSelectedFile()
					.getAbsolutePath());

			this.loadData();
		}
	}

	protected void loadData() {
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
			this.evaluator.prepareInputData(defaultGraphs, new LinkedList<URILiteral>());

			this.suggestionMI.setEnabled(true);
		} catch (final RuntimeException re) {
			this.dataFile = "";

			JOptionPane.showOptionDialog(this,
					"Error while loading data file:\n" + re.getMessage(),
					"Error", JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE, null, null, null);
		} catch (final Exception e) {
			this.dataFile = "";

			e.printStackTrace();
		}

		this.statusBar.clear();
	}

	protected QueryResult queryForSuggestions(final String query) {
		try {
			if (this.evaluator == null)
				return null;

			Bindings.instanceClass = BindingsMap.class;

			System.out.println("Compile query...");

			this.evaluator.compileQuery(query);

			System.out.println("Logical optimization...");

			this.evaluator.logicalOptimization();

			System.out.println("Physical optimization...");

			this.evaluator.physicalOptimization();

			System.out.println("Evaluate query ...");

			final QueryResult resultQueryEvaluator = this.evaluator.getResult();

			System.out.println("\nQuery Result:");
			System.out.println(resultQueryEvaluator);
			System.out.println("----------------Done.");

			return resultQueryEvaluator;
		} catch (final Throwable t) {
			t.printStackTrace();

			return null;
		}
	}

	public void makeSuggestions(final QueryRDFTerm op) {
		this.isInSuggestionMode = false;

		if (!this.visualGraphs.get(0).validateGraph(true, null)) { // validate the query...
			return;
		}

		final String makeSuggestionPredVar = this.visualGraphs.get(0)
		.getFreeVariable("?makeSuggestionPred");
		final String makeSuggestionSOVar = this.visualGraphs.get(0)
		.getFreeVariable("?makeSuggestionSO");

		// build the query...
		final String query_preceding = ((VisualGraphOperatorWithPrefix) this.visualGraphs
				.get(0)).prefix.getPrefixString("PREFIX ", "")
				+ "SELECT DISTINCT "
				+ makeSuggestionPredVar
				+ " "
				+ makeSuggestionSOVar
				+ "\n"
				+ "WHERE {\n"
				+ this.getQueryForSuggestions(op)
				+ makeSuggestionSOVar
				+ " "
				+ makeSuggestionPredVar + " " + op + "\n" + "}";

		System.out.println();
		System.out.println(query_preceding);
		System.out.println();

		final String query_succeeding = ((VisualGraphOperatorWithPrefix) this.visualGraphs
				.get(0)).prefix.getPrefixString("PREFIX ", "")
				+ "SELECT DISTINCT "
				+ makeSuggestionPredVar
				+ " "
				+ makeSuggestionSOVar
				+ "\n"
				+ "WHERE {\n"
				+ this.getQueryForSuggestions(op)
				+ op
				+ " "
				+ makeSuggestionPredVar
				+ " "
				+ makeSuggestionSOVar
				+ "\n"
				+ "}";

		System.out.println();
		System.out.println(query_succeeding);
		System.out.println();

		// evaluate the queries...
		final QueryResult queryResult_preceding = this
		.queryForSuggestions(query_preceding);
		final QueryResult queryResult_succeeding = this
		.queryForSuggestions(query_succeeding);

		if (queryResult_preceding.isEmpty() && queryResult_succeeding.isEmpty()) {
			JOptionPane.showOptionDialog(this, "No suggestions available!",
					"Error", JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE, null, null, null);

			return;
		}

		new SuggestionFrame(this, (QueryGraph) op.getGUIComponent()
				.getParentQG(), queryResult_preceding, queryResult_succeeding,
				op, makeSuggestionSOVar, makeSuggestionPredVar);
	}

	private String getQueryForSuggestions(final Operator op) {
		final OperatorContainer outerReference = op.getParentContainer();

		if (outerReference != null) {
			final String query = outerReference.serializeOperatorAndTree(
					new HashSet<Operator>()).toString();

			return this.findParentNodes(outerReference, query);
		}

		return "";
	}

	private String findParentNodes(final Operator op, String query) {
		for (final Operator preOp : op.getPrecedingOperators()) {
			if (preOp instanceof Filter) {
				query = preOp.serializeOperator() + query;
			} else if (preOp instanceof Graph) {
				final Graph g = (Graph) preOp;

				query = "GRAPH " + g.toString() + " {\n" + query + "}\n";
			} else if (preOp instanceof Optional) { // join left subtree...
				for (final OperatorIDTuple<Operator> opIDt : preOp
						.getSucceedingOperators()) {
					if (!opIDt.getOperator().equals(op) && opIDt.getId() == 0) {
						query = opIDt
						.getOperator()
						.serializeOperatorAndTree(
								new HashSet<Operator>()).toString()
								+ query;
					}
				}
			} else if (preOp instanceof Join) { // join the other subtree...
				for (final OperatorIDTuple<Operator> opIDt : preOp
						.getSucceedingOperators()) {
					if (!opIDt.getOperator().equals(op)) {
						query = opIDt
						.getOperator()
						.serializeOperatorAndTree(
								new HashSet<Operator>()).toString()
								+ query;
					}
				}
			} else if (preOp instanceof RetrieveData) {
				return query;
			}

			query = this.findParentNodes(preOp, query);
		}

		return query;
	}

	private void createQueryInputFrame() {
		final JPanel panel = new JPanel();
		final JFrame frame = this.createSmallFrame(panel, "Enter Query...");

		// create components for query input with syntax highlighting...
		final LuposDocument document = new LuposDocument();
		final JTextPane tp_query = new LuposJTextPane(document);
		document.init(SPARQLParser.createILuposParser(new LuposDocumentReader(document)), false, 100);

		tp_query.addKeyListener(this.getKeyListener(frame));
		tp_query.setFont(new Font("Courier New", Font.PLAIN, 12));
		tp_query.setPreferredSize(new Dimension(794, 200));
		tp_query.setText("PREFIX dc:      <http://purl.org/dc/elements/1.1/>\nPREFIX dcterms: <http://purl.org/dc/terms/>\n\nSELECT DISTINCT ?author ?yr\nWHERE {\n  ?doc1 dc:author ?author.\n  ?doc1 dc:ref ?doc2.\n  ?doc2 dc:ref ?doc3.\n  ?doc3 dc:ref ?doc1.\n  OPTIONAL {\n    ?doc1 dcterms:issued ?yr. FILTER(?yr < 1950)\n  }\n}\nORDER BY ASC(?author)");

		new LinePainter(tp_query, new Color(202, 223, 245));

		final JScrollPane scroll = new JScrollPane(tp_query);

		// create OK button, which starts query evaluation...
		final JButton bt_ok = new JButton("OK");
		bt_ok.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				try {
					visualGraphs.get(0).clearAll();

					// evaluate query and show graph...
					evaluateQuery(tp_query.getText());

					repaint();

					frame.setVisible(false); // hide query input frame

					activateGraphMenus();
				} catch (final Throwable t) {
					t.printStackTrace();

					JOptionPane.showOptionDialog(myself, t.getMessage(),
							"Error", JOptionPane.DEFAULT_OPTION,
							JOptionPane.ERROR_MESSAGE, null, null, null);
				}
			}
		});

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(bt_ok);

		// create main panel and add components to it...
		panel.setLayout(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		frame.setVisible(true);
	}

	private void clearCanvas() {
		this.cancelModi();

		this.visualGraphs.get(0).clearAll();

		this.clearDeletionOperatorsList();
		this.clearDeletionAnnotationList();
		this.deactivateGraphMenu();
		this.repaint();
	}

	public boolean isInSuggestionMode() {
		return this.isInSuggestionMode;
	}

	@Override
	protected void pasteElements(final String content) {
		final int rowY = this.visualGraphs.getFirst().getMaxY()
		+ (int) this.visualGraphs.get(0).SPACING;

		try {
			final Operator rootNode = this.parseQuery(content);

			this.visualGraphs.get(0).addToRootList(
					new GraphWrapperOperator(rootNode));
			this.visualGraphs.get(0).arrange(false, false, false,
					Arrange.values()[0]);
		} catch (final Throwable e) {
			this.statusBar.clear();

			try {
				final LinkedList<Object> result = new LinkedList<Object>();

				final LinkedHashMap<Item, QueryRDFTerm> rdfHash = new LinkedHashMap<Item, QueryRDFTerm>();
				// TODO if insertion into data editor or into an existing
				// TripleContainer, then all existing items (of the
				// TripleContainer
				// or from the whole DataEditor) must be first inserted!

				final ASTGroupConstraint node = SPARQL1_1Parser.parseGroupGraphPatternsAndRDFTerms(content);

				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					final Node child = node.jjtGetChild(i);

					if (child instanceof ASTGroupConstraint) {
						final VisualQueryGenerator vqg = new VisualQueryGenerator(
								((VisualGraphOperatorWithPrefix) this.visualGraphs
										.get(0)).prefix);

						final Object data = child.jjtAccept(
								(SPARQL1_1ParserVisitor) vqg, rdfHash);

						result.add(data);

						final Operator newOp = (Operator) data;
						newOp.setParents();

						final GraphBox graphBox = this.visualGraphs.get(0).graphBoxCreator.createGraphBox(
								this.visualGraphs.get(0),
								new GraphWrapperOperator(newOp));
						graphBox.setY(rowY);
						graphBox.arrange(false, false, false,
								Arrange.values()[0]);

						this.visualGraphs.get(0).addOperator(graphBox.getX(),
								rowY, newOp);

						// TODO: update x position of last box...
						if (data instanceof OperatorContainer) {
							final OperatorContainer tc = (OperatorContainer) data;

							((ContainerPanel) tc.getGUIComponent()).arrange(
									false, false, false, Arrange.values()[0]);

							if (tc.getGUIComponent().getLocation().x < 0) {
								this.visualGraphs
								.get(0)
								.getBoxes()
								.get(new GraphWrapperOperator(tc))
								.setX((int) this.visualGraphs.get(0).PADDING);
							}
						}
					} else {
						// it is an RDF term!
						final Item item = SPARQLCoreParserVisitorImplementation.getItem(child);

						rdfHash.put(
								item,
								new QueryRDFTerm(
										((VisualGraphOperatorWithPrefix) this.visualGraphs
												.get(0)).prefix, item));
					}
				}

				if (result.size() == 1
						&& result.get(0) instanceof OperatorContainer) {
					final OperatorContainer tc = (OperatorContainer) result
					.get(0);

					final LinkedHashSet<Operator> hop = new LinkedHashSet<Operator>();

					for (final Operator op : tc.getOperators()) {
						hop.add(op);

						op.visit(new SimpleOperatorGraphVisitor() {
							private static final long serialVersionUID = -350730291684630002L;

							public Object visit(final Operator basicOperator) {
								hop.add(basicOperator);

								return null;
							}
						});
					}

					for (final Operator op : rdfHash.values()) {
						if (!hop.contains(op)) {
							tc.addOperator(op);

							op.setParents();
						}
					}

					((ContainerPanel) tc.getGUIComponent()).arrange(false,
							false, false, Arrange.values()[0]);

					if (tc.getGUIComponent().getLocation().x < 0) {
						this.visualGraphs.get(0).getBoxes()
						.get(new GraphWrapperOperator(tc))
						.setX((int) this.visualGraphs.get(0).PADDING);
					}
				} else if (rdfHash.size() > 0) {
					// only single RDF terms are pasted...
					// => collect them in a TripleContainer!
					final LinkedHashSet<Operator> lhso = new LinkedHashSet<Operator>();
					lhso.addAll(rdfHash.values());

					final TripleContainer tc = new TripleContainer(lhso);
					tc.setParents();

					final GraphBox graphBox = this.visualGraphs.get(0).graphBoxCreator.createGraphBox(
							this.visualGraphs.get(0), new GraphWrapperOperator(
									tc));
					graphBox.setY(rowY);
					graphBox.arrange(false, false, false, Arrange.values()[0]);

					this.visualGraphs.get(0).addOperator(graphBox.getX(), rowY,
							tc);

					((ContainerPanel) tc.getGUIComponent()).arrange(false,
							false, false, Arrange.values()[0]);

					if (tc.getGUIComponent().getLocation().x < 0) {
						this.visualGraphs.get(0).getBoxes()
						.get(new GraphWrapperOperator(tc))
						.setX((int) this.visualGraphs.get(0).PADDING);
					}
				}
			} catch (final Throwable pe) {
				JOptionPane.showOptionDialog(this, pe.getMessage(), "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
						null, null, null);
			}
		}
	}

	@Override
	public void cancelModi() {
		super.cancelModi();

		this.isInSuggestionMode = false;
	}

	/**
	 * The main method to initialize the VisualEditor.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		new QueryEditor(null);
	}

	@Override
	public void makeSuggestions(Operator operator) {
		if(operator instanceof QueryRDFTerm){
			this.makeSuggestions((QueryRDFTerm)operator);
		}
	}

	@Override
	public boolean isInSuggestionMode(Operator operator) {
		return (this.isInSuggestionMode() && operator instanceof QueryRDFTerm);
	}
}