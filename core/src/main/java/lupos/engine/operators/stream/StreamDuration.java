package lupos.engine.operators.stream;

import java.util.Date;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;

public class StreamDuration extends Stream {

	protected final int duration;
	protected long lastTime;

	public StreamDuration(final CollectResult cr, final int duration) {
		super(cr);
		this.duration = duration;
	}

	public Message preprocessMessage(final StartOfEvaluationMessage msg) {
		lastTime = (new Date()).getTime();
		return msg;
	}

	@Override
	public void consume(final Triple triple) {
		super.consume(triple);
		final long now = (new Date()).getTime();
		if (now - lastTime >= duration) {
			lastTime = now;
			this.notifyStreamResults();
		}
	}

	@Override
	public String toString() {
		return super.toString()+" " + duration;
	}
}
