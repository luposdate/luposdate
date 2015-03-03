
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.JIconButton;
public class PrefixOperatorPanel extends AbstractGuiComponent<Operator>  {


	
	
private static final long serialVersionUID = -4952532158340724404L;
	

	protected GridBagConstraints gbc = null;
	private  JPanel prefixRowsPanel;
	protected PrefixOperator prefix;

	
	

	
	
		// Constructor
		/**
		 * <p>Constructor for PrefixOperatorPanel.</p>
		 *
		 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
		 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
		 * @param prefix a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator} object.
		 * @param name a {@link java.lang.String} object.
		 * @param startNode a boolean.
		 * @param alsoSubClasses a boolean.
		 */
		public PrefixOperatorPanel(final VisualGraph<Operator> parent,
				GraphWrapper gw, final PrefixOperator prefix,
				String name, boolean startNode, boolean alsoSubClasses) {
			
			super(parent, gw, prefix, true);
			
			
			
			/* ************************************************ **
			 * EBNF:                                            **
			 *                                                  **
			 * Prefix ::= 'Prefix' '(' NCName ANGLEBRACKIRI ')' ** 
			 * ************************************************ */
			
			this.prefix = prefix;

			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		

			this.prefixRowsPanel = new JPanel(new GridBagLayout());
			this.prefixRowsPanel.setOpaque(false);
			
			
			Border raisedbevel = BorderFactory.createRaisedBevelBorder();
			this.setBorder(raisedbevel);
			
			this.gbc = new GridBagConstraints();
			this.gbc.anchor = GridBagConstraints.NORTHWEST;
			this.gbc.gridwidth = this.gbc.gridheight = 1;
			this.gbc.weightx = this.gbc.weighty = 1.0;

			this.gbc.insets = new Insets((int) parent.PADDING,
					(int) parent.PADDING, (int) parent.PADDING,
					(int) parent.PADDING);
			this.gbc.gridx = this.gbc.gridy = 0;
			this.gbc.fill = GridBagConstraints.BOTH;


			if (this.prefix.hasElements()) {
				for (final String namespace : this.prefix.getPrefixList().keySet()) {
					this.createPrefixRow(
							this.prefix.getPrefixList().get(namespace), namespace);
							
				}
			}

			// Button
			Dimension buttonDimension = new Dimension();
			buttonDimension.setSize(30d, 24d);
			
			final JIconButton addButton = new JIconButton("icons/001_01.png");
			addButton.setPreferredSize(buttonDimension);
			addButton.setMaximumSize(buttonDimension);
			addButton.setMinimumSize(buttonDimension);
			addButton.setFont(parent.getFONT());
			
			addButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					
					
					if (prefix.getPrefixCount() >= prefix.getPrefixRowCnt()){
						
						
						prefix.setPrefixRowCnt(prefix.getPrefixRowCnt()+1);
						createPrefixRow("", "");
						updateSize();
					}
				
					
				}});
				
			
			

			this.add(this.prefixRowsPanel);

			this.add(addButton); // add add-button to row panel
//			if (!this.prefix.hasElements()){
			this.createPrefixRow("", "");
