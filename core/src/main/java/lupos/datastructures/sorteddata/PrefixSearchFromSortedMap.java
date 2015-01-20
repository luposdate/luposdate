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
package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.paged_dbbptree.PrefixSearchMinMax;
import lupos.misc.util.ImmutableIterator;

public class PrefixSearchFromSortedMap<K extends Comparable<K>, V> implements
		PrefixSearchMinMax<K, V> {

	private final SortedMap<K, V> sm;

	public PrefixSearchFromSortedMap(final SortedMap<K, V> sm) {
		this.sm = Collections.synchronizedSortedMap(sm);
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0) {
		return new ImmutableIterator<V>() {

			Object[] entries = PrefixSearchFromSortedMap.this.sm.entrySet().toArray();
			int index = (this.entries.length == 0) ? 0 : PrefixSearchFromSortedMap.this.search(0,
					this.entries.length - 1, arg0, this.entries);
			V next = null;

			@Override
			public boolean hasNext() {
				if (this.next != null) {
					return true;
				}
				this.next = this.computeNext();
				return (this.next != null);
			}

			@Override
			public V next() {
				if (this.next != null) {
					final V znext = this.next;
					this.next = null;
					return znext;
				}
				return this.computeNext();
			}

			private V computeNext() {
				if (this.index < 0 || this.index >= this.entries.length) {
					return null;
				}
				if (arg0.compareTo(((Entry<K, V>) this.entries[this.index]).getKey()) == 0) {
					return ((Entry<K, V>) this.entries[this.index++]).getValue();
				}
				this.index = -1;
				return null;
			}
		};
	}

	private int search(final int i1, final int i2, final K arg0,
			final Object[] entries) {
		if (i2 == i1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0) {
				return this.searchLeft(i1, arg0, entries);
			} else {
				return -1;
			}
		} else if (i2 - i1 == 1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0) {
				return this.searchLeft(i1, arg0, entries);
			} else if (arg0.compareTo(((Entry<K, V>) entries[i2]).getKey()) == 0) {
				return this.searchLeft(i2, arg0, entries);
			} else {
				return -1;
			}

		} else {
			final int middle = (i1 + i2) / 2;
			final int compare = arg0.compareTo(((Entry<K, V>) entries[middle])
					.getKey());
			if (compare == 0) {
				return this.searchLeft(middle, arg0, entries);
			} else if (compare < 0) {
				return this.search(i1, middle, arg0, entries);
			} else {
				return this.search(middle, i2, arg0, entries);
			}
		}
	}

	private int searchClosest(final int i1, final int i2, final K arg0,
			final Object[] entries) {
		if (i2 == i1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0) {
				return this.searchLeft(i1, arg0, entries);
			} else {
				return i2;
			}
		} else if (i2 - i1 == 1) {
			if (arg0.compareTo(((Entry<K, V>) entries[i1]).getKey()) == 0) {
				return this.searchLeft(i1, arg0, entries);
			} else if (arg0.compareTo(((Entry<K, V>) entries[i2]).getKey()) == 0) {
				return this.searchLeft(i2, arg0, entries);
			} else {
				return i1;
			}

		} else {
			final int middle = (i1 + i2) / 2;
			final int compare = arg0.compareTo(((Entry<K, V>) entries[middle])
					.getKey());
			if (compare == 0) {
				return this.searchLeft(middle, arg0, entries);
			} else if (compare < 0) {
				return this.search(i1, middle, arg0, entries);
			} else {
				return this.search(middle, i2, arg0, entries);
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

	@Override
	public Comparator<? super K> comparator() {
		return this.sm.comparator();
	}

	@Override
	public K firstKey() {
		return this.sm.firstKey();
	}

	@Override
	public SortedMap<K, V> headMap(final K arg0) {
		return this.sm.headMap(arg0);
	}

	@Override
	public K lastKey() {
		return this.sm.lastKey();
	}

	@Override
	public SortedMap<K, V> subMap(final K arg0, final K arg1) {
		return this.sm.subMap(arg0, arg1);
	}

	@Override
	public SortedMap<K, V> tailMap(final K arg0) {
		return this.sm.tailMap(arg0);
	}

	@Override
	public void clear() {
		this.sm.clear();
	}

	@Override
	public boolean containsKey(final Object arg0) {
		return this.sm.containsKey(arg0);
	}

	@Override
	public boolean containsValue(final Object arg0) {
		return this.sm.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return this.sm.entrySet();
	}

	@Override
	public V get(final Object arg0) {
		return this.sm.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.sm.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.sm.keySet();
	}

	@Override
	public V put(final K arg0, final V arg1) {
		return this.sm.put(arg0, arg1);
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> arg0) {
		this.sm.putAll(arg0);
	}

	@Override
	public V remove(final Object arg0) {
		return this.sm.remove(arg0);
	}

	@Override
	public int size() {
		return this.sm.size();
	}

	@Override
	public Collection<V> values() {
		return this.sm.values();
	}

	@Override
	public Object[] getClosestElements(final K arg0) {
		final Object[] entries = this.sm.entrySet().toArray();
		final Object[] closestElements = new Object[2];
		if (entries.length == 0) {
			return null;
		}
		final int index = (entries.length == 0) ? 0 : this.searchClosest(0,
				entries.length - 1, arg0, entries);
		final int indexLeft = this.searchLeftClosest(index, arg0, entries);
		final int indexRight = this.searchRightClosest(index, arg0, entries);
		if (indexLeft < 0
				|| arg0.compareTo(((Entry<K, V>) entries[indexLeft]).getKey()) == 0) {
			// no element is smaller!
			closestElements[0] = null;
		} else {
			closestElements[0] = entries[indexLeft];
		}
		if (indexRight < 0
				|| indexRight >= entries.length
				|| arg0.compareTo(((Entry<K, V>) entries[indexRight]).getKey()) == 0) {
			// no element is bigger!
			closestElements[1] = null;
		} else {
			closestElements[1] = entries[indexRight];
		}
		return closestElements;
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min) {
		// TODO consider min argument really
		return this.prefixSearch(arg0);
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min, final K max) {
		// TODO consider min and max arguments really
		return this.prefixSearch(arg0);
	}

	@Override
	public Iterator<V> prefixSearchMax(final K arg0, final K max) {
		// TODO consider max argument really
		return this.prefixSearch(arg0);
	}
}
