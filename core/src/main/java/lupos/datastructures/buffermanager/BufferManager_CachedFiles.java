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
package lupos.datastructures.buffermanager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import lupos.misc.Quadruple;

/**
 * This class implements the methods for the buffer manager to cache and reuse some files.
 * It provides the LRU caching methods to be used also in BufferManager_RandomAccess for caching pages.
 */
public abstract class BufferManager_CachedFiles extends BufferManager {

	/**
	 * the max. number of bytes stored in a file, which can be handled by java without problems
	 */
	protected static int JAVALIMITFILESIZE_IN_BYTES = 1024 * 1024 * 1024;

	/**
	 * the max. number of opened files
	 */
	private static int MAXOPENEDFILES = 10;

	public static interface REPLACEMENTSTRATEGY<T> {
		/**
		 * this method is called whenever an item is accessed
		 *
		 * @param address
		 *            the accessed item
		 */
		public void accessNow(T address);

		/**
		 * This method is called whenever the buffer manager is full. This
		 * method returns the item to be replaced with another one. The returned
		 * item should be deleted from all records in the replacement strategy.
		 *
		 * @return the item to be removed from the buffer manager to free main
		 *         memory
		 */
		public T getToBeReplaced();

		/**
		 * This method marks an item to be released, i.e., its content does not
		 * need to be stored on disk and the item can be deleted from the
		 * internal data structures of the replacement strategy.
		 */
		public void release(T address);

		/**
		 * This method marks all items to be released, i.e.,
		 * their contents do not need to be stored on disk and all
		 * the items can be deleted from the internal data structures of
		 * the replacement strategy.
		 */
		public void releaseAll();
	}

	/**
	 * This replacement strategy returns the number of the least recently used
	 * item if the buffer is full.
	 */
	public static class LeastRecentlyUsed<T> implements REPLACEMENTSTRATEGY<T> {

		/**
		 * This is one element of the doubly linked list.
		 * The list should have a dummy element, which succeeding element is the head of the list
		 * and its previous element is the tail of the list.
		 * The empty list contains a dummy element referring to itself.
		 * The dummy element avoids checking many special cases when inserting and removing elements...
		 *
		 * @param <T> the type of the keys to be stored...
		 */
		public final static class Pointers<T> {
			private final T key;
			private Pointers<T> before;
			private Pointers<T> after;

			/**
			 * This constructor is just for constructing the dummy element
			 */
			public Pointers(){
				this.key = null;
				// just for the dummy:
				// refer to itself as succeeding/preceding element
				this.before = this;
				this.after = this;
			}

			/**
			 * Constructs one element in the doubly linked list and inserts this element after the given other element (in our use case before is the dummy).
			 *
			 * @param key the key to be stored
			 * @param before the element after which the newly created element is stored
			 */
			public Pointers(final T key, final Pointers<T> before){
				this.key = key;
				this.insertAfter(before);
			}

			/**
			 * removes the current element from the doubly linked list
			 */
			public final void remove(){
				this.before.after = this.after;
				this.after.before = this.before;
			}

			/**
			 * @return the stored key
			 */
			public final T getKey(){
				return this.key;
			}

			/**
			 * inserts this element after the given element
			 * @param before the element after which this element is inserted
			 */
			public final void insertAfter(final Pointers<T> before){
				this.before = before;
				this.after = before.after;
				before.after = this;
				this.after.before = this;
			}

			/**
			 * initializes this element to be the dummy element:
			 * it just points to itself...
			 */
			public final void initDummy(){
				// just for the dummy:
				// refer to itself as succeeding/preceding element
				this.before = this;
				this.after = this;
			}
		}

		private final HashMap<T, Pointers<T>> entries = new HashMap<T, Pointers<T>>();
		final Pointers<T> dummy = new Pointers<T>();

		@Override
		public void accessNow(final T address) {
			final Pointers<T> pointers = this.entries.get(address);
			if(pointers!=null){
				pointers.remove();
				pointers.insertAfter(this.dummy);
			} else {
				this.entries.put(address, new Pointers<T>(address, this.dummy));
			}
		}

		@Override
		public T getToBeReplaced() {
			final Pointers<T> leastRecentlyUsed = this.dummy.before;
			leastRecentlyUsed.remove();
			final T key = leastRecentlyUsed.getKey();
			this.entries.remove(key);
			return key;
		}

		@Override
		public void release(final T address) {
			final Pointers<T> pointers = this.entries.get(address);
			if(pointers!=null){
				pointers.remove();
				this.entries.remove(address);
			}
		}

		@Override
		public void releaseAll(){
			this.entries.clear();
			this.dummy.initDummy();
		}
	}

	/**
	 * The lock used for any operation on a page
	 */
	protected static ReentrantLock lock = new ReentrantLock();

