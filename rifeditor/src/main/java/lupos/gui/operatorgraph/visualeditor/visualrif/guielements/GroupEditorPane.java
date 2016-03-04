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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import lupos.gui.anotherSyntaxHighlighting.LANGUAGE;
import lupos.gui.anotherSyntaxHighlighting.LinePainter;
import lupos.gui.anotherSyntaxHighlighting.LuposJTextPane;
import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.StatusBar;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.GroupGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.AnnotationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.GroupOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.PrefixOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.RuleOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AnnotationOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.GroupOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.AnnotationConnection;
public class GroupEditorPane extends VisualEditor<Operator> {

	private static final long serialVersionUID = 1L;
	private JTabbedPane bottomPane = null;
	private final GroupEditorPane that = this;
	private JMenu graphMenu = null;
	private VisualRifEditor visualRifEditor;
	private GroupGraph groupGraph = null;

	private String groupName;
	private Operator startNode = null;


	private RifCodeEditor rifCodeEditor;
	private Console console;
	private LuposJTextPane tp_rifInput;

	private RulePanel rulePanel;
	private PrefixOperatorPanel prefixOperatorPanel;

	private int componentCnt ;
	private int rulesCnt =0;
	private int rulesOnCanvasCnt=0;

	// Constructor
	/**
	 * <p>Constructor for GroupEditorPane.</p>
	 *
	 * @param statusBar a {@link lupos.gui.operatorgraph.visualeditor.util.StatusBar} object.
	 */
	protected GroupEditorPane(final StatusBar statusBar) {
		super(true);
		this.rifCodeEditor  = new RifCodeEditor();
		this.console = new Console();
		// from VisualEditor<Operator>
		this.statusBar = statusBar;

		this.groupGraph = new GroupGraph(this);
		this.visualGraphs.add(this.groupGraph);
		LANGUAGE.SEMANTIC_WEB.setStyles();
		this.visualGraphs.get(0).addComponentListener(new ComponentAdapter(){

			@Override
			public void componentResized(final ComponentEvent e) {

				GroupEditorPane.this.componentCnt = GroupEditorPane.this.visualGraphs.get(0).getComponentCount();
				GroupEditorPane.this.rulesOnCanvasCnt = GroupEditorPane.this.countRulesOnCanvas();

				// Rules
				GroupEditorPane.this.initRuleComponents();
				if (GroupEditorPane.this.componentCnt > 0
						&& ( GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1).isVisible() )
						&& ( GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1) instanceof RuleOperatorPanel )
						&& ( GroupEditorPane.this.rulesOnCanvasCnt-1 == GroupEditorPane.this.rulesCnt )
						) {

					GroupEditorPane.this.rulesCnt++;

					final RuleOperatorPanel rop = (RuleOperatorPanel) GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1);

					rop.setRuleName(GroupEditorPane.this.rulePanel.getRuleName());
				} // End Rules

				// Prefix
				if (GroupEditorPane.this.componentCnt > 0
						&& ( GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1).isVisible() )
						&& ( GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1) instanceof PrefixOperatorPanel)
						){

					final PrefixOperatorPanel pop = ( PrefixOperatorPanel ) GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1);

					pop.addComponentListener(new ComponentAdapter() {

						@Override
						public void componentResized(final ComponentEvent e) {

							pop.updateSize();
						}

					});

				} // End Prefix

				// Group
				if (GroupEditorPane.this.componentCnt > 0
						&& ( GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1).isVisible() )
						&& ( GroupEditorPane.this.visualGraphs.get(0).getComponent(GroupEditorPane.this.componentCnt - 1) instanceof GroupOperatorPanel)
						){
				}// End Group
			}// End componentResized()
		});

	}//End Constructor

	/**
	 * Add a RuleListener to each Rule
	 */
	private void initRuleComponents() {
		final Component[] temp = new Component[this.countRulesOnCanvas()];
    	final Component[] comp = this.visualGraphs.get(0).getComponents();
    	int j = 0;
    	for (int i = 0; i < comp.length; i++) {
			if(comp[i] instanceof RuleOperatorPanel){
				this.addRuleListenerToComponent(i);
				temp[j] = comp[i];
				j++;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <li> EditMenu
	 * <li> GraphMenu
	 * <li> OperatorMenu
	 * <li> GenerateMenu
	 */
	@Override
	public JMenuBar buildMenuBar() {
		final JMenuBar menuBar = this.createMenuBar();
		menuBar.add(this.buildEditMenu());
		menuBar.add(this.buildGraphMenu());
		menuBar.add(this.buildOperatorMenu());
		menuBar.add(this.buildGenerateMenu());
		menuBar.setMinimumSize(menuBar.getPreferredSize());
		return menuBar;
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
				GroupEditorPane.this.generateRif();
			}
		});

		// create JMenuItem to add a connection between two Operators...
		final JMenuItem visualMI = new JMenuItem("Visual RIF");
		visualMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				System.out.println("DocumentEditorPane.buildGenerateMenu(): visualMI"); // TODO
			}
		});

		final JMenu generateMenu = new JMenu("Generate");
		generateMenu.add(rifMI);
		visualMI.setEnabled(false);
		generateMenu.add(visualMI);

		return generateMenu;
	}

	private JMenu buildGraphMenu() {
		// create JMenuItem to rearrange the QueryGraph...
		final JMenuItem rearrangeMI = new JMenuItem("Arrange Graph");
		rearrangeMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				GroupEditorPane.this.that.statusBar.setText("Arranging query ...");
				for (final VisualGraph<Operator> visualGraph : GroupEditorPane.this.that.getVisualGraphs()) {
					visualGraph.arrange(Arrange.values()[0]);
				}
				GroupEditorPane.this.that.statusBar.clear();
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
	 * <li> Group
	 * <li> Rule
	 * <li> Prefix
	 * <li> Annotation
	 * <li> Connection
	 */
	private JMenu buildOperatorMenu() {
		// create JMenuItem to add a connection between two Operators...
		final JMenuItem groupMI = new JMenuItem("Group");
		groupMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				GroupEditorPane.this.that.prepareOperatorForAdd(GroupOperator.class);
			}
		});

		// create JMenuItem to add Operator...
		final JMenuItem ruleMI = new JMenuItem("Rule");
		ruleMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				GroupEditorPane.this.that.prepareOperatorForAdd(RuleOperator.class);
			}
		});

		// create JMenuItem to add Operator...
		final JMenuItem prefixMI = new JMenuItem("Prefix");
		prefixMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				GroupEditorPane.this.that.prepareOperatorForAdd(PrefixOperator.class);
				prefixMI.setEnabled(false);
			}
		});

		// create JMenuItem
		final JMenuItem annoMI = new JMenuItem("Annotation");
		annoMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				GroupEditorPane.this.that.prepareOperatorForAdd(AnnotationOperator.class);
			}
		});

		// create JMenuItem to add JumpOver-Operator...
		final JMenuItem conMI = new JMenuItem("Connection");
		conMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				boolean tmp = false;
				for (int n = 0 ; n < GroupEditorPane.this.visualGraphs.get(0).getComponentCount() ; n++){
				if (GroupEditorPane.this.visualGraphs.get(0).getComponent(n) instanceof AnnotationOperatorPanel){
						tmp = true;
					}
				}
				if(tmp) {
					GroupEditorPane.this.connectionMode = new AnnotationConnection(GroupEditorPane.this.myself);
				} else {
					GroupEditorPane.this.showThereIsNoAnnotationOperatorDialog();
				}
			}
		});

		// create Operator menu and add components to it...
		final JMenu operatorMenu = new JMenu("Add");
		operatorMenu.add(ruleMI);
		operatorMenu.add(groupMI);
		operatorMenu.addSeparator();
		operatorMenu.add(prefixMI);
		operatorMenu.addSeparator();
		operatorMenu.add(annoMI);
		operatorMenu.add(conMI);
		return operatorMenu;
	}

	/** {@inheritDoc} */
	@Override
	protected void manageMenuItems() {
		super.manageMenuItems();
		boolean empty = true;
		for(final VisualGraph<Operator> visualGraph : this.visualGraphs) {
			if(!visualGraph.getBoxes().isEmpty()) {
				empty = false;
				break;
			}
		}
		this.graphMenu.setEnabled(!empty);
	}

	/**
	 * <p>addJGraphMenu.</p>
	 *
	 * @param menu a {@link javax.swing.JMenu} object.
	 */
	public void addJGraphMenu(final JMenu menu) {
		this.jGraphMenus.add(menu);
	}

	/** {@inheritDoc} */
	@Override
	protected void pasteElements(final String arg0) {}

	/**
	 * <p>buildBottomPane.</p>
	 *
	 * @return a {@link javax.swing.JTabbedPane} object.
	 */
	public JTabbedPane buildBottomPane(){

		new LinePainter(this.tp_rifInput, new Color(202, 223, 245));

		this.rifCodeEditor = new RifCodeEditor(/*this.tp_rifInput*/);

		this.rifCodeEditor.getTp_rifInput().setEditable(false);

		this.rifCodeEditor.getTp_rifInput().setText("(*Dies ist lediglich ein Beispiel*)\n\n" +
				"Document(" +
				"\n\n"+
				"\tPrefix (cpt <http://www.bsp.com#>)\n\n"
				+
				"\t" +
				"Group(" +
				"\n\n" +
				"\t" +
				")" +
				"\n" +
				"\n" +
				")");

		this.bottomPane = new JTabbedPane();
		return this.bottomPane;
	}

	private void addRuleListenerToComponent(final int pos){
		this.getVisualGraphs().get(0).getComponent(pos).addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(final MouseEvent e){
				if (e.getClickCount() == 2){
					final RuleOperatorPanel rop = (RuleOperatorPanel) e.getComponent();
					final String ruleName = rop.getRuleName();
					GroupEditorPane.this.visualRifEditor.getTreePane().getTree_documents().setSelectionPath( GroupEditorPane.this.visualRifEditor.getRuleContainer().getRuleByName(ruleName).getRulePath());
					GroupEditorPane.this.visualRifEditor.getRuleContainer().showRule(ruleName);
				}
			}
		} );
	}

    private int countRulesOnCanvas(){
    	final Component[] components = this.visualGraphs.get(0).getComponents();
    	int cnt=0;
    	for (int i = 0; i < components.length ; i++){
    		if(components[i] instanceof RuleOperatorPanel) {
				cnt++;
			}
    	}
		return cnt;
    }

	private void generateRif(){
		this.statusBar.setText("Validating query ...");
		final boolean valid = this.visualGraphs.get(0).validateGraph(true,null);
		System.out.println(" DocumentEditorPane.isValid(): "+valid);

		this.statusBar.clear();

		if (valid) {
			this.statusBar.setText("Serializing query ...");
			// serialize Rules
			this.serializeRules();

			final String serializedDocument = this.visualGraphs.get(0).serializeGraph();

			this.statusBar.clear();
			final String[] ret = new String[1];
			ret[0] = "";
			final String rif = serializedDocument;

			this.rifCodeEditor.getTp_rifInput().setText(rif);
		}
	}


	private void setPrefixOperatorPanel() {
		final Component[] comp = this.visualGraphs.get(0).getComponents();
		for (int i = 0; i < comp.length; i++) {
			if (comp[i] instanceof PrefixOperatorPanel) {
				this.setPrefixOperatorPanel((PrefixOperatorPanel) comp[i]);
				break;
			}
		}
	}

	private void serializeRules() {
		for (int i = 0; i < this.visualRifEditor.getRuleContainer().getRulePanelList().size(); i++) {
			final StringBuffer sb = this.visualRifEditor.getRuleContainer().getRulePanelList().get(i).getRulePanel().getRuleEditorPane().serializeRule();
			final RuleOperatorPanel rop = (RuleOperatorPanel)this.visualRifEditor.getRuleContainer().getRulePanelList().get(i).getComponent();
			rop.setSerializedOperator(sb);
		}
	}

	private void showThereIsNoAnnotationOperatorDialog(){
		JOptionPane
		.showMessageDialog(this,
		"Please insert an annotation first!");
	}

	/**
	 * <p>deleteRule.</p>
	 *
	 * @param ruleName a {@link java.lang.String} object.
	 */
	public void deleteRule(final String ruleName) {
		final Component[] c = this.visualGraphs.get(0).getComponents();
		final int pos = this.getArrayPosition(ruleName);
		this.visualGraphs.get(0).remove(pos);
		final RuleOperatorPanel rop = (RuleOperatorPanel)c[pos];
		rop.delete();
	}

	/**
	 * <p>updateRuleNameInVisualGraphsComponentArray.</p>
	 *
	 * @param oldName a {@link java.lang.String} object.
	 * @param newName a {@link java.lang.String} object.
	 */
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

	/**
	 * <p>getPrefixList.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
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
					.getPrefixList().size()+1];

			int i = 0;

			for (final String namespace : this.getPrefixOperatorPanel()
					.getPrefix().getPrefixList().keySet()) {

				ret[i] = this.getPrefixOperatorPanel().getPrefix()
						.getPrefixList().get(namespace);
				i++;
			}

			ret[this.getPrefixOperatorPanel().getPrefix()
				.getPrefixList().size()] = "";

			for (int j = 0; j < ret.length; j++) {
				System.out.println("DocumentEditorPanel.getPrefixList(): "+ret[j]);
			}
		}
		return ret;
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */
	/**
	 * <p>Getter for the field <code>bottomPane</code>.</p>
	 *
	 * @return a {@link javax.swing.JTabbedPane} object.
	 */
	public JTabbedPane getBottomPane() {
		return this.bottomPane;
	}

	/**
	 * <p>Setter for the field <code>bottomPane</code>.</p>
	 *
	 * @param bottomPane a {@link javax.swing.JTabbedPane} object.
	 */
	public void setBottomPane(final JTabbedPane bottomPane) {
		this.bottomPane = bottomPane;
	}

	/**
	 * <p>Getter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	/**
	 * <p>Setter for the field <code>visualRifEditor</code>.</p>
	 *
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	/**
	 * <p>Getter for the field <code>console</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.Console} object.
	 */
	public Console getConsole() {
		return this.console;
	}

	/**
	 * <p>Setter for the field <code>console</code>.</p>
	 *
	 * @param console a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.Console} object.
	 */
	public void setConsole(final Console console) {
		this.console = console;
	}

	/**
	 * <p>Getter for the field <code>rulePanel</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public RulePanel getRulePanel() {
		return this.rulePanel;
	}

	/**
	 * <p>Setter for the field <code>rulePanel</code>.</p>
	 *
	 * @param rulePanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RulePanel} object.
	 */
	public void setRulePanel(final RulePanel rulePanel) {
		this.rulePanel = rulePanel;
	}

	/**
	 * <p>Getter for the field <code>rulesCnt</code>.</p>
	 *
	 * @return a int.
	 */
	public int getRulesCnt() {
		return this.rulesCnt;
	}

	/**
	 * <p>Setter for the field <code>rulesCnt</code>.</p>
	 *
	 * @param rulesCnt a int.
	 */
	public void setRulesCnt(final int rulesCnt) {
		this.rulesCnt = rulesCnt;
	}

	/**
	 * <p>Getter for the field <code>rulesOnCanvasCnt</code>.</p>
	 *
	 * @return a int.
	 */
	public int getRulesOnCanvasCnt() {
		return this.rulesOnCanvasCnt;
	}

	/**
	 * <p>Setter for the field <code>rulesOnCanvasCnt</code>.</p>
	 *
	 * @param rulesOnCanvasCnt a int.
	 */
	public void setRulesOnCanvasCnt(final int rulesOnCanvasCnt) {
		this.rulesOnCanvasCnt = rulesOnCanvasCnt;
	}

	/**
	 * <p>Getter for the field <code>prefixOperatorPanel</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.PrefixOperatorPanel} object.
	 */
	public PrefixOperatorPanel getPrefixOperatorPanel() {
		return this.prefixOperatorPanel;
	}

	/**
	 * <p>Setter for the field <code>prefixOperatorPanel</code>.</p>
	 *
	 * @param prefixOperatorPanel a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.PrefixOperatorPanel} object.
	 */
	public void setPrefixOperatorPanel(final PrefixOperatorPanel prefixOperatorPanel) {
		this.prefixOperatorPanel = prefixOperatorPanel;
	}

	/**
	 * <p>Setter for the field <code>startNode</code>.</p>
	 *
	 * @param op a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public void setStartNode(final Operator op) {
		this.startNode = op;
	}

	/**
	 * <p>Getter for the field <code>startNode</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public Operator getStartNode() {
		return this.startNode;
	}

	/**
	 * <p>Getter for the field <code>groupName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * <p>Setter for the field <code>groupName</code>.</p>
	 *
	 * @param groupName a {@link java.lang.String} object.
	 */
	public void setGroupName(final String groupName) {
		this.groupName = groupName;
	}
}
