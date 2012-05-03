package lupos.engine.operators.singleinput.sort.fastsort;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.tosort.ToSort.TOSORT;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class FastSortLazyLiteral extends FastSort {

	public static final int HEAPHEIGHT = 16;

	public FastSortLazyLiteral(final BasicOperator root,
			final List<TriplePattern> triplePatterns,
			final Collection<Variable> sortCriterium) {
		super(triplePatterns, sortCriterium);
	}

	private BasicIndex findIndex(final BasicOperator root,
			final TriplePattern triplePattern) {
		for (final OperatorIDTuple opID : root.getSucceedingOperators()) {
			final BasicOperator bo = opID.getOperator();
			if (bo instanceof BasicIndex) {
				if (((BasicIndex) bo).getTriplePattern()
						.contains(triplePattern))
					return (BasicIndex) bo;
			}
		}
		return null;
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {

		final DBMergeSortedBag<Bindings> bag = new DBMergeSortedBag<Bindings>(
				HEAPHEIGHT, new Comparator<Bindings>() {
					public int compare(final Bindings arg0, final Bindings arg1) {
						for (final Variable var : sortCriterium) {
							final Literal l1 = arg0.get(var);
							final Literal l2 = arg1.get(var);
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
				}, Bindings.class, TOSORT.PARALLELMERGESORT);

		final Iterator<Bindings> it = bindings.oneTimeIterator();

		while (it.hasNext()) {
			bag.add(it.next());
		}

		return QueryResult.createInstance(bag.iterator());
	}
}
