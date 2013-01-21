/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.datastructures.paged_dbbptree;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DBMergeSortedMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedMapOfCollections;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class OptimizedDBBPTreeGeneration<K extends Comparable<K> & Serializable, V extends Serializable>
		implements SortedMap<K, V>, Serializable, PrefixSearchMinMax<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1262243818707458832L;

	protected enum PhaseEnum {
		SORTEDMAP, DBBPTREE
	};

	protected PhaseEnum phase = PhaseEnum.SORTEDMAP;

	protected SortedMap<K, V> sortedMap;
	protected DBBPTree<K, V> dbbptree;

	public OptimizedDBBPTreeGeneration(final SortedMap<K, V> sortedMap,
			final DBBPTree<K, V> dbbptree) {
		this.sortedMap = sortedMap;
		this.dbbptree = dbbptree;
	}

	public boolean generatedCompletely() {
		return phase == PhaseEnum.DBBPTREE;
	}

	public Comparator<? super K> comparator() {
		switch (phase) {
		case SORTEDMAP:
			return sortedMap.comparator();
		default:
			return dbbptree.comparator();
		}
	}

	public Set<Entry<K, V>> entrySet() {
		switch (phase) {
		case SORTEDMAP:
			return sortedMap.entrySet();
		default:
			return dbbptree.entrySet();
		}
	}

	public K firstKey() {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.firstKey();
		}
	}

	public SortedMap<K, V> headMap(final K toKey) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.headMap(toKey);
		}
	}

	public Set<K> keySet() {
		switch (phase) {
		case SORTEDMAP:
			return sortedMap.keySet();
		default:
			return dbbptree.keySet();
		}
	}

	public K lastKey() {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.lastKey();
		}
	}

	public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.subMap(fromKey, toKey);
		}
	}

	public SortedMap<K, V> tailMap(final K fromKey) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.tailMap(fromKey);
		}
	}

	public Collection<V> values() {
		switch (phase) {
		case SORTEDMAP:
			return sortedMap.values();
		default:
			return dbbptree.values();
		}
	}

	public void clear() {
		switch (phase) {
		case SORTEDMAP:
			sortedMap.clear();
			break;
		default:
			dbbptree.clear();
			break;
		}
	}

	public boolean containsKey(final Object key) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.containsKey(key);
		}
	}

	public boolean containsValue(final Object value) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.containsValue(value);
		}
	}

	public V get(final Object key) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.get(key);
		}
	}

	public boolean isEmpty() {
		switch (phase) {
		case SORTEDMAP:
			return sortedMap.isEmpty();
		default:
			return dbbptree.isEmpty();
		}
	}

	public V put(final K key, final V value) {
		switch (phase) {
		case SORTEDMAP:
			return sortedMap.put(key, value);
		default:
			return dbbptree.put(key, value);
		}
	}

	public void putAll(final Map<? extends K, ? extends V> m) {
		switch (phase) {
		case SORTEDMAP:
			sortedMap.putAll(m);
			break;
		default:
			dbbptree.putAll(m);
			break;
		}
	}

	public V remove(final Object key) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.remove(key);
		}
	}

	public int size() {
		switch (phase) {
		case SORTEDMAP:
			return sortedMap.size();
		default:
			return dbbptree.size();
		}
	}

	public void writeLuposObject(final LuposObjectOutputStream loos)
			throws IOException {
		if (phase == PhaseEnum.SORTEDMAP) {
			switchPhase();
		}
		dbbptree.writeLuposObject(loos);
	}

	/**
	 * This constructor is only for reading an object from disk!
	 */
	public OptimizedDBBPTreeGeneration() {

	}

	public static OptimizedDBBPTreeGeneration readLuposObject(
			final LuposObjectInputStream lois) throws IOException,
			ClassNotFoundException {
		final OptimizedDBBPTreeGeneration odtg = new OptimizedDBBPTreeGeneration();
		odtg.phase = PhaseEnum.DBBPTREE;
		odtg.dbbptree = DBBPTree.readLuposObject(lois);
		return odtg;
	}

	public void generateCompletely() {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
		}
	}

	public SortedMap<K, V> getSortedMap() {
		return sortedMap;
	}

	public Iterator<V> prefixSearch(final K arg0) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.prefixSearch(arg0);
		}
	}

	public Object[] getClosestElements(final K arg0) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.getClosestElements(arg0);
		}
	}

	public Iterator<V> prefixSearch(final K arg0, final K min) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.prefixSearch(arg0, min);
		}
	}

	public Iterator<V> prefixSearch(final K arg0, final K min, final K max) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.prefixSearch(arg0, min, max);
		}
	}

	public Iterator<V> prefixSearchMax(final K arg0, final K max) {
		switch (phase) {
		case SORTEDMAP:
			switchPhase();
		default:
			return dbbptree.prefixSearchMax(arg0, max);
		}
	}

	private void switchPhase() {
		try{
			dbbptree.generateDBBPTree(sortedMap);
		} catch(IOException e){
			System.err.println(e);
			e.printStackTrace();
		}
		phase = PhaseEnum.DBBPTREE;
		if (sortedMap instanceof DBMergeSortedBag) {
			((DBMergeSortedBag) sortedMap).release();
		} else if (sortedMap instanceof DBMergeSortedMapOfCollections) {
			((DBMergeSortedMapOfCollections) sortedMap).release();
		} else if (sortedMap instanceof DBMergeSortedMap) {
			((DBMergeSortedMap) sortedMap).release();
		}
		sortedMap = null;
	}

	public DBBPTree<K, V> getDBBPTree() {
		return this.dbbptree;
	}
}
