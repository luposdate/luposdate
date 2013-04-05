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
package lupos.event.consumer.querybuilder;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 * A JDialog which hosts a {@link EventQueryBuilderView}.
 *
 */
public class EventQueryBuilderDialog extends JDialog implements ActionListener {
	
	private JButton cancelButton, okButton;
	private EventQueryBuilderView builderView;
	private String query = null;
	
	
	public EventQueryBuilderDialog(Window owner) {
		super(owner, "Event query builder", ModalityType.APPLICATION_MODAL);
		super.setMinimumSize(new Dimension(450,500));
		super.setLocationRelativeTo(owner);
		
		super.setLayout(new GridBagLayout());			
		
		
		this.builderView = new EventQueryBuilderView();
		JPanel tmp = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tmp.add(this.builderView);
		JScrollPane scrollPane = new JScrollPane(tmp);
		
		this.cancelButton = new JButton("Cancel");
		this.cancelButton.addActionListener(this);
		this.okButton = new JButton("Ok");
		this.okButton.addActionListener(this);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(this.cancelButton);
		buttonsPanel.add(Box.createHorizontalStrut(3));
		buttonsPanel.add(this.okButton);
		
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3,3,3,3);

		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		super.add(scrollPane, c);
		
		c.gridy = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		super.add(buttonsPanel, c);
	}
	
	public String getQuery() {
		return this.query;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.okButton)
			this.query = this.builderView.getQuery();
		
		super.setVisible(false);
	}
}
