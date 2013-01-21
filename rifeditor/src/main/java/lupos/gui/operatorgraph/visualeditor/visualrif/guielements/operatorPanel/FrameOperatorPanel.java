/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel;


import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.HintTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.JIconButton;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.TermConnection;

public class FrameOperatorPanel extends AbstractGuiComponent<Operator> {

	private static final long serialVersionUID = 8238554719560169292L;
	

	private VisualRifEditor visualRifEditor;
	protected GridBagConstraints gbc = null;
	private JPanel termRowsPanel;
	protected FrameOperator frameOperator;
	private  String selectedChoiceString = "";
	private String[] comboBoxEntries;
	private JLabel display;
	private int termCounter = 0;
	private FocusListener comboBoxFocusListener;
	String[] typComboEntries = {"Const","Var","Expr","List"};
	 


	// Constructor
	public FrameOperatorPanel(final VisualGraph<Operator> parent,
			GraphWrapper gw, final FrameOperator frameOperator,
			boolean startNode, boolean alsoSubClasses, VisualRifEditor visualRifEditor) {

		super(parent, gw, frameOperator, true);

		this.setVisualRifEditor(visualRifEditor);
	
		this.frameOperator = frameOperator;

		this.init();

		

	}
	

	private void init() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.termRowsPanel = new JPanel(new GridBagLayout());
		this.termRowsPanel.setOpaque(false);

		// Layout
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.CENTER;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;

		this.gbc.insets = new Insets((int) parent.PADDING,
				(int) parent.PADDING, (int) parent.PADDING,
				(int) parent.PADDING);
		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;
		
//		Font font = new Font(Font.SANS_SERIF, Font.ROMAN_BASELINE, 14);
//		LineBorder lineBorder = new LineBorder(Color.black,1);
//		
//		Border empty = BorderFactory.createEmptyBorder();
		
//		TitledBorder titled = BorderFactory.createTitledBorder(raisedbevel," Frame ", 0, 0, font, Color.BLACK);;
//		titled.setTitleJustification(TitledBorder.CENTER);
//		titled.setTitlePosition(TitledBorder.DEFAULT_POSITION);

//		titled = BorderFactory.createTitledBorder(empty," Frame ");
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.setBorder(raisedbevel);
		
		
		
		/*
		 *  Elements
		 */
		

		// Labels
		this.gbc.gridx = 1;
		JLabel frameLabel = new JLabel("Frame");
		frameLabel.setFont(this.parent.getFONT());
		this.termRowsPanel.add(frameLabel ,this.gbc);
		this.gbc.gridx = 3;
		JPanel displayPanel = new JPanel();
		displayPanel.setForeground(Color.WHITE);
		display = new JLabel(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");
		display.setFont(parent.getFONT());
		displayPanel.add(display);
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		displayPanel.setBorder( loweredbevel);
		this.termRowsPanel.add(displayPanel ,this.gbc);
//		this.createNextTermCombo();


		
		
		/*
		 * calibration
		 */

		this.gbc.gridy++;

		this.gbc.gridx = 0;
		
		

		
		if ( this.frameOperator.hasElements() ) {
			

			
			for (int i = 0; i < this.frameOperator.getTerms().size(); i++) {
				
				// Constant
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isConstant() ) this.createConstantTerm(this.frameOperator.getTerms().get(i),i);
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isConstant() ) this.recreateConstantRow(this.frameOperator.getTerms().get(i),i );
				
				// Variable
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isVariable() ) this.createVariableTerm(this.frameOperator.getTerms().get(i),i);
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isVariable() ) this.recreateVariableRow(this.frameOperator.getTerms().get(i),i );
				
				// Uniterm
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isUniterm() ) this.createUnitermTerm(this.frameOperator.getTerms().get(i),i);
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isUniterm() ) this.recreateUnitermRow(this.frameOperator.getTerms().get(i),i );
				
				// List
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isList() ) this.createListTerm(this.frameOperator.getTerms().get(i),i);
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isList() ) this.recreateListRow(this.frameOperator.getTerms().get(i),i );
			}
		}else{
        	createVariableRow(termCounter); termCounter++;
        	createVariableRow(termCounter); termCounter++;
        	createVariableRow(termCounter); 
		}
		
		
		

		this.add(this.termRowsPanel);


		// FocusListener
		FocusListener FL = new FocusListener() {

			public void focusGained(FocusEvent arg0) {


				
				
				if (visualRifEditor.getDocumentContainer().getActiveDocument()
						.getDocumentEditorPane().getPrefixList().length > 0) {

					frameOperator.savePrefixes();
					frameOperator.saveUnitermPrefix();
					frameOperator.saveNamePrefixes();

					frameOperator.setConstantComboBoxEntries(visualRifEditor
							.getDocumentContainer().getActiveDocument()
							.getDocumentEditorPane().getPrefixList());

				}
			}

			public void focusLost(FocusEvent arg0) {
			}
		};
	
		this.setFocusListener(FL);
		
		this.addComponentListener(new ComponentAdapter() {
			

			public void componentResized(ComponentEvent e) {
				
				updateSize();
			}



		});


		
		if(frameOperator.getConstantComboBox().getFocusListeners().length <= 1)
			frameOperator.getConstantComboBox().addFocusListener(FL);
		
		if (frameOperator.getUniTermComboBox().getFocusListeners().length <= 1) 
			frameOperator.getUniTermComboBox().addFocusListener(FL);
		
		
		this.updateSize();
		
		
	}

	/* ******** **
	 * Constant **
	 * ******** */
	



	private void createConstantRow(int termID) {
		
		final Term term = new Term();
		term.setConstant( true );
		term.setTermFrameID(termID);
		
		/*
		 *  Elements
		 */
		
		// PrefixCombo
		JComboBox constCombo  = new JComboBox();
		constCombo.addFocusListener(comboBoxFocusListener);
		constCombo.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {

				   if (evt.getStateChange() == ItemEvent.SELECTED) {
					   JComboBox selectedChoice = (JComboBox)evt.getSource();
				
				        term.setSelectedPrefix(selectedChoice.getSelectedItem().toString());
				        term.setPrefix(true);
					   
				   }
				   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
				}
		    } );
		term.setConstantCombo(constCombo);
		

		// typCombo

		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						        
						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createVariableRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createConstantRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createUnitermRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createListRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
