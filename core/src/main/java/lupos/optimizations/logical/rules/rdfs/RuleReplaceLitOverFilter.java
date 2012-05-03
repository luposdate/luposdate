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
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceLitOverFilter extends Rule {

	@Override
	protected void init() {
		final ReplaceLit replaceLit = new ReplaceLit();
		final Filter filter = new Filter();

		replaceLit.setSucceedingOperator(new OperatorIDTuple(filter, -1));
		filter.setPrecedingOperator(replaceLit);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(replaceLit, "replaceLit");
		subGraphMap.put(filter, "filter");

		startNode = replaceLit;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Filter filter = (Filter) mso.get("filter");
		final ReplaceLit replaceLit = (ReplaceLit) mso.get("replaceLit");

		final Object[] filterVars = filter.getUsedVariables().toArray();
		final LinkedList<Variable> v = replaceLit.getSubstitutionsLiteralLeft();

		// If there is minimum one substitution which can be pulled down
		for (int i = 0; i < v.size(); i++) {
			if (!arrayContains(filterVars, v.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final ReplaceLit replaceLit = (ReplaceLit) mso.get("replaceLit");
		final Filter filter = (Filter) mso.get("filter");

		final ReplaceLit replaceLitUnder = new ReplaceLit();
		added.add(replaceLitUnder);
		final Collection<Variable> vars = new HashSet<Variable>();
		vars.addAll(filter.getIntersectionVariables());
		replaceLitUnder.setIntersectionVariables(vars);
		replaceLitUnder.setUnionVariables(vars);

		final Object[] filterVars = filter.getUsedVariables().toArray();
		final LinkedList<Variable> replaceLitLeft = replaceLit
				.getSubstitutionsLiteralLeft();
		final LinkedList<Literal> replaceLitRight = replaceLit
				.getSubstitutionsLiteralRight();
		Variable var;
		Literal lit;
		for (int i = replaceLitLeft.size() - 1; i >= 0; i--) {
			var = replaceLitLeft.get(i);
			// Split ReplaceLit and pull only not intersection variables
			// downwards
			if (!arrayContains(filterVars, var)) {
				lit = replaceLitRight.get(i);
				replaceLitUnder.addSubstitution(var, lit);
				replaceLit.removeSubstitutionVars(i);
				replaceLitUnder.getIntersectionVariables().add(var); // var is
				// also
				// added
				// to
				// unionVariables
				// as
				// they
				// are
				// the
				// same
				// objects
				// !
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) replaceLit
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) filter
				.getSucceedingOperators();
		final int index = replaceLit.getOperatorIDTuple(filter).getId();

		// If everything could be pushed downwards, the old ReplaceLit can be
		// deleted
		if (replaceLit.getSubstitutionsLiteralLeft().size() == 0) {
			BasicOperator pre;
			for (int i = 0; i < pres.size(); i++) {
				pre = pres.get(i);
				pre.addSucceedingOperator(new OperatorIDTuple(filter, index));
				pre.removeSucceedingOperator(replaceLit);
				filter.addPrecedingOperator(pre);
			}
			filter.removePrecedingOperator(replaceLit);
			deleted.add(replaceLit);
		}

		// Insert the new ReplaceLit under the Join
		filter.setSucceedingOperator(new OperatorIDTuple(replaceLitUnder, 0));

		replaceLitUnder.setPrecedingOperator(filter);
		replaceLitUnder.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(replaceLitUnder);
			succ.removePrecedingOperator(filter);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		// should have been done already manually: rootOperator.sendMessage(new
		// BoundVariablesMessage());
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
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
