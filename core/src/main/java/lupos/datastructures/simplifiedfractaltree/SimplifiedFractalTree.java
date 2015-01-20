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
package lupos.datastructures.simplifiedfractaltree;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import lupos.datastructures.dbmergesortedds.heap.SequentialHeap;
import lupos.datastructures.paged_dbbptree.PrefixSearchMinMax;
import lupos.datastructures.simplifiedfractaltree.buffermanager.BufferedList;
import lupos.datastructures.simplifiedfractaltree.buffermanager.BufferedList_LuposSerialization;
import lupos.io.Registration;
import lupos.io.Registration.DeSerializerConsideringSubClasses;
import lupos.io.serializer.FRACTALTREEENTRY;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class is an implementation of the simplified fractaltree.
 * @author Denis F�cke
 *
 * @param <K> Key
 * @param <V> Value
 * @see BufferedList_LuposSerialization
 * @see FractalTreeEntry
 */
public class SimplifiedFractalTree<K extends Comparable<K> & Serializable, V extends Serializable> implements SortedMap<K, V>, Serializable,
		PrefixSearchMinMax<K, V> {
	/**
	 * Serial Version ID.
	 */
	protected static final long serialVersionUID = 7099147649283724775L;
	protected File file = new File("bufferedlist");
	protected File file2 = new File("mergelist");
	protected File file3 = new File("removelist");
	protected BufferedList_LuposSerialization<FractalTreeEntry<K, V>> bufferedList;
	protected MergeList mergeList;
	protected BufferedList_LuposSerialization<FractalTreeEntry<K, V>> removeList;
	protected ArrayList<Integer> rCount = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	private static DeSerializerConsideringSubClasses<FractalTreeEntry> fractalTreeEntryDeSerializer = new FRACTALTREEENTRY();
	private static boolean deSerializerAdded = false;

	/**
	 * Must be called before any FractalTree is constructed to take any effect
	 * @param fractalTreeEntryDeSerializer
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static<K extends Comparable<K>, V> void setDeSerializerForFractalTreeEntry(final DeSerializerConsideringSubClasses<FractalTreeEntry<K, V>> fractalTreeEntryDeSerializer){
		SimplifiedFractalTree.fractalTreeEntryDeSerializer = (DeSerializerConsideringSubClasses) fractalTreeEntryDeSerializer;
	}

	private static void addDeSerializer(){
		if(!SimplifiedFractalTree.deSerializerAdded){
			Registration.addDeSerializer(SimplifiedFractalTree.fractalTreeEntryDeSerializer);
			SimplifiedFractalTree.deSerializerAdded = true;
		}
	}

	/**
	 * Constructs am empty fractal tree using a default page size of 8192.
	 *
	 * @see BufferedList
	 */
	public SimplifiedFractalTree() {
		SimplifiedFractalTree.addDeSerializer();
		final FractalTreeEntry<K, V> entry = new FractalTreeEntry<>();
		this.bufferedList = new BufferedList_LuposSerialization<FractalTreeEntry<K, V>>(8 * 1024, this.file.getAbsoluteFile(), entry);
		this.mergeList = new MergeList(entry);
		this.removeList = new BufferedList_LuposSerialization<FractalTreeEntry<K, V>>(8 * 1024, this.file3.getAbsoluteFile(), entry);
	}

	/**
	 * Constructs am empty fractal tree using a default page size of 8192.
	 *
	 * @param file a file specifying the name and the storage location on the disk.
	 *
	 * @see BufferedList
	 */
	public SimplifiedFractalTree(final File file) {
		SimplifiedFractalTree.addDeSerializer();
		final FractalTreeEntry<K, V> entry = new FractalTreeEntry<>();
		this.bufferedList = new BufferedList_LuposSerialization<>(8 * 1024, new File(file.getAbsolutePath() + "bl"), entry);
		this.mergeList = new MergeList(entry, file);
		this.removeList = new BufferedList_LuposSerialization<>(8 * 1024, new File(file.getAbsolutePath() + "rl"), entry);
	}

	/**
	 * Constructs am empty fractal tree.
	 *
	 * @param file a file specifying the name and the storage location on the disk.
	 * @param pageSize the size of a page; greater than <tt>0</tt>
	 *
	 * @throws NegativeArraySizeException if pageSize is less or equal <tt>0</tt>
	 *
	 * @see BufferedList
	 */
	public SimplifiedFractalTree(final File file, final int pageSize) {
		assert pageSize > 0;

		SimplifiedFractalTree.addDeSerializer();
		final FractalTreeEntry<K, V> entry = new FractalTreeEntry<>();
		this.bufferedList = new BufferedList_LuposSerialization<>(pageSize, new File(file.getAbsolutePath() + "bl"), entry);
		this.mergeList = new MergeList(entry, pageSize, file);
		this.removeList = new BufferedList_LuposSerialization<>(pageSize, new File(file.getAbsolutePath() + "rl"), entry);
	}

	/**
	 * Constructs am empty fractal tree.
	 *
	 * @param pageSize the size of a page; greater than <tt>0</tt>
	 *
	 * @throws NegativeArraySizeException if pageSize is less or equal <tt>0</tt>
	 *
	 * @see BufferedList
	 */
	public SimplifiedFractalTree(final int pageSize) {
		assert pageSize > 0;

		SimplifiedFractalTree.addDeSerializer();
		final FractalTreeEntry<K, V> entry = new FractalTreeEntry<>();
		this.bufferedList = new BufferedList_LuposSerialization<>(pageSize, this.file.getAbsoluteFile(), entry);
		this.mergeList = new MergeList(entry, pageSize);
		this.removeList = new BufferedList_LuposSerialization<>(pageSize, this.file3.getAbsoluteFile(), entry);
	}

	@Override
	public void clear() {
		this.bufferedList.clear();
		this.mergeList.clear();
		this.removeList.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsKey(final Object key) {
		assert key != null;

		return this.indexOf(0, (K) key, new FractalTreeEntry<K, V>(), 0) > -1;
	}

	@Override
	public boolean containsValue(final Object value) {
		for (final V v : this.values()) {
			if (v.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(final Object key) {
		assert key != null;
		@SuppressWarnings("unchecked")
		final
		int index = this.indexOf(0, (K) key, new FractalTreeEntry<K, V>(), 0);

		if (index > -1) {
			return this.bufferedList.get(index).value;
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return this.size() == 0;
	}

	@Override
	public V put(final K key, final V value) {
		assert key != null;

		V oldValue = null;
		FractalTreeEntry<K, V> oldEntry = null;
		final int index = this.indexOf(0, key, new FractalTreeEntry<K, V>(), 0);

		if (index >= 0) {
			oldEntry = this.bufferedList.get(index);
			oldValue = oldEntry.value;
			this.bufferedList.setNoReturn(index, new FractalTreeEntry<K, V>(key, value, oldEntry.pointer));
		} else {
			this.merge(new FractalTreeEntry<K, V>(key, value));
		}

		return oldValue;
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> map) {
		for (final K key : map.keySet()) {
			this.put(key, map.get(key));
		}
	}

	@Override
	public V remove(final Object key) {
		assert key != null;

		@SuppressWarnings("unchecked")
		final
		int oldIndex = this.indexOf(0, (K) key, new FractalTreeEntry<K, V>(), 0);
		V oldValue = null;
		if (oldIndex != -1) {
			oldValue = this.bufferedList.get(oldIndex).value;

			// Verschieben in mergelist
			final int imax = this.getNextPosition(oldIndex);
			final int imin = this.getArrayStart(oldIndex);
			int counter = 0;
			for (int j = this.getDeepth(oldIndex); j >= 0; j--) {
				boolean beyondOldElement = false;
				for (int i = (int) (Math.pow(2, j) - 1) + imin; i < imax; i += Math.pow(2, j + 1)) {
					if (!beyondOldElement && i >= oldIndex) {
						if (i == oldIndex) {
							this.bufferedList.set(oldIndex, new FractalTreeEntry<K, V>());
						}
						i++;
						beyondOldElement = true;
					}
					if (i < imax) {
						final FractalTreeEntry<K, V> entry = this.bufferedList.set(i, new FractalTreeEntry<K, V>());
						entry.pointer = -1;
						this.removeList.add(counter, entry);
						counter++;
					}
				}
			}

			// ggf. mergen
			if (this.removeList.size() > 0) {
				this.removeMerge(0);
				if (this.bufferedList.size() == imax) {
					for (; this.mergeList.size() < this.bufferedList.size();) {
						this.bufferedList.remove(this.bufferedList.size() - 1);
					}
				}
				this.copyFromMergeList();
			}
			// mergelist leeren
			this.mergeList.clear();
			this.removeList.clear();
		}
		return oldValue;
	}

	protected int getDeepth(final int index) {
		return (int) Math.floor(Math.log10(index + 1) / Math.log10(2));
	}

	private void copyFromMergeList() {
		final int size = this.mergeList.size();

		for (int i = 0; i < size; i++) {
			if (this.mergeList.get(i).key == null) {
				i = i * 2;
				continue;
			}

			this.bufferedList.set(i, this.mergeList.get(i));
		}

		for (int i = 0; i < size; i = i * 2 + 1) {
			if (this.bufferedList.get(i).key != null && this.bufferedList.get(i).pointer < 0) {
				this.calcPointers(i);
			}
		}
	}

	protected void calcPointers(final int index) {
		if (this.bufferedList.get(index).key == null) {
			return;
		}

		int i = index;
		int j = this.getNextPosition(index);
		final int imax = i * 2 + 1;
		int jmax = j * 2 + 1;
		final int size = this.bufferedList.size();

		if (j >= size) {
			for (; i < imax; i++) {
				final FractalTreeEntry<K, V> entry = this.bufferedList.get(i);
				entry.pointer = -1;
				this.bufferedList.set(i, entry);
			}
			return;
		} else {
			for (; this.bufferedList.get(j).key == null;) {
				j = j * 2 + 1;
				jmax = j * 2 + 1;
			}
		}

		for (; i < imax; i++) {
			final FractalTreeEntry<K, V> entry = this.bufferedList.get(i);

			for (;;) {
				if (j + 1 < jmax && entry.key.compareTo(this.bufferedList.get(j).key) >= 0) {
					j++;
				} else {
					entry.pointer = j;
					break;
				}
			}
			this.bufferedList.set(i, entry);
		}
	}

	private void removeMerge(final int index) {
		if (this.bufferedList.get(index).key == null && this.mergeList.size() == index) { // fall 1
			final int imax = index * 2 + 1;
			for (int i = index; i < imax; i++) {
				this.mergeList.add(this.removeList.get(i));
			}
		} else if (this.bufferedList.get(index).key != null && this.mergeList.size() == index * 2 + 1) { // fall 3
			int b = index;
			int m = index;
			final int imax = 2 * index;
			for (;;) {
				if (b > imax && m > imax) {
					break;
				}
				if (b <= imax && m <= imax) {
					final FractalTreeEntry<K, V> bEntry = this.bufferedList.get(b);
					bEntry.pointer = -1;
					final FractalTreeEntry<K, V> mEntry = this.removeList.get(m);
					mEntry.pointer = -1;
					if (bEntry.key.compareTo(mEntry.key) < 0) {
						this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
						this.mergeList.add(bEntry);
						b++;
					} else {
						this.removeList.setNoReturn(m, new FractalTreeEntry<K, V>());
						this.mergeList.add(mEntry);
						m++;
					}
				} else {
					if (b <= imax) {
						this.mergeList.add(this.bufferedList.set(b, new FractalTreeEntry<K, V>()));
						b++;
					}
					if (m <= imax) {
						this.mergeList.add(this.removeList.set(m, new FractalTreeEntry<K, V>()));
						m++;
					}
				}
			}
		} else if (this.bufferedList.get(index).key != null && this.mergeList.size() == index) { // fall 2
			for (; this.mergeList.size() < index * 2 + 1;) {
				this.mergeList.add(new FractalTreeEntry<K, V>());
			}

			int b = index;
			int m = index;
			final int imax = 2 * index;
			for (;;) {
				if (b > imax && m > imax) {
					break;
				}
				if (b <= imax && m <= imax) {
					final FractalTreeEntry<K, V> bEntry = this.bufferedList.get(b);
					bEntry.pointer = -1;
					final FractalTreeEntry<K, V> mEntry = this.removeList.get(m);
					mEntry.pointer = -1;
					if (bEntry.key.compareTo(mEntry.key) < 0) {
						this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
						this.mergeList.add(bEntry);
						b++;
					} else {
						this.removeList.setNoReturn(m, new FractalTreeEntry<K, V>());
						this.mergeList.add(mEntry);
						m++;
					}
				} else {
					if (b <= imax) {
						this.mergeList.add(this.bufferedList.set(b, new FractalTreeEntry<K, V>()));
						b++;
					}
					if (m <= imax) {
						this.mergeList.add(this.removeList.set(m, new FractalTreeEntry<K, V>()));
						m++;
					}
				}
			}
			// printMergelist();
		} else if (this.bufferedList.get(index).key == null && this.mergeList.size() == index * 2 + 1) {
			int b = index;
			int m = index;
			final int imax = 2 * index;
			for (;;) {
				if (b > imax && m > imax) {
					break;
				}
				if (b <= imax && m <= imax) {
					final FractalTreeEntry<K, V> bEntry = this.removeList.get(b);
					bEntry.pointer = -1;
					final FractalTreeEntry<K, V> mEntry = this.mergeList.get(m);
					mEntry.pointer = -1;
					if (bEntry.key.compareTo(mEntry.key) < 0) {
						this.removeList.setNoReturn(b, new FractalTreeEntry<K, V>());
						this.mergeList.add(bEntry);
						b++;
					} else {
						this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
						this.mergeList.add(mEntry);
						m++;
					}
				} else {
					if (b <= imax) {
						this.mergeList.add(this.removeList.set(b, new FractalTreeEntry<K, V>()));
						b++;
					}
					if (m <= imax) {
						this.mergeList.add(this.mergeList.set(m, new FractalTreeEntry<K, V>()));
						m++;
					}
				}
			}
		} else {
			System.out.println("Error");
		}
		// print();
		if (index * 2 + 1 < this.removeList.size()) {
			this.removeMerge(index * 2 + 1);
		}
	}

	protected int getHeigth(final int size) {
		if(size == 0) {
			return 0;
		}
		return (int) Math.ceil(Math.log10(size) / Math.log10(2));
	}

	@Override
	public int size() {
		int index = 0;
		int size = 0;
		for (;;) {
			if (this.bufferedList.size() > index) {
				if (this.bufferedList.get(index).key != null) {
					size += index + 1;
					index = index * 2 + 1;
				} else {
					index = index * 2 + 1;
				}
			} else {
				return size;
			}
		}
	}

	@Override
	public Object[] getClosestElements(final K key) {
		assert key != null;

		final ArrayList<K> closestKeys = new ArrayList<>();

		return this.getClosestElements(0, key, closestKeys).toArray();
	}

	private ArrayList<K> getClosestElements(final int index, final K key, ArrayList<K> closestKeys) {
		K closest = null;
		if (closestKeys.size() > 0) {
			closest = closestKeys.get(0);
		}
		if (this.isCloserThanClosestKey(closest, key, this.bufferedList.get(index).key)) {
			closestKeys.clear();
			closestKeys.add(this.bufferedList.get(index).key);
		} else if (this.isEqualToClosestKey(closest, this.bufferedList.get(index).key)) {
			closestKeys.add(this.bufferedList.get(index).key);
		}

		final int imin = this.getArrayStart(index);
		final int imax = this.getNextPosition(index) - 1;

		final int pointer = -1;
		if (index + 1 <= imax) {
			closestKeys = this.getClosestElementRight(index + 1, key, closestKeys, pointer);
		}
		if (index - 1 >= imin) {
			closestKeys = this.getClosestElementLeft(index - 1, key, closestKeys, pointer);
		}

		if (pointer != -1) {
			closestKeys = this.getClosestElements(pointer, key, closestKeys);
		} else if (this.bufferedList.get(index).pointer != -1) {
			closestKeys = this.getClosestElements(this.bufferedList.get(index).pointer, key, closestKeys);
		}

		return closestKeys;
	}

	private ArrayList<K> getClosestElementRight(final int index, final K key, ArrayList<K> closestKeys, int pointer) {
		K closest = null;
		if (closestKeys.size() > 0) {
			closest = closestKeys.get(0);
		}
		if (this.isCloserThanClosestKey(closest, key, this.bufferedList.get(index).key)) {
			closestKeys.clear();
			closestKeys.add(this.bufferedList.get(index).key);
			pointer = this.bufferedList.get(index).pointer;
		} else if (this.isEqualToClosestKey(closest, this.bufferedList.get(index).key)) {
			closestKeys.add(this.bufferedList.get(index).key);
			pointer = this.bufferedList.get(index).pointer;
		} else if (this.isNotClosestKey(closest, key, this.bufferedList.get(index).key)) {
			pointer = this.bufferedList.get(index - 1).pointer;
		}

		final int imax = this.getNextPosition(index) - 1;

		if (index + 1 <= imax) {
			closestKeys = this.getClosestElementRight(index + 1, key, closestKeys, pointer);
		}

		return closestKeys;
	}

	private boolean isNotClosestKey(final K closest, final K key, final K newKey) {
		if (closest.compareTo(key) > 0) {
			if (newKey.compareTo(newKey) > 0) {
				return true;
			}
		} else if (closest.compareTo(key) < 0) {
			if (newKey.compareTo(newKey) < 0) {
				return true;
			}
		}
		return false;
	}

	private ArrayList<K> getClosestElementLeft(final int index, final K key, ArrayList<K> closestKeys, int pointer) {
		K closest = null;
		if (closestKeys.size() > 0) {
			closest = closestKeys.get(0);
		}
		if (this.isCloserThanClosestKey(closest, key, this.bufferedList.get(index).key)) {
			closestKeys.clear();
			closestKeys.add(this.bufferedList.get(index).key);
			pointer = this.bufferedList.get(index).pointer;
		} else if (this.isEqualToClosestKey(closest, this.bufferedList.get(index).key)) {
			closestKeys.add(this.bufferedList.get(index).key);
			pointer = this.bufferedList.get(index).pointer;
		} else if (this.isNotClosestKey(closest, key, this.bufferedList.get(index).key)) {
			pointer = this.bufferedList.get(index + 1).pointer;
		}

		final int imin = this.getArrayStart(index);

		if (index - 1 >= imin) {
			closestKeys = this.getClosestElementLeft(index - 1, key, closestKeys, pointer);
		}

		return closestKeys;
	}

	private boolean isCloserThanClosestKey(final K closest, final K key, final K newKey) {
		if (closest == null) {
			return true;
		}
		if (key.compareTo(newKey) != 0) {
			if (key.compareTo(newKey) < 0) {
				if (closest.compareTo(newKey) > 0) {
					return true;
				}
			} else if (key.compareTo(newKey) > 0) {
				if (closest.compareTo(newKey) < 0) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isEqualToClosestKey(final K closest, final K newKey) {
		return closest.compareTo(newKey) == 0;
	}

	/**
	 * Searches all values whose corresponded entrys matching the specified prefix. Returns an Iterator of these values.
	 *
	 * <p>
	 * The user must ensure <tt>prefix.compareTo(anOtherKey) == 0</tt> if the prefix is matching another key by implementing a class that allows to
	 * specify a <tt>Comparator</tt> that is used for the implementation of the <tt>compareTo</tt> method.
	 *
	 * @param prefix a prefix
	 * @return an Iterator containing all values whose entrys having the specified prefix.
	 *
	 * @throws NullPointerException if the specified key is null
	 *
	 * @see Comparator
	 * @see Comparable
	 */
	@Override
	public Iterator<V> prefixSearch(final K prefix) {
		assert prefix != null;

		this.heap = new SequentialHeap<>(this.getDeepth(this.bufferedList.size()));

		this.prefixSearch(0, prefix, null, null);
		return new PrefixIterator(prefix, null, null);
	}

	/**
	 * Searches all values whose corresponded entrys matching the specified prefix and minimum. Returns an Iterator of these values.
	 *
	 * <p>
	 * The user must ensure <tt>prefix.compareTo(anOtherKey) == 0</tt> if the prefix is matching another key by implementing a class that allows to
	 * specify a <tt>Comparator</tt> that is used for the implementation of the <tt>compareTo</tt> method.
	 *
	 * @param prefix a prefix
	 * @param min a key specifying a minimum
	 * @return an Iterator containing all values whose entrys having the specified prefix.
	 *
	 * @throws NullPointerException if the specified key is null
	 *
	 * @see Comparator
	 * @see Comparable
	 */
	@Override
	public Iterator<V> prefixSearch(final K prefix, final K min) {
		assert prefix != null;
		assert min != null;

		this.heap = new SequentialHeap<>(this.getDeepth(this.bufferedList.size()));

		this.prefixSearch(0, prefix, min, null);
		return new PrefixIterator(prefix, min, null);
	}

	/**
	 * Searches all values whose corresponded entrys matching the specified prefix, minimum and maximum. Returns an Iterator of these values.
	 *
	 * <p>
	 * The user must ensure <tt>prefix.compareTo(anOtherKey) == 0</tt> if the prefix is matching another key by implementing a class that allows to
	 * specify a <tt>Comparator</tt> that is used for the implementation of the <tt>compareTo</tt> method.
	 *
	 * @param prefix a prefix
	 * @param min a key specifying a minimum
	 * @param max a key specifying a maximum
	 * @return an Iterator containing all values whose entrys having the specified prefix.
	 *
	 * @throws NullPointerException if the specified key is null
	 *
	 * @see Comparator
	 * @see Comparable
	 */
	@Override
	public Iterator<V> prefixSearch(final K prefix, final K min, final K max) {
		assert prefix != null;
		assert min != null;
		assert max != null;

		this.heap = new SequentialHeap<>(this.getDeepth(this.bufferedList.size()));

		this.prefixSearch(0, prefix, min, max);
		return new PrefixIterator(prefix, null, max);
	}

	/**
	 * Searches all values whose corresponded entrys matching the specified prefix and maximum. Returns an Iterator of these values.
	 *
	 * <p>
	 * The user must ensure <tt>prefix.compareTo(anOtherKey) == 0</tt> if the prefix is matching another key by implementing a class that allows to
	 * specify a <tt>Comparator</tt> that is used for the implementation of the <tt>compareTo</tt> method.
	 *
	 * @param prefix a prefix
	 * @param max a key specifying a maximum
	 * @return an Iterator containing all values whose entrys having the specified prefix.
	 *
	 * @throws NullPointerException if the specified key is null
	 *
	 * @see Comparator
	 * @see Comparable
	 */
	@Override
	public Iterator<V> prefixSearchMax(final K prefix, final K max) {
		assert prefix != null;
		assert max != null;

		this.heap = new SequentialHeap<>(this.getDeepth(this.bufferedList.size()));

		this.prefixSearch(0, prefix, null, max);
		return new PrefixIterator(prefix, null, max);
	}

	private class PrefixIterator implements Iterator<V> {
		private final K prefix;
		private final K min;
		private final K max;

		public PrefixIterator(final K prefix, final K min, final K max) {
			this.prefix = prefix;
			this.min = min;
			this.max = max;
		}

		@Override
		public boolean hasNext() {
			return !SimplifiedFractalTree.this.heap.isEmpty();
		}

		@Override
		public V next() {
			final PrefixElement e = SimplifiedFractalTree.this.heap.pop();
			if (e.position + 1 < SimplifiedFractalTree.this.getNextPosition(e.position) && SimplifiedFractalTree.this.prefixCompare(e.position + 1, this.prefix, this.min, this.max) == 0) {
				SimplifiedFractalTree.this.heap.add(new PrefixElement(e.position + 1, SimplifiedFractalTree.this.bufferedList.get(e.position + 1).key));
			}
			return SimplifiedFractalTree.this.bufferedList.get(e.position).value;
		}

		@Override
		public void remove() {
			throw new NotImplementedException();
		}
	}

	@Override
	public Comparator<? super K> comparator() {
		return new Comparator<K>() {
			@Override
			public int compare(final K o1, final K o2) {
				return o1.compareTo(o2);
			}
		};
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	private class SetEntry implements Entry<K, V> {
		K key;
		V value;

		public SetEntry(final K key, final V value) {
			SetEntry.this.value = value;
			SetEntry.this.key = key;
		}

		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}

		@Override
		public V setValue(final V value) {
			final V oldValue = SetEntry.this.value;
			SetEntry.this.value = value;
			return oldValue;
		}

	}

	private class EntrySet implements Set<Map.Entry<K, V>> {
		@Override
		public int size() {
			return SimplifiedFractalTree.this.size();
		}

		@Override
		public void clear() {
			SimplifiedFractalTree.this.clear();
		}

		@Override
		public boolean isEmpty() {
			return SimplifiedFractalTree.this.isEmpty();
		}

		@Override
		public boolean contains(final Object key) {
			return SimplifiedFractalTree.this.containsKey(key);
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public Object[] toArray() {
			throw new NotImplementedException();
		}

		@Override
		public <T> T[] toArray(final T[] a) {
			throw new NotImplementedException();
		}

		@Override
		public boolean add(final java.util.Map.Entry<K, V> entry) {
			SimplifiedFractalTree.this.put(entry.getKey(), entry.getValue());
			return true;
		}

		@Override
		public boolean remove(final Object key) {
			SimplifiedFractalTree.this.remove(key);
			return true;
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			final Iterator<?> iterator = c.iterator();
			for (; iterator.hasNext();) {
				if (!SimplifiedFractalTree.this.containsKey(iterator.next())) {
					return false;
				}
			}
			return false;
		}

		@Override
		public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
			final Iterator<? extends Entry<K, V>> iterator = c.iterator();
			boolean changed = false;
			for (; iterator.hasNext();) {
				final Map.Entry<K, V> entry = iterator.next();
				SimplifiedFractalTree.this.put(entry.getKey(), entry.getValue());
				changed = true;
			}
			return changed;
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			final Iterator<?> iterator = c.iterator();
			for (; iterator.hasNext();) {
				final Object key = iterator.next();
				if (!this.contains(key)) {
					SimplifiedFractalTree.this.remove(key);
				}
			}
			return false;
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			final Iterator<?> iterator = c.iterator();
			boolean changed = false;
			for (; iterator.hasNext();) {
				this.remove(iterator.next());
				changed = true;
			}
			return changed;
		}
	}

	@Override
	public K firstKey() {
		K firstKey = null;
		final int size = this.bufferedList.size();
		for (int next = 0; next < size; next = this.getNextPosition(next)) {
			if (this.bufferedList.get(next).key != null) {
				if (firstKey == null) {
					firstKey = this.bufferedList.get(next).key;
				} else if (this.bufferedList.get(next).key.compareTo(firstKey) < 0) {
					firstKey = this.bufferedList.get(next).key;
				}
			}
		}
		return firstKey;
	}

	private transient ArrayList<FractalTreeEntry<K, V>> entrys = new ArrayList<>();

	private class EntryIterator implements Iterator<Map.Entry<K, V>> {
		private int index = 0;

		public EntryIterator() {
			final int size = SimplifiedFractalTree.this.bufferedList.size();
			for (int i = 0; i < size; i++) {
				if (SimplifiedFractalTree.this.bufferedList.get(i).key != null) {
					SimplifiedFractalTree.this.entrys.add(SimplifiedFractalTree.this.bufferedList.get(i));
				}
			}
			Collections.sort(SimplifiedFractalTree.this.entrys);
		}

		@Override
		public boolean hasNext() {
			return this.index < SimplifiedFractalTree.this.entrys.size();
		}

		@Override
		public Map.Entry<K, V> next() {
			if (this.hasNext()) {
				this.index++;
				return new SetEntry(SimplifiedFractalTree.this.entrys.get(this.index).key, SimplifiedFractalTree.this.entrys.get(this.index).value);
			} else {
				throw new NoSuchElementException();
			}
		}

		@Deprecated
		@Override
		public void remove() {
			throw new NotImplementedException();
		}
	}

	@Override
	public SortedMap<K, V> headMap(final K toKey) {
		throw new NotImplementedException();
	}

	@Override
	public Set<K> keySet() {
		return new KeySet();
	}

	private class KeySet implements Set<K> {
		ArrayList<K> keys = new ArrayList<>();

		public KeySet() {
			final int size = SimplifiedFractalTree.this.bufferedList.size();
			for (int i = 0; i < size; i++) {
				if (SimplifiedFractalTree.this.bufferedList.get(i).key != null) {
					this.keys.add(SimplifiedFractalTree.this.bufferedList.get(i).key);
				}
			}
			Collections.sort(this.keys);
		}

		@Override
		public int size() {
			return this.keys.size();
		}

		@Override
		public boolean isEmpty() {
			return this.keys.isEmpty();
		}

		@Override
		public boolean contains(final Object key) {
			return this.keys.contains(key);
		}

		@Override
		public Iterator<K> iterator() {
			return this.keys.iterator();
		}

		@Override
		public Object[] toArray() {
			return this.keys.toArray();
		}

		@Override
		public <T> T[] toArray(final T[] a) {
			return this.keys.toArray(a);
		}

		@Override
		public boolean add(final K key) {
			throw new NotImplementedException();
		}

		@Override
		public boolean remove(final Object key) {
			this.keys.remove(key);
			SimplifiedFractalTree.this.remove(key);
			return true;
		}

		@Override
		public boolean containsAll(final Collection<?> c) {
			return this.keys.containsAll(c);
		}

		@Override
		public boolean addAll(final Collection<? extends K> c) {
			throw new NotImplementedException();
		}

		@Override
		public boolean retainAll(final Collection<?> c) {
			throw new NotImplementedException();
		}

		@Override
		public boolean removeAll(final Collection<?> c) {
			throw new NotImplementedException();
		}

		@Override
		public void clear() {
			this.keys.clear();
			SimplifiedFractalTree.this.clear();
		}
	}

	@Override
	public K lastKey() {
		K lastKey = null;
		final int size = this.bufferedList.size();
		for (int next = 0; next < size; next = this.getNextPosition(next) * 2) {
			if (this.bufferedList.get(next).key != null) {
				if (lastKey == null) {
					lastKey = this.bufferedList.get(next).key;
				} else if (this.bufferedList.get(next).key.compareTo(lastKey) > 0) {
					lastKey = this.bufferedList.get(next).key;
				}
			}
		}
		return lastKey;
	}

	@Override
	public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
		throw new NotImplementedException();
	}

	@Override
	public SortedMap<K, V> tailMap(final K arg0) {
		throw new NotImplementedException();
	}

	@Override
	public Collection<V> values() {
		final ArrayList<V> values = new ArrayList<>();

		final int size = this.bufferedList.size();
		for (int i = 0; i < size; i++) {
			if (this.bufferedList.get(i).key != null) {
				this.entrys.add(this.bufferedList.get(i));
			}
		}
		Collections.sort(this.entrys);
		for (int i = 0; i < size; i++) {
			values.add(this.entrys.get(0).value);
		}
		return values;
	}

	private void prefixSearch(final int index, final K prefix, final K min, final K max) {
		if (this.bufferedList.size() > index) {
			int nextArrayIndex = index;
			if (this.bufferedList.get(nextArrayIndex).key == null) {
				for (; nextArrayIndex < this.bufferedList.size();) {
					if (this.bufferedList.get(nextArrayIndex).key != null) {
						break;
					} else {
						nextArrayIndex = nextArrayIndex * 2 + 1;
					}
				}
				this.prefixSearch(nextArrayIndex, prefix, min, max);
			} else {
				nextArrayIndex = this.getNextPosition(index);
				if (this.prefixCompare(index, prefix, min, max) >= 0) {
					final int res = this.prefixBinarySearch(this.getArrayStart(index), index, prefix, min, max);
					if (this.prefixCompare(res, prefix, min, max) == 0) {
						this.heap.add(new PrefixElement(res, this.bufferedList.get(res).key));
					}
					if (this.bufferedList.get(res).pointer > -1) {
						this.prefixSearch(this.bufferedList.get(res).pointer, prefix, min, max);
					}
				} else if (this.prefixCompare(index, prefix, min, max) < 0) {
					final int res = this.prefixBinarySearch(index, this.getNextPosition(index) - 1, prefix, min, max);
					if (this.prefixCompare(res, prefix, min, max) == 0) {
						this.heap.add(new PrefixElement(res, this.bufferedList.get(res).key));
					}
					if (this.bufferedList.get(res).pointer > -1) {
						this.prefixSearch(this.bufferedList.get(res).pointer, prefix, min, max);
					}
				}
			}
		}
	}

	/**
	 * Does a binary search for the prefix search.
	 * @param imin The left border of the interval
	 * @param imax The right border of the interval
	 * @param prefix The prefix
	 * @param min A minimum
	 * @param max A maximum
	 * @return The position of the result
	 */
	private int prefixBinarySearch(final int imin, final int imax, final K prefix, final K min, final K max) {
		if (imax == imin) {
			return imax;
		} else {
			final int imid = this.midpoint(imin, imax);
			if (this.prefixCompare(imin, prefix, min, max) == 0) {
				return imin;
			} else if (this.prefixCompare(imid - 1, prefix, min, max) >= 0) {
				return this.prefixBinarySearch(imin, imid - 1, prefix, min, max);
			} else if (this.prefixCompare(imax, prefix, min, max) <= 0) {
				return this.prefixBinarySearch(imid, imax, prefix, min, max);
			} else {
				return imax;
			}
		}
	}

	private transient SequentialHeap<PrefixElement> heap;

	private class PrefixElement implements Comparable<PrefixElement> {
		int position = -1;
		K key;

		public PrefixElement(final int position, final K key) {
			PrefixElement.this.position = position;
			PrefixElement.this.key = key;
		}

		@Override
		public int compareTo(final PrefixElement o) {
			return this.key.compareTo(o.key);
		}
	}

	private int prefixCompare(final int index, final K prefix, final K min, final K max) {
		if (prefix.compareTo(this.bufferedList.get(index).key) > 0) {
			return 1;

		} else if (prefix.compareTo(this.bufferedList.get(index).key) < 0) {
			return -1;
		} else {
			if (min == null && max == null) {
				return 0;
			} else if (min != null && max == null) {
				if (this.bufferedList.get(index).key.compareTo(min) >= 0) {
					return 0;
				}
			} else if (min == null && max != null) {
				if (this.bufferedList.get(index).key.compareTo(max) <= 0) {
					return 0;
				}
			} else if (min != null && max != null) {
				if (this.bufferedList.get(index).key.compareTo(min) >= 0 && this.bufferedList.get(index).key.compareTo(max) <= 0) {
					return 0;
				}
			}
		}
		return 0;
	}

	protected int getNextPosition(final int index) {
		final double a = Math.pow(2, Math.floor(Math.log10(index + 1) / Math.log10(2)));
		final int nextArrayIndex = (int) (((index + 1) - ((index + 1) % a)) * 2 - 1);
		return nextArrayIndex;
	}

	protected int getArrayStart(final int index) {
		if (index == 0) {
			return 0;
		}
		return (this.getNextPosition(index) - 1) / 2;
	}

	public int indexOf(final int index, final K key, FractalTreeEntry<K, V> entry, int nextArrayIndex) {
		if (index >= this.bufferedList.size()) {
			return -1;
		}
		if (this.bufferedList.size() > 0) {
			entry = this.bufferedList.get(index);
			if (entry.key != null && entry.key.compareTo(key) == 0) {
				return index;
			} else {
				nextArrayIndex = index;
				if (this.bufferedList.get(nextArrayIndex).key == null) {
					for (; nextArrayIndex < this.bufferedList.size();) {
						if (this.bufferedList.get(nextArrayIndex).key != null) {
							break;
						} else {
							nextArrayIndex = nextArrayIndex * 2 + 1;
						}
					}
					return this.indexOf(nextArrayIndex, key, entry, nextArrayIndex);
				} else {
					nextArrayIndex = this.getNextPosition(index);
					if (entry.pointer < 0) {
						if (entry.key.compareTo(key) > 0) {
							final int newindex = this.binarySearch(this.getArrayStart(index), index, key);
							if (newindex == -1) {
								return -1;
							} else {
								if (this.bufferedList.get(newindex).key.compareTo(key) == 0) {
									return newindex;
								} else {
									return -1;
								}
							}
						} else {
							final int newindex = this.binarySearch(index, this.getNextPosition(index) - 1, key);
							if (newindex == -1) {
								return -1;
							} else {
								if (this.bufferedList.get(newindex).key.compareTo(key) == 0) {
									return newindex;
								} else {
									return -1;
								}
							}
						}
					} else {
						if (entry.key.compareTo(key) > 0) {
							final int newindex = this.binarySearch(this.getArrayStart(index), index, key);
							if (this.bufferedList.get(newindex).key.compareTo(key) == 0) {
								return newindex;
							} else {
								return this.indexOf(this.bufferedList.get(newindex).pointer, key, entry, nextArrayIndex);
							}
						} else {
							final int newindex = this.binarySearch(index, this.getNextPosition(index) - 1, key);
							if (this.bufferedList.get(newindex).key.compareTo(key) == 0) {
								return newindex;
							} else {
								return this.indexOf(this.bufferedList.get(newindex).pointer, key, entry, nextArrayIndex);
							}
						}
					}
				}
			}
		} else {
			return -1;
		}
	}

	protected int binarySearch(final int imin, final int imax, final K key) {
		if (imax == imin) {
			return imax;
		} else {
			final int imid = this.midpoint(imin, imax);
			if (this.bufferedList.get(imin).key.compareTo(key) >= 0) {
				return imin;
			} else if (this.bufferedList.get(imid - 1).key.compareTo(key) >= 0) {
				return this.binarySearch(imin, imid - 1, key);
			} else if (this.bufferedList.get(imax).key.compareTo(key) >= 0) {
				return this.binarySearch(imid, imax, key);
			} else {
				return imax;
			}
		}
	}

	private int midpoint(final int imin, final int imax) {
		return (int) (imin + Math.ceil((double) (imax - imin) / 2));
	}

	protected void merge(final int index) {
		if (this.bufferedList.size() <= index || this.bufferedList.get(index).key == null) {
			final int imax = 2 * index;

			for (int i = index; i <= imax; i++) {
				if (this.bufferedList.size() > i) {
					this.bufferedList.set(i, this.mergeList.get(i));
				} else {
					this.bufferedList.add(this.mergeList.get(i));
				}
			}
			this.calcPointers(index);
			this.mergeList.clear();
		} else {
			int b = index;
			int m = index;
			final int imax = 2 * index;
			if (this.bufferedList.size() == index * 2 + 1) {
				for (;;) {
					if (b > imax && m > imax) {
						break;
					}
					if (b <= imax && m <= imax) {
						final FractalTreeEntry<K, V> bEntry = this.bufferedList.get(b);
						final FractalTreeEntry<K, V> mEntry = this.mergeList.get(m);
						if (bEntry.key.compareTo(mEntry.key) < 0) {
							this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
							this.bufferedList.add(bEntry);
							b++;
						} else {
							this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
							this.bufferedList.add(mEntry);
							m++;
						}
					} else {
						if (b <= imax) {
							this.bufferedList.add(this.bufferedList.set(b, new FractalTreeEntry<K, V>()));
							b++;
						}
						if (m <= imax) {
							this.bufferedList.add(this.mergeList.set(m, new FractalTreeEntry<K, V>()));
							m++;
						}
					}
				}
				this.calcPointers(index*2+1);
				this.mergeList.clear();
			} else if (this.bufferedList.get(index * 2 + 1).key == null) {
				for (int i = index * 2 + 1;;) {
					if (b > imax && m > imax) {
						break;
					}
					if (b <= imax && m <= imax) {
						final FractalTreeEntry<K, V> bEntry = this.bufferedList.get(b);
						final FractalTreeEntry<K, V> mEntry = this.mergeList.get(m);
						if (bEntry.key.compareTo(mEntry.key) < 0) {
							this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
							this.bufferedList.set(i, bEntry);
							b++;
							i++;
						} else {
							this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
							this.bufferedList.set(i, mEntry);
							m++;
							i++;
						}
					} else {
						if (b <= imax) {
							this.bufferedList.set(i, this.bufferedList.set(b, new FractalTreeEntry<K, V>()));
							b++;
							i++;
						}
						if (m <= imax) {
							this.bufferedList.set(i, this.mergeList.set(m, new FractalTreeEntry<K, V>()));
							m++;
							i++;
						}
					}
				}
				this.calcPointers(index*2+1);
				this.mergeList.clear();
			} else {
				for (;;) {
					if (b > imax && m > imax) {
						break;
					}
					if (b <= imax && m <= imax) {
						final FractalTreeEntry<K, V> bEntry = this.bufferedList.get(b);
						final FractalTreeEntry<K, V> mEntry = this.mergeList.get(m);
						if (bEntry.key.compareTo(mEntry.key) < 0) {
							this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
							this.mergeList.add(bEntry);
							b++;
						} else {
							this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
							this.mergeList.add(mEntry);
							m++;
						}
					} else {
						if (b <= imax) {
							this.mergeList.add(this.bufferedList.set(b, new FractalTreeEntry<K, V>()));
							b++;
						}
						if (m <= imax) {
							this.mergeList.add(this.mergeList.set(m, new FractalTreeEntry<K, V>()));
							m++;
						}
					}
				}

				this.merge(index * 2 + 1);
			}
		}
	}

	protected int getPreviousPosition(final int index) {
		return (index - 1) / 2;
	}

	protected int calcPointer(final int index, final K key) {
		int j = this.getNextPosition(index); // Startposition des nächsten Arrays ermitteln
		final int size = this.bufferedList.size(); // Größe zwischenspeichern

		if (j >= size) {
			// keinen Pointer
			return -1;
		}

		if (this.bufferedList.get(j).key == null) {
			for (;;) {
				// Wenn das nächste Array leer ist gehe zum n�chsten Array
				if (this.bufferedList.get(j).key != null) {
					break;
				}
				j = j * 2 + 1;

				if (j >= size) {
					return -1;
				} else {
					continue;
				}

			}
		}

		final int imax = j * 2;
		for (int i = j; i <= imax; i++) {
			if (key.compareTo(this.bufferedList.get(i).key) <= 0 || i == imax) {
				return i;
			}
		}
		return -1;
	}

	protected void merge(final FractalTreeEntry<K, V> element) {
		if (this.bufferedList.size() == 0) {
			this.bufferedList.add(0, element);
		} else if (this.bufferedList.get(0).key == null) {
			final FractalTreeEntry<K, V> tmp = element;
			tmp.pointer = this.calcPointer(0, tmp.key);
			this.bufferedList.set(0, tmp);
		} else {
			this.mergeList.add(element);
			this.merge(0);
		}
	}

	public void print2() {
		System.out.print("fractal list (" + this.bufferedList.size() + "): ");
		for (int i = 0; i < this.bufferedList.size(); i++) {
			System.out.print(this.bufferedList.get(i) + "| ");
		}
		System.out.println("");
	}

	public void print3() {
		System.out.print("remove list (" + this.removeList.size() + "): ");
		for (int i = 0; i < this.removeList.size(); i++) {
			System.out.print(this.removeList.get(i) + "| ");
		}
		System.out.println("");
	}

	public void printMergelist() {
		System.out.print("merge list: ");
		final int size = this.mergeList.size();
		for (int i = 0; i < size; i++) {
			System.out.print(this.mergeList.get(i) + "| ");
		}
		System.out.println("");
	}

	protected class MergeList {
		private final BufferedList_LuposSerialization<FractalTreeEntry<K, V>> diskMemory;
		private ArrayList<FractalTreeEntry<K, V>> mainMemory = new ArrayList<>();
		public int threshold = (int) (Math.pow(2, 10) - 1);

		public MergeList(final FractalTreeEntry<K, V> entry) {
			this.diskMemory = new BufferedList_LuposSerialization<FractalTreeEntry<K, V>>(8 * 1024, SimplifiedFractalTree.this.file2.getAbsoluteFile(), entry);
			this.mainMemory = new ArrayList<>();
		}

		public void setNoReturn(final int index, final FractalTreeEntry<K, V> fractalTreeEntry) {
			if (index < this.threshold) {
				this.mainMemory.set(index, fractalTreeEntry);
			} else {
				this.diskMemory.setNoReturn(index - this.threshold, fractalTreeEntry);
			}
		}

		public MergeList(final FractalTreeEntry<K, V> entry, final File file) {
			this.diskMemory = new BufferedList_LuposSerialization<>(8 * 1024, new File(file.getAbsolutePath() + "ml"), entry);
			this.mainMemory = new ArrayList<>();
		}

		public MergeList(final FractalTreeEntry<K, V> entry, final int pageSize) {
			this.diskMemory = new BufferedList_LuposSerialization<>(pageSize, SimplifiedFractalTree.this.file2.getAbsoluteFile(), entry);
			this.mainMemory = new ArrayList<>();
		}

		public MergeList(final FractalTreeEntry<K, V> entry, final int pageSize, final File file) {
			this.diskMemory = new BufferedList_LuposSerialization<>(pageSize, new File(file.getAbsolutePath() + "ml"), entry);
			this.mainMemory = new ArrayList<>();
		}

		public void clear() {
			this.diskMemory.clear();
			this.mainMemory.clear();
		}

		public FractalTreeEntry<K, V> set(final int index, final FractalTreeEntry<K, V> entry) {
			FractalTreeEntry<K, V> oldEntry = null;
			if (index < this.threshold) {
				oldEntry = this.mainMemory.get(index);
				this.mainMemory.set(index, entry);
			} else {
				oldEntry = this.diskMemory.set(index - this.threshold, entry);
			}
			return oldEntry;
		}

		public void add(final int index, final FractalTreeEntry<K, V> entry) {
			if (index < this.threshold) {
				this.mainMemory.add(index, entry);
			} else {
				this.diskMemory.add(index - this.threshold, entry);
			}
		}

		public void add(final FractalTreeEntry<K, V> entry) {
			if (this.mainMemory.size() < this.threshold) {
				this.mainMemory.add(entry);
			} else {
				this.diskMemory.add(entry);
			}
		}

		public FractalTreeEntry<K, V> get(final int index) {
			if (index < this.threshold) {
				return this.mainMemory.get(index);
			} else {
				return this.diskMemory.get(index - this.threshold);
			}
		}

		public int size() {
			return this.diskMemory.size() + this.mainMemory.size();
		}
	}

	public static void main(final String[] args) throws InterruptedException {

		final SimplifiedFractalTree<StringKey, Integer> fractalTree = new SimplifiedFractalTree<StringKey, Integer>(new File("fttest"));
		fractalTree.clear();
		fractalTree.put(new StringKey("T1"), 0);
		fractalTree.print2();
		fractalTree.put(new StringKey("T6"), 84);
		fractalTree.print2();
		fractalTree.put(new StringKey("T4"), 3);
		fractalTree.print2();
		fractalTree.put(new StringKey("T5"), -8);
		fractalTree.print2();
		fractalTree.put(new StringKey("T9"), 111);
		fractalTree.print2();
		fractalTree.put(new StringKey("T6"), 67);
		fractalTree.print2();
		fractalTree.put(new StringKey("T1"), 47);
		fractalTree.print2();
		fractalTree.put(new StringKey("T7"), 27);
		fractalTree.print2();
		fractalTree.put(new StringKey("T3"), 4);
		fractalTree.print2();
		fractalTree.put(new StringKey("T13"), 351);
		fractalTree.print2();
		fractalTree.put(new StringKey("T8"), 45);
		fractalTree.print2();
		fractalTree.put(new StringKey("T2"), 4);
		fractalTree.print2();
		fractalTree.put(new StringKey("T10"), -11);
		fractalTree.print2();
		fractalTree.put(new StringKey("T11"), -12);
		System.out.println("Search:");
		fractalTree.print2();
		System.out.println("T8:  "+ fractalTree.get(new StringKey("T8")));
		System.out.println("T2:  "+ fractalTree.get(new StringKey("T2")));
		System.out.println("T10: "+ fractalTree.get(new StringKey("T10")));
		System.out.println("T3:  "+ fractalTree.get(new StringKey("T3")));
		System.out.println("T6:  "+ fractalTree.get(new StringKey("T6")));
		System.out.println("T5:  "+ fractalTree.get(new StringKey("T5")));
		System.out.println("T9:  "+ fractalTree.get(new StringKey("T9")));
		System.out.println("Size:");
		System.out.println(fractalTree.size());
		System.out.println("Last Key:");
		System.out.println(fractalTree.lastKey());
		System.out.println("First Key:");
		System.out.println(fractalTree.firstKey());
		System.out.println("Empty?:");
		System.out.println(fractalTree.isEmpty());
		System.out.println("Contains key T6:");
		System.out.println(fractalTree.containsKey(new StringKey("T6")));
		System.out.println("Contains key T699:");
		System.out.println(fractalTree.containsKey(new StringKey("T699")));
		System.out.println("Prefix Search:");
		final Comparator<StringKey> comp = new Comparator<StringKey>() {

			@Override
			public int compare(final StringKey o1, final StringKey o2) {
				return o1.string.substring(0, 1).compareTo(o2.string.substring(0, 1));
			}
		};
		final StringKey prefix = new StringKey("T", comp);
		final Iterator<Integer> iterator = fractalTree.prefixSearch(prefix);
		for(;iterator.hasNext();){
			System.out.println(iterator.next());
		}
		System.out.println("Remove:");
		fractalTree.print2();
		System.out.println("T2:  "+ fractalTree.remove(new StringKey("T2")));
		fractalTree.print2();
		System.out.println("T1:  "+ fractalTree.remove(new StringKey("T1")));
		fractalTree.print2();
		System.out.println("T7:  "+ fractalTree.remove(new StringKey("T7")));
		fractalTree.print2();
		System.out.println("T5:  "+ fractalTree.remove(new StringKey("T5")));
		fractalTree.print2();
		System.out.println("T4:  "+ fractalTree.remove(new StringKey("T4")));
		fractalTree.print2();
		System.out.println("T3:  "+ fractalTree.remove(new StringKey("T3")));
		fractalTree.print2();
		System.out.println("T9:  "+ fractalTree.remove(new StringKey("T9")));
		fractalTree.print2();
		System.out.println("T6:  "+ fractalTree.remove(new StringKey("T6")));
		fractalTree.print2();
		System.out.println("T13:  "+ fractalTree.remove(new StringKey("T13")));
		fractalTree.print2();
		System.out.println("T8:  "+ fractalTree.remove(new StringKey("T8")));
		fractalTree.print2();
		System.out.println("T10:  "+ fractalTree.remove(new StringKey("T10")));
		fractalTree.print2();
	}
}