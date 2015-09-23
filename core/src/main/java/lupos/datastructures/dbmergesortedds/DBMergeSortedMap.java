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
package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

@SuppressWarnings("unchecked")
public class DBMergeSortedMap<K extends Serializable, V extends Serializable>
		implements SortedMap<K, V>, Iterable<MapEntry<K, V>>, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 8291344039430377245L;
	protected SortedSet<MapEntry<K, V>> set;
	private final Comparator<? super K> comp;

	/**
	 * Create a new DBMergeSortedMap that sorts according to the elements'
	 * natural order.
	 *
	 * @param sortConfiguration a {@link lupos.datastructures.dbmergesortedds.SortConfiguration} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedMap(final SortConfiguration sortConfiguration, final Class<? extends MapEntry<K, V>> classOfElements) {
		this(sortConfiguration, null, classOfElements);
	}

	private class MapComparator implements Comparator<Entry<K, V>>,
			Serializable {

		private Comparator<K> comp;
		/**
		 *
		 */
		private static final long serialVersionUID = 7910414506074836272L;

		@Override
		public int compare(final java.util.Map.Entry<K, V> e1,
				final java.util.Map.Entry<K, V> e2) {
			return this.comp.compare(e1.getKey(), e2.getKey());
		}

		public MapComparator(final Comparator<K> comp) {
			this.comp = comp;
		}

		public MapComparator() {
		}
	}

	/**
	 * Create a new DBMergeSortedMap that sorts using the specified Comparator.
	 *
	 * @param comp2
	 *            The Comparator to use for sorting.
	 * @param sortConfiguration a {@link lupos.datastructures.dbmergesortedds.SortConfiguration} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedMap(final SortConfiguration sortConfiguration,
			final Comparator<? super K> comp2,
			final Class<? extends MapEntry<K, V>> classOfElements) {
		if (comp2 != null) {
			this.comp = comp2;
			final Comparator<Entry<K, V>> compa = new MapComparator(
					(Comparator<K>) this.comp);
			this.set = new DBMergeSortedSet<MapEntry<K, V>>(sortConfiguration, compa, classOfElements);
		} else {
			this.comp = new StandardComparator();
			this.set = new DBMergeSortedSet<MapEntry<K, V>>(sortConfiguration, null, classOfElements);
		}
	}

	/**
	 * <p>Constructor for DBMergeSortedMap.</p>
	 *
	 * @param set a {@link java.util.SortedSet} object.
	 */
	public DBMergeSortedMap(final SortedSet<MapEntry<K, V>> set) {
		this.set = set;
		this.comp = (Comparator<? super K>) set.comparator();
	}

	/** {@inheritDoc} */
	@Override
	public Comparator<? super K> comparator() {
		return this.comp;
	}

	/** {@inheritDoc} */
	@Override
	public K firstKey() {
		return this.set.first().getKey();
	}

	/** {@inheritDoc} */
	@Override
	public SortedMap<K, V> headMap(final K to) {
		return new DBMergeSortedMap<K, V>(this.set.headSet(new MapEntry<K, V>(to)));
	}

	/** {@inheritDoc} */
	@Override
	public K lastKey() {
		return this.set.last().getKey();
	}

	/** {@inheritDoc} */
	@Override
	public SortedMap<K, V> subMap(final K from, final K to) {
		return new DBMergeSortedMap<K, V>(this.set.subSet(new MapEntry<K, V>(from),
				new MapEntry<K, V>(to)));
	}

	/** {@inheritDoc} */
	@Override
	public SortedMap<K, V> tailMap(final K from) {
		return new DBMergeSortedMap<K, V>(this.set.tailSet(new MapEntry<K, V>(from)));
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.set.clear();
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(final Object key) {
		return this.set.contains(new MapEntry<K, V>((K) key));
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsValue(final Object value) {
		for (final Entry<K, V> entry : this.set) {
			if (entry.getValue().equals(value)) {
				return true;
			}
		}
		;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new LazySortedSet<MapEntry<K, V>, Entry<K, V>>(this.set,
				new LazySortedSet.Converter<MapEntry<K, V>, Entry<K, V>>() {
					@Override
					public MapEntry<K, V> extToInt(
							final java.util.Map.Entry<K, V> obj) {
						return (MapEntry<K, V>) obj;
					}

					@Override
					public java.util.Map.Entry<K, V> intToExt(
							final MapEntry<K, V> obj) {
						return obj;
					}

				});
	}

	/** {@inheritDoc} */
	@Override
	public V get(final Object key) {
		for (final Entry<K, V> entry : this.set) {
			if (entry.getKey().equals(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.set.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public Set<K> keySet() {
		return new LazySortedSet<MapEntry<K, V>, K>(this.set,
				new LazySortedSet.Converter<MapEntry<K, V>, K>() {
					@Override
					public MapEntry<K, V> extToInt(final K obj) {
						return new MapEntry(obj);
					}

					@Override
					public K intToExt(final MapEntry<K, V> obj) {
						return obj.getKey();
					}
				});
	}

	/** {@inheritDoc} */
	@Override
	public V put(final K key, final V value) {
		this.set.add(new MapEntry<K, V>(key, value));
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(final Map<? extends K, ? extends V> t) {
		for (final Entry<? extends K, ? extends V> entry : t.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	/** {@inheritDoc} */
	@Override
	public V remove(final Object key) {
		this.set.remove(new MapEntry<K, V>((K) key));
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.set.size();
	}

	/** {@inheritDoc} */
	@Override
	public Collection<V> values() {
		return new LazySortedSet<MapEntry<K, V>, V>(this.set,
				new LazySortedSet.Converter<MapEntry<K, V>, V>() {
					@Override
					public MapEntry<K, V> extToInt(final V obj) {
						throw (new UnsupportedOperationException(
								"You can't do that with a valueSet"));
					}

					@Override
					public V intToExt(final MapEntry<K, V> obj) {
						return obj.getValue();
					}
				});
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.set.toString();
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<MapEntry<K, V>> iterator() {
		return this.set.iterator();
	}

	/**
	 * <p>release.</p>
	 */
	public void release() {
		if (this.set instanceof DBMergeSortedSet) {
			((DBMergeSortedSet) this.set).release();
		}
	}
}
