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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.visualeditor.guielements.AbstractGuiComponent;
import lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph;
import lupos.gui.operatorgraph.visualeditor.operators.Operator;
import lupos.gui.operatorgraph.visualeditor.util.JTextFieldResizing;
import lupos.gui.operatorgraph.visualeditor.visualrif.guielements.graphs.VisualRIFGraph;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.operators.PrefixOperator;
import lupos.gui.operatorgraph.visualeditor.visualrif.util.JIconButton;
public class ImportOperatorPanel extends AbstractGuiComponent<Operator>  {


	
	
private static final long serialVersionUID = -4952532158340724404L;
	

	protected GridBagConstraints gbc = null;
	


	private  JPanel importRowsPanel;
	protected ImportOperator importOperator;

	
	

	
	
		// Constructor
		/**
		 * <p>Constructor for ImportOperatorPanel.</p>
		 *
		 * @param parent a {@link lupos.gui.operatorgraph.visualeditor.guielements.VisualGraph} object.
		 * @param gw a {@link lupos.gui.operatorgraph.graphwrapper.GraphWrapper} object.
		 * @param importOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator} object.
		 * @param name a {@link java.lang.String} object.
		 * @param startNode a boolean.
		 * @param alsoSubClasses a boolean.
		 */
		public ImportOperatorPanel(final VisualGraph<Operator> parent,
				GraphWrapper gw, final ImportOperator importOperator,
				String name, boolean startNode, boolean alsoSubClasses) {
			
			super(parent, gw, importOperator, true);
			
			
			
			/* ************************************************ **
			 * EBNF:                                            **
			 *                                                  **
			 * Prefix ::= 'Prefix' '(' NCName ANGLEBRACKIRI ')' ** 
			 * ************************************************ */
			
			this.importOperator = importOperator;

			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		

			this.importRowsPanel = new JPanel(new GridBagLayout());
			this.importRowsPanel.setOpaque(false);
			
			this.gbc = new GridBagConstraints();
			this.gbc.anchor = GridBagConstraints.NORTHWEST;
			this.gbc.gridwidth = this.gbc.gridheight = 1;
			this.gbc.weightx = this.gbc.weighty = 1.0;

			this.gbc.insets = new Insets((int) parent.PADDING,
					(int) parent.PADDING, (int) parent.PADDING,
					(int) parent.PADDING);
			this.gbc.gridx = this.gbc.gridy = 0;
			this.gbc.fill = GridBagConstraints.BOTH;


			if (this.importOperator.hasElements()) {
				for (final String namespace : this.importOperator.getPrefixList().keySet()) {
					this.createPrefixRow(
							this.importOperator.getPrefixList().get(namespace), namespace);
							
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
					
					
					if (importOperator.getImportCount() >= importOperator.getImportRowCnt()){
						
						
						importOperator.setImportRowCnt(importOperator.getImportRowCnt()+1);
						createPrefixRow("", "");
						updateSize();
					}
				
					
				}});
				
			
			

