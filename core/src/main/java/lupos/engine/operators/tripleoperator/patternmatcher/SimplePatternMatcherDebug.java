package lupos.engine.operators.tripleoperator.patternmatcher;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.misc.debug.DebugStep;

public class SimplePatternMatcherDebug extends SimplePatternMatcher {
	private final SimplePatternMatcher original;
	private final DebugStep debugstep;

	public SimplePatternMatcherDebug(final SimplePatternMatcher original,
			final DebugStep debugstep) {
		this.succeedingOperators = original.getSucceedingOperators();
		this.original = original;
		this.debugstep = debugstep;
	}

	@Override
	public void consume(final Triple triple) {
		for (final OperatorIDTuple opOuter : original.getSucceedingOperators()) {
			debugstep.step(this, (opOuter.getOperator()), triple);
			((TripleConsumerDebug) opOuter.getOperator()).consumeDebug(triple,
					debugstep);
		}
	}
}
