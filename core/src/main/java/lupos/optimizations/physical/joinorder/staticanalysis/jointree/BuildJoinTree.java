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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.physical.joinorder.RearrangeJoinOrder;

/**
 * This class is the abstract super class for all join ordering algorithms, which optimize the join order by generating a good join tree according to some certain criteria. The "some certain criteria" have to be defined in its subclasses. These join ordering algorithms are useful for evaluators (e.g. in p2p networks), which do not have any access to histograms and must use a static analysis to obtain a join tree.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class BuildJoinTree<T> implements RearrangeJoinOrder {
	
	/** {@inheritDoc} */
	@Override
	public void rearrangeJoinOrder(final Root newRoot, final BasicIndexScan indexScan) {
		
		// this structure will contain the subgraphs so far under which two are chosen to be joined next...
		final List<Tuple<BasicOperator, T>> jointree = new LinkedList<Tuple<BasicOperator, T>>();  

		// prepare the initial subgraphs containing an index scan operator for each triple pattern
		for(TriplePattern tp: indexScan.getTriplePattern()){
			Collection<TriplePattern> tps = new LinkedList<TriplePattern>();
			tps.add(tp);
			BasicIndexScan is = indexScan.getRoot().newIndexScan(null, tps, indexScan.getGraphConstraint());
			HashSet<Variable> vars = tp.getVariables();
			HashSet<Variable> intersection = new HashSet<Variable>(vars); 
			HashSet<Variable> union = new HashSet<Variable>(vars);
			is.setIntersectionVariables(intersection);
			is.setUnionVariables(union);
			is.setGraphConstraint(indexScan.getGraphConstraint());
			T additionalInformation = this.initAdditionalInformation(tp);
			jointree.add(new Tuple<BasicOperator, T>(is, additionalInformation));
			newRoot.addSucceedingOperator(is);
		}
				
		while (jointree.size() > 1) {
			
			Tuple<Integer, Integer> nextBestSubgraphsToJoin = this.getBestNextSubgraphsToJoin(indexScan, jointree);
			
			int first = nextBestSubgraphsToJoin.getFirst();
			int second = nextBestSubgraphsToJoin.getSecond();
			
			Tuple<BasicOperator, T> firstSubGraph = jointree.get(first);
			Tuple<BasicOperator, T> secondSubGraph = jointree.get(second);
			
			// delete from jointree using the index: take care to delete the subgraph with higher index first (avoiding deleting a wrong index, because the lower index was first deleted)
			if(first>second){
				jointree.remove(first);
				jointree.remove(second);
			} else {
				jointree.remove(second);
				jointree.remove(first);
			}
			
			// join the determined subgraphs and put the join back into jointree!
			BasicOperator firstOperand = firstSubGraph.getFirst(); 
			BasicOperator secondOperand = secondSubGraph.getFirst(); 
			
			Join join = new Join();
			
			HashSet<Variable> joinUnion = new HashSet<Variable>(firstOperand.getUnionVariables());
			joinUnion.addAll(secondOperand.getUnionVariables());
			join.setUnionVariables(joinUnion);
			
			HashSet<Variable> joinIntersection = new HashSet<Variable>(firstOperand.getUnionVariables());
			joinIntersection.retainAll(secondOperand.getUnionVariables());
			join.setIntersectionVariables(joinIntersection);
						
			firstOperand.addSucceedingOperator(join, 0);
			secondOperand.addSucceedingOperator(join, 1);
			
			jointree.add(new Tuple<BasicOperator, T>(join, mergeInitialInformations(firstSubGraph.getSecond(), secondSubGraph.getSecond())));			
		}
		
		BasicOperator op = jointree.get(0).getFirst();
		op.setSucceedingOperators(indexScan.getSucceedingOperators());
	}
	
	/**
	 * This method determines the best sub graphs which should be joined next...
	 *
	 * @param indexScan the IndexScan operator to optimize
	 * @param jointree a {@link java.util.List} object.
	 * @return the indices in jointree of the two best subgraphs to be joined next...
	 */
	protected abstract Tuple<Integer, Integer> getBestNextSubgraphsToJoin(final BasicIndexScan indexScan, final List<Tuple<BasicOperator, T>> jointree);
	
	/**
	 * This method initializes the additional information and returns it...
	 *
	 * @param tp the triple pattern according to which the additional information is initialized...
	 * @return the initialized additional information (depending on the concrete join order algorithm)
	 */
	protected abstract T initAdditionalInformation(final TriplePattern tp);
	
	/**
	 * <p>mergeInitialInformations.</p>
	 *
	 * @param additionalInformation1 the additional information from the first subgraph
	 * @param additionalInformation2 the additional information from the second subgraph
	 * @return the merged additional information from two subgraphs
	 */
	protected abstract T mergeInitialInformations(final T additionalInformation1, final T additionalInformation2);
}
