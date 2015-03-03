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
package lupos.datastructures.buffermanager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import lupos.misc.Triple;

/**
 * This buffer manager uses random access files to store and retrieve the pages.
 * It uses a LRU cache.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class BufferManager_RandomAccess extends BufferManager_CachedFiles {


	/**
	 * The percentage of space from the maximum heap space the buffer manager will use
	 */
	private final static int percentageOfHeapSpace = 50;

	/**
	 * The max. number of bytes in the buffer TODO change back
	 */
	protected static long MAXBYTESINBUFFER = (Runtime.getRuntime().maxMemory() * BufferManager_RandomAccess.percentageOfHeapSpace) / 100; // for 100 pages with default page size: 100 * 8 * 1024;

	/**
	 * the used replacement strategy for pages in the buffer, if new pages must be loaded which do not fit any more into the buffer
	 */
	protected REPLACEMENTSTRATEGY<PageAddress> replacementStrategy;

	/**
	 * This class contains the content of a single page plus a flag for storing whether or not the page has been modified...
	 */
	public static class Page {

		public final static int MAXOUTPUT = 5;

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
			final StringBuilder sb=new StringBuilder();
			sb.append('(');
			sb.append(this.pageaddress.toString());
			sb.append(", Size: ");
			sb.append(this.pagesize);
			sb.append(')');
			if(this.modified){
				sb.append('m');
			}
			sb.append('[');
			sb.append((0xFF & this.page[0]));
			for(int i=1; i<Math.min(this.page.length, Page.MAXOUTPUT); i++){
				sb.append(',');
				sb.append((0xFF & this.page[i]));
			}
			if(this.page.length>Page.MAXOUTPUT){
				sb.append("...");
			}
			sb.append(']');
			return sb.toString();
		}
	}

	/**
	 * The buffered pages
	 */
	protected Map<PageAddress, Page> bufferedPages = new HashMap<PageAddress, Page>((int)(BufferManager_RandomAccess.MAXBYTESINBUFFER / PageManager.DEFAULTPAGESIZE));

	/**
	 * the current number of bytes in the buffer
	 */
	protected long currentNumberOfBytesInBuffer = 0;

	/**
	 * holds all modified pages
	 */
	protected Map<PageAddress, Page> modifiedPages = new HashMap<PageAddress, Page>();

	/**
	 * the protected constructor
	 */
	protected BufferManager_RandomAccess() {
		this.replacementStrategy = new LeastRecentlyUsed<PageAddress>((int)(BufferManager_RandomAccess.MAXBYTESINBUFFER / PageManager.DEFAULTPAGESIZE));
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
		final Triple<String, RandomAccessFile, Integer> fileData = this.getFile(pagesize, pageaddress);
		fileData.getSecond().seek(fileData.getThird());
		return fileData.getSecond();
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
			this.jumpToPage(page.pagesize, pageaddress).write(page.page);
			page.modified = false;
			this.modifiedPages.remove(pageaddress);
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
		while (this.currentNumberOfBytesInBuffer >= BufferManager_RandomAccess.MAXBYTESINBUFFER) {
			final PageAddress toBeReplaced = this.replacementStrategy.getToBeReplaced();
			final Page pageToBeReplaced = this.bufferedPages.get(toBeReplaced);
			if (pageToBeReplaced != null) {
				this.writeModifiedPage(pageToBeReplaced, toBeReplaced);
				this.bufferedPages.remove(toBeReplaced);
				this.currentNumberOfBytesInBuffer -= pageToBeReplaced.pagesize;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method returns a page. If the page is not in the buffer, it is
	 * loaded from disk and added to the buffer.
	 */
	@Override
	public byte[] getPage(final int pagesize, final PageAddress pageaddress) throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			Page page = this.bufferedPages.get(pageaddress);
			if (page == null) {
				this.handleFullBuffer();
				// load page
				final byte[] pageContent = new byte[pagesize];
				this.jumpToPage(pagesize, pageaddress).read(pageContent);
				page = new Page(pagesize, pageaddress, pageContent);
				this.bufferedPages.put(pageaddress, page);
				this.currentNumberOfBytesInBuffer += pagesize;
			}
			this.replacementStrategy.accessNow(pageaddress);
			return page.page;
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method modifies a page in the buffer. If the page does not exist so
	 * far in the buffer it is added to the buffer and marked as modified.
	 */
	@Override
	public void modifyPage(final int pagesize, final PageAddress pageaddress, final byte[] pageContent)
			throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			Page page = this.bufferedPages.get(pageaddress);
			if (page == null) {
				this.handleFullBuffer();
				page = new Page(pagesize, pageaddress, pageContent, true);
				this.bufferedPages.put(pageaddress, page);
				this.modifiedPages.put(pageaddress, page);
				this.currentNumberOfBytesInBuffer += pagesize;
			} else {
				page.page = pageContent;
				page.modified = true;
				this.modifiedPages.put(pageaddress, page);
			}
			this.replacementStrategy.accessNow(pageaddress);

		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method releases a page, i.e., its content does not need to be stored
	 * on disk.
	 */
	@Override
	public void releasePage(final PageAddress pageaddress) {
		BufferManager_CachedFiles.lock.lock();
		try {
			this.replacementStrategy.release(pageaddress);
			final Page page = this.bufferedPages.remove(pageaddress);
			if(page!=null){
				if(page.modified){
					this.modifiedPages.remove(pageaddress);
				}
				this.currentNumberOfBytesInBuffer -= page.pagesize;
			}
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method releases all pages, i.e., their contents do not need to be stored
	 * on disk.
	 */
	@Override
	public void releaseAllPages(){
		BufferManager_CachedFiles.lock.lock();
		try {
			this.replacementStrategy.releaseAll();
			this.bufferedPages.clear();
			this.modifiedPages.clear();
			this.currentNumberOfBytesInBuffer = 0;
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * This method writes all modified pages (in the buffer) to disk for a specific basis filename
	 */
	@Override
	public void writeAllModifiedPages(final String filename) throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			for (final Object object : this.modifiedPages.entrySet().toArray()) {
				@SuppressWarnings("unchecked")
				final Entry<PageAddress, Page> entry = (Entry<PageAddress, Page>) object;
				if(entry.getKey().filename.compareTo(filename)==0){
					this.writeModifiedPage(entry.getValue(), entry.getKey());
				}
			}
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * This method writes all modified pages (in the buffer) to disk
	 */
	@Override
	public void writeAllModifiedPages() throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			for (final Object object : this.modifiedPages.entrySet().toArray()) {
				@SuppressWarnings("unchecked")
				final Entry<PageAddress, Page> entry = (Entry<PageAddress, Page>) object;
				this.writeModifiedPage(entry.getValue(), entry.getKey());
			}
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 * This method releases all pages of a basis filename, i.e., their contents do not need to be stored
	 * on disk.
	 */
	@Override
	public void releaseAllPages(final String filename){
		BufferManager_CachedFiles.lock.lock();
		try {
			final LinkedList<PageAddress> pageAddresses = new LinkedList<PageAddress>();
			for(final Entry<PageAddress, Page> entry: this.bufferedPages.entrySet()){
				if(entry.getKey().filename.compareTo(filename)==0){
					pageAddresses.add(entry.getKey());
					this.currentNumberOfBytesInBuffer -= entry.getValue().pagesize;
				}
			}
			for(final PageAddress pageAddress: pageAddresses){
				this.bufferedPages.remove(pageAddress);
				this.modifiedPages.remove(pageAddress);
				this.replacementStrategy.release(pageAddress);
			}
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * <p>getMaxBytesInBuffer.</p>
	 *
	 * @return the max number of bytes in the buffer
	 */
	public static long getMaxBytesInBuffer() {
		return BufferManager_RandomAccess.MAXBYTESINBUFFER;
	}

	/**
	 * <p>setMaxBytesInBuffer.</p>
	 *
	 * @param maxBytesInBuffer the max number of bytes in the buffer
	 */
	public static void setMaxBytesInBuffer(final long maxBytesInBuffer) {
		BufferManager_RandomAccess.MAXBYTESINBUFFER = maxBytesInBuffer;
	}

	/**
	 * <p>Getter for the field <code>replacementStrategy</code>.</p>
	 *
	 * @return The used replacement strategy
	 */
	public REPLACEMENTSTRATEGY<PageAddress> getReplacementStrategy() {
		return this.replacementStrategy;
	}

	/**
	 * This method should be called only if the default replacement strategy is not used and it should be called before the BufferManager is used the first time.
	 *
	 * @param replacementStrategy the replacement strategy to be used
	 */
	public void setReplacementStrategy(final REPLACEMENTSTRATEGY<PageAddress> replacementStrategy) {
		this.replacementStrategy = replacementStrategy;
	}
}
