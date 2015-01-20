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
package lupos.distributed.p2p.gui;

import java.util.HashMap;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.distributed.p2p.distributionstrategy.SimplePartitionDistribution;
import lupos.distributed.p2p.network.AbstractP2PNetwork;
import lupos.distributed.p2p.network.P2PNetworkCreator;
import lupos.distributed.p2p.query.withsubgraph.P2P_QueryClient_Instanciator;
import lupos.distributed.p2p.query.withsubgraph.P2P_QueryClient_Instanciator.IConfigurator;
import lupos.distributed.p2p.storage.StorageWithDistributionStrategy;
import lupos.distributed.storage.distributionstrategy.tripleproperties.IDistributionKeyContainer;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;

/**
 * This is a helper class creating new peers of different p2p-networks
 *
 * @author Bjoern
 */
public class PeerCreator {

	/**
	 * Creates a new peer on the specified port with TomP2P and hierarchy
	 * distribution
	 *
	 * @param port
	 *            the port
	 * @return global controller for the peer
	 */
	public static PeerCreator getTomP2PNetwork(final int port) {
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, port);
		return new PeerCreator().setP2PNetwork(P2PNetworkCreator.TOM_P2P,
				arguments).setDistributionStrategy(
				new SimplePartitionDistribution());
	}

	/**
	 * This is the constructor without any configuration of the used network or
	 * distribution strategy
	 */
	public PeerCreator() {
	}

	private AbstractP2PNetwork<?> p2p = null;
	private IDistributionKeyContainer<?> distribution = new SimplePartitionDistribution();
	private BasicIndexQueryEvaluator client = null;
	@SuppressWarnings("rawtypes")
	private Class<? extends StorageWithDistributionStrategy> storage = StorageWithDistributionStrategy.class;
	@SuppressWarnings("rawtypes")
	private IConfigurator<StorageWithDistributionStrategy> storageConfiguration;
	private Boolean useSG = true;
	private Class<? extends Bindings> bindings;

	/**
	 * Returns the network implementation
	 *
	 * @return the network used in that peer
	 */
	public AbstractP2PNetwork<?> getP2PNetworkImplementatino() {
		return this.p2p;
	}

	/**
	 * Returns the evaluator of the created peer
	 *
	 * @return the query client
	 */
	public BasicIndexQueryEvaluator getEvaluator() {
		return this.client;
	}

	/**
	 * Setter for the to be used distribution strategy
	 *
	 * @param distribution
	 *            the distribution strategy
	 * @return the controller of the peer
	 */
	public PeerCreator setDistributionStrategy(
			final IDistributionKeyContainer<?> distribution) {
		this.distribution = distribution;
		return this;
	}

	/**
	 * Setter for the storage to be used
	 *
	 * @param storage
	 *            the class implementing the storage
	 * @return this instance
	 */
	@SuppressWarnings("rawtypes")
	public PeerCreator setStorage(
			final Class<? extends StorageWithDistributionStrategy> storage) {
		return this.setStorage(storage, null);
	}

	/**
	 * Setter for the storage to be used
	 *
	 * @param storage
	 *            the class implementing the storage
	 * @param cfg
	 *            the configuration for this storage
	 * @return this instance
	 */
	@SuppressWarnings("rawtypes")
	public PeerCreator setStorage(
			final Class<? extends StorageWithDistributionStrategy> storage,
			final IConfigurator<StorageWithDistributionStrategy> cfg) {
		if (storage != null) {
			this.storage = storage;
		}
		if (cfg != null) {
			this.storageConfiguration = cfg;
		}
		return this;
	}

	/**
	 * Setter for the to be used network
	 *
	 * @param p2p
	 *            the p2p network to be used
	 * @return the controller of the peer
	 */
	public PeerCreator setP2PNetwork(final AbstractP2PNetwork<?> p2p) {
		this.p2p = p2p;
		return this;
	}


	/**
	 * Sets the bindings to be used
	 * @param b bindings
	 * @return the controller of the peer
	 */
	public PeerCreator setBindings(final Class<? extends Bindings> b) {
		this.bindings = b;
		return this;
	}

	/**
	 * Setter for the to be used p2p network
	 *
	 * @param p2pNetworkName
	 *            the network name, which is registered in
	 *            {@link P2PNetworkCreator}.
	 * @param arguments
	 *            configuration of the p2p network
	 * @return the controller of the peer
	 */
	public PeerCreator setP2PNetwork(final String p2pNetworkName,
			final Map<String, Object> arguments) {
		this.p2p = P2PNetworkCreator.get(p2pNetworkName, arguments);
		return this;
	}

	/**
	 * Setter for the to be used p2p network implementation, without manual
	 * configuration
	 *
	 * @param p2pNetworkName
	 *            the network type name, registered in {@link P2PNetworkCreator}
	 * @return the controller of the peer
	 */
	public PeerCreator setP2PNetwork(final String p2pNetworkName) {
		return this.setP2PNetwork(p2pNetworkName, null);
	}

	/**
	 * Sets whether to use subgraph container submission, or not!
	 *
	 * @param enable
	 *            sends sg?
	 * @return this
	 */
	public PeerCreator setUseSubgraphSubmission(final Boolean enable) {
		if (enable != null) {
			this.useSG = enable;
		}
		return this;
	}

	/**
	 * Blocks the current thread, until it is interrupted.
	 *
	 * @return the controller of the peer (only result, if interrupted)
	 */
	public PeerCreator waitInfinite() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (final InterruptedException e) {
				return this;
			}
		}
		return this;
	}

	/**
	 * Starts the configured peer
	 *
	 * @return the controller of the peer
	 */
	public PeerCreator start() {
		if (this.p2p == null) {
			throw new RuntimeException("No P2P network set.");
		}
		if (this.distribution == null) {
			throw new RuntimeException("No distribution set.");
		}

		try {
			/*
			 * creates the client
			 */
			try {
				P2P_QueryClient_Instanciator.lock();
				P2P_QueryClient_Instanciator.setP2PDistributionStrategy(this.distribution);
				P2P_QueryClient_Instanciator.setP2PNetwork(this.p2p);
				P2P_QueryClient_Instanciator.setStorageType(this.storage);
				P2P_QueryClient_Instanciator.setSubgraphSubmission(this.useSG);
				P2P_QueryClient_Instanciator.setBindings(this.bindings);
				if (this.storageConfiguration != null){
					P2P_QueryClient_Instanciator.setStorageConfiguration(this.storageConfiguration);
				}
				this.client = P2P_QueryClient_Instanciator.newInstance();
			} finally {
				P2P_QueryClient_Instanciator.unlock();
			}
		} catch (final Exception e) {
			final RuntimeException re = new RuntimeException("Error starting p2p query client.", e);
			throw re;
		}
		return this;
	}

	/**
	 * Creates a new peer on the specified port with Chordless and hierarchy
	 * distribution
	 *
	 * @param port
	 *            the port to be started on
	 * @return the controller of the peer
	 */
	public static PeerCreator getChordlessP2PNetwork(final int port) {
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, port);
		return new PeerCreator().setP2PNetwork(P2PNetworkCreator.CHORDLESS, arguments).setDistributionStrategy(new SimplePartitionDistribution());
	}

	/**
	 * Creates a new peer on the specified port and master peer with Chordless
	 * and hierarchy distribution
	 *
	 * @param port
	 *            the port stating this peer
	 * @param masterIP
	 *            the host of the master peer
	 * @param masterPort
	 *            the port, the master peer is listening to
	 * @return the controller of the peer
	 */
	public static PeerCreator getChordlessP2PNetwork(final int port, final String masterIP,
			final int masterPort) {
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, port);
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_IP, masterIP);
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_PORT, masterPort);
		return new PeerCreator().setP2PNetwork(P2PNetworkCreator.CHORDLESS, arguments).setDistributionStrategy(new SimplePartitionDistribution());
	}

	/**
	 * Gets an instance of the pastry p2p network with given port and master
	 *
	 * @param port
	 *            port to listen to
	 * @param masterIP
	 *            master ip address
	 * @param masterPort
	 *            master port
	 * @return this
	 */
	public static PeerCreator getPastryNetwork(final int port, final String masterIP,
			final int masterPort) {
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, port);
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_IP, masterIP);
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_PORT, masterPort);
		return new PeerCreator().setP2PNetwork(P2PNetworkCreator.PASTRY, arguments).setDistributionStrategy(new SimplePartitionDistribution());
	}

	/**
	 * Creates a new peer on the specified port and master peer with TomP2P and
	 * hierarchy distribution
	 *
	 * @param port
	 *            the port stating this peer
	 * @param masterIP
	 *            the host of the master peer
	 * @param masterPort
	 *            the port, the master peer is listening to
	 * @return the controller of the peer
	 */
	public static PeerCreator getTomP2PNetwork(final int port, final String masterIP,
			final int masterPort) {
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, port);
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_IP, masterIP);
		arguments.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_PORT, masterPort);
		return new PeerCreator().setP2PNetwork(P2PNetworkCreator.TOM_P2P, arguments).setDistributionStrategy(new SimplePartitionDistribution());
	}

}
