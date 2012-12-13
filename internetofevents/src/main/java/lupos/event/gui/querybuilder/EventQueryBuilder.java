/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.event.gui.querybuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides a method to build a stream-based SPARQL query.
 */
public class EventQueryBuilder {

	/**
	 * Query skeleton with place markers which get replaced
	 */
	private final String QueryTemplate ="SELECT *\n" +
			"  STREAM INTERMEDIATERESULT DURATION %IRD%\n" +
			"  WHERE {\n" +
			"%WINDOWS%"+
			"%FILTER%"+
			"  }";
	
	/**
	 * Window skeleton with place markers which get replaced 
	 */
	private final String WindowTemplate =
			"    WINDOW TYPE INSTANCE %EVT% %WINOP% %WINPARAM% {\n" +
			"%TRP%"+
			"    }\n";
	

	private final static Map<WindowOperator, String> win = new HashMap<WindowOperator, String>() {{
		   put(WindowOperator.COUNT, "SLIDINGINSTANCES");
		   put(WindowOperator.DURATION, "SLIDINGDURATION");
		}};
	
	
	/**
	 * Builds a query.
	 * @param intermediateResultDuration Evaluation interval in milliseconds
	 * @param eventWindowDataList Data of the windows
	 * @return 
	 */
	public String buildQuery(int intermediateResultDuration, List<EventWindowData> eventWindowDataList) {
		
		// return an empty string if no window data is specific
		if(eventWindowDataList.isEmpty())
			return "";
					
		String query = this.QueryTemplate.replace("%IRD%", Integer.toString(intermediateResultDuration));
			
		// create windows
		String windows = "";
		int winCounter = 1;
		for(EventWindowData d : eventWindowDataList) {
			String w = this.WindowTemplate.replace("%EVT%", d.eventType.getEventUri().toString());
			w = w.replace("%WINOP%", win.get(d.winOp));			
			w = w.replace("%WINPARAM%", d.winParam+"");
			
			// create triple patterns in current window
			String trp = "";
			for(PropertyFilterData d2 : d.propertyFilterData) {
				// remove '?' and leading/trailing whitespaces
				String trimmedVarName = d2.varName.replace("?", "").trim();
				// only create a triple-pattern if a variable name is given for the property
				if(!trimmedVarName.isEmpty()) {
					trp += "      ?s" + winCounter + " <" + d2.propertyName + "> ?" + d2.varName + ".\n";
				}
			}
			
			// if no triple patterns were created, add one to prevent exception at the evaluation
			if(trp.isEmpty()) {
				trp += "      ?s" + winCounter  + " ?p" + winCounter + " ?o" + winCounter + ".\n";
			}
			w = w.replace("%TRP%", trp);
			windows += w;
			
			winCounter++;
		}		
		query = query.replace("%WINDOWS%", windows);
		
		// build filter
		boolean noFilter = true;
		String filter = "    FILTER(";
		for(EventWindowData d : eventWindowDataList) {
			for(PropertyFilterData d2 : d.propertyFilterData) {
				if(d2.filters.size() > 0) {					
					for(Filter f : d2.filters) {
						if(!noFilter) // false on first filter
							filter += " && ";
						filter += "?" + d2.varName + " " + f.op + " " + f.param;
					}
					noFilter = false;
				}
			}
		}
		filter = /*filter.replace("&& ", "")*/filter + ")\n";
		query = query.replace("%FILTER%", noFilter ? "" : filter);

		
		return query;
	}
}
