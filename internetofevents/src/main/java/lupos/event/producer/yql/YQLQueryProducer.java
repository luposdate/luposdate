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

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.event.communication.SerializingMessageService;
import lupos.event.producer.ProducerBase;
import lupos.event.producer.ProducerBaseNoDuplicates;
import lupos.event.util.Literals;
import lupos.event.util.Utils;
import org.codehaus.jackson.*;


/**
 * <p>YQLQueryProducer class.</p>
 *
 * @author Christopher Gudat, Guillaume Assaud
 * @version $Id: $Id
 */
public class YQLQueryProducer extends ProducerBaseNoDuplicates {
	/**
	 * Example YQL-Queries:
	 * 
	 * Find events in North Beach:
	 * select * from upcoming.events where woeid in (select woeid from geo.places where text="North Beach")
	 * 
	 * Get San Francisco geo data:
	 * select * from geo.places where text="san francisco, ca"
	 * 
	 * Default: Find sushi restaurant in san francisco with an average rating of 4.5:
	 * 			select * from local.search where query="sushi" and location="san francisco, ca" and Rating.AverageRating="4.5"
	 * 
	 * Get rss feed from yahoo news top stories:
	 * select title from rss where url="http://rss.news.yahoo.com/rss/topstories"
	 * 
	 * Get weather date from Berlin
	 * select * from weather.forecast where location in (select id from weather.search where query="berlin, germany")
	 * 
	 * For more information about YQL see: http://developer.yahoo.com/yql/
	 * or try the yahoo console: http://developer.yahoo.com/yql/console/
	 */
	
	public final String NAMESPACE;
	private final static int INTERVAL = 5000;
	private static final String SEARCH_URL = "http://query.yahooapis.com/v1/public/yql?q=";
	private static final String SEARCH_URL_2 = "&format=json&env=store://datatables.org/alltableswithkeys";
	
	
	private final URILiteral YQL_TYPE_OBJECT;
	private final String SEARCH_YQL;
	
			
	/**
	 * <p>Constructor for YQLQueryProducer.</p>
	 *
	 * @param msgService a {@link lupos.event.communication.SerializingMessageService} object.
	 * @param NAMESPACE a {@link java.lang.String} object.
	 * @param YQL_TYPE_OBJECT a {@link lupos.datastructures.items.literal.URILiteral} object.
	 * @param SEARCH_YQL a {@link java.lang.String} object.
	 */
	public YQLQueryProducer(SerializingMessageService msgService, final String NAMESPACE, final URILiteral YQL_TYPE_OBJECT, final String SEARCH_YQL) {
		super(msgService, INTERVAL);
		this.NAMESPACE = NAMESPACE;
		this.YQL_TYPE_OBJECT = YQL_TYPE_OBJECT;
		this.SEARCH_YQL = SEARCH_YQL;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {		
			//Query URL builder:
			String encoded = java.net.URLEncoder.encode(this.SEARCH_YQL, "ISO-8859-1");
			String jsonStr = Utils.httpGet(SEARCH_URL + encoded + SEARCH_URL_2);
			//Query error filter
			if (jsonStr.contains("error")==true){
				System.out.println("Query not found!");
			}else{					
				List<List<Triple>> symbols = new ArrayList<List<Triple>>();
				JsonFactory f = new JsonFactory();
				JsonParser jp = f.createJsonParser(jsonStr);
				//return JsonToken.START_OBJECT
				List<Triple> triples = new ArrayList<Triple>();
				Literal subject = LiteralFactory.createAnonymousLiteral("<"+1+">");
				triples.add(new Triple(subject, Literals.RDF.TYPE, this.YQL_TYPE_OBJECT));
				jp.nextToken();
				while (jp.nextToken() != JsonToken.END_OBJECT){										
					//Create anonymous literal id					
					//read all json file entries:
					while (jp.nextToken() != JsonToken.END_OBJECT) {
					   String fieldname = jp.getCurrentName();
					   String text = jp.getText();
					   //get field values
					   if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
				                // Get the value.
				                jp.nextToken();
				            }
					   //move to value, or START_OBJECT/START_ARRAY
					   jp.nextToken(); 
					   
					   //Filter wrong field names
					   if ((fieldname == text)||(text == "{")||(text == "}")||(text == "[")||(text == "]")){
						 // ignore  
					   } else{
						   //Try to cast String to double
						   try{
								double resultCast = Double.parseDouble(text);
								final Literal SYMBOL_PREDICATE = Literals.createURI(this.NAMESPACE, fieldname);
								System.out.println(fieldname+": "+resultCast);
								triples.add(new Triple(subject, SYMBOL_PREDICATE, LiteralFactory.createTypedLiteral("\""+resultCast+"\"", Literals.XSD.DOUBLE)));
							}
							catch(Exception e){
								final Literal SYMBOL_PREDICATE = Literals.createURI(this.NAMESPACE, fieldname);
								System.out.println(fieldname+": "+text);
								triples.add(new Triple(subject, SYMBOL_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(text)+"\"")));
							}
					   }
					   symbols.add(triples);		   
				   
					}
//					return symbols;
				}
				return symbols;
			}
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
		
		String namespace = JOptionPane.showInputDialog("Namespace", "http://localhost/events/YQLQueryProducer/");
		String query = JOptionPane.showInputDialog("query", "select * from local.search where query=\"sushi\" and location=\"san francisco, ca\" and Rating.AverageRating=\"4.5\"");
		String name = JOptionPane.showInputDialog("name", "YQLQueryProducer");
		
		URILiteral YQL_TYPE_OBJECT = Literals.createURI(namespace, name);
				
		YQLQueryProducer tsp = new YQLQueryProducer(msgService,namespace,YQL_TYPE_OBJECT,query);
		
		//Show dialog message boxes
		
		// start producer
		tsp.start();
	}
}
