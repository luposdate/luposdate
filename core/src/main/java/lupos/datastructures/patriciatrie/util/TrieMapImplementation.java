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
package lupos.datastructures.patriciatrie.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.dbmergesortedds.StandardComparator;
import lupos.datastructures.patriciatrie.TrieMap;
import lupos.misc.util.AbstractSortedMap;

/**
 * This class is a wrapper, such that a given TrieMap object implements the SortedMap<String, V> interface,
 * where V is the type of the stored values...
 * @param <V> the type of the stored values
 */
public class TrieMapImplementation<V> extends AbstractSortedMap<String, V> {
	
	public final TrieMap<V> trie;
	
	/**
	 * @param trie the TrieMap object to be wrapped
	 */
	public TrieMapImplementation(final TrieMap<V> trie){
		this.trie = trie;
	}

	@Override
	public final int size() {
		return this.trie.size();
	}

	@Override
	public final boolean isEmpty() {		
		return this.size()==0;
	}

	@Override
	public boolean containsKey(Object key) {
		if(key instanceof String){
			// use getIndex(key)>=0 instead of get(key)!=null,
			// because in the latter we cannot distinguish whether or not
			// the key has been added or under the key a null value has been put... 
			return this.trie.getIndex((String)key)>=0;
		} else {
			return false;
		}
	}

	@Override
	public V get(Object key) {
		if(key instanceof String){
			return this.trie.get((String)key);
		} else {
			return null;
		}
	}

	@Override
	public V put(String key, V value) {		
		return this.trie.put(key, value);
	}

	@Override
	public V remove(Object key) {
		if(key instanceof String){
			return this.trie.removeKey((String)key);
		} else {
			return null;
		}		
	}

	@Override
	public void clear() {
		this.trie.clear();
	}

	@Override
	public Comparator<? super String> comparator() {
		return new StandardComparator<String>();
	}


	@Override
	public Set<String> keySet() {
		return new java.util.AbstractSet<String>(){

			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>(){

					final Iterator<Entry<String, V>> it = TrieMapImplementation.this.trie.iterator();
					
					@Override
					public boolean hasNext() {
						return this.it.hasNext();
					}

					@Override
					public String next() {						
						return this.it.next().getKey();
					}

					@Override
					public void remove() {
						this.it.remove();
					}					
				};
			}

			@Override
			public int size() {
				return TrieMapImplementation.this.size();
			}			
	        @Override
			public boolean remove(Object o) {
	            return TrieMapImplementation.this.remove(o) != null;
	        }
	        @Override
			public void clear() {
	        	TrieMapImplementation.this.clear();
	        }
		};
	}

	@Override
	public Collection<V> values() {	
		return new java.util.AbstractCollection<V>(){

			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>(){

					final Iterator<Entry<String, V>> it = TrieMapImplementation.this.trie.iterator();
					
					@Override
					public boolean hasNext() {
						return this.it.hasNext();
					}

					@Override
					public V next() {						
						return this.it.next().getValue();
					}

					@Override
					public void remove() {
						this.it.remove();
					}					
				};
			}

			@Override
			public int size() {
				return TrieMapImplementation.this.size();
			}
			
			@Override
			public void clear() {
				TrieMapImplementation.this.clear();
			}			
		};
	}

	@Override
	public Set<java.util.Map.Entry<String, V>> entrySet() {		
		return new java.util.AbstractSet<java.util.Map.Entry<String, V>>(){

			@Override
			public Iterator<java.util.Map.Entry<String, V>> iterator() {
				return TrieMapImplementation.this.trie.iterator();
			}

			@Override
			public int size() {
				return TrieMapImplementation.this.size();
			}			
	        @Override
			public boolean remove(Object o) {
	            if (!(o instanceof Map.Entry)){
	                return false;
	            }
	            @SuppressWarnings("unchecked")
				Map.Entry<String, V> entry = (Map.Entry<String, V>) o;
	            V v = TrieMapImplementation.this.trie.get(entry.getKey());
	            if(entry.getValue()==null){
	            	if(v==null){
	            		return TrieMapImplementation.this.remove(entry.getKey()) != null;
	            	}
	            } else if(entry.getValue().equals(v)) {
	            	return TrieMapImplementation.this.remove(entry.getKey()) != null;
	            }
	            return false;
	        }
	        @Override
			public void clear() {
	        	TrieMapImplementation.this.clear();
	        }
		};
	}
	
	@Override
	public String lastKey() {
		return this.trie.get(this.size()-1);
	}

	@Override
	public SortedMap<String, V> subMap(String fromKey, String toKey,
			boolean inclusiveLastKey) {
		throw new UnsupportedOperationException();
	}	
}
