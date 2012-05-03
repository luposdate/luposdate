package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.singleinput.generate.GenerateAddEnv;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleGenerateAddOverUnion extends Rule {

	@Override
	protected void init() {
		final GenerateAddEnv genAdd = new GenerateAddEnv();
		final Union union = new Union();

		genAdd.setSucceedingOperator(new OperatorIDTuple(union, -1));
		union.setPrecedingOperator(genAdd);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(genAdd, "genAdd");
		subGraphMap.put(union, "union");

		startNode = genAdd;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		return true;
	}

	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final GenerateAddEnv genAdd = (GenerateAddEnv) mso.get("genAdd");
		final Union union = (Union) mso.get("union");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) genAdd
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) union
				.getSucceedingOperators();
		final int index = genAdd.getOperatorIDTuple(union).getId();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(union, index));
			pre.removeSucceedingOperator(genAdd);
			union.addPrecedingOperator(pre);
		}

		union.removePrecedingOperator(genAdd);
		union.setSucceedingOperator(new OperatorIDTuple(genAdd, 0));

		genAdd.setPrecedingOperator(union);
		genAdd.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(genAdd);
			succ.removePrecedingOperator(union);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		return null;
	}
}
