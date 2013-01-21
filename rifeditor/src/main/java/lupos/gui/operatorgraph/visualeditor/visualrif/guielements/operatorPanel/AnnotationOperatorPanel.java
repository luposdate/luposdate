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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.AnnotationOperator;


public class AnnotationOperatorPanel extends AbstractGuiComponent<Operator> {


	private static final long serialVersionUID = -6993813701569431678L;
	
	protected GridBagConstraints gbc = null;
	private JTextField textField;
	private JLabel label;
	private JButton addButton;
	private boolean minimizedByLostFocus;
	
	protected AnnotationOperator annotationOperator;
	
	
	// Constructor
	public AnnotationOperatorPanel(VisualGraph<Operator> parent,
			GraphWrapper gw, final AnnotationOperator operator, String text, String slabel, boolean movable) {
		
		super(parent, gw, operator, movable);
		
		this.annotationOperator = operator;
		
		
		
		/* ********************************************************************** **
		 * EBNF:                                            					  **
		 *                                              					      **
		 * IRIMETA        ::= '(*' IRICONST? (Frame | 'And' '(' Frame* ')')? '*)' ** 
		 * *********************************************************************** */

		init(operator, parent.PADDING, parent.getFONT(), text, slabel);
		updateSize();
		
		
		

		
		}

	
	
	private void init(final Operator operator, double PADDING, Font font, String text, String label) {
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

		this.textField = new JTextFieldResizing(text, font, this);
		
		this.textField.addFocusListener(new FocusAdapter() {
		

			public void focusLost(final FocusEvent fe) {

			
			    minimizedByLostFocus = true;
				minimizeTextField();
				updateSize();
				
				
			
				}});
			
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		this.label = new JLabel("Annotation");
		this.label.setBorder(loweredetched);
		
		this.label.setFont(parent.getFONT());
	
		
		
	
		
		addButton = new JButton("+");
		addButton.setFont(parent.getFONT());
		addButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				
				if(annotationOperator.isMinimized()&&!minimizedByLostFocus){
				maximizeTextField();
				textField.grabFocus();
				}
					
				minimizedByLostFocus=false;
				
			
				updateSize();
			}});
	
		

		this.add(this.label,this.gbc);
		this.gbc.gridy++;
		this.add(this.addButton,this.gbc);
		


	}
	

	private void maximizeTextField(){
		annotationOperator.setMinimized(false);
		minimizedByLostFocus = false;
		this.addButton.setText("-");
		
		this.textField.setText(this.annotationOperator.getAnnotation());
		
		this.gbc.gridx = 0;
		this.gbc.gridy++;
		
		this.textField.setPreferredSize(new Dimension(textField
				.getPreferredSize().width + 250, textField
				.getPreferredSize().height));
		
		this.add(textField,this.gbc);
		this.gbc.gridx++;
		

		
	
		
		
		
	}


	private void minimizeTextField(){
		annotationOperator.setMinimized(true);
		this.addButton.setText("+");

		this.annotationOperator.setAnnotation(this.textField.getText());

		this.remove(this.textField);
	

	
	}
	

	@Override
	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void updateSize(){
		if(this.getComponentCount() >= 2){
			int maxWidthLeftColumn = 50;
	
			Container textField = null;
			Dimension d = null;
			textField = (Container) this.getComponent(1);
			final Dimension size = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
					.calculateSize()
					: textField.getPreferredSize();
					
					maxWidthLeftColumn = Math.max(maxWidthLeftColumn,
							size.width);
					
					textField = (Container) this.getComponent(1);
					d = new Dimension(maxWidthLeftColumn, textField
							.getPreferredSize().height);
					textField.setPreferredSize(d);
					textField.setSize(d);
					textField.setMaximumSize(d);
					textField.setMinimumSize(d);
					textField.repaint();		
					
					if (this.getBox() != null) {
						this.getBox().height = this.getPreferredSize().height;
					}

					this.setSize(this.getPreferredSize());
					this.revalidate(); 	
		}
		
		if (this.getComponentCount() < 2){

//			this.setMinimumSize(this.labelButton.getSize());
			if (this.getBox() != null) {
				this.getBox().height = this.getPreferredSize().height;
			}

			this.setSize(this.getPreferredSize());
			this.revalidate(); // re-validate the PrefixPanel
		
			
		}
		
		
	}
	
	
	public JTextField getTextField() {
		return textField;
	}


	public void setTextField(JTextField textField) {
		this.textField = textField;
	}


	public JLabel getLabel() {
		return label;
	}


	public void setLabel(JLabel label) {
		this.label = label;
	}


	public JButton getAddButton() {
		return addButton;
	}


	public void setAddButton(JButton addButton) {
		this.addButton = addButton;
	}


	public boolean isMinimizedByLostFocus() {
		return minimizedByLostFocus;
	}


	public void setMinimizedByLostFocus(boolean minimizedByLostFocus) {
		this.minimizedByLostFocus = minimizedByLostFocus;
	}








}
