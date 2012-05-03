package lupos.engine.operators.messages;

import java.util.Collection;

import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.DebugStep;

public class EndOfEvaluationMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6728210701766994754L;

	@Override
	public Message postProcess(final BasicOperator op) {
		return op.postProcessMessage(this);
	}

	@Override
	public Message preProcess(final BasicOperator op) {
		return op.preProcessMessage(this);
	}

	@Override
	public Message merge(final Collection<Message> msgs, final BasicOperator op) {
		return op.mergeMessages(msgs, this);
	}

	// no special content of this class to clone => just return this
	@Override
	public Message clone() {
		return this;
	}
	
	public Message preProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return op.preProcessMessageDebug(this, debugstep);
	}
}