//		term.setNameComboBox(namedConstCombo);
		
		
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value",
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
				updateSize();
				}});

		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;
		

		
		this.termRowsPanel.add(constCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue, this.gbc);
		
		term.setTermLabel(termLabel);
		term.setTextFieldResizing(tfValue);
		term.setTypCombo(typCombo);
		term.setInit(true);
		
		frameOperator.getTerms().add( term );
		this.updateSize();
		
		
	}
	
	
	private void createConstantTerm(final Term term, int termID) {
		
		term.setTermFrameID(termID);
		frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
    	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
		
		String selectedPref = term.getSelectedPrefix();
		/*
		 *  Elements
		 */
		
		// typCombo
		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.addItemListener( new ItemListener() {
							public void itemStateChanged(ItemEvent evt) {

								   if (evt.getStateChange() == ItemEvent.SELECTED) {
									   JComboBox selectedChoice = (JComboBox)evt.getSource();
								        
								        if (selectedChoice.getSelectedItem().toString().equals("Var")){
								        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
								        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
								        	createVariableRow(term.getTermFrameID());
								        	removeRow(term);
								        	frameOperator.getTerms().remove(term);
								        	frameOperator.sortTermsByTermFrameID();
								        	repaintAllTerms();
								        	}
								        if (selectedChoice.getSelectedItem().toString().equals("Const")){
								        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
								        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
								        	createConstantRow(term.getTermFrameID());
								        	removeRow(term);
								        	frameOperator.getTerms().remove(term);
								        	frameOperator.sortTermsByTermFrameID();
								        	repaintAllTerms();
								        	}
								        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
								        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
								        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
								        	createUnitermRow(term.getTermFrameID());
								        	removeRow(term);
								        	frameOperator.getTerms().remove(term);
								        	frameOperator.sortTermsByTermFrameID();
								        	repaintAllTerms();
								        	}
								        if (selectedChoice.getSelectedItem().toString().equals("List")){
								        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
								        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
								        	createListRow(term.getTermFrameID());
								        	removeRow(term);
								        	frameOperator.getTerms().remove(term);
								        	frameOperator.sortTermsByTermFrameID();
								        	repaintAllTerms();
								        	}
								        
									   
								   }
								   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
								}
						    } );

		
		// PrefixCombo
		JComboBox constCombo  = new JComboBox();
		constCombo.addFocusListener(comboBoxFocusListener);
		constCombo.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {

				   if (evt.getStateChange() == ItemEvent.SELECTED) {
					   JComboBox selectedChoice = (JComboBox)evt.getSource();
				
				        term.setSelectedPrefix(selectedChoice.getSelectedItem().toString());
				        term.setPrefix(true);
					   
				   }
				   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
				}
		    } );
		term.setConstantCombo(constCombo);
		
		for (String s : term.getComboEntries()){
			term.getConstantCombo().addItem(s);
		}

		term.getConstantCombo().setSelectedItem(selectedPref);
		

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(),
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
			
				}});


		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(constCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue, this.gbc);
		

		
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.termRowsPanel.setBorder(raisedbevel);
		
		term.setTermLabel(termLabel);

		term.setTypCombo(typCombo);

		term.setTextFieldResizing(tfValue);

		term.setInit(true);
		
	

		this.updateSize();
		
	}
	

	private void recreateConstantRow(final Term term, int termID){
		
		term.setTermFrameID(termID);
		frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
    	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getTermLabel(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTypCombo(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getConstantCombo(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTextFieldResizing(), this.gbc);
		

		
	
	}


	/* ******** **
	 * Variable **
	 * ******** */
	
	
	private void createVariableRow(int termID) {
		
		final Term term = new Term();
		term.setVariable( true );
		term.setTermFrameID(termID);
		
		/*
		 *  Elements
		 */
		
	
		
		// typCombo
		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.setSelectedItem("Var");
		typCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
							  
							   if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createVariableRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						      
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createConstantRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createUnitermRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createListRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
//		term.setNameComboBox(namedConstCombo);
		
		
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value",
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
				updateSize();
				}});
		
		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;

		
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;

		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue, this.gbc);
		
		term.setTermLabel(termLabel);
		term.setTextFieldResizing(tfValue);
		term.setTypCombo(typCombo);
		term.setInit(true);
		
		frameOperator.getTerms().add( term );
		this.updateSize();
		
		
	}
	
	
	private void createVariableTerm(final Term term, int termID){
		
		term.setTermFrameID(termID);
		frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
    	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
		/*
		 *  Elements
		 */
		
		
		// typCombo
		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.setSelectedItem("Var");
		typCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
							  
							   if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createVariableRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						      
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createConstantRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createUnitermRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createListRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		
		
		
		
		
		
		
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(),
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
			
				}});

	
	
		
		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;
		
	
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;

		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue,this.gbc);
		

		term.setTermLabel(termLabel);
		term.setTypCombo(typCombo);
		term.setTextFieldResizing(tfValue);
		term.setInit(true);
		

		
		this.updateSize();

		

		
	}
	
	
	private void recreateVariableRow(final Term term, int termID){
		
		term.setTermFrameID(termID);
		frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
    	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getTermLabel(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTypCombo(),this.gbc);
		
		this.gbc.gridx++;
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTextFieldResizing(), this.gbc);

		
	
	}
	
	
	/* ******** **
	 * UniTerm  **
	 * ******** */
	

	private void createUnitermRow(int termID) {
		
		final Term term = new Term();
		term.setUniterm(true);
		term.setTermFrameID(termID);
		
		/*
		 *  Elements
		 */
		
		// typCombo

		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.setSelectedItem("Expr");
		typCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						        
						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createVariableRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createConstantRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createUnitermRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createListRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		
		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,frameOperator,term);
					
					connectionButton.setEnabled(false);  
				
					 }});
		
		
		
		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;
		

		
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton, this.gbc);
		

