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
package lupos.event.consumer.app;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lupos.datastructures.queryresult.QueryResult;
import lupos.event.consumer.Consumer;
import lupos.event.consumer.app.charts.CategoryChart;
import lupos.event.consumer.app.charts.ChartFactory;
import lupos.event.consumer.app.charts.ChartHandler;
import lupos.event.consumer.app.charts.ChartTyp;
import lupos.event.consumer.app.charts.HistogramChart;
import lupos.event.consumer.app.charts.NonNumericalChart;
import lupos.event.consumer.app.charts.XYChart;
import lupos.event.util.TimedWrapper;

/**
 * Result view for presentation as charts.
 * @author heidemey
 *
 */
public class SubscriptionChartView extends AbstractSubscriptionResultView implements ActionListener {

//	private static final int UPDATE_INTERVAL = 500;
	
	//final private Consumer consumer;
	//private Subscription subscription = null;
	private JComboBox chartType;
	private JPanel cPanel;
	
	ChartHandler handler;
	
	//Timer timer;
	
	/** Constructor
	 * 
	 * @param consumer
	 */
	public SubscriptionChartView(Consumer consumer) {
		super(consumer);
		
		//this.consumer =consumer;
		
		GridBagConstraints c = createGBC(0,0, GridBagConstraints.HORIZONTAL);
	    super.add(new JLabel("Chart Type:"), c);
	    
	    chartType = new JComboBox( ChartTyp.values() );
	    chartType.setSelectedIndex(0);
	    handler=ChartFactory.getHandler(ChartTyp.values()[0]);
	    
	    super.add(chartType, createGBC(1,0, GridBagConstraints.HORIZONTAL));
	    chartType.addActionListener(this);
	    
	    cPanel=handler.getChart();
		this.add(cPanel,new GridBagConstraints(0, 4, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));	
		this.updateUI();
	    
//	    this.timer = new Timer(UPDATE_INTERVAL, this);
//        this.timer.start();

	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(chartType)){
			chartTypeChanged();
			return;
		}
		if(this.subscription == null || !this.consumer.isConnected()) 
			return;
		
		System.out.println("Updating subscription results view..");
		
		// get results of the current subscription
		List<TimedWrapper<QueryResult>> l = this.consumer.getQueryResults(this.subscription);
		
		if(l==null){
			return;
		}
		
		if ( !l.isEmpty() ) {
			try{
				// try to populate the dataset and create the chart
				handler.fillDataset(l.get(l.size()-1));
				handler.getChart();
				updateUI();
			}catch(NumberFormatException ex){
				ex.printStackTrace();
				handler.clearFields();
				JOptionPane.showMessageDialog(this, "The choosen Variables are not suitable for this graph", "Graph Rendering Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	
	@Override
	protected void ClearDataset(){
		//handler.clearDataset();
	}
	
	/**
	 * Get the new ChartType from the combo box and change
	 * the GUI to match.
	 */
	protected void chartTypeChanged(){
		ChartTyp typ = (ChartTyp)chartType.getSelectedItem();
		handler = ChartFactory.getHandler(typ);

		this.remove(cPanel);
		cPanel=handler.getChart();
		this.add(cPanel,new GridBagConstraints(0, 4, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));	
		this.updateUI();
	}
	
	
}
