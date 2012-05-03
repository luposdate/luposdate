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
			final int heapHeight,
			final Class<? extends MapEntry<K, V>> classOfElements) {
		this.collectionClass = collectionClass;
		bag = new DBMergeSortedBag<MapEntry<K, V>>(heapHeight, classOfElements);
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
			final int heapHeight, final Comparator<? super K> comp,
			final Class<? extends MapEntry<K, V>> classOfElements) {
		if (comp != null)
			this.comp = comp;
		else
			this.comp = new Comparator<K>() {
				public int compare(final K arg0, final K arg1) {
					return ((Comparable<K>) arg0).compareTo(arg1);
				}
			};
		this.collectionClass = collectionClass;
		final Comparator<Entry<K, V>> compa = new Comparator<Entry<K, V>>() {
			public int compare(final java.util.Map.Entry<K, V> e1,
					final java.util.Map.Entry<K, V> e2) {
				return comp.compare(e1.getKey(), e2.getKey());
			}
		};
		bag = new DBMergeSortedBag<MapEntry<K, V>>(heapHeight, compa,
				classOfElements);
	}

	private DBMergeSortedMapOfCollections(
			final DBMergeSortedBag<MapEntry<K, V>> bag) {
		this.bag = bag;
	}

	@SuppressWarnings("unchecked")
	protected CV createCollection() {
		try {
			return (CV) collectionClass.newInstance();
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Comparator<? super K> comparator() {
		return comp;
	}

	public K firstKey() {
		return bag.first().getKey();
	}

	public SortedMap<K, CV> headMap(final K toKey) {
		return new DBMergeSortedMapOfCollections<K, V, CV>(bag
				.headBag(new MapEntry<K, V>(toKey)));
	}

	public K lastKey() {
		return bag.last().getKey();
	}

	public SortedMap<K, CV> subMap(final K fromKey, final K toKey) {
		return new DBMergeSortedMapOfCollections<K, V, CV>(bag.subBag(
				new MapEntry<K, V>(fromKey), new MapEntry<K, V>(toKey)));
	}

	public SortedMap<K, CV> tailMap(final K fromKey) {
		return new DBMergeSortedMapOfCollections<K, V, CV>(bag
				.tailBag(new MapEntry<K, V>(fromKey)));
	}

	public void clear() {
		bag.clear();
	}

	public boolean containsKey(final Object key) {
		for (final K k : keySet())
			if (k.equals(key))
				return true;
		return false;
	}

	public boolean containsValue(final Object value) {
		return values().contains(value);
	}

	public Set<java.util.Map.Entry<K, CV>> entrySet() {
		return new Set<Map.Entry<K, CV>>() {
			public boolean add(final java.util.Map.Entry<K, CV> o) {
				put(o.getKey(), o.getValue());
				return true;
			}

			public boolean addAll(
					final Collection<? extends java.util.Map.Entry<K, CV>> c) {
				for (final Map.Entry<K, CV> entry : c) {
					add(entry);
				}
				return true;
			}

			public void clear() {
				DBMergeSortedMapOfCollections.this.clear();
			}

			public boolean contains(final Object o) {
				final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
				return containsKey(entry.getKey())
						&& get(entry.getKey()).equals(entry.getValue());
			}

			public boolean containsAll(final Collection<?> c) {
				for (final Object o : c)
					if (!contains(o))
						return false;
				return true;
			}

			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			public ParallelIterator<java.util.Map.Entry<K, CV>> iterator() {
				final Iterator<MapEntry<K, V>> iter = DBMergeSortedMapOfCollections.this
						.iterator();
				return new ParallelIterator<Map.Entry<K, CV>>() {
					Entry<K, V> next = null;

					public boolean hasNext() {
						return next != null || iter.hasNext();
					}

					public Map.Entry<K, CV> next() {
						if (next == null)
							next = iter.next();
						Entry<K, V> cur = next;
						final CV col = createCollection();
						while (next != null
								&& cur.getKey().equals(next.getKey())) {
							cur = next;
							col.add(cur.getValue());
							if (iter.hasNext())
								next = iter.next();
							else
								next = null;
						}
						return new MapEntry<K, CV>(cur.getKey(), col);
					}

					public void remove() {
						throw (new UnsupportedOperationException(
								"Remove not implemented."));
					}

					@Override
					public void finalize() {
						close();
					}

					public void close() {
						if (iter instanceof ParallelIterator) {
							((ParallelIterator) iter).close();
						}
					}
				};

			}

			public boolean remove(final Object o) {
				return DBMergeSortedMapOfCollections.this
						.remove(((Map.Entry<?, ?>) o).getKey()) != null;
			}

			public boolean removeAll(final Collection<?> c) {
				boolean result = true;
				for (final Object o : c)
					result = remove(o) && result;
				return result;
			}

			public boolean retainAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This operation is not supported."));
			}

			public int size() {
				return DBMergeSortedMapOfCollections.this.size();
			}

			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			public <T> T[] toArray(final T[] a) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

		};
	}

	public CV get(final Object key) {
		for (final Map.Entry<K, CV> entry : entrySet()) {
			if (entry.getKey().equals(key))
				return entry.getValue();
		}
		return null;
	}

	public boolean isEmpty() {
		return bag.isEmpty();
	}

	public Set<K> keySet() {
		return new Set<K>() {
			public boolean add(final K arg0) {
				throw (new UnsupportedOperationException(
						"Can't add key without a value"));
			}

			public boolean addAll(final Collection<? extends K> arg0) {
				throw (new UnsupportedOperationException(
						"Can't add key without a value"));
			}

			public void clear() {
				DBMergeSortedMapOfCollections.this.clear();
			}

			public boolean contains(final Object arg0) {
				return DBMergeSortedMapOfCollections.this.containsKey(arg0);
			}

			public boolean containsAll(final Collection<?> arg0) {
				for (final Object o : arg0) {
					if (!contains(o))
						return false;
				}
				return true;
			}

			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			public ParallelIterator<K> iterator() {
				final Iterator<Map.Entry<K, CV>> iter = entrySet().iterator();
				return new ParallelIterator<K>() {
					public boolean hasNext() {
						return iter.hasNext();
					}

					public K next() {
						return iter.next().getKey();
					}

					public void remove() {
						iter.remove();
					}

					@Override
					public void finalize() {
						close();
					}

					public void close() {
						if (iter instanceof ParallelIterator) {
							((ParallelIterator) iter).close();
						}
					}
				};
			}

			public boolean remove(final Object arg0) {
				return DBMergeSortedMapOfCollections.this.remove(arg0) != null;
			}

			public boolean removeAll(final Collection<?> arg0) {
				boolean result = true;
				for (final Object o : arg0)
					result = remove(o) && result;
				return result;
			}

			public boolean retainAll(final Collection<?> arg0) {
				throw (new UnsupportedOperationException(
						"This operation is not supported."));
			}

			public int size() {
				return DBMergeSortedMapOfCollections.this.size();
			}

			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			public <T> T[] toArray(final T[] arg0) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}
		};
	}

	public CV put(final K key, final CV values) {
		// TODO: Do we want to remove the old values when inserting a new
		// collection?
		// If we don't, remove the following line.
		final CV result = remove(key);
		for (final V value : values) {
			putToCollection(key, value);
		}
		return result;
	}

	public void putAll(final Map<? extends K, ? extends CV> t) {
		// TODO: Do we want to remove the old values when inserting a new
		// collection?
		// If we don't, remove the following for-loop.
		for (final Map.Entry<? extends K, ? extends CV> entry : t.entrySet()) {
			remove(entry.getKey());
		}
		for (final Map.Entry<? extends K, ? extends CV> entry : t.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	public CV remove(final Object key) {
		final MapEntry<K, V> entry = new MapEntry<K, V>((K) key);
		final CV res = createCollection();
		Entry<K, V> cur = bag.removeAndReturn(entry);
		while (cur != null) {
			res.add(cur.getValue());
			cur = bag.removeAndReturn(entry);
		}
		return res;
	}

	public int size() {
		if (size < 0)
			size = values().size();
		return size;
	}

	public Collection<CV> values() {
		return new Collection<CV>() {
			private int size = -1;

			public boolean add(final CV o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean addAll(final Collection<? extends CV> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public void clear() {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean contains(final Object o) {
				for (final CV col : this) {
					if (!col.equals(o))
						return false;
				}
				return true;
			}

			public boolean containsAll(final Collection<?> c) {
				for (final Object o : c) {
					if (!contains(o))
						return false;
				}
				return true;
			}

			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			public ParallelIterator<CV> iterator() {
				final Iterator<Map.Entry<K, CV>> iter = entrySet().iterator();
				return new ParallelIterator<CV>() {
					public boolean hasNext() {
						return iter.hasNext();
					}

					public CV next() {
						return iter.next().getValue();
					}

					public void remove() {
						throw (new UnsupportedOperationException(
								"Remove not implemented."));
					}

					@Override
					public void finalize() {
						close();
					}

					public void close() {
						if (iter instanceof ParallelIterator) {
							((ParallelIterator) iter).close();
						}
					}
				};
			}

			public boolean remove(final Object o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean removeAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean retainAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			@SuppressWarnings("unused")
			public int size() {
				if (size < 0) {
					size = 0;
					for (final CV col : this)
						size++;
				}
				return size;
			}

			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			public <T> T[] toArray(final T[] a) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

		};
	}

	public boolean containsValueInCollections(final Object arg0) {
		return bag.contains(arg0);
	}

	public void putAllIntoCollections(final Map<? extends K, ? extends CV> arg0) {
		for (final K key : arg0.keySet()) {
			for (final V value : arg0.get(key)) {
				putToCollection(key, value);
			}
		}
	}

	public void putToCollection(final K key, final V value) {
		bag.add(new MapEntry<K, V>(key, value));
	}

	public boolean removeFromCollection(final K key, final V value) {
		return bag.remove(new MapEntry<K, V>(key, value));
	}

	public int sizeOfElementsInCollections() {
		return bag.size();
	}

	public Collection<V> valuesInCollections() {
		return new Collection<V>() {

			public boolean add(final V o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean addAll(final Collection<? extends V> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public void clear() {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean contains(final Object o) {
				for (final Entry<K, V> entry : DBMergeSortedMapOfCollections.this) {
					if (entry.getKey().equals(o))
						return true;
				}
				return false;
			}

			public boolean containsAll(final Collection<?> c) {
				for (final Object o : c) {
					if (!contains(o))
						return false;
				}
				return true;
			}

			public boolean isEmpty() {
				return DBMergeSortedMapOfCollections.this.isEmpty();
			}

			public ParallelIterator<V> iterator() {
				final Iterator<MapEntry<K, V>> iter = DBMergeSortedMapOfCollections.this
						.iterator();
				return new ParallelIterator<V>() {
					public boolean hasNext() {
						return iter.hasNext();
					}

					public V next() {
						return iter.next().getValue();
					}

					public void remove() {
						iter.remove();
					}

					@Override
					public void finalize() {
						close();
					}

					public void close() {
						if (iter instanceof ParallelIterator)
							((ParallelIterator) iter).close();
					}
				};
			}

			public boolean remove(final Object o) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean removeAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public boolean retainAll(final Collection<?> c) {
				throw (new UnsupportedOperationException(
						"This Collection is ReadOnly."));
			}

			public int size() {
				return bag.size();
			}

			public Object[] toArray() {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

			public <T> T[] toArray(final T[] a) {
				throw (new UnsupportedOperationException(
						"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
			}

		};
	}

	public Iterator<V> valuesInCollectionsIterator() {
		return valuesInCollections().iterator();
	}

	public Iterator<MapEntry<K, V>> iterator() {
		return bag.iterator();
	}

	public void release() {
		if (bag != null)
			bag.release();
	}
}
