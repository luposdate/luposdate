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
package lupos.event.broker.distributed.model;

import java.util.LinkedList;
import java.util.List;

import lupos.event.communication.SerializingMessageService;

/**
 * This class represents a SubBroker which is registered in
 * the master broker class. It will communicate with the
 * broker clients to recieve data such as connected producers
 * or subscriptions
 * @author Kevin
 *
 */
public class SubBrokerData {

	private static int INT_COUNTER = 0;
	
	private SerializingMessageService msgService;
	private int subID;
	private List<BProducer> registeredProducers;
	private List<BConsumer> registeredConsumers;
	
	/**
	 * Constructs a new subbroker object which is
	 * initialized by a message service to the master broker
	 * @param service the connection to the master
	 */
	public SubBrokerData(SerializingMessageService service){
		this.msgService = service;
		this.subID = INT_COUNTER++;

		this.registeredProducers = new LinkedList<BProducer>();
		this.registeredConsumers = new LinkedList<BConsumer>();
	}
	
	public int getID(){
		return this.subID;
	}
	
	public SerializingMessageService getMessageService(){
		return this.msgService;
	}
	
	/**
	 * Gets a list of all events which come
	 * directly from connected producers to
	 * this sub broker
	 * @return a list of all incoming
	 * event types
	 */
	public List<String> getProducedEvents(){
		List<String> events = new LinkedList<String>();
		for (BProducer producer : this.registeredProducers){
			events.add(producer.getProducedEvent());
		}
		return events;
	}
	
	/**
	 * Gets a list of all events which
	 * are requested from subscriptions
	 * of connected consumers
	 * @return a list of all requsted
	 * event types
	 */
	public List<String> getConsumedEvents(){
		List<String> queries = new LinkedList<String>();
		for (BConsumer consumer : this.registeredConsumers){
			queries.addAll(consumer.getWantedEvents());
		}
		return queries;
	}
	
	public void setRegisteredProducers(List<BProducer> producers){
		this.registeredProducers = producers;
	}
	
	public void setRegisteredConsumers(List<BConsumer> consumers){
		this.registeredConsumers = consumers;
	}
	
	public List<BProducer> getRegisteredProducers(){
		return this.registeredProducers;
	}
	
	public List<BConsumer> getRegisteredConsumers(){
		return this.registeredConsumers;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof SubBrokerData){
			return ((SubBrokerData)o).getMessageService().getConnectionInfo().equals(this.msgService.getConnectionInfo());
		}
		return false;
	}
}
