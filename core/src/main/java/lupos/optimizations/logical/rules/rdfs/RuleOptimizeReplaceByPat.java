package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.ReplaceLit;
import lupos.engine.operators.singleinput.ReplaceVar;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleOptimizeReplaceByPat extends Rule {

	@Override
	protected void init() {
		final TriplePattern pat = new TriplePattern();
		final ReplaceVar replaceVar = new ReplaceVar();

		pat.setSucceedingOperator(new OperatorIDTuple(replaceVar, 0));
		replaceVar.setPrecedingOperator(pat);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(pat, "pat");
		subGraphMap.put(replaceVar, "replaceVar");

		startNode = replaceVar;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final TriplePattern pat = (TriplePattern) mso.get("pat");
		// Because of time aspects triplepattern should not have more than one
		// successor
		if (pat.getSucceedingOperators().size() != 1) {
			return false;
		}

		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");
		BasicOperator op = replaceVar.getSucceedingOperators().get(0)
				.getOperator();
		// Ignore ReplaceLit and get the Projection
		if (op instanceof ReplaceLit) {
			op = op.getSucceedingOperators().get(0).getOperator();
		}
		if (!(op instanceof Projection)) {
			System.err
					.println("Replace has not Projection as direct successor or after its ReplaceLit => Correct RuleOptimizeReplaceByPat!!!");
			return false;
		} else {
			final Projection proj = (Projection) op;
			final Object[] projVars = proj.getProjectedVariables().toArray();
			final Item[] patItems = pat.getItems();
			for (int i = 0; i < patItems.length; i++) {
				// Only if a variable in TriplePattern is not projected later
				// (TP creates temporary tuples)
				if (patItems[i].isVariable()) {
					if (!arrayContains(projVars, (Variable) patItems[i])) {
						return true;
					}
				}
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
		final TriplePattern pat = (TriplePattern) mso.get("pat");
		final ReplaceVar replaceVar = (ReplaceVar) mso.get("replaceVar");

		BasicOperator op = replaceVar.getSucceedingOperators().get(0)
				.getOperator();
		if (op instanceof ReplaceLit) {
			op = op.getSucceedingOperators().get(0).getOperator();
		}
		final Projection proj = (Projection) op;

		final LinkedList<Variable> replaceVarLeft = replaceVar
				.getSubstitutionsVariableLeft();
		final LinkedList<Variable> replaceVarRight = replaceVar
				.getSubstitutionsVariableRight();
		final Object[] projVars = proj.getProjectedVariables().toArray();
		final Item[] patItems = pat.getItems();

		Variable var;
		Variable newTripleVar;

		// Before transformation: TriplePattern generates (p,lit) ,
		// ReplaceVar makes (y,lit) [because it has the tupel (y,p)] and
		// the Projection deletes (p,lit)
		// => Transformation replaces p by y in TriplePattern directly
		for (int i = 0; i < patItems.length; i++) {
			if (patItems[i].isVariable()) {
				var = (Variable) patItems[i];
				if (!arrayContains(projVars, var)) {
					final LinkedList<Integer> indices = getPositions(
							replaceVarRight, var);
					// Variable can be substituted by a ReplaceVar-variable
					if (indices.size() > 0) {
						newTripleVar = replaceVarLeft.get(indices.getFirst());
						// Replacement in TriplePattern
						pat.replace(var, newTripleVar);
						// Replacement same Replacement in ReplaceVar
						for (int a = 1; a < indices.size(); a++) {
							replaceVarRight.set(indices.get(a), newTripleVar);
						}
						// Delete the Tupel for Replacement
						replaceVar.removeSubstitutionVars(indices.getFirst());
					}
				}
			}
		}

		// Delete empty ReplaceVar
		if (replaceVar.getSubstitutionsVariableLeft().size() == 0) {
			final OperatorIDTuple succ = replaceVar.getSucceedingOperators()
					.get(0);
			pat.addSucceedingOperator(succ);
			pat.removeSucceedingOperator(replaceVar);
			succ.getOperator().removePrecedingOperator(replaceVar);
			succ.getOperator().addPrecedingOperator(pat);

			rootOperator.deleteParents();
			rootOperator.setParents();
			rootOperator.detectCycles();
			rootOperator.sendMessage(new BoundVariablesMessage());
			deleted.add(replaceVar);
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private LinkedList<Integer> getPositions(
			final LinkedList<Variable> replaceVarRight, final Variable var) {
		final LinkedList<Integer> pos = new LinkedList<Integer>();
		for (int i = 0; i < replaceVarRight.size(); i++) {
			if (replaceVarRight.get(i).equals(var)) {
				pos.add(i);
			}
		}
		return pos;
	}

	private boolean arrayContains(final Object[] vars, final Variable var) {
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].equals(var))
				return true;
		}
		return false;
	}

}
