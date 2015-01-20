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

import java.util.*;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.event.communication.*;
import lupos.event.util.Literals;
import lupos.event.util.Utils;

import org.json.JSONObject;



/**
 * Produces events which contain current Bitcoin-related values gathered from Mt.Gox.
 *
 */
public class MtGoxProducer extends ProducerBaseNoDuplicates {

	public static final String NAMESPACE = "http://localhost/events/MtGox/";
	private static final int INTERVAL = 10000;
	
	private static final String API_BASE_URL = "https://mtgox.com/api/1/"; 
	
	private static enum Currency {
		EUR { 
			@Override
			public String toString() { 
				return "EUR"; 
			} 
		},
		USD { 
			@Override
			public String toString() { 
				return "USD";
			} 
		}
	}
	
	private final Currency currency = Currency.EUR;
	
	
	static class Predicates {
		public static final Literal TYPE = Literals.createURI(NAMESPACE, "MtGoxEvent");
		public static final Literal LAST_TRADE = Literals.createURI(NAMESPACE, "lastTrade");
		public static final Literal BEST_BID = Literals.createURI(NAMESPACE, "bestBid");
		public static final Literal BEST_ASK = Literals.createURI(NAMESPACE, "bestAsk");
	}
	
	public MtGoxProducer(SerializingMessageService msgService) throws Exception {
		super(msgService, INTERVAL);	
	}	
	
	@Override
	public List<List<Triple>> produceWithDuplicates() {
		try {
			String url = API_BASE_URL + "BTC" + this.currency + "/ticker";
			String jsonStr = Utils.httpGet(url);

			// get values from json response
			JSONObject rootObj = new JSONObject(jsonStr);
			JSONObject returnObj = rootObj.getJSONObject("return");

			String lastLocalValue = returnObj.getJSONObject("last_local").getString("value");
			String sellValue = returnObj.getJSONObject("buy").getString("value");
			String buyValue = returnObj.getJSONObject("sell").getString("value");

			List<Triple> triples = new ArrayList<Triple>();
			
			// create triples
			final Literal lastTradeObject = Literals.createTyped(lastLocalValue, Literals.XSD.DECIMAL);
			final Literal bestBidObject = Literals.createTyped(sellValue, Literals.XSD.DECIMAL);		
			final Literal bestAskObject = Literals.createTyped(buyValue, Literals.XSD.DECIMAL);
			
			triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Literals.RDF.TYPE, Predicates.TYPE));
			triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.LAST_TRADE, lastTradeObject));
			triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.BEST_BID, bestBidObject));
			triples.add(new Triple(Literals.AnonymousLiteral.ANONYMOUS, Predicates.BEST_ASK, bestAskObject));

			return ProducerBase.fold(triples);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		// create communication channel
		SerializingMessageService msgService = ProducerBase.connectToMaster();
		
		// start producer
		new MtGoxProducer(msgService).start();
	}
}
