package lupos.optimizations.logical.rules.rdfs;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleSplitJoinOperandsWithSameId extends Rule {

	public RuleSplitJoinOperandsWithSameId() {
		super();
	}

	@Override
	protected void init() {
		final Join join = new Join();

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(join, "join");

		startNode = join;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final Join join = (Join) mso.get("join");
		int numberLeftOperands = 0;
		int numberRightOperands = 0;
		for (final BasicOperator prec : join.getPrecedingOperators()) {
			if (prec.getOperatorIDTuple(join).getId() == 1)
				numberRightOperands++;
			else
				numberLeftOperands++;
		}
		if (numberRightOperands > 1 || numberLeftOperands > 1)
			return true;
		else
			return false;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Join join = (Join) mso.get("join");
		final LinkedList<BasicOperator> leftOperands = new LinkedList<BasicOperator>();
		final LinkedList<BasicOperator> rightOperands = new LinkedList<BasicOperator>();
		for (final BasicOperator prec : join.getPrecedingOperators()) {
			final OperatorIDTuple oidt = prec.getOperatorIDTuple(join);
			if (prec.getOperatorIDTuple(join).getId() == 0)
				leftOperands.add(prec);
			else
				rightOperands.add(prec);
		}
		transformOperands(0, leftOperands, join, added);
		transformOperands(1, rightOperands, join, added);
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}

	private void transformOperands(final int id,
			final LinkedList<BasicOperator> operands, final Join join,
			final Collection<BasicOperator> added) {
		if (operands.size() > 1) {
			final LinkedList<Variable> vars = new LinkedList<Variable>();
			final Union union = new Union();
			added.add(union);
			union.setSucceedingOperator(new OperatorIDTuple(join, id));
			join.addPrecedingOperator(union);
			int i = 0;
			for (final BasicOperator prec : operands) {
				join.removePrecedingOperator(prec);
				prec.removeSucceedingOperator(join);
				vars.addAll(prec.getUnionVariables());
				prec.addSucceedingOperator(new OperatorIDTuple(union, i));
				i++;
			}
			union.setIntersectionVariables(vars);
			union.setUnionVariables(vars);
		}
	}
}
