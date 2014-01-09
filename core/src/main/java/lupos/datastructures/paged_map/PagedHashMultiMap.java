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
package lupos.datastructures.paged_map;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import lupos.misc.Quadruple;
import lupos.misc.Triple;

/**
 * PagedHashMultiMap implements a hash multi map with keys and values on disk.
 *
 * Multi Map means that keys are unique and multiple occurrences of elements referring to a key are counted in the map.
 * Keys and elements are removed completely.
 *
 * It uses three files: In the first file pointers to the start position of keys are stored.
 * In the second file the actual keys are stored and in the third file the actual elements are stored.
 * The write access of the three files is unsynchronized.
 *
 * Keys or values can only be added to the keys or values file at the end.
 *
 * The first four bytes in the pointers file store the maximum index for the keys
 * and the first 8 bytes in the keys or values file store the maximum position in the keys or values file.
 * Each key and each value is part of a linked list, after the serialization of a key or value an 8-bytes pointer
 * is used to point to the next key or value in the list (is 0 for marking the end of the list).
 *
 * see PagedHashMultiMap.pdf in folder documentation of this project
 *
 * @author K. Knof
 *
 * @param <K>
 * keys
 * @param <V>
 * values
 */
public class PagedHashMultiMap<K,V> extends AbstractMap<K,V> {

	// the current size of keys
	long sizeKeys = 0;
	// the current size of values in all keys
	long sizeValues = 0;
	// the next free byte in the keys file
	long lastKey = 1; // the first byte is wasted, as 0 marks no reference!
	// the next free byte in the values file
	long lastValue = 1; // the first byte is wasted, as 0 marks no reference!

	// the filename of the pointers file
	final String pointersFilename;
	// the filename of the keys file
	final String keysFilename;
	// the filename of the values file
	final String valuesFilename;

	// the class of keys
	final Class<K> classOfKeys;
	// the class of values
	final Class<V> classOfValues;

	// the current id, important, if several paged hash maps are instantiated
	// (each instance gets another id and therefore stores its content into different files)
	static int fileID = 0;

	// the lock for getting a new id
	protected static ReentrantLock lock = new ReentrantLock();

	// the initial table size
	private static int INITIALTABLESIZE = 1024;

	// the current table size
	private final int TABLESIZE = INITIALTABLESIZE;

	// tablepagesize
	private final int TABLEPAGESIZE = INITIALTABLESIZE * 8;

	/**
	 * @param classOfKeys
	 * class of keys
	 * @param classOfValues
	 * class of values
	 */
	public PagedHashMultiMap(final Class<K> classOfKeys, final Class<V> classOfValues) {
		this.classOfKeys = classOfKeys;
		this.classOfValues = classOfValues;
		PagedHashMultiMap.lock.lock();
		try{
			// use directory of DiskCollection for storing hash maps on disk!
			DiskCollection.makeFolders();
			final int currentID = fileID++;
			// remove old pointers, keys and values files from disk!
			final String[] dirs = DiskCollection.getTmpDir();
			for(final String dir: dirs){
				FileHelper.deleteFilesStartingWithPattern(dir, currentID + ".table_");
				FileHelper.deleteFilesStartingWithPattern(dir, currentID + ".keys_");
				FileHelper.deleteFilesStartingWithPattern(dir, currentID + ".values_");
			}
			final String dir = dirs[currentID % dirs.length];
			this.pointersFilename = dir + currentID + ".table";
			this.keysFilename = dir + currentID + ".keys";
			this.valuesFilename = dir + currentID + ".values";
		} finally {
			PagedHashMultiMap.lock.unlock();
		}
	}

	/**
	 * @param classOfKeys
	 * the class of keys
	 * @param classOfValues
	 * the class of values
	 * @param pointersFilename
	 * the filename of the pointers file
	 * @param keysFilename
	 * the filename of the keys file
	 * @param valuesFilename
	 * the filename of the values file
	 * @param sizeKeys
	 * the size of keys
	 * @param sizeValues
	 * the size of values in all keys
	 * @param lastKey
	 * the next free byte in the keys file
	 * @param lastValue
	 * the next free byte in the values file
	 * @throws IOException
	 */
	protected PagedHashMultiMap(final Class<K> classOfKeys, final Class<V> classOfValues, final String pointersFilename, final String keysFilename, final String valuesFilename, final long sizeKeys, final long sizeValues, final long lastKey, final long lastValue) throws IOException {
		this.classOfKeys = classOfKeys;
		this.classOfValues = classOfValues;
		this.pointersFilename = pointersFilename;
		this.keysFilename = keysFilename;
		this.valuesFilename = valuesFilename;
		this.sizeKeys = sizeKeys;
		this.sizeValues = sizeValues;
		this.lastKey = lastKey;
		this.lastValue = lastValue;
	}

	/**
	 * read a PagedHashMultiMap from input stream
	 *
	 * @param lois
	 * input stream
	 * @return
	 * PagedHashMultiMap<K,V>
	 * @throws IOException
	 */
	public static<K,V> PagedHashMultiMap<K,V> readLuposPagedHashMap(final InputStream lois) throws IOException{
		final String pointersFilename = InputHelper.readLuposString(lois);
		final String keysFilename = InputHelper.readLuposString(lois);
		final String valuesFilename = InputHelper.readLuposString(lois);
		@SuppressWarnings("unchecked")
		final Class<K> classOfKeys = (Class<K>) Registration.deserializeId(lois)[0];
		@SuppressWarnings("unchecked")
		final Class<V> classOfValues = (Class<V>) Registration.deserializeId(lois)[0];
		final long sizeKeys = InputHelper.readLuposLong(lois);
		final long sizeValues = InputHelper.readLuposLong(lois);
		final long lastKey = InputHelper.readLuposLong(lois);
		final long lastValue = InputHelper.readLuposLong(lois);
		return new PagedHashMultiMap<K,V>(classOfKeys, classOfValues, pointersFilename, keysFilename, valuesFilename, sizeKeys, sizeValues, lastKey, lastValue);
	}

