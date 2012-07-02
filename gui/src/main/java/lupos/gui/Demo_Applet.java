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
package lupos.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.DebugContainerQuery;
import lupos.engine.evaluators.JenaQueryEvaluator;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE;
import lupos.gui.anotherSyntaxHighlighting.LinePainter;
import lupos.gui.anotherSyntaxHighlighting.LuposDocument;
import lupos.gui.anotherSyntaxHighlighting.LuposDocumentReader;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.anotherSyntaxHighlighting.javacc.RIFParser;
import lupos.gui.anotherSyntaxHighlighting.javacc.SPARQLParser;
import lupos.gui.anotherSyntaxHighlighting.javacc.TurtleParser;
import lupos.gui.debug.ShowResult;
import lupos.engine.evaluators.QueryEvaluator.DEBUG;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.evaluators.SesameQueryEvaluator;
import lupos.engine.evaluators.StreamQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.application.CollectRIFResult;
import lupos.engine.operators.application.IterateOneTimeThrough;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoin;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoinNonStandardSPARQL;
import lupos.gui.debug.EvaluationDemoToolBar;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperAST;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperASTRIF;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperator;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperRules;
import lupos.gui.operatorgraph.viewer.Viewer;
import lupos.gui.operatorgraph.viewer.ViewerPrefix;
import lupos.gui.operatorgraph.visualeditor.dataeditor.DataEditor;
import lupos.gui.operatorgraph.visualeditor.dataeditor.IDataEditor;
import lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer.CondensedViewToolBar;
import lupos.gui.operatorgraph.visualeditor.dataeditor.datageneralizer.CondensedViewViewer;
import lupos.gui.operatorgraph.visualeditor.queryeditor.AdvancedQueryEditor;
import lupos.gui.operatorgraph.visualeditor.queryeditor.IQueryEditor;
import lupos.misc.FileHelper;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.rif.BasicIndexRuleEvaluator;
import lupos.rif.datatypes.RuleResult;
import lupos.rif.generated.syntaxtree.CompilationUnit;
import lupos.rif.model.Document;
import lupos.sparql1_1.ASTAs;
import lupos.sparql1_1.ASTSelectQuery;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.TokenMgrError;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.rio.RDFParseException;

import xpref.IXPref;
import xpref.XPref;
import xpref.datatypes.BooleanDatatype;
import xpref.datatypes.CollectionDatatype;
import xpref.datatypes.ColorDatatype;
import xpref.datatypes.FontDatatype;
import xpref.datatypes.IntegerDatatype;

import com.hp.hpl.jena.n3.turtle.TurtleParseException;
import com.hp.hpl.jena.query.QueryParseException;

public class Demo_Applet extends JApplet implements IXPref, IDataEditor, IQueryEditor {
	// set webdemo to false in the case that you want to run it under eclipse
	// webdemo=true => example files are read from the current jar-file
	protected enum DEMO_ENUM {
		ECLIPSE, TUTORIAL, TUTORIAL2, PROJECT_DEMO, LOCALONEJAR
	}

	private static final long serialVersionUID = -2726848841473438879L;

	private final String TAB_TITLE_RULES = "RIF rules";
	private final String TAB_TITLE_QUERY = "SPARQL query";
	private final String TAB_TITLE_DATA = "RDF data";
	private final String TAB_TITLE_RESULT = "Evaluation result";

	protected DEMO_ENUM webdemo = DEMO_ENUM.ECLIPSE;
	private String PATH_QUERIES;
	private String PATH_RULES;
	private String PATH_DATA;
	private LuposJTextPane tp_queryInput;
	private LuposJTextPane tp_rifInput;
	private LuposJTextPane tp_dataInput;
	private LinePainter lp_queryInput;
	private LinePainter lp_rifInput;
	private LinePainter lp_dataInput;
	private Color lp_color;
	private JTextArea ta_rifInputErrors;
	private JTextArea ta_queryInputErrors;
	private JTextArea ta_dataInputErrors;
	private JComboBox cobo_evaluator;
	private JPanel resultpanel = null;
	private String query = "";
	private String data = "";
	private ViewerPrefix prefixInstance = null;
	private BooleanReference usePrefixes = new BooleanReference(true);
	private DebugViewerCreator debugViewerCreator = null;
	private List<DebugContainer<BasicOperatorByteArray>> ruleApplications = null;
	private List<DebugContainer<BasicOperatorByteArray>> ruleApplicationsForMaterialization = null;
	private DebugViewerCreator materializationInfo = null;
	private RuleResult errorsInOntology = null;
	private String inferenceRules = null;
	private JPanel masterpanel = null;
	private JTabbedPane tabbedPane_globalMainpane = null;
	protected boolean isApplet = false;
	protected JFrame frame = null;
	private XPref preferences;
	private JScrollPane rifInputSP;
	private JScrollPane queryInputSP;
	private JScrollPane dataInputSP;
	private Font defaultFont = null;
	private Collection<URILiteral> defaultGraphs;
	private Demo_Applet myself;
	private JButton bt_evalDemo;
	private JButton bt_evaluate;
	private JButton bt_MeasureExecutionTimes;
	private JButton bt_rifEvalDemo;
	private JButton bt_rifEvaluate;
	private JButton bt_rifMeasureExecutionTimes;
	private Viewer operatorGraphViewer = null;
	private JComboBox comboBox_sparqlInference;
	private JComboBox comboBox_sparqlInferenceMaterialization;
	private JComboBox checkBox_sparqlInferenceGenerated;
	private QueryResult[] resultQueryEvaluator;

	public static void main(final String args[]) {
		final Demo_Applet applet = new Demo_Applet();

		if (args.length > 0) {
			applet.setType(args[0]);
		}

		final JPanel panel = applet.initialise();
		applet.frame = new JFrame();

		applet.frame.setIconImage(getIcon(applet.webdemo));

		if (applet.webdemo == DEMO_ENUM.TUTORIAL
				|| applet.webdemo == DEMO_ENUM.TUTORIAL2) {
			applet.frame.setTitle("SPARQL Tutorial using LUPOSDATE");
		} else {
			applet.frame.setTitle("LUPOSDATE Demonstration");
		}

		applet.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		applet.frame.setContentPane(panel);
		applet.frame.setSize(800, 640);
		// applet.frame.setMinimumSize(new Dimension(800, 640));
		applet.frame.setLocationRelativeTo(null);

		applet.preferencesChanged();

		applet.frame.setVisible(true);
	}

	protected static Image getIcon(final DEMO_ENUM webdemo) {
		if (webdemo != DEMO_ENUM.ECLIPSE) {
			return new ImageIcon(Demo_Applet.class.getResource("/icons/demo.gif"))
					.getImage();
		} else {
			return new ImageIcon(Demo_Applet.class.getResource("/icons/demo.gif").getFile()).getImage();
		}
	}

	/**
	 * Main Method.
	 */
	@Override
	public void init() {
		this.isApplet = true;

		this.setType(this.getParameter("type"));

		this.setSize(800, 640);

		final JPanel panel = this.initialise();

		this.add(panel);

		this.preferencesChanged();
	}

	protected void setType(final String type) {
		if (type != null) {
			if (type.toLowerCase().compareTo("demo") == 0) {
				this.webdemo = DEMO_ENUM.PROJECT_DEMO;
			} else if (type.toLowerCase().compareTo("tutorial") == 0) {
				this.webdemo = DEMO_ENUM.TUTORIAL;
			} else if (type.toLowerCase().compareTo("tutorial2") == 0) {
				this.webdemo = DEMO_ENUM.TUTORIAL2;
			} else if (type.toLowerCase().compareTo("eclipse") == 0) {
				this.webdemo = DEMO_ENUM.ECLIPSE;
			} else if (type.toLowerCase().compareTo("localonejar") == 0) {
				this.webdemo = DEMO_ENUM.LOCALONEJAR;
			}
		}
	}

	public JPanel initialise() {
		try {
			this.myself = this;

			if (this.isApplet) {
				System.out.println("starting as applet...");
			} else {
				System.out.println("starting as program...");
			}
			
			if (this.webdemo != DEMO_ENUM.ECLIPSE) {
				this.preferences = XPref.getInstance(Demo_Applet.class.getResource("/preferencesMenu.xml"));
			} else {
				this.preferences = XPref.getInstance(new URL("file:"+GUI.class.getResource("/preferencesMenu.xml").getFile()));

			}

			this.preferences.registerComponent(this);

			this.usePrefixes = new BooleanReference(BooleanDatatype.getValues("applet_usePrefixes").get(0).booleanValue());

			this.PATH_QUERIES = "/sparql/";

			this.PATH_RULES = "/rif/";

			this.PATH_DATA = "/data/";

			this.masterpanel = new JPanel();
			this.masterpanel.setLayout(new BorderLayout());

			// create a tabbed pane inside main window to display query input,
			// data input, ontology input and evaluation results
			this.tabbedPane_globalMainpane = new JTabbedPane();
			this.masterpanel.add(this.tabbedPane_globalMainpane,
					BorderLayout.CENTER);

			this.generateEvaluatorChooseAndPreferences();

			final JSplitPane splitPane_queryInput = generateJSplitPane(
					this.generateQueryTab(), this.generateQueryInputErrorBox());

			final JPanel queryInputTab = new JPanel(new BorderLayout());

			final JPanel evalPanel = generateEvalpanel();

			queryInputTab.add(splitPane_queryInput, BorderLayout.CENTER);
			queryInputTab.add(evalPanel, BorderLayout.SOUTH);

			// add a new tab for query input to the tabbed pane
			this.tabbedPane_globalMainpane.add(this.TAB_TITLE_QUERY, queryInputTab);

			final JSplitPane splitPane_rifInput = generateJSplitPane(
					this.generateRifTab(), this.generateRifInputErrorBox());

			final JPanel rifInputTab = new JPanel(new BorderLayout());

			final JPanel rifEvalPanel = generateRifEvalPanel();

			rifInputTab.add(splitPane_rifInput, BorderLayout.CENTER);
			rifInputTab.add(rifEvalPanel, BorderLayout.SOUTH);

			this.tabbedPane_globalMainpane.add(this.TAB_TITLE_RULES, rifInputTab);

			final JSplitPane splitPane_dataInput = generateJSplitPane(
					this.generateDataTab(), this.generateDataInputErrorBox());

			// add a new tab for data input to the tabbed pane
			this.tabbedPane_globalMainpane.add(this.TAB_TITLE_DATA,
					splitPane_dataInput);

			// add a new tab for evaluation results to the tabbed pane
			this.tabbedPane_globalMainpane.add(this.TAB_TITLE_RESULT, new JPanel());

			this.masterpanel.add(this.tabbedPane_globalMainpane,
					BorderLayout.CENTER);

			this.masterpanel.setVisible(true);

			// System.setErr(new PrintStream(new OutputStream() {
			// public void write(int arg0) throws IOException {
			// StringBuffer s = new StringBuffer();
			//
			// for(char c : Character.toChars(arg0))
			// s.append(c);
			//
			// ta_errors.setText(ta_errors.getText() + s.toString());
			// }
			// }, true));
		} catch (final Throwable th) {
			th.printStackTrace();
		}

		return this.masterpanel;
	}

	private static JSplitPane generateJSplitPane(final Component topComponent,
			final Component bottomComponent) {
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setPreferredSize(new Dimension(790, 600));
		splitPane.setDividerLocation(400);
		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		splitPane.setTopComponent(topComponent);
		splitPane.setBottomComponent(bottomComponent);
		return splitPane;
	}

	public static LinkedList<String> generateLookAndFeelList() {
		final LinkedList<String> lafList = new LinkedList<String>();

//		lafList.add("Acryl");
//		lafList.add("Aero");
//		lafList.add("Aluminium");
//		lafList.add("Bernstein");
//		lafList.add("Fast");
//		lafList.add("HiFi");
//		lafList.add("Luna");
//		lafList.add("McWin");
//		lafList.add("Mint");
//		lafList.add("Noire");
//		lafList.add("Smart");

		final UIManager.LookAndFeelInfo[] lafInfo = UIManager
				.getInstalledLookAndFeels();

		for (int i = 0; i < lafInfo.length; i++) {
			lafList.add(lafInfo[i].getName());
			if(lafInfo[i].getName().compareTo("Metal")==0){
				lafList.add("Metal Ocean");
			}
		}

		return lafList;
	}

