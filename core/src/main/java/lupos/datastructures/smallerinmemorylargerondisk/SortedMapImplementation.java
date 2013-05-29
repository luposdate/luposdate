/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StandardNodeDeSerializer;

public class SortedMapImplementation<K extends Comparable<K> & Serializable, V extends Serializable> extends MapImplementation<K,V> implements SortedMap<K, V>{

	public SortedMapImplementation(){
		super(new TreeMap<K,V>());
	}

	public SortedMapImplementation(final SortedMap<K,V> memoryMap){
		super(memoryMap);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new Set<java.util.Map.Entry<K, V>>(){
			@Override
			public boolean add(final java.util.Map.Entry<K, V> o) {
				return (SortedMapImplementation.this.put(o.getKey(),o.getValue())!=o.getValue());
			}
			@Override
			public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
				boolean flag=false;
				for(final java.util.Map.Entry<K, V> me:c){
					flag=flag || this.add(me);
				}
				return flag;
			}
			@Override
			public void clear() {
				SortedMapImplementation.this.clear();
			}
			@Override
			public boolean contains(final Object o) {
				if(o instanceof java.util.Map.Entry){
					try{
						final java.util.Map.Entry<K, V> me=(java.util.Map.Entry<K, V>) o;
						final V v=SortedMapImplementation.this.get(me.getKey());
						if(v.equals(me.getValue())) {
							return true;
						} else {
							return false;
						}
					} catch(final ClassCastException ce){
						return false;
					}
				}
				return false;
			}
			@Override
			public boolean containsAll(final Collection<?> c) {
				for(final Object o:c){
					if(!this.contains(o)) {
						return false;
					}
				}
				return true;
			}
			@Override
			public boolean isEmpty() {
				return SortedMapImplementation.this.isEmpty();
			}
			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new Iterator<java.util.Map.Entry<K, V>>(){
					Iterator<java.util.Map.Entry<K, V>> memoryIterator=SortedMapImplementation.this.memoryMap.entrySet().iterator();
					Iterator<java.util.Map.Entry<K, V>> diskIterator= (SortedMapImplementation.this.diskMap==null)? null:SortedMapImplementation.this.diskMap.entrySet().iterator();
					java.util.Map.Entry<K, V> nextMemory=null;
					java.util.Map.Entry<K, V> nextDisk=null;
					@Override
					public boolean hasNext() {
						if(this.nextMemory!=null || this.nextDisk!=null) {
							return true;
						}
						if(this.memoryIterator.hasNext()) {
							return true;
						}
						if(this.diskIterator!=null) {
							return this.diskIterator.hasNext();
						}
						return false;
					}
					@Override
					public java.util.Map.Entry<K, V> next() {
						if(this.nextMemory==null && this.memoryIterator.hasNext()) {
							this.nextMemory=this.memoryIterator.next();
						}
						if(this.diskIterator!=null){
							if(this.nextDisk==null && this.diskIterator.hasNext()) {
								this.nextDisk=this.diskIterator.next();
							}
						}
						if(this.nextMemory==null) {
							return this.nextDisk;
						}
						if(this.nextDisk==null) {
							return this.nextMemory;
						}
						if(((SortedMap<K,V>)SortedMapImplementation.this.memoryMap).comparator().compare(this.nextMemory.getKey(), this.nextDisk.getKey())<=0){
							final java.util.Map.Entry<K, V> result=this.nextMemory;
							this.nextMemory=null;
							return result;
						}
						else {
							final java.util.Map.Entry<K, V> result=this.nextDisk;
							this.nextDisk=null;
							return result;
						}
					}
					@Override
					public void remove() {
						throw(new UnsupportedOperationException("This iterator does not support remove."));
					}
				};
			}
			@Override
			public boolean remove(final Object o) {
				if(o instanceof java.util.Map.Entry){
					try{
						final java.util.Map.Entry<K, V> me=(java.util.Map.Entry<K, V>) o;
						final V v=SortedMapImplementation.this.get(me.getKey());
						if(v.equals(me.getValue())) {
							SortedMapImplementation.this.remove(me.getKey());
							return true;
						} else {
							return false;
						}
					} catch(final ClassCastException ce){
						return false;
					}
				}
				return false;
			}
			@Override
			public boolean removeAll(final Collection<?> c) {
				boolean flag=false;
				for(final Object o:c){
					flag=flag || this.remove(o);
				}
				return flag;
			}
			@Override
			public boolean retainAll(final Collection<?> c) {
				boolean flag=false;
				for(final java.util.Map.Entry<K, V> me:this){
					if(!c.contains(me)){
						flag=true;
						this.remove(me);
					}
				}
				return flag;
			}
			@Override
			public int size() {
				return SortedMapImplementation.this.size();
			}
			@Override
			public Object[] toArray() {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
			@Override
			public <T> T[] toArray(final T[] a) {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
		};
	}

	@Override
	public V put(final K arg0, final V arg1) {
		if(this.memoryMap.size()<MAXMEMORYMAPENTRIES) {
			return this.memoryMap.put(arg0,arg1);
		}
		if(this.memoryMap.containsKey(arg0)) {
			return this.memoryMap.put(arg0,arg1);
		}
		if(this.diskMap==null) {
			final Entry<K,V> entry=this.memoryMap.entrySet().iterator().next();
			try {
				this.diskMap=new DBBPTree<K,V>(((SortedMap<K,V>)this.memoryMap).comparator(),20,20, new StandardNodeDeSerializer<K, V>((Class<? extends K>)entry.getKey().getClass(), (Class<? extends V>)entry.getValue().getClass()));
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		return this.diskMap.put(arg0, arg1);
	}

	@Override
	public Comparator<? super K> comparator() {
		return ((SortedMap<K,V>)this.memoryMap).comparator();
	}

	@Override
	public K firstKey() {
		final K key1=((SortedMap<K,V>)this.memoryMap).firstKey();
		K key2=null;
		if(this.diskMap!=null) {
			key2=((SortedMap<K,V>)this.diskMap).firstKey();
		}
		if(key2==null) {
			return key1;
		}
		if(key1==null) {
			return key1;
		}
		if(((SortedMap<K,V>)this.memoryMap).comparator().compare(key1,key2)<=0) {
			return key1;
		} else {
			return key2;
		}
	}

	@Override
	public SortedMap<K, V> headMap(final K arg0) {
		throw(new UnsupportedOperationException("This SortedMap does not support headMap."));
	}

	@Override
	public K lastKey() {
		final K key1=((SortedMap<K,V>)this.memoryMap).lastKey();
		K key2=null;
		if(this.diskMap!=null) {
			key2=((SortedMap<K,V>)this.diskMap).lastKey();
		}
		if(key2==null) {
			return key1;
		}
		if(key1==null) {
			return key1;
		}
		if(((SortedMap<K,V>)this.memoryMap).comparator().compare(key1,key2)>=0) {
			return key1;
		} else {
			return key2;
		}
	}

	@Override
	public SortedMap<K, V> subMap(final K arg0, final K arg1) {
		throw(new UnsupportedOperationException("This SortedMap does not support subMap."));
	}

	@Override
	public SortedMap<K, V> tailMap(final K arg0) {
		throw(new UnsupportedOperationException("This SortedMap does not support tailMap."));
	}
}
