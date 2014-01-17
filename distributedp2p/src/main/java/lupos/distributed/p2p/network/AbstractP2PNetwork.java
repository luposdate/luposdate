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
package lupos.distributed.p2p.network;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.event.EventListenerList;

import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;

/**
 * Abstract p2p network, that enabled adding, removing and contain-check of data
 * in p2p-network.
 */
public abstract class AbstractP2PNetwork<T extends Serializable> {

	@Deprecated
	public String createHashKey(String key) {
		return key;
	}

	/**
	 * does the network contains the given key?
	 * 
	 * @param locationKey
	 *            the given key
	 * @return contained in network?
	 */
	public boolean contains(String locationKey) {
		return !get(locationKey).isEmpty();
	}

	/**
	 * Does the network contains an object on a given key?
	 * 
	 * @param locationKey
	 *            the given key
	 * @param triples
	 *            the object, which should be stored in given key
	 * @return is this object at the given key available?
	 */
	public boolean contains(String locationKey, T triples) {
		return contains(locationKey);
	}

	/**
	 * Returns a list of objects stored in the given key
	 * 
	 * @param locationKey
	 *            the given key
	 * @return a list of stored object
	 */
	public abstract List<T> get(String locationKey);

	/**
	 * Removes all values, that were stored at the specified key 
	 * @param key the given key
	 * @param values the values to be removed
	 */
	@SuppressWarnings("unchecked")
	public void removeAll(String key, T... values) {
		for (T t : values) {
			remove(key, t);
		}
	}

	/**
	 * Removes an object at the given key
	 * 
	 * @param locationKey
	 *            the key, where the object is available
	 * @param triple
	 *            the object to remove
	 */
	public abstract void remove(String locationKey, T triple);

	/**
	 * Adds an object to the given key
	 * 
	 * @param key
	 *            the key where to store the data
	 * @param value
	 *            the data / object, to be stored
	 */
	public abstract void add(String key, T value);

	/**
	 * Adds a few items under the same given key
	 * 
	 * @param key
	 *            the key where to store the data
	 * @param values
	 *            the data / object, to be stored
	 */
	@SuppressWarnings("unchecked")
	public void addAll(String key, T... values) {
		for (T item : values) {
			add(key, item);
		}
	}

	/**
	 * Sends a P2P-message to the given location key
	 * 
	 * @param key
	 *            the key the message is sent to (not the node-id, only the
	 *            given key-hash, for sending messages to a given peer, use:
	 *            {@link #sendMessageTo(String, String)}
	 * @param message
	 *            the message to sent
	 */
	public abstract void sendMessage(String key, String message);

	/*
	 * this is an event-listener
	 */
	private EventListenerList listeners = new EventListenerList();
	private AtomicInteger atomicCounter = new AtomicInteger(0);

	/**
	 * Adds an event message listener for P2P-messages.
	 * 
	 * @param listener
	 *            the local listener-interface
	 */
	public void addMessageListener(IP2PMessageListener listener) {
		listeners.add(IP2PMessageListener.class, listener);
	}

	/**
	 * Removes an event message listener for P2P-messages.
	 * 
	 * @param listener
	 *            the local listener-interface
	 */
	public void removeMessageListener(IP2PMessageListener listener) {
		listeners.remove(IP2PMessageListener.class, listener);
	}

	/**
	 * If a message receives a node in the P2P network, the event is fired to
	 * all listers, which are registered via
	 * {@link #addMessageListener(IP2PMessageListener)}.
	 * 
	 * @param message
	 *            The text message sent via P2P network
	 * @param from
	 *            the sender (node-id)
	 */
	protected void onMessage(final String message, final String from) {
		/* block empty events */
		/* here you could check with event.getSource() who created the event */
		if (message == null)
			return;

		/* Guaranteed to return a non-null array */
		final Object[] listeners = AbstractP2PNetwork.this.listeners
				.getListenerList();
		/*
		 * Process the listeners last to first, notifying those that are
		 * interested in this event
		 */
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IP2PMessageListener.class) {
				final int _i = i;
				Thread t = new Thread() {
					public void run() {
						((IP2PMessageListener) listeners[_i + 1]).onMessage(
								message, from);
					}
				};
				t.setName("#" + atomicCounter.getAndIncrement()
						+ "-> Subgraph from: " + from);
				t.setDaemon(true);
				t.start();
			}
		}
	}

	/**
	 * If a message receives a node in the P2P network, the event is fired to
	 * all listers, which are registered via
	 * {@link #addMessageListener(IP2PMessageListener)}.
	 * 
	 * @param message
	 *            The input stream message sent via P2P network
	 * @param from
	 *            the sender (node-id)
	 */
	protected void onMessage(final InputStream message, final String from) {
		/* block empty events */
		/* here you could check with event.getSource() who created the event */
		if (message == null)
			return;

		/* Guaranteed to return a non-null array */
		final Object[] listeners = AbstractP2PNetwork.this.listeners
				.getListenerList();
		/*
		 * Process the listeners last to first, notifying those that are
		 * interested in this event
		 */
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IP2PMessageListener.class) {
				final int _i = i;
				((IP2PMessageListener) listeners[_i + 1]).onMessage(message,
						from);
			}
		}
	}

	/**
	 * Sends an internal P2P message to a given peer-id
	 * 
	 * @param peer
	 *            the peer-id (the unique instance of a node, not a content key
	 *            as in {@link #sendMessage(String, String)} )
	 * @param message
	 *            the message to sent
	 */
	public abstract void sendMessageTo(String peer, String message);

	/**
	 * Sends an internal P2P message to a given peer-id
	 * 
	 * @param peer
	 *            the peer-id
	 * @param message
	 *            the message
	 */
	public void sendMessageTo(String peer, InputStream message) {

	}

	/**
	 * Returns the local storage for the P2P implementation if available via
	 * {@link #hasLocalStorage()}
	 * 
	 * @param distibution
	 *            the distribution used for querying
	 * @return the local storage (only content queryable which is stored local
	 *         on this node, not in whole P2P network)
	 */
	public IStorage getLocalStorage(IDistribution<?> distibution) {
		return null;
	}

	/**
	 * Is any local storage available?
	 * 
	 * @return yes/no
	 */
	public boolean hasLocalStorage() {
		return false;
	}

	/**
	 * begin a shutdown of the p2p network, so close connection to the network
	 * and shutdown this node
	 */
	public void shutdown() {
	}

	/**
	 * Does the network supports streaming?
	 * 
	 * @return yes no
	 */
	public boolean supportsStreaming() {
		return false;
	}
}
