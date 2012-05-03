package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.generate.GenerateAddEnv;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleDeleteEmptyGenerateAdd extends Rule {

	@Override
	protected void init() {
		final GenerateAddEnv generateAdd = new GenerateAddEnv();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(generateAdd, "generateAdd");

		startNode = generateAdd;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final GenerateAddEnv generateAdd = (GenerateAddEnv) mso
				.get("generateAdd");
		return (generateAdd.getConstants().size() == 0);
	}

	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final GenerateAddEnv generateAdd = (GenerateAddEnv) mso
				.get("generateAdd");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) generateAdd
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) generateAdd
				.getSucceedingOperators();

		BasicOperator pre;
		OperatorIDTuple idTuple;
		for (int i = 0; i < pres.size(); i++) {
			for (int a = 0; a < succs.size(); a++) {
				idTuple = succs.get(a);
				pre = pres.get(i);
				pre.addSucceedingOperator(new OperatorIDTuple(idTuple
						.getOperator(), idTuple.getId()));
				pre.removeSucceedingOperator(generateAdd);
			}
		}

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			for (int a = 0; a < pres.size(); a++) {
				succ = succs.get(i).getOperator();
				succ.addPrecedingOperator(pres.get(a));
				succ.removePrecedingOperator(generateAdd);
			}
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		deleted.add(generateAdd);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
