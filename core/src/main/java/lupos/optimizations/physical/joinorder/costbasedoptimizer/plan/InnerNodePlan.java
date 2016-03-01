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
package lupos.optimizations.physical.joinorder.costbasedoptimizer.plan;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.Statistics;
import lupos.optimizations.logical.statistics.VarBucket;

/**
 * This class represents an inner node of the plan =&gt; join of two other plans
 *
 * @author groppe
 * @version $Id: $Id
 */
public class InnerNodePlan extends Plan {

	/**
	 * the left and right child, which are joined together...
	 */
	protected Plan left, right;

	/**
	 * The join type (merge join or default)
	 */
	protected JoinType joinType;

	/**
	 * This method returns the left child of this inner node
	 *
	 * @return the left child
	 */
	public Plan getLeft() {
		return this.left;
	}

	/**
	 * This method returns the right child of this inner node
	 *
	 * @return the right child
	 */
	public Plan getRight() {
		return this.right;
	}

	/**
	 * This method returns the join type of this inner node
	 *
	 * @return the join type
	 */
	public JoinType getJoinType() {
		return this.joinType;
	}

	/**
	 * Constructor for constructing an inner node representing a join between two children
	 *
	 * @param left the left child
	 * @param right the right child
	 */
	public InnerNodePlan(final Plan left, final Plan right) {
		this.left = left;
		this.right = right;
		// combine the triple patterns of the left and right child
		this.triplePatterns = new LinkedList<TriplePattern>();
		this.triplePatterns.addAll(left.triplePatterns);
		this.triplePatterns.addAll(right.triplePatterns);
		// determine all variables occurring in the subplans
		this.variables = new HashSet<Variable>();
		this.variables.addAll(left.variables);
		this.variables.addAll(right.variables);
		// determine the join partners...
		this.joinPartner = new HashSet<Variable>();
		this.joinPartner.addAll(left.variables);
		this.joinPartner.retainAll(right.variables);
		// compute the number of joins
		this.numberJoins = 1 + left.numberJoins + right.numberJoins;
		// estimate the join selectivity
		this.selectivity = Statistics.estimateJoinSelectivity(left.selectivity, right.selectivity);
		// compute the number of cartesian products
		this.numberOfCartesianProducts = this.numberOfCartesianProducts();
		// compute the cardinality of the join result as well as its cost
		if (this.selectivity == null) {
			this.cost = 0.0;
			this.setCardinality(0.0);
		} else {
			if (this.selectivity.isEmpty()) {
				this.cost = 0.0;
				this.setCardinality(0.0);
			} else {
				this.setCardinality(this.selectivity.values().iterator().next().getSum());
				this.cost = this.determineCostUsingSIP(this.getCardinality());
			}
		}
		// now compute the join type, i.e. determine if we can apply a merge
		// join on already correct sorted intermediate results from previous joins
		this.findMaxMergeJoins();
	}

