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
package lupos.distributed.p2p.query;

import java.util.Map;

import lupos.distributed.p2p.network.P2PNetworkCreator;
import lupos.distributed.p2p.query.withsubgraph.P2P_QueryClient_Creator;
import lupos.distributed.query.QueryClient;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.OneToThreeKeysDistribution;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.sparql1_1.Node;
	/**
	 * TomP2P Network with {@link OneToThreeKeysDistribution} strategy
	 * @author Bjoern
	 *
	 */
public abstract class Chordless_WithOneToThreeKeysDistribution  extends QueryEvaluator<Node> {
	/*
	 * information about the network to be used
	 */
	private static final String NETWORK = P2PNetworkCreator.CHORDLESS;
	@SuppressWarnings("rawtypes")
	/*
	 * information about the selected distribution strategy (will be improved
	 * soon)
	 */
	protected static final IDistribution DISTRIBUTION = new OneToThreeKeysDistribution();

	/**
	 * don't use
	 */
	@Deprecated
	public Chordless_WithOneToThreeKeysDistribution() throws Exception {
		super();
		throw new RuntimeException("Please use static newInstance()-method!");
	}

	/**
	 * Returns an already running queryClient or starts a new one
	 */
	public static QueryClient newInstance() {
		try {
			P2P_QueryClient_Creator creator = new P2P_QueryClient_Creator(
					NETWORK, DISTRIBUTION, false);
			QueryClient instance = creator.newInstance();
			return instance;
		} finally {
		}
	}

	/**
	 * Returns an already running queryClient or starts a new one
	 * 
	 * @param config
	 *            the configuration to be used
	 */
	public static QueryClient newInstance(Map<String, Object> config) {
		try {
			P2P_QueryClient_Creator creator = new P2P_QueryClient_Creator(
					NETWORK, DISTRIBUTION, false);
			QueryClient instance = creator.newInstance(config);
			return instance;
		} finally {

		}
	}

}