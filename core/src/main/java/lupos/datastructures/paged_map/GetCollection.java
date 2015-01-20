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
package lupos.datastructures.paged_map;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * GetCollection implements a collection of values for a key
 * 
 * @author Katja Knof
 *
 * @param <K>
 * keys
 * @param <V>
 * values
 */
public class GetCollection<K, V> extends AbstractCollection<V>{
	
	// own map
	private PagedHashMultiMap<K, V> map;
	// key of values
	private K key = null;
	// file ID
	private int fileID;
	
	/**
	 * this method copies a PagedHashMultiMap
	 * 
	 * @param classOfKeys
	 * class of keys
	 * @param classOfValues
	 * class of values
	 * @param pointersFilename
	 * pointers file
	 * @param keysFilename
	 * keys file
	 * @param valuesFilename
	 * values file
	 * @param sizeKeys
	 * size of keys
	 * @param sizeValues
	 * size of values
	 * @param lastKey
	 * last key
	 * @param lastValue
	 * last value
	 * @param key
	 * key of values
	 * @param fileID
	 * file ID
	 * @throws IOException
	 */
	public GetCollection(final Class<K> classOfKeys, final Class<V> classOfValues, final String pointersFilename, final String keysFilename, final String valuesFilename, final long sizeKeys, final long sizeValues, final long lastKey, final long lastValue, final K key, final int fileID) throws IOException{
		this.map = new PagedHashMultiMap<K, V>(classOfKeys, classOfValues, pointersFilename, keysFilename, valuesFilename, sizeKeys, sizeValues, lastKey, lastValue);
		this.key = key;	
		this.fileID = fileID;
	}

	/**
	 * @return
	 * PagedHashMultiMap
	 */
	public PagedHashMultiMap<K, V> returnMap() {
		return this.map;
	}
	
	/**
	 * @return
	 * key
	 */
	public K returnKey() {
		return this.key;
	}
	
	/**
	 * @return
	 * file ID
	 */
	public int returnFileID() {
		return this.fileID;
	}

