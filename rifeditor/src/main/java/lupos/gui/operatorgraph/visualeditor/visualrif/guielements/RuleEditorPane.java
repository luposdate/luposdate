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



import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.StatusBar;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ContainerPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.FrameOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ListOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.UnitermOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.VariablePanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AndContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ExistsContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.OrContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.parsing.VisualRifGenerator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.ClassificationConnection;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.rif.model.Rule;

import org.json.JSONException;
import org.json.JSONObject;
public class RuleEditorPane extends VisualEditor<Operator> {



	private static final long serialVersionUID = 9129182740285100634L;
	private final RuleEditorPane that = this;
	private VisualRifEditor visualRifEditor;
	private int componentCnt;

    private RuleGraph ruleGraphLeft;
	private RuleGraph ruleGraphRight;

	private LinkedList<Term> ruleVariableList = new LinkedList<Term>();
	private RifCodeEditor rifCodeEditor;




	// Constructor
	/**
	 * <p>Constructor for RuleEditorPane.</p>
	 *
	 * @param statusBar a {@link lupos.gui.operatorgraph.visualeditor.util.StatusBar} object.
	 * @param vRE a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	protected RuleEditorPane(final StatusBar statusBar, final VisualRifEditor vRE) {
		super(true);
		this.statusBar = statusBar;
		this.setVisualRifEditor(vRE);
		 this.ruleGraphLeft = new RuleGraph(this,this.visualRifEditor,false);
		 this.ruleGraphRight = new RuleGraph(this,this.visualRifEditor,false);

		this.ruleGraphLeft.setRuleVariableList(this.ruleVariableList);
		this.ruleGraphRight.setRuleVariableList(this.ruleVariableList);

		this.visualGraphs.add(this.ruleGraphLeft);
		this.visualGraphs.add(this.ruleGraphRight);

	}


	/** {@inheritDoc} */
	@Override
	protected void pasteElements(final String arg0) {
		// currently not supported
	}


	/** {@inheritDoc} */
	@Override
	public JMenuBar buildMenuBar() {
		final JMenuBar menuBar = this.createMenuBar();
		menuBar.add(this.buildEditMenu());
//		menuBar.add(this.buildGraphMenu());
		menuBar.add(this.buildOperatorMenu());
//		menuBar.add(this.buildGenerateMenu());
		menuBar.setMinimumSize(menuBar.getPreferredSize());

		return menuBar;
	}


	private JMenu buildOperatorMenu() {

		final JMenuItem andMI = new JMenuItem("And");
		andMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(AndContainer.class);

			}
		});

		final JMenuItem orMI = new JMenuItem("Or");
		orMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(OrContainer.class);

			}
		});

		final JMenuItem existsMI = new JMenuItem("Exists");
		existsMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(ExistsContainer.class);

			}
		});


		final JMenuItem unitermMI = new JMenuItem("Uniterm");
		unitermMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(UnitermOperator.class);


			}
		});

		final JMenuItem listMI = new JMenuItem("List");
		listMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(ListOperator.class);


			}
		});

		final JMenuItem frameMI = new JMenuItem("Frame");
		frameMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(FrameOperator.class);


			}
		});

		final JMenuItem varMI = new JMenuItem("Variable");
		varMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(VariableOperator.class);


			}
		});

		final JMenuItem constMI = new JMenuItem("Constant");
		constMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {

				RuleEditorPane.this.that.prepareOperatorForAdd(ConstantOperator.class);


			}
		});

		final JMenuItem conMI = new JMenuItem("Connection");
		conMI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {



					RuleEditorPane.this.that.connectionMode = new ClassificationConnection(RuleEditorPane.this.that);

		}});




		// create Operator menu and add components to it...
		final JMenu operatorMenu = new JMenu("Add");

		operatorMenu.setMnemonic('a');
		unitermMI.setMnemonic('u');
		listMI.setMnemonic('l');
		frameMI.setMnemonic('f');
		varMI.setMnemonic('v');
		constMI.setMnemonic('n');
		andMI.setMnemonic('a');
		orMI.setMnemonic('o');
		existsMI.setMnemonic('e');
		conMI.setMnemonic('c');

