
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.gui.operatorgraph.visualeditor.visualrif.guielements.operatorPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.GraphWrapperOperator;
public class ClassificationOperatorPanel extends AbstractGuiComponent<Operator> {

	private static final long serialVersionUID = 3163766913198444249L;

	final private boolean[] EQAULITYSELECTED = {true, false, false};
	final private boolean[] MEMBERSHIPSELECTED = {false, true, false};
	final private boolean[] SUBCLASSSELECTED = {false, false, true};

	protected GridBagConstraints gbc = null;
	private VariableOperator variableOperator;
	private ConstantOperator constantOperator;
	private UnitermOperator unitermOperator;
	private ListOperator listOperator;
	private boolean variable, constant, uniterm, list;

	// Constructor
	/**
	 * <p>Constructor for ClassificationOperatorPanel.</p>
	 *
	 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
	 * @param parentOp a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 * @param childOp a {@link lupos.gui.operatorgraph.visualeditor.operators.Operator} object.
	 */
	public ClassificationOperatorPanel(final VisualGraph<Operator> parent, final Operator parentOp, final Operator childOp){
		super(parent, new GraphWrapperOperator(parentOp), childOp, false);

		// Variable to Variable
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof VariableOperator) ) {

			final VariableOperator parentVO = ( VariableOperator ) parentOp;
			final VariableOperator childVO = ( VariableOperator ) childOp;

			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childVO;
			this.setVariable(true);
			this.init();

		} // End Variable to Variable

		// Constant to Constant
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof ConstantOperator) ) {

			final ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			final ConstantOperator childCO = ( ConstantOperator ) childOp;

			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childCO;
			this.setConstant(true);
			this.init();

		} // End Constant to Constant

		// Constant to Variable
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof VariableOperator) ) {

			final ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			final VariableOperator childVO = ( VariableOperator ) childOp;

			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childVO;
			this.setConstant(true);
			this.init();

		} // End Constant to Constant

		// Variable to Constant
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof ConstantOperator) ) {

			final VariableOperator parentVO = ( VariableOperator ) parentOp;
			final ConstantOperator childCO = ( ConstantOperator ) childOp;

			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childCO;
			this.setVariable(true);
			this.init();

		} // End Variable to Constant

		// Uniterm to Uniterm
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof UnitermOperator) ) {

			final UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			final UnitermOperator childFO = ( UnitermOperator ) childOp;

			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childFO;
			this.setUniterm(true);
			this.init();

		} // End Uniterm to Uniterm

		// Uniterm to Constant
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof ConstantOperator) ) {

			final UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			final ConstantOperator childCO = ( ConstantOperator ) childOp;

			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childCO;
			this.setUniterm(true);
			this.init();

		} // End Uniterm to Constant

		// Constant to Uniterm
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof UnitermOperator) ) {

			final ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			final UnitermOperator childFO = ( UnitermOperator ) childOp;

			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childFO;
			this.setConstant(true);
			this.init();

		} // End Constant to Uniterm

		// Uniterm to Variable
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof VariableOperator) ) {

			final UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			final VariableOperator childVO = ( VariableOperator ) childOp;

			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childVO;
			this.setUniterm(true);
			this.init();

			} // End Uniterm to Variable

		// Variable to Uniterm
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof UnitermOperator) ) {

			final VariableOperator parentVO = ( VariableOperator ) parentOp;
			final UnitermOperator childFO = ( UnitermOperator ) childOp;

			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childFO;
			this.setVariable(true);
			this.init();

			} // End Variable to Uniterm

		// Variable to List
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof ListOperator) ) {

			final VariableOperator parentVO = ( VariableOperator ) parentOp;
			final ListOperator childLO = ( ListOperator ) childOp;

			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childLO;
			this.setVariable(true);
			this.init();

			}
		// End Variable to List

		// List to Variable
		if ( (parentOp instanceof ListOperator) && (childOp instanceof VariableOperator) ) {

			final ListOperator parentLO = ( ListOperator ) parentOp;
			final VariableOperator childVO = ( VariableOperator ) childOp;

			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childVO;
			this.setList(true);
			this.init();

		}
		// End List to Variable

		// Constant to List
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof ListOperator) ) {

			final ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			final ListOperator childLO = ( ListOperator ) childOp;

			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childLO;
			this.setConstant(true);
			this.init();

		}
		// End Constant to List

		// List to Constant
		if ( (parentOp instanceof ListOperator) && (childOp instanceof ConstantOperator) ) {

			final ListOperator parentLO = ( ListOperator ) parentOp;
			final ConstantOperator childCO = ( ConstantOperator ) childOp;

			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childCO;
			this.setList(true);
			this.init();

		}
		// End List to Constant


		// Uniterm to List
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof ListOperator) ) {

			final UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			final ListOperator childLO = ( ListOperator ) childOp;

			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childLO;
			this.setUniterm(true);
			this.init();

		}
		// End Uniterm to List

		// List to Uniterm
		if ( (parentOp instanceof ListOperator) && (childOp instanceof UnitermOperator) ) {

			final ListOperator parentLO = ( ListOperator ) parentOp;
			final UnitermOperator childFO = ( UnitermOperator ) childOp;

			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childFO;
			this.setList(true);
			this.init();

		}
		// End List to Uniterm

		// List to List
		if ( (parentOp instanceof ListOperator) && (childOp instanceof ListOperator) ) {

			final ListOperator parentLO = ( ListOperator ) parentOp;
			final ListOperator childLO = ( ListOperator ) childOp;

			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childLO;
			this.setList(true);
			this.init();
		}
	}

	private void init() {
		this.setLayout(new GridBagLayout());

		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTHWEST;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;

		this.gbc.insets = new Insets((int) this.parent.PADDING,
				(int) this.parent.PADDING, (int) this.parent.PADDING,
				(int) this.parent.PADDING);

		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;

		final Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.setBorder(raisedbevel);

		final JRadioButton rb1 = new JRadioButton( "Equality ( = )" );
		final JRadioButton rb2 = new JRadioButton( "Membership ( # )" );
		final JRadioButton rb3 = new JRadioButton( "Subclass ( ## )" );
		rb1.setSelected(true);

		rb1.setFont(this.parent.getFONT());
		rb2.setFont(this.parent.getFONT());
		rb3.setFont(this.parent.getFONT());

		final ItemListener rbListener = new ItemListener() {
		  @Override public void itemStateChanged( final ItemEvent e ) {

		    if(e.getStateChange() == ItemEvent.SELECTED){

		    	if(( (JRadioButton) e.getItem() ).getText().equals( "Subclass ( ## )" )) {
		    		ClassificationOperatorPanel.this.setSelectedClassification( "##" ) ;
		    		if ( ClassificationOperatorPanel.this.isVariable() ) {
						ClassificationOperatorPanel.this.variableOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.SUBCLASSSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isConstant() ) {
						ClassificationOperatorPanel.this.constantOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.SUBCLASSSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isUniterm() ) {
						ClassificationOperatorPanel.this.unitermOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.SUBCLASSSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isList() ) {
						ClassificationOperatorPanel.this.listOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.SUBCLASSSELECTED );
					}
		    	}

		    	if(( (JRadioButton) e.getItem() ).getText().equals( "Membership ( # )" )){
		    		ClassificationOperatorPanel.this.setSelectedClassification( "#" ) ;
		    		if ( ClassificationOperatorPanel.this.isVariable() ) {
						ClassificationOperatorPanel.this.variableOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.MEMBERSHIPSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isConstant() ) {
						ClassificationOperatorPanel.this.constantOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.MEMBERSHIPSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isUniterm() ) {
						ClassificationOperatorPanel.this.unitermOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.MEMBERSHIPSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isList() ) {
						ClassificationOperatorPanel.this.listOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.MEMBERSHIPSELECTED );
					}
		    	}

		    	if(( (JRadioButton) e.getItem() ).getText().equals( "Equality ( = )" )){
		    		ClassificationOperatorPanel.this.setSelectedClassification( "=" ) ;
		    		if ( ClassificationOperatorPanel.this.isVariable() ) {
						ClassificationOperatorPanel.this.variableOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.EQAULITYSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isConstant() ) {
						ClassificationOperatorPanel.this.constantOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.EQAULITYSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isUniterm() ) {
						ClassificationOperatorPanel.this.unitermOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.EQAULITYSELECTED );
					}
		    		if ( ClassificationOperatorPanel.this.isList() ) {
						ClassificationOperatorPanel.this.listOperator.setSelectedRadioButton( ClassificationOperatorPanel.this.EQAULITYSELECTED );
					}
		    	}

		    }
		  }
		};
		boolean[] tmp = {};
		if ( this.isVariable() ) {
			tmp =  this.variableOperator.getSelectedRadioButton();
		}
		if ( this.isConstant() ) {
			tmp =  this.constantOperator.getSelectedRadioButton();
		}
		if ( this.isUniterm() ) {
			tmp = this.unitermOperator.getSelectedRadioButton();
		}
		if ( this.isList() ) {
			tmp = this.listOperator.getSelectedRadioButton();
		}
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i] == true) {
				if ((i + 1) == 1) {
					rb1.setSelected( true );
					rb2.setSelected( false );
					rb3.setSelected( false );
				} else if ((i + 1) == 2) {
					rb1.setSelected( false );
					rb2.setSelected( true );
					rb3.setSelected( false );
				} else if ((i + 1) == 3) {
					rb1.setSelected( false );
					rb2.setSelected( false );
					rb3.setSelected( true );
				}
				break;
			}
		}
		rb1.addItemListener( rbListener );
		rb2.addItemListener( rbListener );
		rb3.addItemListener( rbListener );

		// Set radio buttons on the ButtonGroup
		final ButtonGroup g = new ButtonGroup();
		g.add( rb1 );
		g.add( rb2 );
		g.add( rb3 );

		this.add(rb1,this.gbc);

		this.gbc.gridy++;

		this.add(rb2,this.gbc);

		this.gbc.gridy++;

		this.add(rb3,this.gbc);
	}

	/** {@inheritDoc} */
	@Override
	public boolean validateOperatorPanel(final boolean showErrors, final Object data) {
		return true;
	}

	/**
	 * <p>setSelectedClassification.</p>
	 *
	 * @param selectedClassification a {@link java.lang.String} object.
	 */
	public void setSelectedClassification(final String selectedClassification) {
		if ( this.isVariable() ){
			final VariableOperator vp = ( VariableOperator ) this.parentOp;
			vp.setSelectedClassification(selectedClassification);
		}
		if ( this.isConstant() ){
			final ConstantOperator cp = ( ConstantOperator ) this.parentOp;
			cp.setSelectedClassification(selectedClassification);
		}
		if ( this.isUniterm() ){
			final UnitermOperator fp = ( UnitermOperator ) this.parentOp;
			fp.setSelectedClassification(selectedClassification);
		}
		if ( this.isList() ){
			final ListOperator lp = ( ListOperator ) this.parentOp;
			lp.setSelectedClassification(selectedClassification);
		}
	}

	/* *************** **
	 * Getter + Setter **
	 * *************** */
	/**
	 * <p>Setter for the field <code>variableOperator</code>.</p>
	 *
	 * @param variableOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator} object.
	 */
	public void setVariableOperator(final VariableOperator variableOperator) {
		this.variableOperator = variableOperator;
	}

	/**
	 * <p>Getter for the field <code>variableOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.VariableOperator} object.
	 */
	public VariableOperator getVariableOperator() {
		return this.variableOperator;
	}

    /**
     * <p>Setter for the field <code>constantOperator</code>.</p>
     *
     * @param constantOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator} object.
     */
    public void setConstantOperator(final ConstantOperator constantOperator) {
		this.constantOperator = constantOperator;
	}

	/**
	 * <p>Getter for the field <code>constantOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator} object.
	 */
	public ConstantOperator getConstantOperator() {
		return this.constantOperator;
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
	 * <p>isVariable.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isVariable() {
		return this.variable;
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
	 * <p>isConstant.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isConstant() {
		return this.constant;
	}

	/**
	 * <p>Getter for the field <code>listOperator</code>.</p>
	 *
	 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator} object.
	 */
	public ListOperator getListOperator() {
		return this.listOperator;
	}

	/**
	 * <p>Setter for the field <code>listOperator</code>.</p>
	 *
	 * @param listOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ListOperator} object.
	 */
	public void setListOperator(final ListOperator listOperator) {
		this.listOperator = listOperator;
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
     * @param unitermOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.UnitermOperator} object.
     */
    public void setUnitermOperator(final UnitermOperator unitermOperator) {
		this.unitermOperator = unitermOperator;
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
}
