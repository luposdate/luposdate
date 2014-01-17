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

import lupos.distributed.p2p.distributionstrategy.SimplePartitionDistribution;
import lupos.distributed.p2p.network.P2PNetworkCreator;
import lupos.distributed.query.QueryClient;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.sparql1_1.Node;

/**
 * Chordless P2P Network with HierarchyDistribution strategy and subgraph
 * submission
 * 
 * @author Bjoern
 * 
 */
public abstract class Chord_WithHierarchyDistribution extends
		QueryEvaluator<Node> {

	public Chord_WithHierarchyDistribution() throws Exception {
		super();
		throw new RuntimeException("Please use static newInstance-method!");
	}

	public static QueryClient newInstance() {
		try {
			P2P_QueryClient_Creator.lock();
			P2P_QueryClient_Creator
					.setP2PImplementationConstant(P2PNetworkCreator.CHORDLESS);
			P2P_QueryClient_Creator
					.setP2PDistributionStrategy(new SimplePartitionDistribution());
			P2P_QueryClient_Creator.setSubgraphSubmission(true);
			return P2P_QueryClient_Creator.newInstance();
		} finally {
			P2P_QueryClient_Creator.unlock();
		}
	}

	public static QueryClient newInstance(Map<String, Object> config) {
		try {
			P2P_QueryClient_Creator.lock();
			P2P_QueryClient_Creator
					.setP2PImplementationConstant(P2PNetworkCreator.CHORDLESS);
			P2P_QueryClient_Creator
					.setP2PDistributionStrategy(new SimplePartitionDistribution());
			P2P_QueryClient_Creator.setSubgraphSubmission(true);
			P2P_QueryClient_Creator.setP2PImplementationConfiguration(config);
			return P2P_QueryClient_Creator.newInstance();
		} finally {
			P2P_QueryClient_Creator.unlock();
		}
	}

}
