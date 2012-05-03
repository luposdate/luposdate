package lupos.engine.operators.tripleoperator;

import lupos.datastructures.items.Triple;
import lupos.misc.debug.DebugStep;

public interface TripleConsumerDebug {
	public void consumeDebug(Triple triple, DebugStep debugStep);
}