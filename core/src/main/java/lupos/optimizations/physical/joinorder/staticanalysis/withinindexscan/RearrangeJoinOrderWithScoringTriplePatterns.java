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
package lupos.optimizations.physical.joinorder.staticanalysis.withinindexscan;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.physical.joinorder.staticanalysis.scoring.triplepattern.ScoringTriplePattern;

/**
 * This class determines the best triple pattern according to scoring alternatives to be specified when initialized...
 * This class must be overridden and the used scoring algorithms must be initialized...
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class RearrangeJoinOrderWithScoringTriplePatterns extends RearrangeTriplePatternsInIndexScan<HashSet<Variable>> {

	/**
	 * The scoring algorithms to be used, if there are several added to this list, then the first is the primary scoring algorithm, the second in this list the secondary scoring algorithm and so on...
	 */
	protected LinkedList<ScoringTriplePattern<HashSet<Variable>>> scorings = new LinkedList<ScoringTriplePattern<HashSet<Variable>>>();
	
	
	/** {@inheritDoc} */
	@Override
	protected TriplePattern getBestNextTriplePatternToJoin(
			BasicIndexScan indexScan, Collection<TriplePattern> remainingTP,
			HashSet<Variable> additionalInformation) {
		if(this.scorings.isEmpty()){
			throw new RuntimeException("No scorings initialized!");
		}		
		Collection<TriplePattern> bestScored = remainingTP;		
		if(bestScored.size()==1){ // only one triple pattern left => no scoring necessary
			return bestScored.iterator().next();
		}
		
		// Score in the order the scoring algorithms are added to the list this.scorings!
		for(ScoringTriplePattern<HashSet<Variable>> scoring: this.scorings){
			
			LinkedList<TriplePattern> newBestScored = new LinkedList<TriplePattern>();
			
			int bestScore = (scoring.scoreIsAscending())? Integer.MAX_VALUE : Integer.MIN_VALUE;
			
			for(TriplePattern tp: bestScored){
				int actualScore = scoring.determineScore(indexScan, tp, additionalInformation);
				if(actualScore == bestScore){
					// we have now several best triple patterns
					newBestScored.add(tp);
				} else if((scoring.scoreIsAscending() && actualScore < bestScore) || (!scoring.scoreIsAscending() && actualScore > bestScore)){
					// actual score is now the best one!
					newBestScored.clear(); // Forget about the triple patterns with a worser score!
					newBestScored.add(tp);
					bestScore = actualScore;
				}
			}
			
			bestScored = newBestScored;
			
			if(bestScored.size()==1){ // only one triple pattern left => the best triple pattern is already determined and no further scoring necessary
				return bestScored.iterator().next();
			}
			
		}
		
		// if there are still more than one best scored triple pattern return just the first in the list!
		return bestScored.iterator().next();
	}

	/** {@inheritDoc} */
	@Override
	protected HashSet<Variable> initAdditionalInformation() {
		return new HashSet<Variable>();
	}

	/** {@inheritDoc} */
	@Override
	protected void updateInitialInformation(
			HashSet<Variable> additionalInformation,
			TriplePattern bestNextTriplePatternToJoin) {
		additionalInformation.addAll(bestNextTriplePatternToJoin.getVariables());
	}

}