//		term.setAbstractTermOperator(abstractTermOperator)
		term.setTermLabel(termLabel);
		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		
		frameOperator.getTerms().add( term );
		this.updateSize();
		
		
	}
	
	private void createUnitermTerm(final Term term, int termID) {
		


		term.setTermFrameID(termID);
		
		/*
		 *  Elements
		 */
		
		// typCombo

		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.setSelectedItem("Expr");
		typCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						        
						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createVariableRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createConstantRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createUnitermRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createListRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		
		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,frameOperator,term);
					
					connectionButton.setEnabled(false);  
				
					 }});
		
		
		
		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;
		

		
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton, this.gbc);
		

//		term.setAbstractTermOperator(abstractTermOperator)
		term.setTermLabel(termLabel);
		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		
		frameOperator.getTerms().add( term );
		this.updateSize();
		

		
		
	}
	
	private void recreateUnitermRow(Term term, int termID) {
		
		term.setTermFrameID(termID);
		frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
    	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getTermLabel(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTypCombo(),this.gbc);
		
		this.gbc.gridx++;
		this.gbc.gridx++;
	
		
		this.termRowsPanel.add(term.getConnectionButton() ,this.gbc);
		


		
	}
	
	
	/* **** **
	 * List **
	 * **** */
	
	
	private void createListRow(int termID) {
		
		final Term term = new Term();
		term.setList(true);
		term.setTermFrameID(termID);
		
		/*
		 *  Elements
		 */
		
		
		// typCombo

		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.setSelectedItem("List");
		typCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						        
						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createVariableRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createConstantRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createUnitermRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createListRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		
		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
								RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,frameOperator,term);
					
					connectionButton.setEnabled(false);  
					  
					 }});
		
		
		
		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;
		

		
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton, this.gbc);
		

