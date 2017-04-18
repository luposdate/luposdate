package lupos.datastructures.lsmtree.level.memory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.ILevel;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.io.helper.OutHelper;

/**
 * An abstract super class of a memory level implementation for storing key-value-pairs
 * and transferring its entries if the memory is full to the next level of the LSM-Tree
 *
 * @author Maike Herting
 *
 */
public abstract class MemoryLevel<K,V,R> implements IMemoryLevel<K,V,R> {

	/**
	 * The next level is initially set to null and only created if needed
	 */
	protected ILevel<K,V,R> nextLevel = null;

	/**
	 * The level factory that creates the next level
	 */
	protected final ILevelFactory<K,V,R> levelFactory;

	/**
	 * The levelnumber of the memory level
	 */
	protected final int level;

	/**
	 * Threshold for the number of entries that can be stored by this level
	 */
	protected final int THRESHOLD;

	/**
	 * Constructor sets parameters
	 *
	 * @param levelFactory a level factory which creates level
	 * @param level number of the level
	 * @param THRESHOLD the maximum number of entries to be stored in the memory
	 */
	public MemoryLevel(final ILevelFactory<K,V,R> levelFactory, final int level, final int THRESHOLD){
		this.THRESHOLD = THRESHOLD;
		this.level = level;
		this.levelFactory = levelFactory;
	}

	/**
	 * Returns the number of entries in this level
	 *
	 * @return the number of entries in this level
	 */
	public abstract int size();

	/**
	 * Puts a key-value pair into the internal data structure
	 *
	 * @param key the key to be inserted
	 * @param value the value to be inserted
	 * @return true in case of success, otherwise false
	 */
	protected abstract boolean putIntoMemory(K key, Container<V> value);

	/**
	 * {@inheritDoc}
	 *
	 * If entry doesn't fit any more into the memory, the next level is created if it doesn't exit yet and entries are rolled out to it
	 * the internal data structure is cleared and entry inserted
	 */
	@Override
	public boolean put(final K key, final Container<V> value) throws ClassNotFoundException, IOException, URISyntaxException{
		try {
			if (this.size()==this.THRESHOLD){
				if(this.nextLevel==null){
					this.nextLevel=this.levelFactory.createLevel(this.level+1);
				}
				this.nextLevel.receiveRunFromLowerLevel(this.rollOut());
				this.release();
			}
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return this.putIntoMemory(key, value);
	}

	/**
	 * {@inheritDoc}
	 *
	 * this implementation doesn't support this method, Exception will be thrown
	 */
	@Override
	public K getFirstSummaryEntry() throws ClassNotFoundException, URISyntaxException {
		 throw new UnsupportedOperationException();
	}



	@Override
	public void clear() {
		this.release();
		if(this.nextLevel!=null){
			this.nextLevel.clear();
		}
	}

	@Override
	public void addRun(final Generator<K, V> generator) throws ClassNotFoundException, IOException, URISyntaxException {
		// do not add the run to a memory level, but instead to a disk-based level
		if(this.nextLevel==null){
			this.nextLevel=this.levelFactory.createLevel(this.level+1);
		}
		this.nextLevel.addRun(generator);
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException, ClassNotFoundException, URISyntaxException {
		if(this.size()==0){
			// in order to avoid error for empty memory level!
			OutHelper.writeLuposByte((byte)0, loos);
		} else {
			OutHelper.writeLuposByte((byte)1, loos);
			final ILevel<K, V, R> run = this.levelFactory.createRun(this.level, 0);
			run.receiveRunFromLowerLevel(this.rollOut());
			run.writeLuposObject(loos);
		}
		if(this.nextLevel!=null){
			OutHelper.writeLuposByte((byte)1, loos);
			this.nextLevel.writeLuposObject(loos);
		} else {
			OutHelper.writeLuposByte((byte)0, loos);
		}
	}

	@Override
	public long numberOfUsedBytesOnDisk() throws IOException {
		if(this.nextLevel!=null){
			return this.nextLevel.numberOfUsedBytesOnDisk();
		} else {
			return 0;
		}
	}

	@Override
	public long numberOfBytesOnDisk() {
		if(this.nextLevel!=null){
			return this.nextLevel.numberOfBytesOnDisk();
		} else {
			return 0;
		}
	}

	@Override
	public String getStructureInfo() throws IOException {
		String result = "Level " + this.level + " #elements="+this.size()+"/"+this.THRESHOLD;
		if(this.nextLevel!=null){
			result+=", "+this.nextLevel.getStructureInfo();
		}
		return result;
	}
}
