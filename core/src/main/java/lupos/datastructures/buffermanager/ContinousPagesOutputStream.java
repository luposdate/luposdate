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
import java.io.OutputStream;

public class ContinousPagesOutputStream extends OutputStream {

	protected final PageManager pageManager;

	protected byte[] currentPage;
	protected int index;
	protected int currentPageNumber;

	public ContinousPagesOutputStream(final int pagenumber, final PageManager pageManager, final boolean emptyPage) throws IOException {
		this(pagenumber, pageManager, emptyPage, 0);
	}

	public ContinousPagesOutputStream(final int pagenumber, final PageManager pageManager, final boolean emptyPage, final int index) throws IOException {
		this.index = index;
		this.pageManager = pageManager;
		this.currentPageNumber = pagenumber;
		if (emptyPage) {
			this.emptyPage();
		} else {
			this.currentPage = pageManager.getPage(pagenumber);
		}
	}

	public ContinousPagesOutputStream(final int pagenumber, final PageManager pageManager) throws IOException {
		this(pagenumber, pageManager, 0);
	}

	public ContinousPagesOutputStream(final int pagenumber, final PageManager pageManager, final int index) throws IOException {
		this(pagenumber, pageManager, false, index);
	}

	public ContinousPagesOutputStream(final PageManager pageManager) {
		this(pageManager, 0);
	}


	public ContinousPagesOutputStream(final PageManager pageManager, final int index) {
		this.index = index;
		this.pageManager = pageManager;
		this.emptyPage();
		this.currentPageNumber = pageManager.getNumberOfNewPage();
	}

	protected void emptyPage() {
		this.currentPage = this.pageManager.getEmptyPage();
	}

	public int getCurrentPageNumber() {
		return this.currentPageNumber;
	}

	public int getPosInCurrentPage() {
		return this.index;
	}

	@Override
	public void write(final int b) throws IOException {
		if (this.index >= this.currentPage.length) {
			// write this page and open new page...
			this.pageManager.modifyPage(this.currentPageNumber, this.currentPage);
			this.currentPageNumber++;
			this.currentPage = this.pageManager.getPage(this.currentPageNumber);
			this.index = 0;
		}
		this.currentPage[this.index++] = (byte) b;
	}

	@Override
	public void close() throws IOException {
		this.pageManager.modifyPage(this.currentPageNumber, this.currentPage);
	}
}
