/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.optimizations.physical.joinorder.withinindexscan;

import java.util.Collection;
import java.util.LinkedList;

import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.physical.joinorder.RearrangeJoinOrder;

/**
 * This class is the abstract super class for all join ordering algorithms, which optimize the join order by changing the position in an IndexScan operator according to some certain criteria. The "some certain criteria" have to be defined in its subclasses. These join ordering algorithms are useful for MemoryIndexScan operators, which join the triple patterns one after each other (analogous to a left-deep join tree).  
 */
public abstract class RearrangeTriplePatternsInIndexScan<T> implements RearrangeJoinOrder {

	@Override
	public void rearrangeJoinOrder(final Root newRoot, final BasicIndexScan indexScan) {
		
		final Collection<TriplePattern> remainingTP = new LinkedList<TriplePattern>();
		remainingTP.addAll(indexScan.getTriplePattern());
		final Collection<TriplePattern> newTriplePattern = new LinkedList<TriplePattern>();
		
		final T additionalInformation = initAdditionalInformation();
		
		while (remainingTP.size() > 0) {
			
			TriplePattern best = getBestNextTriplePatternToJoin(indexScan, remainingTP, additionalInformation);
			
			updateInitialInformation(additionalInformation, best);
			
			newTriplePattern.add(best);
			remainingTP.remove(best);
		}
		indexScan.setTriplePatterns(newTriplePattern);
		newRoot.addSucceedingOperator(indexScan);
	}
	
	/**
	 * This method determines the best triple pattern which should be joined next...
	 * @param indexScan the IndexScan operator to optimize 
	 * @param remainingTP the remaining triple patterns among which the best one next to join is chosen 
	 * @param additionalInformation some additional information is stored in this variable (e.g. the variables of all triple patterns joined so far)
	 * @return the best triple pattern from remainingTP, which should be joined next...
	 */
	protected abstract TriplePattern getBestNextTriplePatternToJoin(final BasicIndexScan indexScan, final Collection<TriplePattern> remainingTP, final T additionalInformation);
	
	/**
	 * This method initializes the additional information and returns it...
	 * @return the initialized additional information (depending on the concrete join order algorithm)
	 */
	protected abstract T initAdditionalInformation();
	
	/**
	 * updates the additional information according to the chosen next best triple pattern
	 * @param additionalInformation the additional information to be updated
	 * @param bestNextTriplePatternToJoin the chosen triple pattern, which is scored to be the best triple pattern to join next
	 */
	protected abstract void updateInitialInformation(final T additionalInformation, final TriplePattern bestNextTriplePatternToJoin);
}
