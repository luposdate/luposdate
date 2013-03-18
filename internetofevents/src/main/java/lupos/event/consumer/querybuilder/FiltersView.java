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
package lupos.event.consumer.querybuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * A View which allows to add and remove filters
 *
 */
public class FiltersView extends JPanel implements ActionListener {
	
	private JButton addButton;
	private List <Row> rows = new ArrayList<Row>();
	
	
	public FiltersView() {
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.addButton = new JButton("+");
		this.addButton.setMargin(new Insets(0,2,0,2));
		//this.addButton.setHorizontalAlignment(SwingConstants.LEFT);
		this.addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.addButton.setHorizontalAlignment(SwingConstants.RIGHT);
		this.addButton.addActionListener(this);
		super.add(this.addButton);		
	}
	
	private void removeRow(Row row) {
		this.rows.remove(row);
		super.remove(row);
		super.revalidate();
	}
	
	/**
	 * Returns a list of filters that has been added.
	 */
	public List<Filter> getFilters() {
		List<Filter> filters = new ArrayList<Filter>();
		for(Row r : this.rows) {
			Filter f = new Filter();
			f.op = r.getOp();
			f.param = r.getParam();
			filters.add(f);
		}
		return filters;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.addButton) {
			// when the add-button is clicked
			super.remove(this.addButton);
			
			final Row r = new Row();
			r.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.rows.add(r);
			r.addRemoveListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e1) {
					removeRow(r);
				}
			});
						
			super.add(r);
			super.add(this.addButton);
			
			super.revalidate();
		}
	}
	
	
	/**
	 * This is the view for a single filter, which lets the user choose a operation and enter a value
	 */
	class Row extends JPanel {
		private final String[] OPS = new String[] { "=","!=","<","<=",">",">=" };
		private JComboBox opsBox;
		private JTextField paramField;
		private JButton removeButton;
		
		public Row() {
			super.setLayout(new BorderLayout(2,2));
			super.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(2,2,2,2)));
			
			this.opsBox = new JComboBox(this.OPS);
			this.paramField = new JTextField(6);
			this.paramField.setMinimumSize(new Dimension(50,20));
			this.paramField.setMinimumSize(new Dimension(50,20));
			this.removeButton = new JButton("x");
			this.removeButton.setMargin(new Insets(0,2,0,2));
			
			super.add(this.opsBox, BorderLayout.WEST);
			super.add(this.paramField, BorderLayout.CENTER);
			super.add(this.removeButton, BorderLayout.EAST);
		}
		
		public void addRemoveListener(ActionListener al) {
			this.removeButton.addActionListener(al);
		}
		
		public String getOp() {
			return (String)this.opsBox.getSelectedItem();
		}
		
		public String getParam() {
			return this.paramField.getText();
		}
	}
}
