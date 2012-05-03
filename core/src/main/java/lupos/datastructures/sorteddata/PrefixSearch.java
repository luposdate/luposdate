package lupos.datastructures.sorteddata;

import java.util.Iterator;
import java.util.SortedMap;

public interface PrefixSearch<K, V> extends SortedMap<K, V> {
	public Iterator<V> prefixSearch(final K arg0);

	public Object[] getClosestElements(final K arg0);
}
