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
import java.util.LinkedList;
import java.util.List;

import lupos.event.communication.TcpConnectInfo;
import lupos.event.pubsub.Subscription;

/**
 * This Class encapsulates some data about
 * a consumer which is created for each subscription
 * which has been registered
 * @author Kevin
 *
 */
public class BConsumer implements Serializable{
	
	private static final long serialVersionUID = -178030887476684969L;
	
	private List<String> wantedEvents = null;
	private Subscription storedSubscription;
	private TcpConnectInfo connectionInfo;

	/**
	 * Constructs a new BConsumer object
	 * by giving it a subscription object
	 * @param sub the subscription
	 */
	public BConsumer(Subscription sub){
		replaceSubscription(sub);
	}
	
	/**
	 * Updates this BConsumer instance by giving
	 * it a new subscription object
	 * @param sub the new or changed subscription object
	 */
	public void replaceSubscription(Subscription sub){
		this.storedSubscription = sub;
		parseQueryIntoEvents(this.storedSubscription.getQuery());
	}
	
	/**
	 * Parses the given query so that
	 * all events are put in the wantedEvents
	 * collection
	 * @param query the native query string
	 */
	private void parseQueryIntoEvents(String query){
		this.wantedEvents = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();
		char[] chars = query.toCharArray();
		boolean begin = false;
		for (char c : chars){
			if (c == '<'){
				begin = true;
			} else if (c == '>'){
				begin = false;
				sb.append(c);
				this.wantedEvents.add(sb.toString());
				sb.delete(0, sb.length());
			}
			
			if (begin){
				sb.append(c);
			}
		}
	}

	/**
	 * Gets a list of all requested events of the
	 * underlying subscription
	 * @return a list of all rdf events
	 */
	public List<String> getWantedEvents() {
		return this.wantedEvents;
	}

	public TcpConnectInfo getConnectionInfo() {
		return this.connectionInfo;
	}

	public void setConnectionInfo(TcpConnectInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}
	
	/**
	 * Gets the underlying subscription object
	 * @return the subscription object
	 */
	public Subscription getSubscription(){
		return this.storedSubscription;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof BConsumer){
			return ((BConsumer)o).storedSubscription.equals(this.storedSubscription);
		}
		return false;
	}
}