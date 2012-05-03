package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.generate.GenerateAddEnv;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleGenerateAddOverOptional extends Rule {

	@Override
	protected void init() {
		final GenerateAddEnv genAdd = new GenerateAddEnv();
		final Optional optional = new Optional();

		// Only left Operand
		genAdd.setSucceedingOperator(new OperatorIDTuple(optional, 0));
		optional.setPrecedingOperator(genAdd);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(genAdd, "genAdd");
		subGraphMap.put(optional, "optional");

		startNode = genAdd;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Optional optional = (Optional) mso.get("optional");
		final GenerateAddEnv genAdd = (GenerateAddEnv) mso.get("genAdd");

		final Object[] optionalVars = optional.getIntersectionVariables()
				.toArray();
		final Object[] v = genAdd.getConstants().keySet().toArray();

		// All Tuples should not have a join-partner of the Optional on the left
		// side
		for (int i = 0; i < v.length; i++) {
			if (arrayContains(optionalVars, (Variable) v[i])) {
				return false;
			}
		}

		// Operator should be the left operand of optional
		return (genAdd.getOperatorIDTuple(optional).getId() == 0);
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final GenerateAddEnv genAdd = (GenerateAddEnv) mso.get("genAdd");
		final Optional optional = (Optional) mso.get("optional");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) genAdd
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) optional
				.getSucceedingOperators();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(optional, 0));
			pre.removeSucceedingOperator(genAdd);
			optional.addPrecedingOperator(pre);
		}

		optional.removePrecedingOperator(genAdd);
		optional.setSucceedingOperator(new OperatorIDTuple(genAdd, 0));

		genAdd.setPrecedingOperator(optional);
		genAdd.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(genAdd);
			succ.removePrecedingOperator(optional);
		}

		rootOperator.deleteParents();
		rootOperator.setParents();
		rootOperator.detectCycles();
		rootOperator.sendMessage(new BoundVariablesMessage());
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
