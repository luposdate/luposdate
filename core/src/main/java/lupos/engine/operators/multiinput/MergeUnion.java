package lupos.engine.operators.multiinput;

import java.util.Collection;
import java.util.Comparator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

public class MergeUnion extends Union {

	protected QueryResult[] operandResults = null;

	protected final Comparator<Bindings> comparator;

	public MergeUnion(final Comparator<Bindings> comparator) {
		super();
		this.comparator = comparator;
	}

	public MergeUnion(final Collection<Variable> sortCriterium) {
		super();
		this.comparator = new Comparator<Bindings>() {
			public int compare(final Bindings o1, final Bindings o2) {
				for (final Variable var : sortCriterium) {
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

			@Override
			public String toString() {
				return "Comparator on " + sortCriterium;
			}
		};
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		if (operandResults == null)
			operandResults = new QueryResult[precedingOperators.size()];

		operandResults[operandID] = bindings;

		boolean flag = true;
		for (final QueryResult qr : operandResults) {
			if (qr == null) {
				flag = false;
				break;
			}
		}
		if (flag)
			return QueryResult
					.createInstance(new MergeUnionIterator(operandResults,
							succeedingOperators.size() > 1, comparator));
		else
			return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		// do we already process the operand results (then all operandResults
		// are null)?
		// otherwise process it now!
		if (operandResults != null) {
			boolean flag = true;
			for (final QueryResult qr : operandResults) {
				if (qr == null) {
					flag = false;
					break;
				}
			}
			if (!flag)
				for (final OperatorIDTuple opId : succeedingOperators) {
					opId
							.processAll(QueryResult
									.createInstance(new MergeUnionIterator(
											operandResults, succeedingOperators
													.size() > 1, comparator)));
				}
		}
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		// do we already process the operand results (then all operandResults
		// are null)?
		// otherwise process it now!
		if (operandResults != null) {
			boolean flag = true;
			for (final QueryResult qr : operandResults) {
				if (qr == null) {
					flag = false;
					break;
				}
			}
			if (!flag)
				for (final OperatorIDTuple opId : succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(
							QueryResult
									.createInstance(new MergeUnionIterator(
											operandResults, succeedingOperators
													.size() > 1, comparator)),
							debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug,
							opId.getId(), debugstep);
				}
		}
		return msg;
	}

	@Override
	public String toString() {
		return super.toString() + ": " + this.comparator.toString();
	}
}
