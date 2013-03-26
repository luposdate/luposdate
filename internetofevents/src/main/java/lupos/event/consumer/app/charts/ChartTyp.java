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

public enum ChartTyp{
	BAR_CHART		("BAR", "Bar Chart"),
	PIE_CHART		("PIE", "Pie Chart"),
	LINE_CHART		("LINE", "Line Chart"),
	HISTOGRAM_CHART	("HISTOGRAM", "Histogram"),
	BUBBLE_CHART	("BUBBLE", "Bubble Chart"),
	TABLE			("TABLE", "Table");		
	
	private String shortName;
	private String longName;
	
	private ChartTyp(String shortName, String longName){
		this.shortName = shortName;
		this.longName = longName;
	}
	
	public String getLongName(){
		return longName;
	}
	
	public String getShortName(){
		return shortName;
	}
	
	@Override
	public String toString(){
		return getLongName();
	}
	
	public boolean equals(String typ){
		return getShortName().equals(typ);
	}
	
//	public ChartTyp find(String shortName){
//		for( ChartTyp typ : values() ){
//			if (typ.getShortName().equals(shortName))
//				return typ;
//		}
//	}
}