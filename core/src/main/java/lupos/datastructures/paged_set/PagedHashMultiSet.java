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
package lupos.datastructures.paged_set;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.buffermanager.BufferManager.PageAddress;
import lupos.datastructures.buffermanager.ContinousPagesInputStream;
import lupos.datastructures.buffermanager.ContinousPagesOutputStream;
import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.io.ExistingByteArrayOutputStream;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.FileHelper;
import lupos.misc.Triple;

/**
 * This class implements a hash multi set on disk.
 * Multi Set means that occurrences of elements are counted in the set.
 * When removing elements, the element is only completed removed whenever the element is removed as often as has been added before.
 *
 * It uses two files: In the first file pointers to the start position of values are stored.
 * In the second file the actual values are stored.
 *
 * Values can be only added to the values file in the end.
 *
 * The first four bytes in the pointers file stores the maximum index for the values,
 * and the first 8 bytes in the values file stores the maximum position in the values file.
 * Each value is part of linked list, after the serialization of a value an 8-bytes pointer
 * is used to point to the next value in the list (is 0 for marking the end of the list).
 */
public class PagedHashMultiSet<V> extends AbstractSet<V> {

	// the current size
	private long size = 0;
	// the next free byte in the values file
	private long lastValue = 1; // the first byte is wasted, as 0 marks no reference!

	// the filename of the pointers file
	private final String pointersFilename;
	// the filename of the values file
	private final String valuesFilename;

	// the class of values
	private final Class<V> classOfValues;

	// the current id, important, if several paged hash sets are instantiated
	// (each instance gets another id and therefore stores its content into different files)
	private static int fileID=0;

	// the lock for getting a new id
	protected static ReentrantLock lock = new ReentrantLock();

	// the inital table size
	private static int INITIALTABLESIZE = 1024;

	// the current table size
	// TODO increase table size dynamically
	private final int TABLESIZE = INITIALTABLESIZE;

	private final int TABLEPAGESIZE = INITIALTABLESIZE * 8;

	public PagedHashMultiSet(final Class<V> classOfValues) {
		this.classOfValues = classOfValues;
		PagedHashMultiSet.lock.lock();
		try{
			// use directory of DiskCollection for storing hash sets on disk!
			DiskCollection.makeFolders();
			final int currentID = fileID++;
			// remove old pointers and values files from disk!
			final String[] dirs = DiskCollection.getTmpDir();
			for(final String dir: dirs){
				FileHelper.deleteFilesStartingWithPattern(dir, currentID + ".table_");
				FileHelper.deleteFilesStartingWithPattern(dir, currentID + ".values_");
			}
			final String dir = dirs[currentID % dirs.length];
			this.pointersFilename = dir + currentID + ".table";
			this.valuesFilename = dir + currentID + ".values";
		} finally {
			PagedHashMultiSet.lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	@Override
	public boolean add(final V element) {

		try {
			this.size++;

			final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final int hashAddress = element.hashCode() % this.TABLESIZE;

			long pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, hashAddress * 8, 8));

			if(pointer == 0) {
				// no entry so far at this position in the hash table...
				final long address = this.storeNewElement(element);
				// write address in hash table
				final OutputStream out = new ExistingByteArrayOutputStream(page, hashAddress * 8);
				OutHelper.writeLuposLong(address, out);
				out.close();
				BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
				return true;
			} else {
				// check existing list of entries for this table position
				Triple<V, Long, Long> entry;
				do {
					entry = this.getElement(pointer);
					if(entry.getFirst().equals(element)) {
						// increase number of elements by one...
						this.storeNumberOfElements(entry.getSecond() + 1, pointer);
						return false;
					}
					if(entry.getThird() == 0) {
						// add element to end of this list!
						final long addressOfNextEntry = this.storeNewElement(element);
						this.storeAddressOfNextEntry(addressOfNextEntry, pointer);
						return true;
					}
					// go to next entry in list...
					pointer = entry.getThird();
				} while(true);
			}

		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return false;
	}

    @Override
	public boolean contains(final Object element) {
		try {
			this.size++;

			final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final int hashAddress = element.hashCode() % this.TABLESIZE;

			long pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, hashAddress * 8, 8));

			if(pointer == 0) {
				// no entry so far at this position in the hash table...
				return false;
			} else {
				// check existing list of entries for this table position
				Triple<V, Long, Long> entry;
				do {
					entry = this.getElement(pointer);
					if(entry.getFirst().equals(element)) {
						// found!
						return (entry.getSecond()>0);
					}
					if(entry.getThird() == 0) {
						// end of this list reached!
						return false;
					}
					// go to next entry in list...
					pointer = entry.getThird();
				} while(true);
			}

		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return false;
    }


	@Override
	public boolean remove(final Object element) {
		try {
			final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final int hashAddress = element.hashCode() % this.TABLESIZE;

			long pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, hashAddress * 8, 8));

