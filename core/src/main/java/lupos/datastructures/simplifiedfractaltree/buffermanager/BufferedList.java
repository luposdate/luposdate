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
package lupos.datastructures.simplifiedfractaltree.buffermanager;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.buffermanager.BufferManager.PageAddress;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class is a implementation of the <tt>List</tt> interface that stores the containing elements on the hard disk by using <tt>BufferManager</tt>
 * class. Implements all optional list operations, and permits all elements, including <tt>null</tt>.
 *
 * <p>
 * To avoid memory fragmentation it uses a memory manager, that reuses abandoned regions in memory. It also offers a defragmentation method.
 *
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If multiple threads access an <tt>BufferedList</tt> instance concurrently, and
 * at least one of the threads modifies the list structurally, it <i>must</i> be synchronized externally. (A structural modification is any operation
 * that adds or deletes one or more elements, or explicitly resizes the backing array; merely setting the value of an element is not a structural
 * modification.) This is typically accomplished by synchronizing on some object that naturally encapsulates the list.
 *
 * @author Denis Fäcke
 * @see List
 * @see Collection
 * @see Cloneable
 * @see BufferManager
 * @see RandomAccess
 * @see MemoryManager
 * @version $Id: $Id
 */
public class BufferedList<E> implements Collection<E>, List<E>, RandomAccess, Cloneable, Serializable {
	/**
	 * Serial Version ID.
	 */
	private static final long serialVersionUID = 6855248227285932133L;
	/**
	 * The <tt>BufferManager</tt> that is managing the pages.
	 */
	protected transient BufferManager bufferManager;
	/**
	 * The size of the <tt>BufferedList</tt>.
	 */
	protected int size = 0;
	/**
	 * The size of one page in bytes for this buffer managers.
	 */
	protected int pageSize = 0;
	/**
	 * The default size of one page (8 KB).
	 */
	public static int DEFAULT_PAGESIZE = 8 * 1024;
	protected File file;
	@SuppressWarnings("rawtypes")
	protected MemoryManager_NextFit memoryManager_NextFit;
	protected int sizeDiv12 = 0;
	protected String path1;
	protected String path2;
	/**
	 * An instance of <tt>E</tt>.
	 */
	protected Object instance = null;

