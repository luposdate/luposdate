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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JComboBox;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.GraphWrapperIDTuple;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.AnnotationPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ClassificationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.FrameOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ListOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.UnitermOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.misc.util.OperatorIDTuple;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class AbstractTermOperator extends Operator {

		protected VisualRifEditor visualRifEditor;
		protected LinkedList<Term> terms = new LinkedList<Term>();
		protected LinkedList<String> variables = new LinkedList<String>();
		
		protected boolean startNode;
		protected boolean alsoSubClasses = false;
		protected String selectedPrefix = "";
		protected String termName =""; 
		protected JComboBox constantComboBox = new JComboBox();
		protected final String[] nextTermComboEntries = {"New Term","Const","Var","Expr","List"};
		protected JComboBox nextTermCombo = new JComboBox(nextTermComboEntries);
		protected LinkedList<String> savedPrefixes , savedNamePrefixes;
		protected boolean[] selectedRadioButton = { false, false, false };
		protected boolean isChild = false; 
		protected boolean parent = false;
		protected String selectedClassification = "=";


		// Uniterm
		protected String uniTermPrefix; 
		protected boolean external = false; 
		protected boolean named = false;
		protected String[] comboBoxEntries; 
		protected JComboBox uniTermComboBox = new JComboBox(); 
		
		protected LinkedList<String> termNames = new LinkedList<String>();
		
		
		// abstract methods
		@Override
		public abstract AbstractGuiComponent<Operator> draw(GraphWrapper gw,
				VisualGraph<Operator> parent);
		public abstract StringBuffer serializeOperator();
		public abstract StringBuffer serializeOperatorAndTree(HashSet<Operator> visited);
		
		
		
		public Term prepareToCreateVariableTerm(String varName) {

			final Term term = new Term(varName);
			term.setVariable( true );

			return term;

		}

		public Term prepareToCreateConstantTerm(String prefix, String name, String[] comboBoxEntries) {
			final Term term = new Term(name);
			term.setConstant(true);
			term.setSelectedPrefix(prefix);

			term.setComboEntries(comboBoxEntries);

			return term;
		}
		
		public Term prepareToCreateUnitermTerm(UnitermOperator unitermOperator, String[] comboBoxEntries) {
			final Term term = new Term();
			term.setUniterm(true);
			term.setAbstractTermOperator(unitermOperator);
			
//			term.setSelectedPrefix(prefix);
//
			term.setComboEntries(comboBoxEntries);
			
			term.setSucceedingOperator(new GraphWrapperOperator(unitermOperator));
			
			this.addSucceedingOperator(new OperatorIDTuple<Operator>(unitermOperator, 0));
			unitermOperator.addPrecedingOperator(this);

			return term;
		}
		
		public Term prepareToCreateListTerm(ListOperator listOperator,
				String[] comboBoxEntries) {
			final Term term = new Term();
			term.setList(true);
			term.setAbstractTermOperator(listOperator);
			
//			term.setSelectedPrefix(prefix);
//
			term.setComboEntries(comboBoxEntries);
			
			term.setSucceedingOperator(new GraphWrapperOperator(listOperator));
			
			this.addSucceedingOperator(new OperatorIDTuple<Operator>(listOperator, 0));
			listOperator.addPrecedingOperator(this);

			return term;
		}
		
		public Term prepareToCreateFrameTerm(ListOperator listOperator,
				String[] comboBoxEntries) {
			final Term term = new Term();
			term.setFrame(true);
			term.setAbstractTermOperator(listOperator);
			
//			term.setSelectedPrefix(prefix);
//
			term.setComboEntries(comboBoxEntries);
			
			term.setSucceedingOperator(new GraphWrapperOperator(listOperator));
			
			this.addSucceedingOperator(new OperatorIDTuple<Operator>(listOperator, 0));
			listOperator.addPrecedingOperator(this);

			return term;
		}
		
		
		public List<Operator> getTermSucceedingElements(){
			LinkedList<Operator> list = new LinkedList<Operator>();

			for( Term term : this.getTerms()){
				System.out.println(term.isUniterm());
				if( (term.isList() || term.isUniterm()) ){ 

					GraphWrapper childGW = term.getSucceedingOperator();
					
					if(childGW==null){
						Operator dummyOperator = term.getDummyOperator();
						
						if(dummyOperator==null){
							continue;
						}
						
						list.add(dummyOperator);
						
					} else {			
						list.add((Operator)childGW.getElement());
					}
				}
			}
			System.out.println("AbstractTermOperator. getTermSucceedingElements() "+list.size());
			return list;
		}
		
		public List<Operator> getSucceedingElementsWithoutTermSucceedingElements(){
			LinkedList<Operator> list = new LinkedList<Operator>();
			for(OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()){
				list.add(opIDTuple.getOperator());
			}

			
			list.removeAll(getTermSucceedingElements());

			return list;
		}
		
		public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(VisualGraph<Operator> parent) {
			Hashtable<GraphWrapper, AbstractSuperGuiComponent> predicates = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();
				
			// walk through children 
			for(Operator op : getSucceedingElementsWithoutTermSucceedingElements()) {
				if (op instanceof AbstractTermOperator){
					AbstractTermOperator child = (AbstractTermOperator) op; // get current children
					child.setChild(true);
				}
		
				if (op instanceof ConstantOperator){
					ConstantOperator child = (ConstantOperator) op ; // get current children
					child.setChild(true);
				}
				
				if (op instanceof VariableOperator){
						VariableOperator child = (VariableOperator) op; // get current children
						child.setChild(true);
					}

					// create predicate panel...
					ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, op);
		
					this.annotationLabels.put(op, classificationOperatorPanel);
		
					// add predicate panel to hash table with its GraphWrapper...
					predicates.put(new GraphWrapperOperator(op), classificationOperatorPanel);
			}

					
			for(Operator op : this.getTermSucceedingElements()) {
				GraphWrapper gw = new GraphWrapperOperator(this);
				GraphWrapper childGW = new GraphWrapperOperator(op);
				System.out.println("AbstractTermOperator.drawAnnotations() : this.getTermSucceedingElements()");
				if (op instanceof AbstractTermOperator){
					AbstractTermOperator child = (AbstractTermOperator) op; // get current children
					child.setChild(true);
				}
		
				if (op instanceof ConstantOperator){
					ConstantOperator child = (ConstantOperator) op ; // get current children
					child.setChild(true);
				}
				
				if (op instanceof VariableOperator){
						VariableOperator child = (VariableOperator) op; // get current children
						child.setChild(true);
					}
				
				
				AbstractGuiComponent<Operator> element = new AnnotationPanel<Operator>(parent, gw, this, op);

				this.annotationLabels.put(op, element);

				predicates.put(childGW, element);
			}


			return predicates;
		}
		
		

		
		
		
		
		@Override
		public boolean variableInUse(String variable, HashSet<Operator> visited) {
			// TODO Auto-generated method stub
			return false;
		}

		
		@Override
		public void prefixRemoved(String prefix, String namespace) {
			// TODO Auto-generated method stub
			
		}

		
		@Override
		public void prefixAdded() {
			// TODO Auto-generated method stub
			
		}

		
		@Override
		public void prefixModified(String oldPrefix, String newPrefix) {
			// TODO Auto-generated method stub
			
		}

		
		private void addVariabelesToList(){
			for (int i = 0; i < this.terms.size(); i++) {
				if(this.terms.get(i).isVariable())
				this.variables.add(this.terms.get(i).getValue());
			}
		}
		
		/**
		 * up = true , down = false
		 * @param term
		 * @param upOrDown
		 */
		public void swapTerms(final Term term, boolean upOrDown){

			
			if ( this.terms.size() > 1 ){
			
				// Up
				if ( upOrDown ){
					
					for (int i = 0; i < this.terms.size() ; i++) {
						
						if ( this.terms.get(i) == term && (i != 0)){
							Term tmp = this.terms.get(i);
							this.terms.remove(this.terms.get(i));
							this.terms.add(i-1, tmp);
							break;
						}
						
						
					}
					
				} // end up
				// down
				else {
					
					for (int i = 0; i < this.terms.size(); i++) {

						if (this.terms.get(i) == term && (i != this.terms.size()-1)) {
							Term tmp = this.terms.get(i);
							this.terms.remove(this.terms.get(i));
							this.terms.add(i + 1, tmp);
							break;
						}

					}
				}// end down
				
				
			}
		}
		
		
		public boolean hasElements() {
			return !this.terms.isEmpty();
		}
		
		
		/**
		 * Sets the ComboBox entries for UniTermComboBox and the 
		 * ConstantComboBox
		 * @param comboBoxEntries
		 */
		public void setConstantComboBoxEntries(String[] comboBoxEntries){

			this.comboBoxEntries = comboBoxEntries;
			this.getUniTermComboBox().removeAllItems();
			this.getConstantComboBox().removeAllItems();

			// UniTermPrefix
			for (String s : comboBoxEntries){
				this.getUniTermComboBox().addItem(s);	
			}
			
			this.getUniTermComboBox().setSelectedItem(this.getUniTermPrefix());
			
			
			int constantCnt = 0;
			int termCnt = 0;
			
			for (int i = 0 ; i < this.getTerms().size() ; i++){
				
				
				if( this.getTerms().get(i).isConstant() ){
					
					this.getTerms().get(i).getConstantCombo().removeAllItems();
					
					for (String s : comboBoxEntries){
						this.getTerms().get(i).getConstantCombo().addItem(s);
					}
					JComboBox tmp = this.getTerms().get(i).getConstantCombo();
					tmp.setSelectedItem(this.getSavedPrefixes().get(constantCnt));
					constantCnt++;
				}
				
				if( this.isNamed() ) {
					
					this.getTerms().get(i).getNameComboBox().removeAllItems();
					
					for (String s : comboBoxEntries){
						this.getTerms().get(i).getNameComboBox().addItem(s);
					}
					
						JComboBox tmp = this.getTerms().get(i).getNameComboBox();
						LinkedList<String> l = this.getSavedNamePrefixes();
						tmp.setSelectedItem(l.get(termCnt));
						termCnt++;
					
					
				}
				
				
			}

		}
		
		
		public JSONObject toJSON() throws JSONException {
			JSONObject saveObject = new JSONObject();
			JSONObject terms = new JSONObject();
			this.termNames.clear();
			
			if ( this.panel instanceof UnitermOperatorPanel ){

				Point position = ((UnitermOperatorPanel) this.panel).getPositionAndDimension().getFirst();

				saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
				
				saveObject.put("TERMNAME", this.termName);
				
				saveObject.put("SELECTEDPREFIX", this.selectedPrefix);
				
				saveObject.put("EXTERNAL", this.external);
				
				saveObject.put("NAMED", this.named);
			
			}
			
			if ( this.panel instanceof ListOperatorPanel ){

				Point position = ((ListOperatorPanel) this.panel).getPositionAndDimension().getFirst();

				saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
				
				saveObject.put("ISOPEN", ((ListOperatorPanel) this.panel).getListOperator().isOpen());
			
			}
			
			if ( this.panel instanceof FrameOperatorPanel ){

				Point position = ((FrameOperatorPanel) this.panel).getPositionAndDimension().getFirst();

				saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
				
			
			}
			
			
		// handle connection
		saveObject.put("ISCONNECTED", !this.getSucceedingOperators().isEmpty());
		if (!this.getSucceedingOperators().isEmpty()) {

			saveObject.put("SELECTEDCLASSIFICTION",
					this.getSelectedClassification());

			for (OperatorIDTuple<Operator> opIDTuple : this
					.getSucceedingOperators()) {

				// Constant
				if (opIDTuple.getOperator() instanceof ConstantOperator) {
					ConstantOperator co = (ConstantOperator) opIDTuple
							.getOperator();
					saveObject.put("CONNECTEDOPERATOR", co.toJSON());

				}

				// Variable
				if (opIDTuple.getOperator() instanceof VariableOperator) {
					VariableOperator vo = (VariableOperator) opIDTuple
							.getOperator();
					saveObject.put("CONNECTEDOPERATOR", vo.toJSON());

				}

				// AbstractTermOperator
				if (opIDTuple.getOperator() instanceof AbstractTermOperator) {
					AbstractTermOperator ato = (AbstractTermOperator) opIDTuple
							.getOperator();
					saveObject.put("CONNECTEDOPERATOR", ato.toJSON());

				}

			}// end for
		} // end handle connection
			
			
			saveObject.put("OP TYPE", this.getClass().getSimpleName());
			


			
			for (int i = 0; i < this.terms.size(); i++) {
				terms.put(this.checkName("term", "term", 0), this.terms.get(i).toJSON());
			}
			
			saveObject.put("TERMS", terms);
			
			

			return saveObject;
		}
		
		protected LinkedList<Term> sortTerms(HashMap<String, Term> unsortedTerms) {
			
			LinkedList<Term> terms = new LinkedList<Term>();
			
			TreeMap<String, Term> treeMap = new TreeMap<String, Term>();
					
			treeMap.putAll(unsortedTerms);
			
			for (Entry<String, Term> entry : treeMap.entrySet()) {
				terms.add(entry.getValue());
			}
		
			
			return terms;
		}
		
		protected HashMap<String, Term> getSavedTerms(JSONObject termsObject, AbstractTermOperator operator) throws JSONException {
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
		
		
		/**
		 * Checks whether the name of 
		 * the operator is already used.
		 * @param basename
		 * @param newname
		 * @param index
		 * @return a new auto-generated name for the new rule
		 */
		public String checkName(String basename, String newname, int index) {
			boolean exists = false;

			if (this.termNames.size() > 0) {
				for (int i = 0; i < this.termNames.size(); i++) {
					if (newname.equalsIgnoreCase(this.termNames.get(i))) {
						newname = basename + index;
						index += 1;
						exists = true;

						break;
					}
				}

				if (exists) {
					newname = this.checkName(basename, newname, index);
				}
			}
			
			this.termNames.add(newname);

			return newname;
		}
		
		
		
		/* ***************** **
		 * Getter and Setter **
		 * ***************** */
		
 		public String getSelectedPrefix() {
			return selectedPrefix;
		}

		public void setSelectedPrefix(String selectedPrefix) {
			this.selectedPrefix = selectedPrefix;
		}

		public LinkedList<Term> getTerms() {
			return terms;
		}

		public void setTerms(LinkedList<Term> terms) {
			this.terms = terms;
		}

		public String getTermName() {
			return termName;
		}

		public void setTermName(String termName) {
			this.termName = termName;
		}

		public JComboBox getUniTermComboBox() {
			return uniTermComboBox;
		}

		public void setUniTermComboBox(JComboBox comboBox) {
			this.uniTermComboBox = comboBox;
		}

		public JComboBox getConstantComboBox() {
			return constantComboBox;
		}

		public void setConstantComboBox(JComboBox constantComboBox) {
			this.constantComboBox = constantComboBox;
		}

		public LinkedList<String> getVariables() {
			this.addVariabelesToList();
			return variables;
		}

		public void setVariables(LinkedList<String> variables) {
			this.variables = variables;
		}

		public void setNextTermCombo(JComboBox nextTermCombo) {
			this.nextTermCombo = nextTermCombo;
		}

		public JComboBox getNextTermCombo() {
			return nextTermCombo;
		}

		public void saveUnitermPrefix(){
			this.setUniTermPrefix(this.getSelectedPrefix());
			
		} 

		public void savePrefixes() {
			this.savedPrefixes = new LinkedList<String>();
			for (int i = 0 ; i < this.getTerms().size() ; i++){
				if(this.getTerms().get(i).isConstant()){
					savedPrefixes.add(this.getTerms().get(i).getSelectedPrefix());
				}
			}
			
			
		}
		
		public void saveNamePrefixes(){
			this.savedNamePrefixes = new LinkedList<String>();
			
			for (int i = 0; i < this.getTerms().size(); i++) {
				this.savedNamePrefixes.add(this.getTerms().get(i).getPrefixForName());
			}
		}

		public LinkedList<String> getSavedPrefixes() {
			return savedPrefixes;
		}

		public void setSavedPrefixes(LinkedList<String> savedPrefixes) {
			this.savedPrefixes = savedPrefixes;
		}

		public void setUniTermPrefix(String uniTermPrefix) {
			this.uniTermPrefix = uniTermPrefix;
		}

		public String getUniTermPrefix() {
			return uniTermPrefix;
		}

		public void setExternal(boolean external) {
			this.external = external;
		}

		public boolean isExternal() {
			return external;
		}
		
		public boolean[] getSelectedRadioButton() {
			return selectedRadioButton;
		}

		public void setSelectedRadioButton(boolean[] selectedRadioButton) {
			this.selectedRadioButton = selectedRadioButton;
		}

		public boolean isChild() {
			return isChild;
		}

		public void setChild(boolean isChild) {
			this.isChild = isChild;
		}

		public String getSelectedClassification() {
			return selectedClassification;
		}

		public void setSelectedClassification(String selectedClassification) {
			this.selectedClassification = selectedClassification;
		}

		public boolean isParent() {
			return parent;
		}

		public void setParent(boolean parent) {
			this.parent = parent;
		}

		public boolean isNamed() {
			return named;
		}

		public void setNamed(boolean named) {
			this.named = named;
		}

		public LinkedList<String> getSavedNamePrefixes() {
			return savedNamePrefixes;
		}

		public void setSavedNamePrefixes(LinkedList<String> savedNamePrefixes) {
			this.savedNamePrefixes = savedNamePrefixes;
		}

		public VisualRifEditor getVisualRifEditor() {
			return visualRifEditor;
		}

		public void setVisualRifEditor(VisualRifEditor visualRifEditor) {

			this.visualRifEditor = visualRifEditor;

		}

		public String[] getComboBoxEntries() {
			return comboBoxEntries;
		}
		
		public UnitermOperatorPanel getFactOperatorPanel(){
			return (UnitermOperatorPanel) this.panel;
		}
		
		public FrameOperatorPanel getFrameOperatorPanel(){
			return (FrameOperatorPanel) this.panel;
		}
		
		public ListOperatorPanel getListOperatorPanel(){
			return (ListOperatorPanel) this.panel;
		}


}
