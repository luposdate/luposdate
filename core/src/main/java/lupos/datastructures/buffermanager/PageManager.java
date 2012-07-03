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

import java.io.IOException;

import lupos.datastructures.paged_dbbptree.DBBPTree;

/**
 * This class manages the free pages in a file. The first page is reserved for
 * storing the free pages before the end of the file.
 * 
 * One page has the following format:
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

	protected final BufferManager bufferManager;
	protected int maxNumberPages = 0;
	protected boolean freePageBeforeEndOfFile = false;

	public PageManager(final String name) throws IOException {
		bufferManager = new BufferManager(name);
		// initialize page for storing released pages...
		final byte[] page0 = getEmptyPage();
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
		bufferManager.modifyPage(0, page0);
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
		return bufferManager.getPage(pagenumber);
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
		bufferManager.modifyPage(pagenumber, pageContent);
	}

	/**
	 * This method writes all modified pages (in the buffer) to disk
	 * 
	 * @throws IOException
	 */
	public void writeAllModifiedPages() throws IOException {
		bufferManager.writeAllModifiedPages();
	}

	/**
	 * Returns an empty page in the size of the default page size.
	 * 
	 * @return an empty page
	 */
	public byte[] getEmptyPage() {
		return bufferManager.getEmptyPage();
	}

	/**
	 * This method closes the underlying file. This method should only be called
	 * if the page manager is not used any more...
	 */
	public void close() throws IOException {
		bufferManager.close();
	}

	/**
	 * This method determines the page number of a free page by first
	 * considering the free pages before the end of the file.
	 * 
	 * @return the page number of a free page
	 */
	public int getNumberOfNewPage() {
		if (!freePageBeforeEndOfFile) {
			maxNumberPages++;
			storeMaxNumberPagesAndFreePageBeforeEndOfFile();
			return maxNumberPages;
		} else {
			// look up first page where all free pages before the end of
			// the file are stored!
			int index = 0;
			try {
				byte[] currentPage = bufferManager.getPage(index);
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
						bufferManager.modifyPage(index, currentPage);
						return result;
					}
					index = (((currentPage[0] + 128) * 256 + (currentPage[1] + 128)) * 256 + (currentPage[2] + 128))
							* 256 + (currentPage[3] + 128);
					if (index == 0) {
						// end of sequence reached but no released page found!
						freePageBeforeEndOfFile = false;
						maxNumberPages++;
						storeMaxNumberPagesAndFreePageBeforeEndOfFile();						
						return maxNumberPages;
					} else
						currentPage = bufferManager.getPage(index);
				} while (true);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
				maxNumberPages++;
				storeMaxNumberPagesAndFreePageBeforeEndOfFile();
				return maxNumberPages;
			}
		}
	}
	
	private void storeMaxNumberPagesAndFreePageBeforeEndOfFile() {
		try {
			byte[] page0 = bufferManager.getPage(0);
			int number = maxNumberPages;
			page0[9] = (byte) ((number % 256) - 128);
			number /= 256;
			page0[8] = (byte) ((number % 256) - 128);
			number /= 256;
			page0[7] = (byte) ((number % 256) - 128);
			number /= 256;
			page0[6] = (byte) ((number % 256) - 128);
			if(freePageBeforeEndOfFile){
				page0[10]=-127;
			} else {
				page0[10]=-128;
			}
			bufferManager.modifyPage(0, page0);
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
		bufferManager.releasePage(pagenumber);
		if (pagenumber == maxNumberPages) {
			maxNumberPages--;
			storeMaxNumberPagesAndFreePageBeforeEndOfFile();
		} else {
			// store the released page on the first page
			// (or one of its succeeding pages)
			int index = 0;
			try {
				byte[] currentPage = bufferManager.getPage(index);
				do {
					int max = (currentPage[4] + 128) * 256
							+ (currentPage[5] + 128);
					if (max + 4 < BufferManager.PAGESIZE) {
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
						freePageBeforeEndOfFile = true;
						storeMaxNumberPagesAndFreePageBeforeEndOfFile();
						bufferManager.modifyPage(index, newPage);
						return;
					}
					index = (((currentPage[0] + 128) * 256 + (currentPage[1] + 128)) * 256 + (currentPage[2] + 128))
							* 256 + (currentPage[3] + 128);
					if (index == 0) {
						// prepare to store more released pages
						// by preparing the page pagenumber
						// to store more released pages.

						int number = pagenumber;
						currentPage[max + 3] = (byte) ((number % 256) - 128);
						number /= 256;
						currentPage[max + 2] = (byte) ((number % 256) - 128);
						number /= 256;
						currentPage[max + 1] = (byte) ((number % 256) - 128);
						number /= 256;
						currentPage[max] = (byte) ((number % 256) - 128);
						bufferManager.modifyPage(pagenumber, currentPage);

						currentPage = new byte[6];
						currentPage[0] = (byte) -128;
						currentPage[1] = (byte) -128;
						currentPage[2] = (byte) -128;
						currentPage[3] = (byte) -128;
						currentPage[4] = (byte) -128;
						currentPage[5] = (byte) (6 - 128);
						freePageBeforeEndOfFile = true;
						storeMaxNumberPagesAndFreePageBeforeEndOfFile();
						bufferManager.modifyPage(pagenumber, currentPage);
						return;
					} else
						currentPage = bufferManager.getPage(index);
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
	public void releaseSequenceOfPages(int pagenumber) throws IOException {
		while (pagenumber > 0) {
			final byte[] page = bufferManager.getPage(pagenumber);
			releasePage(pagenumber);
			pagenumber = (((page[0] + 128) * 256 + (page[1] + 128)) * 256 + (page[2] + 128))
					* 256 + (page[3] + 128);
		}
	}

	/**
	 * This method sets some internal states after loading the dbbptree... 
	 */
	public void initAfterLoading(){
		bufferManager.releaseAllPages();
		try {
			byte[] page0 = bufferManager.getPage(0);
			maxNumberPages=(page0[9]+128) + 256*((page0[8]+128) + 256*((page0[7]+128)+256*(page0[6]+128)));
			freePageBeforeEndOfFile=(page0[10]==-127);
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}		
	}
}