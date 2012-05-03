package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.queryresult.ParallelIterator;

public class DBSetBlockingDistinct extends BlockingDistinct {

	private static int HEAPHEIGHT = 5;

	public static int getHEAPHEIGHT() {
		return HEAPHEIGHT;
	}

	public static void setHEAPHEIGHT(final int heapheight) {
		HEAPHEIGHT = heapheight;
	}

	public DBSetBlockingDistinct() {
		super(new DBMergeSortedSet<Bindings>(HEAPHEIGHT, Bindings.class));
	}

	@Override
	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<Bindings> itb = this.bindings.iterator();
		return new ParallelIterator<Bindings>() {

			public void close() {
				((DBMergeSortedSet) bindings).release();
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
}