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

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JComboBox;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.AnnotationPanel;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
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
		protected JComboBox nextTermCombo = new JComboBox(this.nextTermComboEntries);
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
		public abstract AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent);
		@Override
		public abstract StringBuffer serializeOperator();
		@Override
		public abstract StringBuffer serializeOperatorAndTree(HashSet<Operator> visited);

		public Term prepareToCreateVariableTerm(final String varName) {
			final Term term = new Term(varName);
			term.setVariable( true );
			return term;
		}

		public Term prepareToCreateConstantTerm(final String prefix, final String name, final String[] comboBoxEntries) {
			final Term term = new Term(name);
			term.setConstant(true);
			term.setSelectedPrefix(prefix);
			term.setComboEntries(comboBoxEntries);
			return term;
		}

		public Term prepareToCreateUnitermTerm(final UnitermOperator unitermOperator, final String[] comboBoxEntries) {
			final Term term = new Term();
			term.setUniterm(true);
			term.setAbstractTermOperator(unitermOperator);
			term.setComboEntries(comboBoxEntries);
			term.setSucceedingOperator(new GraphWrapperOperator(unitermOperator));
			this.addSucceedingOperator(new OperatorIDTuple<Operator>(unitermOperator, 0));
			unitermOperator.addPrecedingOperator(this);
			return term;
		}

		public Term prepareToCreateListTerm(final ListOperator listOperator,
				final String[] comboBoxEntries) {
			final Term term = new Term();
			term.setList(true);
			term.setAbstractTermOperator(listOperator);
			term.setComboEntries(comboBoxEntries);
			term.setSucceedingOperator(new GraphWrapperOperator(listOperator));
			this.addSucceedingOperator(new OperatorIDTuple<Operator>(listOperator, 0));
			listOperator.addPrecedingOperator(this);
			return term;
		}

		public Term prepareToCreateFrameTerm(final ListOperator listOperator,
				final String[] comboBoxEntries) {
			final Term term = new Term();
			term.setFrame(true);
			term.setAbstractTermOperator(listOperator);
			term.setComboEntries(comboBoxEntries);
			term.setSucceedingOperator(new GraphWrapperOperator(listOperator));
			this.addSucceedingOperator(new OperatorIDTuple<Operator>(listOperator, 0));
			listOperator.addPrecedingOperator(this);
			return term;
		}

		public List<Operator> getTermSucceedingElements(){
			final LinkedList<Operator> list = new LinkedList<Operator>();
			for( final Term term : this.getTerms()){
				System.out.println(term.isUniterm());
				if( (term.isList() || term.isUniterm()) ){
					final GraphWrapper childGW = term.getSucceedingOperator();
					if(childGW==null){
						final Operator dummyOperator = term.getDummyOperator();
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
			final LinkedList<Operator> list = new LinkedList<Operator>();
			for(final OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()){
				list.add(opIDTuple.getOperator());
			}
			list.removeAll(this.getTermSucceedingElements());
			return list;
		}

		public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(final VisualRIFGraph<Operator> parent) {
			final Hashtable<GraphWrapper, AbstractSuperGuiComponent> predicates = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();
			// walk through children
			for(final Operator op : this.getSucceedingElementsWithoutTermSucceedingElements()) {
				if (op instanceof AbstractTermOperator){
					final AbstractTermOperator child = (AbstractTermOperator) op; // get current children
					child.setChild(true);
				}
				if (op instanceof ConstantOperator){
					final ConstantOperator child = (ConstantOperator) op ; // get current children
					child.setChild(true);
				}
				if (op instanceof VariableOperator){
						final VariableOperator child = (VariableOperator) op; // get current children
						child.setChild(true);
					}
					// create predicate panel...
					final ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, op);
					this.annotationLabels.put(op, classificationOperatorPanel);
					// add predicate panel to hash table with its GraphWrapper...
					predicates.put(new GraphWrapperOperator(op), classificationOperatorPanel);
			}
			for(final Operator op : this.getTermSucceedingElements()) {
				final GraphWrapper gw = new GraphWrapperOperator(this);
				final GraphWrapper childGW = new GraphWrapperOperator(op);
				System.out.println("AbstractTermOperator.drawAnnotations() : this.getTermSucceedingElements()");
				if (op instanceof AbstractTermOperator){
					final AbstractTermOperator child = (AbstractTermOperator) op; // get current children
					child.setChild(true);
				}
				if (op instanceof ConstantOperator){
					final ConstantOperator child = (ConstantOperator) op ; // get current children
					child.setChild(true);
				}
				if (op instanceof VariableOperator){
						final VariableOperator child = (VariableOperator) op; // get current children
						child.setChild(true);
				}
				final AbstractGuiComponent<Operator> element = new AnnotationPanel<Operator>(parent, gw, this, op);
				this.annotationLabels.put(op, element);
				predicates.put(childGW, element);
			}
			return predicates;
		}

		@Override
		public boolean variableInUse(final String variable, final HashSet<Operator> visited) {
			return false;
		}

		@Override
		public void prefixRemoved(final String prefix, final String namespace) {
		}

		@Override
		public void prefixAdded() {
		}

		@Override
		public void prefixModified(final String oldPrefix, final String newPrefix) {
		}

		private void addVariabelesToList(){
			for (int i = 0; i < this.terms.size(); i++) {
				if(this.terms.get(i).isVariable()) {
					this.variables.add(this.terms.get(i).getValue());
				}
			}
		}

		/**
		 * up = true , down = false
		 * @param term
		 * @param upOrDown
		 */
		public void swapTerms(final Term term, final boolean upOrDown){
			if ( this.terms.size() > 1 ){
				// Up
				if ( upOrDown ){
					for (int i = 0; i < this.terms.size() ; i++) {
						if ( this.terms.get(i) == term && (i != 0)){
							final Term tmp = this.terms.get(i);
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
							final Term tmp = this.terms.get(i);
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
		public void setConstantComboBoxEntries(final String[] comboBoxEntries){
			this.comboBoxEntries = comboBoxEntries;
			this.getUniTermComboBox().removeAllItems();
			this.getConstantComboBox().removeAllItems();

			// UniTermPrefix
			for (final String s : comboBoxEntries){
				this.getUniTermComboBox().addItem(s);
			}

			this.getUniTermComboBox().setSelectedItem(this.getUniTermPrefix());

			int constantCnt = 0;
			int termCnt = 0;

			for (int i = 0 ; i < this.getTerms().size() ; i++){
				if( this.getTerms().get(i).isConstant() ){
					this.getTerms().get(i).getConstantCombo().removeAllItems();
					for (final String s : comboBoxEntries){
						this.getTerms().get(i).getConstantCombo().addItem(s);
					}
					final JComboBox tmp = this.getTerms().get(i).getConstantCombo();
					tmp.setSelectedItem(this.getSavedPrefixes().get(constantCnt));
					constantCnt++;
				}

				if( this.isNamed() ) {
					this.getTerms().get(i).getNameComboBox().removeAllItems();
					for (final String s : comboBoxEntries){
						this.getTerms().get(i).getNameComboBox().addItem(s);
					}
					final JComboBox tmp = this.getTerms().get(i).getNameComboBox();
					final LinkedList<String> l = this.getSavedNamePrefixes();
					tmp.setSelectedItem(l.get(termCnt));
					termCnt++;
				}
			}
		}

		public JSONObject toJSON() throws JSONException {
			final JSONObject saveObject = new JSONObject();
			final JSONObject terms = new JSONObject();
			this.termNames.clear();

			if ( this.panel instanceof UnitermOperatorPanel ){
				final Point position = ((UnitermOperatorPanel) this.panel).getPositionAndDimension().getFirst();
				saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
				saveObject.put("TERMNAME", this.termName);
				saveObject.put("SELECTEDPREFIX", this.selectedPrefix);
				saveObject.put("EXTERNAL", this.external);
				saveObject.put("NAMED", this.named);
			}
			if ( this.panel instanceof ListOperatorPanel ) {
				final Point position = ((ListOperatorPanel) this.panel).getPositionAndDimension().getFirst();
				saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
				saveObject.put("ISOPEN", ((ListOperatorPanel) this.panel).getListOperator().isOpen());
			}
			if ( this.panel instanceof FrameOperatorPanel ){
				final Point position = ((FrameOperatorPanel) this.panel).getPositionAndDimension().getFirst();
				saveObject.put("POSITION", new double[]{position.getX(), position.getY()});
			}
			// handle connection
			saveObject.put("ISCONNECTED", !this.getSucceedingOperators().isEmpty());
			if (!this.getSucceedingOperators().isEmpty()) {
				saveObject.put("SELECTEDCLASSIFICTION",
						this.getSelectedClassification());
				for (final OperatorIDTuple<Operator> opIDTuple : this
						.getSucceedingOperators()) {
					// Constant
					if (opIDTuple.getOperator() instanceof ConstantOperator) {
						final ConstantOperator co = (ConstantOperator) opIDTuple
								.getOperator();
						saveObject.put("CONNECTEDOPERATOR", co.toJSON());
					}
					// Variable
					if (opIDTuple.getOperator() instanceof VariableOperator) {
						final VariableOperator vo = (VariableOperator) opIDTuple
								.getOperator();
						saveObject.put("CONNECTEDOPERATOR", vo.toJSON());
					}
					// AbstractTermOperator
					if (opIDTuple.getOperator() instanceof AbstractTermOperator) {
						final AbstractTermOperator ato = (AbstractTermOperator) opIDTuple
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

		protected LinkedList<Term> sortTerms(final HashMap<String, Term> unsortedTerms) {
			final LinkedList<Term> terms = new LinkedList<Term>();
			final TreeMap<String, Term> treeMap = new TreeMap<String, Term>();
			treeMap.putAll(unsortedTerms);
			for (final Entry<String, Term> entry : treeMap.entrySet()) {
				terms.add(entry.getValue());
			}
			return terms;
		}

		protected HashMap<String, Term> getSavedTerms(final JSONObject termsObject, final AbstractTermOperator operator) throws JSONException {
			final HashMap<String,Term> unsortedTerms = new HashMap<String,Term>();
			@SuppressWarnings("unchecked")
			final
			Iterator<String> key = termsObject.keys();
			while(key.hasNext()) {
				final String termName = key.next();
				final JSONObject termObj = termsObject.getJSONObject(termName);
				if (termObj.get("TYPE").equals("variable")) {
					final String value = termObj.getString("VALUE");
					final Term term = operator.prepareToCreateVariableTerm(value);
					unsortedTerms.put(termName, term);
				}
				if (termObj.get("TYPE").equals("constant")) {
					final String prefix = termObj.getString("PREFIXVALUE");
					final String value = termObj.getString("VALUE");
					final Term term = operator.prepareToCreateConstantTerm(prefix, value, this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
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
		public String checkName(final String basename, String newname, int index) {
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
			return this.selectedPrefix;
		}

		public void setSelectedPrefix(final String selectedPrefix) {
			this.selectedPrefix = selectedPrefix;
		}

		public LinkedList<Term> getTerms() {
			return this.terms;
		}

		public void setTerms(final LinkedList<Term> terms) {
			this.terms = terms;
		}

		public String getTermName() {
			return this.termName;
		}

		public void setTermName(final String termName) {
			this.termName = termName;
		}

		public JComboBox getUniTermComboBox() {
			return this.uniTermComboBox;
		}

		public void setUniTermComboBox(final JComboBox comboBox) {
			this.uniTermComboBox = comboBox;
		}

		public JComboBox getConstantComboBox() {
			return this.constantComboBox;
		}

		public void setConstantComboBox(final JComboBox constantComboBox) {
			this.constantComboBox = constantComboBox;
		}

		public LinkedList<String> getVariables() {
			this.addVariabelesToList();
			return this.variables;
		}

		public void setVariables(final LinkedList<String> variables) {
			this.variables = variables;
		}

		public void setNextTermCombo(final JComboBox nextTermCombo) {
			this.nextTermCombo = nextTermCombo;
		}

		public JComboBox getNextTermCombo() {
			return this.nextTermCombo;
		}

		public void saveUnitermPrefix(){
			this.setUniTermPrefix(this.getSelectedPrefix());

		}

		public void savePrefixes() {
			this.savedPrefixes = new LinkedList<String>();
			for (int i = 0 ; i < this.getTerms().size() ; i++){
				if(this.getTerms().get(i).isConstant()){
					this.savedPrefixes.add(this.getTerms().get(i).getSelectedPrefix());
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
			return this.savedPrefixes;
		}

		public void setSavedPrefixes(final LinkedList<String> savedPrefixes) {
			this.savedPrefixes = savedPrefixes;
		}

		public void setUniTermPrefix(final String uniTermPrefix) {
			this.uniTermPrefix = uniTermPrefix;
		}

		public String getUniTermPrefix() {
			return this.uniTermPrefix;
		}

		public void setExternal(final boolean external) {
			this.external = external;
		}

		public boolean isExternal() {
			return this.external;
		}

		public boolean[] getSelectedRadioButton() {
			return this.selectedRadioButton;
		}

		public void setSelectedRadioButton(final boolean[] selectedRadioButton) {
			this.selectedRadioButton = selectedRadioButton;
		}

		public boolean isChild() {
			return this.isChild;
		}

		public void setChild(final boolean isChild) {
			this.isChild = isChild;
		}

		public String getSelectedClassification() {
			return this.selectedClassification;
		}

		public void setSelectedClassification(final String selectedClassification) {
			this.selectedClassification = selectedClassification;
		}

		public boolean isParent() {
			return this.parent;
		}

		public void setParent(final boolean parent) {
			this.parent = parent;
		}

		public boolean isNamed() {
			return this.named;
		}

		public void setNamed(final boolean named) {
			this.named = named;
		}

		public LinkedList<String> getSavedNamePrefixes() {
			return this.savedNamePrefixes;
		}

		public void setSavedNamePrefixes(final LinkedList<String> savedNamePrefixes) {
			this.savedNamePrefixes = savedNamePrefixes;
		}

		public VisualRifEditor getVisualRifEditor() {
			return this.visualRifEditor;
		}

		public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
			this.visualRifEditor = visualRifEditor;
		}

		public String[] getComboBoxEntries() {
			return this.comboBoxEntries;
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
