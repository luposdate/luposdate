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
package lupos.datastructures.trie;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TrieMap<T> implements Map<String, T>{

	public static class TrieAndArraySizesDoNotFitException extends Exception{
		private static final long serialVersionUID = -9004007362451083743L;
		public TrieAndArraySizesDoNotFitException(String message){
			super(message);
		}
	}

	private Trie trie;
	private T[] objectArray;

	public TrieMap(Trie trie, T[] objectArray) throws TrieAndArraySizesDoNotFitException{
		this.trie=trie;
		this.objectArray=objectArray;
		if(trie.size()!=objectArray.length)
			throw new TrieAndArraySizesDoNotFitException("The sizes of the given trie and array do not fit: trie size: "+trie.size()+" versus array length: "+objectArray.length);
	}

	public Trie getTrie(){
		return trie;
	}

	public T[] getObjectArray(){
		return objectArray;
	}

	public T put(String key, T value){
		T result=get(key);
		if(result==null){
			trie.add(key);
			int index=trie.getIndex(key);
			@SuppressWarnings("unchecked")
			T[] zobjectArray=(T[])java.lang.reflect.Array.newInstance(
					objectArray.getClass().getComponentType(), objectArray.length+1);
			System.arraycopy(objectArray, 0, zobjectArray, 0, index);
			zobjectArray[index]=value;
			System.arraycopy(objectArray, index, zobjectArray, index+1, objectArray.length-index);
			objectArray=zobjectArray;
		} else {
			this.objectArray[trie.getIndex(key)]=value;
		}
		return result;
	}

	public boolean isEmpty() {
		return size()==0;
	}

	public boolean containsKey(Object key) {
		return (trie.getIndex((String)key)>=0);
	}

	public boolean containsValue(Object value) {
		for(T t: objectArray){
			if(t.equals(value))
				return true;
		}
		return false;
	}

	public T get(Object key) {
		int index=trie.getIndex((String)key);
		return (index<0)?null:objectArray[index];
	}

	public T remove(Object key) {
		int index=trie.getIndex((String)key);
		if(index<0)
			return null;
		else {
			T result =objectArray[index];
			trie.remove((String)key);
			@SuppressWarnings("unchecked")
			T[] zobjectArray=(T[])java.lang.reflect.Array.newInstance(
					objectArray.getClass().getComponentType(), objectArray.length-1);
			System.arraycopy(objectArray, 0, zobjectArray, 0, index);
			System.arraycopy(objectArray, index+1, zobjectArray, index, objectArray.length-index-1);
			objectArray=zobjectArray;
			return result;
		}
	}

	public void putAll(Map<? extends String, ? extends T> m) {
		for(Map.Entry<? extends String, ? extends T> entry:m.entrySet()){
			put(entry.getKey(), entry.getValue());
		}
	}

	public Set<String> keySet() {
		return new Set<String>(){
			public int size() {
				return TrieMap.this.size();
			}

			public boolean isEmpty() {
				return TrieMap.this.isEmpty();
			}

			public boolean contains(Object o) {
				return TrieMap.this.containsKey(o);
			}

			public Iterator<String> iterator() {
				return new Iterator<String>(){
					Iterator<java.util.Map.Entry<String, T>> it=TrieMap.this.entrySet().iterator();

					public boolean hasNext() {
						return it.hasNext();
					}

					public String next() {
						return it.next().getKey();
					}

					public void remove() {
						it.remove();
					}
				};
			}

			public Object[] toArray() {
				Object[] result=new Object[size()];
				int index=0;
				for(String key:this){
					result[index]=key;
					index++;
				}
				return result;
			}

			@SuppressWarnings("unchecked")
			public <T1> T1[] toArray(T1[] a) {
				T1[] result=(T1[])java.lang.reflect.Array.newInstance(
						a.getClass().getComponentType(), size());
				int index=0;
				for(String key:this){
					result[index]=(T1)key;
					index++;
				}
				return result;
			}

			public boolean add(String e) {
				throw new UnsupportedOperationException();
			}

			public boolean remove(Object o) {
				return TrieMap.this.remove(o)!=null;
			}

			public boolean containsAll(Collection<?> c) {
				for(Object o:c){
					if(!contains(o))
						return false;
				}
				return true;
			}

			public boolean addAll(Collection<? extends String> c) {
				throw new UnsupportedOperationException();
			}

			public boolean retainAll(Collection<?> c) {
				boolean flag=false;
				Iterator<String> it=iterator();
				while(it.hasNext()){
					String key=it.next();
					if(!c.contains(key)){
						flag=true;
						it.remove();
					}
				}
				return flag;
			}

			public boolean removeAll(Collection<?> c) {
				boolean flag=false;
				for(Object entry:c)
					flag=flag || remove(entry);
				return flag;
			}

			public void clear() {
				TrieMap.this.clear();
			}
		};
	}

	public Collection<T> values() {
		return new Collection<T>(){
			public int size() {
				return TrieMap.this.size();
			}

			public boolean isEmpty() {
				return TrieMap.this.isEmpty();
			}

			public boolean contains(Object o) {
				return TrieMap.this.containsValue(o);
			}

			public Iterator<T> iterator() {
				return new Iterator<T>(){
					Iterator<java.util.Map.Entry<String, T>> it=TrieMap.this.entrySet().iterator();

					public boolean hasNext() {
						return it.hasNext();
					}

					public T next() {
						return it.next().getValue();
					}

					public void remove() {
						it.remove();
					}
				};
			}

			public Object[] toArray() {
				Object[] result=new Object[size()];
				int index=0;
				for(T entry:this){
					result[index]=entry;
					index++;
				}
				return result;
			}

			@SuppressWarnings("unchecked")
			public <T1> T1[] toArray(T1[] a) {
				T1[] result=(T1[])java.lang.reflect.Array.newInstance(
						a.getClass().getComponentType(), size());
				int index=0;
				for(T entry:this){
					result[index]=(T1)entry;
					index++;
				}
				return result;
			}

			public boolean add(T e) {
				throw new UnsupportedOperationException();
			}

			public boolean remove(Object o) {
				throw new UnsupportedOperationException();
			}

			public boolean containsAll(Collection<?> c) {
				for(Object o:c){
					if(!contains(o))
						return false;
				}
				return true;
			}

			public boolean addAll(Collection<? extends T> c) {
				throw new UnsupportedOperationException();
			}

			public boolean removeAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			public boolean retainAll(Collection<?> c) {
				throw new UnsupportedOperationException();
			}

			public void clear() {
				TrieMap.this.clear();
			}
		};
	}

	public Set<java.util.Map.Entry<String, T>> entrySet() {
		return new Set<java.util.Map.Entry<String, T>>(){
			public int size() {
				return TrieMap.this.size();
			}

			public boolean isEmpty() {
				return TrieMap.this.isEmpty();
			}

			@SuppressWarnings("rawtypes")
			public boolean contains(Object o) {
				if(o instanceof java.util.Map.Entry){
					T value=TrieMap.this.get(((java.util.Map.Entry)o).getKey());
					if(value==null)
						return false;
					else {
						return value.equals(((java.util.Map.Entry)o).getValue());
					}
				} else return false;
			}

			public Iterator<java.util.Map.Entry<String, T>> iterator() {
				return new Iterator<java.util.Map.Entry<String, T>>(){
					int index=0;
					public boolean hasNext() {
						return index<size();
					}
					public java.util.Map.Entry<String, T> next() {
						if(hasNext()) {
							final int finalIndex=index;
							index++;
							return getEntry(finalIndex);
						} else return null;
					}
					public void remove() {
						if(index>0){
							index--;
							TrieMap.this.remove(trie.get(index));
						}
					}
					private java.util.Map.Entry<String, T> getEntry(final int finalIndex){
						return new java.util.Map.Entry<String, T>(){
							public String getKey() {
								return trie.get(finalIndex);
							}
							public T getValue() {
								return objectArray[finalIndex];
							}
							public T setValue(T value) {
								T result=objectArray[finalIndex];
								objectArray[finalIndex]=value;
								return result;
							}
						};
					}
				};
			}

			public Object[] toArray() {
				Object[] result=new Object[size()];
				int index=0;
				for(java.util.Map.Entry<String, T> entry:this){
					result[index]=entry;
					index++;
				}
				return result;
			}

			@SuppressWarnings("unchecked")
			public <T1> T1[] toArray(T1[] a) {
				T1[] result=(T1[])java.lang.reflect.Array.newInstance(
						a.getClass().getComponentType(), size());
				int index=0;
				for(java.util.Map.Entry<String, T> entry:this){
					result[index]=(T1)entry;
					index++;
				}
				return result;
			}

			public boolean add(java.util.Map.Entry<String, T> e) {
				T result = TrieMap.this.put(e.getKey(), e.getValue());
				return result==null?true:!e.getValue().equals(result);
			}

			@SuppressWarnings("rawtypes")
			public boolean remove(Object o) {
				if(contains(o)){
					TrieMap.this.remove(((java.util.Map.Entry)o).getKey());
					return true;
				} else return false;
			}

			public boolean containsAll(Collection<?> c) {
				boolean flag=true;
				for(Object o:c)
					flag=flag && contains(o);
				return flag;
			}

			public boolean addAll(Collection<? extends java.util.Map.Entry<String, T>> c) {
				boolean flag=false;
				for(java.util.Map.Entry<String, T> entry:c)
					flag=flag || add(entry);
				return flag;
			}

			public boolean retainAll(Collection<?> c) {
				boolean flag=false;
				Iterator<java.util.Map.Entry<String, T>> it=iterator();
				while(it.hasNext()){
					java.util.Map.Entry<String, T> entry=it.next();
					if(!c.contains(entry)){
						flag=true;
						it.remove();
					}
				}
				return flag;
			}

			public boolean removeAll(Collection<?> c) {
				boolean flag=false;
				for(Object entry:c)
					flag=flag || remove(entry);
				return flag;
			}

			public void clear() {
				TrieMap.this.clear();
			}
		};
	}

	public int size() {
		return trie.size();
	}

	@SuppressWarnings("unchecked")
	public void clear() {
		trie.clear();
		objectArray=(T[])java.lang.reflect.Array.newInstance(
				objectArray.getClass().getComponentType(), 0);
	}
}
