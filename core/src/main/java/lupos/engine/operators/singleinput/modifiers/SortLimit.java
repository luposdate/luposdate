package lupos.engine.operators.singleinput.modifiers;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings;

public class SortLimit extends SingleInputOperator {

	private ComparatorBindings comparator;
	private Bindings[] smallestBindings;
	private Bindings max = null;
	private int posMax = -1;
	private int pos = 0;

	public SortLimit(final ComparatorBindings comparator, final int limit) {
		setComparator(comparator);
		setLimit(limit);
	}

	public SortLimit() {
		comparator=null;
		smallestBindings=null;
	}

	
	public void setComparator(final ComparatorBindings comp){
		this.comparator=comp;
	}
	
	public void setLimit(final int limit){
		smallestBindings = new Bindings[limit];
	}

	public QueryResult process(final QueryResult bindings, final int operandID) {
		final Iterator<Bindings> itb = bindings.oneTimeIterator();
		while (pos < smallestBindings.length && itb.hasNext()) {
			final Bindings b = itb.next();
			if (max == null || comparator.compare(b, max) > 0) {
				posMax = pos;
				max = b;
			}
			smallestBindings[pos++] = b;
		}
		if (itb.hasNext()) {
			while (itb.hasNext()) {
				final Bindings b = itb.next();
				if (comparator.compare(b, max) < 0) {
					smallestBindings[posMax] = b;
					max = b;
					// find new maximum
					for (int i = 0; i < smallestBindings.length; i++) {
						if (comparator.compare(smallestBindings[i], max) > 0) {
							max = smallestBindings[i];
							posMax = i;
						}
					}
				}
			}
		}
		if (itb instanceof ParallelIterator)
			((ParallelIterator) itb).close();
		return QueryResult.createInstance(new Iterator<Bindings>() {
			int i = 0;

			public boolean hasNext() {
				return i < pos;
			}

			public Bindings next() {
				if (hasNext())
					return smallestBindings[i++];
				else
					return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		});
	}

	public String toString() {
		return super.toString()+" " + smallestBindings.length;
	}
}
