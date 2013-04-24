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
package lupos.distributedendpoints.query.withoutsubgraphsubmission;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.distributed.query.QueryClient;
import lupos.distributed.storage.distributionstrategy.tripleproperties.IDistributionKeyContainer;
import lupos.distributedendpoints.gui.Start_Demo_Applet_DE;
import lupos.distributedendpoints.storage.DistributionHistogramExecutor;
import lupos.distributedendpoints.storage.Storage_DE_DistributionStrategy;

/**
 * This class is the query evaluator for querying distributed SPARQL endpoints based on a given distribution strategy.
 *
 * It uses the super and helper classes of the distributed module for a first and simple example of a distributed scenario.
 */
public class QueryClient_DE_DistributionStrategy<K> extends QueryClient {

	public QueryClient_DE_DistributionStrategy(final IDistributionKeyContainer<K> distribution) throws Exception {
		super(Storage_DE_DistributionStrategy.createInstance(distribution));
		this.askForHistogramRequests();
	}

	public QueryClient_DE_DistributionStrategy(final IDistributionKeyContainer<K> distribution, final String[] args) throws Exception {
		super(Storage_DE_DistributionStrategy.createInstance(distribution), args);
		this.askForHistogramRequests();
	}

	private void askForHistogramRequests(){
		if(Start_Demo_Applet_DE.askForHistogramRequests()){
			final Storage_DE_DistributionStrategy storage_DE_DS = (Storage_DE_DistributionStrategy) this.storage;
			this.histogramExecutor = new DistributionHistogramExecutor(storage_DE_DS.getDistribution(), storage_DE_DS.getEndpointManagement());
			this.initOptimization();
		}
	}

	@Override
	public void init() throws Exception {
		// just for avoiding problems in distributed scenarios
		Bindings.instanceClass = BindingsMap.class;
		LiteralFactory.setType(LiteralFactory.MapType.NOCODEMAP);
		super.init();
	}
}
