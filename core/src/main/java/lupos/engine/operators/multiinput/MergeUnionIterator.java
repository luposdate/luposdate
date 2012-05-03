package lupos.engine.operators.multiinput;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;

public class MergeUnionIterator extends MergeIterator<Bindings> implements
		Iterator<Bindings> {

	public MergeUnionIterator(final QueryResult[] operandResults,
			final boolean moreThanOnce, final Comparator<Bindings> comparator) {
		init(getArray(operandResults, moreThanOnce), comparator);
	}

	protected Iterator<Bindings>[] getArray(final QueryResult[] operandResults,
			final boolean moreThanOnce) {
		int minus = 0;
		for (int i = 0; i < operandResults.length; i++) {
			if (operandResults[i] == null)
				minus--;
		}
		final Iterator<Bindings>[] ita = new Iterator[operandResults.length
				- minus];
		int index = 0;
		for (int i = 0; i < operandResults.length; i++) {
			if (operandResults[i] != null)
				ita[index++] = (moreThanOnce) ? operandResults[i].iterator()
						: operandResults[i].oneTimeIterator();
		}
		return ita;
	}

	protected Iterator<Bindings>[] getArray(
			final List<QueryResult> operandResults, final boolean moreThanOnce) {
		final Iterator<Bindings>[] ita = new Iterator[operandResults.size()];
		final Iterator<QueryResult> itqr = operandResults.iterator();
		for (int i = 0; i < operandResults.size(); i++) {
			final QueryResult qr = itqr.next();
			ita[i] = (moreThanOnce) ? qr.iterator() : qr.oneTimeIterator();
		}
		return ita;
	}

	protected Iterator<Bindings>[] getArray(
			final Collection<Iterator<Bindings>> operandResults) {
		final Iterator<Bindings>[] ita = new Iterator[operandResults.size()];
		final Iterator<Iterator<Bindings>> ititb = operandResults.iterator();
		for (int i = 0; i < operandResults.size(); i++) {
			ita[i] = ititb.next();
		}
		return ita;
	}

	public MergeUnionIterator(
			final Collection<Iterator<Bindings>> operandResults,
			final Comparator<Bindings> comparator) {
		init(getArray(operandResults), comparator);
	}

	public MergeUnionIterator(
			final Collection<Iterator<Bindings>> operandResults,
			final Collection<Variable> toCompare) {
		this(operandResults, new Comparator<Bindings>() {
			public int compare(final Bindings o1, final Bindings o2) {
				for (final Variable var : toCompare) {
					final Literal l1 = o1.get(var);
					final Literal l2 = o2.get(var);
					if (l1 != null && l2 != null) {
						final int compare = l1
								.compareToNotNecessarilySPARQLSpecificationConform(l2);
						if (compare != 0)
							return compare;
					} else if (l1 != null)
						return -1;
					else if (l2 != null)
						return 1;
				}
				return 0;
			}
		});
	}

	public MergeUnionIterator(final QueryResult[] operandResults,
			final boolean moreThanOnce, final Collection<Variable> toCompare) {
		this(operandResults, moreThanOnce, new Comparator<Bindings>() {
			public int compare(final Bindings o1, final Bindings o2) {
				for (final Variable var : toCompare) {
					final Literal l1 = o1.get(var);
					final Literal l2 = o2.get(var);
					if (l1 != null && l2 != null) {
						final int compare = l1
								.compareToNotNecessarilySPARQLSpecificationConform(l2);
						if (compare != 0)
							return compare;
					} else if (l1 != null)
						return -1;
					else if (l2 != null)
						return 1;
				}
				return 0;
			}
		});
	}

	public MergeUnionIterator(final List<QueryResult> operandResults,
			final boolean moreThanOnce, final Collection<Variable> toCompare) {
		this(operandResults, moreThanOnce, new Comparator<Bindings>() {
			public int compare(final Bindings o1, final Bindings o2) {
				for (final Variable var : toCompare) {
					final Literal l1 = o1.get(var);
					final Literal l2 = o2.get(var);
					if (l1 != null && l2 != null) {
						final int compare = l1
								.compareToNotNecessarilySPARQLSpecificationConform(l2);
						if (compare != 0)
							return compare;
					} else if (l1 != null)
						return -1;
					else if (l2 != null)
						return 1;
				}
				return 0;
			}
		});
	}

	public MergeUnionIterator(final List<QueryResult> operandResults,
			final boolean moreThanOnce, final Comparator<Bindings> comparator) {
		init(getArray(operandResults, moreThanOnce), comparator);
	}
}
