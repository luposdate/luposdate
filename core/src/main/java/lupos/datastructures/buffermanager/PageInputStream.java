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
