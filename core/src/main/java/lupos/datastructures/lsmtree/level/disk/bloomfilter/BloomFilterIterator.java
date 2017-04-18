package lupos.datastructures.lsmtree.level.disk.bloomfilter;
import java.util.Iterator;
import java.util.Map.Entry;
/**
* Iterator to pass Entries on to Run and set their keys in Bloomfilter
* 
* @author Maike Herting
*
*/

public class BloomFilterIterator<K,V> implements Iterator<Entry<K, V>> {

	/**
	* Iterator of Entries of key-value-pairs
	*/
	protected final Iterator<Entry<K, V>> it;
	
	/**
	* Bloomfilter that is used
	*/
	protected final IBloomFilter<K> bloomFilter;

	/**
	* Constructor specifying the iterator and bloomfilter
	*  
	* @param it an iterator of Entries of key-value-pairs
	* @param bloomFilter a Bloomfilter
	*/
	public BloomFilterIterator(final Iterator<Entry<K,V>> it, final IBloomFilter<K> bloomFilter){
		this.it = it;
		this.bloomFilter = bloomFilter;
	}

	/**
	* Checks if iterator has more Entries
	*  
	* @return true if iterator has a next entry
	*/
	@Override
	public boolean hasNext() {
		return this.it.hasNext();
	}

	/**
	* Returns the next element of the iterator and sets its key in Bloomfilter
	* 
	* @return Entry<K, V> an Entry-Object consisting of key and value
	*/
	@Override
	public Entry<K, V> next() {
		final Entry<K, V> entry = this.it.next();
		this.bloomFilter.set(entry.getKey());
		return entry;
	}
}
