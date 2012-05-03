package lupos.engine.operators.stream;

import java.util.Date;

import lupos.datastructures.items.TimestampedTriple;
import lupos.datastructures.items.Triple;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.misc.debug.DebugStep;

public class WindowTriples extends Window {

	private int numberOfTriples = 0;
	// ring buffer for storing the triples of the window:
	private TimestampedTriple[] triplesInWindow;
	private int start = -1;
	private int end = 0;

	public WindowTriples(final int numberOfTriples) {
		if (numberOfTriples < 1) {
			System.err
					.println("X must be >=1 for WINDOW TYPE SLIDINGTRIPLES X");
			System.err.println("Assuming WINDOW TYPE SLIDINGTRIPLES 1...");
			this.numberOfTriples = 1;
		} else
			this.numberOfTriples = numberOfTriples;
	}

	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage message) {
		triplesInWindow = new TimestampedTriple[numberOfTriples];
		start = -1;
		end = 0;
		return message;
	}

	@Override
	public void consume(final Triple triple) {
		if (end == start) {
			// ring buffer is full
			this.deleteTriple(triplesInWindow[start]);
			start = (start + 1) % numberOfTriples;
		} else {
			if (start == -1)
				start = 0;
		}
		final TimestampedTriple t = new TimestampedTriple(triple, (new Date())
				.getTime());
		triplesInWindow[end] = t;
		end = (end + 1) % numberOfTriples;
		super.consume(t);
	}
	
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		if (end == start) {
			// ring buffer is full
			this.deleteTripleDebug(triplesInWindow[start], debugstep);
			start = (start + 1) % numberOfTriples;
		} else {
			if (start == -1)
				start = 0;
		}
		final TimestampedTriple t = new TimestampedTriple(triple, (new Date())
				.getTime());
		triplesInWindow[end] = t;
		end = (end + 1) % numberOfTriples;
		super.consumeDebug(t, debugstep);
	}

	@Override
	public String toString() {
		return super.toString()+" " + numberOfTriples;
	}
}
