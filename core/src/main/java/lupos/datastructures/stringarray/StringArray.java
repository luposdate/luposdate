/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.datastructures.stringarray;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.buffermanager.BufferManager.PageAddress;
import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.items.literal.codemap.IntegerStringMap;
import lupos.datastructures.items.literal.codemap.TProcedureEntry;
import lupos.datastructures.items.literal.codemap.TProcedureValue;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.misc.FileHelper;

/**
 * This class implements an array of strings on disk.
 * Its functionality can be also described as an only-appendable disk-based map from integer to string.
 * This class is used as part of the dictionary for the mapping from an integer number to a string.
 * 
 * It uses two files: In the first file pointers to the start position of strings are stored.
 * In the second file the actual strings are stored.
 * 
 * Strings can be only added to this array in the end.
 * We do not support any functionality to update or remove strings from the array.
 * 
 * The first four bytes in the pointers file stores the maximum index for the strings,
 * and the first 8 bytes in the strings file stores the maximum position in the strings file. 
 */
public class StringArray implements Iterable<Entry<Integer, String>>, IntegerStringMap{
	
	// the maximum index
	private long max = 0;
	// the next free byte in the strings file
	private long lastString = 8;
	
	// the filename of the pointers file
	private final String pointersFilename;
	// the filename of the strings file
	private final String stringsFilename;
	
	// the current id, important, if several string arrays are instantiated
	// (each instance gets another id and therefore stores its content into different files)
	private static int fileID=0;
	
	// the lock for getting a new id
	protected static ReentrantLock lock = new ReentrantLock();
	
	public StringArray() {
		StringArray.lock.lock();
		try{
			final int currentID = fileID++;
			// use directory of DBBPTree for storing string arrays on disk!
			final String mainFolder = DBBPTree.getMainFolder();
			final File f = new File(mainFolder);
			f.mkdirs();
			if(currentID==0){
				// only the first instance removes old pointers and strings files from disk!
				FileHelper.deleteFilesStartingWithPattern(mainFolder,currentID+ ".pointers_");
				FileHelper.deleteFilesStartingWithPattern(mainFolder,currentID+ ".strings_");
			}			
			this.pointersFilename = mainFolder + currentID + ".pointers";
			this.stringsFilename = mainFolder + currentID + ".strings";
		} finally {
			StringArray.lock.unlock();
		}
	}

	/**
	 * Adds a string to this map
	 * @param string the string to be added
	 * @throws IOException in case of any i/o failures
	 */
	public void add(final String string) throws IOException {
		
		// first store position of string in pointers file...
		
		// the first four bytes store the maximum position in this file!
		final long posInFile = 4 + this.max*8;
				
		StringArray.storeLongInPage(this.pointersFilename, posInFile, this.lastString);
		
		// update max also in pointers file...
		
		this.max++;
		
		StringArray.storeIntInPage(this.pointersFilename, 0, this.max);
		
		// now store the string...
		this.lastString = StringArray.storeStringInPage(this.stringsFilename, this.lastString, string);
		
		// and update the position into which the last string is stored!
		
		StringArray.storeLongInPage(this.stringsFilename, 0, this.lastString);
	}
	
	/**
	 * Retrieves a string
	 * @param index the index of the string
	 * @return the string
	 * @throws IOException in case of any i/o failures...
	 */
	@Override
	public String get(final int index){
		// the index ranges from 1 to max, as 0 is an error code in the dictionary
		if(index>this.max || index<=0){
			return null;
		}
		// the first four bytes store the maximum position in this file!
		final long posInFile = 4 + (index-1)*8;
		try {
			long posOfString = StringArray.getLongFromPage(this.pointersFilename, posInFile);
	
			return StringArray.getStringFromPage(this.stringsFilename, posOfString);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Stores an integer into a file
	 * @param fileName the file into which is stored
	 * @param posInFile the position in the file
	 * @param value the integer which is stored into the file
	 * @throws IOException in case of any i/o failure!
	 */
	private final static void storeIntInPage(final String fileName, final long posInFile, final long value) throws IOException{
		final int defaultPageSize = PageManager.getDefaultPageSize();
		final int pageNumber = (int) (posInFile / defaultPageSize);		
		PageAddress pageAddress = new PageAddress(pageNumber, fileName);
		byte[] page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);		
		int posInPage = (int) (posInFile % defaultPageSize);
		long toBeStored = value;
		if(posInPage+3 >= defaultPageSize){
			// the integer must be stored over two pages!
			for(int i=0; i<4; i++){
				if(posInPage>=defaultPageSize){
					// next page is affected!
					BufferManager.getBufferManager().modifyPage(defaultPageSize, pageAddress, page);
					pageAddress = new PageAddress(pageAddress.pagenumber+1, pageAddress.filename);
					page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);
					posInPage %= defaultPageSize;
				}
				page[posInPage] = (byte)((toBeStored % 256)-128);
				toBeStored/=256;
				posInPage++;		
			}
		} else {
			// optimize the normal case!
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);			
		}
		
