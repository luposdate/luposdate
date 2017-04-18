package lupos.datastructures.lsmtree.level.memory;
import java.io.IOException;
import java.net.URISyntaxException;

import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.ILevel;

/**
 *Interface specifying the basic methods for a concrete memory level implementation
 *
 * @author Maike Herting
 *
 */

public interface IMemoryLevel<K,V,R> extends ILevel<K,V,R> {

	/**
	* Inserts the key and its value in the memory level
	*
	* @param K the key
	* @param Container<V> container that store value and removed marker
	* @return boolean if insert was successful
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public boolean put(K key, Container<V> container) throws ClassNotFoundException, IOException, URISyntaxException;
}
