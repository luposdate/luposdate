package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.Result;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleDeleteOperatorWithNoSuccs extends Rule {

	@Override
	protected void init() {
		final BasicOperator op = new BasicOperator();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(op, "op");

		startNode = op;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final BasicOperator op = mso.get("op");
		// Remark: Query-Operator is allowed to have no successor
		return ((op.getSucceedingOperators().size() == 0) && (!(op instanceof Result)));
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final BasicOperator op = mso.get("op");

		final List<BasicOperator> pres = op.getPrecedingOperators();
		for (int i = 0; i < pres.size(); i++) {
			final BasicOperator pre = pres.get(i);

			// Delete from fathers, because it has no succs
			pre.removeSucceedingOperator(op);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		deleted.add(op);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
