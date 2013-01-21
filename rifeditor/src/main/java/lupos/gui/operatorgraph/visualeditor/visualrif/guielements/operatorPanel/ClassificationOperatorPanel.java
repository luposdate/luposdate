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
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
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
//	private AbstractTermOperator abstractTermOperator;
	
	private boolean variable, constant, uniterm, list;

	



	// Constructor
	public ClassificationOperatorPanel(VisualGraph<Operator> parent, final Operator parentOp, final Operator childOp){
		super(parent, new GraphWrapperOperator(parentOp), childOp, false);
		
		// Variable to Variable
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof VariableOperator) ) {
			
			VariableOperator parentVO = ( VariableOperator ) parentOp;
			VariableOperator childVO = ( VariableOperator ) childOp;
			
			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childVO;
			this.setVariable(true);
			this.init();
			
		} // End Variable to Variable
		
		
		// Constant to Constant
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof ConstantOperator) ) {
			
			ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			ConstantOperator childCO = ( ConstantOperator ) childOp;
			
			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childCO;
			this.setConstant(true);
			this.init();
			
		} // End Constant to Constant

		// Constant to Variable
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof VariableOperator) ) {
			
			ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			VariableOperator childVO = ( VariableOperator ) childOp;
			
			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childVO;
			this.setConstant(true);
			this.init();
			
		} // End Constant to Constant
		
		
		// Variable to Constant
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof ConstantOperator) ) {
			
			VariableOperator parentVO = ( VariableOperator ) parentOp;
			ConstantOperator childCO = ( ConstantOperator ) childOp;
			
			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childCO;
			this.setVariable(true);
			this.init();
			
		} // End Variable to Constant
		
		// Uniterm to Uniterm
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof UnitermOperator) ) {
			
			UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			UnitermOperator childFO = ( UnitermOperator ) childOp;
			
			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childFO;
			this.setUniterm(true);
			this.init();
			
		} // End Uniterm to Uniterm
		
		// Uniterm to Constant
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof ConstantOperator) ) {
			
			UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			ConstantOperator childCO = ( ConstantOperator ) childOp;
			
			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childCO;
			this.setUniterm(true);
			this.init();
			
		} // End Uniterm to Constant
		
		// Constant to Uniterm
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof UnitermOperator) ) {
			
			ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			UnitermOperator childFO = ( UnitermOperator ) childOp;
			
			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childFO;
			this.setConstant(true);
			this.init();
			
		} // End Constant to Uniterm
		
		
		// Uniterm to Variable
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof VariableOperator) ) {
					
			UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			VariableOperator childVO = ( VariableOperator ) childOp;
					
			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childVO;
			this.setUniterm(true);
			this.init();
					
			} // End Uniterm to Variable
				
		// Variable to Uniterm
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof UnitermOperator) ) {
					
			VariableOperator parentVO = ( VariableOperator ) parentOp;
			UnitermOperator childFO = ( UnitermOperator ) childOp;
					
			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childFO;
			this.setVariable(true);
			this.init();
					
			} // End Variable to Uniterm		
		
		
		// Variable to List
		if ( (parentOp instanceof VariableOperator) && (childOp instanceof ListOperator) ) {
			
			VariableOperator parentVO = ( VariableOperator ) parentOp;
			ListOperator childLO = ( ListOperator ) childOp;
					
			this.setVariableOperator(parentVO);
			this.parentOp = parentVO;
			this.child = childLO;
			this.setVariable(true);
			this.init();
					
			}
		// End Variable to List
		
		
		// List to Variable
		if ( (parentOp instanceof ListOperator) && (childOp instanceof VariableOperator) ) {
			
			ListOperator parentLO = ( ListOperator ) parentOp;
			VariableOperator childVO = ( VariableOperator ) childOp;
			
			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childVO;
			this.setList(true);
			this.init();
			
		}
		// End List to Variable
		
		
		// Constant to List
		if ( (parentOp instanceof ConstantOperator) && (childOp instanceof ListOperator) ) {
			
			ConstantOperator parentCO = ( ConstantOperator ) parentOp;
			ListOperator childLO = ( ListOperator ) childOp;
			
			this.setConstantOperator(parentCO);
			this.parentOp = parentCO;
			this.child = childLO;
			this.setConstant(true);
			this.init();
			
		}
		// End Constant to List
		
		
		// List to Constant
		if ( (parentOp instanceof ListOperator) && (childOp instanceof ConstantOperator) ) {
			
			ListOperator parentLO = ( ListOperator ) parentOp;
			ConstantOperator childCO = ( ConstantOperator ) childOp;
			
			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childCO;
			this.setList(true);
			this.init();
			
		}
		// End List to Constant
		
		
		// Uniterm to List
		if ( (parentOp instanceof UnitermOperator) && (childOp instanceof ListOperator) ) {
			
			UnitermOperator parentFO = ( UnitermOperator ) parentOp;
			ListOperator childLO = ( ListOperator ) childOp;
					
			this.setUnitermOperator(parentFO);
			this.parentOp = parentFO;
			this.child = childLO;
			this.setUniterm(true);
			this.init();
					
		}
		// End Uniterm to List
		
		
		// List to Uniterm
		if ( (parentOp instanceof ListOperator) && (childOp instanceof UnitermOperator) ) {
			
			ListOperator parentLO = ( ListOperator ) parentOp;
			UnitermOperator childFO = ( UnitermOperator ) childOp;
			
			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childFO;
			this.setList(true);
			this.init();
			
		}
		// End List to Uniterm
		
		
		// List to List
		if ( (parentOp instanceof ListOperator) && (childOp instanceof ListOperator) ) {
			
			ListOperator parentLO = ( ListOperator ) parentOp;
			ListOperator childLO = ( ListOperator ) childOp;
			
			this.setListOperator(parentLO);
			this.parentOp = parentLO;
			this.child = childLO;
			this.setList(true);
			this.init();
			
		}
		// End List to List
		
