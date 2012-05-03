package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.Result;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleDeleteUseLessProjection extends Rule {

	@Override
	protected void init() {
		final Projection projection = new Projection();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(projection, "projection");

		startNode = projection;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Projection projection = (Projection) mso.get("projection");
		if (projection.getSucceedingOperators().get(0).getOperator() instanceof Result) {
			return false;
		}
		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) projection
				.getPrecedingOperators();

		final Object[] projVars = projection.getProjectedVariables().toArray();

		final LinkedList<Variable> unionPres = new LinkedList<Variable>();

		BasicOperator pre;
		Object[] union;
		// calculate UNION-Variables of the precessors
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			union = pre.getUnionVariables().toArray();
			for (int u = 0; u < union.length; u++) {
				if (!unionPres.contains(union[u])) {
					unionPres.add((Variable) union[u]);
				}
			}
		}

		// check whether Projection projects everything
		for (int a = 0; a < unionPres.size(); a++) {
			if (!arrayContains(projVars, unionPres.get(a))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Projection projection = (Projection) mso.get("projection");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) projection
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) projection
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
				pre.removeSucceedingOperator(projection);
			}
		}

		BasicOperator succ;
		// And all successors to all precessors
		for (int i = 0; i < succs.size(); i++) {
			for (int a = 0; a < pres.size(); a++) {
				succ = succs.get(i).getOperator();
				succ.addPrecedingOperator(pres.get(a));
				succ.removePrecedingOperator(projection);
			}
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// should be no problem to leave this out: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		deleted.add(projection);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private boolean arrayContains(final Object[] vars, final Variable var) {
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].equals(var))
				return true;
		}
		return false;
	}
}
