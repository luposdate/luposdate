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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.queryresult.QueryResult;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;
/**
 * Wrapper for JFreeChart DataSets
 * Contains Logic to generate Chart from a QueryResult 
 * @author heidemey
 *
 */
abstract public class DataModel {
	
	protected Dataset dataset;

	protected Variable categoryVar;
	protected ChartTyp chartType;
	
	
	/**
	 * Constructor with ChartTyp
	 * @param chartType
	 */
	public DataModel(ChartTyp chartType){
		
		this.chartType=chartType;
		categoryVar = new Variable("");
		
	}
	
	/**
	 * Returns the DataSet of this model  
	 * @return
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Set the Variable containing data for the X-Axis 
	 * @param xVar
	 */
	public void setXVar(Variable xVar) {
		this.categoryVar = xVar;
	}
	
	/**
	 * Extract data from QueryResult into the DataSet
	 * @param l
	 */
	abstract public void fillDataset(QueryResult l); 
	
	/**
	 * Returns a legend for the chart.
	 * @return
	 */
	public String getLegend(){
		return "Chart";
	}
	
	/**
	 * Return the type of the chart, see ChartTyp
	 * @return
	 */
	public ChartTyp getChartTyp(){
		return chartType;
	}
	
	/** Parses a String into a Java Number class
	 * 
	 * @param type XML schema data type of the String  
	 * @param content String to be parsed
	 * @return Number class 
	 *  Long - for <http://www.w3.org/2001/XMLSchema#long>
	 *  Integer - for <http://www.w3.org/2001/XMLSchema#int>, <http://www.w3.org/2001/XMLSchema#integer>
	 *  Float - for <http://www.w3.org/2001/XMLSchema#float>
	 *  Double - for <http://www.w3.org/2001/XMLSchema#double>, <http://www.w3.org/2001/XMLSchema#decimal>
	 * @throws NumberFormatException if literal is not a number
	 */
	protected Number content2Number(String type, String content) throws NumberFormatException{
		
		if (type == null || content == null)
			return null;
			
		content=content.substring(1, content.length()-1);
		
		if(type.equals("<http://www.w3.org/2001/XMLSchema#long>")) 
			return Long.parseLong(content);
		if(type.equals("<http://www.w3.org/2001/XMLSchema#int>"))
			return Integer.parseInt(content);
		if(type.equals("<http://www.w3.org/2001/XMLSchema#integer>"))
			return Integer.parseInt(content);
		if (type.equals("<http://www.w3.org/2001/XMLSchema#float>"))
			return Float.parseFloat(content);
		if (type.equals("<http://www.w3.org/2001/XMLSchema#double>"))
			return Double.parseDouble(content);
		if (type.equals("<http://www.w3.org/2001/XMLSchema#decimal>"))
			return Double.parseDouble(content);
		
		throw new NumberFormatException();
		
	}
	
	/**
	 * Creates a chart for the contained DataSet
	 * @return
	 */
	abstract public JFreeChart makeChart();

	/**
	 * Parses a Lietral into a Number
	 * @param literal - Literal to be parsed
	 * @return Number of same value
	 * @throws NumberFormatException if literal is not typed or type is not a number (see content2Number)
	 */
	protected Number literal2Number(Literal literal) throws NumberFormatException{
		if (literal.isTypedLiteral()) {
			TypedLiteral tl = (TypedLiteral) literal;
			String typ = tl.getType();
			String content = tl.getContent();
			return content2Number(typ, content);
		}
		
		throw new NumberFormatException();
		//return null;

	}
	
	/**
	 * Returns the untyped content of a Literal
	 * @param literal
	 * @return
	 */
	protected String stripType(Literal literal){
		TypedLiteral tl = null;
		String content;
		if (literal.isTypedLiteral()) {
			// strip of the type
			tl = (TypedLiteral) literal;
			content = tl.getContent();
			return content.substring(1, content.length() - 1);
		} else {  // has no type
			return literal.toString();
		}
	}
	
	/**
	 * Generate the chart as jpeg Image
	 * @param width - width of the image
	 * @param height - height of the image
	 * @return ByteArrayOutputStream containing the image
	 */
	public ByteArrayOutputStream asImage(int width, int height){
		JFreeChart chart = makeChart();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try{
		ChartUtilities.writeChartAsJPEG(out, chart, width, height);//saveChartAsPNG(new File(""), chart, width, height);
		}catch(IOException ioExc){
			ioExc.printStackTrace();
		}
		
		return out;
		
	}

}
