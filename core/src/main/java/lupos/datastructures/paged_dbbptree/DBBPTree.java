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

		if (dir.compareTo("") != 0
				&& (!(dir.endsWith("//") || dir.endsWith("/") || dir
						.endsWith("\""))))
			dir = dir + "//";
		DBBPTree.mainFolder = dir + "dbbptree//";
		if (delete)
			FileHelper.deleteDirectory(new File(mainFolder));
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
		init(comparator, k, k_);
		this.nodeDeSerializer = nodeDeSerializer;
	}

	public DBBPTree(final int k, final int k_, final NodeDeSerializer<K, V> nodeDeSerializer) throws IOException {
		init(null, k, k_);
		this.nodeDeSerializer = nodeDeSerializer;
	}
	
	public void setName(String name){
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
		size = 0;
		lock.lock();
		try {
			currentID = currentFileID++;
			final File f = new File(DBBPTree.mainFolder);
			f.mkdirs();
			if(currentID==0){
				FileHelper.deleteFilesStartingWithPattern(DBBPTree.mainFolder, currentID + ".dbbptree_");
			}
			pageManager = new PageManager(DBBPTree.mainFolder + currentID + ".dbbptree");
		} finally {
			lock.unlock();
		}

		if (comparator == null) {
			this.comparator = new StandardComparator<K>();
		} else
			this.comparator = comparator;
	}

	public Comparator<? super K> comparator() {
		return this.comparator;
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		if (firstLeafPage < 0)
			return new HashSet<java.util.Map.Entry<K, V>>();
		else
			return new Set<java.util.Map.Entry<K, V>>() {
			public boolean add(final java.util.Map.Entry<K, V> arg0) {
				final V value = DBBPTree.this.put(arg0.getKey(), arg0
						.getValue());
				if (value == null)
					return true;
				else
					return !value.equals(arg0.getValue());
			}

			public boolean addAll(
					final Collection<? extends java.util.Map.Entry<K, V>> arg0) {
				boolean result = false;
				for (final java.util.Map.Entry<K, V> me : arg0) {
					result = result || add(me);
				}
				return result;
			}

			public void clear() {
				DBBPTree.this.clear();
			}

			public boolean contains(final Object arg0) {
				final V value = DBBPTree.this
				.get(((java.util.Map.Entry<K, V>) arg0).getKey());
				if (value != null) {
					return (value.equals(((java.util.Map.Entry<K, V>) arg0)
							.getValue()));
				} else
					return false;
			}

			public boolean containsAll(final Collection<?> arg0) {
				for (final Object o : arg0)
					if (!contains(o))
						return false;
				return true;
			}

			public boolean isEmpty() {
				return DBBPTree.this.isEmpty();
			}

			public SIPParallelIterator<java.util.Map.Entry<K, V>, K> iterator() {
				if (size() == 0)
					return new SIPParallelIterator<java.util.Map.Entry<K, V>, K>() {
					public boolean hasNext() {
						return false;
					}

					public java.util.Map.Entry<K, V> next() {
						return null;
					}

					public void remove() {
					}

					public java.util.Map.Entry<K, V> next(final K k) {
						return null;
					}

					public void close() {
					}
				};
				try {
					return new SIPParallelIterator<java.util.Map.Entry<K, V>, K>() {
						private LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader<V>(
								new PageInputStream(firstLeafPage,
										pageManager), null);
						{
							innerNodes = new LinkedList<Tuple<K, LuposObjectInputStream<V>>>();
							in.readLuposBoolean();
						}
						private List<Tuple<K, LuposObjectInputStream<V>>> innerNodes;
						private int entrynumber = 0;
						private K lastKey = null;
						private V lastValue = null;

						public boolean hasNext() {
							return (entrynumber < DBBPTree.this.size());
						}

						private java.util.Map.Entry<K, V> getFirst(
								final int filename, final K k) {
							if (filename < 0)
								return null;
							InputStream fis;
							try {
								fis = new PageInputStream(filename,
										pageManager);
								final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
										fis, null);
								final boolean leaf = in.readLuposBoolean();
								if (leaf) { // leaf node reached!
									lastKey = null;
									lastValue = null;
									while (true) {
										final DBBPTreeEntry<K, V> e = getNextLeafEntry(
												in, lastKey, lastValue);
										if (e == null || e.key == null) {
											in.close();
											close();
											return null;
										}
										final K key = e.key;
										lastKey = key;
										lastValue = e.value;
										final int compare = comparator
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
										final Tuple<K, Integer> nextEntry = getNextInnerNodeEntry(
												lastKey, in);
										if (nextEntry == null
												|| nextEntry.getSecond() <= 0) {
											in.close();
											close();
											return null;
										}
										lastKey = nextEntry.getFirst();
										if (nextEntry.getFirst() == null) {
											innerNodes
											.add(new Tuple<K, LuposObjectInputStream<V>>(
													null, in));
											return getFirst(nextEntry
													.getSecond(), k);
										}
										final int compare = comparator
										.compare(nextEntry
												.getFirst(), k);
										if (compare >= 0) {
											innerNodes
											.add(new Tuple<K, LuposObjectInputStream<V>>(
													nextEntry
													.getFirst(),
													in));
											return getFirst(nextEntry
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
							if (index < 0)
								return null;
							try {
								if (innerNodes.size() <= index) {
									close();
									return null;
									// close();
									// innerNodes.clear();
									// return getFirst(rootFilename,
									// triplekey);
								}
								final Tuple<K, LuposObjectInputStream<V>> current = innerNodes
								.get(index);
								final LuposObjectInputStream<V> in = current
								.getSecond();
								K lastKey = current.getFirst();
								if (lastKey == null
										|| comparator
										.compare(lastKey, kkey) >= 0)
									return getFirstUsingCache(index + 1,
											kkey);
								while (innerNodes.size() > index + 1) {
									final Tuple<K, LuposObjectInputStream<V>> toBeDeleted = innerNodes
									.remove(innerNodes.size() - 1);
									try {
										toBeDeleted.getSecond().close();
									} catch (final IOException e) {
										e.printStackTrace();
										System.err.println(e);
									}
								}
								while (true) {
									final Tuple<K, Integer> nextEntry = getNextInnerNodeEntry(
											lastKey, in);
									if (nextEntry == null
											|| nextEntry.getSecond() <= 0) {
										in.close();
										close();
										return null;
									}
									lastKey = nextEntry.getFirst();
									if (nextEntry.getFirst() == null) {
										current.setFirst(null);
										return getFirst(nextEntry
												.getSecond(), kkey);
									}
									final int compare = comparator.compare(
											nextEntry.getFirst(), kkey);
									if (compare >= 0) {
										current.setFirst(nextEntry
												.getFirst());
										return getFirst(nextEntry
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

						public java.util.Map.Entry<K, V> next() {
							if(!hasNext())
								return null;
							try {
								final DBBPTreeEntry<K, V> e = getNextLeafEntry(
										in, lastKey, lastValue);
								if (e != null) {
									if (e.key == null) {
										if (e.filenameOfNextLeafNode >= 0) {
											in.close();
											try{
												in = new LuposObjectInputStreamWithoutReadingHeader<V>(
														new PageInputStream(
																e.filenameOfNextLeafNode,
																pageManager),
																null);
												in.readLuposBoolean();
												lastKey = null;
												lastValue = null;
												return next();
											} catch(final Exception e1){
												System.err.println(e1);
												e1.printStackTrace();
												return null;
											}
										}
									} else {
										lastKey = e.key;
										lastValue = e.value;
										entrynumber++;
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

						public void remove() {
							throw (new UnsupportedOperationException(
							"This iterator is ReadOnly."));
						}

						@Override
						protected void finalize() throws Throwable {
							try {
								in.close();
							} finally {
								super.finalize();
							}
						}

						public void close() {
							for (final Tuple<K, LuposObjectInputStream<V>> tuple : innerNodes) {
								try {
									tuple.getSecond().close();
								} catch (final IOException e) {
								}
							}
							try {
								in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
						}

						private java.util.Map.Entry<K, V> getNext(final K k) {
							try {
								final DBBPTreeEntry<K, V> e = getNextLeafEntry(
										in, lastKey, lastValue);
								if (e != null) {
									if (e.key == null) {
										if (e.filenameOfNextLeafNode >= 0) {
											in.close();
											in = new LuposObjectInputStreamWithoutReadingHeader<V>(
													new PageInputStream(
															e.filenameOfNextLeafNode,
															pageManager),
															null);
											in.readLuposBoolean();
											lastKey = null;
											lastValue = null;
											while (true) {
												final DBBPTreeEntry<K, V> e1 = getNextLeafEntry(
														in, lastKey,
														lastValue);
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
															in.close();
															if (innerNodes
																	.size() == 0)
																return getFirst(
																		rootPage,
																		k);
															else
																return getFirstUsingCache(
																		0,
																		k);
														}
													} else {
														entrynumber++;
														lastKey = e1.key;
														lastValue = e1.value;
														if (comparator
																.compare(
																		k,
																		e1.key) <= 0) {
															return new MapEntry<K, V>(
																	e1.key,
																	e1.value);
														}
													}
												} else {
													in.close();
													close();
													return null;
												}
											}
										}
										in.close();
										close();
										return null;
									} else {
										lastKey = e.key;
										lastValue = e.value;
										entrynumber++;
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

						public java.util.Map.Entry<K, V> next(final K k) {
							java.util.Map.Entry<K, V> result;
							do {
								result = getNext(k);
							} while (result != null
									&& comparator.compare(k, result
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

			public boolean remove(final Object arg0) {
				final V value = DBBPTree.this
				.remove(((java.util.Map.Entry<K, V>) arg0).getKey());
				if (value == null)
					return false;
				else
					return value.equals(((java.util.Map.Entry<K, V>) arg0)
							.getValue());
			}

			public boolean removeAll(final Collection<?> arg0) {
				boolean result = false;
				for (final Object me : arg0) {
					result = result || remove(me);
				}
				return result;
			}

			public boolean retainAll(final Collection<?> arg0) {
				boolean result = false;
				for (final java.util.Map.Entry<K, V> o : this) {
					if (!arg0.contains(o)) {
						remove(o);
						result = true;
					}
				}
				return result;
			}

			public int size() {
				return DBBPTree.this.size();
			}

			public Object[] toArray() {
				final Object[] o = new Object[size()];
				final Iterator<java.util.Map.Entry<K, V>> kit = iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = kit.next();
				}
				return o;
			}

			public <T> T[] toArray(final T[] arg0) {
				final T[] o = (T[]) new Object[size()];
				final Iterator<java.util.Map.Entry<K, V>> kit = iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = (T) kit.next();
				}
				return o;
			}
		};
	}

	protected Tuple<K, Integer> getNextInnerNodeEntry(final K lastKey2,
			final LuposObjectInputStream<V> in2) {
		return nodeDeSerializer.getNextInnerNodeEntry(lastKey2, in2);
	}

	public K firstKey() {
		final Iterator<K> it = keySet().iterator();
		if (it.hasNext())
			return it.next();
		else
			return null;
	}

	public SortedMap<K, V> headMap(final K arg0) {
		throw (new UnsupportedOperationException("headMap is not supported."));
	}

	public Set<K> keySet() {
		if (firstLeafPage < 0)
			return new HashSet<K>();
		else
			return new Set<K>() {
			public boolean add(final K arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			public boolean addAll(final Collection<? extends K> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			public void clear() {
				DBBPTree.this.clear();
			}

			public boolean contains(final Object arg0) {
				return DBBPTree.this.containsKey(arg0);
			}

			public boolean containsAll(final Collection<?> arg0) {
				for (final Object o : arg0)
					if (!contains(o))
						return false;
				return true;
			}

			public boolean isEmpty() {
				return DBBPTree.this.isEmpty();
			}

			public Iterator<K> iterator() {
				return new Iterator<K>() {
					Iterator<java.util.Map.Entry<K, V>> it = DBBPTree.this
					.entrySet().iterator();

					public boolean hasNext() {
						return it.hasNext();
					}

					public K next() {
						final java.util.Map.Entry<K, V> me = it.next();
						if (me != null)
							return me.getKey();
						else
							return null;
					}

					public void remove() {
						it.remove();
					}
				};
			}

			public boolean remove(final Object arg0) {
				return (DBBPTree.this
						.remove(((java.util.Map.Entry<K, V>) arg0).getKey()) == null);
			}

			public boolean removeAll(final Collection<?> arg0) {
				boolean result = false;
				for (final Object me : arg0) {
					result = result || remove(me);
				}
				return result;
			}

			public boolean retainAll(final Collection<?> arg0) {
				boolean result = false;
				for (final K o : this) {
					if (!arg0.contains(o)) {
						remove(o);
						result = true;
					}
				}
				return result;
			}

			public int size() {
				return DBBPTree.this.size();
			}

			public Object[] toArray() {
				final Object[] o = new Object[size()];
				final Iterator<K> kit = iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = kit.next();
				}
				return o;
			}

			public <T> T[] toArray(final T[] arg0) {
				final T[] o = (T[]) new Object[size()];
				final Iterator<K> kit = iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = (T) kit.next();
				}
				return o;
			}
		};
	}

	public K lastKey() {
		throw (new UnsupportedOperationException("lastKey is not supported."));
	}

	public SortedMap<K, V> subMap(final K arg0, final K arg1) {
		throw (new UnsupportedOperationException("subMap is not supported."));
	}

	public SortedMap<K, V> tailMap(final K arg0) {
		throw (new UnsupportedOperationException("tailMap is not supported."));
	}

	public Collection<V> values() {
		if (firstLeafPage < 0)
			return new HashSet<V>();
		else
			return new Collection<V>() {
			public boolean add(final V arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			public boolean addAll(final Collection<? extends V> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			public void clear() {
				DBBPTree.this.clear();
			}

			public boolean contains(final Object arg0) {
				return DBBPTree.this.containsKey(arg0);
			}

			public boolean containsAll(final Collection<?> arg0) {
				for (final Object o : arg0)
					if (!contains(o))
						return false;
				return true;
			}

			public boolean isEmpty() {
				return DBBPTree.this.isEmpty();
			}

			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<java.util.Map.Entry<K, V>> it = DBBPTree.this
					.entrySet().iterator();

					public boolean hasNext() {
						return it.hasNext();
					}

					public V next() {
						final java.util.Map.Entry<K, V> me = it.next();
						if (me != null)
							return me.getValue();
						else
							return null;
					}

					public void remove() {
						it.remove();
					}
				};
			}

			public boolean remove(final Object arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			public boolean removeAll(final Collection<?> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			public boolean retainAll(final Collection<?> arg0) {
				throw (new UnsupportedOperationException(
				"This set is ReadOnly."));
			}

			public int size() {
				return DBBPTree.this.size();
			}

			public Object[] toArray() {
				final Object[] o = new Object[size()];
				final Iterator<V> kit = iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = kit.next();
				}
				return o;
			}

			public <T> T[] toArray(final T[] arg0) {
				final T[] o = (T[]) new Object[size()];
				final Iterator<V> kit = iterator();
				int i = 0;
				while (kit.hasNext()) {
					o[i++] = (T) kit.next();
				}
				return o;
			}
		};
	}

	public void clear() {
		FileHelper.deleteFile(DBBPTree.mainFolder+currentID+ ".dbbptree_*");
		final File f = new File(DBBPTree.mainFolder);
		f.mkdirs();
		try {
			pageManager.close();
			pageManager = new PageManager(DBBPTree.mainFolder + currentID
					+ ".dbbptree");
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		size = 0;
		rootPage = -1;
		firstLeafPage = -1;
	}

	public boolean containsKey(final Object arg0) {
		return (get(arg0) != null);
	}

	public boolean containsValue(final Object arg0) {
		for (final V v : values()) {
			if (v.equals(arg0))
				return true;
		}
		return false;
	}

	public V get(final Object arg0) {
		return get(arg0, rootPage);
	}

	protected DBBPTreeEntry<K, V> getNextLeafEntry(
			final LuposObjectInputStream<V> in, final K lastKey,
			final V lastValue) {
		return nodeDeSerializer.getNextLeafEntry(in, lastKey, lastValue);
	}

	private V get(final Object arg0, final int filename) {
		
		if (filename < 0 || size == 0)
			return null;
		InputStream fis;
		try {
			fis = new PageInputStream(filename, pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader<V>(
					fis, null);
			final boolean leaf = in.readLuposBoolean();
			if (leaf) { // leaf node reached!
				K lastKey = null;
				V lastValue = null;
				while (true) {
					final DBBPTreeEntry<K, V> e = getNextLeafEntry(in, lastKey,
							lastValue);
					if (e == null || e.key == null) {
						in.close();
						return null;
					}
					final int compare = comparator.compare(e.key, (K) arg0);
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
						
						Tuple<K, Integer> nextEntry=nodeDeSerializer.getNextInnerNodeEntry(lastKey, in);
						
						if(nextEntry==null){
							in.close();
							return null;
						}
						
						int nextFilename=nextEntry.getSecond();
						
						if(nextEntry.getSecond()<0){
							in.close();
							return null;
						}
												
						K nextKey = nextEntry.getFirst();
						if (nextKey == null) {
							in.close();
							return get(arg0, nextFilename);
						}
						final int compare = comparator.compare(nextKey,
								(K) arg0);
						if (compare >= 0) {
							in.close();
							return get(arg0, nextFilename);
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

	public boolean isEmpty() {
		return (size == 0);
	}

	protected int newFilename() {
		return pageManager.getNumberOfNewPage();
	}

	public V put(final K arg0, final V arg1) {
		keyClass = (Class<? super K>) arg0.getClass();
		valueClass = (Class<? super V>) arg1.getClass();
		if (rootPage < 0 || size == 0) {
			// just create one new leaf node as root node of the B+-tree...
			rootPage = newFilename();
			firstLeafPage = rootPage;
			try {
				final OutputStream fosRoot = new PageOutputStream(rootPage, pageManager, true);
				final LuposObjectOutputStreamWithoutWritingHeader outRoot = new LuposObjectOutputStreamWithoutWritingHeader(fosRoot);
				outRoot.writeLuposBoolean(true);
				writeLeafEntry(arg0, arg1, outRoot, null, null);
				size = 1;
				outRoot.close();
			} catch (final FileNotFoundException e) {
				System.err.println(e);
				e.printStackTrace();
				rootPage = -1;
				firstLeafPage = -1;
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
				rootPage = -1;
				firstLeafPage = -1;
			}
		} else {
			final List<Node<K, V>> navCol = navigateTo(arg0, rootPage,
					new LinkedList<Node<K, V>>());
			if (navCol == null)
				System.err.println("Error while navigating to insertion position.");
			else {
				final Node<K, V> navigateToClass = navCol
				.get(navCol.size() - 1);
				if (navigateToClass == null)
					System.err.println("Error while navigating to insertion position.");
				else {
					final LeafNode<K, V> leafNode = (LeafNode<K, V>) navigateToClass;
					int pos = leafNode.readValues.size();
					leafNode.readFullLeafNode();
					V oldValue;
					if (leafNode.found) {
						// replace value!
						oldValue = leafNode.readValues.get(pos - 1);
						leafNode.readValues.set(pos - 1, arg1);
						closeInputStreams(navCol);
						leafNode.writeLeafNode(true);
						return oldValue;
					} else {
						if (pos > 0) {
							if (arg0.compareTo(leafNode.getKeys().get(pos - 1)) < 0)
								pos--;
						}
						final int pos2 = leafNode.readValues.size();
						// add node!
						leafNode.readKeys.add(pos, arg0);
						leafNode.readValues.add(pos, arg1);
						size++;

						if (pos2 + 1 > 2 * k_) {
							// split leaf node!
							final LeafNode<K, V> newLeafNode = new LeafNode<K, V>(
									keyClass, valueClass, k, pageManager,
									nodeDeSerializer);
							newLeafNode.filename = newFilename();
							newLeafNode.nextLeafNode = leafNode.nextLeafNode;
							leafNode.nextLeafNode = newLeafNode.filename;
							for (int i = k_ + 1; i < leafNode.readKeys.size(); i++) {
								newLeafNode.readKeys.add(leafNode.readKeys
										.get(i));
								newLeafNode.readValues.add(leafNode.readValues
										.get(i));
							}
							for (int i = leafNode.readKeys.size() - 1; i > k_; i--) {
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
											keyClass, valueClass, k,
											pageManager, nodeDeSerializer);
									innerNode.filename = newFilename();
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
										.size())
									innerNode.readKeys.set(posInnerNode - 1,
											arg0);
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
								if (innerNode.readKeys.size() > 2 * k) {
									// split node!
									final InnerNode<K, V> newInnerNode = new InnerNode<K, V>(
											keyClass, valueClass, k,
											pageManager, nodeDeSerializer);
									newInnerNode.filename = newFilename();
									for (int i = k + 1; i < innerNode.readKeys
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
									for (int i = innerNode.readKeys.size() - 1; i > k; i--) {
										innerNode.readKeys.remove(i);
										innerNode.readReferences.remove(i);
									}
									innerNode.readKeys.remove(k);

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
									key = rightMost(innerNode);
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
											if (posInnerNode < posInnerNode2)
												break;
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
									if (posInnerNode < posInnerNode2)
										break;
								}
							}
						}
						closeInputStreams(navCol);
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
			final Node<K, V> n = getNode(rightMostFilename);
			if (n instanceof LeafNode)
				rightMostLeaf = (LeafNode<K, V>) n;
			else
				rightMostInnerNode = (InnerNode<K, V>) n;
		}
		return rightMostLeaf.readKeys.get(rightMostLeaf.readKeys.size() - 1);
	}

	public void displayDBBPTree(final String title) {
		// TODO
		// new Viewer(new GraphWrapperDBBPTree(this.getNode(this.rootPage),
		// this,
		// null), title, false, false);
	}

	public Node<K, V> getNode(final int filename) {
		InputStream fis;
		try {
			fis = new PageInputStream(filename, pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader<V>(
					fis, null);
			final boolean leaf = in.readLuposBoolean();
			if (leaf) {
				final LeafNode<K, V> leafNode = new LeafNode<K, V>(keyClass,
						valueClass, k_, pageManager, nodeDeSerializer);
				leafNode.filename = filename;
				leafNode.in = in;
				leafNode.readFullLeafNode();
				in.close();
				return leafNode;
			} else {
				final InnerNode<K, V> innerNode = new InnerNode<K, V>(keyClass,
						valueClass, k, pageManager, nodeDeSerializer);
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
		nodeDeSerializer.writeLeafEntry(k, v, out, lastKey, lastValue);
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
		if (filename < 0)
			return null;
		InputStream fis;
		try {
			fis = new PageInputStream(filename, pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
					fis, null);
			final boolean leaf = in.readLuposBoolean();
			if (leaf) { // leaf node reached!
				final LeafNode<K, V> navigateToClassLeafNode = new LeafNode<K, V>(
						keyClass, valueClass, k_, pageManager, nodeDeSerializer);
				navigateToClassLeafNode.filename = filename;
				navigateToClassLeafNode.in = in;
				currentCollection.add(navigateToClassLeafNode);
				K lastKey = null;
				V lastValue = null;
				while (true) {
					final DBBPTreeEntry<K, V> e = getNextLeafEntry(in, lastKey,
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
					if (e.value != null)
						navigateToClassLeafNode.readValues.add(e.value);
					final int compare = comparator.compare(e.key, (K) arg0);
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
							keyClass, valueClass, k, pageManager,
							nodeDeSerializer);
					navigateToClassInnerNode.filename = filename;
					navigateToClassInnerNode.in = in;
					currentCollection.add(navigateToClassInnerNode);
					K lastKey=null;
					while (true) {
						
						Tuple<K, Integer> nextEntry=nodeDeSerializer.getNextInnerNodeEntry(lastKey, in);
						
						if(nextEntry==null){
							closeInputStreams(currentCollection);
							return null;
						}
						
						int nextFilename=nextEntry.getSecond();
						
						if(nextEntry.getSecond()<0){
							closeInputStreams(currentCollection);
							return null;
						}
						
						navigateToClassInnerNode.readReferences
						.add(nextFilename);
						
						K nextKey = nextEntry.getFirst();
						if (nextKey == null) {
							return navigateTo(arg0, nextFilename,
									currentCollection);
						}
						navigateToClassInnerNode.readKeys.add(nextKey);
						final int compare = comparator.compare(nextKey,
								(K) arg0);
						if (compare >= 0) {
							return navigateTo(arg0, nextFilename,
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

	public void putAll(final Map<? extends K, ? extends V> arg0) {
		for (final K k : arg0.keySet()) {
			put(k, arg0.get(k));
		}
	}

	public V remove(final Object arg0) {
		if (rootPage < 0)
			return null;
		final List<Node<K, V>> navCol = navigateTo(arg0, rootPage,
				new LinkedList<Node<K, V>>());
		if (navCol == null)
			System.err.println("Error while navigating to insertion position.");
		else {
			final Node<K, V> navigateToClass = navCol.get(navCol.size() - 1);
			if (navigateToClass == null)
				System.err.println("Error while navigating to insertion position.");
			else {
				final LeafNode<K, V> leafNode = (LeafNode<K, V>) navigateToClass;
				if (!leafNode.found) {
					return null;
				} else {
					final int pos = leafNode.readValues.size();
					leafNode.readFullLeafNode();
					// final int pos2 = leafNode.readValues.size();
					leafNode.readKeys.remove(pos - 1);
					final V oldValue = leafNode.readValues.remove(pos - 1);
					if (leafNode.readKeys.size() >= k_ || navCol.size()==1) {
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
									final Node<K, V> leftNeighbor = getNode(innerNode
											.getReferences().get(
													posOfCurrentNode - 1));
									final int comp = leftNeighbor instanceof LeafNode ? k_
											: k;
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
													rightMost(getNode(ref)));
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
									final Node<K, V> rightNeighbor = getNode(innerNode
											.getReferences().get(
													posOfCurrentNode + 1));
									final int comp = rightNeighbor instanceof LeafNode ? k_
											: k;
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
													rightMost(currentNode));
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
													rightMost(getNode(((InnerNode<K, V>) currentNode)
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
												final LeafNode<K, V> leftNeighbor = (LeafNode<K, V>) getNode(innerNode.readReferences
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
										if (firstLeafPage == currentNode.filename)
											firstLeafPage = rightNeighbor.filename;
										try {
											pageManager
											.releaseSequenceOfPages(currentNode.filename);
										} catch (final IOException e) {
											System.err.println(e);
											e.printStackTrace();
										}
										if (innerNode.getKeys().size() >= k)
											return oldValue;
										if (innerNode.filename == rootPage) {
											if (innerNode.getKeys().size() == 0
													&& rightNeighbor instanceof InnerNode) {
												try {
													pageManager
													.releaseSequenceOfPages(rootPage);
												} catch (final IOException e) {
													System.err.println(e);
													e.printStackTrace();
												}
												rootPage = rightNeighbor.filename;
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
									final Node<K, V> leftNeighbor = getNode(innerNode
											.getReferences().get(
													posOfCurrentNode - 1));
									if (leftNeighbor instanceof InnerNode) {
										leftNeighbor
										.getKeys()
										.add(
												rightMost(getNode(((InnerNode<K, V>) leftNeighbor)
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
									if (firstLeafPage == currentNode.filename)
										firstLeafPage = leftNeighbor.filename;
									try {
										pageManager
										.releaseSequenceOfPages(currentNode.filename);
									} catch (final IOException e) {
										System.err.println(e);
										e.printStackTrace();
									}
									if (innerNode.getKeys().size() >= k)
										return oldValue;
									if (innerNode.filename == rootPage) {
										if (innerNode.getKeys().size() == 0
												&& leftNeighbor instanceof InnerNode) {
											try {
												pageManager
												.releaseSequenceOfPages(rootPage);
											} catch (final IOException e) {
												System.err.println(e);
												e.printStackTrace();
											}
											rootPage = leftNeighbor.filename;
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
								} else
									((InnerNode<K, V>) currentNode)
									.writeInnerNode(true);
								return oldValue;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public int size() {
		return size;
	}

	public void writeInnerNodeEntry(final int fileName, final K key,
			final LuposObjectOutputStream out, final K lastKey)
	throws IOException {
		nodeDeSerializer.writeInnerNodeEntry(fileName, key, out, lastKey);
	}

	public void writeInnerNodeEntry(final int fileName,
			final LuposObjectOutputStream out) throws IOException {
		nodeDeSerializer.writeInnerNodeEntry(fileName, out);
	}

	public void generateDBBPTree(final Generator<K, V> generator) throws IOException {
		generateDBBPTree(new SortedMap<K, V>() {
			public Comparator<? super K> comparator() {
				return null;
			}

			public Set<java.util.Map.Entry<K, V>> entrySet() {
				return new Set<java.util.Map.Entry<K, V>>() {
					public boolean add(final java.util.Map.Entry<K, V> e) {
						throw new UnsupportedOperationException();
					}

					public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
						throw new UnsupportedOperationException();
					}

					public void clear() {
						throw new UnsupportedOperationException();
					}

					public boolean contains(final Object o) {
						throw new UnsupportedOperationException();
					}

					public boolean containsAll(final Collection<?> c) {
						throw new UnsupportedOperationException();
					}

					public boolean isEmpty() {
						throw new UnsupportedOperationException();
					}

					public Iterator<java.util.Map.Entry<K, V>> iterator() {
						return generator.iterator();
					}

					public boolean remove(final Object o) {
						throw new UnsupportedOperationException();
					}

					public boolean removeAll(final Collection<?> c) {
						throw new UnsupportedOperationException();
					}

					public boolean retainAll(final Collection<?> c) {
						throw new UnsupportedOperationException();
					}

					public int size() {
						throw new UnsupportedOperationException();
					}

					public Object[] toArray() {
						throw new UnsupportedOperationException();
					}

					public <T> T[] toArray(final T[] a) {
						throw new UnsupportedOperationException();
					}
				};
			}

			public K firstKey() {
				throw new UnsupportedOperationException();
			}

			public SortedMap<K, V> headMap(final K toKey) {
				throw new UnsupportedOperationException();
			}

			public Set<K> keySet() {
				throw new UnsupportedOperationException();
			}

			public K lastKey() {
				throw new UnsupportedOperationException();
			}

			public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
				throw new UnsupportedOperationException();
			}

			public SortedMap<K, V> tailMap(final K fromKey) {
				throw new UnsupportedOperationException();
			}

			public Collection<V> values() {
				throw new UnsupportedOperationException();
			}

			public void clear() {
				throw new UnsupportedOperationException();
			}

			public boolean containsKey(final Object key) {
				throw new UnsupportedOperationException();
			}

			public boolean containsValue(final Object value) {
				throw new UnsupportedOperationException();
			}

			public V get(final Object key) {
				throw new UnsupportedOperationException();
			}

			public boolean isEmpty() {
				throw new UnsupportedOperationException();
			}

			public V put(final K key, final V value) {
				throw new UnsupportedOperationException();
			}

			public void putAll(final Map<? extends K, ? extends V> m) {
				throw new UnsupportedOperationException();
			}

			public V remove(final Object key) {
				throw new UnsupportedOperationException();
			}

			public int size() {
				return generator.size();
			}
		});
	}

	public void generateDBBPTree(final SortedMap<K, V> sortedMap) throws IOException {
		final LinkedList<Container> innerNodes = new LinkedList<Container>();
		this.size = sortedMap.size();
		final Container leaf = new Container(this.size, k_, true);
		firstLeafPage = leaf.getFileName();
		if (sortedMap.comparator() != null)
			this.comparator = sortedMap.comparator();
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
		rootPage = previous.filename;
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
			filename = newFilename();
			init();
			numberOfNodes = Math.round(Math
					.ceil((double) numberOfEntries / kk_));
			this.factor = (double) numberOfEntries / numberOfNodes;
			this.limitNextNode = this.factor;
		}

		protected void init() {
			try {
				if (out != null)
					out.close();
				final OutputStream fos = new PageOutputStream(filename,
						pageManager, true);
				out = new LuposObjectOutputStreamWithoutWritingHeader(fos);
				out.writeLuposBoolean(leaf);
				lastKey = null;
				lastValue = null;
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(e);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public int getFileName() {
			return filename;
		}

		public boolean newNodeForNextEntry() {
			if (currentEntry + 1 > limitNextNode) {
				return true;
			} else
				return false;
		}

		public void storeInLeafNode(final Entry<K, V> entry) {
			lastStoredEntry = entry;
			currentEntry++;
			keyClass = (Class<? super K>) entry.getKey().getClass();
			valueClass = (Class<? super V>) entry.getValue().getClass();
			try {
				writeLeafEntry(entry.getKey(), entry.getValue(), out, lastKey,
						lastValue);
				lastKey = entry.getKey();
				lastValue = entry.getValue();
				// System.out.println("leaf "+ filename
				// +" ("+entry.getKey()+", "+entry.getValue()+")");
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void storeInInnerNode(final int fileName, final Entry<K, V> entry) {
			currentEntry++;
			try {
				writeInnerNodeEntry(fileName, entry.getKey(), out, lastKey);
				lastKey = entry.getKey();
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		public void storeInInnerNode(final int fileName) {
			try {
				writeInnerNodeEntry(fileName, out);
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void closeNode(final LinkedList<Container> innerNodes) {
			addToInnerNodes(innerNodes, 0, this, this.filename,
					this.lastStoredEntry);
			filename = newFilename();
			writeLeafEntryNextFileName(filename, out);

			init();
			limitNextNode = currentEntry + factor;
		}

		public void close() {
			try {
				out.close();
			} catch (final IOException e) {
				e.printStackTrace();
				System.err.println(e);
			}
		}

		public void addToInnerNodes(final LinkedList<Container> innerNodes,
				final int position, Container previous, final int filename,
				final Entry<K, V> lastStoredEntry) {
			while (innerNodes.size() < position + 1) {
				final Container container = new Container(numberOfNodes - 1, k,
						false);
				previous = container;
				innerNodes.add(container);
			}
			final Container container = innerNodes.get(position);
			if (container.newNodeForNextEntry()) {
				container.storeInInnerNode(filename);
				addToInnerNodes(innerNodes, position + 1, container,
						container.filename, lastStoredEntry);
				container.filename = newFilename();
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
		pageManager.writeAllModifiedPages();
		loos.writeLuposInt(currentID);
		loos.writeLuposInt(k);
		loos.writeLuposInt(k_);
		loos.writeLuposInt(size);
		loos.writeObject(comparator);
		loos.writeLuposInt(rootPage);
		loos.writeLuposInt(firstLeafPage);
		loos.writeObject(keyClass);
		loos.writeObject(valueClass);
		loos.writeObject(nodeDeSerializer);
	}

	protected void writeLeafEntryNextFileName(final int filename,
			final LuposObjectOutputStream out) {
		try {
			nodeDeSerializer.writeLeafEntryNextFileName(filename, out);
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
		pageManager = new PageManager(DBBPTree.mainFolder + currentID + ".dbbptree", false);
	}

	public static DBBPTree readLuposObject(final LuposObjectInputStream lois)
	throws IOException, ClassNotFoundException {
		final int currentID = lois.readLuposInt();
		final int k = lois.readLuposInt();
		final int k_ = lois.readLuposInt();
		final int size = lois.readLuposInt();
		final Comparator comp = (Comparator) lois.readObject();
		final int rootFilename = lois.readLuposInt();
		final int firstLeafFileName = lois.readLuposInt();
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
			if (next != null) {
				if (largest.compareTo(lastKey) < 0)
					next = null;
			}
		}

		@Override
		public V next() {
			final V result = next;
			if (result != null) {
				next = getNext();
				if (next != null) {
					if (largest.compareTo(lastKey) < 0)
						next = null;
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
			if (next != null) {
				if (largest.compareTo(lastKey) < 0)
					next = null;
			}
		}

		@Override
		public V next() {
			final V result = next;
			if (result != null) {
				next = getNext();
				if (next != null) {
					if (largest.compareTo(lastKey) < 0)
						next = null;
				}
			}
			return result;
		}

		@Override
		public V next(final K k) {
			V result = next;
			if (result != null) {
				next = getNext(k);
			}
			while (result != null && k.compareTo(lastKey) > 0) {
				result = next;
				next = getNext(k);
				if (next != null) {
					if (largest.compareTo(lastKey) < 0)
						next = null;
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
			lastTriple = null;
			lastKey = null;
			innerNodes = new LinkedList<Tuple<K, LuposObjectInputStream<V>>>();
			next = getFirst(rootPage);
		}

		public PrefixSearchIteratorWithoutSIP(final K arg0, final K smallest) {
			this.arg0 = arg0;
			lastTriple = null;
			lastKey = null;
			innerNodes = new LinkedList<Tuple<K, LuposObjectInputStream<V>>>();
			if (smallest != null)
				next = getFirst(rootPage, smallest);
			else
				next = getFirst(rootPage);
		}

		List<Tuple<K, LuposObjectInputStream<V>>> innerNodes;
		LuposObjectInputStream<V> currentLeafIn;
		V lastTriple;
		K lastKey;

		private V getFirst(final int filename) {
			if (filename < 0)
				return null;
			InputStream fis;
			try {
				fis = new PageInputStream(filename, pageManager);
				final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
						fis, null);
				final boolean leaf = in.readLuposBoolean();
				if (leaf) { // leaf node reached!
					while (true) {
						final DBBPTreeEntry<K, V> e = nodeDeSerializer
						.getNextLeafEntry(in, lastKey, lastTriple);
						if (e == null || e.key == null) {
							currentLeafIn = in;
							close();
							return null;
						}
						lastTriple = e.value;
						lastKey = e.key;
						final int compare = comparator.compare(lastKey, arg0);
						if (compare == 0) {
							currentLeafIn = in;
							return e.value;
						} else if (compare > 0) {
							currentLeafIn = in;
							close();
							return null;
						}
					}
				} else {
					K lastKey = null;
					while (true) {
						final Tuple<K, Integer> nextEntry = getNextInnerNodeEntry(
								lastKey, in);
						if (nextEntry == null || nextEntry.getSecond() == 0
								|| nextEntry.getSecond() < 0) {
							in.close();
							close();
							return null;
						}
						if (nextEntry.getFirst() == null) {
							innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									null, in));
							return getFirst(nextEntry.getSecond());
						}
						final int compare = comparator.compare(nextEntry
								.getFirst(), arg0);
						if (compare >= 0) {
							innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									nextEntry.getFirst(), in));
							return getFirst(nextEntry.getSecond());
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
			if (filename < 0)
				return null;
			InputStream fis;
			try {
				fis = new PageInputStream(filename, pageManager);
				final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
						fis, null);
				final boolean leaf = in.readLuposBoolean();
				lastTriple = null;
				lastKey = null;
				if (leaf) { // leaf node reached!
					while (true) {
						final DBBPTreeEntry<K, V> e = nodeDeSerializer
						.getNextLeafEntry(in, lastKey, lastTriple);
						if (e == null || e.key == null) {
							currentLeafIn = in;
							close();
							return null;
						}
						lastTriple = e.value;
						lastKey = e.key;
						final int compare = comparator.compare(lastKey,
								triplekey);
						if (compare == 0) {
							currentLeafIn = in;
							return e.value;
						} else if (compare > 0) {
							if (comparator.compare(lastKey, arg0) > 0) {
								currentLeafIn = in;
								close();
								return null;
							} else {
								currentLeafIn = in;
								return e.value;
							}
						}
					}
				} else {
					K lastKey = null;
					while (true) {
						final Tuple<K, Integer> nextEntry = getNextInnerNodeEntry(
								lastKey, in);
						if (nextEntry == null || nextEntry.getSecond() <= 0) {
							in.close();
							close();
							return null;
						}
						lastKey = nextEntry.getFirst();
						if (nextEntry.getFirst() == null) {
							innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									null, in));
							return getFirst(nextEntry.getSecond(), triplekey);
						}
						final int compare = comparator.compare(nextEntry
								.getFirst(), triplekey);
						if (compare >= 0) {
							innerNodes
							.add(new Tuple<K, LuposObjectInputStream<V>>(
									nextEntry.getFirst(), in));
							return getFirst(nextEntry.getSecond(), triplekey);
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
			if (index < 0)
				return null;
			try {
				if (innerNodes.size() <= index) {
					close();
					return null;
					// close();
					// innerNodes.clear();
					// return getFirst(rootFilename, triplekey);
				}
				final Tuple<K, LuposObjectInputStream<V>> current = innerNodes
				.get(index);
				final LuposObjectInputStream<V> in = current.getSecond();
				K lastKey = current.getFirst();
				if (lastKey == null
						|| comparator.compare(lastKey, triplekey) >= 0)
					return getFirstUsingCache(index + 1, triplekey);
				while (innerNodes.size() > index + 1) {
					final Tuple<K, LuposObjectInputStream<V>> toBeDeleted = innerNodes
					.remove(innerNodes.size() - 1);
					try {
						toBeDeleted.getSecond().close();
					} catch (final IOException e) {
						e.printStackTrace();
						System.err.println(e);
					}
				}
				while (true) {
					final Tuple<K, Integer> nextEntry = getNextInnerNodeEntry(
							lastKey, in);
					if (nextEntry == null || nextEntry.getSecond() <= 0) {
						in.close();
						close();
						return null;
					}
					lastKey = nextEntry.getFirst();
					if (nextEntry.getFirst() == null) {
						current.setFirst(null);
						return getFirst(nextEntry.getSecond(), triplekey);
					}
					final int compare = comparator.compare(
							nextEntry.getFirst(), triplekey);
					if (compare >= 0) {
						current.setFirst(nextEntry.getFirst());
						return getFirst(nextEntry.getSecond(), triplekey);
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
				DBBPTreeEntry<K, V> e = nodeDeSerializer.getNextLeafEntry(
						currentLeafIn, lastKey, lastTriple);
				if (e == null) {
					currentLeafIn.close();
					return null;
				}
				if (e.key == null) {
					// next leaf node!
					if (e.filenameOfNextLeafNode >= 0) {
						currentLeafIn.close();
						lastTriple = null;
						lastKey = null;
						final InputStream fis = new PageInputStream(
								e.filenameOfNextLeafNode, pageManager);
						currentLeafIn = new LuposObjectInputStreamWithoutReadingHeader<V>(fis, null);
						// read over the leaf flag!
						currentLeafIn.readLuposBoolean();
						e = nodeDeSerializer.getNextLeafEntry(currentLeafIn,
								lastKey, lastTriple);
						if (e == null || e.key == null) {
							// should never happen!
							currentLeafIn.close();
							return null;
						}
					} else {
						// should never happen!
						currentLeafIn.close();
						return null;
					}
				}
				lastTriple = e.value;
				lastKey = e.key;
				final int compare = comparator.compare(lastKey, arg0);
				if (compare == 0) {
					return e.value;
				}
				close();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
			return null;
		}

		public boolean hasNext() {
			return (next != null);
		}

		public V next() {
			final V result = next;
			if (result != null) {
				next = getNext();
			}
			return result;
		}

		public void remove() {
			throw (new UnsupportedOperationException(
			"This iterator is ReadOnly."));
		}

		public void close() {
			for (final Tuple<K, LuposObjectInputStream<V>> tuple : innerNodes) {
				try {
					tuple.getSecond().close();
				} catch (final IOException e) {
				}
			}
			innerNodes.clear();
			try {
				currentLeafIn.close();
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
				DBBPTreeEntry<K, V> e = nodeDeSerializer.getNextLeafEntry(
						currentLeafIn, lastKey, lastTriple);
				if (e == null) {
					currentLeafIn.close();
					return null;
				}
				if (e.key == null) {
					// next leaf node!
					if (e.filenameOfNextLeafNode >= 0) {
						currentLeafIn.close();
						lastTriple = null;
						lastKey = null;
						final InputStream fis = new PageInputStream(
								e.filenameOfNextLeafNode, pageManager);
						currentLeafIn = new LuposObjectInputStreamWithoutReadingHeader<V>(fis, null);
						// read over the leaf flag!
						currentLeafIn.readLuposBoolean();
						e = nodeDeSerializer.getNextLeafEntry(currentLeafIn,
								lastKey, lastTriple);
						if (e == null || e.key == null) {
							// should never happen!
							close();
							return null;
						}
						lastTriple = e.value;
						lastKey = e.key;
						int compare = comparator.compare(lastKey, k);
						while (compare < 0) {
							e = nodeDeSerializer.getNextLeafEntry(
									currentLeafIn, lastKey, lastTriple);
							if (e == null) {
								close();
								return null;
							}
							if (e.key == null) {
								currentLeafIn.close();
								// one leaf node does not had any triples
								// for key
								// => use SIP information to jump to the
								// right B+-tree leaf node directly!
								return getFirstUsingCache(0, k);
							} else {
								lastTriple = e.value;
								lastKey = e.key;
								compare = comparator.compare(lastKey, k);
							}
						}
						if (comparator.compare(lastKey, arg0) == 0) {
							return e.value;
						}
						currentLeafIn.close();
						return null;
					} else {
						// should never happen!
						currentLeafIn.close();
						return null;
					}
				}
				lastTriple = e.value;
				lastKey = e.key;
				final int compare = comparator.compare(lastKey, arg0);
				if (compare == 0) {
					return e.value;
				}
				close();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
			return null;
		}

		public V next(final K k) {
			V result = next;
			if (result != null) {
				next = getNext(k);
			}
			while (result != null && k.compareTo(lastKey) > 0) {
				result = next;
				next = getNext(k);
				// next = getNext();
			}
			return result;
		}
	}

	public Iterator<V> prefixSearch(final K arg0) {
		if (enableSIP)
			return new PrefixSearchIterator(arg0);
		else
			return new PrefixSearchIteratorWithoutSIP(arg0);
	}

	public Iterator<V> prefixSearch(final K arg0, final K min) {
		if (enableSIP)
			return new PrefixSearchIterator(arg0, min);
		else
			return new PrefixSearchIteratorWithoutSIP(arg0, min);
	}

	public Iterator<V> prefixSearch(final K arg0, final K smallest,
			final K largest) {
		if (enableSIP)
			return new PrefixSearchIteratorMaxMin(arg0, smallest, largest);
		else
			return new PrefixSearchIteratorMaxMinWithoutSIP(arg0, smallest,
					largest);
	}

	public Iterator<V> prefixSearchMax(final K arg0, final K largest) {
		if (enableSIP)
			return new PrefixSearchIteratorMaxMin(arg0, null, largest);
		else
			return new PrefixSearchIteratorMaxMinWithoutSIP(arg0, null, largest);
	}

	private V getMaximum(final int filename, final K arg0) {
		if (filename < 0)
			return null;
		InputStream fis;
		try {
			fis = new PageInputStream(filename, pageManager);
			final LuposObjectInputStream<V> in = new LuposObjectInputStreamWithoutReadingHeader(
					fis, null);
			final boolean leaf = in.readLuposBoolean();
			if (leaf) { // leaf node reached!
				V lastTriple = null;
				K lastKey = null;
				while (true) {
					final DBBPTreeEntry<K, V> e = nodeDeSerializer
					.getNextLeafEntry(in, lastKey, lastTriple);
					if (e == null || e.key == null) {
						in.close();
						return lastTriple;
					}
					final K key = e.key;
					final int compare = comparator.compare(key, arg0);
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
					final Tuple<K, Integer> nextEntry = getNextInnerNodeEntry(
							lastKey, in);
					if (nextEntry == null || nextEntry.getSecond() == 0
							|| nextEntry.getSecond() < 0) {
						in.close();
						return null;
					}
					if (nextEntry.getFirst() == null) {
						in.close();
						return getMaximum(nextEntry.getSecond(), arg0);
					}
					final int compare = comparator.compare(
							nextEntry.getFirst(), arg0);
					if (compare > 0) {
						in.close();
						final V t = getMaximum(nextEntry.getSecond(), arg0);
						if (t != null)
							return t;
						if (lastFilename > 0) {
							return getMaximum(lastFilename, arg0);
						} else
							return null;
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
		return getMaximum(rootPage, arg0);
	}

	public Object[] getClosestElements(final K arg0) {
		final List<Node<K, V>> navCol = navigateTo(arg0, rootPage,
				new LinkedList<Node<K, V>>());
		if (navCol == null)
			return null;
		final int pos = navCol.get(navCol.size() - 1).readKeys.size() - 1;
		final Object[] oa = new Object[2];
		oa[0] = getLeft(navCol, pos, arg0);
		oa[1] = getRight(navCol, pos, arg0);
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
			if (comparator.compare(leafNode.readKeys.get(pos), arg0) < 0)
				return leafNode.readValues.get(pos);
			// otherwise it is the one which is one left from the found!
			if (pos > 0)
				return leafNode.getValues().get(pos - 1);
			else {
				// get the last entry of the previous leaf node!
				int ipos = navCol.size() - 2;
				if (ipos < 0)
					return null;
				InnerNode<K, V> innerNode = (InnerNode<K, V>) navCol.get(ipos);
				while (innerNode.readReferences.size() == 1) {
					ipos--;
					if (ipos < 0)
						return null;
					innerNode = (InnerNode<K, V>) navCol.get(ipos);
				}
				return rightMostValue(getNode(innerNode.readReferences
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
			closeInputStreams(navCol);
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
							closeInputStreams(navCol);
							return null;
						}
						leafNode = (LeafNode<K, V>) getNode(leafNode
								.getNextLeafNode());
						pos2 = 0;
					}
				} while (comparator.compare(leafNode.readKeys.get(pos2), arg0) <= 0);
			}
			closeInputStreams(navCol);
			if (comparator.compare(leafNode.readKeys.get(pos2), arg0) > 0)
				return leafNode.readValues.get(pos2);
			else
				return null;

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
			final Node<K, V> n = getNode(rightMostFilename);
			if (n instanceof LeafNode)
				rightMostLeaf = (LeafNode<K, V>) n;
			else
				rightMostInnerNode = (InnerNode<K, V>) n;
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

				public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
					return new Iterator<java.util.Map.Entry<String, Integer>>() {
						private int current = 0;

						public boolean hasNext() {
							return current < max;
						}

						public java.util.Map.Entry<String, Integer> next() {
							final int zcurrent = current;
							current++;
							String s = "";
							for (int i = 0; i < maxNumbers
							- (int) Math.ceil(Math.log10(zcurrent)); i++)
								s += "0";
							return new MapEntry<String, Integer>("from_" + s
									+ zcurrent, zcurrent);
						}

						public void remove() {
							throw new UnsupportedOperationException();
						}

					};
				}

				public int size() {
					return max;
				}
			});
			final SIPParallelIterator<java.util.Map.Entry<String, Integer>, String> iterator = (SIPParallelIterator<java.util.Map.Entry<String, Integer>, String>) dbbptree
			.entrySet().iterator();

			int i = 10000;

			while (iterator.hasNext()) {
				String s = "";
				for (int j = 0; j < 6 - (int) Math.ceil(Math.log10(i)); j++)
					s += "0";
				final String key = "from_" + s + i;
				System.out.print(key + ":");
				final java.util.Map.Entry<String, Integer> entry = iterator
				.next(key);
				if (entry == null)
					break;
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
