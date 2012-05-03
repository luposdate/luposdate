package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Comparator;

public class StandardComparator<K> implements Comparator<K>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7910414506074836272L;

	public int compare(final K k1, final K k2) {
		return ((Comparable<K>) k1).compareTo(k2);
	}

	public StandardComparator() {
	}
}