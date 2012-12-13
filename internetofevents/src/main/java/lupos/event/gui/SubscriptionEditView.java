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
package lupos.event.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lupos.event.Action;
import lupos.event.gui.querybuilder.EventQueryBuilderDialog;




/**
 * A view for editing a subscription (name, query, action)
 *
 */
@SuppressWarnings("serial")
public class SubscriptionEditView extends JPanel implements ActionListener {
	
	private JTextField subscriptionNameField;
	private JButton queryBuilderButton;
	private JTextArea queryTextArea;
	private JButton submitButton;
	private ActionsEditView actionsEditView;
	
	
	public SubscriptionEditView() {		
		super(new GridBagLayout());
	
		// Name Label 
	    GridBagConstraints c = createGBC(0,0);
	    super.add(new JLabel("Name:"), c);
	    
	    // Name TextField
	    this.subscriptionNameField = new JTextField();
	    c = createGBC(1,0);
	    c.weightx = 1.0;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    super.add(this.subscriptionNameField, c);
	
	    // Query Label
	    c = createGBC(0,1);
	    c.anchor = GridBagConstraints.EAST;
	    super.add(new JLabel("Query:"), c);
	    
	    
	    // button for opening the query builder dialog
	    this.queryBuilderButton = new JButton("Open query builder...");
	    this.queryBuilderButton.addActionListener(this);
	    c = createGBC(1,1);
	    c.anchor = GridBagConstraints.WEST;
	    super.add(this.queryBuilderButton, c);
	    
	    // Query TextArea
	    this.queryTextArea = new JTextArea();
	    //queryTextArea.setColumns(20);
	    this.queryTextArea.setTabSize(2);
	    this.queryTextArea.setLineWrap(false);
	    this.queryTextArea.setRows(7);
	    this.queryTextArea.setWrapStyleWord(true);
	    this.queryTextArea.setEditable(true);
	    JScrollPane queryTextAreaScrollPane = new JScrollPane(this.queryTextArea);

	    c = createGBC(1,2);
	    c.fill = GridBagConstraints.BOTH;
	    c.weighty = 1.0;
	    super.add(queryTextAreaScrollPane, c);
	    
	    
	    // action label
	    c = createGBC(0,3);
	    c.anchor = GridBagConstraints.EAST;
	    super.add(new JLabel("Action:"), c);
	    
	    c = createGBC(1,3);
	    this.actionsEditView = new ActionsEditView();
	    c.anchor = GridBagConstraints.WEST;
	    super.add(this.actionsEditView, c);
	    	    
	    
	    this.submitButton = new JButton("Submit");
	    c = createGBC(1,4);
	    c.anchor = GridBagConstraints.EAST;
	    super.add(this.submitButton, c);
	    
	    // align everything at top by creating a filler which takes up all the remaining vertical space
	    // Component filler = Box.createVerticalGlue();
	    c = createGBC(0,3);
	    c.weighty = 1.0;
	    // super.add(filler, c);	   
	}

	private static GridBagConstraints createGBC(int gridx, int gridy) {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,2,2);
		c.gridx = gridx;
		c.gridy = gridy;
		return c;
	}

	public void addSubmitActionListener(ActionListener al) {
		this.submitButton.addActionListener(al);
	}

	/**
	 * Sets the text of the views name textfield.
	 */
	public void setSubscriptionName(String name) {
		this.subscriptionNameField.setText(name);
	}

	/**
	 * Sets the text of the views query textfield.
	 */
	public void setSubscriptionQuery(String query) {
		this.queryTextArea.setText(query);
	}
	
	public String getSubscriptionName() {
		return this.subscriptionNameField.getText();
	}

	public String getSubscriptionQuery() {
		return this.queryTextArea.getText();
	}
	
	public Action getAction() {
		return this.actionsEditView.getAction();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.queryBuilderButton) {
			Window owner = SwingUtilities.windowForComponent(this);
			EventQueryBuilderDialog dialog = new EventQueryBuilderDialog(owner);
			dialog.setVisible(true);
			if(dialog.getQuery() != null)
				this.queryTextArea.setText(dialog.getQuery());
		}
		
	}
}
