package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleFindAllGenerates extends Rule {

	@Override
	protected void init() {
		final Generate generate = new Generate();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(generate, "generate");

		startNode = generate;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {

		// wie BasicOperator.deepClone

		final Generate generate = (Generate) mso.get("generate");
		RDFSRuleEngine0.generates.add(generate);
		return null;
	}

}
