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
package lupos.event.consumer.querybuilder;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * A view for configuration of a window for a query. 
 */
public class EventWindowView extends JPanel implements ActionListener {
	
	private JComboBox eventTypesBox;
	private JComboBox winTypeBox;
	private JPanel windowSpinnerPanel;	
	private JSpinner windowDurationSpinner, windowCountSpinner;
	private JLabel propLabel;
	private JPanel propPanel;
	private JButton removeButton;
	

	public EventWindowView(EventType[] eventTypes) {
		
		super.setBorder(new EmptyBorder(2,2,2,2));
		super.setLayout(new GridBagLayout());
		JPanel mainPanel = new JPanel(new GridBagLayout());		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(8, 4, 8, 4);
		
        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        mainPanel.add(new JLabel("Event type:"), c);
        
        this.eventTypesBox = new JComboBox(eventTypes);
        this.eventTypesBox.setSelectedIndex(-1);
        this.eventTypesBox.addActionListener(this);
        c.gridy = 0;
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        mainPanel.add(this.eventTypesBox, c);
        
        c.gridy = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        mainPanel.add(new JLabel("Window:"), c);
        
		SpinnerModel windowDurationModel = new SpinnerNumberModel(1000,
				100, // min
				999999, // max
				500); // step
		this.windowDurationSpinner = new JSpinner(windowDurationModel);
		this.windowDurationSpinner.setMaximumSize(new Dimension(80,20));
		
		SpinnerModel windowCountModel = new SpinnerNumberModel(5,
				1, // min
				10000, // max
				1); // step
		this.windowCountSpinner = new JSpinner(windowCountModel);
		this.windowCountSpinner.setMaximumSize(new Dimension(80,20));
		
		JPanel windowDurationPanel = new JPanel();
		windowDurationPanel.setLayout(new BoxLayout(windowDurationPanel, BoxLayout.LINE_AXIS));
		windowDurationPanel.add(this.windowDurationSpinner);
		windowDurationPanel.add(Box.createRigidArea(new Dimension(3,0)));
		windowDurationPanel.add(new JLabel("ms"));
		
		JPanel windowCountPanel = new JPanel();
		windowCountPanel.setLayout(new BoxLayout(windowCountPanel, BoxLayout.LINE_AXIS));
		windowCountPanel.add(this.windowCountSpinner);
		windowCountPanel.add(Box.createRigidArea(new Dimension(3,0)));
		windowCountPanel.add(new JLabel("Instances"));
        
		this.windowSpinnerPanel = new JPanel(new CardLayout());
		this.windowSpinnerPanel.add(windowDurationPanel, WindowOperator.DURATION.toString());
		this.windowSpinnerPanel.add(windowCountPanel, WindowOperator.COUNT.toString());
		
				
        this.winTypeBox = new JComboBox(WindowOperator.values());
        this.winTypeBox.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout)(EventWindowView.this.windowSpinnerPanel.getLayout());
			    cl.show(EventWindowView.this.windowSpinnerPanel, EventWindowView.this.winTypeBox.getSelectedItem().toString());
			}
		});
        JPanel windowPanel = new JPanel();
        windowPanel.setLayout(new BoxLayout(windowPanel, BoxLayout.X_AXIS));
        windowPanel.add(this.winTypeBox);
        windowPanel.add(Box.createHorizontalStrut(3));
        windowPanel.add(this.windowSpinnerPanel);
        
        c.gridy = 1;
        c.gridx = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        mainPanel.add(windowPanel, c);
   
        this.propLabel = new JLabel("Properties:");
        this.propLabel.setVisible(false);
        c.gridy = 4;
        c.gridx = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        mainPanel.add(this.propLabel, c);
        
        this.propPanel = new JPanel(new BorderLayout());
        c.gridy = 4;
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        mainPanel.add(this.propPanel, c);
        
        JPanel tmpPanel = new JPanel(new GridBagLayout());
        tmpPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        GridBagConstraints c3 = new GridBagConstraints();
        c3.anchor = GridBagConstraints.WEST;
        c3.weightx = 1;
        tmpPanel.add(mainPanel, c3);
        
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 1.0;
        c2.anchor = GridBagConstraints.FIRST_LINE_START;
        super.add(tmpPanel, c2);
        this.removeButton = new JButton("X");
        this.removeButton.setMargin(new Insets(0,3,0,3));
        c2.gridx = 1;
        c2.weightx = 0.0;
        c2.anchor = GridBagConstraints.FIRST_LINE_END;
        super.add(this.removeButton, c2);
	}

	class TempHelper {
		public JTextField varnameField;
		public FiltersView filterView;
	}
	private Map<URILiteralWrapper,TempHelper> tmphelper;
	
	@SuppressWarnings("null")
	private JPanel buildPropertiesPanel(EventType eventTyp) {
		
		Stack<String> vars = new Stack<String>();
		vars.add("f");
		vars.add("e");
		vars.add("d");
		vars.add("c");
		vars.add("b");
		vars.add("a");
		
		this.tmphelper = new HashMap<URILiteralWrapper,TempHelper>();
		
		this.propLabel.setVisible(eventTyp.getProperties().length > 0);
		
		JPanel pane = new JPanel(new GridBagLayout());
		
		if(eventTyp==null || eventTyp.getProperties().length==0)
			return pane;
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4,2,4,2);
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		int row = 0;
		c.gridy = row++;
		c.gridx = 1;
		pane.add(new JLabel("Var."), c);
		c.gridx = 2;
		pane.add(new JLabel("Filters"), c);
		
		for(URILiteralWrapper prop : eventTyp.getProperties()) {
			c.gridy = row++;
			c.fill = GridBagConstraints.NONE;
		
			JTextField nameField = new JTextField(prop.toString());
			//nameField.setMinimumSize(new Dimension(100,20));
			nameField.setEditable(false);
			nameField.setMinimumSize(nameField.getPreferredSize());
			
			c.gridx = 0;
			pane.add(nameField, c);
			
			
			JTextField varnameField = new JTextField(5);
			//varnameField.setText(vars.pop());
			varnameField.setMinimumSize(varnameField.getPreferredSize());
			c.gridx = 1;
			//c.weightx = 1.0;
			pane.add(varnameField, c);
			//c.weightx = 0;
			
			
			FiltersView filterView = new FiltersView();
			
			c.gridx = 2;
			pane.add(filterView, c);
			
			TempHelper t = new TempHelper();
			t.varnameField = varnameField;
			t.filterView = filterView;
			this.tmphelper.put(prop, t);
		}
		
		return pane;
	}

	@Override
	public void actionPerformed(ActionEvent e) {	
		if(e.getSource() == this.eventTypesBox) {
			EventType sel = (EventType)this.eventTypesBox.getSelectedItem();
			JPanel panel = buildPropertiesPanel(sel);
			this.propPanel.removeAll();
			this.propPanel.add(panel, BorderLayout.CENTER);
			this.propPanel.updateUI();
		}
	}
	
	public void addRemoveActionListener(ActionListener al) {
		this.removeButton.addActionListener(al);
	}
	
	public EventType getEventType() {
		return (EventType)this.eventTypesBox.getSelectedItem();
	}

	public WindowOperator getWinOp() {
		return (WindowOperator)this.winTypeBox.getSelectedItem();
	}

	public int getWinParam() {
		switch ((WindowOperator) this.winTypeBox.getSelectedItem()) {
		case COUNT:
			return (Integer) this.windowCountSpinner.getValue();
		case DURATION:
			return (Integer) this.windowDurationSpinner.getValue();
		}
		return -1;
	}
	
	public List<PropertyFilterData> getPropertyFilterData() {
		List<PropertyFilterData> propFilterDataList = new ArrayList<PropertyFilterData>();
		
		for(URILiteralWrapper prop : this.tmphelper.keySet()) {
			PropertyFilterData d = new PropertyFilterData();
			d.propertyName = prop.getWrappedLiteral().getString();
			TempHelper tmp = this.tmphelper.get(prop);
			d.varName = tmp.varnameField.getText();
			d.filters = tmp.filterView.getFilters();
			propFilterDataList.add(d);
		}
		
		return propFilterDataList;
	}
}
