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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import lupos.datastructures.items.Item;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.VisualEditor;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AndContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ExistsContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.OrContainer;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.RuleOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphBoxRif;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.VisualGraphOperator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RuleGraph  extends VisualGraphOperator{


	private static final long serialVersionUID = -2936295936044533187L;
	private boolean recursiveOperatorGraph;
	private final LinkedList<String> operatorNames = new LinkedList<String>();
	private VisualEditor<Operator> visualEditor;

	private AbstractContainer operatorContainer;
	private LinkedList<Term> ruleVariableList = new LinkedList<Term>();










	// Constructor
	public RuleGraph(final VisualEditor<Operator> visualEditor, final VisualRifEditor visualRifEditor,final boolean isRecursiveOperatorGraph) {
		super(visualEditor);

		this.setVisualEditor(visualEditor);

		this.graphBoxCreator = new GraphBoxRif.RifGraphBoxCreator();

		this.visualRifEditor = visualRifEditor;

		this.setRecursiveOperatorGraph(isRecursiveOperatorGraph);

		this.SPACING_X = 190;
		this.SPACING_Y = 190;

		this.construct();
	}







	@Override
	protected Operator createOperator(final Class<? extends Operator> clazz, final Item content) throws Exception {

		Operator newOp = null;

		newOp = clazz.newInstance();

		return newOp;
	}

	@Override
	protected void handleAddOperator(final Operator arg0) {}

	@Override
	public String serializeGraph() {
		final String ruleGraph = super.serializeSuperGraph();
		final StringBuffer ret = new StringBuffer();
		ret.append(ruleGraph);
		return ret.toString();
	}

	@Override
	protected boolean validateAddOperator(final int arg0, final int arg1, final String arg2) {
		return true;
	}

	@Override
	protected void createNewRule(final RuleOperator ro) {
	}


	@Override
	protected void createNewPrefix(final PrefixOperator po) {
	}

	@Override
	protected void createNewImport(final ImportOperator io) {
	}


	/*
	 * Rule Elements
	 */
	@Override
	public void createNewUniterm(final UnitermOperator fo) {
		fo.setVisualRifEditor(this.visualRifEditor);

		if( this.isRecursiveOperatorGraph() ) {
			this.getOperatorContainer().addOperator(fo);
		}
	}

	@Override
	protected void createNewOperatorContainer(final AbstractContainer oc) {
		oc.setVisualRifEditor(this.visualRifEditor);
		System.out.println("RuleGraph.createNewOperatorContainer(AbstractContainer oc)");
		if ( oc.getGUIComponent() == null ){
			oc.draw(new GraphWrapperOperator(oc),
					this);
			System.out.println("oc.getGUIComponent() == null!!!!!!!!!!!!!!!!!!!!");
		}
		if( this.isRecursiveOperatorGraph() ) {
			this.getOperatorContainer().addOperator(oc);
		}
	}

	@Override
	protected void createNewListOperator(final ListOperator lo) {
		lo.setVisualRifEditor(this.visualRifEditor);
		if( this.isRecursiveOperatorGraph() ) {
			this.getOperatorContainer().addOperator(lo);
		}
	}

	@Override
	protected void createNewFrameOperator(final FrameOperator fo) {
		fo.setVisualRifEditor(this.visualRifEditor);
		if( this.isRecursiveOperatorGraph() ) {
			this.getOperatorContainer().addOperator(fo);
		}
	}

	@Override
	protected void createNewConstantOperator(final ConstantOperator co) {
		co.setVisualRifEditor(this.visualRifEditor);
		if( this.isRecursiveOperatorGraph() ) {
			this.getOperatorContainer().addOperator(co);
		}
	}

	@Override
	protected void createNewVariableOperator(final VariableOperator vo) {
		vo.setVisualRifEditor(this.visualRifEditor);
		if( this.isRecursiveOperatorGraph() ) {
			this.getOperatorContainer().addOperator(vo);
		}
	}

	public AbstractContainer getOperatorContainer() {
		return this.operatorContainer;
	}

	public void setOperatorContainer(final AbstractContainer operatorContainer) {
		this.operatorContainer = operatorContainer;
	}

	public JSONObject toJSON() {
		final JSONObject saveObject = new JSONObject();
		this.operatorNames.clear();

		for(final GraphWrapper gw : this.boxes.keySet()) {
			final Operator op = (Operator) gw.getElement();

			// AbstractTermOperator
			if (op instanceof AbstractTermOperator) {
				final AbstractTermOperator ato = (AbstractTermOperator) op;
				if (!ato.isChild()) {
					try {
						saveObject.put(this.checkName("AbstractTermOperator",
								"AbstractTermOperator", 0), ato.toJSON());
					} catch (final JSONException e) {
						e.printStackTrace();
					}
				}
			}

			// OperatorContainer
			if (op instanceof AbstractContainer) {
				final AbstractContainer ac = (AbstractContainer) op;

				try {
					saveObject.put(this.checkName("AbstractContainer",
							"AbstractContainer", 0), ac.toJSON());
				} catch (final JSONException e) {
					e.printStackTrace();
				}

			}

			// Variable
			if (op instanceof VariableOperator) {
				final VariableOperator vo = (VariableOperator) op;
				if (!vo.isChild()) {
					try {
						saveObject.put(this.checkName("VariableOperator",
								"VariableOperator", 0), vo.toJSON());
					} catch (final JSONException e) {
						e.printStackTrace();
					}
				}
			}

			// Constant
			if (op instanceof ConstantOperator) {
				final ConstantOperator co = (ConstantOperator) op;
				if (!co.isChild()) {
					try {
						saveObject.put(this.checkName("ConstantOperator",
								"ConstantOperator", 0), co.toJSON());
					} catch (final JSONException e) {
						e.printStackTrace();
					}
				}
			}


		}


		return saveObject;
	}

	public void fromJSON(final JSONObject loadRuleGraph) throws JSONException{
		@SuppressWarnings("unchecked")
		final
		Iterator<String> keyIt = loadRuleGraph.keys();
		JSONObject termsObject = null;

		while (keyIt.hasNext()) {
			final String opName = keyIt.next();

			final JSONObject operatorObject = loadRuleGraph.getJSONObject(opName);

			if( operatorObject.has("TERMS") ) {
				termsObject = operatorObject.getJSONObject("TERMS");
			}

			Operator op = null;
			// Connections
			if (opName.equals("CONNECTIONS")) {
				continue;
			}
			// UnitermOperator
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"UnitermOperator" ) && (termsObject != null)) {

				final UnitermOperator uniTerm = new UnitermOperator();
				uniTerm.fromJSON(operatorObject, uniTerm, this);

				uniTerm.setConstantComboBoxEntries(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
				uniTerm.setVisualRifEditor(this.visualRifEditor);
				uniTerm.setTermName(operatorObject.getString("TERMNAME"));
				uniTerm.getUniTermComboBox().setSelectedItem(operatorObject.getString("SELECTEDPREFIX"));
				uniTerm.setSelectedPrefix(operatorObject.getString("SELECTEDPREFIX"));
				uniTerm.setExternal(operatorObject.getBoolean("EXTERNAL"));
				uniTerm.setNamed(operatorObject.getBoolean("NAMED"));

				// get savedTerms
				final HashMap<String,Term> unsortedTerms = this.getSavedTerms(termsObject,uniTerm);

				// sort terms
				final LinkedList<Term> terms =  this.sortTerms(unsortedTerms);

				uniTerm.setTerms(terms);

				op = uniTerm;
			} // end UnitermOperator

			// ListOperator
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"ListOperator") && (termsObject != null)) {

				final ListOperator listOp = new ListOperator();

				listOp.fromJSON(operatorObject, listOp, this);

				listOp.setConstantComboBoxEntries(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
				listOp.setVisualRifEditor(this.visualRifEditor);
				listOp.setOpen(operatorObject.getBoolean("ISOPEN"));

				// get savedTerms
				final HashMap<String,Term> unsortedTerms = this.getSavedTerms(termsObject,listOp);

				// sort terms
				final LinkedList<Term> terms =  this.sortTerms(unsortedTerms);

				listOp.setTerms(terms);

				op = listOp;
			} // end ListOperator

			// FrameOperator
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"FrameOperator") && (termsObject != null)) {

				final FrameOperator frameOp = new FrameOperator();
				frameOp.setConstantComboBoxEntries(this.visualRifEditor.getDocumentContainer().getActiveDocument().getDocumentEditorPane().getPrefixList());
				frameOp.setVisualRifEditor(this.visualRifEditor);


				// get savedTerms
				final HashMap<String,Term> unsortedTerms = this.getSavedTerms(termsObject,frameOp);

				// sort terms
				final LinkedList<Term> terms =  this.sortTerms(unsortedTerms);

				frameOp.setTerms(terms);

				op = frameOp;
			} // end FrameOperator

			// AND
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"and")) {
				final AndContainer andContainer = new AndContainer();
				andContainer.setVisualRifEditor(this.visualRifEditor);
				andContainer.fromJSON(operatorObject.getJSONObject("OPERATORGRAPH"));
				op = andContainer;
			}

			// Or
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"or")) {
				final OrContainer orContainer = new OrContainer();
				orContainer.setVisualRifEditor(this.visualRifEditor);
				orContainer.fromJSON(operatorObject.getJSONObject("OPERATORGRAPH"));
				op = orContainer;
			}

			// Exists
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"exists")) {
				final ExistsContainer existsContainer = new ExistsContainer();
				existsContainer.setVisualRifEditor(this.visualRifEditor);
				existsContainer.fromJSON(operatorObject.getJSONObject("OPERATORGRAPH"));
				op = existsContainer;
			}

			// Variable
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"VariableOperator")) {
				final VariableOperator variableOperator = new VariableOperator();
				variableOperator.setVisualRifEditor(this.visualRifEditor);
				variableOperator.fromJSON(operatorObject,variableOperator,this);
				op = variableOperator;
			}

			// Constant
			if (operatorObject.has("OP TYPE")
					&& operatorObject.getString("OP TYPE").equals(
							"ConstantOperator")) {
				final ConstantOperator constantOperator = new ConstantOperator();
				constantOperator.setVisualRifEditor(this.visualRifEditor);
				constantOperator.fromJSON(operatorObject, constantOperator, this);
				op = constantOperator;
			}

			System.out.println(operatorObject.getString("OP TYPE"));
			final JSONArray positionArray = operatorObject.getJSONArray("POSITION");

			if (this.isRecursiveOperatorGraph() ) {
				System.out.println("this.isRecursiveOperatorGraph() "+op.getClass().getSimpleName());

				this.operatorContainer.addOperator(op);
			}

			this.addOperator(positionArray.getInt(0), positionArray.getInt(1), op);
		}
	}


	private HashMap<String, Term> getSavedTerms(final JSONObject termsObject, final AbstractTermOperator operator) throws JSONException {
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

	private LinkedList<Term> sortTerms(final HashMap<String, Term> unsortedTerms) {
		final LinkedList<Term> terms = new LinkedList<Term>();
		final TreeMap<String, Term> treeMap = new TreeMap<String, Term>();
		treeMap.putAll(unsortedTerms);
		for (final Entry<String, Term> entry : treeMap.entrySet()) {
			terms.add(entry.getValue());
		}
		return terms;
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

		if (this.operatorNames.size() > 0) {
			for (int i = 0; i < this.operatorNames.size(); i++) {
				if (newname.equalsIgnoreCase(this.operatorNames.get(i))) {
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
		this.operatorNames.add(newname);
		return newname;
	}

	public boolean isRecursiveOperatorGraph() {
		return this.recursiveOperatorGraph;
	}

	public void setRecursiveOperatorGraph(final boolean recursiveOperatorGraph) {
		this.recursiveOperatorGraph = recursiveOperatorGraph;
	}

	public VisualRifEditor getVisualRifEditor(){
		return this.visualRifEditor;
	}


	public VisualEditor<Operator> getVisualEditor() {
		return this.visualEditor;
	}

	public void setVisualEditor(final VisualEditor<Operator> visualEditor) {
		this.visualEditor = visualEditor;
	}

	public void setRuleVariableList(final LinkedList<Term> ruleVariableList) {
		this.ruleVariableList = ruleVariableList;
	}

	public LinkedList<Term> getRuleVariableList() {
		return this.ruleVariableList;
	}
}
