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
import lupos.misc.IOCostsInputStream;
import lupos.misc.IOCostsOutputStream;
public class Run<E extends Serializable> implements Iterable<Entry<E>> {

	protected final DBMergeSortedBag<E> dbmergesortedbag;

	public E max;
	public int size = 0;
	public int runID;
	public int numberFiles = 0;
	protected File file;
	private LuposObjectOutputStream os;

	/** Constant <code>STORAGELIMIT=1000000000</code> */
	public static final int STORAGELIMIT = 1000000000; // 1 GByte

	/**
	 * <p>createInstance.</p>
	 *
	 * @param dbmergesortedbag a {@link lupos.datastructures.dbmergesortedds.DBMergeSortedBag} object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.Run} object.
	 */
	public static Run createInstance(final DBMergeSortedBag dbmergesortedbag) {
		return new Run(dbmergesortedbag);
	}

	/**
	 * <p>createInstance.</p>
	 *
	 * @param dbmergesortedbag a {@link lupos.datastructures.dbmergesortedds.DBMergeSortedBag} object.
	 * @param tmp a {@link java.lang.String} object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.Run} object.
	 */
	public static Run createInstance(final DBMergeSortedBag dbmergesortedbag,
			final String tmp) {
		return new Run(dbmergesortedbag, tmp);
	}

