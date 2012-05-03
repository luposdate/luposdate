package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.IndexCollection;
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
							|| toClone instanceof IndexCollection || toClone
							.getPrecedingOperators().get(0) instanceof TriggerOneTime)) {
				final BasicOperator toCloneNew = toClone
						.getPrecedingOperators().get(0);
				final BasicOperator cloneNew = (toCloneNew instanceof PatternMatcher || toCloneNew instanceof IndexCollection) ? toCloneNew
						: toCloneNew.clone();

				final OperatorIDTuple oidOld = toClone.getPrecedingOperators()
						.get(0).getOperatorIDTuple(toClone);
				cloneNew.replaceOperatorIDTuple(oidOld, new OperatorIDTuple(
						clone, toCloneNew.getSucceedingOperators().get(0)
								.getId()));
				clone.setPrecedingOperator(cloneNew);
				clone = cloneNew;
				toClone = toCloneNew;
				if (!(toCloneNew instanceof PatternMatcher || toCloneNew instanceof IndexCollection))
					added.add(cloneNew);
			}
			if (!(clone instanceof PatternMatcher || clone instanceof IndexCollection))
				for (final BasicOperator bo : toClone.getPrecedingOperators()) {
					final int id2 = bo.getOperatorIDTuple(toClone).getId();
					bo.addSucceedingOperator(new OperatorIDTuple(clone, id2));
					clone.addPrecedingOperator(bo);
				}
		}
	}
}
