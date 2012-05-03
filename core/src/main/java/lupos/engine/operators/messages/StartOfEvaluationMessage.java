package lupos.engine.operators.messages;

import java.util.Collection;

import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.DebugStep;



public class StartOfEvaluationMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 148925743153694571L;

	@Override
	public Message postProcess(final BasicOperator op) {
		return op.postProcessMessage(this);
	}

	@Override
	public Message preProcess(final BasicOperator op) {
		return op.preProcessMessage(this);
	}
	
	public Message postProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return op.postProcessMessageDebug(this, debugstep);
	}

	@Override
	public Message merge(final Collection<Message> msgs, final BasicOperator op) {
		return op.mergeMessages(msgs, this);
	}

	// no special content of this class to clone => just return this
	@Override
	public Message clone(){
		return this;
	}
}
