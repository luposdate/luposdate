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
import javax.swing.border.EtchedBorder;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.RuleGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.HintTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.JIconButton;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.Term;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.TermConnection;
public class UnitermOperatorPanel extends AbstractGuiComponent<Operator> {

	private static final long serialVersionUID = 8238554719560169292L;

	private VisualRifEditor visualRifEditor;

	protected GridBagConstraints gbc = null;
	private JPanel termRowsPanel;
	protected UnitermOperator unitermOperator;
	private  String selectedChoiceString = "";
	String[] typComboEntries = {"Const","Var","Expr","List"};
	private FocusListener comboBoxFocusListener;

	// Constructor
	/**
	 * <p>Constructor for UnitermOperatorPanel.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
	 * @param unitermOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator} object.
	 * @param startNode a boolean.
	 * @param alsoSubClasses a boolean.
	 * @param visualRifEditor a {@link lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor} object.
	 */
	public UnitermOperatorPanel(final VisualGraph<Operator> parent,
			final GraphWrapper gw, final UnitermOperator unitermOperator,
			final boolean startNode, final boolean alsoSubClasses, final VisualRifEditor visualRifEditor) {

		super(parent, gw, unitermOperator, true);

		this.visualRifEditor = visualRifEditor;

		this.unitermOperator = unitermOperator;

		this.init();
	}

