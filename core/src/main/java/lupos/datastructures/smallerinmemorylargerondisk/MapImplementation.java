/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StandardNodeDeSerializer;
import lupos.misc.util.ImmutableIterator;
public class MapImplementation<K extends Comparable<K> & Serializable, V extends Serializable> implements Map<K, V>{

	protected final Map<K,V> memoryMap;
	protected Map<K,V> diskMap=null;

	/** Constant <code>MAXMEMORYMAPENTRIES=3000000</code> */
	protected final static int MAXMEMORYMAPENTRIES = 3000000;

	/**
	 * <p>Constructor for MapImplementation.</p>
	 */
	public MapImplementation(){
		this(new HashMap<K,V>());
	}

	/**
	 * <p>Constructor for MapImplementation.</p>
	 *
	 * @param memoryMap a {@link java.util.Map} object.
	 */
	public MapImplementation(final Map<K,V> memoryMap){
		this.memoryMap = memoryMap;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.memoryMap.clear();
		if(this.diskMap!=null){
			this.diskMap.clear();
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(final Object arg0) {
		if(this.memoryMap.containsKey(arg0)) {
			return true;
		}
		if(this.diskMap!=null) {
			return this.diskMap.containsKey(arg0);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsValue(final Object arg0) {
		if(this.memoryMap.containsValue(arg0)) {
			return true;
		}
		if(this.diskMap!=null) {
			return this.diskMap.containsValue(arg0);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new Set<java.util.Map.Entry<K, V>>(){
			@Override
			public boolean add(final java.util.Map.Entry<K, V> o) {
				return (MapImplementation.this.put(o.getKey(),o.getValue())!=o.getValue());
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
				MapImplementation.this.clear();
			}
			@Override
			public boolean contains(final Object o) {
				if(o instanceof java.util.Map.Entry){
					try{
						@SuppressWarnings("unchecked")
						final java.util.Map.Entry<K, V> me=(java.util.Map.Entry<K, V>) o;
						final V v=MapImplementation.this.get(me.getKey());
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
				return MapImplementation.this.isEmpty();
			}
			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new ImmutableIterator<java.util.Map.Entry<K, V>>(){
					Iterator<java.util.Map.Entry<K, V>> memoryIterator=MapImplementation.this.memoryMap.entrySet().iterator();
					Iterator<java.util.Map.Entry<K, V>> diskIterator= (MapImplementation.this.diskMap==null)? null:MapImplementation.this.diskMap.entrySet().iterator();
					@Override
					public boolean hasNext() {
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
						if(this.memoryIterator.hasNext()) {
							return this.memoryIterator.next();
						}
						if(this.diskIterator!=null) {
							return this.diskIterator.next();
						}
						return null;
					}
				};
			}
			@Override
			public boolean remove(final Object o) {
				if(o instanceof java.util.Map.Entry){
					try{
						@SuppressWarnings("unchecked")
						final java.util.Map.Entry<K, V> me=(java.util.Map.Entry<K, V>) o;
						final V v=MapImplementation.this.get(me.getKey());
						if(v.equals(me.getValue())) {
							MapImplementation.this.remove(me.getKey());
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
				return MapImplementation.this.size();
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

	/** {@inheritDoc} */
	@Override
	public V get(final Object arg0) {
		final V v=this.memoryMap.get(arg0);
		if(v!=null) {
			return v;
		}
		if(this.diskMap!=null) {
			return this.diskMap.get(arg0);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		if(this.memoryMap.size()>0) {
			return false;
		}
		if(this.diskMap!=null && !this.diskMap.isEmpty()) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public Set<K> keySet() {
		return new Set<K>(){
			@Override
			public boolean add(final K arg0) {
				throw(new UnsupportedOperationException("This set does not support add."));
			}
			@Override
			public boolean addAll(final Collection<? extends K> arg0) {
				throw(new UnsupportedOperationException("This set does not support addAll."));
			}
			@Override
			public void clear() {
				MapImplementation.this.clear();
			}
			@Override
			public boolean contains(final Object arg0) {
				return MapImplementation.this.containsKey(arg0);
			}
			@Override
			public boolean containsAll(final Collection<?> arg0) {
				for(final Object o:arg0){
					if(!this.contains(o)) {
						return false;
					}
				}
				return true;
			}
			@Override
			public boolean isEmpty() {
				return MapImplementation.this.isEmpty();
			}
			@Override
			public Iterator<K> iterator() {
				return new ImmutableIterator<K>(){
					final Iterator<java.util.Map.Entry<K, V>> entryIt=MapImplementation.this.entrySet().iterator();
					@Override
					public boolean hasNext() {
						return this.entryIt.hasNext();
					}
					@Override
					public K next() {
						return this.entryIt.next().getKey();
					}
				};
			}
			@Override
			public boolean remove(final Object arg0) {
				return (MapImplementation.this.remove(arg0)!=null);
			}
			@Override
			public boolean removeAll(final Collection<?> arg0) {
				boolean flag=false;
				for(final Object o:arg0){
					flag=flag || this.remove(o);
				}
				return flag;
			}
			@Override
			public boolean retainAll(final Collection<?> arg0) {
				boolean flag=false;
				for(final K k:this){
					if(!arg0.contains(k)){
						flag=true;
						this.remove(k);
					}
				}
				return flag;
			}
			@Override
			public int size() {
				return MapImplementation.this.size();
			}
			@Override
			public Object[] toArray() {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
			@Override
			public <T> T[] toArray(final T[] arg0) {
				throw(new UnsupportedOperationException("This set does not support toArray."));
			}
		};
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public V put(final K arg0, final V arg1) {
		if(this.memoryMap.size()<MAXMEMORYMAPENTRIES) {
			return this.memoryMap.put(arg0,arg1);
		}
		if(this.memoryMap.containsKey(arg0)) {
			return this.memoryMap.put(arg0,arg1);
		}
		if (this.diskMap == null){
			final Entry<K, V> entry=this.memoryMap.entrySet().iterator().next();
			try {
				this.diskMap = new DBBPTree<K, V>(100000, 100000, new StandardNodeDeSerializer<K, V>((Class<? extends K>)entry.getKey().getClass(),(Class<? extends V>) entry.getValue().getClass()));
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
		return this.diskMap.put(arg0, arg1);
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(final java.util.Map<? extends K, ? extends V> arg0) {
		for(final java.util.Map.Entry<? extends K, ? extends V> me:arg0.entrySet()){
			this.put(me.getKey(),me.getValue());
		}
	}

	/** {@inheritDoc} */
	@Override
	public V remove(final Object arg0) {
		V v=this.memoryMap.remove(arg0);
		if(v==null){
			if(this.diskMap!=null) {
				v=this.diskMap.remove(arg0);
			}
		}
		return v;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		int size=this.memoryMap.size();
		if(this.diskMap!=null) {
			size+=this.diskMap.size();
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<V> values() {
		return new Collection<V>(){
			@Override
			public boolean add(final V o) {
				throw(new UnsupportedOperationException("This collection does not support add."));
			}
			@Override
			public boolean addAll(final Collection<? extends V> c) {
				throw(new UnsupportedOperationException("This collection does not support add."));
			}
			@Override
			public void clear() {
				throw(new UnsupportedOperationException("This collection does not support clear."));
			}
			@Override
			public boolean contains(final Object o) {
				return MapImplementation.this.containsValue(o);
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
				return MapImplementation.this.isEmpty();
			}
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>(){
					final Iterator<java.util.Map.Entry<K, V>> entryIt=MapImplementation.this.entrySet().iterator();
					@Override
					public boolean hasNext() {
						return this.entryIt.hasNext();
					}
					@Override
					public V next() {
						return this.entryIt.next().getValue();
					}
					@Override
					public void remove() {
						this.entryIt.remove();
					}
				};
			}
			@Override
			public boolean remove(final Object o) {
				throw(new UnsupportedOperationException("This collection does not support remove."));
			}
			@Override
			public boolean removeAll(final Collection<?> c) {
				throw(new UnsupportedOperationException("This collection does not support removeAll."));
			}
			@Override
			public boolean retainAll(final Collection<?> c) {
				throw(new UnsupportedOperationException("This collection does not support retainAll."));
			}
			@Override
			public int size() {
				return MapImplementation.this.size();
			}
			@Override
			public Object[] toArray() {
				throw(new UnsupportedOperationException("This collection does not support toArray."));
			}
			@Override
			public <T> T[] toArray(final T[] a) {
				throw(new UnsupportedOperationException("This collection does not support toArray."));
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public String toString(){
		String result = "Map in memory: " + this.memoryMap.toString();
		if(this.diskMap!=null){
			result += "Map on disk: " + this.diskMap.toString();
		}
		return result;
	}
}
