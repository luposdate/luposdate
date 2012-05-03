package lupos.engine.operators.tripleoperator.patternmatcher;

import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.items.Triple;

public class RDFSSimplePatternMatcher extends SimplePatternMatcher {
	protected Set<Triple> alreadyProcessed = new HashSet<Triple>();

	@Override
	public void consume(final Triple triple) {
		if (alreadyProcessed.contains(triple))
			return;
		alreadyProcessed.add(triple);
		super.consume(triple);
	}
}
