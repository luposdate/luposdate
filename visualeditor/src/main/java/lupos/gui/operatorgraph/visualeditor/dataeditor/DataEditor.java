package lupos.gui.operatorgraph.visualeditor.dataeditor;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE;
import lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperPrefixNonEditable;
import lupos.gui.operatorgraph.prefix.Prefix;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.dataeditor.guielements.DataGraph;
import lupos.gui.operatorgraph.visualeditor.dataeditor.operators.DataRDFTerm;
import lupos.gui.operatorgraph.visualeditor.dataeditor.parsing.VisualDataGenerator;
import lupos.gui.operatorgraph.visualeditor.dataeditor.util.DataConnection;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraphOperatorWithPrefix;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.RDFTerm;
import lupos.gui.operatorgraph.visualeditor.util.GraphWrapperOperator;
import lupos.misc.util.OperatorIDTuple;
import lupos.gui.operatorgraph.visualeditor.util.VEPrefix;
import lupos.misc.FileHelper;
import lupos.rdf.JenaTurtleTripleConsumerPipe;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SPARQL1_1ParserVisitor;
import lupos.sparql1_1.operatorgraph.SPARQLCoreParserVisitorImplementation;

/**
 * This is an editor to build visual data for RDF N3.
 */
public class DataEditor extends VisualEditor<Operator> {
	private static final long serialVersionUID = 1L;
	private Prefix prefix;
	private IDataEditor component;
	private JFrame frame;

	/**
	 * This Methods creates the GUI for the visual data editor.
	 */
	public DataEditor(final String n3daten, final Image image) {
		super(true);

		this.create(n3daten, image);
	}

