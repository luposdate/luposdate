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
package lupos.optimizations.physical.joinorder.costbasedoptimizer.splitheuristic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.LeafNodePlan;

/**
 * This strategy splits a group of leaf nodes to join into a subgraph with the maximum possible number of merge joins (and the remaining subgraph)
 *
 * @author groppe
 * @version $Id: $Id
 */
public class SplitGraphWithMaxNumberOfMergeJoins implements SplitHeuristic {

	/** {@inheritDoc} */
	@Override
	public List<List<LeafNodePlan>> split(List<LeafNodePlan> initialPlans) {
		// We determine the maximum number of possible merge joins!
		final HashMap<Item, LinkedList<LeafNodePlan>> mergeJoins = new HashMap<Item, LinkedList<LeafNodePlan>>();
		for (final LeafNodePlan tp : initialPlans) {
			for (final Variable var : tp.getVariables()) {
				// just the group the leaf nodes with common variables together (this group can be surely joined only with merge joins)
				LinkedList<LeafNodePlan> lltp = mergeJoins.get(var);
				if (lltp == null){
					lltp = new LinkedList<LeafNodePlan>();
				}
				lltp.add(tp);
				mergeJoins.put(var, lltp);
			}
		}
		// determine the group the most possible merge joins
		int maxMergeJoins = 0;
		for (final LinkedList<LeafNodePlan> lltp : mergeJoins.values()) {
			if (lltp.size() > maxMergeJoins){
				maxMergeJoins = lltp.size();
			}
		}
		// avoid the trivial cases (only few merge joins or the whole leaf nodes form a star-shaped join)
		if (maxMergeJoins > 2 && maxMergeJoins < initialPlans.size()) {
			final LinkedList<LeafNodePlan> otherSubgraph = new LinkedList<LeafNodePlan>();
			for (final LinkedList<LeafNodePlan> lltp : mergeJoins.values()) {
				if (lltp.size() == maxMergeJoins){
					// determine the other leaf nodes, which are not contained in the subgraph with maximum number of merge joins!
					for(LeafNodePlan lfp: initialPlans){
						if(!lltp.contains(lfp)){
							otherSubgraph.add(lfp);
						}
					}
					// now check if otherSubGraph contains a cartesian product, which would be unacceptable!
					List<List<LeafNodePlan>> cartesianProduct2 = SplitCartesianProduct.cartesianProducts(otherSubgraph);
					if(cartesianProduct2.size()<=1){
						// no cartesian product detected => acceptable splitting!
						List<List<LeafNodePlan>> result = new LinkedList<List<LeafNodePlan>>();
						result.add(lltp);
						result.add(otherSubgraph);
						return result;
					}					
				}
			}
		}
		// this strategy did not work!
		List<List<LeafNodePlan>> result = new LinkedList<List<LeafNodePlan>>();
		result.add(initialPlans);
		return result;
	}
}
