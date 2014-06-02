package lupos.engine.operators.multiinput.mergeunion;

import java.util.Comparator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.Union;
import lupos.misc.debug.DebugStep;

public class ComparatorMergeUnion extends Union {

	protected QueryResult[] operandResults = null;

	protected final Comparator<Bindings> comparator;

	public ComparatorMergeUnion(final Comparator<Bindings> comparator) {
		super();
		this.comparator = comparator;
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		if (this.operandResults == null) {
			this.operandResults = new QueryResult[this.precedingOperators.size()];
		}

		this.operandResults[operandID] = bindings;

		boolean flag = true;
		for (final QueryResult qr : this.operandResults) {
			if (qr == null) {
				flag = false;
				break;
			}
		}
		if (flag) {
			return QueryResult
					.createInstance(new MergeUnionIterator(this.operandResults,
							this.succeedingOperators.size() > 1, this.comparator));
		} else {
			return null;
		}
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		// do we already process the operand results (then all operandResults
		// are null)?
		// otherwise process it now!
		if (this.operandResults != null) {
			boolean flag = true;
			for (final QueryResult qr : this.operandResults) {
				if (qr == null) {
					flag = false;
					break;
				}
			}
			if (!flag) {
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					opId
							.processAll(QueryResult
									.createInstance(new MergeUnionIterator(
											this.operandResults, this.succeedingOperators
													.size() > 1, this.comparator)));
				}
			}
		}
		return msg;
	}

	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		// do we already process the operand results (then all operandResults
		// are null)?
		// otherwise process it now!
		if (this.operandResults != null) {
			boolean flag = true;
			for (final QueryResult qr : this.operandResults) {
				if (qr == null) {
					flag = false;
					break;
				}
			}
			if (!flag) {
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(
							QueryResult
									.createInstance(new MergeUnionIterator(
											this.operandResults, this.succeedingOperators
													.size() > 1, this.comparator)),
							debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug,
							opId.getId(), debugstep);
				}
			}
		}
		return msg;
	}

	@Override
	public String toString() {
		return super.toString() + ": " + this.comparator.toString();
	}

}
