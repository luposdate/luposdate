package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;

public class SortedDataDistinct extends Distinct {

	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {

		return QueryResult.createInstance(new Iterator<Bindings>() {
			Iterator<Bindings> itb = queryResult.oneTimeIterator();
			Bindings next = null;
			Bindings previous = null;

			public boolean hasNext() {
				if (next != null)
					return true;
				next = next();
				return (next != null);
			}

			public Bindings next() {
				if (next != null) {
					final Bindings znext = next;
					next = null;
					return znext;
				}
				Bindings b = itb.next();
				if (previous == null) {
					previous = b;
					return b;
				}
				while (b != null && b.equals(previous))
					b = itb.next();
				previous = b;
				return b;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		});
	}

}
