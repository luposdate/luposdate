package lupos.engine.operators.stream;

import lupos.datastructures.items.Triple;
import lupos.engine.operators.application.CollectResult;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;

public class StreamTriples extends Stream {

	protected final int numberOfTriples;
	protected int count = 0;

	public StreamTriples(final CollectResult cr, final int numberOfTriples) {
		super(cr);
		this.numberOfTriples = numberOfTriples;
	}

	public Message preprocessMessage(final StartOfEvaluationMessage msg) {
		count = 0;
		return msg;
	}

	@Override
	public void consume(final Triple triple) {
		super.consume(triple);
		count++;
		if (count >= numberOfTriples) {
			count = 0;
			this.notifyStreamResults();
		}
	}

	@Override
	public String toString() {
		return super.toString()+" " + numberOfTriples;
	}
}
