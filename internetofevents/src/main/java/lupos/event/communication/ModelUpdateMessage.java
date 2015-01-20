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
package lupos.event.communication;

import java.io.Serializable;
import java.util.List;

import lupos.event.broker.distributed.model.BConsumer;
import lupos.event.broker.distributed.model.BProducer;

/**
 * This message is used to tell the master
 * broker that the model of a subbroker has been
 * changed and transmits the new model data
 * @author Kevin
 *
 */
public class ModelUpdateMessage implements Serializable {

	private static final long serialVersionUID = -163937057661681038L;
	private List<BProducer> producers;
	private List<BConsumer> consumers;
	
	/**
	 * Construct a new message by filling the data
	 * @param producers the list of producers
	 * @param consumers the list of consumers
	 */
	public ModelUpdateMessage(List<BProducer> producers, List<BConsumer> consumers){
		this.producers = producers;
		this.consumers = consumers;
	}
	
	public List<BProducer> getProducers(){
		return this.producers;
	}
	
	public List<BConsumer> getConsumers(){
		return this.consumers;
	}

}
