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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import lupos.gui.BooleanReference;
import lupos.gui.ResultPanelHelper;
import lupos.gui.anotherSyntaxHighlighting.LANGUAGE;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.viewer.ViewerPrefix;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.SaveDialog;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.DocumentGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.AnnotationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.GroupOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ImportOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.PrefixOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.RuleOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AnnotationOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.parsing.VisualRifGenerator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.AnnotationConnection;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
import lupos.misc.FileHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class DocumentEditorPane extends VisualEditor<Operator> {

	private static final long serialVersionUID = 1L;
	private JTabbedPane bottomPane = null;
	private final DocumentEditorPane that = this;
	private JMenu graphMenu = null;
	private VisualRifEditor visualRifEditor;
	private DocumentGraph documentGraph = null;

	private String documentName;
	private Operator startNode = null;

	private RifCodeEditor rifCodeEditor;

	private Console console;

	private RulePanel rulePanel;

	private PrefixOperatorPanel prefixOperatorPanel;
	private ImportOperatorPanel importOperatorPanel;

	private VisualRifGenerator vrg;

	private JPanel rifEvaluator;

	// Constructor
 	protected DocumentEditorPane(final VisualRifEditor visualRifEditor) {
		super(true);
		this.setVisualRifEditor(visualRifEditor);

		this.rifCodeEditor  = new RifCodeEditor();
		this.console = new Console();
		// from VisualEditor<Operator>
		this.statusBar = visualRifEditor.getStatusBar();

		this.documentGraph = new DocumentGraph(this, this.visualRifEditor);

		this.visualGraphs.add(this.documentGraph);
		LANGUAGE.SEMANTIC_WEB.setStyles();
	}//End Constructor

	/* ************* **
	 * Menu + Layout **
	 * ************* */

	/**
	 * <li> EditMenu
	 * <li> GraphMenu
	 * <li> OperatorMenu
	 * <li> GenerateMenu
	 */
	@Override
	public JMenuBar buildMenuBar() {
		final JMenuBar menuBar = this.createMenuBar();
		menuBar.add(this.buildDocumentMenu());
		menuBar.add(this.buildEditMenu());
		menuBar.add(this.buildGraphMenu());
		menuBar.add(this.buildOperatorMenu());
		menuBar.add(this.buildGenerateMenu());
		menuBar.setMinimumSize(menuBar.getPreferredSize());

		return menuBar;
	}


	private JMenu buildDocumentMenu() {
		final JMenuItem exportMI = new JMenuItem("Export Document");
		exportMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				final SaveDialog chooser = new SaveDialog(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("rif document", "rif","txt"));

				if(chooser.showDialog(DocumentEditorPane.this.that, "Export") == SaveDialog.APPROVE_OPTION) {
					String fileName = chooser.getSelectedFile().getAbsolutePath();

					if(!fileName.endsWith(".rif")) {
						fileName += ".rif";
					}
					DocumentEditorPane.this.visualRifEditor.getSaveLoader().export(fileName);
				}
			}
		});

		final JMenuItem importMI = new JMenuItem("Import Document");
		importMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				final JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter("rif document", "rif","txt"));

				if(chooser.showDialog(DocumentEditorPane.this.that, "Import") == JFileChooser.APPROVE_OPTION) {
					final String fileName = chooser.getSelectedFile().getAbsolutePath();
					DocumentEditorPane.this.visualRifEditor.importNewDocument(FileHelper.fastReadFile(fileName));
				}
			}
		});

		final JMenu documentMenu = new JMenu("Document");

		importMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'I', InputEvent.CTRL_DOWN_MASK )
				);
		exportMI.setAccelerator(
				  KeyStroke.getKeyStroke( 'E', InputEvent.CTRL_DOWN_MASK )
				);

		documentMenu.add(importMI);
		documentMenu.add(exportMI);
		return documentMenu;
	}

	private JMenu buildGraphMenu() {
		// create JMenuItem to rearrange the QueryGraph...
		final JMenuItem rearrangeMI = new JMenuItem("Arrange Graph");
		rearrangeMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				DocumentEditorPane.this.that.statusBar.setText("Arranging query ...");

				for (final VisualGraph<Operator> visualGraph : DocumentEditorPane.this.that.getVisualGraphs()) {
					visualGraph.arrange(Arrange.values()[0]);
				}

				DocumentEditorPane.this.that.statusBar.clear();
			}
		});

		// create Graph menu and add components to it...
		this.graphMenu = new JMenu("Graph");
		this.graphMenu.setEnabled(false);
		this.graphMenu.add(rearrangeMI);

		this.jGraphMenus.add(this.graphMenu);

		return this.graphMenu;
	}


	/**
	 *
	 * @return JMenu with following MenuItems :
	 * <li> RIF Code
	 * <li> Visual RIF
	 */
	private JMenu buildGenerateMenu() {

		// create JMenuItem to add a connection between two Operators...
		final JMenuItem rifMI = new JMenuItem("RIF Code");
		rifMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				DocumentEditorPane.this.generateRif();

			}
		});

		// create JMenuItem to add a connection between two Operators...
		final JMenuItem visualMI = new JMenuItem("Visual RIF");
		visualMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				DocumentEditorPane.this.evaluate();
			}
		});

		final JMenu generateMenu = new JMenu("Generate");

		generateMenu.setMnemonic('g');
		rifMI.setMnemonic('r');
		visualMI.setMnemonic('v');
		generateMenu.add(rifMI);
		generateMenu.add(visualMI);

		return generateMenu;
	}

	/**
	 *
	 * @return JMenu with following MenuItems :
	 * <li> Group
	 * <li> Rule
	 * <li> Prefix
	 * <li> Annotation
	 * <li> Connection
	 */
	private JMenu buildOperatorMenu() {

		// create JMenuItem to add Operator...
		final JMenuItem ruleMI = new JMenuItem("Rule");
		ruleMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				DocumentEditorPane.this.documentGraph.setVisualRifEditor(DocumentEditorPane.this.visualRifEditor);
				DocumentEditorPane.this.that.prepareOperatorForAdd(RuleOperator.class);
			}
		});

		// create JMenuItem to add Operator...
		final JMenuItem prefixMI = new JMenuItem("Prefix");
		prefixMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				DocumentEditorPane.this.that.prepareOperatorForAdd(PrefixOperator.class);
				prefixMI.setEnabled(false);
			}
		});

		// create JMenuItem to add Operator...
		final JMenuItem importMI = new JMenuItem("Import");
		importMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
						DocumentEditorPane.this.that.prepareOperatorForAdd(ImportOperator.class);
						importMI.setEnabled(false);
					}
		});

		// create JMenuItem
		final JMenuItem annoMI = new JMenuItem("Annotation");
		annoMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				DocumentEditorPane.this.that.prepareOperatorForAdd(AnnotationOperator.class);
			}
		});

		// create JMenuItem to add JumpOver-Operator...
		final JMenuItem conMI = new JMenuItem("Connection");
		conMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				boolean tmp = false;
				for (int n = 0 ; n < DocumentEditorPane.this.visualGraphs.get(0).getComponentCount() ; n++){
				if (DocumentEditorPane.this.visualGraphs.get(0).getComponent(n) instanceof AnnotationOperatorPanel){
						tmp = true;
					}
				}

				if(tmp) {
					DocumentEditorPane.this.connectionMode = new AnnotationConnection(DocumentEditorPane.this.myself);
				} else {
					DocumentEditorPane.this.showThereIsNoAnnotationOperatorDialog();
				}

			}
		});

		// create Operator menu and add components to it...
		final JMenu operatorMenu = new JMenu("Add");

		operatorMenu.setMnemonic('a');
		ruleMI.setMnemonic('r');
		prefixMI.setMnemonic('p');
		importMI.setMnemonic('i');
		annoMI.setMnemonic('a');
		conMI.setMnemonic('c');
		operatorMenu.add(ruleMI);
		operatorMenu.addSeparator();
		operatorMenu.add(prefixMI);
		operatorMenu.add(importMI);
		operatorMenu.addSeparator();
		operatorMenu.add(annoMI);
		operatorMenu.add(conMI);

		return operatorMenu;
	}


	@Override
	protected void manageMenuItems() {
		super.manageMenuItems();

		boolean empty = true;

		for(final VisualGraph<Operator> visualGraph : this.visualGraphs) {
			if(visualGraph.getBoxes().size() != 0) {
				empty = false;
				break;
			}
		}
		this.graphMenu.setEnabled(!empty);
	}


	public void addJGraphMenu(final JMenu menu) {
		this.jGraphMenus.add(menu);
	}


	@Override
	protected void pasteElements(final String arg0) {}


	public JTabbedPane buildBottomPane(){

		this.bottomPane = new JTabbedPane();

		this.rifCodeEditor = new RifCodeEditor();
		this.rifEvaluator = new JPanel(new BorderLayout());

		final JButton buttonRifCode = new JButton("Rif Code");

		final ActionListener alRifCodeEditor = new ActionListener() {
	      @Override
		public void actionPerformed(final ActionEvent ae) {
	    	  DocumentEditorPane.this.generateRif();
	      }
	    };
	    buttonRifCode.addActionListener(alRifCodeEditor);

		final JPanel panelRifCodeEditor = new JPanel();
		panelRifCodeEditor.setOpaque(false);
		panelRifCodeEditor.add(buttonRifCode);


		final JButton buttonVisualRif = new JButton("Visual Rif");

		final ActionListener alVisualRif = new ActionListener() {
	      @Override
		public void actionPerformed(final ActionEvent ae) {
	    	  DocumentEditorPane.this.evaluate();
	      }
	    };
	    buttonVisualRif.addActionListener(alVisualRif);


		final JPanel panelVisualRif = new JPanel();
		panelVisualRif.setOpaque(false);
		panelVisualRif.add(buttonVisualRif);

		final JButton buttonEvaluate = new JButton("Evaluate");

		final ActionListener alEvaluate = new ActionListener() {
	      @Override
		public void actionPerformed(final ActionEvent ae) {
	    	  DocumentEditorPane.this.bottomPane.setSelectedIndex(2);
				try {
					ResultPanelHelper.appyRIFRules(DocumentEditorPane.this.rifCodeEditor.getTp_rifInput().getText(), DocumentEditorPane.this.rifEvaluator, new BooleanReference(false), new ViewerPrefix(false));
				} catch (final Exception e) {
					DocumentEditorPane.this.console.getTextArea().setText(e.getMessage());
					DocumentEditorPane.this.bottomPane.setSelectedIndex(1);
				}

	      }
	    };
	    buttonEvaluate.addActionListener(alEvaluate);

		final JPanel panelEvaluate = new JPanel();
		panelEvaluate.setOpaque(false);
		panelEvaluate.add(buttonEvaluate);

		this.bottomPane.add("RIF Code",this.rifCodeEditor.getRifInputSP());
		this.bottomPane.setTabComponentAt(0, panelRifCodeEditor);

		this.bottomPane.add("Visual Rif",this.console.getScrollPane());
		this.bottomPane.setTabComponentAt(1, panelVisualRif);

		this.bottomPane.add("Rif Evaluator", new JScrollPane(this.rifEvaluator));
		this.bottomPane.setTabComponentAt(2, panelEvaluate);
		return this.bottomPane;
	}


	@Override
	public JMenu buildEditMenu() {
		this.copyMI = new JMenuItem("Copy");
		this.copyMI.setEnabled(false);
		this.copyMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				DocumentEditorPane.this.copyElements();
			}
		});

		// TODO
		this.pasteMI = new JMenuItem("Paste");
		this.pasteMI.setEnabled(true);
