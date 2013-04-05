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
package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.tripleoperator.TriggerOneTime;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleFactorOutUnionInJoin extends Rule {

	@Override
	protected void init() {
		final Union union = new Union();
		final Join join = new Join();

		union.setSucceedingOperator(new OperatorIDTuple(join, -1));
		join.setPrecedingOperator(union);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(union, "union");
		subGraphMap.put(join, "join");

		startNode = union;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		// only one union?
		// int number = 0;
		// for (final BasicOperator bo : ((Join) mso.get("join"))
		// .getPrecedingOperators()) {
		// if (bo instanceof Union)
		// number++;
		// }
		// return (number == 1);
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Union union = (Union) mso.get("union");
		final Join join = (Join) mso.get("join");
		final int id = union.getOperatorIDTuple(join).getId();
		final List<BasicOperator> unionOperands = union.getPrecedingOperators();
		union.setPrecedingOperator(join);
		union.setSucceedingOperators(join.getSucceedingOperators());
		for (final OperatorIDTuple oid : join.getSucceedingOperators()) {
			oid.getOperator().removePrecedingOperator(join);
			oid.getOperator().addPrecedingOperator(union);
		}
		final List<OperatorIDTuple> precedingOperatorsJoin = new LinkedList<OperatorIDTuple>();
		for (final BasicOperator bo : join.getPrecedingOperators()) {
			if (!(bo.equals(union))) {
				precedingOperatorsJoin.add(new OperatorIDTuple(bo, bo
						.getOperatorIDTuple(join).getId()));
			}
		}
		boolean firstTime = true;
		for (final BasicOperator toMove : unionOperands) {
			Join joinNew;
			if (firstTime) {
				// use existing join operator
				joinNew = join;
				joinNew.removePrecedingOperator(union);
				firstTime = false;
			} else {
				// clone join operator plus its other operands
				joinNew = new Join();
				added.add(joinNew);
				union.addPrecedingOperator(joinNew);

				for (final OperatorIDTuple oid : precedingOperatorsJoin) {
					final BasicOperator toClone = oid.getOperator();
					final BasicOperator clone = toClone.clone();
					added.add(clone);
					clone.setSucceedingOperator(new OperatorIDTuple(joinNew,
							oid.getId()));
					joinNew.addPrecedingOperator(clone);
					cloneFurther(clone, toClone, added);
				}

			}
			joinNew.setSucceedingOperator(new OperatorIDTuple(union, union
					.getPrecedingOperators().size() - 1));
			joinNew.addPrecedingOperator(toMove);

			final LinkedList<Variable> intersectionVariables = new LinkedList<Variable>();
			final LinkedList<Variable> unionVariables = new LinkedList<Variable>();
			intersectionVariables.addAll(joinNew.getPrecedingOperators().get(0)
					.getUnionVariables());
			for (final BasicOperator bo : joinNew.getPrecedingOperators()) {
				unionVariables.addAll(bo.getUnionVariables());
				intersectionVariables.retainAll(bo.getUnionVariables());
			}
			joinNew.setIntersectionVariables(intersectionVariables);
			joinNew.setUnionVariables(unionVariables);

			toMove.setSucceedingOperator(new OperatorIDTuple(joinNew, id));
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private void cloneFurther(BasicOperator clone, BasicOperator toClone,
			final Collection<BasicOperator> added) {
		if (clone instanceof Union) {
			for (final BasicOperator toCloneNew : toClone
					.getPrecedingOperators()) {
				final BasicOperator cloneNew = toCloneNew.clone();
				final OperatorIDTuple oid = toCloneNew
						.getOperatorIDTuple(toClone);
				cloneNew.removeSucceedingOperator(toClone);
				// try {
				cloneNew.addSucceedingOperator(new OperatorIDTuple(clone, oid
						.getId()));
				// } catch (final Exception e) {
				// System.out.println("2");
				// }
				clone.removePrecedingOperator(toCloneNew);
				clone.addPrecedingOperator(cloneNew);
				added.add(cloneNew);
				cloneFurther(cloneNew, toCloneNew, added);
			}
		} else {
			// if (toClone instanceof TriplePattern)
			// System.out.println();
			while (toClone.getPrecedingOperators().size() == 1
					&& !(toClone instanceof PatternMatcher
							|| toClone instanceof Root || toClone
							.getPrecedingOperators().get(0) instanceof TriggerOneTime)) {
				final BasicOperator toCloneNew = toClone
						.getPrecedingOperators().get(0);
				final BasicOperator cloneNew = (toCloneNew instanceof PatternMatcher || toCloneNew instanceof Root) ? toCloneNew
						: toCloneNew.clone();

				final OperatorIDTuple oidOld = toClone.getPrecedingOperators()
						.get(0).getOperatorIDTuple(toClone);
				cloneNew.replaceOperatorIDTuple(oidOld, new OperatorIDTuple(
						clone, toCloneNew.getSucceedingOperators().get(0)
								.getId()));
				clone.setPrecedingOperator(cloneNew);
				clone = cloneNew;
				toClone = toCloneNew;
				if (!(toCloneNew instanceof PatternMatcher || toCloneNew instanceof Root))
					added.add(cloneNew);
			}
			if (!(clone instanceof PatternMatcher || clone instanceof Root))
				for (final BasicOperator bo : toClone.getPrecedingOperators()) {
					final int id2 = bo.getOperatorIDTuple(toClone).getId();
					bo.addSucceedingOperator(new OperatorIDTuple(clone, id2));
					clone.addPrecedingOperator(bo);
				}
		}
	}
}
