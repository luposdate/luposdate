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
import java.io.InputStream;

public class PageInputStream extends InputStream {

	protected final PageManager pageManager;

	protected byte[] currentPage;
	protected int index = 6;
	protected int maxOnThisPage;

	public PageInputStream(final int pagenumber, final PageManager pageManager)
			throws IOException {
		this.pageManager = pageManager;
		currentPage = pageManager.getPage(pagenumber);
		setMaxOnThisPage();
	}

	private void setMaxOnThisPage() {
		maxOnThisPage = (currentPage[4] + 128) * 256 + (currentPage[5] + 128);
	}

	@Override
	public int read() throws IOException {
		if (index >= maxOnThisPage) {
			final int nextPage = (((currentPage[0] + 128) * 256 + (currentPage[1] + 128)) * 256 + (currentPage[2] + 128))
					* 256 + (currentPage[3] + 128);
			if (nextPage == 0)
				return -1;
			currentPage = pageManager.getPage(nextPage);
			index = 6;
			setMaxOnThisPage();
		}
		return currentPage[index++] + 128;
	}

}
