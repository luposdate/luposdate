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
	public Term(){}

	// Constructor
	public Term (final String value){
		this.setValue(value);
		if(this.getSelectedPrefix().equals("Var")){
			this.setVariable(true);
		} else {
			this.setVariable(false);
		}
	}

	// save term
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

	public void setSelectedPrefix(final String selectedPrefix) {
		this.selectedPrefix = selectedPrefix;
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */
	public boolean isVariable() {
		return this.variable;
	}

	public void setVariable(final boolean variable) {
		this.variable = variable;
	}

	public boolean isConstant() {
		return this.constant;
	}

	public void setConstant(final boolean constant) {
		this.constant = constant;
	}

	public boolean isUniterm() {
		return this.uniterm;
	}

	public void setUniterm(final boolean uniterm) {
		this.uniterm = uniterm;
	}

	public boolean isList() {
		return this.list;
	}

	public void setList(final boolean list) {
		this.list = list;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getSelectedPrefix() {
		return this.selectedPrefix;
	}

	public void setConstantCombo(final JComboBox constantCombo) {
		this.constantCombo = constantCombo;
	}

	public JComboBox getConstantCombo() {
		return this.constantCombo;
	}

	public void setPrefix(final boolean prefix) {
		this.prefix = prefix;
	}

	public boolean isPrefix(){
		return this.prefix;
	}

	public JButton getUpButton() {
		return this.upButton;
	}

	public void setUpButton(final JButton upButton) {
		this.upButton = upButton;
	}

	public JButton getDownButton() {
		return this.downButton;
	}

	public void setDownButton(final JButton downButton) {
		this.downButton = downButton;
	}

	public JButton getDeleteButton() {
		return this.deleteButton;
	}

	public void setDeleteButton(final JButton deleteButton) {
		this.deleteButton = deleteButton;
	}

	public JLabel getLabel() {
		return this.label;
	}

	public void setLabel(final JLabel label) {
		this.label = label;
	}

	public void setTextFieldResizing(final JTextFieldResizing textFieldResizing) {
		this.tfValue = textFieldResizing;
	}

	public JTextFieldResizing getTextFieldResizing() {
		return this.tfValue;
	}

	public JTextFieldResizing getTfName() {
		return this.tfName;
	}

	public void setTfName(final JTextFieldResizing tfName) {
		this.tfName = tfName;
	}

	public JComboBox getNameComboBox() {
		return this.nameComboBox;
	}

	public void setNameComboBox(final JComboBox nameComboBox) {
		this.nameComboBox = nameComboBox;
	}

	public String getPrefixForName() {
		return this.prefixForName;
	}

	public void setPrefixForName(final String prefixForName) {
		this.prefixForName = prefixForName;
	}

	public boolean isInit() {
		return this.init;
	}

	public void setInit(final boolean init) {
		this.init = init;
	}

	public String[] getComboEntries() {
		return this.comboEntries;
	}

	public void setComboEntries(final String[] comboEntries) {
		this.comboEntries = comboEntries;
	}

	public void setTermFrameID(final int termID) {
		this.termFrameID = termID;
	}

	public int getTermFrameID(){
		return this.termFrameID;
	}

	public void setTypCombo(final JComboBox typCombo) {
		this.termTypCombo = typCombo;
	}

	public JComboBox getTypCombo(){
		return this.termTypCombo;
	}

	public boolean isFrame() {
		return this.frame;
	}

	public void setFrame(final boolean frame) {
		this.frame = frame;
	}

	public JButton getConnectionButton() {
		return this.connectionButton;
	}

	public void setConnectionButton(final JButton connectionButton) {
		this.connectionButton = connectionButton;
	}

	public void setSucceedingOperatorGW(final GraphWrapper graphWrapper) {
		this.succeedingOperatorGW = graphWrapper;
	}

	public GraphWrapper getSucceedingOperatorGW(){
		return this.succeedingOperatorGW;
	}

	public boolean hasSucceedingOperator() {
		return this.hasSucceedingOperator;
	}

	public void setHasSucceedingOperator(final boolean hasSucceedingOperator) {
		this.hasSucceedingOperator = hasSucceedingOperator;
	}

	public void setSucceedingOperatorBox(final GraphBox childBox) {
		this.childBox = childBox;
	}

	public GraphBox getSucceedingOperatorBox(){
		return this.childBox;
	}

	public void setSucceedingOperator(final GraphWrapperEditable secondOp) {
		this.secondOp = secondOp;
	}

	public GraphWrapperEditable getSucceedingOperator(){
		return this.secondOp;
	}

	public void setDummyOperator(final Operator dummyOperator) {
		this.dummyOperator = dummyOperator;
	}

	public Operator getDummyOperator(){
		return this.dummyOperator;
	}

	public AbstractTermOperator getAbstractTermOperator() {
		return this.abstractTermOperator;
	}

	public void setAbstractTermOperator(final AbstractTermOperator abstractTermOperator) {
		this.abstractTermOperator = abstractTermOperator;
	}

	public JLabel getTermLabel() {
		return this.termLabel;
	}

	public void setTermLabel(final JLabel termLabel) {
		this.termLabel = termLabel;
	}
}
