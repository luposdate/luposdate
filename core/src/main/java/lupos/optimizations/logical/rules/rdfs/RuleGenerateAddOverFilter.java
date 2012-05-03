package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.singleinput.generate.GenerateAddEnv;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleGenerateAddOverFilter extends Rule {

	@Override
	protected void init() {
		final GenerateAddEnv genAdd = new GenerateAddEnv();
		final Filter filter = new Filter();

		genAdd.setSucceedingOperator(new OperatorIDTuple(filter, 0));
		filter.setPrecedingOperator(genAdd);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(genAdd, "genAdd");
		subGraphMap.put(filter, "filter");

		startNode = genAdd;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Filter filter = (Filter) mso.get("filter");
		final GenerateAddEnv genAdd = (GenerateAddEnv) mso.get("genAdd");

		final Object[] filterVars = filter.getUsedVariables().toArray();
		final Object[] v = genAdd.getConstants().keySet().toArray();
		for (int i = 0; i < v.length; i++) {
			if (arrayContains(filterVars, (Variable) v[i])) {
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
		final Filter filter = (Filter) mso.get("filter");

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) genAdd
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) filter
				.getSucceedingOperators();

		final int index = genAdd.getOperatorIDTuple(filter).getId();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(filter, index));
			pre.removeSucceedingOperator(genAdd);
			filter.addPrecedingOperator(pre);
		}

		filter.removePrecedingOperator(genAdd);
		filter.setSucceedingOperator(new OperatorIDTuple(genAdd, 0));

		genAdd.setPrecedingOperator(filter);
		genAdd.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(genAdd);
			succ.removePrecedingOperator(filter);
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
