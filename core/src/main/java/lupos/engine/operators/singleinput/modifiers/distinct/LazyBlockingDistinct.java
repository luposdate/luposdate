package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

/**
 * This class is similar to BlockingDistinct, but adds bindings to its set only
 * after EndOfStreamMessage or ComputeIntermediateResultMessage. In this way, no
 * errors occur for Stream-processing with windows!
 * 
 */
public class LazyBlockingDistinct extends Distinct {

	protected final Set<Bindings> bindings;
	protected QueryResult operandsData;

	public LazyBlockingDistinct() {
		bindings = new HashSet<Bindings>();
	}

	public LazyBlockingDistinct(final Set<Bindings> bindings) {
		this.bindings = bindings;
	}

	@Override
	public synchronized QueryResult process(final QueryResult queryResult,
			final int operandID) {
		if (operandsData == null) {
			operandsData = queryResult;
		} else
			operandsData.addAll(queryResult);
		return null;
	}

	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<Bindings> itb = this.bindings.iterator();
		return new ParallelIterator<Bindings>() {

			public void close() {
				// derived classes may override the above method in order to
				// release some resources here!
			}

			public boolean hasNext() {
				return itb.hasNext();
			}

			public Bindings next() {
				return itb.next();
			}

			public void remove() {
				itb.remove();
			}

		};
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		bindings.clear();
		if (operandsData != null) {
			final Iterator<Bindings> itb2 = operandsData.oneTimeIterator();
			while (itb2.hasNext())
				bindings.add(itb2.next());
		}
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : succeedingOperators) {
			opId.processAll(qr);
		}
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		this.deleteAllAtSucceedingOperators();
		preProcessMessage(new EndOfEvaluationMessage());
		return msg;
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		operandsData.removeAll(queryResult);
		return null;
	}

	public void deleteQueryResult(final int operandID) {
		bindings.clear();
		if (operandsData != null)
			operandsData.release();
		operandsData = null;
	}

	protected boolean isPipelineBreaker() {
		return true;
	}

	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		this.deleteAllDebugAtSucceedingOperators(debugstep);
		preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		bindings.clear();
		if (operandsData != null) {
			final Iterator<Bindings> itb2 = operandsData.oneTimeIterator();
			while (itb2.hasNext())
				bindings.add(itb2.next());
		}
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		return msg;
	}
}