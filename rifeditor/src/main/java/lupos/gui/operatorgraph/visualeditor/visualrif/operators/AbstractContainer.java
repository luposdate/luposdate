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
package lupos.gui.operatorgraph.visualeditor.visualrif.operators;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.util.HashSet;

import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import lupos.gui.operatorgraph.arrange.Arrange;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.operators.OperatorContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.ContainerVisualEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ContainerPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.FrameOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ListOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.UnitermOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.VariablePanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;

import org.json.JSONException;
import org.json.JSONObject;




public abstract class AbstractContainer extends OperatorContainer {
	
	protected VisualRifEditor visualRifEditor;
	protected RuleGraph recursiveOperatorGraph;
	private ContainerVisualEditor containerVisualEditor = new ContainerVisualEditor(false);
	protected JSONObject loadOperatorGraph;
	

	

	public AbstractContainer() {
	} // needed for insertOperator()...

	public void addOperator(final Operator op) {
		this.getOperators().add(op);
//		containerVisualEditor.getVisualGraphs().get(0).add(op);
		this.determineRootNodes();
	}

	protected AbstractGuiComponent<Operator> drawPanel(final GraphWrapper gw, final RuleGraph parent, final Color bgColor, final String title) {
		
		recursiveOperatorGraph = new RuleGraph(parent.getVisualEditor(),this.visualRifEditor,true);

		parent.addChildComponent(recursiveOperatorGraph);
		
		this.initRecursiveOperatorGraph(parent);

		
		if(this.loadOperatorGraph != null){
			try {
				this.recursiveOperatorGraph.fromJSON(this.loadOperatorGraph);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		final JPanel panel = recursiveOperatorGraph.createGraph(
				gw.getContainerElements(),
				Arrange.values()[0]);

		this.panel = new ContainerPanel(this, gw, panel, recursiveOperatorGraph, parent, visualRifEditor);
		
		// Border
		LineBorder lineBorder = new LineBorder(bgColor,5);
		TitledBorder titled;
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, 20);
		titled = BorderFactory.createTitledBorder(lineBorder," "+title+" ", 0, 0, font, Color.BLACK);
        this.panel.setBorder(titled);

		if(this.getOperators().size() == 0) {
			this.panel.setPreferredSize(new Dimension(150, 100));

			panel.setPreferredSize(new Dimension(150 - 14, 100 - 2));
		}
		else
			this.panel.setPreferredSize(this.panel.getPreferredSize());

//		this.recursiveOperatorGraph.setAbstractContainer(this);
		
		return this.panel;
	}

	
	public void initRecursiveOperatorGraph(final RuleGraph parent) {
		
		System.out.println("AbstractContainer.initRecursiveOperatorGraph(final RuleGraph parent) NullTest:" +" "+ (this.containerVisualEditor == null) +" "+(this.recursiveOperatorGraph==null));
		
		this.containerVisualEditor.getVisualGraphs().add(this.recursiveOperatorGraph);
		this.recursiveOperatorGraph.setRecursiveOperatorGraph(true);
		this.recursiveOperatorGraph.setOperatorContainer(this);
		
	
	}



	// TODO!!!
	public LinkedList<Term> getVariableList(LinkedList<Term> varTerms){
//		LinkedList<Term> varTerms = new LinkedList<Term>();
		System.out.println("AbstractContainer.getVariableList() NullTest:" +" "+ (this.containerVisualEditor == null) +" "+(this.recursiveOperatorGraph==null));

		System.out.println("AbstractContainer.getVariableList()"+containerVisualEditor.getVisualGraphs().get(0).getComponents().length);
//		Component[] comp = containerVisualEditor.getVisualGraphs().get(0).getComponents();
		Component[] comp = this.recursiveOperatorGraph.getVisualGraph().getComponents();
		

	
		for (int i = 0; i < comp.length; i++) {
			
			// UnitermOperator
			if( comp[i] instanceof UnitermOperatorPanel ){
				
				UnitermOperatorPanel fop = (UnitermOperatorPanel) comp[i];
				
				for (int j = 0; j < fop.getUnitermOperator().getTerms().size(); j++) {
					
					if(!listContainsElement(varTerms,fop.getUnitermOperator().getTerms().get(j).getValue())){
						varTerms.add(fop.getUnitermOperator().getTerms().get(j));
					}
					
				}
				
			} // end UnitermOperator
			
			
			// ListOperator
			if( comp[i] instanceof ListOperatorPanel ){
				
				ListOperatorPanel lop = (ListOperatorPanel) comp[i];
				
				for (int j = 0; j < lop.getListOperator().getTerms().size(); j++) {
					
					if(!listContainsElement(varTerms,lop.getListOperator().getTerms().get(j).getValue())){
						varTerms.add(lop.getListOperator().getTerms().get(j));
					}
					
				}
				
			} // end ListOperator
			
			
			// FrameOperator
			if( comp[i] instanceof FrameOperatorPanel ){
				
				FrameOperatorPanel fop = (FrameOperatorPanel) comp[i];
				
				for (int j = 0; j < fop.getFrameOperator().getTerms().size(); j++) {
					
					if(!listContainsElement(varTerms,fop.getFrameOperator().getTerms().get(j).getValue())){
						varTerms.add(fop.getFrameOperator().getTerms().get(j));
					}
					
				}
				
			} // end FrameOperator
			
			// Container Panel
			if( comp[i] instanceof ContainerPanel ){
				
				ContainerPanel cp = (ContainerPanel) comp[i];
				cp.getOperatorContainer().getVariableList(varTerms);
				
			}//end ContainerPanel
		
		
			// Variable
			if( comp[i] instanceof  VariablePanel ){
			
				VariablePanel vp = ( VariablePanel ) comp[i];
				Term term = new Term( vp.getVariableOperator().getVariable() );
				term.setVariable(true);
				varTerms.add(term);
				
				
			}// end Variable
			
			
			
			
			
		} // end for comp.length

		


		return this.deleteRedundantVariables(varTerms);
		
	}
	
	protected LinkedList<Term> deleteRedundantVariables(LinkedList<Term> varTerms) {

		LinkedList<Term> tmp = new LinkedList<Term>();
	
		for (int i = 0; i < varTerms.size(); i++) {
			if (varTerms.get(i).isVariable() && !this.listContainsElement(tmp, varTerms.get(i).getValue()) ) {
				tmp.add(varTerms.get(i));
			}
		}
		
		
		
		return tmp;
	}
	
	/**
	 * 
	 * @param varTerms
	 * @param value
	 * @return returns true if the List contains the String
	 */
	protected boolean listContainsElement(LinkedList<Term> varTerms, String value) {
		for (int i = 0; i < varTerms.size(); i++) {
			if (varTerms.get(i).isVariable() && varTerms.get(i).getValue().equals(value)) return true;
		}
		return false;
	}

	public abstract StringBuffer serializeOperator();

	
	public abstract StringBuffer serializeOperatorAndTree(HashSet<Operator> visited);


	
	public JSONObject toJSON() {
		JSONObject saveObject = new JSONObject();
		
		try {
			
			if( this instanceof AndContainer ) saveObject.put("OP TYPE", "and");
			if( this instanceof OrContainer ) saveObject.put("OP TYPE", "or");
			if( this instanceof ExistsContainer )  saveObject.put("OP TYPE", "exists");
			
			Point position = ((ContainerPanel) this.panel).getPositionAndDimension().getFirst();

			saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
			
	
			
			saveObject.put("OPERATORGRAPH", this.recursiveOperatorGraph.toJSON());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		return saveObject;
	}
	
	public void fromJSON(JSONObject loadOperatorGraph) throws JSONException{
		this.loadOperatorGraph = loadOperatorGraph;
	}
	
	/*
	 * Getter + Setter
	 */
	
	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}

	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	public RuleGraph getRecursiveOperatorGraph() {
		return recursiveOperatorGraph;
	}

	public void setRecursiveOperatorGraph(RuleGraph recursiveOperatorGraph) {
		this.recursiveOperatorGraph = recursiveOperatorGraph;
	}
	
}