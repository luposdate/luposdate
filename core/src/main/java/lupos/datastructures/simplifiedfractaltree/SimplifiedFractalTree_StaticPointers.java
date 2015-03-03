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

import lupos.datastructures.simplifiedfractaltree.buffermanager.BufferedList;

/**
 * This class is an implementation of the simplified fractaltree using static pointers.
 *
 * @author Denis Fäcke
 * @param <K> Key
 * @param <V> Value
 * @version $Id: $Id
 */
public class SimplifiedFractalTree_StaticPointers<K extends Comparable<K> & Serializable, V extends Serializable> extends SimplifiedFractalTree<K, V> {

	/**
	 *
	 */
	private static final long serialVersionUID = -5746266880259934265L;

	/**
	 * Constructs am empty fractal tree using a default page size of 8192.
	 *
	 * @see BufferedList
	 */
	public SimplifiedFractalTree_StaticPointers() {
		super();
	}

	/**
	 * Constructs am empty fractal tree using a default page size of 8192.
	 *
	 * @param file a file specifying the name and the storage location on the disk.
	 * @see BufferedList
	 */
	public SimplifiedFractalTree_StaticPointers(final File file) {
		super(file);
	}

	/**
	 * Constructs am empty fractal tree.
	 *
	 * @param file a file specifying the name and the storage location on the disk.
	 * @param pageSize the size of a page; greater than <tt>0</tt>
	 * @throws java.lang.NegativeArraySizeException if pageSize is less or equal <tt>0</tt>
	 * @see BufferedList
	 */
	public SimplifiedFractalTree_StaticPointers(final File file, final int pageSize) {
		super(file, pageSize);
	}

	/**
	 * Constructs am empty fractal tree.
	 *
	 * @param pageSize the size of a page; greater than <tt>0</tt>
	 * @throws java.lang.NegativeArraySizeException if pageSize is less or equal <tt>0</tt>
	 * @see BufferedList
	 */
	public SimplifiedFractalTree_StaticPointers(final int pageSize) {
		super(pageSize);
	}

	/** {@inheritDoc} */
	@Override
	public V get(final Object key) {
		assert key != null;

		return this.get(0, key);
	}

	/** {@inheritDoc} */
	@Override
	public V put(final K key, final V value) {
		assert key != null;

		V oldValue = null;
		FractalTreeEntry<K, V> oldEntry = null;
		final int index = this.indexOf(0, key, new FractalTreeEntry<K, V>(), 0);
		if (index >= 0) {
			oldEntry = this.bufferedList.get(index);
			oldValue = oldEntry.value;
			this.bufferedList.setNoReturn(index, new FractalTreeEntry<K, V>(key, value));
		} else {
			this.merge(new FractalTreeEntry<K, V>(key, value, -2));
		}

		return oldValue;
	}

	/**
	 * Returns a pointer.
	 * @param index The index of an element
	 * @return A pointer
	 */
	private int getPointer(final int index) {
		final int pointer = (index + 1) * 2;
		if (pointer >= this.bufferedList.size()) {
			return -1;
		} else if (this.bufferedList.get(pointer).key == null) {
			return this.getPointer(pointer);
		} else {
			return pointer;
		}
	}

	/**
	 * <p>get.</p>
	 *
	 * @param index a int.
	 * @param key a {@link java.lang.Object} object.
	 * @return a V object.
	 */
	@SuppressWarnings({ "unchecked" })
	protected V get(final int index, final Object key) {
		if (this.bufferedList.size() <= index) {
			return null;
		}
		if (this.bufferedList.size() > 0) {
			FractalTreeEntry<K, V> entry = this.bufferedList.get(index);
			if (entry.key != null && entry.key.compareTo((K) key) == 0) {
				return entry.value;
			} else {
				int nextArrayIndex = index;
				if (this.bufferedList.get(nextArrayIndex).key == null) {
					for (; nextArrayIndex < this.bufferedList.size();) {
						if (this.bufferedList.get(nextArrayIndex).key != null) {
							break;
						} else {
							nextArrayIndex = nextArrayIndex * 2 + 1;
						}
					}
					return this.get(nextArrayIndex, key);
				} else {
					nextArrayIndex = this.getNextPosition(index);
					if (entry.pointer == -2) {
						entry.pointer = this.getPointer(index);
						this.bufferedList.set(index, entry);
					}
					if (entry.pointer == -1) {
						if (entry.key.compareTo((K) key) > 0) {
							entry = null;
							if (index - 1 >= this.getArrayStart(index) && this.bufferedList.get(index - 1).key.compareTo((K) key) >= 0) {
								return this.get(index - 1, key);
							} else {
								return null;
							}
						} else {
							if (index + 1 < nextArrayIndex && this.bufferedList.get(index + 1).key.compareTo((K) key) <= 0) {
								return this.get(index + 1, key);
							} else {
								return null;
							}
						}
					} else {
						entry = this.improvePointer(entry, index, 0);
						if (entry.key.compareTo((K) key) > 0) {
							if (index - 1 >= this.getArrayStart(index) && this.bufferedList.get(index - 1).key.compareTo((K) key) >= 0) {
								entry = null;
								return this.get(index - 1, key);
							} else {
								return this.get(entry.pointer, key);
							}
						} else {
							if (index + 1 < nextArrayIndex && this.bufferedList.get(index + 1).key.compareTo((K) key) <= 0) {
								entry = null;
								return this.get(index + 1, key);
							} else {
								return this.get(entry.pointer, key);
							}
						}
					}
				}
			}
		} else {
			final V value = this.bufferedList.get(index).value;
			return value;
		}
	}

