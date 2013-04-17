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
		return this.phase == PhaseEnum.DBBPTREE;
	}

	@Override
	public Comparator<? super K> comparator() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.comparator();
		default:
			return this.dbbptree.comparator();
		}
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.entrySet();
		default:
			return this.dbbptree.entrySet();
		}
	}

	@Override
	public K firstKey() {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.firstKey();
		}
	}

	@Override
	public SortedMap<K, V> headMap(final K toKey) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.headMap(toKey);
		}
	}

	@Override
	public Set<K> keySet() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.keySet();
		default:
			return this.dbbptree.keySet();
		}
	}

	@Override
	public K lastKey() {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.lastKey();
		}
	}

	@Override
	public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.subMap(fromKey, toKey);
		}
	}

	@Override
	public SortedMap<K, V> tailMap(final K fromKey) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.tailMap(fromKey);
		}
	}

	@Override
	public Collection<V> values() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.values();
		default:
			return this.dbbptree.values();
		}
	}

	@Override
	public void clear() {
		switch (this.phase) {
		case SORTEDMAP:
			this.sortedMap.clear();
			break;
		default:
			this.dbbptree.clear();
			break;
		}
	}

	@Override
	public boolean containsKey(final Object key) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.containsKey(key);
		}
	}

	@Override
	public boolean containsValue(final Object value) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.containsValue(value);
		}
	}

	@Override
	public V get(final Object key) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.get(key);
		}
	}

	@Override
	public boolean isEmpty() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.isEmpty();
		default:
			return this.dbbptree.isEmpty();
		}
	}

	@Override
	public V put(final K key, final V value) {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.put(key, value);
		default:
			return this.dbbptree.put(key, value);
		}
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		switch (this.phase) {
		case SORTEDMAP:
			this.sortedMap.putAll(m);
			break;
		default:
			this.dbbptree.putAll(m);
			break;
		}
	}

	@Override
	public V remove(final Object key) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.remove(key);
		}
	}

	@Override
	public int size() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.size();
		default:
			return this.dbbptree.size();
		}
	}

	public void writeLuposObject(final LuposObjectOutputStream loos)
			throws IOException {
		if (this.phase == PhaseEnum.SORTEDMAP) {
			this.switchPhase();
		}
		this.dbbptree.writeLuposObject(loos);
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
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
		}
	}

	public SortedMap<K, V> getSortedMap() {
		return this.sortedMap;
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.prefixSearch(arg0);
		}
	}

	@Override
	public Object[] getClosestElements(final K arg0) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.getClosestElements(arg0);
		}
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.prefixSearch(arg0, min);
		}
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min, final K max) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.prefixSearch(arg0, min, max);
		}
	}

	@Override
	public Iterator<V> prefixSearchMax(final K arg0, final K max) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.prefixSearchMax(arg0, max);
		}
	}

	private void switchPhase() {
		try{
			this.dbbptree.generateDBBPTree(this.sortedMap);
		} catch(final IOException e){
			System.err.println(e);
			e.printStackTrace();
		}
		this.phase = PhaseEnum.DBBPTREE;
		if (this.sortedMap instanceof DBMergeSortedBag) {
			((DBMergeSortedBag) this.sortedMap).release();
		} else if (this.sortedMap instanceof DBMergeSortedMapOfCollections) {
			((DBMergeSortedMapOfCollections) this.sortedMap).release();
		} else if (this.sortedMap instanceof DBMergeSortedMap) {
			((DBMergeSortedMap) this.sortedMap).release();
		}
		this.sortedMap = null;
	}

	public DBBPTree<K, V> getDBBPTree() {
		return this.dbbptree;
	}
}
