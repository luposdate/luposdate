package lupos.misc;

public class Tuple<T1, T2> {

	protected T1 t1;
	protected T2 t2;

	public Tuple(final T1 t1, final T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	public T1 getFirst() {
		return t1;
	}

	public void setFirst(final T1 t1) {
		this.t1 = t1;
	}

	public T2 getSecond() {
		return t2;
	}

	public void setSecond(final T2 t2) {
		this.t2 = t2;
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof Tuple) {
			final Tuple tuple = (Tuple) object;
			if (!this.t1.equals(tuple.t1))
				return false;
			return this.t2.equals(tuple.t2);
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return (int) (((long) t1.hashCode() + t2.hashCode()) % Integer.MAX_VALUE);
	}
	
	public String toString(){
		return "(" + t1.toString()+", "+t2.toString()+")";
	}
}