	/**
	 * write a PagedHashMultiMap to output stream
	 *
	 * @param loos
	 * output stream
	 * @throws IOException
	 */
	public void writeLuposPagedHashMap(final OutputStream loos) throws IOException{
		BufferManager.getBufferManager().writeAllModifiedPages();
		OutHelper.writeLuposString(this.pointersFilename, loos);
		OutHelper.writeLuposString(this.keysFilename, loos);
		OutHelper.writeLuposString(this.valuesFilename, loos);
		Registration.serializeClass(this.classOfKeys, loos);
		Registration.serializeClass(this.classOfValues, loos);
		OutHelper.writeLuposLong(this.sizeKeys, loos);
		OutHelper.writeLuposLong(this.sizeValues, loos);
		OutHelper.writeLuposLong(this.lastKey, loos);
		OutHelper.writeLuposLong(this.lastValue, loos);
	}

	/**
	 * put a key and an element referring to the key to PagedHashMultiMap
	 *
	 * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
	 *
	 * @param key
	 * key
	 * @param element
	 * element referring to the key
	 * @return
	 * element
	 */
	@Override
	public V put(final K key, final V element) {

		try {
			final PageAddress pageAddressPointers = new PageAddress(0, this.pointersFilename);
			final byte[] pagePointers = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddressPointers);

			final int hashAddress = Math.abs(key.hashCode()) % this.TABLESIZE;

			final long pointer = InputHelper.readLuposLong(new ByteArrayInputStream(pagePointers, hashAddress * 8, 8));

			final ResSet resultKey = this.containKeyGetAddressOfFoundKeyGetAddressOfLastKey(key);
			final boolean containKey = resultKey.containEntry;
			final long addressOfFoundKey = resultKey.addressOfFoundEntry;
			final long addressOfLastKey = resultKey.addressOfLastEntry;
			// check if hash table contains key
			if(!containKey) {
				this.sizeKeys++;
				final long addressOfKey = this.storeNewKey(key);

				if(pointer == 0){
					// no entry so far at this position in the hash table...
					// write address in hash table
					final OutputStream outKeys = new ExistingByteArrayOutputStream(pagePointers, hashAddress * 8);
					OutHelper.writeLuposLong(addressOfKey, outKeys);
					outKeys.close();
					BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddressPointers, pagePointers);
				} else {
					// already entries at this position in the hash table...
					this.storeAddressOfNextKey(addressOfKey, addressOfLastKey);
				}

				// store element referring to the key
				this.sizeValues++;
				final long addressOfElement = this.storeNewElement(element);
				this.storeAddressOfValues(addressOfElement, addressOfKey);
				this.storeNumberOfKeyElements(1, addressOfKey);
				return element;
			} else {
					this.sizeValues++;
					// increment number of key elements
					final long numberOfKeyElements = this.getNumberOfKeyElements(addressOfFoundKey);
					this.storeNumberOfKeyElements((numberOfKeyElements + 1), addressOfFoundKey);

					final long addressOfValues = this.getAddressOfValues(addressOfFoundKey);
					final ResSet resultElement = this.containElementGetAddressOfFoundElementGetAddressOfLastElement(element, addressOfValues);
					final boolean containElement = resultElement.containEntry;
					final long addressOfFoundElement = resultElement.addressOfFoundEntry;
					final long addressOfLastElement = resultElement.addressOfLastEntry;
					// check if list contains element
					if (containElement){
						// increment number of elements
						final long numberOfElements = this.getNumberOfElements(addressOfFoundElement);
						this.storeNumberOfElements(numberOfElements + 1, addressOfFoundElement);
						return element;
					}
					else {
						// add element to end of list
						final long addressOfElement = this.storeNewElement(element);
						this.storeAddressOfNextElement(addressOfElement, addressOfLastElement);
						return element;
					}
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return element;
	}

	/**
	 * this method completely removes a key from PagedHashMultiMap
	 *
	 * @see java.util.AbstractMap#remove(java.lang.Object)
	 *
	 * @param key
	 * key
	 * @return
	 * element
	 */
	@SuppressWarnings("unchecked")
	@Override
	public V remove(final Object key){
		K thiskey = null;
		try {
			thiskey = (K)key;
		} catch (final ClassCastException e){
			System.err.println("... couldn't cast object");
			System.err.println("ClassCastException: " + e.getMessage());
			return null;
		}
		final ResSet resultKey = this.containKeyGetAddressOfFoundKeyGetAddressOfLastKey(thiskey);
		final boolean containKey = resultKey.containEntry;
		final long addressOfFoundKey = resultKey.addressOfFoundEntry;
		// check if hash table contains key
		if(!containKey) {
			return null;
		} else {
			if (this.sizeKeys > 0){
				this.sizeKeys--;
			}
			final long numberOfKeyElements = this.getNumberOfKeyElements(this.getAddressOfKey(thiskey));
			if (this.sizeValues > 0) {
				this.sizeValues-=numberOfKeyElements;
			}
			long addressOfPreviousKey = 0;
			try {
				final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
				final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

				final int hashAddress = Math.abs(thiskey.hashCode()) % this.TABLESIZE;

				long pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, hashAddress * 8, 8));

				// check existing list of entries for this table position
				Quadruple<K, Long, Long, Long> entry;
				boolean found = false;
				do {
					entry = this.getKey(pointer);
					if(entry.getFourth() == addressOfFoundKey) {
						// found!
						addressOfPreviousKey = pointer;
						found = true;
					}
					if((entry.getFourth() == 0)) {
						found = true;
					}
					// go to next entry in list...
					pointer = entry.getFourth();
				} while(!found);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			// check if key is first entry in key list
			if (addressOfPreviousKey == 0) {
				// check if key is last entry in key list
				if (this.getKey(addressOfFoundKey).getFourth() == 0){
					try {
						final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
						final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

						final int hashAddress = Math.abs(thiskey.hashCode()) % this.TABLESIZE;

						final OutputStream outKeys = new ExistingByteArrayOutputStream(page, hashAddress * 8);
						OutHelper.writeLuposLong(0, outKeys);
						outKeys.close();
						BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
					} catch (final IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				} // key is not last entry in key list
				else {
					try {
						final PageAddress pageAddressPointers = new PageAddress(0, this.pointersFilename);
						final byte[] pagePointers = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddressPointers);

						final int hashAddress = Math.abs(thiskey.hashCode()) % this.TABLESIZE;

						final OutputStream outKeys = new ExistingByteArrayOutputStream(pagePointers, hashAddress * 8);
						OutHelper.writeLuposLong(this.getKey(addressOfFoundKey).getFourth(), outKeys);
						outKeys.close();
						BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddressPointers, pagePointers);
					} catch (final IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			} // key is not first entry in key list
			else {
				// check if key is last entry in key list
				if (this.getKey(addressOfFoundKey).getFourth() == 0){
					this.storeAddressOfNextKey(0, addressOfPreviousKey);
				} // key is not last entry in key list
				else {
					this.storeAddressOfNextKey(this.getKey(addressOfFoundKey).getFourth(), addressOfPreviousKey);
				}
			}
		return null;
		}
	}

