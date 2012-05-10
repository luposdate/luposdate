package lupos.gui.debug;

import lupos.engine.operators.BasicOperator;

/**
 * The class StepContainer just contains all neccessary values of a single step
 * in order to reproduce it through the use of the RingBuffer
 * 
 * @author Markus & Peter
 * 
 */
public class StepContainer {
	private final BasicOperator from;
	private final BasicOperator to;
	private final Object object;
	private final boolean deleteStep;

	/**
	 * Constructor
	 * 
	 * @param from
	 * @param to
	 * @param object
	 */
	public StepContainer(final BasicOperator from, final BasicOperator to,
			final Object object, final boolean deleteStep) {
		super();
		this.from = from;
		this.to = to;
		this.object = object;
		this.deleteStep = deleteStep;
	}

	public BasicOperator getFrom() {
		return from;
	}

	public BasicOperator getTo() {
		return to;
	}

	public Object getObject() {
		return object;
	}

	public boolean isDeleteStep() {
		return deleteStep;
	}
}