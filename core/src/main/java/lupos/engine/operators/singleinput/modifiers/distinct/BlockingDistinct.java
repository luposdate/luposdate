package lupos.engine.operators.singleinput.modifiers.distinct;

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

public class BlockingDistinct extends Distinct {

	protected Set<Bindings> bindings;

	public BlockingDistinct(final Set<Bindings> bindings) {
		this.bindings = bindings;
	}

	@Override
	public synchronized QueryResult process(final QueryResult queryResult,
			final int operandID) {
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext())
			bindings.add(itb.next());
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
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		// final QueryResult qr = QueryResult.createInstance();
		// for (final Bindings b : bindings)
		// qr.add(b);
		for (final OperatorIDTuple opId : succeedingOperators) {
			opId.processAll(qr);
		}
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		preProcessMessage(new EndOfEvaluationMessage());
		return msg;
	}
	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		// problem: it does not count the number of occurences of a binding
		// i.e. { ?a=<a> }, { ?a=<a> } and delete { ?a=<a> } will result in
		// {} instead of { ?a=<a> }!!!!!!
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext())
			bindings.remove(itb.next());
		return null;
	}

	public void deleteAll(final int operandID) {
		bindings.clear();
	}

	protected boolean isPipelineBreaker() {
		return true;
	}
	
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return msg;
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (this.succeedingOperators.size() > 1)
			qr.materialize();
		// final QueryResult qr = QueryResult.createInstance();
		// for (final Bindings b : bindings)
		// qr.add(b);
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		return msg;
	}
}