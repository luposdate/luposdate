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
package lupos.gui.operatorgraph.visualeditor.visualrif.util;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import lupos.gui.operatorgraph.GraphBox;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator;

import org.json.JSONException;
import org.json.JSONObject;
public class Term {

	private boolean init = false;

	// type
	private	boolean variable = false;
	private boolean constant = false;
	private boolean uniterm = false;
	private boolean list = false;
	private boolean frame = false;

	// prefix
	private boolean prefix = false;
	private String selectedPrefix = "";

	// constant or variable value  // (named UnitermOperator)
	private String value, prefixForName;

	/*
	 * Swing elements
	 */
	private JTextFieldResizing tfName, tfValue;
	private JButton upButton , downButton, deleteButton, connectionButton;
	private JLabel label;

	// PrefixCombo for Value
	private JComboBox constantCombo;

	//PrefixCombo for Name (named unitermOperator)
	private JComboBox nameComboBox;

	private String[] comboEntries;

	// special frame fields
	private int termFrameID;
	private JComboBox termTypCombo;

	private JLabel termLabel;

	/*
	 * term as succeeding operator
	 */
	private GraphWrapper succeedingOperatorGW;
	private Operator dummyOperator;
	private boolean hasSucceedingOperator = false;
	private GraphBox childBox;
	private GraphWrapperEditable secondOp;

	private AbstractTermOperator abstractTermOperator;

	// Constructor
	/**
	 * <p>Constructor for Term.</p>
	 */
	public Term(){}

	// Constructor
	/**
	 * <p>Constructor for Term.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 */
	public Term (final String value){
		this.setValue(value);
		if(this.getSelectedPrefix().equals("Var")){
			this.setVariable(true);
		} else {
			this.setVariable(false);
		}
	}

