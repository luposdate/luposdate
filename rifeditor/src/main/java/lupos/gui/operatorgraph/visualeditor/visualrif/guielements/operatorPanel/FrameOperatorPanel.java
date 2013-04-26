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
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.FrameOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.HintTextFieldResizing;
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
			final GraphWrapper gw, final FrameOperator frameOperator,
			final boolean startNode, final boolean alsoSubClasses, final VisualRifEditor visualRifEditor) {
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

		this.gbc.insets = new Insets((int) this.parent.PADDING,
				(int) this.parent.PADDING, (int) this.parent.PADDING,
				(int) this.parent.PADDING);
		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;

		final Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.setBorder(raisedbevel);

		/*
		 *  Elements
		 */
		// Labels
		this.gbc.gridx = 1;
		final JLabel frameLabel = new JLabel("Frame");
		frameLabel.setFont(this.parent.getFONT());
		this.termRowsPanel.add(frameLabel ,this.gbc);
		this.gbc.gridx = 3;
		final JPanel displayPanel = new JPanel();
		displayPanel.setForeground(Color.WHITE);
		this.display = new JLabel(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");
		this.display.setFont(this.parent.getFONT());
		displayPanel.add(this.display);
		final Border loweredbevel = BorderFactory.createLoweredBevelBorder();
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
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isConstant() ) {
					this.createConstantTerm(this.frameOperator.getTerms().get(i),i);
				}
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isConstant() ) {
					this.recreateConstantRow(this.frameOperator.getTerms().get(i),i );
				}

				// Variable
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isVariable() ) {
					this.createVariableTerm(this.frameOperator.getTerms().get(i),i);
				}
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isVariable() ) {
					this.recreateVariableRow(this.frameOperator.getTerms().get(i),i );
				}

				// Uniterm
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isUniterm() ) {
					this.createUnitermTerm(this.frameOperator.getTerms().get(i),i);
				}
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isUniterm() ) {
					this.recreateUnitermRow(this.frameOperator.getTerms().get(i),i );
				}

				// List
				if ( !this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isList() ) {
					this.createListTerm(this.frameOperator.getTerms().get(i),i);
				}
				if (  this.frameOperator.getTerms().get(i).isInit() && this.frameOperator.getTerms().get(i).isList() ) {
					this.recreateListRow(this.frameOperator.getTerms().get(i),i );
				}
			}
		}else{
        	this.createVariableRow(this.termCounter); this.termCounter++;
        	this.createVariableRow(this.termCounter); this.termCounter++;
        	this.createVariableRow(this.termCounter);
		}

		this.add(this.termRowsPanel);

		// FocusListener
		final FocusListener FL = new FocusListener() {
			@Override
			public void focusGained(final FocusEvent arg0) {
				if (FrameOperatorPanel.this.visualRifEditor.getDocumentContainer().getActiveDocument()
						.getDocumentEditorPane().getPrefixList().length > 0) {
					FrameOperatorPanel.this.frameOperator.savePrefixes();
					FrameOperatorPanel.this.frameOperator.saveUnitermPrefix();
					FrameOperatorPanel.this.frameOperator.saveNamePrefixes();

					FrameOperatorPanel.this.frameOperator.setConstantComboBoxEntries(FrameOperatorPanel.this.visualRifEditor
							.getDocumentContainer().getActiveDocument()
							.getDocumentEditorPane().getPrefixList());
				}
			}
			@Override
			public void focusLost(final FocusEvent arg0) {
			}
		};

		this.setFocusListener(FL);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				FrameOperatorPanel.this.updateSize();
			}
		});

		if(this.frameOperator.getConstantComboBox().getFocusListeners().length <= 1) {
			this.frameOperator.getConstantComboBox().addFocusListener(FL);
		}

		if (this.frameOperator.getUniTermComboBox().getFocusListeners().length <= 1) {
			this.frameOperator.getUniTermComboBox().addFocusListener(FL);
		}

		this.updateSize();
	}

	/* ******** **
	 * Constant **
	 * ******** */
	private void createConstantRow(final int termID) {

		final Term term = new Term();
		term.setConstant( true );
		term.setTermFrameID(termID);

		/*
		 *  Elements
		 */
		// PrefixCombo
		final JComboBox constCombo  = new JComboBox();
		constCombo.addFocusListener(this.comboBoxFocusListener);
		constCombo.addItemListener( new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent evt) {
				   if (evt.getStateChange() == ItemEvent.SELECTED) {
					   final JComboBox selectedChoice = (JComboBox)evt.getSource();
				        term.setSelectedPrefix(selectedChoice.getSelectedItem().toString());
				        term.setPrefix(true);
				   }
				   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
				}
		    } );
		term.setConstantCombo(constCombo);

		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();

						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value",
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				FrameOperatorPanel.this.updateSize();
				}});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

		this.frameOperator.getTerms().add( term );
		this.updateSize();
	}

	private void createConstantTerm(final Term term, final int termID) {
		term.setTermFrameID(termID);
		this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
    	this.display.setText(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");

		final String selectedPref = term.getSelectedPrefix();
		/*
		 *  Elements
		 */
		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.addItemListener( new ItemListener() {
							@Override
							public void itemStateChanged(final ItemEvent evt) {

								   if (evt.getStateChange() == ItemEvent.SELECTED) {
									   final JComboBox selectedChoice = (JComboBox)evt.getSource();

								        if (selectedChoice.getSelectedItem().toString().equals("Var")){
								        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
								        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
								        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
								        	FrameOperatorPanel.this.removeRow(term);
								        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
								        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
								        	FrameOperatorPanel.this.repaintAllTerms();
								        	}
								        if (selectedChoice.getSelectedItem().toString().equals("Const")){
								        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
								        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
								        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
								        	FrameOperatorPanel.this.removeRow(term);
								        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
								        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
								        	FrameOperatorPanel.this.repaintAllTerms();
								        	}
								        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
								        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
								        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
								        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
								        	FrameOperatorPanel.this.removeRow(term);
								        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
								        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
								        	FrameOperatorPanel.this.repaintAllTerms();
								        	}
								        if (selectedChoice.getSelectedItem().toString().equals("List")){
								        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
								        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
								        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
								        	FrameOperatorPanel.this.removeRow(term);
								        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
								        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
								        	FrameOperatorPanel.this.repaintAllTerms();
								        	}
								   }
								   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
								}
						    } );
		// PrefixCombo
		final JComboBox constCombo  = new JComboBox();
		constCombo.addFocusListener(this.comboBoxFocusListener);
		constCombo.addItemListener( new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent evt) {
				   if (evt.getStateChange() == ItemEvent.SELECTED) {
					   final JComboBox selectedChoice = (JComboBox)evt.getSource();
				        term.setSelectedPrefix(selectedChoice.getSelectedItem().toString());
				        term.setPrefix(true);
				   }
				   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
				}
		    } );
		term.setConstantCombo(constCombo);

		for (final String s : term.getComboEntries()){
			term.getConstantCombo().addItem(s);
		}

		term.getConstantCombo().setSelectedItem(selectedPref);

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(), "Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				}});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

		final Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.termRowsPanel.setBorder(raisedbevel);

		term.setTermLabel(termLabel);

		term.setTypCombo(typCombo);

		term.setTextFieldResizing(tfValue);

		term.setInit(true);

		this.updateSize();
	}

	private void recreateConstantRow(final Term term, final int termID){
		term.setTermFrameID(termID);
		this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
    	this.display.setText(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");
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
	private void createVariableRow(final int termID) {

		final Term term = new Term();
		term.setVariable( true );
		term.setTermFrameID(termID);

		/*
		 *  Elements
		 */
		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.setSelectedItem("Var");
		typCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
							   if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}

						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}


						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value", "Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				FrameOperatorPanel.this.updateSize();
				}});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

		this.frameOperator.getTerms().add( term );
		this.updateSize();
	}


	private void createVariableTerm(final Term term, final int termID){
		term.setTermFrameID(termID);
		this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
    	this.display.setText(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");
		/*
		 *  Elements
		 */
		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.setSelectedItem("Var");
		typCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();

							   if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}

						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(), "Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				}});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

	private void recreateVariableRow(final Term term, final int termID){
		term.setTermFrameID(termID);
		this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
    	this.display.setText(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");
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
	private void createUnitermRow(final int termID) {
		final Term term = new Term();
		term.setUniterm(true);
		term.setTermFrameID(termID);

		/*
		 *  Elements
		 */
		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.setSelectedItem("Expr");
		typCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();

						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					final RuleGraph ruleGraph = (RuleGraph) FrameOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,FrameOperatorPanel.this.frameOperator,term);
					connectionButton.setEnabled(false);
					 }});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

		term.setTermLabel(termLabel);
		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);

		this.frameOperator.getTerms().add( term );
		this.updateSize();
	}

	private void createUnitermTerm(final Term term, final int termID) {

		term.setTermFrameID(termID);

		/*
		 *  Elements
		 */
		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.setSelectedItem("Expr");
		typCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					final RuleGraph ruleGraph = (RuleGraph) FrameOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,FrameOperatorPanel.this.frameOperator,term);
					connectionButton.setEnabled(false);
					 }});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

		term.setTermLabel(termLabel);
		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);

		this.frameOperator.getTerms().add( term );
		this.updateSize();
	}

	private void recreateUnitermRow(final Term term, final int termID) {
		term.setTermFrameID(termID);
		this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
    	this.display.setText(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");
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
	private void createListRow(final int termID) {

		final Term term = new Term();
		term.setList(true);
		term.setTermFrameID(termID);

		/*
		 *  Elements
		 */
		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.setSelectedItem("List");
		typCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();

						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
								final RuleGraph ruleGraph = (RuleGraph) FrameOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,FrameOperatorPanel.this.frameOperator,term);
					connectionButton.setEnabled(false);
					 }});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		term.setTermLabel(termLabel);

		this.frameOperator.getTerms().add( term );
		this.updateSize();
	}

	private void recreateListRow(final Term term, final int termID) {
		term.setTermFrameID(termID);
		this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
    	this.display.setText(this.frameOperator.getTermTypArray()[0]+"["+this.frameOperator.getTermTypArray()[1]+"->"+this.frameOperator.getTermTypArray()[2]+"]");
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

	private void createListTerm(final Term term, final int termID) {
		term.setTermFrameID(termID);
		/*
		 *  Elements
		 */
		// typCombo
		final JComboBox typCombo = new JComboBox(this.typComboEntries);
		typCombo.setSelectedItem("List");
		typCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        if (selectedChoice.getSelectedItem().toString().equals("Var")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Var";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createVariableRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Const")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Const";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createConstantRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("Expr")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "Expr";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createUnitermRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						        if (selectedChoice.getSelectedItem().toString().equals("List")){
						        	FrameOperatorPanel.this.frameOperator.getTermTypArray()[term.getTermFrameID()] = "List";
						        	FrameOperatorPanel.this.display.setText(FrameOperatorPanel.this.frameOperator.getTermTypArray()[0]+"["+FrameOperatorPanel.this.frameOperator.getTermTypArray()[1]+"->"+FrameOperatorPanel.this.frameOperator.getTermTypArray()[2]+"]");
						        	FrameOperatorPanel.this.createListRow(term.getTermFrameID());
						        	FrameOperatorPanel.this.removeRow(term);
						        	FrameOperatorPanel.this.frameOperator.getTerms().remove(term);
						        	FrameOperatorPanel.this.frameOperator.sortTermsByTermFrameID();
						        	FrameOperatorPanel.this.repaintAllTerms();
						        	}
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		final JButton connectionButton = new JButton("Connection");
		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
								final RuleGraph ruleGraph = (RuleGraph) FrameOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,FrameOperatorPanel.this.frameOperator,term);
					connectionButton.setEnabled(false);
					 }});

		final JLabel termLabel = new JLabel();
		termLabel.setFont(this.parent.getFONT());
		String labelString = "";
		if(termID == 0) {
			labelString = "Subject:";
		}
		if(termID == 1) {
			labelString = "Predicate:";
		}
		if(termID == 2) {
			labelString = "Object:";
		}
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

		term.setTypCombo(typCombo);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		term.setTermLabel(termLabel);

		this.frameOperator.getTerms().add( term );
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
	public void setConstantComboBoxEntries(final String[] comboBoxEntries){
		this.comboBoxEntries = comboBoxEntries;
		this.frameOperator.getConstantComboBox().removeAllItems();
		int constantCnt = 0;
		int termCnt = 0;
		for (int i = 0 ; i < this.frameOperator.getTerms().size() ; i++){
			if( this.frameOperator.getTerms().get(i).isConstant() ){
				this.frameOperator.getTerms().get(i).getConstantCombo().removeAllItems();
				for (final String s : comboBoxEntries){
					this.frameOperator.getTerms().get(i).getConstantCombo().addItem(s);
				}
				final JComboBox tmp = this.frameOperator.getTerms().get(i).getConstantCombo();
				tmp.setSelectedItem(this.frameOperator.getSavedPrefixes().get(constantCnt));
				constantCnt++;
			}

			if( this.frameOperator.isNamed() ) {
				this.frameOperator.getTerms().get(i).getNameComboBox().removeAllItems();
				for (final String s : comboBoxEntries){
					this.frameOperator.getTerms().get(i).getNameComboBox().addItem(s);
				}
				final JComboBox tmp = this.frameOperator.getTerms().get(i).getNameComboBox();
				final LinkedList<String> l = this.frameOperator.getSavedNamePrefixes();
				tmp.setSelectedItem(l.get(termCnt));
				termCnt++;
			}
		}
	}

	private void removeRow(final Term term) {
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

	@Override
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

				maxWidthLeftColumn = Math.max(maxWidthLeftColumn, leftSize.width);
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
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
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
		return this.frameOperator;
	}

	public void setFactOperator(final FrameOperator frameOperator) {
		this.frameOperator = frameOperator;
	}

	public String getSelectedChoiceString() {
		return this.selectedChoiceString;
	}

	public void setSelectedChoiceString(final String selectedChoiceString) {
		this.selectedChoiceString = selectedChoiceString;
	}

	public String[] getComboBoxEntries() {
		return this.comboBoxEntries;
	}

	public void setFocusListener(final FocusListener fL) {
		this.comboBoxFocusListener = fL;
	}

	public VisualRifEditor getVisualRifEditor() {
		return this.visualRifEditor;
	}

	public void setVisualRifEditor(final VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}
}
