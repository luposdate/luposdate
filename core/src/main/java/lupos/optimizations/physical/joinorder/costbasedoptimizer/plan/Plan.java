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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;

/**
 * This class represents a node in a plan for join ordering holding several information like joined triple patterns, joinpartner, if and how the joined result is ordered, the selectivity, estimated cardinality and cost, number of cartesian products and the computed histogram of the result
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class Plan implements Comparable<Plan>, Cloneable {

		/**
		 * the so far joined triple patterns
		 */
		protected List<TriplePattern> triplePatterns;

		/**
		 * the join partners between the left and right child
		 */
		protected HashSet<Variable> joinPartner;

		/**
		 * the variables appearing
		 */
		protected HashSet<Variable> variables;

		/**
		 * if and how the result of the join is ordered
		 */
		protected Collection<Variable> order;

		/**
		 * the number of merge joins
		 */
		protected int numberMergeJoins;

		/**
		 * the number of overall joins
		 */
		protected int numberJoins;

		/**
		 * the histogram of the result
		 */
		protected Map<Variable, VarBucket> selectivity;

		/**
		 * the estimated cardinality of the result
		 */
		private double card = 0.0;

		/**
		 * the estimated cost of the result
		 */
		protected double cost = 0.0;

		/**
		 * the number of cartesian products in this plan
		 */
		protected int numberOfCartesianProducts = 0;

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return this.toString("");
		}

		/**
		 * This method returns the string representation of this plan (using a certain indentation allowing a recursive construction of the string representation).
		 *
		 * @param indent the indentation used
		 * @return the string representation of this plan
		 */
		protected String toString(final String indent) {
			return indent + this.getNodeString() + "\n";
		}

		/**
		 * This method determines the string representation of this node without its children.
		 *
		 * @return the string representation of this node without its children
		 */
		protected String getNodeString() {
			String s = "[";
			for (final TriplePattern tp : this.triplePatterns) {
				s += "(";
				int i = 0;
				for (final Item item : tp) {
					if (item instanceof LazyLiteral) {
						s += ((LazyLiteral) item).getCode();
					} else {
						s += item.toString();
					}
					if (++i < 3) {
						s += ",";
					}
				}
				s += ")";
			}
			s += "]";
			return " order:" + this.order + ", number of joins:" + this.numberJoins
					+ ", number of merge joins:" + this.numberMergeJoins + ", card:"
					+ this.getCardinality() + ", cost:" + this.cost + ", selectivity:" + this.selectivity
					+ ", triple patterns:" + s;
		}

		/**
		 * <p>numberOfCartesianProducts.</p>
		 *
		 * @return the number of cartesian products in this plan
		 */
		protected int numberOfCartesianProducts() {
			return 0; // this method is overridden by InnerNode which returns a precise number
		}

		/**
		 * <p>Getter for the field <code>order</code>.</p>
		 *
		 * @return the order of the result
		 */
		public Collection<Variable> getOrder() {
			return this.order;
		}

		/**
		 * <p>Getter for the field <code>variables</code>.</p>
		 *
		 * @return the variables appearing in the result
		 */
		public HashSet<Variable> getVariables() {
			return this.variables;
		}

		/**
		 * <p>Getter for the field <code>joinPartner</code>.</p>
		 *
		 * @return the join partner of the join represented by this node
		 */
		public HashSet<Variable> getJoinPartner() {
			return this.joinPartner;
		}

		/**
		 * <p>Getter for the field <code>triplePatterns</code>.</p>
		 *
		 * @return all triple patterns which are joined by this plan
		 */
		public Collection<TriplePattern> getTriplePatterns() {
			return this.triplePatterns;
		}

		/**
		 * This method determines the estimated costs when using sideways information passing (SIP)
		 *
		 * @param cardinalityOtherOperand the cardinality of the other operand (this node is the operand with the higher cardinality in a succeeding join)
		 * @return the estimated costs when using SIP
		 */
		public double determineCostUsingSIP(final double cardinalityOtherOperand) {
			if (this.selectivity == null) {
				return 0.0;
			} else {
				if (this.selectivity.size() == 0 || cardinalityOtherOperand == 0.0) {
					return 1.0;
				} else {
					return cardinalityOtherOperand;
				}
			}
		}

		/**
		 * {@inheritDoc}
		 *
		 * This method is essential and determines if this or another plan is better.
		 */
		@Override
		public int compareTo(final Plan arg0) {
			// avoid Cartesian product
			if (arg0.numberOfCartesianProducts > this.numberOfCartesianProducts) {
				return -1;
			}
			if (this.numberOfCartesianProducts > arg0.numberOfCartesianProducts) {
				return 1;
			}

			// choose the plan with most merge joins!
			if (this.numberMergeJoins != arg0.numberMergeJoins) {
				if (this.numberMergeJoins > arg0.numberMergeJoins) {
					return -1;
				} else {
					return 1;
				}
			}

			// choose the plan with less joins!
			if (this.numberJoins != arg0.numberJoins) {
				if (this.numberJoins < arg0.numberJoins) {
					return -1;
				} else {
					return 1;
				}
			}

			if (this.cost == arg0.cost) {
				// normally 0. However, TreeSet will map them to the same entry
				// => make extra difference!
				return this.toString().compareTo(arg0.toString());
			}
			// choose the plan with less costs!
			return (this.cost < arg0.cost) ? -1 : 1;
		}

		/**
		 * This method checks whether or not a merge join can be used
		 *
		 * @param possibleOrdering the ordering which must be fulfilled
		 * @return a boolean.
		 */
		protected abstract boolean canUseMergeJoin(LinkedList<Variable> possibleOrdering);

		/**
		 * This method determines the maximum number of merge joins.
		 *
		 * @return the maximum number of possible merge joins
		 */
		public abstract int findMaxMergeJoins();

		/**
		 * This method determines if a merge join is possible for this node and how many merge joins occur then in this plan
		 *
		 * @param possibleOrdering the possible ordering to be checked
		 * @param remainingJoinPartner the remaining join partners to be checked if the result of the operands can be ordered such that a merge join can be applied
		 * @return -1 if no merge join can be applied
		 */
		protected int permutationOfOrderings(
				final LinkedList<Variable> possibleOrdering,
				final HashSet<Variable> remainingJoinPartner) {
			if (remainingJoinPartner.size() == 0) {
				// the possible ordering of the result (one of its permutations) is now complete => check if the operands can fulfill the ordering!
				if (!this.canUseMergeJoin(possibleOrdering)){
					return -1;
				}
				if(!this.checkOrdering(possibleOrdering)){
					return -1;
				}
				// this.numberMergeJoins has been set by checkOrdering!
				return this.numberMergeJoins;
			}
			/**
			 * try out all possible orderings of the result whether or not this ordering of the result can be fulfilled
			 */
			for (final Variable v : remainingJoinPartner) {
				@SuppressWarnings("unchecked")
				final LinkedList<Variable> zPossibleOrdering = (LinkedList<Variable>) possibleOrdering.clone();
				zPossibleOrdering.add(v);
				@SuppressWarnings("unchecked")
				final HashSet<Variable> zRemainingJoinPartner = (HashSet<Variable>) remainingJoinPartner.clone();
				zRemainingJoinPartner.remove(v);
				this.numberMergeJoins = this.permutationOfOrderings(zPossibleOrdering,zRemainingJoinPartner);
				if (this.numberMergeJoins > -1){
					// we have found an ordering of the result such that a merge join can be applied!
					return this.numberMergeJoins;
				}
			}
			// no ordering is possible for a merge join in this node
			return -1;
		}

		/**
		 * This method is called by permutationOfOrderings in order to check whether or not a given ordering is possible by this plan
		 *
		 * @param possibleOrdering the ordering to be checked
		 * @return a boolean.
		 */
		protected abstract boolean checkOrdering(final LinkedList<Variable> possibleOrdering);

		/**
		 * {@inheritDoc}
		 *
		 * This clone method overrides the standard clone method (just to specify that the result is again of type Plan in order to avoid cast operations)
		 */
		@Override
		public abstract Plan clone();

		/**
		 * <p>setCardinality.</p>
		 *
		 * @param card the cardinality to set
		 */
		public void setCardinality(final double card) {
			this.card = card;
		}

		/**
		 * <p>getCardinality.</p>
		 *
		 * @return the cardinality
		 */
		public double getCardinality() {
			return this.card;
		}

		/**
		 * <p>Getter for the field <code>cost</code>.</p>
		 *
		 * @return the cost
		 */
		public double getCost() {
			return this.cost;
		}

		/**
		 * <p>Getter for the field <code>selectivity</code>.</p>
		 *
		 * @return the histogram of this node...
		 */
		public Map<Variable, VarBucket> getSelectivity() {
			return this.selectivity;
		}
}
