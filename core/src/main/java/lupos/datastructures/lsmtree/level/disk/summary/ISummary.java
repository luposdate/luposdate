package lupos.datastructures.lsmtree.level.disk.summary;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import lupos.datastructures.lsmtree.debug.IKeyValuePrinter;
import lupos.misc.Triple;

/**
 *Interface for Summary specifying the basic methods for a concrete summary implementation
 *
 * @author Maike Herting
 *
 */
public interface ISummary<K> {

	/**
	* Returns the pagenumber for the specified key
	*
	* @param key the key that is searched for
	* @param summaryPage the page of the summary
	* @param summaryLevel the level of the summary
	* @return int the pagenumber where key is stored
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public int getPagenumber(K key, int summaryPage, int summaryLevel) throws ClassNotFoundException, IOException, URISyntaxException;

	/**
	 * Returns the pagenumber for the specified prefix key as well further information in order to enable faster processing of SIP information
	 *
	 * @param prefixComparator the comparator used for the prefix search
 	 * @param prefixkey the prefix key that is searched for
	 * @param summaryPage the page of the summary
	 * @param summaryLevel the level of the summary
	 * @return the determined pagenumber, the current entry of the summary iterator and the summary iterator itself (null if the iterator does not have any entry any more), or null if the searched prefix key is not contained (should not happen...)
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public Triple<Integer, Entry<K, Integer>, Iterator<Entry<K, Integer>>> prefixSearch(Comparator<K> prefixComparator, K prefixkey, int summaryPage, int summaryLevel) throws ClassNotFoundException, IOException, URISyntaxException;


	/**
	* Adds a new entry as a key-pagenumber-pair to the summary
	*
	* @param key the key
	* @param pagenumber pagenumber where key is stored
	* @throws java.io.IOException if any.
	*/
	public void addEntry(K key, int pagenumber) throws IOException;

	/**
	* Releases the level
	*
	*/
	public void release();

	/**
	* Returns the filename of the summary as a String
	*
	* @return String the filename of summary
	*/
	public String getFilename();

	/**
	* Sets the last entry if last is true
	*
	* @param last true if last entry of summary should be set
	* @throws java.io.IOException if any.
	*/
	public void lastEntry(boolean last) throws IOException;

	/**
	 * for debug purposes only: prints the content of the summary...
	 *
	 * @param printer the printer to print the key in a user-defined way...
	 */
	public<V> void printSummary(final IKeyValuePrinter<K, V> printer);

	/**
	 * Create the summary of this summary and returns it back. It is only used during loading the LSM tree.
	 *
	 * @return the newly created summary of this summary
	 * @throws IOException
	 */
	public ISummary<K> createSummaryOfTheSummary() throws IOException;

	/**
	 * @return the number of bytes used for this summary on disk (with wasted bytes to complete pages)
	 */
	public long numberOfBytesOnDisk();

	/**
	 * @return the number of bytes used for this summary on disk (without wasted bytes to complete pages)
	 */
	public long numberOfUsedBytesOnDisk() throws IOException;
}