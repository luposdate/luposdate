package lupos.datastructures.lsmtree.sip;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * Merges the results of several sideways information passing (SIP) iterators and provides a SIP iterator based on the merged result.
 * Iterators of younger runs must be given first in the constructor before older ones:
 * If keys are the same, only the values of younger runs are returned back...
 *
 * TODO: flatten a hierarchy of MergeSIPIterators into one and check if heaps for merging is more efficient (only in the case that there are many iterators to merge, i.e. flattening is a precondition for this way)
 *
 * @param <K>
 * @param <V>
 */
public class MergeSIPIterator<K, V> implements ISIPIterator<K, V> {

	/**
	 * The iterators which are going to be merged...
	 */
	protected final ISIPIterator<K, V>[] iterators;

	/**
	 * To store current entries if they need to be saved for the next call.
	 * However, we apply a method which is as lazy as possible, i.e., if entries[i] is null it does not necessarily mean that the i-th iterator does not have any entries any more...
	 */
	protected final Entry<K, V>[] entries;

	/**
	 * The comparator to compare the keys...
	 */
	protected final Comparator<? super K> comparator;

	/**
	 * if one time has been detected that the iterator has been finished, then remember this in order to increase performance...
	 */
	protected boolean finished = false;

	/**
	 * Constructor
	 *
	 * @param comparator the comparator to be used for comparing the keys
	 * @param iterators the SIP iterators to be merged (also null values are allowed)
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public MergeSIPIterator(final Comparator<K> comparator, final ISIPIterator<K, V>... iterators) {
		this.comparator = comparator;
		// first count non-null iterators
		int count=0;
		for(final ISIPIterator<K, V> iterator: iterators){
			if(iterator!=null){
				count++;
			}
		}
		if(count!=iterators.length){
			// there are null values in the iterators-array: do not copy them into the this.iterators-array!
			this.iterators = new ISIPIterator[count];
			int i = 0;
			for(final ISIPIterator<K, V> iterator: iterators){
				if(iterator!=null){
					this.iterators[i] = iterator;
					i++;
				}
			}
		} else {
			// there are only non-null iterators: just take the whole iterators-array over!
			this.iterators = iterators;
		}
		// the this.entries-array is automatically initialized with null-values!
		this.entries = new Entry[count];
	}

	@Override
	public boolean hasNext() {
		if(this.finished){
			return false;
		}
		final int end = this.entries.length;
		for(int i=0; i<end; i++){
			if(this.entries[i] != null){
				return true;
			} else {
				// try to get the next entry from the corresponding iterator
				this.entries[i] = this.iterators[i].next();
				if(this.entries[i] != null){
					return true;
				}
			}
		}
		this.finished = true;
		return false;
	}

	@Override
	public Entry<K, V> next() {
		if(this.finished){
			return null;
		}
		final int end = this.entries.length;
		int min = 0;
		// find the first entry, which is not null (just in order to avoid many comparisons later)
		while(min<end && this.entries[min]==null){
			final Entry<K, V> entry = this.iterators[min].next();
			if(entry!=null){
				this.entries[min] = entry;
				break;
			}
			min++;
		}
		// we do not have any entry any more
		if(min>=end){
			this.finished = true;
			return null;
		}
		K minKey = this.entries[min].getKey();
		for(int i=min+1; i<end; i++){
			Entry<K, V> entry = this.entries[i];
			if(entry==null){
				entry = this.iterators[i].next();
				this.entries[i] = entry;
			}
			if(entry!=null){
				final K currentKey = entry.getKey();
				final int compareResult = this.comparator.compare(currentKey, minKey);
				if(compareResult==0){
					// forget the entries having the same key in older runs
					this.entries[i] = null;
				} else if(compareResult<0){
					// we have a new minimum key!
					min = i;
					minKey = currentKey;
				}
			}
		}
		final Entry<K, V> result = this.entries[min];
		// forget the current minimum entry, such that it is not determined again in the next call
		this.entries[min] = null;
		return result;
	}

	@Override
	public Entry<K, V> next(final K k) {
		if(this.finished){
			return null;
		}
		final int end = this.entries.length;
		int min = 0;
		// find the first entry, which is not null and assert a value greater than k (just in order to avoid many comparisons later)
		while(min<end && (this.entries[min]==null || this.comparator.compare(this.entries[min].getKey(), k)<0)){
			final Entry<K, V> entry = this.iterators[min].next(k); // use SIP information for retrieving the next entry of the corresponding iterator!
			if(entry!=null){
				this.entries[min] = entry;
				break;
			}
			this.entries[min] = null;
			min++;
		}
		// we do not have any entry any more
		if(min>=end){
			this.finished = true;
			return null;
		}
		K minKey = this.entries[min].getKey();
		for(int i=min+1; i<end; i++){
			Entry<K, V> entry = this.entries[i];
			if(entry==null || this.comparator.compare(entry.getKey(), k)<0){
				entry = this.iterators[i].next(k); // use SIP information for retrieving the next entry of the corresponding iterator!
				this.entries[i] = entry;
			}
			if(entry!=null){
				final K currentKey = entry.getKey();
				final int compareResult = this.comparator.compare(currentKey, minKey);
				if(compareResult==0){
					// forget the entries having the same key in older runs
					this.entries[i] = null;
				} else if(compareResult<0){
					// we have a new minimum key!
					min = i;
					minKey = currentKey;
				}
			}
		}
		final Entry<K, V> result = this.entries[min];
		// forget the current minimum entry, such that it is not determined again in the next call
		this.entries[min] = null;
		return result;
	}
}
