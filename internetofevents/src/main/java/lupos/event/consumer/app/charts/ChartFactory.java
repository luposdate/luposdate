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

import lupos.datastructures.queryresult.QueryResult;
import lupos.event.consumer.app.charts.ChartTyp;

/**
 * Class for finding the GUI-Handler and Datamodel for a type of chart
 * @author heidemey
 *
 */
public class ChartFactory {
	
	/**
	 * Finds and returns the ChartHandler GUI for the given type of chart
	 * @param typ
	 * @return
	 */
	static public ChartHandler getHandler(ChartTyp typ){
		if( typ == ChartTyp.PIE_CHART)
			return new CategoryChart(typ);
		if(typ == ChartTyp.BAR_CHART)
			return new CategoryChart(typ);
		if(typ == ChartTyp.LINE_CHART)
			return new CategoryChart(typ);
		if(typ == ChartTyp.HISTOGRAM_CHART)
			return new HistogramChart(typ);
		if(typ == ChartTyp.BUBBLE_CHART)
			return new XYChart(typ);
		if(typ == ChartTyp.TABLE)
			return new NonNumericalChart(typ);
		
		return null;
	}
	
	/** 
	 * Creates a DataModel for given type of chart and fills it with the values of
	 * the variables in the query result. Then returns the model. 
	 * 
	 * @param typ ChartTyp
	 * @param vars Array of variable names
	 * @param queryResult 
	 * @return DataModel for typ filled with data from the query result  
	 */
	static public DataModel getModel(String typ, String[] vars, QueryResult queryResult){
		DataModel model = null;
		if(ChartTyp.PIE_CHART.equals(typ)){
			model = new CategoryChartModel(ChartTyp.PIE_CHART, vars);
			model.fillDataset(queryResult);
		}
		if(ChartTyp.BAR_CHART.equals(typ)){
			model = new CategoryChartModel(ChartTyp.BAR_CHART, vars);
			model.fillDataset(queryResult);
		}
		if(ChartTyp.LINE_CHART.equals(typ)){
			model = new CategoryChartModel(ChartTyp.LINE_CHART, vars);
			model.fillDataset(queryResult);
		}
		if(ChartTyp.HISTOGRAM_CHART.equals(typ)){
			model = new HistogramChartModel(ChartTyp.HISTOGRAM_CHART, vars);
			model.fillDataset(queryResult);
		}
		if(ChartTyp.BUBBLE_CHART.equals(typ)){
			model = new XYChartModel(ChartTyp.BUBBLE_CHART, vars);
			model.fillDataset(queryResult);
		}
		if(ChartTyp.TABLE.equals(typ)){
			model = new NonNumericalChartModel(ChartTyp.TABLE, vars);
			model.fillDataset(queryResult);
		}
		
		return model;
	}
}
