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
package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.paged_dbbptree.PrefixSearchMinMax;

public class PrefixSearchFromSortedMap<K extends Comparable<K>, V> implements
		PrefixSearchMinMax<K, V> {

	private final SortedMap<K, V> sm;

	public PrefixSearchFromSortedMap(final SortedMap<K, V> sm) {
		this.sm = Collections.synchronizedSortedMap(sm);
	}

	public Iterator<V> prefixSearch(final K arg0) {
		return new Iterator<V>() {

			Object[] entries = sm.entrySet().toArray();
			int index = (entries.length == 0) ? 0 : search(0,
					entries.length - 1, arg0, entries);
			V next = null;

			public boolean hasNext() {
				if (next != null)
					return true;
				next = computeNext();
				return (next != null);
			}

			public V next() {
				if (next != null) {
					final V znext = next;
					next = null;
					return znext;
				}
				return computeNext();
			}

			private V computeNext() {
				if (index < 0 || index >= entries.length)
					return null;
				if (arg0.compareTo(((Entry<K, V>) entries[index]).getKey()) == 0) {
					return ((Entry<K, V>) entries[index++]).getValue();
				}
				index = -1;
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	private int search(final int i1, final int i2, final K arg0,
			final Object[] entries) {
		if (i2 == i1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0)
				return searchLeft(i1, arg0, entries);
			else
				return -1;
		} else if (i2 - i1 == 1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0)
				return searchLeft(i1, arg0, entries);
			else if (arg0.compareTo(((Entry<K, V>) entries[i2]).getKey()) == 0)
				return searchLeft(i2, arg0, entries);
			else
				return -1;

		} else {
			final int middle = (i1 + i2) / 2;
			final int compare = arg0.compareTo(((Entry<K, V>) entries[middle])
					.getKey());
			if (compare == 0)
				return searchLeft(middle, arg0, entries);
			else if (compare < 0) {
				return search(i1, middle, arg0, entries);
			} else {
				return search(middle, i2, arg0, entries);
			}
		}
	}

	private int searchClosest(final int i1, final int i2, final K arg0,
			final Object[] entries) {
		if (i2 == i1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0)
				return searchLeft(i1, arg0, entries);
			else
				return i2;
		} else if (i2 - i1 == 1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0)
				return searchLeft(i1, arg0, entries);
			else if (arg0.compareTo(((Entry<K, V>) entries[i2]).getKey()) == 0)
				return searchLeft(i2, arg0, entries);
			else
				return i1;

		} else {
			final int middle = (i1 + i2) / 2;
			final int compare = arg0.compareTo(((Entry<K, V>) entries[middle])
					.getKey());
			if (compare == 0)
				return searchLeft(middle, arg0, entries);
			else if (compare < 0) {
				return search(i1, middle, arg0, entries);
			} else {
				return search(middle, i2, arg0, entries);
			}
		}
	}

	private int searchLeft(int i1, final K arg0, final Object[] entries) {
		while (i1 > 0
				&& arg0.compareTo(((Entry<K, V>) entries[i1 - 1]).getKey()) == 0) {
			i1--;
		}
		return i1;
	}

	private int searchLeftClosest(int i1, final K arg0, final Object[] entries) {
		while (i1 > 0
				&& arg0.compareTo(((Entry<K, V>) entries[i1 - 1]).getKey()) > 0) {
			i1--;
		}
		return i1;
	}

	private int searchRightClosest(int i1, final K arg0, final Object[] entries) {
		while (i1 < entries.length - 2
				&& arg0.compareTo(((Entry<K, V>) entries[i1 + 1]).getKey()) < 0) {
			i1++;
		}
		return i1;
	}

	public Comparator<? super K> comparator() {
		return sm.comparator();
	}

	public K firstKey() {
		return sm.firstKey();
	}

	public SortedMap<K, V> headMap(final K arg0) {
		return sm.headMap(arg0);
	}

	public K lastKey() {
		return sm.lastKey();
	}

	public SortedMap<K, V> subMap(final K arg0, final K arg1) {
		return sm.subMap(arg0, arg1);
	}

	public SortedMap<K, V> tailMap(final K arg0) {
		return sm.tailMap(arg0);
	}

	public void clear() {
		sm.clear();
	}

	public boolean containsKey(final Object arg0) {
		return sm.containsKey(arg0);
	}

	public boolean containsValue(final Object arg0) {
		return sm.containsValue(arg0);
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return sm.entrySet();
	}

	public V get(final Object arg0) {
		return sm.get(arg0);
	}

	public boolean isEmpty() {
		return sm.isEmpty();
	}

	public Set<K> keySet() {
		return sm.keySet();
	}

	public V put(final K arg0, final V arg1) {
		return sm.put(arg0, arg1);
	}

	public void putAll(final Map<? extends K, ? extends V> arg0) {
		sm.putAll(arg0);
	}

	public V remove(final Object arg0) {
		return sm.remove(arg0);
	}

	public int size() {
		return sm.size();
	}

	public Collection<V> values() {
		return sm.values();
	}

	public Object[] getClosestElements(final K arg0) {
		final Object[] entries = sm.entrySet().toArray();
		final Object[] closestElements = new Object[2];
		if (entries.length == 0)
			return null;
		final int index = (entries.length == 0) ? 0 : searchClosest(0,
				entries.length - 1, arg0, entries);
		final int indexLeft = searchLeftClosest(index, arg0, entries);
		final int indexRight = searchRightClosest(index, arg0, entries);
		if (indexLeft < 0
				|| arg0.compareTo(((Entry<K, V>) entries[indexLeft]).getKey()) == 0) {
			// no element is smaller!
			closestElements[0] = null;
		} else
			closestElements[0] = entries[indexLeft];
		if (indexRight < 0
				|| indexRight >= entries.length
				|| arg0.compareTo(((Entry<K, V>) entries[indexRight]).getKey()) == 0) {
			// no element is bigger!
			closestElements[1] = null;
		} else
			closestElements[1] = entries[indexRight];
		return closestElements;
	}

	public Iterator<V> prefixSearch(final K arg0, final K min) {
		// TODO consider min argument really
		return prefixSearch(arg0);
	}

	public Iterator<V> prefixSearch(final K arg0, final K min, final K max) {
		// TODO consider min and max arguments really
		return prefixSearch(arg0);
	}

	public Iterator<V> prefixSearchMax(final K arg0, final K max) {
		// TODO consider max argument really
		return prefixSearch(arg0);
	}
}