			if(pointer == 0) {
				return false;
			} else {
				// check existing list of entries for this table position
				Triple<V, Long, Long> entry;
				do {
					entry = this.getElement(pointer);
					if(entry.getFirst().equals(element)) {
						if(entry.getSecond()>0){
							// decrease number of elements by one...
							this.storeNumberOfElements(entry.getSecond() - 1, pointer);
							this.size--;
							return true;
						} else {
							return false;
						}
					}
					if(entry.getThird() == 0) {
						// end of list reached!
						return false;
					}
					// go to next entry in list...
					pointer = entry.getThird();
				} while(true);			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	public boolean removeAllDuplicates(final Object element) {
		try {
			final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final int hashAddress = element.hashCode() % this.TABLESIZE;

			long pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, hashAddress * 8, 8));

			if(pointer == 0) {
				return false;
			} else {
				// check existing list of entries for this table position
				Triple<V, Long, Long> entry;
				do {
					entry = this.getElement(pointer);
					if(entry.getFirst().equals(element)) {
						if(entry.getSecond()>0){
							// set number of elements to zero...
							this.storeNumberOfElements(0, pointer);
							this.size-=entry.getSecond();
							return true;
						} else {
							return false;
						}
					}
					if(entry.getThird() == 0) {
						// end of list reached!
						return false;
					}
					// go to next entry in list...
					pointer = entry.getThird();
				} while(true);			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	private long storeNewElement(final V element) {
		final long iLastValue = this.lastValue;

		this.storeElement(element, 1, 0, this.lastValue);

		this.lastValue += Registration.lengthSerializeWithoutId(element) + 2 * LengthHelper.lengthLuposLong();

		return iLastValue;
	}

	private final void storeNumberOfElements(final long numberOfElements, final long address){
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			OutHelper.writeLuposLong(numberOfElements, out); // currently one element inside!
			out.close();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private final boolean decrementNumberOfElements(final long address){
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			long numberOfElements = InputHelper.readLuposLong(in);
			in.close();
			if(numberOfElements>0){
				numberOfElements--;
				final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
				OutHelper.writeLuposLong(numberOfElements, out);
				out.close();
				return true;
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	private final long setNumberOfElementsTo0(final long address){
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			final long numberOfElements = InputHelper.readLuposLong(in);
			in.close();
			if(numberOfElements>0){
				final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
				OutHelper.writeLuposLong(0, out);
				out.close();
				return numberOfElements;
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	private final void storeAddressOfNextEntry(final long addressOfNextEntry, final long address){
		this.storeNumberOfElements(addressOfNextEntry, address + 8); // from semantic point of view not a nice code, but reusing code is optimal!
	}

	private final void storeElement(final V element, final long numberOfElements, final long nextEntry, final long address) {
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			OutHelper.writeLuposLong(numberOfElements, out); // currently one element inside!
			OutHelper.writeLuposLong(nextEntry, out); // end of list!
			Registration.serializeWithoutId(element, out);
			out.close();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private final Triple<V, Long, Long> getElement(final long address) {
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			final long numberOfElements = InputHelper.readLuposLong(in);
			final long addressOfNextElement = InputHelper.readLuposLong(in);
			final V element = Registration.deserializeWithoutId(this.classOfValues, in);
			in.close();
			return new Triple<V, Long, Long>(element, numberOfElements, addressOfNextElement);
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
		return null;
	}

	public Iterator<V> iteratorWithDuplicates() {
		return new Iterator<V>(){

			private long current = 0;

			private int address = -8;

			private long addressOfNextEntry = 0;

			private long addressOfCurrentEntry = 0;

			private V currentElement = null;

			private long remainingNumberOfElements = 0;

			@Override
			public boolean hasNext() {
				return (this.current<PagedHashMultiSet.this.size);
			}

			@Override
			public V next() {
				if(this.hasNext()){
					if(this.remainingNumberOfElements>0){
						this.current++;
						this.remainingNumberOfElements--;
						return this.currentElement;
					}

					if(this.addressOfNextEntry == 0){
						try {
							final PageAddress pageAddress = new PageAddress(0, PagedHashMultiSet.this.pointersFilename);
							final byte[] page = BufferManager.getBufferManager().getPage(PagedHashMultiSet.this.TABLEPAGESIZE, pageAddress);
							long pointer;
							do {
								this.address += 8;
								pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, this.address, 8));
							} while(pointer==0);

							this.addressOfNextEntry = pointer;
						} catch (final IOException e) {
							System.err.println(e);
							e.printStackTrace();
						}
					}

					this.addressOfCurrentEntry = this.addressOfNextEntry;
					final Triple<V, Long, Long> entry = PagedHashMultiSet.this.getElement(this.addressOfNextEntry);
					this.currentElement = entry.getFirst();
					this.remainingNumberOfElements = entry.getSecond();
					this.addressOfNextEntry = entry.getThird();

					return this.next();
				}
				return null;
			}

			@Override
			public void remove() {
				if(this.addressOfCurrentEntry>0){
					if(PagedHashMultiSet.this.decrementNumberOfElements(this.addressOfCurrentEntry)){
						PagedHashMultiSet.this.size--;
						this.current--;
					}
				}
			}
		};
	}

	@Override
	public Iterator<V> iterator() {
		return new Iterator<V>(){

			private long current = 0;

			private int address = -8;

			private long addressOfNextEntry = 0;

			private long addressOfCurrentEntry = 0;

			private V currentElement = null;

			private long remainingNumberOfElements = 0;

			@Override
			public boolean hasNext() {
				return (this.current<PagedHashMultiSet.this.size);
			}

			@Override
			public V next() {
				if(this.hasNext()){
					if(this.remainingNumberOfElements>0){
						this.current+=this.remainingNumberOfElements;
						this.remainingNumberOfElements=0;
						return this.currentElement;
					}

					if(this.addressOfNextEntry == 0){
						try {
							final PageAddress pageAddress = new PageAddress(0, PagedHashMultiSet.this.pointersFilename);
							final byte[] page = BufferManager.getBufferManager().getPage(PagedHashMultiSet.this.TABLEPAGESIZE, pageAddress);
							long pointer;
							do {
								this.address += 8;
								pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, this.address, 8));
							} while(pointer==0);

							this.addressOfNextEntry = pointer;
						} catch (final IOException e) {
							System.err.println(e);
							e.printStackTrace();
						}
					}

					this.addressOfCurrentEntry = this.addressOfNextEntry;
					final Triple<V, Long, Long> entry = PagedHashMultiSet.this.getElement(this.addressOfNextEntry);
					this.currentElement = entry.getFirst();
					this.remainingNumberOfElements = entry.getSecond();
					this.addressOfNextEntry = entry.getThird();

					return this.next();
				}
				return null;
			}

			@Override
			public void remove() { // remove all entries inclusive duplicates!
				if(this.addressOfCurrentEntry>0){
					final long diff = PagedHashMultiSet.this.setNumberOfElementsTo0(this.addressOfCurrentEntry);
					if(diff>0){
						PagedHashMultiSet.this.size-=diff;
						this.current-=diff;
					}
				}
			}
		};
	}

	@Override
	public String toString(){
		return PagedHashMultiSet.toString(this.iteratorWithDuplicates());
	}

	public String toStringWithoutDuplicates(){
		return PagedHashMultiSet.toString(this.iterator());
	}

	public static<V> String toString(final Iterator<V> it){
		String result = "Paged Hash Set: {";
		boolean firstTime = true;
		while(it.hasNext()){
			final V entry = it.next();
			if(firstTime){
				firstTime = false;
			} else {
				result+=", ";
			}
			result+=entry;
		}
		return result+" }";
	}

	@Override
	public void clear() {
		this.size=0;
		this.lastValue = 0;
	}

	@Override
	public int size() {
		return (int) this.size;
	}

	protected PagedHashMultiSet(final Class<V> classOfValues, final String pointersFilename, final String stringsFilename, final long size, final long lastValue) throws IOException {
		this.classOfValues = classOfValues;
		this.pointersFilename = pointersFilename;
		this.valuesFilename = stringsFilename;
		this.size = size;
		this.lastValue = lastValue;
	}

	public static<V> PagedHashMultiSet<V> readLuposPagedHashSet(final InputStream lois) throws IOException{
		final String pointersFilename = InputHelper.readLuposString(lois);
		final String valuesFilename = InputHelper.readLuposString(lois);
		@SuppressWarnings("unchecked")
		final Class<V> classOfValues = (Class<V>) Registration.deserializeId(lois)[0];
		final long size = InputHelper.readLuposLong(lois);
		final long lastValue = InputHelper.readLuposLong(lois);
		return new PagedHashMultiSet<V>(classOfValues, pointersFilename, valuesFilename, size, lastValue);
	}

	public void writeLuposPagedHashSet(final OutputStream loos) throws IOException{
		BufferManager.getBufferManager().writeAllModifiedPages();
		OutHelper.writeLuposString(this.pointersFilename, loos);
		OutHelper.writeLuposString(this.valuesFilename, loos);
		OutHelper.writeLuposLong(this.size, loos);
		OutHelper.writeLuposLong(this.lastValue, loos);
	}

	public static int getFileID() {
		return PagedHashMultiSet.fileID;
	}

	public static void setFileID(final int fileID) {
		PagedHashMultiSet.fileID = fileID;
	}

	/**
	 * just to quickly test the implementation...
	 */
	public static void main(final String[] args) throws IOException{
		final PagedHashMultiSet<String> d = new PagedHashMultiSet<String>(String.class);
		d.add("hello");
		d.add("hallo");
		d.add("3");
		d.add("4");
		d.add("4");
		System.out.println(d);
		System.out.println(d.toStringWithoutDuplicates());
		d.removeAllDuplicates("4");
		d.remove("3");
		d.add("3");
		d.add("3");
		System.out.println(d);
		System.out.println(d.toStringWithoutDuplicates());
		BufferManager.getBufferManager().writeAllModifiedPages();
	}
}
