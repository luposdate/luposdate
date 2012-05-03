package lupos.rif.datatypes;

import java.util.Iterator;

public class IteratorRuleResult extends RuleResult {

	private final Iterator<Predicate> iterator;

	public IteratorRuleResult(final Iterator<Predicate> it) {
		super();
		iterator = it;
	}

	@Override
	public Iterator<Predicate> getPredicateIterator() {
		return iterator;
	}

	@Override
	public boolean isEmpty() {
		return iterator.hasNext();
	}

	@Override
	public int size() {
		return isEmpty() ? 0 : 1;
	}
}
