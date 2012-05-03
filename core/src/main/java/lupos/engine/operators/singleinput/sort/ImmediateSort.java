package lupos.engine.operators.singleinput.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.sorteddata.SortedBag;

public class ImmediateSort extends Sort {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6875180850101362304L;
	final static int HEAPHEIGHT = 5;
	protected Comparator<Bindings> compLocal = comparator;
	protected List<Variable> sortCriterium;

	public ImmediateSort() {
		super();
	}

	public ImmediateSort(final List<Variable> sortCriterium) {
		super();
		this.sortCriterium = sortCriterium;
		compLocal = new Comparator<Bindings>() {

			public int compare(final Bindings o1, final Bindings o2) {
				for (final Variable var : sortCriterium) {
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

		};
	}

	public ImmediateSort(final lupos.sparql1_1.Node node) {
		super(node);
	}

	public Collection<Variable> getSortCriterium() {
		return sortCriterium;
	}

	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		final SortedBag<Bindings> sswd = createBag();
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (itb.hasNext()) {
			sswd.add(itb.next());
		}
		return QueryResult.createInstance(getIterator(sswd.iterator()));
	}

	protected SortedBag<Bindings> createBag() {
		return new DBMergeSortedBag<Bindings>(HEAPHEIGHT, compLocal,
				Bindings.class);
	}

	protected ParallelIterator<Bindings> getIterator(
			final Iterator<Bindings> itb) {
		return new ParallelIterator<Bindings>() {

			public void close() {
				if (itb instanceof ParallelIterator)
					((ParallelIterator<Bindings>) itb).close();
			}

			public boolean hasNext() {
				return itb.hasNext();
			}

			public Bindings next() {
				return itb.next();
			}

			public void remove() {
				itb.remove();
			}

		};
	}
}
