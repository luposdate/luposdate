/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.StandardNodeDeSerializer;

public class SortedMapImplementation<K extends Comparable<K> & Serializable, V extends Serializable> extends MapImplementation<K,V> implements SortedMap<K, V>{
	
	private final SortedMap<K,V> memoryMap;
	private SortedMap<K,V> diskMap=null;
	
	public SortedMapImplementation(){
		memoryMap=new TreeMap<K,V>();
	}
	
	public SortedMapImplementation(final SortedMap<K,V> memoryMap){
		this.memoryMap=memoryMap;
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new Set<java.util.Map.Entry<K, V>>(){
			public boolean add(final java.util.Map.Entry<K, V> o) {
				return (SortedMapImplementation.this.put(o.getKey(),o.getValue())!=o.getValue());
			}
			public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
				boolean flag=false;
				for(final java.util.Map.Entry<K, V> me:c){
					flag=flag || add(me);
				}
				return flag;
			}
			public void clear() {
				SortedMapImplementation.this.clear();	
			}
			public boolean contains(final Object o) {
				if(o instanceof java.util.Map.Entry){
					try{
						final java.util.Map.Entry<K, V> me=(java.util.Map.Entry<K, V>) o;
						final V v=SortedMapImplementation.this.get(me.getKey());
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
				return SortedMapImplementation.this.isEmpty();	
			}
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new Iterator<java.util.Map.Entry<K, V>>(){			
					Iterator<java.util.Map.Entry<K, V>> memoryIterator=memoryMap.entrySet().iterator();
					Iterator<java.util.Map.Entry<K, V>> diskIterator= (diskMap==null)? null:diskMap.entrySet().iterator();
					java.util.Map.Entry<K, V> nextMemory=null;
					java.util.Map.Entry<K, V> nextDisk=null;
					public boolean hasNext() {
						if(nextMemory!=null || nextDisk!=null) return true;
						if(memoryIterator.hasNext()) return true;
						if(diskIterator!=null) return diskIterator.hasNext();
						return false;
					}
					public java.util.Map.Entry<K, V> next() {						
						if(nextMemory==null && memoryIterator.hasNext()) nextMemory=memoryIterator.next();
						if(diskIterator!=null){
							if(nextDisk==null && diskIterator.hasNext()) nextDisk=diskIterator.next();
						}
						if(nextMemory==null) return nextDisk;
						if(nextDisk==null) return nextMemory;
						if(SortedMapImplementation.this.memoryMap.comparator().compare(nextMemory.getKey(), nextDisk.getKey())<=0){
							final java.util.Map.Entry<K, V> result=nextMemory;
							nextMemory=null;
							return result;
						}
						else {
							final java.util.Map.Entry<K, V> result=nextDisk;
							nextDisk=null;
							return result;							
						}
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
						final V v=SortedMapImplementation.this.get(me.getKey());
						if(v.equals(me.getValue())) {
							SortedMapImplementation.this.remove(me.getKey());
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
				return SortedMapImplementation.this.size();
			}
			public Object[] toArray() {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
			public <T> T[] toArray(final T[] a) {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
		};
	}

	@Override
	public V put(final K arg0, final V arg1) {
		if(memoryMap.size()<MAXMEMORYMAPENTRIES) return memoryMap.put(arg0,arg1);
		if(memoryMap.containsKey(arg0)) return memoryMap.put(arg0,arg1);
		if(diskMap==null) {
			Entry<K,V> entry=memoryMap.entrySet().iterator().next();
			try {
				diskMap=new DBBPTree<K,V>(memoryMap.comparator(),20,20, new StandardNodeDeSerializer<K, V>((Class<? super K>)entry.getKey().getClass(), (Class<? super V>)entry.getValue().getClass()));
			} catch (IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		return diskMap.put(arg0, arg1);
	}

	public Comparator<? super K> comparator() {
		return memoryMap.comparator();
	}

	public K firstKey() {
		final K key1=memoryMap.firstKey();
		K key2=null;
		if(diskMap!=null) key2=diskMap.firstKey();
		if(key2==null) return key1;
		if(key1==null) return key1;
		if(memoryMap.comparator().compare(key1,key2)<=0) return key1;
		else return key2;
	}

	public SortedMap<K, V> headMap(final K arg0) {
		throw(new UnsupportedOperationException("This SortedMap does not support headMap."));
	}

	public K lastKey() {
		final K key1=memoryMap.lastKey();
		K key2=null;
		if(diskMap!=null) key2=diskMap.lastKey();
		if(key2==null) return key1;
		if(key1==null) return key1;
		if(memoryMap.comparator().compare(key1,key2)>=0) return key1;
		else return key2;
	}

	public SortedMap<K, V> subMap(final K arg0, final K arg1) {
		throw(new UnsupportedOperationException("This SortedMap does not support subMap."));
	}

	public SortedMap<K, V> tailMap(final K arg0) {
		throw(new UnsupportedOperationException("This SortedMap does not support tailMap."));
	}
}
