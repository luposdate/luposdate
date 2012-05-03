package lupos.engine.operators.messages;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import lupos.engine.operators.BasicOperator;
import lupos.misc.debug.DebugStep;

public abstract class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3183344099291306589L;

	protected static int lastId;
	protected int id;
	protected HashSet<BasicOperator> visited = new HashSet<BasicOperator>();

	public boolean hasVisited(final BasicOperator op) {
		return visited.contains(op);
	}

	public void setVisited(final BasicOperator op) {
		visited.add(op);
	}

	public Message() {
		id = ++lastId;
	}

	public Message(final Message msg) {
		id = msg.id;
		visited = (HashSet<BasicOperator>) msg.visited.clone();
	}

	public abstract Message postProcess(BasicOperator op);

	public abstract Message preProcess(BasicOperator op);
	
	public abstract Message merge(Collection<Message> msgs, BasicOperator op);

	public String getId() {
		return this.getClass().getSimpleName() + " (Message ID " + id + ")";
	}

	@Override
	public String toString() {
		return getId();
	}

	@Override
	public abstract Message clone();
	
	public Message postProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return postProcess(op);
	}

	public Message preProcessDebug(final BasicOperator op,
			final DebugStep debugstep) {
		return preProcess(op);
	}
}
