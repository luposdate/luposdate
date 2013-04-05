/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import java.util.HashSet;
import java.util.LinkedList;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;

public class ExistsContainer extends AbstractContainer {

	
	private LinkedList<Term> existsVariableList;
	private LinkedList<Term> boundedVariables = new LinkedList<Term>();
	
	// Constructor
	public ExistsContainer() {}

	public StringBuffer serializeOperator() {
		final StringBuffer ret = new StringBuffer();
		for(final Operator op : this.getOperators()) {
			ret.append(op.serializeOperatorAndTree(new HashSet<Operator>()));
		}

		return ret;
	}

	@Override
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		final StringBuffer ret = new StringBuffer();
		
		this.existsVariableList = super.getVariableList(new LinkedList<Term>());
//		if ( this.getParentRuleGraph() != null && this.getParentRuleGraph().getRuleVariableList() != null){
//			this.boundedVariableList = this.getParentRuleGraph().getBoundedVariableList();
//	
//		
//			System.out.println("FORALL_LIST:");
//			for (int i = 0; i < this.boundedVariableList.size(); i++) {
//				System.out.println(this.boundedVariableList.get(i).getValue());
//			}
//			System.out.println("EXISTS_LIST:");
//			for (int i = 0; i < this.existsVariableList.size(); i++) {
//				System.out.println(this.existsVariableList.get(i).getValue());
//			}
//			
//	
		this.existsVariableList = this.deleteRedundantVariables(existsVariableList, boundedVariables);
//		this.setRuleVariableListForRecrusiveRuleGraph(this.boundedVariableList,this.existsVariableList);	
		
//		}
		ret.append("\nExists ");
		
		for (int i = 0; i < this.existsVariableList.size(); i++) {
			if ( this.existsVariableList.get(i).isVariable() )
			ret.append("?"+this.existsVariableList.get(i).getValue()+" ");
		}
		
		ret.append("(\n");
		visited = new HashSet<Operator>();

		for(final Operator op : this.getOperators()){
			ret.append(op.serializeOperatorAndTree(visited));
			ret.append("\n");
		}	
		ret.append(")\n");
		return ret;
	}

	


	protected LinkedList<Term> deleteRedundantVariables(LinkedList<Term> varTerms, LinkedList<Term> boundedVariableList2) {

		LinkedList<Term> tmp = new LinkedList<Term>();
	
		for (int i = 0; i < varTerms.size(); i++) {
			if (varTerms.get(i).isVariable() && !this.listContainsElement(tmp, varTerms.get(i).getValue())
					 && !this.listContainsElement(boundedVariableList2, varTerms.get(i).getValue())) {
				tmp.add(varTerms.get(i));
			}
		}
		
		
		
		return tmp;
	}
	


	public LinkedList<Term> getVariableList(LinkedList<Term> varTerms){
		this.boundedVariables = varTerms;
		return varTerms;
	}
	
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw,
			VisualGraph<Operator> parent) {
		return this.drawPanel(gw, (RuleGraph) parent, Color.red, "Exists");
	}

	public LinkedList<Term> getExistsVariableList() {
		return existsVariableList;
	}

	public void setExistsVariableList(LinkedList<Term> existsVariableList) {
		this.existsVariableList = existsVariableList;
	}

}
