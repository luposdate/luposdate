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
package lupos.event.consumer.app.charts;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.util.TimedWrapper;

import org.jfree.chart.ChartPanel;


/**
 *  * Class for displaying input elements for non-numerical charts and display
 * of the chart itself.
 *
 * @author heidemey
 * @version $Id: $Id
 */
public class NonNumericalChart extends ChartHandler {

//	protected DefaultCategoryDataset dataset;
	protected JTextField value;
	protected JTextField category;
	protected JTextArea legende;
	protected JScrollPane scrollpane;
	
	//NonNumericalChartModel model;
	
	/**
	 * Contructor. Creates an appropiate DataModel and inits all required GUI-Elements.
	 *
	 * @param type a {@link lupos.event.consumer.app.charts.ChartTyp} object.
	 */
	public NonNumericalChart(ChartTyp type){
		super(type);
		
		super.add(new JLabel("value"),createGBC(0,0, GridBagConstraints.HORIZONTAL));
	    super.add(new JLabel("category"),createGBC(0,1, GridBagConstraints.HORIZONTAL));
	    
	    value = new JTextField(25);
	    value.setEditable(true);
	    value.setMinimumSize(new Dimension(80,25));
	    super.add(value, createGBC(1,0, GridBagConstraints.HORIZONTAL));
	    
	    category = new JTextField(25);
	    category.setEditable(true);
	    category.setMinimumSize(new Dimension(80,25));
	    super.add(category, createGBC(1,1, GridBagConstraints.HORIZONTAL));
	    
	    JPanel sub = new JPanel(new GridBagLayout());
	    this.add(sub, new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));
	    
	    legende = new JTextArea();
	    legende.setEditable(false);
	    legende.setVisible(false);
	    scrollpane = new JScrollPane(legende);
	    scrollpane.setEnabled(true);
	    scrollpane.setMinimumSize(new Dimension(120,250));
	    scrollpane.setVisible(false);
	    sub.add(scrollpane,new GridBagConstraints(0,0, 1, 1, 0.2, 0.2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));
	    
		chartPanel = new ChartPanel(null);
		sub.add(chartPanel,new GridBagConstraints(1, 0, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));
		
		model = new NonNumericalChartModel(type);
	}
	
	/** {@inheritDoc} */
	@Override
	public void fillDataset(TimedWrapper<QueryResult> l) {
		
		getModel().setXVar(new Variable(category.getText()));
		getModel().setValueVar(new Variable(value.getText()));

		getModel().fillDataset(l.getWrappedObject());
	}
	
	/** {@inheritDoc} */
	@Override
	public void clearFields(){
		value.setText(null);
		category.setText(null);
		
	}
	
	/**
	 * <p>getModel.</p>
	 *
	 * @return a {@link lupos.event.consumer.app.charts.NonNumericalChartModel} object.
	 */
	protected NonNumericalChartModel getModel(){
		return (NonNumericalChartModel) super.getModel();
	}
	
	/** {@inheritDoc} */
	@Override
	protected void makeChart() {
		super.makeChart();
		//chartPanel.setChart(model.makeChart());
		
		// create the legend for the y-axis
//		if (!hashMap.isEmpty()){
//			for (int k=0;k< hashMap.size();k++)
//			{
//				legend=legend.concat(k+1 + " - " + stringList.get(k)+"\n");
//			}
		if( model.getLegend().length()!=0 ){
			legende.setText(model.getLegend());
			legende.setVisible(true);
			scrollpane.setVisible(true);
			updateUI();
		}else {
			legende.setVisible(false);
			scrollpane.setVisible(false);
			updateUI();
		}
		
		
	}

}