	/**
	 * <p>Constructor for Run.</p>
	 *
	 * @param dbmergesortedbag a {@link lupos.datastructures.dbmergesortedds.DBMergeSortedBag} object.
	 */
	public Run(final DBMergeSortedBag<E> dbmergesortedbag) {
		this.dbmergesortedbag = dbmergesortedbag;
		try {
			this.runID = this.dbmergesortedbag.getNewId();
			final File dir = new File(dbmergesortedbag.folder[this.runID % dbmergesortedbag.folder.length]);
			dir.mkdirs();
			this.file = new File(dbmergesortedbag.folder[this.runID % dbmergesortedbag.folder.length] + this.runID);
			if (this.file.exists()) {
				this.file.delete();
			}
			this.os = new LuposObjectOutputStream(this.dbmergesortedbag.sortConfiguration.createOutputStream(IOCostsOutputStream.createIOCostsOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)))));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>Constructor for Run.</p>
	 *
	 * @param dbmergesortedbag a {@link lupos.datastructures.dbmergesortedds.DBMergeSortedBag} object.
	 * @param tmp a {@link java.lang.String} object.
	 */
	public Run(final DBMergeSortedBag<E> dbmergesortedbag, final String tmp) {
		this.dbmergesortedbag = dbmergesortedbag;
		try {
			final File dir = new File(dbmergesortedbag.folder[0]);
			dir.mkdirs();
			this.file = new File(dbmergesortedbag.folder[0] + tmp);
			if (this.file.exists()) {
				this.file.delete();
			}
			this.os = new LuposObjectOutputStream(this.dbmergesortedbag.sortConfiguration.createOutputStream(IOCostsOutputStream.createIOCostsOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)))));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	int indexOf(final E e) {
		if (this.max == null) {
			return -1;
		}
		if (this.dbmergesortedbag.comp.compare(e, this.max) >= 0) {
			return e.equals(this.max) ? this.size - 1 : -1;
		}
		int i = 0;
		for (final Entry<E> entry : this) {
			if (this.dbmergesortedbag.comp.compare(e, entry.e) <= 0) {
				return e.equals(entry.e) ? i : -1;
			}
			i++;
		}
		return -1;
	}

	void add(final Entry<E> e) {
		this.max = e.e;
		this.size++;
		if (this.file.length() > STORAGELIMIT) {
			try {
				this.os.close();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
			this.numberFiles++;
			this.file = new File(this.dbmergesortedbag.folder[this.runID
			                                        % this.dbmergesortedbag.folder.length]
			                                        + this.runID + "_" + this.numberFiles);
			if (this.file.exists()) {
				this.file.delete();
			}
			try {
				this.os = new LuposObjectOutputStream(IOCostsOutputStream.createIOCostsOutputStream(new BufferedOutputStream(new FileOutputStream(this.file))));
			} catch (final FileNotFoundException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			} catch (final IOException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}
		try {
			this.os.writeLuposObject(e);
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	Entry<E> remove(final E e) {
		try {
			this.dbmergesortedbag.currentRun.os.close();
		} catch (final IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
		final Run newRun = new Run(this.dbmergesortedbag, "tmp");
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
		this.become(newRun);
		return res;
	}

	private void deleteAllFiles() {
		this.file.delete();
		for (int i = 0; i < this.numberFiles; i++) {
			if (i == 0) {
				this.file = new File(this.dbmergesortedbag.folder[this.runID
				                                        % this.dbmergesortedbag.folder.length]
				                                        + this.runID);
			} else {
				this.file = new File(this.dbmergesortedbag.folder[this.runID
				                                        % this.dbmergesortedbag.folder.length]
				                                        + this.runID + "_" + i);
			}
			if (this.file.exists()) {
				this.file.delete();
			}
		}
	}

	void become(final Run<E> run) {
		try {
			this.clear();
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
		final Run newRun = new Run(this.dbmergesortedbag, "tmp");
		for (final Entry<E> entry : this) {
			if (!elements.contains(entry.e)) {
				newRun.add(entry);
			}
		}
		this.become(newRun);
	}

	Entry<E> getIndex(final int i) {
		final Iterator<Entry<E>> iter = this.iterator();
		Entry result = null;
		for (int j = 0; j <= i; j++) {
			result = iter.next();
		}
		return result;
	}

	void clear() throws FileNotFoundException, IOException {
		this.deleteAllFiles();
		this.file = new File(this.dbmergesortedbag.folder[this.runID
		                                        % this.dbmergesortedbag.folder.length]
		                                        + this.runID);
		this.os = new LuposObjectOutputStream(this.dbmergesortedbag.sortConfiguration.createOutputStream(IOCostsOutputStream.createIOCostsOutputStream(new BufferedOutputStream(new FileOutputStream(this.file)))));
	}

	boolean contains(final E e) {
		return this.indexOf(e) >= 0;
	}

	boolean containsAny(final Collection<E> elements) {
		for (final Entry<E> entry : this) {
			if (elements.contains(entry.e)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>iterator.</p>
	 *
	 * @return a {@link lupos.datastructures.queryresult.ParallelIterator} object.
	 */
	@Override
	public ParallelIterator<Entry<E>> iterator() {
		try {
			this.os.close();
		} catch (final IOException e2) {
		}
		try {
			return new ParallelIterator<Entry<E>>() {
				File fileLocal = new File(Run.this.dbmergesortedbag.folder[Run.this.runID
				                                                  % Run.this.dbmergesortedbag.folder.length]
				                                                  + Run.this.runID);
				int currentFile = 0;
				LuposObjectInputStream<E> is = new LuposObjectInputStream<E>(
						Run.this.dbmergesortedbag.sortConfiguration.createInputStream(IOCostsInputStream.createIOCostsInputStream(new BufferedInputStream(new FileInputStream(this.fileLocal)))),
						Run.this.dbmergesortedbag.classOfElements);
				boolean isClosed = false;
				Entry<E> next = null;
				int n = 0;

				@Override
				public boolean hasNext() {
					if (this.next == null) {
						this.next = this.next();
					}
					return this.next != null;
				}

				@Override
				public Entry<E> next() {
					if (this.next != null) {
						final Entry<E> res = this.next;
						this.next = null;
						return res;
					}
					if (this.isClosed) {
						return null;
					}
					try {
						Entry<E> e = null;
						try {
							e = this.is.readLuposEntry();
						} catch (final EOFException e1) {
							this.close();
						}
						if (e == null) {
							if (this.fileLocal.length() > STORAGELIMIT) {
								this.currentFile++;
								this.fileLocal = new File(
										Run.this.dbmergesortedbag.folder[Run.this.runID
										                        % Run.this.dbmergesortedbag.folder.length]
										                        + Run.this.runID + "_" + this.currentFile);
								if (this.fileLocal.exists()) {
									try {
										this.is.close();
									} catch (final IOException ee) {
									}
									this.is = new LuposObjectInputStream<E>(
											Run.this.dbmergesortedbag.sortConfiguration.createInputStream(IOCostsInputStream.createIOCostsInputStream(new BufferedInputStream(new FileInputStream(this.fileLocal)))),
															Run.this.dbmergesortedbag.classOfElements);
									e = this.is.readLuposEntry();
								}
							}
						}
						if (e != null) {
							e.comp = Run.this.dbmergesortedbag.comp;
							e.runMatters = false;
							e.n = this.n++;
							e.run = Run.this.runID;
						} else {
							this.close();
						}
						return e;
					} catch (final EOFException e) {

					} catch (final IOException e) {
						e.printStackTrace();
					} catch (final ClassNotFoundException e) {
						e.printStackTrace();
					}
					this.close();
					return null;
				}

				@Override
				public void remove() {
					throw (new UnsupportedOperationException());
				}

				@Override
				public void finalize() {
					this.close();
				}

				@Override
				public void close() {
					if (!this.isClosed) {
						try {
							this.is.close();
							this.isClosed = true;
						} catch (final IOException e) {
						}
					}
				}
			};
		} catch (final EOFException e) {
			return new ParallelIterator<Entry<E>>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public Entry<E> next() {
					return null;
				}

				@Override
				public void remove() {
					throw (new UnsupportedOperationException());
				}

				@Override
				public void close() {
				}
			};
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty() {
		return this.size == 0;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final Iterator<Entry<E>> iter = this.iterator();
		String result = "[";
		while (iter.hasNext()) {
			result += iter.next();
			if (iter.hasNext()) {
				result += ", ";
			}
		}
		result += "]";
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		try {
			if (this.os != null) {
				this.os.close();
			}
		} finally {
			super.finalize();
		}
	}

	/**
	 * <p>close.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public void close() throws IOException {
		if (this.os != null) {
			this.os.close();
		}
	}

	/**
	 * <p>flush.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	public void flush() throws IOException {
		if (this.os != null) {
			this.os.flush();
		}
	}
}
