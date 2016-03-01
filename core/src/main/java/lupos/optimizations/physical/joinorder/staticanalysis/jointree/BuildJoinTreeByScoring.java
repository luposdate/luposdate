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
package lupos.optimizations.physical.joinorder.staticanalysis.jointree;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.physical.joinorder.staticanalysis.scoring.subgraph.ScoringSubGraph;

/**
 * This class determines the best combination of subgraphs according to scoring alternatives to be specified when initialized...
 * This class must be overridden and the used scoring algorithms must be initialized...
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class BuildJoinTreeByScoring extends BuildJoinTree<HashSet<Variable>> {

	/**
	 * The scoring algorithms to be used, if there are several added to this list, then the first is the primary scoring algorithm, the second in this list the secondary scoring algorithm and so on...
	 */
	protected LinkedList<ScoringSubGraph<HashSet<Variable>>> scorings = new LinkedList<ScoringSubGraph<HashSet<Variable>>>();
	
	/** {@inheritDoc} */
	@Override
	protected Tuple<Integer, Integer> getBestNextSubgraphsToJoin(
			BasicIndexScan indexScan,
			List<Tuple<BasicOperator, HashSet<Variable>>> jointree) {
		if(this.scorings.isEmpty()){
			throw new RuntimeException("No scorings initialized!");
		}
		
		List<Tuple<Integer, Integer>> bestSubGraphsToJoin = new LinkedList<Tuple<Integer, Integer>>();
		
		// do the initial scoring with the primary scoring strategy
		ScoringSubGraph<HashSet<Variable>> scoring = this.scorings.get(0);
		
		int bestScore = (scoring.scoreIsAscending())? Integer.MAX_VALUE : Integer.MIN_VALUE;
		
		for(int i=0; i<jointree.size()-1; i++){
			Tuple<BasicOperator, HashSet<Variable>> firstSubGraph = jointree.get(i);
			for(int j=i+1; j<jointree.size(); j++){
				Tuple<BasicOperator, HashSet<Variable>> secondSubGraph = jointree.get(j);
				final int actualScore = scoring.determineScore(indexScan, firstSubGraph.getSecond(), secondSubGraph.getSecond());
				if(actualScore == bestScore){
					// we have several subgraphs with best scores!
					bestSubGraphsToJoin.add(new Tuple<Integer, Integer>(i, j));
				} else if((scoring.scoreIsAscending() && actualScore < bestScore) || (!scoring.scoreIsAscending() && actualScore > bestScore)){
					// actual score is now the best one!
					bestSubGraphsToJoin.clear(); // forget about the subgraphs combinations with a worser score!
					bestSubGraphsToJoin.add(new Tuple<Integer, Integer>(i, j));
					bestScore = actualScore;
				}
			}
		}
		
		// do we have only one best combination of subgraphs?
		if(bestSubGraphsToJoin.size()==1){
			return bestSubGraphsToJoin.get(0);
		}
		
		// now do primary, tertiary, ..., n-ary scoring
		for(int k=1; k<this.scorings.size(); k++){
			ScoringSubGraph<HashSet<Variable>> scoring2 = this.scorings.get(k);
			int bestScore2 = (scoring2.scoreIsAscending())? Integer.MAX_VALUE : Integer.MIN_VALUE;
			
			List<Tuple<Integer, Integer>> newBestSubGraphsToJoin = new LinkedList<Tuple<Integer, Integer>>();
			
			for(Tuple<Integer, Integer> currentCombination: bestSubGraphsToJoin){
				int actualScore = scoring2.determineScore(indexScan, jointree.get(currentCombination.getFirst()).getSecond(), jointree.get(currentCombination.getSecond()).getSecond());
				if(actualScore == bestScore2){
					// we have several subgraphs with best scores!
					newBestSubGraphsToJoin.add(currentCombination);
				} else if((scoring2.scoreIsAscending() && actualScore < bestScore) || (!scoring2.scoreIsAscending() && actualScore > bestScore)){
					// actual score is now the best one!
					newBestSubGraphsToJoin.clear(); // forget about the subgraphs combinations with a worser score!
					newBestSubGraphsToJoin.add(currentCombination);
					bestScore2 = actualScore;
				}
			}
			
			// do we have only one best combination of subgraphs?
			if(newBestSubGraphsToJoin.size()==1){
				return newBestSubGraphsToJoin.get(0);
			}
			
			bestSubGraphsToJoin = newBestSubGraphsToJoin;
		}

		// if there are still more than one best scored combination of subgraphs return just the first in the list!
		return bestSubGraphsToJoin.get(0);
	}

	/** {@inheritDoc} */
	@Override
	protected HashSet<Variable> initAdditionalInformation(TriplePattern tp) {
		return new HashSet<Variable>(tp.getUnionVariables());
	}

	/** {@inheritDoc} */
	@Override
	protected HashSet<Variable> mergeInitialInformations(
			HashSet<Variable> additionalInformation1,
			HashSet<Variable> additionalInformation2) {
		HashSet<Variable> mergedInfo = new HashSet<Variable>(additionalInformation1.size() + additionalInformation2.size()); // use an optimal initial capacity
		mergedInfo.addAll(additionalInformation1);
		mergedInfo.addAll(additionalInformation2);
		return mergedInfo;
	}
}
