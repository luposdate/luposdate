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
package lupos.event.consumer.app.charts;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.util.TimedWrapper;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;
/**
 * Wrapper for XY-DataSets
 * @author heidemey
 *
 */
public class XYChartModel extends DataModel {

	private Variable valueVar;
	private Variable valueVar2;
	
	public XYChartModel(ChartTyp type){
		super(type);
		
		valueVar = new Variable("");
		valueVar2 = new Variable("");
		dataset = new MatrixSeriesCollection();
		
	}
	
	public XYChartModel(ChartTyp type, String[] vars){
		this(type);
		
		categoryVar = new Variable(vars[0]);
		valueVar = new Variable(vars[1]);
		valueVar2 = new Variable(vars[2]);
	}
	
	/**
	 * Set the Variable containing the y-values
	 * @param y
	 */
	public void setY(Variable y){
		valueVar = y;
	}
	
	/**
	 * Sets the variable containing the z-values
	 * @param z
	 */
	public void setZ(Variable z){
		valueVar2 = z;
		
	}
	
	@Override
	public void fillDataset(QueryResult l) {
		
		getDataset().removeAllSeries();	
		
		Iterator<Bindings> it = l.getCollection().iterator();
		
		int size = l.size();
		
		int[] x = new int[size];
		int[] y = new int[size];
		double[] z = new double[size];
		int maxX=0;
		int maxY=0;
		int countBind = 0;
		
		// fill the data model of the chart
		while (it.hasNext()) {
			Bindings bind = it.next();
			Number tmp;
			// find the variable ...
			Literal lit = bind.get(valueVar);
			// ... and extract the value
			if (lit != null) {
				tmp = literal2Number(lit);
				if (tmp != null) {
					y[countBind] = tmp.intValue();
					if (tmp.intValue() > maxY)
						maxY = tmp.intValue();
				}
			}
			lit = bind.get(valueVar2);
			if (lit != null) {
				// ... and extract the value
				tmp = literal2Number(lit);
				if (tmp != null)
					z[countBind] = tmp.doubleValue();
			}
			lit = bind.get(categoryVar);
			if (lit != null) {
				// ... and extract the category
				tmp = literal2Number(lit);
				if (tmp != null) {
					x[countBind] = tmp.intValue();
					if (tmp.intValue() > maxX)
						maxX = tmp.intValue();
				}
			}
			countBind++;
		}
		// build the dataset
		MatrixSeries series = new MatrixSeries("Chart", maxX + 1, maxY + 1);
		for (int j = 0; j < size; j++) {
			series.update(x[j], y[j], z[j]);
		}

		getDataset().addSeries(series);

	}
	
	@Override
	public MatrixSeriesCollection getDataset(){
		
		return (MatrixSeriesCollection) super.dataset;
		
	}

	@Override
	public JFreeChart makeChart() {
		
		return ChartFactory.createBubbleChart("Chart", categoryVar.getName(), valueVar.getName(), getDataset(), PlotOrientation.VERTICAL, false, true , false);
		
	}

}
