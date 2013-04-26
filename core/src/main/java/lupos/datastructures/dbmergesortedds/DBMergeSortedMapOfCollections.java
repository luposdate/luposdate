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
package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.sorteddata.SortedMapOfCollections;

public class DBMergeSortedMapOfCollections<K extends Serializable, V extends Serializable, CV extends Collection<V>>
		implements SortedMapOfCollections<K, V, CV>, Iterable<MapEntry<K, V>> {
	private final DBMergeSortedBag<MapEntry<K, V>> bag;
	private int size = -1;
	private Class<?> collectionClass;
	private Comparator<? super K> comp;

	/**
	 * Create a new DBMergeSortedMapOfCollections that sorts according to the
	 * elements' natural order.
	 *
	 * @param collectionClass
	 *            The class object used to create the collections that are
	 *            returned by this map. This should be the same collection class
	 *            that this map is parametrisized with.
	 * @param heapHeight
	 *            The height of the heap used to presort the elements in memory.
	 *            (The maximum number of elements that are held in memory at any
	 *            given time will be 2**heapHeight-1)
	 */
	public DBMergeSortedMapOfCollections(final Class<?> collectionClass,
			final SortConfiguration sortConfiguration,
			final Class<? extends MapEntry<K, V>> classOfElements) {
		this.collectionClass = collectionClass;
		this.bag = new DBMergeSortedBag<MapEntry<K, V>>(sortConfiguration, classOfElements);
	}

	/**
	 * Create a new DBMergeSortedMapOfCollections that sorts using the specified
	 * Comparator.
	 *
	 * @param collectionClass
	 *            The class object used to create the collections that are
	 *            returned by this map. This should be the same collection class
	 *            that this map is parametrisized with.
	 * @param heapHeight
	 *            The height of the heap used to presort the elements in memory.
	 *            (The maximum number of elements that are held in memory at any
	 *            given time will be 2**heapHeight-1)
	 * @param comp
	 *            The Comparator to use for sorting.
	 */
	public DBMergeSortedMapOfCollections(final Class<?> collectionClass,
			final SortConfiguration sortConfiguration, final Comparator<? super K> comp,
			final Class<? extends MapEntry<K, V>> classOfElements) {
		if (comp != null) {
			this.comp = comp;
		} else {
			this.comp = new Comparator<K>() {
				@Override
				public int compare(final K arg0, final K arg1) {
					return ((Comparable<K>) arg0).compareTo(arg1);
				}
			};
		}
		this.collectionClass = collectionClass;
		final Comparator<Entry<K, V>> compa = new Comparator<Entry<K, V>>() {
			@Override
			public int compare(final java.util.Map.Entry<K, V> e1,
					final java.util.Map.Entry<K, V> e2) {
				return comp.compare(e1.getKey(), e2.getKey());
			}
		};
		this.bag = new DBMergeSortedBag<MapEntry<K, V>>(sortConfiguration, compa,
				classOfElements);
	}

	private DBMergeSortedMapOfCollections(
			final DBMergeSortedBag<MapEntry<K, V>> bag) {
		this.bag = bag;
	}

	@SuppressWarnings("unchecked")
	protected CV createCollection() {
		try {
			return (CV) this.collectionClass.newInstance();
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Comparator<? super K> comparator() {
		return this.comp;
	}

	@Override
	public K firstKey() {
		return this.bag.first().getKey();
	}

	@Override
	public SortedMap<K, CV> headMap(final K toKey) {
		return new DBMergeSortedMapOfCollections<K, V, CV>(this.bag
				.headBag(new MapEntry<K, V>(toKey)));
	}

	@Override
	public K lastKey() {
		return this.bag.last().getKey();
	}

	@Override
	public SortedMap<K, CV> subMap(final K fromKey, final K toKey) {
		return new DBMergeSortedMapOfCollections<K, V, CV>(this.bag.subBag(
				new MapEntry<K, V>(fromKey), new MapEntry<K, V>(toKey)));
	}

	@Override
	public SortedMap<K, CV> tailMap(final K fromKey) {
		return new DBMergeSortedMapOfCollections<K, V, CV>(this.bag
				.tailBag(new MapEntry<K, V>(fromKey)));
	}

	@Override
	public void clear() {
		this.bag.clear();
	}

	@Override
	public boolean containsKey(final Object key) {
		for (final K k : this.keySet()) {
			if (k.equals(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(final Object value) {
		return this.values().contains(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, CV>> entrySet() {
		return new Set<Map.Entry<K, CV>>() {
			@Override
			public boolean add(final java.util.Map.Entry<K, CV> o) {
				DBMergeSortedMapOfCollections.this.put(o.getKey(), o.getValue());
				return true;
			}

			@Override
			public boolean addAll(
					final Collection<? extends java.util.Map.Entry<K, CV>> c) {
				for (final Map.Entry<K, CV> entry : c) {
					this.add(entry);
				}
				return true;
			}

			@Override
			public void clear() {
				DBMergeSortedMapOfCollections.this.clear();
			}

			@Override
			public boolean contains(final Object o) {
				final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
				return DBMergeSortedMapOfCollections.this.containsKey(entry.getKey())
						&& DBMergeSortedMapOfCollections.this.get(entry.getKey()).equals(entry.getValue());
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				for (final Object o : c) {
					if (!this.contains(o)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			@Override
			public ParallelIterator<java.util.Map.Entry<K, CV>> iterator() {
				final Iterator<MapEntry<K, V>> iter = DBMergeSortedMapOfCollections.this
						.iterator();
				return new ParallelIterator<Map.Entry<K, CV>>() {
					Entry<K, V> next = null;

					@Override
					public boolean hasNext() {
						return this.next != null || iter.hasNext();
					}

					@Override
					public Map.Entry<K, CV> next() {
						if (this.next == null) {
							this.next = iter.next();
						}
						Entry<K, V> cur = this.next;
						final CV col = DBMergeSortedMapOfCollections.this.createCollection();
						while (this.next != null
								&& cur.getKey().equals(this.next.getKey())) {
							cur = this.next;
							col.add(cur.getValue());
							if (iter.hasNext()) {
								this.next = iter.next();
							} else {
								this.next = null;
							}
						}
						return new MapEntry<K, CV>(cur.getKey(), col);
					}

					@Override
					public void remove() {
						throw (new UnsupportedOperationException(
								"Remove not implemented."));
					}

					@Override
					public void finalize() {
						this.close();
					}

					@Override
					public void close() {
						if (iter instanceof ParallelIterator) {
							((ParallelIterator) iter).close();
						}
					}
				};

			}

			@Override
			public boolean remove(final Object o) {
				return DBMergeSortedMapOfCollections.this
						.remove(((Map.Entry<?, ?>) o).getKey()) != null;
			}

			@Override
			public boolean removeAll(final Collection<?> c) {
				boolean result = true;
				for (final Object o : c) {
					result = this.remove(o) && result;
				}
				return result;
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This operation is not supported."));
			}

			@Override
			public int size() {
				return DBMergeSortedMapOfCollections.this.size();
			}

			@Override
			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			@Override
			public <T> T[] toArray(final T[] a) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

		};
	}

	@Override
	public CV get(final Object key) {
		for (final Map.Entry<K, CV> entry : this.entrySet()) {
			if (entry.getKey().equals(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return this.bag.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return new Set<K>() {
			@Override
			public boolean add(final K arg0) {
				throw (new UnsupportedOperationException(
						"Can't add key without a value"));
			}

			@Override
			public boolean addAll(final Collection<? extends K> arg0) {
				throw (new UnsupportedOperationException(
						"Can't add key without a value"));
			}

			@Override
			public void clear() {
				DBMergeSortedMapOfCollections.this.clear();
			}

			@Override
			public boolean contains(final Object arg0) {
				return DBMergeSortedMapOfCollections.this.containsKey(arg0);
			}

			@Override
			public boolean containsAll(final Collection<?> arg0) {
				for (final Object o : arg0) {
					if (!this.contains(o)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			@Override
			public ParallelIterator<K> iterator() {
				final Iterator<Map.Entry<K, CV>> iter = DBMergeSortedMapOfCollections.this.entrySet().iterator();
				return new ParallelIterator<K>() {
					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public K next() {
						return iter.next().getKey();
					}

					@Override
					public void remove() {
						iter.remove();
					}

					@Override
					public void finalize() {
						this.close();
					}

					@Override
					public void close() {
						if (iter instanceof ParallelIterator) {
							((ParallelIterator) iter).close();
						}
					}
				};
			}

			@Override
			public boolean remove(final Object arg0) {
				return DBMergeSortedMapOfCollections.this.remove(arg0) != null;
			}

			@Override
			public boolean removeAll(final Collection<?> arg0) {
				boolean result = true;
				for (final Object o : arg0) {
					result = this.remove(o) && result;
				}
				return result;
			}

			@Override
			public boolean retainAll(final Collection<?> arg0) {
				throw (new UnsupportedOperationException(
						"This operation is not supported."));
			}

			@Override
			public int size() {
				return DBMergeSortedMapOfCollections.this.size();
			}

			@Override
			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			@Override
			public <T> T[] toArray(final T[] arg0) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}
		};
	}

	@Override
	public CV put(final K key, final CV values) {
		// Do we want to remove the old values when inserting a new
		// collection?
		// If we don't, remove the following line.
		final CV result = this.remove(key);
		for (final V value : values) {
			this.putToCollection(key, value);
		}
		return result;
	}

	@Override
	public void putAll(final Map<? extends K, ? extends CV> t) {
		// Do we want to remove the old values when inserting a new
		// collection?
		// If we don't, remove the following for-loop.
		for (final Map.Entry<? extends K, ? extends CV> entry : t.entrySet()) {
			this.remove(entry.getKey());
		}
		for (final Map.Entry<? extends K, ? extends CV> entry : t.entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public CV remove(final Object key) {
		final MapEntry<K, V> entry = new MapEntry<K, V>((K) key);
		final CV res = this.createCollection();
		Entry<K, V> cur = this.bag.removeAndReturn(entry);
		while (cur != null) {
			res.add(cur.getValue());
			cur = this.bag.removeAndReturn(entry);
		}
		return res;
	}

	@Override
	public int size() {
		if (this.size < 0) {
			this.size = this.values().size();
		}
		return this.size;
	}

	@Override
	public Collection<CV> values() {
		return new Collection<CV>() {
			private int size = -1;

			@Override
			public boolean add(final CV o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean addAll(final Collection<? extends CV> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public void clear() {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean contains(final Object o) {
				for (final CV col : this) {
					if (!col.equals(o)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				for (final Object o : c) {
					if (!this.contains(o)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			@Override
			public ParallelIterator<CV> iterator() {
				final Iterator<Map.Entry<K, CV>> iter = DBMergeSortedMapOfCollections.this.entrySet().iterator();
				return new ParallelIterator<CV>() {
					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public CV next() {
						return iter.next().getValue();
					}

					@Override
					public void remove() {
						throw (new UnsupportedOperationException(
								"Remove not implemented."));
					}

					@Override
					public void finalize() {
						this.close();
					}

					@Override
					public void close() {
						if (iter instanceof ParallelIterator) {
							((ParallelIterator) iter).close();
						}
					}
				};
			}

			@Override
			public boolean remove(final Object o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean removeAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			@SuppressWarnings("unused")
			public int size() {
				if (this.size < 0) {
					this.size = 0;
					for (final CV col : this) {
						this.size++;
					}
				}
				return this.size;
			}

			@Override
			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			@Override
			public <T> T[] toArray(final T[] a) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

		};
	}

	@Override
	public boolean containsValueInCollections(final Object arg0) {
		return this.bag.contains(arg0);
	}

	@Override
	public void putAllIntoCollections(final Map<? extends K, ? extends CV> arg0) {
		for (final K key : arg0.keySet()) {
			for (final V value : arg0.get(key)) {
				this.putToCollection(key, value);
			}
		}
	}

	@Override
	public void putToCollection(final K key, final V value) {
		this.bag.add(new MapEntry<K, V>(key, value));
	}

	@Override
	public boolean removeFromCollection(final K key, final V value) {
		return this.bag.remove(new MapEntry<K, V>(key, value));
	}

	@Override
	public int sizeOfElementsInCollections() {
		return this.bag.size();
	}

	@Override
	public Collection<V> valuesInCollections() {
		return new Collection<V>() {

			@Override
			public boolean add(final V o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean addAll(final Collection<? extends V> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public void clear() {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean contains(final Object o) {
				for (final Entry<K, V> entry : DBMergeSortedMapOfCollections.this) {
					if (entry.getKey().equals(o)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean containsAll(final Collection<?> c) {
				for (final Object o : c) {
					if (!this.contains(o)) {
						return false;
					}
				}
				return true;
			}

			@Override
			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			@Override
			public ParallelIterator<V> iterator() {
				final Iterator<MapEntry<K, V>> iter = DBMergeSortedMapOfCollections.this
						.iterator();
				return new ParallelIterator<V>() {
					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public V next() {
						return iter.next().getValue();
					}

					@Override
					public void remove() {
						iter.remove();
					}

					@Override
					public void finalize() {
						this.close();
					}

					@Override
					public void close() {
						if (iter instanceof ParallelIterator) {
							((ParallelIterator) iter).close();
						}
					}
				};
			}

			@Override
			public boolean remove(final Object o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean removeAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public boolean retainAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@Override
			public int size() {
				return DBMergeSortedMapOfCollections.this.bag.size();
			}

			@Override
			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			@Override
			public <T> T[] toArray(final T[] a) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

		};
	}

	@Override
	public Iterator<V> valuesInCollectionsIterator() {
		return this.valuesInCollections().iterator();
	}

	@Override
	public Iterator<MapEntry<K, V>> iterator() {
		return this.bag.iterator();
	}

	public void release() {
		if (this.bag != null) {
			this.bag.release();
		}
	}
}
