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
package lupos.optimizations.logical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.index.memoryindex.MemoryIndexScan;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting;
import lupos.engine.operators.multiinput.join.NAryMergeJoinWithoutSorting;
import lupos.engine.operators.singleinput.sort.fastsort.FastSort;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.statistics.Statistics;
import lupos.optimizations.logical.statistics.VarBucket;

public class OptimizeJoinOrder {

	public enum JoinType {
		DEFAULT, MERGEJOIN
	};

	public enum PlanType {
		RDF3X, RDF3XSORT, RELATIONALINDEX
	};

	private static int limitForSplitting = 7;

	public abstract class Plan implements Comparable<Plan>, Cloneable {
		protected Plan left, right;
		protected List<TriplePattern> triplePatterns;
		protected HashSet<Variable> joinPartner;
		protected HashSet<Variable> variables;
		protected Collection<Variable> order;
		protected int numberMergeJoins;
		protected int numberJoins;
		protected Map<Variable, VarBucket> selectivity;
		protected double card = 0.0;
		protected double cost = 0.0;
		protected int numberOfCartesianProducts = 0;

		@Override
		public String toString() {
			return toString("");
		}

		private String toString(final String ident) {
			String result = ident + getNodeString() + "\n";
			if (left != null) {
				result += left.toString(ident + "|");
			}
			if (right != null) {
				result += right.toString(ident + "|");
			}
			return result;
		}

		protected String getNodeString() {
			String s = "[";
			for (final TriplePattern tp : triplePatterns) {
				s += "(";
				int i = 0;
				for (final Item item : tp) {
					if (item instanceof LazyLiteral) {
						s += ((LazyLiteral) item).getCode();
					} else
						s += item.toString();
					if (++i < 3)
						s += ",";
				}
				s += ")";
			}
			s += "]";
			return " order:" + order + ", number of joins:" + numberJoins
					+ ", number of merge joins:" + numberMergeJoins + ", card:"
					+ card + ", cost:" + cost + ", selectivity:" + selectivity
					+ ", triple patterns:" + s;
		}

		protected int numberOfCartesianProducts() {
			return 0;
		}

		public Collection<Variable> getOrder() {
			return order;
		}

		public HashSet<Variable> getVariables() {
			return variables;
		}

		public HashSet<Variable> getJoinPartner() {
			return joinPartner;
		}

		public Collection<TriplePattern> getTriplePatterns() {
			return triplePatterns;
		}

		public double determineCostUsingSIP(final double cardinalityOtherOperand) {
			if (selectivity == null) {
				return 0.0;
			} else {
				if (selectivity.size() == 0 || cardinalityOtherOperand == 0.0) {
					return 1.0;
				} else {
					return cardinalityOtherOperand;
				}
			}
		}

		public int compareTo(final Plan arg0) {
			// avoid Cartesian product
			if (arg0.numberOfCartesianProducts > this.numberOfCartesianProducts)
				return -1;
			if (this.numberOfCartesianProducts > arg0.numberOfCartesianProducts)
				return 1;

			// choose the plan with most merge joins!
			if (numberMergeJoins != arg0.numberMergeJoins) {
				if (numberMergeJoins > arg0.numberMergeJoins)
					return -1;
				else
					return 1;
				// if ((double)numberMergeJoins/numberJoins !=
				// (double)arg0.numberMergeJoins/arg0.numberJoins)
				// return ((double)numberMergeJoins/numberJoins <
				// (double)arg0.numberMergeJoins/arg0.numberJoins) ? -1 : 1;
				// else
				// System.out.println(
				// "Two plans have the same |MergeJoins|/|Joins|");
			}

			// choose the plan with less joins!
			if (numberJoins != arg0.numberJoins) {
				if (numberJoins < arg0.numberJoins)
					return -1;
				else
					return 1;
			}

			if (cost == arg0.cost) {
				// normally 0. However, TreeSet will map them to the same entry
				// => make extra difference!
				return this.toString().compareTo(arg0.toString());
			}
			// choose the plan with less costs!
			return (cost < arg0.cost) ? -1 : 1;
		}

		protected abstract boolean canUseMergeJoin(
				LinkedList<Variable> possibleOrdering);

		protected abstract int findMaxMergeJoins();

		@Override
		public Object clone() {
			if (this instanceof LeafNodePlan) {
				return new LeafNodePlan(triplePatterns, joinPartner, variables,
						order, numberMergeJoins, numberJoins, selectivity,
						card, cost);
			} else {
				return new InnerNodePlan((Plan) left.clone(), (Plan) right
						.clone());
			}
		}

		protected int permutationOfOrderings(
				final LinkedList<Variable> possibleOrdering,
				final HashSet<Variable> remainingJoinPartner) {
			if (remainingJoinPartner.size() == 0) {
				if (!this.canUseMergeJoin(possibleOrdering))
					return -1;
				// leaf node? => does not count as merge join!
				if (left == null && right == null) {
					order = possibleOrdering;
					return 0;
				}
				numberMergeJoins = 1;
				if (left != null) {
					final LinkedList<Variable> po2 = (LinkedList<Variable>) possibleOrdering
							.clone();
					final HashSet<Variable> jp = (HashSet<Variable>) left.joinPartner
							.clone();
					jp.removeAll(po2);
					final int numberMergeJoins2 = left.permutationOfOrderings(
							po2, jp);
					if (numberMergeJoins2 == -1)
						return -1;
					numberMergeJoins += numberMergeJoins2;
				}
				if (right != null) {
					final LinkedList<Variable> po2 = (LinkedList<Variable>) possibleOrdering
							.clone();
					final HashSet<Variable> jp = (HashSet<Variable>) right.joinPartner
							.clone();
					jp.removeAll(po2);
					final int numberMergeJoins2 = right.permutationOfOrderings(
							po2, jp);
					;
					if (numberMergeJoins2 == -1)
						return -1;
					numberMergeJoins += numberMergeJoins2;
				}
				order = possibleOrdering;
				return numberMergeJoins;
			}
			for (final Variable v : remainingJoinPartner) {
				final LinkedList<Variable> zPossibleOrdering = (LinkedList<Variable>) possibleOrdering
						.clone();
				zPossibleOrdering.add(v);
				final HashSet<Variable> zRemainingJoinPartner = (HashSet<Variable>) remainingJoinPartner
						.clone();
				zRemainingJoinPartner.remove(v);
				numberMergeJoins = permutationOfOrderings(zPossibleOrdering,
						zRemainingJoinPartner);
				if (numberMergeJoins > -1)
					return numberMergeJoins;
			}
			return -1;
		}
	}

	public class LeafNodePlan extends Plan {
		LeafNodePlan(final List<TriplePattern> triplePatterns,
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
			this.card = card;
			this.cost = cost;
		}

		LeafNodePlan(final TriplePattern tp, final BasicIndexScan index,				
				final Class<? extends Bindings> classBindings,
				final HashSet<Variable> joinPartners) {
			triplePatterns = new LinkedList<TriplePattern>();
			triplePatterns.add(tp);
			joinPartner = tp.getVariables();
			variables = tp.getVariables();
			numberMergeJoins = 0;
			numberJoins = 0;
			order = new LinkedList<Variable>();
			// TODO compute cardinality (==cost) of triple pattern
			selectivity = index.getVarBuckets(tp, classBindings,
					joinPartners, minima, maxima);
			if (selectivity == null) {
				cost = 0.0;
				card = 0.0;
			} else {
				if (selectivity.size() == 0) {
					cost = 1.0;
					card = 0.0;
					tp.setCardinality(0);
				} else {
					card = selectivity.values().iterator().next().getSum();
					cost = card;
					tp.setCardinality((long) card);
				}
			}
		}

		LeafNodePlan(final TriplePattern tp, final double card,
				final double cost) {
			triplePatterns = new LinkedList<TriplePattern>();
			triplePatterns.add(tp);
			joinPartner = tp.getVariables();
			variables = tp.getVariables();
			numberMergeJoins = 0;
			numberJoins = 0;
			order = new LinkedList<Variable>();
			this.cost = cost;
			this.card = card;
		}

		@Override
		protected boolean canUseMergeJoin(
				final LinkedList<Variable> possibleOrdering) {
			if (!variables.containsAll(possibleOrdering))
				return false;
			order = possibleOrdering;
			return true;
		}

		@Override
		protected int findMaxMergeJoins() {
			return 0;
		}

		@Override
		protected String getNodeString() {
			return "+ Leaf:" + super.getNodeString();
		}
	}

	public class InnerNodePlan extends Plan {
		protected JoinType joinType;

		public Plan getLeft() {
			return left;
		}

		public Plan getRight() {
			return right;
		}

