package lupos.engine.operators.singleinput.readtriplesdistinct;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleComparator;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndex;

public class DBSetBlockingDistinct extends BlockingDistinct {

	public DBSetBlockingDistinct() throws RemoteException {
		super(new DBMergeSortedSet<BindingsArrayReadTriples>(5,
				new Comparator<BindingsArrayReadTriples>() {

					public int compare(final BindingsArrayReadTriples arg0,
							final BindingsArrayReadTriples arg1) {
						final TripleComparator tc = new TripleComparator(
								RDF3XIndex.CollationOrder.SPO);
						final Iterator<Triple> it0 = arg0.getTriples()
								.iterator();
						final Iterator<Triple> it1 = arg1.getTriples()
								.iterator();
						while (it0.hasNext()) {
							if (!it1.hasNext())
								return -1;
							final Triple t0 = it0.next();
							final Triple t1 = it1.next();
							final int compare = tc.compare(t0, t1);
							if (compare != 0)
								return compare;
						}
						if (it1.hasNext())
							return 1;
						return 0;
					}

				}, BindingsArrayReadTriples.class));
	}

	@Override
	protected ParallelIterator<Bindings> getIterator() {
		final Iterator<BindingsArrayReadTriples> itb = this.bindings.iterator();
		return new ParallelIterator<Bindings>() {

			public void close() {
				((DBMergeSortedSet) bindings).release();
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
	public String toString() {
		return super.toString()+" for read triples";
	}

}