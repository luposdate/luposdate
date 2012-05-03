package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.tripleoperator.TriggerOneTime;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleDeleteTriggerOneTimeJoin extends Rule {

	@Override
	protected void init() {
		final TriggerOneTime trigger = new TriggerOneTime();
		final Join join = new Join();

		trigger.setSucceedingOperator(new OperatorIDTuple(join, -1));
		join.setPrecedingOperator(trigger);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(trigger, "trigger");
		subGraphMap.put(join, "join");

		startNode = join;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final TriggerOneTime trigger = (TriggerOneTime) mso.get("trigger");
		final Join join = (Join) mso.get("join");
		join.removePrecedingOperator(trigger);
		if (join.getPrecedingOperators().size() <= 1) {
			// remove join
			for (final OperatorIDTuple oit : join.getSucceedingOperators()) {
				oit.getOperator().removePrecedingOperator(join);
			}
			for (final BasicOperator bo : join.getPrecedingOperators()) {
				bo.removeSucceedingOperator(join);
				bo.addSucceedingOperators(join.getSucceedingOperators());
				for (final OperatorIDTuple oit : join.getSucceedingOperators()) {
					oit.getOperator().addPrecedingOperator(bo);
				}
			}
			deleted.add(join);
		}
		// remove join from trigger operator
		trigger.removeSucceedingOperator(join);
		if (trigger.getSucceedingOperators().size() == 0) {
			// remove trigger operator
			for (final BasicOperator bo : trigger.getPrecedingOperators()) {
				bo.removeSucceedingOperator(trigger);
			}
			deleted.add(trigger);
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
