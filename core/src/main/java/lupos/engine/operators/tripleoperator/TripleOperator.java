package lupos.engine.operators.tripleoperator;

import java.util.List;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.misc.debug.DebugStep;

public class TripleOperator extends BasicOperator implements TripleConsumer, TripleConsumerDebug {

	public TripleOperator() {
	}

	public TripleOperator(final List<OperatorIDTuple> succeedingOperators) {
		super(succeedingOperators);
	}

	public TripleOperator(final OperatorIDTuple succeedingOperator) {
		super(succeedingOperator);
	}

	@Override
	public void consume(final Triple triple) {
		throw (new UnsupportedOperationException("This Operator(" + this
				+ ") should have been replaced before being used."));
	}
	
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		consume(triple);
	}
}