//		term.setAbstractTermOperator(abstractTermOperator)
		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		term.setTermLabel(termLabel);
		
		frameOperator.getTerms().add( term );
		this.updateSize();
		
		
	}
	
	
	private void recreateListRow(Term term, int termID) {
		
		term.setTermFrameID(termID);
		frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
    	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getTermLabel(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTypCombo(),this.gbc);
		
		this.gbc.gridx++;
		this.gbc.gridx++;
	
		
		this.termRowsPanel.add(term.getConnectionButton() ,this.gbc);
		


		
	}
	
	private void createListTerm(final Term term, int termID) {

		term.setTermFrameID(termID);
		
		/*
		 *  Elements
		 */
		
		
		// typCombo

		JComboBox typCombo = new JComboBox(typComboEntries);
		typCombo.setSelectedItem("List");
		typCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						        
						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createVariableRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createConstantRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createUnitermRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	display.setText(frameOperator.getTermTypArray()[0]+"["+frameOperator.getTermTypArray()[1]+"->"+frameOperator.getTermTypArray()[2]+"]");
						        	createListRow(term.getTermFrameID());
						        	removeRow(term);
						        	frameOperator.getTerms().remove(term);
						        	frameOperator.sortTermsByTermFrameID();
						        	repaintAllTerms();
						        	}
						        
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		
		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
								RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,frameOperator,term);
					
					connectionButton.setEnabled(false);  
					  
					 }});
		
		
		
		JLabel termLabel = new JLabel();
		termLabel.setFont(parent.getFONT());
		String labelString = "";
		if(termID == 0) labelString = "Subject:";
		if(termID == 1) labelString = "Predicate:";
		if(termID == 2) labelString = "Object:";
		termLabel.setText(labelString);
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(termLabel,this.gbc);
		
		this.gbc.gridx++;
		

		
		this.termRowsPanel.add(typCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton, this.gbc);
		