	/**
	 * Constructs an empty list with default page size.
	 *
	 * @param instance A instance of <tt>E</tt>
	 */
	@SuppressWarnings("rawtypes")
	public BufferedList(final Object instance) {
		super();
		this.instance = instance;
		this.pageSize = DEFAULT_PAGESIZE;
		this.sizeDiv12 = (int) Math.floor(this.pageSize / 12);
		String path = (this.getClass().getResource(".")).toString() + "default_name";
		path = path.replaceFirst("file:/", "");
		this.file = new File(path + "/tmp/fractaltree/bufferedlist");
		if (!this.file.getParentFile().exists()) {
			this.file.getParentFile().mkdirs();
		}
		this.path1 = this.file.getPath() + "1";
		this.path2 = this.file.getPath() + "2";
		this.memoryManager_NextFit = new MemoryManager_NextFit(new File(path + "/tmp/fractaltree/fsmanager"));

		this.bufferManager = BufferManager.getBufferManager();

		if (this.size() == 0) {
			try {
				final byte[] page = this.bufferManager.getPage(this.pageSize, new BufferManager.PageAddress(0, this.path1));
				final ByteBuffer byteBuffer = ByteBuffer.wrap(page);
				byteBuffer.putInt(0, byteBuffer.getInt(0)); // size
				byteBuffer.putInt(4, 0); // rightpage
				byteBuffer.putInt(8, 11); // rightbound
				this.bufferManager.modifyPage(this.pageSize, new BufferManager.PageAddress(0, this.path1), byteBuffer.array());
				this.bufferManager.writeAllModifiedPages();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Constructs an empty list with the specified page size and name.
	 *
	 * @param pageSize The page size (should be 2^x)
	 * @param instance A instance of <tt>E</tt>
	 * @param file a {@link java.io.File} object.
	 */
	@SuppressWarnings("rawtypes")
	public BufferedList(final int pageSize, File file, final Object instance) {
		this.instance = instance;
		this.pageSize = pageSize;
		file = file.getAbsoluteFile();
		this.file = new File(file.getParentFile() + "/tmp/fractaltree/" + file.getName());
		this.sizeDiv12 = (int) Math.floor(pageSize / 12);
		this.memoryManager_NextFit = new MemoryManager_NextFit(new File(file.getAbsolutePath() + "/tmp/fractaltree/fsmanager"));
		if (!this.file.getParentFile().exists()) {
			this.file.getParentFile().mkdirs();
		}
		this.path1 = file.getParentFile() + "/tmp/fractaltree/" + file.getName() + "1";
		this.path2 = file.getParentFile() + "/tmp/fractaltree/" + file.getName() + "2";

		this.bufferManager = BufferManager.getBufferManager();

		if (this.size() == 0) {
			try {
				final byte[] page = this.bufferManager.getPage(this.pageSize, new BufferManager.PageAddress(0, this.path1));
				final ByteBuffer byteBuffer = ByteBuffer.wrap(page);
				byteBuffer.putInt(0, byteBuffer.getInt(0)); // size
				byteBuffer.putInt(4, 0); // rightpage
				byteBuffer.putInt(8, 11); // rightbound
				this.bufferManager.modifyPage(this.pageSize, new BufferManager.PageAddress(0, this.path1), byteBuffer.array());
				this.bufferManager.writeAllModifiedPages();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.bufferManager.releaseAllPages();
		this.memoryManager_NextFit.clear();
		this.size = 0;

		try {
			final byte[] page = this.bufferManager.getPage(this.pageSize, new BufferManager.PageAddress(0, this.path1));
			final ByteBuffer byteBuffer = ByteBuffer.wrap(page);
			byteBuffer.putInt(0, byteBuffer.getInt(0)); // size
			byteBuffer.putInt(4, 0); // rightpage
			byteBuffer.putInt(8, 11); // rightbound
			this.bufferManager.modifyPage(this.pageSize, new BufferManager.PageAddress(0, this.path1), byteBuffer.array());
			this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the specified <tt>index</tt> is valid.
	 *
	 * @param index A index
	 * @throws java.lang.IndexOutOfBoundsException
	 */
	protected void rangeCheck(final int index) {
		if (index >= this.size) {
			throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index));
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.size;
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(final E e) {
		this.add(this.size, e);

		return true;
	}

	/**
	 * Serializes an element and returns the serialized element as a byte array.
	 *
	 * @param element An element
	 * @return The serialized element
	 */
	protected byte[] serialize(final E element) {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		byte[] array = null;

		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(element);
			out.flush();
			out.close();
			bos.close();
			array = bos.toByteArray();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return array;
	}

	/**
	 * Deserializes a byte array and the returns the deserialized element.
	 *
	 * @param array A byte array
	 * @param instance An instance of <tt>E</tt>
	 * @return The deserialized element
	 */
	@SuppressWarnings("unchecked")
	protected E deserialize(final byte[] array, final Object instance) {
		final ByteArrayInputStream bis = new ByteArrayInputStream(array);
		ObjectInputStream in = null;
		Object o = null;

		try {
			in = new ObjectInputStream(bis);
			o = in.readObject();
			bis.close();
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (E) o;
	}

	private void writeObject(final java.io.ObjectOutputStream s) throws java.io.IOException {
		// Write out element count, and any hidden stuff
		s.defaultWriteObject();

		// Write out size as capacity for behavioural compatibility with clone()
		s.writeInt(this.size());

		// Write out all elements in the proper order.
		for (int i = 0; i < this.size(); i++) {
			s.writeObject(this.get(i));
		}
	}

	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {

		// Read in size, and any hidden stuff
		s.defaultReadObject();

		// Read in the size
		final int size = s.readInt();

		for (int i = 0; i < size; i++) {
			this.add((E) s.readObject());
		}
	}

	/**
	 * <p>getPointerPage.</p>
	 *
	 * @param index a int.
	 * @return a int.
	 */
	public int getPointerPage(final int index) {
		return (int) Math.floor(index / this.sizeDiv12);
	}

	/**
	 * <p>getPointerBound.</p>
	 *
	 * @param index a int.
	 * @return a int.
	 */
	protected int getPointerBound(final int index) {
		return (index % this.sizeDiv12) * 12;
	}

	/**
	 * <p>setEnd.</p>
	 *
	 * @param end a {@link java.awt.Point} object.
	 */
	protected void setEnd(final Point end) {
		try {
			final byte[] page = this.bufferManager.getPage(this.pageSize, new BufferManager.PageAddress(0, this.path1));
			final ByteBuffer byteBuffer = ByteBuffer.wrap(page);
			byteBuffer.putInt(4, end.x);
			byteBuffer.putInt(8, end.y);
			this.bufferManager.modifyPage(this.pageSize, new BufferManager.PageAddress(0, this.path1), byteBuffer.array());
			this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>getEnd.</p>
	 *
	 * @return a {@link java.awt.Point} object.
	 */
	protected Point getEnd() {
		ByteBuffer byteBuffer = null;
		try {
			final byte[] page = this.bufferManager.getPage(this.pageSize, new BufferManager.PageAddress(0, this.path1));
			byteBuffer = ByteBuffer.wrap(page);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new Point(byteBuffer.getInt(4), byteBuffer.getInt(8));
	}

	/** {@inheritDoc} */
	@Override
	public void add(final int index, final E e) {
		final Point end = this.getEnd();
		int leftBound = end.y + 1;
		int leftPage = end.x;
		if (end.y == this.pageSize) {
			leftBound = 0;
			leftPage++;
		}
		final byte[] serializedObject = this.serialize(e);
		final int rightPage = this.calcRightPage(leftPage, leftBound, serializedObject.length);
		final int rightBound = this.calcRightBound(leftBound, serializedObject.length);

		this.writeData(serializedObject, leftPage, rightPage, leftBound, rightBound);

		this.setEnd(new Point(rightPage, rightBound));

		for (int i = this.size - 1; i >= index;) {
			if (i % this.pageSize == 0) {
				final Pointer pointer2 = this.getPointers(i);
				this.setPointers(i + 1, pointer2.leftPage, pointer2.leftBound, pointer2.size);
				i--;
			} else {
				byte[] page = null;
				try {
					page = this.bufferManager.getPage(this.pageSize, new BufferManager.PageAddress(this.getPointerPage(i), this.path2));
				} catch (final IOException e2) {
					e2.printStackTrace();
				}
				if (this.getPointerBound(index) != this.getPointerBound(i)) {
					try {
						System.arraycopy(page, 0, page, 1, this.pageSize - 1);
						this.bufferManager.modifyPage(this.pageSize, new PageAddress(this.getPointerPage(i), this.path2), page);
					} catch (final IOException e1) {
						e1.printStackTrace();
					}
					i -= this.pageSize - 1;
				} else {
					try {
						System.arraycopy(page, index, page, index + 1, this.pageSize - index);
						this.bufferManager.modifyPage(this.pageSize, new PageAddress(this.getPointerPage(i), this.path2), page);
					} catch (final IOException e1) {
						e1.printStackTrace();
					}
					i -= this.pageSize - index;
				}
			}
		}
		try {
			this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		this.setPointers(index, leftPage, leftBound, serializedObject.length);

		this.incSize(1);
	}

	/**
	 * Writes a serialized object to the the specified location.
	 *
	 * @param serializedObject A serialized object
	 * @param leftPage The left page
	 * @param rightPage The right page
	 * @param leftBound The left bound
	 * @param rightBound The right bound
	 */
	protected void writeData(final byte[] serializedObject, final int leftPage, final int rightPage, final int leftBound, final int rightBound) {
		byte[] page = null;
		int offset = 0;
		try {
			for (int i = leftPage; i <= rightPage; i++) {
				page = this.bufferManager.getPage(this.pageSize, new PageAddress(i, this.path1));
				if (leftPage == rightPage) {
					System.arraycopy(serializedObject, 0, page, leftBound, serializedObject.length);
					this.bufferManager.modifyPage(this.pageSize, new PageAddress(i, this.path1), page);
					offset += serializedObject.length;
				} else if (i == leftPage) {
					System.arraycopy(serializedObject, 0, page, leftBound, this.pageSize - leftBound);
					this.bufferManager.modifyPage(this.pageSize, new PageAddress(i, this.path1), page);
					offset += this.pageSize - leftBound;
				} else if (i != leftPage && i != rightPage) {
					System.arraycopy(serializedObject, offset, page, 0, this.pageSize);
					this.bufferManager.modifyPage(this.pageSize, new PageAddress(i, this.path1), page);
					offset += this.pageSize;
				} else if (i == rightPage) {
					System.arraycopy(serializedObject, offset, page, 0, rightBound);
					this.bufferManager.modifyPage(this.pageSize, new PageAddress(i, this.path1), page);
					offset += rightBound;
				}
			}
			this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Increments the size stored in page 0 by <tt>i</tt>.
	 *
	 * @param i The amount to increase the size
	 */
	protected void incSize(final int i) {
		this.size += i;
		try {
			final ByteBuffer byteBuffer = ByteBuffer.wrap(this.bufferManager.getPage(this.pageSize, new PageAddress(0, this.path1)));
			byteBuffer.putInt(0, this.size() + i);
			this.bufferManager.modifyPage(this.pageSize, new PageAddress(0, this.path1), byteBuffer.array());
			this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Decrements the size stored in page 0 by <tt>i</tt>.
	 *
	 * @param i The amount to decrease the size
	 */
	protected void decSize(final int i) {
		this.size -= i;
		try {
			final ByteBuffer byteBuffer = ByteBuffer.wrap(this.bufferManager.getPage(this.pageSize, new PageAddress(0, this.path1)));
			byteBuffer.putInt(0, this.size() - i);
			this.bufferManager.modifyPage(this.pageSize, new PageAddress(0, this.path1), byteBuffer.array());
			this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		return this.addAll(this.size(), c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(int index, final Collection<? extends E> c) {
		this.rangeCheckForAdd(index);

		boolean modified = false;
		for (final E e : c) {
			this.add(index++, e);
			modified = true;
		}
		return modified;
	}

	/** {@inheritDoc} */
	@Override
	public int indexOf(final Object o) {
		if (o == null) {
			for (int i = 0; i < this.size; i++) {
				if (this.get(i) == null) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < this.size; i++) {
				if (o.equals(this.get(i))) {
					return i;
				}
			}
		}
		return -1;
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final Object o) {
		return this.indexOf(o) >= 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(final Collection<?> c) {
		@SuppressWarnings("unchecked")
		final
		Iterator<? extends E> iterator = (Iterator<? extends E>) c.iterator();
		boolean modified = true;
		for (int i = 0; i < c.size(); i++) {
			if (!this.contains(iterator.next())) {
				modified = false;
			}
		}
		return modified;
	}

	/** {@inheritDoc} */
	@Override
	public E get(final int index) {
		this.rangeCheck(index);

		return this.getElement(this.getPointers(index));
	}

	/**
	 * Returns the element specified by the <tt>pointer</tt>.
	 *
	 * @param pointer A <tt>pointer</tt>
	 * @return A element
	 */
	protected E getElement(final Pointer pointer) {
		final int leftPage = pointer.leftPage;
		final int rightPage = this.calcRightPage(pointer.leftPage, pointer.leftBound, pointer.size);
		final int leftBound = pointer.leftBound;
		final int rightBound = this.calcRightBound(pointer.leftBound, pointer.size);

		final int size = pointer.size;
		final byte[] element = new byte[size];

		int offset = 0;
		for (int i = leftPage; i <= rightPage; i++) {
			try {
				final byte[] page = this.bufferManager.getPage(this.pageSize, new PageAddress(i, this.path1));
				if (leftPage == rightPage) {
					System.arraycopy(page, leftBound, element, offset, size);
					break;
				} else if (i == leftPage) {
					if (this.pageSize < leftBound) {
						System.out.println((this.pageSize < leftBound));
					}
					System.arraycopy(page, leftBound, element, offset, this.pageSize - leftBound);
					offset += this.pageSize - leftBound;
				} else if (i != leftPage && i != rightPage) {
					System.arraycopy(page, 0, element, offset, this.pageSize);
					offset += this.pageSize;
				} else if (i == rightPage) {
					System.arraycopy(page, 0, element, offset, rightBound);
				}

				this.bufferManager.writeAllModifiedPages();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return this.deserialize(element, this.instance);
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<E> iterator() {
		return this.listIterator();
	}

	/**
	 * Checks if the <tt>index</tt> is valid for the add methd.
	 *
	 * @param index A index
	 */
	protected void rangeCheckForAdd(final int index) {
		if (index < 0 || index > this.size) {
			throw new IndexOutOfBoundsException(this.outOfBoundsMsg(index));
		}
	}

	/**
	 * Returns a message for a <tt>IndexOutOfBoundException</tt>.
	 *
	 * @param index A index
	 * @return A String
	 */
	protected String outOfBoundsMsg(final int index) {
		return "Index: " + index + ", Size: " + this.size();
	}

	/** {@inheritDoc} */
	@Override
	public int lastIndexOf(final Object o) {
		if (o == null) {
			for (int i = this.size() - 1; i >= 0; i--) {
				if (this.get(i) == null) {
					return i;
				}
			}
		} else {
			for (int i = this.size() - 1; i >= 0; i--) {
				if (o.equals(this.get(i))) {
					return i;
				}
			}
		}
		return -1;
	}

	/** {@inheritDoc} */
	@Override
	public ListIterator<E> listIterator(final int index) {
		if (index < 0 || index > this.size()) {
			throw new IndexOutOfBoundsException("Index: " + index);
		}
		return new ListItr(index);
	}

	/** {@inheritDoc} */
	@Override
	public ListIterator<E> listIterator() {
		return new ListItr(0);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final Object o) {
		final int size = this.size();
		if (o == null) {
			for (int index = 0; index < size; index++) {
				if (this.get(index) == null) {
					this.remove(index);
					return true;
				}
			}
		} else {
			for (int index = 0; index < size; index++) {
				if (o.equals(this.get(index))) {
					this.remove(index);
					return true;
				}
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public E remove(final int index) {
		final E oldValue = this.get(index);

		final Pointer pointer = this.getPointers(index);
		final int pageStart = pointer.leftPage;
		final int leftBound = pointer.leftBound;
		final int bm2LastElement = this.getPointerPage(this.size() - 1);
		final int oldSize = pointer.size;
		this.memoryManager_NextFit.release(pageStart, leftBound, oldSize);

		final int pageEndNew = 0;

		for (int i = index; i < this.size; i++) {
			final Pointer pointers = this.getPointers(i + 1);
			this.setPointers(i, pointers.leftPage, pointers.leftBound, pointers.size);
		}

		// Remove unused pages
		if (index == this.size - 1) {
			for (int l = pageEndNew + 1; l < this.size; l++) {
				this.bufferManager.releasePage(new PageAddress(l, this.path1));
			}
			if (this.getPointerPage(this.size - 1) < bm2LastElement) {
				this.bufferManager.releasePage(new PageAddress(bm2LastElement, this.path2));
			}
		}

		this.decSize(1);

		return oldValue;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(final Collection<?> c) {
		final Iterator<?> itr = c.iterator();
		boolean modified = false;
		for (int i = 0; i < c.size(); i++) {
			this.remove(itr.next());
			modified = true;
		}
		return modified;
	}

	/** {@inheritDoc} */
	@Override
	public boolean retainAll(final Collection<?> c) {
		boolean modified = false;
		for (int i = 0; i < c.size(); i++) {
			if (!c.contains(this.get(i))) {
				this.remove(i);
			}
			modified = true;
		}
		return modified;
	}

	/**
	 * <p>setNoReturn.</p>
	 *
	 * @param index a int.
	 * @param element a E object.
	 */
	public void setNoReturn(final int index, final E element) {
		this.rangeCheck(index);

		final Pointer pointers = this.getPointers(index);
		this.memoryManager_NextFit.release(pointers.leftPage, pointers.leftBound, pointers.size);

		final byte[] serializedObject = this.serialize(element);
		final Point end = this.getEnd();
		int leftPage = end.x;
		int leftBound = end.y + 1;
		if (end.y == this.pageSize) {
			leftBound = 0;
			leftPage++;
		}
		final int rightPage = this.calcRightPage(leftPage, leftBound, serializedObject.length);
		final int rightBound = this.calcRightBound(leftBound, serializedObject.length);

		this.writeData(serializedObject, leftPage, rightPage, leftBound, rightBound);
		this.setPointers(index, leftPage, leftBound, serializedObject.length);

		this.setEnd(new Point(rightPage, rightBound));
	}

	/** {@inheritDoc} */
	@Override
	public E set(final int index, final E element) {
		this.rangeCheck(index);

		final E oldValue = this.get(index);

		final Pointer pointers = this.getPointers(index);
		this.memoryManager_NextFit.release(pointers.leftPage, pointers.leftBound, pointers.size);

		final byte[] serializedObject = this.serialize(element);
		final Point end = this.getEnd();
		int leftPage = end.x;
		int leftBound = end.y + 1;
		if (end.y == this.pageSize) {
			leftBound = 0;
			leftPage++;
		}
		final int rightPage = this.calcRightPage(leftPage, leftBound, serializedObject.length);
		final int rightBound = this.calcRightBound(leftBound, serializedObject.length);

		this.writeData(serializedObject, leftPage, rightPage, leftBound, rightBound);
		this.setPointers(index, leftPage, leftBound, serializedObject.length);

		this.setEnd(new Point(rightPage, rightBound));

		return oldValue;
	}

	/**
	 * <p>getPointers.</p>
	 *
	 * @param index a int.
	 * @return a {@link lupos.datastructures.simplifiedfractaltree.buffermanager.Pointer} object.
	 */
	protected Pointer getPointers(final int index) {
		ByteBuffer byteBuffer = null;
		try {
			byteBuffer = ByteBuffer.wrap(this.bufferManager.getPage(this.pageSize, new PageAddress(this.getPointerPage(index), this.path2)));
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final int pointerBound = this.getPointerBound(index);
		return new Pointer(byteBuffer.getInt(pointerBound), byteBuffer.getInt(pointerBound + 4), byteBuffer.getInt(pointerBound + 8));
	}

	/**
	 * <p>setPointers.</p>
	 *
	 * @param index a int.
	 * @param leftPage a int.
	 * @param leftBound a int.
	 * @param size a int.
	 */
	protected void setPointers(final int index, final int leftPage, final int leftBound, final int size) {
		try {
			final ByteBuffer byteBuffer = ByteBuffer.wrap(this.bufferManager.getPage(this.pageSize, new PageAddress(this.getPointerPage(index), this.path2)));

			byteBuffer.putInt(this.getPointerBound(index), leftPage);
			byteBuffer.putInt(this.getPointerBound(index) + 4, leftBound);
			byteBuffer.putInt(this.getPointerBound(index) + 8, size);
			this.bufferManager.modifyPage(this.pageSize, new PageAddress(this.getPointerPage(index), this.path2), byteBuffer.array());
			this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * <p>calcRightBound.</p>
	 *
	 * @param leftBound a int.
	 * @param size a int.
	 * @return a int.
	 */
	public int calcRightBound(final int leftBound, final int size) {
		final int ans = (leftBound + size) % this.pageSize;
		if (ans == 0) {
			return this.pageSize;
		} else {
			return ans;
		}
	}

	/**
	 * <p>calcRightPage.</p>
	 *
	 * @param leftPage a int.
	 * @param leftBound a int.
	 * @param size a int.
	 * @return a int.
	 */
	public int calcRightPage(final int leftPage, final int leftBound, final int size) {
		double ans = 0;
		if (leftBound + size <= this.pageSize) {
			ans = leftPage;
		} else {
			ans = leftPage + Math.ceil((double) ((leftBound + size) % this.pageSize) / this.pageSize);
		}

		return (int) (ans);
	}

	/** {@inheritDoc} */
	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		this.subListRangeCheck(fromIndex, toIndex, this.size());

		final List<E> list = new ArrayList<E>();
		for (int i = fromIndex; i < toIndex; i++) {
			list.add(this.get(i));
		}
		return list;
	}

	/** {@inheritDoc} */
	@Override
	public E[] toArray() {
		@SuppressWarnings("unchecked")
		final
		E[] array = (E[]) new Object[this.size()];
		final int size = this.size();
		for (int i = 0; i < size; i++) {
			array[i] = this.get(i);
		}
		return array;
	}

	/** {@inheritDoc} */
	@Override
	public <T> T[] toArray(final T[] a) {
		throw new NotImplementedException();
	}

	/** {@inheritDoc} */
	@Override
	public Object clone() {
		try {
			@SuppressWarnings("unchecked")
			final
			BufferedList<E> v = (BufferedList<E>) super.clone();
			return v;
		} catch (final CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}

	private void subListRangeCheck(final int fromIndex, final int toIndex, final int size) {
		if (fromIndex < 0) {
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		}
		if (toIndex > size) {
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		}
		if (fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		}
	}

	/**
	 * An adapted version of ArrayList.Itr
	 */
	protected class Itr implements Iterator<E> {
		int cursor; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such

		@Override
		public boolean hasNext() {
			return this.cursor != BufferedList.this.size();
		}

		@Override
		public E next() {
			final int i = this.cursor;
			if (i >= BufferedList.this.size()) {
				throw new NoSuchElementException();
			}
			this.cursor = i + 1;
			return BufferedList.this.get(this.lastRet = i);
		}

		@Override
		public void remove() {
			if (this.lastRet < 0) {
				throw new IllegalStateException();
			}

			try {
				BufferedList.this.remove(this.lastRet);
				this.cursor = this.lastRet;
				this.lastRet = -1;
			} catch (final IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}
	}

	protected class ListItr extends Itr implements ListIterator<E> {
		ListItr(final int index) {
			super();
			this.cursor = index;
		}

		@Override
		public boolean hasPrevious() {
			return this.cursor != 0;
		}

		@Override
		public int nextIndex() {
			return this.cursor;
		}

		@Override
		public int previousIndex() {
			return this.cursor - 1;
		}

		@Override
		public E previous() {
			final int i = this.cursor - 1;
			if (i < 0) {
				throw new NoSuchElementException();
			}
			this.cursor = i;
			return BufferedList.this.get(this.lastRet = i);
		}

		@Override
		public void set(final E e) {
			if (this.lastRet < 0) {
				throw new IllegalStateException();
			}

			try {
				BufferedList.this.set(this.lastRet, e);
			} catch (final IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add(final E e) {

			try {
				final int i = this.cursor;
				BufferedList.this.add(i, e);
				this.cursor = i + 1;
				this.lastRet = -1;
			} catch (final IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}
	}
}
