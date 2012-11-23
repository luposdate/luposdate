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
package lupos.optimizations.physical.joinorder.jointree;

import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.optimizations.physical.joinorder.jointree.operatorgraphgenerator.RDF3XOperatorGraphGenerator;

/**
 * This class is the cost-based optimizer for the RDF3X query evaluator. 
 */
public class RDF3XCostBasedOptimizer extends CostBasedOptimizer {

	/**
	 * Constructor for the choice of n-ary versus binary merge joins, and enforcing always merge joins by eventual preceding sorting phases or using other join algorithms for unsorted data  
	 * @param RDF3XSORT true, if merge joins should be enforced by eventual preceding sorting phases; false, if other join algorithms are used whenever the data is not already sorted in the right way
	 * @param NARYMERGEJOIN n-ary (true) versus binary (false) merge joins
	 */	
	public RDF3XCostBasedOptimizer(final boolean RDF3XSORT, final boolean NARYMERGEJOIN) {
		super(new RDF3XOperatorGraphGenerator(RDF3XSORT, NARYMERGEJOIN));
	}

	/**
	 * Static method to call the cost-based optimizer for the RDF3X query evaluator 
	 * @param indexScan the IndexScan operator with at least two triple patterns to join....
	 * @param RDF3XSORT true, if merge joins should be enforced by eventual preceding sorting phases; false, if other join algorithms are used whenever the data is not already sorted in the right way
	 * @param NARYMERGEJOIN n-ary (true) versus binary (false) merge joins
	 * @return the root operator under which the subgraph with the reordered joins are inserted
	 */
	public static Root rearrangeJoinOrder(BasicIndexScan indexScan, final boolean RDF3XSORT, final boolean NARYMERGEJOIN){
		Root newRoot = indexScan.getRoot().newInstance(indexScan.getRoot().dataset);		
		RDF3XCostBasedOptimizer optimizer = new RDF3XCostBasedOptimizer(RDF3XSORT, NARYMERGEJOIN);
		optimizer.rearrangeJoinOrder(newRoot, indexScan);
		return newRoot;
	}
}
