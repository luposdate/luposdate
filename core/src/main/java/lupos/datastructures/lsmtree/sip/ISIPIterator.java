package lupos.datastructures.lsmtree.sip;

import java.util.Map.Entry;

/**
 * The interface for sideways information passing iterators...
 *
 * @param <K> the key class of the returned entries
 * @param <V> the value class of the returned entries
 */
public interface ISIPIterator<K, V> {

	/**
	 * Checks if the iterator has another entry
	 *
	 * @return true if the iterator contains another entry
	 */
	public boolean hasNext();

	/**
	 * Determines the next entry of the iterator
	 *
	 * @return the next entry of the iterator, or null if there is no next entry
	 */
	public Entry<K, V> next();

	/**
	 * Determines the next entry of the iterator, the key of which is equal to or greater than the given key
	 *
	 * @param k the next returned entry must contain a key, which is equal to or greater than k
	 * @return the next entry of the iterator, the key of which is equal to or greater than the given key, or null if there is no such entry
	 */
	public Entry<K, V> next(K k);
}
