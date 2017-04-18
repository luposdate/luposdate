package lupos.datastructures.lsmtree.level.memory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;

/**
 * A memory level implementation that stores key-value-pairs in a tree map
 * and transfers its entries if tree map is full to the next level of the LSM-Tree
 *
 * @see TreeMap<K,V>
 * @author Maike Herting
 *
 */
public class MemoryLevelTreeMap<K,V> extends MemoryLevelIterator<K, V> implements IMemoryLevelIterator<K ,V>,IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>> {

	/**
	 * A tree map is used to store key-value pairs
	 */
	protected final TreeMap<K,Container<V>> tm;

	/**
	 * Constructor sets parameters and creates a new tree map
	 *
	 * @param levelFactory a level factory which creates level
	 * @param level number of the level
	 * @param THRESHOLD the maximum number of entries to be stored by tree map
	 * @param comp a comparator that is used to compare keys
	 */
	public MemoryLevelTreeMap(final ILevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> levelFactory, final int level, final int THRESHOLD, final Comparator<K> comp){
		super(levelFactory, level, THRESHOLD);
		this.tm = new TreeMap<K,Container<V>>(comp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final boolean putIntoMemory(final K key, final Container<V> value) {
		this.tm.put(key,value);
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * if key can't be found in the tree map, the next level is searched for it on condition that it exists
	 */
	@Override
	public Container<V> get(final K key) throws ClassNotFoundException, IOException, URISyntaxException{
		final Container<V> result = this.tm.get(key);
		if (result==null){
			if(this.nextLevel!=null){
				return this.nextLevel.get(key);
			}
			return null;
		} else{
			return result;
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public Iterator<Map.Entry<K,Container<V>>> rollOut(){
		return this.tm.entrySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void release() {
		this.tm.clear();
	}

	@Override
	public int size() {
		return this.tm.size();
	}
}