		public JoinType getJoinType() {
			return joinType;
		}

		protected InnerNodePlan(final Plan left, final Plan right) {
			this.left = left;
			this.right = right;
			triplePatterns = new LinkedList<TriplePattern>();
			triplePatterns.addAll(left.triplePatterns);
			triplePatterns.addAll(right.triplePatterns);
			variables = new HashSet<Variable>();
			variables.addAll(left.variables);
			variables.addAll(right.variables);
			joinPartner = new HashSet<Variable>();
			joinPartner.addAll(left.variables);
			joinPartner.retainAll(right.variables);
			numberJoins = 1 + left.numberJoins + right.numberJoins;
			// TODO Estimate cardinality (=cost) of new plan!
			selectivity = Statistics.estimateJoinSelectivity(left.selectivity,
					right.selectivity);
			this.numberOfCartesianProducts = this.numberOfCartesianProducts();
			if (selectivity == null) {
				cost = 0.0;
				card = 0.0;
			} else {
				if (selectivity.size() == 0) {
					cost = 0.0;
					card = 0.0;
				} else {
					card = selectivity.values().iterator().next().getSum();
					cost = card + left.cost + right.cost;
					if (joinPartner.size() == 0)
						// cartesian product! Set the cost high to avoid
						// cartesian products!
						cost = Double.POSITIVE_INFINITY;
					else if (joinType != JoinType.MERGEJOIN) {
						// no direct merge join can be applied! Set the cost
						// high (but lower than cart. products) to avoid hash
						// joins etc.!
						cost *= 3.0;
						// if the left or right operand is a leaf node,
						// then SIP cannot be used to jump over big gaps
						// => assign more costs in these cases to avoid
						// these plans!
						// if (left instanceof LeafNodePlan
						// || right instanceof LeafNodePlan) {
						// cost *= 3.0;
						// }
					} else {
						// Check if SIP information can be used efficiently.
						// This is done by checking if the operand with the
						// larger
						// intermediate result has similar number of
						// literals and distinct literals (i.e., the cardinality
						// of the other operand determines the overall
						// cardinality)
						// Then just reduce the cost of the operand with the
						// larger
						// intermediate result!
						final Map<Variable, VarBucket> selectivityL = (left.card < right.card) ? right.selectivity
								: left.selectivity;
						boolean flag = true;
						for (final Variable v : joinPartner) {
							final double cardV = selectivityL.get(v).getSum();
							final double distinctLiteralsV = selectivityL
									.get(v).getSumDistinctLiterals();
							if (cardV / distinctLiteralsV > 1.2)
								flag = false;
						}
						if (flag) {
							if (left.card < right.card) {
								cost = card
										+ left.cost
										+ right
												.determineCostUsingSIP(left.card);
							} else {
								cost = card
										+ right.cost
										+ left
												.determineCostUsingSIP(right.card);
							}
						}
					}
				}
			}

			// now compute the join type, i.e. determine if we can apply a merge
			// join on already correct sorted intermediate results from previous
			// joins
			findMaxMergeJoins();
		}

		@Override
		public double determineCostUsingSIP(final double cardinalityOtherOperand) {
			if (selectivity == null) {
				return 0.0;
			} else {
				if (selectivity.size() == 0) {
					return 0.0;
				} else {
					double costV = cardinalityOtherOperand + left.cost
							+ right.cost;
					if (joinPartner.size() == 0)
						// cartesian product! Set the cost high to avoid
						// cartesian products!
						costV *= 10.0;
					else if (joinType != JoinType.MERGEJOIN) {
						// no direct merge join can be applied! Set the cost
						// high (but lower than cart. products) to avoid hash
						// joins etc.!
						costV *= 3.0;
						// if the left or right operand is a leaf node,
						// then SIP cannot be used to jump over big gaps
						// => assign more costs in these cases to avoid
						// these plans!
						// if (left instanceof LeafNodePlan
						// || right instanceof LeafNodePlan) {
						// cost *= 3.0;
						// }
					} else {
						// Check if SIP information can be used efficiently.
						// This is done by checking if the operand with the
						// larger
						// intermediate result has similar number of
						// literals and distinct literals (i.e., the cardinality
						// of the other operand determines the overall
						// cardinality)
						// Then just reduce the cost of the operand with the
						// larger
						// intermediate result!
						final Map<Variable, VarBucket> selectivityL = (left.card < right.card) ? right.selectivity
								: left.selectivity;
						boolean flag = true;
						for (final Variable v : joinPartner) {
							final double cardV = selectivityL.get(v).getSum();
							final double distinctLiteralsV = selectivityL
									.get(v).getSumDistinctLiterals();
							if (cardV / distinctLiteralsV > 1.2)
								flag = false;
						}
						if (flag) {
							if (left.card < right.card) {
								return cardinalityOtherOperand
										+ left.cost
										+ right
												.determineCostUsingSIP(left.card);
							} else {
								return cardinalityOtherOperand
										+ right.cost
										+ left
												.determineCostUsingSIP(right.card);
							}
						}
					}
					return costV;
				}
			}
		}

		@Override
		protected int numberOfCartesianProducts() {
			if (this.joinPartner.size() == 0)
				return this.left.numberOfCartesianProducts()
						+ this.right.numberOfCartesianProducts() + 1;
			else
				return this.left.numberOfCartesianProducts()
						+ this.right.numberOfCartesianProducts();
		}

