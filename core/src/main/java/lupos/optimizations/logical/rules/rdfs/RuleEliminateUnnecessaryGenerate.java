package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleEliminateUnnecessaryGenerate extends Rule {

	@Override
	protected void init() {
		final TriplePattern tp = new TriplePattern();
		final Generate generate = new Generate();

		tp.setSucceedingOperator(new OperatorIDTuple(generate, 0));
		generate.setPrecedingOperator(tp);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(tp, "triplepattern");
		subGraphMap.put(generate, "generate");

		startNode = generate;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Generate generate = (Generate) mso.get("generate");
		final TriplePattern tp = (TriplePattern) mso.get("triplepattern");

		final Item[] itemsGenerate = generate.getValueOrVariable();
		final Item[] itemsTriplePattern = tp.getItems();

		for (int i = 0; i < 3; i++) {
			if (!itemsGenerate[i].equals(itemsTriplePattern[i]))
				return false;
		}
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Generate generate = (Generate) mso.get("generate");
		final TriplePattern tp = (TriplePattern) mso.get("triplepattern");
		// remove the generate operator...
		for (final OperatorIDTuple oit : generate.getSucceedingOperators()) {
			oit.getOperator().removePrecedingOperator(generate);
		}
		for (final BasicOperator bo : generate.getPrecedingOperators()) {
			bo.removeSucceedingOperator(generate);
		}
		deleted.add(generate);
		// generate.setPrecedingOperators(null);
		// generate.setSucceedingOperators(null);

		// are there no other operators than generate???
		if (tp.getSucceedingOperators().size() == 0) {
			for (final BasicOperator bo : tp.getPrecedingOperators()) {
				bo.removeSucceedingOperator(tp);
			}
			// tp.setPrecedingOperators(null);
			// tp.setSucceedingOperators(null);
			deleted.add(tp);
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
