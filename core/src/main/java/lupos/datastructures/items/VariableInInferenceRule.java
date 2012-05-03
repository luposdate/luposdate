package lupos.datastructures.items;

public class VariableInInferenceRule extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3447987827099784338L;

	public VariableInInferenceRule(final String name) {
		super(name.endsWith("?") ? name.substring(0, name.length() - 1) : name);
	}

	@Override
	public int hashCode() {
		// make the hash code different from a "normal" variable with the same
		// name!
		return ("?" + name + "?").hashCode();
	}

	@Override
	public String toString() {
		return "?" + name;
	}

	@Override
	public boolean equals(final Object o) {
		return ((o instanceof VariableInInferenceRule) && ((VariableInInferenceRule) o)
				.getName().compareTo(name) == 0);
	}
}
