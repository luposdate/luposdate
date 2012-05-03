package lupos.engine.operators.stream;

import java.util.LinkedList;

import lupos.datastructures.items.TimestampedTriple;
import lupos.datastructures.items.Triple;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.misc.debug.DebugStep;

public class WindowDuration extends Window {

	private final int duration;
	private LinkedList<TimestampedTriple> tripleList;

	public WindowDuration(final int duration) {
		this.duration = duration;
	}

	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage message) {
		tripleList = new LinkedList<TimestampedTriple>();
		return message;
	}

	@Override
	public void consume(final Triple triple) {
		final TimestampedTriple timestampedTriple = (TimestampedTriple) triple;
		final long now = timestampedTriple.getTimestamp();
		int index = 0;
		for (final TimestampedTriple t : tripleList) {
			if (now - t.getTimestamp() >= duration) {
				this.deleteTriple(t);
				index++;
			} else
				break;
		}
		while (index > 0) {
			this.tripleList.remove(0);
			index--;
		}
		tripleList.add(timestampedTriple);
		super.consume(timestampedTriple);
	}
	
	@Override
	public void consumeDebug(final Triple triple, final DebugStep debugstep) {
		final TimestampedTriple timestampedTriple = (TimestampedTriple) triple;
		final long now = timestampedTriple.getTimestamp();
		int index = 0;
		for (final TimestampedTriple t : tripleList) {
			if (now - t.getTimestamp() >= duration) {
				this.deleteTripleDebug(t, debugstep);
				index++;
			} else
				break;
		}
		while (index > 0) {
			this.tripleList.remove(0);
			index--;
		}
		tripleList.add(timestampedTriple);
		super.consumeDebug(timestampedTriple, debugstep);
	}

	@Override
	public String toString() {
		return super.toString()+" " + duration;
	}
}
