package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.generate.GenerateAddEnv;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleGenerateAddOverJoin extends Rule {

	@Override
	protected void init() {
		final GenerateAddEnv genAdd = new GenerateAddEnv();
		final Join join = new Join();

		genAdd.setSucceedingOperator(new OperatorIDTuple(join, -1));
		join.setPrecedingOperator(genAdd);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(genAdd, "genAdd");
		subGraphMap.put(join, "join");

		startNode = genAdd;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Join join = (Join) mso.get("join");
		final GenerateAddEnv genAdd = (GenerateAddEnv) mso.get("genAdd");

		final Object[] joinVars = join.getIntersectionVariables().toArray();
		final Object[] v = genAdd.getConstants().keySet().toArray();

		// If there is minimum one substitution which can be pulled down
		for (int i = 0; i < v.length; i++) {
			// Otherwise join could trigger after transformation
			if (arrayContains(joinVars, (Variable) v[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final GenerateAddEnv genAdd = (GenerateAddEnv) mso.get("genAdd");
		final Join join = (Join) mso.get("join");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) genAdd
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) join
				.getSucceedingOperators();

		final int index = genAdd.getOperatorIDTuple(join).getId();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(join, index));
			pre.removeSucceedingOperator(genAdd);
			join.addPrecedingOperator(pre);
		}

		join.removePrecedingOperator(genAdd);
		join.setSucceedingOperator(new OperatorIDTuple(genAdd, 0));

		genAdd.setPrecedingOperator(join);
		genAdd.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(genAdd);
			succ.removePrecedingOperator(join);
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
