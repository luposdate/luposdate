package lupos.datastructures.items;

public class TimestampedTriple extends Triple {

	private final long timestamp;

	public TimestampedTriple(final Triple triple, final long timestamp) {
		this.timestamp = timestamp;
		this.subject = triple.subject;
		this.predicate = triple.predicate;
		this.object = triple.object;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Variable getVariable(final Variable var) {
		return new TimestampedVariable(var, timestamp);
	}
}
