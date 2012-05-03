package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleReplaceLitOverOptional extends Rule {

	@Override
	protected void init() {
		final ReplaceLit replaceLit = new ReplaceLit();
		final Optional optional = new Optional();

		// Only left Operand
		replaceLit.setSucceedingOperator(new OperatorIDTuple(optional, 0));
		optional.setPrecedingOperator(replaceLit);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(replaceLit, "replaceLit");
		subGraphMap.put(optional, "optional");

		startNode = replaceLit;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Optional optional = (Optional) mso.get("optional");
		final ReplaceLit replaceLit = (ReplaceLit) mso.get("replaceLit");

		final Object[] optionalVars = optional.getIntersectionVariables()
				.toArray();
		final LinkedList<Variable> v = replaceLit.getSubstitutionsLiteralLeft();

		// If there is minimum one substitution which can be pulled down
		for (int i = 0; i < v.size(); i++) {
			if (!arrayContains(optionalVars, v.get(i))) {
				// Operator should be the left operand of optional
				return (replaceLit.getOperatorIDTuple(optional).getId() == 0);
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
		final Optional optional = (Optional) mso.get("optional");

		final ReplaceLit replaceLitUnder = new ReplaceLit();
		added.add(replaceLitUnder);

		final Object[] optionalVars = optional.getIntersectionVariables()
				.toArray();
		final LinkedList<Variable> replaceLitLeft = replaceLit
				.getSubstitutionsLiteralLeft();
		final LinkedList<Literal> replaceLitRight = replaceLit
				.getSubstitutionsLiteralRight();
		Variable var;
		Literal lit;

		for (int i = 0; i < replaceLitLeft.size(); i++) {
			var = replaceLitLeft.get(i);
			// Split ReplaceLit and pull only not intersection variables
			// downwards
			if (!arrayContains(optionalVars, var)) {
				lit = replaceLitRight.get(i);
				replaceLitUnder.addSubstitution(var, lit);
				replaceLit.removeSubstitutionVars(i);
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) replaceLit
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) optional
				.getSucceedingOperators();
		final int index = replaceLit.getOperatorIDTuple(optional).getId();

		// If everything could be pushed downwards, the old ReplaceLit can be
		// deleted
		if (replaceLit.getSubstitutionsLiteralLeft().size() == 0) {
			BasicOperator pre;
			for (int i = 0; i < pres.size(); i++) {
				pre = pres.get(i);
				pre.addSucceedingOperator(new OperatorIDTuple(optional, index));
				pre.removeSucceedingOperator(replaceLit);
				optional.addPrecedingOperator(pre);
			}
			optional.removePrecedingOperator(replaceLit);
			deleted.add(replaceLit);
		}

		// Insert the new ReplaceLit under the Optional
		optional.setSucceedingOperator(new OperatorIDTuple(replaceLitUnder, 0));

		replaceLitUnder.setPrecedingOperator(optional);
		replaceLitUnder.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(replaceLitUnder);
			succ.removePrecedingOperator(optional);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
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