//		// AbstractTermOperator to AbstractTermOperator
//		if ( (parentOp instanceof AbstractTermOperator) && (childOp instanceof AbstractTermOperator) ) {
//			
//			AbstractTermOperator parentATO = ( AbstractTermOperator ) parentOp;
//			AbstractTermOperator childATO = ( AbstractTermOperator ) childOp;
//			
//			this.setAbstractTermOperator(parentATO);
//			this.parentOp = parentATO;
//			this.child = childATO;
//			this.setList(true);
//			this.init();
//			
//		}
//		// End List to List
		
		
		
	}

	
	private void init() {
		this.setLayout(new GridBagLayout());

		
		this.gbc = new GridBagConstraints();
		this.gbc.anchor = GridBagConstraints.NORTHWEST;
		this.gbc.gridwidth = this.gbc.gridheight = 1;
		this.gbc.weightx = this.gbc.weighty = 1.0;

		this.gbc.insets = new Insets((int) parent.PADDING,
				(int) parent.PADDING, (int) parent.PADDING,
				(int) parent.PADDING);
		
		this.gbc.gridx = this.gbc.gridy = 0;
		this.gbc.fill = GridBagConstraints.BOTH;
		
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		this.setBorder(raisedbevel);
		
		JRadioButton rb1 = new JRadioButton( "Equality ( = )" );
		JRadioButton rb2 = new JRadioButton( "Membership ( # )" );
		JRadioButton rb3 = new JRadioButton( "Subclass ( ## )" );
		rb1.setSelected(true);
		
		rb1.setFont(parent.getFONT());
		rb2.setFont(parent.getFONT());
		rb3.setFont(parent.getFONT());
		

		ItemListener rbListener = new ItemListener() {
		  @Override public void itemStateChanged( ItemEvent e ) {
			  
		    if(e.getStateChange() == ItemEvent.SELECTED){
		    	
		    	if(( (JRadioButton) e.getItem() ).getText().equals( "Subclass ( ## )" )) {
		    		setSelectedClassification( "##" ) ;
		    		if ( isVariable() ) variableOperator.setSelectedRadioButton( SUBCLASSSELECTED );
		    		if ( isConstant() ) constantOperator.setSelectedRadioButton( SUBCLASSSELECTED );
		    		if ( isUniterm() ) unitermOperator.setSelectedRadioButton( SUBCLASSSELECTED );
		    		if ( isList() ) listOperator.setSelectedRadioButton( SUBCLASSSELECTED );
		    		
		    	}
		    	
		    	if(( (JRadioButton) e.getItem() ).getText().equals( "Membership ( # )" )){
		    		setSelectedClassification( "#" ) ;
		    		if ( isVariable() ) variableOperator.setSelectedRadioButton( MEMBERSHIPSELECTED );
		    		if ( isConstant() ) constantOperator.setSelectedRadioButton( MEMBERSHIPSELECTED );
		    		if ( isUniterm() ) unitermOperator.setSelectedRadioButton( MEMBERSHIPSELECTED );
		    		if ( isList() ) listOperator.setSelectedRadioButton( MEMBERSHIPSELECTED );
		    	}
		    	
		    	if(( (JRadioButton) e.getItem() ).getText().equals( "Equality ( = )" )){
		    		setSelectedClassification( "=" ) ;
		    		if ( isVariable() ) variableOperator.setSelectedRadioButton( EQAULITYSELECTED );
		    		if ( isConstant() ) constantOperator.setSelectedRadioButton( EQAULITYSELECTED );
		    		if ( isUniterm() ) unitermOperator.setSelectedRadioButton( EQAULITYSELECTED );
		    		if ( isList() ) listOperator.setSelectedRadioButton( EQAULITYSELECTED );
		    	}
		    	
		    }
		  }
		};
		
		boolean[] tmp = {};
		
		


		
		if ( isVariable() ) tmp =  variableOperator.getSelectedRadioButton();
		if ( isConstant() ) tmp =  constantOperator.getSelectedRadioButton();
		if ( isUniterm() ) tmp = unitermOperator.getSelectedRadioButton();
		if ( isList() ) tmp = listOperator.getSelectedRadioButton();
		
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
			
//			else
//				
//				rb1.setSelected(true);
//
//			if ( isVariable() ) variableOperator.setSelectedRadioButton(this.EQAULITYSELECTED);
//			if ( isConstant() ) constantOperator.setSelectedRadioButton(this.EQAULITYSELECTED);
//			if ( isUniterm() ) factOperator.setSelectedRadioButton(this.EQAULITYSELECTED);
//			if ( isList() ) listOperator.setSelectedRadioButton(this.EQAULITYSELECTED);
			
		}
		
		

		
		
		rb1.addItemListener( rbListener );
		rb2.addItemListener( rbListener );
		rb3.addItemListener( rbListener );

		
		

		// Set radio buttons on the ButtonGroup

		ButtonGroup g = new ButtonGroup();
		g.add( rb1 );
		g.add( rb2 );
		g.add( rb3 );
		

		
		
		
		this.add(rb1,this.gbc);
		
		this.gbc.gridy++;

		this.add(rb2,this.gbc);
		
		this.gbc.gridy++;

		this.add(rb3,this.gbc);
		
		
	}


	@Override
	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		// TODO Auto-generated method stub
		return true;
	}


	public void setSelectedClassification(String selectedClassification) {
		if ( isVariable() ){
			VariableOperator vp = ( VariableOperator ) this.parentOp;
			vp.setSelectedClassification(selectedClassification);
		}
		
		if ( isConstant() ){
			ConstantOperator cp = ( ConstantOperator ) this.parentOp;
			cp.setSelectedClassification(selectedClassification);
		}
		
		if ( isUniterm() ){
			UnitermOperator fp = ( UnitermOperator ) this.parentOp;
			fp.setSelectedClassification(selectedClassification);
		}
		
		if ( isList() ){
			ListOperator lp = ( ListOperator ) this.parentOp;
			lp.setSelectedClassification(selectedClassification);
		}
		
	}

	
	

	/* *************** **
	 * Getter + Setter **
	 * *************** */

	public void setVariableOperator(VariableOperator variableOperator) {
		this.variableOperator = variableOperator;
	}

	public VariableOperator getVariableOperator() {
		return variableOperator;
	}
	
    public void setConstantOperator(ConstantOperator constantOperator) {
		this.constantOperator = constantOperator;
	}
   
	public ConstantOperator getConstantOperator() {
		return constantOperator;
	}

	public void setVariable(boolean variable) {
		this.variable = variable;
	}

	public boolean isVariable() {
		return variable;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public boolean isConstant() {
		return constant;
	}

	public ListOperator getListOperator() {
		return listOperator;
	}

	public void setListOperator(ListOperator listOperator) {
		this.listOperator = listOperator;
	}

	public boolean isList() {
		return list;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public UnitermOperator getUnitermOperator() {
		return unitermOperator;
	}

    public void setUnitermOperator(UnitermOperator unitermOperator) {
		this.unitermOperator = unitermOperator;
	}

	public boolean isUniterm() {
		return uniterm;
	}

	public void setUniterm(boolean uniterm) {
		this.uniterm = uniterm;
	}


	
//	public AbstractTermOperator getAbstractTermOperator() {
//		return abstractTermOperator;
//	}
//
//
//	public void setAbstractTermOperator(AbstractTermOperator abstractTermOperator) {
//		this.abstractTermOperator = abstractTermOperator;
//	}





}
