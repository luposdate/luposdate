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
package lupos.event.producer;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.event.communication.SerializingMessageService;
import lupos.event.util.Literals;
import lupos.event.util.Utils;

/**
 * Searches twitter with a keyword and creates an event every a new tweet is found.
 *
 */
public class TwitterSearchProducer extends ProducerBaseNoDuplicates {
	
	public static final String NAMESPACE = "http://localhost/events/TwitterSearch/";
	private final static int INTERVAL = 3000;
	private static final String SEARCH_URL = "http://search.twitter.com/search.json?q=";
	private String SEARCH_QUERY = "weather";
	
	private final Literal TWEET_TYPE_OBJECT = Literals.createURI(NAMESPACE, "TwitterSearchEvent");
	private final Literal TEXT_PREDICATE = Literals.createURI(NAMESPACE, "text");
	private final Literal ID_PREDICATE = Literals.createURI(NAMESPACE, "id");
	
	/**
	 * Remember id of last published tweet to be able to publish only newer tweets
	 */
	private long max_id = 0;
		
	
	public TwitterSearchProducer(SerializingMessageService msgService) {
		super(msgService, INTERVAL);
	}
	
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {			
			// search for query via twitter search API
			String jsonStr = Utils.httpGet(SEARCH_URL + URLEncoder.encode(this.SEARCH_QUERY, "UTF-8") + "&since_id="+this.max_id);
			System.out.println(jsonStr);

			// get results list from json-response
			JSONObject rootObj = new JSONObject(jsonStr);
			this.max_id = rootObj.getLong("max_id");
			JSONArray resultsArray = rootObj.getJSONArray("results");
			
			System.out.println(resultsArray.length() + " new tweets");
			
			List<List<Triple>> result = new ArrayList<List<Triple>>();

			// create an event instance for each found tweet
			for(int i=0; i<resultsArray.length(); i++) {				
				List<Triple> triples = new ArrayList<Triple>();
				JSONObject jsonObj = resultsArray.getJSONObject(i);
				long id = jsonObj.getLong("id");

				// String from_user = jsonObj.getString("from_user");
				String text = jsonObj.getString("text");

				Literal subject = LiteralFactory.createAnonymousLiteral("<"+id+">");

				triples.add(new Triple(subject, Literals.RDF.TYPE, this.TWEET_TYPE_OBJECT));
				triples.add(new Triple(subject, this.TEXT_PREDICATE, LiteralFactory.createStringLiteral("\""+Utils.escape(text)+"\"")));
				triples.add(new Triple(subject, this.ID_PREDICATE, LiteralFactory.createTypedLiteral("\""+id+"\"", Literals.XSD.LONG)));
				
				result.add(triples);
			}			
			return result;			
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		TwitterSearchProducer tsp = new TwitterSearchProducer(msgService);
		
		tsp.SEARCH_QUERY = JOptionPane.showInputDialog("Enter keyword to be searched for on twitter:", tsp.SEARCH_QUERY);
		
		// start producer
		tsp.start();
	}
}
