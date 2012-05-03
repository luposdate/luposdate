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

		public void accessNow(final int pagenumber) {
			timestamps.put(pagenumber, System.currentTimeMillis());
		}

		public int getToBeReplaced() {
			Entry<Integer, Long> min = null;
			for (final Entry<Integer, Long> entry : timestamps.entrySet()) {
				if (min == null || entry.getValue() < min.getValue())
					min = entry;
			}
			final int key = min.getKey();
			timestamps.remove(key);
			return key;
		}

		public void releasePage(final int pagenumber) {
			timestamps.remove(pagenumber);
		}
		
		public void releaseAll(){
			timestamps.clear();
		}		
	}

	public class Page {
		public byte[] page;
		public boolean modified;

		public Page(final byte[] page) {
			this.page = page;
			modified = false;
		}

		public Page(final byte[] page, final boolean modified) {
			this.page = page;
			this.modified = modified;
		}
		
		public String toString(){
			StringBuilder sb=new StringBuilder();
			if(modified)
				sb.append('m');
			sb.append('[');
			sb.append((page[0]+128));
			for(int i=1;i<page.length;i++){
				sb.append(',');
				sb.append((page[i]+128));
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
		this(new File(name+ "_0"));
		this.fileName = name;
	}

	private BufferManager(final File file) throws FileNotFoundException {
		bufferedFile = new RandomAccessFile(file, "rw");
		replacementStrategy = new LeastRecentlyUsed();
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
		if (newFile != currentFile) {
			bufferedFile.close();
			bufferedFile = new RandomAccessFile(new File(fileName + "_"
					+ newFile), "rw");
			currentFile = newFile;
		}
		bufferedFile.seek((pagenumber - currentFile * JAVALIMITFILESIZE)
				* PAGESIZE);
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
			bufferedFile.write(page.page);
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
		if (bufferedPages.size() >= MAXPAGESINBUFFER) {
			final int toBeReplaced = replacementStrategy.getToBeReplaced();
			final Page pageToBeReplaced = bufferedPages.get(toBeReplaced);
			if (pageToBeReplaced != null) {
				writeModifiedPage(pageToBeReplaced, toBeReplaced);
				bufferedPages.remove(toBeReplaced);
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
		lock.lock();
		try {
			Page page = bufferedPages.get(pagenumber);
			if (page == null) {
				handleFullBuffer();
				// load page
				jumpToPage(pagenumber);
				final byte[] pageContent = new byte[PAGESIZE];
				bufferedFile.read(pageContent);
				page = new Page(pageContent);				
			} 
			replacementStrategy.accessNow(pagenumber);
			return page.page;
		} finally {
			lock.unlock();
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
		lock.lock();
		try {
			Page page = bufferedPages.get(pagenumber);
			if (page == null) {
				handleFullBuffer();
				page = new Page(pageContent, true);
				bufferedPages.put(pagenumber, page);
			} else {
				page.page = pageContent;
				page.modified = true;
			}
			replacementStrategy.accessNow(pagenumber);
			
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This method releases a page, i.e., its content does not need to be stored
	 * on disk.
	 */
	public void releasePage(final int pagenumber) {
		lock.lock();
		try {
			replacementStrategy.releasePage(pagenumber);
			bufferedPages.remove(pagenumber);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * This method releases all pages, i.e., their contents do not need to be stored
	 * on disk.
	 */
	public void releaseAllPages(){
		lock.lock();
		try {
			replacementStrategy.releaseAll();
			bufferedPages.clear();
		} finally {
			lock.unlock();
		}		
	}

	/**
	 * This method writes all modified pages (in the buffer) to disk
	 * 
	 * @throws IOException
	 */
	public void writeAllModifiedPages() throws IOException {
		lock.lock();
		try {
			for (final Entry<Integer, Page> entry : bufferedPages.entrySet()) {
				writeModifiedPage(entry.getValue(), entry.getKey());
			}
		} finally {
			lock.unlock();
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
		bufferedFile.close();
	}
}
