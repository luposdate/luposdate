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
import javax.swing.JTextField;

import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.util.TimedWrapper;

import org.jfree.chart.ChartPanel;

/**
 * Class for displaying input elements for bar, line and pie charts and display 
 * of the chart itself.
 * @author heidemey
 *
 */
public class CategoryChart extends ChartHandler {
	
	protected JTextField value;
	protected JTextField category;
	protected JTextField value2;
	
	/**
	 * Contructor. Creates an appropiate DataModel and inits all required GUI-Elements.
	 * @param chartTyp
	 */
	public CategoryChart(ChartTyp chartTyp){
		super(new GridBagLayout(), chartTyp);
		
		super.add(new JLabel("Value"),createGBC(0,0, GridBagConstraints.HORIZONTAL));
	    //super.add(new JLabel("Value2"),createGBC(0,1, GridBagConstraints.HORIZONTAL));
	    super.add(new JLabel("Category"),createGBC(0,2, GridBagConstraints.HORIZONTAL));
		
	    value = new JTextField(25);
	    value.setEditable(true);
	    value.setMinimumSize(new Dimension(80,25));
	    super.add(value, createGBC(1,0, GridBagConstraints.HORIZONTAL));

	    value2 = new JTextField(25);
	    value2.setEditable(true);
	    value2.setMinimumSize(new Dimension(80,25));
	    if(chartTyp!=ChartTyp.PIE_CHART){
	    	super.add(new JLabel("Value2"),createGBC(0,1, GridBagConstraints.HORIZONTAL));
	    	super.add(value2, createGBC(1,1, GridBagConstraints.HORIZONTAL));
	    }
	    

	    category = new JTextField(25);
	    category.setEditable(true);
	    category.setMinimumSize(new Dimension(80,25));
	    super.add(category, createGBC(1,2, GridBagConstraints.HORIZONTAL));
	    
	    model = new CategoryChartModel(chartTyp);
	    
		chartPanel = new ChartPanel(null);
		this.add(chartPanel,new GridBagConstraints(0, 3, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 1, 1));
	}
	
	
	/**
	 * Extracts data from QueryResult into the DataModel
	 */
	@Override
	public void fillDataset(TimedWrapper<QueryResult> l)
	{
		getModel().setXVar(new Variable(category.getText()));
		getModel().setValueVar(new Variable(value.getText()));
		getModel().setValueVar2(new Variable(value2.getText()));
		
		getModel().fillDataset(l.getWrappedObject());
	}
	
	
	@Override
	protected CategoryChartModel getModel(){
		return (CategoryChartModel) super.getModel(); 
	}
	
	/**
	 * Clears all input fields of this element
	 */
	@Override
	public void clearFields(){
		value.setText(null);
		category.setText(null);
		value2.setText(null);
		
	}

}
