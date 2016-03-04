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
package lupos.optimizations.physical.joinorder.costbasedoptimizer.operatorgraphgenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.join.MergeJoinWithoutSorting;
import lupos.engine.operators.multiinput.join.NAryMergeJoinWithoutSorting;
import lupos.engine.operators.singleinput.sort.fastsort.FastSort;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.InnerNodePlan;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.JoinType;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.LeafNodePlan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generated an operator graph for the RDF3X query evaluator
 *
 * @author groppe
 * @version $Id: $Id
 */
public class RDF3XOperatorGraphGenerator extends OperatorGraphGenerator {

	private static final Logger log = LoggerFactory.getLogger(RDF3XOperatorGraphGenerator.class);

	/**
	 * This flag specifies whether or not n-ary merge joins (true) or only binary merge joins (false) are used
	 */
	private final boolean NARYMERGEJOIN;

	/**
	 * This flag specifies whether or not merge joins are always used (true) (eventually with a preceding sorting phase),
	 * or other join algorithms like the hash join are used whenever the input of a join is not sorted suitable to apply a merge join.
	 */
	private final boolean RDF3XSORT;

	/**
	 * Default constructor using only binary joins, but merge joins are enforced by preceding sorting phases
	 */
	public RDF3XOperatorGraphGenerator(){
		this(true, false);
	}

	/**
	 * Constructor for allowing only binary joins and having the choice of enforcing merge joins or using only merge joins if the data is already sorted in the right way
	 *
	 * @param RDF3XSORT true, if merge joins should be enforced by eventual preceding sorting phases; false, if other join algorithms are used whenever the data is not already sorted in the right way
	 */
	public RDF3XOperatorGraphGenerator(final boolean RDF3XSORT){
		this(RDF3XSORT, false);
	}

	/**
	 * Constructor for the choice of n-ary versus binary merge joins, and enforcing always merge joins by eventual preceding sorting phases or using other join algorithms for unsorted data
	 *
	 * @param RDF3XSORT true, if merge joins should be enforced by eventual preceding sorting phases; false, if other join algorithms are used whenever the data is not already sorted in the right way
	 * @param NARYMERGEJOIN n-ary (true) versus binary (false) merge joins
	 */
	public RDF3XOperatorGraphGenerator(final boolean RDF3XSORT, final boolean NARYMERGEJOIN){
		this.RDF3XSORT = RDF3XSORT;
		this.NARYMERGEJOIN = NARYMERGEJOIN;
	}

	/** {@inheritDoc} */
	@Override
	protected RDF3XIndexScan getIndex(final LeafNodePlan plan,
			final BasicIndexScan indexScan,
			final Collection<Variable> sortCriterium,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima) {
		final RDF3XIndexScan index1 = new RDF3XIndexScan((OperatorIDTuple) null, plan.getTriplePatterns(), indexScan.getGraphConstraint(), minima, maxima, indexScan.getRoot());

		// determine the collation order to be used to access the index during an index scan
		int[] collationOrder1 = { -1, -1, -1 };
		int i1 = 0;
		for (int i = 0; i < 3; i++) {
			if (!index1.getTriplePattern().iterator().next().getPos(i).isVariable()) {
				collationOrder1[i1] = i;
				i1++;
			}
		}
		for (final Variable v : plan.getOrder()) {
			collationOrder1[i1] = index1.getTriplePattern().iterator().next().getPos(v);
			i1++;
		}
		collationOrder1 = fill(collationOrder1, i1);
		final CollationOrder co1 = getCollationOrder(collationOrder1);

		index1.setCollationOrder(co1);
		return index1;
	}