	/**
	 * this method completely removes an element referring to a key from PagedHashMultiMap
	 *
	 * @param key
	 * key
	 * @param element
	 * element
	 * @return
	 * element
	 */
	public V remove(final K key, final V element){
		final ResSet resultKey = this.containKeyGetAddressOfFoundKeyGetAddressOfLastKey(key);
		final boolean containKey = resultKey.containEntry;
		final long addressOfFoundKey = resultKey.addressOfFoundEntry;
		// check if hash table contains key
		if(!containKey) {
			return null;
		} else {

			final long addressOfValues = this.getAddressOfValues(addressOfFoundKey);
			final ResSet resultElement = this.containElementGetAddressOfFoundElementGetAddressOfLastElement(element, addressOfValues);
			final boolean containElement = resultElement.containEntry;
			final long addressOfFoundElement = resultElement.addressOfFoundEntry;
			// check if hash table contains element referring to key
			if(!containElement){
				return null;
			} else {


				final long numberOfElements = this.getNumberOfElements(addressOfFoundElement);
				if (this.sizeValues > 0){
					this.sizeValues -= numberOfElements;
				}
				final long numberOfKeyElements = this.getNumberOfKeyElements(addressOfFoundKey);
				final long newNumberOfKeyElements = numberOfKeyElements - numberOfElements;
				this.storeNumberOfKeyElements(newNumberOfKeyElements, addressOfFoundKey);
				if (newNumberOfKeyElements == 0){
					this.remove(key);
					return null;
				} else {
					long addressOfPreviousElement = 0;
					long pointer = addressOfValues;
					// check existing list of elements
					Triple<V, Long, Long> entry;
					boolean found = false;
					do {
						entry = this.getElement(pointer);
						if(entry.getThird() == addressOfFoundElement) {
							// found!
							addressOfPreviousElement = pointer;
							found = true;
						}
						if((entry.getThird() == 0)) {
							found = true;
						}
						// go to next entry in list...
						pointer = entry.getThird();
					} while(!found);
					// check if element is first entry in element list
					if (addressOfPreviousElement == 0) {
						// check if element is last entry in element list
						if (this.getElement(addressOfFoundElement).getThird() == 0){
							// if element is the first and last element in list: remove key
							this.remove(key);
						} else {
							// element is not last entry in element list
							this.storeAddressOfValues(this.getElement(addressOfFoundElement).getThird(), addressOfFoundKey);
						}
					} // element is not first entry in element list
					else {
						// check if element is last entry in element list
						if (this.getElement(addressOfFoundElement).getThird() == 0){
							this.storeAddressOfNextElement(0, addressOfPreviousElement);
						} // element is not last entry in element list
						else {
							this.storeAddressOfNextElement(this.getElement(addressOfFoundElement).getThird(), addressOfPreviousElement);
						}
					}
					return null;
				}
			}
		}
	}

