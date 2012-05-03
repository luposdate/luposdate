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