		BufferManager.getBufferManager().modifyPage(defaultPageSize, pageAddress, page);
	}
	
	/**
	 * Retrieves an integer from disk 
	 * @param fileName the file in which the integer is stored
	 * @param posInFile the position in the file in which the integer is stored
	 * @return the integer stored at posInFile in file fileName
	 * @throws IOException in case of any i/o failures...
	 */
	private final static long getIntFromPage(final String fileName, final long posInFile) throws IOException{
		final int defaultPageSize = PageManager.getDefaultPageSize();
		final int pageNumber = (int) (posInFile / defaultPageSize);		
		PageAddress pageAddress = new PageAddress(pageNumber, fileName);
		byte[] page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);		
		int posInPage = (int) (posInFile % defaultPageSize);

		if(posInPage+3>=defaultPageSize){
			// the integer is stored over two pages!
			long result = 0;
			long factor = 1;
			for(int i=0; i<4; i++){
				if(posInPage>=defaultPageSize){
					// next page is affected!
					pageAddress = new PageAddress(pageAddress.pagenumber+1, pageAddress.filename);
					page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);
					posInPage %= defaultPageSize;
				}
				result += (page[posInPage] + 128) * factor;
				factor *= 256;
				posInPage++;
			}
			return result;
		} else {
			// optimize the normal case!
			return (page[posInPage]+128) + 256 * ( (page[posInPage+1]+128) + 256 * ( (page[posInPage+2]+128) + 256 * (page[posInPage+3]+128)));
		}
	}
	
	/**
	 * Stores a long into a file
	 * @param fileName the file into which is stored
	 * @param posInFile the position in the file
	 * @param value the long which is stored into the file
	 * @throws IOException in case of any i/o failure!
	 */
	private final static void storeLongInPage(final String fileName, final long posInFile, final long value) throws IOException{
		final int defaultPageSize = PageManager.getDefaultPageSize();
		final int pageNumber = (int) (posInFile / defaultPageSize);		
		PageAddress pageAddress = new PageAddress(pageNumber, fileName);
		byte[] page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);		
		int posInPage = (int) (posInFile % defaultPageSize);
		long toBeStored = value;
		if(posInPage+7 >= defaultPageSize){
			// the long must be stored over two pages!
			for(int i=0; i<8; i++){
				if(posInPage>=defaultPageSize){
					// next page is affected!
					BufferManager.getBufferManager().modifyPage(defaultPageSize, pageAddress, page);
					pageAddress = new PageAddress(pageAddress.pagenumber+1, pageAddress.filename);
					page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);
					posInPage %= defaultPageSize;
				}
				page[posInPage] = (byte)((toBeStored % 256)-128);
				toBeStored/=256;
				posInPage++;		
			}
		} else {
			// optimize the normal case!
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
			toBeStored/=256;
			posInPage++;		
			page[posInPage] = (byte)((toBeStored % 256)-128);
		}
		
		BufferManager.getBufferManager().modifyPage(defaultPageSize, pageAddress, page);
	}
	
	/**
	 * Retrieves a long from disk 
	 * @param fileName the file in which the long is stored
	 * @param posInFile the position in the file in which the long is stored
	 * @return the long stored at posInFile in file fileName
	 * @throws IOException in case of any i/o failures...
	 */
	private final static long getLongFromPage(final String fileName, final long posInFile) throws IOException{
		final int defaultPageSize = PageManager.getDefaultPageSize();
		final int pageNumber = (int) (posInFile / defaultPageSize);		
		PageAddress pageAddress = new PageAddress(pageNumber, fileName);
		byte[] page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);		
		int posInPage = (int) (posInFile % defaultPageSize);

		if(posInPage+7>=defaultPageSize){
			// the long is stored over two pages!
			long result = 0;
			long factor = 1;
			for(int i=0; i<8; i++){
				if(posInPage>=defaultPageSize){
					// next page is affected!
					pageAddress = new PageAddress(pageAddress.pagenumber+1, pageAddress.filename);
					page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);
					posInPage %= defaultPageSize;
				}
				result += (page[posInPage] + 128L) * factor;
				factor *= 256;
				posInPage++;
			}
			return result;
		} else {
			// optimize the normal case!
			return ((page[posInPage])+128L) + 
					256L * ( (page[posInPage+1]+128L) + 
					256L * ( (page[posInPage+2]+128L) + 
					256L * ( (page[posInPage+3]+128L) +
					256L * ( (page[posInPage+4]+128L) +
					256L * ( (page[posInPage+5]+128L) +
					256L * ( (page[posInPage+6]+128L) +
					256L * ( (page[posInPage+7]+128L)
				)))))));
		}
	}
	
	/**
	 * Stores a string into the file...
	 * @param fileName the file in which the string is stored
	 * @param posInFile the position in the file!
	 * @param value the string to be stored
	 * @return the new position for storing the next string!
	 * @throws IOException in case of any i/o failure!
	 */
	private final static long storeStringInPage(final String fileName, final long posInFile, final String value) throws IOException {
		// store length of string...
		byte[] bytesOfValue = value.getBytes(LuposObjectInputStream.UTF8);
		final int length = bytesOfValue.length;
		StringArray.storeIntInPage(fileName, posInFile, length);

		// store bytes of string...
		final long startString = posInFile + 4;
		final int defaultPageSize = PageManager.getDefaultPageSize();
		final int pageNumber = (int) (startString / defaultPageSize);		
		PageAddress pageAddress = new PageAddress(pageNumber, fileName);
		byte[] page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);		
		int posInPage = (int) (startString % defaultPageSize);

		// Is the string stored over several pages?
		if(posInPage+length>=defaultPageSize){
			for(byte b: bytesOfValue){
				page[posInPage] = b;
				posInPage++;
				if(posInPage>=defaultPageSize){
					// next page is affected!
					BufferManager.getBufferManager().modifyPage(defaultPageSize, pageAddress, page);
					pageAddress = new PageAddress(pageAddress.pagenumber+1, pageAddress.filename);
					page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);
					posInPage %= defaultPageSize;
				}			
			}
		} else {
			// just doing the normal case faster!
			System.arraycopy(bytesOfValue, 0, page, posInPage, length);
		}
		BufferManager.getBufferManager().modifyPage(defaultPageSize, pageAddress, page);
		return posInFile + 4 + length; 
	}
	
	/**
	 * Retrieves a string from a file at a certain position
	 * @param fileName the file in which the string is stored
	 * @param posInFile the position in the file in which the string is stored
	 * @return a string stored at posInFile in the file fileName
	 * @throws IOException in case of any i/o failures
	 */
	private final static String getStringFromPage(final String fileName, final long posInFile) throws IOException {
		// get the length of the string
		final int length = (int) StringArray.getIntFromPage(fileName, posInFile);
		// get the string as byte array
		final long startOfString = posInFile + 4;
		final int defaultPageSize = PageManager.getDefaultPageSize();
		final int pageNumber = (int) (startOfString / defaultPageSize);		
		PageAddress pageAddress = new PageAddress(pageNumber, fileName);
		byte[] page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);		
		int posInPage = (int) (startOfString % defaultPageSize);

		final byte[] result = new byte[length];

		if(posInPage+length>=defaultPageSize){
			// the string is stored over two pages!
			for(int i=0; i<length; i++){
				if(posInPage>=defaultPageSize){
					// next page is affected!
					pageAddress = new PageAddress(pageAddress.pagenumber+1, pageAddress.filename);
					page = BufferManager.getBufferManager().getPage(defaultPageSize, pageAddress);
					posInPage %= defaultPageSize;
				}				
				result[i] = page[posInPage];
				posInPage++;
			}
		} else {
			// optimize the standard case!
			System.arraycopy(page, posInPage, result, 0, length);
		}
		// create resultant string!
		return new String(result, LuposObjectInputStream.UTF8);
	}
	
	@Override
	public Iterator<Entry<Integer, String>> iterator() {
		return new Iterator<Entry<Integer, String>>(){
			
			private int current = 0;

			@Override
			public boolean hasNext() {
				// the index ranges from 1 to max, as 0 is an error code in the dictionary
				return (this.current<StringArray.this.max);
			}

			@Override
			public Entry<Integer, String> next() {
				if(hasNext()){
					// start with index=1
					this.current++;
					return new MapEntry<Integer, String>(this.current, StringArray.this.get(this.current));
				}
				return null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
	
	@Override
	public String toString(){
		String result = "(Only appendable disk-based) Map from Integer to String: {";
		boolean firstTime = true;
		for(Entry<Integer, String> entry: this){
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
	public void put(int key, String value) {
		if(key!=this.max+1){
			// the typical use in a dictionary should follow this condition!
			throw new RuntimeException("It is only allowed to put under the key " + (this.max+1) + ", but it is tried to put under the key " + key + "!");
		} else {
			try {
				this.add(value);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void clear() {
		try {
			this.max=0;
			StringArray.storeIntInPage(this.pointersFilename, 0, this.max);
			this.lastString = 8;
			StringArray.storeLongInPage(this.stringsFilename, 0, this.lastString);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public int size() {
		return (int) this.max;
	}

	@Override
	public void forEachValue(TProcedureValue<String> arg0) {
		for(Entry<Integer, String> entry: this){
			arg0.execute(entry.getValue());
		}
	}

	@Override
	public void forEachEntry(TProcedureEntry<Integer, String> arg0) {
		for(Entry<Integer, String> entry: this){
			arg0.execute(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * This method creates the string array on disk by storing the string given by an iterator.
	 * It is optimized to store many strings and is therefore faster than just using the methods add or put for each string...
	 * @param valuesIterator an iterator for the strings to be stored in this string array
	 * @throws IOException in case of any i/o failures
	 */
	public void generate(final Iterator<String> valuesIterator) throws IOException{
		if(this.max!=0){
			this.clear();
		}
		// the first four bytes store the maximum position in this file!
		long posInFile = 4;
		
		while(valuesIterator.hasNext()){
			// first store position of string in pointers file...

			StringArray.storeLongInPage(this.pointersFilename, posInFile, this.lastString);
			
			posInFile += 8;
			
			this.max++;
			
			// now store the string...
			this.lastString = StringArray.storeStringInPage(this.stringsFilename, this.lastString, valuesIterator.next());
			
		}
		// update max also in pointers file...

		StringArray.storeIntInPage(this.pointersFilename, 0, this.max);
		// and update the position into which the last string is stored!

		StringArray.storeLongInPage(this.stringsFilename, 0, this.lastString);
	}
	
	protected StringArray(final String pointersFilename, final String stringsFilename) throws IOException{
		this.pointersFilename = pointersFilename;
		this.stringsFilename = stringsFilename;
		this.max = getIntFromPage(this.pointersFilename, 0);
		this.lastString = getLongFromPage(this.stringsFilename, 0);
	}
	
	@SuppressWarnings("rawtypes")
	public static StringArray readLuposStringArray(final LuposObjectInputStream lois) throws IOException{
		final String pointersFilename = lois.readLuposString();
		final String stringsFilename = lois.readLuposString();
		return new StringArray(pointersFilename, stringsFilename);
	}
	
	public void writeLuposStringArray(final LuposObjectOutputStream loos) throws IOException{
		BufferManager.getBufferManager().writeAllModifiedPages();
		loos.writeLuposString(this.pointersFilename);
		loos.writeLuposString(this.stringsFilename);
	}
	
	public static int getFileID() {
		return StringArray.fileID;
	}

	public static void setFileID(int fileID) {
		StringArray.fileID = fileID;
	}

	/**
	 * just to quickly test the implementation... 
	 */
	public static void main(final String[] args) throws IOException{
		StringArray d = new StringArray();
		d.add("hallo");
		d.add("hello");
		d.add("3");
		d.add("4");
		System.out.println(d);
		d.put(d.size()+1, "5");
		System.out.println(d);
		StringArray d2 = new StringArray();
		d2.generate(Arrays.asList("a", "b", "c").iterator());
		System.out.println(d2);
		BufferManager.getBufferManager().writeAllModifiedPages();
	}
}