//		term.setAbstractTermOperator(abstractTermOperator)
		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		term.setTermLabel(termLabel);
		
		frameOperator.getTerms().add( term );
		this.updateSize();
		
	}
	
	
	
	/* **** **
	 * Util **
	 * **** */
	
	/**
	 * Sets the ComboBox entries for UniTermComboBox and the 
	 * ConstantComboBox
	 * @param comboBoxEntries
	 */
	public void setConstantComboBoxEntries(String[] comboBoxEntries){

		this.comboBoxEntries = comboBoxEntries;

		this.frameOperator.getConstantComboBox().removeAllItems();


		

		
		
		int constantCnt = 0;
		int termCnt = 0;
		
		for (int i = 0 ; i < this.frameOperator.getTerms().size() ; i++){
			
			
			if( this.frameOperator.getTerms().get(i).isConstant() ){
				
				this.frameOperator.getTerms().get(i).getConstantCombo().removeAllItems();
				
				for (String s : comboBoxEntries){
					this.frameOperator.getTerms().get(i).getConstantCombo().addItem(s);
				}
				JComboBox tmp = this.frameOperator.getTerms().get(i).getConstantCombo();
				tmp.setSelectedItem(frameOperator.getSavedPrefixes().get(constantCnt));
				constantCnt++;
			}
			
			if( this.frameOperator.isNamed() ) {
				
				this.frameOperator.getTerms().get(i).getNameComboBox().removeAllItems();
				
				for (String s : comboBoxEntries){
					this.frameOperator.getTerms().get(i).getNameComboBox().addItem(s);
				}
				
					JComboBox tmp = this.frameOperator.getTerms().get(i).getNameComboBox();
					LinkedList<String> l = frameOperator.getSavedNamePrefixes();
					tmp.setSelectedItem(l.get(termCnt));
					termCnt++;
				
				
			}
			
			
		}

	}
	
	
	private void removeRow(Term term) {
		
		// Constant
		if ( term.isConstant() ) {

			this.termRowsPanel.remove( term.getConstantCombo() );
			this.termRowsPanel.remove( term.getTextFieldResizing() );
			


		}
		
		// Variable
		
		if ( term.isVariable() ){
			
			this.termRowsPanel.remove( term.getTextFieldResizing() );

			
		}
		
		// Uniterm || List
		if ( term.isUniterm() || term.isList() ){
			
			this.termRowsPanel.remove( term.getConnectionButton() );
			
		}
		
		this.termRowsPanel.remove( term.getTermLabel() );
		this.termRowsPanel.remove(term.getTypCombo());
	
		

		this.updateSize();
	}
	
	
	public void updateSize() {
		this.setMinimumSize(this.termRowsPanel.getSize());

		// --- update width of the JTextFieldResizing to the max size per
		// column
		// - begin ---
		if (this.termRowsPanel.getComponentCount() >= 3) {
			// -- get max width for each column - begin --
			int maxWidthLeftColumn = 10;
			Container textField = null;
			Dimension d = null;

			for (int i = 0; i < this.termRowsPanel.getComponentCount(); i ++) {
				

				if(this.termRowsPanel.getComponent(i) instanceof JTextFieldResizing){

				textField = (Container) this.termRowsPanel.getComponent(i);

				final Dimension leftSize = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
						.calculateSize() : textField.getPreferredSize();

				maxWidthLeftColumn = Math.max(maxWidthLeftColumn,
						leftSize.width);


				}
			}
			// -- get max width for each column - end --

			// -- update elements of each column - begin --
			// walk through rows...
			for (int i = 0; i < this.termRowsPanel.getComponentCount(); i ++) {
			
				if(this.termRowsPanel.getComponent(i) instanceof JTextFieldResizing){

				textField = (Container) this.termRowsPanel.getComponent(i);
				d = new Dimension(maxWidthLeftColumn,
						textField.getPreferredSize().height);
				textField.setPreferredSize(d);
				textField.setSize(d);
				textField.setMaximumSize(d);
				textField.setMinimumSize(d);
				textField.repaint();

				}
			}
			// -- update elements of each column - end --
		}
		// --- update width of the JTextFieldResizing to the max size per
		// column
		// - begin ---

		// update height of the GraphBox...
		if (this.getBox() != null) {
			this.getBox().height = this.getPreferredSize().height;
		}

		this.setSize(this.getPreferredSize());
		this.revalidate(); // re-validate the PrefixPanel
	}

	
	@Override
	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		// TODO Auto-generated method stub
		return false;
	}


	private void repaintAllTerms(){
		for (int i = 0; i < this.frameOperator.getTerms().size(); i++) {
			this.removeRow(this.frameOperator.getTerms().get(i));
		}
		
		if ( this.frameOperator.hasElements() ) {
			
			for (int i = 0; i < this.frameOperator.getTerms().size(); i++) {
				
	
				
				if (  this.frameOperator.getTerms().get(i).isConstant() ){

					this.recreateConstantRow(this.frameOperator.getTerms().get(i),i );
				}
				
				if (  this.frameOperator.getTerms().get(i).isVariable() ) {
				
					this.recreateVariableRow(this.frameOperator.getTerms().get(i),i );
				}
				
				if (  this.frameOperator.getTerms().get(i).isUniterm() ) {
					
					this.recreateUnitermRow(this.frameOperator.getTerms().get(i),i );
				}
				
				if (  this.frameOperator.getTerms().get(i).isList() ) {
					
					this.recreateListRow(this.frameOperator.getTerms().get(i),i );
				}
				
			}
		}
		
		

		this.updateSize();
	}
	
	



	/* ***************** **
	 * Getter and Setter **
	 * ***************** */
	

	public FrameOperator getFrameOperator() {
		return frameOperator;
	}

	public void setFactOperator(FrameOperator frameOperator) {
		this.frameOperator = frameOperator;
	}

	public String getSelectedChoiceString() {
		return selectedChoiceString;
	}

	public void setSelectedChoiceString(String selectedChoiceString) {
		this.selectedChoiceString = selectedChoiceString;
	}

	public String[] getComboBoxEntries() {
		return comboBoxEntries;
	}

	public void setFocusListener(FocusListener fL) {
		this.comboBoxFocusListener = fL;
		
	}

	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}

	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}


	




	
	
	
}
