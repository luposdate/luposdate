package lupos.datastructures.lsmtree.level.factory;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;

import lupos.datastructures.lsmtree.level.ILevel;
import lupos.datastructures.lsmtree.level.memory.IMemoryLevel;

/**
* Interface specifying the basic methods for a concrete level factory implementation
*
* @author Maike Herting
*
*/
public interface ILevelFactory<K,V,R> {

	/**
	* creates the memory level
	*
	* @return IMemoryLevel<K, V> a memory level
	*/
	public IMemoryLevel<K,V,R> createLevel0();

	/**
	*  creates a level according to the levelnumber
	*
	* @param level the number of the level
	* @return ILevel<K,V>
	*/
	public ILevel<K,V,R> createLevel(int level);

	/**
	*  creates a run according to the levelnumber and the number
	*
	* @param level the number of the level
	* @param number the number of the run
	* @return ILevel<K,V>
	* @throws java.io.IOException if any.
	*/
	public ILevel<K,V,R> createRun(int level, int number) throws IOException;

	/**
	 * returns the comparator for comparing the keys...
	 *
	 * @return the comparator used for comparing the keys
	 */
	public Comparator<K> getComparator();

	/**
	 * The disk-based runs of each lsm-tree must be stored in a different folder in order to avoid interferences. Hence this method should return the path where the disk runs of the current lsm-tree are stored...
	 *
	 * @return the path where the disk runs are stored on disk
	 */
	public String getPathToDiskRuns();

	/**
	 * for writing all data to disk such that the LSM tree can be loaded again after program exit...
	 *
	 * @param loos the output stream to which the LSM tree is written
	 * @throws IOException
	 */
	public void writeLuposObject(final OutputStream loos) throws IOException;
}