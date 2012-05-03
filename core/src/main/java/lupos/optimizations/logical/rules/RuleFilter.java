package lupos.optimizations.logical.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.singleinput.Filter;
import lupos.misc.Tuple;

/**
 * super class for all those rules, the left side of which is
 * 
 * operator | filter
 **/
public abstract class RuleFilter extends Rule {

	public RuleFilter() {
		super();
	}

	protected void init() {
		// Define left side of rule
		final Operator a = new Operator();
		final Operator b = new Filter();
		a.setSucceedingOperator(new OperatorIDTuple(b, -1));
		b.setPrecedingOperator(a);

		subGraphMap = new HashMap<BasicOperator, String>();
		subGraphMap.put(a, "operator");
		subGraphMap.put(b, "filter");

		startNode = b;
	}

	protected abstract boolean checkPrecondition(Map<String, BasicOperator> mso);

	protected abstract Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			Map<String, BasicOperator> mso, BasicOperator rootOperator);
}