package lupos.datastructures.lsmtree.level.memory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.lsmtree.level.Container;

public interface IMemoryLevelIterator<K,V> extends IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>> {
	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public default boolean receiveRunFromLowerLevel(final Iterator<Map.Entry<K,Container<V>>> run) throws ClassNotFoundException, IOException, URISyntaxException{
		while (run.hasNext()){
			final Map.Entry<K,Container<V>> entry = run.next();
			this.put(entry.getKey(), entry.getValue());
		}
		return true;
	}
}
