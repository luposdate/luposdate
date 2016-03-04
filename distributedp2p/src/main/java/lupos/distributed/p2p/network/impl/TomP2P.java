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
package lupos.distributed.p2p.network.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.distributed.p2p.network.P2PTripleNetwork;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageDisk;
import net.tomp2p.storage.StorageGeneric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of TomP2P as P2P-Network in LuposDate
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class TomP2P extends P2PTripleNetwork {

	/**
	 * Interface for local logging of TomP2P
	 *
	 * @author Bjoern
	 *
	 */
	static interface ITomP2PLog {
		/**
		 * Adds a logging entry
		 *
		 * @param type
		 *            the type of log message
		 * @param logString
		 *            the message of the log entry
		 * @param level
		 *            the level
		 */
		public void log(String type, String logString, int level);
	}

	final static Logger logger = LoggerFactory.getLogger(TomP2P.class);
	final static ITomP2PLog l = new TomP2PLog();
	private static final String TYPE_CREATED = "CREATE";
	private static final String TYPE_FINALIZED = "SHUTDOWN";
	private static final String TYPE_RETRIEVE = "GET";
	private static final String TYPE_REMOVE = "REM";
	private static final String TYPE_ADD = "ADD";
	private static final String TYPE_SEND = "SEND";
	private static final String TYPE_RECEIVED = "RECV";

	private static boolean usingDebugObject = false;

	/**
	 * This object is used to add T (here: {@link Triple}s) to the DHT. But only
	 * if the flag {@code usingDebugObject} is set, this is used. Otherwise the
	 * normal object is added.
	 *
	 * @author Bjoern
	 *
	 * @param <T>
	 *            The type of the value the object is from.
	 */
	public static class TomP2P_Item<T> implements Serializable {
		private static final long serialVersionUID = -5290281075357383053L;
		private String key;
		private T value;

		/**
		 * Gets the stored original value
		 *
		 * @return the value
		 */
		public T getValue() {
			return this.value;
		}

		/**
		 * New item (constructor only used for de-serialization)
		 */
		public TomP2P_Item() {

		}

		/**
		 * New item containing
		 *
		 * @param key
		 *            the key
		 * @param value
		 *            and an value
		 */
		public TomP2P_Item(final String key, final T value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("at %s : %s", this.key, this.value);
		}
	}

	/**
	 * Sets the seperate logging style for the TomP2P network
	 *
	 * @param enabled
	 *            logging enabled?
	 */
	public void setLogFile(final boolean enabled) {
		TomP2PLog.LOG = enabled;
	}

	Peer p = null;

	/**
	 * Creates an new peer on port 4000
	 *
	 * @return the peer
	 * @throws java.io.IOException
	 *             error creating and listening the peer
	 */
	public static TomP2P_Peer createPeer() throws IOException {
		return createPeer(4000);
	}

	/**
	 * Static class holding the peer and extra configurable stuff to use TomP2P.
	 * This instance is to set in {@link TomP2P#TomP2P(TomP2P_Peer)}.
	 *
	 * @author Bjoern
	 *
	 */
	public static class TomP2P_Peer {
		private String storage = null;
		private Peer peer;
		private boolean usingEmptyStorage = true;
		private boolean usingStorage;

		/**
		 * Setups using the file storage
		 *
		 * @param enabled
		 *            enable this option=?
		 */
		public void setUsingStorage(final boolean enabled) {
			this.usingStorage = enabled;
		}

		private TomP2P_Peer() {
		}

		/**
		 * Returns the TomP2P peer (for more or editable config)
		 *
		 * @return the peer
		 */
		public Peer getPeer() {
			return this.peer;
		}

		private void setPeer(final Peer peer) {
			this.peer = peer;
		}

		/**
		 * returns the storage path (filepath) for storing the nodes items
		 *
		 * @return the path
		 */
		public final String getStorage() {
			return this.storage;
		}

		/**
		 * sets the storage path
		 *
		 * @param storage
		 *            the path (or null, if none is set)
		 */
		public void setStorage(final String storage) {
			this.storage = storage;
		}

		/**
		 * Should the peer start with a new storage or use the existing data in
		 * the given storage?
		 *
		 * @return use a clean database storage
		 */
		public final boolean isUsingEmptyStorage() {
			return this.usingEmptyStorage;
		}

		/**
		 * sets the config to use a clean / previous storage
		 *
		 * @param usingEmptyStorage
		 *            the configuration
		 */
		public final void setUsingEmptyStorage(final boolean usingEmptyStorage) {
			this.usingEmptyStorage = usingEmptyStorage;
		}

		/**
		 * Using the file storage (otherwise in memory storage)
		 *
		 * @return yes/no, is used via {@link TomP2P#TomP2P(TomP2P_Peer)}
		 */
		public boolean isUsingStorage() {
			return this.usingStorage;
		}
	}

	/**
	 * Creates a peer with the specified options
	 *
	 * @param port
	 *            the port
	 * @param masterPeerAddress
	 *            the master-ip for discovering
	 * @param masterPeerPort
	 *            the master-port for discovering
	 * @return a peer connection
	 * @throws java.io.IOException
	 *             error creating the new peer
	 */
	public static TomP2P_Peer createPeer(final int port,
			final InetAddress masterPeerAddress, final int masterPeerPort)
			throws IOException {
		final TomP2P_Peer p = new TomP2P_Peer();

		final Random r = new Random();
		final Peer peer;
		try {
			peer = new PeerMaker(new Number160(r)).setPorts(port)
					.makeAndListen();

			final PeerAddress pa = new PeerAddress(Number160.ZERO, masterPeerAddress,
					masterPeerPort, masterPeerPort);
			logger.info(String.format("Client-Node connecting to master: %s",
					pa));

			// Future Discover
			final FutureDiscover futureDiscover = peer.discover()
					.setInetAddress(masterPeerAddress).setPorts(masterPeerPort)
					.start();
			futureDiscover.awaitUninterruptibly();
			logger.info(String.format("Discover of %s %s", pa,
					(futureDiscover.isSuccess()) ? "succeeded" : "failed"));
			// Future Bootstrap - slave
			final FutureBootstrap futureBootstrap = peer.bootstrap()
					.setInetAddress(masterPeerAddress).setPorts(masterPeerPort)
					.start();
			futureBootstrap.awaitUninterruptibly();
			logger.info(String.format("Bootstrap of %s %s", pa,
					(futureDiscover.isSuccess()) ? "succeeded" : "failed"));
			if (futureBootstrap.getBootstrapTo() != null) {
				logger.info("Future Bootstrap to ... all known");
				peer.discover()
						.setPeerAddress(
								futureBootstrap.getBootstrapTo().iterator()
										.next()).start().awaitUninterruptibly();
			}
		} catch (java.net.BindException
				| org.jboss.netty.channel.ChannelException e) {
			e.printStackTrace();
			throw new java.net.BindException(
					"Peer allready online on that port.");
		}
		p.setPeer(peer);
		return p;
	}

	/**
	 * Creates an new peer
	 *
	 * @param port
	 *            the port, where to listen to the peer
	 * @return the peer
	 * @throws java.io.IOException
	 *             error creating and listening the peer
	 */
	public static TomP2P_Peer createPeer(final int port) throws IOException {
		final TomP2P_Peer p = new TomP2P_Peer();

		final Random r = new Random();
		final Peer peer;
		try {

			if (masterPeer != null) {
				peer = new PeerMaker(new Number160(r)).setPorts(port)
						.makeAndListen();
				final FutureBootstrap res = peer.bootstrap()
						.setPeerAddress(masterPeer.getPeerAddress()).start();
				res.awaitUninterruptibly();

			} else {
				peer = new PeerMaker(new Number160(r)).setPorts(port)
						.makeAndListen();
			}

			// Only if using the internet:
			// peer.getConfiguration().setBehindFirewall(true);
		} catch (java.net.BindException
				| org.jboss.netty.channel.ChannelException e) {
			e.printStackTrace();
			throw new java.net.BindException(
					"Peer already online on that port.");
		}
		p.setPeer(peer);
		return p;
	}

	static Peer masterPeer = null;

	/**
	 * New P2P-Adapter with the specified peer.
	 *
	 * @param pp
	 *            the peer
	 * @throws java.io.IOException if any.
	 */
	public TomP2P(final TomP2P_Peer pp) throws IOException {
		if (masterPeer == null) {
			masterPeer = pp.getPeer();
		}
		l.log(TYPE_CREATED,
				String.format("New Peer created: %s", pp.getPeer()), 10);
		this.p = pp.getPeer();

		/* wait for incoming messages and fire them */
		this.p.setObjectDataReply(new ObjectDataReply() {
			@Override
			public Object reply(final PeerAddress sender, final Object request)
					throws Exception {
				if (request instanceof String) {
					final String from = TomP2P.this.toHex(sender.toByteArray());
					final String message = (String) request;
					l.log(TYPE_RECEIVED, String.format(
							"Peer %s receives message: %s from %s",
							TomP2P.this.p, message, from), 10);
					TomP2P.this.onMessage(message, from);
					return null;
				}
				System.err.println("unknwon type");
				return null;
			}
		});
		this.p.getConfiguration().setConnectTimeoutMillis(20000);

		/*
		 * get the path of the location for storage
		 */
		Path path = null;
		if (pp.getStorage() == null) {
			path = Paths.get("storage", "node_data", this.p.getP2PID() + "");
		} else {
			path = new File(pp.getStorage()).toPath();
		}
		/*
		 * and clean, if new session started and data should be zero
		 */
		if (pp.isUsingEmptyStorage()) {
			logger.info("Cleaning storage folder...");
			deleteDirectory(path.toFile());
		}
		/*
		 * create directory
		 */

		final Peer peer = this.p;

		if (pp.isUsingStorage()) {
			/*
			 * init the storage
			 */
			Files.createDirectories(path);
			final String file = path.toString();
			final StorageGeneric storage = new StorageDisk(file);
			peer.getPeerBean().setStorage(storage);
			logger.info(String.format("Using TomP2P with file-storage on %s",
					file));
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		l.log(TYPE_FINALIZED, String.format("Peer %s shutdown.", this.p), 10);
		logger.debug(String.format("Shutdown of peer: %s on port %i",
				this.p.getP2PID(), this.p.getPeerAddress().portTCP()));
		this.p.shutdown();
		super.finalize();
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String locationKey) {
		return !this.get(locationKey).isEmpty();
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<Triple> get(final String locationKey) {
		l.log(TYPE_RETRIEVE,
				String.format("Peer %s: get keys: %s", this.p, locationKey), 10);
		final Number160 hash = Number160.createHash(locationKey);
		FutureDHT request;
		try {
			request = this.p.get(hash).setRequestP2PConfiguration(this.reqParam).setAll()
					.start();
			request.awaitUninterruptibly();
			final Data f = request.getData();
			if (!request.isSuccess() || f == null) {
				logger.debug(String.format("Got no triple in key \"%s\" (%s)",
						locationKey, hash));
				return new ArrayList<Triple>(0);
			}

			final List<Triple> result = new ArrayList<Triple>();
			for (final Data d : request.getDataMap().values()) {
				logger.debug(String.format(
						"Got triple \"%s\" in key \"%s\" (%s) at peer \"%s\"",
						d.getObject(), locationKey, hash, d.getPeerId()));
				try {
					Triple t;
					if (d.getObject() instanceof TomP2P_Item) {
						t = ((TomP2P_Item<Triple>) d.getObject()).getValue();
					} else if (d.getObject() instanceof Triple) {
						t = (Triple) d.getObject();
					} else {
						throw new ClassCastException(d.getObject().getClass()
								.getName());
					}
					result.add(t);
				} catch (final ClassCastException e) {
					// ignore
					logger.error("Unknown data: " + d.getObject(), e);
				}
			}
			if (result.isEmpty() || result.isEmpty()) {
				logger.debug(String.format("Got no triple in key \"%s\" (%s)",
						locationKey, hash));
			}
			return result;
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		return new ArrayList<Triple>(0);
	}

	static private boolean deleteDirectory(final File path) {
		if (path.exists()) {
			final File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String locationKey, final Triple item) {
		final Number160 hash = Number160.createHash(locationKey);
		FutureDHT request;
		l.log(TYPE_REMOVE, String.format("Peer %s: remove item %s in key: %s",
				this.p, item, locationKey), 10);
		final Number160 contentKey = Number160.createHash(item.toString());
		request = this.p.remove(hash).setContentKey(contentKey)
				.setReturnResults(false).start();
		request.awaitUninterruptibly();
		logger.debug(String.format("Remove triple in key %s (%s)", locationKey,
				hash));
	}

	/** {@inheritDoc} */
	@Override
	public void removeAll(final String locationKey, final Triple... item) {
		final Number160 hash = Number160.createHash(locationKey);
		FutureDHT request;
		final Set<Number160> contentKeys = new HashSet<Number160>();
		for (final Triple t : item) {
			final Number160 contentKey = Number160.createHash(t.toString());
			contentKeys.add(contentKey);
		}
		l.log(TYPE_REMOVE, String.format(
				"Peer %s: removed %d  item in key: %s", this.p, contentKeys.size(),
				locationKey), 10);
		request = this.p.remove(hash).setContentKeys(contentKeys)
				.setReturnResults(false).start();
		request.awaitUninterruptibly();
		logger.debug(String.format("Remove %d triples in key %s (%s)",
				contentKeys.size(), locationKey, hash));
	}

	/**
	 * Request parameter for TomP2P, so no duplicates and maximal two tries on
	 * error
	 */
	RequestP2PConfiguration reqParam = new RequestP2PConfiguration(1, 2, 0);

	/** {@inheritDoc} */
	@Override
	public void add(final String locationKey, final Triple t) {
		l.log(TYPE_ADD, String.format("Peer %s: add item %s in key: %s", this.p, t,
				locationKey), 10);
		final Number160 hash = Number160.createHash(locationKey);
		final Number160 contentKey = Number160.createHash(t.toString());
		FutureDHT request;
		try {
			Data valueToAdd;
			if (usingDebugObject) {
				valueToAdd = new Data(new TomP2P_Item<Triple>(locationKey, t));
			} else {
				valueToAdd = new Data(t);
			}
			request = this.p.put(hash).setData(contentKey, valueToAdd)
					.setRequestP2PConfiguration(this.reqParam).start();
			request.awaitUninterruptibly();
			logger.debug(String.format("Insert triple %s in key %s (%s)", t,
					locationKey, hash));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void addAll(final String locationKey, final Triple... values) {
		final Number160 hash = Number160.createHash(locationKey);
		final Map<Number160, Data> map = new HashMap<Number160, Data>();
		for (final Triple t : values) {
			Data valueToAdd = null;
			final Number160 contentKey = Number160.createHash(t.toString());
			try {
				if (usingDebugObject) {
					valueToAdd = new Data(new TomP2P_Item<Triple>(locationKey,
							t));
				} else {
					valueToAdd = new Data(t);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			map.put(contentKey, valueToAdd);
		}
		final FutureDHT request = this.p.put(hash).setDataMap(map)
				.setRequestP2PConfiguration(this.reqParam).start();
		request.awaitUninterruptibly();
		logger.debug(String.format("Insert %d triple(s) in key %s (%s)",
				map.size(), locationKey, hash));
	}

	/*
	Not used anoymore, only for debugging, because other information
	can be stored with the Triple, such as the key
	*/
	/**
	 * <p>old_addAll.</p>
	 *
	 * @param locationKey a {@link java.lang.String} object.
	 * @param values a {@link lupos.datastructures.items.Triple} object.
	 */
	@Deprecated
	public void old_addAll(final String locationKey, final Triple... values) {
		final Number160 hash = Number160.createHash(locationKey);
		final Set<Data> list = new HashSet<Data>();
		for (final Triple t : values) {
			Data valueToAdd = null;
			try {
				if (usingDebugObject) {
					valueToAdd = new Data(new TomP2P_Item<Triple>(locationKey,
							t));
				} else {
					valueToAdd = new Data(t);
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			list.add(valueToAdd);
		}
		final FutureDHT request = this.p.add(hash).setDataSet(list)
				.setRequestP2PConfiguration(this.reqParam).start();
		request.awaitUninterruptibly();
		logger.debug(String.format("Insert %d triple(s) in key %s (%s)",
				list.size(), locationKey, hash));
	}

	/** {@inheritDoc} */
	@Override
	public void sendMessage(final String key, final String message) {
		l.log(TYPE_SEND, String.format("Peer %s sends message: %s to key %s",
				this.p, message, key), 10);
		final Number160 locKey = Number160.createHash(key);
		final RequestP2PConfiguration reqParam = new RequestP2PConfiguration(1, 1, 0);
		this.p.send(locKey).setObject(message).setRefreshSeconds(0)
				.setDirectReplication(false)
				.setRequestP2PConfiguration(reqParam).start();
	}

	/*
	 * from hex string to a byte array
	 */
	private byte[] toByteArray(final String hexString) {
		// old: byte[] by = new BigInteger(hexString, 16).toByteArray();
		final int len = hexString.length();
		final byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
					.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

	/*
	 * a byte array as hex string
	 */
	private String toHex(final byte[] bytes) {
		final StringBuilder builder = new StringBuilder();
		for (final byte b : bytes) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void sendMessageTo(final String peer, final String message) {
		l.log(TYPE_SEND, String.format("Peer %s sends message: %s to peer %s",
				this.p, message, peer), 10);
		final byte[] recipient = this.toByteArray(peer);
		this.p.sendDirect(new PeerAddress(recipient)).setObject(message).start();
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IStorage getLocalStorage(final IDistribution<?> distibution) {
		final TomP2PLocalStorage<?> storage = new TomP2PLocalStorage(this.p.getPeerBean()
				.getStorage()).setDistribution(distibution);
		return storage;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasLocalStorage() {
		/**
		 * TomP2P supports a local storage!
		 */
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void shutdown() {
		this.p.shutdown();
	}

}
