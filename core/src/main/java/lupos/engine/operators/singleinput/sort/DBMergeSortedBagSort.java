package lupos.engine.operators.singleinput.sort;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;

public class DBMergeSortedBagSort extends SortedBagSort {
	final static int HEAPHEIGHT = 5;

	public DBMergeSortedBagSort() {
		super();
	}

	public DBMergeSortedBagSort(final lupos.sparql1_1.Node node) {
		super(new DBMergeSortedBag<Bindings>(HEAPHEIGHT, new ComparatorAST(node),
				Bindings.class), node);
	}

	@Override
	public void cloneFrom(final BasicOperator op) {
		super.cloneFrom(op);
		this.sswd = new DBMergeSortedBag<Bindings>(HEAPHEIGHT, ((Sort) op)
				.getComparator(), Bindings.class);
	}

	@Override
	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<Bindings> itb = sswd.iterator();
		return new ParallelIterator<Bindings>() {

			public void close() {
				((DBMergeSortedBag) sswd).release();
				if (itb instanceof ParallelIterator)
					((ParallelIterator) itb).close();
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

			@Override
			public void finalize() {
				close();
			}
		};
	}

	@Override
	public void finalize() {
		((DBMergeSortedBag) sswd).release();
	}
}
