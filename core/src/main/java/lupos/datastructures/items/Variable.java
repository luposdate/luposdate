package lupos.datastructures.items;

import java.io.Serializable;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.literal.Literal;

public class Variable implements Serializable, Item, Comparable<Variable> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8944702351169595750L;

	protected final String name;

	public Variable(final String name) {
		this.name = name;
	}

	public boolean isVariable() {
		return true;
	}

	public String getName() {
		return name;
	}

	public Literal getLiteral(final Bindings b) {
		return b.get(this);
	}

	@Override
	public String toString() {
		return "?" + name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		return ((o instanceof Variable) && ((Variable) o).getName().compareTo(
				name) == 0);
	}

	public int compareTo(final Variable o) {
		return name.compareTo(o.getName());
	}

	public boolean equalsNormalOrVariableInInferenceRule(final Object o) {
		return (o instanceof Variable && this.getName().compareTo(
				((Variable) o).getName()) == 0);
	}
}
