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
package lupos.event.pubsub;

import java.io.Serializable;
import java.util.*;

import lupos.event.communication.*;


/**
 * Server of the publish/subscribe-architecture.
 */
public class PubSubServer extends Observable implements IMessageReceivedHandler<Serializable>, Observer {

	private final List<Subscription> subscriptions;
	private final SubscriptionChangedHandlerList subscriptionChangedHandlers;
	private final SerializingMessageService msgService;
	
	
	public PubSubServer(SerializingMessageService msgService) throws Exception {
		this.subscriptions = new ArrayList<Subscription>();
		this.subscriptionChangedHandlers = new SubscriptionChangedHandlerList();
		this.msgService = msgService;
		this.msgService.addHandler2(this);
		this.msgService.addObserver(this);
	}
	
	public List<Subscription> getSubscriptions() {
		return this.subscriptions;
	}
	
	public SerializingMessageService getMessageService() {
		return this.msgService;
	}

	@Override
	public void messageReceived(Object src, Serializable msg) {
		if(msg instanceof Subscription)
		{
			Subscription sub = (Subscription)msg;
			
			// remove subscription if it's already contained in the internal subscriptions list
			this.subscriptions.remove(sub);
			
			this.subscriptions.add(sub);
			
			// notify all handles about the changed subscription
			this.subscriptionChangedHandlers.callAll(sub, this);
			
			System.out.println("Received Subscription: " + sub.getName());
		}
	}

	public void addHandler(ISubscriptionChangedHandler handler) {
		this.subscriptionChangedHandlers.add(handler);
	}

	@Override
	public void update(Observable o, Object obj) {
		this.setChanged();
		this.notifyObservers();
	}

	public boolean isConnected() {
		return this.msgService.isConnected();
	}
}
