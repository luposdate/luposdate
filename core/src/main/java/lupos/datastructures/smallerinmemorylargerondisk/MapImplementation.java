package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;

public class MapImplementation<K extends Comparable<K> & Serializable, V extends Serializable> implements Map<K, V>{
	
	private final Map<K,V> memoryMap=new HashMap<K,V>();
	private Map<K,V> diskMap=null;
	
	protected final static int MAXMEMORYMAPENTRIES = 3000000;

	public void clear() {
		memoryMap.clear();
		if(diskMap!=null) diskMap.clear();
	}

	public boolean containsKey(final Object arg0) {
		if(memoryMap.containsKey(arg0)) return true;
		if(diskMap!=null) return diskMap.containsKey(arg0);
		return false;
	}

	public boolean containsValue(final Object arg0) {
		if(memoryMap.containsValue(arg0)) return true;
		if(diskMap!=null) return diskMap.containsValue(arg0);
		return false;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new Set<java.util.Map.Entry<K, V>>(){
			public boolean add(final java.util.Map.Entry<K, V> o) {
				return (MapImplementation.this.put(o.getKey(),o.getValue())!=o.getValue());
			}
			public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
				boolean flag=false;
				for(final java.util.Map.Entry<K, V> me:c){
					flag=flag || add(me);
				}
				return flag;
			}
			public void clear() {
				MapImplementation.this.clear();	
			}
			public boolean contains(final Object o) {
				if(o instanceof java.util.Map.Entry){
					try{
						final java.util.Map.Entry<K, V> me=(java.util.Map.Entry<K, V>) o;
						final V v=MapImplementation.this.get(me.getKey());
						if(v.equals(me.getValue())) return true;
						else return false;
					} catch(final ClassCastException ce){
						return false;
					}
				}
				return false;
			}
			public boolean containsAll(final Collection<?> c) {
				for(final Object o:c){
					if(!contains(o)) return false;
				}
				return true;
			}
			public boolean isEmpty() {
				return MapImplementation.this.isEmpty();	
			}
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new Iterator<java.util.Map.Entry<K, V>>(){			
					Iterator<java.util.Map.Entry<K, V>> memoryIterator=memoryMap.entrySet().iterator();
					Iterator<java.util.Map.Entry<K, V>> diskIterator= (diskMap==null)? null:diskMap.entrySet().iterator();
					public boolean hasNext() {
						if(memoryIterator.hasNext()) return true;
						if(diskIterator!=null) return diskIterator.hasNext();
						return false;
					}
					public java.util.Map.Entry<K, V> next() {
						if(memoryIterator.hasNext()) return memoryIterator.next();
						if(diskIterator!=null) return diskIterator.next();
						return null;
					}
					public void remove() {
						throw(new UnsupportedOperationException("This iterator does not support remove."));
					}			
				};			
			}
			public boolean remove(final Object o) {
				if(o instanceof java.util.Map.Entry){
					try{
						final java.util.Map.Entry<K, V> me=(java.util.Map.Entry<K, V>) o;
						final V v=MapImplementation.this.get(me.getKey());
						if(v.equals(me.getValue())) {
							MapImplementation.this.remove(me.getKey());
							return true;
						}
						else return false;
					} catch(final ClassCastException ce){
						return false;
					}
				}
				return false;
			}
			public boolean removeAll(final Collection<?> c) {
				boolean flag=false;
				for(final Object o:c){
					flag=flag || remove(o);
				}
				return flag;
			}
			public boolean retainAll(final Collection<?> c) {
				boolean flag=false;
				for(final java.util.Map.Entry<K, V> me:this){
					if(!c.contains(me)){
						flag=true;
						remove(me);
					}
				}
				return flag;
			}
			public int size() {
				return MapImplementation.this.size();
			}
			public Object[] toArray() {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
			public <T> T[] toArray(final T[] a) {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
		};
	}

	public V get(final Object arg0) {
		final V v=memoryMap.get(arg0);
		if(v!=null) return v;
		if(diskMap!=null) return diskMap.get(arg0); 
		return null;
	}

	public boolean isEmpty() {
		if(memoryMap.size()>0) return false;
		if(diskMap!=null && !diskMap.isEmpty()) return false;
		return true;
	}

