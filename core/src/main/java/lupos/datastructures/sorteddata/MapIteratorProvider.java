package lupos.datastructures.sorteddata;

import lupos.datastructures.queryresult.SIPParallelIterator;

public interface MapIteratorProvider<K, V> {
	public SIPParallelIterator<java.util.Map.Entry<K, V>, K> iterator();
}