	/** 
	 * Iterator with duplicates
	 * 
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public Iterator<V> iterator() {
		return GetCollection.this.map.iteratorWithDuplicates(this.key);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return (int) GetCollection.this.map.getNumberOfKeyElements(GetCollection.this.map.getAddressOfKey(this.key));
	}
	
	/**
	 * this method implements methods for a collection
	 * 
	 * @return
	 * AbstractCollection
	 */
	public AbstractCollection<V> getCollection(){
		
		if (!(GetCollection.this.key == null)){
			return new AbstractCollection<V>(){

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#add(java.lang.Object)
				 */
				@Override
				public boolean add(V element) {
					GetCollection.this.map.put(GetCollection.this.key, element);
					return true;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#addAll(java.util.Collection)
				 */
				@Override
				public boolean addAll(Collection<? extends V> coll) {
					Iterator<? extends V> itelements = coll.iterator();
					while(itelements.hasNext()){
						final V element = itelements.next();
						GetCollection.this.map.put(GetCollection.this.key, element);
					}
					return true;
				}
				
				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#clear()
				 */
				@Override
				public void clear() {
					Iterator<V> itelements = GetCollection.this.map.iteratorElements(GetCollection.this.key);
					while(itelements.hasNext()){
						final V element = itelements.next();
						long addressElement = GetCollection.this.map.getAddressOfElement(GetCollection.this.key, element);
						GetCollection.this.map.setNumberOfElementsTo0(addressElement);
					}
					long addressKey = GetCollection.this.map.getAddressOfKey(GetCollection.this.key);
					GetCollection.this.map.setNumberOfKeyElementsTo0(addressKey);
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#contains(java.lang.Object)
				 */
				@SuppressWarnings("unchecked")
				@Override
				public boolean contains(Object obj) {
					V element = null;
					try {
						element = (V)obj;
					} catch (ClassCastException e){
						System.err.println("... couldn't cast object");
						System.err.println("ClassCastException: " + e.getMessage());
						return false;
					}
					long addressKey = GetCollection.this.map.getAddressOfKey(GetCollection.this.key);
					long addressOfValues = GetCollection.this.map.getAddressOfValues(addressKey);
					ResSet result = GetCollection.this.map.containElementGetAddressOfFoundElementGetAddressOfLastElement(element, addressOfValues);
					if (result.containEntry){
						return true;
					} else {
						return false;
					}
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
				 */
				@SuppressWarnings("unchecked")
				@Override
				public boolean containsAll(Collection<?> coll) {
					Collection<V> elements = null;
					try {
						elements = (Collection<V>)coll;
					} catch (ClassCastException e){
						System.err.println("... couldn't cast objects");
						System.err.println("ClassCastException: " + e.getMessage());
						return false;
					}
					Iterator<V> itelements = elements.iterator();
					while(itelements.hasNext()){
						final V element = itelements.next();
						long addressKey = GetCollection.this.map.getAddressOfKey(GetCollection.this.key);
						long addressOfValues = GetCollection.this.map.getAddressOfValues(addressKey);
						ResSet result = GetCollection.this.map.containElementGetAddressOfFoundElementGetAddressOfLastElement(element, addressOfValues);
						if (!result.containEntry){
							return false;
						}
					}
					return true;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#isEmpty()
				 */
				@Override
				public boolean isEmpty() {
					long addressKey = GetCollection.this.map.getAddressOfKey(GetCollection.this.key);
					long numberOfKeyElements = GetCollection.this.map.getNumberOfKeyElements(addressKey);
					if (numberOfKeyElements == 0) {
						return true;
					} else {
						return false;
					}
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#iterator()
				 */
				@Override
				public Iterator<V> iterator() {
					return GetCollection.this.map.iteratorWithDuplicates(GetCollection.this.key);
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#remove(java.lang.Object)
				 */
				@SuppressWarnings("unchecked")
				@Override
				public boolean remove(Object obj) {
					V element = null;
					try {
						element = (V)obj;
					} catch (ClassCastException e){
						System.err.println("... couldn't cast object");
						System.err.println("ClassCastException: " + e.getMessage());
						return false;
					}
					GetCollection.this.map.removeKeyWithValue(GetCollection.this.key, element);
					return true;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
				 */
				@SuppressWarnings("unchecked")
				@Override
				public boolean removeAll(Collection<?> coll) {
					Collection<V> elements = null;
					try {
						elements = (Collection<V>)coll;
					} catch (ClassCastException e){
						System.err.println("... couldn't cast objects");
						System.err.println("ClassCastException: " + e.getMessage());
						return false;
					}
					Iterator<V> itelements = elements.iterator();
					while(itelements.hasNext()){
						final V element = itelements.next();
						GetCollection.this.map.removeKeyWithValue(GetCollection.this.key, element);
					}
					return true;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#size()
				 */
				@Override
				public int size() {
					long addressKey = GetCollection.this.map.getAddressOfKey(GetCollection.this.key);
					long numberOfKeyElements = GetCollection.this.map.getNumberOfKeyElements(addressKey);
					return (int)numberOfKeyElements;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#toString()
				 */
				@Override
				public String toString(){
					String result = null;
					Iterator<V> itelements = GetCollection.this.map.iteratorWithDuplicates(GetCollection.this.key);
					boolean firsttime = true;
					result = "[";
					while (itelements.hasNext()){
						V element = itelements.next();
						if (firsttime){
							firsttime = false;
						} else {
							result += ", ";
						}
						result += element;
						firsttime = false;
						
					}
					result += "]";
					return result;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
				 */
				@Override
				public boolean retainAll(Collection<?> c) {
					return false;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#toArray()
				 */
				@Override
				public Object[] toArray() {
					return null;
				}

				/* (non-Javadoc)
				 * @see java.util.AbstractCollection#toArray(T[])
				 */
				@Override
				public <T> T[] toArray(T[] a) {
					return null;
				}
			};
		} else {
			return null;
		}
	}

}