	// save term
	/**
	 * <p>toJSON.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 */
	public JSONObject toJSON() {
		final JSONObject saveObject = new JSONObject();
		try {

			if(this.variable) {
				saveObject.put("TYPE", "variable");
			}
			if(this.constant) {
				saveObject.put("TYPE", "constant");
			}
			if(this.uniterm) {
				saveObject.put("TYPE", "expression");
			}
			if(this.list) {
				saveObject.put("TYPE", "list");
			}
			saveObject.put("PREFIXVALUE", this.selectedPrefix);
			saveObject.put("VALUE", this.value);
			saveObject.put("PREFIXNAME", this.prefixForName);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return saveObject;
	}

	/**
	 * <p>Setter for the field <code>selectedPrefix</code>.</p>
	 *
	 * @param selectedPrefix a {@link java.lang.String} object.
	 */
	public void setSelectedPrefix(final String selectedPrefix) {
		this.selectedPrefix = selectedPrefix;
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */
	/**
	 * <p>isVariable.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isVariable() {
		return this.variable;
	}

	/**
	 * <p>Setter for the field <code>variable</code>.</p>
	 *
	 * @param variable a boolean.
	 */
	public void setVariable(final boolean variable) {
		this.variable = variable;
	}

	/**
	 * <p>isConstant.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isConstant() {
		return this.constant;
	}

	/**
	 * <p>Setter for the field <code>constant</code>.</p>
	 *
	 * @param constant a boolean.
	 */
	public void setConstant(final boolean constant) {
		this.constant = constant;
	}

	/**
	 * <p>isUniterm.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isUniterm() {
		return this.uniterm;
	}

	/**
	 * <p>Setter for the field <code>uniterm</code>.</p>
	 *
	 * @param uniterm a boolean.
	 */
	public void setUniterm(final boolean uniterm) {
		this.uniterm = uniterm;
	}

	/**
	 * <p>isList.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isList() {
		return this.list;
	}

	/**
	 * <p>Setter for the field <code>list</code>.</p>
	 *
	 * @param list a boolean.
	 */
	public void setList(final boolean list) {
		this.list = list;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * <p>Getter for the field <code>selectedPrefix</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSelectedPrefix() {
		return this.selectedPrefix;
	}

	/**
	 * <p>Setter for the field <code>constantCombo</code>.</p>
	 *
	 * @param constantCombo a {@link javax.swing.JComboBox} object.
	 */
	public void setConstantCombo(final JComboBox constantCombo) {
		this.constantCombo = constantCombo;
	}

	/**
	 * <p>Getter for the field <code>constantCombo</code>.</p>
	 *
	 * @return a {@link javax.swing.JComboBox} object.
	 */
	public JComboBox getConstantCombo() {
		return this.constantCombo;
	}

	/**
	 * <p>Setter for the field <code>prefix</code>.</p>
	 *
	 * @param prefix a boolean.
	 */
	public void setPrefix(final boolean prefix) {
		this.prefix = prefix;
	}

	/**
	 * <p>isPrefix.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPrefix(){
		return this.prefix;
	}

	/**
	 * <p>Getter for the field <code>upButton</code>.</p>
	 *
	 * @return a {@link javax.swing.JButton} object.
	 */
	public JButton getUpButton() {
		return this.upButton;
	}

	/**
	 * <p>Setter for the field <code>upButton</code>.</p>
	 *
	 * @param upButton a {@link javax.swing.JButton} object.
	 */
	public void setUpButton(final JButton upButton) {
		this.upButton = upButton;
	}

	/**
	 * <p>Getter for the field <code>downButton</code>.</p>
	 *
	 * @return a {@link javax.swing.JButton} object.
	 */
	public JButton getDownButton() {
		return this.downButton;
	}

	/**
	 * <p>Setter for the field <code>downButton</code>.</p>
	 *
	 * @param downButton a {@link javax.swing.JButton} object.
	 */
	public void setDownButton(final JButton downButton) {
		this.downButton = downButton;
	}

	/**
	 * <p>Getter for the field <code>deleteButton</code>.</p>
	 *
	 * @return a {@link javax.swing.JButton} object.
	 */
	public JButton getDeleteButton() {
		return this.deleteButton;
	}

	/**
	 * <p>Setter for the field <code>deleteButton</code>.</p>
	 *
	 * @param deleteButton a {@link javax.swing.JButton} object.
	 */
	public void setDeleteButton(final JButton deleteButton) {
		this.deleteButton = deleteButton;
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link javax.swing.JLabel} object.
	 */
	public JLabel getLabel() {
		return this.label;
	}

	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link javax.swing.JLabel} object.
	 */
	public void setLabel(final JLabel label) {
		this.label = label;
	}

	/**
	 * <p>setTextFieldResizing.</p>
	 *
	 * @param textFieldResizing a {@link lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing} object.
	 */
	public void setTextFieldResizing(final JTextFieldResizing textFieldResizing) {
		this.tfValue = textFieldResizing;
	}

	/**
	 * <p>getTextFieldResizing.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing} object.
	 */
	public JTextFieldResizing getTextFieldResizing() {
		return this.tfValue;
	}

	/**
	 * <p>Getter for the field <code>tfName</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing} object.
	 */
	public JTextFieldResizing getTfName() {
		return this.tfName;
	}

	/**
	 * <p>Setter for the field <code>tfName</code>.</p>
	 *
	 * @param tfName a {@link lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing} object.
	 */
	public void setTfName(final JTextFieldResizing tfName) {
		this.tfName = tfName;
	}

	/**
	 * <p>Getter for the field <code>nameComboBox</code>.</p>
	 *
	 * @return a {@link javax.swing.JComboBox} object.
	 */
	public JComboBox getNameComboBox() {
		return this.nameComboBox;
	}

	/**
	 * <p>Setter for the field <code>nameComboBox</code>.</p>
	 *
	 * @param nameComboBox a {@link javax.swing.JComboBox} object.
	 */
	public void setNameComboBox(final JComboBox nameComboBox) {
		this.nameComboBox = nameComboBox;
	}

	/**
	 * <p>Getter for the field <code>prefixForName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPrefixForName() {
		return this.prefixForName;
	}

	/**
	 * <p>Setter for the field <code>prefixForName</code>.</p>
	 *
	 * @param prefixForName a {@link java.lang.String} object.
	 */
	public void setPrefixForName(final String prefixForName) {
		this.prefixForName = prefixForName;
	}

	/**
	 * <p>isInit.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isInit() {
		return this.init;
	}

	/**
	 * <p>Setter for the field <code>init</code>.</p>
	 *
	 * @param init a boolean.
	 */
	public void setInit(final boolean init) {
		this.init = init;
	}

	/**
	 * <p>Getter for the field <code>comboEntries</code>.</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getComboEntries() {
		return this.comboEntries;
	}

	/**
	 * <p>Setter for the field <code>comboEntries</code>.</p>
	 *
	 * @param comboEntries an array of {@link java.lang.String} objects.
	 */
	public void setComboEntries(final String[] comboEntries) {
		this.comboEntries = comboEntries;
	}

	/**
	 * <p>Setter for the field <code>termFrameID</code>.</p>
	 *
	 * @param termID a int.
	 */
	public void setTermFrameID(final int termID) {
		this.termFrameID = termID;
	}

	/**
	 * <p>Getter for the field <code>termFrameID</code>.</p>
	 *
	 * @return a int.
	 */
	public int getTermFrameID(){
		return this.termFrameID;
	}

	/**
	 * <p>setTypCombo.</p>
	 *
	 * @param typCombo a {@link javax.swing.JComboBox} object.
	 */
	public void setTypCombo(final JComboBox typCombo) {
		this.termTypCombo = typCombo;
	}

	/**
	 * <p>getTypCombo.</p>
	 *
	 * @return a {@link javax.swing.JComboBox} object.
	 */
	public JComboBox getTypCombo(){
		return this.termTypCombo;
	}

	/**
	 * <p>isFrame.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFrame() {
		return this.frame;
	}

	/**
	 * <p>Setter for the field <code>frame</code>.</p>
	 *
	 * @param frame a boolean.
	 */
	public void setFrame(final boolean frame) {
		this.frame = frame;
	}

	/**
	 * <p>Getter for the field <code>connectionButton</code>.</p>
	 *
	 * @return a {@link javax.swing.JButton} object.
	 */
	public JButton getConnectionButton() {
		return this.connectionButton;
	}

	/**
	 * <p>Setter for the field <code>connectionButton</code>.</p>
	 *
	 * @param connectionButton a {@link javax.swing.JButton} object.
	 */
	public void setConnectionButton(final JButton connectionButton) {
		this.connectionButton = connectionButton;
	}

	/**
	 * <p>Setter for the field <code>succeedingOperatorGW</code>.</p>
	 *
	 * @param graphWrapper a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 */
	public void setSucceedingOperatorGW(final GraphWrapper graphWrapper) {
		this.succeedingOperatorGW = graphWrapper;
	}

	/**
	 * <p>Getter for the field <code>succeedingOperatorGW</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 */
	public GraphWrapper getSucceedingOperatorGW(){
		return this.succeedingOperatorGW;
	}

	/**
	 * <p>hasSucceedingOperator.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasSucceedingOperator() {
		return this.hasSucceedingOperator;
	}

	/**
	 * <p>Setter for the field <code>hasSucceedingOperator</code>.</p>
	 *
	 * @param hasSucceedingOperator a boolean.
	 */
	public void setHasSucceedingOperator(final boolean hasSucceedingOperator) {
		this.hasSucceedingOperator = hasSucceedingOperator;
	}

	/**
	 * <p>setSucceedingOperatorBox.</p>
	 *
	 * @param childBox a {@link lupos.gui.operatorgraph.GraphBox} object.
	 */
	public void setSucceedingOperatorBox(final GraphBox childBox) {
		this.childBox = childBox;
	}

	/**
	 * <p>getSucceedingOperatorBox.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.GraphBox} object.
	 */
	public GraphBox getSucceedingOperatorBox(){
		return this.childBox;
	}

	/**
	 * <p>setSucceedingOperator.</p>
	 *
	 * @param secondOp a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable} object.
	 */
	public void setSucceedingOperator(final GraphWrapperEditable secondOp) {
		this.secondOp = secondOp;
	}

	/**
	 * <p>getSucceedingOperator.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapperEditable} object.
	 */
	public GraphWrapperEditable getSucceedingOperator(){
		return this.secondOp;
	}

	/**
	 * <p>Setter for the field <code>dummyOperator</code>.</p>
	 *
	 * @param dummyOperator a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public void setDummyOperator(final Operator dummyOperator) {
		this.dummyOperator = dummyOperator;
	}

	/**
	 * <p>Getter for the field <code>dummyOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public Operator getDummyOperator(){
		return this.dummyOperator;
	}

	/**
	 * <p>Getter for the field <code>abstractTermOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator} object.
	 */
	public AbstractTermOperator getAbstractTermOperator() {
		return this.abstractTermOperator;
	}

	/**
	 * <p>Setter for the field <code>abstractTermOperator</code>.</p>
	 *
	 * @param abstractTermOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.AbstractTermOperator} object.
	 */
	public void setAbstractTermOperator(final AbstractTermOperator abstractTermOperator) {
		this.abstractTermOperator = abstractTermOperator;
	}

	/**
	 * <p>Getter for the field <code>termLabel</code>.</p>
	 *
	 * @return a {@link javax.swing.JLabel} object.
	 */
	public JLabel getTermLabel() {
		return this.termLabel;
	}

	/**
	 * <p>Setter for the field <code>termLabel</code>.</p>
	 *
	 * @param termLabel a {@link javax.swing.JLabel} object.
	 */
	public void setTermLabel(final JLabel termLabel) {
		this.termLabel = termLabel;
	}
}
