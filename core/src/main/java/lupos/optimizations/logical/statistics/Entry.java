package lupos.optimizations.logical.statistics;

import lupos.datastructures.items.literal.Literal;

public class Entry implements Cloneable {
	public double selectivity = 0.0;
	public double distinctLiterals = 0.0;
	public Literal literal = null;

	@Override
	public Object clone() {
		final Entry e = new Entry();
		e.selectivity = selectivity;
		e.literal = literal;
		e.distinctLiterals = distinctLiterals;
		return e;
	}
}
