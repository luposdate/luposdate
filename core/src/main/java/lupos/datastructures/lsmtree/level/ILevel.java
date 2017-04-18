package lupos.datastructures.lsmtree.level;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Comparator;

import lupos.datastructures.lsmtree.debug.IKeyValuePrinter;
import lupos.datastructures.lsmtree.sip.ISIPIterator;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;

/**
 * Interface specifying the basic methods for a concrete level implementation
 *
 * @author Maike Herting
 *
 */
public interface ILevel<K,V, R> {

	/**
	* Returns the value container that belongs to the key, if it can't be found null is returned
	*
	* @param key the key that is looked for
	* @return Container<V> including the value and removed marker or null if it can't be found
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public Container<V> get(K key) throws ClassNotFoundException, IOException, URISyntaxException;

	/**
	 * Processes a prefix search and returns all the results matching a given prefix key in form of a sideways information passing iterator
	 *
	 * @param prefixComparator the comparator used for the prefix search
	 * @param prefixkey the given prefix key
	 * @return a sideways information passing iterator, which returns the key-value-pairs matching the given prefix key
	 */
	public ISIPIterator<K, Container<V>> prefixSearch(Comparator<K> prefixComparator, final K prefixkey);

	/**
	* Returns the sorted entries of a level as an iterator (or the run itself)
	*
	* @return iterator with entries (or run itself) of key-value-pairs
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public R rollOut() throws IOException, ClassNotFoundException, URISyntaxException;

	/**
	* Receives a run from the lower level and stores it
	*
	* @param run iterator of run (or run itself) that is rolled out from lower level
	* @return boolean to state whether receiving was successful
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public boolean receiveRunFromLowerLevel(R run) throws ClassNotFoundException, IOException, URISyntaxException;

	/**
	* Releases the level
	*/
	public void release();

	/**
	* Clears this level and its higher levels
	*/
	public void clear();

	/**
	* Returns the first entry of a summary
	*
	* @return K key which is the first entry of summary
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public K getFirstSummaryEntry() throws ClassNotFoundException, URISyntaxException;

	/**
	 * Adds a run to this or one of its higher levels (if the run's size fits better to higher levels)
	 *
	 * @param generator the run to add as generator
	 */
	public void addRun(final Generator<K, V> generator) throws ClassNotFoundException, IOException, URISyntaxException;

	/**
	* only used for debugging
	* displays the entries in a level
	*
	* @param printer to be used for printing the keys and values
	*/
	public void printLevels(final IKeyValuePrinter<K, V> printer);

	/**
	 * For storing this and its succeeding levels such that the LSM tree can be loaded again after program exit
	 *
	 * @param loos the output stream into which the LSM tree is saved
	 * @throws IOException
	 */
	public void writeLuposObject(final OutputStream loos) throws IOException, ClassNotFoundException, URISyntaxException;

	/**
	 * For reading in a previously stored level (and its succeeding levels) as part of loading the whole LSM tree
	 *
	 * @param lois the input stream from which the levels are loaded
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws URISyntaxException
	 */
	public void readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException;

	/**
	 * @return the number of bytes, which are used on disk (without wasted bytes to complete pages)
	 */
	public long numberOfUsedBytesOnDisk() throws IOException;

	/**
	 * @return the number of bytes, which are used on disk (with wasted bytes to complete pages)
	 */
	public long numberOfBytesOnDisk();

	/**
	 * @return information about structure (number of entries/runs) in this and succeeding levels as string
	 */
	public String getStructureInfo() throws IOException;
}
