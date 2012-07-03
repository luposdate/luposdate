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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.paged_dbbptree.DBBPTree;

public class BufferManager {

	public interface REPLACEMENTSTRATEGY {
		/**
		 * this method is called whenever a page is accessed
		 * 
		 * @param pagenumber
		 *            the accessed page
		 */
		public void accessNow(int pagenumber);

		/**
		 * This method is called whenever the buffer manager is full. This
		 * method returns the page to be replaced with another one. The returned
		 * page should be deleted from all records in the replacement strategy.
		 * 
		 * @return the page to be removed from the buffer manager to free main
		 *         memory
		 */
		public int getToBeReplaced();

		/**
		 * This method marks a page to be released, i.e., its content does not
		 * need to be stored on disk and the page can be deleted from the
		 * internal data structures of the replacement strategy.
		 */
		public void releasePage(int pagenumber);
		
		/**
		 * This method marks all pages to be released, i.e., 
		 * their contents do not need to be stored on disk and all
		 * the pages can be deleted from the internal data structures of
		 * the replacement strategy.
		 */
		public void releaseAll();
	}

	/**
	 * This replacement strategy returns the number of the least recently used
	 * page if the buffer is full.
	 */
	public class LeastRecentlyUsed implements REPLACEMENTSTRATEGY {
		protected HashMap<Integer, Long> timestamps = new HashMap<Integer, Long>();
		
		private long currentTime = 0;

		@Override
		public void accessNow(final int pagenumber) {
			this.timestamps.put(pagenumber, this.currentTime++);
		}

		@Override
		public int getToBeReplaced() {
			Entry<Integer, Long> min = null;
			for (final Entry<Integer, Long> entry : this.timestamps.entrySet()) {
				if (min == null || entry.getValue() < min.getValue())
					min = entry;
			}
			final int key = min.getKey();
			this.timestamps.remove(key);
			return key;
		}

		@Override
		public void releasePage(final int pagenumber) {
			this.timestamps.remove(pagenumber);
		}
		
		@Override
		public void releaseAll(){
			this.timestamps.clear();
		}		
	}

	public class Page {
		public byte[] page;
		public boolean modified;

		public Page(final byte[] page) {
			this.page = page;
			this.modified = false;
		}

		public Page(final byte[] page, final boolean modified) {
			this.page = page;
			this.modified = modified;
		}
		
