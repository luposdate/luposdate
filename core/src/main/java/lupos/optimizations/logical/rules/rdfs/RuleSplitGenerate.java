package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleSplitGenerate extends Rule {

	@Override
	protected void init() {
		final Generate generate = new Generate();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(generate, "generate");

		startNode = generate;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Generate generate = (Generate) mso.get("generate");
		// While Generate has more than one successor
		return (generate.getSucceedingOperators().size() > 1)
				&& generate.getPrecedingOperators().size() == 1;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Generate generate = (Generate) mso.get("generate");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) generate
				.getPrecedingOperators();
		if (pres.size() > 1) {
			throw (new UnsupportedOperationException(
					"Generate has more predecessors => Correct RuleSplitGenerate!!!"));
		} else {
			final List<OperatorIDTuple> succs = generate
					.getSucceedingOperators();

			final BasicOperator pre = pres.get(0);

			Generate generate_new;

			pre.removeSucceedingOperator(generate);
			deleted.add(generate);

			// For each successor
			for (int i = 0; i < succs.size(); i++) {
				// generate a new Generate and connect it to the i-th successor
				generate_new = new Generate((TriplePattern) succs.get(i)
						.getOperator(), generate.getValueOrVariable());
				added.add(generate_new);
				// connect the new one instead of the old Generate to the
				// predecessors
				generate_new.setPrecedingOperators(pres);
				pre.addSucceedingOperator(new OperatorIDTuple(generate_new, 0));
			}

			rootOperator.deleteParents();
			rootOperator.setParents();
			rootOperator.detectCycles();
			rootOperator.sendMessage(new BoundVariablesMessage());
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
