package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceLitOverUnion extends Rule {

	@Override
	protected void init() {
		final ReplaceLit replaceLit = new ReplaceLit();
		final Union union = new Union();

		replaceLit.setSucceedingOperator(new OperatorIDTuple(union, -1));
		union.setPrecedingOperator(replaceLit);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(replaceLit, "replaceLit");
		subGraphMap.put(union, "union");

		startNode = replaceLit;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final ReplaceLit replaceLit = (ReplaceLit) mso.get("replaceLit");
		final Union union = (Union) mso.get("union");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) replaceLit
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) union
				.getSucceedingOperators();
		final int index = replaceLit.getOperatorIDTuple(union).getId();

		BasicOperator pre;
		// Connect the precessors of the ReplaceLit directly to the Union
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			union.addPrecedingOperator(pre);
			pre.removeSucceedingOperator(replaceLit);
			pre.addSucceedingOperator(new OperatorIDTuple(union, index));
		}
		union.removePrecedingOperator(replaceLit);

		// ReplaceLit becomes the new sucessor of Union
		union.setSucceedingOperator(new OperatorIDTuple(replaceLit, 0));

		// ReplaceLit gets the joins old sucessors
		replaceLit.setPrecedingOperator(union);
		replaceLit.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.removePrecedingOperator(union);
			succ.addPrecedingOperator(replaceLit);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		return null;
	}
}