	/**
	 * the used replacement strategy for opened files
	 */
	private REPLACEMENTSTRATEGY<String> replacementStrategyOpenedFiles;

	/**
	 * The buffered opened files
	 */
	private final Map<String, RandomAccessFile> bufferedFiles = new HashMap<String, RandomAccessFile>();

	/**
	 * The constructor...
	 */
	protected BufferManager_CachedFiles(){
		this.replacementStrategyOpenedFiles = new LeastRecentlyUsed<String>();
	}

	/**
	 * Determines the filename, the file the page of which is addressed, the offset in the file, as well as eventually the filename of the file to be closed due to a full buffer...
	 * This method also deals with buffering all the files...
	 *
	 * @param pagesize the size of a page
	 * @param pageaddress
	 *            The page address to be accessed afterwards...
	 * @return a quadruple of the determined filename, file and offset, as well as eventually the filename of the file to be closed due to a full buffer
	 * @throws IOException
	 */
	protected final Quadruple<String, RandomAccessFile, Integer, String> getFile(final int pagesize, final PageAddress pageaddress) throws IOException{
		final int javalimitfilesize = (BufferManager_CachedFiles.JAVALIMITFILESIZE_IN_BYTES / pagesize);
		final int newFile = pageaddress.pagenumber / javalimitfilesize;
		final String newFilename = pageaddress.filename + "_" + newFile;
		RandomAccessFile file = this.bufferedFiles.get(newFilename);
		String filenameToBeClosed = null;
		if(file==null){
			if(this.bufferedFiles.size()>=MAXOPENEDFILES){
				filenameToBeClosed = this.replacementStrategyOpenedFiles.getToBeReplaced();
				final RandomAccessFile oldFile=this.bufferedFiles.remove(filenameToBeClosed);
				oldFile.close();
			}
			file = new RandomAccessFile(new File(newFilename), "rw");
			this.bufferedFiles.put(newFilename, file);
		}
		this.replacementStrategyOpenedFiles.accessNow(newFilename);
		return new Quadruple<String, RandomAccessFile, Integer, String>(newFilename, file, (pageaddress.pagenumber - newFile * javalimitfilesize) * pagesize, filenameToBeClosed);
	}

	/**
	 * @return The used replacement strategy
	 */
	public REPLACEMENTSTRATEGY<String> getReplacementStrategyOpenedFiles() {
		return this.replacementStrategyOpenedFiles;
	}

	/**
	 * This method should be called only if the default replacement strategy is not used and it should be called before the BufferManager is used the first time.
	 * @param replacementStrategy the replacement strategy to be used
	 */
	public void setReplacementStrategyOpenedFiles(final REPLACEMENTSTRATEGY<String> replacementStrategyOpenedFiles) {
		this.replacementStrategyOpenedFiles = replacementStrategyOpenedFiles;
	}

	/**
	 * @return the max number of opened files in the buffer manager
	 */
	public static int getMaxOpenedFiles() {
		return MAXOPENEDFILES;
	}

	/**
	 * @param maxOpenedFiles the max opened number of files in the buffer manager
	 */
	public static void setMaxOpenedFiles(final int maxOpenedFiles) {
		MAXOPENEDFILES = maxOpenedFiles;
	}

	/**
	 * This method closes the underlying files. This method should only be called
	 * if the buffer manager is not used any more...
	 */
	@Override
	public void close() throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			for(final RandomAccessFile file: this.bufferedFiles.values()){
				file.close();
			}
			this.bufferedFiles.clear();
			this.replacementStrategyOpenedFiles.releaseAll();
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * This method closes the underlying files of basis filename.
	 * @param filename the filename of the basis file
	 */
	@Override
	public void close(final String filename) throws IOException {
		final String filenamePrefix = filename + "_";
		BufferManager_CachedFiles.lock.lock();
		try {
			final LinkedList<String> files = new LinkedList<String>();
			for(final Entry<String, RandomAccessFile> entry: this.bufferedFiles.entrySet()){
				if(entry.getKey().startsWith(filenamePrefix)){
					files.add(entry.getKey());
					entry.getValue().close();
				}
			}
			for(final String file: files){
				this.bufferedFiles.remove(file);
				this.replacementStrategyOpenedFiles.release(file);
			}
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * This method releases all pages, deletes the buffered file from disk and starts with a new file,
	 * i.e. all content of the buffered file is deleted.
	 * @param filename the filename of the basis file
	 * @throws IOException
	 */
	@Override
	public void reset(final String filename) throws IOException {
		this.releaseAllPages(filename);
		this.close(filename);
		final int i=0;
		boolean flag = true;
		do {
			final File file = new File(filename + "_" + i);
			if(file.exists()){
				file.delete();
			} else {
				flag = false;
			}
		} while(flag);
	}
}
