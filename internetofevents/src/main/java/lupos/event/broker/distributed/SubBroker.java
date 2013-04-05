/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.event.broker.distributed;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.swing.JOptionPane;

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
import lupos.event.broker.distributed.model.BConsumer;
import lupos.event.broker.distributed.model.BProducer;
import lupos.event.broker.distributed.model.IModelChangedListener;
import lupos.event.broker.distributed.model.Model;
import lupos.event.communication.*;
import lupos.event.pubsub.*;
import lupos.event.util.Literals;
import lupos.event.util.Utils;


/**
 * This is the central component which acts as a server. It manages the subscriptions from consumers and the stream-based evaluation of their queries.
 */
public class SubBroker implements IMessageReceivedHandler<Serializable>, ISubscriptionChangedHandler, Observer,
																	IModelChangedListener, IDisconnectedHandler, Runnable{

	/**
	 * A list of all current subscribers (consumers)
	 */
	private final List<PubSubServer> subscribers = new ArrayList<PubSubServer>();
	
	/**
	 * The connected model of this broker
	 */
	private final Model bModel = new Model();
	
	/**
	 * Holds pairs of subscriptions and their corresponding streams.
	 */
	private final Map<Subscription, Stream> streamMap = new HashMap<Subscription, Stream>();
	
	/**
	 * Holds all subscriptions and their correpsonding BConsumers
	 */
	private final Hashtable<Subscription, BConsumer> subTable = new Hashtable<Subscription, BConsumer>();
	
	/**
	 * Holds pairs of event type string and their corresponding forward addresses
	 */
	private Hashtable<String, List<TcpConnectInfo>> forwardingTable;
	
	/**
	 * Holds pairs of forward addresses and their corresponding message services
	 */
	private final Hashtable<TcpConnectInfo, SerializingMessageService> connectionTable = new Hashtable<TcpConnectInfo, SerializingMessageService>();
	
	private SerializingMessageService msgService;

	/**
	 * Starts the broker and waits for new connections in an infinite loop.
	 * @throws Exception
	 */
	public void start() throws Exception {
		// add the model changed listener
		this.bModel.addModelChangeListener(this);
		// start the connection awareness ping thread
		new Thread(this).start();
		
		while(true) {
			// create communication endpoint and wait for an incoming connection 
			SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);			
			msgService.addHandler2(this);
			msgService.waitForConnection();
		}
	}
	
	/**
	 * This thread method handles a ping to all connected
	 * pubsub server to force the message service to be
	 * closed if the conenction broke
	 */
	@Override
	public void run(){
		while (true){
			// pings the consumer clients to force disconnected()
			// - calls on the message service if conenction lost
			for (PubSubServer server : this.subscribers){
				try{
					server.getMessageService().sendMessage(0);
				} catch (IOException ioe){
				}
			}
			try{
				Thread.sleep(3000);
			} catch (InterruptedException ie){
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Connects to the master broker and afterwards sends
	 * a connection request message to it
	 */
	public void connectToMaster(){
		try {
			this.msgService = new SerializingMessageService(TcpMessageTransport.class);
			this.msgService.connect(new TcpConnectInfo(JOptionPane.showInputDialog("Enter the host IP adress of the MasterBroker:", "localhost"), Integer.parseInt(JOptionPane.showInputDialog("Enter the host port of the MasterBroker:", "4444"))));
			this.msgService.addHandler2(this);
			ConnectionRequest conReq = new ConnectionRequest(ConnectionRequest.REQUESTTYPE_BROKER);
			conReq.setPort(TcpMessageTransport.SERVER_PORT);
			this.msgService.sendMessage(conReq);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Callback method for {@link SerializingMessageService} which gets called when a message is received.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(Object src, Serializable msg){
		SerializingMessageService service = (SerializingMessageService) src;
		
		// handles incoming connection requests
		if (msg instanceof ConnectionRequest){
			ConnectionRequest request = (ConnectionRequest)msg;
			service.setConnectionPort(request.getPort());
			
			// handles consumers incoming
			if (request.getRequestType() == ConnectionRequest.REQUESTTYPE_CONSUMER){
				// create PubSubServer which uses the new connection
				try {
					PubSubServer server = new PubSubServer(service);
					service.addDisconnectHandler(this);
					server.addHandler(this);
					server.addObserver(this);
					this.subscribers.add(server);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		// handles redistribution updates of Clients (redirecting of MasterBroker Messages)
		else if (msg instanceof BrokerUpdateMessageCollector){
			for (BrokerUpdateMessage message : ((BrokerUpdateMessageCollector) msg).getCollectedMessages()){
				BConsumer consumer = message.getConsumer();
				TcpConnectInfo consumerConnect = consumer.getConnectionInfo();
				SerializingMessageService msgServiceConsumer = null;
				for (PubSubServer curPSS : this.subscribers){
					if (curPSS.getMessageService().getConnectionInfo().equals(consumerConnect)){
						msgServiceConsumer = curPSS.getMessageService();
						break;
					}
				}
				try {
					msgServiceConsumer.sendMessage(message.getNewBrokerInfo());
					System.out.println("Redirected Consumer from "+msgServiceConsumer.getConnectionInfo().getHost()+":"
							+msgServiceConsumer.getConnectionInfo().getPort()+" to "+message.getNewBrokerInfo().getHost()
							+":"+message.getNewBrokerInfo().getPort());
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}					
		}
		// handles the Inter-SubBroker-Network-Communication update
		else if (msg instanceof BrokerNetworkUpdateMessage){
			this.forwardingTable = ((BrokerNetworkUpdateMessage)msg).getTable();
			for (List<TcpConnectInfo> tcpList : this.forwardingTable.values()){
				for (TcpConnectInfo tcpInfo : tcpList){
					if (!this.connectionTable.containsKey(tcpInfo)){
						try {
							SerializingMessageService msgService = new SerializingMessageService(TcpMessageTransport.class);
							msgService.connect(tcpInfo);
							this.connectionTable.put(tcpInfo, msgService);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
			}
		}

		// check if the received message is a generic List which contains SerializedTriple instances
		if(Utils.isHomogenousList(msg, SerializedTriple.class)) {
			List<SerializedTriple> stl = (List<SerializedTriple>)msg;
						
			// -deserialize the triples and make subjects unique
			// -das type-Tripel wird als letztes eingespeist
			Literal subject = LiteralFactory.createAnonymousLiteral("_:subj"+UUID.randomUUID());
			Triple typeTriple = null;
			for(SerializedTriple st : stl) {
				Triple t = st.getTriple();				
				Triple nt = new Triple(subject, t.getPredicate(), t.getObject());
				
				// if this triplet contains the event type, then
				// it might be added as a new BProducer
				// and is forwarded to appropriate sub brokers
				if (t.getPredicate().getName().equals("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>")){
					// first check, if this message came from a producer
					// which means that its connection info is not null
					if (service.getConnectionInfo() != null){
						// add a new BProducer, which only does show effect if
						// this specific BProducer did not already exist 
						this.bModel.addBProducer(new BProducer(t.getObject().getName(), service.getConnectionInfo()));
						
						// now just forward this message
						if (this.forwardingTable != null){
							List<TcpConnectInfo> forwardAdresses = this.forwardingTable.get(t.getObject().getName());
							if (forwardAdresses != null){
								for (TcpConnectInfo con : forwardAdresses){
									if (!service.getConnectionInfo().equals(con)){
										try {
											this.connectionTable.get(con).sendMessage(msg);
											System.out.println("Forwarded message of type "+t.getObject().getName()+" to "+con.getHost()+":"+con.getPort());
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
				
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
		// handles this subscription change event for our model
		if (this.subTable.containsKey(sub)){
			// update old bconsumer
			BConsumer consumer = this.subTable.get(sub);
			consumer.replaceSubscription(sub);
			// manually call the handler method
			modelChanged(this.bModel);
		} else{
			// new bconsumer
			BConsumer consumer = new BConsumer(sub);
			consumer.setConnectionInfo(server.getMessageService().getConnectionInfo());
			this.bModel.addBConsumer(consumer);
			this.subTable.put(sub, consumer);
		}
		
		// removes stream if the subscription is not new
		removeStream(sub);
		
		// create new stream for the subscription
		Stream stream = createStream(sub, server.getMessageService());
		this.streamMap.put(sub, stream);
	}

	/**
	 * Creates a new stream
	 * @param sub The subscription for which the stream will be created.
	 * @param msgService_param
	 * @return
	 */
	private Stream createStream(final Subscription sub, final SerializingMessageService msgService_param) {
			
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
						msgService_param.sendMessage(r);
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
				// call disconnected method to handle BConsumer drop
				disconnected();
				// UNCOMMENTED this remove line since disconnected() executes this task
				//this.subscribers.remove(server);
				for(Subscription sub : server.getSubscriptions()) 
					removeStream(sub);
			}
		}
	}

	@Override
	public void modelChanged(Model m) {
		// sends a message to the master broker if
		// this model has been changed
		try {
			this.msgService.sendMessage(m.getUpdateMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will be called after a client
	 * disconnected. In fact, only disconnects by pubsub
	 * clients (so BConsumer objects) are handled and
	 * therefore kicked out of the data structure
	 */
	@Override
	public void disconnected() {
		// handles a disconnect of a consumer
		PubSubServer disonnectedServer;
		do{
			disonnectedServer = null;
			for (PubSubServer server : this.subscribers){
				if (!server.getMessageService().isConnected()){
					disonnectedServer = server;
					break;
				}
			}
			if (disonnectedServer != null){
				// drop this disconnected server
				this.subscribers.remove(disonnectedServer);
				for (Subscription sub : disonnectedServer.getSubscriptions()){
					this.subscribers.remove(disonnectedServer);
					List<BConsumer> toDrop = new ArrayList<BConsumer>();
					for (BConsumer cons : this.bModel.getBConsumers()){
						if (cons.getSubscription().equals(sub)){
							toDrop.add(cons);
						}
					}
					for (BConsumer drop : toDrop){
						this.bModel.removeBConsumer(drop);
						System.out.println("Dropped a Consumer");
					}
				}
			}
		} while (disonnectedServer != null);
	}
	
	public static void main(String[] args) throws Exception{		

		// Request the listening connection port
		TcpMessageTransport.SERVER_PORT = Integer.parseInt(JOptionPane.showInputDialog("Set Main Broker Server Port", "4445"));		
		
		SubBroker broker = new SubBroker();
		broker.connectToMaster();
		broker.start();
	}
}
