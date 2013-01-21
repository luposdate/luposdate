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
package lupos.datastructures.buffermanager;

import java.io.File;
import java.io.IOException;

import lupos.datastructures.buffermanager.BufferManager.PageAddress;

/**
 * This class manages the free pages in a file. The first page is reserved for
 * storing the free pages before the end of the file.
 * 
 * Each page has the following format:
 * Bytes 0 - 3 for the next page of a sequence of pages
 * Bytes 4 and 5 for the maximum number of bytes within this page
 * 
 * The Page 0 has a special semantic:
 * Bytes 6 to 9 of page 0 store the current total number of pages.
 * Byte 10 of page 0 stores if free pages are only in the end (and new pages must be appended) (value=0),
 * or if page 0 stores some free pages (value=1), which should be used for the new pages...
 * Afterwards page 0 (and maybe its following sequence) stores the free pages.
 */
public class PageManager {

	/**
	 * the buffer manager (is a singleton)
	 */
	protected final BufferManager bufferManager;
	
	/**
	 * the maximum page number used so far
	 */
	protected int maxNumberPages = 0;
	
	/**
	 * are there any free pages between used pages?
	 */
	protected boolean freePageBeforeEndOfFile = false;
	
	/**
	 * the basis filename for the file in which the pages are stored.
	 * Due to java problems with large file sizes, several physical files (filename+"_0", filename+"_1", filename+"_2", ...) are used to store the pages.  
	 */
	protected final String filename;
	
	/**
	 * The size of one page in bytes for this page manager
	 */
	protected final int pagesize;
	
	/**
	 * The default size of one page in bytes
	 */
	protected static int DEFAULTPAGESIZE = 8 * 1024;
		
	/**
	* creates a new PageManager object, which reuses a given file if it exists and creates a new file otherwise...
	* The default page size (8 KBytes) is used.
	*/
	public static PageManager createPageManager(final String name) throws IOException{
		return PageManager.createPageManager(name, PageManager.DEFAULTPAGESIZE);
	}
	
	/**
	* creates a new PageManager object, which reuses a given file if it exists and creates a new file otherwise...
	* The page size used can be specified.
	*/
	public static PageManager createPageManager(final String name, int pagesize) throws IOException{
		File f = new File(name + "_0");
		return new PageManager(name, !f.exists(), pagesize); 
	}

	public PageManager(final String name) throws IOException {
		this(name, PageManager.DEFAULTPAGESIZE);
	}

	public PageManager(final String name, final int pagesize) throws IOException {
		this(name, true, pagesize);
	}

	public PageManager(final String name, boolean overwriteExistingFile) throws IOException {
		this(name, overwriteExistingFile, PageManager.DEFAULTPAGESIZE);
	}
		
