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
		/** {@inheritDoc} */
		@Override
		public abstract AbstractGuiComponent<Operator> draw(GraphWrapper gw, VisualGraph<Operator> parent);
		/** {@inheritDoc} */
		@Override
		public abstract StringBuffer serializeOperator();
		/** {@inheritDoc} */
		@Override
		public abstract StringBuffer serializeOperatorAndTree(HashSet<Operator> visited);

		/**
		 * <p>prepareToCreateVariableTerm.</p>
		 *
		 * @param varName a {@link java.lang.String} object.
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
		 */
		public Term prepareToCreateVariableTerm(final String varName) {
			final Term term = new Term(varName);
			term.setVariable( true );
			return term;
		}

		/**
		 * <p>prepareToCreateConstantTerm.</p>
		 *
		 * @param prefix a {@link java.lang.String} object.
		 * @param name a {@link java.lang.String} object.
		 * @param comboBoxEntries an array of {@link java.lang.String} objects.
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
		 */
		public Term prepareToCreateConstantTerm(final String prefix, final String name, final String[] comboBoxEntries) {
			final Term term = new Term(name);
			term.setConstant(true);
			term.setSelectedPrefix(prefix);
			term.setComboEntries(comboBoxEntries);
			return term;
		}

		/**
		 * <p>prepareToCreateUnitermTerm.</p>
		 *
		 * @param unitermOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator} object.
		 * @param comboBoxEntries an array of {@link java.lang.String} objects.
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
		 */
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

		/**
		 * <p>prepareToCreateListTerm.</p>
		 *
		 * @param listOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator} object.
		 * @param comboBoxEntries an array of {@link java.lang.String} objects.
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
		 */
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

		/**
		 * <p>prepareToCreateFrameTerm.</p>
		 *
		 * @param listOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator} object.
		 * @param comboBoxEntries an array of {@link java.lang.String} objects.
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
		 */
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

		/**
		 * <p>getTermSucceedingElements.</p>
		 *
		 * @return a {@link java.util.List} object.
		 */
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

		/**
		 * <p>getSucceedingElementsWithoutTermSucceedingElements.</p>
		 *
		 * @return a {@link java.util.List} object.
		 */
		public List<Operator> getSucceedingElementsWithoutTermSucceedingElements(){
			final LinkedList<Operator> list = new LinkedList<Operator>();
			for(final OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()){
				list.add(opIDTuple.getOperator());
			}
			list.removeAll(this.getTermSucceedingElements());
			return list;
		}

		/**
		 * <p>drawAnnotations.</p>
		 *
		 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph} object.
		 * @return a {@link java.util.Hashtable} object.
		 */
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

		/** {@inheritDoc} */
		@Override
		public boolean variableInUse(final String variable, final HashSet<Operator> visited) {
			return false;
		}

		/** {@inheritDoc} */
		@Override
		public void prefixRemoved(final String prefix, final String namespace) {
		}

		/** {@inheritDoc} */
		@Override
		public void prefixAdded() {
		}

		/** {@inheritDoc} */
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
		 *
		 * @param term a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
		 * @param upOrDown a boolean.
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

		/**
		 * <p>hasElements.</p>
		 *
		 * @return a boolean.
		 */
		public boolean hasElements() {
			return !this.terms.isEmpty();
		}

		/**
		 * Sets the ComboBox entries for UniTermComboBox and the
		 * ConstantComboBox
		 *
		 * @param comboBoxEntries an array of {@link java.lang.String} objects.
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

		/**
		 * <p>toJSON.</p>
		 *
		 * @return a {@link org.json.JSONObject} object.
		 * @throws org.json.JSONException if any.
		 */
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

		/**
		 * <p>sortTerms.</p>
		 *
		 * @param unsortedTerms a {@link java.util.HashMap} object.
		 * @return a {@link java.util.LinkedList} object.
		 */
		protected LinkedList<Term> sortTerms(final HashMap<String, Term> unsortedTerms) {
			final LinkedList<Term> terms = new LinkedList<Term>();
			final TreeMap<String, Term> treeMap = new TreeMap<String, Term>();
			treeMap.putAll(unsortedTerms);
			for (final Entry<String, Term> entry : treeMap.entrySet()) {
				terms.add(entry.getValue());
			}
			return terms;
		}

		/**
		 * <p>getSavedTerms.</p>
		 *
		 * @param termsObject a {@link org.json.JSONObject} object.
		 * @param operator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator} object.
		 * @return a {@link java.util.HashMap} object.
		 * @throws org.json.JSONException if any.
		 */
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
		 *
		 * @param basename a {@link java.lang.String} object.
		 * @param newname a {@link java.lang.String} object.
		 * @param index a int.
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
 		/**
 		 * <p>Getter for the field <code>selectedPrefix</code>.</p>
 		 *
 		 * @return a {@link java.lang.String} object.
 		 */
 		public String getSelectedPrefix() {
			return this.selectedPrefix;
		}

		/**
		 * <p>Setter for the field <code>selectedPrefix</code>.</p>
		 *
		 * @param selectedPrefix a {@link java.lang.String} object.
		 */
		public void setSelectedPrefix(final String selectedPrefix) {
			this.selectedPrefix = selectedPrefix;
		}

		/**
		 * <p>Getter for the field <code>terms</code>.</p>
		 *
		 * @return a {@link java.util.LinkedList} object.
		 */
		public LinkedList<Term> getTerms() {
			return this.terms;
		}

		/**
		 * <p>Setter for the field <code>terms</code>.</p>
		 *
		 * @param terms a {@link java.util.LinkedList} object.
		 */
		public void setTerms(final LinkedList<Term> terms) {
			this.terms = terms;
		}

		/**
		 * <p>Getter for the field <code>termName</code>.</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getTermName() {
			return this.termName;
		}

		/**
		 * <p>Setter for the field <code>termName</code>.</p>
		 *
		 * @param termName a {@link java.lang.String} object.
		 */
		public void setTermName(final String termName) {
			this.termName = termName;
		}

		/**
		 * <p>Getter for the field <code>uniTermComboBox</code>.</p>
		 *
		 * @return a {@link javax.swing.JComboBox} object.
		 */
		public JComboBox getUniTermComboBox() {
			return this.uniTermComboBox;
		}

		/**
		 * <p>Setter for the field <code>uniTermComboBox</code>.</p>
		 *
		 * @param comboBox a {@link javax.swing.JComboBox} object.
		 */
		public void setUniTermComboBox(final JComboBox comboBox) {
			this.uniTermComboBox = comboBox;
		}

		/**
		 * <p>Getter for the field <code>constantComboBox</code>.</p>
		 *
		 * @return a {@link javax.swing.JComboBox} object.
		 */
		public JComboBox getConstantComboBox() {
			return this.constantComboBox;
		}

		/**
		 * <p>Setter for the field <code>constantComboBox</code>.</p>
		 *
		 * @param constantComboBox a {@link javax.swing.JComboBox} object.
		 */
		public void setConstantComboBox(final JComboBox constantComboBox) {
			this.constantComboBox = constantComboBox;
		}

		/**
		 * <p>Getter for the field <code>variables</code>.</p>
		 *
		 * @return a {@link java.util.LinkedList} object.
		 */
		public LinkedList<String> getVariables() {
			this.addVariabelesToList();
			return this.variables;
		}

		/**
		 * <p>Setter for the field <code>variables</code>.</p>
		 *
		 * @param variables a {@link java.util.LinkedList} object.
		 */
		public void setVariables(final LinkedList<String> variables) {
			this.variables = variables;
		}

		/**
		 * <p>Setter for the field <code>nextTermCombo</code>.</p>
		 *
		 * @param nextTermCombo a {@link javax.swing.JComboBox} object.
		 */
		public void setNextTermCombo(final JComboBox nextTermCombo) {
			this.nextTermCombo = nextTermCombo;
		}

		/**
		 * <p>Getter for the field <code>nextTermCombo</code>.</p>
		 *
		 * @return a {@link javax.swing.JComboBox} object.
		 */
		public JComboBox getNextTermCombo() {
			return this.nextTermCombo;
		}

		/**
		 * <p>saveUnitermPrefix.</p>
		 */
		public void saveUnitermPrefix(){
			this.setUniTermPrefix(this.getSelectedPrefix());

		}

		/**
		 * <p>savePrefixes.</p>
		 */
		public void savePrefixes() {
			this.savedPrefixes = new LinkedList<String>();
			for (int i = 0 ; i < this.getTerms().size() ; i++){
				if(this.getTerms().get(i).isConstant()){
					this.savedPrefixes.add(this.getTerms().get(i).getSelectedPrefix());
				}
			}
		}

		/**
		 * <p>saveNamePrefixes.</p>
		 */
		public void saveNamePrefixes(){
			this.savedNamePrefixes = new LinkedList<String>();
			for (int i = 0; i < this.getTerms().size(); i++) {
				this.savedNamePrefixes.add(this.getTerms().get(i).getPrefixForName());
			}
		}

		/**
		 * <p>Getter for the field <code>savedPrefixes</code>.</p>
		 *
		 * @return a {@link java.util.LinkedList} object.
		 */
		public LinkedList<String> getSavedPrefixes() {
			return this.savedPrefixes;
		}

		/**
		 * <p>Setter for the field <code>savedPrefixes</code>.</p>
		 *
		 * @param savedPrefixes a {@link java.util.LinkedList} object.
		 */
		public void setSavedPrefixes(final LinkedList<String> savedPrefixes) {
			this.savedPrefixes = savedPrefixes;
		}

		/**
		 * <p>Setter for the field <code>uniTermPrefix</code>.</p>
		 *
		 * @param uniTermPrefix a {@link java.lang.String} object.
		 */
		public void setUniTermPrefix(final String uniTermPrefix) {
			this.uniTermPrefix = uniTermPrefix;
		}

		/**
		 * <p>Getter for the field <code>uniTermPrefix</code>.</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getUniTermPrefix() {
			return this.uniTermPrefix;
		}

		/**
		 * <p>Setter for the field <code>external</code>.</p>
		 *
		 * @param external a boolean.
		 */
		public void setExternal(final boolean external) {
			this.external = external;
		}

		/**
		 * <p>isExternal.</p>
		 *
		 * @return a boolean.
		 */
		public boolean isExternal() {
			return this.external;
		}

		/**
		 * <p>Getter for the field <code>selectedRadioButton</code>.</p>
		 *
		 * @return an array of boolean.
		 */
		public boolean[] getSelectedRadioButton() {
			return this.selectedRadioButton;
		}

		/**
		 * <p>Setter for the field <code>selectedRadioButton</code>.</p>
		 *
		 * @param selectedRadioButton an array of boolean.
		 */
		public void setSelectedRadioButton(final boolean[] selectedRadioButton) {
			this.selectedRadioButton = selectedRadioButton;
		}

		/**
		 * <p>isChild.</p>
		 *
		 * @return a boolean.
		 */
		public boolean isChild() {
			return this.isChild;
		}

		/**
		 * <p>setChild.</p>
		 *
		 * @param isChild a boolean.
		 */
		public void setChild(final boolean isChild) {
			this.isChild = isChild;
		}

		/**
		 * <p>Getter for the field <code>selectedClassification</code>.</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getSelectedClassification() {
			return this.selectedClassification;
		}

		/**
		 * <p>Setter for the field <code>selectedClassification</code>.</p>
		 *
		 * @param selectedClassification a {@link java.lang.String} object.
		 */
		public void setSelectedClassification(final String selectedClassification) {
			this.selectedClassification = selectedClassification;
		}

		/**
		 * <p>isParent.</p>
		 *
		 * @return a boolean.
		 */
		public boolean isParent() {
			return this.parent;
		}

		/**
		 * <p>Setter for the field <code>parent</code>.</p>
		 *
		 * @param parent a boolean.
		 */
		public void setParent(final boolean parent) {
			this.parent = parent;
		}

		/**
		 * <p>isNamed.</p>
		 *
		 * @return a boolean.
		 */
		public boolean isNamed() {
			return this.named;
		}

		/**
		 * <p>Setter for the field <code>named</code>.</p>
		 *
		 * @param named a boolean.
		 */
		public void setNamed(final boolean named) {
			this.named = named;
		}

		/**
		 * <p>Getter for the field <code>savedNamePrefixes</code>.</p>
		 *
		 * @return a {@link java.util.LinkedList} object.
		 */
		public LinkedList<String> getSavedNamePrefixes() {
			return this.savedNamePrefixes;
		}

		/**
		 * <p>Setter for the field <code>savedNamePrefixes</code>.</p>
		 *
		 * @param savedNamePrefixes a {@link java.util.LinkedList} object.
		 */
		public void setSavedNamePrefixes(final LinkedList<String> savedNamePrefixes) {
			this.savedNamePrefixes = savedNamePrefixes;
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
		 * <p>Getter for the field <code>comboBoxEntries</code>.</p>
		 *
		 * @return an array of {@link java.lang.String} objects.
		 */
		public String[] getComboBoxEntries() {
			return this.comboBoxEntries;
		}

		/**
		 * <p>getFactOperatorPanel.</p>
		 *
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.UnitermOperatorPanel} object.
		 */
		public UnitermOperatorPanel getFactOperatorPanel(){
			return (UnitermOperatorPanel) this.panel;
		}

		/**
		 * <p>getFrameOperatorPanel.</p>
		 *
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.FrameOperatorPanel} object.
		 */
		public FrameOperatorPanel getFrameOperatorPanel(){
			return (FrameOperatorPanel) this.panel;
		}

		/**
		 * <p>getListOperatorPanel.</p>
		 *
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ListOperatorPanel} object.
		 */
		public ListOperatorPanel getListOperatorPanel(){
			return (ListOperatorPanel) this.panel;
		}
}
