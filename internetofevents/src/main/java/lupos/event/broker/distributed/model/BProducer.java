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

import java.io.Serializable;

import lupos.event.communication.TcpConnectInfo;

/**
 * This class encapsulates some data about
 * a producer which is created for each event
 * type which is produced and sent to a broker
 *
 * @author Kevin
 * @version $Id: $Id
 */
public class BProducer implements Serializable{
	
	private static final long serialVersionUID = 5742786742120270728L;
	
	private String producedEvent;
	private TcpConnectInfo tcpInfo;
	
	/**
	 * Constructs a new BProducer by data
	 *
	 * @param producedEvent the string rdf event type
	 * @param tcpHost the connection data of this producer
	 */
	public BProducer(String producedEvent, TcpConnectInfo tcpHost){
		
		this.producedEvent = producedEvent;
		
		this.tcpInfo = tcpHost;
		
		if (this.tcpInfo == null){
			throw new NullPointerException("TcpConnectInfo may not be null");
		}
	}
	
	/**
	 * <p>Getter for the field <code>producedEvent</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getProducedEvent() {
		return this.producedEvent;
	}
	
	/**
	 * <p>getConnectionInfo.</p>
	 *
	 * @return a {@link lupos.event.communication.TcpConnectInfo} object.
	 */
	public TcpConnectInfo getConnectionInfo(){
		return this.tcpInfo;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o){
		if (o instanceof BProducer){
			BProducer prod = (BProducer)o;
			return prod.producedEvent.equals(this.producedEvent) && prod.tcpInfo.equals(this.tcpInfo);
		}
		return false;
	}
}
