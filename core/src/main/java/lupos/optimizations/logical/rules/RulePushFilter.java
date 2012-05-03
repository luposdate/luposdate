package lupos.optimizations.logical.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.Filter;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.Tuple;

/**
 * This class implements following rule to push filter in the operator graph:
 * 
 * o1 ... on o1 ... on | | | | operator => (filter) (filter) | | | filter
 * operator | (filter) Preconditions: - operator should not be a triple pattern.
 * - All the variables of at least one of o1 to on should contain all the used
 * variables of filter. The filter is moved after these o1 to on. - if operator
 * is an optional operator: filter is only moved if it can be moved upwards to
 * the left operand of optional - if operator is an union operator: filter is
 * only moved if it can be moved upwards to all operands of union
 * 
 * The original filter is deleted if there does not exist any of o1 to on, which
 * contains a subset of the used variables of the filter.
 **/
public class RulePushFilter extends RuleFilter {

	public RulePushFilter() {
		super();
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		BasicOperator operator = mso.get("operator");
		final Filter filter = (Filter) mso.get("filter");
		while (operator instanceof Filter) {
			if (operator.getPrecedingOperators().size() > 1
					|| operator.getSucceedingOperators().size() > 1)
				return false;
			operator = operator.getPrecedingOperators().get(0);
		}
		if (operator.getSucceedingOperators().size() > 1)
			return false;
		if (operator instanceof TriplePattern
				|| operator instanceof lupos.engine.operators.index.BasicIndex
				|| operator instanceof lupos.engine.operators.index.IndexCollection)
			return false;
		if (operator instanceof Union) {
			for (final BasicOperator o : operator.getPrecedingOperators())
				if (o.getSucceedingOperators().size() > 1
						|| !(o.getUnionVariables().containsAll(
								filter.getUsedVariables()) && !(o instanceof Filter && filter
								.equalFilterExpression((Filter) o))))
					return false;
			return true;
		}
		if (operator instanceof Optional) {
			if (operator.getPrecedingOperators().size() == 2) {
				BasicOperator o = operator.getPrecedingOperators().get(0);
				if (// o.getSucceedingOperators().size() == 1 &&
				o.getUnionVariables().containsAll(filter.getUsedVariables())
						&& !(o instanceof Filter && filter
								.equalFilterExpression((Filter) o))) {
					o = operator.getPrecedingOperators().get(1);
					if (// o.getSucceedingOperators().size() == 1 &&
					o.getUnionVariables()
							.containsAll(filter.getUsedVariables())
							&& !(o instanceof Filter && filter
									.equalFilterExpression((Filter) o)))
						return true;
				}
			}
			return false;
		}
		if (operator instanceof Join) {
			// check if the join has preceding operators in a loop
			if (operator.getCycleOperands() != null
					&& operator.getCycleOperands().size() > 0)
				return false;
		}
		for (final BasicOperator o : operator.getPrecedingOperators()) {
			if (o.getSucceedingOperators().size() == 1
					&& o.getUnionVariables().containsAll(
							filter.getUsedVariables())
					&& !(o instanceof Filter && filter
							.equalFilterExpression((Filter) o)))
				return true;
		}
		return false;
	}

	@Override
	protected Tuple<Collection<BasicOperator>, Collection<BasicOperator>> transformOperatorGraph(
			final Map<String, BasicOperator> mso,
			final BasicOperator rootOperator) {
		final Collection<BasicOperator> deleted = new LinkedList<BasicOperator>();
		final Collection<BasicOperator> added = new LinkedList<BasicOperator>();
		
		BasicOperator operator = mso.get("operator");
		final Filter filter = (Filter) mso.get("filter");

		while (operator instanceof Filter)
			operator = operator.getPrecedingOperators().get(0);
		boolean deleteFilter = true;
		boolean change = true;
		while (change) {
			deleteFilter = true;
			change = false;
			if (operator.getPrecedingOperators() != null) {
				for (final BasicOperator o : operator.getPrecedingOperators()) {
					if (!(o instanceof Filter && filter
							.equalFilterExpression((Filter) o))) {
						if (o.getUnionVariables().containsAll(
								filter.getUsedVariables())) {
							// System.out.println("TTTTTTTTTTTTTbefore:"+o.
							// graphString
							// ());
							final Filter filter2 = (Filter) filter.clone();
							added.add(filter2);
							final OperatorIDTuple oit = o
									.getOperatorIDTuple(operator);
							filter2.setSucceedingOperator(new OperatorIDTuple(
									operator, oit.getId()));
							final HashSet<Variable> hsv = new HashSet<Variable>();
							hsv.addAll(o.getIntersectionVariables());
							filter2.setIntersectionVariables(hsv);
							filter2.setUnionVariables(hsv);
							// System.out.println("TTTTTTTTTTTTToit:"+oit);
							o.replaceOperatorIDTuple(oit, new OperatorIDTuple(
									filter2, 0));
							operator
									.setPrecedingOperators(new LinkedList<BasicOperator>());
							//System.out.println("TTTTTTTTTTTTT"+o.graphString()
							// );
							rootOperator.deleteParents();
							rootOperator.setParents();
							rootOperator.detectCycles();
							// should have been done manually:
							// rootOperator.sendMessage(new
							// BoundVariablesMessage());
							change = true;
							break;
						} else {
							if (deleteFilter) {
								for (final Variable v : o.getUnionVariables())
									if (filter.getUsedVariables().contains(v)) {
										deleteFilter = false;
										break;
									}
							}
						}
					}
				}
			}
		}
		if (deleteFilter || operator instanceof Join) {
			final BasicOperator op2 = filter.getPrecedingOperators().get(0);
			op2.setSucceedingOperators(filter.getSucceedingOperators());
			rootOperator.deleteParents();
			rootOperator.setParents();
			rootOperator.sendMessage(new BoundVariablesMessage());
			deleted.add(filter);
		}
		return new Tuple<Collection<BasicOperator>, Collection<BasicOperator>>(
				added, deleted);
	}

	@Override
	public String getName() {
		return "Push Filter";
	}
}