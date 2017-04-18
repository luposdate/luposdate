package lupos.datastructures.lsmtree.level.disk.bloomfilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface specifying the basic methods for a concrete bloom filter implementation
 *
 * @author Maike Herting
 *
 */

public interface IBloomFilter<K> {

	/**
	 * Sets the key
	 *
	 *@param k key to set
	 *
	 */
	public void set(K k);

	/**
	 * Returns whether key is set
	 *
	 * @param k key
	 * @return true if key is set
	 */
	public boolean get(K k);

	/**
	 * Returns whether or not the bloom filter is true for the given prefix-key
	 *
	 * @param prefixkey the given prefix key
	 * @return true if the bloom filter is true for the given prefix-key, otherwise false
	 */
	public boolean getPrefix(K prefixKey);

	/**
	 * Write this bloom filter to disk
	 *
	 * @param loos the output stream to which this bloom filter is written
	 * @throws IOException
	 */
	public void writeLuposObject(final OutputStream loos) throws IOException;

	/**
	 * Read this bloom filter from disk
	 *
	 * @param lois the input stream from which this bloom filter is read
	 * @throws IOException
	 */
	public void readLuposObject(final InputStream lois) throws IOException;
}
