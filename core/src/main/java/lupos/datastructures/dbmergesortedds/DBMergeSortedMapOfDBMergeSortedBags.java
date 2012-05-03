package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Comparator;

public class DBMergeSortedMapOfDBMergeSortedBags<K extends Serializable,V extends Serializable> extends DBMergeSortedMapOfCollections<K,V, DBMergeSortedBag<V>> {
	private final int heapHeightForBag;
	
	/**
	 * Create a new DBMergeSortedMapOfDBMergeSortedBags that sorts according to the elements' natural order. Both the map's and each bag's heap will have the same height. 
	 * @param heapHeight The height of the heap used to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final int heapHeight, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, heapHeight,classOfElements);
		heapHeightForBag = heapHeight;
	}
	
	/**
	 * Create a new DBMergeSortedMap that sorts using the specified Comparator. Both the map's and each bag's heap will have the same height.
	 * @param heapHeight The height of the heap used to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 * @param comp The Comparator to use for sorting.
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final int heapHeight, final Comparator<? super K> comp, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, heapHeight, comp, classOfElements);
		heapHeightForBag = heapHeight;
	}
	
	/**
	 * Create a new DBMergeSortedMapOfCollections that sorts according to the elements' natural order.
	 * @param heapHeightForMap The height of the heap used by the map to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 * @param heapHeightForBag The height of the heap used by each bag.
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final int heapHeightForMap, final int heapHeightForBag, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, heapHeightForMap,classOfElements);
		this.heapHeightForBag = heapHeightForBag;
	}
	
	/**
	 * Create a new DBMergeSortedMap that sorts using the specified Comparator.
	 * @param heapHeightForMap The height of the heap used by the map to presort the elements in memory. (The maximum number of elements that are held in memory at any given time will be 2**heapHeight-1)
	 * @param heap The height of the heap used by each bag.
	 * @param comp The Comparator to use for sorting.
	 */
	public DBMergeSortedMapOfDBMergeSortedBags(final int heapHeightForMap, final int heapHeightForBag, final Comparator<? super K> comp, final Class<? extends MapEntry<K,V>> classOfElements) {
		super(null, heapHeightForMap, comp,classOfElements);
		this.heapHeightForBag = heapHeightForBag;
	}
	
	protected DBMergeSortedBag<V> createCollection(final Class<? extends V> classOfElements) {
		return new DBMergeSortedBag<V>(heapHeightForBag,classOfElements);
	}
}
