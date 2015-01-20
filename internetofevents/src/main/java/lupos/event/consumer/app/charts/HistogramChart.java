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
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.util.TimedWrapper;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
  * Class for displaying input elements for histogram charts and display 
 * of the histogram itself.
 * @author heidemey
 *
 */
public class HistogramChart extends ChartHandler {

	protected JTextField bins;
	protected JTextField variable;

	//HistogramChartModel model;
	
	/**
	 * Contructor. Creates an appropiate DataModel and inits all required GUI-Elements.
	 * @param type
	 */
	public HistogramChart(ChartTyp type){
		super(new GridBagLayout(), type);
		
		
	    super.add(new JLabel("Number of Bins"),createGBC(0,0, GridBagConstraints.HORIZONTAL));
	    super.add(new JLabel("Variable"),createGBC(0,1, GridBagConstraints.HORIZONTAL));
		
	    bins = new JTextField(25);
	    bins.setEditable(true);
	    bins.setMinimumSize(new Dimension(80,25));
	    super.add(bins, createGBC(1,0, GridBagConstraints.HORIZONTAL));

	    variable = new JTextField(25);
	    variable.setEditable(true);
	    variable.setMinimumSize(new Dimension(80,25));
	    super.add(variable, createGBC(1,1, GridBagConstraints.HORIZONTAL));
	    
		chartPanel = new ChartPanel(null);
		this.add(chartPanel,new GridBagConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));
		
		model = new HistogramChartModel(type);
	}
	

	@Override
	public void fillDataset(TimedWrapper<QueryResult> l) {
		
		
		String stringBins = bins.getText();
		int numberBins=0;
		
		// get the number of bins in the histogram
		if (stringBins!=null)
		{
			try{
				if(stringBins != null && !stringBins.equals(""))
					numberBins=Integer.parseInt(stringBins);
			}
			catch(NumberFormatException ex){
				bins.setText(null);
				JOptionPane.showMessageDialog(this, "Number of Bins should be an Integer", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		getModel().setXVar( new Variable(variable.getText()));
		getModel().setNrBins(numberBins);
		getModel().fillDataset(l.getWrappedObject());
	}

	
	@Override
	protected HistogramChartModel getModel(){
		return (HistogramChartModel) super.getModel();
	}
	
	@Override
	public void clearFields(){
		bins.setText(null);
		variable.setText(null);
		
	}

}
