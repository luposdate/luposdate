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

import lupos.datastructures.simplifiedfractaltree.buffermanager.BufferedList_LuposSerialization;

/**
 * This class is an implementation of the simplified fractaltree with lazy insertion.
 *
 * @author Denis Fäcke
 * @param <K> Key
 * @param <V> Value
 * @version $Id: $Id
 */
public class SimplifiedFractalTree_Lazy<K extends Comparable<K> & Serializable, V extends Serializable> extends SimplifiedFractalTree<K, V> {
	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = 5863663006038699636L;
	BufferedList_LuposSerialization<Integer> delList = new BufferedList_LuposSerialization<Integer>(8 * 1024, this.file.getAbsoluteFile(), new Integer(1));

	/**
	 * Creats a new <tt>SimplifiedFractalTree_Lazy</tt>.
	 *
	 * @param pageSize The pageSize
	 */
	public SimplifiedFractalTree_Lazy(final int pageSize) {
		super(pageSize);
	}

	/**
	 * Creats a new <tt>SimplifiedFractalTree_Lazy</tt>.
	 *
	 * @param file A file
	 * @param pageSize The pageSize
	 */
	public SimplifiedFractalTree_Lazy(final File file, final int pageSize) {
		super(file, pageSize);
	}

	/**
	 * Creats a new <tt>SimplifiedFractalTree_Lazy</tt>.
	 */
	public SimplifiedFractalTree_Lazy() {
		super();
	}

	/**
	 * Creats a new <tt>SimplifiedFractalTree_Lazy</tt>.
	 *
	 * @param file A file
	 */
	public SimplifiedFractalTree_Lazy(final File file) {
		super(file);
	}

	/** {@inheritDoc} */
	@Override
	public V put(final K key, final V value) {
		assert key != null;

		this.merge(new FractalTreeEntry<K, V>(key, value));

		return null;
	}

	/**
	 * Increments the count of deleted elements for the specified array.
	 *
	 * @param array An array
	 */
	protected void incDeletedCount(final int array) {
		for (; this.delList.size() <= array; this.delList.add(new Integer(0))) {
		}
		this.delList.set(array, this.delList.get(array) + 1);

	}