//		operatorMenu.addSeparator();
		operatorMenu.add(unitermMI);
		operatorMenu.add(listMI);
		operatorMenu.add(frameMI);

		operatorMenu.addSeparator();

		operatorMenu.add(varMI);
		operatorMenu.add(constMI);

		operatorMenu.addSeparator();

		operatorMenu.add(andMI);
		operatorMenu.add(orMI);
		operatorMenu.add(existsMI);

		operatorMenu.addSeparator();

		operatorMenu.add(conMI);




		return operatorMenu;
	}


	/**
	 * <p>serializeRule.</p>
	 *
	 * @return a {@link java.lang.StringBuffer} object.
	 */
	public StringBuffer serializeRule() {
		final StringBuffer sb = new StringBuffer();
		// check whether there are operatorPanels on the left panel
		final boolean operatorOnTheLeftSide = this.checkForOperators();


		// get Variable
		this.ruleVariableList = this.getRuleVariableList();
		System.out.println("RuleVariableList:");
		for (int i = 0; i < this.ruleVariableList.size(); i++) {
			System.out.println(this.ruleVariableList.get(i).getValue());
		}

		// set VariableList for the ExistsOpertor
//		this.ruleGraphRight.setBoundedVariableList(ruleVariableList);
//		this.ruleGraphLeft.setRuleVariableList(this.ruleVariableList);

		final String serializedRuleLeft = this.visualGraphs.get(0)
				.serializeGraph();

		final String serializedRuleRight = this.visualGraphs.get(1)
				.serializeGraph();

		if ( operatorOnTheLeftSide ) {



			if(!this.ruleVariableList.isEmpty()) {
				sb.append("\n\tForall");
			}

			for ( int i = 0; i < this.ruleVariableList.size(); i++ ) {

				if ( this.ruleVariableList.get(i).isVariable() ) {
					sb.append(" ?" + this.ruleVariableList.get(i).getValue());
				}
			}

			if(!this.ruleVariableList.isEmpty()) {
				sb.append(" (\n");
			}

//			sb.append("\t");
			sb.append(serializedRuleLeft);
			sb.append(" :- ");

		}

//		if( !operatorOnTheLeftSide ) sb.append("\t");

		sb.append(serializedRuleRight);

		if( !operatorOnTheLeftSide ) {
			sb.append("\n");
		}

		if ( operatorOnTheLeftSide ) {

			sb.append("\n");

			if(!this.ruleVariableList.isEmpty()) {
				sb.append(")\n");
			}
		}

		return sb;
	}


	/**
	 * <p>Getter for the field <code>ruleVariableList</code>.</p>
	 *
	 * @return a {@link java.util.LinkedList} object.
	 */
	public LinkedList<Term> getRuleVariableList(){
		final LinkedList<Term> varTerms = this.ruleVariableList;
		varTerms.clear();

		final Component[] comp = this.getVisualGraphs().get(0).getComponents();



		for (int i = 0; i < comp.length; i++) {

			System.out.println("RuleEditorPane.getRuleVariableList():  comp.length "+ comp.length);

			// UnitermOperator
			if( comp[i] instanceof UnitermOperatorPanel ){

				final UnitermOperatorPanel fop = (UnitermOperatorPanel) comp[i];

				for (int j = 0; j < fop.getUnitermOperator().getTerms().size(); j++) {

					if((fop.getUnitermOperator().getTerms().get(j).isVariable()) &&  !this.listContainsElement(varTerms,fop.getUnitermOperator().getTerms().get(j).getValue())){
						varTerms.add(fop.getUnitermOperator().getTerms().get(j));
					}

				}

			} // end FactOperator


			// ListOperator
			if( comp[i] instanceof ListOperatorPanel ){

				final ListOperatorPanel lop = (ListOperatorPanel) comp[i];

				for (int j = 0; j < lop.getListOperator().getTerms().size(); j++) {

					if((lop.getListOperator().getTerms().get(j).isVariable()) &&  !this.listContainsElement(varTerms,lop.getListOperator().getTerms().get(j).getValue())){
						varTerms.add(lop.getListOperator().getTerms().get(j));
					}

				}

			} // end ListOperator


			// FrameOperator
			if( comp[i] instanceof FrameOperatorPanel ){
				System.out.println("EIN Frame!!!");
				final FrameOperatorPanel fop = (FrameOperatorPanel) comp[i];

				for (int j = 0; j < fop.getFrameOperator().getTerms().size(); j++) {

					if((fop.getFrameOperator().getTerms().get(j).isConstant() || fop.getFrameOperator().getTerms().get(j).isVariable()) && !this.listContainsElement(varTerms,fop.getFrameOperator().getTerms().get(j).getValue())){
						varTerms.add(fop.getFrameOperator().getTerms().get(j));
					}

				}

			} // end FrameOperator

			// Container Panel
			if( comp[i] instanceof ContainerPanel ){
				System.out.println("EIN CONTAINER!!!");
				final ContainerPanel cp = (ContainerPanel) comp[i];
				cp.getOperatorContainer().getVariableList(varTerms);

			}//end ContainerPanel


			// Variable
			if( comp[i] instanceof  VariablePanel ){

				final VariablePanel vp = ( VariablePanel ) comp[i];
				final Term term = new Term( vp.getVariableOperator().getVariable() );
				term.setVariable(true);
				varTerms.add(term);


			}// end Variable





		} // end for comp.length


			this.getVariableList(varTerms);



			return this.deleteRedundantVariables(varTerms);

	}


	private LinkedList<Term> deleteRedundantVariables(final LinkedList<Term> varTerms) {

		final LinkedList<Term> tmp = new LinkedList<Term>();

		for (int i = 0; i < varTerms.size(); i++) {
			if (varTerms.get(i).isVariable() && !this.listContainsElement(tmp, varTerms.get(i).getValue()) ) {
				tmp.add(varTerms.get(i));
			}
		}



		return tmp;
	}


	private LinkedList<Term> getVariableList(final LinkedList<Term> varTerms){
//		LinkedList<Term> varTerms = new LinkedList<Term>();


		final Component[] comp = this.getVisualGraphs().get(1).getComponents();



		for (int i = 0; i < comp.length; i++) {

			// UnitermOperator
			if( comp[i] instanceof UnitermOperatorPanel ){

				final UnitermOperatorPanel fop = (UnitermOperatorPanel) comp[i];

				for (int j = 0; j < fop.getUnitermOperator().getTerms().size(); j++) {

					if(fop.getUnitermOperator().getTerms().get(j).isVariable() && !this.listContainsElement(varTerms,fop.getUnitermOperator().getTerms().get(j).getValue())){
						varTerms.add(fop.getUnitermOperator().getTerms().get(j));
					}

				}

			} // end FactOperator


			// ListOperator
			if( comp[i] instanceof ListOperatorPanel ){

				final ListOperatorPanel lop = (ListOperatorPanel) comp[i];

				for (int j = 0; j < lop.getListOperator().getTerms().size(); j++) {

					if(lop.getListOperator().getTerms().get(j).isVariable() && !this.listContainsElement(varTerms,lop.getListOperator().getTerms().get(j).getValue())){
						varTerms.add(lop.getListOperator().getTerms().get(j));
					}

				}

			} // end ListOperator


			// FrameOperator
			if( comp[i] instanceof FrameOperatorPanel ){

				final FrameOperatorPanel fop = (FrameOperatorPanel) comp[i];

				for (int j = 0; j < fop.getFrameOperator().getTerms().size(); j++) {

					if(fop.getFrameOperator().getTerms().get(j).isVariable() && !this.listContainsElement(varTerms,fop.getFrameOperator().getTerms().get(j).getValue())){
						varTerms.add(fop.getFrameOperator().getTerms().get(j));
					}

				}

			} // end FrameOperator

			// Container Panel
			if( comp[i] instanceof ContainerPanel ){

				final ContainerPanel cp = (ContainerPanel) comp[i];
				cp.getOperatorContainer().getVariableList(varTerms);

			}//end ContainerPanel


			// Variable
			if( comp[i] instanceof  VariablePanel ){

				final VariablePanel vp = ( VariablePanel ) comp[i];
				final Term term = new Term( vp.getVariableOperator().getVariable() );
				term.setVariable(true);
				varTerms.add(term);


			}// end Variable





		} // end for comp.length




			return varTerms;


	}

	/**
	 *
	 * @param varTerms
	 * @param value
	 * @return returns true if the List contains the String
	 */
	private boolean listContainsElement(final LinkedList<Term> varTerms, final String value) {
		if(varTerms != null){
		for (int i = 0; i < varTerms.size(); i++) {
			if (varTerms.get(i).isVariable() && varTerms.get(i).getValue().equals(value)) {
				return true;
			}
		}
		}
		return false;
	}


	private boolean checkForOperators() {
		if(this.visualGraphs.get(0).getComponents().length > 0) {
			return true;
		}
		return false;
	}


	// Rif -> VisualRif
	/**
	 * <p>evaluate.</p>
	 *
	 * @param unVisitedObject a {@link lupos.rif.model.Rule} object.
	 * @param vrg a {@link lupos.gui.operatorgraph.visualeditor.visualrif.parsing.VisualRifGenerator} object.
	 */
	public void evaluate(final Rule unVisitedObject, final VisualRifGenerator vrg) {


		this.ruleGraphLeft.removeAll();
		this.ruleGraphRight.removeAll();


		final LinkedList<GraphWrapper> rootListLeft = new LinkedList<GraphWrapper>();
		final LinkedList<GraphWrapper> rootListRight = new LinkedList<GraphWrapper>();

		try {


				Object left = unVisitedObject.getHead().accept(vrg, this.ruleGraphLeft);

				// linke Seite hat nur eine Variable
				if (left instanceof String){
					final VariableOperator varOp = new VariableOperator();
					varOp.setVariable((String)left);
					left = varOp;
				}

				// linke Seite hat nur eine Konstante
				if (left instanceof String[]){
					final ConstantOperator constOp = new ConstantOperator();
					constOp.setVisualRifEditor(this.visualRifEditor);
					final String[] prefConst = (String[]) left;
					final String[] constantArray = new String[prefConst.length-2];

					for (int i = 0; i < constantArray.length; i++) {
						constantArray[i] = prefConst[i+2];
					}

					constOp.setComboBoxEntries(constantArray);
					constOp.setSelectedPrefix(prefConst[0]);
					constOp.getConstantComboBox().setSelectedItem(prefConst[0]);
					constOp.setConstant(prefConst[1]);
					left = constOp;
				}


				Object right = unVisitedObject.getBody().accept(vrg, this.ruleGraphRight);

				if (right instanceof String){
					final VariableOperator varOp = new VariableOperator();
					varOp.setVariable((String)right);
					right = varOp;
				}
				if (right instanceof String[]){
					final ConstantOperator constOp = new ConstantOperator();
					constOp.setVisualRifEditor(this.visualRifEditor);
					final String[] prefConst = (String[]) right;
					final String[] constantArray = new String[prefConst.length-2];

					for (int i = 0; i < constantArray.length; i++) {
						constantArray[i] = prefConst[i+2];
					}

					constOp.setComboBoxEntries(constantArray);
					constOp.setSelectedPrefix(prefConst[0]);
					constOp.getConstantComboBox().setSelectedItem(prefConst[0]);
					constOp.setConstant(prefConst[1]);
					right = constOp;
				}
				// List
				if ( left instanceof AbstractTermOperator ){
					final AbstractTermOperator abstractTermOperator = (AbstractTermOperator) left;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(abstractTermOperator);
					rootListLeft.add(gwo);
				}

				if ( right instanceof AbstractTermOperator ){
					final AbstractTermOperator abstractTermOperator = (AbstractTermOperator) right;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(abstractTermOperator);
					rootListRight.add(gwo);
				}
//				// Uniterm
//				if ( left instanceof UnitermOperator ){
//					UnitermOperator unitermOperator = (UnitermOperator) left;
//					GraphWrapperOperator gwo = new GraphWrapperOperator(unitermOperator);
//					rootListLeft.add(gwo);
//				}
//
//				if ( right instanceof UnitermOperator ){
//					UnitermOperator unitermOperator = (UnitermOperator) right;
//					GraphWrapperOperator gwo = new GraphWrapperOperator(unitermOperator);
//					rootListRight.add(gwo);
//				}
//
//				// Frame
//				if ( left instanceof FrameOperator ){
//					FrameOperator frameOperator = (FrameOperator) left;
//					GraphWrapperOperator gwo = new GraphWrapperOperator(frameOperator);
//					rootListLeft.add(gwo);
//				}
//
//				if ( right instanceof FrameOperator ){
//					FrameOperator frameOperator = (FrameOperator) right;
//					GraphWrapperOperator gwo = new GraphWrapperOperator(frameOperator);
//					rootListRight.add(gwo);
//				}
//				// List
//				if ( left instanceof ListOperator ){
//					ListOperator listOperator = (ListOperator) left;
//					GraphWrapperOperator gwo = new GraphWrapperOperator(listOperator);
//					rootListLeft.add(gwo);
//				}
//
//				if ( right instanceof ListOperator ){
//					ListOperator listOperator = (ListOperator) right;
//					GraphWrapperOperator gwo = new GraphWrapperOperator(listOperator);
//					rootListRight.add(gwo);
//				}

				// And
				if ( left instanceof AndContainer ){
					final AndContainer andContainer = (AndContainer) left;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(andContainer);
					rootListLeft.add(gwo);
				}

				if ( right instanceof AndContainer ){
					final AndContainer andContainer = (AndContainer) right;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(andContainer);
					rootListRight.add(gwo);
				}

				// OR
				if ( right instanceof OrContainer ){
					final OrContainer orContainer = (OrContainer) right;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(orContainer);
					rootListRight.add(gwo);
				}

				// Exists
				if ( right instanceof ExistsContainer ){
					final ExistsContainer existsContainer = (ExistsContainer) right;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(existsContainer);
					rootListRight.add(gwo);
				}


				// Variable
				if ( left instanceof VariableOperator ){
					final VariableOperator variableOperator = (VariableOperator) left;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(variableOperator);
					rootListLeft.add(gwo);
				}

				if ( right instanceof VariableOperator ){
					final VariableOperator variableOperator = (VariableOperator) right;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(variableOperator);
					rootListRight.add(gwo);
				}

				// Constant
				if ( left instanceof ConstantOperator ){
					final ConstantOperator constantOperator = (ConstantOperator) left;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(constantOperator);
					rootListLeft.add(gwo);
				}

				if ( right instanceof ConstantOperator ){
					final ConstantOperator constantOperator = (ConstantOperator) right;
					final GraphWrapperOperator gwo = new GraphWrapperOperator(constantOperator);
					rootListRight.add(gwo);
				}

			// generate Left QueryGraph...
			final JPanel graphPanelLeft = this.visualGraphs.get(0).createGraph(
					rootListLeft,
					Arrange.values()[0]);

			this.visualGraphs.get(0).updateMainPanel(graphPanelLeft);

			// generate Right QueryGraph...
			final JPanel graphPanelRight = this.visualGraphs.get(1).createGraph(
					rootListRight,
					Arrange.values()[0]);

			this.visualGraphs.get(0).updateMainPanel(graphPanelLeft);
			this.visualGraphs.get(1).updateMainPanel(graphPanelRight);


		} catch (final Throwable e) {

			this.statusBar.clear();

			e.printStackTrace();


		}

	}

	// JSON
	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 */
	public JSONObject toJSON() {
		final JSONObject saveObject = new JSONObject();
			try {
				saveObject.put("RULEGRAPH_LEFT", this.ruleGraphLeft.toJSON());
				saveObject.put("RULEGRAPH_RIGHT", this.ruleGraphRight.toJSON());
			} catch (final JSONException e) {
				e.printStackTrace();
			}

		return saveObject;
	}

	/**
	 * <p>fromJSON.</p>
	 *
	 * @param jsonObject a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public void fromJSON(final JSONObject jsonObject)  throws JSONException {


		final JSONObject loadRuleGraphLeft = jsonObject.getJSONObject("RULEGRAPH_LEFT");
		final JSONObject loadRuleGraphRight = jsonObject.getJSONObject("RULEGRAPH_RIGHT");


		this.ruleGraphLeft.fromJSON(loadRuleGraphLeft);
		this.ruleGraphRight.fromJSON(loadRuleGraphRight);
	}


	/**
	 * <p>buildBottomPane.</p>
	 *
	 * @return a {@link javax.swing.JTabbedPane} object.
	 */
	public JTabbedPane buildBottomPane(){

		this.rifCodeEditor = new RifCodeEditor();

		final JTabbedPane bottomPane = new JTabbedPane();
		final JButton buttonRifCode = new JButton("Rif Code");

		final ActionListener alRifCodeEditor = new ActionListener() {
	      @Override
		public void actionPerformed(final ActionEvent ae) {
	       final StringBuffer rule = RuleEditorPane.this.serializeRule();
	       RuleEditorPane.this.rifCodeEditor.getTp_rifInput().setText(rule.toString());
	      }
	    };
	    buttonRifCode.addActionListener(alRifCodeEditor);


		final JPanel panelRifCodeEditor = new JPanel();
		panelRifCodeEditor.setOpaque(false);
		panelRifCodeEditor.add(buttonRifCode);

		bottomPane.add("RIF Code",this.rifCodeEditor.getRifInputSP());
		bottomPane.setTabComponentAt(bottomPane.getTabCount()-1, panelRifCodeEditor);
//		bottomPane.add("Console",this.console.getScrollPane());
		return bottomPane;
	}


	/* *************** **
	 * Getter + Setter **
	 * *************** */


	/**
	 * <p>Getter for the field <code>ruleGraphLeft</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 */
	public RuleGraph getRuleGraphLeft() {
		return this.ruleGraphLeft;
	}

	/**
	 * <p>Setter for the field <code>ruleGraphLeft</code>.</p>
	 *
	 * @param ruleGraphLeft a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 */
	public void setRuleGraphLeft(final RuleGraph ruleGraphLeft) {
		this.ruleGraphLeft = ruleGraphLeft;
	}

	/**
	 * <p>Getter for the field <code>ruleGraphRight</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 */
	public RuleGraph getRuleGraphRight() {
		return this.ruleGraphRight;
	}

	/**
	 * <p>Setter for the field <code>ruleGraphRight</code>.</p>
	 *
	 * @param ruleGraphRight a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 */
	public void setRuleGraphRight(final RuleGraph ruleGraphRight) {
		this.ruleGraphRight = ruleGraphRight;
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
	 * <p>Getter for the field <code>that</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.RuleEditorPane} object.
	 */
	public RuleEditorPane getThat() {
		return this.that;
	}

	/**
	 * <p>Getter for the field <code>componentCnt</code>.</p>
	 *
	 * @return a int.
	 */
	public int getComponentCnt() {
		return this.componentCnt;
	}

	/**
	 * <p>Setter for the field <code>componentCnt</code>.</p>
	 *
	 * @param componentCnt a int.
	 */
	public void setComponentCnt(final int componentCnt) {
		this.componentCnt = componentCnt;
	}
















}
