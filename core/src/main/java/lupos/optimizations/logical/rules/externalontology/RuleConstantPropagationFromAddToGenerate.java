package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleConstantPropagationFromAddToGenerate extends Rule {

	@Override
	protected void init() {
		final AddBinding add = new AddBinding(null, null);
		final Generate generate = new Generate();

		add.setSucceedingOperator(new OperatorIDTuple(generate, -1));
		generate.setPrecedingOperator(add);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(add, "add");
		subGraphMap.put(generate, "generate");

		startNode = add;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		return mso.get("generate").getPrecedingOperators().size() == 1;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final AddBinding add = (AddBinding) mso.get("add");
		final Generate generate = (Generate) mso.get("generate");
		generate.replaceItems(add.getVar(), add.getLiteral());
		// remove add
		for (final BasicOperator bo : add.getPrecedingOperators()) {
			bo.removeSucceedingOperator(add);
			bo.getSucceedingOperators().addAll(add.getSucceedingOperators());
			generate.addPrecedingOperator(bo);
		}
		deleted.add(add);
		generate.removePrecedingOperator(add);
		alreadyAppliedTo = new HashSet<BasicOperator>();
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