	/**
	 * this method removes all duplicate of elements
	 *
	 * @param key
	 * key
	 * @param element
	 * element
	 * @return
	 * remove status
	 */
	public boolean removeAllDuplicates(final K key, final V element) {
		final ResSet resultKey = this.containKeyGetAddressOfFoundKeyGetAddressOfLastKey(key);
		final boolean containKey = resultKey.containEntry;
		final long addressOfFoundKey = resultKey.addressOfFoundEntry;
		// check if hash table contains key
		if(!containKey) {
			return false;
		} else {
			// check existing list of entries
			if(this.getNumberOfKeyElements(addressOfFoundKey)> 0){
				final long addressOfValues = this.getAddressOfValues(addressOfFoundKey);
				final ResSet resultElement = this.containElementGetAddressOfFoundElementGetAddressOfLastElement(element, addressOfValues);
				final boolean containElement = resultElement.containEntry;
				final long addressOfFoundElement = resultElement.addressOfFoundEntry;
				if (containElement){
					final long numberOfElements = this.getNumberOfElements(addressOfFoundElement);
					if (numberOfElements > 0) {
						// set number of elements to zero and decrement number of key elements by number of elements
						this.setNumberOfElementsTo0(addressOfFoundElement);
						this.sizeValues-=numberOfElements;
						final long numberOfKeyElements = this.getNumberOfKeyElements(addressOfFoundKey);
						this.storeNumberOfKeyElements(numberOfKeyElements - numberOfElements, addressOfFoundKey);
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * set of entries without duplicates
	 *
	 * @see java.util.AbstractMap#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		final Set<Entry<K, V>> mapSet = new HashSet<Entry<K, V>>();
		SimpleEntry<K,V> entry = null;
		final Iterator<K> itkeys = this.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				final Iterator<V> itelements = this.iteratorElements(key);
				while(itelements.hasNext()){
					final V element = itelements.next();
					entry = new SimpleEntry<K,V>(key, element);
					mapSet.add(entry);
				}
			}
		}
		return mapSet;
	}

	/**
	 * list of entries with duplicates
	 *
	 * @return
	 * list of entries
	 */
	public List<java.util.Map.Entry<K, V>> entryList() {
		final List<Entry<K, V>> mapSet = new ArrayList<Entry<K, V>>();
		SimpleEntry<K,V> entry = null;
		final Iterator<K> itkeys = this.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				final Iterator<V> itelements = this.iteratorWithDuplicates(key);
				while(itelements.hasNext()){
					final V element = itelements.next();
					entry = new SimpleEntry<K,V>(key, element);
					mapSet.add(entry);
				}
			}
		}
		return mapSet;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#keySet()
	 */
	@Override
	public Set<K> keySet() {
		final Set<K> keySet = new HashSet<K>();
		final Iterator<K> itkeys = this.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				keySet.add(key);
			}
		}
		return keySet;
	}

	/**
	 * values list with duplicates
	 *
	 * @param key
	 * key
	 * @return
	 * values list
	 */
	public Collection<V> getValuesList(final K key){
		final List<V> valuesList = new ArrayList<V>();
		if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
			final Iterator<V> itelements = this.iteratorWithDuplicates(key);
			while(itelements.hasNext()){
				final V element = itelements.next();
				valuesList.add(element);
			}
		}
		return valuesList;
	}

	/**
	 * collection of element with duplicates
	 *
	 * @param key
	 * key
	 * @return
	 * collection of elements
	 * @throws IOException
	 */
	public Collection<V> getCollection(final K key) throws IOException{

		if (this.getAddressOfKey(key) == 0){
			return null;
		} else {
			final GetCollection<K, V> coll = new GetCollection<K, V>(this.classOfKeys, this.classOfValues, this.pointersFilename, this.keysFilename, this.valuesFilename, this.sizeKeys, this.sizeValues, this.lastKey, this.lastValue, key, fileID);
			return coll.getCollection();
		}
	}

	/**
	 * collection of values with duplicates
	 *
	 * @see java.util.AbstractMap#values()
	 */
	@Override
	public Collection<V> values(){
		final List<V> values = new ArrayList<V>();
		final Iterator<K> itkeys = this.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				final Iterator<V> itelements = this.iteratorWithDuplicates(key);
				while(itelements.hasNext()){
					final V element = itelements.next();
					values.add(element);
				}
			}
		}
		return values;
	}

	/**
	 * this method tests if PagedHashMultiMap contains key
	 *
	 * @param key
	 * key
	 * @return
	 * result set contain key, address of found key, addres of last key
	 */
	public ResSet containKeyGetAddressOfFoundKeyGetAddressOfLastKey(final K key) {
		try {
			final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final int hashAddress = Math.abs(key.hashCode()) % this.TABLESIZE;

			long pointer = InputHelper.readLuposLong(new ByteArrayInputStream(page, hashAddress * 8, 8));

			if(pointer == 0) {
				// no entry so far at this position in the hash table...
				return new ResSet (false, 0, 0);
			} else {
				// check existing list of entries for this table position
				Quadruple<K, Long, Long, Long> entry;
				do {
					entry = this.getKey(pointer);
					if(entry.getFirst().equals(key)) {
						// found!
						return new ResSet (true, pointer, 0);
					}
					if(entry.getFourth() == 0) {
						// end of this list reached!
						return new ResSet (false, 0, pointer);
					}
					// go to next entry in list...
					pointer = entry.getFourth();
				} while(true);
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return new ResSet (false, 0, 0);
    }

	/**
	 * this method tests if PagedHashMultiMap contains an element referring to a key
	 *
	 * @param element
	 * element
	 * @param addressOfValues
	 * address of values list of key
	 * @return
	 * result set contain element, address of found element, address of last element
	 */
	public ResSet containElementGetAddressOfFoundElementGetAddressOfLastElement(final V element, final long addressOfValues) {
		long pointer = addressOfValues;
		if(pointer == 0) {
			// no entry so far...
			return new ResSet(false, 0, 0);
		} else {
			// check existing list of elements
			Triple<V, Long, Long> entry;
			do {
				entry = this.getElement(pointer);
				if(entry.getFirst().equals(element)) {
					// found!
					return new ResSet(true, pointer, 0);
				}
				if(entry.getThird() == 0) {
					// end of this list reached!
					return new ResSet(false, 0, pointer);
				}
				// go to next entry in list...
				pointer = entry.getThird();
			} while(true);
		}
    }

	/**
	 * this method stores a new key
	 *
	 * @param key
	 * key
	 * @return
	 * address of key
	 */
	private long storeNewKey(final K key) {

		if (!(key == null)){
			final long iLastKey = this.lastKey;

			this.storeKey(key, 0, 0, 0, this.lastKey);

			this.lastKey += Registration.lengthSerializeWithoutId(key) + 3 * LengthHelper.lengthLuposLong();

			return iLastKey;
		}
		return this.lastKey;
	}

	/**
	 * this method stores a new element
	 *
	 * @param element
	 * element
	 * @return
	 * address of element
	 */
	private long storeNewElement(final V element) {

		if (!(element == null)){
			final long iLastValue = this.lastValue;

			this.storeElement(element, 1, 0, this.lastValue);

			this.lastValue += Registration.lengthSerializeWithoutId(element) + 2 * LengthHelper.lengthLuposLong();

			return iLastValue;
		}
		return this.lastValue;
	}

	/**
	 * this method stores a key
	 *
	 * @param key
	 * key
	 * @param addressOfValues
	 * address of values
	 * @param numberOfKeyElements
	 * number of key elements
	 * @param addressOfNextKey
	 * address of next key
	 * @param addressKey
	 * address of key
	 */
	private final void storeKey(final K key, final long addressOfValues, final long numberOfKeyElements, final long addressOfNextKey, final long addressKey) {
		final int pagenumber = (int) (addressKey / PageManager.getDefaultPageSize());
		final int index = (int) (addressKey % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.keysFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			OutHelper.writeLuposLong(addressOfValues, out);
			OutHelper.writeLuposLong(numberOfKeyElements, out);
			OutHelper.writeLuposLong(addressOfNextKey, out);
			Registration.serializeWithoutId(key, out);
			out.close();
			BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * this method stores an element
	 *
	 * @param element
	 * element
	 * @param numberOfElements
	 * number of elements
	 * @param addressOfNextElement
	 * address of next element
	 * @param addressElement
	 * address element
	 */
	private final void storeElement(final V element, final long numberOfElements, final long addressOfNextElement, final long addressElement) {
		final int pagenumber = (int) (addressElement / PageManager.getDefaultPageSize());
		final int index = (int) (addressElement % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.valuesFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			OutHelper.writeLuposLong(numberOfElements, out);
			OutHelper.writeLuposLong(addressOfNextElement, out);
			Registration.serializeWithoutId(element, out);
			out.close();
			BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * this method stores the address of values
	 *
	 * @param addressOfValues
	 * address of values
	 * @param addressKey
	 * address of key
	 */
	public final void storeAddressOfValues(final long addressOfValues, final long addressKey){
		final int pagenumber = (int) (addressKey / PageManager.getDefaultPageSize());
		final int index = (int) (addressKey % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.keysFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			OutHelper.writeLuposLong(addressOfValues, out);
			out.close();
			BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * this method stores the number of key elements
	 *
	 * @param numberOfKeyElements
	 * number of kez elements
	 * @param address
	 * address
	 */
	private final void storeNumberOfKeyElements(final long numberOfKeyElements, final long address){
		this.storeAddressOfValues(numberOfKeyElements, address + 8);
	}

	/**
	 * this method stores the address of next key
	 *
	 * @param addressOfNextEntry
	 * address of next key
	 * @param address
	 * address
	 */
	private final void storeAddressOfNextKey(final long addressOfNextEntry, final long address){
		this.storeNumberOfKeyElements(addressOfNextEntry, address + 8);
	}

	/**
	 * this method stores the number of elements
	 *
	 * @param numberOfElements
	 * number of elements
	 * @param addressElement
	 * address of element
	 */
	private final void storeNumberOfElements(final long numberOfElements, final long addressElement){
		final int pagenumber = (int) (addressElement / PageManager.getDefaultPageSize());
		final int index = (int) (addressElement % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.valuesFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			OutHelper.writeLuposLong(numberOfElements, out);
			out.close();
			BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * this method stores the address of next element
	 *
	 * @param addressOfNextEntry
	 * address of next element
	 * @param address
	 * address
	 */
	public final void storeAddressOfNextElement(final long addressOfNextEntry, final long address){
		this.storeNumberOfElements(addressOfNextEntry, address + 8);
	}

	/**
	 * structure of data
	 *
	 * @param address
	 * address of key
	 * @return
	 * Quadruple of key informations
	 */
	public final Quadruple<K, Long, Long, Long> getKey(final long address) {
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			final long addressOfValues = InputHelper.readLuposLong(in);
			final long numberOfKeyElements = InputHelper.readLuposLong(in);
			final long addressOfNextKey = InputHelper.readLuposLong(in);
			final K key = Registration.deserializeWithoutId(this.classOfKeys, in);
			in.close();
			return new Quadruple<K, Long, Long, Long>(key, addressOfValues, numberOfKeyElements, addressOfNextKey);
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


/**
 * structure of data
 *
 * @param address
 * address of element
 * @return
 * Triple of element informations
 */
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

	/**
	 * this method decrements the number of key elements
	 *
	 * @param addressKey
	 * address of key
	 * @return
	 * decrement status
	 */
	private final boolean decrementNumberOfKeyElements(final long addressKey){
		final int pagenumber = (int) (addressKey / PageManager.getDefaultPageSize());
		final int index = (int) (addressKey % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.keysFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			final long addressOfValues = InputHelper.readLuposLong(in);
			long numberOfKeyElements = InputHelper.readLuposLong(in);
			in.close();
			if(numberOfKeyElements>0){
				numberOfKeyElements--;
				final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
				OutHelper.writeLuposLong(addressOfValues, out);
				OutHelper.writeLuposLong(numberOfKeyElements, out);
				out.close();
				BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
				return true;
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * this method decrements the number of elements
	 *
	 * @param addressElement
	 * address of element
	 * @return
	 * decrement status
	 */
	private final boolean decrementNumberOfElements(final long addressElement){
		final int pagenumber = (int) (addressElement / PageManager.getDefaultPageSize());
		final int index = (int) (addressElement % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.valuesFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			long numberOfElements = InputHelper.readLuposLong(in);
			in.close();
			if(numberOfElements>0){
				numberOfElements--;
				final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
				OutHelper.writeLuposLong(numberOfElements, out);
				out.close();
				BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
				return true;
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * this method sets the number of key elements to 0
	 *
	 * @param addressKey
	 * address of key
	 * @return
	 * address
	 */
	public final long setNumberOfKeyElementsTo0(final long addressKey){
		final int pagenumber = (int) (addressKey / PageManager.getDefaultPageSize());
		final int index = (int) (addressKey % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.keysFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			final long addressOfValues = InputHelper.readLuposLong(in);
			final long numberOfKeyElements = InputHelper.readLuposLong(in);
			in.close();
			if(numberOfKeyElements>0){
				final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
				OutHelper.writeLuposLong(addressOfValues, out);
				OutHelper.writeLuposLong(0, out);
				out.close();
				BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
				return numberOfKeyElements;
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * this method sets the number of elements to 0
	 *
	 * @param addressElement
	 * address of element
	 * @return
	 * address
	 */
	public final long setNumberOfElementsTo0(final long addressElement){
		final int pagenumber = (int) (addressElement / PageManager.getDefaultPageSize());
		final int index = (int) (addressElement % PageManager.getDefaultPageSize());
		try {
			final PageAddress pageAddress = new PageAddress(0, this.valuesFilename);
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);

			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			final long numberOfElements = InputHelper.readLuposLong(in);
			in.close();
			if(numberOfElements>0){
				final OutputStream out = new ContinousPagesOutputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
				OutHelper.writeLuposLong(0, out);
				out.close();
				BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
				return numberOfElements;
			}
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * iterator with duplicates of elements referring to the key
	 *
	 * @param key
	 * key
	 * @return
	 * Iterator
	 */
	public Iterator<V> iteratorWithDuplicates(final K key) {

		return new Iterator<V>(){

			private long current = 0;

			private long addressOfNextEntry = 0;

			private long addressOfCurrentEntry = 0;

			private V currentElement = null;

			private long remainingNumberOfElements = 0;

			private final long addressOfKey = PagedHashMultiMap.this.getAddressOfKey(key);

			private final long numberOfKeyElements = PagedHashMultiMap.this.getNumberOfKeyElements(this.addressOfKey);

			long addressOfValues = PagedHashMultiMap.this.getAddressOfValues(this.addressOfKey);

			/* (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {

				return (this.current < this.numberOfKeyElements);
			}

			/* (non-Javadoc)
			 * @see java.util.Iterator#next()
			 */
			@Override
			public V next() {
				if(this.hasNext()){
					if(this.remainingNumberOfElements>0){
						this.current++;
						this.remainingNumberOfElements--;
						return this.currentElement;
					}

					if(this.addressOfNextEntry == 0){
						this.addressOfNextEntry = this.addressOfValues;
					}

					this.addressOfCurrentEntry = this.addressOfNextEntry;
					final Triple<V, Long, Long> entry = PagedHashMultiMap.this.getElement(this.addressOfNextEntry);

					this.currentElement = entry.getFirst();
					this.remainingNumberOfElements = entry.getSecond();
					this.addressOfNextEntry = entry.getThird();

					return this.next();
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() {
				if(this.addressOfCurrentEntry>0){
					if(PagedHashMultiMap.this.decrementNumberOfElements(this.addressOfCurrentEntry)){
						PagedHashMultiMap.this.sizeValues--;
						PagedHashMultiMap.this.decrementNumberOfKeyElements(this.addressOfKey);
						this.current--;
					}
				}
			}
		};
	}

	/**
	 * iterator of keys
	 *
	 * @return
	 * Iterator
	 */
	public Iterator<K> iteratorKeys() {

		return new Iterator<K>(){

			private long current = 0;

			private int address = -8;

			private long addressOfNextEntry = 0;

			private long addressOfCurrentEntry = 0;

			private K currentKey = null;

			private long remainingNumberOfKeys = 0;

			/* (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {
				return (this.current < PagedHashMultiMap.this.sizeKeys);
			}

			/* (non-Javadoc)
			 * @see java.util.Iterator#next()
			 */
			@Override
			public K next() {
				if(this.hasNext()){
					if(this.remainingNumberOfKeys>0){
						this.current++;
						this.remainingNumberOfKeys--;
						return this.currentKey;
					}

					if(this.addressOfNextEntry == 0){

						try {
							final PageAddress pageAddress = new PageAddress(0, PagedHashMultiMap.this.pointersFilename);
							final byte[] page = BufferManager.getBufferManager().getPage(PagedHashMultiMap.this.TABLEPAGESIZE, pageAddress);

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
					final Quadruple<K, Long, Long, Long> entry = PagedHashMultiMap.this.getKey(this.addressOfNextEntry);

					this.currentKey = entry.getFirst();
					this.remainingNumberOfKeys = 1;
					this.addressOfNextEntry = entry.getFourth();

					return this.next();
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() {
				if(this.addressOfCurrentEntry>0){
					final long diff = PagedHashMultiMap.this.setNumberOfKeyElementsTo0(this.addressOfCurrentEntry);
					if(diff>0){
						PagedHashMultiMap.this.sizeValues-=diff;
						this.current-=diff;
					}
				}
			}
		};
	}

	/**
	 * iterator without duplicates of elements referring to a key
	 *
	 * @param key
	 * key
	 * @return
	 * Iterator
	 */
	public Iterator<V> iteratorElements(final K key) {

		return new Iterator<V>(){

			private long current = 0;

			private long addressOfNextEntry = 0;

			private long addressOfCurrentEntry = 0;

			private V currentElement = null;

			private long remainingNumberOfElements = 0;

			private final long addressOfKey = PagedHashMultiMap.this.getAddressOfKey(key);
			private final long numberOfKeyElements = PagedHashMultiMap.this.getNumberOfKeyElements(this.addressOfKey);

			/* (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 */
			@Override
			public boolean hasNext() {
				return (this.current < this.numberOfKeyElements);
			}

			/* (non-Javadoc)
			 * @see java.util.Iterator#next()
			 */
			@Override
			public V next() {
				if(this.hasNext()){
					if(this.remainingNumberOfElements>0){
						this.current+=this.remainingNumberOfElements;
						this.remainingNumberOfElements=0;
						return this.currentElement;
					}

					if(this.addressOfNextEntry == 0){
						final long addressOfValues = PagedHashMultiMap.this.getAddressOfValues(this.addressOfKey);
						this.addressOfNextEntry = addressOfValues;
					}

					this.addressOfCurrentEntry = this.addressOfNextEntry;
					final Triple<V, Long, Long> entry = PagedHashMultiMap.this.getElement(this.addressOfNextEntry);

					this.currentElement = entry.getFirst();
					this.remainingNumberOfElements = entry.getSecond();
					this.addressOfNextEntry = entry.getThird();

					return this.next();
				}
				return null;
			}

			/* (non-Javadoc)
			 * @see java.util.Iterator#remove()
			 */
			@Override
			public void remove() { // remove all entries inclusive duplicates!
				if(this.addressOfCurrentEntry>0){
					final long diff = PagedHashMultiMap.this.setNumberOfElementsTo0(this.addressOfCurrentEntry);
					if(diff>0){
						PagedHashMultiMap.this.sizeValues-=diff;
						final long numberOfKeyElements = PagedHashMultiMap.this.getNumberOfKeyElements(this.addressOfKey);
						PagedHashMultiMap.this.storeNumberOfKeyElements(numberOfKeyElements - diff, this.addressOfKey);
						this.current-=diff;
					}
				}
			}
		};
	}

	/**
	 * @param key
	 * key
	 * @return
	 * address of key
	 */
	public long getAddressOfKey(final K key){
		final ResSet resultKey = this.containKeyGetAddressOfFoundKeyGetAddressOfLastKey(key);
		final boolean containKey = resultKey.containEntry;
		final long addressOfFoundKey = resultKey.addressOfFoundEntry;
		if (containKey){
			return addressOfFoundKey;
		} else {
			return 0;
		}
	}

	/**
	 * @param addressKey
	 * address of key
	 * @return
	 * address of values
	 */
	public long getAddressOfValues(final long addressKey){
		final int pagenumber = (int) (addressKey / PageManager.getDefaultPageSize());
		final int index = (int) (addressKey % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			final long addressOfValues = InputHelper.readLuposLong(in);
			in.close();
			return addressOfValues;
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param addressKey
	 * address of key
	 * @return
	 * number of key elements
	 */
	public long getNumberOfKeyElements(final long addressKey){
		final int pagenumber = (int) (addressKey / PageManager.getDefaultPageSize());
		final int index = (int) (addressKey % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			@SuppressWarnings("unused")
			final long addressOfValues = InputHelper.readLuposLong(in);
			final long numberOfKeyElements = InputHelper.readLuposLong(in);
			in.close();
			return numberOfKeyElements;
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param address
	 * address of key
	 * @return
	 * address of next key
	 */
	@SuppressWarnings("unused")
	private long getAddressOfNextKey(final long address){
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.keysFilename, false, false), index);
			final long addressOfValues = InputHelper.readLuposLong(in);
			final long numberOfKeyElements = InputHelper.readLuposLong(in);
			final long addressOfNextKey = InputHelper.readLuposLong(in);
			in.close();
			return addressOfNextKey;
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param key
	 * key
	 * @param element
	 * element
	 * @return
	 * address of element
	 */
	public long getAddressOfElement(final K key, final V element){
		final ResSet resultKey = this.containKeyGetAddressOfFoundKeyGetAddressOfLastKey(key);
		final boolean containKey = resultKey.containEntry;
		final long addressOfFoundKey = resultKey.addressOfFoundEntry;
		if (!containKey){
			return 0;
		} else {
			final long addressOfValues = this.getAddressOfValues(addressOfFoundKey);
			final ResSet result = this.containElementGetAddressOfFoundElementGetAddressOfLastElement(element, addressOfValues);
			final boolean containElement = result.containEntry;
			final long addressOfElement = result.addressOfFoundEntry;
			if (containElement){
				return addressOfElement;
			} else {
				return 0;
			}
		}
	}

	/**
	 * @param addressElement
	 * addres of element
	 * @return
	 * number of elements
	 */
	private long getNumberOfElements(final long addressElement){
		final int pagenumber = (int) (addressElement / PageManager.getDefaultPageSize());
		final int index = (int) (addressElement % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			final long numberOfElements = InputHelper.readLuposLong(in);
			in.close();
			return numberOfElements;
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param address
	 * address of element
	 * @return
	 * address of next element
	 */
	@SuppressWarnings("unused")
	private long getAddressOfNextElement(final long address){
		final int pagenumber = (int) (address / PageManager.getDefaultPageSize());
		final int index = (int) (address % PageManager.getDefaultPageSize());
		try {
			final InputStream in = new ContinousPagesInputStream(pagenumber, new PageManager(this.valuesFilename, false, false), index);
			final long numberOfElements = InputHelper.readLuposLong(in);
			final long addressOfNextElement = InputHelper.readLuposLong(in);
			in.close();
			return addressOfNextElement;
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return 0;
	}

	// with duplicates
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#size()
	 */
	@Override
	public int size() {
		return (int) this.sizeValues;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		if (this.sizeValues == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return
	 * file ID
	 */
	public int getFileID() {
		return PagedHashMultiMap.fileID;
	}

	/**
	 * this method sts the file ID
	 *
	 * @param fileID
	 * file ID
	 */
	public void setFileID(final int fileID) {
		PagedHashMultiMap.fileID = fileID;
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#clear()
	 */
	@Override
	public void clear() {
		final PageAddress pageAddress = new PageAddress(0, this.pointersFilename);
		try {
			// delete content of pointers page...
			final byte[] page = BufferManager.getBufferManager().getPage(this.TABLEPAGESIZE, pageAddress);
			for(int i=0; i<page.length; i++){
				page[i]=0;
			}
			BufferManager.getBufferManager().modifyPage(this.TABLEPAGESIZE, pageAddress, page);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		this.sizeKeys = 0;
		this.sizeValues = 0;
		this.lastKey = 0;
		this.lastValue = 0;
	}

	/**
	 * this method releases all
	 *
	 * @throws IOException
	 */
	public void release() throws IOException {
		BufferManager.getBufferManager().releaseAllPages(this.pointersFilename);
		BufferManager.getBufferManager().close(this.pointersFilename);
	}

	/**
	 * String presentation without duplicates of PagedHashMultiMap
	 *
	 * @return
	 * String of map
	 */
	public String toStringWithoutDuplicates() {
		String result = "Paged Hash Map: { ";
		final Iterator<K> itkeys = this.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				result+=key;
				result+=" = [ ";
			}
			boolean firstTimeElements = true;
			final Iterator<V> itelements = this.iteratorElements(key);
			while(itelements.hasNext()){
				final V entry = itelements.next();
				if(firstTimeElements){
					firstTimeElements = false;
				} else {
					result+=", ";
				}
				result+=entry;
			}
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				result+=" ] ";
			}
		}
		return result+" }";
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#toString()
	 */
	@ Override
	public String toString(){
		String result = "Paged Hash Map: { ";
		final Iterator<K> itkeys = this.iteratorKeys();
		while(itkeys.hasNext()){
			final K key = itkeys.next();
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				result+=key;
				result+=" = [ ";
			}
			boolean firstTimeElements = true;
			final Iterator<V> itelements = this.iteratorWithDuplicates(key);
			while(itelements.hasNext()){
				final V entry = itelements.next();
				if(firstTimeElements){
					firstTimeElements = false;
				} else {
					result+=", ";
				}
				result+=entry;
			}
			if (!(this.getKey(this.getAddressOfKey(key)).getThird() == 0)){
				result+=" ] ";
			}
		}
		return result+" }";
	}

	/**
	 * this method tests the implementation
	 *
	 * @param args
	 * console arguments
	 */
	public static void main(final String[] args) throws IOException {
		final PagedHashMultiMap<String, String> m = new PagedHashMultiMap<String, String>(String.class, String.class);
		System.out.println("is empty: " + m.isEmpty());
		m.put("alfons", "Alf");
		m.put("alfons", "A");
		m.put("alfons", "Alf");
		m.put("boris", "Bertha");
		m.put("boris", "Bertha");
		m.put("boris", "Brit");
		m.put("boris", "Beate");
		m.put("c", "C");
		m.put("d", "D");
		m.put("f", "F");
		m.put("f", "Fred");
		m.put("f", "Ferdi");
		m.put("f", "Fill");
		m.put("f", "Fred");
		System.out.println(m);
		System.out.println("size: " + m.size());
		System.out.println("file ID: " + m.getFileID());
		System.out.println("is empty: " + m.isEmpty());
		System.out.println("without duplicates: " + m.toStringWithoutDuplicates());
		System.out.println("entry set: "  + m.entrySet());
		System.out.println("entry list: "  + m.entryList());
		System.out.println("number of elements for key \"boris\": " + m.getNumberOfKeyElements(m.getAddressOfKey("boris")));
		m.remove("c", "C");
		m.remove("boris", "Beate");
		m.remove("boris", "Brit");
		m.remove("z", "Z");
		System.out.println("remove (c,C), (boris,Beate), (boris,Brit), (z,Z):");
		System.out.println(m);
		System.out.println("entry set: "  + m.entrySet());
		System.out.println("key set: "  + m.keySet());
		System.out.println("values: "  + m.values());
		System.out.println("values list for key \"f\": "  + m.getValuesList("f"));
		System.out.println("number of elements for key \"boris\": " + m.getNumberOfKeyElements(m.getAddressOfKey("boris")));
		System.out.println("without duplicates: " + m.toStringWithoutDuplicates());
		m.removeAllDuplicates("alfons", "Alf");
		m.removeAllDuplicates("f", "Fred");
		m.removeAllDuplicates("t", "T");
		System.out.println("remove all duplicates of (alfons,Alf), (f,Fred), (t,T):");
		System.out.println(m);
		m.put("e", "Emil");
		m.put("alfons", "Alberta");
		System.out.println("put (e,Emil), (alfons,Alberta):");
		System.out.println(m);
		System.out.println("size: " + m.size());
		// test keys with same hashCode % 1024
		m.put("a", "B");
		m.put("1002", "B");
		m.put("1123", "B");
		System.out.println("put (a,B), (1002,B) (1123,B) with same hashCode % 1024:");
		System.out.println("a\t hashCode % 1024 :\t"  + (Math.abs("a".hashCode()) % 1024));
		System.out.println("1002\t hashCode % 1024 :\t" + (Math.abs("1002".hashCode()) % 1024));
		System.out.println("1123\t hashCode % 1024 :\t" + (Math.abs("1123".hashCode()) % 1024));
		System.out.println(m);
		System.out.println("size: " + m.size());
		// test remove(key)
		m.remove("d");
		System.out.println("remove(d): " + m);
		m.remove("a");
		System.out.println("remove(a): " + m);
		m.remove("1123");
		System.out.println("remove(1123): " + m);
		m.put("a", "B");
		m.put("1123", "B");
		System.out.println("put (a,B), (1123,B) with same hashCode % 1024 (to test remove(key)):");
		System.out.println(m);
		m.remove("a");
		System.out.println("remove(a): " + m);
		System.out.println("size: " + m.size());
		// test method getCollection (class GetCollection)
		System.out.println("test getValuesList(\"alfons\"): " + m.getValuesList("alfons"));
		System.out.println("test getCollection(\"alfons\"): " + m.getCollection("alfons"));
		m.getCollection("alfons").add("Anton");
		System.out.println("getCollection(\"alfons\").add(\"Anton\"): \n" + m);
		m.getCollection("f").clear();
		System.out.println("getCollection(\"f\").clear(): \n" + m);
		System.out.println("getCollection(\"boris\").contains(\"A\"): " + m.getCollection("boris").contains("A"));
		System.out.println("getCollection(\"boris\").contains(\"Bertha\"): " + m.getCollection("boris").contains("Bertha"));
		System.out.println("getCollection(\"f\").isEmpty(): " + m.getCollection("f").isEmpty());
		m.getCollection("boris").remove("B");
		System.out.println("getCollection(\"boris\").remove(\"B\"): \n" + m);
		m.clear();
		System.out.println("clear: " + m);
		BufferManager.getBufferManager().writeAllModifiedPages();
	}
}
