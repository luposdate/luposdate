package lupos.engine.operators.stream;

import lupos.datastructures.items.Triple;
import lupos.misc.debug.DebugStep;

public class StreamTriplesDebug extends StreamTriples {

	private final DebugStep debugstep;

	public StreamTriplesDebug(final StreamTriples stream,
			final DebugStep debugstep) {
		super(stream.collectResult, stream.numberOfTriples);
		this.collectResult = stream.collectResult;
		this.debugstep = debugstep;
		this.notifyStreamResults = stream.notifyStreamResults;
		this.succeedingOperators = stream.getSucceedingOperators();
	}

	@Override
	public void consume(final Triple triple) {
		super.consumeDebug(triple, debugstep);
		count++;
		if (count >= numberOfTriples) {
			count = 0;
			this.notifyStreamResultsDebug(debugstep);
		}
	}
}
