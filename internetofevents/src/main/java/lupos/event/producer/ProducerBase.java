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
package lupos.event.producer;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.*;

import javax.swing.JOptionPane;

import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.event.communication.ConnectionRequest;
import lupos.event.communication.IDisconnectedHandler;
import lupos.event.communication.IMessageReceivedHandler;
import lupos.event.communication.SerializedTriple;
import lupos.event.communication.SerializingMessageService;
import lupos.event.communication.TcpConnectInfo;
import lupos.event.communication.TcpMessageTransport;
import lupos.event.util.Literals;

/**
 * Base class for producers.
 *
 */
public abstract class ProducerBase implements IMessageReceivedHandler<Serializable>, IDisconnectedHandler {

	public static int MIN_INTERVAL = 100;
	private final Literal TIMESTAMP_LITERAL = LiteralFactory.createURILiteralWithoutException("<timestamp>"); 
	
	private final SerializingMessageService msgService;
	private TcpConnectInfo connectingSubBroker;
	private final int interval;
	
	/**
	 * 
	 * @param msgService The message service that the producer should use to communicate.
	 * @param interval Interval in milliseconds
	 */
	public ProducerBase(SerializingMessageService msgService, int interval) {
		
		this.msgService = msgService;
		this.interval = interval;	
		
		// register message listener
		msgService.addHandler2(this);
		msgService.addDisconnectHandler(this);
	}	
	
	/**
	 * Starts the producer. {@link produce} 
	 */
	public void start() {
		try {
			while (true) {
				long startTime = System.currentTimeMillis();

				// produce triples (implemented in subclasses)
				List<List<Triple>> triples = produce();

				if (triples != null && !triples.isEmpty()) {
					// adds timestamp triple if not existent
					List<List<Triple>> timestampedTriples = addTimestampTriples(triples);
					
					// use one message per event:
					for(List<Triple> listOfTriples: timestampedTriples){
						// serialize triples
						ArrayList<SerializedTriple> serializedTriples = serializeTriples(listOfTriples);
						// send triples to broker
						this.msgService.sendMessage(serializedTriples);
					}
				} else {
					// ignore!
					// System.out.println("Producer.produce returned null");
					System.out.print(".");
				}
				waitForEndOfInterval(startTime);
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	private void waitForEndOfInterval(long startTime) throws InterruptedException {
		long elapsedTime = this.interval - (System.currentTimeMillis() - startTime);
		if(elapsedTime>0){
			Thread.sleep(elapsedTime);
		}
	}

	
	private List<List<Triple>> addTimestampTriples(List<List<Triple>> triples) throws URISyntaxException {
		
		List<List<Triple>> triples2 = new ArrayList<List<Triple>>();
		
		for(List<Triple> listOfTriples: triples){
			triples2.add(this.addTimestampTriple(listOfTriples));
		}
		
		return triples2;		
	}
	
	/**
	 * Adds a timestamp triple to a list of triples, if it doesn't contain one.
	 * @param triples
	 * @throws URISyntaxException 
	 */
	private List<Triple> addTimestampTriple(List<Triple> triples) throws URISyntaxException {
		for(Triple t : triples) {
			if(0 == this.TIMESTAMP_LITERAL.compareToNotNecessarilySPARQLSpecificationConform(t.getPredicate())){
				return triples;
			}
		}
		
		long timestamp = System.currentTimeMillis() / 1000;
		Literal obj = Literals.createTyped(timestamp+"", Literals.XSD.LONG);
		Triple timestampTriple = new Triple(triples.get(0).getSubject(), this.TIMESTAMP_LITERAL, obj);
		
		List<Triple> triples2 = new ArrayList<Triple>(triples);
		triples2.add(timestampTriple);
		return triples2;
	}

	private ArrayList<SerializedTriple> serializeTriples(List<Triple> triples) throws IOException {
		ArrayList<SerializedTriple> l = new ArrayList<SerializedTriple>();
		for(Triple t : triples)
			l.add(new SerializedTriple(t));
		return l;
	}
	
	protected static List<List<Triple>> fold(List<Triple> triplesToFold){
		List<List<Triple>> result = new LinkedList<List<Triple>>();
		result.add(triplesToFold);
		return result;
	}

	public abstract List<List<Triple>> produce();
	
	protected static String askForHostOfBroker(){
		return JOptionPane.showInputDialog("Enter the host of the broker:", "localhost");
	}
	
	/**
	 * Establishes a connection to the master broker
	 * and afterwards send a connection request message to it
	 * @return msgService the message service of the connection
	 */
	protected static SerializingMessageService connectToMaster(){
		// create communication channel
		SerializingMessageService msgService = null;
		try {
			msgService = new SerializingMessageService(TcpMessageTransport.class);
			msgService.connect(new TcpConnectInfo(JOptionPane.showInputDialog("Enter the host IP adress of the MasterBroker:", "localhost"), Integer.parseInt(JOptionPane.showInputDialog("Enter the host port of the MasterBroker:", "4444"))));
			msgService.sendMessage(new ConnectionRequest(ConnectionRequest.REQUESTTYPE_PRODUCER));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgService;
	}
	
	@Override
	public void messageReceived(Object src, Serializable msg){
		if (msg instanceof TcpConnectInfo){
			this.connectingSubBroker = (TcpConnectInfo)msg;
			this.msgService.disconnect();
		}
	}
	
	/**
	 * This method will be called after the master
	 * broker has sent a forwarding tcpConnect object.
	 * This method will then connect to this specific
	 * subbroker
	 */
	@Override
	public void disconnected(){
		// Connect to the sub broker when disconnected
		// which should only occur on handshake
		try {
			this.msgService.connect(this.connectingSubBroker);
			ConnectionRequest conReq = new ConnectionRequest(ConnectionRequest.REQUESTTYPE_PRODUCER);
			conReq.setPort(TcpMessageTransport.SERVER_PORT);
			this.msgService.sendMessage(conReq);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
