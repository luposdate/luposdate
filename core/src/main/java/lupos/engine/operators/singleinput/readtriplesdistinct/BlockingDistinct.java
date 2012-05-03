package lupos.engine.operators.singleinput.readtriplesdistinct;

import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

public class BlockingDistinct extends ReadTriplesDistinct {

	protected Set<BindingsArrayReadTriples> bindings;

	public BlockingDistinct(final Set<BindingsArrayReadTriples> bindings) {
		this.bindings = bindings;
	}

	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext()) {
			final BindingsArrayReadTriples bart = (BindingsArrayReadTriples) itb
					.next();
			bart.sortReadTriples();
			bindings.add(bart);
		}
		return null;
	}

	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<BindingsArrayReadTriples> itb = this.bindings.iterator();
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
		final QueryResult qr = QueryResult.createInstance(bindings.iterator());
		if (succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : succeedingOperators) {
			opId.processAll(qr);
		}
		bindings.clear();
		return msg;
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		final QueryResult qr = QueryResult.createInstance(bindings.iterator());
		if (succeedingOperators.size() > 1)
			qr.materialize();
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(qr,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		bindings.clear();
		return msg;
	}

	@Override
	public String toString() {
		return super.toString()+" for read triples";
	}

}