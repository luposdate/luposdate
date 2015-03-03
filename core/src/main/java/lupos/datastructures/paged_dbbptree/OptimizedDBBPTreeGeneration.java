
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.datastructures.paged_dbbptree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DBMergeSortedMap;
import lupos.datastructures.dbmergesortedds.DBMergeSortedMapOfCollections;
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

	/**
	 * <p>Constructor for OptimizedDBBPTreeGeneration.</p>
	 *
	 * @param sortedMap a {@link java.util.SortedMap} object.
	 * @param dbbptree a {@link lupos.datastructures.paged_dbbptree.DBBPTree} object.
	 */
	public OptimizedDBBPTreeGeneration(final SortedMap<K, V> sortedMap,
			final DBBPTree<K, V> dbbptree) {
		this.sortedMap = sortedMap;
		this.dbbptree = dbbptree;
	}

	/**
	 * <p>generatedCompletely.</p>
	 *
	 * @return a boolean.
	 */
	public boolean generatedCompletely() {
		return this.phase == PhaseEnum.DBBPTREE;
	}

	/** {@inheritDoc} */
	@Override
	public Comparator<? super K> comparator() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.comparator();
		default:
			return this.dbbptree.comparator();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<Entry<K, V>> entrySet() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.entrySet();
		default:
			return this.dbbptree.entrySet();
		}
	}

	/** {@inheritDoc} */
	@Override
	public K firstKey() {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.firstKey();
		}
	}

	/** {@inheritDoc} */
	@Override
	public SortedMap<K, V> headMap(final K toKey) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.headMap(toKey);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Set<K> keySet() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.keySet();
		default:
			return this.dbbptree.keySet();
		}
	}

	/** {@inheritDoc} */
	@Override
	public K lastKey() {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.lastKey();
		}
	}

	/** {@inheritDoc} */
	@Override
	public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.subMap(fromKey, toKey);
		}
	}

	/** {@inheritDoc} */
	@Override
	public SortedMap<K, V> tailMap(final K fromKey) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.tailMap(fromKey);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Collection<V> values() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.values();
		default:
			return this.dbbptree.values();
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(final Object key) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.containsKey(key);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsValue(final Object value) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.containsValue(value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public V get(final Object key) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.get(key);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.isEmpty();
		default:
			return this.dbbptree.isEmpty();
		}
	}

	/** {@inheritDoc} */
	@Override
	public V put(final K key, final V value) {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.put(key, value);
		default:
			return this.dbbptree.put(key, value);
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public V remove(final Object key) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.remove(key);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		switch (this.phase) {
		case SORTEDMAP:
			return this.sortedMap.size();
		default:
			return this.dbbptree.size();
		}
	}

	/**
	 * <p>writeLuposObject.</p>
	 *
	 * @param loos a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	public void writeLuposObject(final OutputStream loos)
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

	/**
	 * <p>readLuposObject.</p>
	 *
	 * @param lois a {@link java.io.InputStream} object.
	 * @return a {@link lupos.datastructures.paged_dbbptree.OptimizedDBBPTreeGeneration} object.
	 * @throws java.io.IOException if any.
	 * @throws java.lang.ClassNotFoundException if any.
	 * @throws java$net$URISyntaxException if any.
	 */
	public static OptimizedDBBPTreeGeneration readLuposObject(
			final InputStream lois) throws IOException,
			ClassNotFoundException, URISyntaxException {
		final OptimizedDBBPTreeGeneration odtg = new OptimizedDBBPTreeGeneration();
		odtg.phase = PhaseEnum.DBBPTREE;
		odtg.dbbptree = DBBPTree.readLuposObject(lois);
		return odtg;
	}

	/**
	 * <p>generateCompletely.</p>
	 */
	public void generateCompletely() {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
		}
	}

	/**
	 * <p>Getter for the field <code>sortedMap</code>.</p>
	 *
	 * @return a {@link java.util.SortedMap} object.
	 */
	public SortedMap<K, V> getSortedMap() {
		return this.sortedMap;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<V> prefixSearch(final K arg0) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.prefixSearch(arg0);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object[] getClosestElements(final K arg0) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.getClosestElements(arg0);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.prefixSearch(arg0, min);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min, final K max) {
		switch (this.phase) {
		case SORTEDMAP:
			this.switchPhase();
		default:
			return this.dbbptree.prefixSearch(arg0, min, max);
		}
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>getDBBPTree.</p>
	 *
	 * @return a {@link lupos.datastructures.paged_dbbptree.DBBPTree} object.
	 */
	public DBBPTree<K, V> getDBBPTree() {
		return this.dbbptree;
	}
}
