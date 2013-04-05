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
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryToPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Wrapper for CategoryDataSets
 * @author heidemey
 *
 */
public class CategoryChartModel extends DataModel {
	
	
	protected Variable valueVar;
	protected Variable valueVar2;
	
	public CategoryChartModel(ChartTyp chartType){
		super(chartType);
		valueVar = new Variable("");
		valueVar2 = new Variable("");
		dataset = new DefaultCategoryDataset();
	}
	
	public CategoryChartModel(ChartTyp chartType, String[] vars){
		super(chartType);
		categoryVar = new Variable(vars[0]);
		valueVar = new Variable(vars[1]);
		if (vars.length>2)
			valueVar2 = new Variable(vars[2]);
		dataset = new DefaultCategoryDataset();
	}
	
	/**
	 * Sets the variable containing first y-value
	 * @param valueVar
	 */
	public void setValueVar(Variable valueVar) {
		this.valueVar = valueVar;
	}
	/**
	 * Sets the variable containing second y-value
	 * @param valueVar2
	 */
	public void setValueVar2(Variable valueVar2) {
		this.valueVar2 = valueVar2;
	}
	

	@Override
	public DefaultCategoryDataset getDataset(){
		return (DefaultCategoryDataset) super.dataset;
	}
	
	@Override
	public void fillDataset(QueryResult l) {
		dataset = new DefaultCategoryDataset();

		Iterator<Bindings> it = l.getCollection().iterator();

		// fill the data model of the chart
		while (it.hasNext()) {
			Bindings bind = it.next();
			Number y1 = null;
			Number y2 = null;
			String column = null;
			Literal lit = bind.get(valueVar);
			// find the variable ...
			if (lit != null) {
				// ... and extract the value
				y1 = literal2Number(lit);

			}
			lit = bind.get(valueVar2);
			if (lit != null) {
				// ... and extract the value
				y2 = literal2Number(lit);
			}
			lit = bind.get(categoryVar);
			if (lit != null) {
				// ... and extract the category
				column = stripType(lit);
			}

			if (y1 != null && column != null)
				getDataset().addValue(y1, valueVar.getName(), column);
			if (y2 != null && column != null)
				getDataset().addValue(y2, valueVar2.getName(), column);

		}
	}
	
	/**
	 * Generates and returns the chart for the contained DataSet
	 */
	@Override
	public JFreeChart makeChart(){
		
		JFreeChart chart=null;
		if (chartType==ChartTyp.BAR_CHART)
			chart = ChartFactory.createBarChart("Chart", categoryVar.getName(), valueVar.getName(), getDataset(), PlotOrientation.VERTICAL, false, true, false);
		else if (chartType==ChartTyp.PIE_CHART)
			chart = ChartFactory.createPieChart("Chart", new CategoryToPieDataset((CategoryDataset)getDataset(),org.jfree.util.TableOrder.BY_ROW,0 ), false, true, false);
		else if (chartType==ChartTyp.LINE_CHART)
			chart = ChartFactory.createLineChart("Chart", categoryVar.getName(), valueVar.getName(), getDataset(), PlotOrientation.VERTICAL, false, true, false);
		return chart;
		
	}

}
