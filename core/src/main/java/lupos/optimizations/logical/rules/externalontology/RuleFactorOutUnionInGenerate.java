package lupos.optimizations.logical.rules.externalontology;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.Rule;

public class RuleFactorOutUnionInGenerate extends Rule {

	@Override
	protected void init() {
		final Union union = new Union();
		final Generate generate = new Generate();

		union.setSucceedingOperator(new OperatorIDTuple(generate, -1));
		generate.setPrecedingOperator(union);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(union, "union");
		subGraphMap.put(generate, "generate");

		startNode = union;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		// only one union?
		// int number = 0;
		// for (final BasicOperator bo : ((Join) mso.get("join"))
		// .getPrecedingOperators()) {
		// if (bo instanceof Union)
		// number++;
		// }
		// return (number == 1);
		return true;
	}

	@Override
	public Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		final Union union = (Union) mso.get("union");
		final Generate generate = (Generate) mso.get("generate");
		final List<BasicOperator> unionOperands = union.getPrecedingOperators();
		generate.removePrecedingOperator(union);
		deleted.add(union);
		boolean firstTime = true;
		if (generate.getPrecedingOperators().size() > 0) {
			firstTime = false;
		}
		for (final BasicOperator toMove : unionOperands) {
			Generate generateNew;
			if (firstTime) {
				// use existing generate operator
				generateNew = generate;
				firstTime = false;
			} else {
				// clone join operator plus its other operands
				generateNew = new Generate();
				generateNew.cloneFrom(generate);
				added.add(generateNew);
			}
			generateNew.setPrecedingOperator(toMove);

			toMove.setSucceedingOperator(new OperatorIDTuple(generateNew, 0));
		}
		if (deleted.size() > 0 || added.size() > 0)
			return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
					added, deleted);
		else
			return null;
	}
}
