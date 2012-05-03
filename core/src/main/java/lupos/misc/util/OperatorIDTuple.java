package lupos.misc.util;

public class OperatorIDTuple<T> {
	protected T op;
	protected int id;

	public OperatorIDTuple(T op, int id) {
		this.op = op;
		this.id = id;
	}

	public String toString() {
		return id + ": " + this.op;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public T getOperator() {
		return this.op;
	}

	public void setOperator(final T op) {
		this.op = op;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if(o instanceof OperatorIDTuple) {
			OperatorIDTuple<T> oid = (OperatorIDTuple<T>) o;

			if(oid.getOperator().equals(this.getOperator())) {
				if(this.getId() < 0 || oid.getId() < 0) {
					return true;
				}

				return this.getId() == oid.getId();
			}
		}

		return false;
	}
}
