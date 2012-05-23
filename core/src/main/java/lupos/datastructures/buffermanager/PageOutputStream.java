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
import java.io.OutputStream;

public class PageOutputStream extends OutputStream {

	protected final PageManager pageManager;

	protected byte[] currentPage;
	protected int index = 6;
	protected int currentPageNumber;

	public PageOutputStream(final int pagenumber,
			final PageManager pageManager, final boolean emptyPage)
			throws IOException {
		this.pageManager = pageManager;
		currentPageNumber = pagenumber;
		if (emptyPage) {
			emptyPage();
		} else {
			currentPage = pageManager.getPage(pagenumber);
		}
	}

	public PageOutputStream(final int pagenumber, final PageManager pageManager)
			throws IOException {
		this(pagenumber, pageManager, false);
	}

	public PageOutputStream(final PageManager pageManager) throws IOException {
		this.pageManager = pageManager;
		emptyPage();
		currentPageNumber = pageManager.getNumberOfNewPage();
	}

	private void emptyPage() {
		currentPage = pageManager.getEmptyPage();
		currentPage[0] = -128;
		currentPage[1] = -128;
		currentPage[2] = -128;
		currentPage[3] = -128;
	}

	private int getNextPageNumber() {
		return (((currentPage[0] + 128) * 256 + (currentPage[1] + 128)) * 256 + (currentPage[2] + 128))
				* 256 + (currentPage[3] + 128);
	}

	private void setMaxOnThisPage() {
		int intermediate = (index > currentPage.length) ? currentPage.length
				: index;
		currentPage[5] = (byte) ((intermediate % 256) - 128);
		intermediate /= 256;
		currentPage[4] = (byte) ((intermediate % 256) - 128);
	}

	@Override
	public void write(final int b) throws IOException {
		if (index >= currentPage.length) {
			// write this page and open new page...
			setMaxOnThisPage();
			int nextPageNumber = getNextPageNumber();
			if (nextPageNumber <= 0) { // old sequence of pages cannot be
				// reused!
				nextPageNumber = pageManager.getNumberOfNewPage();
				int intermediate = nextPageNumber;
				currentPage[3] = (byte) ((intermediate % 256) - 128);
				intermediate /= 256;
				currentPage[2] = (byte) ((intermediate % 256) - 128);
				intermediate /= 256;
				currentPage[1] = (byte) ((intermediate % 256) - 128);
				intermediate /= 256;
				currentPage[0] = (byte) ((intermediate % 256) - 128);
				pageManager.modifyPage(currentPageNumber, currentPage);
				emptyPage();
			} else { // follow the old sequence of pages...
				pageManager.modifyPage(currentPageNumber, currentPage);
				currentPage = pageManager.getPage(nextPageNumber);
			}
			currentPageNumber = nextPageNumber;
			index = 6;
		}
		currentPage[index++] = (byte) ((b % 256) - 128);
	}

	@Override
	public void close() throws IOException {
		int nextPageNumber = getNextPageNumber();
		setMaxOnThisPage();
		// mark current page as end of this sequence of pages...
		currentPage[0] = -128;
		currentPage[1] = -128;
		currentPage[2] = -128;
		currentPage[3] = -128;
		pageManager.modifyPage(currentPageNumber, currentPage);
		while (nextPageNumber > 0) {
			// release old sequence of pages...
			currentPage = pageManager.getPage(nextPageNumber);
			pageManager.releasePage(nextPageNumber);
			nextPageNumber = getNextPageNumber();
		}
	}
}