	private FractalTreeEntry<K, V> improvePointer(final FractalTreeEntry<K, V> entry, final int index, final int level) {
		final FractalTreeEntry<K, V> pointedAt = this.bufferedList.get(entry.pointer);
		if (entry.pointer + 1 < this.getNextPosition(entry.pointer) && pointedAt.key.compareTo(entry.key) < 0) {
			entry.pointer += 1;
			if (level < 3) {
				return this.improvePointer(entry, index, level + 1);
			} else {
				this.bufferedList.set(index, entry);
			}
		} else if (entry.pointer - 1 >= this.getArrayStart(entry.pointer) && pointedAt.key.compareTo(entry.key) > 0
				&& this.bufferedList.get(entry.pointer - 1).key.compareTo(entry.key) > 0) {
			entry.pointer -= 1;
			if (level < 3) {
				return this.improvePointer(entry, index, level + 1);
			} else {
				this.bufferedList.set(index, entry);
			}
		}
		return entry;
	}

	/** {@inheritDoc} */
	@Override
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
					if (entry.pointer == -2) {
						entry.pointer = this.getPointer(index);
					}
					if (entry.pointer == -1) {
						if (entry.key.compareTo(key) > 0) {
							if (index - 1 >= this.getArrayStart(index) && this.bufferedList.get(index - 1).key.compareTo(key) >= 0) {
								return this.indexOf(index - 1, key, entry, nextArrayIndex);
							} else {
								return -1;
							}
						} else {
							if (index + 1 < nextArrayIndex && this.bufferedList.get(index + 1).key.compareTo(key) <= 0) {
								return this.indexOf(index + 1, key, entry, nextArrayIndex);
							} else {
								return -1;
							}
						}
					} else {
						if (entry.key.compareTo(key) > 0) {
							if (index - 1 >= this.getArrayStart(index) && this.bufferedList.get(index - 1).key.compareTo(key) >= 0) {
								return this.indexOf(index - 1, key, entry, nextArrayIndex);
							} else {
								return this.indexOf(entry.pointer, key, entry, nextArrayIndex);
							}
						} else {
							if (index + 1 < nextArrayIndex && this.bufferedList.get(index + 1).key.compareTo(key) <= 0) {
								return this.indexOf(index + 1, key, entry, nextArrayIndex);
							} else {
								return this.indexOf(entry.pointer, key, entry, nextArrayIndex);
							}
						}
					}
				}
			}
		} else {
			return -1;
		}
	}

	/** {@inheritDoc} */
	@Override
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

			this.mergeList.clear();
		} else {
			int b = index;
			int m = index;
			final int imax = 2 * index;
			for (;;) {
				if (b > imax && m > imax) {
					break;
				}
				if (b <= imax && m <= imax) {
					final FractalTreeEntry<K, V> bEntry = this.bufferedList.get(b);
					final FractalTreeEntry<K, V> mEntry = this.mergeList.get(m);
					if (bEntry.key.compareTo(mEntry.key) < 0) {
						this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
						bEntry.pointer = -2;
						this.mergeList.add(bEntry);
						b++;
					} else {
						this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
						mEntry.pointer = -2;
						this.mergeList.add(mEntry);
						m++;
					}
				} else {
					if (b <= imax) {
						final FractalTreeEntry<K, V> bEntry = this.bufferedList.get(b);
						this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
						bEntry.pointer = -2;
						this.mergeList.add(bEntry);
						b++;
					}
					if (m <= imax) {
						final FractalTreeEntry<K, V> mEntry = this.mergeList.get(m);
						this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
						mEntry.pointer = -2;
						this.mergeList.add(mEntry);
						m++;
					}
				}
			}

			this.merge(index * 2 + 1);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void merge(final FractalTreeEntry<K, V> element) {
		if (this.bufferedList.size() == 0) {
			this.bufferedList.add(0, element);
		} else if (this.bufferedList.get(0).key == null) {
			this.bufferedList.set(0, element);
		} else {
			this.mergeList.add(element);
			this.merge(0);
		}
	}
}
