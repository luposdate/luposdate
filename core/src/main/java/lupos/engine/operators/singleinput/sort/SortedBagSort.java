package lupos.engine.operators.singleinput.sort;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.datastructures.sorteddata.SortedBag;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

public abstract class SortedBagSort extends Sort {

	protected SortedBag<Bindings> sswd;

	public SortedBagSort() {
		super();
	}

	public SortedBagSort(final SortedBag<Bindings> sswd,
			final lupos.sparql1_1.Node node) {
		super(node);
		this.sswd = sswd;
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (itb.hasNext()) {
			sswd.add(itb.next());
		}
		return null;
	}

	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<Bindings> itb = sswd.iterator();
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
	
	private void computeResult(){
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : succeedingOperators) {
			opId.processAll(qr);
		}
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		computeResult();
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		computeResult();
		return msg;
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		// problem: it does not count the number of occurences of a binding
		// i.e. { ?a=<a> }, { ?a=<a> } and delete { ?a=<a> } will result in
		// {} instead of { ?a=<a> }!!!!!!
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext())
			sswd.remove(itb.next());
		return null;
	}

	public void deleteAll(final int operandID) {
		sswd.clear();
	}

	protected boolean isPipelineBreaker() {
		return true;
	}
	
	private void computeResultDebug(final DebugStep debugstep){
		final QueryResult qr = QueryResult.createInstance(getIterator());
		if (succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
	}

	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		computeResultDebug(debugstep);
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		computeResultDebug(debugstep);
		return msg;
	}
}