	/**
	 * <p>init.</p>
	 */
	public void init() {
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

		/* ******** **
		 * Elements **
		 * ******** */
		// UniTermCombo
		this.unitermOperator.getUniTermComboBox().setFont(this.parent.getFONT());
		this.unitermOperator.getUniTermComboBox().setEditable(false);
		this.unitermOperator.getUniTermComboBox().setMaximumRowCount(3);
		this.unitermOperator.getUniTermComboBox().setSelectedItem(
				this.unitermOperator.getSelectedPrefix());
		this.unitermOperator.getUniTermComboBox().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				final JComboBox selectedChoice = (JComboBox) e.getSource();
				UnitermOperatorPanel.this.setSelectedPrefix((String) selectedChoice.getSelectedItem());
			}
		});

		// Labels
		final Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		final JLabel uniTermLabel = new JLabel("UNITERM");
		uniTermLabel.setFont(this.parent.getFONT());
		uniTermLabel.setBorder(loweredetched);

		final JLabel constLabel = new JLabel("Function:");
		constLabel.setFont(this.parent.getFONT());

		// External
		final JCheckBox cbExternal = new JCheckBox( "External", this.unitermOperator.isExternal());
		cbExternal.setFont(this.parent.getFONT());
		final ItemListener ilExternal = new ItemListener() {
			  @Override public void itemStateChanged( final ItemEvent e ) {
				  UnitermOperatorPanel.this.unitermOperator.setExternal(!UnitermOperatorPanel.this.unitermOperator.isExternal());
			  }
			};
		cbExternal.addItemListener(ilExternal);

		// Named Uniterm
		final JCheckBox cbName = new JCheckBox( "Named", false);
		cbName.setFont(this.parent.getFONT());
		final ItemListener ilName = new ItemListener() {
			  @Override public void itemStateChanged( final ItemEvent e ) {
				   UnitermOperatorPanel.this.unitermOperator.setNamed(!UnitermOperatorPanel.this.unitermOperator.isNamed());
				   UnitermOperatorPanel.this.repaintAllTerms();
			  }
			};
		cbName.addItemListener(ilName);

		// TextField
		final JTextFieldResizing tf = new JTextFieldResizing(this.unitermOperator.getTermName(), this.parent.getFONT(), this);

		tf.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				UnitermOperatorPanel.this.setFactName(tf.getText());
			}
		});

		/* *********** **
		 * calibration **
		 * *********** */
		// first row
		if (this.unitermOperator.isNamed()){
			this.gbc.gridx = 3;
		}else{
			this.gbc.gridx = 2;
		}

		this.termRowsPanel.add(uniTermLabel ,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(cbExternal);

		this.gbc.gridx++;

		this.termRowsPanel.add(cbName);

		this.gbc.gridy++;

		// second row
		this.gbc.gridx = 2;

		this.termRowsPanel.add(constLabel,this.gbc);

		this.gbc.gridx++;

		// first JCombobo
		this.termRowsPanel.add(this.unitermOperator.getUniTermComboBox(), this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(tf, this.gbc);

		this.gbc.gridy++;

		this.gbc.gridx = 0;

		if ( this.unitermOperator.hasElements() ) {
			for (int i = 0; i < this.unitermOperator.getTerms().size(); i++) {
				// Constant
				if ( !this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isConstant() ) {
					this.createConstantTerm(this.unitermOperator.getTerms().get(i) );
				}
				if ( this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isConstant() ) {
					this.recreateConstantRow(this.unitermOperator.getTerms().get(i) );
				}

				// Variable
				if ( !this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isVariable() ) {
					this.createVariableTerm(this.unitermOperator.getTerms().get(i) );
				}
				if ( this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isVariable() ) {
					this.recreateVariableRow(this.unitermOperator.getTerms().get(i) );
				}

				// Uniterm
				if ( !this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isUniterm() ) {
					this.createUnitermTerm(this.unitermOperator.getTerms().get(i) );
				}
				if ( this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isUniterm() ) {
					this.recreateUnitermRow(this.unitermOperator.getTerms().get(i) );
				}

				// List
				if ( !this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isList() ) {
					this.createListTerm(this.unitermOperator.getTerms().get(i) );
				}
				if ( this.unitermOperator.getTerms().get(i).isInit() && this.unitermOperator.getTerms().get(i).isList() ) {
					this.recreateListRow(this.unitermOperator.getTerms().get(i) );
				}
			}
		}

		this.createNextTermCombo();

		this.add(this.termRowsPanel);

		// FocusListener
		final FocusListener FL = new FocusListener() {
			@Override
			public void focusGained(final FocusEvent arg0) {
				if (UnitermOperatorPanel.this.visualRifEditor.getDocumentContainer().getActiveDocument()
						.getDocumentEditorPane().getPrefixList().length > 0) {

					UnitermOperatorPanel.this.unitermOperator.savePrefixes();
					UnitermOperatorPanel.this.unitermOperator.saveUnitermPrefix();
					UnitermOperatorPanel.this.unitermOperator.saveNamePrefixes();

					UnitermOperatorPanel.this.unitermOperator.setConstantComboBoxEntries(UnitermOperatorPanel.this.visualRifEditor
							.getDocumentContainer().getActiveDocument()
							.getDocumentEditorPane().getPrefixList());
				}
			}

			@Override
			public void focusLost(final FocusEvent arg0) {
			}
		};

		this.setComboBoxFocusListener(FL);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				UnitermOperatorPanel.this.updateSize();
			}
		});

		if(this.unitermOperator.getConstantComboBox().getFocusListeners().length <= 1) {
			this.unitermOperator.getConstantComboBox().addFocusListener(FL);
		}

		if (this.unitermOperator.getUniTermComboBox().getFocusListeners().length <= 1) {
			this.unitermOperator.getUniTermComboBox().addFocusListener(FL);
		}

		this.updateSize();
	}

	/*
	 * Constant
	 */
	private void createConstantRow() {
		final Term term = new Term();
		term.setConstant( true );
		/*
		 *  Elements
		 */
		// PrefixCombo
		final JComboBox constCombo  = new JComboBox();
		constCombo.setFont(this.parent.getFONT());
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

		// NameConstCombo
		final JComboBox namedConstCombo  = new JComboBox();
		namedConstCombo.setFont(this.parent.getFONT());
		namedConstCombo.addFocusListener(this.comboBoxFocusListener);
		namedConstCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {

						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();

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
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value", "Value", this.parent.getFONT(), this);
		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				}});
		// Label
		final JLabel label = new JLabel("Constant:");
		label.setFont(this.parent.getFONT());
		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {

					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();

					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
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

		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.termRowsPanel.add(constCombo,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(tfValue, this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);

		this.unitermOperator.getTerms().add( term );

		this.updateSize();
	}

	private void createConstantTerm(final Term term) {

		final String selectedPref = term.getSelectedPrefix();
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
		for (final String s : term.getComboEntries()){
			term.getConstantCombo().addItem(s);
		}
		term.getConstantCombo().setSelectedItem(selectedPref);
		// NameCombo
				final JComboBox namedConstCombo  = new JComboBox();
				namedConstCombo.addFocusListener(this.comboBoxFocusListener);
				namedConstCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
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
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(), "Value", this.parent.getFONT(), this);
		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				}});

		// Label
		final JLabel label = new JLabel("Constant:");
		label.setFont(this.parent.getFONT());

		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
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

		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.termRowsPanel.add(constCombo,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(tfValue, this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);
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

		if ( this.unitermOperator.isNamed() ){
			this.termRowsPanel.add(term.getNameComboBox(),this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(term.getTfName(),this.gbc);
			this.gbc.gridx++;
		}

		this.termRowsPanel.add(term.getConstantCombo(),this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getTextFieldResizing(), this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
	}

	/*
	 * Variable
	 */
	private void createVariableRow() {
		final Term term = new Term();
		term.setVariable( true );
		/*
		 *  Elements
		 */
		// PrefixCombo
		final JComboBox namedConstCombo  = new JComboBox();
		namedConstCombo.addFocusListener(this.comboBoxFocusListener);
		namedConstCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		term.setNameComboBox(namedConstCombo);
		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name", "Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing("Value", "Value", this.parent.getFONT(), this);

		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				}});

		// Label
		final JLabel label = new JLabel("Variable:");
		label.setFont(this.parent.getFONT());

		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);
		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
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

		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(tfValue,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);

		this.unitermOperator.getTerms().add( term );

		this.updateSize();
	}

	private void createVariableTerm(final Term term) {
		/*
		 *  Elements
		 */
		// PrefixCombo
				final JComboBox namedConstCombo  = new JComboBox();
				namedConstCombo.addFocusListener(this.comboBoxFocusListener);
				namedConstCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		term.setNameComboBox(namedConstCombo);
		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name","Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// TextField Value
		final JTextFieldResizing tfValue = new HintTextFieldResizing(term.getValue(), "Value", this.parent.getFONT(), this);
		tfValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setValue(tfValue.getText());
				}});

		// Label
		final JLabel label = new JLabel("Variable:");
		label.setFont(this.parent.getFONT());

		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
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


		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(tfValue,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setTextFieldResizing(tfValue);
		term.setTfName(tfName);
		term.setInit(true);

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

		if ( this.unitermOperator.isNamed() ){
			this.termRowsPanel.add(term.getNameComboBox(),this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(term.getTfName(),this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getTextFieldResizing(), this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
	}

	/*
	 * Uniterm
	 */
	private void createUnitermRow(){
		final Term term = new Term();
		term.setUniterm(true);
		/*
		 *  Elements
		 */
		// PrefixCombo
		final JComboBox namedConstCombo  = new JComboBox();
		namedConstCombo.addFocusListener(this.comboBoxFocusListener);
		namedConstCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		term.setNameComboBox(namedConstCombo);

		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name", "Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// Label
		final JLabel label = new JLabel("Uniterm:");
		label.setFont(this.parent.getFONT());

		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
		 }});


		final JButton connectionButton = new JButton("Connection");

		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					final RuleGraph ruleGraph = (RuleGraph) UnitermOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,UnitermOperatorPanel.this.unitermOperator,term);
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

		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(connectionButton,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setTfName(tfName);
		term.setInit(true);

		this.unitermOperator.getTerms().add( term );

		this.updateSize();
	}

	private void createUnitermTerm(final Term term) {
		/*
		 *  Elements
		 */
		// PrefixCombo
		final JComboBox namedConstCombo  = new JComboBox();
		namedConstCombo.addFocusListener(this.comboBoxFocusListener);
		namedConstCombo.addItemListener( new ItemListener() {
		@Override
		public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		term.setNameComboBox(namedConstCombo);

		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name","Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// Label
		final JLabel label = new JLabel("Uniterm:");
		label.setFont(this.parent.getFONT());

		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
		 }});

		final JButton connectionButton = new JButton("Connection");

		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					final RuleGraph ruleGraph = (RuleGraph) UnitermOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,UnitermOperatorPanel.this.unitermOperator,term);
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

		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(connectionButton,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setTfName(tfName);
		term.setInit(true);

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

		if ( this.unitermOperator.isNamed() ){
			this.termRowsPanel.add(term.getNameComboBox(),this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(term.getTfName(),this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getConnectionButton(), this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
	}

	/*
	 * List
	 */
	private void createListRow(){
		final Term term = new Term();
		term.setList(true);
		/*
		 *  Elements
		 */
		// PrefixCombo
		final JComboBox namedConstCombo  = new JComboBox();
		namedConstCombo.addFocusListener(this.comboBoxFocusListener);
		namedConstCombo.addItemListener( new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );
		term.setNameComboBox(namedConstCombo);

		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name", "Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// Label
		final JLabel label = new JLabel("List:");
		label.setFont(this.parent.getFONT());

		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
		 }});


		final JButton connectionButton = new JButton("Connection");

		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					final RuleGraph ruleGraph = (RuleGraph) UnitermOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,UnitermOperatorPanel.this.unitermOperator,term);
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

		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(connectionButton,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setTfName(tfName);
		term.setInit(true);

		this.unitermOperator.getTerms().add( term );

		this.updateSize();
	}

	private void createListTerm(final Term term) {
		/*
		 *  Elements
		 */
		// PrefixCombo
		final JComboBox namedConstCombo  = new JComboBox();
		namedConstCombo.addFocusListener(this.comboBoxFocusListener);
		namedConstCombo.addItemListener( new ItemListener() {
		@Override
		public void itemStateChanged(final ItemEvent evt) {
						   if (evt.getStateChange() == ItemEvent.SELECTED) {
							   final JComboBox selectedChoice = (JComboBox)evt.getSource();
						        term.setPrefixForName(selectedChoice.getSelectedItem().toString());
						   }
						   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
						}
				    } );

		term.setNameComboBox(namedConstCombo);

		// TextField Name
		final JTextFieldResizing tfName = new HintTextFieldResizing("Name", "Name", this.parent.getFONT(), this);
		tfName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent fe) {
				term.setTfName(tfName);
				}});

		// Label
		final JLabel label = new JLabel("List:");
		label.setFont(this.parent.getFONT());

		// Buttons
		final Dimension buttonDimension = new Dimension();
		buttonDimension.setSize(30d, 24d);

		final JIconButton upButton = new JIconButton("icons/001_24.png");
		upButton.setPreferredSize(buttonDimension);
		upButton.setMaximumSize(buttonDimension);
		upButton.setMinimumSize(buttonDimension);

		upButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					UnitermOperatorPanel.this.unitermOperator.swapTerms(term,true);
					UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton downButton = new JIconButton("icons/001_22.png");
		downButton.setPreferredSize(buttonDimension);
		downButton.setMaximumSize(buttonDimension);
		downButton.setMinimumSize(buttonDimension);

		downButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
				 UnitermOperatorPanel.this.unitermOperator.swapTerms(term,false);
				 UnitermOperatorPanel.this.repaintAllTerms();
					 }});

		final JIconButton deleteButton = new JIconButton("icons/001_02.png");
		deleteButton.setPreferredSize(buttonDimension);
		deleteButton.setMaximumSize(buttonDimension);
		deleteButton.setMinimumSize(buttonDimension);

		deleteButton.addActionListener(new ActionListener(){
		 @Override
		public void actionPerformed(final ActionEvent e) {
		  UnitermOperatorPanel.this.removeRow(term);
		  UnitermOperatorPanel.this.unitermOperator.getTerms().remove(term);
		 }});


		final JButton connectionButton = new JButton("Connection");

		connectionButton.addActionListener(new ActionListener(){
			 @Override
			public void actionPerformed(final ActionEvent e) {
					final RuleGraph ruleGraph = (RuleGraph) UnitermOperatorPanel.this.parent;
					ruleGraph.getVisualEditor().connectionMode = new TermConnection(ruleGraph,UnitermOperatorPanel.this.unitermOperator,term);
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

		if ( this.unitermOperator.isNamed() ) {
			this.termRowsPanel.add(namedConstCombo,this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(tfName,this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(connectionButton,this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(deleteButton,this.gbc);

		this.termRowsPanel.remove(this.unitermOperator.getNextTermCombo());

		this.createNextTermCombo();

		term.setDeleteButton(deleteButton);
		term.setUpButton(upButton);
		term.setDownButton(downButton);
		term.setLabel(label);
		term.setConnectionButton(connectionButton);
		term.setTfName(tfName);
		term.setInit(true);

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

		if ( this.unitermOperator.isNamed() ){
			this.termRowsPanel.add(term.getNameComboBox(),this.gbc);
			this.gbc.gridx++;
			this.termRowsPanel.add(term.getTfName(),this.gbc);
			this.gbc.gridx++;
		}

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getConnectionButton(), this.gbc);

		this.gbc.gridx++;

		this.termRowsPanel.add(term.getDeleteButton(),this.gbc);
	}


	/*
	 * Util
	 */
	/**
	 * <p>removeRow.</p>
	 *
	 * @param term a {@link lupos.gui.operatorgraph.visualeditor.visualrif.util.Term} object.
	 */
	public void removeRow(final Term term) {
		// Constant
		if ( term.isConstant() ) {
			this.termRowsPanel.remove( term.getConstantCombo() );
			this.termRowsPanel.remove( term.getTextFieldResizing() );

			if ( this.unitermOperator.isNamed() ){
				this.termRowsPanel.remove( term.getTfName() );
				this.termRowsPanel.remove( term.getNameComboBox() );
			}
		}

		// Variable
		if ( term.isVariable() ){
			this.termRowsPanel.remove( term.getTextFieldResizing() );
			if ( this.unitermOperator.isNamed() ){
				this.termRowsPanel.remove( term.getTfName() );
				this.termRowsPanel.remove(term.getNameComboBox());
			}
		}

		// Uniterm + List
		if ( term.isUniterm() || term.isList() ){
			this.termRowsPanel.remove( term.getConnectionButton() );
			if ( this.unitermOperator.isNamed() ){
				this.termRowsPanel.remove( term.getTfName() );
				this.termRowsPanel.remove(term.getNameComboBox());
			}
		}

		this.termRowsPanel.remove(term.getUpButton());
		this.termRowsPanel.remove(term.getDownButton());
		this.termRowsPanel.remove(term.getDeleteButton());
		this.termRowsPanel.remove(term.getLabel());
		this.updateSize();
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		return false;
	}

	/**
	 * <p>repaintAllTerms.</p>
	 */
	public void repaintAllTerms(){
		for (int i = 0; i < this.unitermOperator.getTerms().size(); i++) {
			this.removeRow(this.unitermOperator.getTerms().get(i));
		}
		if ( this.unitermOperator.hasElements() ) {
			for (int i = 0; i < this.unitermOperator.getTerms().size(); i++) {
				if (  this.unitermOperator.getTerms().get(i).isConstant() ){
					if ( !this.unitermOperator.isNamed() ) {
						this.termRowsPanel.remove(this.unitermOperator.getTerms().get(i).getTfName());
						this.termRowsPanel.remove(this.unitermOperator.getTerms().get(i).getNameComboBox());
					}
					this.recreateConstantRow(this.unitermOperator.getTerms().get(i) );
				}
				if (  this.unitermOperator.getTerms().get(i).isVariable() ) {
					if ( !this.unitermOperator.isNamed() ) {
						this.termRowsPanel.remove(this.unitermOperator.getTerms().get(i).getTfName());
						this.termRowsPanel.remove(this.unitermOperator.getTerms().get(i).getNameComboBox());
					}
					this.recreateVariableRow(this.unitermOperator.getTerms().get(i) );
				}
				if (  this.unitermOperator.getTerms().get(i).isUniterm() || this.unitermOperator.getTerms().get(i).isList() ) {
					if ( !this.unitermOperator.isNamed() ) {
						this.termRowsPanel.remove(this.unitermOperator.getTerms().get(i).getTfName());
						this.termRowsPanel.remove(this.unitermOperator.getTerms().get(i).getNameComboBox());
					}
					if( this.unitermOperator.getTerms().get(i).isUniterm() ) {
						this.recreateUnitermRow( this.unitermOperator.getTerms().get(i) );
					}
					if( this.unitermOperator.getTerms().get(i).isList()) {
						this.recreateListRow( this.unitermOperator.getTerms().get(i) );
					}
				}
			}
		}
		this.createNextTermCombo();
		this.updateSize();
	}

	private void createNextTermCombo() {
		this.unitermOperator.getNextTermCombo().addItemListener( new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent evt) {
				   if (evt.getStateChange() == ItemEvent.SELECTED) {
					   final JComboBox selectedChoice = (JComboBox)evt.getSource();
					   // Constant
				        if ( selectedChoice.getSelectedItem().equals("Const") ){
				        	// New Term
				        	UnitermOperatorPanel.this.createConstantRow();
				        	UnitermOperatorPanel.this.unitermOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				 	   // Variable
				        if ( selectedChoice.getSelectedItem().equals("Var") ){
				        	UnitermOperatorPanel.this.createVariableRow();
				        	UnitermOperatorPanel.this.unitermOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				 	   // Expression
				        if ( selectedChoice.getSelectedItem().equals("Expr") ){
				        	UnitermOperatorPanel.this.createUnitermRow();
				        	UnitermOperatorPanel.this.unitermOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				 	   // List
				        if ( selectedChoice.getSelectedItem().equals("List") ){
				        	UnitermOperatorPanel.this.createListRow();
				        	UnitermOperatorPanel.this.unitermOperator.getNextTermCombo().setSelectedIndex(0);
				        }
				   }
				   else if (evt.getStateChange() == ItemEvent.DESELECTED) {}
				}
		    } );
		this.gbc.gridy++;
		this.gbc.gridx = 4;
		this.termRowsPanel.add(this.unitermOperator.getNextTermCombo(),this.gbc);
	}

	/* ***************** **
	 * Getter and Setter **
	 * ***************** */
	/**
	 * <p>Getter for the field <code>unitermOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator} object.
	 */
	public UnitermOperator getUnitermOperator() {
		return this.unitermOperator;
	}

	/**
	 * <p>Setter for the field <code>unitermOperator</code>.</p>
	 *
	 * @param factOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator} object.
	 */
	public void setUnitermOperator(final UnitermOperator factOperator) {
		this.unitermOperator = factOperator;
	}

	/**
	 * <p>Getter for the field <code>selectedChoiceString</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSelectedChoiceString() {
		return this.selectedChoiceString;
	}

	/**
	 * <p>Setter for the field <code>selectedChoiceString</code>.</p>
	 *
	 * @param selectedChoiceString a {@link java.lang.String} object.
	 */
	public void setSelectedChoiceString(final String selectedChoiceString) {
		this.selectedChoiceString = selectedChoiceString;
	}

	private void setSelectedPrefix(final String selectedItem) {
		this.unitermOperator.setSelectedPrefix(selectedItem);
	}

	private void setFactName(final String text) {
		 this.unitermOperator.setTermName(text);
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
	 * <p>Getter for the field <code>comboBoxFocusListener</code>.</p>
	 *
	 * @return a {@link java.awt.event.FocusListener} object.
	 */
	public FocusListener getComboBoxFocusListener() {
		return this.comboBoxFocusListener;
	}

	/**
	 * <p>Setter for the field <code>comboBoxFocusListener</code>.</p>
	 *
	 * @param comboBoxFocusListener a {@link java.awt.event.FocusListener} object.
	 */
	public void setComboBoxFocusListener(final FocusListener comboBoxFocusListener) {
		this.comboBoxFocusListener = comboBoxFocusListener;
	}
}
