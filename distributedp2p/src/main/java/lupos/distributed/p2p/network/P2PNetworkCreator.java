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
package lupos.distributed.p2p.network;

import java.net.BindException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import lupos.distributed.p2p.network.impl.Chordless;
import lupos.distributed.p2p.network.impl.EndpointNetwork;
import lupos.distributed.p2p.network.impl.TomP2P;
import lupos.distributed.p2p.network.impl.TomP2P.TomP2P_Peer;
import lupos.distributed.p2p.storage.StorageWithDistributionStrategy;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import de.rwglab.p2pts.DHashService;

/**
 * This is a factory, that creates instances of P2P-Networks which are
 * implemented and registered here.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class P2PNetworkCreator {
	/**
	 * Constant for the P2P network TomP2P, to be used as parameter in
	 * {@link #get(String)}
	 */
	public static final String TOM_P2P = "TomP2P";
	/**
	 * Constant for the P2P network Chord (modified by the ITM), to be used as
	 * parameter in {@link #get(String)}
	 */
	public static final String CHORDLESS = "Chord";
	/**
	 * Constant for the P2P network FreePastry, to be used as
	 * parameter in {@link #get(String)}
	 */
	public static final String PASTRY = "Pastry";
	/**
	 * Constant for the P2P fake network with URL-Endpoints (created by IFIS),
	 *  to be used as parameter in {@link #get(String)}
	 */
	public static final String ENDPOINT_NETWORK = "EndpointNetwork";

	/**
	 * Some constants used for global configuration to instantiate a p2p network
	 * that can be used for the SPARQL-engine
	 *
	 * @author Bjoern
	 *
	 */
	public static class P2PConfigurationConstants {
		/**
		 * Config-key for the port to be used
		 */
		public static final String cPORT = "Port";
		/**
		 * Config-key for the ip of the master, the node should be bootstrapped
		 * to
		 */
		public static final String cMASTER_IP = "MasterIP";
		/**
		 * Config-key for the port of the master's node, the node should be
		 * bootstrapped to
		 */
		public static final String cMASTER_PORT = "MasterPort";
		/**
		 * The path to store the local data of each node (if available in
		 * implementation)
		 */
		public static final String cSTORAGE_PATH = "StoragePath";

		private P2PConfigurationConstants() {
		}
	}

	/**
	 * This is the interface which is to implement, if a new implementation is
	 * created and registered in
	 * {@link P2PNetworkCreator#registerNetworkFactory(String, P2PImplementation)}
	 *
	 * @author Bjoern
	 *
	 */
	public static abstract class P2PImplementation implements
			Callable<AbstractP2PNetwork<?>> {
		/*
		 * the given arguments are stored here
		 */
		private Map<String, Object> arguments;

		/**
		 * Sets arguments, which can be accessed from implementation
		 *
		 * @param arg
		 *            argument list
		 * @return own class
		 */
		public P2PImplementation setArguments(final Map<String, Object> arg) {
			this.arguments = arg;
			return this;
		}

		/**
		 * sets/updates an argument<br>
		 *
		 * @param key
		 *            the config key
		 * @param value
		 *            the new value to be stored in the given key
		 *
		 */
		public void updateArgument(final String key, final Object value) {
			this.arguments.put(key, value);
		}

		/**
		 * returns the arguments, if stored<br>
		 *
		 * @return the arguments
		 */
		public Object getArgument(final String key) {
			return this.arguments.get(key);
		}

		/**
		 * is an argument with given key set?
		 *
		 * @param key
		 *            the key
		 * @return is any object stored?
		 */
		public boolean hasArgument(final String key) {
			return this.arguments.containsKey(key);
		}

		/**
		 * are arguments set?
		 *
		 * @return yes/no
		 */
		public boolean hasArguments() {
			return this.arguments != null && !this.arguments.isEmpty();
		}
	}

	/*
	 * Inits the given implementation of P2P networks
	 */
	static {
		list = new HashMap<>();
		try {
			// register our TomP2P-Implementation
			registerNetworkFactory(TOM_P2P, new P2PImplementation() {
				/*
				 * default port
				 */
				private int port = 4000;
				/*
				 * connecting the peer to a master node?, if yes, port and ip-address
				 * are to be set!
				 */
				private boolean connectWithMaster = false;
				private String mIp = null;
				private Integer mPort = null;
				private String storage = null;

				@Override
				public TomP2P call() throws Exception {
					try {

						if (this.hasArguments()) {
							/*
							 * set the port, if specified via config
							 */
							if (this.hasArgument(P2PConfigurationConstants.cPORT)) {
								this.port = (int) this.getArgument(P2PConfigurationConstants.cPORT);
							}
							/*
							 * sets the path to be used, if disk-storage is used
							 */
							if (this.hasArgument(P2PConfigurationConstants.cSTORAGE_PATH)) {
								this.storage = (String) this.getArgument(P2PConfigurationConstants.cSTORAGE_PATH);
							}
							/*
							 * set the master-peer, if specified via config
							 */
							if (this.hasArgument(P2PConfigurationConstants.cMASTER_IP)
									&& this.hasArgument(P2PConfigurationConstants.cMASTER_PORT)) {
								this.connectWithMaster = true;
								this.mIp = (String) this.getArgument(P2PConfigurationConstants.cMASTER_IP);
								this.mPort = (Integer) this.getArgument(P2PConfigurationConstants.cMASTER_PORT);
							}
						}

						TomP2P_Peer p;
						if (this.connectWithMaster) {
							//set Master peer
							final InetAddress masterPeerAddress = InetAddress
									.getByName(this.mIp);
							p = TomP2P.createPeer(this.port, masterPeerAddress,
									this.mPort);
						} else {
							p = TomP2P.createPeer(this.port);
						}
						if (this.storage != null) {
							//set disk storage, if set
							p.setUsingStorage(true);
							p.setStorage(this.storage);
						}
						return new TomP2P(p);
					} catch (BindException
							| org.jboss.netty.channel.ChannelException e) {
						Logger.getLogger(this.getClass())
								.warn(String
										.format("Port %d already used, so try next one.",
												this.port));
						// try next port, because actual port already bind to
						++this.port;
						if (this.hasArguments()) {
							// set the new port, so update the argument list!
							this.updateArgument(P2PConfigurationConstants.cPORT,
									this.port);
						}
						/*
						 * try next port
						 */
						return this.call();
					}
				}
			});
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
		try {
			// register our Chordless-Implementation
			registerNetworkFactory(CHORDLESS, new P2PImplementation() {
				private int port = 4000;
				/*
				 * connecting the peer to a master node?, if yes, port and ip
				 * are to be set!
				 */
				private boolean connectWithMaster = false;
				private String mIp = null;
				private Integer mPort = null;
				@SuppressWarnings("unused")
				private String storage = null;

				@Override
				public Chordless call() throws Exception {
					try {
						/*
						 * set the port of the peer, if specified via config
						 */
						if (this.hasArguments()) {
							if (this.hasArgument(P2PConfigurationConstants.cPORT)) {
								this.port = (int) this.getArgument(P2PConfigurationConstants.cPORT);
							}
							if (this.hasArgument(P2PConfigurationConstants.cSTORAGE_PATH)) {
								this.storage = (String) this.getArgument(P2PConfigurationConstants.cSTORAGE_PATH);
							}
							/*
							 * set the master-peer, if specified via config
							 */
							if (this.hasArgument(P2PConfigurationConstants.cMASTER_IP)
									&& this.hasArgument(P2PConfigurationConstants.cMASTER_PORT)) {
								this.connectWithMaster = true;
								this.mIp = (String) this.getArgument(P2PConfigurationConstants.cMASTER_IP);
								this.mPort = (Integer) this.getArgument(P2PConfigurationConstants.cMASTER_PORT);
							}
						}
						// create peer and build p2p network
						DHashService p;
						if (this.connectWithMaster) {
							final InetAddress masterPeerAddress = InetAddress
									.getByName(this.mIp);
							p = Chordless.createPeer(this.port++, masterPeerAddress,
									this.mPort);
						} else {
							p = Chordless.createPeer(this.port++);
						}
						return new Chordless(p);
					} catch (final Exception e) {
						// try next port
						++this.port;
						if (this.hasArguments()) {
							this.updateArgument(P2PConfigurationConstants.cPORT,
									this.port);
						}
						return this.call();
					}
				}
			});
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}

		try {
			// register our EndpontManagement-Implementation
			registerNetworkFactory(ENDPOINT_NETWORK, new P2PImplementation() {
				/*
				 * default port
				 */
				private int port = 8080;
				/*
				 * connecting the peer to a master node?, if yes, port and ip
				 * are to be set!
				 */
				private String storage = null;

				@Override
				public EndpointNetwork call() throws Exception {
					try {

						if (this.hasArguments()) {
							/*
							 * set the port, if specified via config
							 */
							if (this.hasArgument(P2PConfigurationConstants.cPORT)) {
								this.port = (int) this.getArgument(P2PConfigurationConstants.cPORT);
							}
							if (this.hasArgument(P2PConfigurationConstants.cSTORAGE_PATH)) {
								this.storage = (String) this.getArgument(P2PConfigurationConstants.cSTORAGE_PATH);
							}
						}

						//in endpoint-network use "storage"-folder, if nothing set
						//individually
						if (this.storage == null) {
							this.storage = Paths.get(".", "storage").toString();
						}

						return new EndpointNetwork(this.storage, this.port);
					} catch (final BindException e) {
						Logger.getLogger(this.getClass())
								.warn(String
										.format("Port %d already used, so try next one.",
												this.port));
						// try next port, because actual port already bind to
						++this.port;
						if (this.hasArguments()) {
							// set the new port, so update the argument list!
							this.updateArgument(P2PConfigurationConstants.cPORT,
									this.port);
						}
						/*
						 * try next port
						 */
						return this.call();
					}
				}
			});
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
		LoggerFactory.getLogger(P2PNetworkCreator.class).debug(
				String.format("Registered P2P-networks: %s",
						Arrays.toString(getKnownImplementations())));
	}



	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.lang.InterruptedException if any.
	 */
	@Deprecated
	public static void main(final String[] args) throws InterruptedException {
		new P2PNetworkCreator();
		while (true) {
			Thread.sleep(1000);
		}
	}

	/**
	 * here we store our registered networks
	 */
	private static Map<String, P2PImplementation> list;

	/**
	 * registers a new p2p-implementation with the specified name and instance
	 *
	 * @param name
	 *            the name of the network
	 * @param func
	 *            the function
	 */
	public static void registerNetworkFactory(final String name,
			final P2PImplementation func) {
		synchronized (list) {
			list.put(name, func);
		}
	}

	/**
	 * Get known implementations of p2p networks
	 *
	 * @return list of p2pnetworks known.
	 */
	public static String[] getKnownImplementations() {
		String[] a = null;
		synchronized (list) {
			a = list.keySet().toArray(new String[list.keySet().size()]);
		}
		return a;
	}

	/**
	 * Returns the registered network via its unique name
	 *
	 * @param name
	 *            the name of the implementation
	 * @return an instance, which can be used in
	 *         {@link lupos.distributed.p2p.storage.StorageWithDistributionStrategy}
	 */
	public static AbstractP2PNetwork<?> get(final String name) {
		return get(name, null);
	}

	/**
	 * Returns the registered network via its unique name
	 *
	 * @param name
	 *            the name of the implementation
	 * @return an instance, which can be used in
	 *         {@link lupos.distributed.p2p.storage.StorageWithDistributionStrategy}
	 * @param arguments a {@link java.util.Map} object.
	 */
	public static AbstractP2PNetwork<?> get(final String name,
			final Map<String, Object> arguments) {
		AbstractP2PNetwork<?> network = null;
		synchronized (list) {
			try {
				// get the implementation
				final P2PImplementation p2pImplementation = list.get(name);
				if (p2pImplementation == null) {
					throw new RuntimeException(String.format(
							"Implementation \"%s\"not found.", name));
				}
				// set arguments, if set
				if (arguments != null && !arguments.isEmpty()) {
					p2pImplementation.setArguments(arguments);
				} else {
					p2pImplementation.setArguments(null);
				}
				// instanciate the p2p implementation
				network = p2pImplementation.call();
			} catch (final Exception e) {
				final RuntimeException re = new RuntimeException(String.format(
						"Error instanciating p2p implementation \"%s\"", name));
				re.addSuppressed(e);
				throw re;
			}
		}
		return network;
	}
}
