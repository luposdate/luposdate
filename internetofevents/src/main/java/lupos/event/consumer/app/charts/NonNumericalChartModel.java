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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.util.TimedWrapper;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Wrapper for CategoryDatasets that accepts non-numerical
 * y-values
 *
 * @author heidemey
 * @version $Id: $Id
 */
public class NonNumericalChartModel extends DataModel {
	

	private Variable valueVar;
	private HashMap<Integer,Integer> hashMap;
	private ArrayList<String> stringList;
	
	/**
	 * <p>Constructor for NonNumericalChartModel.</p>
	 *
	 * @param type a {@link lupos.event.consumer.app.charts.ChartTyp} object.
	 */
	public NonNumericalChartModel(ChartTyp type){
		super(type);
		
		valueVar = new Variable("");
		dataset = new DefaultCategoryDataset();
		hashMap = new HashMap<Integer, Integer>();
		stringList = new ArrayList<String>();
		
	}
	
	/**
	 * <p>Constructor for NonNumericalChartModel.</p>
	 *
	 * @param type a {@link lupos.event.consumer.app.charts.ChartTyp} object.
	 * @param vars an array of {@link java.lang.String} objects.
	 */
	public NonNumericalChartModel(ChartTyp type, String[] vars){
		this(type);
		valueVar = new Variable(vars[1]);
		categoryVar = new Variable(vars[0]);
		
	}

	/** {@inheritDoc} */
	@Override
	public void fillDataset(QueryResult l) {
		
		
		
		getDataset().clear();
		hashMap.clear();
		stringList.clear();
		
		//get names of variables 
		//xEntry: Strings that appear on x-Axis, yEntry: height of String on y-Axis
		String[] xEntry = new String[l.size()];
		int[] yEntry = new int[l.size()];
		
		//temporary variables
		int key;
		int diffY=0;
		int i=0;
		
		//walk through bindings 
		Iterator<Bindings> it = l.getCollection().iterator();
		
		while (it.hasNext()) {
			Bindings bind = it.next();
			String y = null;
			Literal lit = bind.get(valueVar);
			// find the variable ...
			if (lit != null) {
				// ... and extract the value
				y = stripType(lit);

				if (y != null) {
					// strings that appear on y-axis are put into a hashmap
					// connect the string with an arbitrary number diffY through
					// the hashMap
					// store diffY for use as chart value
					// store the string for creating the legend
					key = y.hashCode();
					if (hashMap.containsKey(key)) {
						yEntry[i] = hashMap.get(key);
					} else {
						diffY++;
						stringList.add(y);
						hashMap.put(key, diffY);
						yEntry[i] = diffY;
					}
				}
			}
			lit = bind.get(categoryVar);
			if (lit != null) {
				// ... and extract the category
				xEntry[i] = stripType(lit);
			}
			i++;
		}
		// build the dataset 
		if (!hashMap.isEmpty()){
			for (int k=0;k<i;k++) {
				if (xEntry[k] != null)
					getDataset().addValue(yEntry[k], new Integer(yEntry[k]), xEntry[k]);
			}
		}
		

	}
	
	/**
	 * Sets the variable containing the y-values
	 *
	 * @param var a {@link lupos.datastructures.items.Variable} object.
	 */
	public void setValueVar(Variable var){
		valueVar = var;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public DefaultCategoryDataset getDataset(){
		
		return (DefaultCategoryDataset) super.dataset;
		
	}
	
	/** {@inheritDoc} */
	@Override
	public String getLegend(){

		String legend=new String();
		
		if (!hashMap.isEmpty()){
			for (int k=0;k< hashMap.size();k++)
			{
				legend=legend.concat(k+1 + " - " + stringList.get(k)+"\n");
			}
		}
			
		return legend;
	}

	/** {@inheritDoc} */
	@Override
	public JFreeChart makeChart() {
		
		JFreeChart chart=null;
		chart = ChartFactory.createLineChart("Chart", categoryVar.getName(), valueVar.getName(), getDataset(), PlotOrientation.VERTICAL, false, true, false);
		// and change the rendering to shapes
		((LineAndShapeRenderer)((CategoryPlot)chart.getPlot()).getRenderer()).setLinesVisible(false);
		((LineAndShapeRenderer)((CategoryPlot)chart.getPlot()).getRenderer()).setShapesVisible(true);
		return chart;
	}

}
