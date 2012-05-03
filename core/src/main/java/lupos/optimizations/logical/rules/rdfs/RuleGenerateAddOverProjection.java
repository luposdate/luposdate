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
import lupos.engine.operators.singleinput.generate.GenerateAddEnv;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleGenerateAddOverProjection extends Rule {

	@Override
	protected void init() {
		final GenerateAddEnv genAdd = new GenerateAddEnv();
		final Projection projection = new Projection();

		genAdd.setSucceedingOperator(new OperatorIDTuple(projection, -1));
		projection.setPrecedingOperator(genAdd);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(genAdd, "genAdd");
		subGraphMap.put(projection, "projection");

		startNode = genAdd;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final GenerateAddEnv genAdd = (GenerateAddEnv) mso.get("genAdd");
		final Projection projection = (Projection) mso.get("projection");

		final Object[] projVars = projection.getProjectedVariables().toArray();
		final Object[] c = genAdd.getConditions().keySet().toArray();
		for (int i = 0; i < c.length; i++) {
			// Condition should be replaceable after transformation
			if (arrayContains(projVars, (Variable) c[i])) {
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
		final Projection projection = (Projection) mso.get("projection");

		final Object[] projVars = projection.getProjectedVariables().toArray();
		final Object[] subst = genAdd.getConstants().keySet().toArray();

		for (int i = 0; i < subst.length; i++) {
			if (!arrayContains(projVars, (Variable) subst[i])) {
				genAdd.getConstants().remove(subst[i]);
			}
		}

		final LinkedList<BasicOperator> pres = (LinkedList<BasicOperator>) genAdd
				.getPrecedingOperators();
		final LinkedList<OperatorIDTuple> succs = (LinkedList<OperatorIDTuple>) projection
				.getSucceedingOperators();

		final int index = genAdd.getOperatorIDTuple(projection).getId();

		BasicOperator pre;
		for (int i = 0; i < pres.size(); i++) {
			pre = pres.get(i);
			pre.addSucceedingOperator(new OperatorIDTuple(projection, index));
			pre.removeSucceedingOperator(genAdd);
			projection.addPrecedingOperator(pre);
		}

		projection.removePrecedingOperator(genAdd);
		projection.setSucceedingOperator(new OperatorIDTuple(genAdd, 0));

		genAdd.setPrecedingOperator(projection);
		genAdd.setSucceedingOperators(succs);

		BasicOperator succ;
		for (int i = 0; i < succs.size(); i++) {
			succ = succs.get(i).getOperator();
			succ.addPrecedingOperator(genAdd);
			succ.removePrecedingOperator(projection);
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
