package lupos.datastructures.lsmtree.level.disk.store;

import lupos.misc.Tuple;

/**
 * Similar functionality as an java iterator. However, because of performance reasons a tuple object is used for setting the result in order to avoid the generation of these tuple objects for each call...
 *
 * @param <K1> first generic parameter
 * @param <K2> second generic parameter
 */
public interface IKeyValueIterator<K1, K2> {

	/**
	 * checks for existence of a next entry in the iterator
	 *
	 * @return true if the iterator has a next entry, otherwise false
	 */
	public boolean hasNext();

	/**
	 * Determines the next entry in the iterator...
	 *
	 * @param resultObject the object, in which the result will be stored (Be aware of side effects! The given resultObject will be modified during the method call!)
	 * @return the resultObject
	 */
	public Tuple<K1, K2> next(Tuple<K1, K2> resultObject);
}