	/**
	 * Checks if the specified array has enough elements to delete.
	 *
	 * @param array An array
	 */
	protected void delCheck(final int array) {
		if (array + 1 > this.delList.size()) {
			return;
		}
		this.mergeList.clear();
		if (this.delList.get(array).intValue() == Math.pow(2, array) / 2) {
			if (array == 0) {
				this.bufferedList.set(0, new FractalTreeEntry<K, V>());
				this.delList.set(0, 0);
			} else if (array == 1) {
				if (this.bufferedList.get(1).flag) {
					this.mergeList.add(this.bufferedList.get(2));
				} else {
					this.mergeList.add(this.bufferedList.get(1));
				}
				if (this.bufferedList.get(0).pointer != -2) {
					final FractalTreeEntry<K, V> e = this.bufferedList.get(0);
					e.pointer = -2;
					this.bufferedList.set(0, e);
				}
				this.bufferedList.setNoReturn(1, new FractalTreeEntry<K, V>());
				this.bufferedList.setNoReturn(2, new FractalTreeEntry<K, V>());
				this.merge(0);
				this.delList.set(1, 0);
			} else {
				int m = (int) (Math.pow(2, array - 1) - 1);
				for (; this.mergeList.size() <= m; this.mergeList.add(new FractalTreeEntry<K, V>())) {
					;
				}
				final int imin = (int) (Math.pow(2, array) - 1);
				final int imax = imin * 2 + 1;
				for (int i = imin; i < imax; i++) {
					if (!this.bufferedList.get(i).flag) {
						this.mergeList.add(this.bufferedList.get(i));
					}
					this.bufferedList.setNoReturn(i, new FractalTreeEntry<K, V>());
				}
				this.merge(m);
				for (;;) {
					m = this.getNextPosition(m);
					if (this.bufferedList.get(m).key == null && m != 0) {
						continue;
					} else if (this.bufferedList.get(m).key == null && m == 0) {
						break;
					} else if (this.bufferedList.get(m).key != null) {
						for (int j = m; j < m * 2 + 1; j++) {
							final FractalTreeEntry<K, V> e = this.bufferedList.get(j);
							e.pointer = -2;
							this.bufferedList.set(j, e);
						}
						break;
					}
				}
				this.delList.set(array, 0);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void merge(final int index) {
		int imax = 2 * index;
		if (this.bufferedList.size() <= index || this.bufferedList.get(index).key == null) {
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
			imax = 2 * index;
			boolean delCheck = false;
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
						} else if (bEntry.key.compareTo(mEntry.key) > 0) {
							this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
							this.bufferedList.add(mEntry);
							m++;
						} else if (bEntry.key.compareTo(mEntry.key) == 0) {
							if (!bEntry.flag && !mEntry.flag) {
								bEntry.flag = true;
								this.bufferedList.add(bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
								this.incDeletedCount(this.getHeigth(index) + 1);
								delCheck = true;
							} else if (!bEntry.flag && mEntry.flag) {
								this.bufferedList.add(mEntry);
								this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
								m++;
							} else if (bEntry.flag && !mEntry.flag) {
								this.bufferedList.add(bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
							} else if (bEntry.flag && mEntry.flag) {
								this.bufferedList.add(bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
								this.bufferedList.add(mEntry);
								this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
								m++;
							}
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
				this.calcPointers(index * 2 + 1);
				this.mergeList.clear();
				if(delCheck) {
					this.delCheck(this.getHeigth(index) + 1);
				}
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
						} else if (bEntry.key.compareTo(mEntry.key) > 0) {
							this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
							this.bufferedList.set(i, mEntry);
							m++;
							i++;
						} else if (bEntry.key.compareTo(mEntry.key) == 0) {
							if (!bEntry.flag && !mEntry.flag) {
								bEntry.flag = true;
								this.bufferedList.set(i, bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
								i++;
								this.incDeletedCount(this.getHeigth(index) + 1);
								delCheck = true;
							} else if (bEntry.flag && !mEntry.flag) {
								this.bufferedList.set(i, bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
								i++;
							} else if (!bEntry.flag && mEntry.flag) {
								this.bufferedList.set(i, mEntry);
								this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
								m++;
								i++;
							} else if (bEntry.flag && mEntry.flag) {
								this.bufferedList.set(i, bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
								i++;
								this.bufferedList.set(i, mEntry);
								this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
								m++;
								i++;
							}
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
				this.calcPointers(index * 2 + 1);
				this.mergeList.clear();
				if(delCheck) {
					this.delCheck(this.getHeigth(index) + 1);
				}
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
						} else if (bEntry.key.compareTo(mEntry.key) > 0) {
							this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
							this.mergeList.add(mEntry);
							m++;
						} else if (bEntry.key.compareTo(mEntry.key) == 0) {
							if (!bEntry.flag && !mEntry.flag) {
								bEntry.flag = true;
								this.mergeList.add(bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
								this.incDeletedCount(this.getHeigth(index) + 1);
								delCheck = true;
							} else if (bEntry.flag && !mEntry.flag) {
								this.mergeList.add(bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
							} else if (!bEntry.flag && mEntry.flag) {
								this.mergeList.add(mEntry);
								this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
								m++;
							} else if (bEntry.flag && mEntry.flag) {
								this.mergeList.add(bEntry);
								this.bufferedList.setNoReturn(b, new FractalTreeEntry<K, V>());
								b++;
								this.mergeList.add(mEntry);
								this.mergeList.setNoReturn(m, new FractalTreeEntry<K, V>());
								m++;
							}
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

	/** {@inheritDoc} */
	@Override
	protected void merge(final FractalTreeEntry<K, V> element) {
		element.pointer = -2;
		if (this.bufferedList.size() == 0) {
			this.bufferedList.add(0, element);
		} else if (this.bufferedList.get(0).key == null) {
			this.bufferedList.set(0, element);
		} else {
			this.mergeList.add(element);
			this.merge(0);
		}
	}

	/**
	 * <p>calcPointers.</p>
	 *
	 * @param index a int.
	 * @param max a int.
	 */
	protected void calcPointers(final int index, final int max) {
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

	/** {@inheritDoc} */
	@Override
	public int indexOf(final int index, final K key, FractalTreeEntry<K, V> entry, int nextArrayIndex) {
		if (index >= this.bufferedList.size()) {
			return -1;
		}
		if (this.bufferedList.size() > 0) {
			if (index < 0) {
				System.out.println(key);
			}
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
						this.calcPointers(this.getArrayStart(index));
					}
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
}
