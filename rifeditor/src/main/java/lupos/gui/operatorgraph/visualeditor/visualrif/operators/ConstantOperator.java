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
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.JComboBox;

import lupos.gui.operatorgraph.AbstractSuperGuiComponent;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ClassificationOperatorPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel.ConstantPanel;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
import lupos.misc.util.OperatorIDTuple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConstantOperator extends Operator  {

	private VisualRifEditor visualRifEditor;
	private String selectedClassification = "=";
	private String constant = "";
	private boolean[] selectedRadioButton = { false, false, false };
	private boolean isChild = false;
	private JComboBox constantComboBox = new JComboBox();
	private String selectedPrefix = "";
	private String[] comboBoxEntries;
	private FocusListener comboBoxFocusListener;

	//Constructor
	public ConstantOperator(){}

	@Override
	public void prefixAdded() {}

	@Override
	public void prefixModified(final String arg0, final String arg1) {}

	@Override
	public void prefixRemoved(final String arg0, final String arg1) {}

	@Override
	public boolean variableInUse(final String arg0, final HashSet<Operator> arg1) {
		return false;
	}

	@Override
	public boolean validateOperator(final boolean showErrors, final HashSet<Operator> visited, final Object data) {
		return true;
	}

	@Override
	public AbstractGuiComponent<Operator> draw(final GraphWrapper gw,
			final VisualGraph<Operator> parent) {
		this.panel = new ConstantPanel(parent, gw, this, true, this.visualRifEditor);
		return this.panel;
	}

	public Hashtable<GraphWrapper, AbstractSuperGuiComponent> drawAnnotations(final VisualRIFGraph<Operator> parent) {
		final Hashtable<GraphWrapper, AbstractSuperGuiComponent> predicates = new Hashtable<GraphWrapper, AbstractSuperGuiComponent>();
		// walk through children
		for(final OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()) {
			if (opIDTuple.getOperator() instanceof UnitermOperator){
				final UnitermOperator child = (UnitermOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
				// create predicate panel...
				final ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
				this.annotationLabels.put(child, classificationOperatorPanel);
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
			}
			if (opIDTuple.getOperator() instanceof ConstantOperator){
				final ConstantOperator child = (ConstantOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
				// create predicate panel...
				final ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
				this.annotationLabels.put(child, classificationOperatorPanel);
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
			}
			if (opIDTuple.getOperator() instanceof VariableOperator){
				final VariableOperator child = (VariableOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
				// create predicate panel...
				final ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
				this.annotationLabels.put(child, classificationOperatorPanel);
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
			}
			if (opIDTuple.getOperator() instanceof ListOperator){
				final ListOperator child = (ListOperator) opIDTuple.getOperator(); // get current children
				child.setChild(true);
				// create predicate panel...
				final ClassificationOperatorPanel classificationOperatorPanel = new ClassificationOperatorPanel(parent, this, child);
				this.annotationLabels.put(child, classificationOperatorPanel);
				// add predicate panel to hash table with its GraphWrapper...
				predicates.put(new GraphWrapperOperator(child), classificationOperatorPanel);
			}
		}
		return predicates;
	}

	@Override
	public StringBuffer serializeOperator() {
		final StringBuffer sb = new StringBuffer();
		if ( this.selectedPrefix.equals("integer") ){
			sb.append(this.constant);
		} else
			if ( this.selectedPrefix.endsWith("#string") || this.selectedPrefix.endsWith("#integer") ){
				String[] tmp = new String[2];
				tmp = this.selectedPrefix.split("#");
				final String iri = "\""+this.constant+"\"^^"+tmp[0]+":"+tmp[1];
				sb.append(iri);
			} else {
				sb.append(this.selectedPrefix+":"+this.constant);
			}
		return sb;
	}

	@Override
	public StringBuffer serializeOperatorAndTree(final HashSet<Operator> visited) {
		final StringBuffer sb = new StringBuffer();
		if(this.getSucceedingOperators().size() < 1){
			this.selectedClassification = "";
		} else {
			this.selectedClassification = "=";
		}

		if(!this.isChild){
			if ( this.selectedPrefix.equals("integer") ){
				sb.append(this.constant);
			} else
				if ( this.selectedPrefix.endsWith("#string") || this.selectedPrefix.endsWith("#integer") ){
					String[] tmp = new String[2];
					tmp = this.selectedPrefix.split("#");
					final String iri = "\""+this.constant+"\"^^"+tmp[0]+":"+tmp[1]+" "+this.selectedClassification+" ";
					sb.append(iri);
				} else {
					sb.append(this.selectedPrefix+":"+this.constant+" "+this.selectedClassification+" ");
				}
			for(final OperatorIDTuple<Operator> opIDT : this.getSucceedingOperators()) {
				sb.append(opIDT.getOperator().serializeOperator());
			}
		}
		return sb;
	}

	public JSONObject toJSON() throws JSONException {
		final JSONObject saveObject = new JSONObject();

		saveObject.put("OP TYPE", this.getClass().getSimpleName());

		saveObject.put("VALUE", this.getConstant());

		saveObject.put("ISCONNECTED", !this.getSucceedingOperators().isEmpty());

		final Point position = ((ConstantPanel) this.panel).getPositionAndDimension().getFirst();

		saveObject.put("POSITION", new double[]{position.getX(), position.getY()});

		if(!this.getSucceedingOperators().isEmpty()){

			saveObject.put("SELECTEDCLASSIFICTION", this.getSelectedClassification());

			for(final OperatorIDTuple<Operator> opIDTuple : this.getSucceedingOperators()) {

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
			}
		}
		return saveObject;
	}

	public void fromJSON(final JSONObject operatorObject, final ConstantOperator constantOperator,final VisualRIFGraph<Operator> parent) throws JSONException {
		constantOperator.setConstant(operatorObject.get("VALUE").toString());
		final boolean isConnected = operatorObject.getBoolean("ISCONNECTED");
		if (isConnected) {
			JSONObject loadObject = new JSONObject();
			loadObject = (JSONObject) operatorObject.get("CONNECTEDOPERATOR");
			constantOperator.setSelectedClassification(operatorObject.getString("SELECTEDCLASSIFICTION"));

			if (constantOperator.getSelectedClassification().equals("=")) {
				final boolean[] equality = { true, false, false };
				constantOperator.setSelectedRadioButton(equality);
			} else
				if (constantOperator.getSelectedClassification().equals("#")) {
					final boolean[] membership = { false, true, false };
					constantOperator.setSelectedRadioButton(membership);

				} else if (constantOperator.getSelectedClassification()
						.equals("##")) {
					final boolean[] subclass = { false, false, true };
					constantOperator.setSelectedRadioButton(subclass);
				}

			// Constant
			if ( loadObject.get("OP TYPE").equals("ConstantOperator") ){
				final ConstantOperator child = new ConstantOperator();
				child.fromJSON(loadObject, constantOperator, parent);
				child.setChild(true);

				final OperatorIDTuple<Operator> oidtConst = new OperatorIDTuple<Operator> (child, 0);
				constantOperator.addSucceedingOperator(oidtConst);

				final JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1), child);
			} // end constant

			// Variable
			if ( loadObject.get("OP TYPE").equals("VariableOperator") ){
				final VariableOperator child = new VariableOperator();
				child.fromJSON(loadObject, child, parent);
				child.setChild(true);

				final OperatorIDTuple<Operator> oidtVar = new OperatorIDTuple<Operator> (child, 0);
				constantOperator.addSucceedingOperator(oidtVar);

				final JSONArray positionArray = loadObject.getJSONArray("POSITION");
				parent.addOperator(positionArray.getInt(0), positionArray.getInt(1), child);
			} // end Variable
		}

	}

	public void setOpID(final String opIDLabel, final boolean boolean1) {
	}

	public void setSelectedClassification(final String selectedClassification) {
		this.selectedClassification = selectedClassification;
	}

	public String getSelectedClassification() {
		return this.selectedClassification;
	}

	public void setConstant(final String constant) {
		this.constant = constant;
	}

	public String getConstant() {
		return this.constant;
	}

	public void setSelectedRadioButton(final boolean[] selectedRadioButton) {
		this.selectedRadioButton = selectedRadioButton;
	}

	public boolean[] getSelectedRadioButton() {
		return this.selectedRadioButton;
	}

	public void setChild(final boolean isChild) {
		this.isChild = isChild;
	}

	public boolean isChild() {
		return this.isChild;
	}

	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}

	public JComboBox getConstantComboBox() {
		return this.constantComboBox;
	}

	public void setConstantComboBox(final JComboBox constantComboBox) {
		this.constantComboBox = constantComboBox;
	}

	public void setSelectedPrefix(final String string) {
		this.selectedPrefix = string;
	}

	public String getSelectedPrefix() {
		return this.selectedPrefix;
	}

	public String[] getComboBoxEntries() {
		return this.comboBoxEntries;
	}

	public void setComboBoxEntries(final String[] comboBoxEntries) {
		this.comboBoxEntries = comboBoxEntries;
	}

	public void setConstantCombo(final JComboBox constCombo) {
		this.constantComboBox = constCombo;
	}

	public FocusListener getComboBoxFocusListener() {
		return this.comboBoxFocusListener;
	}

	public void setComboBoxFocusListener(final FocusListener comboBoxFocusListener) {
		this.comboBoxFocusListener = comboBoxFocusListener;
	}
}