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

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * @author groppe
 * @version $Id: $Id
 */
public abstract class ProducerBase implements IMessageReceivedHandler<Serializable>, IDisconnectedHandler {

	/** Constant <code>MIN_INTERVAL=100</code> */
	public static int MIN_INTERVAL = 100;
	private final Literal TIMESTAMP_LITERAL = LiteralFactory.createURILiteralWithoutException("<timestamp>");

	private final SerializingMessageService msgService;
	private TcpConnectInfo connectingSubBroker;
	private final int interval;

	/**
	 * <p>Constructor for ProducerBase.</p>
	 *
	 * @param msgService The message service that the producer should use to communicate.
	 * @param interval Interval in milliseconds
	 */
	public ProducerBase(final SerializingMessageService msgService, final int interval) {

		this.msgService = msgService;
		this.interval = interval;

		// register message listener
		msgService.addHandler2(this);
		msgService.addDisconnectHandler(this);
	}

	/**
	 * Starts the producer. produce
	 */
	public void start() {
		try {
			while (true) {
				final long startTime = System.currentTimeMillis();

				// produce triples (implemented in subclasses)
				final List<List<Triple>> triples = this.produce();

				if (triples != null && !triples.isEmpty()) {
					// adds timestamp triple if not existent
					final List<List<Triple>> timestampedTriples = this.addTimestampTriples(triples);

					// use one message per event:
					for(final List<Triple> listOfTriples: timestampedTriples){
						// serialize triples
						final ArrayList<SerializedTriple> serializedTriples = this.serializeTriples(listOfTriples);
						// send triples to broker
						this.msgService.sendMessage(serializedTriples);
					}
				} else {
					// ignore!
					// System.out.println("Producer.produce returned null");
					System.out.print(".");
				}
				this.waitForEndOfInterval(startTime);
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private void waitForEndOfInterval(final long startTime) throws InterruptedException {
		final long elapsedTime = this.interval - (System.currentTimeMillis() - startTime);
		if(elapsedTime>0){
			Thread.sleep(elapsedTime);
		}
	}


	private List<List<Triple>> addTimestampTriples(final List<List<Triple>> triples) throws URISyntaxException {

		final List<List<Triple>> triples2 = new ArrayList<List<Triple>>();

		for(final List<Triple> listOfTriples: triples){
			triples2.add(this.addTimestampTriple(listOfTriples));
		}

		return triples2;
	}

	/**
	 * Adds a timestamp triple to a list of triples, if it doesn't contain one.
	 * @param triples
	 * @throws URISyntaxException
	 */
	private List<Triple> addTimestampTriple(final List<Triple> triples) throws URISyntaxException {
		for(final Triple t : triples) {
			if(0 == this.TIMESTAMP_LITERAL.compareToNotNecessarilySPARQLSpecificationConform(t.getPredicate())){
				return triples;
			}
		}

		final long timestamp = System.currentTimeMillis() / 1000;
		final Literal obj = Literals.createTyped(timestamp+"", Literals.XSD.LONG);
		final Triple timestampTriple = new Triple(triples.get(0).getSubject(), this.TIMESTAMP_LITERAL, obj);

		final List<Triple> triples2 = new ArrayList<Triple>(triples);
		triples2.add(timestampTriple);
		return triples2;
	}

	private ArrayList<SerializedTriple> serializeTriples(final List<Triple> triples) throws IOException {
		final ArrayList<SerializedTriple> l = new ArrayList<SerializedTriple>();
		for(final Triple t : triples) {
			l.add(new SerializedTriple(t));
		}
		return l;
	}

	/**
	 * <p>fold.</p>
	 *
	 * @param triplesToFold a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	protected static List<List<Triple>> fold(final List<Triple> triplesToFold){
		final List<List<Triple>> result = new LinkedList<List<Triple>>();
		result.add(triplesToFold);
		return result;
	}

	/**
	 * <p>produce.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public abstract List<List<Triple>> produce();

	/**
	 * <p>askForHostOfBroker.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected static String askForHostOfBroker(){
		return JOptionPane.showInputDialog("Enter the host of the broker:", "localhost");
	}

	/**
	 * Establishes a connection to the master broker
	 * and afterwards send a connection request message to it
	 *
	 * @return msgService the message service of the connection
	 */
	protected static SerializingMessageService connectToMaster(){
		// create communication channel
		SerializingMessageService msgService = null;
		try {
			msgService = new SerializingMessageService(TcpMessageTransport.class);
			msgService.connect(new TcpConnectInfo(JOptionPane.showInputDialog("Enter the host IP adress of the MasterBroker:", "localhost"), Integer.parseInt(JOptionPane.showInputDialog("Enter the host port of the MasterBroker:", "4444"))));
			msgService.sendMessage(new ConnectionRequest(ConnectionRequest.REQUESTTYPE_PRODUCER));
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return msgService;
	}

	/** {@inheritDoc} */
	@Override
	public void messageReceived(final Object src, final Serializable msg){
		if (msg instanceof TcpConnectInfo){
			this.connectingSubBroker = (TcpConnectInfo)msg;
			this.msgService.disconnect();
		}
	}

	/**
	 * {@inheritDoc}
	 *
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
			final ConnectionRequest conReq = new ConnectionRequest(ConnectionRequest.REQUESTTYPE_PRODUCER);
			conReq.setPort(TcpMessageTransport.SERVER_PORT);
			this.msgService.sendMessage(conReq);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
