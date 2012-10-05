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
	public Term (String value){
		
		this.setValue(value);
		
		if(this.getSelectedPrefix().equals("Var")){
			this.setVariable(true);
		}else
			this.setVariable(false);
	}
	
	
	// save term
	public JSONObject toJSON() {
		JSONObject saveObject = new JSONObject();
		
		try {
			
			if(this.variable) saveObject.put("TYPE", "variable");
			if(this.constant) saveObject.put("TYPE", "constant");
			if(this.uniterm) saveObject.put("TYPE", "expression");
			if(this.list) saveObject.put("TYPE", "list");
			saveObject.put("PREFIXVALUE", this.selectedPrefix);
			saveObject.put("VALUE", this.value);
			saveObject.put("PREFIXNAME", this.prefixForName);
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		
		return saveObject;
	}

	
	public void setSelectedPrefix(String selectedPrefix) {

		this.selectedPrefix = selectedPrefix;
		
//		if(this.getSelectedPrefix().equals("Var")){
//			this.setVariable(true);
//		}else
//			this.setVariable(false);
	
	}
	
	
	/* *************** **
	 * Getter + Setter **
	 * *************** */ 
	
	public boolean isVariable() {
		return variable;
	}

	public void setVariable(boolean variable) {
		this.variable = variable;
	}
	
    public boolean isConstant() {
		return constant;
	}
	
	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public boolean isUniterm() {
		return uniterm;
	}

	public void setUniterm(boolean uniterm) {
		this.uniterm = uniterm;
	}

	public boolean isList() {
		return list;
	}

	public void setList(boolean list) {
		this.list = list;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSelectedPrefix() {
		return selectedPrefix;
	}
	
	public void setConstantCombo(JComboBox constantCombo) {
		this.constantCombo = constantCombo;
	}

	public JComboBox getConstantCombo() {
		return constantCombo;
	}

	public void setPrefix(boolean prefix) {
		this.prefix = prefix;
		
	}

	public boolean isPrefix(){
		return this.prefix;
	}

	public JButton getUpButton() {
		return upButton;
	}

	public void setUpButton(JButton upButton) {
		this.upButton = upButton;
	}

	public JButton getDownButton() {
		return downButton;
	}

	public void setDownButton(JButton downButton) {
		this.downButton = downButton;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(JButton deleteButton) {
		this.deleteButton = deleteButton;
	}

	public JLabel getLabel() {
		return label;
	}

	public void setLabel(JLabel label) {
		this.label = label;
	}

	public void setTextFieldResizing(JTextFieldResizing textFieldResizing) {
		this.tfValue = textFieldResizing;
	}

	public JTextFieldResizing getTextFieldResizing() {
		return tfValue;
	}

	public JTextFieldResizing getTfName() {
		return tfName;
	}

	public void setTfName(JTextFieldResizing tfName) {
		this.tfName = tfName;
	}

	public JComboBox getNameComboBox() {
		return nameComboBox;
	}
	
	public void setNameComboBox(JComboBox nameComboBox) {
		this.nameComboBox = nameComboBox;
	}

	public String getPrefixForName() {
		return prefixForName;
	}
	
	public void setPrefixForName(String prefixForName) {
		this.prefixForName = prefixForName;
	}

	public boolean isInit() {
		return init;
	}

	public void setInit(boolean init) {
		this.init = init;
	}

	public String[] getComboEntries() {
		return comboEntries;
	}

	public void setComboEntries(String[] comboEntries) {
		this.comboEntries = comboEntries;
	}

	public void setTermFrameID(int termID) {
		this.termFrameID = termID;
		
	}
	
	public int getTermFrameID(){
		return this.termFrameID;
	}

	public void setTypCombo(JComboBox typCombo) {
		this.termTypCombo = typCombo;
		
	}
	
	public JComboBox getTypCombo(){
		return this.termTypCombo;
	}


	public boolean isFrame() {
		return frame;
	}

	public void setFrame(boolean frame) {
		this.frame = frame;
	}

	public JButton getConnectionButton() {
		return connectionButton;
	}

	public void setConnectionButton(JButton connectionButton) {
		this.connectionButton = connectionButton;
	}

	
	public void setSucceedingOperatorGW(GraphWrapper graphWrapper) {
		this.succeedingOperatorGW = graphWrapper;
		
	}

	public GraphWrapper getSucceedingOperatorGW(){
		return this.succeedingOperatorGW;
	}

	public boolean hasSucceedingOperator() {
		return hasSucceedingOperator;
	}

	public void setHasSucceedingOperator(boolean hasSucceedingOperator) {
		this.hasSucceedingOperator = hasSucceedingOperator;
	}

	public void setSucceedingOperatorBox(GraphBox childBox) {
		// TODO Auto-generated method stub
		this.childBox = childBox;
	}
	
	public GraphBox getSucceedingOperatorBox(){
		return this.childBox;
	}

	public void setSucceedingOperator(GraphWrapperEditable secondOp) {
		this.secondOp = secondOp;
		
	}
	
	public GraphWrapperEditable getSucceedingOperator(){
		return this.secondOp;
	}

	public void setDummyOperator(Operator dummyOperator) {
		this.dummyOperator = dummyOperator;
		
	}
	
	public Operator getDummyOperator(){
		return this.dummyOperator;
	}

	
	public AbstractTermOperator getAbstractTermOperator() {
		return abstractTermOperator;
	}
	

	public void setAbstractTermOperator(AbstractTermOperator abstractTermOperator) {
		this.abstractTermOperator = abstractTermOperator;
	}

	public JLabel getTermLabel() {
		return termLabel;
	}

	public void setTermLabel(JLabel termLabel) {
		this.termLabel = termLabel;
	}



	
}
