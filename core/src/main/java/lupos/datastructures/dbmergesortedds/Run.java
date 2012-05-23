/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.queryresult.ParallelIterator;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;

public class Run<E extends Serializable> implements Iterable<Entry<E>> {

	protected final DBMergeSortedBag<E> dbmergesortedbag;

	public E max;
	public int size = 0;
	public int runID;
	public int numberFiles = 0;
	protected File file;
	private LuposObjectOutputStream os;

	public static final int STORAGELIMIT = 1000000000; // 1 GByte

	public static Run createInstance(final DBMergeSortedBag dbmergesortedbag) {
		return new Run(dbmergesortedbag);
	}

	public static Run createInstance(final DBMergeSortedBag dbmergesortedbag,
			final String tmp) {
		return new Run(dbmergesortedbag, tmp);
	}

	public Run(final DBMergeSortedBag<E> dbmergesortedbag) {
		this.dbmergesortedbag = dbmergesortedbag;
		try {
			runID = this.dbmergesortedbag.getNewId();
			final File dir = new File(dbmergesortedbag.folder[runID
			                                                  % dbmergesortedbag.folder.length]);
			dir.mkdirs();
			file = new File(dbmergesortedbag.folder[runID
			                                        % dbmergesortedbag.folder.length]
			                                        + runID);
			if (file.exists())
				file.delete();
			os = new LuposObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(file)));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public Run(final DBMergeSortedBag<E> dbmergesortedbag, final String tmp) {
		this.dbmergesortedbag = dbmergesortedbag;
		try {
			final File dir = new File(dbmergesortedbag.folder[0]);
			dir.mkdirs();
			file = new File(dbmergesortedbag.folder[0] + tmp);
			if (file.exists())
				file.delete();
			os = new LuposObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(file)));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	int indexOf(final E e) {
		if (max == null)
			return -1;
		if (dbmergesortedbag.comp.compare(e, max) >= 0)
			return e.equals(max) ? size - 1 : -1;
		int i = 0;
		for (final Entry<E> entry : this) {
			if (dbmergesortedbag.comp.compare(e, entry.e) <= 0) {
				return e.equals(entry.e) ? i : -1;
			}
			i++;
		}
		return -1;
	}

