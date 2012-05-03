package lupos.engine.operators.multiinput.optional;

import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.join.MergeJoin;
import lupos.misc.debug.DebugStep;

public class MergeWithoutSortingOptional extends Optional {

	protected QueryResult left = null;
	protected QueryResult right = null;

	protected Comparator<Bindings> comp = new Comparator<Bindings>() {

		public int compare(final Bindings o1, final Bindings o2) {
			for (final Variable var : intersectionVariables) {
				final Literal l1 = o1.get(var);
				final Literal l2 = o2.get(var);
				if (l1 != null && l2 != null) {
					final int compare = l1
							.compareToNotNecessarilySPARQLSpecificationConform(l2);
					if (compare != 0)
						return compare;
				} else if (l1 != null)
					return -1;
				else if (l2 != null)
					return 1;
			}
			return 0;
		}
	};

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (this.precedingOperators.size() == 1) {
			return bindings;
		} else {
			if (operandID == 0) {
				left = bindings;
			} else if (operandID == 1) {
				right = bindings;
			} else
				System.err.println("MergeWithoutSortingOptional is a binary operator, but received the operand number "
								+ operandID);
			if (left != null && right != null) {				

				final Iterator<Bindings> currentResult = MergeJoin
						.mergeOptionalIterator(left.oneTimeIterator(), right
								.oneTimeIterator(), comp);

				if (currentResult != null && currentResult.hasNext()) {
					final QueryResult result = QueryResult
							.createInstance(currentResult);
					return result;					
				} else
					return null;
			} else
				return null;
		}
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if (left != null && right == null) {
			if (succeedingOperators.size() > 1)
				left.materialize();
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(left);
			}
		}
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		if (left != null && right == null) {
			if (succeedingOperators.size() > 1)
				left.materialize();
			for (final OperatorIDTuple opId : succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(left,
						debugstep, this, opId.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
		}
		return msg;
	}
}
