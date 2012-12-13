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
package lupos.event.pubsub;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import lupos.datastructures.queryresult.QueryResult;
import lupos.event.SerializedQueryResult;
import lupos.event.communication.*;
import lupos.event.util.TimedWrapper;

/**
 * Client of the publish/subscribe-architecture.
 */
public class PubSubClient extends Observable implements IMessageReceivedHandler<Serializable> {
	
	private final SerializingMessageService msgService;
	
	/**
	 * <Subscription-id, Subscription>
	 */	
	private final Map<String, Subscription> subscriptions;
	
	/**
	 * <Subscription, list-of-results>
	 */
	private final Map<Subscription, List<TimedWrapper<QueryResult>>> queryResults;
	
	private QueryResultReceivedHandlerList queryResultReceivedHandlers;
	

	public PubSubClient(SerializingMessageService msgService) {
		this.subscriptions = new HashMap<String, Subscription>();
		this.queryResults = new HashMap<Subscription, List<TimedWrapper<QueryResult>>>();
		this.queryResultReceivedHandlers = new QueryResultReceivedHandlerList();
		this.msgService = msgService;
		this.msgService.addHandler2(this);
	}
	
	public void addHandler(IQueryResultReceivedHandler handler) {
		this.queryResultReceivedHandlers.add(handler);
	}
	
	@Override
	public void messageReceived(Object src, Serializable msg) {
		if(msg instanceof SerializedQueryResult) {			
			SerializedQueryResult sqr = (SerializedQueryResult)msg;
			// only store result if we have a associated subscription-id
			Subscription sub = this.subscriptions.get(sqr.getId());
			QueryResult qr = sqr.getQueryResult();
			List<TimedWrapper<QueryResult>> l = this.queryResults.get(sub);
			if(l != null) {
//				Logging.logger.info("QueryResult received: " + sqr.getQueryResult());
				TimedWrapper<QueryResult> w = new TimedWrapper<QueryResult>(qr);
				l.add(w);
				
				// changed!
				super.setChanged();
				super.notifyObservers(sub);
				// notify handlers
				this.queryResultReceivedHandlers.callAll(qr, sub);
			}
		}
	}

	/**
	 * Sends a subscription via the underlying message service to the broker.
	 * @param sub
	 * @throws IOException
	 */
	public void subscribe(Subscription sub) throws IOException {
		// store id and subscription itself in a map to be able to lookup a subscription by its id later
		this.subscriptions.put(sub.getId(), sub);
		
		// create a list to store future results for this subscription, 
		this.queryResults.put(sub, new ArrayList<TimedWrapper<QueryResult>>());

		this.msgService.sendMessage(sub);
	}
	
	/**
	 * Returns the QueryResults for a given subscription
	 * @param sub The subscription for which all queryresults should be returned
	 * @return
	 */
	public List<TimedWrapper<QueryResult>> getQueryResults(Subscription sub) {
		return this.queryResults.get(sub);
	}
}