	void add(final Entry<E> e) {
		max = e.e;
		size++;
		if (file.length() > STORAGELIMIT) {
			try {
				os.close();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
			numberFiles++;
			file = new File(dbmergesortedbag.folder[runID
			                                        % dbmergesortedbag.folder.length]
			                                        + runID + "_" + numberFiles);
			if (file.exists())
				file.delete();
			try {
				os = new LuposObjectOutputStream(new BufferedOutputStream(
						new FileOutputStream(file)));
			} catch (final FileNotFoundException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}
		try {
			os.writeLuposObject(e);
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	Entry<E> remove(final E e) {
		try {
			dbmergesortedbag.currentRun.os.close();
		} catch (final IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
		final Run newRun = new Run(dbmergesortedbag, "tmp");
		Entry<E> res = null;
		boolean alreadyRemoved = false;
		for (final Entry<E> entry : this) {
			if (!alreadyRemoved && e.equals(entry.e)) {
				res = entry;
				alreadyRemoved = true;
			} else {
				newRun.add(entry);
			}
		}
		become(newRun);
		return res;
	}

	private void deleteAllFiles() {
		this.file.delete();
		for (int i = 0; i < numberFiles; i++) {
			if (i == 0)
				file = new File(dbmergesortedbag.folder[runID
				                                        % dbmergesortedbag.folder.length]
				                                        + runID);
			else
				file = new File(dbmergesortedbag.folder[runID
				                                        % dbmergesortedbag.folder.length]
				                                        + runID + "_" + i);
			if (file.exists())
				file.delete();
		}
	}

	void become(final Run<E> run) {
		try {
			clear();
		} catch (final FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (final IOException e2) {
			e2.printStackTrace();
		}
		try {
			run.os.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		for (final Entry<E> e : run) {
			this.add(e);
		}
		try {
			this.os.flush();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		run.file.delete();
		this.max = run.max;
		this.size = run.size;
	}

	void removeAll(final Collection<E> elements) {
		try {
			this.os.close();
		} catch (final IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
		final Run newRun = new Run(dbmergesortedbag, "tmp");
		for (final Entry<E> entry : this) {
			if (!elements.contains(entry.e)) {
				newRun.add(entry);
			}
		}
		become(newRun);
	}

	Entry<E> getIndex(final int i) {
		final Iterator<Entry<E>> iter = iterator();
		Entry result = null;
		for (int j = 0; j <= i; j++) {
			result = iter.next();
		}
		return result;
	}

	void clear() throws FileNotFoundException, IOException {
		deleteAllFiles();
		file = new File(dbmergesortedbag.folder[runID
		                                        % dbmergesortedbag.folder.length]
		                                        + runID);
		os = new LuposObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(file)));
	}

	boolean contains(final E e) {
		return indexOf(e) >= 0;
	}

	boolean containsAny(final Collection<E> elements) {
		for (final Entry<E> entry : this) {
			if (elements.contains(entry.e))
				return true;
		}
		return false;
	}

	public ParallelIterator<Entry<E>> iterator() {
		try {
			return new ParallelIterator<Entry<E>>() {
				File fileLocal = new File(dbmergesortedbag.folder[runID
				                                                  % dbmergesortedbag.folder.length]
				                                                  + runID);
				int currentFile = 0;
				LuposObjectInputStream<E> is = new LuposObjectInputStream<E>(
						new BufferedInputStream(new FileInputStream(fileLocal)),
						dbmergesortedbag.classOfElements);
				boolean isClosed = false;
				Entry<E> next = null;
				int n = 0;

				public boolean hasNext() {
					if (next == null)
						next = next();
					return next != null;
				}

				public Entry<E> next() {
					if (next != null) {
						final Entry<E> res = next;
						next = null;
						return res;
					}
					if (isClosed)
						return null;
					try {
						Entry<E> e = null;
						try {
							e = is.readLuposEntry();
						} catch (final EOFException e1) {
						}
						if (e == null) {
							if (fileLocal.length() > STORAGELIMIT) {
								currentFile++;
								fileLocal = new File(
										dbmergesortedbag.folder[runID
										                        % dbmergesortedbag.folder.length]
										                        + runID + "_" + currentFile);
								if (fileLocal.exists()) {
									try {
										is.close();
									} catch (final IOException ee) {
									}
									is = new LuposObjectInputStream<E>(
											new BufferedInputStream(
													new FileInputStream(
															fileLocal)),
															dbmergesortedbag.classOfElements);
									e = is.readLuposEntry();
								}
							}
						}
						if (e != null) {
							e.comp = dbmergesortedbag.comp;
							e.runMatters = false;
							e.n = n++;
							e.run = runID;
						} else {
							close();
						}
						return e;
					} catch (final EOFException e) {

					} catch (final IOException e) {
						e.printStackTrace();
					} catch (final ClassNotFoundException e) {
						e.printStackTrace();
					}
					close();
					return null;
				}

				public void remove() {
					throw (new UnsupportedOperationException());
				}

				@Override
				public void finalize() {
					close();
				}

				public void close() {
					if (!isClosed) {
						try {
							is.close();
							isClosed = true;
						} catch (final IOException e) {
						}
					}
				}
			};
		} catch (final EOFException e) {
			return new ParallelIterator<Entry<E>>() {
				public boolean hasNext() {
					return false;
				}

				public Entry<E> next() {
					return null;
				}

				public void remove() {
					throw (new UnsupportedOperationException());
				}

				public void close() {
				}
			};
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public String toString() {
		final Iterator<Entry<E>> iter = iterator();
		String result = "[";
		while (iter.hasNext()) {
			result += iter.next();
			if (iter.hasNext())
				result += ", ";
		}
		result += "]";
		return result;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (os != null)
				os.close();
		} finally {
			super.finalize();
		}
	}

	public void close() throws IOException {
		if (os != null)
			os.close();
	}

	public void flush() throws IOException {
		if (os != null)
			os.flush();
	}
}
