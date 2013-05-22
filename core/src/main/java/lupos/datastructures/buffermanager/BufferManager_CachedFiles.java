package lupos.datastructures.buffermanager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import lupos.misc.Quadruple;

/**
 * This class implements the methods for the buffer manager to cache and reuse some files.
 * It provides the LRU caching methods to be used also in BufferManager_RandomAccess for caching pages.
 */
public abstract class BufferManager_CachedFiles extends BufferManager {

	/**
	 * the max. number of bytes stored in a file, which can be handled by java without problems
	 */
	protected static int JAVALIMITFILESIZE_IN_BYTES = 1024 * 1024 * 1024;

	/**
	 * the max. number of opened files
	 */
	private static int MAXOPENEDFILES = 10;

	public interface REPLACEMENTSTRATEGY<T> {
		/**
		 * this method is called whenever an item is accessed
		 *
		 * @param address
		 *            the accessed item
		 */
		public void accessNow(T address);

		/**
		 * This method is called whenever the buffer manager is full. This
		 * method returns the item to be replaced with another one. The returned
		 * item should be deleted from all records in the replacement strategy.
		 *
		 * @return the item to be removed from the buffer manager to free main
		 *         memory
		 */
		public T getToBeReplaced();

		/**
		 * This method marks an item to be released, i.e., its content does not
		 * need to be stored on disk and the item can be deleted from the
		 * internal data structures of the replacement strategy.
		 */
		public void release(T address);

		/**
		 * This method marks all items to be released, i.e.,
		 * their contents do not need to be stored on disk and all
		 * the items can be deleted from the internal data structures of
		 * the replacement strategy.
		 */
		public void releaseAll();
	}

	/**
	 * This replacement strategy returns the number of the least recently used
	 * item if the buffer is full.
	 */
	public class LeastRecentlyUsed<T> implements REPLACEMENTSTRATEGY<T> {
		protected HashMap<T, Long> timestamps = new HashMap<T, Long>();

		private long currentTime = 0;

		@Override
		public void accessNow(final T address) {
			this.timestamps.put(address, this.currentTime++);
		}

		@Override
		public T getToBeReplaced() {
			Entry<T, Long> min = null;
			for (final Entry<T, Long> entry : this.timestamps.entrySet()) {
				if (min == null || entry.getValue() < min.getValue()) {
					min = entry;
				}
			}
			if(min!=null){
				final T key = min.getKey();
				this.timestamps.remove(key);
				return key;
			} else {
				// return error code...
				return null;
			}
		}

		@Override
		public void release(final T address) {
			this.timestamps.remove(address);
		}

		@Override
		public void releaseAll(){
			this.timestamps.clear();
		}
	}

	/**
	 * The lock used for any operation on a page
	 */
	protected static ReentrantLock lock = new ReentrantLock();

	/**
	 * the used replacement strategy for opened files
	 */
	private REPLACEMENTSTRATEGY<String> replacementStrategyOpenedFiles;

	/**
	 * The buffered opened files
	 */
	private final Map<String, RandomAccessFile> bufferedFiles = new HashMap<String, RandomAccessFile>();

	/**
	 * The constructor...
	 */
	protected BufferManager_CachedFiles(){
		this.replacementStrategyOpenedFiles = new LeastRecentlyUsed<String>();
	}

	/**
	 * Determines the filename, the file the page of which is addressed, the offset in the file, as well as eventually the filename of the file to be closed due to a full buffer...
	 * This method also deals with buffering all the files...
	 *
	 * @param pagesize the size of a page
	 * @param pageaddress
	 *            The page address to be accessed afterwards...
	 * @return a quadruple of the determined filename, file and offset, as well as eventually the filename of the file to be closed due to a full buffer
	 * @throws IOException
	 */
	protected final Quadruple<String, RandomAccessFile, Integer, String> getFile(final int pagesize, final PageAddress pageaddress) throws IOException{
		final int javalimitfilesize = (BufferManager_CachedFiles.JAVALIMITFILESIZE_IN_BYTES / pagesize);
		final int newFile = pageaddress.pagenumber / javalimitfilesize;
		final String newFilename = pageaddress.filename + "_" + newFile;
		RandomAccessFile file = this.bufferedFiles.get(newFilename);
		String filenameToBeClosed = null;
		if(file==null){
			if(this.bufferedFiles.size()>=MAXOPENEDFILES){
				filenameToBeClosed = this.replacementStrategyOpenedFiles.getToBeReplaced();
				final RandomAccessFile oldFile=this.bufferedFiles.remove(filenameToBeClosed);
				oldFile.close();
			}
			file = new RandomAccessFile(new File(newFilename), "rw");
			this.bufferedFiles.put(newFilename, file);
		}
		this.replacementStrategyOpenedFiles.accessNow(newFilename);
		return new Quadruple<String, RandomAccessFile, Integer, String>(newFilename, file, (pageaddress.pagenumber - newFile * javalimitfilesize) * pagesize, filenameToBeClosed);
	}

	/**
	 * @return The used replacement strategy
	 */
	public REPLACEMENTSTRATEGY<String> getReplacementStrategyOpenedFiles() {
		return this.replacementStrategyOpenedFiles;
	}

	/**
	 * This method should be called only if the default replacement strategy is not used and it should be called before the BufferManager is used the first time.
	 * @param replacementStrategy the replacement strategy to be used
	 */
	public void setReplacementStrategyOpenedFiles(final REPLACEMENTSTRATEGY<String> replacementStrategyOpenedFiles) {
		this.replacementStrategyOpenedFiles = replacementStrategyOpenedFiles;
	}

	/**
	 * @return the max number of opened files in the buffer manager
	 */
	public static int getMaxOpenedFiles() {
		return MAXOPENEDFILES;
	}

	/**
	 * @param maxOpenedFiles the max opened number of files in the buffer manager
	 */
	public static void setMaxOpenedFiles(final int maxOpenedFiles) {
		MAXOPENEDFILES = maxOpenedFiles;
	}

	/**
	 * This method closes the underlying files. This method should only be called
	 * if the buffer manager is not used any more...
	 */
	@Override
	public void close() throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			for(final RandomAccessFile file: this.bufferedFiles.values()){
				file.close();
			}
			this.bufferedFiles.clear();
			this.replacementStrategyOpenedFiles.releaseAll();
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * This method closes the underlying files of basis filename.
	 * @param filename the filename of the basis file
	 */
	@Override
	public void close(final String filename) throws IOException {
		final String filenamePrefix = filename + "_";
		BufferManager_CachedFiles.lock.lock();
		try {
			final LinkedList<String> files = new LinkedList<String>();
			for(final Entry<String, RandomAccessFile> entry: this.bufferedFiles.entrySet()){
				if(entry.getKey().startsWith(filenamePrefix)){
					files.add(entry.getKey());
					entry.getValue().close();
				}
			}
			for(final String file: files){
				this.bufferedFiles.remove(file);
				this.replacementStrategyOpenedFiles.release(file);
			}
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	/**
	 * This method releases all pages, deletes the buffered file from disk and starts with a new file,
	 * i.e. all content of the buffered file is deleted.
	 * @param filename the filename of the basis file
	 * @throws IOException
	 */
	@Override
	public void reset(final String filename) throws IOException {
		this.releaseAllPages(filename);
		this.close(filename);
		final int i=0;
		boolean flag = true;
		do {
			final File file = new File(filename + "_" + i);
			if(file.exists()){
				file.delete();
			} else {
				flag = false;
			}
		} while(flag);
	}
}