		@Override
		public String toString(){
			StringBuilder sb=new StringBuilder();
			if(this.modified)
				sb.append('m');
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

	protected static int PAGESIZE = 8 * 1024;
	protected static int MAXPAGESINBUFFER = 10;

	protected final static int JAVALIMITFILESIZE = 1024 * 1024 * 1024 / PAGESIZE;

	protected RandomAccessFile bufferedFile;
	protected int currentFile = 0;
	protected String fileName;
	protected final REPLACEMENTSTRATEGY replacementStrategy;

	protected Map<Integer, Page> bufferedPages = new HashMap<Integer, Page>();

	protected ReentrantLock lock = new ReentrantLock();
	
	public BufferManager(final String name) throws FileNotFoundException {
		this(new File(name + "_0"));
		this.fileName = name;
	}

	private BufferManager(final File file) throws FileNotFoundException {
		this.bufferedFile = new RandomAccessFile(file, "rw");
		this.replacementStrategy = new LeastRecentlyUsed();
	}

	/**
	 * This method sets the offset of the file to the beginning of a page
	 * 
	 * @param pagenumber
	 *            The number of the page to be accessed afterwards...
	 * 
	 * @throws IOException
	 */
	private void jumpToPage(final int pagenumber) throws IOException {
		final int newFile = pagenumber / JAVALIMITFILESIZE;
		if (newFile != this.currentFile) {
			this.bufferedFile.close();
			this.bufferedFile = new RandomAccessFile(new File(this.fileName + "_" + newFile), "rw");
			this.currentFile = newFile;
		}
		this.bufferedFile.seek((pagenumber - this.currentFile * BufferManager.JAVALIMITFILESIZE) * BufferManager.PAGESIZE);
	}

	/**
	 * This method writes a page on disk if it has been modified...
	 * 
	 * @param page
	 *            The page to be stored on disk if it has been modified...
	 * @param pagenumber
	 *            The number of the page
	 * 
	 * @throws IOException
	 */
	private void writeModifiedPage(final Page page, final int pagenumber)
			throws IOException {		
		if (page.modified) {
			// write outside in file
			jumpToPage(pagenumber);
			this.bufferedFile.write(page.page);
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
		if (this.bufferedPages.size() >= BufferManager.MAXPAGESINBUFFER) {
			final int toBeReplaced = this.replacementStrategy.getToBeReplaced();
			final Page pageToBeReplaced = this.bufferedPages.get(toBeReplaced);
			if (pageToBeReplaced != null) {
				writeModifiedPage(pageToBeReplaced, toBeReplaced);
				this.bufferedPages.remove(toBeReplaced);
			}
		}
	}

	/**
	 * This method returns a page. If the page is not in the buffer, it is
	 * loaded from disk and added to the buffer.
	 * 
	 * @param pagenumber
	 *            The number of the page to be retrieved.
	 * @return The content of the page
	 * 
	 * @throws IOException
	 */
	public byte[] getPage(final int pagenumber) throws IOException {
		this.lock.lock();
		try {
			Page page = this.bufferedPages.get(pagenumber);
			if (page == null) {
				handleFullBuffer();
				// load page
				jumpToPage(pagenumber);
				final byte[] pageContent = new byte[PAGESIZE];
				this.bufferedFile.read(pageContent);
				page = new Page(pageContent);
				this.bufferedPages.put(pagenumber, page);
			} 
			this.replacementStrategy.accessNow(pagenumber);
			return page.page;
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * This method modifies a page in the buffer. If the page does not exist so
	 * far in the buffer it is added to the buffer and marked as modified.
	 * 
	 * @param pagenumber
	 *            The number of the modified page
	 * @param pageContent
	 *            The modified page
	 * 
	 * @throws IOException
	 */
	public void modifyPage(final int pagenumber, final byte[] pageContent)
			throws IOException {
		this.lock.lock();
		try {
			Page page = this.bufferedPages.get(pagenumber);
			if (page == null) {
				handleFullBuffer();
				page = new Page(pageContent, true);
				this.bufferedPages.put(pagenumber, page);
			} else {
				page.page = pageContent;
				page.modified = true;
			}
			this.replacementStrategy.accessNow(pagenumber);
			
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * This method releases a page, i.e., its content does not need to be stored
	 * on disk.
	 */
	public void releasePage(final int pagenumber) {
		this.lock.lock();
		try {
			this.replacementStrategy.releasePage(pagenumber);
			this.bufferedPages.remove(pagenumber);
		} finally {
			this.lock.unlock();
		}
	}
	
	/**
	 * This method releases all pages, i.e., their contents do not need to be stored
	 * on disk.
	 */
	public void releaseAllPages(){
		this.lock.lock();
		try {
			this.replacementStrategy.releaseAll();
			this.bufferedPages.clear();
		} finally {
			this.lock.unlock();
		}		
	}

	/**
	 * This method writes all modified pages (in the buffer) to disk
	 * 
	 * @throws IOException
	 */
	public void writeAllModifiedPages() throws IOException {
		this.lock.lock();
		try {
			for (final Entry<Integer, Page> entry : this.bufferedPages.entrySet()) {
				writeModifiedPage(entry.getValue(), entry.getKey());
			}
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Returns an empty page in the size of the default page size.
	 * 
	 * @return an empty page
	 */
	public byte[] getEmptyPage() {
		return new byte[PAGESIZE];
	}

	/**
	 * This method closes the underlying file. This method should only be called
	 * if the buffer manager is not used any more...
	 */
	public void close() throws IOException {
		this.bufferedFile.close();
	}
}