		@Override
		protected int findMaxMergeJoins() {
			if (joinPartner.size() > 0)
				numberMergeJoins = permutationOfOrderings(
						new LinkedList<Variable>(), joinPartner);
			else
				numberMergeJoins = -1;

			if (numberMergeJoins == -1) {
				joinType = JoinType.DEFAULT;
				final int numberMergeJoins1 = left.findMaxMergeJoins();
				final int numberMergeJoins2 = right.findMaxMergeJoins();
				numberMergeJoins = numberMergeJoins1 + numberMergeJoins2;
			} else
				joinType = JoinType.MERGEJOIN;

			return numberMergeJoins;
		}

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
					if (!joinPartner.contains(v)) {
						if (left.variables.contains(v))
							state = 1;
						else if (right.variables.contains(v))
							state = 2;
						else
							return false;
					}
					break;
				case 1:
					if (joinPartner.contains(v))
						return false;
					if (right.variables.contains(v))
						state = 2;
					break;
				case 2:
					if (joinPartner.contains(v))
						return false;
					if (left.variables.contains(v))
						return false;
					break;
				default:
					System.out.println("Should never happen! 2");
				}
			}
			return true;
		}

		@Override
		protected String getNodeString() {
			return "+ INNER NODE: join type:"
					+ JoinType.values()[joinType.ordinal()]
					+ super.getNodeString();
		}
	}

	protected HashMap<Variable, Literal> minima = new HashMap<Variable, Literal>();
	protected HashMap<Variable, Literal> maxima = new HashMap<Variable, Literal>();

	public Plan getBestPlan(final List<TriplePattern> ctp,
			final BasicIndexScan index, final Dataset dataset,
			final PlanType planType) {
		// final HashMap<Long,SortedSet<Plan>>[] bestPlans = new
		// HashMap[ctp.size()];
		// bestPlans[0] = new HashMap<Long, SortedSet<Plan>>();
		final List<LeafNodePlan> initialPlans = Collections
				.synchronizedList(new LinkedList<LeafNodePlan>());
		final LinkedList<Thread> intialPlansThreads = new LinkedList<Thread>();
		// setFlagEmptyResult(false);
		final Class<? extends Bindings> classBindings = Bindings.instanceClass;
		Bindings.instanceClass = BindingsArrayReadTriples.class;
		// determine all join partners of the triple partners
		// afterwards only generate histograms for the join partners
		final HashSet<Variable> joinPartners = new HashSet<Variable>();
		for (final TriplePattern tp : ctp) {
			joinPartners.addAll(tp.getVariables());
		}
		final int[] count = new int[joinPartners.size()];
		joinPartners.clear();
		final HashMap<Variable, Integer> map = new HashMap<Variable, Integer>();
		// count the occurrences of the variables in the triple patterns
		for (final TriplePattern tp : ctp) {
			for (final Variable v : tp.getVariables()) {
				Integer i = map.get(v);
				if (i == null) {
					i = map.size();
					map.put(v, i);
				}
				count[i]++;
			}
		}
		for (final Variable v : map.keySet()) {
			if (count[map.get(v)] > 1)
				joinPartners.add(v);
		}
		minima.clear();
		maxima.clear();
		// determine minimum and maximum of the join partners!
		for (final Variable v : joinPartners) {
			Literal min = null;
			Literal max = null;
			for (final TriplePattern tp : ctp) {
				if (tp.getVariables().contains(v)) {
					final Tuple<Literal, Literal> minMax = index.getMinMax(v, tp);
					if (minMax != null) {
						if (minMax.getFirst() != null) {
							if (min == null
									|| min
											.compareToNotNecessarilySPARQLSpecificationConform(minMax
													.getFirst()) <= 0)
								min = minMax.getFirst();
						}
						if (minMax.getSecond() != null) {
							if (max == null
									|| max
											.compareToNotNecessarilySPARQLSpecificationConform(minMax
													.getSecond()) >= 0)
								max = minMax.getSecond();
						}
					}
				}
			}
			// if (min != null && max != null) {
			// if (min.compareToNotNecessarilySPARQLSpecificationConform(max) <=
			// 0) {
			// minima.put(v, min);
			// maxima.put(v, max);
			// }
			// } else {
			if (min != null)
				minima.put(v, min);
			if (max != null)
				maxima.put(v, max);
			// }
		}
		// now generate initial plans of the leaf nodes!
		for (final TriplePattern tp : ctp) {
			// final SortedSet<Plan> best=new TreeSet<Plan>();
			// best.add(new LeafNodePlan(tp, index, dataset));
			// bestPlans[0].put(key, best);
			// if ((planType != PlanType.RDF3XSORT && planType !=
			// PlanType.VLDB2008SORT)
			// && (ctp.size() <= 1 || (planType != PlanType.RELATIONALINDEX &&
			// ctp
			// .size() <= 2)))
			// we do not have to estimate the card. + cost for less than
			// three triple patterns! This is clear for one triple pattern.
			// For two triple patterns:
			// as only a join between two triple patterns is possible: (for
			// most approaches except IndexQueryEvaluator) it does not
			// matter, which is the left or right triple pattern!
			// bestPlans[0].put(key, new LeafNodePlan(tp, 1.0, 1.0));
			// initialPlans.add(new LeafNodePlan(tp, 1.0, 1.0));
			// else {
			final Thread thread = new Thread() {
				final TriplePattern tp2 = tp;
				final BasicIndexScan index2 = (BasicIndexScan) index.clone();

				@Override
				public void run() {
					index2.setTriplePatterns(new LinkedList<TriplePattern>());
					final LeafNodePlan leafNodePlan = new LeafNodePlan(tp2,
							index2, classBindings, joinPartners);
					// if (leafNodePlan.card.compareTo(ZERO) == 0) {
					// // this triple pattern returns the empty result =>
					// // we need to compute only an empty result!
					// setFlagEmptyResult(true);
					// }
					// bestPlans[0].put(key, new LeafNodePlan(tp, index,
					// dataset));
					initialPlans.add(leafNodePlan);
				}
			};
			startThread(thread, intialPlansThreads);
			// sequential: thread.run();
		}
		// }
		for (final Thread thread : intialPlansThreads) {
			try {
				thread.join();
				lockNumberOfThreads.lock();
				try {
					numberThreads--;
				} finally {
					lockNumberOfThreads.unlock();
				}
			} catch (final InterruptedException e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		Bindings.instanceClass = classBindings;
		// if (flagEmptyResult)
		// return null;
		return getBestPlanBeforeSplittingHugeStarShapedJoins(ctp, index,
				dataset, planType, initialPlans);
	}

	// private boolean flagEmptyResult;
	//
	// private synchronized void setFlagEmptyResult(final boolean flag) {
	// flagEmptyResult = flag;
	// }

	private Plan getBestPlanBeforeSplittingHugeStarShapedJoins(
			final BasicIndexScan index, final Dataset dataset,
			final PlanType planType, final List<LeafNodePlan> initialPlans) {
		final List<TriplePattern> ctp = new LinkedList<TriplePattern>();
		for (final LeafNodePlan leafNodePlan : initialPlans) {
			ctp.addAll(leafNodePlan.getTriplePatterns());
		}
		return getBestPlanAfterSplittingHugeStarShapedJoins(ctp, index,
				dataset, planType, initialPlans);
	}

	private Plan getBestPlanBeforeSplittingHugeStarShapedJoins(
			final List<TriplePattern> ctp, final BasicIndexScan index,
			final Dataset dataset, final PlanType planType,
			final List<LeafNodePlan> initialPlans) {

		// Are there any cartesian products, where we can split the plan?
		final List<List<TriplePattern>> cartesianProducts = cartesianProducts(ctp);
		if (cartesianProducts.size() > 1) {
			// split the plan by joining the most triple patterns with the
			// rest!

			final List<TriplePattern>[] solution = new List[cartesianProducts
					.size()];
			final Iterator<List<TriplePattern>> tpsIterator = cartesianProducts
					.iterator();
			for (int i = 0; i < cartesianProducts.size(); i++)
				solution[i] = tpsIterator.next();
			int max = 0;

			for (int i = 1; i < solution.length; i++) {
				if (solution[i].size() > solution[max].size())
					max = i;
			}
			return getBestPlanBeforeSplittingHugeStarShapedJoins(ctp, index,
					dataset, planType, initialPlans, solution[max]);
		}

		if (ctp.size() > limitForSplitting) {
			// We have to split the plan, otherwise our plan generator becomes
			// too slow!

			// Is there one triple pattern, which connects two subgraphs,
			// between which is no other edge than the one of the considered
			// triple pattern?
			final LinkedList<List<TriplePattern>> possibleSolutions = new LinkedList<List<TriplePattern>>();
			for (final TriplePattern tp : ctp) {
				final LinkedList<TriplePattern> withoutTP = new LinkedList<TriplePattern>();
				withoutTP.addAll(ctp);
				withoutTP.remove(tp);
				final List<List<TriplePattern>> cartesianProducts2 = cartesianProducts(withoutTP);
				if (cartesianProducts2.size() > 1) {
					boolean flag = true;
					for (final List<TriplePattern> ltp : cartesianProducts2) {
						if (ltp.size() <= 1)
							flag = false;
					}
					if (flag) {
						for (final List<TriplePattern> ltp : cartesianProducts2) {
							ltp.add(tp);
							possibleSolutions.add(ltp);
						}
					}
				}
			}
			final LinkedList<Plan> bestPlans2 = new LinkedList<Plan>();
			for (final List<TriplePattern> ltp : possibleSolutions) {
				bestPlans2.add(getBestPlanBeforeSplittingHugeStarShapedJoins(
						ctp, index, dataset, planType, initialPlans, ltp));
			}
			if (bestPlans2.size() > 0) {
				if (bestPlans2.size() == 1)
					return bestPlans2.get(0);
				Plan bestPlan2 = bestPlans2.get(0);
				double minCost2 = bestPlan2.cost;
				for (final Plan p : bestPlans2) {
					if (p.cost < minCost2) {
						minCost2 = p.cost;
						bestPlan2 = p;
					}
				}
				return bestPlan2;
			}
			// next strategy
			// We determine the maximum number of possible merge joins!
			final HashMap<Item, LinkedList<TriplePattern>> mergeJoins = new HashMap<Item, LinkedList<TriplePattern>>();
			for (final TriplePattern tp : ctp) {
				for (final Item item : tp) {
					if (item.isVariable()) {
						LinkedList<TriplePattern> lltp = mergeJoins.get(item);
						if (lltp == null)
							lltp = new LinkedList<TriplePattern>();
						lltp.add(tp);
						mergeJoins.put(item, lltp);
					}
				}
			}
			int maxMergeJoins = 0;
			for (final LinkedList<TriplePattern> lltp : mergeJoins.values()) {
				if (lltp.size() > maxMergeJoins)
					maxMergeJoins = lltp.size();
			}
			if (maxMergeJoins > 2 && maxMergeJoins < ctp.size()) {
				final LinkedList<Plan> bestPlans = new LinkedList<Plan>();
				for (final LinkedList<TriplePattern> lltp : mergeJoins.values()) {
					if (lltp.size() == maxMergeJoins)
						bestPlans
								.add(getBestPlanBeforeSplittingHugeStarShapedJoins(
										ctp, index, dataset, planType,
										initialPlans, lltp));
				}
				if (bestPlans.size() == 1)
					return bestPlans.get(0);
				Plan bestPlan = bestPlans.get(0);
				int numberOfCartesianProducts = bestPlan.numberOfCartesianProducts;
				int maxNumberOfMergeJoins = bestPlan.numberMergeJoins;
				double minCost = bestPlan.cost;
				for (final Plan p : bestPlans) {
					final int currentNumberOfCartesianProducts = p.numberOfCartesianProducts;
					if (p.numberOfCartesianProducts < numberOfCartesianProducts) {
						numberOfCartesianProducts = p.numberOfCartesianProducts;
						maxNumberOfMergeJoins = p.numberMergeJoins;
						minCost = p.cost;
						bestPlan = p;
					} else if (currentNumberOfCartesianProducts == numberOfCartesianProducts) {
						if (maxNumberOfMergeJoins < bestPlan.numberMergeJoins) {
							maxNumberOfMergeJoins = p.numberMergeJoins;
							minCost = p.cost;
							bestPlan = p;
						} else if (maxNumberOfMergeJoins == bestPlan.numberMergeJoins) {
							if (p.cost < minCost) {
								minCost = p.cost;
								bestPlan = p;
							}
						}
					}
				}
				return bestPlan;
			}
			// next strategy:
			// We split the plan at star-shaped joins!
			final HashMap<Item, LinkedList<TriplePattern>> starJoins = new HashMap<Item, LinkedList<TriplePattern>>();
			final HashMap<Item, LinkedList<TriplePattern>> helperStarJoins = new HashMap<Item, LinkedList<TriplePattern>>();
			for (final TriplePattern tp : ctp) {
				final Item subject = tp.getPos(0);
				LinkedList<TriplePattern> list = starJoins.get(subject);
				if (list == null)
					list = new LinkedList<TriplePattern>();
				list.add(tp);
				starJoins.put(subject, list);
				final Item predicate = tp.getPos(1);
				if (predicate instanceof Variable) {
					list = helperStarJoins.get(predicate);
					if (list == null)
						list = new LinkedList<TriplePattern>();
					list.add(tp);
					helperStarJoins.put(predicate, list);
				}
				final Item object = tp.getPos(2);
				if (object instanceof Variable) {
					list = helperStarJoins.get(object);
					if (list == null)
						list = new LinkedList<TriplePattern>();
					list.add(tp);
					helperStarJoins.put(object, list);
				}
			}
			final Collection<LinkedList<TriplePattern>> lltp = new LinkedList<LinkedList<TriplePattern>>();
			lltp.addAll(starJoins.values());
			for (final LinkedList<TriplePattern> ltp : lltp) {
				if (ltp.size() == 1) {
					// look if the object or predicate can be joined with other
					// triple patterns!
					final TriplePattern tp = ltp.get(0);
					if (tp.getPos(2) instanceof Variable) {
						if (starJoins.get(tp.getPos(2)) != null) {
							if (!tp.getPos(2).equals(tp.getPos(0))) {
								starJoins.get(tp.getPos(2)).add(tp);
								starJoins.remove(tp.getPos(0));
								continue;
							}
						}
						if (helperStarJoins.get(tp.getPos(0)) != null) {
							boolean flagCont = false;
							for (final TriplePattern tp2 : helperStarJoins
									.get(tp.getPos(0))) {
								if (!tp2.equals(tp)) {
									starJoins.get(tp2.getPos(0)).add(tp);
									starJoins.remove(tp.getPos(0));
									flagCont = true;
									break;
								}
							}
							if (flagCont)
								continue;
						}
						if (helperStarJoins.get(tp.getPos(2)) != null) {
							boolean flagCont = false;
							for (final TriplePattern tp2 : helperStarJoins
									.get(tp.getPos(2))) {
								if (!tp2.equals(tp)) {
									starJoins.get(tp2.getPos(0)).add(tp);
									starJoins.remove(tp.getPos(0));
									flagCont = true;
									break;
								}
							}
							if (flagCont)
								continue;
						}
					}
					if (tp.getPos(1) instanceof Variable) {
						if (starJoins.get(tp.getPos(1)) != null) {
							if (!tp.getPos(1).equals(tp.getPos(0))) {
								starJoins.get(tp.getPos(1)).add(tp);
								starJoins.remove(tp.getPos(0));
								continue;
							}
						}
						if (helperStarJoins.get(tp.getPos(0)) != null) {
							boolean flagCont = false;
							for (final TriplePattern tp2 : helperStarJoins
									.get(tp.getPos(0))) {
								if (!tp2.equals(tp)) {
									starJoins.get(tp2.getPos(0)).add(tp);
									starJoins.remove(tp.getPos(0));
									flagCont = true;
									break;
								}
							}
							if (flagCont)
								continue;
						}
						if (helperStarJoins.get(tp.getPos(1)) != null) {
							boolean flagCont = false;
							for (final TriplePattern tp2 : helperStarJoins
									.get(tp.getPos(1))) {
								if (!tp2.equals(tp)) {
									starJoins.get(tp2.getPos(0)).add(tp);
									starJoins.remove(tp.getPos(0));
									flagCont = true;
									break;
								}
							}
							if (flagCont)
								continue;
						}
					}
				}
			}
			final LinkedList<TriplePattern>[] solution = new LinkedList[starJoins
					.size()];
			final Iterator<LinkedList<TriplePattern>> starJoinsIterator = starJoins
					.values().iterator();
			for (int i = 0; i < starJoins.size(); i++)
				solution[i] = starJoinsIterator.next();

			if (solution.length == 1) {
				// We have to split one huge star-shaped join!

				// find out the two triple patterns with the smallest
				// cardinalities to split the join!
				final LeafNodePlan[] initialPlansArray = initialPlans
						.toArray(new LeafNodePlan[0]);
				int max0;
				int max1;
				if (initialPlansArray[0].card <= initialPlansArray[1].card) {
					max0 = 0;
					max1 = 1;
				} else {
					max0 = 1;
					max1 = 0;
				}
				for (int i = 2; i < initialPlansArray.length; i++) {
					if (initialPlansArray[i].card < initialPlansArray[max1].card) {
						if (initialPlansArray[i].card < initialPlansArray[max0].card) {
							max1 = max0;
							max0 = i;
						} else
							max1 = i;
					}
				}
				final LinkedList<LeafNodePlan> left = new LinkedList<LeafNodePlan>();
				left.add(initialPlansArray[max0]);
				final LinkedList<LeafNodePlan> right = new LinkedList<LeafNodePlan>();
				right.add(initialPlansArray[max1]);
				// now split the join by putting the current triple pattern to
				// the one, which will have the smallest
				// cardinality when joined with max0 or with max1!
				for (int i = 0; i < initialPlansArray.length; i++) {
					if (i == max0 || i == max1)
						continue;
					final InnerNodePlan plan0 = new InnerNodePlan(
							initialPlansArray[max0], initialPlansArray[i]);
					final InnerNodePlan plan1 = new InnerNodePlan(
							initialPlansArray[max0], initialPlansArray[i]);
					if (plan0.card <= plan1.card) {
						left.add(initialPlansArray[i]);
					} else {
						right.add(initialPlansArray[i]);
					}
				}
				if (left.size() == 1 || right.size() == 1) {
					// greedy: assume a left-deep or right-deep tree as best
					// solution!
					Collections.sort(initialPlans,
							new Comparator<LeafNodePlan>() {
								public int compare(final LeafNodePlan o1,
										final LeafNodePlan o2) {
									if (o1.card == o2.card)
										return 0;
									else if (o1.card < o2.card)
										return -1;
									else
										return 1;
								}
							});
					Plan plan = null;
					for (final LeafNodePlan leafNodePlan : initialPlans) {
						if (plan == null)
							plan = leafNodePlan;
						else
							plan = new InnerNodePlan(plan, leafNodePlan);
					}
					return plan;
				} else {

					final Plan leftPlan = getBestPlanBeforeSplittingHugeStarShapedJoins(
							index, dataset, planType, left);
					final Plan rightPlan = getBestPlanBeforeSplittingHugeStarShapedJoins(
							index, dataset, planType, right);
					return new InnerNodePlan(leftPlan, rightPlan);
				}
			} else {

				int max = 0;

				for (int i = 1; i < solution.length; i++) {
					if (solution[i].size() > solution[max].size())
						max = i;
				}
				if (solution[max].size() == 1) {
					// join is a path join

					// generate plans for paths:
					return generatePlanForPathJoin(initialPlans);

				}

				// join has at least one star-shaped join
				// split the huge join by separating a huge
				// star-shaped join from the rest!
				return getBestPlanBeforeSplittingHugeStarShapedJoins(ctp,
						index, dataset, planType, initialPlans, solution[max]);
			}
		}
		return getBestPlanAfterSplittingHugeStarShapedJoins(ctp, index,
				dataset, planType, initialPlans);
	}

	private Plan generatePlanForPathJoin(final List<LeafNodePlan> initialPlans) {
		final HashMap<Item, Tuple<Item, Plan>> subjects = new HashMap<Item, Tuple<Item, Plan>>();
		for (final LeafNodePlan leafNodePlan : initialPlans) {
			final TriplePattern tp = leafNodePlan.getTriplePatterns()
					.iterator().next();
			subjects.put(tp.getPos(0), new Tuple<Item, Plan>(tp.getPos(2),
					leafNodePlan));
		}
		while (subjects.size() > 1) {
			for (final Map.Entry<Item, Tuple<Item, Plan>> entry : subjects
					.entrySet()) {
				final Tuple<Item, Plan> tuple = subjects.get(entry.getValue()
						.getFirst());
				if (tuple != null && tuple.getSecond() != null) {
					entry.getValue().setSecond(null);
					final Plan plan = new InnerNodePlan(entry.getValue()
							.getSecond(), tuple.getSecond());
					entry.getValue().setSecond(plan);
					break;
				}
			}
		}
		// TODO: transform into left-deep tree if last triple pattern has a
		// smaller cardinality than the first triple pattern!
		return subjects.values().iterator().next().getSecond();
	}

	private List<List<TriplePattern>> cartesianProducts(
			final List<TriplePattern> ctp) {
		final List<Object> listOrReference = new ArrayList<Object>();
		final HashMap<Variable, Integer> map = new HashMap<Variable, Integer>();
		for (final TriplePattern tp : ctp) {
			final HashSet<Integer> existingLists = new HashSet<Integer>();
			for (final Variable v : tp.getVariables()) {
				if (map.containsKey(v))
					existingLists.add(map.get(v));
			}
			int index;
			if (existingLists.size() == 0) {
				// make new list
				index = listOrReference.size();
				final LinkedList<TriplePattern> newList = new LinkedList<TriplePattern>();
				newList.add(tp);
				listOrReference.add(newList);
			} else if (existingLists.size() > 1) {
				// merge lists!
				final Iterator<Integer> intIt = existingLists.iterator();
				final Tuple<List<TriplePattern>, Integer> mergedInTuple = getList(
						listOrReference, intIt.next());
				final List<TriplePattern> mergedInList = mergedInTuple
						.getFirst();
				index = mergedInTuple.getSecond();
				mergedInList.add(tp);
				while (intIt.hasNext()) {
					final Tuple<List<TriplePattern>, Integer> toBeMergedTuple = getList(
							listOrReference, intIt.next());
					mergedInList.addAll(toBeMergedTuple.getFirst());
					listOrReference.set(toBeMergedTuple.getSecond(), index);
				}
			} else {
				// use existing list
				index = existingLists.iterator().next();
				getList(listOrReference, index).getFirst().add(tp);
			}
			for (final Variable v : tp.getVariables()) {
				map.put(v, index);
			}
		}
		final List<List<TriplePattern>> result = new LinkedList<List<TriplePattern>>();
		for (final Object o : listOrReference) {
			if (o instanceof List)
				result.add((List<TriplePattern>) o);
		}
		return result;
	}

	private Tuple<List<TriplePattern>, Integer> getList(
			final List<Object> listOrReference, final int index) {
		final Object o = listOrReference.get(index);
		if (o == null)
			return null;
		else {
			if (o instanceof Integer)
				return getList(listOrReference, (Integer) o);
			else if (o instanceof List) {
				return new Tuple<List<TriplePattern>, Integer>(
						(List<TriplePattern>) o, index);
			} else {
				System.err.println("Unexpected content in listOrReference!");
				return null;
			}
		}
	}

	private Plan getBestPlanBeforeSplittingHugeStarShapedJoins(
			final List<TriplePattern> ctp, final BasicIndexScan index,
			final Dataset dataset, final PlanType planType,
			final List<LeafNodePlan> initialPlans,
			final List<TriplePattern> splittedPart) {
		final List<TriplePattern> ctp_right = new LinkedList<TriplePattern>();
		for (final TriplePattern tp : ctp) {
			if (!splittedPart.contains(tp)) {
				ctp_right.add(tp);
			}
		}
		// check cartesian products in the right part:
		final List<List<TriplePattern>> cartesianProducts = cartesianProducts(ctp_right);
		if (cartesianProducts.size() > 1) {
			// now try out every combination of the subsets put to the left or
			// right part...
			return getBestPlanTryOutCombinations(ctp, index, dataset, planType,
					initialPlans, splittedPart, cartesianProducts);
			// Plan bestPlan = null;
			// for (int i = 0; i < cartesianProducts.size(); i++) {
			// final List<TriplePattern> newSplittedPart = new
			// ArrayList<TriplePattern>();
			// for (int j = 0; j < i; j++) {
			// newSplittedPart.addAll(cartesianProducts.get(j));
			// }
			// for (int j = i + 1; j < cartesianProducts.size(); j++) {
			// newSplittedPart.addAll(cartesianProducts.get(j));
			// }
			//
			// ctp_right = new LinkedList<TriplePattern>();
			// for (final TriplePattern tp : ctp) {
			// if (!newSplittedPart.contains(tp)) {
			// ctp_right.add(tp);
			// }
			// }
			//
			// final LinkedList<LeafNodePlan> initialPlans_left = new
			// LinkedList<LeafNodePlan>();
			// final LinkedList<LeafNodePlan> initialPlans_right = new
			// LinkedList<LeafNodePlan>();
			// final Iterator<LeafNodePlan> it = initialPlans.iterator();
			// for (final TriplePattern tp : ctp) {
			// final LeafNodePlan plan = it.next();
			// if (newSplittedPart.contains(tp)) {
			// initialPlans_left.add(plan);
			// } else {
			// initialPlans_right.add(plan);
			// }
			// }
			//
			// final Plan currentPlan = new InnerNodePlan(
			// getBestPlanBeforeSplittingHugeStarShapedJoins(
			// splittedPart, index, dataset, planType,
			// initialPlans_left),
			// getBestPlanBeforeSplittingHugeStarShapedJoins(
			// ctp_right, index, dataset, planType,
			// initialPlans_right));
			// if (bestPlan == null || currentPlan.cost < bestPlan.cost)
			// bestPlan = currentPlan;
			// }
			// return bestPlan;
		}

		final LinkedList<LeafNodePlan> initialPlans_left = new LinkedList<LeafNodePlan>();
		final LinkedList<LeafNodePlan> initialPlans_right = new LinkedList<LeafNodePlan>();
		final Iterator<LeafNodePlan> it = initialPlans.iterator();
		for (final TriplePattern tp : ctp) {
			final LeafNodePlan plan = it.next();
			if (splittedPart.contains(tp)) {
				initialPlans_left.add(plan);
			} else {
				initialPlans_right.add(plan);
			}
		}

		return new InnerNodePlan(getBestPlanBeforeSplittingHugeStarShapedJoins(
				splittedPart, index, dataset, planType, initialPlans_left),
				getBestPlanBeforeSplittingHugeStarShapedJoins(ctp_right, index,
						dataset, planType, initialPlans_right));
	}

	private Plan getBestPlanBeforeSplittingHugeStarShapedJoinsWithoutConsideringCartesianProducts(
			final List<TriplePattern> ctp, final BasicIndexScan index,
			final Dataset dataset, final PlanType planType,
			final List<LeafNodePlan> initialPlans,
			final List<TriplePattern> splittedPart) {
		final List<TriplePattern> ctp_right = new LinkedList<TriplePattern>();
		for (final TriplePattern tp : ctp) {
			if (!splittedPart.contains(tp)) {
				ctp_right.add(tp);
			}
		}
		final LinkedList<LeafNodePlan> initialPlans_left = new LinkedList<LeafNodePlan>();
		final LinkedList<LeafNodePlan> initialPlans_right = new LinkedList<LeafNodePlan>();
		final Iterator<LeafNodePlan> it = initialPlans.iterator();
		for (final TriplePattern tp : ctp) {
			final LeafNodePlan plan = it.next();
			if (splittedPart.contains(tp)) {
				initialPlans_left.add(plan);
			} else {
				initialPlans_right.add(plan);
			}
		}

		return new InnerNodePlan(getBestPlanBeforeSplittingHugeStarShapedJoins(
				splittedPart, index, dataset, planType, initialPlans_left),
				getBestPlanBeforeSplittingHugeStarShapedJoins(ctp_right, index,
						dataset, planType, initialPlans_right));
	}

	private Plan getBestPlanTryOutCombinations(final List<TriplePattern> ctp,
			final BasicIndexScan index, final Dataset dataset,
			final PlanType planType, final List<LeafNodePlan> initialPlans,
			final List<TriplePattern> splittedPart,
			final List<List<TriplePattern>> cartesianProducts) {
		if (cartesianProducts.size() == 0) {
			if (splittedPart.size() < ctp.size())
				return getBestPlanBeforeSplittingHugeStarShapedJoinsWithoutConsideringCartesianProducts(
						ctp, index, dataset, planType, initialPlans,
						splittedPart);
			else
				return null;
		} else {
			final List<TriplePattern> first = cartesianProducts.remove(0);
			// try out to put next subset not to the splittedPart
			final Plan plan1 = getBestPlanTryOutCombinations(ctp, index,
					dataset, planType, initialPlans, splittedPart,
					cartesianProducts);
			// try out to put next subset to the splittedPart
			final List<TriplePattern> newSplittedPart = new LinkedList<TriplePattern>();
			newSplittedPart.addAll(splittedPart);
			newSplittedPart.addAll(first);
			final Plan plan2 = getBestPlanTryOutCombinations(ctp, index,
					dataset, planType, initialPlans, newSplittedPart,
					cartesianProducts);
			if (plan1 == null)
				return plan2;
			if (plan2 == null)
				return plan1;
			if (plan1.cost <= plan2.cost)
				return plan1;
			else
				return plan2;
		}
	}

	private Plan getBestPlanAfterSplittingHugeStarShapedJoins(
			final List<TriplePattern> ctp, final BasicIndexScan index,
			final Dataset dataset, final PlanType planType,
			final List<LeafNodePlan> initialPlans) {
		final HashMap<Long, Plan>[] bestPlans = new HashMap[ctp.size()];
		bestPlans[0] = new HashMap<Long, Plan>();
		long key = 1;
		final Iterator<LeafNodePlan> it = initialPlans.iterator();
		for (final TriplePattern tp : ctp) {
			final LeafNodePlan plan = it.next();
			bestPlans[0].put(key, plan);
			key *= 2;
		}
		for (int i = 1; i < ctp.size(); i++) {
			// bestPlans[i] = new HashMap<Long, SortedSet<Plan>>();
			bestPlans[i] = new HashMap<Long, Plan>();
			allCombinations(1, 0, 0, 0, 0, i + 1, ctp, bestPlans);
			// Due to performance reasons, we only consider the best plans
			// => pruning as below in the commented out code is not necessary!
			// // delete all plans with a cartesian product
			// final HashSet<Plan> toDelete = new HashSet<Plan>();
			// for(final long k:bestPlans[i].keySet()){
			// final SortedSet<Plan> best=bestPlans[i].get(k);
			// for (final Plan plan : best) {
			// if (plan.joinPartner.size() == 0) {
			// toDelete.add(plan);
			// }
			// }
			// if (!toDelete.containsAll(best)) {
			// best.removeAll(toDelete);
			// }
			//
			// if (((InnerNodePlan) best.first()).getJoinType() ==
			// JoinType.MERGEJOIN) {
			// // delete all other plans, where a merge join is not possible!
			// toDelete.clear();
			// for (final Plan plan : best) {
			// if (((InnerNodePlan) plan).getJoinType() != JoinType.MERGEJOIN) {
			// toDelete.add(plan);
			// }
			// }
			// best.removeAll(toDelete);
			// }
			//
			// // delete all plans, which are "dominated" by another plan,
			// // i.e. which can produce the same sorted lists and have higher
			// costs!
			// // Here, we detect plans, which join the triple patterns in the
			// same order.
			// // These plans surely produce the same sorted lists. However,
			// there could
			// // be more dominated plans!
			// toDelete.clear();
			// final TreeSet<Collection<TriplePattern>> alreadyAvailable=new
			// TreeSet<Collection<TriplePattern>>(new
			// Comparator<Collection<TriplePattern>>(){
			// public int compare(final Collection<TriplePattern> arg0,
			// final Collection<TriplePattern> arg1) {
			// final Iterator<TriplePattern> itp=arg1.iterator();
			// for(final TriplePattern tp1:arg0){
			// if(!itp.hasNext()) return 1;
			// final TriplePattern tp2=itp.next();
			// if(!tp1.equals(tp2)){
			// return tp1.toString().compareTo(tp2.toString());
			// }
			// }
			// if(itp.hasNext()) return -1;
			// return 0;
			// }
			// });
			// for (final Plan plan: best) {
			// if(alreadyAvailable.contains(plan.triplePatterns))
			// toDelete.add(plan);
			// else alreadyAvailable.add(plan.triplePatterns);
			// }
			// best.removeAll(toDelete);
			// }
		}
		// final SortedSet<Plan> topLevelBestPlans=new TreeSet<Plan>();
		// for(final Long l:bestPlans[ctp.size() - 1].keySet()){
		// topLevelBestPlans.add(bestPlans[ctp.size() - 1].get(l).first());
		// System.out.println("+++"+bestPlans[ctp.size() -
		// 1].get(l).first().getNodeString());
		// }
		// final Plan result=topLevelBestPlans.first();
		final Plan result = bestPlans[ctp.size() - 1]
				.get(bestPlans[ctp.size() - 1].keySet().iterator().next());
		result.findMaxMergeJoins();
		return result;
	}

	private final ReentrantLock lockBestPlan = new ReentrantLock();
	private final ReentrantLock lockNumberOfThreads = new ReentrantLock();
	private int numberThreads = 0;
	private final static int MAXNUMBERTHREADS = 0;

	// private void allCombinations(final long keyFactor, final long keyLeft,
	// final long keyRight, final int currentLeft, final int currentRight, final
	// int max, final List<TriplePattern> ctp,final
	// HashMap<Long,SortedSet<Plan>>[] bestPlans){
	private void allCombinations(final long keyFactor, final long keyLeft,
			final long keyRight, final int currentLeft, final int currentRight,
			final int max, final List<TriplePattern> ctp,
			final HashMap<Long, Plan>[] bestPlans) {
		if (ctp.size() == 0 || currentLeft + currentRight >= max) {
			// recursion end reached!
			// correct number of already joined triple patterns?
			if (currentLeft + currentRight != max || currentLeft == 0
					|| currentRight == 0)
				return;
			final Plan left = bestPlans[currentLeft - 1].get(keyLeft);
			final Plan right = bestPlans[currentRight - 1].get(keyRight);
			final Plan combined = new InnerNodePlan((Plan) left.clone(),
					(Plan) right.clone());
			lockBestPlan.lock();
			try {
				final Plan currentBest = bestPlans[max - 1].get(keyLeft
						+ keyRight);
				if (currentBest == null || currentBest.compareTo(combined) > 0)
					bestPlans[max - 1].put(keyLeft + keyRight, combined);
				return;
			} finally {
				lockBestPlan.unlock();
			}
			// best plans from left operand:
			// final SortedSet<Plan>
			// leftPlans=bestPlans[currentLeft-1].get(keyLeft);
			// final SortedSet<Plan>
			// rightPlans=bestPlans[currentRight-1].get(keyRight);
			// SortedSet<Plan> result=bestPlans[max-1].get(keyLeft+keyRight);
			// if(result==null) result=new TreeSet<Plan>();
			// // combine only the best plans:
			// // result.add(new
			// InnerNodePlan(leftPlans.first(),rightPlans.first()));
			//
			// // combine all these left and right plans!
			// for(final Plan left:leftPlans){
			// for(final Plan right:rightPlans){
			// result.add(new InnerNodePlan(left,right));
			// }
			// }
			// bestPlans[max-1].put(keyLeft+keyRight, result);
			// return;
		}
		// next triple pattern should remain unjoined
		final LinkedList<TriplePattern> temp = new LinkedList<TriplePattern>();
		temp.addAll(ctp);
		temp.remove(0);
		final long nextKeyFactor = keyFactor * 2;

		final LinkedList<Thread> listOfThreads = new LinkedList<Thread>();

		final Thread thread0 = new Thread() {
			@Override
			public void run() {
				allCombinations(nextKeyFactor, keyLeft, keyRight, currentLeft,
						currentRight, max, temp, bestPlans);
			}
		};
		startThread(thread0, listOfThreads);

		final Thread thread1 = new Thread() {
			@Override
			public void run() {
				// next triple pattern should be already joined in the left
				// operand
				allCombinations(nextKeyFactor, keyLeft + keyFactor, keyRight,
						currentLeft + 1, currentRight, max, temp, bestPlans);
			}
		};
		startThread(thread1, listOfThreads);

		final Thread thread2 = new Thread() {
			@Override
			public void run() {
				// next triple pattern should be already joined in the right
				// operand
				allCombinations(nextKeyFactor, keyLeft, keyRight + keyFactor,
						currentLeft, currentRight + 1, max, temp, bestPlans);
			}
		};
		startThread(thread2, listOfThreads);

		for (final Thread thread : listOfThreads)
			try {
				thread.join();
				lockNumberOfThreads.lock();
				try {
					numberThreads--;
				} finally {
					lockNumberOfThreads.unlock();
				}
			} catch (final InterruptedException e) {
				System.out.println(e);
				e.printStackTrace();
			}
	}

	private void startThread(final Thread thread,
			final LinkedList<Thread> listOfThreads) {
		lockNumberOfThreads.lock();
		try {
			if (numberThreads < MAXNUMBERTHREADS) {
				thread.start();
				listOfThreads.add(thread);
				numberThreads++;
			} else
				thread.run();
		} finally {
			lockNumberOfThreads.unlock();
		}
	}

	public static Root getBinaryJoinWithManyMergeJoins(
			final Root ic, final BasicIndexScan index,
			final PlanType planType, final Dataset dataset) {
		final OptimizeJoinOrder optimizer = new OptimizeJoinOrder();
		final OptimizeJoinOrder.Plan plan = optimizer.getBestPlan(
				(List<TriplePattern>) index.getTriplePattern(), index, dataset,
				planType);
		// System.out.println(plan.toString());
		final BasicOperator op = generateOperatorGraph(plan, ic, index, planType,
				new LinkedList<Variable>(), optimizer.minima,
				optimizer.maxima, false,
				new HashMap<TriplePattern, Map<Variable, VarBucket>>());
		op.setSucceedingOperators(index.getSucceedingOperators());
		return ic;
	}

	public static Root getPlanWithNAryMergeJoins(
			final Root ic, final BasicIndexScan index,
			final PlanType planType, final Dataset dataset) {
		final OptimizeJoinOrder optimizer = new OptimizeJoinOrder();
		final OptimizeJoinOrder.Plan plan = optimizer.getBestPlan(
				(List<TriplePattern>) index.getTriplePattern(), index, dataset,
				planType);
		// System.out.println(plan.toString());
		final BasicOperator op = generateOperatorGraph(plan, ic, index, planType,
				new LinkedList<Variable>(), optimizer.minima,
				optimizer.maxima, true,
				new HashMap<TriplePattern, Map<Variable, VarBucket>>());
		op.setSucceedingOperators(index.getSucceedingOperators());
		return ic;
	}

	protected static BasicOperator generateOperatorGraph(
			final OptimizeJoinOrder.Plan plan, final Root ic,
			final BasicIndexScan index, final PlanType planType,
			final Collection<Variable> sortCriterium,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima, final boolean NARYMERGEJOIN,
			final Map<TriplePattern, Map<Variable, VarBucket>> sel) {
		if (plan instanceof OptimizeJoinOrder.LeafNodePlan) {
			final BasicIndexScan index1 = getIndex((LeafNodePlan) plan, planType,
					index, sortCriterium, minima, maxima);
			sel.put(plan.triplePatterns.get(0), plan.selectivity);
			ic.addSucceedingOperator(new OperatorIDTuple(index1, 0));
			return index1;
		} else {
			final OptimizeJoinOrder.InnerNodePlan inp = (OptimizeJoinOrder.InnerNodePlan) plan;
			final BasicOperator left = generateOperatorGraph(inp.getLeft(), ic,
					index, planType, inp.joinPartner, minima,
					maxima, NARYMERGEJOIN, sel);
			final BasicOperator right = generateOperatorGraph(inp.getRight(), ic,
					index, planType, inp.joinPartner, minima,
					maxima, NARYMERGEJOIN, sel);
			if (planType == PlanType.RELATIONALINDEX) {
				// left-deep-tree or right-deep-tree?
				if (left instanceof BasicIndexScan && right instanceof BasicIndexScan) {
					if (((BasicIndexScan) right).getTriplePattern().size() == 1
							|| ((BasicIndexScan) left).getTriplePattern().size() == 1) {
						final Collection<TriplePattern> ctp;
						if (inp.getLeft().cost < inp.getRight().cost) {
							ctp = ((BasicIndexScan) left).getTriplePattern();
							ctp.addAll(((BasicIndexScan) right).getTriplePattern());
						} else {
							ctp = ((BasicIndexScan) right).getTriplePattern();
							ctp.addAll(((BasicIndexScan) left).getTriplePattern());
						}
						((BasicIndexScan) left).setTriplePatterns(ctp);
						ic.remove((BasicIndexScan) right);
						return left;
					}
				}
			}
			Join join;
			Operator last;
			if (planType == PlanType.RDF3XSORT) {
				if (sortCriterium.size() == 0
						|| equalCriterium(sortCriterium, inp.order)) {
					join = new MergeJoinWithoutSorting();
					join.setEstimatedCardinality(inp.card);
					last = join;
				} else {
					join = new MergeJoinWithoutSorting();
					join.setEstimatedCardinality(inp.card);
					if (!sortCriterium.equals(inp.joinPartner)) {
						last = FastSort.createInstance(ic, inp.triplePatterns,
								sortCriterium);
						join.setEstimatedCardinality(inp.card);
						last.setPrecedingOperator(join);
						join
								.setSucceedingOperator(new OperatorIDTuple(
										last, 0));
						// join = new MergeJoinSort(inp.triplePatterns,
						// sortCriterium, presortion);
						moveToLeft(inp.getTriplePatterns(), ic);
					} else
						last = join;
				}
				if (left instanceof RDF3XIndexScan) {
					((RDF3XIndexScan) left).setCollationOrder(inp.joinPartner);
				}
				if (right instanceof RDF3XIndexScan) {
					((RDF3XIndexScan) right).setCollationOrder(inp.joinPartner);
				}
			} else {
				if (inp.getJoinType() == OptimizeJoinOrder.JoinType.MERGEJOIN
						&& planType != PlanType.RELATIONALINDEX) {
					if (NARYMERGEJOIN) {
						int number;
						if (left instanceof NAryMergeJoinWithoutSorting) {
							number = ((NAryMergeJoinWithoutSorting) left)
									.getNumberOfOperands();
						} else
							number = 1;
						if (right instanceof NAryMergeJoinWithoutSorting) {
							number += ((NAryMergeJoinWithoutSorting) right)
									.getNumberOfOperands();
						} else
							number += 1;
						Bindings min = Bindings.createNewInstance();
						for (final Variable v : inp.joinPartner) {
							if (inp.selectivity == null) {
								min = null;
								break;
							}
							final VarBucket vb = inp.selectivity.get(v);
							if (vb == null) {
								min = null;
								break;
							}
							min.add(v, vb.minimum);
						}
						Bindings max = Bindings.createNewInstance();
						for (final Variable v : inp.joinPartner) {
							if (inp.selectivity == null) {
								max = null;
								break;
							}
							final VarBucket vb = inp.selectivity.get(v);
							if (vb == null) {
								max = null;
								break;
							}
							max.add(v, vb.maximum);
						}
						join = new NAryMergeJoinWithoutSorting(number, min, max);
						final BasicOperator[] bos = new BasicOperator[number];

						int index2 = 0;
						if (left instanceof NAryMergeJoinWithoutSorting) {
							for (final BasicOperator bo : left
									.getPrecedingOperators()) {
								bos[index2++] = bo;
								join.addPrecedingOperator(bo);
							}
						} else {
							bos[index2++] = left;
							join.addPrecedingOperator(left);
						}
						if (right instanceof NAryMergeJoinWithoutSorting) {
							for (final BasicOperator bo : right
									.getPrecedingOperators()) {
								bos[index2++] = bo;
								join.addPrecedingOperator(bo);
							}
						} else {
							bos[index2++] = right;
							join.addPrecedingOperator(right);
						}
						if (min != null)
							Arrays.sort(bos, new Comparator() {
								public int compare(final Object o1,
										final Object o2) {
									final double sel1 = sel.get(
											((BasicIndexScan) o1)
													.getTriplePattern()
													.iterator().next())
											.values().iterator().next()
											.getSumDistinctLiterals();
									final double sel2 = sel.get(
											((BasicIndexScan) o2)
													.getTriplePattern()
													.iterator().next())
											.values().iterator().next()
											.getSumDistinctLiterals();
									if (sel1 < sel2)
										return -1;
									else if (sel2 < sel1)
										return 1;
									else
										return 0;
								}
							});
						for (int i = 0; i < bos.length; i++)
							bos[i].setSucceedingOperator(new OperatorIDTuple(
									join, i));
					} else {
						join = new MergeJoinWithoutSorting();
					}
					join.setEstimatedCardinality(inp.card);
					last = join;
					if (left instanceof RDF3XIndexScan) {
						((RDF3XIndexScan) left).setCollationOrder(inp.joinPartner);
					}
					if (right instanceof RDF3XIndexScan) {
						((RDF3XIndexScan) right).setCollationOrder(inp.joinPartner);
					}
				} else {
					join = new Join();
					join.setEstimatedCardinality(inp.card);
					last = join;
				}
			}
			// if (!(join instanceof MergeJoinWithoutSorting)
			// && !(join instanceof NAryMergeJoinWithoutSorting)) {
			// if (inp.left instanceof LeafNodePlan
			// && !(inp.right instanceof LeafNodePlan)) {
			// moveToLeft(inp.right.getTriplePatterns(), ic);
			// } else if (inp.right instanceof LeafNodePlan
			// && !(inp.left instanceof LeafNodePlan)) {
			// moveToLeft(inp.left.getTriplePatterns(), ic);
			// } else

			if (!(inp.left instanceof InnerNodePlan && ((InnerNodePlan) inp.left).joinType == OptimizeJoinOrder.JoinType.DEFAULT)
					&& (inp.right instanceof InnerNodePlan && ((InnerNodePlan) inp.right).joinType == OptimizeJoinOrder.JoinType.DEFAULT)
					|| (inp.left instanceof LeafNodePlan && inp.right instanceof InnerNodePlan)) {
				moveToLeft(inp.right.getTriplePatterns(), ic);
			} else if (!(inp.right instanceof InnerNodePlan && ((InnerNodePlan) inp.right).joinType == OptimizeJoinOrder.JoinType.DEFAULT)
					&& (inp.left instanceof InnerNodePlan && ((InnerNodePlan) inp.left).joinType == OptimizeJoinOrder.JoinType.DEFAULT)
					|| (inp.right instanceof LeafNodePlan && inp.left instanceof InnerNodePlan)) {
				moveToLeft(inp.left.getTriplePatterns(), ic);
			} else if (inp.left.cost > inp.right.cost) {
				System.out
						.println("Card. of joins with estimated lower cost vs. est. higher cost:"
								+ inp.right.card + "<->" + inp.left.card);
				System.out
						.println("Cost of joins with estimated lower cost vs. est. higher cost:"
								+ inp.right.cost + "<->" + inp.left.cost);
				moveToLeft(inp.right.getTriplePatterns(), ic);
			} else {
				System.out
						.println("Card. of joins with estimated lower cost vs. est. higher cost:"
								+ inp.left.card + "<->" + inp.right.card);
				System.out
						.println("Cost of joins with estimated lower cost vs. est. higher cost:"
								+ inp.left.cost + "<->" + inp.right.cost);
				moveToLeft(inp.left.getTriplePatterns(), ic);
			}
			// } else if (left instanceof FastSort && right instanceof FastSort)
			// {
			// if (inp.left.cost > inp.right.cost) {
			// System.out
			// .println(
			// "Card. of joins with estimated lower cost vs. est. higher cost:"
			// + inp.right.card + "<->" + inp.left.card);
			// System.out
			// .println(
			// "Cost of joins with estimated lower cost vs. est. higher cost:"
			// + inp.right.cost + "<->" + inp.left.cost);
			// moveToLeft(inp.right.getTriplePatterns(), ic);
			// } else {
			// System.out
			// .println(
			// "Card. of joins with estimated lower cost vs. est. higher cost:"
			// + inp.left.card + "<->" + inp.right.card);
			// System.out
			// .println(
			// "Cost of joins with estimated lower cost vs. est. higher cost:"
			// + inp.left.cost + "<->" + inp.right.cost);
			// moveToLeft(inp.left.getTriplePatterns(), ic);
			// }
			// }
			join.setIntersectionVariables(plan.getJoinPartner());
			join.setUnionVariables(plan.getVariables());
			if (!last.equals(join)) {
				final LinkedList<Variable> llv = new LinkedList<Variable>();
				llv.addAll(join.getUnionVariables());
				last.setIntersectionVariables(llv);
				last.setUnionVariables(llv);
			}
			if (!(join instanceof NAryMergeJoinWithoutSorting)) {
				left.setSucceedingOperator(new OperatorIDTuple(join, 0));
				right.setSucceedingOperator(new OperatorIDTuple(join, 1));
				join.addPrecedingOperator(left);
				join.addPrecedingOperator(right);
			}
			return last;
		}
	}

	private static void moveToLeft(
			final Collection<TriplePattern> triplePatterns,
			final Root ic) {
		final List<OperatorIDTuple> succeedingOperators = ic
				.getSucceedingOperators();
		int insertPosition = 0;
		int max = 0;
		boolean change = true;
		while (change) {
			change = false;
			int index = max;
			for (; index < succeedingOperators.size(); index++) {
				final OperatorIDTuple oid = succeedingOperators.get(index);
				if (oid.getOperator() instanceof BasicIndexScan) {
					final Collection<TriplePattern> ctp = ((BasicIndexScan) oid
							.getOperator()).getTriplePattern();
					for (final TriplePattern tp : triplePatterns)
						if (ctp.contains(tp)) {
							change = true;
							break;
						}
					if (change)
						break;
				}
			}
			if (change) {
				max = index + 1;
				final OperatorIDTuple oid = succeedingOperators.remove(index);
				succeedingOperators.add(insertPosition, oid);
				insertPosition++;
			}
		}
		ic.setSucceedingOperators(succeedingOperators);
	}

	protected static boolean equalCriterium(
			final Collection<Variable> sortCriterium1,
			final Collection<Variable> sortCriterium2) {
		if (sortCriterium1 == null)
			return (sortCriterium2 == null);
		if (sortCriterium2 == null)
			return false;
		final Iterator<Variable> iv1 = sortCriterium2.iterator();
		for (final Variable v : sortCriterium1) {
			if (!iv1.hasNext())
				return false;
			if (!v.equals(iv1.next()))
				return false;
		}
		if (iv1.hasNext())
			return false;
		else
			return true;
	}

	protected static BasicIndexScan getIndex(final LeafNodePlan plan,
			final PlanType planType, final BasicIndexScan index,
			final Collection<Variable> sortCriterium,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		BasicIndexScan index1;
		switch (planType) {
		default:
		case RDF3XSORT:
		case RDF3X:
			index1 = new RDF3XIndexScan((OperatorIDTuple) null, plan
					.getTriplePatterns(), index.getGraphConstraint(), minima,
					maxima, index.getRoot());
			break;
		case RELATIONALINDEX:
			index1 = new MemoryIndexScan((OperatorIDTuple) null, plan
					.getTriplePatterns(), index.getGraphConstraint(), index.getRoot());
			break;
		}
		index1.setIntersectionVariables(plan.getVariables());
		index1.setUnionVariables(plan.getVariables());
		if (planType == PlanType.RDF3X || planType == PlanType.RDF3XSORT) {
			int[] collationOrder1 = { -1, -1, -1 };
			int i1 = 0;
			for (int i = 0; i < 3; i++) {
				if (!index1.getTriplePattern().iterator().next().getPos(i)
						.isVariable()) {
					collationOrder1[i1] = i;
					i1++;
				}
			}
			for (final Variable v : plan.getOrder()) {
				collationOrder1[i1] = index1.getTriplePattern().iterator()
						.next().getPos(v);
				i1++;
			}
			collationOrder1 = fill(collationOrder1, i1);
			final CollationOrder co1 = getCollationOrder(collationOrder1);
			if (planType == PlanType.RDF3X || planType == PlanType.RDF3XSORT) {
				((RDF3XIndexScan) index1).setCollationOrder(co1);
			} 
		}
		return index1;
	}

	public static CollationOrder getCollationOrder(final TriplePattern tp,
			final Collection<Variable> orderVars) {
		int[] collationOrder1 = { -1, -1, -1 };
		int i1 = 0;
		for (int i = 0; i < 3; i++) {
			if (!tp.getPos(i).isVariable()) {
				collationOrder1[i1] = i;
				i1++;
			}
		}
		for (final Variable v : orderVars) {
			final int pos = tp.getPos(v);
			if (pos >= 0) {
				collationOrder1[i1] = pos;
				i1++;
			}
		}
		collationOrder1 = fill(collationOrder1, i1);
		return getCollationOrder(collationOrder1);
	}

	protected static int[] fill(final int[] collationOrder, int i) {
		for (; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int i2 = 0;
				for (; i2 < i; i2++) {
					if (j == collationOrder[i2])
						break;
				}
				if (i2 == i) {
					collationOrder[i] = j;
					break;
				}
			}
		}
		return collationOrder;
	}

	protected static CollationOrder getCollationOrder(
			final int[] collationOrderArray) {
		if (collationOrderArray[0] == 0) {
			if (collationOrderArray[1] == 1) {
				return CollationOrder.SPO;
			} else {
				return CollationOrder.SOP;
			}
		} else if (collationOrderArray[0] == 1) {
			if (collationOrderArray[1] == 0) {
				return CollationOrder.PSO;
			} else {
				return CollationOrder.POS;
			}
		} else {
			if (collationOrderArray[1] == 0) {
				return CollationOrder.OSP;
			} else {
				return CollationOrder.OPS;
			}
		}
	}
}