			this.add(this.importRowsPanel);

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
		 * @param locatorString a {@link java.lang.String} object.
		 * @param profileString a {@link java.lang.String} object.
		 */
		public void createPrefixRow(final String locatorString,
				final String profileString) {
	
			this.gbc.gridx = 0;
			this.gbc.gridy++;

			final JTextFieldResizing locatorTF = new JTextFieldResizing(
					locatorString, this.parent.getFONT(), this);


			final JTextFieldResizing profileTF = new JTextFieldResizing(
					profileString, this.parent.getFONT(), this);
			
			profileTF.setPreferredSize(new Dimension(locatorTF
					.getPreferredSize().width + 150, locatorTF
					.getPreferredSize().height));

			
			locatorTF.setPreferredSize(new Dimension(locatorTF
					.getPreferredSize().width + 150, locatorTF
					.getPreferredSize().height));
	
		
			if(!profileString.equals("")){
				profileTF.setFocusable(false);
				locatorTF.setFocusable(false);
			}
		
			
		locatorTF.addFocusListener(new FocusAdapter() {
			private String oldValue = locatorString;

			public void focusLost(final FocusEvent fe) {

				
			
				}});
			
			
		profileTF.addFocusListener(new FocusAdapter() {
				private String oldValue = profileString;
				
				
				public void focusGained(final FocusEvent fe){
			
				}
				

				public void focusLost(final FocusEvent fe) {
					
				}
			});

		profileTF.addKeyListener( new KeyListener()
		{
			  public void keyTyped( KeyEvent e ) {

			  }
			  public void keyPressed( KeyEvent e ) {

			  }
			  public void keyReleased( KeyEvent e ) {
			 
			   if(e.getKeyCode()==10){
				   

				 int i =   checkTheCase(locatorTF.getText(),profileTF.getText());
				   
				  
				  switch(i){
				  
				  case 0: break;
				  
				  case 1: addEntry(locatorTF.getText(),profileTF.getText());
				  		  locatorTF.setEditable(false);
				  		  profileTF.setEditable(false);
				  		  profileTF.setFocusable(false);
				  		  locatorTF.setFocusable(false);
				  		  importOperator.setImportRowCnt(importOperator.getImportRowCnt()+1);
				  		  break;
				  
				  case 2: locatorTF.selectAll();
						  locatorTF.grabFocus();
					      showPrefixAlreadyExistsDialog();
					      break;
					      
				  case 3: locatorTF.selectAll();
				  		  locatorTF.grabFocus();
				  		  showNamespaceIsAlreadyInUseDialog();
				  		  break;
				  		  
				  case 4: locatorTF.selectAll();
						  locatorTF.grabFocus();
					      showNoNamespaceDialog();
					      break;
					  
				  default: break;
				  
				  
				  }
				  
//				  System.out.println(prefix.getPrefixCount()+" "+prefix.getPrefixRowCnt());
				  
			   } 
			  
			  }
			});
			
			
			final JLabel prefixLabel = new JLabel("Locator:");
			prefixLabel.setFont(parent.getFONT());
			
			final JLabel iriLabel = new JLabel("Profile:");
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
					if (!profileTF.getText().equals("")) {
						final int choice = showPrefixRemovedOptionDialog();

						if (choice == JOptionPane.YES_OPTION) { // remove prefix and
							// notify
							// operators...
							importOperator.removeEntry(profileTF.getText(), true);

							removeRow(prefixLabel, locatorTF, iriLabel, profileTF, deleteButton);
						} else if (choice == JOptionPane.NO_OPTION) { // remove
							// prefix
							// but don't
							// notify
							// operators
							// ...
							importOperator.removeEntry(profileTF.getText(), false);

							removeRow(prefixLabel, locatorTF, iriLabel, profileTF, deleteButton);
						}
					} else {
						removeRow(prefixLabel, locatorTF, iriLabel, profileTF, deleteButton);
					}
				}});
			
	
			
			this.importRowsPanel.add(prefixLabel,this.gbc);
			
			this.gbc.gridx++;
			
			this.importRowsPanel.add(locatorTF, this.gbc);
	//
			this.gbc.gridx++;
			
			this.importRowsPanel.add(iriLabel,this.gbc);
			
			this.gbc.gridx++;

			this.importRowsPanel.add(profileTF, this.gbc);

			this.gbc.gridx++;

			this.importRowsPanel.add(deleteButton, this.gbc);
			
			
		
		}

		
		private void removeRow(final JLabel prefixLabel, final JTextField prefixTF,
				final JLabel iriLabel,
				final JTextField namespaceTF, final JButton deleteButton) {
			importOperator.setImportRowCnt(importOperator.getImportRowCnt()-1);
			this.importRowsPanel.remove(prefixLabel);
			this.importRowsPanel.remove(prefixTF);
			this.importRowsPanel.remove(iriLabel);
			this.importRowsPanel.remove(namespaceTF);
			this.importRowsPanel.remove(deleteButton);

			this.updateSize();
		}

		
		/**
		 * <p>updateSize.</p>
		 */
		public void updateSize() {
			this.setMinimumSize(this.importRowsPanel.getSize());

			// --- update width of the JTextFieldResizing to the max size per
			// column
			// - begin ---
			if (this.importRowsPanel.getComponentCount() >= 3) {
				// -- get max width for each column - begin --
				int maxWidthLeftColumn = 10;
				int maxWidthRightColumn = 150;
				Container textField = null;
				Dimension d = null;

				// walk through rows...
				for (int i = 0; i < this.importRowsPanel.getComponentCount(); i += 1) {
					// left text field...
					i+=1; // skip Label
					textField = (Container) this.importRowsPanel.getComponent(i);

					final Dimension leftSize = (textField instanceof JTextFieldResizing) ? ((JTextFieldResizing) textField)
							.calculateSize()
							: textField.getPreferredSize();

							maxWidthLeftColumn = Math.max(maxWidthLeftColumn,
									leftSize.width);

							// right text field...
							i += 1;
							i+=1; // skip Label
							textField = (Container) this.importRowsPanel.getComponent(i);

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
				for (int i = 0; i < this.importRowsPanel.getComponentCount(); i += 1) {
					i+=1; // skip Label
					// left text field...
					textField = (Container) this.importRowsPanel.getComponent(i);
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
					textField = (Container) this.importRowsPanel.getComponent(i);
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
			System.out.println("this.prefix.prefixIsInUse(prefix): "+this.importOperator.prefixIsInUse(prefix));
			System.out.println("this.prefix.namespaceIsInUse(namespace): "+this.importOperator.namespaceIsInUse(namespace));
			System.out.println("namespace.equals(): "+namespace.equals(""));
			
			
			// 1:
			if (	   !this.importOperator.prefixIsInUse(prefix)
					&& !namespace.equals("")
					&& !this.importOperator.namespaceIsInUse(namespace)){return 1;}
			
			// 2: 
			if ( this.importOperator.prefixIsInUse(prefix)){return 2;}
			
			// 3:
			if ( this.importOperator.namespaceIsInUse(namespace)){return 3;}
			
			// 4:
			if (namespace.equals("")){return 4;}
			
			return 0;
		}	
		
		
		private void addEntry(String prefix, String namespace){
			this.importOperator.addEntry(prefix,namespace);
			createPrefixRow("", "");
			updateSize();
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

		
		
		// Getter + Setter
		
		
		
		
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
		 * <p>Getter for the field <code>importRowsPanel</code>.</p>
		 *
		 * @return a {@link javax.swing.JPanel} object.
		 */
		public JPanel getImportRowsPanel() {
			return importRowsPanel;
		}


		/**
		 * <p>Setter for the field <code>importRowsPanel</code>.</p>
		 *
		 * @param importRowsPanel a {@link javax.swing.JPanel} object.
		 */
		public void setImportRowsPanel(JPanel importRowsPanel) {
			this.importRowsPanel = importRowsPanel;
		}


		/**
		 * <p>Getter for the field <code>importOperator</code>.</p>
		 *
		 * @return a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator} object.
		 */
		public ImportOperator getImportOperator() {
			return importOperator;
		}


		/**
		 * <p>Setter for the field <code>importOperator</code>.</p>
		 *
		 * @param importOperator a {@link lupos.gui.operatorgraph.visualeditor.visualrif.operators.ImportOperator} object.
		 */
		public void setImportOperator(ImportOperator importOperator) {
			this.importOperator = importOperator;
		}


}
