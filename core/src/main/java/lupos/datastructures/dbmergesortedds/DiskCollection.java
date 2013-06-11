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
/**
 *
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.items.Triple;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectInputStreamWithoutReadingHeader;
import lupos.io.LuposObjectOutputStream;
import lupos.io.LuposObjectOutputStreamWithoutWritingHeader;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.FileHelper;

/**
 * This class implements a collection, which stores its entries on disk and not
 * in main memory. This class supports storing big data sets, which do not fit
 * into main memory any more. DiskCollection adds entries fast (to the end) and
 * provides a fast implementation to retrieve the iterator of the entries
 * besides the fact that all is stored and read from disk. Most of the other
 * methods are rather slow.
 *
 * @author groppe
 *
 */
public class DiskCollection<E extends Serializable> implements Collection<E>,
		Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 950171040308418130L;

	private long size = 0;
	private String filename;
	private int numberFiles = 0;
	private File file = null;

	private static volatile int id = 0;
	private static String[] folder = new String[] { "tmp//DiskCollection//" };
	private LuposObjectOutputStream out = null;
	private boolean wroteOnDisk = false;
	private final Class<? extends E> classOfElements;

	private static final int STORAGELIMIT = 1000000000;

	private final static ReentrantLock lock = new ReentrantLock();

	static {
		lock.lock();
		try {
			for (final String f : folder) {
				FileHelper.deleteDirectory(new File(f));
			}
		} finally {
			lock.unlock();
		}
	}

	public static void setTmpDir(final String[] dir) {
		lock.lock();
		try {
			if (dir == null || dir.length == 0
					|| (dir.length == 1 && dir[0].compareTo("") == 0)) {
				return;
			}
			folder = new String[dir.length];
			for (int i = 0; i < dir.length; i++) {
				if (!(dir[i].endsWith("//") || dir[i].endsWith("/") || dir[i]
						.endsWith("\""))) {
					dir[i] = dir[i] + "//";
				}
				folder[i] = dir[i] + "tmp//DiskCollection//";
				FileHelper.deleteDirectory(new File(folder[i]));
			}
		} finally {
			lock.unlock();
		}
	}

	public static void removeCollectionsFromDisk() {
		lock.lock();
		try {
			for (final String f : folder) {
				FileHelper.deleteDirectory(new File(f));
			}
		} finally {
			lock.unlock();
		}
	}

	public static void makeFolders(){
		lock.lock();
		try {
			for (final String singleFolder : DiskCollection.folder) {
				final File f = new File(singleFolder);
				f.mkdirs();
			}
		} finally {
			lock.unlock();
		}
	}

	public DiskCollection(final Class<? extends E> classOfElements) {
		DiskCollection.makeFolders();
		this.makeNewFile();
		this.classOfElements = classOfElements;
	}

	public DiskCollection(final Class<? extends E> classOfElements, final String filename) {
		DiskCollection.makeFolders();
		this.makeNewFile(filename);
		this.classOfElements = classOfElements;
	}

	public DiskCollection(final Class<? extends E> classOfElements,
			final long size, final String filename, final int numberFiles) {
		this.size = size;
		this.filename = filename;
		this.classOfElements = classOfElements;
		this.numberFiles = numberFiles;
	}

	public static String newBaseFilename() {
		return DiskCollection.newBaseFilename("DiskCollection");
	}

	public static String newBaseFilename(final String prefix) {
		lock.lock();
		try {
			id++;
			return folder[id % folder.length] + prefix + id + "_";
		} finally {
			lock.unlock();
		}
	}

	private void makeNewFile() {
		this.makeNewFile(newBaseFilename());
	}

	private void makeNewFile(final String filename) {
		this.filename = filename;
		FileOutputStream fos;
		this.numberFiles = 0;
		try {
			this.file = new File(filename + this.numberFiles);
			if (this.file.exists()) {
				this.file.delete();
			}
			fos = new FileOutputStream(this.file, false);
			this.out = new LuposObjectOutputStreamWithoutWritingHeader(
					new BufferedOutputStream(fos));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(final E arg0) {
		try {
			if (this.out == null) {
				this.file = new File(this.filename + this.numberFiles);
				final FileOutputStream fos = new FileOutputStream(this.file, true);
				this.out = new LuposObjectOutputStreamWithoutWritingHeader(
						new BufferedOutputStream(fos));
			}
			if (this.file.length() > STORAGELIMIT) {
				this.out.close();
				this.numberFiles++;
				this.file = new File(this.filename + this.numberFiles);
				final FileOutputStream fos = new FileOutputStream(this.file, true);
				this.out = new LuposObjectOutputStreamWithoutWritingHeader(
						new BufferedOutputStream(fos));

			}
			this.out.writeLuposObject(arg0);
			this.size++;
			return true;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e);
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag = true;
		for (final E e : arg0) {
			flag = flag && this.add(e);
		}
		return flag;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		if (this.out != null) {
			try {
				this.out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			this.out = null;
		}
		this.deleteAllFiles();
		this.makeNewFile();
		this.size = 0;
	}

	private void deleteAllFiles() {
		deleteAllFiles(this.filename, this.numberFiles);
	}

	private static void deleteAllFiles(final String baseFilename, final int numberFiles) {
		for (int i = 0; i <= numberFiles; i++) {
			FileHelper.deleteFile(baseFilename + i);
		}
	}

	public void close() {
		if (this.out != null) {
			try {
				this.out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			this.out = null;
		}
	}


	public void release() {
		if (this.out != null) {
			try {
				this.out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			this.out = null;
		}
		this.deleteAllFiles();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object arg0) {
		try {
			if (this.out != null) {
				this.out.close();
				this.out = null;
			}
			for (int i = 0; i <= this.numberFiles; i++) {
				final LuposObjectInputStream in = new LuposObjectInputStreamWithoutReadingHeader<E>(
						new BufferedInputStream(new FileInputStream(this.filename
								+ this.numberFiles)), (Class<E>) arg0.getClass());
				try {
					while (true) {
						final E e = (E) in.readLuposObject();
						if (e == null) {
							in.close();
							break;
						}
						if (e.equals(arg0)) {
							in.close();
							return true;
						}
					}
				} catch (final EOFException e) {
					in.close();
				}
			}
		} catch (final FileNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> arg0) {
		final HashSet<E> hs = new HashSet<E>();
		hs.addAll((Collection<E>) arg0);
		try {
			if (this.out != null) {
				this.out.close();
				this.out = null;
			}
			for (int i = 0; i <= this.numberFiles; i++) {
				final LuposObjectInputStream in = new LuposObjectInputStreamWithoutReadingHeader<E>(
						new BufferedInputStream(new FileInputStream(this.filename
								+ i)), this.classOfElements);
				try {
					while (true) {
						final E e = (E) in.readLuposObject();
						if (e == null) {
							in.close();
							if (hs.size() == 0) {
								return true;
							} else {
								break;
							}
						}
						hs.remove(e);
					}
				} catch (final EOFException e) {
					in.close();
					if (hs.size() == 0) {
						return true;
					}
				}
			}
		} catch (final FileNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return (this.size == 0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public ParallelIterator<E> iterator() {
		try {
			// if (released)
			// System.err
			// .println(
			// "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! already released !!!!!!!!!!!!!!!!!! "
			// + filename + "\n" + traceString);
			if (this.out != null) {
				try {
					this.out.close();
				} catch (final IOException ex) {
					System.err.println(ex);
					ex.printStackTrace();
				}
				this.out = null;
			}
			if (!this.isEmpty()) {
				return new ParallelIterator<E>() {
					private LuposObjectInputStream<E> in;
					private int currentFile = 0;
					private final String baseFilename = DiskCollection.this.filename;
					private final int numberFilesLocal = DiskCollection.this.numberFiles;
					private E next;
					{
						this.in = new LuposObjectInputStreamWithoutReadingHeader<E>(
								new BufferedInputStream(new FileInputStream(
										this.baseFilename + "0")), DiskCollection.this.classOfElements);
						this.next();
					}

					@Override
					public boolean hasNext() {
						return (this.next != null);
					}

					@Override
					public E next() {
						final E current = this.next;
						this.next = null;
						try {
							this.next = this.in.readLuposObject();
						} catch (final EOFException e) {
						} catch (final IOException e) {
							System.err.println(e);
							e.printStackTrace();
						} catch (final ClassNotFoundException e) {
							System.err.println(e);
							e.printStackTrace();
						} catch (final URISyntaxException e) {
							System.err.println(e);
							e.printStackTrace();
						}
						if (this.next == null) {
							if (this.openNextFile()) {
								try {
									this.next = this.in.readLuposObject();
								} catch (final IOException e) {
									System.err.println(e);
									e.printStackTrace();
								} catch (final ClassNotFoundException e) {
									System.err.println(e);
									e.printStackTrace();
								} catch (final URISyntaxException e) {
									System.err.println(e);
									e.printStackTrace();
								}
							}
						}
						return current;
					}

					private boolean openNextFile() {
						if (this.currentFile < this.numberFilesLocal) {
							try {
								this.in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
							this.currentFile++;
							try {
								this.in = new LuposObjectInputStreamWithoutReadingHeader<E>(
										new BufferedInputStream(
												new FileInputStream(
														this.baseFilename
																+ this.currentFile)),
										DiskCollection.this.classOfElements);
							} catch (final EOFException e1) {
								System.err.println(e1);
								e1.printStackTrace();
							} catch (final FileNotFoundException e1) {
								System.err.println(e1);
								e1.printStackTrace();
							} catch (final IOException e1) {
								System.err.println(e1);
								e1.printStackTrace();
							}
							return true;
						} else {
							return false;
						}
					}

					@Override
					public void remove() {
						throw (new UnsupportedOperationException(
								"This iterator is read-only."));
					}

					@Override
					protected void finalize() throws Throwable {
						try {
							this.close();
						} finally {
							super.finalize();
						}
					}

					@Override
					public void close() {
						if (this.in != null) {
							try {
								this.in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
							this.in = null;
						}
					}
				};
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return new ParallelIterator<E>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public E next() {
				return null;
			}

			@Override
			public void remove() {
			}

			@Override
			public void close() {
			}
		};
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object arg0) {
		final String oldFilename = this.filename;
		final int oldNumberFiles = this.numberFiles;
		boolean result = false;
		boolean firstTime = true;
		for (final E e : this) {
			if (firstTime) {
				firstTime = false;
				if (this.out != null) {
					try {
						this.out.close();
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
					this.out = null;
				}
				this.makeNewFile();
				this.size = 0;
			}
			if (!e.equals(arg0) && !result) {
				this.add(e);
			} else {
				result = true;
			}
		}

		deleteAllFiles(oldFilename, oldNumberFiles);

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> arg0) {
		final String oldFilename = this.filename;
		final int oldNumberFiles = this.numberFiles;
		boolean result = false;
		boolean firstTime = true;
		for (final E e : this) {
			if (firstTime) {
				firstTime = false;
				if (this.out != null) {
					try {
						this.out.close();
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
					this.out = null;
				}
				this.makeNewFile();
				this.size = 0;
			}
			if (!arg0.contains(e)) {
				this.add(e);
			} else {
				result = true;
			}
		}

		deleteAllFiles(oldFilename, oldNumberFiles);

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		final String oldFilename = this.filename;
		final int oldNumberFiles = this.numberFiles;
		boolean result = false;
		boolean firstTime = true;
		for (final E e : this) {
			if (firstTime) {
				firstTime = false;
				if (this.out != null) {
					try {
						this.out.close();
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
					this.out = null;
				}
				this.makeNewFile();
				this.size = 0;
			}
			if (arg0.contains(e)) {
				this.add(e);
			} else {
				result = true;
			}
		}

		deleteAllFiles(oldFilename, oldNumberFiles);

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return (int) this.size;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	public void writeLuposObject(final LuposObjectOutputStream out) throws IOException {
		this.writeCommonPart(out);
		Registration.serializeClass(this.classOfElements, out);
	}

	public void writeLuposObject(final OutputStream out) throws IOException {
		this.writeCommonPart(out);
		Registration.serializeClass(this.classOfElements, out);
	}

	private void writeCommonPart(final OutputStream out) throws IOException {
		if (this.out != null) {
			this.out.close();
			this.out = null;
		}
		OutHelper.writeLuposLong(this.size, out);
		OutHelper.writeLuposString(this.filename, out);
		OutHelper.writeLuposInt(this.numberFiles, out);
		this.wroteOnDisk = true;
	}

	public static DiskCollection readAndCreateLuposObject(
			final LuposObjectInputStream in) throws IOException,
			ClassNotFoundException {
		final long size = InputHelper.readLuposLong(in);
		final String filename = InputHelper.readLuposString(in);
		final int numberFiles = InputHelper.readLuposInt((InputStream) in);
		final Class<?> classOfElements = Registration.deserializeId(in)[0];
		DiskCollection dc;
		if (classOfElements == Triple.class) {
			dc = new DiskCollection<Triple>((Class<Triple>) classOfElements,
					size, filename, numberFiles);
		} else {
			dc = new DiskCollection(classOfElements, size, filename,
					numberFiles);
		}
		dc.wroteOnDisk = true;
		return dc;
	}

	public static DiskCollection readAndCreateLuposObject(final InputStream in) throws IOException, ClassNotFoundException {
		final long size = InputHelper.readLuposLong(in);
		final String filename = InputHelper.readLuposString(in);
		final int numberFiles = InputHelper.readLuposInt(in);
		final Class<?> classOfElements = Registration.deserializeId(in)[0];
		DiskCollection dc;
		if (classOfElements == Triple.class) {
			dc = new DiskCollection<Triple>((Class<Triple>) classOfElements,
					size, filename, numberFiles);
		} else {
			dc = new DiskCollection(classOfElements, size, filename,
					numberFiles);
		}
		dc.wroteOnDisk = true;
		return dc;
	}

	public int lengthLuposObject() {
		return DiskCollection.lengthLuposObject(this.filename);
	}

	public static int lengthLuposObject(final String filename){
		return	LengthHelper.lengthLuposLong() +
				LengthHelper.lengthLuposString(filename) +
				LengthHelper.lengthLuposInt() +
				LengthHelper.lengthLuposByte();
	}

	public static int lengthLuposObjectOfNextDiskCollection(){
		return DiskCollection.lengthLuposObject(folder[id % folder.length] + "DiskCollection" + (id+1) + "_");
	}

	@Override
	public String toString() {
		String s = "";
		for (final E e : this) {
			if (s.compareTo("") != 0) {
				s += ", ";
			}
			s += e.toString();
		}
		return "[ " + s + " ]";
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (this.out != null) {
				this.out.close();
				this.out = null;
			}
			if (!this.wroteOnDisk) {
				final File file = new File(this.filename);
				if (file.exists()) {
					file.delete();
				}
			}
		} finally {
			super.finalize();
		}
	}
}
