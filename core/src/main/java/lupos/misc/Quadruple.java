package lupos.misc;

public class Quadruple<T1, T2, T3, T4> extends Triple<T1, T2, T3> {
	protected T4 t4;

	public Quadruple(final T1 t1, final T2 t2, final T3 t3, final T4 t4) {
		super(t1, t2, t3);
		this.t4 = t4;
	}

	public T4 getFourth() {
		return this.t4;
	}

	public void setFourth(final T4 t4) {
		this.t4 = t4;
	}

	@Override
	public boolean equals(final Object object) {
		if(object instanceof Quadruple) {
			if(!super.equals(object)) {
				return false;
			}

			@SuppressWarnings("unchecked")
			final Quadruple<T1, T2, T3, T4> quadruple = (Quadruple<T1, T2, T3, T4>) object;

			return this.t4.equals(quadruple.t4);
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) (((long) this.t1.hashCode() + this.t2.hashCode() + this.t3.hashCode() + this.t4.hashCode() ) % Integer.MAX_VALUE);
	}

	@Override
	public String toString(){
		return "(" + this.t1.toString()+", "+this.t2.toString()+", "+this.t3.toString()+", "+this.t4.toString()+")";
	}
}
