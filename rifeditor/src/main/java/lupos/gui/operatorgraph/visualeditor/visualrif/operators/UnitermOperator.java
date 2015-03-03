
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.visualrif.operators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.UnitermOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.misc.util.OperatorIDTuple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class UnitermOperator  extends AbstractTermOperator {

	


	


	/** {@inheritDoc} */
	@Override
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw,
			VisualGraph<Operator> parent) {
		this.panel = new UnitermOperatorPanel(parent, gw, this,
				 this.startNode,
				this.alsoSubClasses,visualRifEditor);
		return this.panel;
	}


	/** {@inheritDoc} */
	@Override
	public StringBuffer serializeOperator() {
	
		StringBuffer sb = new StringBuffer("");
		if (!this.isChild) {
			if (this.external)
				sb.append("External(");

			if (this.selectedPrefix.equals("BASE")) {

				sb.append(":"+this.termName);

			} else {
				if ( this.selectedPrefix.equals("integer") ){
					sb.append(this.termName);
				}else
				if ( this.selectedPrefix.endsWith("#string") || this.selectedPrefix.endsWith("#integer") ){
					String[] tmp = new String[2];
					tmp = this.selectedPrefix.split("#");
								String iri = "\""+this.termName+"\"^^"+tmp[0]+":"+tmp[1]+" ";

					
					sb.append(iri);
				}else

				sb.append(this.selectedPrefix + ":" + this.termName);

			}

			sb.append("( ");

			for (int i = 0; i < this.terms.size(); i++) {
				
				// Variable
				if (this.terms.get(i).isVariable()) {
					if (this.isNamed()) sb.append(this.terms.get(i).getPrefixForName() + ":"
								+ this.terms.get(i).getTfName().getText()
								+ " -> ");
					sb.append("?" + this.terms.get(i).getValue() + " ");
				} 
				
				// Constant Base
				if (this.terms.get(i).getSelectedPrefix().equals("BASE")) {
					if (this.isNamed())
						sb.append(this.terms.get(i).getPrefixForName() + ":"
								+ this.terms.get(i).getTfName().getText()
								+ " -> ");
					sb.append("<" + this.terms.get(i).getValue() + "> ");
				} else if (!this.terms.get(i).getSelectedPrefix()
						.equals("BASE")) {
					
					if (this.isNamed()) sb.append(this.terms.get(i).getPrefixForName() + ":"
								+ this.terms.get(i).getTfName().getText()
								+ " -> ");
					
					// Constant 
					if (this.terms.get(i).isConstant()){
						if ( this.terms.get(i).getSelectedPrefix().equals("integer") ){
							sb.append(this.terms.get(i).getValue());
						}else
						if ( this.terms.get(i).getSelectedPrefix().endsWith("#string") || this.terms.get(i).getSelectedPrefix().endsWith("#integer") ){
							String[] tmp = new String[2];
							tmp = this.terms.get(i).getSelectedPrefix().split("#");
										String iri = "\""+this.terms.get(i).getValue()+"\"^^"+tmp[0]+":"+tmp[1]+" ";

							
							sb.append(iri);
						}else
						sb.append(this.terms.get(i).getSelectedPrefix() + ":"
								+ this.terms.get(i).getValue() + " ");
					}
					
					
					// Uniterm
					if ( this.terms.get(i).isUniterm() ){
						System.out.println("UnitermOperator.serilizeOperator: Uniterm");
						sb.append(this.terms.get(i).getAbstractTermOperator().serializeOperator());
					}
				
					// List 
					if (this.terms.get(i).isList()){
						sb.append(this.terms.get(i).getAbstractTermOperator().serializeOperator());
					}
								
				}
			}
			sb.append(")");
			if (this.external)
				sb.append(")");
		}
		return sb;
	}

	
	/** {@inheritDoc} */
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer sb = new StringBuffer("");

		if (!this.isChild) {

			if (this.external)
				sb.append("External(");

			if (this.selectedPrefix.equals("BASE")) {

				sb.append(":" + this.termName);

			} else {
				if ( this.selectedPrefix.equals("integer") ){
					sb.append(this.termName);
				}else
				if ( this.selectedPrefix.endsWith("#string") || this.selectedPrefix.endsWith("#integer") ){
					String[] tmp = new String[2];
					tmp = this.selectedPrefix.split("#");
								String iri = "\""+this.termName+"\"^^"+tmp[0]+":"+tmp[1]+" ";

					
					sb.append(iri);
				}else
				sb.append(this.selectedPrefix + ":" + this.termName);

			}

			sb.append("( ");

			for (int i = 0; i < this.terms.size(); i++) {
				
				// Variable
				if (this.terms.get(i).isVariable()) {
					if (this.isNamed()) sb.append(this.terms.get(i).getPrefixForName() + ":"
								+ this.terms.get(i).getTfName().getText()
								+ " -> ");
					sb.append("?" + this.terms.get(i).getValue() + " ");
				} 
				
				// Constant Base
				if (this.terms.get(i).getSelectedPrefix().equals("BASE")) {
					if (this.isNamed())
						sb.append(this.terms.get(i).getPrefixForName() + ":"
								+ this.terms.get(i).getTfName().getText()
								+ " -> ");
					sb.append("<" + this.terms.get(i).getValue() + "> ");
				} else if (!this.terms.get(i).getSelectedPrefix()
						.equals("BASE")) {
					
					if (this.isNamed()) sb.append(this.terms.get(i).getPrefixForName() + ":"
								+ this.terms.get(i).getTfName().getText()
								+ " -> ");
					
					// Constant 
					if (this.terms.get(i).isConstant()){
						if ( this.terms.get(i).getSelectedPrefix().equals("integer") ){
							sb.append(this.terms.get(i).getValue());
						}else
						if ( this.terms.get(i).getSelectedPrefix().endsWith("#string") || this.terms.get(i).getSelectedPrefix().endsWith("#integer") ){
							String[] tmp = new String[2];
							tmp = this.terms.get(i).getSelectedPrefix().split("#");
										String iri = "\""+this.terms.get(i).getValue()+"\"^^"+tmp[0]+":"+tmp[1]+" ";

							
							sb.append(iri);
						}else
						sb.append(this.terms.get(i).getSelectedPrefix() + ":"
								+ this.terms.get(i).getValue() + " ");
					}
					
					
					// Uniterm
					if ( this.terms.get(i).isUniterm() ){

						sb.append(this.terms.get(i).getAbstractTermOperator().serializeOperator());
					}
				
					// List 
					if (this.terms.get(i).isList()){
						sb.append(this.terms.get(i).getAbstractTermOperator().serializeOperator());
					}
								
				}
			}
			sb.append(")");
			if (this.external)
				sb.append(")");

//			if (!this.isChild && this.getSucceedingOperators().size() != 0) {
//				sb.append(" " + this.selectedClassification + " ");
//				for (OperatorIDTuple<Operator> opIDT : this
//						.getSucceedingOperators()) {
//					sb.append(opIDT.getOperator().serializeOperator());
//
//				}
//			}
			
			if (!this.isChild && this.getSucceedingElementsWithoutTermSucceedingElements().size() != 0) {
				sb.append(" " + this.selectedClassification + " ");
				for (int j = 0; j < this.getSucceedingElementsWithoutTermSucceedingElements().size(); j++) {
					sb.append(this.getSucceedingElementsWithoutTermSucceedingElements().get(j).serializeOperator());
				}
			}
			
		}
		return sb;
	}


	
	/**
	 * <p>fromJSON.</p>
	 *
	 * @param operatorObject a {@link org.json.JSONObject} object.
	 * @param unitermOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator} object.
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph} object.
	 * @throws org.json.JSONException if any.
	 */
	public void fromJSON(JSONObject operatorObject, UnitermOperator unitermOperator,RuleGraph parent) throws JSONException {
		
		this.setVisualRifEditor(parent.getVisualRifEditor());
		
		boolean isConnected = operatorObject.getBoolean("ISCONNECTED");
		
		if (isConnected) {
			JSONObject loadObject = new JSONObject();
			loadObject = (JSONObject) operatorObject.get("CONNECTEDOPERATOR");
			unitermOperator.setSelectedClassification((String)operatorObject.get("SELECTEDCLASSIFICTION"));
			
		if (unitermOperator.getSelectedClassification().equals("=")){
							boolean[] equality = {true,false,false};
							unitermOperator.setSelectedRadioButton(equality);
	
		}else
	
			if (unitermOperator.getSelectedClassification().equals("#")){
							boolean[] membership = {false,true,false};
							unitermOperator.setSelectedRadioButton(membership);
			
			}else		
				if (unitermOperator.getSelectedClassification().equals("##")){
							boolean[] subclass = {false,false, true};
							unitermOperator.setSelectedRadioButton(subclass);
				}
			
			
			// Constant
			if ( loadObject.get("OP TYPE").equals("ConstantOperator") ){
				ConstantOperator child = new ConstantOperator();
				child.fromJSON(loadObject, child, parent);
				child.setChild(true);

				OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (child, 0);
				unitermOperator.addSucceedingOperator(oidtConst);
				
				JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1),
					child);

			} // end constant
			
			
			// Variable
			if ( loadObject.get("OP TYPE").equals("VariableOperator") ){
				VariableOperator child = new VariableOperator();
				child.fromJSON(loadObject, child, parent);
				child.setChild(true);

				OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (child, 0);
				unitermOperator.addSucceedingOperator(oidtVar);
				
				JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1),
					child);

			} // end variable
			
			// ListOperator
			if ( loadObject.get("OP TYPE").equals("ListOperator") ){
				ListOperator child = new ListOperator();
				JSONObject termsObject = null;
				
				child.fromJSON(loadObject, child, parent);
				child.setChild(true);
				child.setConstantComboBoxEntries(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
				child.setVisualRifEditor(visualRifEditor);
				child.setOpen(loadObject.getBoolean("ISOPEN"));
		
				if( loadObject.has("TERMS") )
					termsObject = loadObject.getJSONObject("TERMS");
				
				// get savedTerms
				HashMap<String,Term> unsortedTerms = this.getSavedTerms(termsObject,child);
				
				// sort terms
				LinkedList<Term> terms =  this.sortTerms(unsortedTerms);
				
				child.setTerms(terms);
		
				

				OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (child, 0);
				unitermOperator.addSucceedingOperator(oidtVar);
				
				JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1),
					child);

			} // end list
			
			
			
			// UnitermOperator
			if ( loadObject.get("OP TYPE").equals("UnitermOperator") ){
				UnitermOperator child = new UnitermOperator();
				JSONObject termsObject = null;
				
				child.fromJSON(loadObject, child, parent);
				child.setChild(true);
				child.setConstantComboBoxEntries(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
				child.setVisualRifEditor(visualRifEditor);

		
				child.setConstantComboBoxEntries(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
				child.setVisualRifEditor(visualRifEditor);
				child.setTermName(loadObject.getString("TERMNAME"));
				child.getUniTermComboBox().setSelectedItem(loadObject.getString("SELECTEDPREFIX"));
				child.setSelectedPrefix(loadObject.getString("SELECTEDPREFIX"));
				child.setExternal(loadObject.getBoolean("EXTERNAL"));
				child.setNamed(loadObject.getBoolean("NAMED"));
	
		
				if( loadObject.has("TERMS") )
					termsObject = loadObject.getJSONObject("TERMS");
				
				// get savedTerms
				HashMap<String,Term> unsortedTerms = this.getSavedTerms(termsObject,child);
				
				// sort terms
				LinkedList<Term> terms =  this.sortTerms(unsortedTerms);
				
				child.setTerms(terms);
		
				

				OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (child, 0);
				unitermOperator.addSucceedingOperator(oidtVar);
				
				JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1),
					child);

			} // end UnitermOperator
			
			
			
		}
		
	}


	

	


	
	
	

}
