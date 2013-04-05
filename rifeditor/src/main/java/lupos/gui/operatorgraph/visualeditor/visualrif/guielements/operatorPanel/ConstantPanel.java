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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.VisualRifEditor;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ConstantOperator;



public class ConstantPanel extends AbstractGuiComponent<Operator>{


	private static final long serialVersionUID = -6789368326247215391L;
	
	protected GridBagConstraints gbc = null;
	private ConstantOperator constantOperator;

	private VisualRifEditor visualRifEditor;





	public ConstantPanel(VisualGraph<Operator> parent, GraphWrapper gw,
			ConstantOperator operator, boolean movable, VisualRifEditor visualRifEditor2) {
		super(parent, gw, operator, movable);

		this.visualRifEditor = visualRifEditor2;
		
		
		this.parent = parent;
		
		this.setConstantOperator(operator);
		
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
		TitledBorder titled;
		Font font = new Font(Font.SANS_SERIF, Font.ROMAN_BASELINE, 11);
		titled = BorderFactory.createTitledBorder(raisedbevel," Constant ", 0, 0, font, Color.BLACK);
		this.setBorder(titled);
		

		final JTextFieldResizing textField = new JTextFieldResizing(
				"", this.parent.getFONT(), this);
		
		textField.setPreferredSize(new Dimension(50 , 20));
		textField.setMinimumSize(new Dimension(50 ,20));

		textField.addFocusListener((new FocusAdapter() {

			public void focusGained(final FocusEvent fe){
				updateSize();
			}
			

			public void focusLost(final FocusEvent fe) {
				constantOperator.setConstant(textField.getText());
				updateSize();
			}
		}));

		textField.addKeyListener( new KeyListener()
		{
			  public void keyTyped( KeyEvent e ) {

			  }
			  public void keyPressed( KeyEvent e ) {

			  }
			  public void keyReleased( KeyEvent e ) {
			 
//			   if(e.getKeyCode()==10){
				   
				   updateSize();
//			   } 
			  
			  }
			});
		
		textField.setText(this.constantOperator.getConstant());
		
		
		// FocusListener
		FocusListener FL = new FocusListener() {

			public void focusGained(FocusEvent arg0) {
				

				
				if (visualRifEditor
						.getDocumentContainer()
						.getActiveDocument()
						.getDocumentEditorPane().getPrefixList().length > 0) {
					
					constantOperator.setComboBoxEntries(visualRifEditor
							.getDocumentContainer().getActiveDocument()
							.getDocumentEditorPane().getPrefixList());
					
					for (String s : constantOperator.getComboBoxEntries()){
						constantOperator.getConstantComboBox().addItem(s);	
					}
					updateSize();
					

				}
			}

			public void focusLost(FocusEvent arg0) {
			}
		};
	
		this.constantOperator.setComboBoxFocusListener(FL);
		
		
		this.constantOperator.getConstantComboBox().setFont(parent.getFONT());
		this.constantOperator.getConstantComboBox().setEditable(false);
		this.constantOperator.getConstantComboBox().setMaximumRowCount(3);
		this.constantOperator.getConstantComboBox().setSelectedItem(
		this.constantOperator.getConstantComboBox().getSelectedItem());
		
		this.constantOperator.getConstantComboBox().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JComboBox selectedChoice = (JComboBox) e.getSource();
				constantOperator.setSelectedPrefix((String) selectedChoice.getSelectedItem());
			}

		});
		
		if (this.constantOperator.getConstantComboBox().getFocusListeners().length <= 1) 
			this.constantOperator.getConstantComboBox().addFocusListener(FL);
		
		this.add(this.constantOperator.getConstantComboBox(),this.gbc);
		
		this.gbc.gridx++;
				
		this.add(textField,this.gbc); 
		

		
		this.addComponentListener(new ComponentAdapter() {
			

			public void componentResized(ComponentEvent e) {
				
				updateSize();
			}



		});
		
		
		this.updateSize();
	}
	


	public void updateSize(){
	

		if (this.getComponents().length > 1){
		
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
		
	}
	

	
	

	@Override
	public boolean validateOperatorPanel(boolean showErrors, Object data) {
		// TODO Auto-generated method stub
		return true;
	}

	public ConstantOperator getVariableOperator() {
		return constantOperator;
	}

	public void setConstantOperator(ConstantOperator variableOperator) {
		this.constantOperator = variableOperator;
	}


	public VisualRifEditor getVisualRifEditor() {
		return visualRifEditor;
	}

	public void setVisualRifEditor(VisualRifEditor visualRifEditor) {
		this.visualRifEditor = visualRifEditor;
	}
	

	
}
