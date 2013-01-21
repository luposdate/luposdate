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
package lupos.event;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.communication.*;
import lupos.event.pubsub.IQueryResultReceivedHandler;
import lupos.event.pubsub.PubSubClient;
import lupos.event.pubsub.Subscription;
import lupos.event.util.TimedWrapper;


/**
 * The consumer component which is used to connect to a broker and submit subscription.
 */
public class Consumer extends Observable implements IMessageReceivedHandler<Serializable>,
													IQueryResultReceivedHandler {

	/**
	 * define which transport type should be used for communication
	 */
	private final Class<? extends IMessageTransport> transportClass = TcpMessageTransport.class;
	
	/**
	 * the message service that is used for communication
	 */
	private SerializingMessageService msgService = null;
	private PubSubClient pubSubClient;
	
	private Map<Subscription, Action> subscriptionActionMap = new HashMap<Subscription, Action>();
	
	//final List<IResultReceivedHandler> resultReceivedHandlers = new ArrayList<IResultReceivedHandler>();
	
	
	public Consumer() throws Exception {
		Bindings.instanceClass = BindingsMap.class;
		this.msgService = new SerializingMessageService(this.transportClass);
		this.msgService.addHandler2(this);
	}
	
	/**
	 * Connects to a broker.
	 * @param host
	 * @param port
	 * @throws Exception
	 */
	public void connect(String host, int port) throws Exception {				
		this.msgService.connect(new TcpConnectInfo(host, port));
		this.pubSubClient = new PubSubClient(this.msgService);
		this.pubSubClient.addHandler(this);
		
		this.setChanged();
		this.notifyObservers();
	}
	
	/**
	 * Closes the connection.
	 */
	public void disconnect() {
		this.msgService.disconnect();
		
		this.setChanged();
		this.notifyObservers();
	}
	
	/**
	 * Submits a subscription to the broker.
	 * @param s
	 * @throws IOException
	 */
	public void subscribe(Subscription s, Action a) throws IOException {
		this.pubSubClient.subscribe(s);
		this.subscriptionActionMap.put(s, a);
	}

	@Override
	public void messageReceived(Object src, Serializable msg) {
		// just ignore...
	}

	/**
	 * Checks if the consumer is currently connected.
	 * @return true, if connected
	 */
	public boolean isConnected() {
		return this.msgService.isConnected();
	}

	/**
	 * Get the query results for a particular subscription.
	 * @param The subscription whose query results should be returned.
	 * @return A list of query results.
	 */
	public List<TimedWrapper<QueryResult>> getQueryResults(Subscription s) {
		return this.pubSubClient.getQueryResults(s);
	}

	@Override
	public void queryResultReceived(QueryResult qr, Subscription sub) {
		System.out.println("QUERY RESULT RECEIVED");
		if(this.subscriptionActionMap.containsKey(sub)) {
			Action action = this.subscriptionActionMap.get(sub);
			action.execute(qr);
		}
	}
}
