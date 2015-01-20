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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.optimizations.logical.statistics.VarBucket;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.InnerNodePlan;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.LeafNodePlan;
import lupos.optimizations.physical.joinorder.costbasedoptimizer.plan.Plan;

/**
 * This class is used for generating the operator graph from a plan 
 */
public abstract class OperatorGraphGenerator {
	
	/**
	 * This method is used to generate a join. It is overridden by subclasses for the different evaluators...
	 * @param inp the inner node plan for the join to be generated
	 * @param root the root of the operator graph
	 * @param left the left operand of the join
	 * @param right the right operand of the join
	 * @param sortCriterium the sort criteria the result of the new join must follow
	 * @param selectivity the histograms of the variable values
	 * @return the new join
	 */
	protected abstract BasicOperator generateJoin(final InnerNodePlan inp, final Root root, final BasicOperator left, final BasicOperator right, final Collection<Variable> sortCriterium, final Map<TriplePattern, Map<Variable, VarBucket>> selectivity);

	/**
	 * This method is used to generate an index scan operator. It is overridden by subclasses for the different evaluators...
	 * @param plan the leaf node from which the index scan operator is generated
	 * @param indexScan the original index scan operator which will be replaced with a join tree 
	 * @param sortCriterium the sort criteria the result of the new index scan operator must follow
	 * @param minima the minimum values of the variables
	 * @param maxima the maximum values of the variables
	 * @return the new index scan operator
	 */
	protected abstract BasicIndexScan getIndex(final LeafNodePlan plan, final BasicIndexScan indexScan, final Collection<Variable> sortCriterium, final Map<Variable, Literal> minima, final Map<Variable, Literal> maxima);
	
	/**
	 * This method generates the operator graph from a plan
	 * @param plan the plan from which the operator graph is generated
	 * @param root the root of the operator graph
	 * @param indexScan the index scan operator with many triple patterns to be replaced with a join tree
	 * @param sortCriterium the sort criteria which must be followed when generating the operator graph
	 * @param minima the minimum values of the variables
	 * @param maxima the maximum values of the variables
	 * @param selectivity the histograms of the values of the variables
	 * @return
	 */
	public BasicOperator generateOperatorGraph(
			final Plan plan, final Root root,
			final BasicIndexScan indexScan,
			final Collection<Variable> sortCriterium,
			final Map<Variable, Literal> minima,
			final Map<Variable, Literal> maxima,
			final Map<TriplePattern, Map<Variable, VarBucket>> selectivity) {
		if (plan instanceof LeafNodePlan) {
			final BasicIndexScan index1 = getIndex((LeafNodePlan) plan, indexScan, sortCriterium, minima, maxima);
			selectivity.put(plan.getTriplePatterns().iterator().next(), plan.getSelectivity());
			root.addSucceedingOperator(new OperatorIDTuple(index1, 0));
			return index1;
		} else {
			final InnerNodePlan inp = (InnerNodePlan) plan;
			final BasicOperator left = generateOperatorGraph(inp.getLeft(), root, indexScan, inp.getJoinPartner(), minima, maxima, selectivity);
			final BasicOperator right = generateOperatorGraph(inp.getRight(), root, indexScan, inp.getJoinPartner(), minima, maxima, selectivity);
			
			return this.generateJoin(inp, root, left, right, sortCriterium, selectivity);
		}
	}

	/**
	 * This method moves all index scan operators, which contain certain triple patterns to the left in the operator graph, such that they are evaluated before the others
	 * @param triplePatterns the triple patterns, which are contained in the index scan operators to be moved to the left
	 * @param root the root of the operator graph
	 */
	protected void moveToLeft(final Collection<TriplePattern> triplePatterns, final Root root) {
		final List<OperatorIDTuple> succeedingOperators = root.getSucceedingOperators();
		int insertPosition = 0;
		int max = 0;
		boolean change = true;
		while (change) {
			change = false;
			int index = max;
			for (; index < succeedingOperators.size(); index++) {
				final OperatorIDTuple oid = succeedingOperators.get(index);
				if (oid.getOperator() instanceof BasicIndexScan) {
					final Collection<TriplePattern> ctp = ((BasicIndexScan) oid.getOperator()).getTriplePattern();
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
		root.setSucceedingOperators(succeedingOperators);
	}
	
	/**
	 * This method checks if two given sort criteria are equal
	 * @param sortCriterium1 the first sort criteria to compare
	 * @param sortCriterium2 the second sort criteria to compare
	 * @return true if sortCriterium=sortCriterium2, otherwise false
	 */
	protected static boolean equalCriterium(final Collection<Variable> sortCriterium1, final Collection<Variable> sortCriterium2) {
		if (sortCriterium1 == null){
			return (sortCriterium2 == null);
		}
		if (sortCriterium2 == null){
			return false;
		}
		final Iterator<Variable> iv1 = sortCriterium2.iterator();
		for (final Variable v : sortCriterium1) {
			if (!iv1.hasNext()){
				return false;
			}
			if (!v.equals(iv1.next())){
				return false;
			}
		}
		if (iv1.hasNext()){
			return false;
		} else {
			return true;
		}
	}
}