	/**
	 * Generate the stuff to choose a evaluator.
	 */
	private void generateEvaluatorChooseAndPreferences() {
		final JPanel rowpanel = new JPanel(new BorderLayout());

		final JPanel leftpanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5,
				0));
		leftpanel.setBounds(0, 5, 505, 30);

		final JLabel info = new JLabel();
		info.setText("Choose an evaluator:\t");
		leftpanel.add(info);

		// create combobox for query files, fill it and add it to Applet...
		this.cobo_evaluator = new JComboBox(this.getEvaluators());
		this.cobo_evaluator.setSelectedIndex(0);
		this.cobo_evaluator.setPreferredSize(new Dimension(160, 20));
		this.cobo_evaluator.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				enableOrDisableButtons(false);
				enableOrDisableButtons(true);
			}

		});
		leftpanel.add(this.cobo_evaluator);

		rowpanel.add(leftpanel, BorderLayout.WEST);

		final JButton preferencesButton = new JButton("Preferences");
		preferencesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				final String[] ids = new String[] { "lookAndFeel",
						"syntaxHighlighting", "operatorGraph_useStyledBoxes",
						"ast_useStyledBoxes", "queryEditor_useStyledBoxes",
						"dataEditor_useStyledBoxes",
						"condensedViewViewer_useStyledBoxes",
						"serviceCallApproach"};

				final LinkedList<String> idList = new LinkedList<String>();

				for (int i = 0; i < ids.length; ++i) {
					idList.add(ids[i]);
				}
				try {
					Demo_Applet.this.preferences.showDialog(false, idList);
				} catch (final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		});

		rowpanel.add(preferencesButton, BorderLayout.EAST);

		this.masterpanel.add(rowpanel, BorderLayout.NORTH);
	}

	private void enableOrDisableEvaluationDemoButtonSPARQL() {
		final String chosen = (String) this.cobo_evaluator.getSelectedItem();
		if (chosen.compareTo("Jena") == 0 || chosen.compareTo("Sesame") == 0) {
			this.bt_evalDemo.setEnabled(false);
		} else
			this.bt_evalDemo.setEnabled(true);
	}
	
	private boolean isEvaluatorWithSupportOfRIFChosen(){
		final String chosen = (String) this.cobo_evaluator.getSelectedItem();
		return (chosen.compareTo("Jena") != 0 && chosen.compareTo("Sesame") != 0);
	}

	private void enableOrDisableEvaluationButtonsRIF() {
		boolean enable=isEvaluatorWithSupportOfRIFChosen();
		this.bt_rifEvaluate.setEnabled(enable);
		this.bt_rifMeasureExecutionTimes.setEnabled(enable);
	}

	private void enableOrDisableEvaluationDemoButtonRIF() {
		this.bt_rifEvalDemo.setEnabled(isEvaluatorWithSupportOfRIFChosen());
	}

	private void enableOrDisableButtons(final boolean queryOrRif) {
		if(queryOrRif){
			enableOrDisableEvaluationDemoButtonSPARQL();
			this.bt_evaluate.setEnabled(true);
			this.bt_MeasureExecutionTimes.setEnabled(true);
		} else {
			enableOrDisableEvaluationDemoButtonRIF();
			enableOrDisableEvaluationButtonsRIF();
		}
	}
	
	private class LineNumbers extends JLabel {
		private static final long serialVersionUID = 1L;
		private int lWidth = 15;
		private final JTextPane pane;
		private final Font font;
		private final FontMetrics fm;
		private final int h;

		public LineNumbers(final JTextPane jTextPane) {
			this.pane = jTextPane;
			this.font = this.pane.getFont();
			this.fm = this.pane.getFontMetrics(this.font);
			this.h = this.fm.getHeight();

			this.calculateWidth();
		}

		private void calculateWidth() {
			int i = this.h;
			int row = 1;

			while (i < this.pane.getHeight()) {
				final String s = Integer.toString(row);

				if (this.fm.stringWidth(s) > this.lWidth) {
					this.lWidth = this.fm.stringWidth(s);
				}

				i += this.h;
				row++;
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(this.lWidth + 1, (int) this.pane
					.getPreferredSize().getHeight());
		}

		@Override
		public void paintComponent(final Graphics g) {
			super.paintComponents(g);

			g.setFont(this.font);

			this.calculateWidth();

			int i = this.h;
			int row = 1;

			while (i < this.pane.getHeight()) {
				final String s = Integer.toString(row);

				g.drawString(s, 0 + this.lWidth - this.fm.stringWidth(s), i);

				i += this.h;
				row++;
			}
		}
	}

	/**
	 * Generate the stuff for the query input.
	 */
	private JPanel generateQueryTab() {

		final JButton bt_visualEdit = new JButton("Visual Edit");
		bt_visualEdit.addActionListener(new ActionListener() {
			@SuppressWarnings("unused")
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				new AdvancedQueryEditor(Demo_Applet.this.tp_queryInput.getText(), Demo_Applet.this.tp_dataInput.getText(), Demo_Applet.this.myself, getIcon(Demo_Applet.this.webdemo));
			}
		});

		final LuposDocument document = new LuposDocument();
		this.tp_queryInput = new LuposJTextPane(document);
		document.init(SPARQLParser.createILuposParser(new LuposDocumentReader(document)), true, 100);

		this.queryInputSP = new JScrollPane(this.tp_queryInput);

		return generateInputTab(bt_visualEdit, null,
				"Choose a SPARQL query:\t", this.getQueries(), this.PATH_QUERIES,
				"Clear query field", this.tp_queryInput, this.queryInputSP);
	}

	/**
	 * Generate the stuff for the query input.
	 */
	private JPanel generateRifTab() {

		// final JButton bt_visualEdit = new JButton("Visual Edit");
		// bt_visualEdit.addActionListener(new ActionListener() {
		// public void actionPerformed(final ActionEvent arg0) {
		// new AdvancedQueryEditor(tp_queryInput.getText(), tp_dataInput
		// .getText(), myself, getIcon(webdemo));
		// }
		// });

		final LuposDocument document = new LuposDocument();
		this.tp_rifInput = new LuposJTextPane(document);
		document.init(RIFParser.createILuposParser(new LuposDocumentReader(document)), true, 100);

		this.rifInputSP = new JScrollPane(this.tp_rifInput);

		return generateInputTab(null, null, "Choose a RIF query:\t",
				this.getRuleFiles(), this.PATH_RULES, "Clear rule field",
				this.tp_rifInput, this.rifInputSP);
	}

	/**
	 * Generate the stuff for error output on the SPARQL query tab.
	 */
	private JPanel generateQueryInputErrorBox() {
		this.ta_queryInputErrors = new JTextArea();
		return generateInputErrorBox(this.tp_queryInput,
				this.ta_queryInputErrors);
	}

	/**
	 * Generate the stuff for error output on the RDF Data tab.
	 */
	private JPanel generateDataInputErrorBox() {
		this.ta_dataInputErrors = new JTextArea();
		return generateInputErrorBox(this.tp_dataInput, this.ta_dataInputErrors);
	}

	/**
	 * Generate the stuff for error output on the RIF tab.
	 */
	private JPanel generateRifInputErrorBox() {
		this.ta_rifInputErrors = new JTextArea();
		return generateInputErrorBox(this.tp_rifInput, this.ta_rifInputErrors);
	}

	/**
	 * Generate the stuff for error output on the RDF Data tab.
	 */
	private JPanel generateInputErrorBox(
			final LuposJTextPane input, final JTextArea inputErrors) {
		final JPanel rowpanel = new JPanel(new BorderLayout());

		final JLabel info = new JLabel();
		info.setText("Errors detected:");
		rowpanel.add(info, BorderLayout.WEST);

		final JPanel clearpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		// create clear-button, add actionListener and add it to Applet...
		final JButton bt_clear = new JButton("Clear error field");
		bt_clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				inputErrors.setText("");
				input.disableErrorLine();
			}
		});

		clearpanel.add(bt_clear);

		rowpanel.add(clearpanel, BorderLayout.EAST);

		final JPanel masterpanel1 = new JPanel(new BorderLayout());
		masterpanel1.add(rowpanel, BorderLayout.NORTH);

		inputErrors.setFont(new Font("Courier New", Font.PLAIN, 12));
		inputErrors.setEditable(false);

		final JScrollPane scroll = new JScrollPane(inputErrors);

		masterpanel1.add(scroll, BorderLayout.CENTER);

		return masterpanel1;
	}

	/**
	 * Generate the stuff for the data input.
	 */
	private JPanel generateDataTab() {

		final JButton bt_visualEdit = new JButton("Visual Edit");
		bt_visualEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				new DataEditor(Demo_Applet.this.tp_dataInput.getText(), myself, getIcon(Demo_Applet.this.webdemo));
			}
		});

		final JButton bt_CondensedView = new JButton("Condense Data");
		bt_CondensedView.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				if (prefixInstance == null)
					prefixInstance = new ViewerPrefix(usePrefixes.isTrue(), null);
				final CondensedViewToolBar toolBar = new CondensedViewToolBar(
						Demo_Applet.this.tp_dataInput.getText(),
						prefixInstance);
				final CondensedViewViewer operatorGraphViewer = new CondensedViewViewer(
						prefixInstance, false, getIcon(webdemo), toolBar);
				toolBar.setOperatorGraphViewer(operatorGraphViewer);

				repaint();
			}
		});

		final LuposDocument document_data = new LuposDocument();
		this.tp_dataInput = new LuposJTextPane(document_data);
		document_data.init(TurtleParser.createILuposParser(new LuposDocumentReader(document_data)), true, 100);
		
		this.dataInputSP = new JScrollPane(this.tp_dataInput);

		return generateInputTab(bt_visualEdit, bt_CondensedView,
				"Choose RDF data:\t", this.getDataFiles(), this.PATH_DATA,
				"Clear data field", this.tp_dataInput, this.dataInputSP);
	}
	
	public String getResourceAsString(String resource){
		final URL url = GUI.class.getResource(resource);
		
		return FileHelper.readFile(resource, new FileHelper.GetReader() {

				public Reader getReader(final String filename) throws FileNotFoundException {
					try {
						InputStream stream = null;
						stream = this.getClass().getResourceAsStream(filename);
						return new java.io.InputStreamReader(stream);
					} catch(Exception e){
						return new FileReader(url.getFile());
					}
				}
			});
	}

	private JPanel generateInputTab(final JButton bt_visualEdit,
			final JButton bt_CondensedView, final String chooseText,
			final String[] toChoose, final String PATH, final String clearText,
			final LuposJTextPane tp_input,
			final JScrollPane inputSP) {
		final JPanel rowpanel = new JPanel(new BorderLayout());

		final JPanel choosepanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
				5, 0));

		// create buttons 'visual edit' and 'condensed data'
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
				10, 0));

		if (bt_visualEdit != null)
			buttonPanel.add(bt_visualEdit);

		if (bt_CondensedView != null)
			buttonPanel.add(bt_CondensedView);

		rowpanel.add(buttonPanel, BorderLayout.WEST);

		final JLabel info2 = new JLabel(chooseText);

		choosepanel.add(info2);

		// create combobox for query files, fill it and add it to Applet...
		final JComboBox cb_data = new JComboBox(toChoose);
		cb_data.setPreferredSize(new Dimension(130, 20));
		cb_data.setSelectedIndex(0);

		choosepanel.add(cb_data);

		// create select-button, add actionListener and add it to Applet...
		final JButton bt_select = new JButton("Select");
		bt_select.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				// open file and write content to textarea...
				tp_input.setText(Demo_Applet.this.getResourceAsString(PATH + cb_data.getSelectedItem().toString()));
				tp_input.disableErrorLine();
				tp_input.grabFocus();
			}
		});

		choosepanel.add(bt_select);

		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		panel.add(choosepanel);

		// create clear-button, add actionListener and add it to Applet...
		final JButton bt_clear = new JButton(clearText);
		bt_clear.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				tp_input.setText("");
				tp_input.disableErrorLine();
			}
		});

		panel.add(bt_clear);

		rowpanel.add(panel, BorderLayout.EAST);

		final JPanel mainpanel = new JPanel(new BorderLayout());
		mainpanel.add(rowpanel, BorderLayout.NORTH);
		mainpanel.add(inputSP, BorderLayout.CENTER);

		return mainpanel;
	}
	
	public class RuleSets {
		public String getRIF(){
			return Demo_Applet.this.tp_rifInput.getText();
		}
		public String getRDFS(){
			switch((GENERATION)Demo_Applet.this.checkBox_sparqlInferenceGenerated.getSelectedItem()){
			case FIXED:
				return (Demo_Applet.this.webdemo == DEMO_ENUM.ECLIPSE) ? FileHelper.fastReadFile(Demo_Applet.this.PATH_RULES + "rule_rdfs.rif")
						: readFile(Demo_Applet.this.PATH_RULES + "rule_rdfs.rif");
			case GENERATED:
				return InferenceHelper.getRIFInferenceRulesForRDFSOntology(Demo_Applet.this.tp_dataInput.getText());
			default:
			case GENERATEDOPT:
				return InferenceHelper.getRIFInferenceRulesForRDFSOntologyAlternative(Demo_Applet.this.tp_dataInput.getText());
			}
		}
		public String getOWL2RL(){
			switch((GENERATION)Demo_Applet.this.checkBox_sparqlInferenceGenerated.getSelectedItem()){
			case FIXED:
				return (Demo_Applet.this.webdemo == DEMO_ENUM.ECLIPSE) ? FileHelper.fastReadFile(Demo_Applet.this.PATH_RULES + "rule_owl2rl.rif")
						: readFile(Demo_Applet.this.PATH_RULES + "rule_owl2rl.rif");
			case GENERATED:
				return InferenceHelper.getRIFInferenceRulesForOWL2Ontology(Demo_Applet.this.tp_dataInput.getText());
			default:
			case GENERATEDOPT:
				return InferenceHelper.getRIFInferenceRulesForOWL2OntologyAlternative(Demo_Applet.this.tp_dataInput.getText());
			}
		}
	}
	
	private RuleSets rulesets = new RuleSets();
	
	protected enum SPARQLINFERENCE {
		NONE(){
			@Override
			public String toString(){
				return "None";
			}
			@Override			
			public boolean isMaterializationChoice() {
				return false;
			}
			@Override
			public boolean isGeneratedChoice() {
				return false;
			}
			@Override
			public String getRuleSet(RuleSets rulesets) {
				return null;
			}
		},
		RIF(){
			@Override			
			public boolean isMaterializationChoice() {
				return true;
			}
			@Override
			public boolean isGeneratedChoice() {
				return false;
			}			
			@Override
			public String getRuleSet(RuleSets rulesets) {
				return rulesets.getRIF();
			}
		},
		RDFS(){
			@Override			
			public boolean isMaterializationChoice() {
				return true;
			}
			@Override
			public boolean isGeneratedChoice() {
				return true;
			}
			@Override
			public String getRuleSet(RuleSets rulesets) {
				return rulesets.getRDFS();
			}
		},
		OWL2RL{
			@Override
			public String toString(){
				return "OWL2 RL";
			}
			@Override			
			public boolean isMaterializationChoice() {
				return true;
			}
			@Override
			public boolean isGeneratedChoice() {
				return true;
			}
			@Override
			public String getRuleSet(RuleSets rulesets) {
				return rulesets.getOWL2RL();
			}
		};

		public abstract boolean isMaterializationChoice();

		public abstract boolean isGeneratedChoice();
		
		public abstract String getRuleSet(RuleSets rulesets);
	}

	protected static enum SPARQLINFERENCEMATERIALIZATION {
		COMBINEDQUERYOPTIMIZATION(){
			@Override
			public String toString(){
				return "On Demand";
			}
		},
		MATERIALIZEALL(){
			@Override
			public String toString(){
				return "Materialize";
			}
		}
	}
	
	protected static enum GENERATION {
		GENERATEDOPT(){
			@Override
			public String toString(){
				return "Gen. Alt.";
			}
		},
		GENERATED(){
			@Override
			public String toString(){
				return "Generated";
			}
		},
		FIXED(){
			@Override
			public String toString(){
				return "Fixed";
			}
		}
	}

	private JPanel generateEvalpanel() {
		
		this.comboBox_sparqlInferenceMaterialization = new JComboBox();
		
		for (int i = 0; i < SPARQLINFERENCEMATERIALIZATION.values().length; i++) {
			this.comboBox_sparqlInferenceMaterialization.addItem(SPARQLINFERENCEMATERIALIZATION.values()[i]);
		}

		this.comboBox_sparqlInferenceMaterialization.setSelectedIndex(0);
		
		this.checkBox_sparqlInferenceGenerated = new JComboBox();
		for(GENERATION generation: GENERATION.values()){
			this.checkBox_sparqlInferenceGenerated.addItem(generation);
		}
		
		this.comboBox_sparqlInference  = new JComboBox();

		for (int i = 0; i < SPARQLINFERENCE.values().length; i++) {
			this.comboBox_sparqlInference.addItem(SPARQLINFERENCE.values()[i]);
		}

		this.comboBox_sparqlInference.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				SPARQLINFERENCE sparqlinference = (SPARQLINFERENCE)comboBox_sparqlInference.getSelectedItem();
				comboBox_sparqlInferenceMaterialization.setEnabled(sparqlinference.isMaterializationChoice());
				checkBox_sparqlInferenceGenerated.setEnabled(sparqlinference.isGeneratedChoice());
			}
		});
		
		this.comboBox_sparqlInference.setSelectedIndex(0);
		
		// create evaluate-button, add actionListener and add it to Applet...
		this.bt_evaluate = new JButton("Evaluate");
		bt_evaluate.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {

				if (prepareForEvaluation(false)) {

					evaluateSPARQLQuery(EvaluationMode.RESULT);

					repaint();
				}
			}
		});

		bt_evalDemo = this.createEvaluationDemoButton(true);
		bt_evalDemo.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				if (prepareForEvaluation(false)) {

					evaluateSPARQLQuery(EvaluationMode.DEMO);

					repaint();
				}
			}
		});

		this.bt_MeasureExecutionTimes = new JButton("Execution Times");
		bt_MeasureExecutionTimes.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				if (prepareForEvaluation(false)) {
					evaluateSPARQLQuery(EvaluationMode.TIMES);

					repaint();
				}
			}
		});

		return generateEvalpanel(new JLabel("Inference:"), this.comboBox_sparqlInference, this.comboBox_sparqlInferenceMaterialization, this.checkBox_sparqlInferenceGenerated, new JLabel(" Evaluation:"), this.bt_evaluate, this.bt_evalDemo,
				this.bt_MeasureExecutionTimes);
	}

	private JPanel generateRifEvalPanel() {
		// create evaluate-button, add actionListener and add it to Applet...
		this.bt_rifEvaluate = new JButton("Evaluate");
		bt_rifEvaluate.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {

				if (prepareForEvaluation(false)) {

					evaluateRIFRule(EvaluationMode.RESULT);

					repaint();
				}
			}
		});

		bt_rifEvalDemo = this.createEvaluationDemoButton(true);
		bt_rifEvalDemo.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				if (prepareForEvaluation(false)) {

					evaluateRIFRule(EvaluationMode.DEMO);

					repaint();
				}
			}
		});

		this.bt_rifMeasureExecutionTimes = new JButton("Execution Times");
		bt_rifMeasureExecutionTimes.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				if (prepareForEvaluation(false)) {

					evaluateRIFRule(EvaluationMode.TIMES);

					repaint();
				}
			}
		});

		return generateEvalpanel(this.bt_rifEvaluate, this.bt_rifEvalDemo,
				this.bt_rifMeasureExecutionTimes);
	}

	private static JPanel generateEvalpanel(final JComponent... components ) {
		final JPanel evalpanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		for(JComponent component: components){
			evalpanel.add(component);
		}
		return evalpanel;
	}

	private boolean prepareForEvaluation(final boolean rif) {
		if (operatorGraphViewer != null && operatorGraphViewer.isVisible()) {
			if (JOptionPane
					.showConfirmDialog(
							null,
							"This operation first closes the existing Evaluation Demo...",
							"Closing existing Evaluation Demo",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.WARNING_MESSAGE) != 0)
				return false;
			operatorGraphViewer.processWindowEvent(new WindowEvent(
					operatorGraphViewer, WindowEvent.WINDOW_CLOSING));
		}
//		if (Indices.operatorGraphViewer != null
//				&& Indices.operatorGraphViewer.isVisible()) {
//			if (JOptionPane
//					.showConfirmDialog(
//							null,
//							"This operation first closes the existing Materialization Demo...",
//							"Closing existing Materialization Demo",
//							JOptionPane.OK_CANCEL_OPTION,
//							JOptionPane.WARNING_MESSAGE) != 0)
//				return false;
//			Indices.operatorGraphViewer.processWindowEvent(new WindowEvent(
//					Indices.operatorGraphViewer, WindowEvent.WINDOW_CLOSING));
//		}
		try {
			if (BooleanDatatype.getValues("clearErrorField").get(0)
					.booleanValue()) {
				displayErrorMessage("", false);
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		if (rif)
			tp_rifInput.disableErrorLine();
		else
			tp_queryInput.disableErrorLine();
		tp_dataInput.disableErrorLine();

		if (resultpanel != null) {
			remove(this.resultpanel);
		}

		data = tp_dataInput.getText();

		return true;
	}

	/*
	 * Create Button to display RDFS Materialization when using an external
	 * Ontology file
	 */
//	private JButton createRDFSMaterializationButton() {
//		final JButton bt_RDFSMat = new JButton("Show RDFS Materialization");
//
//		bt_RDFSMat.setMargin(new Insets(0, 0, 0, 0));
//		final boolean enabled = StaticDataHolder.getOperatorGraphRules() != null;
//		bt_RDFSMat.setEnabled(enabled);
//
//		if (enabled) {
//			bt_RDFSMat.addActionListener(new ActionListener() {
//
//				public void actionPerformed(final ActionEvent arg0) {
//					if (StaticDataHolder.getOperatorGraphRules() != null)
//						new Viewer(StaticDataHolder.getOperatorGraphRules(),
//								"RDFS materialization", false,
//								webdemo != DEMO_ENUM.ECLIPSE, StaticDataHolder
//										.getPrefixInstance());
//				}
//
//			});
//		}
//
//		return bt_RDFSMat;
//	}

	/**
	 * create the button to show the operator graph.
	 */
	private JButton createEvaluationDemoButton(final Boolean enabled) {
		// create OperatorGraph-button, add actionListener and add it to
		// Applet...
		final JButton bt_evalDemo_local = new JButton("Eval. Demo");
		bt_evalDemo_local.setEnabled(enabled);
		return bt_evalDemo_local;
	}

	private String[] getEvaluators() {
		// started with Java Web Start? Java Web start has a more restrictive
		// rights
		// management, i.e. Jena and Sesame do not work with Java Web Start...
		if (!this.isApplet && this.webdemo != DEMO_ENUM.ECLIPSE) {
			if (this.webdemo == DEMO_ENUM.LOCALONEJAR) {
				return new String[] { "MemoryIndex", "RDF3X", "Stream", "Jena",
						"Sesame" };
			} else {
				return new String[] { "MemoryIndex", "RDF3X", 
						"Stream" };
			}
		} else if (this.webdemo == DEMO_ENUM.LOCALONEJAR) {
			return new String[] { "MemoryIndex", "RDF3X", "Stream", "Jena",
					"Sesame" };
		} else {
			return new String[] { "MemoryIndex", "RDF3X", 
					"Stream", "Jena", "Sesame" };
		}
	}

	protected Class<?> getEvaluatorClass(final int index) {
			final Class<?>[] s = { MemoryIndexQueryEvaluator.class,
					RDF3XQueryEvaluator.class, StreamQueryEvaluator.class,
					JenaQueryEvaluator.class, SesameQueryEvaluator.class };

			return s[index];
	}
	
	private String[] getFiles(final String path, final String suffix){
		// create array list for files...
		final ArrayList<String> tmp = new ArrayList<String>();

		// load files...
		final String[] tmp_lst = new File(Demo_Applet.class.getResource(path).getFile()).list();

		if(tmp_lst!=null)
			// walk through files...
			for (int i = 0; i < tmp_lst.length; i++) {
				// if file ends with suffix...
				if (tmp_lst[i].endsWith(suffix)) {
					tmp.add(tmp_lst[i]); // add file to file array list
				}
			}

		// return list of available queries...
		return tmp.toArray(new String[tmp.size()]);
	}

	/**
	 * Get Query Files.
	 * 
	 * @return string array of available queries
	 */
	private String[] getQueries() {
		switch (this.webdemo) {
		case LOCALONEJAR:
		case PROJECT_DEMO:
			// started with Java Web Start?
			// Java Web start has a more restrictive rights
			// management, i.e. sp2b and lubm queries > 7 do not work with Java
			// Web Start...
			if (!this.isApplet && this.webdemo != DEMO_ENUM.ECLIPSE
					&& this.webdemo != DEMO_ENUM.LOCALONEJAR) {
				return new String[] { "lubm_asktest.sparql",
						"lubm_constructtest.sparql", "lubm_query1.sparql",
						"lubm_query2.sparql", "lubm_query3.sparql",
						"lubm_query4.sparql", "lubm_query5.sparql",
						"lubm_query6.sparql", "lubm_query7.sparql" };
			}

			// if we are in a jar for the project demo...
			return new String[] { "lubm_query1.sparql", "lubm_query2.sparql",
					"lubm_query3.sparql", "lubm_query4.sparql",
					"lubm_query5.sparql", "lubm_query6.sparql",
					"lubm_query7.sparql", "lubm_query8.sparql",
					"lubm_query9.sparql", "lubm_asktest.sparql",
					"lubm_constructtest.sparql", "sp2b_q1.sparql",
					"sp2b_q2.sparql", "sp2b_q3a.sparql", "sp2b_q3b.sparql",
					"sp2b_q3c.sparql", "sp2b_q4.sparql", "sp2b_q5a.sparql",
					"sp2b_q5b.sparql", "sp2b_q6.sparql", "sp2b_q7.sparql",
					"sp2b_q8.sparql", "sp2b_q9.sparql", "sp2b_q10.sparql",
					"sp2b_q11.sparql", "sp2b_q12a.sparql", "sp2b_q12b.sparql",
					"sp2b_q12c.sparql", "sp2b_E1.sparql", "sp2b_E2.sparql",
					"sp2b_E3.sparql", "sp2b_E4.sparql", "lubm_test.sparql",
					"yago_q1.sparql", "yago_q2.sparql" };
		case TUTORIAL:
			return new String[] { "lubm_test.sparql", "lubm_asktest.sparql",
					"lubm_constructtest.sparql" };
		case TUTORIAL2:
			return new String[] { "sp2b_q1.sparql", "sp2b_q2.sparql",
					"sp2b_q3a.sparql", "sp2b_q3b.sparql", "sp2b_q3c.sparql",
					"sp2b_q4.sparql", "sp2b_q5a.sparql", "sp2b_q5b.sparql",
					"sp2b_q6.sparql", "sp2b_q7.sparql", "sp2b_q8.sparql",
					"sp2b_q9.sparql", "sp2b_q10.sparql", "sp2b_q11.sparql",
					"sp2b_q12a.sparql", "sp2b_q12b.sparql", "sp2b_q12c.sparql" };
		default:
		case ECLIPSE:
			return this.getFiles(this.PATH_QUERIES, ".sparql");
		}
	}

	private String[] getRuleFiles() {
		switch (this.webdemo) {
		default:
			return new String[] { "rule_And.rif", "rule_assignment.rif",
					"rule_comparsion.rif", "rule_comparsion.rif",
					"rule_equality.rif", "rule_exists.rif",
					"rule_fibonacci.rif", "rule_functional.rif", "rule_Or.rif",
					"rule_owl2.rif", "rule_owl_simpletriplerules.rif",
					"rule_parent_discount.rif", "rule_predicates.rif",
					"rule_rdfs.rif" };
		case TUTORIAL2:
			return new String[] { "facts.rif" };
		case ECLIPSE:
			return this.getFiles(this.PATH_RULES, ".rif");
		}
	}

	/**
	 * Get Data Files.
	 * 
	 * @return string array of available data files
	 */
	private String[] getDataFiles() {
		switch (this.webdemo) {
		case LOCALONEJAR:
		case PROJECT_DEMO:
			// started with Java Web Start?
			// Java Web start has a more restrictive rights
			// management, i.e. sp2b does not work with Java
			// Web Start...
			if (!this.isApplet && this.webdemo != DEMO_ENUM.ECLIPSE
					&& this.webdemo != DEMO_ENUM.LOCALONEJAR) {
				return new String[] { "lubm_demo.n3" };
			}

			// if we are in a jar for the project demo...
			return new String[] { "lubm_demo.n3", "sp2b_demo.n3", "yagodata.n3" };
		case TUTORIAL:
			return new String[] { "lubm_demo.n3" };
		case TUTORIAL2:
			return new String[] { "sp2b.n3" };
		default:
		case ECLIPSE:
			return this.getFiles(this.PATH_DATA, ".n3");
		}
	}

	/**
	 * Opens a file.
	 * 
	 * @param filename
	 *            filename of file to open
	 * 
	 * @return file as string
	 */
	private String readFile(final String filename) {
		return FileHelper.readFile(filename,
				(this.webdemo != DEMO_ENUM.ECLIPSE));
	}

	public QueryEvaluator<Node> setupEvaluator(final EvaluationMode mode)
			throws Throwable {
		ServiceApproaches serviceApproach = xpref.datatypes.EnumDatatype.getFirstValue("serviceCallApproach");
		FederatedQueryBitVectorJoin.APPROACH bitVectorApproach = xpref.datatypes.EnumDatatype.getFirstValue("serviceCallBitVectorApproach");
		bitVectorApproach.setup();
		serviceApproach.setup();
		FederatedQueryBitVectorJoin.substringSize = xpref.datatypes.IntegerDatatype.getFirstValue("serviceCallBitVectorSize");
		FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize = FederatedQueryBitVectorJoin.substringSize;
		LiteralFactory.semanticInterpretationOfLiterals = xpref.datatypes.BooleanDatatype.getFirstValue("semanticInterpretationOfDatatypes");
		final QueryEvaluator<Node> evaluator = (QueryEvaluator<Node>) this.getEvaluatorClass(this.cobo_evaluator.getSelectedIndex()).newInstance();
		evaluator.setupArguments();
		evaluator.getArgs().set("debug", DEBUG.ALL);
		evaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
		evaluator.getArgs().set("codemap", LiteralFactory.MapType.TRIEMAP);
		evaluator.getArgs().set("distinct", CommonCoreQueryEvaluator.DISTINCT.HASHSET);
		evaluator.getArgs().set("optional", CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);

		final String engine = (String) this.cobo_evaluator.getSelectedItem();

		if (engine.compareTo("Jena") == 0) {
				evaluator.getArgs().set("RDFS", JenaQueryEvaluator.ONTOLOGY.NONE);
		} else if (engine.compareTo("Sesame") == 0) {
				evaluator.getArgs().set("RDFS", SesameQueryEvaluator.ONTOLOGY.NONE);
		} else {
			evaluator.getArgs().set("RDFS", CommonCoreQueryEvaluator.RDFS.NONE);
		}
		// started with Java Web Start?
		// Java Web start has a more restrictive rights
		// management, i.e. JenaN3 does not work with Java Web
		// Start...
		if (engine.compareTo("Jena") == 0
				|| engine.compareTo("Sesame") == 0
				|| (!this.isApplet && this.webdemo != DEMO_ENUM.ECLIPSE && this.webdemo != DEMO_ENUM.LOCALONEJAR)) {
			evaluator.getArgs().set("type", "N3");
		} else {
			evaluator.getArgs().set("type", "Turtle");
			// evaluator.getArgs()
			// .set("codemap", LiteralFactory.MapType.NOCODEMAP);
			evaluator.getArgs().set("core", true);
		}

		if (evaluator instanceof RDF3XQueryEvaluator) {
			evaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.BPTREE);
		} else {
			evaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.HASHMAP);
		}

		try {
			evaluator.init();
		} catch (final Throwable t) {
			// can be only rdf data error!
			dealWithThrowable(t, mode, true);
			throw t;
		}

		return evaluator;
	}

	private static enum EvaluationMode {
		RESULT, TIMES, DEMO
	};

	private abstract class Evaluation {
		
		public Evaluation(){
			Demo_Applet.this.errorsInOntology = null;
		}
		
		public abstract String getQuery();

		public abstract long compileQuery(String query) throws Exception;

		public abstract DebugViewerCreator compileQueryDebugByteArray(String query) throws Exception;

		public abstract JButton getButtonEvaluate();

		public abstract JButton getButtonEvalDemo();

		public abstract JButton getButtonMeasureExecutionTimes();

		public abstract QueryEvaluator<Node> getEvaluator();
		
		public void enableButtons(){
			enableDemoButtonDependingOnEvaluator();
			enableEvaluateButtonDependingOnEvaluator();
		}
		
		public void enableDemoButtonDependingOnEvaluator(){
			this.getButtonEvalDemo().setEnabled(evaluatorSuitableForDemo((String)cobo_evaluator.getSelectedItem()));
		}
		
		public abstract boolean evaluatorSuitableForDemo(String s);

		public void enableEvaluateButtonDependingOnEvaluator(){
			boolean enable = evaluatorSuitableForEvaluation((String)cobo_evaluator.getSelectedItem());
			this.getButtonEvaluate().setEnabled(enable);
			this.getButtonMeasureExecutionTimes().setEnabled(enable);
		}
		
		public abstract boolean evaluatorSuitableForEvaluation(String s);
		
		public abstract long prepareInputData(Collection<URILiteral> defaultGraphs, LinkedList<URILiteral> namedGraphs) throws Exception;
	}
	
	public class SPARQLEvaluation extends Evaluation {
		private final QueryEvaluator<Node> evaluator;
		private BasicOperator rootInference;
		private Result resultInference;

		public SPARQLEvaluation(final QueryEvaluator<Node> evaluator) {
			super();
			this.evaluator = evaluator;
		}

		public String getQuery() {
			return tp_queryInput.getText();
		}

		public long compileQuery(final String query) throws Exception {
			final long a = (new Date()).getTime();
			evaluator.compileQuery(query);
			integrateInferenceOperatorgraph();
			return (new Date()).getTime() - a;
		}

		public DebugViewerCreator compileQueryDebugByteArray(final String query)
				throws Exception {
			DebugViewerCreator result = new SPARQLDebugViewerCreator(evaluator.compileQueryDebugByteArray(query, prefixInstance));
			integrateInferenceOperatorgraph();
			return result;
		}

		public JButton getButtonEvaluate() {
			return bt_evaluate;
		}

		public JButton getButtonEvalDemo() {
			return bt_evalDemo;
		}

		public JButton getButtonMeasureExecutionTimes() {
			return bt_MeasureExecutionTimes;
		}


		public QueryEvaluator<Node> getEvaluator() {
			return evaluator;
		}

		@Override
		public boolean evaluatorSuitableForDemo(String s) {
			return s.compareTo("Jena")!=0 && s.compareTo("Sesame")!=0;
		}

		@Override
		public boolean evaluatorSuitableForEvaluation(String s) {
			return true;
		}
		
		private void setupInference(){
			final Object chosen=comboBox_sparqlInference.getSelectedItem();
			if(chosen != SPARQLINFERENCE.NONE && (evaluator instanceof JenaQueryEvaluator || evaluator instanceof SesameQueryEvaluator)){
				if(evaluator instanceof JenaQueryEvaluator && (chosen == SPARQLINFERENCE.OWL2RL) 
				   || chosen == SPARQLINFERENCE.RDFS) {
					JOptionPane.showMessageDialog(Demo_Applet.this,
							"Jena and Sesame evaluators do not support different rulesets and materialization strategies!\nUsing their standard inference for "+chosen+"...",
							"Ontology support of Jena and Sesame evaluators",
							JOptionPane.INFORMATION_MESSAGE);
					if(evaluator instanceof JenaQueryEvaluator){
						if(chosen == SPARQLINFERENCE.OWL2RL){
							((JenaQueryEvaluator)evaluator).setOntology(JenaQueryEvaluator.ONTOLOGY.OWL);
						} else ((JenaQueryEvaluator)evaluator).setOntology(JenaQueryEvaluator.ONTOLOGY.RDFS);						
						return;
					} else if(evaluator instanceof SesameQueryEvaluator){
						((SesameQueryEvaluator)evaluator).setOntology(SesameQueryEvaluator.ONTOLOGY.RDFS);
						return;
					}
				}
				JOptionPane.showMessageDialog(Demo_Applet.this,
						"The "+((evaluator instanceof JenaQueryEvaluator)?"Jena":"Sesame")+" evaluator does not support this type of inference ("+chosen+")...\nEvaluate query without considering inference!",
						"Inference support of Jena and Sesame evaluators",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}

		private void inference() throws Exception {
			if(evaluator instanceof JenaQueryEvaluator || evaluator instanceof SesameQueryEvaluator){
				return;
			}
			ruleApplicationsForMaterialization = null;
			materializationInfo = null;
			inferenceRules = ((SPARQLINFERENCE)comboBox_sparqlInference.getSelectedItem()).getRuleSet(rulesets);
			if(inferenceRules != null){
				BasicIndexRuleEvaluator birqe = new BasicIndexRuleEvaluator((CommonCoreQueryEvaluator<Node>)evaluator);
				birqe.compileQuery(inferenceRules);
				materializationInfo = new RIFDebugViewerCreator(birqe.getCompilationUnit(), birqe.getDocument());				

				// TODO improve RIF logical optimization such that it is fast enough for large operatorgraphs!
				// as workaround here only use the logical optimization of the underlying evaluator!
				System.out.println("Logical optimization...");
				if (ruleApplicationsForMaterialization != null) {
					ruleApplicationsForMaterialization.addAll(evaluator.logicalOptimizationDebugByteArray(prefixInstance));
				} else {
					ruleApplicationsForMaterialization = evaluator.logicalOptimizationDebugByteArray(prefixInstance);
				}

				System.out.println("Physical optimization...");

				if (ruleApplicationsForMaterialization != null) {
					ruleApplicationsForMaterialization.addAll(birqe.physicalOptimizationDebugByteArray(prefixInstance));
				} else {
					ruleApplicationsForMaterialization = birqe.physicalOptimizationDebugByteArray(prefixInstance);
				}
								
				if(comboBox_sparqlInferenceMaterialization.getSelectedItem() == SPARQLINFERENCEMATERIALIZATION.MATERIALIZEALL){
					Demo_Applet.this.errorsInOntology = birqe.inferTriplesAndStoreInDataset();					
				} else {
					this.rootInference = birqe.getRootNode();
					this.resultInference = birqe.getResultOperator();
				}
			}
		}
		
		private void integrateInferenceOperatorgraph() throws Exception{
			if(evaluator instanceof JenaQueryEvaluator || evaluator instanceof SesameQueryEvaluator){
				return;
			}
			if(comboBox_sparqlInference.getSelectedItem() != SPARQLINFERENCE.NONE &&
					comboBox_sparqlInferenceMaterialization.getSelectedItem() == SPARQLINFERENCEMATERIALIZATION.COMBINEDQUERYOPTIMIZATION){
				CommonCoreQueryEvaluator<Node> commonCoreQueryEvaluator = (CommonCoreQueryEvaluator<Node>)evaluator;
				BasicIndexRuleEvaluator.integrateInferenceOperatorgraphIntoQueryOperatorgraph(this.rootInference, this.resultInference, commonCoreQueryEvaluator.getRootNode(), commonCoreQueryEvaluator.getResultOperator());
				commonCoreQueryEvaluator.setBindingsVariablesBasedOnOperatorgraph();
			}			
		}

		@Override
		public long prepareInputData(Collection<URILiteral> defaultGraphs, LinkedList<URILiteral> namedGraphs) throws Exception {
			final long a = (new Date()).getTime();
			this.setupInference();
			evaluator.prepareInputData(defaultGraphs, namedGraphs);
			this.inference();
			return ((new Date()).getTime() - a);
		}
	}

	public class RIFEvaluation extends Evaluation {
		private BasicIndexRuleEvaluator ruleEvaluator;

		public RIFEvaluation(final QueryEvaluator<Node> evaluator) {
			super();
			try {
				ruleEvaluator = new BasicIndexRuleEvaluator((CommonCoreQueryEvaluator<Node>)evaluator);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		public String getQuery() {
			return tp_rifInput.getText();
		}

		public long compileQuery(final String rule) throws Exception {
			final long a = (new Date()).getTime();
			ruleEvaluator.compileQuery(rule);
			return (new Date().getTime()) - a;
		}

		public DebugViewerCreator compileQueryDebugByteArray(final String rule)
				throws Exception {
			ruleEvaluator.compileQuery(rule);
			return new RIFDebugViewerCreator(
					ruleEvaluator.getCompilationUnit(),
					ruleEvaluator.getDocument());
		}

		public JButton getButtonEvaluate() {
			return bt_rifEvaluate;
		}

		public JButton getButtonEvalDemo() {
			return bt_rifEvalDemo;
		}

		public JButton getButtonMeasureExecutionTimes() {
			return bt_rifMeasureExecutionTimes;
		}

		public QueryEvaluator<Node> getEvaluator() {
			return ruleEvaluator;
		}

		@Override
		public boolean evaluatorSuitableForDemo(String s) {
			return s.compareTo("Jena")!=0 && s.compareTo("Sesame")!=0;
		}

		@Override
		public boolean evaluatorSuitableForEvaluation(String s) {
			return evaluatorSuitableForDemo(s);
		}

		@Override
		public long prepareInputData(Collection<URILiteral> defaultGraphs, LinkedList<URILiteral> namedGraphs) throws Exception {
			return this.ruleEvaluator.prepareInputData(defaultGraphs, namedGraphs);
		}
	}

	public class SPARQLDebugViewerCreator extends DebugViewerCreator {

		private final DebugContainerQuery<BasicOperatorByteArray, Node> debugContainerQuery;

		public SPARQLDebugViewerCreator(
				final DebugContainerQuery<BasicOperatorByteArray, Node> debugContainerQuery) {
			this.debugContainerQuery = debugContainerQuery;
		}

		@Override
		public GraphWrapper getASTGraphWrapper() {
			return (debugContainerQuery == null) ? null : new GraphWrapperAST(
					debugContainerQuery.getAst());
		}

		@Override
		public String queryOrRule() {
			return "SPARQL query";
		}

		@Override
		public GraphWrapper getASTCoreGraphWrapper() {
			return (debugContainerQuery == null) ? null : new GraphWrapperAST(
					debugContainerQuery.getAstCoreSPARQLQuery());
		}

		@Override
		public String getCore() {
			return (debugContainerQuery == null) ? null : debugContainerQuery
					.getCoreSPARQLQuery();
		}

		@Override
		public List<DebugContainer<BasicOperatorByteArray>> getCorrectOperatorGraphRules() {
			return (debugContainerQuery == null) ? null : debugContainerQuery
					.getCorrectOperatorGraphRules();
		}
	}

	public class RIFDebugViewerCreator extends DebugViewerCreator {

		final private CompilationUnit compilationUnit;
		final private Document rifDoc;

		public RIFDebugViewerCreator(final CompilationUnit compilationUnit,
				final Document rifDoc) {
			this.compilationUnit = compilationUnit;
			this.rifDoc = rifDoc;
		}

		@Override
		public GraphWrapper getASTGraphWrapper() {
			return new GraphWrapperASTRIF(compilationUnit);
		}

		@Override
		public GraphWrapper getASTCoreGraphWrapper() {
			return new GraphWrapperRules(rifDoc);
		}

		@Override
		public String getCore() {
			return null;
		}

		@Override
		public String queryOrRule() {
			return "RIF rule";
		}

	}

	public abstract class DebugViewerCreator {

		public abstract GraphWrapper getASTGraphWrapper();

		public abstract GraphWrapper getASTCoreGraphWrapper();

		public abstract String getCore();

		public abstract String queryOrRule();

		/**
		 * create the button to show the operator graph.
		 */
		public JButton createASTButton() {
			// create operatorgraph-button, add actionListener and add it to
			// Applet...
			final GraphWrapper graphWrapper = getASTGraphWrapper();
			final JButton bt_AST = new JButton("Show AST of " + queryOrRule());
			bt_AST.setMargin(new Insets(0, 0, 0, 0));
			bt_AST.setEnabled(graphWrapper != null);

			if (graphWrapper != null) {
				bt_AST.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent arg0) {
						new Viewer(graphWrapper, "Abstract syntax tree of the "
								+ queryOrRule(), false,
								webdemo != DEMO_ENUM.ECLIPSE);
					}
				});
			}

			return bt_AST;
		}

		public JButton createCoreSPARQLQueryButton() {
			// create coreSPARQLQuery-button, add actionListener and add it to
			// Applet...
			final String core = getCore();
			if(core==null){
				return null;
			}
			final JButton bt_coreSPARQLQuery = new JButton("Show Core "
					+ queryOrRule());
			bt_coreSPARQLQuery.setEnabled(core != null);
			bt_coreSPARQLQuery.setMargin(new Insets(0, 0, 0, 0));

			bt_coreSPARQLQuery.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent arg0) {
					final JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout());

					final LuposDocument document = new LuposDocument();
					final JTextPane tp_coreSPARQLQuery = new LuposJTextPane(document);
					document.init(SPARQLParser.createILuposParser(new LuposDocumentReader(document)), false);

					tp_coreSPARQLQuery.setFont(new Font("Courier New",
							Font.PLAIN, 12));
					tp_coreSPARQLQuery.setText(core);
					tp_coreSPARQLQuery.setEditable(false);

					final JScrollPane scroll = new JScrollPane(
							tp_coreSPARQLQuery);

					panel.add(scroll);

					final JFrame frame = new JFrame("Core " + queryOrRule());

					if (webdemo != DEMO_ENUM.ECLIPSE) {
						frame.setIconImage(new ImageIcon(
								Demo_Applet.class
								.getResource("/demo.gif"))
						.getImage());
					} else {
						frame.setIconImage(new ImageIcon("data"
								+ File.separator + "demo.gif").getImage());
					}

					frame.setSize(794, 200);
					frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					frame.getContentPane().add(panel);
					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				}
			});

			return bt_coreSPARQLQuery;
		}
		
		public JButton createInferenceRulesButton(final String inferenceRules) {
			// create coreSPARQLQuery-button, add actionListener and add it to Applet...
			final JButton bt_InferenceRules = new JButton("Show Rules");
			bt_InferenceRules.setEnabled(inferenceRules != null);
			bt_InferenceRules.setMargin(new Insets(0, 0, 0, 0));

			if (inferenceRules != null) {
				bt_InferenceRules.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent arg0) {
						final JPanel panel = new JPanel();
						panel.setLayout(new BorderLayout());

						final LuposDocument document = new LuposDocument();
						final JTextPane tp_coreSPARQLQuery = new LuposJTextPane(document);
						document.init(RIFParser.createILuposParser(new LuposDocumentReader(document)), false);

						tp_coreSPARQLQuery.setFont(new Font("Courier New", Font.PLAIN, 12));
						tp_coreSPARQLQuery.setText(inferenceRules);
						tp_coreSPARQLQuery.setEditable(false);

						final JScrollPane scroll = new JScrollPane(tp_coreSPARQLQuery);

						panel.add(scroll);

						final JFrame frame = new JFrame("Inference Rules");

						if (webdemo != DEMO_ENUM.ECLIPSE) {
							frame.setIconImage(new ImageIcon(
									Demo_Applet.class
											.getResource("/demo.gif"))
									.getImage());
						} else {
							frame.setIconImage(new ImageIcon("data"
									+ File.separator + "demo.gif").getImage());
						}

						frame.setSize(794, 200);
						frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
						frame.getContentPane().add(panel);
						frame.pack();
						frame.setLocationRelativeTo(null);
						frame.setVisible(true);
					}
				});
			}

			return bt_InferenceRules;
		}

		/**
		 * create the button to show the operator graph.
		 */
		public JButton createASTCoreSPARQLButton() {
			// create operatorgraph-button, add actionListener and add it to
			// Applet...
			final GraphWrapper graphWrapper = getASTCoreGraphWrapper();
			final JButton bt_coreAST = new JButton("Show AST Core "
					+ queryOrRule());
			bt_coreAST.setMargin(new Insets(0, 0, 0, 0));
			bt_coreAST.setEnabled(graphWrapper != null);

			if (graphWrapper != null) {
				bt_coreAST.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						new Viewer(graphWrapper,
								"Abstract syntax tree of the Core "
										+ queryOrRule(), false,
								webdemo != DEMO_ENUM.ECLIPSE);
					}
				});
			}

			return bt_coreAST;
		}

		/**
		 * create the button to show the operator graph.
		 */
		public JButton createOperatorGraphButton() {
			return this.createOperatorGraphButton(ruleApplications);
		}

		public JButton createOperatorGraphButton(final List<DebugContainer<BasicOperatorByteArray>> ruleApplications) {
			// create OperatorGraph-button, add actionListener and add it to
			// Applet...
			final JButton bt_opgraph = new JButton("Show Operator Graph");
			bt_opgraph.setMargin(new Insets(0, 0, 0, 0));
			bt_opgraph.setEnabled(ruleApplications != null);
			if (ruleApplications != null) {
				bt_opgraph.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent ae) {
						if (prefixInstance == null)
							prefixInstance = new ViewerPrefix(usePrefixes.isTrue(), null);
						new Viewer(ruleApplications, "OperatorGraph", false,
								webdemo != DEMO_ENUM.ECLIPSE, prefixInstance);
					}
				});
			}

			return bt_opgraph;
		}

		public List<DebugContainer<BasicOperatorByteArray>> getCorrectOperatorGraphRules() {
			return null;
		}
	}

	/**
	 * Evaluates the given query on the given data.
	 */
	private void evaluateSPARQLQuery(final EvaluationMode mode) {
		try {
			evaluate(new SPARQLEvaluation(setupEvaluator(mode)), mode);
		} catch (final Throwable t) {
			// ignore forwarded Throwable!
			this.enableOrDisableButtons(true);
		}
	}

	private void evaluateRIFRule(final EvaluationMode mode) {
		try {
			evaluate(new RIFEvaluation(setupEvaluator(mode)), mode);
		} catch (final Throwable t) {
			// ignore forwarded Throwable!
			this.enableOrDisableButtons(false);
		}
	}

	private void evaluate(final Evaluation evaluation,
			final EvaluationMode mode) {
		evaluation.getButtonEvaluate().setEnabled(false);
		evaluation.getButtonEvalDemo().setEnabled(false);
		evaluation.getButtonMeasureExecutionTimes().setEnabled(false);

//		StaticDataHolder.resetOperatorGraphRules();
//		this.resultOrder = new LinkedList<String>();
		this.query = evaluation.getQuery(); // get query
//		Indices.materializationDemo = (mode == EvaluationMode.DEMO);

		if (query.compareTo("") == 0) { // no query given...
			displayErrorMessage("Error: empty query", true);
			evaluation.enableButtons();
		} else { // evaluate query...
			try {
				try {
					final QueryEvaluator<Node> evaluator = evaluation.getEvaluator();
					final URILiteral rdfURL = LiteralFactory
							.createStringURILiteral("<inlinedata:" + data + ">");
					defaultGraphs = new LinkedList<URILiteral>();
					defaultGraphs.add(rdfURL);

					if (mode == EvaluationMode.DEMO
							|| mode == EvaluationMode.TIMES) {

						final long prepareInputData = evaluation
								.prepareInputData(defaultGraphs,
										new LinkedList<URILiteral>());

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								final Thread thread = new Thread() {
									@Override
									public void run() {
										try {
//											Indices.waitForMaterializationDemoThread();
											System.out.println("Compile query...");
											try {
												final long compileQuery = evaluation.compileQuery(query);

												System.out.println("Logical optimization...");
												final long logicalOptimization = evaluator.logicalOptimization();

												System.out.println("Physical optimization...");
												final long physicalOptimization = evaluator.physicalOptimization();

												if (mode == EvaluationMode.DEMO) {
													final EvaluationDemoToolBar bottomToolBar = new EvaluationDemoToolBar(
															webdemo != DEMO_ENUM.ECLIPSE);
													
													Result result = (evaluator instanceof BasicIndexRuleEvaluator)?
														((BasicIndexRuleEvaluator)evaluator).getResultOperator()
														:((CommonCoreQueryEvaluator<Node>)evaluator).getResultOperator();
													
													final ShowResult sr = new ShowResult(bottomToolBar, result);
													
													evaluator.prepareForQueryDebugSteps(bottomToolBar);

													System.out.println("Evaluate query ...");
													final Thread thread = new Thread() {
														@Override
														public void run() {
															try {
																evaluator.evaluateQueryDebugSteps(bottomToolBar, sr);
																bottomToolBar.endOfEvaluation();
																enableOrDisableButtons(evaluation instanceof SPARQLEvaluation);
															} catch (final Exception e) {
																System.err
																		.println(e);
																e.printStackTrace();
															}
														}
													};
													bottomToolBar.setEvaluationThread(thread);
													thread.start();
													if (prefixInstance == null)
														prefixInstance = new ViewerPrefix(
																usePrefixes.isTrue(),
																null);
													BasicOperator root = (evaluator instanceof BasicIndexRuleEvaluator)? ((BasicIndexRuleEvaluator)evaluator).getRootNode() :((CommonCoreQueryEvaluator<Node>) evaluator).getRootNode();
														
													operatorGraphViewer = new Viewer(
															new GraphWrapperBasicOperator(
																	root),
															prefixInstance,
															"Evaluation Demo",
															false,
															webdemo != DEMO_ENUM.ECLIPSE,
															bottomToolBar);
													bottomToolBar.setOperatorGraphViewer(operatorGraphViewer);
												} else {

													final JTextArea ta_prefixes = new JTextArea();
													ta_prefixes.setEditable(false);
													ta_prefixes.setFont(new Font("Courier New", Font.PLAIN, 12));

													System.out.println("Evaluate query ...");
													if(evaluator instanceof CommonCoreQueryEvaluator){
														((CommonCoreQueryEvaluator)evaluator).getResultOperator().addApplication(new IterateOneTimeThrough());
													}

													final long evaluateQuery = evaluator.evaluateQuery();
													int times = xpref.datatypes.IntegerDatatype.getFirstValue("repetitionsOfExecution");
													if(times >1){
														long compileQueryTime = 0;
														long logicalOptimizationTime = 0;
														long physicalOptimizationTime = 0;
														long evaluateQueryTime = 0;
														long totalTime = 0;
														final long[] compileQueryTimeArray = new long[times];
														final long[] logicalOptimizationTimeArray = new long[times];
														final long[] physicalOptimizationTimeArray = new long[times];
														final long[] evaluateQueryTimeArray = new long[times];
														final long[] totalTimeArray = new long[times];
														for (int i = 0; i < times; i++) {
															compileQueryTimeArray[i] = evaluator.compileQuery(query);
															compileQueryTime += compileQueryTimeArray[i];
															logicalOptimizationTimeArray[i] = evaluator.logicalOptimization();
															logicalOptimizationTime += logicalOptimizationTimeArray[i];
															physicalOptimizationTimeArray[i] = evaluator.physicalOptimization();
															physicalOptimizationTime += physicalOptimizationTimeArray[i];
															if(evaluator instanceof CommonCoreQueryEvaluator){
																((CommonCoreQueryEvaluator)evaluator).getResultOperator().addApplication(new IterateOneTimeThrough());
															}
															evaluateQueryTimeArray[i] = evaluator.evaluateQuery();
															evaluateQueryTime += evaluateQueryTimeArray[i];
															totalTimeArray[i] = compileQueryTimeArray[i] + logicalOptimizationTimeArray[i] + physicalOptimizationTimeArray[i] + evaluateQueryTimeArray[i];
															totalTime += totalTimeArray[i];
														}
														String result = "Evaluator " + cobo_evaluator.getSelectedItem().toString() + "\n\nBuild indices              : " + ((double) prepareInputData / 1000);
														result += "\n\n(I) Time in seconds to compile query:\nAvg" + QueryEvaluator.toString(compileQueryTimeArray) + "/1000 = " + (((double) compileQueryTime) / times) / 1000;
														result += "\nStandard deviation of the sample: " + QueryEvaluator.computeStandardDeviationOfTheSample(compileQueryTimeArray) / 1000;
														result += "\nSample standard deviation       : " + QueryEvaluator.computeSampleStandardDeviation(compileQueryTimeArray) / 1000;
														result += "\n\n(II) Time in seconds used for logical optimization:\nAvg" + QueryEvaluator.toString(logicalOptimizationTimeArray) + "/1000 = " + (((double) logicalOptimizationTime) / times) / 1000;
														result += "\nStandard deviation of the sample: " + QueryEvaluator.computeStandardDeviationOfTheSample(logicalOptimizationTimeArray) / 1000;
														result += "\nSample standard deviation       : " + QueryEvaluator.computeSampleStandardDeviation(logicalOptimizationTimeArray) / 1000;
														result += "\n\n(III) Time in seconds used for physical optimization:\nAvg" + QueryEvaluator.toString(physicalOptimizationTimeArray) + "/1000 = " + (((double) physicalOptimizationTime) / times) / 1000;
														result += "\nStandard deviation of the sample: " + QueryEvaluator.computeStandardDeviationOfTheSample(physicalOptimizationTimeArray) / 1000;
														result += "\nSample standard deviation       : " + QueryEvaluator.computeSampleStandardDeviation(physicalOptimizationTimeArray) / 1000;
														result += "\n\n(IV) Time in seconds to evaluate query:\nAvg" + QueryEvaluator.toString(evaluateQueryTimeArray) + "/1000 = " + (((double) evaluateQueryTime) / times) / 1000;
														result += "\nStandard deviation of the sample: " + QueryEvaluator.computeStandardDeviationOfTheSample(evaluateQueryTimeArray) / 1000;
														result += "\nSample standard deviation       : " + QueryEvaluator.computeSampleStandardDeviation(evaluateQueryTimeArray) / 1000;
														result += "\n\nTotal time in seconds (I)+(II)+(III)+(IV):\nAvg" + QueryEvaluator.toString(totalTimeArray) + "/1000 = " + (((double) totalTime) / times) / 1000;
														result += "\nStandard deviation of the sample: " + QueryEvaluator.computeStandardDeviationOfTheSample(totalTimeArray) / 1000;
														result += "\nSample standard deviation       : " + QueryEvaluator.computeSampleStandardDeviation(totalTimeArray) / 1000;
														ta_prefixes.setText(result);
													} else {
														ta_prefixes.setText("Evaluator "
																+ cobo_evaluator.getSelectedItem().toString()
																+ "\n\nBuild indices              : " + ((double) prepareInputData / 1000)
																+ "\n\nTotal time query processing: " + ((double) (compileQuery + logicalOptimization + physicalOptimization + evaluateQuery) / 1000)
																+ ((evaluator instanceof JenaQueryEvaluator || evaluator instanceof SesameQueryEvaluator) ? ""
																		: "\n    - Compile query        : "
																			+ ((double) compileQuery / 1000)
																			+ "\n    - Logical optimization : "
																			+ ((double) logicalOptimization / 1000)
																			+ "\n    - Physical optimization: "
																			+ ((double) physicalOptimization / 1000)
																			+ "\n    - Evaluation           : "
																			+ ((double) evaluateQuery / 1000)));
													}
													final JFrame frame = new JFrame("Execution times in seconds");
													frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
													frame.setLocationRelativeTo(Demo_Applet.this);

													final JScrollPane scroll = new JScrollPane(ta_prefixes);

													frame.add(scroll);
													frame.pack();
													frame.setVisible(true);
												}
											} catch (final Throwable t) {
												dealWithThrowable(t, mode, evaluation instanceof SPARQLEvaluation);
											}
										} catch (final Throwable t) {
											dealWithThrowable(t, mode, evaluation instanceof SPARQLEvaluation);
										}
										evaluation.enableButtons();
									}
								};
								thread.start();
							}
						});
					} else {
						if (prefixInstance == null)
							prefixInstance = new ViewerPrefix(usePrefixes.isTrue());

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								final Thread thread = new Thread() {
									@Override
									public void run() {
										try {		
											evaluation.prepareInputData(defaultGraphs, new LinkedList<URILiteral>());
											
											System.out.println("Compile query...");
											try {
												try {
													debugViewerCreator = evaluation.compileQueryDebugByteArray(query);

													if (debugViewerCreator != null) {
														ruleApplications = debugViewerCreator.getCorrectOperatorGraphRules();
													} else {
														ruleApplications = null;
													}
													
													System.out.println("Logical optimization...");

													if (ruleApplications != null) {
														ruleApplications.addAll(evaluator.logicalOptimizationDebugByteArray(prefixInstance));
													} else {
														ruleApplications = evaluator.logicalOptimizationDebugByteArray(prefixInstance);
													}
													
													System.out.println("Physical optimization...");

													if (ruleApplications != null) {
														ruleApplications.addAll(evaluator.physicalOptimizationDebugByteArray(prefixInstance));
													} else {
														ruleApplications = evaluator.physicalOptimizationDebugByteArray(prefixInstance);
													}
													
													// new Viewer(ruleApplications, "OperatorGraph", false,webdemo != DEMO_ENUM.ECLIPSE, prefixInstance);

													System.out.println("Evaluate query ...");

													if (evaluator instanceof CommonCoreQueryEvaluator || evaluator instanceof BasicIndexRuleEvaluator) {
														final CollectRIFResult crr = new CollectRIFResult(false);
														Result resultOperator = (evaluator instanceof CommonCoreQueryEvaluator)?((CommonCoreQueryEvaluator<Node>)evaluator).getResultOperator(): ((BasicIndexRuleEvaluator)evaluator).getResultOperator();
														resultOperator.addApplication(crr);
														evaluator.evaluateQuery();
														resultQueryEvaluator = crr.getQueryResults();
													} else {
														resultQueryEvaluator = new QueryResult[1];
														resultQueryEvaluator[0] = evaluator.getResult();
													}

													System.out.println("\nQuery Result:");
													for (final QueryResult qr : resultQueryEvaluator)
														System.out.println(qr);
													System.out.println("----------------Done.");

													if (isApplet) {
														setSize(800, 905);
														repaint();
													}

													resultpanel = new JPanel(new BorderLayout());

													outputResult();

													// set resultpanel as component for the result tab and make the tab active
													tabbedPane_globalMainpane.setComponentAt(
															tabbedPane_globalMainpane.indexOfTab(TAB_TITLE_RESULT),
															resultpanel);
													tabbedPane_globalMainpane.setSelectedComponent(resultpanel);
												} catch (final Throwable t) {
													dealWithThrowable(t, mode, evaluation instanceof SPARQLEvaluation);
												}
											} catch (final Throwable t) {
												dealWithThrowable(t, mode, evaluation instanceof SPARQLEvaluation);
											}
										} catch (final Throwable t) {
											dealWithThrowable(t, mode, evaluation instanceof SPARQLEvaluation);
										}

										evaluation.enableButtons();
									}
								};
								thread.start();
							}
						});
					}
				} catch (final Throwable t) {
					// ignore forwarded Throwable!

					evaluation.enableButtons();
				}
			} catch (final Throwable t) {
				dealWithThrowable(t, mode, evaluation instanceof SPARQLEvaluation);

				evaluation.enableButtons();
			}
			// append an empty string to the error textarea to force
			// the
			// system
			// to
			// flush the System.err
			displayErrorMessage("", true);
		}
	}

	private boolean dealWithThrowableFromQueryParser(final Throwable e,
			final EvaluationMode mode, boolean queryOrRif) {
		if (e instanceof TokenMgrError) {
			final TokenMgrError tme = (TokenMgrError) e;
			displayErrorMessage(tme.getMessage(), false, queryOrRif);

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern
					.compile("line (\\d+), column (\\d+)");
			final Matcher matcher = pattern.matcher(tme.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				final int line = Integer.parseInt(matcher.group(1));
				final int column = Integer.parseInt(matcher.group(2));

				setErrorPosition(line, column, queryOrRif);
			}
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
			return true;
		} else	if (e instanceof lupos.rif.generated.parser.TokenMgrError) {
			final lupos.rif.generated.parser.TokenMgrError tme = (lupos.rif.generated.parser.TokenMgrError) e;
			displayErrorMessage(tme.getMessage(), false, queryOrRif);

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern
					.compile("line (\\d+), column (\\d+)");
			final Matcher matcher = pattern.matcher(tme.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				final int line = Integer.parseInt(matcher.group(1));
				final int column = Integer.parseInt(matcher.group(2));

				setErrorPosition(line, column, queryOrRif);
			}
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
			return true;			
		} else if (e instanceof ParseException) {
			final ParseException pe = (ParseException) e;
			displayErrorMessage(pe.getMessage(), false, queryOrRif);

			int line;
			int column;

			// get precise line and column...
			if (pe.currentToken.next == null) {
				line = pe.currentToken.beginLine;
				column = pe.currentToken.beginColumn;
			} else {
				line = pe.currentToken.next.beginLine;
				column = pe.currentToken.next.beginColumn;
			}

			setErrorPosition(line, column, queryOrRif);
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
			return true;
		} else if(e instanceof lupos.rif.generated.parser.ParseException){
			final lupos.rif.generated.parser.ParseException pe = (lupos.rif.generated.parser.ParseException) e;
			displayErrorMessage(pe.getMessage(), false, queryOrRif);

			int line;
			int column;

			// get precise line and column...
			if (pe.currentToken.next == null) {
				line = pe.currentToken.beginLine;
				column = pe.currentToken.beginColumn;
			} else {
				line = pe.currentToken.next.beginLine;
				column = pe.currentToken.next.beginColumn;
			}

			setErrorPosition(line, column, queryOrRif);
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
			return true;			
		} else if (e instanceof QueryParseException) {
			final QueryParseException qpe = (QueryParseException) e;
			displayErrorMessage(qpe.getMessage(), false, queryOrRif);

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern.compile(
					"line (\\d+), column (\\d+)", Pattern.CASE_INSENSITIVE);
			final Matcher matcher = pattern.matcher(qpe.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				final int line = Integer.parseInt(matcher.group(1));
				final int column = Integer.parseInt(matcher.group(2));

				setErrorPosition(line, column, queryOrRif);
			}
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
			return true;
		} else if (e instanceof MalformedQueryException) {
			final MalformedQueryException mqe = (MalformedQueryException) e;
			displayErrorMessage(mqe.getMessage(), false, queryOrRif);

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern
					.compile("line (\\d+), column (\\d+)");
			final Matcher matcher = pattern.matcher(mqe.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				final int line = Integer.parseInt(matcher.group(1));
				final int column = Integer.parseInt(matcher.group(2));

				setErrorPosition(line, column, queryOrRif);
			}
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
			return true;
		}
		return false;
	}
	
	private void setErrorPosition(final int line, final int column, final boolean queryOrRif){
		if(queryOrRif){
			tp_queryInput.setErrorPosition(line, column);
		} else {
			tp_rifInput.setErrorPosition(line, column);
		}
	}

	private void dealWithThrowable(final Throwable e, final EvaluationMode mode, boolean queryOrRif) {
		if (this.dealWithThrowableFromQueryParser(e, mode, queryOrRif))
			return;
		if (e instanceof TurtleParseException) {
			final TurtleParseException n3e = (TurtleParseException) e;
			displayDataErrorMessage(n3e.getMessage(), false);

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern.compile("\\[(\\d+):(\\d+)\\]");
			final Matcher matcher = pattern.matcher(n3e.getMessage());

			final Pattern pattern2 = Pattern.compile("Line (\\d+): ");
			final Matcher matcher2 = pattern2.matcher(n3e.getMessage());

			int line = -1;
			int column = -1;

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				line = Integer.parseInt(matcher.group(1));
				column = Integer.parseInt(matcher.group(2));
			} else if (matcher2.find() == true) {
				// get matches....
				line = Integer.parseInt(matcher2.group(1));
				column = 1;
			}

			if (line != -1 && column != -1) {
				tp_dataInput.setErrorPosition(line, column);
			}
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
		} else if (e instanceof RDFParseException) {
			final RDFParseException rdfpe = (RDFParseException) e;
			displayDataErrorMessage(rdfpe.getMessage(), false);

			// get precise line and column...
			final int line = rdfpe.getLineNumber();
			int column = rdfpe.getColumnNumber();

			if (column == -1) {
				column = 1;
			}

			tp_dataInput.setErrorPosition(line, column);
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
		} else {
			// do not use System.err.println(...) as Java
			// Web
			// Start
			// forbids to redirect System.err, such that
			// nor error message would be printed!
			displayErrorMessage(e.toString(), false, queryOrRif);
			e.printStackTrace();
			if (mode == EvaluationMode.DEMO) {
				enableOrDisableButtons(queryOrRif);
			}
		}

	}
	
	private void outputResult(){
		try{
			final Container contentPane = (this.isApplet) ? this.getContentPane() : frame.getContentPane();

			setupResultPanel(this.resultpanel, this.resultQueryEvaluator, this.debugViewerCreator, this.materializationInfo, this.inferenceRules, this.ruleApplicationsForMaterialization, this.errorsInOntology, this.usePrefixes, this.prefixInstance, contentPane );
			
		} catch (final Exception ex) {
			// this.ta_errors.setText(ex.toString());
			displayErrorMessage(ex.toString(), false);

			ex.printStackTrace();
		}

		// append an empty string to the error textarea to get the system to
		// flush the System.err
		// this.ta_errors.append("");
		displayErrorMessage("", true);
	}
	
	/**
	 * This is just for external use an easy way to display the result of a RIF rules application... 
	 */
	public static void appyRIFRules(final String ruleset, final JPanel resultpanel, final BooleanReference usePrefixes,  final ViewerPrefix prefixInstance) throws Exception {		
		final BasicIndexRuleEvaluator ruleEvaluator = new BasicIndexRuleEvaluator();
		ruleEvaluator.prepareInputData(new LinkedList<URILiteral>(), new LinkedList<URILiteral>());
		ruleEvaluator.compileQuery(ruleset);
		ruleEvaluator.logicalOptimization();
		ruleEvaluator.physicalOptimization();
		QueryResult[] resultQueryEvaluator = ruleEvaluator.getResults();
		
		Demo_Applet.setupResultPanel(resultpanel, resultQueryEvaluator, null, null, null, null, null, usePrefixes, prefixInstance, resultpanel);
	}

	public static void setupResultPanel(final JPanel resultpanel, final QueryResult[] resultQueryEvaluator, final DebugViewerCreator debugViewerCreator, final DebugViewerCreator materializationInfo, final String inferenceRules,  final List<DebugContainer<BasicOperatorByteArray>> ruleApplicationsForMaterialization, final RuleResult errorsInOntology, final BooleanReference usePrefixes,  final ViewerPrefix prefixInstance, final Container contentPane) throws Exception {
		final Dimension contentPaneSize = contentPane.getSize();
		resultpanel.removeAll();
		final FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 0, 0);
		final JPanel buttonpanel = new JPanel(layout);

		final JButton bt_AST = (debugViewerCreator!=null)? debugViewerCreator.createASTButton(): null;

		final JButton bt_coreQuery = (debugViewerCreator!=null)? debugViewerCreator.createCoreSPARQLQueryButton(): null;

		final JButton bt_coreAST = (debugViewerCreator!=null)? debugViewerCreator.createASTCoreSPARQLButton(): null;

		final List<String> resultOrder = new LinkedList<String>();

		// if AST exists...
		if (debugViewerCreator!=null && debugViewerCreator instanceof SPARQLDebugViewerCreator
				&& ((SPARQLDebugViewerCreator) debugViewerCreator).debugContainerQuery != null
				&& ((SPARQLDebugViewerCreator) debugViewerCreator).debugContainerQuery.getAst() != null) {

			final Node ast = ((SPARQLDebugViewerCreator) debugViewerCreator).debugContainerQuery.getAst(); // get AST

			// walk through first level children of AST...
			for (int i = 0; i < ast.jjtGetNumChildren(); ++i) {
				final Node child = ast.jjtGetChild(i); // get current child

				if (child instanceof ASTSelectQuery) {
					final ASTSelectQuery selectChild = (ASTSelectQuery) child;

					// SELECT is not the wildcard *...
					if (!selectChild.isSelectAll()) {
						// walk through select children...
						for (int j = 0; j < selectChild.jjtGetNumChildren(); ++j) {
							final Node selectChildChild = selectChild
							.jjtGetChild(j);

							// child of select is variable...
							if (selectChildChild instanceof ASTVar) {
								final ASTVar var = (ASTVar) selectChildChild;

								// add name of variable to order...
								if (!resultOrder.contains(var.getName())) {
									resultOrder.add(var.getName());
								}
							} else if (selectChildChild instanceof ASTAs) {
								for (int j1 = 0; j1 < selectChildChild
								.jjtGetNumChildren(); ++j1) {
									final Node selectChildChildChild = selectChildChild
									.jjtGetChild(j1);
									if (selectChildChildChild instanceof ASTVar) {
										final ASTVar var = (ASTVar) selectChildChildChild;

										// add name of variable to order...
										if (!resultOrder.contains(var
												.getName())) {
											resultOrder.add(var
													.getName());
										}
									}
								}
							}
						}
					}
				}
			}
		}

		final JButton bt_opgraph = (debugViewerCreator!=null)? debugViewerCreator.createOperatorGraphButton(): null;
		if(bt_AST!=null){
			buttonpanel.add(bt_AST);
		}
		if(bt_coreQuery!=null){
			buttonpanel.add(bt_coreQuery);
		}
		if(bt_coreAST!=null){
			buttonpanel.add(bt_coreAST);
		}
		if(bt_opgraph!=null){
			buttonpanel.add(bt_opgraph);
		}

		//			if ((debugViewerCreator instanceof SPARQLDebugViewerCreator && ((SPARQLDebugViewerCreator) debugViewerCreator).debugContainerQuery != null)
		//					&& (this.checkBox_useOntology.isSelected())) {
		//				final JButton bt_RDFSMaterialization = createRDFSMaterializationButton();
		//				this.buttonpanel.add(bt_RDFSMaterialization);
		//			}
		if(materializationInfo!=null){
			buttonpanel.add(new JLabel("Inference:"));
			buttonpanel.add(materializationInfo.createInferenceRulesButton(inferenceRules));	
			buttonpanel.add(materializationInfo.createASTButton());
			buttonpanel.add(materializationInfo.createASTCoreSPARQLButton());
			buttonpanel.add(materializationInfo.createOperatorGraphButton(ruleApplicationsForMaterialization));
		}

		resultpanel.addHierarchyBoundsListener(new HierarchyBoundsListener() {

			public void ancestorMoved(final HierarchyEvent e) {
				Demo_Applet.updateButtonPanelSize(layout, buttonpanel, resultpanel, contentPaneSize);
			}

			public void ancestorResized(final HierarchyEvent e) {
				Demo_Applet.updateButtonPanelSize(layout, buttonpanel, resultpanel, contentPaneSize);
			}
		});

		resultpanel.add(buttonpanel, BorderLayout.NORTH);

		boolean tablesOccur = false;
		for (final QueryResult qr : resultQueryEvaluator) {
			if (!(qr instanceof BooleanResult) && (qr.size() > 0))
				tablesOccur = true;
		}

		JSplitPane splitPane_result = null;

		if (tablesOccur) {

			final JCheckBox cb_prefixes = new JCheckBox("Use prefixes", true);
			cb_prefixes.setSelected(usePrefixes.isTrue());
			cb_prefixes.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					// set boolean flag...
					if (e.getStateChange() == ItemEvent.SELECTED) {
						usePrefixes.setValue(true);
					} else if (e.getStateChange() == ItemEvent.DESELECTED) {
						usePrefixes.setValue(false);
					}
					if (prefixInstance != null)
						prefixInstance.setStatus(usePrefixes.isTrue());
					resultpanel.removeAll();
					try {
						Demo_Applet.setupResultPanel(resultpanel, resultQueryEvaluator, debugViewerCreator, materializationInfo, inferenceRules,  ruleApplicationsForMaterialization, errorsInOntology, usePrefixes,  prefixInstance, contentPane );
					} catch(Exception exception){
						System.err.println("Should only occurr if it already occurred before:\n"+exception);
						exception.printStackTrace();
					}
					contentPane.validate();
				}
			});

			final JPanel prefixesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
			prefixesPanel.add(cb_prefixes);

			buttonpanel.add(prefixesPanel);

			if (usePrefixes.isTrue()) {
				final JPanel pPanel = new JPanel(new BorderLayout());

				final JLabel info = new JLabel();
				info.setText("Prefixes:");

				final JPanel infoPanel = new JPanel(new FlowLayout(
						FlowLayout.LEFT));
				infoPanel.add(info);
				pPanel.add(infoPanel, BorderLayout.NORTH);

				final LuposDocument document = new LuposDocument();
				final LuposJTextPane ta_prefixes = new LuposJTextPane(document);
				document.init(TurtleParser.createILuposParser(new LuposDocumentReader(document)), false);

				ta_prefixes.setText(prefixInstance.getPrefixString("", "").toString());
				ta_prefixes.setEditable(false);
				final JScrollPane scroll = new JScrollPane(ta_prefixes);
				pPanel.add(scroll, BorderLayout.CENTER);

				splitPane_result = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
				splitPane_result.setOneTouchExpandable(true);
				splitPane_result.setContinuousLayout(true);
				splitPane_result.setTopComponent(pPanel);
				splitPane_result.setResizeWeight(0.15);

				resultpanel.add(splitPane_result, BorderLayout.CENTER);
			}
		}

		// --- output result table - end ---
		// final JScrollPane sp_result = new JScrollPane(resultTable);
		final QueryResult[] toDisplay;
		if(errorsInOntology!=null){
			toDisplay = new QueryResult[1+resultQueryEvaluator.length];
			toDisplay[0] = errorsInOntology;
			System.arraycopy(resultQueryEvaluator, 0, toDisplay, 1, resultQueryEvaluator.length);
		} else {
			toDisplay = resultQueryEvaluator;
		}

		final JScrollPane scrollpane = new JScrollPane(ShowResult.getResultPanel(errorsInOntology!=null, toDisplay, prefixInstance, resultOrder, null));

		if (usePrefixes.isTrue() && tablesOccur) {
			splitPane_result.setBottomComponent(scrollpane);
		} else {
			resultpanel.add(scrollpane, BorderLayout.CENTER);
		}

		Demo_Applet.updateButtonPanelSize(layout, buttonpanel, resultpanel, contentPaneSize);

	}


	private static void updateButtonPanelSize(final FlowLayout layout, final JPanel buttonpanel, final JPanel resultpanel, final Dimension contentPaneSize) {
		if (buttonpanel != null && resultpanel != null) {
			final Dimension d = layout.minimumLayoutSize(buttonpanel);
			final int rows = 1 + (int) Math.ceil(d.width / contentPaneSize.width);
			final Dimension n = new Dimension(contentPaneSize.width, rows * d.height);
			buttonpanel.setPreferredSize(n);
		}
	}


	private void setGlobalFont(final Font font) {
		final Enumeration<Object> keys = UIManager.getDefaults().keys();

		while (keys.hasMoreElements()) {
			final Object key = keys.nextElement();

			if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource) {
				UIManager.put(key, new FontUIResource(font));
			}
		}
	}

	public void preferencesChanged() {
		final int lp_query_pos = this.tp_queryInput.getCaretPosition();
		final int lp_data_pos = this.tp_dataInput.getCaretPosition();
		final int lp_rif_pos = this.tp_rifInput.getCaretPosition();

		this.removeLinePainterAndErrorLinePainter();
		this.loadLookAndFeel();
		this.loadMainFont();

		this.loadSyntaxHighlighting();
		this.loadTextFieldFont();
		this.loadLineNumbers();

		if (this.isApplet) {
			SwingUtilities.updateComponentTreeUI(this);
		} else {
			SwingUtilities.updateComponentTreeUI(this.frame);
		}

		this.tp_queryInput.setCaretPosition(lp_query_pos);
		this.tp_rifInput.setCaretPosition(lp_rif_pos);
		this.tp_dataInput.setCaretPosition(lp_data_pos);

		this.loadCurrentLineColor();
		this.loadErrorLineColor();
	}

	private void loadMainFont() {
		try {
			if (BooleanDatatype.getValues("standardFont.fontEnable").get(0)
					.booleanValue()) {
				this.setGlobalFont(FontDatatype.getValues("standardFont.font")
						.get(0));
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private void loadLookAndFeel() {
		if (this.defaultFont != null) {
			this.setGlobalFont(this.defaultFont);
		}

		final HashMap<String, String> classesLAF = new HashMap<String, String>();

//		classesLAF.put("Acryl", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");
//		classesLAF.put("Aero", "com.jtattoo.plaf.aero.AeroLookAndFeel");
//		classesLAF.put("Aluminium",
//				"com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
//		classesLAF.put("Bernstein",
//				"com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
//		classesLAF.put("Fast", "com.jtattoo.plaf.fast.FastLookAndFeel");
//		classesLAF.put("HiFi", "com.jtattoo.plaf.hifi.HiFiLookAndFeel");
//		classesLAF.put("Luna", "com.jtattoo.plaf.luna.LunaLookAndFeel");
//		classesLAF.put("McWin", "com.jtattoo.plaf.mcwin.McWinLookAndFeel");
//		classesLAF.put("Mint", "com.jtattoo.plaf.mint.MintLookAndFeel");
//		classesLAF.put("Noire", "com.jtattoo.plaf.noire.NoireLookAndFeel");
//		classesLAF.put("Smart", "com.jtattoo.plaf.smart.SmartLookAndFeel");

		final UIManager.LookAndFeelInfo[] lafInfo = UIManager
				.getInstalledLookAndFeels();

		for (int i = 0; i < lafInfo.length; i++) {
			classesLAF.put(lafInfo[i].getName(), lafInfo[i].getClassName());
		}
		try {
			String chosen = CollectionDatatype.getValues("lookAndFeel").get(0);
			try {
				// reset to default theme
				if (chosen.compareTo("Metal") == 0) {
					javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.DefaultMetalTheme());
				} else if (chosen.compareTo("Metal Ocean") == 0) {
					javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new javax.swing.plaf.metal.OceanTheme());
					chosen = "Metal";
				}
//				} else if (chosen.compareTo("Fast") == 0) {
//					com.jtattoo.plaf.fast.FastLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Smart") == 0) {
//					com.jtattoo.plaf.smart.SmartLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Acryl") == 0) {
//					com.jtattoo.plaf.acryl.AcrylLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Aero") == 0) {
//					com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Bernstein") == 0) {
//					com.jtattoo.plaf.bernstein.BernsteinLookAndFeel
//							.setTheme("Default");
//				} else if (chosen.compareTo("Aluminium") == 0) {
//					com.jtattoo.plaf.aluminium.AluminiumLookAndFeel
//							.setTheme("Default");
//				} else if (chosen.compareTo("McWin") == 0) {
//					com.jtattoo.plaf.mcwin.McWinLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Mint") == 0) {
//					com.jtattoo.plaf.mint.MintLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Hifi") == 0) {
//					com.jtattoo.plaf.hifi.HiFiLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Noire") == 0) {
//					com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme("Default");
//				} else if (chosen.compareTo("Luna") == 0) {
//					com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme("Default");
//				}

				final String lookAndFeel = classesLAF.get(chosen);
				UIManager.setLookAndFeel(lookAndFeel);

				this.defaultFont = UIManager.getFont("Label.font");

				this.repaint();
			} catch (final ClassNotFoundException e) {
				System.err
						.println("Couldn't find class for specified look and feel:"
								+ classesLAF.get(chosen));
				System.err
						.println("Did you include the L&F library in the class path?");
				System.err.println("Using the default look and feel.");

				e.printStackTrace();
			} catch (final UnsupportedLookAndFeelException e) {
				System.err.println("Can't use the specified look and feel ("
						+ classesLAF.get(chosen) + ") on this platform.");
				System.err.println("Using the default look and feel.");

				e.printStackTrace();
			} catch (final Exception e) {
				System.err.println("Couldn't get specified look and feel ("
						+ classesLAF.get(chosen) + "), for some reason.");
				System.err.println("Using the default look and feel.");

				e.printStackTrace();
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private void removeLinePainterAndErrorLinePainter() {
		removeLinePainter(this.lp_queryInput, this.tp_queryInput);
		removeLinePainter(this.lp_rifInput, this.tp_rifInput);
		removeLinePainter(this.lp_dataInput, this.tp_dataInput);

		this.tp_queryInput.disableErrorLine();
		this.tp_dataInput.disableErrorLine();
	}

	private void loadCurrentLineColor() {
		try {
			if (BooleanDatatype.getValues("currentLineColor.colorEnable")
					.get(0).booleanValue()) {
				final int alphaValue = IntegerDatatype
						.getValues("currentLineColor.colorTransparency").get(0)
						.intValue();

				final Color color = ColorDatatype.getValues(
						"currentLineColor.color").get(0);

				this.lp_color = new Color(color.getRed(), color.getGreen(),
						color.getBlue(), alphaValue);

				this.lp_queryInput = new LinePainter(this.tp_queryInput,
						this.lp_color);
				this.lp_rifInput = new LinePainter(this.tp_rifInput,
						this.lp_color);
				this.lp_dataInput = new LinePainter(this.tp_dataInput,
						this.lp_color);
			} else {
				removeLinePainter(this.lp_queryInput, this.tp_queryInput);
				removeLinePainter(this.lp_rifInput, this.tp_rifInput);
				removeLinePainter(this.lp_dataInput, this.tp_dataInput);
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private static void removeLinePainter(LinePainter lp, final JTextPane tp) {
		if (lp != null) {
			lp.removeLinePainter();
			lp = null;
			tp.revalidate();
			tp.repaint();
		}
	}

	private void loadErrorLineColor() {
		try {
			if (BooleanDatatype.getValues("errorLine.colorEnable").get(0)
					.booleanValue()) {
				this.tp_queryInput.setErrorLineStatus(true);
				this.tp_dataInput.setErrorLineStatus(true);

				final int alphaValue = IntegerDatatype
						.getValues("errorLine.colorTransparency").get(0)
						.intValue();

				final Color color = ColorDatatype.getValues("errorLine.color")
						.get(0);

				final Color errorLineColor = new Color(color.getRed(),
						color.getGreen(), color.getBlue(), alphaValue);

				this.tp_queryInput.setErrorLineColor(errorLineColor);
				this.tp_dataInput.setErrorLineColor(errorLineColor);
			} else {
				this.tp_queryInput.setErrorLineStatus(false);
				this.tp_dataInput.setErrorLineStatus(false);
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private void loadTextFieldFont() {
		try {
			Font tfFont;

			if (BooleanDatatype.getValues("textFieldFont.fontEnable").get(0)
					.booleanValue()) {
				tfFont = FontDatatype.getValues("textFieldFont.font").get(0);
			} else {
				tfFont = UIManager.getFont("TextPane.font");
			}

			this.tp_queryInput.setFont(tfFont);
			this.tp_rifInput.setFont(tfFont);
			this.tp_dataInput.setFont(tfFont);
			// this.ta_errors.setFont(tfFont);
			this.ta_dataInputErrors.setFont(tfFont);
			this.ta_queryInputErrors.setFont(tfFont);
			this.ta_rifInputErrors.setFont(tfFont);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private void loadLineNumbers() {
		try {
			if (BooleanDatatype.getValues("lineCount").get(0).booleanValue()) {
				this.queryInputSP.setRowHeaderView(new LineNumbers(
						this.tp_queryInput));
				this.rifInputSP.setRowHeaderView(new LineNumbers(
						this.tp_rifInput));
				this.dataInputSP.setRowHeaderView(new LineNumbers(
						this.tp_dataInput));
			} else {
				this.queryInputSP.setRowHeaderView(new JLabel());
				this.rifInputSP.setRowHeaderView(new JLabel());
				this.dataInputSP.setRowHeaderView(new JLabel());
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private void loadSyntaxHighlighting() {
		try {
			final boolean highlighterStatus = BooleanDatatype
					.getValues("syntaxHighlighting").get(0).booleanValue();

			if (!highlighterStatus) { // no syntax highlighting...
				LANGUAGE.SEMANTIC_WEB.setBlankStyles();
				((LuposDocument) this.tp_queryInput.getDocument()).colorOneTimeAll(); // highlight
				((LuposDocument) this.tp_queryInput.getDocument()).setIgnoreColoring(true); // disable highlighter

				((LuposDocument) this.tp_rifInput.getDocument()).colorOneTimeAll(); // highlight
				((LuposDocument) this.tp_rifInput.getDocument()).setIgnoreColoring(true); // disable highlighter

				((LuposDocument) this.tp_dataInput.getDocument()).colorOneTimeAll(); // highlight
				((LuposDocument) this.tp_dataInput.getDocument()).setIgnoreColoring(true); // disable highlighter
			} else { // syntax highlighting...
				LANGUAGE.SEMANTIC_WEB.setStyles();
				((LuposDocument) this.tp_queryInput.getDocument()).setIgnoreColoring(false); // enable highlighter
				((LuposDocument) this.tp_queryInput.getDocument()).colorOneTimeAll(); // highlight

				((LuposDocument) this.tp_rifInput.getDocument()).setIgnoreColoring(false); // enable highlighter
				((LuposDocument) this.tp_rifInput.getDocument()).colorOneTimeAll(); // highlight

				((LuposDocument) this.tp_dataInput.getDocument()).setIgnoreColoring(false); // enable highlighter
				((LuposDocument) this.tp_dataInput.getDocument()).colorOneTimeAll(); // highlight
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public LuposJTextPane getTp_dataInput() {
		return tp_dataInput;
	}

	public void setSerializedData(final String n3daten) {
		this.tp_dataInput.setText(n3daten);
	}

	public void setSerializedQuery(final String query) {
		this.tp_queryInput.setText(query);
	}

	public String getData() {
		return this.tp_dataInput.getText();
	}

	/*
	 * Display an error message inside both the 'SPARQL query' tab and the 'RDF
	 * data' tab
	 * 
	 * @param error message to display
	 * 
	 * @param append if true, the message will be appended to the current
	 * content of the error box if false, the previous content of the error box
	 * is deleted
	 */
	private void displayErrorMessage(final String error, final boolean append) {
		displayDataErrorMessage(error, append);
		displayRifErrorMessage(error, append);
		displayQueryErrorMessage(error, append);
	}

	private void displayErrorMessage(final String error, final boolean append,
			final JTextArea ta_inputErrors, final int index) {
		if (ta_inputErrors != null) {
			if (append) {
				ta_inputErrors.append(error);
			} else {
				ta_inputErrors.setText(error);
			}
		}
		if (error.compareTo("") != 0)
			this.tabbedPane_globalMainpane.setSelectedIndex(index);
	}

	/*
	 * Display an error message in the error box inside the 'RDF data' tab
	 * 
	 * @param dataError message to display
	 * 
	 * @param append if true, the message will be appended to the current
	 * content of the error box if false, the previous content of the error box
	 * is deleted
	 */
	private void displayDataErrorMessage(final String dataError,
			final boolean append) {
		displayErrorMessage(dataError, append, this.ta_dataInputErrors, 2);
	}
	
	private void displayErrorMessage(final String queryError, final boolean append, final boolean queryOrRif){
		if(queryOrRif){
			displayQueryErrorMessage(queryError, append);
		} else {
			displayRifErrorMessage(queryError, append);
		}
	}

	/*
	 * Display an error message in the error box inside the 'SPARQL query' tab
	 * 
	 * @param queryError message to display
	 * 
	 * @param append if true, the message will be appended to the current
	 * content of the error box if false, the previous content of the error box
	 * is deleted
	 */
	private void displayQueryErrorMessage(final String queryError,
			final boolean append) {
		displayErrorMessage(queryError, append, this.ta_queryInputErrors, 0);
	}

	/*
	 * Display an error message in the error box inside the 'RIF rules' tab
	 * 
	 * @param ruleError message to display
	 * 
	 * @param append if true, the message will be appended to the current
	 * content of the error box if false, the previous content of the error box
	 * is deleted
	 */
	private void displayRifErrorMessage(final String rifError,
			final boolean append) {
		displayErrorMessage(rifError, append, this.ta_rifInputErrors, 1);
	}
	
	public static class BooleanReference {
		private boolean value;
		public BooleanReference(boolean value){
			this.value=value;
		}
		public boolean isTrue() {
			return value;
		}
		public void setValue(boolean value) {
			this.value = value;
		}
	}
}