//		this.pasteMI.addActionListener(new ActionListener() {
//			public void actionPerformed(final ActionEvent ae) {
//				final Transferable clipboardContent = clipboard
//				.getContents(this);
//
//				if (clipboardContent != null
//						&& clipboardContent
//						.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//					try {
//						final String tempString = (String) clipboardContent
//						.getTransferData(DataFlavor.stringFlavor);
//
//						pasteElements(tempString);
//					} catch (final Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});

		// create JMenuItem to delete the selected Operators...
		this.deleteElementsMI = new JMenuItem("Delete selected element(s)");
		this.deleteElementsMI.setEnabled(false);
		this.deleteElementsMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				final int returnValue = JOptionPane
				.showOptionDialog(
						DocumentEditorPane.this,
						"Do you really want to delete the selected element(s)?",
						"Delete selected element(s)",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null,
						new String[] { "YES", "NO" }, 0);

				if (returnValue == 0) {

					for (final Operator op : DocumentEditorPane.this.selectedOperatorsList) {
						if (op instanceof RuleOperator){
							DocumentEditorPane.this.visualRifEditor.getRuleContainer().deleteRule(((RuleOperator)op).getRuleName());
						}
					}
					DocumentEditorPane.this.deleteOperators(false);
					DocumentEditorPane.this.deleteAnnotations();
				}
			}
		});

		// create JMenuItem to delete the selected Operators and their
		// subtrees...
		this.deleteOpsAndTreesMI = new JMenuItem(
		"Delete selected element(s) with subtree(s)");
		this.deleteOpsAndTreesMI.setEnabled(false);
		this.deleteOpsAndTreesMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				final int returnValue = JOptionPane
				.showOptionDialog(
						DocumentEditorPane.this,
						"Do you really want to delete the selected element(s) and subtree(s)?",
						"Delete selected element(s) and subtree(s)",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE, null,
						new String[] { "YES", "NO" }, 0);

				if (returnValue == 0) {
					for (final Operator op : DocumentEditorPane.this.selectedOperatorsList) {
						if (op instanceof RuleOperator){
							DocumentEditorPane.this.visualRifEditor.getRuleContainer().deleteRule(((RuleOperator)op).getRuleName());
						}
					}
					DocumentEditorPane.this.deleteOperators(true);
				}
			}
		});

		// create Edit menu and add components to it...
		final JMenu editMenu = new JMenu("Edit");
		editMenu.add(this.copyMI);
		editMenu.add(this.pasteMI);
		editMenu.add(this.deleteElementsMI);
		editMenu.add(this.deleteOpsAndTreesMI);

		this.jGraphMenus.add(editMenu);

		return editMenu;
	}


	/* **************** **
	 * Rif -> VisualRif **
	 * **************** */

	public void evaluate() {
		final String rifCode = this.rifCodeEditor.getTp_rifInput().getText();

		try {

		final LinkedList<GraphWrapper> rootList = new LinkedList<GraphWrapper>();

		this.visualRifEditor.getRuleContainer().deleteAllRules(this.getDocumentName());
		this.visualGraphs.get(0).removeAll();


			final Operator[] rootNodes = this.parseRif(rifCode);


			for (int i = 0; i < rootNodes.length; i++) {

				// Prefix
				if (rootNodes[i] instanceof PrefixOperator){
					final PrefixOperator prefixOperator = (PrefixOperator) rootNodes[i];

					final GraphWrapperOperator gwo = new GraphWrapperOperator(prefixOperator);

					rootList.add(gwo);
				}

				// Rule
				if ( rootNodes[i] instanceof RuleOperator ){
					final RuleOperator ro = (RuleOperator) rootNodes[i];

					final GraphWrapperOperator gwo = new GraphWrapperOperator(ro);
					ro.getRulePanel().getRuleEditorPane().evaluate(ro.getUnVisitedObject(),this.vrg);
					rootList.add(gwo);
				}else{

					rootList.add(new GraphWrapperOperator(rootNodes[i]));
				}
			}

			// generate QueryGraph...
			final JPanel graphPanel = this.visualGraphs.get(0).createGraph(
					rootList,
					Arrange.values()[0]);

			this.visualGraphs.get(0).updateMainPanel(graphPanel);

		} catch (final Throwable e) {

			this.statusBar.clear();

//			this.bottomPane.setSelectedIndex(1);
//			this.console.getTextArea().setText(e.getMessage());

//			JOptionPane.showOptionDialog(this, e.getMessage(), "Error",
//					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
//					null, null, null);
		}

	}

	private Operator[] parseRif(final String rifCode) {
		this.statusBar.setText("Parsing rif ...");
		int numberOfRootElements = 0;
		int cnt = 0 ;
		Operator[] rootArray;

		this.vrg = new VisualRifGenerator(rifCode,this.that,this.console,this.bottomPane,this.visualRifEditor, null);

		if ( !this.vrg.getRifDocument().getPrefixMap().isEmpty() ) {
			numberOfRootElements++;
		}
		if ( !this.vrg.getRifDocument().getChildren().isEmpty() ) {
			numberOfRootElements = numberOfRootElements + this.vrg.getRifDocument().getChildren().size();
		}

		rootArray = new Operator[numberOfRootElements];

		// Prefix
		if ( !this.vrg.getRifDocument().getPrefixMap().isEmpty() ){
			final Operator root = (Operator) this.vrg.getRifDocument().accept(this.vrg, null);
//			root.setParents();
			rootArray[cnt] = root;
			cnt++;
		}

		// Rules
		if ( !this.vrg.getRifDocument().getChildren().isEmpty() ){
			for (int i = 0; i < this.vrg.getRifDocument().getChildren().size(); i++) {
				final Operator root = (Operator) this.vrg.getRifDocument().getChildren().get(i).accept(this.vrg, null);
//				root.setParents();
				rootArray[cnt] = root;
				cnt++;
			}
		}
		this.statusBar.clear();

		return rootArray;
	}


	/* **************** **
	 * VisualRif -> Rif **
	 * **************** */

	public void generateRif(){
		this.statusBar.setText("Validating query ...");

		final boolean valid = this.visualGraphs.get(0).validateGraph(true,null);
		System.out.println(" DocumentEditorPane.isValid(): "+valid);

		this.statusBar.clear();

		if (valid) {
			this.statusBar.setText("Serializing query ...");

			if (this.prefixIsOnCanvas()) {

				this.setPrefixOperatorPanel();

				final HashMap<String, String> prefixList = this.prefixOperatorPanel
						.getPrefix().getPrefixList();

				this.documentGraph.setPrefixList(prefixList);
			}

			if (this.importIsOnCanvas()) {

				this.setImportOperatorPanel();

				final HashMap<String, String> importList = this.importOperatorPanel
						.getImportOperator().getImportList();

				this.documentGraph.setImportList(importList);
			}


			// serialize Rules
			this.serializeRules();

			final String serializedDocument = this.visualGraphs.get(0)
			.serializeGraph();


			this.statusBar.clear();
			final String[] ret = new String[1];
			ret[0] = "";
			final String rif = serializedDocument;

			this.bottomPane.setSelectedIndex(0);
			this.rifCodeEditor.getTp_rifInput().setText(rif);
		}
	}

	private void serializeRules() {
		for (int i = 0; i < this.visualRifEditor.getRuleContainer().getRulePanelList().size(); i++) {

			final StringBuffer sb = this.visualRifEditor.getRuleContainer().getRulePanelList().get(i).getRulePanel().getRuleEditorPane().serializeRule();

			final RuleOperatorPanel rop = (RuleOperatorPanel)this.visualRifEditor.getRuleContainer().getRulePanelList().get(i).getComponent();

			rop.setSerializedOperator(sb);
		}
	}


	/*
	 * Load + Safe
	 */

	public JSONObject toJSON() throws JSONException {
		final JSONObject saveObject = new JSONObject();

		saveObject.put("TOPTOOLBAR", this.topToolbar.toJSON());
		saveObject.put("DOCUMENTGRAPH", ((DocumentGraph) this.visualGraphs.get(0)).toJSON());
		saveObject.put("CODEEDITOR", this.rifCodeEditor.toJSON());

		return saveObject;
	}

	@SuppressWarnings("unchecked")
	public void fromJSON(final JSONObject loadObject) {
		if(loadObject != null) {
			try {
				((DocumentGraph) this.visualGraphs.get(0)).fromJSON(loadObject.getJSONObject("DOCUMENTGRAPH"));
				((DocumentGraph) this.visualGraphs.get(0)).updateSize();
			}
			catch(final JSONException e) {
				e.printStackTrace();
			}

			try {
				this.rifCodeEditor.fromJSON(loadObject.getJSONObject("CODEEDITOR"));
			} catch (final JSONException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}


	/*
	 * util
	 */
	private void setPrefixOperatorPanel() {
		final Component[] comp = this.visualGraphs.get(0).getComponents();

		for (int i = 0; i < comp.length; i++) {
			if (comp[i] instanceof PrefixOperatorPanel) {
				this.setPrefixOperatorPanel((PrefixOperatorPanel) comp[i]);
				break;
			}
		}
	}


	private void setImportOperatorPanel() {
		final Component[] comp = this.visualGraphs.get(0).getComponents();

		for (int i = 0; i < comp.length; i++) {
			if (comp[i] instanceof  ImportOperatorPanel) {
				this.setImportOperatorPanel((ImportOperatorPanel) comp[i]);
				break;
			}
		}
	}


	private void showThereIsNoAnnotationOperatorDialog(){
		JOptionPane
		.showMessageDialog(this,
		"Please insert an annotation first!");
	}


	public void deleteRule(final String ruleName) {
		final Component[] c = this.visualGraphs.get(0).getComponents();

		final int pos = this.getArrayPosition(ruleName);

		this.visualGraphs.get(0).remove(pos);

		final RuleOperatorPanel rop = (RuleOperatorPanel)c[pos];
		rop.delete();
	}

	public void deleteGroup(final String groupName){
		final Component[] c = this.visualGraphs.get(0).getComponents();

		final int pos = this.getArrayPosition(groupName);
		this.visualGraphs.get(0).remove(pos);

		final GroupOperatorPanel gop = (GroupOperatorPanel)c[pos];
		gop.delete();
	}


	public void updateRuleNameInVisualGraphsComponentArray(final String oldName, final String newName){
		final Component[] c = this.visualGraphs.get(0).getComponents();
		for(int i = 0 ; i < c.length ; i++){
			if(c[i] instanceof RuleOperatorPanel){
				final RuleOperatorPanel rop = (RuleOperatorPanel)c[i];
				if(rop.getRuleName().equals(oldName)){
					rop.setRuleName(newName);
					rop.setRuleLabelName(newName);
				}
			}
		}
	}

	public void updateGroupNameInVisualGraphsComponentArray(final String oldName, final String newName){
		final Component[] c = this.visualGraphs.get(0).getComponents();

		for(int i = 0 ; i < c.length ; i++){
			if(c[i] instanceof GroupOperatorPanel){
				final GroupOperatorPanel gop = (GroupOperatorPanel)c[i];
				if(gop.getGroupName().equals(oldName)){
					gop.setGroupName(newName);
					gop.setGroupLabelName(newName);
				}
			}
		}
	}


	private int getArrayPosition(final String name){
		final Component[] c = this.visualGraphs.get(0).getComponents();

		for(int i = 0 ; i < c.length ; i++){
			if(c[i] instanceof RuleOperatorPanel){
				final RuleOperatorPanel rop = (RuleOperatorPanel)c[i];
				if(rop.getRuleName().equals(name)){
					return i;
				}
			}
		}
		return 9999;
	}


	private boolean prefixIsOnCanvas(){
		final Component[] comp = this.visualGraphs.get(0).getComponents();

		for (int i = 0; i < comp.length; i++) {
			if(comp[i] instanceof PrefixOperatorPanel){
				return true;
			}
		}
		return false;
	}


	private boolean importIsOnCanvas(){
		final Component[] comp = this.visualGraphs.get(0).getComponents();

		for (int i = 0; i < comp.length; i++) {
			if(comp[i] instanceof ImportOperatorPanel){
				return true;
			}
		}
		return false;
	}


	public String[] getPrefixList() {
		final Component[] comp = this.visualGraphs.get(0).getComponents();
		String[] ret = new String[1];
		ret[0] = "";

		for (int i = 0; i < comp.length; i++) {
			if (comp[i] instanceof PrefixOperatorPanel) {
				this.setPrefixOperatorPanel((PrefixOperatorPanel) comp[i]);
				break;
			}
		}

		if (this.prefixIsOnCanvas()
				&& this.getPrefixOperatorPanel().getPrefix().hasElements()) {

			ret = new String[this.getPrefixOperatorPanel().getPrefix()
					.getPrefixList().size()+4];

			int i = 0;

			for (final String namespace : this.getPrefixOperatorPanel()
					.getPrefix().getPrefixList().keySet()) {

				ret[i] = this.getPrefixOperatorPanel().getPrefix()
						.getPrefixList().get(namespace);

				if (ret[i].equals("xs")){

					ret[i+1] = "xs#integer";

					ret[i+2] = "xs#string";

					ret[i+3] = "integer";
					i+=3;
				}
				i++;
			}
			ret[this.getPrefixOperatorPanel().getPrefix()
				.getPrefixList().size()+3] = "";
		}
		return ret;
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */


	public JTabbedPane getBottomPane() {
		return this.bottomPane;
	}

	public void setBottomPane(final JTabbedPane bottomPane) {
		this.bottomPane = bottomPane;
	}

	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	public void setDocumentName(final String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentName() {
		return this.documentName;
	}

	public Console getConsole() {
		return this.console;
	}

	public void setConsole(final Console console) {
		this.console = console;
	}

	public RulePanel getRulePanel() {
		return this.rulePanel;
	}

	public void setRulePanel(final RulePanel rulePanel) {
		this.rulePanel = rulePanel;
	}

	public PrefixOperatorPanel getPrefixOperatorPanel() {
		return this.prefixOperatorPanel;
	}

	public void setPrefixOperatorPanel(final PrefixOperatorPanel prefixOperatorPanel) {
		this.prefixOperatorPanel = prefixOperatorPanel;
	}

	public DocumentGraph getDocumentGraph() {
		return this.documentGraph;
	}

	public void setDocumentGraph(final DocumentGraph documentGraph) {
		this.documentGraph = documentGraph;
	}

	public void setStartNode(final Operator op) {
		this.startNode = op;
	}

	public Operator getStartNode() {
		return this.startNode;
	}

	public ImportOperatorPanel getImportOperatorPanel() {
		return this.importOperatorPanel;
	}

	public void setImportOperatorPanel(final ImportOperatorPanel importOperatorPanel) {
		this.importOperatorPanel = importOperatorPanel;
	}

	public RifCodeEditor getRifCodeEditor() {
		return this.rifCodeEditor;
	}

	public void setRifCodeEditor(final RifCodeEditor rifCodeEditor) {
		this.rifCodeEditor = rifCodeEditor;
	}
}
