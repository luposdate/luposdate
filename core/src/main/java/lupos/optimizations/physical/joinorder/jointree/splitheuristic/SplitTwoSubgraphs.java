/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.optimizations.physical.joinorder.jointree.splitheuristic;

import java.util.LinkedList;
import java.util.List;

import lupos.optimizations.physical.joinorder.jointree.plan.LeafNodePlan;

/**
 * This strategy splits the plans at an edge, which connects two subgraphs and which are not connected any more without this edge...  
 */
public class SplitTwoSubgraphs implements SplitHeuristic {

	@SuppressWarnings("null")
	@Override
	public List<List<LeafNodePlan>> split(List<LeafNodePlan> initialPlans) {
		// Is there one triple pattern, which connects two subgraphs,
		// between which is no other edge than the one of the considered
		// triple pattern?
		for (final LeafNodePlan leafNodePlan : initialPlans) { // try out all edges one after each other...
			final LinkedList<LeafNodePlan> withoutCurrentEdge = new LinkedList<LeafNodePlan>();
			withoutCurrentEdge.addAll(initialPlans);
			withoutCurrentEdge.remove(leafNodePlan);
			// check now if there is a cartesian product in withoutCurrentEdge:
			final List<List<LeafNodePlan>> cartesianProducts2 = SplitCartesianProduct.cartesianProducts(withoutCurrentEdge);
			if (cartesianProducts2.size() > 1) {
				boolean flag = true;
				List<LeafNodePlan> listWithSmallestSize = null;
				for (final List<LeafNodePlan> listOfLeafNodePlan : cartesianProducts2) {
					if (listOfLeafNodePlan.size() <= 1){ // do not delete an edge from a star-shaped-join!
						flag = false;
						break;
					}
					if(listWithSmallestSize==null || listOfLeafNodePlan.size() < listWithSmallestSize.size()){
						listWithSmallestSize = listOfLeafNodePlan;
					}
				}
				if (flag) {
					listWithSmallestSize.add(leafNodePlan); // do not forget the currently considered edge => add it to the subgraph with less nodes, such that the two subgraphs to join have more similar sizes. 
					return cartesianProducts2;
				}
			}
		}
		// this heuristic cannot split this plan! 
		LinkedList<List<LeafNodePlan>> result = new LinkedList<List<LeafNodePlan>>();
		result.add(initialPlans);
		return result;
	}
}