	public PageManager(final String name, boolean overwriteExistingFile, final int pagesize) throws IOException {
		this.bufferManager = BufferManager.getBufferManager();
		this.filename = name;
		this.pagesize = pagesize;
		if(overwriteExistingFile){
			// initialize page for storing released pages...
			this.bufferManager.modifyPage(this.pagesize, new PageAddress(0, this.filename), this.getEmptyPage0());
		} else {
			this.initAfterLoading();
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
		return this.bufferManager.getPage(this.pagesize, new PageAddress(pagenumber, this.filename));
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
		this.bufferManager.modifyPage(this.pagesize, new PageAddress(pagenumber, this.filename), pageContent);
	}

	/**
	 * This method writes all modified pages (in the buffer) to disk
	 * 
	 * @throws IOException
	 */
	public void writeAllModifiedPages() throws IOException {
		this.bufferManager.writeAllModifiedPages(this.filename);
	}

	/**
	 * Returns an empty page in the size of the given page size.
	 * 
	 * @return an empty page
	 */
	public byte[] getEmptyPage() {
		return this.bufferManager.getEmptyPage(this.pagesize);
	}
	
	/**
	 * Returns an empty page for page 0 in the size of the default page size.
	 * 
	 * @return an empty page
	 */
	public byte[] getEmptyPage0() {
		// initialize page for storing released pages...
		final byte[] page0 = this.getEmptyPage();
		page0[0] = (byte) -128;
		page0[1] = (byte) -128;
		page0[2] = (byte) -128;
		page0[3] = (byte) -128;
		page0[4] = (byte) -128;
		page0[5] = (byte) (11 - 128);
		page0[6] = (byte) -128;
		page0[7] = (byte) -128;
		page0[8] = (byte) -128;
		page0[9] = (byte) -128;
		page0[10] = (byte) -128;
		return page0;
	}

	/**
	 * This method closes the underlying file. This method should only be called
	 * if the page manager is not used any more...
	 */
	public void close() throws IOException {
		this.bufferManager.close(this.filename);
	}
	
	/**
	 * This method releases all pages, deletes the buffered file from disk and starts with a new file,
	 * i.e. all content of the buffered file is deleted. 
	 * @throws IOException
	 */
	public void reset() throws IOException{
		this.bufferManager.reset(this.filename);
		this.bufferManager.modifyPage(this.pagesize, new PageAddress(0, this.filename), this.getEmptyPage0());	
	}

	/**
	 * This method determines the page number of a free page by first
	 * considering the free pages before the end of the file.
	 * 
	 * @return the page number of a free page
	 */
	public int getNumberOfNewPage() {
		if (!this.freePageBeforeEndOfFile) {
			this.maxNumberPages++;
			storeMaxNumberPagesAndFreePageBeforeEndOfFile();
			return this.maxNumberPages;
		} else {
			// look up first page where all free pages before the end of
			// the file are stored!
			int index = 0;
			try {
				byte[] currentPage = this.bufferManager.getPage(this.pagesize, new PageAddress(index, this.filename));
				do {
					int max = (currentPage[4] + 128) * 256
							+ (currentPage[5] + 128);
					if (max > ((index==0)?11:6)) {
						// page with entries found!
						final int result = (((currentPage[max - 4] + 128) * 256 + (currentPage[max - 3] + 128)) * 256 + (currentPage[max - 2] + 128))
								* 256 + (currentPage[max - 1] + 128);
						max -= 4;
						currentPage[5] = (byte) ((max % 256) - 128);
						currentPage[4] = (byte) (((max / 256) % 256) - 128);
						this.bufferManager.modifyPage(this.pagesize, new PageAddress(index, this.filename), currentPage);
						return result;
					}
					index = (((currentPage[0] + 128) * 256 + (currentPage[1] + 128)) * 256 + (currentPage[2] + 128))
							* 256 + (currentPage[3] + 128);
					if (index == 0) {
						// end of sequence reached but no released page found!
						this.freePageBeforeEndOfFile = false;
						this.maxNumberPages++;
						storeMaxNumberPagesAndFreePageBeforeEndOfFile();						
						return this.maxNumberPages;
					} else
						currentPage = this.bufferManager.getPage(this.pagesize, new PageAddress(index, this.filename));
				} while (true);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
				this.maxNumberPages++;
				storeMaxNumberPagesAndFreePageBeforeEndOfFile();
				return this.maxNumberPages;
			}
		}
	}
	
	/**
	 * this method is used to store additional information in the first page like the max. page number and if there are free pages before the end of the file (which are stored in the first page...)
	 */
	private void storeMaxNumberPagesAndFreePageBeforeEndOfFile() {
		try {
			PageAddress pageaddress0 = new PageAddress(0, this.filename);
			byte[] page0 = this.bufferManager.getPage(this.pagesize, pageaddress0);
			int number = this.maxNumberPages;
			page0[9] = (byte) ((number % 256) - 128);
			number /= 256;
			page0[8] = (byte) ((number % 256) - 128);
			number /= 256;
			page0[7] = (byte) ((number % 256) - 128);
			number /= 256;
			page0[6] = (byte) ((number % 256) - 128);
			if(this.freePageBeforeEndOfFile){
				page0[10]=-127;
			} else {
				page0[10]=-128;
			}
			this.bufferManager.modifyPage(this.pagesize, pageaddress0, page0);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * This method releases a page.
	 * 
	 * @param pagenumber
	 *            the number of the page to be released!
	 */
	public void releasePage(final int pagenumber) {
		this.bufferManager.releasePage(new PageAddress(pagenumber, this.filename));
		if (pagenumber == this.maxNumberPages) {
			this.maxNumberPages--;
			storeMaxNumberPagesAndFreePageBeforeEndOfFile();
		} else {
			// store the released page on the first page
			// (or one of its succeeding pages)
			int index = 0;
			try {
				byte[] currentPage = this.bufferManager.getPage(this.pagesize, new PageAddress(index, this.filename));
				do {
					int max = (currentPage[4] + 128) * 256
							+ (currentPage[5] + 128);
					if (max + 4 < this.pagesize) {
						final byte[] newPage = new byte[max + 4];
						System.arraycopy(currentPage, 0, newPage, 0, max);
						int number = pagenumber;
						newPage[max + 3] = (byte) ((number % 256) - 128);
						number /= 256;
						newPage[max + 2] = (byte) ((number % 256) - 128);
						number /= 256;
						newPage[max + 1] = (byte) ((number % 256) - 128);
						number /= 256;
						newPage[max] = (byte) ((number % 256) - 128);
						max += 4;
						newPage[5] = (byte) ((max % 256) - 128);
						newPage[4] = (byte) (((max / 256) % 256) - 128);
						this.freePageBeforeEndOfFile = true;
						storeMaxNumberPagesAndFreePageBeforeEndOfFile();
						this.bufferManager.modifyPage(this.pagesize, new PageAddress(index, this.filename), newPage);
						return;
					}
					final int oldindex = index;
					index = (((currentPage[0] + 128) * 256 + (currentPage[1] + 128)) * 256 + (currentPage[2] + 128))
							* 256 + (currentPage[3] + 128);
					if (index == 0) {
						// prepare to store more released pages
						// by preparing the page pagenumber
						// to store more released pages.

						int number = pagenumber;
						currentPage[3] = (byte) ((number % 256) - 128);
						number /= 256;
						currentPage[2] = (byte) ((number % 256) - 128);
						number /= 256;
						currentPage[1] = (byte) ((number % 256) - 128);
						number /= 256;
						currentPage[0] = (byte) ((number % 256) - 128);
						this.bufferManager.modifyPage(this.pagesize, new PageAddress(oldindex, this.filename), currentPage);

						currentPage = new byte[6];
						currentPage[0] = (byte) -128;
						currentPage[1] = (byte) -128;
						currentPage[2] = (byte) -128;
						currentPage[3] = (byte) -128;
						currentPage[4] = (byte) -128;
						currentPage[5] = (byte) (6 - 128);
						this.freePageBeforeEndOfFile = true;
						storeMaxNumberPagesAndFreePageBeforeEndOfFile();
						this.bufferManager.modifyPage(this.pagesize, new PageAddress(pagenumber, this.filename), currentPage);
						return;
					} else
						currentPage = this.bufferManager.getPage(this.pagesize, new PageAddress(index, this.filename));
				} while (true);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Releases a sequence of pages under the assumption that the 0th to 3rd (inclusive)
	 * bytes contain the next page of the sequence and the last page contains
	 * the next page 0.
	 * 
	 * @param pagenumber
	 *            the starting page of the sequence to be released...
	 * 
	 * @throws IOException
	 */
	public void releaseSequenceOfPages(final int pagenumber) throws IOException {
		int pagenumber_tmp = pagenumber;
		while (pagenumber_tmp > 0) {
			final byte[] page = this.bufferManager.getPage(this.pagesize, new PageAddress(pagenumber_tmp, this.filename));
			releasePage(pagenumber_tmp);
			pagenumber_tmp = (((page[0] + 128) * 256 + (page[1] + 128)) * 256 + (page[2] + 128)) * 256 + (page[3] + 128);
		}
	}

	/**
	 * This method sets some internal states after loading... 
	 */
	public void initAfterLoading(){
		this.bufferManager.releaseAllPages();
		try {
			byte[] page0 = this.bufferManager.getPage(this.pagesize, new PageAddress(0, this.filename));
			this.maxNumberPages=(page0[9]+128) + 256*((page0[8]+128) + 256*((page0[7]+128)+256*(page0[6]+128)));
			this.freePageBeforeEndOfFile=(page0[10]==-127);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}		
	}

	/**
	 * @return the default page size in bytes
	 */
	public static int getDefaultPageSize() {
		return PageManager.DEFAULTPAGESIZE;
	}

	/**
	 * sets the default page size 
	 * @param defaultpagesize the default page size in bytes
	 */
	public static void setDefaultPageSize(int defaultpagesize) {
		PageManager.DEFAULTPAGESIZE = defaultpagesize;
	}
}