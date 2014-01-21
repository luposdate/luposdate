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
package lupos.distributed.p2p.query.withsubgraph;

import java.util.Map;

import lupos.distributed.p2p.network.P2PNetworkCreator;
import lupos.distributed.query.QueryClient;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributedendpoints.gui.P2PConfigFrame;
import lupos.distributedendpoints.gui.P2PConfigFrame.PeerItem;

/**
 * This class is to be used, to get an evaluator for a p2p network.
 * Furthermore a UI will present the network to be connected to, or will
 * give possibility to choose an already started evaluator.
 */
public class P2P_QueryClient_Creator {
	
	/*
	 * information about the network to be used
	 */
	private final String NETWORK; 
	/*
	 * information about the selected distribution strategy (will be improved soon)
	 */
	private final IDistribution DISTRIBUTION;
	
	/*
	 * use Subgraph submission
	 */
	private final boolean withSubgraphSubmission;
	
	/**
	 * Net P2P-Query-Client creater with the given p2p-network and the given
	 * distribution strategy
	 * @param p2pNetwork the P2P network identifier
	 * @param distributionStrategy the strategy for distribution to be used
	 * @param sgSubmission use subgraph submission?
	 */
	public P2P_QueryClient_Creator(String p2pNetwork, IDistribution distributionStrategy,
			boolean sgSubmission) {
		this.NETWORK = p2pNetwork;
		this.DISTRIBUTION = distributionStrategy;
		withSubgraphSubmission = sgSubmission;
	}
	

	/**
	 * Net P2P-Query-Client creater with the given p2p-network and the given
	 * distribution strategy
	 * @param p2pNetwork the P2P network identifier
	 * @param distributionStrategy the strategy for distribution to be used
	 */
	public P2P_QueryClient_Creator(String p2pNetwork, IDistribution distributionStrategy) {
		this.NETWORK = p2pNetwork;
		this.DISTRIBUTION = distributionStrategy;
		withSubgraphSubmission = true;
	}
	
	/**
	 * Dialog to choose the evaluator which is to be used.
	 */
	private P2PConfigFrame configurationUI = new P2PConfigFrame() {
		
		private QueryClient createInstance() {
			return P2P_QueryClient_Instanciator.newInstance();
		}
		
		@Override
		public PeerItem onQueryEvaluator(PeerItem evaluator) throws Exception {
			//Here you should throw an exception, if the selected QueryEvaluator is not 
			//allowed, because wrong network or different distribution strategy ...
			if (evaluator.queryEvaluator == null) throw new NullPointerException("Evaluator is not existing anymore.");
			if (!evaluator.networkName.equals(NETWORK)) {
				throw new IllegalArgumentException("Only same P2P network can be used.");
			}
			if (!evaluator.distributionStrategy.equals(DISTRIBUTION.toString())) {
				throw new IllegalArgumentException("Only same distribution strategy can be used.");
			}
			
			
			if (evaluator.useSubgraphSubmission != withSubgraphSubmission) {
				QueryClient evalInstance = evaluator.queryEvaluator;
				/*
				 * try to change the submission type, if special instance
				 */
				if (evalInstance instanceof P2P_SG_QueryClient_WithSubgraph) {
					//update the queryclient and the table row
					evaluator.useSubgraphSubmission = withSubgraphSubmission;
					((P2P_SG_QueryClient_WithSubgraph)evalInstance).setUseSubgraphSubmission(withSubgraphSubmission);
				} else throw new IllegalArgumentException("Evaluator does not support changing the behavior of sending subgraphs");
			}
			
			return evaluator;
			
		}
		
		@Override
		public PeerItem onMasterInstance(int localPort, int masterPort,
				String masterAddress) {
			//store information 
			PeerItem item = new PeerItem();
			item.networkName = NETWORK;
			item.isMaster = false;
			item.masterName= masterAddress;
			item.masterPort = masterPort;
			item.distributionStrategy = DISTRIBUTION.toString();
			item.port = localPort;
			item.useSubgraphSubmission = withSubgraphSubmission;
			
			//set ports and master
			Map<String,Object> cfg = P2P_QueryClient_Instanciator.getP2PImplementationConfiguration();
			cfg.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, localPort);
			cfg.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_IP,
					masterAddress);
			cfg.put(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_PORT,
					masterPort);
			P2P_QueryClient_Instanciator.setP2PImplementationConfiguration(cfg);
			P2P_QueryClient_Instanciator.setSubgraphSubmission(withSubgraphSubmission);
			// create instance and store instance
			QueryClient qC = createInstance();
			item.queryEvaluator = qC;
			return item;
		}
		
		@Override
		public PeerItem onLocalInstance(int localPort) {
			/*
			 * store information
			 */
			PeerItem item = new PeerItem();
			item.networkName = NETWORK;
			item.isMaster = true;
			item.distributionStrategy = DISTRIBUTION.toString();
			item.port = localPort;
			item.useSubgraphSubmission = withSubgraphSubmission;
			/*
			 * store configuration
			 */
			Map<String,Object> cfg = P2P_QueryClient_Instanciator.getP2PImplementationConfiguration();
			cfg.put(P2PNetworkCreator.P2PConfigurationConstants.cPORT, localPort);
			cfg.remove(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_IP);
			cfg.remove(P2PNetworkCreator.P2PConfigurationConstants.cMASTER_PORT);
			P2P_QueryClient_Instanciator.setP2PImplementationConfiguration(cfg);
			P2P_QueryClient_Instanciator.setSubgraphSubmission(withSubgraphSubmission);
			// create instance and store instance
			QueryClient qC = createInstance();
			item.queryEvaluator = qC;
			return item;
		}
		
		@Override
		public void onCancel() {
		}
	};
	
	

	/**
	 * Returns an already running queryClient or starts a new one
	 * @return a new evaluator which can be used in LuposDate UI
	 */
	public QueryClient newInstance() {
		try {
			P2P_QueryClient_Instanciator.lock();
			P2P_QueryClient_Instanciator
					.setP2PImplementationConstant(NETWORK);
			/*should be configured before via UI? */
			P2P_QueryClient_Instanciator
					.setP2PDistributionStrategy(DISTRIBUTION);
			P2P_QueryClient_Instanciator.setSubgraphSubmission(withSubgraphSubmission);
			
			/*
			 * ask which instance is to be used
			 */
			PeerItem instance = configurationUI.showDialog();
			if (instance != null && instance.queryEvaluator != null) return instance.queryEvaluator;
			return null; 
		} finally {
			P2P_QueryClient_Instanciator.unlock();
		}
	}

	/**
	 * Returns an already running queryClient or starts a new one
	 * @param config the configuration to be used
	 * @return a new evaluator which can be used in LuposDate UI
	 */
	public QueryClient newInstance(Map<String, Object> config) {
		try {
			P2P_QueryClient_Instanciator.lock();
			P2P_QueryClient_Instanciator
					.setP2PImplementationConstant(NETWORK);
			P2P_QueryClient_Instanciator
					.setP2PDistributionStrategy(DISTRIBUTION);
			P2P_QueryClient_Instanciator.setSubgraphSubmission(withSubgraphSubmission);
			P2P_QueryClient_Instanciator.setP2PImplementationConfiguration(config);
			
			/*
			 * ask which instance is to be used
			 */
			PeerItem instance = configurationUI.showDialog();
			if (instance != null && instance.queryEvaluator != null) return instance.queryEvaluator;
			return null; 
		} finally {
			P2P_QueryClient_Instanciator.unlock();
		}
	}

}
