package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapOfCollectionsImplementation<K, V, CV extends Collection<V>> implements MapOfCollections<K, V, CV> {

	protected Map<K, CV> map;
	protected Class<? extends CV> klass;
	
	public MapOfCollectionsImplementation(final Map<K, CV> map, final Class<? extends CV> klass){
		this.map=map;
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
		final CV cv=map.get(key);
		if(cv!=null) return cv.remove(value);
		else return false;
	}

	
	public void putToCollection(final K key, final V value) throws InstantiationException,
			IllegalAccessException {
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

	
	public Iterator<V> valuesInCollectionsIterator() {
		try {
			final CV cvAll=getNewCollection();
			for(final K key: keySet()){
				final CV cv=get(key);
				if(cv!=null) cvAll.addAll(cv);
			}
			return cvAll.iterator();
		} catch(final InstantiationException e)
		{}
		catch (final IllegalAccessException e)
		{}
		return null;
	}

	
	public void clear() {
		map.clear();
	}

	
	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	
	public boolean containsValue(final Object value) {
		return map.containsValue(value);
	}

	
	public Set<java.util.Map.Entry<K, CV>> entrySet() {
		return map.entrySet();
	}

	
	public CV get(final Object key) {
		return map.get(key);
	}

	
	public boolean isEmpty() {
		return map.isEmpty();
	}

	
	public Set<K> keySet() {
		return map.keySet();
	}

	
	public CV put(final K key, final CV value) {
		return map.put(key, value);
	}

	
	public void putAll(final Map<? extends K, ? extends CV> m) {
		map.putAll(m);
	}

	
	public CV remove(final Object key) {
		return map.remove(key);
	}
	

	
	public int size() {
		return map.size();
	}

	
	public Collection<CV> values() {
		return map.values();
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
