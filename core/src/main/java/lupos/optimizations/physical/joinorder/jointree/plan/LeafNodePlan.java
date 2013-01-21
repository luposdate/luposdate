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
package lupos.optimizations.physical.joinorder.jointree.plan;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;

/**
 * This class represents a leaf node of the plan. 
 */
public class LeafNodePlan extends Plan {
	
	/**
	 * This constructor is used for cloning... 
	 * @param triplePatterns the triple patterns of this node (usually one if it is a leaf)
	 * @param joinPartner the join partners (usually empty for a leaf node)
	 * @param variables the variables appearing (can be determined from the triple patterns)
	 * @param order the order the result should follow
	 * @param numberMergeJoins the number of merge joins
	 * @param numberJoins the number of joins
	 * @param selectivity the selectivity
	 * @param card the cardinality of the result
	 * @param cost the estimated cost of the result
	 */
	public LeafNodePlan(final List<TriplePattern> triplePatterns,
			final HashSet<Variable> joinPartner,
			final HashSet<Variable> variables,
			final Collection<Variable> order, final int numberMergeJoins,
			final int numberJoins,
			final Map<Variable, VarBucket> selectivity, final double card,
			final double cost) {
		this.triplePatterns = triplePatterns;
		this.joinPartner = joinPartner;
		this.variables = variables;
		this.order = order;
		this.numberMergeJoins = numberMergeJoins;
		this.numberJoins = numberJoins;
		this.selectivity = selectivity;
		this.setCardinality(card);
		this.cost = cost;
	}

	/**
	 * This constructor is normally used to set up the leaf node. Missing information like cardinality is computed...
	 * @param tp the triple pattern of this leaf node
	 * @param indexScan the index scan operator such that the histogram of the result can be determined
	 * @param classBindings the currently used Bindings class
	 * @param minima the minimum values of a variable
	 * @param maxima the maximum values of a variable
	 */
	public LeafNodePlan(
			final TriplePattern tp, 
			final BasicIndexScan indexScan,
			final Class<? extends Bindings> classBindings,
			HashMap<Variable, Literal> minima,
			HashMap<Variable, Literal> maxima) {
		this.triplePatterns = new LinkedList<TriplePattern>();
		this.triplePatterns.add(tp);
		this.joinPartner = tp.getVariables();
		this.variables = tp.getVariables();
		this.numberMergeJoins = 0;
		this.numberJoins = 0;
		this.order = new LinkedList<Variable>();
		// determine the selectivity of this triple pattern
		this.selectivity = indexScan.getVarBuckets(tp, classBindings, this.joinPartner, minima, maxima);
		// determine the cardinality and cost of this triple pattern...
		if (this.selectivity == null) {
			this.cost = 0.0;
			this.setCardinality(0.0);
			tp.setCardinality(0);
		} else {
			if (this.selectivity.size() == 0) {
				this.cost = 1.0;
				this.setCardinality(0.0);
				tp.setCardinality(0);
			} else {
				this.setCardinality(this.selectivity.values().iterator().next().getSum());
				this.cost = this.getCardinality();
				tp.setCardinality((long) this.getCardinality());
			}
		}
	}

	/**
	 * This method checks whether or not the ordering can be fulfilled by this leaf node (i.e., all variables of the given ordering to check appear in the result?) 
	 */
	@Override
	protected boolean canUseMergeJoin(final LinkedList<Variable> possibleOrdering) {
		if (!this.variables.containsAll(possibleOrdering)){
			return false;
		}
		this.order = possibleOrdering;
		return true;
	}

	@Override
	public int findMaxMergeJoins() {
		return 0; // leaf nodes do not have any joins and therefore also no merge joins!
	}

	@Override
	protected String getNodeString() {
		return "+ Leaf:" + super.getNodeString();
	}
	
	@Override
	public LeafNodePlan clone(){
		return new LeafNodePlan(this.triplePatterns, this.joinPartner, this.joinPartner, this.order, this.numberJoins, this.numberJoins, this.selectivity, this.getCardinality(), this.getCardinality());
	}

	@Override
	protected boolean checkOrdering(LinkedList<Variable> possibleOrdering) {
		// Leaf node does not count as merge join! => this.numberMergeJoin remains 0 through whole lifetime of this leaf node!
		// Any ordering is anyway possible in the leaf node!
		this.order = possibleOrdering;
		return true;
	}
}