	/**
	 * Determines a complete collation order array
	 *
	 * @param collationOrder the incomplete collation order array
	 * @param pos the position where the collation order starts being incomplete
	 * @return the complete collation order array
	 */
	protected static int[] fill(final int[] collationOrder, final int pos) {
		for (int i=pos; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int i2 = 0;
				for (; i2 < i; i2++) {
					if (j == collationOrder[i2]) {
						break;
					}
				}
				if (i2 == i) {
					collationOrder[i] = j;
					break;
				}
			}
		}
		return collationOrder;
	}

	/**
	 * Determines the collation order from a collation order array
	 *
	 * @param collationOrderArray the collation order as array
	 * @return the collation order
	 */
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

	/**
	 * Determines the collation order for a triple pattern, such that using an index with the determined collation order, returns its result sorted according to a given sort criterium
	 *
	 * @param tp the triple pattern to be considered
	 * @param sortCriterium the sort criterium
	 * @return the collation order
	 */
	public static CollationOrder getCollationOrder(final TriplePattern tp, final Collection<Variable> sortCriterium) {
		int[] collationOrder1 = { -1, -1, -1 };
		int i1 = 0;
		for (int i = 0; i < 3; i++) {
			if (!tp.getPos(i).isVariable()) {
				collationOrder1[i1] = i;
				i1++;
			}
		}
		for (final Variable v : sortCriterium) {
			final int pos = tp.getPos(v);
			if (pos >= 0) {
				collationOrder1[i1] = pos;
				i1++;
			}
		}
		collationOrder1 = fill(collationOrder1, i1);
		return getCollationOrder(collationOrder1);
	}

	/** {@inheritDoc} */
	@Override
	protected BasicOperator generateJoin(final InnerNodePlan inp, final Root root, final BasicOperator left, final BasicOperator right, final Collection<Variable> sortCriterium, final Map<TriplePattern, Map<Variable, VarBucket>> selectivity){
		Join join;
		Operator last;
		if (this.RDF3XSORT) {
			if (sortCriterium.isEmpty() || equalCriterium(sortCriterium, inp.getOrder())) {
				join = new MergeJoinWithoutSorting();
				join.setEstimatedCardinality(inp.getCardinality());
				last = join;
			} else {
				join = new MergeJoinWithoutSorting();
				join.setEstimatedCardinality(inp.getCardinality());
				if (!sortCriterium.equals(inp.getJoinPartner())) {
					// insert necessary sort operator
					last = FastSort.createInstance(root, inp.getTriplePatterns(), sortCriterium);
					join.setEstimatedCardinality(inp.getCardinality());
					last.setPrecedingOperator(join);
					join.setSucceedingOperator(new OperatorIDTuple(last, 0));
					this.moveToLeft(inp.getTriplePatterns(), root);
				} else {
					last = join;
				}
			}
			if (left instanceof RDF3XIndexScan) {
				((RDF3XIndexScan) left).setCollationOrder(inp.getJoinPartner());
			}
			if (right instanceof RDF3XIndexScan) {
				((RDF3XIndexScan) right).setCollationOrder(inp.getJoinPartner());
			}
		} else {
			if (inp.getJoinType() == JoinType.MERGEJOIN) {
				if (this.NARYMERGEJOIN) {
					throw new UnsupportedOperationException("Deprecated");
					// combine existing n-ary merge joins with the one which will be newly created
//					int number;
//					if (left instanceof NAryMergeJoinWithoutSorting) {
//						number = ((NAryMergeJoinWithoutSorting) left).getNumberOfOperands();
//					} else {
//						number = 1;
//					}
//					if (right instanceof NAryMergeJoinWithoutSorting) {
//						number += ((NAryMergeJoinWithoutSorting) right).getNumberOfOperands();
//					} else {
//						number += 1;
//					}
//					// determine minima and maxima...
//					Bindings min = Bindings.createNewInstance();
//					for (final Variable v : inp.getJoinPartner()) {
//						if (inp.getSelectivity() == null) {
//							min = null;
//							break;
//						}
//						final VarBucket vb = inp.getSelectivity().get(v);
//						if (vb == null) {
//							min = null;
//							break;
//						}
//						min.add(v, vb.minimum);
//					}
//					Bindings max = Bindings.createNewInstance();
//					for (final Variable v : inp.getJoinPartner()) {
//						if (inp.getSelectivity() == null) {
//							max = null;
//							break;
//						}
//						final VarBucket vb = inp.getSelectivity().get(v);
//						if (vb == null) {
//							max = null;
//							break;
//						}
//						max.add(v, vb.maximum);
//					}
//					join = new NAryMergeJoinWithoutSorting(number, min, max);
//					final BasicOperator[] bos = new BasicOperator[number];
//
//					int index2 = 0;
//					if (left instanceof NAryMergeJoinWithoutSorting) {
//						for (final BasicOperator bo : left
//								.getPrecedingOperators()) {
//							bos[index2++] = bo;
//							join.addPrecedingOperator(bo);
//						}
//					} else {
//						bos[index2++] = left;
//						join.addPrecedingOperator(left);
//					}
//					if (right instanceof NAryMergeJoinWithoutSorting) {
//						for (final BasicOperator bo : right
//								.getPrecedingOperators()) {
//							bos[index2++] = bo;
//							join.addPrecedingOperator(bo);
//						}
//					} else {
//						bos[index2++] = right;
//						join.addPrecedingOperator(right);
//					}
//					if (min != null){
//						Arrays.sort(bos, new Comparator<Object>() {
//							@Override
//							public int compare(final Object o1, final Object o2) {
//								final double sel1 = selectivity.get(
//										((BasicIndexScan) o1).getTriplePattern().iterator().next()).values().iterator().next().getSumDistinctLiterals();
//								final double sel2 = selectivity.get(
//										((BasicIndexScan) o2).getTriplePattern().iterator().next()).values().iterator().next().getSumDistinctLiterals();
//								if (sel1 < sel2){
//									return -1;
//								} else if (sel2 < sel1){
//									return 1;
//								} else {
//									return 0;
//								}
//							}
//						});
//					}
//					for (int i = 0; i < bos.length; i++) {
//						bos[i].setSucceedingOperator(new OperatorIDTuple(join, i));
//					}
				} else {
					join = new MergeJoinWithoutSorting();
				}
				join.setEstimatedCardinality(inp.getCardinality());
				last = join;
				if (left instanceof RDF3XIndexScan) {
					((RDF3XIndexScan) left).setCollationOrder(inp.getJoinPartner());
				}
				if (right instanceof RDF3XIndexScan) {
					((RDF3XIndexScan) right).setCollationOrder(inp.getJoinPartner());
				}
			} else {
				join = new Join();
				join.setEstimatedCardinality(inp.getCardinality());
				last = join;
			}
		}

		// optimize the order of executions of the left and right operand...
		if (!(inp.getLeft() instanceof InnerNodePlan && ((InnerNodePlan) inp.getLeft()).getJoinType() == JoinType.DEFAULT)
				&& (inp.getRight() instanceof InnerNodePlan && ((InnerNodePlan) inp.getRight()).getJoinType() == JoinType.DEFAULT)
				|| (inp.getLeft() instanceof LeafNodePlan && inp.getRight() instanceof InnerNodePlan)) {
			this.moveToLeft(inp.getRight().getTriplePatterns(), root);
		} else if (!(inp.getRight() instanceof InnerNodePlan && ((InnerNodePlan) inp.getRight()).getJoinType() == JoinType.DEFAULT)
				&& (inp.getLeft() instanceof InnerNodePlan && ((InnerNodePlan) inp.getLeft()).getJoinType() == JoinType.DEFAULT)
				|| (inp.getRight() instanceof LeafNodePlan && inp.getLeft() instanceof InnerNodePlan)) {
			this.moveToLeft(inp.getLeft().getTriplePatterns(), root);
		} else if (inp.getLeft().getCost() > inp.getRight().getCost()) {
			log.debug("Card. of joins with estimated lower cost vs. est. higher cost: {} <-> {}",
					inp.getRight().getCardinality(), inp.getLeft().getCardinality());
			log.debug("Cost of joins with estimated lower cost vs. est. higher cost: {} <-> {}",
					inp.getRight().getCost(), inp.getLeft().getCost());
			this.moveToLeft(inp.getRight().getTriplePatterns(), root);
		} else {
			log.debug("Card. of joins with estimated lower cost vs. est. higher cost: {} <-> {}",
					inp.getLeft().getCardinality(), inp.getRight().getCardinality());
			log.debug("Cost of joins with estimated lower cost vs. est. higher cost: {} <-> {}",
					inp.getLeft().getCost(), inp.getRight().getCost());
			this.moveToLeft(inp.getLeft().getTriplePatterns(), root);
		}

		join.setIntersectionVariables(inp.getJoinPartner());
		final HashSet<Variable> unionVars = new HashSet<Variable>();
		for(final TriplePattern tp: inp.getTriplePatterns()){
			unionVars.addAll(tp.getVariables());
		}
		join.setUnionVariables(unionVars);
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
