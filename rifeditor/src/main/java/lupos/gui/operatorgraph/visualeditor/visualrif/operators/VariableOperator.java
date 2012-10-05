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

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ClassificationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ContainerPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.VariablePanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.misc.util.OperatorIDTuple;








public class VariableOperator extends Operator  {
	
	
	private VisualRifEditor visualRifEditor;
	private String selectedClassification = "=";
	private String variable = ""; 
	private boolean[] selectedRadioButton = { false, false, false };
	private boolean isChild = false; 

	
	
	//Constructor
	public VariableOperator(){}
	
	
	
	
	@Override
	public void prefixAdded() {}

	@Override
	public void prefixModified(String arg0, String arg1) {}

	@Override
	public void prefixRemoved(String arg0, String arg1) {}



	@Override
	public boolean variableInUse(String arg0, HashSet<Operator> arg1) {
		return false;
	}



	public boolean validateOperator(boolean showErrors, HashSet<Operator> visited, Object data) {
	

		return true;
	}

	@Override
	public AbstractGuiComponent<Operator> draw(GraphWrapper gw,
			VisualGraph<Operator> parent) {
		System.out.println("this.getSucceedingOperators().isEmpty()" +this.getSucceedingOperators().isEmpty());
		if(!this.getSucceedingOperators().isEmpty())this.drawAnnotations(parent);
			this.panel = new VariablePanel(parent, gw, this, true);
	
			
		return this.panel;
	}

	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph<Operator> parent) {
		Hashtable<GraphWrapper, AbstractSuperGuiComponent> predicates = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();


		
		// walk through children 
		for(OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()) {
			
			if (opIDTuple.getOperator() instanceof UnitermOperator){
				UnitermOperator child = (UnitermOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
	
				// create predicate panel...
				ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
	
				this.annotationLabels.put(child, classificationOperatorPanel);
	
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
				}
			
			
			if (opIDTuple.getOperator() instanceof ConstantOperator){
				ConstantOperator child = (ConstantOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
	
				// create predicate panel...
				ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
	
				this.annotationLabels.put(child, classificationOperatorPanel);
	
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
				}
			
			if (opIDTuple.getOperator() instanceof VariableOperator){
				VariableOperator child = (VariableOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
	
				// create predicate panel...
				ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
	
				this.annotationLabels.put(child, classificationOperatorPanel);
	
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
				}
			
			if (opIDTuple.getOperator() instanceof ListOperator){
				ListOperator child = (ListOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
	
				// create predicate panel...
				ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
	
				this.annotationLabels.put(child, classificationOperatorPanel);
	
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
				}
		}

		return predicates;
	}
	
	


	
	@Override
	public StringBuffer serializeOperator() {
		StringBuffer sb = new StringBuffer();
		sb.append("?"+this.variable);
		return sb;
	}

	@Override
	public StringBuffer serializeOperatorAndTree(HashSet<Operator> visited) {
		StringBuffer sb = new StringBuffer();
		
		if(this.getSucceedingOperators().size() < 1){
			this.selectedClassification = "";
		}else
			this.selectedClassification = "=";
		
		if(!this.isChild){
		sb.append("?"+this.variable+" "+this.selectedClassification+" ");
		for(OperatorIDTuple<Operator> opIDT : this.getSucceedingOperators()) {
			sb.append(opIDT.getOperator().serializeOperator());
//			System.out.println(opIDT.serializeOperator());
		}
		}
		
		return sb;
	}

	public void setOpID(String opIDLabel, boolean boolean1) {
		// TODO Auto-generated method stub
		
	}

	
	public void setSelectedClassification(String selectedClassification) {
		this.selectedClassification = selectedClassification;
	}

	public String getSelectedClassification() {
		return selectedClassification;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	
	}

	public String getVariable() {
		return variable;
	}

	
	public void setSelectedRadioButton(boolean[] selectedRadioButton) {
		this.selectedRadioButton = selectedRadioButton;
	}

	public boolean[] getSelectedRadioButton() {
		return selectedRadioButton;
	}



	public void setChild(boolean isChild) {
		this.isChild = isChild;
	}




	public boolean isChild() {
		return isChild;
	}




	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}




	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}


	


	
	public JSONObject toJSON() throws JSONException {
		JSONObject saveObject = new JSONObject();
		
		saveObject.put("OP TYPE", this.getClass().getSimpleName());
		
		saveObject.put("VALUE", this.getVariable());

		saveObject.put("ISCONNECTED", !this.getSucceedingOperators().isEmpty());
		

		Point position = ((VariablePanel) this.panel).getPositionAndDimension().getFirst();

		saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
		
		if(!this.getSucceedingOperators().isEmpty()){
			
			saveObject.put("SELECTEDCLASSIFICTION", this.getSelectedClassification());

			for(OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()) {
			
				// Constant
			if (opIDTuple.getOperator() instanceof ConstantOperator){
				ConstantOperator co = (ConstantOperator) opIDTuple.getOperator();
					saveObject.put("CONNECTEDOPERATOR", co.toJSON());
				
			}
			
			// Variable
			if (opIDTuple.getOperator() instanceof VariableOperator){
				VariableOperator vo = (VariableOperator) opIDTuple.getOperator();
					saveObject.put("CONNECTEDOPERATOR", vo.toJSON());
				
			}
			
			
			
			// AbstractTermOperator
			if (opIDTuple.getOperator() instanceof AbstractTermOperator){
				AbstractTermOperator ato = (AbstractTermOperator) opIDTuple.getOperator();
					saveObject.put("CONNECTEDOPERATOR", ato.toJSON());
				
			}
			
			
			
			}// end for
		}
		
		return saveObject;
	}




	public void fromJSON(JSONObject operatorObject, VariableOperator variableOperator,VisualGraph<Operator> parent) throws JSONException {
		

		variableOperator.setVariable(operatorObject.get("VALUE").toString());
		boolean isConnected = operatorObject.getBoolean("ISCONNECTED");
		
		if (isConnected) {
			JSONObject loadObject = new JSONObject();
			loadObject = (JSONObject) operatorObject.get("CONNECTEDOPERATOR");
			variableOperator.setSelectedClassification((String)operatorObject.get("SELECTEDCLASSIFICTION"));
			
		if (variableOperator.getSelectedClassification().equals("=")){
							boolean[] equality = {true,false,false};
							variableOperator.setSelectedRadioButton(equality);
	
		}else
	
			if (variableOperator.getSelectedClassification().equals("#")){
							boolean[] membership = {false,true,false};
							variableOperator.setSelectedRadioButton(membership);
			
			}else		
				if (variableOperator.getSelectedClassification().equals("##")){
							boolean[] subclass = {false,false, true};
							variableOperator.setSelectedRadioButton(subclass);
				}
			
			
			// Constant
			if ( loadObject.get("OP TYPE").equals("ConstantOperator") ){
				ConstantOperator child = new ConstantOperator();
				child.fromJSON(loadObject, child, parent);
				child.setChild(true);

				OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (child, 0);
				variableOperator.addSucceedingOperator(oidtConst);
				
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
				variableOperator.addSucceedingOperator(oidtVar);
				
				JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1),
					child);

			} // end variable
			
			// ListOperator
			if ( loadObject.get("OP TYPE").equals("ListOperator") ){
				ListOperator child = new ListOperator();
				JSONObject termsObject = null;
				
				child.fromJSON(loadObject, child, (RuleGraph) parent);
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
				variableOperator.addSucceedingOperator(oidtVar);
				
				JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1),
					child);

			} // end list
			
			
			
			// UnitermOperator
			if ( loadObject.get("OP TYPE").equals("UnitermOperator") ){
				UnitermOperator child = new UnitermOperator();
				JSONObject termsObject = null;
				
				child.fromJSON(loadObject, child, (RuleGraph) parent);
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
				variableOperator.addSucceedingOperator(oidtVar);
				
				JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1),
					child);

			} // end UnitermOperator
			
			
			
		}
		
	}
	
	private LinkedList<Term> sortTerms(HashMap<String, Term> unsortedTerms) {
		
		LinkedList<Term> terms = new LinkedList<Term>();
		
		TreeMap<String, Term> treeMap = new TreeMap<String, Term>();
				
		treeMap.putAll(unsortedTerms);
		
		for (Entry<String, Term> entry : treeMap.entrySet()) {
			terms.add(entry.getValue());
		}
	
		
		return terms;
	}
	
	private HashMap<String, Term> getSavedTerms(JSONObject termsObject, AbstractTermOperator operator) throws JSONException {
		HashMap<String,Term> unsortedTerms = new HashMap<String,Term>();
		
		@SuppressWarnings("unchecked")
		Iterator<String> key = termsObject.keys();
		
		while(key.hasNext()) {
			
			String termName = key.next();
			JSONObject termObj = termsObject.getJSONObject(termName);
			
			if (termObj.get("TYPE").equals("variable")) {

				String value = termObj.getString("VALUE");
				Term term = operator.prepareToCreateVariableTerm(value);
				unsortedTerms.put(termName, term);
			}
			
			if (termObj.get("TYPE").equals("constant")) {
				
				String prefix = termObj.getString("PREFIXVALUE");
				String value = termObj.getString("VALUE");
			
				Term term = operator.prepareToCreateConstantTerm(prefix, value, this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
				unsortedTerms.put(termName, term);
			}
			
		}

		return unsortedTerms;
	}


}
