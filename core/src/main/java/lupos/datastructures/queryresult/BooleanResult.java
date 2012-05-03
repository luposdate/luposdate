package lupos.datastructures.queryresult;

public class BooleanResult extends QueryResult {
	/**
	 * 
	 */
	private static final long serialVersionUID = -788500826155688680L;

	@Override
	public String toString() {
		return Boolean.toString(isTrue());
	}
	
	public boolean isTrue() {
		return !isEmpty();
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof BooleanResult && ((BooleanResult)o).isTrue() == isTrue()
		    || o instanceof Boolean && ((Boolean)o).booleanValue() == isTrue(); 
	}
}