	public Set<K> keySet() {
		return new Set<K>(){
			public boolean add(final K arg0) {
				throw(new UnsupportedOperationException("This set does not support add."));
			}
			public boolean addAll(final Collection<? extends K> arg0) {
				throw(new UnsupportedOperationException("This set does not support addAll."));
			}
			public void clear() {
				MapImplementation.this.clear();	
			}
			public boolean contains(final Object arg0) {
				return MapImplementation.this.containsKey(arg0);	
			}
			public boolean containsAll(final Collection<?> arg0) {
				for(final Object o:arg0){
					if(!contains(o)) return false;
				}
				return true;
			}
			public boolean isEmpty() {
				return MapImplementation.this.isEmpty();	
			}
			public Iterator<K> iterator() {
				return new Iterator<K>(){
					final Iterator<java.util.Map.Entry<K, V>> entryIt=MapImplementation.this.entrySet().iterator();
					public boolean hasNext() {
						return entryIt.hasNext();
					}
					public K next() {
						return entryIt.next().getKey();
					}
					public void remove() {
						entryIt.remove();
					}					
				};
			}
			public boolean remove(final Object arg0) {
				return (MapImplementation.this.remove(arg0)!=null);	
			}
			public boolean removeAll(final Collection<?> arg0) {
				boolean flag=false;
				for(final Object o:arg0){
					flag=flag || remove(o);
				}
				return flag;
			}
			public boolean retainAll(final Collection<?> arg0) {
				boolean flag=false;
				for(final K k:this){
					if(!arg0.contains(k)){
						flag=true;
						remove(k);
					}
				}
				return flag;
			}
			public int size() {
				return MapImplementation.this.size();
			}
			public Object[] toArray() {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
			public <T> T[] toArray(final T[] arg0) {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}			
		};
	}

	public V put(final K arg0, final V arg1) {
		if(memoryMap.size()<MAXMEMORYMAPENTRIES) return memoryMap.put(arg0,arg1);
		if(memoryMap.containsKey(arg0)) return memoryMap.put(arg0,arg1);
		if (diskMap == null){
			Entry<K, V> entry=memoryMap.entrySet().iterator().next();
			try {
				diskMap = new DBBPTree<K, V>(100000, 100000, new StandardNodeDeSerializer<K, V>((Class<? super K>)entry.getKey().getClass(),(Class<? super V>) entry.getValue().getClass()));
			} catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		return diskMap.put(arg0, arg1);
	}

	public void putAll(final java.util.Map<? extends K, ? extends V> arg0) {
		for(final java.util.Map.Entry<? extends K, ? extends V> me:arg0.entrySet()){
			put(me.getKey(),me.getValue());
		}
	}

	public V remove(final Object arg0) {
		V v=memoryMap.remove(arg0);
		if(v==null){
			if(diskMap!=null) v=diskMap.remove(arg0);
		}
		return v;
	}

	public int size() {
		int size=memoryMap.size();
		if(diskMap!=null) size+=diskMap.size();
		return size;
	}

	public Collection<V> values() {
		return new Collection<V>(){
			public boolean add(final V o) {
				throw(new UnsupportedOperationException("This collection does not support add."));
			}
			public boolean addAll(final Collection<? extends V> c) {
				throw(new UnsupportedOperationException("This collection does not support add."));
			}
			public void clear() {
				throw(new UnsupportedOperationException("This collection does not support clear."));
			}
			public boolean contains(final Object o) {
				return MapImplementation.this.containsValue(o);
			}
			public boolean containsAll(final Collection<?> c) {
				for(final Object o:c){
					if(!contains(o)) return false;
				}
				return true;
			}
			public boolean isEmpty() {
				return MapImplementation.this.isEmpty();
			}
			public Iterator<V> iterator() {
				return new Iterator<V>(){
					final Iterator<java.util.Map.Entry<K, V>> entryIt=MapImplementation.this.entrySet().iterator();
					public boolean hasNext() {
						return entryIt.hasNext();
					}
					public V next() {
						return entryIt.next().getValue();
					}
					public void remove() {
						entryIt.remove();
					}					
				};
			}
			public boolean remove(final Object o) {
				throw(new UnsupportedOperationException("This collection does not support remove."));
			}
			public boolean removeAll(final Collection<?> c) {
				throw(new UnsupportedOperationException("This collection does not support removeAll."));
			}
			public boolean retainAll(final Collection<?> c) {
				throw(new UnsupportedOperationException("This collection does not support retainAll."));
			}
			public int size() {
				return MapImplementation.this.size();
			}
			public Object[] toArray() {
				throw(new UnsupportedOperationException("This collection does not support toArray."));
			}
			public <T> T[] toArray(final T[] a) {
				throw(new UnsupportedOperationException("This collection does not support toArray."));
			}
		};
	}
}
