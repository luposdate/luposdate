package lupos.datastructures.items;

public class BlankNode extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3447987827099784338L;

	public BlankNode(final String name) {
		super(name);
	}

	@Override
	public int hashCode() {
		// make the hash code different from a "normal" variable with the same
		// name!
		return ("_" + name + "_").hashCode();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "_:" + name;
	}

	@Override
	public boolean equals(final Object o) {
		return ((o instanceof BlankNode) && ((BlankNode) o).getName()
				.compareTo(name) == 0);
	}
}
