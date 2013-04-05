/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.optimizations.logical.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.misc.Tuple;

/**
 * super class for all those rules, the left side of which is
 * 
 * operator | filter
 **/
public class RuleMakeBinaryJoin extends Rule {

	public RuleMakeBinaryJoin() {
		super();
	}

	@Override
	protected void init() {
		// Define left side of rule
		final Operator a = new Join();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(a, "join");

		startNode = a;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Join join = (Join) mso.get("join");

		if (join.getNumberOfOperands() > 2)
			return true;
		else
			return false;
	}

	@Override
	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Join join = (Join) mso.get("join");
		final List<BasicOperator> ctp = join.getPrecedingOperators();
		final BasicOperator finalJoin = getBinaryJoin(ctp, added);
		finalJoin.setSucceedingOperators(join.getSucceedingOperators());
		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		deleted.add(join);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	public BasicOperator getBinaryJoin(final List<BasicOperator> ctp,
			final Collection<BasicOperator> added) {

		final Collection<BasicOperator> newOrder = optimizeJoinOrderAccordingToMostRestrictionsForMergeJoin(ctp);
		final Collection<BasicOperator> remainingJoins = new LinkedList<BasicOperator>();
		final Iterator<BasicOperator> itp = newOrder.iterator();

		while (itp.hasNext()) {

			final BasicOperator first = itp.next();
			if (itp.hasNext()) {
				final Join newJoin = new Join();
				added.add(newJoin);
				first.setSucceedingOperator(new OperatorIDTuple(newJoin, 0));
				final BasicOperator second = itp.next();
				second.setSucceedingOperator(new OperatorIDTuple(newJoin, 1));
				final HashSet<Variable> hv = new HashSet<Variable>();
				hv.addAll(first.getUnionVariables());
				hv.addAll(second.getUnionVariables());
				newJoin.setUnionVariables(hv);
				remainingJoins.add(newJoin);
			} else {
				remainingJoins.add(first);
			}
		}

		while (remainingJoins.size() > 1) {
			// choose best combination
			final Collection<BasicOperator> co = getNextJoin(remainingJoins);
			final Iterator<BasicOperator> io = co.iterator();
			final BasicOperator first = io.next();
			final BasicOperator second = io.next();
			final Join join = new Join();
			added.add(join);

			join.setIntersectionVariables(new HashSet<Variable>());
			join.setUnionVariables(new HashSet<Variable>());
			join.getUnionVariables().addAll(first.getUnionVariables());
			join.getUnionVariables().addAll(second.getUnionVariables());
			first.setSucceedingOperator(new OperatorIDTuple(join, 0));
			second.setSucceedingOperator(new OperatorIDTuple(join, 1));
			remainingJoins.remove(first);
			remainingJoins.remove(second);
			remainingJoins.add(join);
		}
		return remainingJoins.iterator().next();
	}

	private Collection<BasicOperator> optimizeJoinOrderAccordingToMostRestrictionsForMergeJoin(
			final List<BasicOperator> remaining) {
		final Collection<BasicOperator> newOrder = new LinkedList<BasicOperator>();

		while (remaining.size() > 1) {
			BasicOperator best1 = null;
			BasicOperator best2 = null;
			int minOpenPositions = 4;

			for (final BasicOperator bo1 : remaining) {
				for (final BasicOperator bo2 : remaining) {
					if (!bo1.equals(bo2)) {
						final Collection<Variable> v = bo1.getUnionVariables();
						v.retainAll(bo2.getUnionVariables());
						final int openPositions = bo1.getUnionVariables()
								.size()
								- v.size();

						if (openPositions < minOpenPositions) {
							minOpenPositions = openPositions;
							best1 = bo1;
							best2 = bo2;
						}
					}
				}
			}
			newOrder.add(best1);
			newOrder.add(best2);
			remaining.remove(best1);
			remaining.remove(best2);
		}
		if (remaining.size() == 1) {
			for (final BasicOperator bo1 : remaining) {
				newOrder.add(bo1);
			}
		}
		return newOrder;
	}

	private Collection<BasicOperator> getNextJoin(
			final Collection<BasicOperator> remainingJoins) {
		final Collection<BasicOperator> co = new LinkedList<BasicOperator>();
		BasicOperator best1 = null;
		BasicOperator best2 = null;
		int minCommonVariables = -1;

		for (final BasicOperator o1 : remainingJoins) {
			for (final BasicOperator o2 : remainingJoins) {
				if (!o1.equals(o2)) {
					final Collection<Variable> v = o1.getUnionVariables();
					v.retainAll(o2.getUnionVariables());
					final int commonVariables = v.size();

					if (commonVariables > minCommonVariables) {
						minCommonVariables = commonVariables;
						best1 = o1;
						best2 = o2;
					}
				}
			}
		}
		co.add(best1);
		co.add(best2);
		return co;
	}

	@Override
	public String getName() {
		return "Binary Join";
	}
}