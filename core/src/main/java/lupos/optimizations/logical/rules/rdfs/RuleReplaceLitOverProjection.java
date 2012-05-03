package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceLitOverProjection extends Rule {

	@Override
	protected void init() {
		final ReplaceLit replaceLit = new ReplaceLit();
		final Projection projection = new Projection();

		replaceLit.setSucceedingOperator(new OperatorIDTuple(projection, -1));
		projection.setPrecedingOperator(replaceLit);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(replaceLit, "replaceLit");
		subGraphMap.put(projection, "projection");

		startNode = replaceLit;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Projection projection = (Projection) mso.get("projection");
		if (projection.getPrecedingOperators().size() == 1)
			return true;
		else
			return false;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final ReplaceLit replaceLit = (ReplaceLit) mso.get("replaceLit");
		final Projection projection = (Projection) mso.get("projection");

		final Object[] projectionVars = projection.getProjectedVariables()
				.toArray();
		final LinkedList<Variable> replaceLitLeft = replaceLit
				.getSubstitutionsLiteralLeft();
		final LinkedList<Literal> replaceLitRight = replaceLit
				.getSubstitutionsLiteralRight();
		Variable var;
		Literal lit;
		// Delete all not projected Tupels from ReplaceLit
		for (int i = 0; i < replaceLitLeft.size(); i++) {
			var = replaceLitLeft.get(i);
			if (!arrayContains(projectionVars, var)) {
				lit = replaceLitRight.get(i);
				replaceLit.removeSubstitution(var, lit);
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) replaceLit
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) projection
				.getSucceedingOperators();
		final int index = replaceLit.getOperatorIDTuple(projection).getId();

		BasicOperator pre;
		// Connect the ReplaceLit precessors directly to the Projection
		for (int i = pres.size() - 1; i >= 0; i--) {
			pre = pres.get(i);
			projection.addPrecedingOperator(pre);
			pre.removeSucceedingOperator(replaceLit);
			pre.addSucceedingOperator(new OperatorIDTuple(projection, index));
		}

		// Make ReplaceLit the successor of Projection
		projection.removePrecedingOperator(replaceLit);
		projection.setSucceedingOperator(new OperatorIDTuple(replaceLit, 0));

		replaceLit.setPrecedingOperator(projection);

		final HashSet<Variable> hsv = new HashSet<Variable>();
		hsv.addAll(projection.getIntersectionVariables());
		replaceLit.setIntersectionVariables(hsv);
		replaceLit.setUnionVariables(hsv);

		// Connect ReplaceLit to the Projections successors instead of him
		replaceLit.setSucceedingOperators(succs);
		BasicOperator succ;
		for (int i = succs.size() - 1; i >= 0; i--) {
			succ = succs.get(i).getOperator();
			succ.removePrecedingOperator(projection);
			succ.addPrecedingOperator(replaceLit);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// should have been done manually: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		return null;
	}

	private boolean arrayContains(final Object[] vars, final Variable var) {
		for (int i = 0; i < vars.length; i++) {
			System.out.println(vars[i].toString() + "," + var.toString());
			if (vars[i].equals(var))
				return true;
		}
		return false;
	}
}
