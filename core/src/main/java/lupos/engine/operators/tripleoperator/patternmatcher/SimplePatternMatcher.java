package lupos.engine.operators.tripleoperator.patternmatcher;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TripleConsumer;

public class SimplePatternMatcher extends PatternMatcher {

	@Override
	public void consume(final Triple triple) {
		for (final OperatorIDTuple opOuter : getSucceedingOperators()) {
			((TripleConsumer) opOuter.getOperator()).consume(triple);
		}
	}
}
