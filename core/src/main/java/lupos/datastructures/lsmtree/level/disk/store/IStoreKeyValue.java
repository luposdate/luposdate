package lupos.datastructures.lsmtree.level.disk.store;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map.Entry;

import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.IBloomFilter;
import lupos.misc.Triple;
import lupos.misc.Tuple;

/**
 *Interface specifying the basic methods for a concrete storekeyvalue implementation
 *
 * @author Maike Herting
 *
 */
public interface IStoreKeyValue<K,V> {

	/**
	* Stores entries (key-value-pairs) of a disk run delivered by iterator and returns null if all entries could be stored
	* or last entry that couldn't be stored anymore including the current offset to indicate the last address on page
	*
	* @param entry first entry to be stored
	* @param it iterator with the all entries to be stored
	* @param maxNumberOfEntriesToStore the maximum number of entries to be stored
	* @param page page where entries should be stored
	* @param offset address on page where entries should be stored
	* @return Tuple<Integer,Entry<K, Container<V>>> last offset on page and entry that doesn't fit to page anymore or null if all elements of it were stored
	* @throws java.io.IOException if any.
	*/
	public Tuple<Integer,Entry<K, Container<V>>> store(Entry<K, Container<V>> entry, Iterator<Entry<K, Container<V>>> it, int maxNumberOfEntriesToStore, byte[] page, int offset) throws IOException;

	/**
	* Returns an iterator with entries (key-value-pairs) that are stored consecutively on a page of a disk run, not necessarily all entries
	* size of iterator is the maximum number of entries that are stored compressed
	*
	* @param maxNumberOfCompressedEntriese the maximum number of entries that are stored compressed
	* @param page page where entries are stored
	* @param offset address on page where entries should be read from
	* @param maxBytesOnPage last address where an entry is stored
	* @return IKeyValueIterator<Integer,Entry<K, Container<V>>> iterator with entries and their offsets on page
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public IKeyValueIterator<Integer, Entry<K, Container<V>>> getNextEntries(int maxNumberOfCompressedEntries, byte[] page, int offset, int maxBytesOnPage) throws ClassNotFoundException, IOException, URISyntaxException ;

	/**
	* Stores entries (key-pagenumber-pairs) of a summary delivered by iterator and returns null if all entries could be stored
	* or last entry that couldn't be stored anymore including the current offset to indicate the last address on page
	*
	* @param entry iterator with the all entries to be stored
	* @param previouslyStoredKey the previously stored key (is null in the case that no key is previously stored on this page)
	* @param storageInfo additional information to be given for the next call (used e.g. by StoreIntTriples)
	* @param maxNumberOfEntriesToStore the maximum number of entries to be stored
	* @param page page where entries should be stored
	* @param offset address on page where entries should be stored
	* @return riple<Integer, Entry<K, Integer>, Object> last offset on page and entry that doesn't fit to page anymore or null if all elements of it were stored, the third component is the storageInfo (used e.g. by StoreIntTriples)
	* @throws java.io.IOException if any.
	*/
	public Triple<Integer, Entry<K, Integer>, Object> storeSummary(Entry<K, Integer> entry, K previouslyStoredKey, Object storageInfo, int maxNumberOfEntriesToStore, byte[] page, int offset) throws IOException;

	/**
	* Returns an iterator with entries (key-pagenumber-pairs) that are stored consecutively on a page of a summary, not necessarily all entries
	* size of iterator is the maximum number of entries that are stored compressed
	*
	* @param maxNumberOfCompressedEntriese the maximum number of entries that are stored compressed
	* @param page page where entries are stored
	* @param offset address on page where entries should be read from
	* @param maxBytesOnPage last address where an entry is stored
	* @return IKeyValueIterator<Integer, Entry<K, Integer>> iterator with entries and their offsets on page
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public IKeyValueIterator<Integer, Entry<K, Integer>> getNextSumEntries(int maxNumberOfEntriesToStore, byte[] page, int offset, int maxBytesOnPage) throws ClassNotFoundException, IOException, URISyntaxException;

	/**
	* Creates the Bloomfilter that is used for the keys
	*
	* @param maxRunLength the maximum number of entries in the run in order to size the bloom filter accordingly
	* @return IBloomFilter<K> a bloomfilter
	*/
	public IBloomFilter<K> createBloomFilter(final long maxRunLength);

	/**
	* Returns a the bloom filter iterator with entries of a disk run
	*
	* @param it iterator of entries of a disk run
	* @param iBloomFilter bloom filter that is used
	* @return Iterator<Entry<K, Container<V>>> a bloom filter iterator
	*/
	public Iterator<Entry<K, Container<V>>> getBloomFilterIterator(final Iterator<Entry<K, Container<V>>> it, IBloomFilter<K> iBloomFilter);

	/**
	 * for writing out all information of this IStoreKeyValue object such that it can be loaded after program exit
	 *
	 * @param loos the output stream to which this IStoreKeyValue object is written...
	 */
	public void writeLuposObject(OutputStream loos) throws IOException;
}
