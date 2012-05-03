package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleDeleteEmptyReplaceVar extends Rule {

	@Override
	protected void init() {
		final ReplaceVar replaceVar = new ReplaceVar();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(replaceVar, "replaceVar");

		startNode = replaceVar;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");
		// No substitution is left
		return (replaceVar.getSubstitutionsVariableLeft().size() == 0);
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) replaceVar
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) replaceVar
				.getSucceedingOperators();

		BasicOperator pre;
		OperatorIDTuple idTuple;
		// Connect all precessors to all successors
		for (int i = 0; i < pres.size(); i++) {
			for (int a = 0; a < succs.size(); a++) {
				idTuple = succs.get(a);
				pre = pres.get(i);
				pre.addSucceedingOperator(new OperatorIDTuple(idTuple
						.getOperator(), idTuple.getId()));
				pre.removeSucceedingOperator(replaceVar);
			}
		}

		BasicOperator succ;
		// And all successors to all precessors
		for (int i = 0; i < succs.size(); i++) {
			for (int a = 0; a < pres.size(); a++) {
				succ = succs.get(i).getOperator();
				succ.addPrecedingOperator(pres.get(a));
				succ.removePrecedingOperator(replaceVar);
			}
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// should have been done manually: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		deleted.add(replaceVar);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
