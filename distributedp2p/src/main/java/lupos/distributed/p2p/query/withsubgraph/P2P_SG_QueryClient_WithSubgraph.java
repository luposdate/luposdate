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
package lupos.distributed.p2p.query.withsubgraph;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.distributed.operator.ISubgraphExecutor;
import lupos.distributed.p2p.network.AbstractP2PNetwork;
import lupos.distributed.p2p.rules.HierachialDistributionRulePackage;
import lupos.distributed.p2p.storage.BlockStorageWithDistributionStrategy;
import lupos.distributed.p2p.storage.StorageWithDistributionStrategy;
import lupos.distributed.query.QueryClientWithSubgraphTransmission;
import lupos.distributed.storage.distributionstrategy.IDistribution;
import lupos.distributed.storage.distributionstrategy.tripleproperties.KeyContainer;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.optimizations.logical.rules.generated.AddSubGraphContainerRule;
import lupos.optimizations.logical.rules.generated.DistributedRulePackage;
import lupos.optimizations.logical.rules.generated.LogicalOptimizationRulePackage;
import lupos.rdf.Prefix;

/**
 * QueryClient with optimization for P2P network
 * @author Bjoern
 *
 * @param <T> the type of the {@link KeyContainer}
 */
public class P2P_SG_QueryClient_WithSubgraph<T> extends QueryClientWithSubgraphTransmission<KeyContainer<T>>{

	private boolean useSG = true;

	/**
	 * Sets whether to use subgraph execution and submission
	 * @param enabled enable the option
	 */
	public void setUseSubgraphSubmission(boolean enabled) {
		this.useSG  = enabled;
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		LiteralFactory.setType(LiteralFactory.MapType.NOCODEMAP);
		Bindings.instanceClass = BindingsMap.class;
	}
	
	@Override
	public long logicalOptimization() {
		//do no use DistributedRulePackage if not using Subgraph Submission ...
		if (!useSG) {
			final Date a = new Date();
			this.setBindingsVariablesBasedOnOperatorgraph();
			final LogicalOptimizationRulePackage refie = new LogicalOptimizationRulePackage();
			refie.applyRules(this.root);
			this.root.optimizeJoinOrder(this.opt);
			final LogicalOptimizationRulePackage refie2 = new LogicalOptimizationRulePackage();
			refie2.applyRules(this.root);
			this.parallelOperator(this.root);
			return ((new Date()).getTime() - a.getTime());
		}
		//else use optimization for SG rules
		final Date a = new Date();
		final LogicalOptimizationRulePackage refie = new LogicalOptimizationRulePackage();
		refie.applyRules(root);
		root.optimizeJoinOrder(opt);
		final LogicalOptimizationRulePackage refie2 = new LogicalOptimizationRulePackage();
		refie2.applyRules(root);
		parallelOperator(root);
		
		AddSubGraphContainerRule.distribution = this.distribution;
		AddSubGraphContainerRule.subgraphExecutor = this.subgraphExecutor;
		final DistributedRulePackage rules = new DistributedRulePackage();
		rules.applyRules(root);
		
		HierachialDistributionRulePackage rulePackage = new HierachialDistributionRulePackage();
		rulePackage.applyRules(root);
		
		return ((new Date()).getTime() - a.getTime());
	}
	
