package lupos.engine.operators.stream;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.debug.DebugStep;

public abstract class Window extends TripleOperator implements TripleConsumer,
		TripleDeleter {

	public PatternMatcher getPatternMatcher() {
		if (this.getSucceedingOperators().size() == 1) {
			final BasicOperator bo = this.getSucceedingOperators().get(0)
					.getOperator();
			if (bo instanceof PatternMatcher)
				return (PatternMatcher) bo;
		}
		System.out.println("Error in Window-Operator!");
		return null;
	}

	public void consume(final Triple triple) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			((TripleConsumer) oid.getOperator()).consume(triple);
		}
	}

	public void deleteTriple(final Triple triple) {
		for (final OperatorIDTuple oid : succeedingOperators) {
			((TripleDeleter) oid.getOperator()).deleteTriple(triple);
		}
	}
	
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		for (final OperatorIDTuple oid : this.getSucceedingOperators()) {
			debugstep.step(this, oid.getOperator(), triple);
			((TripleOperator) oid.getOperator()).consume(triple);
		}
	}
	
	public void deleteTripleDebug(final Triple triple, final DebugStep debugstep) {
		for (final OperatorIDTuple oid : succeedingOperators) {
			debugstep.stepDelete(this, oid.getOperator(), triple);
			((TripleDeleter) oid.getOperator()).deleteTripleDebug(triple,
					debugstep);
		}
	}
}
