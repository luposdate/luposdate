package lupos.engine.operators.stream;

import java.util.Date;

import lupos.datastructures.items.Triple;
import lupos.misc.debug.DebugStep;

public class StreamDurationDebug extends StreamDuration {
	
	private final DebugStep debugstep;

	public StreamDurationDebug(final StreamDuration stream,
			final DebugStep debugstep) {
		super(stream.collectResult, stream.duration);
		this.collectResult = stream.collectResult;
		this.debugstep=debugstep;
		this.notifyStreamResults = stream.notifyStreamResults;
		this.succeedingOperators = stream.getSucceedingOperators();
	}

	@Override
	public void consume(final Triple triple) {
		super.consumeDebug(triple, debugstep);
		final long now = (new Date()).getTime();
		if (now - lastTime >= duration) {
			lastTime = now;
			this.notifyStreamResultsDebug(debugstep);
		}
	}
}