	public DataEditor(final String n3daten, final IDataEditor component,
			final Image image) {
		super(false);

		this.component = component;

		this.frame = this.create(n3daten, image);

		this.frame
		.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		this.frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				closeWindow();
			}
		});
	}

	private JFrame create(final String n3daten, final Image image) {
		LANGUAGE.SEMANTIC_WEB.setStyles();

		this.prefix = new VEPrefix(true);

		this.visualGraphs.add(new DataGraph(this, this.prefix));

		final JFrame frame = this.createMainWindowSingleGraph(
				"LUPOSDATE-DEdit", true, image);

		if (!n3daten.equals("")) {
			this.parseN3Data(n3daten);
		} else {
			final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();
			rootList.add(new GraphWrapperPrefixNonEditable(this.prefix));

			this.visualGraphs.get(0).updateMainPanel(
					this.visualGraphs.get(0).createGraph(rootList, false,
							false, false, Arrange.values()[0]));
		}

		return frame;
	}

	private void generateN3Data(final boolean closeWindow) {
		if(this.standAlone) {
			if(closeWindow) {
				this.setVisible(false);

				System.exit(0);
			}
			else {
				this.statusBar.setText("Validating N3 data ...");

				final boolean valid = this.visualGraphs.get(0).validateGraph(true, null);

				this.statusBar.clear();

				if(!valid) {
					return;
				}

				this.statusBar.setText("Serializing N3 data ...");

				final String serializedData = this.visualGraphs.get(0).serializeGraph();

				this.statusBar.clear();

				createShowTextFrame("Generated N3 Data", serializedData, TurtleParser.class);
			}
		}
		else {
			this.statusBar.setText("Validating N3 data ...");

			final boolean valid = this.visualGraphs.get(0).validateGraph(!closeWindow, null);

			this.statusBar.clear();

			if(valid) {
				this.statusBar.setText("Serializing N3 data ...");

				final String serializedData = this.visualGraphs.get(0).serializeGraph();

				this.statusBar.clear();

				final int returnValue = this.createShowTextDialog("Generated N3 Data", serializedData, "Take over data into calling application?", false);

				if(returnValue == 1 || returnValue == 2) {
					if(returnValue == 1) {
						this.component.setSerializedData(serializedData);
					}

					if(closeWindow) {
						this.frame.setVisible(false);
					}
				}
			}
			else if(closeWindow) {
				final int returnValue = JOptionPane.showOptionDialog(
						this, "An error has occured during validation",
						"Validation error", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
						new Object[] {"Correct Input", "Close Program" }, 0
				);

				if(returnValue == 1) {
					this.frame.setVisible(false);
				}
			}
		}
	}

	private void closeWindow() {
		this.generateN3Data(true);
	}

	private void parseN3Data(final String n3daten) {
		this.statusBar.setText("Parsing N3 data ...");

		try {
			System.out.println("Parsing data...");

			final URILiteral rdfURL = LiteralFactory
			.createURILiteralWithoutLazyLiteral("<inlinedata:"
					+ n3daten + ">");


			final Reader reader = new InputStreamReader(rdfURL.openStream());

			final Map<String, String> prefixMap = JenaTurtleTripleConsumerPipe
					.retrievePrefixes(reader);

			for (final String prefix : prefixMap.keySet()) {
				this.prefix.addEntry(prefix, "<"
 + prefixMap.get(prefix) + ">",
						false);
			}

			// now really deal with the triples...
			final LinkedHashMap<Item, DataRDFTerm> rdfHash = new LinkedHashMap<Item, DataRDFTerm>();
			final LinkedHashSet<Operator> rdfTermToJoin = new LinkedHashSet<Operator>();

			try {
				CommonCoreQueryEvaluator.readTriples("N3", rdfURL.openStream(),
						new TripleConsumer() {
					public void consume(final Triple triple) {
						DataRDFTerm rdfTermSubject = rdfHash.get(triple
								.getPos(0));

						if (rdfTermSubject == null) {
							rdfTermSubject = new DataRDFTerm(prefix,
									triple.getPos(0));
							rdfHash.put(triple.getPos(0),
									rdfTermSubject);
						}

						DataRDFTerm rdfTermObject = rdfHash.get(triple
								.getPos(2));

						if (rdfTermObject == null) {
							rdfTermObject = new DataRDFTerm(prefix,
									triple.getPos(2));
							rdfHash.put(triple.getPos(2), rdfTermObject);
						}

						rdfTermSubject.addPredicate(rdfTermObject,
								triple.getPos(1));

						final OperatorIDTuple<Operator> opIDT = new OperatorIDTuple<Operator>(
								rdfTermObject, 0);

						if (!rdfTermSubject.getSucceedingOperators()
								.contains(opIDT)) {
							rdfTermSubject.addSucceedingOperator(opIDT);
						}

						rdfTermToJoin.add(rdfTermSubject);
					}
				});
			} catch (final Exception e) {
				JOptionPane.showOptionDialog(this, e.getMessage(), "Error",
						JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
						null, null, null);
			}

			final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();

			for (final Operator op : RDFTerm.findRootNodes(rdfTermToJoin)) {
				op.setParents();

				rootList.add(new GraphWrapperOperator(op));
			}

			System.out.println("Displaying data...");

			this.statusBar.setText("Displaying N3 data ...");

			this.visualGraphs.get(0).updateMainPanel(
					this.visualGraphs.get(0).createGraph(rootList, false,
							false, false, Arrange.values()[0]));

			this.activateGraphMenus();
		} catch (final Exception e) {
			JOptionPane.showOptionDialog(this, e.getMessage(), "Error",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
					null, null, null);
		}

		this.statusBar.clear();
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
		final JMenuItem emptyQueryMI = new JMenuItem("New empty data");
		emptyQueryMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				clearCanvas();
			}
		});

		// create JMenuItem to load data from a file...
		final JMenuItem loadDataMI = new JMenuItem("Load data");
		loadDataMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				loadDataDialog();
			}
		});

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
			fileMenu.addSeparator();
			fileMenu.add(loadDataMI);
		}

		fileMenu.addSeparator();
		fileMenu.add(endMI);

		return fileMenu;
	}

	private JMenu buildGraphMenu() {
		// create JMenuItem to rearrange the QueryGraph...
		final JMenuItem rearrangeMI = new JMenuItem("Arrange DataGraph");
		rearrangeMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				statusBar.setText("Arranging N3 data ...");

				visualGraphs.get(0).arrange(false, false, false,
						Arrange.values()[0]);
				visualGraphs.get(0).arrange(false, false, false,
						Arrange.values()[0]);

				statusBar.clear();
			}
		});

		// create JMenuItem to serialize the QueryGraph...
		final JMenuItem serializeMI = new JMenuItem("Generate N3 Data");
		serializeMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				generateN3Data(false);
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
				connectionMode = new DataConnection(myself);
			}
		});

		// create JMenuItem to add RDFTerm-Operator...
		final JMenuItem rdfTermOpMI = new JMenuItem("RDFTerm");
		rdfTermOpMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				prepareOperatorForAdd(DataRDFTerm.class);
			}
		});

		// create Operator menu and add components to it...
		final JMenu operatorMenu = new JMenu("Add");
		operatorMenu.add(addConnectionMI);
		operatorMenu.addSeparator();
		operatorMenu.add(rdfTermOpMI);

		return operatorMenu;
	}

	private void loadDataDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser
		.setFileFilter(new FileNameExtensionFilter("N3 Files", "n3"));

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			this.clearCanvas();

			this.parseN3Data(FileHelper.readFile(fileChooser.getSelectedFile()
					.getAbsolutePath()));
		}
	}

	private void clearCanvas() {
		this.cancelModi();

		this.visualGraphs.get(0).clear();

		this.clearDeletionOperatorsList();
		this.clearDeletionAnnotationList();
		this.deactivateGraphMenu();
		this.repaint();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void pasteElements(final String content) {
		final LinkedHashMap<Item, DataRDFTerm> rdfHash = new LinkedHashMap<Item, DataRDFTerm>();
		final LinkedHashSet<Operator> rdfTermToJoin = new LinkedHashSet<Operator>();

		for(final GraphWrapper gw : this.visualGraphs.get(0).getBoxes().keySet()) {
			if(gw instanceof GraphWrapperOperator) {
				final DataRDFTerm rdfTerm = (DataRDFTerm) ((GraphWrapperOperator) gw).getElement();
				rdfTermToJoin.add(rdfTerm);
				rdfHash.put(rdfTerm.getItem(), rdfTerm);
			}
		}

		try {
			final URILiteral rdfURL = LiteralFactory.createURILiteralWithoutLazyLiteral("<inlinedata:" + content + ">");

			// first just retrieve the prefixes:
			final Reader reader = new InputStreamReader(rdfURL.openStream());

			final Map<String, String> prefixMap = JenaTurtleTripleConsumerPipe
					.retrievePrefixes(reader);

			final HashSet<String> alreadyUsedPrefixes = this.prefix.getPrefixNames();

			for (final String prefix : prefixMap.keySet()) {
				if(!alreadyUsedPrefixes.contains(prefix)) {
					this.prefix.addEntry(prefix, "<" + prefixMap.get(prefix)
							+ ">", false);
				}
			}

			// now really get the triples!
			CommonCoreQueryEvaluator.readTriples("N3", rdfURL.openStream(), new TripleConsumer() {
				public void consume(final Triple triple) {
					DataRDFTerm rdfTermSubject = rdfHash.get(triple.getPos(0));

					if(rdfTermSubject == null) {
						rdfTermSubject = new DataRDFTerm(prefix, triple.getPos(0));
						rdfHash.put(triple.getPos(0), rdfTermSubject);
					}

					DataRDFTerm rdfTermObject = rdfHash.get(triple.getPos(2));

					if(rdfTermObject == null) {
						rdfTermObject = new DataRDFTerm(prefix, triple.getPos(2));
						rdfHash.put(triple.getPos(2), rdfTermObject);
					}

					rdfTermSubject.addPredicate(rdfTermObject, triple.getPos(1));

					final OperatorIDTuple<Operator> opIDT = new OperatorIDTuple<Operator>(rdfTermObject, 0);

					if(!rdfTermSubject.getSucceedingOperators().contains(opIDT)) {
						rdfTermSubject.addSucceedingOperator(opIDT);
					}

					rdfTermToJoin.add(rdfTermSubject);
				}
			});
		}
		catch(final Exception e) {
			System.out.println("Pasted string does not contain N3-data. Trying { (triple pattern)* } (RDF Term)* format!");

			try {
				final ASTGroupConstraint node = SPARQL1_1Parser.parseN3GroupGraphPatternsAndRDFTerms(content);

				for(int i = 0; i < node.jjtGetNumChildren(); i++) {
					final Node child = node.jjtGetChild(i);

					if(child instanceof ASTGroupConstraint) {
						final VisualDataGenerator vdg = new VisualDataGenerator(((VisualGraphOperatorWithPrefix) this.visualGraphs.get(0)).prefix);
						rdfTermToJoin.addAll((Collection<Operator>) child.jjtAccept((SPARQL1_1ParserVisitor) vdg, rdfHash));
					}
					else {
						// it is an RDF term!
						final Item item = SPARQLCoreParserVisitorImplementation.getItem(child);

						DataRDFTerm rdfTerm = rdfHash.get(item);

						if(rdfTerm == null) {
							rdfTerm = new DataRDFTerm(((VisualGraphOperatorWithPrefix) this.visualGraphs.get(0)).prefix, item);
							rdfHash.put(item, rdfTerm);
							rdfTermToJoin.add(rdfTerm);
						}
					}
				}
			}
			catch(final Throwable ex) {
				JOptionPane.showOptionDialog(this, ex.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
			}
		}

		final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();

		for(final Operator op : RDFTerm.findRootNodes(rdfTermToJoin)) {
			op.setParents();

			rootList.add(new GraphWrapperOperator(op));
		}

		System.out.println("Displaying data...");

		this.visualGraphs.get(0).updateMainPanel(
				this.visualGraphs.get(0).createGraph(rootList, false, false,
						false,
						Arrange.values()[0]));
	}

	/**
	 * The main method to initialize the VisualEditor.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		new DataEditor("", null);
	}
}