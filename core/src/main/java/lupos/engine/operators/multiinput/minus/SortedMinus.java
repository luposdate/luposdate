package lupos.engine.operators.multiinput.minus;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.sort.Sort;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorVariables;

public class SortedMinus extends Minus {

	protected ComparatorBindings comp;
	protected Sort predecessorLeft, predecessorRight; // sorting nodes which
	// preprocess the input
	protected boolean isSortable;

	public SortedMinus(Sort predecessorLeft, Sort predecessorRight) {
		this.predecessorLeft = predecessorLeft;
		this.predecessorRight = predecessorRight;
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {

		if (!isSortable)
			return super.process(bindings, operandID);

		// wait for all query results and process them when
		// EndOfEvaluationMessage arrives
		if (operandID == 0) {
			if (lChild == null)
				lChild = bindings;
			else {
				throw new UnsupportedOperationException(
						"More than one query result, but result should be sorted.");
			}
		} else if (operandID == 1) {
			if (rChild == null)
				rChild = bindings;
			else {
				throw new UnsupportedOperationException(
						"More than one query result, but result should be sorted.");
			}
		}

		return null;
	}

	@Override
	public Message preProcessMessage(EndOfEvaluationMessage msg) {

		if (!isSortable || rChild == null) {
			return super.preProcessMessage(msg);
		} else if (lChild != null && rChild != null) {
			QueryResult result = QueryResult.createInstance();

			Iterator<Bindings> leftIt = lChild.iterator();
			Iterator<Bindings> rightIt = rChild.iterator();
			Bindings leftBindings = null, rightBindings = null;
			if (leftIt.hasNext()) {
				leftBindings = leftIt.next();
			}
			if (rightIt.hasNext()) {
				rightBindings = rightIt.next();
			}

			while (rightBindings != null && leftBindings != null) {
				int difference = comp.compare(leftBindings, rightBindings);
				if (difference > 0) {
					rightBindings = rightIt.hasNext() ? rightIt.next() : null;
				} else if (difference == 0) {
					leftBindings = leftIt.hasNext() ? leftIt.next() : null;
				} else if (difference < 0) {
					result.add(leftBindings);
					leftBindings = leftIt.hasNext() ? leftIt.next() : null;
				}

			}

			while (leftBindings != null) {
				result.add(leftBindings);
				leftBindings = leftIt.hasNext() ? leftIt.next() : null;
			}

			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(result);
			}
		}

		return msg;
	}

	@Override
	public Message preProcessMessage(BoundVariablesMessage msg) {
		Message msgResult = super.preProcessMessage(msg);
		Set<Variable> leftUnion = null; 
		Set<Variable> rightUnion = null;

		// if all variables that must be compared are always bound in every
		// binding, then they can easily be presorted,
		// we check this
		for (BasicOperator parent : this.getPrecedingOperators()) {
			OperatorIDTuple opID = parent.getOperatorIDTuple(this);
			if (opID.getId() == 0) {
				if (leftUnion == null) {
					leftUnion = new HashSet<Variable>();
					leftUnion.addAll(parent.getIntersectionVariables());
				} else {
					leftUnion.retainAll(parent.getIntersectionVariables());
				}
			} else if (opID.getId() == 1) {
				if (rightUnion == null) {
					rightUnion = new HashSet<Variable>();
					rightUnion.addAll(parent.getIntersectionVariables());
				} else {
					rightUnion.retainAll(parent.getIntersectionVariables());
				}
			}
		}

		Set<Variable> intersection = leftUnion;
		intersection.retainAll(rightUnion);

		isSortable = !intersectionVariables.isEmpty()
				&& intersection.equals(intersectionVariables);

		comp = new ComparatorVariables(intersectionVariables);
		predecessorLeft.setComparator(comp);
		predecessorRight.setComparator(comp);

		return msgResult;
	}

}
