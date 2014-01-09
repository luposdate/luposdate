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
package lupos.datastructures.paged_map;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;

/**
 * PagedQueryResultMapValues is a wrapper class of {@link PagedHashMultiMap}
 *
 * @author K. Knof
 *
 * @param <K>
 * class of keys
 */
public class PagedQueryResultMapValues<K> extends AbstractMap<K, QueryResult>{

	private final PagedHashMultiMap<K, Bindings> map;

	/**
	 * constructor
	 *
	 * @param classOfKeys
	 * class of keys
	 */
	public PagedQueryResultMapValues(final Class<K> classOfKeys){
		this.map = new PagedHashMultiMap<K, Bindings>(classOfKeys, Bindings.class);
	}

	/**
	 * @return
	 * PagedHashMultiMap
	 */
	public PagedHashMultiMap<K, Bindings> returnMap(){
		return this.map;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<K, QueryResult>> entrySet() {
		final Set<Entry<K, QueryResult>> mapSet = new HashSet<Entry<K, QueryResult>>();
		SimpleEntry<K, QueryResult> entry = null;
		final Iterator<K> itkeys = this.map.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.map.getKey(this.map.getAddressOfKey(key)).getThird() == 0)){
				final Iterator<Bindings> itelements = this.map.iteratorElements(key);
				final Collection<Bindings> coll = new ArrayList<Bindings>();
				while(itelements.hasNext()){
					final Bindings element = itelements.next();
					coll.add(element);
				}
				entry = new SimpleEntry<K, QueryResult>(key, QueryResult.createInstance(coll));
				mapSet.add(entry);
			}
		}
		return mapSet;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#clear()
	 */
	@Override
	public void clear() {
		this.map.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#get(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public QueryResult get(final Object key){
		K thiskey = null;
		try {
			thiskey = (K)key;
		} catch (final ClassCastException e){
			System.err.println("... couldn't cast object");
			System.err.println("ClassCastException: " + e.getMessage());
			return null;
		}
		Collection<Bindings> coll = null;
		try {
			coll = this.map.getCollection(thiskey);
		} catch (final IOException e) {
	    	System.err.println("... IOException: " + e.getMessage());
		}
		if (coll == null) {
			return null;
		} else {
			return QueryResult.createInstance(coll);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		if (this.map.isEmpty()){
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#keySet()
	 */
	@Override
	public Set<K> keySet() {
		return this.map.keySet();
	}

	/**
	 * this method puts a value to the elements list of a key
	 *
	 * before putting data the method checks if the data are from the last get call
	 *
	 * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public QueryResult put(final K key, final QueryResult value) {
		final Collection<Bindings> collQueryResult = value.getCollection();
		int queryResultFileID = 0;
		K queryResultKey = null;
		if (collQueryResult instanceof GetCollection) {
			GetCollection<K, Bindings> getcollQueryResult = null;
			try {
			getcollQueryResult = (GetCollection<K, Bindings>) collQueryResult;
			} catch (final ClassCastException e){
				System.err.println("... couldn't cast objects");
				System.err.println("ClassCastException: " + e.getMessage());
				return null;
			}
			queryResultFileID = getcollQueryResult.returnFileID();
			queryResultKey = getcollQueryResult.returnKey();
		}
		if (collQueryResult instanceof GetCollection && (this.map.getFileID() == queryResultFileID) && (key.equals(queryResultKey)) ) {
			return null;
		} else {
			this.remove(key);

			for (final Bindings b : collQueryResult) {
				this.map.put(key, b);
			}
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#remove(java.lang.Object)
	 */
	@Override
	public QueryResult remove(final Object key) {
		this.map.remove(key);
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#size()
	 */
	@Override
	public int size() {
		return this.map.size();
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#toString()
	 */
	@Override
	public String toString(){
		return this.map.toString();
	}

	// with duplicates
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#values()
	 */
	@Override
	public Collection<QueryResult> values() {
		final Collection<QueryResult> values = new ArrayList<QueryResult>();
		final Iterator<K> itkeys = this.map.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.map.getKey(this.map.getAddressOfKey(key)).getThird() == 0)){
				final Iterator<Bindings> itelements = this.map.iteratorWithDuplicates(key);
				final Collection<Bindings> coll = new ArrayList<Bindings>();
				while(itelements.hasNext()){
					final Bindings element = itelements.next();
					coll.add(element);
				}
				values.add(QueryResult.createInstance(coll));
			}
		}
		return values;
	}
}