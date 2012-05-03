package lupos.engine.operators.stream;

import lupos.datastructures.items.Triple;
import lupos.misc.debug.DebugStep;

public interface TripleDeleter {
	public void deleteTriple(Triple triple);
	public void deleteTripleDebug(final Triple triple, final DebugStep debugstep);
}