//			}
			
			this.addComponentListener(new ComponentAdapter() {
				
				public void componentResized(ComponentEvent e) {
					
					updateSize();
				}

			});
			
			this.updateSize();
		}

		
		/**
		 * <p>createPrefixRow.</p>
		 *
		 * @param prefixString a {@link java.lang.String} object.
		 * @param namespaceString a {@link java.lang.String} object.
		 */
		public void createPrefixRow(final String prefixString,
				final String namespaceString) {
	
			this.gbc.gridx = 0;
			this.gbc.gridy++;

			final JTextFieldResizing prefixTF = new JTextFieldResizing(
					prefixString, this.parent.getFONT(), this);


			final JTextFieldResizing namespaceTF = new JTextFieldResizing(
					namespaceString, this.parent.getFONT(), this);
			
			namespaceTF.setPreferredSize(new Dimension(prefixTF
					.getPreferredSize().width + 10, prefixTF
					.getPreferredSize().height));

			
			prefixTF.setPreferredSize(new Dimension(prefixTF
					.getPreferredSize().width + 150, prefixTF
					.getPreferredSize().height));
	
		
			if(!namespaceString.equals("")){
				namespaceTF.setFocusable(false);
				prefixTF.setFocusable(false);
			}
		
			
		prefixTF.addFocusListener(new FocusAdapter() {
			private String oldValue = prefixString;

			public void focusLost(final FocusEvent fe) {

				
			
				}});
			
			
		namespaceTF.addFocusListener(new FocusAdapter() {
				private String oldValue = namespaceString;
				
				
				public void focusGained(final FocusEvent fe){
			
				}
				

				public void focusLost(final FocusEvent fe) {
					
				}
			});

		namespaceTF.addKeyListener( new KeyListener()
		{
			  public void keyTyped( KeyEvent e ) {

			  }
			  public void keyPressed( KeyEvent e ) {

			  }
			  public void keyReleased( KeyEvent e ) {
			 
			   if(e.getKeyCode()==10){
				   

				 int i =   checkTheCase(prefixTF.getText(),namespaceTF.getText());
				   
				  
				  switch(i){
				  
				  case 0: break;
				  
				  case 1: addEntry(prefixTF.getText(),namespaceTF.getText());
				  		  prefixTF.setEditable(false);
				  		  namespaceTF.setEditable(false);
				  		  namespaceTF.setFocusable(false);
				  		  prefixTF.setFocusable(false);
				  		  prefix.setPrefixRowCnt(prefix.getPrefixRowCnt()+1);
				  		  break;
				  
				  case 2: prefixTF.selectAll();
						  prefixTF.grabFocus();
					      showPrefixAlreadyExistsDialog();
					      break;
					      
				  case 3: prefixTF.selectAll();
				  		  prefixTF.grabFocus();
				  		  showNamespaceIsAlreadyInUseDialog();
				  		  break;
				  		  
				  case 4: prefixTF.selectAll();
						  prefixTF.grabFocus();
					      showNoNamespaceDialog();
					      break;
					  
				  default: break;
				  
				  
				  }
				  
//				  System.out.println(prefix.getPrefixCount()+" "+prefix.getPrefixRowCnt());
				  
			   } 
			  
			  }
			});
			
			
			final JLabel prefixLabel = new JLabel("Prefix:");
			prefixLabel.setFont(parent.getFONT());
			
			final JLabel iriLabel = new JLabel("IRI:");
			iriLabel.setFont(parent.getFONT());
			
			
			// Button
			Dimension buttonDimension = new Dimension();
			buttonDimension.setSize(30d, 24d);
			
			final JIconButton deleteButton = new JIconButton("icons/001_02.png");
			deleteButton.setPreferredSize(buttonDimension);
			deleteButton.setMaximumSize(buttonDimension);
			deleteButton.setMinimumSize(buttonDimension);
			deleteButton.setFont(parent.getFONT());
			deleteButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if (!namespaceTF.getText().equals("")) {
						final int choice = showPrefixRemovedOptionDialog();

						if (choice == JOptionPane.YES_OPTION) { // remove prefix and
							// notify
							// operators...
							prefix.removeEntry(namespaceTF.getText(), true);

							removeRow(prefixLabel, prefixTF, iriLabel, namespaceTF, deleteButton);
						} else if (choice == JOptionPane.NO_OPTION) { // remove
							// prefix
							// but don't
							// notify
							// operators
							// ...
							prefix.removeEntry(namespaceTF.getText(), false);

							removeRow(prefixLabel, prefixTF, iriLabel, namespaceTF, deleteButton);
						}
					} else {
						removeRow(prefixLabel, prefixTF, iriLabel, namespaceTF, deleteButton);
					}
				}});
			
	
			
			this.prefixRowsPanel.add(prefixLabel,this.gbc);
			
			this.gbc.gridx++;
			
			this.prefixRowsPanel.add(prefixTF, this.gbc);
	//
			this.gbc.gridx++;
			
			this.prefixRowsPanel.add(iriLabel,this.gbc);
			
			this.gbc.gridx++;

			this.prefixRowsPanel.add(namespaceTF, this.gbc);

			this.gbc.gridx++;

			this.prefixRowsPanel.add(deleteButton, this.gbc);
			
			
		
		}

		
		private void removeRow(final JLabel prefixLabel, final JTextField prefixTF,
				final JLabel iriLabel,
				final JTextField namespaceTF, final JButton deleteButton) {
			prefix.setPrefixRowCnt(prefix.getPrefixRowCnt()-1);
			this.prefixRowsPanel.remove(prefixLabel);
			this.prefixRowsPanel.remove(prefixTF);
			this.prefixRowsPanel.remove(iriLabel);
			this.prefixRowsPanel.remove(namespaceTF);
			this.prefixRowsPanel.remove(deleteButton);

			this.updateSize();
		}

		
		/**
		 * <p>updateSize.</p>
		 */
		public void updateSize() {
			this.setMinimumSize(this.prefixRowsPanel.getSize());

			// --- update width of the JTextFieldResizing to the max size per
			// column
			// - begin ---
			if (this.prefixRowsPanel.getComponentCount() >= 3) {
				// -- get max width for each column - begin --
				int maxWidthLeftColumn = 10;
				int maxWidthRightColumn = 150;
				Container textField = null;
				Dimension d = null;

				// walk through rows...
				for (int i = 0; i < this.prefixRowsPanel.getComponentCount(); i += 1) {
					// left text field...
					i+=1; // skip Label
					textField = (Container) this.prefixRowsPanel.getComponent(i);

					final Dimension leftSize = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
							.calculateSize()
							: textField.getPreferredSize();

							maxWidthLeftColumn = Math.max(maxWidthLeftColumn,
									leftSize.width);

							// right text field...
							i += 1;
							i+=1; // skip Label
							textField = (Container) this.prefixRowsPanel.getComponent(i);

							final Dimension rightSize = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
									.calculateSize()
									: textField.getPreferredSize();

									maxWidthRightColumn = Math.max(maxWidthRightColumn,
											rightSize.width);

									i += 1; // skip delete-label
				}
				// -- get max width for each column - end --

				// -- update elements of each column - begin --
				// walk through rows...
				for (int i = 0; i < this.prefixRowsPanel.getComponentCount(); i += 1) {
					i+=1; // skip Label
					// left text field...
					textField = (Container) this.prefixRowsPanel.getComponent(i);
					d = new Dimension(maxWidthLeftColumn, textField
							.getPreferredSize().height);
					textField.setPreferredSize(d);
					textField.setSize(d);
					textField.setMaximumSize(d);
					textField.setMinimumSize(d);
					textField.repaint();

					// right text field...
					i += 1;
					i+=1; // skip Label
					textField = (Container) this.prefixRowsPanel.getComponent(i);
					d = new Dimension(maxWidthRightColumn, textField
							.getPreferredSize().height);
					textField.setPreferredSize(d);
					textField.setSize(d);
					textField.setMaximumSize(d);
					textField.setMinimumSize(d);
					textField.repaint();

					i += 1; // skip delete-label
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

		/**
		 * 
		 * @param prefix
		 * @param namespace
		 * @return 0: unknown case
		 * 		   1: everything is ok
		 * 		   2: prefix is already used
		 * 		   3: namespace is already used
		 * 		   4: namespace is empty
		 * 		   
		 */
		private int	checkTheCase(String prefix, String namespace){
			System.out.println("this.prefix.prefixIsInUse(prefix): "+this.prefix.prefixIsInUse(prefix));
			System.out.println("this.prefix.namespaceIsInUse(namespace): "+this.prefix.namespaceIsInUse(namespace));
			System.out.println("namespace.equals(): "+namespace.equals(""));
			
			
			// 1:
			if (	   !this.prefix.prefixIsInUse(prefix)
					&& !namespace.equals("")
					&& !this.prefix.namespaceIsInUse(namespace)){return 1;}
			
			// 2: 
			if ( this.prefix.prefixIsInUse(prefix)){return 2;}
			
			// 3:
			if ( this.prefix.namespaceIsInUse(namespace)){return 3;}
			
			// 4:
			if (namespace.equals("")){return 4;}
			
			return 0;
		}	
		
		
		private void addEntry(String prefix, String namespace){
			this.prefix.addEntry(prefix,namespace);
			createPrefixRow("", "");
			updateSize();
		} 
		
		
		private int showPrefixAddedOptionDialog() {
			return JOptionPane
			.showOptionDialog(
					this.parent.visualEditor,
					"A prefix has been added. Do you want to replace all occurences of the prefix with the defined prefix name?",
					"Prefix added", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new Object[] {
							"Yes", "No and ignore warning",
					"Don't add prefix" }, 0);
		}

		
		private int showPrefixNameChangedOptionDialog() {
			return JOptionPane
			.showOptionDialog(
					this.parent.visualEditor,
					"A prefix name has been chanced. Do you want to replace all occurences of the old prefix name with the new one?",
					"Prefix name changed",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new Object[] {
							"Yes", "No and ignore warning",
					"Don't change prefix name" }, 0);
		}

		
		private int showPrefixRemovedOptionDialog() {
			return JOptionPane
			.showOptionDialog(
					this.parent.visualEditor,
					"A prefix has been removed. Do you want to replace all occurences of the prefix name with it's prefix?",
					"Prefix removed", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, new Object[] {
							"Yes", "No and ignore warning",
					"Don't remove prefix" }, 0);
		}

		
		private void showPrefixAlreadyExistsDialog() {
			JOptionPane
				.showMessageDialog(this.parent.visualEditor,
				"This Prefix already exists. Please choose another Prefix!");
			
		}
		
		
		private void showNamespaceIsAlreadyInUseDialog(){
			JOptionPane
			.showMessageDialog(this.parent.visualEditor,
			"This Namespace already exists. Please choose another Namespace!");
		
		} 
		
		
		private void showNoNamespaceDialog(){
			JOptionPane
			.showMessageDialog(this.parent.visualEditor,
			"The namespace is missing. Please specify a namespace!");
		}

		
		/** {@inheritDoc} */
		public boolean validateOperatorPanel(final boolean showErrors, Object data) {
			return true;
		}

		
		/**
		 * <p>Getter for the field <code>gbc</code>.</p>
		 *
		 * @return a {@link java.awt.GridBagConstraints} object.
		 */
		public GridBagConstraints getGbc() {
			return gbc;
		}


		/**
		 * <p>Setter for the field <code>gbc</code>.</p>
		 *
		 * @param gbc a {@link java.awt.GridBagConstraints} object.
		 */
		public void setGbc(GridBagConstraints gbc) {
			this.gbc = gbc;
		}


		/**
		 * <p>Getter for the field <code>prefixRowsPanel</code>.</p>
		 *
		 * @return a {@link javax.swing.JPanel} object.
		 */
		public JPanel getPrefixRowsPanel() {
			return prefixRowsPanel;
		}


		/**
		 * <p>Setter for the field <code>prefixRowsPanel</code>.</p>
		 *
		 * @param prefixRowsPanel a {@link javax.swing.JPanel} object.
		 */
		public void setPrefixRowsPanel(JPanel prefixRowsPanel) {
			this.prefixRowsPanel = prefixRowsPanel;
		}


		/**
		 * <p>Getter for the field <code>prefix</code>.</p>
		 *
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator} object.
		 */
		public PrefixOperator getPrefix() {
			return prefix;
		}


		/**
		 * <p>Setter for the field <code>prefix</code>.</p>
		 *
		 * @param prefix a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator} object.
		 */
		public void setPrefix(PrefixOperator prefix) {
			this.prefix = prefix;
		}

	
		/**
		 * <p>getSerialversionuid.</p>
		 *
		 * @return a long.
		 */
		public static long getSerialversionuid() {
			return serialVersionUID;
		}

		
		

}
