package lupos.misc;

public class Triple<T1, T2, T3> {
	protected T1 t1;
	protected T2 t2;
	protected T3 t3;

	public Triple(T1 t1, T2 t2, T3 t3) {
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
	}

	public T1 getFirst() {
		return this.t1;
	}

	public void setFirst(T1 t1) {
		this.t1 = t1;
	}

	public T2 getSecond() {
		return this.t2;
	}

	public void setSecond(T2 t2) {
		this.t2 = t2;
	}

	public T3 getThird() {
		return this.t3;
	}

	public void setThird(T3 t3) {
		this.t3 = t3;
	}

	public boolean equals(Object object) {
		if(object instanceof Triple) {
			Triple triple = (Triple) object;

			if(!this.t1.equals(triple.t1)) {
				return false;
			}

			if(!this.t2.equals(triple.t2)) {
				return false;
			}

			return this.t3.equals(triple.t3);
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		return (int) (((long) this.t1.hashCode() + this.t2.hashCode() + this.t3.hashCode()) % Integer.MAX_VALUE);
	}
	
	public String toString(){
		return "(" + t1.toString()+", "+t2.toString()+", "+t3.toString()+")";
	}
}
