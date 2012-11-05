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
package lupos.datastructures.buffermanager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

public class BufferManager {

	public interface REPLACEMENTSTRATEGY<T> {
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
	public class LeastRecentlyUsed<T> implements REPLACEMENTSTRATEGY<T> {
		protected HashMap<T, Long> timestamps = new HashMap<T, Long>();
		
		private long currentTime = 0;

		@Override
		public void accessNow(final T address) {
			this.timestamps.put(address, this.currentTime++);
		}

		@Override
		public T getToBeReplaced() {
			Entry<T, Long> min = null;
			for (final Entry<T, Long> entry : this.timestamps.entrySet()) {
				if (min == null || entry.getValue() < min.getValue())
					min = entry;
			}
			if(min!=null){
				final T key = min.getKey();
				this.timestamps.remove(key);
				return key;
			} else {
				// return error code...
				return null;
			}
		}

		@Override
		public void release(final T address) {
			this.timestamps.remove(address);
		}
		
		@Override
		public void releaseAll(){
			this.timestamps.clear();
		}		
	}
	
	/**
	 * This class stores all the information necessary to access a page (pagenumber plus filename of basis file)... 
	 */
	public static class PageAddress{
		public int pagenumber;
		public String filename;
		
		public PageAddress(final int pagenumber, final String filename){
			this.pagenumber = pagenumber;
			this.filename = filename;
		}
		
		@Override
		public boolean equals(Object o){
			if(o instanceof PageAddress){
				PageAddress other = (PageAddress)o;
				return other.pagenumber == this.pagenumber && other.filename.compareTo(this.filename)==0;
			} else {
				return false;
			}			
		}
		
		@Override
		public int hashCode(){
			return (int)((long)this.pagenumber + this.filename.hashCode());
		}
		
		@Override
		public String toString(){
			return this.pagenumber + " in " + this.filename;
		}
	}
	
	/**
	 * This class contains the content of a single page plus a flag for storing whether or not the page has been modified... 
	 */
	public static class Page {
		public byte[] page;
		public boolean modified;
		public PageAddress pageaddress;
		public final int pagesize;

		public Page(final int pagesize, final PageAddress pageaddress, final byte[] page) {
			this(pagesize, pageaddress, page, false);
		}

		public Page(final int pagesize, final PageAddress pageaddress, final byte[] page, final boolean modified) {
			this.page = page;
			this.modified = modified;
			this.pageaddress = pageaddress;
			this.pagesize = pagesize;
		}
		
		@Override
		public String toString(){
			StringBuilder sb=new StringBuilder();
			sb.append('(');
			sb.append(this.pageaddress.toString());
			sb.append(", Size: ");
			sb.append(this.pagesize);
			sb.append(')');
			if(this.modified){
				sb.append('m');
			}
			sb.append('[');
			sb.append((this.page[0]+128));
			for(int i=1;i<this.page.length;i++){
				sb.append(',');
				sb.append((this.page[i]+128));
			}
			sb.append(']');
			return sb.toString();
		}
	}
	
	/**
	 * The max. number of bytes in the buffer
	 */
	protected static int MAXBYTESINBUFFER = 100 * 8 * 1024;

	/**
	 * the max. number of bytes stored in a file, which can be handled by java without problems
	 */
	protected static int JAVALIMITFILESIZE_IN_BYTES = 1024 * 1024 * 1024;
			
	/**
	 * the max. number of opened files
	 */
	protected static int MAXOPENEDFILES = 10;
	
	/**
	 * BufferManager is a singleton!
	 */
	private static BufferManager bufferManager = null;
	
