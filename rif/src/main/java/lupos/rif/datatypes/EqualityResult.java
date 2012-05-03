package lupos.rif.datatypes;

import java.util.Collection;
import java.util.HashSet;

import lupos.datastructures.queryresult.QueryResult;
import lupos.rif.model.Equality;

public class EqualityResult extends QueryResult {
	final Collection<Equality> equalitySet = new HashSet<Equality>();

	public Collection<Equality> getEqualityResult() {
		return equalitySet;
	}

	@Override
	public int size() {
		return equalitySet.size();
	}

	@Override
	public boolean isEmpty() {
		return equalitySet.isEmpty();
	}

	@Override
	public String toString() {
		return equalitySet.toString();
	}

}
