package lupos.datastructures.items;

public class TimestampedVariable extends Variable {

	private final long timestamp;

	public TimestampedVariable(final Variable var, final long timestamp) {
		super(var.name);
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