	/** {@inheritDoc} */
	@Override
	public double determineCostUsingSIP(final double cardinalityOtherOperand) {
		if (this.selectivity == null) {
			return 0.0;
		} else {
			if (this.selectivity.isEmpty()) {
				return 0.0;
			} else {
				double costV = cardinalityOtherOperand + this.left.cost + this.right.cost;
				if (this.joinPartner.isEmpty()) {
					// cartesian product! Set the cost high to avoid
					// cartesian products!
					costV *= 10.0;
				} else if (this.joinType != JoinType.MERGEJOIN) {
					// no direct merge join can be applied! Set the cost
					// high (but lower than cart. products) to avoid hash
					// joins etc.!
					costV *= 3.0;
				} else {
					// Check if SIP information can be used efficiently.
					// This is done by checking if the operand with the
					// larger intermediate result has similar number of
					// literals and distinct literals (i.e., the cardinality
					// of the other operand determines the overall
					// cardinality)
					// Then just reduce the cost of the operand with the
					// larger intermediate result!
					final Map<Variable, VarBucket> selectivityL = (this.left.getCardinality() < this.right.getCardinality()) ? this.right.selectivity : this.left.selectivity;
					boolean flag = true;
					for (final Variable v : this.joinPartner) {
						final double cardV = selectivityL.get(v).getSum();
						final double distinctLiteralsV = selectivityL.get(v).getSumDistinctLiterals();
						if (cardV / distinctLiteralsV > 1.2){
							flag = false;
							break;
						}
					}
					if (flag) {
						if (this.left.getCardinality() < this.right.getCardinality()) {
							return cardinalityOtherOperand
									+ this.left.cost
									+ this.right.determineCostUsingSIP(this.left.getCardinality());
						} else {
							return cardinalityOtherOperand
									+ this.right.cost
									+ this.left.determineCostUsingSIP(this.right.getCardinality());
						}
					}
				}
				return costV;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected int numberOfCartesianProducts() {
		final int numberOfCartesianProductsSubTrees = this.left.numberOfCartesianProducts() + this.right.numberOfCartesianProducts();
		// is this join a cartesian product?
		if (this.joinPartner.isEmpty()) {
			return numberOfCartesianProductsSubTrees + 1;
		} else {
			return numberOfCartesianProductsSubTrees;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int findMaxMergeJoins() {
		if (!this.joinPartner.isEmpty()){
			this.numberMergeJoins = this.permutationOfOrderings(new LinkedList<Variable>(), this.joinPartner);
		} else {
			this.numberMergeJoins = -1;
		}

		if (this.numberMergeJoins == -1) {
			this.joinType = JoinType.DEFAULT;
			final int numberMergeJoins1 = this.left.findMaxMergeJoins();
			final int numberMergeJoins2 = this.right.findMaxMergeJoins();
			this.numberMergeJoins = numberMergeJoins1 + numberMergeJoins2;
		} else {
			this.joinType = JoinType.MERGEJOIN;
		}

		return this.numberMergeJoins;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean canUseMergeJoin(
			final LinkedList<Variable> possibleOrdering) {
		// can we guarantee the correct ordering?
		// We can fulfill the correct ordering whenever
		// first only joinpartner appear in the ordering,
		// then only variables from the left operand and
		// then only variables from the right operand
		int state = 0;
		for (final Variable v : possibleOrdering) {
			switch (state) {
			case 0:
				if (!this.joinPartner.contains(v)) {
					if (this.left.variables.contains(v)){
						state = 1;
					} else if (this.right.variables.contains(v)){
						state = 2;
					} else {
						return false;
					}
				}
				break;
			case 1:
				if (this.joinPartner.contains(v)){
					return false;
				}
				if (this.right.variables.contains(v)){
					state = 2;
				}
				break;
			case 2:
				if (this.joinPartner.contains(v)){
					return false;
				}
				if (this.left.variables.contains(v)){
					return false;
				}
				break;
			default:
				throw new RuntimeException("Should never happen!");
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected String getNodeString() {
		return "+ INNER NODE: join type:"
				+ JoinType.values()[this.joinType.ordinal()]
				+ super.getNodeString();
	}

	/** {@inheritDoc} */
	@Override
	protected String toString(final String indent) {
		return super.toString(indent) + this.left.toString(indent + "|") + this.right.toString(indent + "|");
	}

	/** {@inheritDoc} */
	@Override
	public InnerNodePlan clone() {
		// clone deeply!
		return new InnerNodePlan(this.left.clone(), this.right.clone());
	}

	/** {@inheritDoc} */
	@Override
	protected boolean checkOrdering(final LinkedList<Variable> possibleOrdering) {
		this.numberMergeJoins = 1; // in case of success this node represents a merge join
		if (this.left != null) {
			@SuppressWarnings("unchecked")
			final LinkedList<Variable> po2 = (LinkedList<Variable>) possibleOrdering.clone();
			@SuppressWarnings("unchecked")
			final HashSet<Variable> jp = (HashSet<Variable>) this.left.joinPartner.clone();
			jp.removeAll(po2);
			// permute all the orderings of the join partners of the left operand except those which are already ordered
			final int numberMergeJoins2 = this.left.permutationOfOrderings(po2, jp);
			if (numberMergeJoins2 == -1){
				// ordering in the left operand is not possible!
				return false;
			}
			this.numberMergeJoins += numberMergeJoins2;
		}
		if (this.right != null) {
			@SuppressWarnings("unchecked")
			final LinkedList<Variable> po2 = (LinkedList<Variable>) possibleOrdering.clone();
			@SuppressWarnings("unchecked")
			final HashSet<Variable> jp = (HashSet<Variable>) this.right.joinPartner.clone();
			jp.removeAll(po2);
			// permute all the orderings of the join partners of the right operand except those which are already ordered
			final int numberMergeJoins2 = this.right.permutationOfOrderings(po2, jp);
			if (numberMergeJoins2 == -1){
				// ordering in the right operand is not possible!
				return false;
			}
			this.numberMergeJoins += numberMergeJoins2;
		}
		this.order = possibleOrdering; // remember the ordering such that it is taken care of in the final operator graph
		return true;
	}
}
