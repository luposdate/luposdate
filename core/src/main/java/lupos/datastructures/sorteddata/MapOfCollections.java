package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface MapOfCollections<K, V, CV extends Collection<V>> extends Map<K, CV>{
	
	public void putToCollection(K key, V value) throws InstantiationException, IllegalAccessException;
	public boolean removeFromCollection(K key, V value);
	public Iterator<V> valuesInCollectionsIterator();
	public Collection<V> valuesInCollections();
	public boolean containsValueInCollections(Object arg0);
	public void putAllIntoCollections(Map<? extends K, ? extends CV> arg0);
	public int sizeOfElementsInCollections();
}
