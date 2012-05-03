package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceVarUnderProjection extends Rule {

	@Override
	protected void init() {
		final Projection projection = new Projection();
		final ReplaceVar replaceVar = new ReplaceVar();

		projection.setSucceedingOperator(new OperatorIDTuple(replaceVar, 0));
		replaceVar.setPrecedingOperator(projection);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(projection, "projection");
		subGraphMap.put(replaceVar, "replaceVar");

		startNode = replaceVar;
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
		final Projection projection = (Projection) mso.get("projection");
		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");

		// Clone ReplaceVar
		final ReplaceVar replaceVar_new = new ReplaceVar();
		replaceVar_new.setSubstitutionsVariableLeft(replaceVar
				.getSubstitutionsVariableLeft());
		replaceVar_new.setSubstitutionsVariableRight(replaceVar
				.getSubstitutionsVariableRight());

		replaceVar.removePrecedingOperator(projection);

		// Enhance projection variables by left tuple variables of ReplaceVar
		final LinkedList<Variable> vars = replaceVar
				.getSubstitutionsVariableLeft();
		for (int i = 0; i < vars.size(); i++) {
			if (!projection.getProjectedVariables().contains(vars.get(i))) {
				projection.addProjectionElement(vars.get(i));
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) projection
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) replaceVar
				.getSucceedingOperators();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(replaceVar_new, 0));
			pre.removeSucceedingOperator(projection);
		}

		replaceVar_new.setPrecedingOperators(pres);
		replaceVar_new
				.setSucceedingOperator(new OperatorIDTuple(projection, 0));

		projection.setPrecedingOperator(replaceVar_new);
		projection.setSucceedingOperators(succs);

		for (int i = 0; i < succs.size(); i++) {
			succs.get(i).getOperator().addPrecedingOperator(projection);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
		deleted.add(replaceVar);
		added.add(replaceVar_new);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
