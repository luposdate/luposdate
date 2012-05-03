package lupos.engine.operators.singleinput.modifiers.distinct;

import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;

public class InMemoryDistinct extends Distinct {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5670129779878953225L;

	final HashSet<Bindings> bindings = new HashSet<Bindings>();

	@Override
	public QueryResult process(final QueryResult _bindings, final int operandID) {
		final Iterator<Bindings> itb = _bindings.oneTimeIterator();
		if (!itb.hasNext())
			return null;
		else
			return QueryResult.createInstance(new Iterator<Bindings>() {
				Bindings next = null;

				public boolean hasNext() {
					if (next != null)
						return true;
					if (itb.hasNext()) {
						next = next();
						if (next != null)
							return true;
					}
					return false;
				}

				public Bindings next() {
					if (next != null) {
						final Bindings znext = next;
						next = null;
						return znext;
					}
					while (itb.hasNext()) {
						final Bindings b = itb.next();
						if (!bindings.contains(b)) {
							bindings.add(b);
							return b;
						}
					}
					return null;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			});
	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		// problem: it does not count the number of occurences of a binding
		// i.e. { ?a=<a> }, { ?a=<a> } and delete { ?a=<a> } will result in
		// {} instead of { ?a=<a> }!!!!!!
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext())
			bindings.remove(itb.next());
		return null;
	}

	@Override
	public void deleteAll(final int operandID) {
		bindings.clear();
	}

	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}
}