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
package lupos.event.consumer.app.charts;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 * Wrapper for HistogramDatasets
 * @author heidemey
 *
 */
public class HistogramChartModel extends DataModel {
	
	int numberBins;
	
	public HistogramChartModel(ChartTyp type){
		super(type);
		dataset = new HistogramDataset();
	}
	public HistogramChartModel(ChartTyp type, String[] vars){
		super(type);
		dataset = new HistogramDataset();
		numberBins=Integer.parseInt(vars[1]);
		categoryVar=new Variable(vars[0]);
	}
	
	@Override
	public HistogramDataset getDataset(){
		return (HistogramDataset) super.dataset;
	}
	
	/**
	 * Set the number of bins for the histogram
	 * @param numberOfBins
	 */
	public void setNrBins(int numberOfBins){
		this.numberBins=numberOfBins;
	}
	
	@Override
	public void fillDataset(QueryResult l) {
		
		
		dataset=new HistogramDataset();
		
		double[] values = new double[l.size()];
		int i=0;
		
		Iterator<Bindings> it = l.getCollection().iterator();
		
		// fill the data model of the chart
		while (it.hasNext()) {
			Bindings bind = it.next();
			Number value = null;
			// find the variable ...
			Literal lit = bind.get(categoryVar);
			if (lit != null) {
				// ... and extract the category
				value = literal2Number(lit);
				if (value != null)
					values[i] = value.doubleValue();
			}
			i++;
		}

		if (numberBins != 0)
			getDataset().addSeries("Histogram", values, numberBins);
	}

	
	@Override
	public JFreeChart makeChart() {
		JFreeChart chart=null;
		
		getDataset().setType(HistogramType.RELATIVE_FREQUENCY);
		chart = ChartFactory.createHistogram("Chart", categoryVar.getName(), "height", getDataset(), PlotOrientation.VERTICAL, false, true, false);
		
		return chart;
	}

}