	@Override
	public List<DebugContainer<BasicOperatorByteArray>> logicalOptimizationDebugByteArray(
			final Prefix prefixInstance) {
		if (!useSG) {
			final List<DebugContainer<BasicOperatorByteArray>> result = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
			this.setBindingsVariablesBasedOnOperatorgraph();
			result.add(new DebugContainer<BasicOperatorByteArray>(
					"Before logical optimization...",
					"logicaloptimizationPackageDescription", BasicOperatorByteArray
					.getBasicOperatorByteArray(this.root.deepClone(),
							prefixInstance)));
			final LogicalOptimizationRulePackage refie = new LogicalOptimizationRulePackage();
			result.addAll(refie.applyRulesDebugByteArray(this.root,
					prefixInstance));
			this.root.optimizeJoinOrder(this.opt);
			result.add(new DebugContainer<BasicOperatorByteArray>(
					"After optimizing the join order...",
					"optimizingjoinord;erRule", BasicOperatorByteArray
					.getBasicOperatorByteArray(this.root.deepClone(),
							prefixInstance)));
			final LogicalOptimizationRulePackage refie2 = new LogicalOptimizationRulePackage();
			result.addAll(refie2.applyRulesDebugByteArray(this.root,
					prefixInstance));
			final List<DebugContainer<BasicOperatorByteArray>> ldc = this.parallelOperatorDebugByteArray(
					this.root, prefixInstance);
			if (ldc != null) {
				result.addAll(ldc);
			}
			return result;
		}
		final List<DebugContainer<BasicOperatorByteArray>> result = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
		result.add(new DebugContainer<BasicOperatorByteArray>(
				"Before logical optimization...",
				"logicaloptimizationPackageDescription", BasicOperatorByteArray
				.getBasicOperatorByteArray(root.deepClone(),
						prefixInstance)));
		final LogicalOptimizationRulePackage refie = new LogicalOptimizationRulePackage();
		result.addAll(refie.applyRulesDebugByteArray(root,
				prefixInstance));

		root.optimizeJoinOrder(opt);
		result.add(new DebugContainer<BasicOperatorByteArray>(
				"After optimizing the join order...",
				"optimizingjoinorderRule", BasicOperatorByteArray
				.getBasicOperatorByteArray(root.deepClone(),
						prefixInstance)));
		final LogicalOptimizationRulePackage refie2 = new LogicalOptimizationRulePackage();
		result.addAll(refie2.applyRulesDebugByteArray(root,
				prefixInstance));
		final List<DebugContainer<BasicOperatorByteArray>> ldc = parallelOperatorDebugByteArray(
				root, prefixInstance);
		
		if (ldc != null)
			result.addAll(ldc);
		
		AddSubGraphContainerRule.distribution = this.distribution;
		AddSubGraphContainerRule.subgraphExecutor = this.subgraphExecutor;
		final DistributedRulePackage rules = new DistributedRulePackage();
		List<DebugContainer<BasicOperatorByteArray>> result2 = rules.applyRulesDebugByteArray(this.rootNode, prefixInstance);
		if(result2!=null){
			result.addAll(result2);
		}
		
		HierachialDistributionRulePackage rulePackage = new HierachialDistributionRulePackage();
		rulePackage.applyRules(root);
		result.add(new DebugContainer<BasicOperatorByteArray>(
				"After HierachialDistributionRulePackage optimization...",
				"optimizingjoinorderRule", BasicOperatorByteArray
				.getBasicOperatorByteArray(root.deepClone(),
						prefixInstance)));
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public P2P_SG_QueryClient_WithSubgraph(final StorageWithDistributionStrategy s,
			ISubgraphExecutor<KeyContainer<T>> subgraphExecutor)
			throws Exception {
		super(s, s.getDistribution(), subgraphExecutor);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public P2P_SG_QueryClient_WithSubgraph(final StorageWithDistributionStrategy s,
			ISubgraphExecutor<KeyContainer<T>> subgraphExecutor,String[] args)
			throws Exception {
		super(s, s.getDistribution(), subgraphExecutor,args);
	}
	@SuppressWarnings({ })
	public P2P_SG_QueryClient_WithSubgraph(final AbstractP2PNetwork<Triple> p2p,
			IDistribution<KeyContainer<T>> distribution,
			ISubgraphExecutor<KeyContainer<T>> subgraphExecutor,String[] args)
			throws Exception {
		super(new BlockStorageWithDistributionStrategy<>(p2p, distribution), distribution, subgraphExecutor,args);
	}
	
	@SuppressWarnings("rawtypes")
	public P2P_SG_QueryClient_WithSubgraph(final StorageWithDistributionStrategy s,
			IDistribution<KeyContainer<T>> distribution,
			ISubgraphExecutor<KeyContainer<T>> subgraphExecutor,String[] args)
			throws Exception {
		super(s, distribution, subgraphExecutor,args);
	}
	
}
