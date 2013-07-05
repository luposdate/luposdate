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
package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.AbstractCollection;
import java.util.Iterator;

import lupos.datastructures.buffermanager.PageInputStream;
import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.buffermanager.PageOutputStream;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.io.ExistingByteArrayOutputStream;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;

/**
 * This collection especially uses the buffer manager to store a collection.
 * Smaller collections will probably remain in main memory
 * (with its elements stored memory efficient in pages of byte arrays)
 * and larger collections - not fitting in main memory - will
 * partly be stored on disk.
 *
 * This collection is faster than a normal disk collection
 * (storing all of its elements on disk), because parts of the collection
 * remain in main memory.
 *
 * However, this collection is slower than main memory collections, as
 * its elements are serialized into and deserialized from a byte array,
 * which does not need to be done for a main memory collection.
 * The storage in byte arrays is on the other hand maybe more memory efficient
 * than a large number of objects.
 *
 * @param <E> the type of the elements in the collection
 */
public class PagedCollection<E> extends AbstractCollection<E> {

	protected int size;
	protected final String filename;
	protected final PageManager pageManager;
	protected int lastPage;
	protected final Class<? extends E> classname;
	protected PageOutputStream out = null;

	protected final static byte NOTREMOVED = 0;

	/**
	 * Constructor to create new (empty) PagedCollection...
	 * @throws IOException
	 */
	public PagedCollection(final Class<? extends E> classname) throws IOException {
		DiskCollection.makeFolders();
		this.filename = DiskCollection.newBaseFilename();
		this.size=0;
		this.pageManager = new PageManager(this.filename);
		this.lastPage = 1;
		this.classname = classname;
		this.initFirstPage();
	}

	/**
	 * Constructor to open an existing PagedCollection...
	 *
	 * @param filename the filename under which this collection is stored!
	 * @throws IOException
	 */
	public PagedCollection(final String filename, final Class<? extends E> classname) throws IOException {
		DiskCollection.makeFolders();
		this.filename = filename;
		this.pageManager = new PageManager(this.filename, false);
		final byte[] page = this.pageManager.getPage(1);
		final InputStream in = new ByteArrayInputStream(page, PageInputStream.DEFAULTSTARTINDEX, 8);
		this.size = InputHelper.readLuposInteger(in);
		this.lastPage = InputHelper.readLuposInteger(in);
		in.close();
		this.classname = classname;
	}

