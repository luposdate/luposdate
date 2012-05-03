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
	private Class<? extends E> classOfElements;

	private static final int STORAGELIMIT = 1000000000;

	private final static ReentrantLock lock = new ReentrantLock();

	static {
		lock.lock();
		try {
			for (final String f : folder)
				FileHelper.deleteDirectory(new File(f));
		} finally {
			lock.unlock();
		}
	}

	public static void setTmpDir(final String[] dir) {
		lock.lock();
		try {
			if (dir == null || dir.length == 0
					|| (dir.length == 1 && dir[0].compareTo("") == 0))
				return;
			folder = new String[dir.length];
			for (int i = 0; i < dir.length; i++) {
				if (!(dir[i].endsWith("//") || dir[i].endsWith("/") || dir[i]
						.endsWith("\"")))
					dir[i] = dir[i] + "//";
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
			for (final String f : folder)
				FileHelper.deleteDirectory(new File(f));
		} finally {
			lock.unlock();
		}
	}

	public DiskCollection(final Class<? extends E> classOfElements) {
		lock.lock();
		try {
			for (final String singleFolder : DiskCollection.folder) {
				final File f = new File(singleFolder);
				f.mkdirs();
			}
		} finally {
			lock.unlock();
		}
		makeNewFile();
		this.classOfElements = classOfElements;
	}

	public DiskCollection(final Class<? extends E> classOfElements,
			final String filename) {
		lock.lock();
		try {
			for (final String singleFolder : DiskCollection.folder) {
				final File f = new File(singleFolder);
				f.mkdirs();
			}
		} finally {
			lock.unlock();
		}
		makeNewFile(filename);
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
		lock.lock();
		try {
			id++;
			final String filename = folder[id % folder.length]
					+ "DiskCollection" + id + "_";
			return filename;
		} finally {
			lock.unlock();
		}

	}

	private void makeNewFile() {
		makeNewFile(newBaseFilename());
	}

	private void makeNewFile(final String filename) {
		this.filename = filename;
		FileOutputStream fos;
		numberFiles = 0;
		try {
			file = new File(filename + numberFiles);
			if (file.exists())
				file.delete();
			fos = new FileOutputStream(file, false);
			out = new LuposObjectOutputStreamWithoutWritingHeader(
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
	public boolean add(final E arg0) {
		try {
			if (out == null) {
				file = new File(filename + numberFiles);
				final FileOutputStream fos = new FileOutputStream(file, true);
				out = new LuposObjectOutputStreamWithoutWritingHeader(
						new BufferedOutputStream(fos));
			}
			if (file.length() > STORAGELIMIT) {
				out.close();
				numberFiles++;
				file = new File(filename + numberFiles);
				final FileOutputStream fos = new FileOutputStream(file, true);
				out = new LuposObjectOutputStreamWithoutWritingHeader(
						new BufferedOutputStream(fos));

			}
			out.writeLuposObject(arg0);
			size++;
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
	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag = true;
		for (final E e : arg0) {
			flag = flag && add(e);
		}
		return flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		if (out != null) {
			try {
				out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			out = null;
		}
		deleteAllFiles();
		makeNewFile();
		size = 0;
	}

	private void deleteAllFiles() {
		deleteAllFiles(filename, numberFiles);
	}

	private static void deleteAllFiles(final String baseFilename,
			final int numberFiles) {
		for (int i = 0; i <= numberFiles; i++) {
			FileHelper.deleteFile(baseFilename + i);
		}
	}

	public void close() {
		if (out != null) {
			try {
				out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			out = null;
		}
	}

	// protected boolean released = false;
	// protected String traceString = "";

	public void release() {
		// released = true;
		// for (final StackTraceElement trace : new Throwable().getStackTrace())
		// traceString += trace + "\n";

		if (out != null) {
			try {
				out.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			out = null;
		}
		deleteAllFiles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(final Object arg0) {
		try {
			if (out != null) {
				out.close();
				out = null;
			}
			for (int i = 0; i <= numberFiles; i++) {
				final LuposObjectInputStream in = new LuposObjectInputStreamWithoutReadingHeader<E>(
						new BufferedInputStream(new FileInputStream(filename
								+ numberFiles)), (Class<E>) arg0.getClass());
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
	public boolean containsAll(final Collection<?> arg0) {
		final HashSet<E> hs = new HashSet<E>();
		hs.addAll((Collection<E>) arg0);
		try {
			if (out != null) {
				out.close();
				out = null;
			}
			for (int i = 0; i <= numberFiles; i++) {
				final LuposObjectInputStream in = new LuposObjectInputStreamWithoutReadingHeader<E>(
						new BufferedInputStream(new FileInputStream(filename
								+ i)), classOfElements);
				try {
					while (true) {
						final E e = (E) in.readLuposObject();
						if (e == null) {
							in.close();
							if (hs.size() == 0)
								return true;
							else
								break;
						}
						hs.remove(e);
					}
				} catch (final EOFException e) {
					in.close();
					if (hs.size() == 0)
						return true;
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
	public boolean isEmpty() {
		return (size == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public ParallelIterator<E> iterator() {
		try {
			// if (released)
			// System.err
			// .println(
			// "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! already released !!!!!!!!!!!!!!!!!! "
			// + filename + "\n" + traceString);
			if (out != null) {
				try {
					out.close();
				} catch (final IOException ex) {
					System.err.println(ex);
					ex.printStackTrace();
				}
				out = null;
			}
			if (!isEmpty())
				return new ParallelIterator<E>() {
					private LuposObjectInputStream<E> in;
					private int currentFile = 0;
					private final String baseFilename = filename;
					private final int numberFilesLocal = numberFiles;
					private E next;
					{
						in = new LuposObjectInputStreamWithoutReadingHeader<E>(
								new BufferedInputStream(new FileInputStream(
										baseFilename + "0")), classOfElements);
						next();
					}

					public boolean hasNext() {
						return (next != null);
					}

					public E next() {
						final E current = next;
						next = null;
						try {
							next = in.readLuposObject();
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
						if (next == null) {
							if (openNextFile()) {
								try {
									next = in.readLuposObject();
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
						if (currentFile < numberFilesLocal) {
							try {
								in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
							currentFile++;
							try {
								in = new LuposObjectInputStreamWithoutReadingHeader<E>(
										new BufferedInputStream(
												new FileInputStream(
														baseFilename
																+ currentFile)),
										classOfElements);
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
						} else
							return false;
					}

					public void remove() {
						throw (new UnsupportedOperationException(
								"This iterator is read-only."));
					}

					@Override
					protected void finalize() throws Throwable {
						try {
							close();
						} finally {
							super.finalize();
						}
					}

					public void close() {
						if (in != null) {
							try {
								in.close();
							} catch (final IOException e) {
								System.err.println(e);
								e.printStackTrace();
							}
							in = null;
						}
					}
				};
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return new ParallelIterator<E>() {
			public boolean hasNext() {
				return false;
			}

			public E next() {
				return null;
			}

			public void remove() {
			}

			public void close() {
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(final Object arg0) {
		final String oldFilename = filename;
		final int oldNumberFiles = numberFiles;
		boolean result = false;
		boolean firstTime = true;
		for (final E e : this) {
			if (firstTime) {
				firstTime = false;
				if (out != null) {
					try {
						out.close();
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
					out = null;
				}
				makeNewFile();
				size = 0;
			}
			if (!e.equals(arg0) && !result)
				add(e);
			else
				result = true;
		}

		deleteAllFiles(oldFilename, oldNumberFiles);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(final Collection<?> arg0) {
		final String oldFilename = filename;
		final int oldNumberFiles = numberFiles;
		boolean result = false;
		boolean firstTime = true;
		for (final E e : this) {
			if (firstTime) {
				firstTime = false;
				if (out != null) {
					try {
						out.close();
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
					out = null;
				}
				makeNewFile();
				size = 0;
			}
			if (!arg0.contains(e))
				add(e);
			else
				result = true;
		}

		deleteAllFiles(oldFilename, oldNumberFiles);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(final Collection<?> arg0) {
		final String oldFilename = filename;
		final int oldNumberFiles = numberFiles;
		boolean result = false;
		boolean firstTime = true;
		for (final E e : this) {
			if (firstTime) {
				firstTime = false;
				if (out != null) {
					try {
						out.close();
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
					out = null;
				}
				makeNewFile();
				size = 0;
			}
			if (arg0.contains(e))
				add(e);
			else
				result = true;
		}

		deleteAllFiles(oldFilename, oldNumberFiles);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return (int) size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	private void writeObject(final java.io.ObjectOutputStream out)
			throws IOException {
		if (this.out != null) {
			this.out.close();
			this.out = null;
		}
		out.writeLong(size);
		out.writeUTF(filename);
		out.writeInt(numberFiles);
		out.writeObject(classOfElements);
		wroteOnDisk = true;
	}

	private void readObject(final java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		size = in.readLong();
		filename = in.readUTF();
		numberFiles = in.readInt();
		classOfElements = (Class<? extends E>) in.readObject();
		out = null;
		wroteOnDisk = true;
	}

	public void writeLuposObject(final LuposObjectOutputStream out)
			throws IOException {
		if (this.out != null) {
			this.out.close();
			this.out = null;
		}
		out.writeLuposLong(size);
		out.writeLuposString(filename);
		out.writeLuposInt(numberFiles);
		Registration.serializeClass(classOfElements, out);
		wroteOnDisk = true;
	}

	public void readLuposObject(final LuposObjectInputStream in)
			throws IOException, ClassNotFoundException {
		size = in.readLuposLong();
		filename = in.readLuposString();
		numberFiles = in.readLuposInt();
		classOfElements = (Class<? extends E>) Registration.deserializeId(in)[0];
		out = null;
		wroteOnDisk = true;
	}

	public static DiskCollection readAndCreateLuposObject(
			final LuposObjectInputStream in) throws IOException,
			ClassNotFoundException {
		final long size = in.readLuposLong();
		final String filename = in.readLuposString();
		final int numberFiles = in.readLuposInt();
		final Class<?> classOfElements = Registration.deserializeId(in)[0];
		DiskCollection dc;
		if (classOfElements == Triple.class) {
			dc = new DiskCollection<Triple>((Class<Triple>) classOfElements,
					size, filename, numberFiles);
		} else
			dc = new DiskCollection(classOfElements, size, filename,
					numberFiles);
		dc.wroteOnDisk = true;
		return dc;
	}

	@Override
	public String toString() {
		String s = "";
		for (final E e : this) {
			if (s.compareTo("") != 0)
				s += ", ";
			s += e.toString();
		}
		return "[ " + s + " ]";
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (out != null) {
				out.close();
				out = null;
			}
			if (!wroteOnDisk) {
				final File file = new File(filename);
				if (file.exists())
					file.delete();
			}
		} finally {
			super.finalize();
		}
	}
}
