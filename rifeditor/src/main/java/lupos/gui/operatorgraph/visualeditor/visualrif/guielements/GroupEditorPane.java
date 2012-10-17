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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

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
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.DocumentGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.GroupGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.AnnotationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.GroupOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.PrefixOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.RuleOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AnnotationOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.GroupOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.AnnotationConnection;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.RuleIdentifier;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.ScrollPane;



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
 	protected GroupEditorPane(final StatusBar statusBar) {
		super(true);
		this.rifCodeEditor  = new RifCodeEditor();
		this.console = new Console();
		// from VisualEditor<Operator>
		this.statusBar = statusBar;
		
		this.groupGraph = new GroupGraph(this); 
		this.visualGraphs.add(this.groupGraph);
		LANGUAGE.SEMANTIC_WEB.setStyles();
//		LANGUAGE.SPARQL_N3.setStyles();
//		LANGUAGE.JAVA.setStyles();
//		LANGUAGE.HTML.setStyles();
		

		this.visualGraphs.get(0).addComponentListener(new ComponentAdapter(){
	    
	    public void componentResized(ComponentEvent e) {
	    	
	    	
	    	componentCnt = visualGraphs.get(0).getComponentCount();
	    	rulesOnCanvasCnt = countRulesOnCanvas();

	    	// Rules 
	    	initRuleComponents();
	    	if (componentCnt > 0 
	    				&& ( visualGraphs.get(0).getComponent(componentCnt - 1).isVisible() )
						&& ( visualGraphs.get(0).getComponent(componentCnt - 1) instanceof RuleOperatorPanel )
						&& ( rulesOnCanvasCnt-1 == rulesCnt ) 
				) {	    		
	    	
			    rulesCnt++;

//				rulePanel = visualRifEditor.getRuleContainer().createNewRule();
				
				RuleOperatorPanel rop = (RuleOperatorPanel) visualGraphs.get(0).getComponent(componentCnt - 1);
				
				rop.setRuleName(rulePanel.getRuleName());
				
//				visualRifEditor.getRuleContainer().getRulePanelList().add(new RuleIdentifier(rulePanel.getRuleName(), rulePanel,  rop, rulePanel.getRulePath()));
	
				
	    	} // End Rules
	    	
	    	// Prefix
	    	if (componentCnt > 0 
	    			&& ( visualGraphs.get(0).getComponent(componentCnt - 1).isVisible() )
					&& ( visualGraphs.get(0).getComponent(componentCnt - 1) instanceof PrefixOperatorPanel) 
				){
	    		
	    		final PrefixOperatorPanel pop = ( PrefixOperatorPanel ) visualGraphs.get(0).getComponent(componentCnt - 1);
	    		
	    		pop.addComponentListener(new ComponentAdapter() {
	    			

					public void componentResized(ComponentEvent e) {
						
						pop.updateSize();
					}

				});
	
	    	} // End Prefix
	    	
	    	// Group
	    	if (componentCnt > 0 
	    			&& ( visualGraphs.get(0).getComponent(componentCnt - 1).isVisible() )
					&& ( visualGraphs.get(0).getComponent(componentCnt - 1) instanceof GroupOperatorPanel) 
				){
	    		
	    		
	    	}// End Group

	    	
	    }// End componentResized()

	    
		});
		
		
		
	}//End Constructor

	

	/**
	 * Add a RuleListener to each Rule 
	 */
	private void initRuleComponents() {

		Component[] temp = new Component[countRulesOnCanvas()];
    	Component[] comp = this.visualGraphs.get(0).getComponents();
    	
    	
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
	 * <li> EditMenu 
	 * <li> GraphMenu
	 * <li> OperatorMenu
	 * <li> GenerateMenu
	 */
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
			public void actionPerformed(final ActionEvent ae) {
				generateRif();

			}
		});

		// create JMenuItem to add a connection between two Operators...
		final JMenuItem visualMI = new JMenuItem("Visual RIF");
		visualMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				System.out
						.println("DocumentEditorPane.buildGenerateMenu(): visualMI"); // TODO

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
			public void actionPerformed(final ActionEvent ae) {
				that.statusBar.setText("Arranging query ...");

				for (final VisualGraph<Operator> visualGraph : that.getVisualGraphs()) {
					visualGraph.arrange(Arrange.values()[0]);
				}

				that.statusBar.clear();
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
			public void actionPerformed(final ActionEvent ae) {
		
				that.prepareOperatorForAdd(GroupOperator.class);

			}
		});

		// create JMenuItem to add Operator...
		final JMenuItem ruleMI = new JMenuItem("Rule");
		ruleMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				
			
				that.prepareOperatorForAdd(RuleOperator.class);
				

			}
		});
		

		// create JMenuItem to add Operator...
		final JMenuItem prefixMI = new JMenuItem("Prefix");
		prefixMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
		
				that.prepareOperatorForAdd(PrefixOperator.class);
				prefixMI.setEnabled(false);
			}
		});
				
				
		// create JMenuItem 
		final JMenuItem annoMI = new JMenuItem("Annotation");
		annoMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
			
				that.prepareOperatorForAdd(AnnotationOperator.class);
			
				
			}
		});
		
		
		// create JMenuItem to add JumpOver-Operator...
		final JMenuItem conMI = new JMenuItem("Connection");
		conMI.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent ae) {
				boolean tmp = false;
				for (int n = 0 ; n < visualGraphs.get(0).getComponentCount() ; n++){
				if (visualGraphs.get(0).getComponent(n) instanceof AnnotationOperatorPanel){
						tmp = true;
					}
				}
				
				if(tmp) {
					connectionMode = new AnnotationConnection(myself);
				}else
					showThereIsNoAnnotationOperatorDialog();
				
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


	protected void pasteElements(String arg0) {}

	
	public JTabbedPane buildBottomPane(){
		
//		final HighlightedDocumentColorAll document = new HighlightedDocumentColorAll(100);
//		document.setHighlightStyle(SPARQLLexer.class);
//
//		this.tp_rifInput  = new JTextPaneControllingRepaint(document);
//		this.tp_rifInput.setFont(new Font("Courier New", Font.PLAIN, 12));
//		

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
		

		
		bottomPane = new JTabbedPane();
//		bottomPane.add("RIF Code",rifCodeEditor);
//		bottomPane.add("Console",new ScrollPane(this.console));
		return bottomPane;
	}
	
	
	private void addRuleListenerToComponent(int pos){
		getVisualGraphs().get(0).getComponent(pos).addMouseListener(new MouseAdapter(){
		     public void mouseClicked(MouseEvent e){
		         if (e.getClickCount() == 2){
		        	
		        	RuleOperatorPanel rop = (RuleOperatorPanel) e.getComponent();
		        	String ruleName = rop.getRuleName();

		        	visualRifEditor.getTreePane().getTree_documents().setSelectionPath( visualRifEditor.getRuleContainer().getRuleByName(ruleName).getRulePath());
		            visualRifEditor.getRuleContainer().showRule(ruleName);
		            
		            }
		         }
		        } );
	}
	

    private int countRulesOnCanvas(){
    	Component[] components = visualGraphs.get(0).getComponents();
    	int cnt=0;
    	for (int i = 0; i < components.length ; i++){
    	
    		if(components[i] instanceof RuleOperatorPanel) cnt++;
    	}
    	
		return cnt;
    }
    
    
	private void generateRif(){
		this.statusBar.setText("Validating query ...");
		
		
		// TODO returns always true
		final boolean valid = this.visualGraphs.get(0).validateGraph(true,null);
		System.out.println(" DocumentEditorPane.isValid(): "+valid);

		this.statusBar.clear();
	
		if (valid) {
			this.statusBar.setText("Serializing query ...");
			
//			if (this.prefixIsOnCanvas()) {
//				this.setPrefixOperatorPanel();
//				HashMap<String, String> prefixList = this.prefixOperatorPanel
//						.getPrefix().getPrefixList();
//				this.groupGraph.setPrefixList(prefixList);
//			}
			
			// serialize Rules 
			this.serializeRules();
			
			final String serializedDocument = this.visualGraphs.get(0)
			.serializeGraph();
			

			this.statusBar.clear();
			String[] ret = new String[1];
			ret[0] = "";
			String rif = serializedDocument;
			
			
			this.rifCodeEditor.getTp_rifInput().setText(rif);


			
		}
				
	

		
	
	}
	
	
	private void setPrefixOperatorPanel() {
		Component[] comp = this.visualGraphs.get(0).getComponents();
		
		for (int i = 0; i < comp.length; i++) {
			if (comp[i] instanceof PrefixOperatorPanel) {
				setPrefixOperatorPanel((PrefixOperatorPanel) comp[i]);
				break;
			}
		}

		
	}

	
	private void serializeRules() {

		
		for (int i = 0; i < visualRifEditor.getRuleContainer().getRulePanelList().size(); i++) {
			StringBuffer sb = visualRifEditor.getRuleContainer().getRulePanelList().get(i).getRulePanel().getRuleEditorPane().serializeRule();
			RuleOperatorPanel rop = (RuleOperatorPanel)visualRifEditor.getRuleContainer().getRulePanelList().get(i).getComponent();
			rop.setSerializedOperator(sb);
		}
		
	}

	
	private void showThereIsNoAnnotationOperatorDialog(){
		JOptionPane
		.showMessageDialog(this,
		"Please insert an annotation first!");
	
	} 

	
	public void deleteRule(String ruleName) {
		
		Component[] c = this.visualGraphs.get(0).getComponents();
		
		int pos = getArrayPosition(ruleName);
		

				this.visualGraphs.get(0).remove(pos);
				
			RuleOperatorPanel rop = (RuleOperatorPanel)c[pos];
					rop.delete();

		
	}
	
	
	public void updateRuleNameInVisualGraphsComponentArray(String oldName, String newName){
		Component[] c = this.visualGraphs.get(0).getComponents();
		
		for(int i = 0 ; i < c.length ; i++){
			if(c[i] instanceof RuleOperatorPanel){
				RuleOperatorPanel rop = (RuleOperatorPanel)c[i];
				if(rop.getRuleName().equals(oldName)){
					rop.setRuleName(newName);
					rop.setRuleLabelName(newName);
				}
			}
		}
		
		
	}
	
	
	private int getArrayPosition(String name){
		Component[] c = this.visualGraphs.get(0).getComponents();
		
		for(int i = 0 ; i < c.length ; i++){
			if(c[i] instanceof RuleOperatorPanel){
				RuleOperatorPanel rop = (RuleOperatorPanel)c[i];
				if(rop.getRuleName().equals(name)){
					return i;
				}
			}
		}
		return 9999;
	}
	
	
	private boolean prefixIsOnCanvas(){
		Component[] comp = this.visualGraphs.get(0).getComponents();
		
		for (int i = 0; i < comp.length; i++) {
			if(comp[i] instanceof PrefixOperatorPanel){
				return true;
			}
		}

		return false;
		}
	
	
	public String[] getPrefixList() {
		Component[] comp = this.visualGraphs.get(0).getComponents();
		String[] ret = new String[1];
		ret[0] = "";
		
		for (int i = 0; i < comp.length; i++) {
			if (comp[i] instanceof PrefixOperatorPanel) {
				setPrefixOperatorPanel((PrefixOperatorPanel) comp[i]);
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


	public JTabbedPane getBottomPane() {
		return bottomPane;
	}

	public void setBottomPane(JTabbedPane bottomPane) {
		this.bottomPane = bottomPane;
	}

	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}

	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}


	
	public Console getConsole() {
		return console;
	}

	public void setConsole(Console console) {
		this.console = console;
	}

	public RulePanel getRulePanel() {
		return rulePanel;
	}

	public void setRulePanel(RulePanel rulePanel) {
		this.rulePanel = rulePanel;
	}

	public int getRulesCnt() {
		return rulesCnt;
	}

	public void setRulesCnt(int rulesCnt) {
		this.rulesCnt = rulesCnt;
	}

	public int getRulesOnCanvasCnt() {
		return rulesOnCanvasCnt;
	}

	public void setRulesOnCanvasCnt(int rulesOnCanvasCnt) {
		this.rulesOnCanvasCnt = rulesOnCanvasCnt;
	}

	public PrefixOperatorPanel getPrefixOperatorPanel() {
		return prefixOperatorPanel;
	}

	public void setPrefixOperatorPanel(PrefixOperatorPanel prefixOperatorPanel) {
		this.prefixOperatorPanel = prefixOperatorPanel;
	}



	public void setStartNode(final Operator op) {
		this.startNode = op;
	}

	public Operator getStartNode() {
		return startNode;
	}



	public String getGroupName() {
		return groupName;
	}



	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	

}
