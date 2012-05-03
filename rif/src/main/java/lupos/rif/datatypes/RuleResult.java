package lupos.rif.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;

public class RuleResult extends QueryResult {
	protected final Collection<Predicate> predicateSet = new HashSet<Predicate>();

	public RuleResult() {
		super();
	}
	
	public Iterator<Predicate> getPredicateIterator(){
		return predicateSet.iterator();
	}

	public Collection<Predicate> getPredicateResults() {
		return predicateSet;
	}

	@Override
	public int size() {
		return predicateSet.size();
	}

	@Override
	public boolean isEmpty() {
		return predicateSet.isEmpty();
	}

	@Override
	public String toString() {
		return predicateSet.toString();
	}

}
