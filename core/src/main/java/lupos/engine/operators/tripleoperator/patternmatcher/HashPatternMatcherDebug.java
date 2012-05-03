package lupos.engine.operators.tripleoperator.patternmatcher;

import java.util.Vector;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TripleConsumerDebug;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.misc.debug.DebugStep;

public class HashPatternMatcherDebug extends HashPatternMatcher {

	private final HashPatternMatcher original;
	private final DebugStep debugstep;

	public HashPatternMatcherDebug(final HashPatternMatcher original,
			final DebugStep debugstep) {
		this.succeedingOperators = original.getSucceedingOperators();
		this.original = original;
		this.debugstep = debugstep;
	}

	@Override
	public void consume(final Triple triple) {

		final Vector<Integer> MatchingTriplePatterns = new Vector<Integer>();
		// consider all 8 possibilities:
		final String[] combinations = {
				"||",
				triple.getSubject().toString() + "||",
				"|" + triple.getPredicate().toString() + "|",
				triple.getSubject().toString() + "|"
						+ triple.getPredicate().toString() + "|",
				"||" + triple.getObject().toString(),
				triple.getSubject().toString() + "||"
						+ triple.getObject().toString(),
				"|" + triple.getPredicate().toString() + "|"
						+ triple.getObject().toString(),
				triple.getSubject().toString() + "|"
						+ triple.getPredicate().toString() + "|"
						+ triple.getObject().toString() };
		Vector<Integer> inter;
		for (final String combination : combinations) {
			inter = original.keysOfTriplePatterns.get(combination);
			if (inter != null)
				MatchingTriplePatterns.addAll(inter);
		}
		final TriplePattern[] tp = original.getTriplePatterns();
		for (int i = 0; i < MatchingTriplePatterns.size(); i++) {
			final int num = MatchingTriplePatterns.get(i).intValue();
			debugstep.step(this, tp[num], triple);
			tp[num].consumeDebug(triple, debugstep);
		}
	}
}
