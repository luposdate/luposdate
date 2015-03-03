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
package lupos.event.producer.yql;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

import org.json.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.producer.ProducerBase;
import lupos.event.producer.ProducerBaseNoDuplicates;
import lupos.event.util.Literals;
import lupos.event.util.Utils;

/**
 * <p>FinanceProducer class.</p>
 *
 * @author Christopher Gudat, Guillaume Assaud
 * Search finances with YQL-Query
 * @version $Id: $Id
 */
public class FinanceProducer extends ProducerBaseNoDuplicates {
	
	/** Constant <code>NAMESPACE="http://localhost/events/FinanceProducer"{trunked}</code> */
	public static final String NAMESPACE = "http://localhost/events/FinanceProducer/";
	private final static int INTERVAL = 3000;
 	private String SEARCH_SYMBOL = "Yahoo";

	private static final String SEARCH_URL_SYMBOL = "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=";
	private static final String SEARCH_URL_SYMBOL_2 = "&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
		
	/** Constant <code>FINANCE_TYPE_OBJECT</code> */
	public final static URILiteral FINANCE_TYPE_OBJECT = Literals.createURI(NAMESPACE, "FinanceProducer"); 	
 
	/**
	 * <p>Constructor for FinanceProducer.</p>
	 *
	 * @param msgService a {@link lupos.event.communication.SerializingMessageService} object.
	 */
	public FinanceProducer(SerializingMessageService msgService) {
		super(msgService, INTERVAL);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {			
			String encodedSymbol = java.net.URLEncoder.encode(this.SEARCH_SYMBOL, "ISO-8859-1");
			
			//Search Symbol from wished company
			String jsonStr0 = Utils.httpGet(SEARCH_URL_SYMBOL + encodedSymbol + SEARCH_URL_SYMBOL_2);
			jsonStr0 = jsonStr0.replace("YAHOO.Finance.SymbolSuggest.ssCallback(", "");
			
			JSONObject rootSymbolObject = new JSONObject(jsonStr0);
			JSONObject querySymbolObject = rootSymbolObject.getJSONObject("ResultSet");
			JSONArray resultArray = querySymbolObject.getJSONArray("Result");
			//Create list of triples
			List<List<Triple>> symbols = new ArrayList<List<Triple>>();
			
			
			//Create an instance for each event
			for(int i=0; i<resultArray.length(); i++) {	
				JSONObject jsonObj = resultArray.getJSONObject(i);
				
				//Get every parameter from the API
				String symbolId = jsonObj.getString("symbol");
				String encodedSymbolId = java.net.URLEncoder.encode(symbolId, "ISO-8859-1");
				String jsonStr = Utils.httpGet("http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22"+encodedSymbolId+"%22)&format=json&env=store://datatables.org/alltableswithkeys");
				
				//Query error filter
				if (jsonStr.contains("error")==true){
					System.out.println("Query not found!");
				}else{					
				JSONObject rootObj = new JSONObject(jsonStr);
				JSONObject queryObject = rootObj.getJSONObject("query");
				JSONObject reslutsObject = queryObject.getJSONObject("results");
				JSONObject quoteObject = reslutsObject.getJSONObject("quote");				
				
					List<Triple> triples = new ArrayList<Triple>();
					
					JSONArray results = quoteObject.names();
					Literal subject = LiteralFactory.createAnonymousLiteral("<"+1+">");
					triples.add(new Triple(subject, Literals.RDF.TYPE, FinanceProducer.FINANCE_TYPE_OBJECT));
					for (int c=0; c<results.length();c++){
						String resultName = results.getString(c);
						String resultString = quoteObject.getString((String) results.get(c));
						
						//try to cast String to double
						try{
							double resultCast = Double.parseDouble(resultString);
							final Literal SYMBOL_PREDICATE = Literals.createURI(NAMESPACE, resultName);
							triples.add(new Triple(subject, SYMBOL_PREDICATE, LiteralFactory.createTypedLiteral("\""+resultCast+"\"", Literals.XSD.DOUBLE)));
						}
						catch(Exception e){
							final Literal SYMBOL_PREDICATE = Literals.createURI(NAMESPACE, resultName);
							triples.add(new Triple(subject, SYMBOL_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(resultString)+"\"")));
						}
					}
					symbols.add(triples);
				}
			}
			return symbols;
			
		} catch (Exception e) {
			//Catch error and show message
			System.err.println(e);
			e.printStackTrace();
			
		}
		return null;
	}
	
	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public static void main(String[] args) throws Exception {
		//Create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		FinanceProducer tsp = new FinanceProducer(msgService);
		
		//Show dialog message box/		
		tsp.SEARCH_SYMBOL = JOptionPane.showInputDialog("Enter Company name", tsp.SEARCH_SYMBOL);

		// start producer
		tsp.start();
	}
}