	/**
	 * the only way to get the singleton buffer manager
	 * @return the singleton buffer manager
	 */
	public static BufferManager getBufferManager(){
		lock.lock();
		try {
			if(BufferManager.bufferManager==null){
				bufferManager = new BufferManager();
			}
			return bufferManager;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * the used replacement strategy for pages in the buffer, if new pages must be loaded which do not fit any more into the buffer
	 */
	protected REPLACEMENTSTRATEGY<PageAddress> replacementStrategy;

	/**
	 * the used replacement strategy for opened files
	 */
	protected REPLACEMENTSTRATEGY<String> replacementStrategyOpenedFiles;
	
	/**
	 * The buffered pages
	 */
	protected Map<PageAddress, Page> bufferedPages = new HashMap<PageAddress, Page>();
	
	/**
	 * The buffered opened files
	 */
	protected Map<String, RandomAccessFile> bufferedFiles = new HashMap<String, RandomAccessFile>();
	
	/**
	 * The lock used for any operation on a page
	 */
	protected static ReentrantLock lock = new ReentrantLock();
	
	/**
	 * the current number of bytes in the buffer
	 */
	protected int currentNumberOfBytesInPuffer = 0;	
	
	/**
	 * the private constructor
	 * @see getBufferManager()
	 */
	private BufferManager() {
		this.replacementStrategy = new LeastRecentlyUsed<PageAddress>();
		this.replacementStrategyOpenedFiles = new LeastRecentlyUsed<String>();
	}

	/**
	 * This method sets the offset of the file returned to the beginning of a page
	 * @param pagesize the size of a page
	 * @param pageaddress
	 *            The page address to be accessed afterwards...
	 * 
	 * @throws IOException
	 */
	private RandomAccessFile jumpToPage(final int pagesize, final PageAddress pageaddress) throws IOException {
		final int javalimitfilesize = (BufferManager.JAVALIMITFILESIZE_IN_BYTES / pagesize); 
		final int newFile = pageaddress.pagenumber / javalimitfilesize;
		final String newFilename = pageaddress.filename + "_" + newFile;
		RandomAccessFile file = this.bufferedFiles.get(newFilename);
		if(file==null){
			if(this.bufferedFiles.size()>=MAXOPENEDFILES){
				String filenameToBeClosed = this.replacementStrategyOpenedFiles.getToBeReplaced();
				RandomAccessFile oldFile=this.bufferedFiles.remove(filenameToBeClosed);
				oldFile.close();
			}
			file = new RandomAccessFile(new File(newFilename), "rw");
			this.bufferedFiles.put(newFilename, file);
		}
		this.replacementStrategyOpenedFiles.accessNow(newFilename);
		file.seek((pageaddress.pagenumber - newFile * javalimitfilesize) * pagesize);
		return file;
	}

	/**
	 * This method writes a page on disk if it has been modified...
	 * 
	 * @param page
	 *            The page to be stored on disk if it has been modified...
	 * @param pageaddress
	 *            The page address (filename, pagenumber, ...)
	 * 
	 * @throws IOException
	 */
	private void writeModifiedPage(final Page page, final PageAddress pageaddress)
			throws IOException {		
		if (page.modified) {
			// write outside in file
			jumpToPage(page.pagesize, pageaddress).write(page.page);
			page.modified = false;
		}
	}

	/**
	 * This method checks if the buffer is full. If the buffer full, then the
	 * method determines a page according to the used replacement strategy,
	 * which is then removed from the buffer (and stored on disk if it has been
	 * modified)
	 * 
	 * @throws IOException
	 */
	private void handleFullBuffer() throws IOException {
		while (this.currentNumberOfBytesInPuffer >= BufferManager.MAXBYTESINBUFFER) {
			final PageAddress toBeReplaced = this.replacementStrategy.getToBeReplaced();
			final Page pageToBeReplaced = this.bufferedPages.get(toBeReplaced);
			if (pageToBeReplaced != null) {
				writeModifiedPage(pageToBeReplaced, toBeReplaced);
				this.bufferedPages.remove(toBeReplaced);
				this.currentNumberOfBytesInPuffer -= pageToBeReplaced.pagesize;
			}
		}
	}

	/**
	 * This method returns a page. If the page is not in the buffer, it is
	 * loaded from disk and added to the buffer.
	 * 
	 * @param pagesize the size of the page
	 * @param pagenumber
	 *            The number of the page to be retrieved.
	 * @return The content of the page
	 * 
	 * @throws IOException
	 */
	public byte[] getPage(final int pagesize, final PageAddress pageaddress) throws IOException {
		BufferManager.lock.lock();
		try {
			Page page = this.bufferedPages.get(pageaddress);
			if (page == null) {
				handleFullBuffer();
				// load page
				final byte[] pageContent = new byte[pagesize];
				jumpToPage(pagesize, pageaddress).read(pageContent);
				page = new Page(pagesize, pageaddress, pageContent);
				this.bufferedPages.put(pageaddress, page);
				this.currentNumberOfBytesInPuffer += pagesize;
			} 
			this.replacementStrategy.accessNow(pageaddress);
			return page.page;
		} finally {
			BufferManager.lock.unlock();
		}
	}

	/**
	 * This method modifies a page in the buffer. If the page does not exist so
	 * far in the buffer it is added to the buffer and marked as modified.
	 * @param pagesize the size of the page
	 * @param pagenumber
	 *            The number of the modified page
	 * @param pageContent
	 *            The modified page
	 * 
	 * @throws IOException
	 */
	public void modifyPage(final int pagesize, final PageAddress pageaddress, final byte[] pageContent)
			throws IOException {
		BufferManager.lock.lock();
		try {
			Page page = this.bufferedPages.get(pageaddress);
			if (page == null) {
				handleFullBuffer();
				page = new Page(pagesize, pageaddress, pageContent, true);
				this.bufferedPages.put(pageaddress, page);
				this.currentNumberOfBytesInPuffer += pagesize;
			} else {
				page.page = pageContent;
				page.modified = true;
			}
			this.replacementStrategy.accessNow(pageaddress);
			
		} finally {
			BufferManager.lock.unlock();
		}
	}

	/**
	 * This method releases a page, i.e., its content does not need to be stored
	 * on disk.
	 * @param pageaddress the address of the page
	 */
	public void releasePage(final PageAddress pageaddress) {
		BufferManager.lock.lock();
		try {
			this.replacementStrategy.release(pageaddress);
			Page page = this.bufferedPages.remove(pageaddress);
			if(page!=null){
				this.currentNumberOfBytesInPuffer -= page.pagesize;
			}
		} finally {
			BufferManager.lock.unlock();
		}
	}
	
	/**
	 * This method releases all pages, i.e., their contents do not need to be stored
	 * on disk.
	 */
	public void releaseAllPages(){
		BufferManager.lock.lock();
		try {
			this.replacementStrategy.releaseAll();
			this.bufferedPages.clear();
			this.currentNumberOfBytesInPuffer = 0;
		} finally {
			BufferManager.lock.unlock();
		}		
	}

	/**
	 * This method writes all modified pages (in the buffer) to disk for a specific basis filename
	 * @param filename the basis filename of the pages to be written
	 * @throws IOException
	 */
	public void writeAllModifiedPages(final String filename) throws IOException {
		BufferManager.lock.lock();
		try {
			for (final Entry<PageAddress, Page> entry : this.bufferedPages.entrySet()) {
				if(entry.getKey().filename.compareTo(filename)==0){
					writeModifiedPage(entry.getValue(), entry.getKey());
				}
			}
		} finally {
			BufferManager.lock.unlock();
		}
	}

	
	/**
	 * This method writes all modified pages (in the buffer) to disk
	 * @throws IOException
	 */
	public void writeAllModifiedPages() throws IOException {
		BufferManager.lock.lock();
		try {
			for (final Entry<PageAddress, Page> entry : this.bufferedPages.entrySet()) {
				writeModifiedPage(entry.getValue(), entry.getKey());
			}
		} finally {
			BufferManager.lock.unlock();
		}
	}

	/**
	 * Returns an empty page in the size of the given page size.
	 * 
	 * @param pagesize the size of a page
	 * @return an empty page
	 */
	public byte[] getEmptyPage(final int pagesize) {
		return new byte[pagesize];
	}

	/**
	 * This method closes the underlying files. This method should only be called
	 * if the buffer manager is not used any more...
	 */
	public void close() throws IOException {
		BufferManager.lock.lock();
		try {
			for(RandomAccessFile file: this.bufferedFiles.values()){
				file.close();
			}
			this.bufferedFiles.clear();
			this.replacementStrategyOpenedFiles.releaseAll();
		} finally {
			BufferManager.lock.unlock();
		}
	}
	
	/**
	 * This method releases all pages of a basis filename, i.e., their contents do not need to be stored
	 * on disk.
	 * @param filename the filename of the basis file 
	 */
	public void releaseAllPages(final String filename){
		BufferManager.lock.lock();
		try {
			LinkedList<PageAddress> pageAddresses = new LinkedList<PageAddress>();
			for(Entry<PageAddress, Page> entry: this.bufferedPages.entrySet()){
				if(entry.getKey().filename.compareTo(filename)==0){
					pageAddresses.add(entry.getKey());
					this.currentNumberOfBytesInPuffer -= entry.getValue().pagesize; 
				}
			}
			for(final PageAddress pageAddress: pageAddresses){
				this.bufferedPages.remove(pageAddress);
				this.replacementStrategy.release(pageAddress);
			}
		} finally {
			BufferManager.lock.unlock();
		}		
	}

	/**
	 * This method closes the underlying files of basis filename.
	 * @param filename the filename of the basis file 
	 */
	public void close(final String filename) throws IOException {
		final String filenamePrefix = filename + "_";
		BufferManager.lock.lock();
		try {
			LinkedList<String> files = new LinkedList<String>();
			for(Entry<String, RandomAccessFile> entry: this.bufferedFiles.entrySet()){
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
			BufferManager.lock.unlock();
		}
	}	
	
	/**
	 * This method releases all pages, deletes the buffered file from disk and starts with a new file,
	 * i.e. all content of the buffered file is deleted.
	 * @param filename the filename of the basis file 
	 * @throws IOException
	 */
	public void reset(String filename) throws IOException {
		this.releaseAllPages(filename);
		this.close(filename);
		int i=0;
		boolean flag = true;
		do {
			File file = new File(filename + "_" + i);
			if(file.exists()){
				file.delete();
			} else {
				flag = false;
			}
		} while(flag);
	}

	/**
	 * @return the max number of bytes in the buffer
	 */
	public static int getMaxBytesInBuffer() {
		return BufferManager.MAXBYTESINBUFFER;
	}

	/**
	 * @param mAXBYTESINBUFFER the max number of bytes in the buffer
	 */
	public static void setMaxBytesInBuffer(int maxBytesInBuffer) {
		BufferManager.MAXBYTESINBUFFER = maxBytesInBuffer;
	}


	/**
	 * @return The used replacement strategy
	 */
	public REPLACEMENTSTRATEGY<PageAddress> getReplacementStrategy() {
		return this.replacementStrategy;
	}

	/**
	 * This method should be called only if the default replacement strategy is not used and it should be called before the BufferManager is used the first time.
	 * @param replacementStrategy the replacement strategy to be used
	 */
	public void setReplacementStrategy(REPLACEMENTSTRATEGY<PageAddress> replacementStrategy) {
		this.replacementStrategy = replacementStrategy;
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
	public static void setMaxOpenedFiles(int maxOpenedFiles) {
		MAXOPENEDFILES = maxOpenedFiles;
	}
}
