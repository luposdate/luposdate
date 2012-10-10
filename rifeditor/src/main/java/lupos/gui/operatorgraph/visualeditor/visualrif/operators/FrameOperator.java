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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JComboBox;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.FrameOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.misc.util.OperatorIDTuple;

public class FrameOperator extends AbstractTermOperator {





	private String[] termTypArray = {"Var","Var","Var"};
	


	@Override
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw,
			VisualGraph<Operator> parent) {
		this.panel = new FrameOperatorPanel(parent, gw, this,
				 this.startNode,
				this.alsoSubClasses, this.visualRifEditor);
		return this.panel;
	}


	@Override
	public StringBuffer serializeOperator() {
	
		StringBuffer sb = new StringBuffer("");
		if (this.isChild) {

			
				if (this.terms.get(0).isVariable()) {
		
					sb.append("?" + this.terms.get(0).getValue() + " ");
				} else 
					if (this.terms.get(0).getSelectedPrefix().equals("BASE")) {
					
					sb.append("<" + this.terms.get(0).getValue() + "> ");
					
				} else
					
					if (this.terms.get(0).isConstant()){
						if ( this.terms.get(0).getSelectedPrefix().equals("integer") ){
							sb.append(this.terms.get(0).getValue());
						}else
						if ( this.terms.get(0).getSelectedPrefix().endsWith("#string") || this.terms.get(0).getSelectedPrefix().endsWith("#integer") ){
							String[] tmp = new String[2];
							tmp = this.terms.get(0).getSelectedPrefix().split("#");
										String iri = "\""+this.terms.get(0).getValue()+"\"^^"+tmp[0]+":"+tmp[1]+" ";

							
							sb.append(iri);
						}else
						sb.append(this.terms.get(0).getSelectedPrefix() + ":"+ this.terms.get(0).getValue() + " ");
					}
			

			sb.append("[ ");
			int cnt =0;
			for (int i = 1; i < this.terms.size(); i++) {
				if (this.terms.get(i).isVariable()) {
		
					sb.append("?" + this.terms.get(i).getValue() + " ");
					if (cnt % 2 == 0) sb.append("-> ");
				} else 
					if (this.terms.get(i).getSelectedPrefix().equals("BASE")) {
					
					sb.append("<" + this.terms.get(i).getValue() + "> ");
					if (cnt % 2 == 0) sb.append("-> ");
					
				} else{
					
					if (this.terms.get(i).isConstant()){
						if ( this.terms.get(i).getSelectedPrefix().equals("integer") ){
							sb.append(this.terms.get(i).getValue());
						}else
						if ( this.terms.get(i).getSelectedPrefix().endsWith("#string") || this.terms.get(i).getSelectedPrefix().endsWith("#integer") ){
							String[] tmp = new String[2];
							tmp = this.terms.get(i).getSelectedPrefix().split("#");
										String iri = "\""+this.terms.get(0).getValue()+"\"^^"+tmp[0]+":"+tmp[1]+" ";

							
							sb.append(iri);
						}else
						sb.append(this.terms.get(i).getSelectedPrefix() + ":" + this.terms.get(i).getValue() + " ");
					}
					if (cnt % 2 == 0) sb.append("-> ");
				}
				cnt += 1;
			}
			sb.append("]");

		}
		return sb;
	}

	
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer sb = new StringBuffer("");
		

		
		if(!this.isChild){
		
			// Variable
			if (this.terms.get(0).isVariable()) {
				sb.append("?" + this.terms.get(0).getValue() + " ");
			} 
				
			// Constant
			if (this.terms.get(0).isConstant()){
				if ( this.terms.get(0).getSelectedPrefix().endsWith("#string") || this.terms.get(0).getSelectedPrefix().endsWith("#integer") ){
					String[] tmp = new String[2];
					tmp = this.terms.get(0).getSelectedPrefix().split("#");
								String iri = "\""+this.terms.get(0).getValue()+"\"^^"+tmp[0]+":"+tmp[1]+" ";

					
					sb.append(iri);
				}else
				sb.append(this.terms.get(0).getSelectedPrefix() + ":"+ this.terms.get(0).getValue() + " ");
			}
			
			// Uniterm
			if (this.terms.get(0).isUniterm()){
				sb.append(this.terms.get(0).getAbstractTermOperator().serializeOperator());
			}
			
			// List
			if (this.terms.get(0).isList()){
				sb.append(this.terms.get(0).getAbstractTermOperator().serializeOperator());
			}
			

		sb.append("[ ");
		
		// counter for arrow 
		int cnt = 0;

		
		
		for (int i = 1; i < this.terms.size(); i++) {
			
			// Variable
			if (this.terms.get(i).isVariable()) {
			
				sb.append("?" + this.terms.get(i).getValue() + " ");
				if (cnt % 2 == 0) sb.append("-> ");
			} 
			
			// Constant BASE
			if (this.terms.get(i).getSelectedPrefix().equals("BASE")) {
				sb.append("<" + this.terms.get(i).getValue() + "> ");
				if (cnt % 2 == 0) sb.append("-> ");
			} 
			
			
			// Constant
			if ( this.terms.get(i).isConstant() ){
				if ( this.terms.get(i).getSelectedPrefix().endsWith("#string") || this.terms.get(i).getSelectedPrefix().endsWith("#integer") ){
					String[] tmp = new String[2];
					tmp = this.terms.get(i).getSelectedPrefix().split("#");
								String iri = "\""+this.terms.get(i).getValue()+"\"^^"+tmp[0]+":"+tmp[1]+" ";

					
					sb.append(iri);
				}else
				sb.append(this.terms.get(i).getSelectedPrefix() + ":"
						+ this.terms.get(i).getValue() + " ");
				if (cnt % 2 == 0) sb.append("-> ");
			}
			
			// Uniterm
			if ( this.terms.get(i).isUniterm() && !(this.terms.get(i).getAbstractTermOperator()== null)){

				sb.append(this.terms.get(i).getAbstractTermOperator().serializeOperator());
			}
		
			// List
			if (this.terms.get(i).isList()){
				sb.append(this.terms.get(i).getAbstractTermOperator().serializeOperator());
			}
		
			cnt += 1;
		}
		sb.append("]");

		
		if (!this.isChild && (this.getSucceedingOperators().size() != 0)) {
			
			for (OperatorIDTuple<Operator> opIDT : this
					.getSucceedingOperators()) {
				if (opIDT.getOperator() instanceof ConstantOperator || opIDT.getOperator() instanceof VariableOperator){
				sb.append(" " + this.selectedClassification + " ");
				sb.append(opIDT.getOperator().serializeOperator());
				}
			}
		}
		}
		return sb;
	}


	
	public JComboBox getNextTermCombo() {
		return nextTermCombo;
	}


	public void setNextTermCombo(JComboBox nextTermCombo) {
		this.nextTermCombo = nextTermCombo;
	}


	public String[] getTermTypArray() {
		return termTypArray;
	}


	public void setTermTypArray(String[] termTypArray) {
		this.termTypArray = termTypArray;
	}


	
	public void sortTermsByTermFrameID() {
		
		HashMap<Integer,Term> unsortedTerms = new HashMap<Integer,Term>();
		
		for (int i = 0; i < this.terms.size(); i++) {
			unsortedTerms.put(this.terms.get(i).getTermFrameID(),this.terms.get(i));
		}
		
		this.terms.clear();
		
		TreeMap<Integer, Term> treeMap = new TreeMap<Integer, Term>();
				
		treeMap.putAll(unsortedTerms);
		
		for (Entry<Integer, Term> entry : treeMap.entrySet()) {
			terms.add(entry.getValue());
		}
	
		
	
	}



	
	
	}