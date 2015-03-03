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
package lupos.event.consumer;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.queryresult.QueryResult;
import lupos.event.action.Action;
import lupos.event.communication.ConnectionRequest;
import lupos.event.communication.IDisconnectedHandler;
import lupos.event.communication.IMessageReceivedHandler;
import lupos.event.communication.IMessageTransport;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;
import lupos.event.pubsub.IQueryResultReceivedHandler;
import lupos.event.pubsub.PubSubClient;
import lupos.event.pubsub.Subscription;
import lupos.event.util.TimedWrapper;


/**
 * The consumer component which is used to connect to a broker and submit subscription.
 */
public class Consumer extends Observable implements IMessageReceivedHandler<Serializable>, IQueryResultReceivedHandler, IDisconnectedHandler {

	/**
	 * define which transport type should be used for communication
	 */
	private final Class<? extends IMessageTransport> transportClass = TcpMessageTransport.class;

	/**
	 * the message service that is used for communication
	 */
	private SerializingMessageService msgService = null;
	private PubSubClient pubSubClient;
	private TcpConnectInfo connectingSubBroker;

	private final Map<Subscription, Action> subscriptionActionMap = new HashMap<Subscription, Action>();

	//final List<IResultReceivedHandler> resultReceivedHandlers = new ArrayList<IResultReceivedHandler>();


	public Consumer() throws Exception {
		Bindings.instanceClass = BindingsMap.class;
		this.msgService = new SerializingMessageService(this.transportClass);
	}

	/**
	 * Connects to the master broker and sends
	 * a connection request to get notified about
	 * a possible subbroker connection (this will
	 * be handled in messageRecieved())
	 * @param host the host of the master
	 * @param port the port of the master
	 * @throws Exception if the connection has
	 * not been built properly
	 */
	public void connect(final String host, final int port) throws Exception {
		this.msgService.connect(new TcpConnectInfo(host, port));
		final ConnectionRequest conReq = new ConnectionRequest(ConnectionRequest.REQUESTTYPE_CONSUMER);
		this.msgService.sendMessage(conReq);
		this.msgService.addHandler2(this);
		this.msgService.addDisconnectHandler(this);
		this.pubSubClient = new PubSubClient(this.msgService);
		this.pubSubClient.addHandler(this);

		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * Closes the connection.
	 */
	public void disconnect() {

		this.connectingSubBroker = null;

		this.msgService.disconnect();

		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * Submits a subscription to the broker.
	 * @param s
	 * @throws IOException
	 */
	public void subscribe(final Subscription s, final Action a) throws IOException {
		this.pubSubClient.subscribe(s);
		this.subscriptionActionMap.put(s, a);
	}

	@Override
	public void messageReceived(final Object src, final Serializable msg) {
		if(msg instanceof TcpConnectInfo){
			this.connectingSubBroker = (TcpConnectInfo)msg;
			this.msgService.disconnect();
		}

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
	 * @param s The subscription whose query results should be returned.
	 * @return A list of query results.
	 */
	public List<TimedWrapper<QueryResult>> getQueryResults(final Subscription s) {
		return this.pubSubClient.getQueryResults(s);
	}

	@Override
	public void queryResultReceived(final QueryResult qr, final Subscription sub) {
		System.out.println("QUERY RESULT RECEIVED");
		if(this.subscriptionActionMap.containsKey(sub)) {
			final Action action = this.subscriptionActionMap.get(sub);
			action.execute(qr);
		}
	}

	/**
	 * After a disconnect, this could be a sign for
	 * reconnecting to a new broker. this is done in
	 * this handler method
	 */
	@Override
	public void disconnected() {
		if (this.connectingSubBroker == null) {
			return;
		}

		// Connect to the sub broker when disconnected
		try {
			this.msgService.connect(this.connectingSubBroker);
			final ConnectionRequest conReq = new ConnectionRequest(ConnectionRequest.REQUESTTYPE_CONSUMER);
			conReq.setPort(TcpMessageTransport.SERVER_PORT);
			this.msgService.sendMessage(conReq);
			this.pubSubClient.changeMessageService(this.msgService);
			// resend all former subscriptions
			this.pubSubClient.resendSubscriptions();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}