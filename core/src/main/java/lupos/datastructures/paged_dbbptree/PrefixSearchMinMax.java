package lupos.datastructures.paged_dbbptree;

import java.util.Iterator;

import lupos.datastructures.sorteddata.PrefixSearch;

public interface PrefixSearchMinMax<K, V> extends PrefixSearch<K, V> {
	public Iterator<V> prefixSearch(final K arg0, final K min);

	public Iterator<V> prefixSearch(final K arg0, final K min, final K max);

	public Iterator<V> prefixSearchMax(final K arg0, final K max);
}
