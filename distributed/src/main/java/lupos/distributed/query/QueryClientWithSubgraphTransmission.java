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
package lupos.distributed.query;

import java.util.Date;
import java.util.List;

import lupos.distributed.operator.ISubgraphExecutor;
import lupos.distributed.query.operator.histogramsubmission.IHistogramExecutor;
import lupos.distributed.storage.IStorage;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.optimizations.logical.rules.generated.AddSubGraphContainerRule;
import lupos.optimizations.logical.rules.generated.DistributedRulePackage;
import lupos.rdf.Prefix;

public class QueryClientWithSubgraphTransmission<K> extends QueryClient {

	public final IDistribution<K> distribution;

	public final ISubgraphExecutor<K> subgraphExecutor;

	public QueryClientWithSubgraphTransmission(final IStorage storage, final IDistribution<K> distribution, final ISubgraphExecutor<K> subgraphExecutor) throws Exception {
		this(storage, null, distribution, subgraphExecutor);
	}

	public QueryClientWithSubgraphTransmission(final IStorage storage, final IHistogramExecutor histogramExecutor, final IDistribution<K> distribution, final ISubgraphExecutor<K> subgraphExecutor) throws Exception {
		super(storage, histogramExecutor);
		this.distribution = distribution;
		this.subgraphExecutor = subgraphExecutor;
	}

	public QueryClientWithSubgraphTransmission(final IStorage storage, final IDistribution<K> distribution, final ISubgraphExecutor<K> subgraphExecutor, final String[] args) throws Exception {
		this(storage, null, distribution, subgraphExecutor, args);
	}

	public QueryClientWithSubgraphTransmission(final IStorage storage, final IHistogramExecutor histogramExecutor, final IDistribution<K> distribution, final ISubgraphExecutor<K> subgraphExecutor, final String[] args) throws Exception {
		super(storage, histogramExecutor, args);
		this.distribution = distribution;
		this.subgraphExecutor = subgraphExecutor;
	}

	@Override
	protected void initOptimization() {
		if(this.histogramExecutor == null){
			// make binary joins such that subgraphs can be identified...
			this.opt = BasicIndexScan.BINARYSTATICANALYSIS;
		} else {
			// use histograms to find best join order
			this.opt = BasicIndexScan.BINARY;
		}
	}

	@Override
	public long logicalOptimization() {
		final long start = new Date().getTime();
		super.logicalOptimization();

		// evaluate rule to identify subgraphs and put them into a SubgraphContainer operator
		AddSubGraphContainerRule.distribution = this.distribution;
		AddSubGraphContainerRule.subgraphExecutor = this.subgraphExecutor;
		final DistributedRulePackage rules = new DistributedRulePackage();
		rules.applyRules(this.rootNode);

		return new Date().getTime() - start;
	}

	@Override
	public List<DebugContainer<BasicOperatorByteArray>> logicalOptimizationDebugByteArray(
			final Prefix prefixInstance) {
		final List<DebugContainer<BasicOperatorByteArray>> result = super.logicalOptimizationDebugByteArray(prefixInstance);

		// evaluate rule to identify subgraphs and put them into a SubgraphContainer operator
		AddSubGraphContainerRule.distribution = this.distribution;
		AddSubGraphContainerRule.subgraphExecutor = this.subgraphExecutor;
		final DistributedRulePackage rules = new DistributedRulePackage();
		final List<DebugContainer<BasicOperatorByteArray>> result2 = rules.applyRulesDebugByteArray(this.rootNode, prefixInstance);
		if(result2!=null){
			result.addAll(result2);
		}

		return result;
	}
}