	public void release() throws IOException {
		if(this.out!=null){
			this.out.close();
			this.out = null;
		}
		this.pageManager.release();
		this.size = 0;
		this.lastPage = 1;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Iterator<E> iterator() {
		try {
			this.closeOutputStream();
			return new Iterator<E>(){

				final PageInputStream in = new PageInputStream(1, PagedCollection.this.pageManager, PageInputStream.DEFAULTSTARTINDEX + 8);
				E next = null;
				int currentPageNumber;
				int currentIndexInPage;

				@Override
				public boolean hasNext() {
					this.next = this.next();
					if(this.next!=null){
						return true;
					} else {
						return false;
					}
				}

				@Override
				public E next() {
					if(this.next!=null){
						final E iNext = this.next;
						this.next = null;
						return iNext;
					} else {
						try {
							this.currentPageNumber = this.in.getCurrentPageNumber();
							this.currentIndexInPage = this.in.getIndex();

							byte flag;
							while((flag = InputHelper.readLuposByte(this.in))!=PagedCollection.NOTREMOVED){
								for(int i=0; i<flag; i++){
									InputHelper.readLuposByte(this.in); // just read over removed entry!
								}
							}
							return Registration.deserializeWithoutId(PagedCollection.this.classname, this.in);
						} catch (final EOFException e) {
							return null;
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
					return null;
				}

				@Override
				public void remove() {
					// mark bindings in current page at current index as deleted!
					// just the number of bytes to be skipped are written in one byte!
					// (it is assumed that regions to be skipped are not large,
					//  additionally, each bindings must be markable to be removed,
					//  the one with fewest bytes is only one byte long
					//  => with our scheme we are able to mark also an empty bindings {} as removed!)
					final int endPage = this.in.getCurrentPageNumber();
					final int endIndex = this.in.getIndex();

					try {
						byte[] page = PagedCollection.this.pageManager.getPage(this.currentPageNumber);

						while(this.currentPageNumber!=endPage){
							while(PagedCollection.this.pageManager.getPageSize()-this.currentIndexInPage>127){
								page[this.currentIndexInPage] = (byte) 127;
								this.currentIndexInPage+=127;
							}
							page[this.currentIndexInPage] = (byte) (PagedCollection.this.pageManager.getPageSize()-this.currentIndexInPage);
							PagedCollection.this.pageManager.modifyPage(this.currentPageNumber, page);

							final int nextPage = (((0xFF & page[0]) << 8 | (0xFF & page[1])) << 8 | (0xFF & page[2])) << 8 | (0xFF & page[3]);
							page = PagedCollection.this.pageManager.getPage(nextPage);
							this.currentIndexInPage = PageInputStream.DEFAULTSTARTINDEX;
							this.currentPageNumber = nextPage;
						}
						while(endIndex-this.currentIndexInPage>127){
							page[this.currentIndexInPage] = (byte) 127;
							this.currentIndexInPage+=127;
						}
						page[this.currentIndexInPage] = (byte) (endIndex-this.currentIndexInPage);

						PagedCollection.this.pageManager.modifyPage(this.currentPageNumber, page);

						this.currentIndexInPage = endIndex;
						this.currentPageNumber = endPage;

						PagedCollection.this.size--;
						PagedCollection.this.storeSizeAndLastPage();
					} catch (final IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}

				}
			};
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean add(final E e) {
		try {
			this.openOutputStream();
			OutHelper.writeLuposByte(PagedCollection.NOTREMOVED, this.out);
			Registration.serializeWithoutId(e, this.out);
			this.size++;
		} catch (final IOException e1) {
			System.err.println(e1);
			e1.printStackTrace();
		}

		return true;
	}

	protected final void openOutputStream() throws IOException {
		if(this.out==null){
			this.out = new PageOutputStream(this.lastPage, this.pageManager, false, true);
		}
	}

	protected final void closeOutputStream() throws IOException {
		if(this.out!=null){
			this.out.close();
			this.lastPage = this.out.getCurrentPageNumber();
			this.out = null;
			this.storeSizeAndLastPage();
		}
	}

	protected final void storeSizeAndLastPage() throws IOException {
		final byte[] page = this.pageManager.getPage(1);
		final ExistingByteArrayOutputStream out1 = new ExistingByteArrayOutputStream(page, PageInputStream.DEFAULTSTARTINDEX);
		OutHelper.writeLuposInt(this.size, out1);
		OutHelper.writeLuposInt(this.lastPage, out1);
		out1.close();
		this.pageManager.modifyPage(1, page);
	}

	protected final void initFirstPage() throws IOException {
		final int newPageNumber = this.pageManager.getNumberOfNewPage();
		if(newPageNumber!=1){
			System.err.println("lupos.datastructures.smallerinmemorylargerondisk.PagedCollection: Something went wrong, new page from Page Manager should be 1, but is " + newPageNumber);
		}
		final byte[] page = this.pageManager.getEmptyPage();
		page[4] = (byte) 0;
		page[5] = (byte) (PageInputStream.DEFAULTSTARTINDEX + 8);
		this.pageManager.modifyPage(1, page);
	}

	public void writeLuposObject(final OutputStream out) throws IOException {
		this.closeOutputStream();
		OutHelper.writeLuposString(this.filename, out);
		Registration.serializeClass(this.classname, out);
	}

	@SuppressWarnings("unchecked")
	public static<T> PagedCollection<T> readAndCreateLuposObject(final InputStream in) throws IOException{
		return new PagedCollection<T>(InputHelper.readLuposString(in), (Class<? extends T>) Registration.deserializeId(in)[0]);
	}

	public int lengthLuposObject() {
		return LengthHelper.lengthLuposString(this.filename) + Registration.lengthSerializeId();
	}
}
