package lupos.engine.operators.tripleoperator.patternmatcher;

import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.items.Triple;
import lupos.misc.debug.DebugStep;

public class RDFSSimplePatternMatcherDebug extends SimplePatternMatcherDebug {

	protected Set<Triple> alreadyProcessed = new HashSet<Triple>();

	public RDFSSimplePatternMatcherDebug(final SimplePatternMatcher original,
			final DebugStep debugstep) {
		super(original, debugstep);
	}

	@Override
	public void consume(final Triple triple) {
		if (alreadyProcessed.contains(triple))
			return;
		alreadyProcessed.add(triple);
		super.consume(triple);
	}
}
