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
package lupos.event.broker.centralized;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.bindings.*;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.evaluators.StreamQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.stream.*;
import lupos.event.communication.*;
import lupos.event.pubsub.*;
import lupos.event.util.Literals;
import lupos.event.util.Utils;


/**
 * This is the central component which acts as a server. It manages the subscriptions from consumers and the stream-based evaluation of their queries.
 */
public class Broker implements IMessageReceivedHandler<Serializable>, ISubscriptionChangedHandler, Observer {
	
	/**
	 * used to generate really unique blank nodes
	 */
	private long running_number = 0;

	/**
	 * A list of all current subscribers (consumers)
	 */
	private final List<PubSubServer> subscribers = new ArrayList<PubSubServer>();
	
	/**
	 * Holds pairs of subscriptions and their corresponding streams.
	 */
	private final Map<Subscription, Stream> streamMap = new HashMap<Subscription, Stream>();
		

	/**
	 * Starts the broker and waits for new connections in an infinite loop.
	 * @throws Exception
	 */
	public void start() throws Exception {
		while(true) {
			// create communication endpoint and wait for an incoming connection 
			SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);			
			msgService.addHandler2(this);
			msgService.waitForConnection();

			// create PubSubServer which uses the new connection
			PubSubServer server = new PubSubServer(msgService);
			server.addHandler(this);
			server.addObserver(this);
			
			this.subscribers.add(server);
		}
	}
	

	/**
	 * Callback method for {@link SerializingMessageService} which gets called when a message is received.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void messageReceived(Object src, Serializable msg){
		// check if the received message is a generic List which contains SerializedTriple instances
		if(Utils.isHomogenousList(msg, SerializedTriple.class)) {
			List<SerializedTriple> stl = (List<SerializedTriple>)msg;
						
			// -deserialize the triples and make subjects unique
			// -das type-Tripel wird als letztes eingespeist
			Literal subject = LiteralFactory.createAnonymousLiteral("_:broker"+this.running_number);
			this.running_number++;
			System.out.println(subject);
			Triple typeTriple = null;
			for(SerializedTriple st : stl) {
				Triple t = st.getTriple();				
				Triple nt = new Triple(subject, t.getPredicate(), t.getObject());
				//System.out.println("B:" + nt);
				
				if(nt.getPredicate().compareToNotNecessarilySPARQLSpecificationConform(Literals.RDF.TYPE) == 0) {
					typeTriple = nt;
				} else {
					consumeForAllStreams(nt);
				}
			}
			if(typeTriple != null)
				consumeForAllStreams(typeTriple);
			
			System.out.println("Received Triple-List: " + stl.size() + " triples");
		}
	}
	
	/**
	 * Callback method for {@link PubSubServer} which gets called when a subscription message was received.
	 */
	@Override
	public void subscriptionChanged(Subscription sub, PubSubServer server) {
		// removes stream if the subscription is not new
		removeStream(sub);
		
		// create new stream for the subscription
		Stream stream = createStream(sub, server.getMessageService());
		this.streamMap.put(sub, stream);
	}

	/**
	 * Creates a new stream
	 * @param sub The subscription for which the stream will be created.
	 * @param msgService
	 * @return
	 */
	private Stream createStream(final Subscription sub, final SerializingMessageService msgService) {
			
		try {
			final StreamQueryEvaluator evaluator = new StreamQueryEvaluator();
			
			final NotifyStreamResult notifyStreamResult = new NotifyStreamResult() {
				private final StreamQueryEvaluator e = evaluator;
				@Override
				public void notifyStreamResult(final QueryResult result) {
					// empty query results get discarded
					if(result==null || result.isEmpty()) {
						System.out.println("emtpy QueryResult, not sending it");
						return;
					}
					// serializing and sending the query result
					try {
						Set<Variable> vars = this.e.getVariablesOfQuery();
						SerializedQueryResult r = new SerializedQueryResult(vars, result, sub.getId());
						msgService.sendMessage(r);
					} catch (IOException e1) {
						System.err.println(e1);
						e1.printStackTrace();
					}
				}
			};

			evaluator.setupArguments();
			Bindings.instanceClass = BindingsMap.class;
			evaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
			evaluator.getArgs().set("codemap", LiteralFactory.MapType.HASHMAP);
			evaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.HASHMAP);
			evaluator.init();
	
			evaluator.compileQuery(sub.getQuery());
			evaluator.logicalOptimization();
			evaluator.physicalOptimization();
			if (evaluator.getRootNode() instanceof Stream) {
				Stream stream = (Stream) evaluator.getRootNode();
				stream.addNotifyStreamResult(notifyStreamResult);
				stream.sendMessage(new StartOfEvaluationMessage());
				return stream;
			}
			
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * if a stream for the given subscription exists, its evaluation is ended and it gets removed
	 * @param sub
	 */
	private void removeStream(Subscription sub) {
		Stream stream = this.streamMap.get(sub);
		if(stream != null) {
			stream.sendMessage(new EndOfEvaluationMessage());
			this.streamMap.remove(sub);
		}
	}
	
	/**
	 * Adds a triple to all streams.
	 */
	private void consumeForAllStreams(Triple t) {
		for(Stream stream : this.streamMap.values()){
			System.out.println("B: Consuming "+t);
			stream.consume(t);
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof PubSubServer) {
			PubSubServer server = (PubSubServer)o;
			if(!server.isConnected()) {
				System.out.println("PubSubServer not connected anymore, removing all associated streams..");
				this.subscribers.remove(server);
				for(Subscription sub : server.getSubscriptions()) 
					removeStream(sub);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Broker broker = new Broker();
		broker.start();
	}
}
