package lupos.datastructures.queryresult;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.engine.operators.index.adaptedRDF3X.MergeIndicesTripleIterator;
import lupos.engine.operators.tripleoperator.TriplePattern;

public class IdIteratorQueryResult extends IteratorQueryResult {

	private final MergeIndicesTripleIterator itt;
	private int idOfLastElement;

	public IdIteratorQueryResult(final MergeIndicesTripleIterator itt,
			final TriplePattern tp) {
		super(null);
		this.itt = itt;
		this.itb = new Iterator<Bindings>() {
			Bindings next = computeNext();
			int idOfLastElementIterator;

			public boolean hasNext() {
				return (next != null);
			}

			public Bindings next() {
				if (next == null)
					return null;
				final Bindings znext = next;
				idOfLastElement = idOfLastElementIterator;
				next = computeNext();
				return znext;
			}

			private Bindings computeNext() {
				while (itt.hasNext()) {
					// also consider inner joins in triple patterns like ?a ?a
					// ?b.
					final Bindings znext = tp.process(itt.next(), false, itt
							.getIdOfLastElement());
					if (znext != null) {
						idOfLastElementIterator = itt.getIdOfLastElement();
						return znext;
					}
				}
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	// return the id of the indices used in the case e.g. that there are
	// several default graphs...
	public int getIDOfLastBinding() {
		return idOfLastElement;
	}

	public int getMaxId() {
		return itt.getMaxId();
	}
}
