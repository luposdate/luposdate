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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.buffermanager.PageInputStream;
import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.buffermanager.PageOutputStream;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.dbmergesortedds.StandardComparator;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectInputStreamWithoutReadingHeader;
import lupos.io.LuposObjectOutputStream;
import lupos.io.LuposObjectOutputStreamWithoutWritingHeader;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.FileHelper;
import lupos.misc.Tuple;

public class DBBPTree<K extends Comparable<K> & Serializable, V extends Serializable>
implements SortedMap<K, V>, Serializable, PrefixSearchMinMax<K, V> {

	private static final long serialVersionUID = -3345017876896171725L;

	protected int currentID = 0;

	protected int k, k_;
	protected int size = 0;

	protected Comparator<? super K> comparator;

	protected int rootPage = -1;
	protected int firstLeafPage = -1;

	protected static int currentFileID = 0;

	protected static String mainFolder = "tmp//dbbptree//";

	protected Class<? super K> keyClass;
	protected Class<? super V> valueClass;

	protected PageManager pageManager;

	protected final NodeDeSerializer<K, V> nodeDeSerializer;

	public static boolean enableSIP = true;

	// you can give a name to this dbbptree (e.g. for debugging purposes...)
	protected String name = "Name not yet given!";

	public static void setTmpDir(String dir, final boolean delete) {

		if (dir.compareTo("") != 0 && (!(dir.endsWith("//") || dir.endsWith("/") || dir.endsWith("\\")))){
			dir = dir + "//";
		}
		DBBPTree.mainFolder = dir + "dbbptree//";
		if (delete) {
			FileHelper.deleteDirectory(new File(mainFolder));
		}
	}

	public static String getMainFolder(){
		return DBBPTree.mainFolder;
	}

	public static void setCurrentFileID(final int fileID) {
		currentFileID = fileID;
	}

	public static int getCurrentFileID() {
		return currentFileID;
	}

	public DBBPTree(final Comparator<? super K> comparator, final int k, final int k_, final NodeDeSerializer<K, V> nodeDeSerializer) throws IOException {
		this.init(comparator, k, k_);
		this.nodeDeSerializer = nodeDeSerializer;
	}

	public DBBPTree(final int k, final int k_, final NodeDeSerializer<K, V> nodeDeSerializer) throws IOException {
		this.init(null, k, k_);
		this.nodeDeSerializer = nodeDeSerializer;
	}

	public void setName(final String name){
		this.name=name;
	}

	public String getName(){
		return this.name;
	}

	protected static ReentrantLock lock = new ReentrantLock();

	private void init(final Comparator<? super K> comparator, final int k,
			final int k_) throws IOException {
		this.k = k;
		this.k_ = k_;
		this.size = 0;
		lock.lock();
		try {
			this.currentID = currentFileID++;
			final File f = new File(DBBPTree.mainFolder);
			f.mkdirs();
			if(this.currentID==0){
				FileHelper.deleteFilesStartingWithPattern(DBBPTree.mainFolder, this.currentID + ".dbbptree_");
			}
			this.pageManager = new PageManager(DBBPTree.mainFolder + this.currentID + ".dbbptree");
		} finally {
			lock.unlock();
		}

		if (comparator == null) {
			this.comparator = new StandardComparator<K>();
		} else {
			this.comparator = comparator;
		}
	}

	@Override
	public Comparator<? super K> comparator() {
		return this.comparator;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		if (this.firstLeafPage < 0) {
			return new HashSet<java.util.Map.Entry<K, V>>();
		} else {
			return new Set<java.util.Map.Entry<K, V>>() {
			@Override
			public boolean add(final java.util.Map.Entry<K, V> arg0) {
				final V value = DBBPTree.this.put(arg0.getKey(), arg0
						.getValue());
				if (value == null) {
					return true;
				} else {
					return !value.equals(arg0.getValue());
				}
			}

			@Override
			public boolean addAll(
					final Collection<? extends java.util.Map.Entry<K, V>> arg0) {
				boolean result = false;
				for (final java.util.Map.Entry<K, V> me : arg0) {
					result = result || this.add(me);
				}
				return result;
			}

			@Override
			public void clear() {
				DBBPTree.this.clear();
			}

			@Override
			public boolean contains(final Object arg0) {
				final V value = DBBPTree.this
				.get(((java.util.Map.Entry<K, V>) arg0).getKey());
				if (value != null) {
					return (value.equals(((java.util.Map.Entry<K, V>) arg0)
							.getValue()));
				} else {
					return false;
				}
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
				return DBBPTree.this.isEmpty();
			}

			@Override
			public SIPParallelIterator<java.util.Map.Entry<K, V>, K> iterator() {
				if (this.size() == 0) {
					return new SIPParallelIterator<java.util.Map.Entry<K, V>, K>() {
					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public java.util.Map.Entry<K, V> next() {
						return null;
					}

					@Override
					public void remove() {
					}

					@Override
					public java.util.Map.Entry<K, V> next(final K k) {
						return null;
					}

					@Override
					public void close() {
					}
				};
				}
				try {
					return new SIPParallelIterator<java.util.Map.Entry<K, V>, K>() {
						private LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader<V>(
								new PageInputStream(DBBPTree.this.firstLeafPage,
										DBBPTree.this.pageManager), null);
						{
							this.innerNodes = new LinkedList<Tuple<K, LuposObjectInputStream<V>>>();
							InputHelper.readLuposBoolean(this.in.is);
						}
						private List<Tuple<K, LuposObjectInputStream<V>>> innerNodes;
						private int entrynumber = 0;
						private K lastKey = null;
						private V lastValue = null;

						@Override
						public boolean hasNext() {
							return (this.entrynumber < DBBPTree.this.size());
						}

						private java.util.Map.Entry<K, V> getFirst(
								final int filename, final K k) {
							if (filename < 0) {
								return null;
							}
							InputStream fis;
							try {
								fis = new PageInputStream(filename,
										DBBPTree.this.pageManager);
								final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
										fis, null);
								final boolean leaf = InputHelper.readLuposBoolean(this.in.is);
								if (leaf) { // leaf node reached!
									this.lastKey = null;
									this.lastValue = null;
									while (true) {
										final DBBPTreeEntry<K, V> e = DBBPTree.this.getNextLeafEntry(
												in, this.lastKey, this.lastValue);
										if (e == null || e.key == null) {
											in.close();
											this.close();
											return null;
										}
										final K key = e.key;
										this.lastKey = key;
										this.lastValue = e.value;
										final int compare = DBBPTree.this.comparator
										.compare(key, k);
										if (compare == 0) {
											this.in = in;
											return new MapEntry<K, V>(
													e.key, e.value);
										} else if (compare > 0) {
											this.in = in;
											return new MapEntry<K, V>(
													e.key, e.value);
										}
									}
								} else {
									K lastKey = null;
									while (true) {
										final Tuple<K, Integer> nextEntry = DBBPTree.this.getNextInnerNodeEntry(
												lastKey, in);
										if (nextEntry == null
												|| nextEntry.getSecond() <= 0) {
											in.close();
											this.close();
											return null;
										}
										lastKey = nextEntry.getFirst();
										if (nextEntry.getFirst() == null) {
											this.innerNodes
											.add(new Tuple<K, LuposObjectInputStream<V>>(
													null, in));
											return this.getFirst(nextEntry
													.getSecond(), k);
										}
										final int compare = DBBPTree.this.comparator
										.compare(nextEntry
												.getFirst(), k);
										if (compare >= 0) {
											this.innerNodes
											.add(new Tuple<K, LuposObjectInputStream<V>>(
													nextEntry
													.getFirst(),
													in));
											return this.getFirst(nextEntry
													.getSecond(), k);
										}
									}
								}

							} catch (final FileNotFoundException e) {
								e.printStackTrace();
								System.err.println(e);
							} catch (final IOException e) {
								System.err.println("filename:"+filename);
								e.printStackTrace();
								System.err.println(e);
							}
							return null;
						}

						private java.util.Map.Entry<K, V> getFirstUsingCache(
								final int index, final K kkey) {
							if (index < 0) {
								return null;
							}
							try {
								if (this.innerNodes.size() <= index) {
									this.close();
									return null;
									// close();
									// innerNodes.clear();
									// return getFirst(rootFilename,
									// triplekey);
								}
								final Tuple<K, LuposObjectInputStream<V>> current = this.innerNodes
								.get(index);
								final LuposObjectInputStream<V> in = current
								.getSecond();
								K lastKey = current.getFirst();
								if (lastKey == null
										|| DBBPTree.this.comparator
										.compare(lastKey, kkey) >= 0) {
									return this.getFirstUsingCache(index + 1,
											kkey);
								}
								while (this.innerNodes.size() > index + 1) {
									final Tuple<K, LuposObjectInputStream<V>> toBeDeleted = this.innerNodes
									.remove(this.innerNodes.size() - 1);
									try {
										toBeDeleted.getSecond().close();
									} catch (final IOException e) {
										e.printStackTrace();
										System.err.println(e);
									}
								}
								while (true) {
									final Tuple<K, Integer> nextEntry = DBBPTree.this.getNextInnerNodeEntry(
											lastKey, in);
									if (nextEntry == null
											|| nextEntry.getSecond() <= 0) {
										in.close();
										this.close();
										return null;
									}
									lastKey = nextEntry.getFirst();
									if (nextEntry.getFirst() == null) {
										current.setFirst(null);
										return this.getFirst(nextEntry
												.getSecond(), kkey);
									}
									final int compare = DBBPTree.this.comparator.compare(
											nextEntry.getFirst(), kkey);
									if (compare >= 0) {
										current.setFirst(nextEntry
												.getFirst());
										return this.getFirst(nextEntry
												.getSecond(), kkey);
									}
								}

							} catch (final FileNotFoundException e) {
								e.printStackTrace();
								System.err.println(e);
							} catch (final IOException e) {
								e.printStackTrace();
								System.err.println(e);
							}
							return null;
						}

						@Override
						public java.util.Map.Entry<K, V> next() {
							if(!this.hasNext()) {
								return null;
							}
							try {
								final DBBPTreeEntry<K, V> e = DBBPTree.this.getNextLeafEntry(
										this.in, this.lastKey, this.lastValue);
								if (e != null) {
									if (e.key == null) {
										if (e.filenameOfNextLeafNode >= 0) {
											this.in.close();
											try{
												this.in = new LuposObjectInputStreamWithoutReadingHeader<V>(
														new PageInputStream(
																e.filenameOfNextLeafNode,
																DBBPTree.this.pageManager),
																null);
												InputHelper.readLuposBoolean(this.in.is);
												this.lastKey = null;
												this.lastValue = null;
												return this.next();
											} catch(final Exception e1){
												System.err.println(e1);
												e1.printStackTrace();
												return null;
											}
										}
									} else {
										this.lastKey = e.key;
										this.lastValue = e.value;
										this.entrynumber++;
										return new MapEntry<K, V>(e.key,
												e.value);
									}
								}
							} catch (final FileNotFoundException e1) {
								System.err.println(e1);
								e1.printStackTrace();
							} catch (final IOException e1) {
								System.err.println(e1);
								e1.printStackTrace();
							}
							return null;
						}

						@Override
						public void remove() {
							throw (new UnsupportedOperationException(
							"This iterator is ReadOnly."));
						}

						@Override
						protected void finalize() throws Throwable {
							try {
								this.in.close();
							} finally {
								super.finalize();
							}
						}

						@Override
						public void close() {
							for (final Tuple<K, LuposObjectInputStream<V>> tuple : this.innerNodes) {
								try {
									tuple.getSecond().close();
								} catch (final IOException e) {
								}
							}
							try {
								this.in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
						}

						private java.util.Map.Entry<K, V> getNext(final K k) {
							try {
								final DBBPTreeEntry<K, V> e = DBBPTree.this.getNextLeafEntry(
										this.in, this.lastKey, this.lastValue);
								if (e != null) {
									if (e.key == null) {
										if (e.filenameOfNextLeafNode >= 0) {
											this.in.close();
											this.in = new LuposObjectInputStreamWithoutReadingHeader<V>(
													new PageInputStream(
															e.filenameOfNextLeafNode,
															DBBPTree.this.pageManager),
															null);
											InputHelper.readLuposBoolean(this.in.is);
											this.lastKey = null;
											this.lastValue = null;
											while (true) {
												final DBBPTreeEntry<K, V> e1 = DBBPTree.this.getNextLeafEntry(
														this.in, this.lastKey,
														this.lastValue);
												if (e1 != null) {
													if (e1.key == null) {
														// read over one
														// complete leaf
														// node!
														// using sideways
														// information
														// passing and jump
														// directly to the
														// corresponding
														// leaf node!
														if (e.filenameOfNextLeafNode >= 0) {
															this.in.close();
															if (this.innerNodes
																	.size() == 0) {
																return this.getFirst(
																		DBBPTree.this.rootPage,
																		k);
															} else {
																return this.getFirstUsingCache(
																		0,
																		k);
															}
														}
													} else {
														this.entrynumber++;
														this.lastKey = e1.key;
														this.lastValue = e1.value;
														if (DBBPTree.this.comparator
																.compare(
																		k,
																		e1.key) <= 0) {
															return new MapEntry<K, V>(
																	e1.key,
																	e1.value);
														}
													}
												} else {
													this.in.close();
													this.close();
													return null;
												}
											}
										}
										this.in.close();
										this.close();
										return null;
									} else {
										this.lastKey = e.key;
										this.lastValue = e.value;
										this.entrynumber++;
										return new MapEntry<K, V>(e.key,
												e.value);
									}
								}
							} catch (final FileNotFoundException e1) {
								System.err.println(e1);
								e1.printStackTrace();
							} catch (final IOException e1) {
								System.err.println(e1);
								e1.printStackTrace();
							}
							return null;
						}

						@Override
						public java.util.Map.Entry<K, V> next(final K k) {
							java.util.Map.Entry<K, V> result;
							do {
								result = this.getNext(k);
							} while (result != null
									&& DBBPTree.this.comparator.compare(k, result
											.getKey()) > 0);
							return result;
						}
					};
				} catch (final IOException e) {
					System.err.println(e);
					e.printStackTrace();
					return null;
				}
			}

			@Override
			public boolean remove(final Object arg0) {
				final V value = DBBPTree.this
				.remove(((java.util.Map.Entry<K, V>) arg0).getKey());
				if (value == null) {
					return false;
				} else {
					return value.equals(((java.util.Map.Entry<K, V>) arg0)
							.getValue());
				}
			}

			@Override
			public boolean removeAll(final Collection<?> arg0) {
				boolean result = false;
				for (final Object me : arg0) {
					result = result || this.remove(me);
				}
				return result;
			}

			@Override
			public boolean retainAll(final Collection<?> arg0) {
				boolean result = false;
				for (final java.util.Map.Entry<K, V> o : this) {
					if (!arg0.contains(o)) {
						this.remove(o);
						result = true;
					}
				}
				return result;
			}

			@Override
			public int size() {
				return DBBPTree.this.size();
			}

			@Override
			public Object[] toArray() {
				final Object[] o = new Object[this.size()];
				final Iterator<java.util.Map.Entry<K, V>> kit = this.iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = kit.next();
				}
				return o;
			}

			@Override
			public <T> T[] toArray(final T[] arg0) {
				final T[] o = (T[]) new Object[this.size()];
				final Iterator<java.util.Map.Entry<K, V>> kit = this.iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = (T) kit.next();
				}
				return o;
			}
		};
		}
	}

	protected Tuple<K, Integer> getNextInnerNodeEntry(final K lastKey2,
			final LuposObjectInputStream<V> in2) {
		return this.nodeDeSerializer.getNextInnerNodeEntry(lastKey2, in2);
	}

	@Override
	public K firstKey() {
		final Iterator<K> it = this.keySet().iterator();
		if (it.hasNext()) {
			return it.next();
		} else {
			return null;
		}
	}

	@Override
	public SortedMap<K, V> headMap(final K arg0) {
		throw (new UnsupportedOperationException("headMap is not supported."));
	}

	@Override
	public Set<K> keySet() {
		if (this.firstLeafPage < 0) {
			return new HashSet<K>();
		} else {
			return new Set<K>() {
			@Override
			public boolean add(final K arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			@Override
			public boolean addAll(final Collection<? extends K> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			@Override
			public void clear() {
				DBBPTree.this.clear();
			}

			@Override
			public boolean contains(final Object arg0) {
				return DBBPTree.this.containsKey(arg0);
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
				return DBBPTree.this.isEmpty();
			}

			@Override
			public Iterator<K> iterator() {
				return new Iterator<K>() {
					Iterator<java.util.Map.Entry<K, V>> it = DBBPTree.this
					.entrySet().iterator();

					@Override
					public boolean hasNext() {
						return this.it.hasNext();
					}

					@Override
					public K next() {
						final java.util.Map.Entry<K, V> me = this.it.next();
						if (me != null) {
							return me.getKey();
						} else {
							return null;
						}
					}

					@Override
					public void remove() {
						this.it.remove();
					}
				};
			}

			@Override
			public boolean remove(final Object arg0) {
				return (DBBPTree.this
						.remove(((java.util.Map.Entry<K, V>) arg0).getKey()) == null);
			}

			@Override
			public boolean removeAll(final Collection<?> arg0) {
				boolean result = false;
				for (final Object me : arg0) {
					result = result || this.remove(me);
				}
				return result;
			}

			@Override
			public boolean retainAll(final Collection<?> arg0) {
				boolean result = false;
				for (final K o : this) {
					if (!arg0.contains(o)) {
						this.remove(o);
						result = true;
					}
				}
				return result;
			}

			@Override
			public int size() {
				return DBBPTree.this.size();
			}

			@Override
			public Object[] toArray() {
				final Object[] o = new Object[this.size()];
				final Iterator<K> kit = this.iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = kit.next();
				}
				return o;
			}

			@Override
			public <T> T[] toArray(final T[] arg0) {
				final T[] o = (T[]) new Object[this.size()];
				final Iterator<K> kit = this.iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = (T) kit.next();
				}
				return o;
			}
		};
		}
	}

	@Override
	public K lastKey() {
		throw (new UnsupportedOperationException("lastKey is not supported."));
	}

	@Override
	public SortedMap<K, V> subMap(final K arg0, final K arg1) {
		throw (new UnsupportedOperationException("subMap is not supported."));
	}

	@Override
	public SortedMap<K, V> tailMap(final K arg0) {
		throw (new UnsupportedOperationException("tailMap is not supported."));
	}

	@Override
	public Collection<V> values() {
		if (this.firstLeafPage < 0) {
			return new HashSet<V>();
		} else {
			return new Collection<V>() {
			@Override
			public boolean add(final V arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			@Override
			public boolean addAll(final Collection<? extends V> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			@Override
			public void clear() {
				DBBPTree.this.clear();
			}

			@Override
			public boolean contains(final Object arg0) {
				return DBBPTree.this.containsKey(arg0);
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
				return DBBPTree.this.isEmpty();
			}

			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<java.util.Map.Entry<K, V>> it = DBBPTree.this
					.entrySet().iterator();

					@Override
					public boolean hasNext() {
						return this.it.hasNext();
					}

					@Override
					public V next() {
						final java.util.Map.Entry<K, V> me = this.it.next();
						if (me != null) {
							return me.getValue();
						} else {
							return null;
						}
					}

					@Override
					public void remove() {
						this.it.remove();
					}
				};
			}

			@Override
			public boolean remove(final Object arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			@Override
			public boolean removeAll(final Collection<?> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			@Override
			public boolean retainAll(final Collection<?> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			@Override
			public int size() {
				return DBBPTree.this.size();
			}

			@Override
			public Object[] toArray() {
				final Object[] o = new Object[this.size()];
				final Iterator<V> kit = this.iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = kit.next();
				}
				return o;
			}

			@Override
			public <T> T[] toArray(final T[] arg0) {
				final T[] o = (T[]) new Object[this.size()];
				final Iterator<V> kit = this.iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = (T) kit.next();
				}
				return o;
			}
		};
		}
	}

	@Override
	public void clear() {
		FileHelper.deleteFile(DBBPTree.mainFolder+this.currentID+ ".dbbptree_*");
		final File f = new File(DBBPTree.mainFolder);
		f.mkdirs();
		try {
			this.pageManager.close();
			this.pageManager = new PageManager(DBBPTree.mainFolder + this.currentID
					+ ".dbbptree");
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		this.size = 0;
		this.rootPage = -1;
		this.firstLeafPage = -1;
	}

	@Override
	public boolean containsKey(final Object arg0) {
		return (this.get(arg0) != null);
	}

	@Override
	public boolean containsValue(final Object arg0) {
		for (final V v : this.values()) {
			if (v.equals(arg0)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(final Object arg0) {
		return this.get(arg0, this.rootPage);
	}

	protected DBBPTreeEntry<K, V> getNextLeafEntry(
			final LuposObjectInputStream<V> in, final K lastKey,
			final V lastValue) {
		return this.nodeDeSerializer.getNextLeafEntry(in, lastKey, lastValue);
	}

	private V get(final Object arg0, final int filename) {

		if (filename < 0 || this.size == 0) {
			return null;
		}
		InputStream fis;
		try {
			fis = new PageInputStream(filename, this.pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader<V>(
					fis, null);
			final boolean leaf = InputHelper.readLuposBoolean(in.is);
			if (leaf) { // leaf node reached!
				K lastKey = null;
				V lastValue = null;
				while (true) {
					final DBBPTreeEntry<K, V> e = this.getNextLeafEntry(in, lastKey,
							lastValue);
					if (e == null || e.key == null) {
						in.close();
						return null;
					}
					final int compare = this.comparator.compare(e.key, (K) arg0);
					lastKey = e.key;
					lastValue = e.value;
					if (compare == 0) {
						in.close();
						return e.value;
					} else if (compare > 0) {
						in.close();
						return null;
					}
				}
			} else {
					K lastKey=null;
					while (true) {

						final Tuple<K, Integer> nextEntry=this.nodeDeSerializer.getNextInnerNodeEntry(lastKey, in);

						if(nextEntry==null){
							in.close();
							return null;
						}

						final int nextFilename=nextEntry.getSecond();

						if(nextEntry.getSecond()<0){
							in.close();
							return null;
						}

						final K nextKey = nextEntry.getFirst();
						if (nextKey == null) {
							in.close();
							return this.get(arg0, nextFilename);
						}
						final int compare = this.comparator.compare(nextKey,
								(K) arg0);
						if (compare >= 0) {
							in.close();
							return this.get(arg0, nextFilename);
						}
						lastKey=nextKey;
					}
			}

		} catch (final FileNotFoundException e) {
			System.err.println("Page "+filename+" of DBBPTree "+this.currentID);
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			System.err.println("Page "+filename+" of DBBPTree "+this.currentID);
			e.printStackTrace();
			System.err.println(e);
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return (this.size == 0);
	}

	protected int newFilename() {
		return this.pageManager.getNumberOfNewPage();
	}

	@Override
	public V put(final K arg0, final V arg1) {
		this.keyClass = (Class<? super K>) arg0.getClass();
		this.valueClass = (Class<? super V>) arg1.getClass();
		if (this.rootPage < 0 || this.size == 0) {
			// just create one new leaf node as root node of the B+-tree...
			this.rootPage = this.newFilename();
			this.firstLeafPage = this.rootPage;
			try {
				final OutputStream fosRoot = new PageOutputStream(this.rootPage, this.pageManager, true);
				final LuposObjectOutputStreamWithoutWritingHeader outRoot = new LuposObjectOutputStreamWithoutWritingHeader(fosRoot);
				OutHelper.writeLuposBoolean(true, outRoot.os);
				this.writeLeafEntry(arg0, arg1, outRoot, null, null);
				this.size = 1;
				outRoot.close();
			} catch (final FileNotFoundException e) {
				System.err.println(e);
				e.printStackTrace();
				this.rootPage = -1;
				this.firstLeafPage = -1;
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
				this.rootPage = -1;
				this.firstLeafPage = -1;
			}
		} else {
			final List<Node<K, V>> navCol = this.navigateTo(arg0, this.rootPage,
					new LinkedList<Node<K, V>>());
			if (navCol == null) {
				System.err.println("Error while navigating to insertion position.");
			} else {
				final Node<K, V> navigateToClass = navCol
				.get(navCol.size() - 1);
				if (navigateToClass == null) {
					System.err.println("Error while navigating to insertion position.");
				} else {
					final LeafNode<K, V> leafNode = (LeafNode<K, V>) navigateToClass;
					int pos = leafNode.readValues.size();
					leafNode.readFullLeafNode();
					V oldValue;
					if (leafNode.found) {
						// replace value!
						oldValue = leafNode.readValues.get(pos - 1);
						leafNode.readValues.set(pos - 1, arg1);
						this.closeInputStreams(navCol);
						leafNode.writeLeafNode(true);
						return oldValue;
					} else {
						if (pos > 0) {
							if (arg0.compareTo(leafNode.getKeys().get(pos - 1)) < 0) {
								pos--;
							}
						}
						final int pos2 = leafNode.readValues.size();
						// add node!
						leafNode.readKeys.add(pos, arg0);
						leafNode.readValues.add(pos, arg1);
						this.size++;

						if (pos2 + 1 > 2 * this.k_) {
							// split leaf node!
							final LeafNode<K, V> newLeafNode = new LeafNode<K, V>(
									this.keyClass, this.valueClass, this.k, this.pageManager,
									this.nodeDeSerializer);
							newLeafNode.filename = this.newFilename();
							newLeafNode.nextLeafNode = leafNode.nextLeafNode;
							leafNode.nextLeafNode = newLeafNode.filename;
							for (int i = this.k_ + 1; i < leafNode.readKeys.size(); i++) {
								newLeafNode.readKeys.add(leafNode.readKeys
										.get(i));
								newLeafNode.readValues.add(leafNode.readValues
										.get(i));
							}
							for (int i = leafNode.readKeys.size() - 1; i > this.k_; i--) {
								leafNode.readKeys.remove(i);
								leafNode.readValues.remove(i);
							}
							leafNode.writeLeafNode(true);
							try {
								leafNode.in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
							newLeafNode.writeLeafNode(false);
							int leftNodeFilename = leafNode.filename;
							int rightNodeFilename = newLeafNode.filename;
							K key = leafNode.readKeys.get(leafNode.readKeys
									.size() - 1);
							int posInNavCol = navCol.size() - 1;
							while (posInNavCol >= 0) {
								try {
									navCol.get(posInNavCol).in.close();
								} catch (final IOException e) {
									System.err.println(e);
									e.printStackTrace();
								}
								navCol.remove(posInNavCol);
								posInNavCol--;
								if (posInNavCol < 0) {
									final InnerNode<K, V> innerNode = new InnerNode<K, V>(
											this.keyClass, this.valueClass, this.k,
											this.pageManager, this.nodeDeSerializer);
									innerNode.filename = this.newFilename();
									innerNode.readReferences
									.add(leftNodeFilename);
									innerNode.readKeys.add(key);
									innerNode.readReferences
									.add(rightNodeFilename);
									innerNode.writeInnerNode(true);
									this.rootPage = innerNode.filename;
									break;
								}
								InnerNode<K, V> innerNode = (InnerNode<K, V>) navCol
								.get(posInNavCol);
								int posInnerNode = innerNode.readReferences.size();
								innerNode.readFullInnerNode();
								int posInnerNode2 = innerNode.readReferences
								.size();
								if (pos2 == pos
										&& posInnerNode - 1 < innerNode.readKeys
										.size()) {
									innerNode.readKeys.set(posInnerNode - 1,
											arg0);
								}
								innerNode.readReferences.set(
										posInnerNode == 0 ? 0
												: posInnerNode - 1,
												rightNodeFilename);
								innerNode.readKeys.add(posInnerNode == 0 ? 0
										: posInnerNode - 1, key);
								innerNode.readReferences.add(
										posInnerNode == 0 ? 0
												: posInnerNode - 1,
												leftNodeFilename);
								if (innerNode.readKeys.size() > 2 * this.k) {
									// split node!
									final InnerNode<K, V> newInnerNode = new InnerNode<K, V>(
											this.keyClass, this.valueClass, this.k,
											this.pageManager, this.nodeDeSerializer);
									newInnerNode.filename = this.newFilename();
									for (int i = this.k + 1; i < innerNode.readKeys
									.size(); i++) {
										newInnerNode.readKeys
										.add(innerNode.readKeys.get(i));
										newInnerNode.readReferences
										.add(innerNode.readReferences
												.get(i));
									}
									newInnerNode.readReferences
									.add(innerNode.readReferences
											.get(innerNode.readReferences
													.size() - 1));

									innerNode.readReferences
									.remove(innerNode.readReferences
											.size() - 1);
									for (int i = innerNode.readKeys.size() - 1; i > this.k; i--) {
										innerNode.readKeys.remove(i);
										innerNode.readReferences.remove(i);
									}
									innerNode.readKeys.remove(this.k);

									innerNode.writeInnerNode(true);
									try {
										innerNode.in.close();
									} catch (final IOException e) {
										System.err.println(e);
										e.printStackTrace();
									}
									newInnerNode.writeInnerNode(false);

									leftNodeFilename = innerNode.filename;
									rightNodeFilename = newInnerNode.filename;
									key = this.rightMost(innerNode);
								} else {
									innerNode.writeInnerNode(true);
									if (pos2 == pos) {
										try {
											innerNode.in.close();
										} catch (final IOException e) {
											System.err.println(e);
											e.printStackTrace();
										}
										navCol.remove(navCol.size() - 1);
										for (int i = navCol.size() - 1; i >= 0; i--) {
											innerNode = (InnerNode<K, V>) navCol
											.get(i);
											posInnerNode = innerNode.readReferences
											.size();
											innerNode.readFullInnerNode();
											posInnerNode2 = innerNode.readReferences
											.size();
											if (posInnerNode - 1 < innerNode.readKeys
													.size()) {
												innerNode.readKeys.set(
														posInnerNode - 1, arg0);
												innerNode.writeInnerNode(true);
											}
											try {
												innerNode.in.close();
											} catch (final IOException e) {
												System.err.println(e);
												e.printStackTrace();
											}
											navCol.remove(i);
											if (posInnerNode < posInnerNode2) {
												break;
											}
										}
									}
									break;
								}
							}
						} else {
							// we do not have to split the leaf node!
							leafNode.writeLeafNode(true);
							try {
								leafNode.in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
							navCol.remove(navCol.size() - 1);
							if (pos2 == pos) {
								// we may have to update the key(s) in the inner
								// nodes
								for (int i = navCol.size() - 2; i >= 0; i--) {
									final InnerNode<K, V> innerNode = (InnerNode<K, V>) navCol
									.get(i);
									final int posInnerNode = innerNode.readReferences
									.size();
									innerNode.readFullInnerNode();
									final int posInnerNode2 = innerNode.readReferences
									.size();
									if (posInnerNode - 1 < innerNode.readKeys
											.size()) {
										innerNode.readKeys.set(
												posInnerNode - 1, arg0);
										innerNode.writeInnerNode(true);
									}
									try {
										innerNode.in.close();
									} catch (final IOException e) {
										System.err.println(e);
										e.printStackTrace();
									}
									navCol.remove(i);
									if (posInnerNode < posInnerNode2) {
										break;
									}
								}
							}
						}
						this.closeInputStreams(navCol);
						return null;
					}
				}
			}
		}
		return null;
	}

	private K rightMost(final Node<K, V> node) {
		// determine rightmost entry!
		InnerNode<K, V> rightMostInnerNode;
		LeafNode<K, V> rightMostLeaf;
		if (node instanceof LeafNode) {
			rightMostLeaf = (LeafNode<K, V>) node;
			rightMostInnerNode = null;
		} else {
			rightMostLeaf = null;
			rightMostInnerNode = (InnerNode<K, V>) node;
		}
		while (rightMostLeaf == null) {
			final int rightMostFilename = rightMostInnerNode.getReferences()
			.get(rightMostInnerNode.getReferences().size() - 1);
			final Node<K, V> n = this.getNode(rightMostFilename);
			if (n instanceof LeafNode) {
				rightMostLeaf = (LeafNode<K, V>) n;
			} else {
				rightMostInnerNode = (InnerNode<K, V>) n;
			}
		}
		return rightMostLeaf.readKeys.get(rightMostLeaf.readKeys.size() - 1);
	}

	public Node<K, V> getNode(final int filename) {
		InputStream fis;
		try {
			fis = new PageInputStream(filename, this.pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader<V>(
					fis, null);
			final boolean leaf = InputHelper.readLuposBoolean(in.is);
			if (leaf) {
				final LeafNode<K, V> leafNode = new LeafNode<K, V>(this.keyClass,
						this.valueClass, this.k_, this.pageManager, this.nodeDeSerializer);
				leafNode.filename = filename;
				leafNode.in = in;
				leafNode.readFullLeafNode();
				in.close();
				return leafNode;
			} else {
				final InnerNode<K, V> innerNode = new InnerNode<K, V>(this.keyClass,
						this.valueClass, this.k, this.pageManager, this.nodeDeSerializer);
				innerNode.filename = filename;
				innerNode.in = in;
				innerNode.readFullInnerNode();
				in.close();
				return innerNode;
			}
		} catch (final FileNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	protected void writeLeafEntry(final K k, final V v,
			final LuposObjectOutputStream out, final K lastKey,
			final V lastValue) throws IOException {
		this.nodeDeSerializer.writeLeafEntry(k, v, out, lastKey, lastValue);
	}

	protected void closeInputStreams(
			final Collection<Node<K, V>> currentCollection) {
		for (final Node<K, V> navigateToClass : currentCollection) {
			try {
				navigateToClass.in.close();
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	protected List<Node<K, V>> navigateTo(final Object arg0,
			final int filename, final List<Node<K, V>> currentCollection) {
		if (filename < 0) {
			return null;
		}
		InputStream fis;
		try {
			fis = new PageInputStream(filename, this.pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
					fis, null);
			final boolean leaf = InputHelper.readLuposBoolean(in.is);
			if (leaf) { // leaf node reached!
				final LeafNode<K, V> navigateToClassLeafNode = new LeafNode<K, V>(
						this.keyClass, this.valueClass, this.k_, this.pageManager, this.nodeDeSerializer);
				navigateToClassLeafNode.filename = filename;
				navigateToClassLeafNode.in = in;
				currentCollection.add(navigateToClassLeafNode);
				K lastKey = null;
				V lastValue = null;
				while (true) {
					final DBBPTreeEntry<K, V> e = this.getNextLeafEntry(in, lastKey,
							lastValue);
					if (e != null && e.filenameOfNextLeafNode >= 0) {
						navigateToClassLeafNode.nextLeafNode = e.filenameOfNextLeafNode;
						navigateToClassLeafNode.found = false;
						return currentCollection;
					}
					if (e == null || e.key == null) {
						return currentCollection;
					}
					lastKey = e.key;
					lastValue = e.value;
					navigateToClassLeafNode.readKeys.add(e.key);
					if (e.value != null) {
						navigateToClassLeafNode.readValues.add(e.value);
					}
					final int compare = this.comparator.compare(e.key, (K) arg0);
					if (compare == 0) {
						navigateToClassLeafNode.found = true;
						return currentCollection;
					} else if (compare > 0) {
						navigateToClassLeafNode.found = false;
						return currentCollection;
					}
				}
			} else {
					final InnerNode<K, V> navigateToClassInnerNode = new InnerNode<K, V>(
							this.keyClass, this.valueClass, this.k, this.pageManager,
							this.nodeDeSerializer);
					navigateToClassInnerNode.filename = filename;
					navigateToClassInnerNode.in = in;
					currentCollection.add(navigateToClassInnerNode);
					K lastKey=null;
					while (true) {

						final Tuple<K, Integer> nextEntry=this.nodeDeSerializer.getNextInnerNodeEntry(lastKey, in);

						if(nextEntry==null){
							this.closeInputStreams(currentCollection);
							return null;
						}

						final int nextFilename=nextEntry.getSecond();

						if(nextEntry.getSecond()<0){
							this.closeInputStreams(currentCollection);
							return null;
						}

						navigateToClassInnerNode.readReferences
						.add(nextFilename);

						final K nextKey = nextEntry.getFirst();
						if (nextKey == null) {
							return this.navigateTo(arg0, nextFilename,
									currentCollection);
						}
						navigateToClassInnerNode.readKeys.add(nextKey);
						final int compare = this.comparator.compare(nextKey,
								(K) arg0);
						if (compare >= 0) {
							return this.navigateTo(arg0, nextFilename,
									currentCollection);
						}
						lastKey=nextKey;
					}
			}

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
		return null;
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> arg0) {
		for (final K k : arg0.keySet()) {
			this.put(k, arg0.get(k));
		}
	}

	@Override
	public V remove(final Object arg0) {
		if (this.rootPage < 0) {
			return null;
		}
		final List<Node<K, V>> navCol = this.navigateTo(arg0, this.rootPage,
				new LinkedList<Node<K, V>>());
		if (navCol == null) {
			System.err.println("Error while navigating to insertion position.");
		} else {
			final Node<K, V> navigateToClass = navCol.get(navCol.size() - 1);
			if (navigateToClass == null) {
				System.err.println("Error while navigating to insertion position.");
			} else {
				final LeafNode<K, V> leafNode = (LeafNode<K, V>) navigateToClass;
				if (!leafNode.found) {
					return null;
				} else {
					final int pos = leafNode.readValues.size();
					leafNode.readFullLeafNode();
					// final int pos2 = leafNode.readValues.size();
					leafNode.readKeys.remove(pos - 1);
					final V oldValue = leafNode.readValues.remove(pos - 1);
					if (leafNode.readKeys.size() >= this.k_ || navCol.size()==1) {
						// this leaf node can remain and does not have to be
						// deleted!
						leafNode.writeLeafNode(true);
						return oldValue;
					} else {
						Node<K, V> currentNode = leafNode;
						while (navCol.size() > 0) {
							navCol.remove(navCol.size() - 1);
							if(navCol.size()>0){
								final InnerNode<K, V> innerNode = (InnerNode<K, V>) navCol
								.get(navCol.size() - 1);
								// int posKeys=innerNode.readKeys.size();
								innerNode.readFullInnerNode();
								// borrow from left neighbor?
								final int posOfCurrentNode = innerNode
								.getReferences().indexOf(
										currentNode.filename);
								if (posOfCurrentNode > 0) {
									final Node<K, V> leftNeighbor = this.getNode(innerNode
											.getReferences().get(
													posOfCurrentNode - 1));
									final int comp = leftNeighbor instanceof LeafNode ? this.k_
											: this.k;
									if (leftNeighbor.getKeys().size() > comp) {
										final K k = leftNeighbor.getKeys().remove(
												leftNeighbor.getKeys().size() - 1);
										if (leftNeighbor instanceof LeafNode) {
											final V v = ((LeafNode<K, V>) leftNeighbor)
											.getValues()
											.remove(
													((LeafNode<K, V>) leftNeighbor)
													.getValues()
													.size() - 1);
											leafNode.getValues().add(0, v);
											leafNode.getKeys().add(0, k);
											innerNode
											.getKeys()
											.set(
													posOfCurrentNode == 0 ? 0
															: posOfCurrentNode - 1,
															((LeafNode<K, V>) leftNeighbor)
															.getKeys()
															.get(
																	((LeafNode<K, V>) leftNeighbor)
																	.getKeys()
																	.size() - 1));
											leafNode.writeLeafNode(true);
											((LeafNode<K, V>) leftNeighbor)
											.writeLeafNode(true);
										} else {
											final int ref = ((InnerNode<K, V>) leftNeighbor)
											.getReferences()
											.remove(
													((InnerNode<K, V>) leftNeighbor)
													.getReferences()
													.size() - 1);
											((InnerNode<K, V>) currentNode)
											.getReferences().add(0, ref);
											currentNode.getKeys().add(0,
													this.rightMost(this.getNode(ref)));
											innerNode.getKeys().set(
													posOfCurrentNode == 0 ? 0
															: posOfCurrentNode - 1,
															k);
											((InnerNode<K, V>) currentNode)
											.writeInnerNode(true);
											((InnerNode<K, V>) leftNeighbor)
											.writeInnerNode(true);
										}
										innerNode.writeInnerNode(true);
										return oldValue;
									}
								}
								// borrow from right neighbor?
								if (posOfCurrentNode < innerNode.getReferences()
										.size() - 1) {
									final Node<K, V> rightNeighbor = this.getNode(innerNode
											.getReferences().get(
													posOfCurrentNode + 1));
									final int comp = rightNeighbor instanceof LeafNode ? this.k_
											: this.k;
									if (rightNeighbor.getKeys().size() > comp) {
										final K k = rightNeighbor.getKeys().remove(
												0);
										innerNode.getKeys()
										.set(posOfCurrentNode, k);
										if (rightNeighbor instanceof LeafNode) {
											final V v = ((LeafNode<K, V>) rightNeighbor)
											.getValues().remove(0);
											leafNode.getValues().add(
													leafNode.getValues().size(), v);
											leafNode.getKeys().add(
													leafNode.getKeys().size(), k);
											leafNode.writeLeafNode(true);
											((LeafNode<K, V>) rightNeighbor)
											.writeLeafNode(true);
										} else {
											currentNode.getKeys().add(
													currentNode.getKeys().size(),
													this.rightMost(currentNode));
											final int ref = ((InnerNode<K, V>) rightNeighbor)
											.getReferences().remove(0);
											((InnerNode<K, V>) currentNode)
											.getReferences()
											.add(
													((InnerNode<K, V>) currentNode)
													.getReferences()
													.size(), ref);
											((InnerNode<K, V>) currentNode)
											.writeInnerNode(true);
											((InnerNode<K, V>) rightNeighbor)
											.writeInnerNode(true);
										}
										innerNode.writeInnerNode(true);
										return oldValue;
									} else {
										// nothing can be borrowed => merge with
										// right neighbor!
										if (rightNeighbor instanceof InnerNode) {
											for (int i = ((InnerNode<K, V>) currentNode)
													.getReferences().size() - 1; i >= 0; i--) {
												((InnerNode<K, V>) rightNeighbor)
												.getReferences()
												.add(
														0,
														((InnerNode<K, V>) currentNode)
														.getReferences()
														.get(i));
											}
											rightNeighbor
											.getKeys()
											.add(
													0,
													this.rightMost(this.getNode(((InnerNode<K, V>) currentNode)
															.getReferences()
															.get(
																	((InnerNode<K, V>) currentNode)
																	.getReferences()
																	.size() - 1))));
											for (int i = currentNode.getKeys()
													.size() - 1; i >= 0; i--) {
												rightNeighbor.getKeys().add(
														0,
														currentNode.getKeys()
														.get(i));
											}
											((InnerNode<K, V>) rightNeighbor)
											.writeInnerNode(true);
										} else {
											for (int i = ((LeafNode<K, V>) currentNode)
													.getValues().size() - 1; i >= 0; i--) {
												((LeafNode<K, V>) rightNeighbor)
												.getValues()
												.add(
														0,
														((LeafNode<K, V>) currentNode)
														.getValues()
														.get(i));
											}
											for (int i = currentNode.getKeys()
													.size() - 1; i >= 0; i--) {
												rightNeighbor.getKeys().add(
														0,
														currentNode.getKeys()
														.get(i));
											}
											// update pointers at leaf nodes!
											if (posOfCurrentNode > 0) {
												final LeafNode<K, V> leftNeighbor = (LeafNode<K, V>) this.getNode(innerNode.readReferences
														.get(posOfCurrentNode - 1));
												leftNeighbor.nextLeafNode = rightNeighbor.filename;
												leftNeighbor.writeLeafNode(true);
											}
											((LeafNode<K, V>) rightNeighbor)
											.writeLeafNode(true);
										}
										innerNode.getKeys()
										.remove(posOfCurrentNode);
										innerNode.getReferences().remove(
												posOfCurrentNode);
										innerNode.writeInnerNode(true);
										if (this.firstLeafPage == currentNode.filename) {
											this.firstLeafPage = rightNeighbor.filename;
										}
										try {
											this.pageManager
											.releaseSequenceOfPages(currentNode.filename);
										} catch (final IOException e) {
											System.err.println(e);
											e.printStackTrace();
										}
										if (innerNode.getKeys().size() >= this.k) {
											return oldValue;
										}
										if (innerNode.filename == this.rootPage) {
											if (innerNode.getKeys().size() == 0
													&& rightNeighbor instanceof InnerNode) {
												try {
													this.pageManager
													.releaseSequenceOfPages(this.rootPage);
												} catch (final IOException e) {
													System.err.println(e);
													e.printStackTrace();
												}
												this.rootPage = rightNeighbor.filename;
												return oldValue;
											}
											return oldValue;
										}
										// handle case innerNode has too less
										// entries!
										currentNode = innerNode;
										continue;
									}
								}
								// special case: merge with left neighbor!
								if (posOfCurrentNode > 0) {
									final Node<K, V> leftNeighbor = this.getNode(innerNode
											.getReferences().get(
													posOfCurrentNode - 1));
									if (leftNeighbor instanceof InnerNode) {
										leftNeighbor
										.getKeys()
										.add(
												this.rightMost(this.getNode(((InnerNode<K, V>) leftNeighbor)
														.getReferences()
														.get(
																((InnerNode<K, V>) leftNeighbor)
																.getReferences()
																.size() - 1))));
										for (int i = 0; i < ((InnerNode<K, V>) currentNode)
										.getReferences().size(); i++) {
											((InnerNode<K, V>) leftNeighbor)
											.getReferences()
											.add(
													((InnerNode<K, V>) currentNode)
													.getReferences()
													.get(i));
										}
										for (int i = 0; i < currentNode.getKeys()
										.size(); i++) {
											leftNeighbor.getKeys().add(
													currentNode.getKeys().get(i));
										}
										((InnerNode<K, V>) leftNeighbor)
										.writeInnerNode(true);
									} else {
										for (int i = 0; i < ((LeafNode<K, V>) currentNode)
										.getValues().size(); i++) {
											((LeafNode<K, V>) leftNeighbor)
											.getValues()
											.add(
													((LeafNode<K, V>) currentNode)
													.getValues()
													.get(i));
										}
										for (int i = 0; i < currentNode.getKeys()
										.size(); i++) {
											leftNeighbor.getKeys().add(
													currentNode.getKeys().get(i));
										}
										// update pointers at leaf nodes!
										((LeafNode<K, V>) leftNeighbor).nextLeafNode = ((LeafNode<K, V>) currentNode).nextLeafNode;
										((LeafNode<K, V>) leftNeighbor)
										.writeLeafNode(true);
									}
									innerNode
									.getKeys()
									.remove(
											posOfCurrentNode < innerNode
											.getKeys().size() - 1 ? posOfCurrentNode
													: innerNode.getKeys()
													.size() - 1);
									innerNode.getReferences().remove(
											posOfCurrentNode);
									innerNode.writeInnerNode(true);
									if (this.firstLeafPage == currentNode.filename) {
										this.firstLeafPage = leftNeighbor.filename;
									}
									try {
										this.pageManager
										.releaseSequenceOfPages(currentNode.filename);
									} catch (final IOException e) {
										System.err.println(e);
										e.printStackTrace();
									}
									if (innerNode.getKeys().size() >= this.k) {
										return oldValue;
									}
									if (innerNode.filename == this.rootPage) {
										if (innerNode.getKeys().size() == 0
												&& leftNeighbor instanceof InnerNode) {
											try {
												this.pageManager
												.releaseSequenceOfPages(this.rootPage);
											} catch (final IOException e) {
												System.err.println(e);
												e.printStackTrace();
											}
											this.rootPage = leftNeighbor.filename;
											return oldValue;
										}
										return oldValue;
									}
									// handle case innerNode has too less entries!
									currentNode = innerNode;
									continue;
								}

								if (currentNode instanceof LeafNode) {
									((LeafNode<K, V>) currentNode)
									.writeLeafNode(true);
								} else {
									((InnerNode<K, V>) currentNode)
									.writeInnerNode(true);
								}
								return oldValue;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public int size() {
		return this.size;
	}

	public void writeInnerNodeEntry(final int fileName, final K key,
			final LuposObjectOutputStream out, final K lastKey)
	throws IOException {
		this.nodeDeSerializer.writeInnerNodeEntry(fileName, key, out, lastKey);
	}

	public void writeInnerNodeEntry(final int fileName,
			final LuposObjectOutputStream out) throws IOException {
		this.nodeDeSerializer.writeInnerNodeEntry(fileName, out);
	}

	public void generateDBBPTree(final Generator<K, V> generator) throws IOException {
		this.generateDBBPTree(new SortedMap<K, V>() {
			@Override
			public Comparator<? super K> comparator() {
				return null;
			}

			@Override
			public Set<java.util.Map.Entry<K, V>> entrySet() {
				return new Set<java.util.Map.Entry<K, V>>() {
					@Override
					public boolean add(final java.util.Map.Entry<K, V> e) {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
						throw new UnsupportedOperationException();
					}

					@Override
					public void clear() {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean contains(final Object o) {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean containsAll(final Collection<?> c) {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean isEmpty() {
						throw new UnsupportedOperationException();
					}

					@Override
					public Iterator<java.util.Map.Entry<K, V>> iterator() {
						return generator.iterator();
					}

					@Override
					public boolean remove(final Object o) {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean removeAll(final Collection<?> c) {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean retainAll(final Collection<?> c) {
						throw new UnsupportedOperationException();
					}

					@Override
					public int size() {
						throw new UnsupportedOperationException();
					}

					@Override
					public Object[] toArray() {
						throw new UnsupportedOperationException();
					}

					@Override
					public <T> T[] toArray(final T[] a) {
						throw new UnsupportedOperationException();
					}
				};
			}

			@Override
			public K firstKey() {
				throw new UnsupportedOperationException();
			}

			@Override
			public SortedMap<K, V> headMap(final K toKey) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Set<K> keySet() {
				throw new UnsupportedOperationException();
			}

			@Override
			public K lastKey() {
				throw new UnsupportedOperationException();
			}

			@Override
			public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
				throw new UnsupportedOperationException();
			}

			@Override
			public SortedMap<K, V> tailMap(final K fromKey) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Collection<V> values() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsKey(final Object key) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean containsValue(final Object value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public V get(final Object key) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isEmpty() {
				throw new UnsupportedOperationException();
			}

			@Override
			public V put(final K key, final V value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void putAll(final Map<? extends K, ? extends V> m) {
				throw new UnsupportedOperationException();
			}

			@Override
			public V remove(final Object key) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int size() {
				return generator.size();
			}
		});
	}

	public void generateDBBPTree(final SortedMap<K, V> sortedMap) throws IOException {
		this.pageManager.reset();
		final LinkedList<Container> innerNodes = new LinkedList<Container>();
		this.size = sortedMap.size();
		final Container leaf = new Container(this.size, this.k_, true);
		this.firstLeafPage = leaf.getFileName();
		if (sortedMap.comparator() != null) {
			this.comparator = sortedMap.comparator();
		}
		final Iterator<Entry<K, V>> it = sortedMap.entrySet().iterator();

		while (it.hasNext()) {
			final Entry<K, V> entry = it.next();
			if (leaf.newNodeForNextEntry()) {
				leaf.closeNode(innerNodes);
			}
			leaf.storeInLeafNode(entry);
		}
		if (it instanceof ParallelIterator) {
			((ParallelIterator) it).close();
		}
		leaf.close();
		Container previous = leaf;
		for (final Container container : innerNodes) {
			container.storeInInnerNode(previous.filename);
			container.close();
			previous = container;
		}
		this.rootPage = previous.filename;
		this.pageManager.writeAllModifiedPages();
	}

	protected class Container {
		private LuposObjectOutputStream out = null;
		private int filename;
		private int currentEntry = 0;
		private final double factor;
		private Entry<K, V> lastStoredEntry;
		private final long numberOfNodes;
		private double limitNextNode;
		private final boolean leaf;
		private K lastKey = null;
		private V lastValue = null;

		public Container(final long numberOfEntries, final int kk_,
				final boolean leaf) {
			this.leaf = leaf;
			this.filename = DBBPTree.this.newFilename();
			this.init();
			this.numberOfNodes = Math.round(Math
					.ceil((double) numberOfEntries / kk_));
			this.factor = (double) numberOfEntries / this.numberOfNodes;
			this.limitNextNode = this.factor;
		}

		protected void init() {
			try {
				if (this.out != null) {
					this.out.close();
				}
				final OutputStream fos = new PageOutputStream(this.filename,
						DBBPTree.this.pageManager, true);
				this.out = new LuposObjectOutputStreamWithoutWritingHeader(fos);
				OutHelper.writeLuposBoolean(this.leaf,this.out.os);
				this.lastKey = null;
				this.lastValue = null;
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public int getFileName() {
			return this.filename;
		}

		public boolean newNodeForNextEntry() {
			if (this.currentEntry + 1 > this.limitNextNode) {
				return true;
			} else {
				return false;
			}
		}

		public void storeInLeafNode(final Entry<K, V> entry) {
			this.lastStoredEntry = entry;
			this.currentEntry++;
			DBBPTree.this.keyClass = (Class<? super K>) entry.getKey().getClass();
			DBBPTree.this.valueClass = (Class<? super V>) entry.getValue().getClass();
			try {
				DBBPTree.this.writeLeafEntry(entry.getKey(), entry.getValue(), this.out, this.lastKey,
						this.lastValue);
				this.lastKey = entry.getKey();
				this.lastValue = entry.getValue();
				// System.out.println("leaf "+ filename
				// +" ("+entry.getKey()+", "+entry.getValue()+")");
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void storeInInnerNode(final int fileName, final Entry<K, V> entry) {
			this.currentEntry++;
			try {
				DBBPTree.this.writeInnerNodeEntry(fileName, entry.getKey(), this.out, this.lastKey);
				this.lastKey = entry.getKey();
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		public void storeInInnerNode(final int fileName) {
			try {
				DBBPTree.this.writeInnerNodeEntry(fileName, this.out);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void closeNode(final LinkedList<Container> innerNodes) {
			this.addToInnerNodes(innerNodes, 0, this, this.filename,
					this.lastStoredEntry);
			this.filename = DBBPTree.this.newFilename();
			DBBPTree.this.writeLeafEntryNextFileName(this.filename, this.out);

			this.init();
			this.limitNextNode = this.currentEntry + this.factor;
		}

		public void close() {
			try {
				this.out.close();
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void addToInnerNodes(final LinkedList<Container> innerNodes,
				final int position, Container previous, final int filename,
				final Entry<K, V> lastStoredEntry) {
			while (innerNodes.size() < position + 1) {
				final Container container = new Container(this.numberOfNodes - 1, DBBPTree.this.k,
						false);
				previous = container;
				innerNodes.add(container);
			}
			final Container container = innerNodes.get(position);
			if (container.newNodeForNextEntry()) {
				container.storeInInnerNode(filename);
				this.addToInnerNodes(innerNodes, position + 1, container,
						container.filename, lastStoredEntry);
				container.filename = DBBPTree.this.newFilename();
				container.init();
				container.limitNextNode = container.currentEntry
				+ container.factor;
			} else {
				container.storeInInnerNode(filename, lastStoredEntry);
			}
		}
	}

	public void writeLuposObject(final LuposObjectOutputStream loos)
	throws IOException {
		this.pageManager.writeAllModifiedPages();
		OutHelper.writeLuposInt(this.currentID, loos.os);
		OutHelper.writeLuposInt(this.k, loos.os);
		OutHelper.writeLuposInt(this.k_, loos.os);
		OutHelper.writeLuposInt(this.size, loos.os);
		loos.writeObject(this.comparator);
		OutHelper.writeLuposInt(this.rootPage, loos.os);
		OutHelper.writeLuposInt(this.firstLeafPage, loos.os);
		loos.writeObject(this.keyClass);
		loos.writeObject(this.valueClass);
		loos.writeObject(this.nodeDeSerializer);
	}

	protected void writeLeafEntryNextFileName(final int filename,
			final LuposObjectOutputStream out) {
		try {
			this.nodeDeSerializer.writeLeafEntryNextFileName(filename, out);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * This constructor is only used for creating a DBBPTree after reading it
	 * from file!
	 *
	 * @param k
	 * @param k_
	 * @param size
	 * @param comp
	 * @param rootFilename
	 * @param firstLeafFileName
	 * @param keyClass
	 * @param valueClass
	 */
	protected DBBPTree(final int k, final int k_, final int size,
			final Comparator comp, final int rootFilename,
			final int firstLeafFileName, final Class keyClass,
			final Class valueClass, final int currentID,
			final NodeDeSerializer<K, V> nodeDeSerializer) throws IOException {
		this.k = k;
		this.k_ = k_;
		this.size = size;
		this.comparator = comp;
		this.rootPage = rootFilename;
		this.firstLeafPage = firstLeafFileName;
		this.keyClass = keyClass;
		this.valueClass = valueClass;
		this.currentID = currentID;
		this.nodeDeSerializer = nodeDeSerializer;
		this.pageManager = new PageManager(DBBPTree.mainFolder + currentID + ".dbbptree", false);
	}

	public static DBBPTree readLuposObject(final LuposObjectInputStream lois) throws IOException, ClassNotFoundException {
		final int currentID = InputHelper.readLuposInt(lois.is);
		final int k = InputHelper.readLuposInt(lois.is);
		final int k_ = InputHelper.readLuposInt(lois.is);
		final int size = InputHelper.readLuposInt(lois.is);
		final Comparator comp = (Comparator) lois.readObject();
		final int rootFilename = InputHelper.readLuposInt(lois.is);
		final int firstLeafFileName = InputHelper.readLuposInt(lois.is);
		final Class keyClass = (Class) lois.readObject();
		final Class valueClass = (Class) lois.readObject();
		final NodeDeSerializer nodeDeSerializer = (NodeDeSerializer) lois.readObject();
		final DBBPTree dbbptree = new DBBPTree(k, k_, size, comp, rootFilename, firstLeafFileName, keyClass, valueClass, currentID, nodeDeSerializer);
		return dbbptree;
	}

	private class PrefixSearchIteratorMaxMinWithoutSIP extends
	PrefixSearchIteratorWithoutSIP {
		final K largest;

		public PrefixSearchIteratorMaxMinWithoutSIP(final K arg0,
				final K smallest, final K largest) {
			super(arg0, smallest);
			this.largest = largest;
			if (this.next != null) {
				if (largest.compareTo(this.lastKey) < 0) {
					this.next = null;
				}
			}
		}

		@Override
		public V next() {
			final V result = this.next;
			if (result != null) {
				this.next = this.getNext();
				if (this.next != null) {
					if (this.largest.compareTo(this.lastKey) < 0) {
						this.next = null;
					}
				}
			}
			return result;
		}
	}

	private class PrefixSearchIteratorMaxMin extends PrefixSearchIterator {
		final K largest;

		public PrefixSearchIteratorMaxMin(final K arg0, final K smallest,
				final K largest) {
			super(arg0, smallest);
			this.largest = largest;
			if (this.next != null) {
				if (largest.compareTo(this.lastKey) < 0) {
					this.next = null;
				}
			}
		}

		@Override
		public V next() {
			final V result = this.next;
			if (result != null) {
				this.next = this.getNext();
				if (this.next != null) {
					if (this.largest.compareTo(this.lastKey) < 0) {
						this.next = null;
					}
				}
			}
			return result;
		}

		@Override
		public V next(final K k) {
			V result = this.next;
			if (result != null) {
				this.next = this.getNext(k);
			}
			while (result != null && k.compareTo(this.lastKey) > 0) {
				result = this.next;
				this.next = this.getNext(k);
				if (this.next != null) {
					if (this.largest.compareTo(this.lastKey) < 0) {
						this.next = null;
					}
				}
			}
			return result;
		}

	}

	private class PrefixSearchIteratorWithoutSIP implements ParallelIterator<V> {
		protected V next;
		final protected K arg0;

		public PrefixSearchIteratorWithoutSIP(final K arg0) {
			this.arg0 = arg0;
			this.lastTriple = null;
			this.lastKey = null;
			this.innerNodes = new LinkedList<Tuple<K, LuposObjectInputStream<V>>>();
			this.next = this.getFirst(DBBPTree.this.rootPage);
		}

		public PrefixSearchIteratorWithoutSIP(final K arg0, final K smallest) {
			this.arg0 = arg0;
			this.lastTriple = null;
			this.lastKey = null;
			this.innerNodes = new LinkedList<Tuple<K, LuposObjectInputStream<V>>>();
			if (smallest != null) {
				this.next = this.getFirst(DBBPTree.this.rootPage, smallest);
			} else {
				this.next = this.getFirst(DBBPTree.this.rootPage);
			}
		}

		List<Tuple<K, LuposObjectInputStream<V>>> innerNodes;
		LuposObjectInputStream<V> currentLeafIn;
		V lastTriple;
		K lastKey;

		private V getFirst(final int filename) {
			if (filename < 0) {
				return null;
			}
			InputStream fis;
			try {
				fis = new PageInputStream(filename, DBBPTree.this.pageManager);
				final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
						fis, null);
				final boolean leaf = InputHelper.readLuposBoolean(in.is);
				if (leaf) { // leaf node reached!
					while (true) {
						final DBBPTreeEntry<K, V> e = DBBPTree.this.nodeDeSerializer
						.getNextLeafEntry(in, this.lastKey, this.lastTriple);
						if (e == null || e.key == null) {
							this.currentLeafIn = in;
							this.close();
							return null;
						}
						this.lastTriple = e.value;
						this.lastKey = e.key;
						final int compare = DBBPTree.this.comparator.compare(this.lastKey, this.arg0);
						if (compare == 0) {
							this.currentLeafIn = in;
							return e.value;
						} else if (compare > 0) {
							this.currentLeafIn = in;
							this.close();
							return null;
						}
					}
				} else {
					K lastKey = null;
					while (true) {
						final Tuple<K, Integer> nextEntry = DBBPTree.this.getNextInnerNodeEntry(
								lastKey, in);
						if (nextEntry == null || nextEntry.getSecond() == 0
								|| nextEntry.getSecond() < 0) {
							in.close();
							this.close();
							return null;
						}
						if (nextEntry.getFirst() == null) {
							this.innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									null, in));
							return this.getFirst(nextEntry.getSecond());
						}
						final int compare = DBBPTree.this.comparator.compare(nextEntry
								.getFirst(), this.arg0);
						if (compare >= 0) {
							this.innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									nextEntry.getFirst(), in));
							return this.getFirst(nextEntry.getSecond());
						}
						lastKey = nextEntry.getFirst();
					}
				}

			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
			return null;
		}

		private V getFirst(final int filename, final K triplekey) {
			if (filename < 0) {
				return null;
			}
			InputStream fis;
			try {
				fis = new PageInputStream(filename, DBBPTree.this.pageManager);
				final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
						fis, null);
				final boolean leaf = InputHelper.readLuposBoolean(in.is);
				this.lastTriple = null;
				this.lastKey = null;
				if (leaf) { // leaf node reached!
					while (true) {
						final DBBPTreeEntry<K, V> e = DBBPTree.this.nodeDeSerializer
						.getNextLeafEntry(in, this.lastKey, this.lastTriple);
						if (e == null || e.key == null) {
							this.currentLeafIn = in;
							this.close();
							return null;
						}
						this.lastTriple = e.value;
						this.lastKey = e.key;
						final int compare = DBBPTree.this.comparator.compare(this.lastKey,
								triplekey);
						if (compare == 0) {
							this.currentLeafIn = in;
							return e.value;
						} else if (compare > 0) {
							if (DBBPTree.this.comparator.compare(this.lastKey, this.arg0) > 0) {
								this.currentLeafIn = in;
								this.close();
								return null;
							} else {
								this.currentLeafIn = in;
								return e.value;
							}
						}
					}
				} else {
					K lastKey = null;
					while (true) {
						final Tuple<K, Integer> nextEntry = DBBPTree.this.getNextInnerNodeEntry(
								lastKey, in);
						if (nextEntry == null || nextEntry.getSecond() <= 0) {
							in.close();
							this.close();
							return null;
						}
						lastKey = nextEntry.getFirst();
						if (nextEntry.getFirst() == null) {
							this.innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									null, in));
							return this.getFirst(nextEntry.getSecond(), triplekey);
						}
						final int compare = DBBPTree.this.comparator.compare(nextEntry
								.getFirst(), triplekey);
						if (compare >= 0) {
							this.innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									nextEntry.getFirst(), in));
							return this.getFirst(nextEntry.getSecond(), triplekey);
						}
					}
				}

			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
			return null;
		}

		protected V getFirstUsingCache(final int index, final K triplekey) {
			if (index < 0) {
				return null;
			}
			try {
				if (this.innerNodes.size() <= index) {
					this.close();
					return null;
					// close();
					// innerNodes.clear();
					// return getFirst(rootFilename, triplekey);
				}
				final Tuple<K, LuposObjectInputStream<V>> current = this.innerNodes
				.get(index);
				final LuposObjectInputStream<V> in = current.getSecond();
				K lastKey = current.getFirst();
				if (lastKey == null
						|| DBBPTree.this.comparator.compare(lastKey, triplekey) >= 0) {
					return this.getFirstUsingCache(index + 1, triplekey);
				}
				while (this.innerNodes.size() > index + 1) {
					final Tuple<K, LuposObjectInputStream<V>> toBeDeleted = this.innerNodes
					.remove(this.innerNodes.size() - 1);
					try {
						toBeDeleted.getSecond().close();
					} catch (final IOException e) {
						e.printStackTrace();
						System.err.println(e);
					}
				}
				while (true) {
					final Tuple<K, Integer> nextEntry = DBBPTree.this.getNextInnerNodeEntry(
							lastKey, in);
					if (nextEntry == null || nextEntry.getSecond() <= 0) {
						in.close();
						this.close();
						return null;
					}
					lastKey = nextEntry.getFirst();
					if (nextEntry.getFirst() == null) {
						current.setFirst(null);
						return this.getFirst(nextEntry.getSecond(), triplekey);
					}
					final int compare = DBBPTree.this.comparator.compare(
							nextEntry.getFirst(), triplekey);
					if (compare >= 0) {
						current.setFirst(nextEntry.getFirst());
						return this.getFirst(nextEntry.getSecond(), triplekey);
					}
				}

			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
			return null;
		}

		protected V getNext() {
			try {
				DBBPTreeEntry<K, V> e = DBBPTree.this.nodeDeSerializer.getNextLeafEntry(
						this.currentLeafIn, this.lastKey, this.lastTriple);
				if (e == null) {
					this.currentLeafIn.close();
					return null;
				}
				if (e.key == null) {
					// next leaf node!
					if (e.filenameOfNextLeafNode >= 0) {
						this.currentLeafIn.close();
						this.lastTriple = null;
						this.lastKey = null;
						final InputStream fis = new PageInputStream(
								e.filenameOfNextLeafNode, DBBPTree.this.pageManager);
						this.currentLeafIn = new LuposObjectInputStreamWithoutReadingHeader<V>(fis, null);
						// read over the leaf flag!
						InputHelper.readLuposBoolean(this.currentLeafIn.is);
						e = DBBPTree.this.nodeDeSerializer.getNextLeafEntry(this.currentLeafIn,
								this.lastKey, this.lastTriple);
						if (e == null || e.key == null) {
							// should never happen!
							this.currentLeafIn.close();
							return null;
						}
					} else {
						// should never happen!
						this.currentLeafIn.close();
						return null;
					}
				}
				this.lastTriple = e.value;
				this.lastKey = e.key;
				final int compare = DBBPTree.this.comparator.compare(this.lastKey, this.arg0);
				if (compare == 0) {
					return e.value;
				}
				this.close();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
			return null;
		}

		@Override
		public boolean hasNext() {
			return (this.next != null);
		}

		@Override
		public V next() {
			final V result = this.next;
			if (result != null) {
				this.next = this.getNext();
			}
			return result;
		}

		@Override
		public void remove() {
			throw (new UnsupportedOperationException(
			"This iterator is ReadOnly."));
		}

		@Override
		public void close() {
			for (final Tuple<K, LuposObjectInputStream<V>> tuple : this.innerNodes) {
				try {
					tuple.getSecond().close();
				} catch (final IOException e) {
				}
			}
			this.innerNodes.clear();
			try {
				this.currentLeafIn.close();
			} catch (final IOException e) {
			}
		}
	}

	private class PrefixSearchIterator extends PrefixSearchIteratorWithoutSIP
	implements SIPParallelIterator<V, K> {

		public PrefixSearchIterator(final K arg0) {
			super(arg0);
		}

		public PrefixSearchIterator(final K arg0, final K smallest) {
			super(arg0, smallest);
		}

		protected V getNext(final K k) {
			try {
				DBBPTreeEntry<K, V> e = DBBPTree.this.nodeDeSerializer.getNextLeafEntry(
						this.currentLeafIn, this.lastKey, this.lastTriple);
				if (e == null) {
					this.currentLeafIn.close();
					return null;
				}
				if (e.key == null) {
					// next leaf node!
					if (e.filenameOfNextLeafNode >= 0) {
						this.currentLeafIn.close();
						this.lastTriple = null;
						this.lastKey = null;
						final InputStream fis = new PageInputStream(
								e.filenameOfNextLeafNode, DBBPTree.this.pageManager);
						this.currentLeafIn = new LuposObjectInputStreamWithoutReadingHeader<V>(fis, null);
						// read over the leaf flag!
						InputHelper.readLuposBoolean(this.currentLeafIn.is);
						e = DBBPTree.this.nodeDeSerializer.getNextLeafEntry(this.currentLeafIn,
								this.lastKey, this.lastTriple);
						if (e == null || e.key == null) {
							// should never happen!
							this.close();
							return null;
						}
						this.lastTriple = e.value;
						this.lastKey = e.key;
						int compare = DBBPTree.this.comparator.compare(this.lastKey, k);
						while (compare < 0) {
							e = DBBPTree.this.nodeDeSerializer.getNextLeafEntry(
									this.currentLeafIn, this.lastKey, this.lastTriple);
							if (e == null) {
								this.close();
								return null;
							}
							if (e.key == null) {
								this.currentLeafIn.close();
								// one leaf node does not had any triples
								// for key
								// => use SIP information to jump to the
								// right B+-tree leaf node directly!
								return this.getFirstUsingCache(0, k);
							} else {
								this.lastTriple = e.value;
								this.lastKey = e.key;
								compare = DBBPTree.this.comparator.compare(this.lastKey, k);
							}
						}
						if (DBBPTree.this.comparator.compare(this.lastKey, this.arg0) == 0) {
							return e.value;
						}
						this.currentLeafIn.close();
						return null;
					} else {
						// should never happen!
						this.currentLeafIn.close();
						return null;
					}
				}
				this.lastTriple = e.value;
				this.lastKey = e.key;
				final int compare = DBBPTree.this.comparator.compare(this.lastKey, this.arg0);
				if (compare == 0) {
					return e.value;
				}
				this.close();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
			return null;
		}

		@Override
		public V next(final K k) {
			V result = this.next;
			if (result != null) {
				this.next = this.getNext(k);
			}
			while (result != null && k.compareTo(this.lastKey) > 0) {
				result = this.next;
				this.next = this.getNext(k);
				// next = getNext();
			}
			return result;
		}
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0) {
		if (enableSIP) {
			return new PrefixSearchIterator(arg0);
		} else {
			return new PrefixSearchIteratorWithoutSIP(arg0);
		}
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K min) {
		if (enableSIP) {
			return new PrefixSearchIterator(arg0, min);
		} else {
			return new PrefixSearchIteratorWithoutSIP(arg0, min);
		}
	}

	@Override
	public Iterator<V> prefixSearch(final K arg0, final K smallest,
			final K largest) {
		if (enableSIP) {
			return new PrefixSearchIteratorMaxMin(arg0, smallest, largest);
		} else {
			return new PrefixSearchIteratorMaxMinWithoutSIP(arg0, smallest,
					largest);
		}
	}

	@Override
	public Iterator<V> prefixSearchMax(final K arg0, final K largest) {
		if (enableSIP) {
			return new PrefixSearchIteratorMaxMin(arg0, null, largest);
		} else {
			return new PrefixSearchIteratorMaxMinWithoutSIP(arg0, null, largest);
		}
	}

	private V getMaximum(final int filename, final K arg0) {
		if (filename < 0) {
			return null;
		}
		InputStream fis;
		try {
			fis = new PageInputStream(filename, this.pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
					fis, null);
			final boolean leaf = InputHelper.readLuposBoolean(in.is);
			if (leaf) { // leaf node reached!
				V lastTriple = null;
				K lastKey = null;
				while (true) {
					final DBBPTreeEntry<K, V> e = this.nodeDeSerializer
					.getNextLeafEntry(in, lastKey, lastTriple);
					if (e == null || e.key == null) {
						in.close();
						return lastTriple;
					}
					final K key = e.key;
					final int compare = this.comparator.compare(key, arg0);
					if (compare > 0) {
						in.close();
						return lastTriple;
					}
					if (e.value != null) {
						lastTriple = e.value;
						lastKey = e.key;
					}
				}
			} else {
				K lastKey = null;
				int lastFilename = -1;
				while (true) {
					final Tuple<K, Integer> nextEntry = this.getNextInnerNodeEntry(
							lastKey, in);
					if (nextEntry == null || nextEntry.getSecond() == 0
							|| nextEntry.getSecond() < 0) {
						in.close();
						return null;
					}
					if (nextEntry.getFirst() == null) {
						in.close();
						return this.getMaximum(nextEntry.getSecond(), arg0);
					}
					final int compare = this.comparator.compare(
							nextEntry.getFirst(), arg0);
					if (compare > 0) {
						in.close();
						final V t = this.getMaximum(nextEntry.getSecond(), arg0);
						if (t != null) {
							return t;
						}
						if (lastFilename > 0) {
							return this.getMaximum(lastFilename, arg0);
						} else {
							return null;
						}
					}
					lastKey = nextEntry.getFirst();
					if (compare == 0) {
						lastFilename = nextEntry.getSecond();
					}
				}
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
		return null;
	}

	public V getMaximum(final K arg0) {
		return this.getMaximum(this.rootPage, arg0);
	}

	@Override
	public Object[] getClosestElements(final K arg0) {
		final List<Node<K, V>> navCol = this.navigateTo(arg0, this.rootPage,
				new LinkedList<Node<K, V>>());
		if (navCol == null) {
			return null;
		}
		final int pos = navCol.get(navCol.size() - 1).readKeys.size() - 1;
		final Object[] oa = new Object[2];
		oa[0] = this.getLeft(navCol, pos, arg0);
		oa[1] = this.getRight(navCol, pos, arg0);
		return oa;
	}

	private V getLeft(final List<Node<K, V>> navCol, final int pos, final K arg0) {
		if (navCol == null || navCol.size() == 0) {
			return null;
		}
		final Node<K, V> navigateToClass = navCol.get(navCol.size() - 1);
		if (navCol.get(navCol.size() - 1) instanceof LeafNode) {
			final LeafNode<K, V> leafNode = (LeafNode<K, V>) navigateToClass;

			// right end reached?
			if (this.comparator.compare(leafNode.readKeys.get(pos), arg0) < 0) {
				return leafNode.readValues.get(pos);
			}
			// otherwise it is the one which is one left from the found!
			if (pos > 0) {
				return leafNode.getValues().get(pos - 1);
			} else {
				// get the last entry of the previous leaf node!
				int ipos = navCol.size() - 2;
				if (ipos < 0) {
					return null;
				}
				InnerNode<K, V> innerNode = (InnerNode<K, V>) navCol.get(ipos);
				while (innerNode.readReferences.size() == 1) {
					ipos--;
					if (ipos < 0) {
						return null;
					}
					innerNode = (InnerNode<K, V>) navCol.get(ipos);
				}
				return this.rightMostValue(this.getNode(innerNode.readReferences
						.get(innerNode.readReferences.size() - 2)));
			}
		} else {
			System.err.println("No leaf node found!");
			return null;
		}
	}

	private V getRight(final List<Node<K, V>> navCol, final int pos,
			final K arg0) {
		if (navCol == null || navCol.size() == 0) {
			this.closeInputStreams(navCol);
			return null;
		}
		final Node<K, V> navigateToClass = navCol.get(navCol.size() - 1);
		if (navCol.get(navCol.size() - 1) instanceof LeafNode) {
			LeafNode<K, V> leafNode = (LeafNode<K, V>) navigateToClass;
			leafNode.readFullLeafNode();
			int pos2 = pos;

			if (leafNode.found) {

				do {
					pos2++;
					if (pos2 > leafNode.readKeys.size() - 1) {
						if (leafNode.getNextLeafNode() < 0) {
							this.closeInputStreams(navCol);
							return null;
						}
						leafNode = (LeafNode<K, V>) this.getNode(leafNode
								.getNextLeafNode());
						pos2 = 0;
					}
				} while (this.comparator.compare(leafNode.readKeys.get(pos2), arg0) <= 0);
			}
			this.closeInputStreams(navCol);
			if (this.comparator.compare(leafNode.readKeys.get(pos2), arg0) > 0) {
				return leafNode.readValues.get(pos2);
			} else {
				return null;
			}

		} else {
			System.err.println("No leaf node found!");
			return null;
		}
	}

	private V rightMostValue(final Node<K, V> node) {
		// determine rightmost entry!
		InnerNode<K, V> rightMostInnerNode;
		LeafNode<K, V> rightMostLeaf;
		if (node instanceof LeafNode) {
			rightMostLeaf = (LeafNode<K, V>) node;
			rightMostInnerNode = null;
		} else {
			rightMostLeaf = null;
			rightMostInnerNode = (InnerNode<K, V>) node;
		}
		while (rightMostLeaf == null) {
			final int rightMostFilename = rightMostInnerNode.getReferences()
			.get(rightMostInnerNode.getReferences().size() - 1);
			final Node<K, V> n = this.getNode(rightMostFilename);
			if (n instanceof LeafNode) {
				rightMostLeaf = (LeafNode<K, V>) n;
			} else {
				rightMostInnerNode = (InnerNode<K, V>) n;
			}
		}
		return rightMostLeaf.readValues
		.get(rightMostLeaf.readValues.size() - 1);
	}

	public static interface Generator<K2, V2> {
		public int size();

		public Iterator<Entry<K2, V2>> iterator();
	}

	public static void main(final String[] args) {
		// test the object...

		// try {
		// final DBBPTree<String, String> dbbptree = new DBBPTree<String,
		// String>(
		// new StandardComparator<String>(), 10, 10,
		// new StandardNodeDeSerializer<String, String>(String.class,
		// String.class));
		// dbbptree.generateDBBPTree(new Generator<String, String>() {
		//
		// private final static int max = 1000000;
		//
		// @Override
		// public Iterator<java.util.Map.Entry<String, String>> iterator() {
		// return new Iterator<java.util.Map.Entry<String, String>>() {
		// private int current = 0;
		//
		// @Override
		// public boolean hasNext() {
		// return current < max;
		// }
		//
		// @Override
		// public java.util.Map.Entry<String, String> next() {
		// final int zcurrent = current;
		// current++;
		// return new MapEntry<String, String>("from_"
		// + zcurrent, "to_" + zcurrent);
		// }
		//
		// @Override
		// public void remove() {
		// throw new UnsupportedOperationException();
		// }
		//
		// };
		// }
		//
		// @Override
		// public int size() {
		// return max;
		// }
		// });
		// final SIPParallelIterator<java.util.Map.Entry<String, String>,
		// String> iterator = (SIPParallelIterator<java.util.Map.Entry<String,
		// String>, String>) dbbptree
		// .entrySet().iterator();
		// while (iterator.hasNext())
		// System.out.println(iterator.next());

		try {
			final DBBPTree<String, Integer> dbbptree = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(
					10, 10, new StandardNodeDeSerializer<String, Integer>(
							String.class, Integer.class));
			dbbptree.generateDBBPTree(new Generator<String, Integer>() {
				private final static int max = 1000000;
				private final int maxNumbers = (int) Math.ceil(Math.log10(max));

				@Override
				public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
					return new Iterator<java.util.Map.Entry<String, Integer>>() {
						private int current = 0;

						@Override
						public boolean hasNext() {
							return this.current < max;
						}

						@Override
						public java.util.Map.Entry<String, Integer> next() {
							final int zcurrent = this.current;
							this.current++;
							String s = "";
							for (int i = 0; i < maxNumbers
							- (int) Math.ceil(Math.log10(zcurrent)); i++) {
								s += "0";
							}
							return new MapEntry<String, Integer>("from_" + s
									+ zcurrent, zcurrent);
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}

					};
				}

				@Override
				public int size() {
					return max;
				}
			});
			final SIPParallelIterator<java.util.Map.Entry<String, Integer>, String> iterator = (SIPParallelIterator<java.util.Map.Entry<String, Integer>, String>) dbbptree
			.entrySet().iterator();

			int i = 10000;

			while (iterator.hasNext()) {
				String s = "";
				for (int j = 0; j < 6 - (int) Math.ceil(Math.log10(i)); j++) {
					s += "0";
				}
				final String key = "from_" + s + i;
				System.out.print(key + ":");
				final java.util.Map.Entry<String, Integer> entry = iterator
				.next(key);
				if (entry == null) {
					break;
				}
				System.out.println(entry);
				i += 1000;
			}

		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public void writeAllModifiedPages() throws IOException {
		this.pageManager.writeAllModifiedPages();
	}
}
