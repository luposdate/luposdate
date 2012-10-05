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
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel;

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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.HintTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.JIconButton;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.TermConnection;



public class ListOperatorPanel extends AbstractGuiComponent<Operator> {



	private static final long serialVersionUID = 8238554719560169292L;
	
	private VisualRifEditor visualRifEditor;
	protected GridBagConstraints gbc = null;
	private JPanel termRowsPanel;
	protected ListOperator listOperator;
	private  String selectedChoiceString = "";
	private String[] comboBoxEntries;


	private FocusListener comboBoxFocusListener;




	// Constructor
	public ListOperatorPanel(final VisualGraph<Operator> parent,
			GraphWrapper gw, final ListOperator listOperator,
			boolean startNode, boolean alsoSubClasses, VisualRifEditor visualRifEditor) {

		super(parent, gw, listOperator, true);

	
		this.setVisualRifEditor(visualRifEditor);
	
		this.listOperator = listOperator;		
		

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
		
		/*
		 *  Elements
		 */
		
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.setBorder(raisedbevel);

		// External 
		JCheckBox cbOpenList = new JCheckBox( "Open list", listOperator.isOpen());
		cbOpenList.setFont(this.parent.getFONT());
		ItemListener ilExternal = new ItemListener() {
			  @Override public void itemStateChanged( ItemEvent e ) {
				  listOperator.setOpen(!listOperator.isOpen());
		
			  }
			};
			
		cbOpenList.addItemListener(ilExternal);
		
		
		// Labels
		JLabel listLabel = new JLabel("List");
		listLabel.setFont(this.parent.getFONT());

	
		
		/*
		 * calibration
		 */
		
			
		// first row

			this.gbc.gridx = 3;
		
		

		this.termRowsPanel.add(listLabel ,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(cbOpenList,this.gbc);
		
		this.gbc.gridy++;
		
		

		if ( this.listOperator.hasElements() ) {
			
			for (int i = 0; i < this.listOperator.getTerms().size(); i++) {
				
				// Constant
				if ( !this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isConstant() ) this.createConstantTerm(this.listOperator.getTerms().get(i) );
				if (  this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isConstant() ) this.recreateConstantRow(this.listOperator.getTerms().get(i) );
				
				// Variable
				if ( !this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isVariable() ) this.createVariableTerm(this.listOperator.getTerms().get(i) );			
				if (  this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isVariable() ) this.recreateVariableRow(this.listOperator.getTerms().get(i) );
				
				// Uniterm
				if ( !this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isUniterm() ) this.createUnitermTerm(this.listOperator.getTerms().get(i) );			
				if (  this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isUniterm() ) this.recreateUnitermRow(this.listOperator.getTerms().get(i) );
				
				// List
				if ( !this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isList() ) this.createListTerm(this.listOperator.getTerms().get(i) );			
				if (  this.listOperator.getTerms().get(i).isInit() && this.listOperator.getTerms().get(i).isList() ) this.recreateListRow(this.listOperator.getTerms().get(i) );
			}
		}
		
		
		this.createNextTermCombo();

		this.add(this.termRowsPanel);

		// FocusListener
		FocusListener FL = new FocusListener() {

			public void focusGained(FocusEvent arg0) {


				
				
				if (visualRifEditor.getDocumentContainer().getActiveDocument()
						.getDocumentEditorPane().getPrefixList().length > 0) {

					listOperator.savePrefixes();
					listOperator.saveUnitermPrefix();
					listOperator.saveNamePrefixes();

					listOperator.setConstantComboBoxEntries(visualRifEditor
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


		
		if(listOperator.getConstantComboBox().getFocusListeners().length <= 1)
			listOperator.getConstantComboBox().addFocusListener(FL);
		
		if (listOperator.getUniTermComboBox().getFocusListeners().length <= 1) 
			listOperator.getUniTermComboBox().addFocusListener(FL);
		
		
		this.updateSize();
		
	}



	/* ******** **
	 * Constant **
	 * ******** */
	
	private void createConstantRow() {
		
		final Term term = new Term();
		term.setConstant( true );
		
		/*
		 *  Elements
		 */
		
		// PrefixCombo
		JComboBox constCombo  = new JComboBox();
		constCombo.setFont(this.parent.getFONT());
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
		
		
		// PrefixCombo
				JComboBox namedConstCombo  = new JComboBox();
				namedConstCombo.addFocusListener(comboBoxFocusListener);
				namedConstCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		term.setNameComboBox(namedConstCombo);
		
		
		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name",
				"Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {


			public void focusLost(final FocusEvent fe) {
				
				term.setTfName(tfName);
			
				}});


		
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value",
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
			
				}});

		
		// Label
		final JLabel label = new JLabel("Constant:");
		label.setFont(this.parent.getFONT());
		
		// Buttons
		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				 listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		  listOperator.getTerms().remove(term);
		  
		 }});
		
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		

		
		this.termRowsPanel.add(constCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue, this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);
		
		listOperator.getTerms().add( term );

		this.updateSize();
		
		
	}
	
	
	private void recreateConstantRow(final Term term){
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getUpButton(),this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDownButton(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getLabel(),this.gbc);
		
		this.gbc.gridx++;
		

		
		this.termRowsPanel.add(term.getConstantCombo(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTextFieldResizing(), this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
		
	
	}

	
	private void createConstantTerm(final Term term) {
		
		String selectedPref = term.getSelectedPrefix();
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
		
		for (String s : term.getComboEntries()){
			term.getConstantCombo().addItem(s);
		}

		term.getConstantCombo().setSelectedItem(selectedPref);
		
		// NameCombo
				JComboBox namedConstCombo  = new JComboBox();
				namedConstCombo.addFocusListener(comboBoxFocusListener);
				namedConstCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		term.setNameComboBox(namedConstCombo);
		
		
		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name",
				"Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {


			public void focusLost(final FocusEvent fe) {
				
				term.setTfName(tfName);
			
				}});


		
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(),
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
			
				}});

		
		// Label
		final JLabel label = new JLabel("Constant:");
		label.setFont(this.parent.getFONT());
		
		// Buttons
		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		 listOperator.getTerms().remove(term);
		  
		 }});
		
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		
		
		this.termRowsPanel.add(constCombo,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue, this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);
		
	

		this.updateSize();
		
		
		
	}
	
	
	/* ******** **
	 * Variable ** 
	 * ******** */
	
	private void createVariableRow() {
		
		final Term term = new Term();
		term.setVariable( true );
		
		/*
		 *  Elements
		 */
		
		
		// PrefixCombo
				JComboBox namedConstCombo  = new JComboBox();
				namedConstCombo.addFocusListener(comboBoxFocusListener);
				namedConstCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		term.setNameComboBox(namedConstCombo);
		
		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name",
				"Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {


			public void focusLost(final FocusEvent fe) {
				
				term.setTfName(tfName);
			
				}});
		
		
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value",
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
			
				}});

		
		// Label
		final JLabel label = new JLabel("Variable:");
		label.setFont(this.parent.getFONT());
		
		// Buttons

		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				 listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		  listOperator.getTerms().remove(term);
		  
		 }});
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);
		listOperator.getTerms().add( term );
		
		this.updateSize();

		
		
	}

	
	private void recreateVariableRow(final Term term){
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getUpButton(),this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDownButton(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getLabel(),this.gbc);
		
		this.gbc.gridx++;
		
			
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getTextFieldResizing(), this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
		
	
	}
	
	
	private void createVariableTerm(final Term term){
		

		/*
		 *  Elements
		 */
		
		
		// PrefixCombo
				JComboBox namedConstCombo  = new JComboBox();
				namedConstCombo.addFocusListener(comboBoxFocusListener);
				namedConstCombo.addItemListener( new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   JComboBox selectedChoice = (JComboBox)evt.getSource();
						
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
							   
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
				
		term.setNameComboBox(namedConstCombo);
		
		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing(
				"Name","Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {


			public void focusLost(final FocusEvent fe) {
				
				term.setTfName(tfName);
			
				}});
		
		
		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(),
				"Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {

			public void focusLost(final FocusEvent fe) {
				
				term.setValue(tfValue.getText());
			
				}});

	
		
		// Label
		final JLabel label = new JLabel("Variable:");
		label.setFont(this.parent.getFONT());
		
		// Buttons

		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				 listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		  listOperator.getTerms().remove(term);
		  
		 }});
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(tfValue,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);
		

		
		this.updateSize();

		
	}
	
	
	/* ******* **
	 * Uniterm ** 
	 * ******* */
	
	private void createUnitermRow() {
		
		final Term term = new Term();
		term.setUniterm( true );
		
		/*
		 *  Elements
		 */
		
		
		// Label
		final JLabel label = new JLabel("Uniterm:");
		label.setFont(this.parent.getFONT());
		
		// Buttons

		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				 listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		  listOperator.getTerms().remove(term);
		  
		 }});
		
		
		final JButton connectionButton = new JButton("Connection");
		
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,listOperator,term);
					
					connectionButton.setEnabled(false);  
				
					 }});
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		listOperator.getTerms().add( term );
		
		this.updateSize();

		
		
	}

	
	private void recreateUnitermRow(final Term term){
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getUpButton(),this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDownButton(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getLabel(),this.gbc);
		
		this.gbc.gridx++;
		
			
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getConnectionButton(), this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
		
	
	}
	
	
	private void createUnitermTerm(final Term term){
		

		/*
		 *  Elements
		 */

		
		// Label
		final JLabel label = new JLabel("Uniterm:");
		label.setFont(this.parent.getFONT());
		
		// Buttons
		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				 listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		  listOperator.getTerms().remove(term);
		  
		 }});
		
		
	final JButton connectionButton = new JButton("Connection");
		
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,listOperator,term);
					
					connectionButton.setEnabled(false);  
				
					 }});
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		

		
		this.updateSize();

		
	}
	
	
	
	/* **** **
	 * List ** 
	 * **** */
	
	private void createListRow() {
		
		final Term term = new Term();
		term.setList( true );
		
		/*
		 *  Elements
		 */
		
		
		// Label
		final JLabel label = new JLabel("List:");
		label.setFont(this.parent.getFONT());
		
		// Buttons

		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				 listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		  listOperator.getTerms().remove(term);
		  
		 }});
		
		
		final JButton connectionButton = new JButton("Connection");
		
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,listOperator,term);
					
					connectionButton.setEnabled(false);  
				
					 }});
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		listOperator.getTerms().add( term );
		
		this.updateSize();

		
		
	}

	
	private void recreateListRow(final Term term){
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(term.getUpButton(),this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDownButton(),this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getLabel(),this.gbc);
		
		this.gbc.gridx++;
		
			
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getConnectionButton(), this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
		
	
	}
	
	
	private void createListTerm(final Term term){
		

		/*
		 *  Elements
		 */

		
		// Label
		final JLabel label = new JLabel("List:");
		label.setFont(this.parent.getFONT());
		
		// Buttons
		Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);
		

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					listOperator.swapTerms(term,true);
					repaintAllTerms();
					  
					 }});
		
		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);
		
		downButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
				 listOperator.swapTerms(term,false);
				 repaintAllTerms();	  
					 }});
		
		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);
		
		
		deleteButton.addActionListener(new ActionListener(){
		 public void actionPerformed(ActionEvent e) {
		
		  removeRow(term);
		  listOperator.getTerms().remove(term);
		  
		 }});
		
		
	final JButton connectionButton = new JButton("Connection");
		
		connectionButton.addActionListener(new ActionListener(){
			 public void actionPerformed(ActionEvent e) {
					
					RuleGraph ruleGraph = (RuleGraph) parent;

					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,listOperator,term);
					
					connectionButton.setEnabled(false);  
				
					 }});
		
		
		/*
		 * Calibration
		 */
		
		this.gbc.gridy++;
		this.gbc.gridx = 0;
		
		this.termRowsPanel.add(upButton,this.gbc);

		this.gbc.gridx++;
		
		this.termRowsPanel.add(downButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(label,this.gbc);
		
		this.gbc.gridx++;
		
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(connectionButton,this.gbc);
		
		this.gbc.gridx++;
		
		this.termRowsPanel.add(deleteButton,this.gbc);
		
		this.termRowsPanel.remove(this.listOperator.getNextTermCombo());
		
		createNextTermCombo();
		
		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setInit(true);
		

		
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

		this.listOperator.getConstantComboBox().removeAllItems();


		
	
		
		
		int constantCnt = 0;

		
		for (int i = 0 ; i < this.listOperator.getTerms().size() ; i++){
			
			
			if( this.listOperator.getTerms().get(i).isConstant() ){
				
				this.listOperator.getTerms().get(i).getConstantCombo().removeAllItems();
				
				for (String s : comboBoxEntries){
					this.listOperator.getTerms().get(i).getConstantCombo().addItem(s);
				}
				JComboBox tmp = this.listOperator.getTerms().get(i).getConstantCombo();
				tmp.setSelectedItem(listOperator.getSavedPrefixes().get(constantCnt));
				constantCnt++;
			}
			

			
			
		}

	}
	
	
	private void createNextTermCombo() {
		
		this.listOperator.getNextTermCombo().addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {

				   if (evt.getStateChange() == ItemEvent.SELECTED) {
					   JComboBox selectedChoice = (JComboBox)evt.getSource();
					   
					   // Constant
				        if ( selectedChoice.getSelectedItem().equals("Const") ){
				        	// New Term
				        	createConstantRow();
				        	listOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				        
				 	   // Variable
				        if ( selectedChoice.getSelectedItem().equals("Var") ){
				        	createVariableRow();
				        	listOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				        
				 	   // Expression
				        if ( selectedChoice.getSelectedItem().equals("Expr") ){
				        	createUnitermRow();
				        	listOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				        
				 	   // List
				        if ( selectedChoice.getSelectedItem().equals("List") ){
				        	createListRow();
				        	listOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				        
				   }
				   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
				}
		    } );
		
		this.gbc.gridy++;
		this.gbc.gridx = 4;
		this.termRowsPanel.add(this.listOperator.getNextTermCombo(),this.gbc);
		
	}
	
	
	public void removeRow(Term term) {
		
		// Constant
		if ( term.isConstant() ) {

			this.termRowsPanel.remove( term.getConstantCombo() );
			this.termRowsPanel.remove( term.getTextFieldResizing() );
			


		}
		
		// Variable
		
		if ( term.isVariable() ){
			
			this.termRowsPanel.remove( term.getTextFieldResizing() );

			
		}
		
		
		if ( term.isUniterm() || term.isList() ){
			this.termRowsPanel.remove( term.getConnectionButton() );
		}
		
		
		this.termRowsPanel.remove(term.getUpButton());
		this.termRowsPanel.remove(term.getDownButton());
		this.termRowsPanel.remove(term.getDeleteButton());
		this.termRowsPanel.remove(term.getLabel());
		
	
		

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
		for (int i = 0; i < this.listOperator.getTerms().size(); i++) {
			this.removeRow(this.listOperator.getTerms().get(i));
		}
		
		if ( this.listOperator.hasElements() ) {
			
			for (int i = 0; i < this.listOperator.getTerms().size(); i++) {
				
				// Constant
				if (  this.listOperator.getTerms().get(i).isConstant() ){
		
					this.recreateConstantRow(this.listOperator.getTerms().get(i) );
				}
				
				// Variable
				if (  this.listOperator.getTerms().get(i).isVariable() ) {

					this.recreateVariableRow(this.listOperator.getTerms().get(i) );
				}
				
				
				// Uniterm
				if (  this.listOperator.getTerms().get(i).isUniterm() ) {

					this.recreateUnitermRow(this.listOperator.getTerms().get(i) );
				}
				
				// List
				if (  this.listOperator.getTerms().get(i).isList() ) {

					this.recreateListRow(this.listOperator.getTerms().get(i) );
				}
			}
		}
		
		
		this.createNextTermCombo();
		this.updateSize();
	}
	
	
	
	
	
	/* ***************** **
	 * Getter and Setter **
	 * ***************** */
	

	public ListOperator getListOperator() {
		return listOperator;
	}

	public void setListOperator(ListOperator factOperator) {
		this.listOperator = factOperator;
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
