package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class SortedMapOfCollectionsImplementation<K, V, CV extends Collection<V>> implements
		SortedMapOfCollections<K, V, CV> {

	protected SortedMap<K, CV> sortedMap;
	protected Class<? extends CV> klass;
	
	public SortedMapOfCollectionsImplementation(final SortedMap<K, CV> sortedMap, final Class<? extends CV> klass){
		this.sortedMap=sortedMap;
		this.klass=klass;
	}
	

	protected CV getNewCollection() throws InstantiationException, IllegalAccessException{
		return klass.newInstance();
	}
	
	
	public boolean containsValueInCollections(final Object arg0) {
		for(final K key: keySet()){
			final CV cv=get(key);
			if(cv.contains(arg0)) return true;
		}
		return false;
	}

	
	public void putAllIntoCollections(
			final Map<? extends K, ? extends CV> arg0) {
		for(final K key: arg0.keySet()){
			final CV cv=arg0.get(key);
			final CV cvInto=get(key);
			if(cvInto==null) put(key,cv);
			else cvInto.addAll(cv);
		}		
	}

	public boolean removeFromCollection(final K key, final V value){
		final CV cv=sortedMap.get(key);
		if(cv!=null) return cv.remove(value);
		else return false;
	}

	
	public void putToCollection(final K key, final V value) throws InstantiationException, IllegalAccessException {
		CV cv=get(key);
		if(cv==null)cv=getNewCollection();
		cv.add(value);
		put(key,cv);
	}

	
	public int sizeOfElementsInCollections() {
		int size=0;
		for(final K key: keySet()){
			final CV cv=get(key);
			if(cv!=null) size+=cv.size();
		}
		return size;
	}

	
	public Iterator<V> valuesInCollectionsIterator(){
		try {
			final CV cvAll=getNewCollection();
			for(final K key: keySet()){
				final CV cv=get(key);
				if(cv!=null) cvAll.addAll(cv);
			}
			return cvAll.iterator();
		} catch(final InstantiationException e)
		{}
		catch(final IllegalAccessException e)
		{}
		return null;
	}

	
	public Comparator<? super K> comparator() {
		return sortedMap.comparator();
	}

	
	public Set<java.util.Map.Entry<K, CV>> entrySet() {
		return sortedMap.entrySet();
	}

	
	public K firstKey() {
		return sortedMap.firstKey();
	}

	
	public SortedMap<K, CV> headMap(final K toKey) {
		return sortedMap.headMap(toKey);
	}

	
	public Set<K> keySet() {
		return sortedMap.keySet();
	}

	
	public K lastKey() {
		return sortedMap.lastKey();
	}

	
	public SortedMap<K, CV> subMap(final K fromKey, final K toKey) {
		return sortedMap.subMap(fromKey, toKey);
	}

	
	public SortedMap<K, CV> tailMap(final K fromKey) {
		return sortedMap.tailMap(fromKey);
	}

	
	public Collection<CV> values() {
		return sortedMap.values();
	}

	
	public void clear() {
		sortedMap.clear();
	}

	
	public boolean containsKey(final Object key) {
		return sortedMap.containsKey(key);
	}

	
	public boolean containsValue(final Object value) {
		return sortedMap.containsValue(value);
	}

	
	public CV get(final Object key) {
		return sortedMap.get(key);
	}

	
	public boolean isEmpty() {
		return sortedMap.isEmpty();
	}

	
	public CV put(final K key, final CV value) {
		return sortedMap.put(key, value);
	}

	
	public void putAll(final Map<? extends K, ? extends CV> m) {
		sortedMap.putAll(m);
	}

	
	public CV remove(final Object key) {
		return sortedMap.remove(key);
	}

	
	public int size() {
		return sortedMap.size();
	}

	public Collection<V> valuesInCollections() {
		try {
			final CV cvAll = getNewCollection();
		for(final K key: keySet()){
			final CV cv=get(key);
			if(cv!=null) cvAll.addAll(cv);
		}
		return cvAll;
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
