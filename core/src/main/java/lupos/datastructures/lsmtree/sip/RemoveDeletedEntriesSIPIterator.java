package lupos.datastructures.lsmtree.sip;

import java.util.Comparator;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.queryresult.SIPParallelIterator;

/**
 * This iterator class takes an (inner) iterator over inserted and deleted entries and just returns the inserted entries.
 *
 * @param <K>
 * @param <V>
 */
public class RemoveDeletedEntriesSIPIterator<K, V> implements ISIPIterator<K, V>, SIPParallelIterator<java.util.Map.Entry<K, V>, K> {

	/**
	 * The comparator for comparing the keys...
	 */
	protected final Comparator<? super K> comparator;

	/**
	 * The inner iterator (having inserted and deleted entries) which is pipelined to an iterator just having the inserted entries
	 */
	protected final ISIPIterator<K, Container<V>> innerIterator;

	/**
	 * Just to intermediately store an entry if needed
	 */
	protected Entry<K, V> nextEntry = null;

	/**
	 * Constructor
	 *
	 * @param comparator The comparator for comparing the keys
	 * @param innerIterator The inner iterator (having inserted and deleted entries) which is pipelined to an iterator just having the inserted entries
	 */
	public RemoveDeletedEntriesSIPIterator(final Comparator<? super K> comparator, final ISIPIterator<K, Container<V>> innerIterator){
		this.comparator = comparator;
		this.innerIterator = innerIterator;
	}

	@Override
	public boolean hasNext() {
		if(this.nextEntry==null){
			this.nextEntry = this.next();
		}
		return (this.nextEntry!=null);
	}

	@Override
	public Entry<K, V> next() {
		if(this.nextEntry!=null){
			final Entry<K, V> result = this.nextEntry;
			this.nextEntry = null;
			return result;
		}
		Entry<K, Container<V>> result;
		do{
			result = this.innerIterator.next();
		} while(result!=null && result.getValue().removed);
		if(result==null){
			return null;
		}
		return new MapEntry<K, V>(result.getKey(), result.getValue().value);
	}

	@Override
	public Entry<K, V> next(final K k) {
		if(this.nextEntry!=null){
			final Entry<K, V> result = this.nextEntry;
			this.nextEntry = null;
			if(this.comparator.compare(result.getKey(), k)>=0){
				return result;
			}
		}
		Entry<K, Container<V>> result;
		do{
			result = this.innerIterator.next(k);
		} while(result!=null && result.getValue().removed);
		if(result==null){
			return null;
		}
		return new MapEntry<K, V>(result.getKey(), result.getValue().value);
	}

	@Override
	public void close() {
	}
}
