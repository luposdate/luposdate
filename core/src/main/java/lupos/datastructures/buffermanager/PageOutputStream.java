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

import java.io.IOException;
import java.io.OutputStream;

public class PageOutputStream extends OutputStream {

	protected final PageManager pageManager;

	protected byte[] currentPage;
	protected int index;
	protected int currentPageNumber;

	public PageOutputStream(final int pagenumber, final PageManager pageManager, final boolean emptyPage, final boolean append) throws IOException {
		this(pagenumber, pageManager, emptyPage, PageInputStream.DEFAULTSTARTINDEX);
		if(append){
			this.index = this.getMaxOnThisPage();
		}
	}

	public PageOutputStream(final int pagenumber, final PageManager pageManager, final boolean emptyPage) throws IOException {
		this(pagenumber, pageManager, emptyPage, PageInputStream.DEFAULTSTARTINDEX);
	}

	public PageOutputStream(final int pagenumber, final PageManager pageManager, final boolean emptyPage, final int index) throws IOException {
		this.index = index;
		this.pageManager = pageManager;
		this.currentPageNumber = pagenumber;
		if (emptyPage) {
			this.emptyPage();
		} else {
			this.currentPage = pageManager.getPage(pagenumber);
		}
	}

	public PageOutputStream(final int pagenumber, final PageManager pageManager) throws IOException {
		this(pagenumber, pageManager, PageInputStream.DEFAULTSTARTINDEX);
	}

	public PageOutputStream(final int pagenumber, final PageManager pageManager, final int index) throws IOException {
		this(pagenumber, pageManager, false, index);
	}

	public PageOutputStream(final PageManager pageManager) {
		this(pageManager, PageInputStream.DEFAULTSTARTINDEX);
	}


	public PageOutputStream(final PageManager pageManager, final int index) {
		this.index = index;
		this.pageManager = pageManager;
		this.emptyPage();
		this.currentPageNumber = pageManager.getNumberOfNewPage();
	}

	private void emptyPage() {
		this.currentPage = this.pageManager.getEmptyPage();
		this.currentPage[0] = 0;
		this.currentPage[1] = 0;
		this.currentPage[2] = 0;
		this.currentPage[3] = 0;
	}

	private final int getNextPageNumber() {
		return (((0xFF & this.currentPage[0]) << 8 | (0xFF & this.currentPage[1])) << 8 | (0xFF & this.currentPage[2])) << 8 | (0xFF & this.currentPage[3]);
	}

	private final void setMaxOnThisPage() {
		final int intermediate = (this.index > this.currentPage.length) ? this.currentPage.length : this.index;
		this.currentPage[5] = (byte) intermediate;
		this.currentPage[4] = (byte) (intermediate >>> 8);
	}

	private final int getMaxOnThisPage() {
		return (0xFF & this.currentPage[4]) << 8 | (0xFF & this.currentPage[5]);
	}

	public int getCurrentPageNumber() {
		return this.currentPageNumber;
	}

	@Override
	public void write(final int b) throws IOException {
		if (this.index >= this.currentPage.length) {
			// write this page and open new page...
			this.setMaxOnThisPage();
			int nextPageNumber = this.getNextPageNumber();
			if (nextPageNumber <= 0) { // old sequence of pages cannot be reused!
				nextPageNumber = this.pageManager.getNumberOfNewPage();
				int intermediate = nextPageNumber;
				this.currentPage[3] = (byte) intermediate;
				intermediate >>>= 8;
				this.currentPage[2] = (byte) intermediate;
				intermediate >>>= 8;
				this.currentPage[1] = (byte) intermediate;
				intermediate >>>= 8;
				this.currentPage[0] = (byte) intermediate;
				this.pageManager.modifyPage(this.currentPageNumber, this.currentPage);
				this.emptyPage();
			} else { // follow the old sequence of pages...
				this.pageManager.modifyPage(this.currentPageNumber, this.currentPage);
				this.currentPage = this.pageManager.getPage(nextPageNumber);
			}
			this.currentPageNumber = nextPageNumber;
			this.index = PageInputStream.DEFAULTSTARTINDEX;
		}
		this.currentPage[this.index++] = (byte) b;
	}

	@Override
	public void close() throws IOException {
		int nextPageNumber = this.getNextPageNumber();
		this.setMaxOnThisPage();
		// mark current page as end of this sequence of pages...
		this.currentPage[0] = 0;
		this.currentPage[1] = 0;
		this.currentPage[2] = 0;
		this.currentPage[3] = 0;
		this.pageManager.modifyPage(this.currentPageNumber, this.currentPage);
		while (nextPageNumber > 0) {
			// release old sequence of pages...
			this.currentPage = this.pageManager.getPage(nextPageNumber);
			this.pageManager.releasePage(nextPageNumber);
			nextPageNumber = this.getNextPageNumber();
		}
	}
}
