package lupos.datastructures.buffermanager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import lupos.misc.Quadruple;
import lupos.misc.Tuple;

/**
 * This class is a buffer manager, which uses memory mapped files.
 * However, it seems to be that there is no advantage in comparison to BufferManager_RandomAccess
 */
public class BufferManager_MemoryMapped extends BufferManager_CachedFiles {

	private Tuple<MappedByteBuffer, Integer> getByteBufferAndOffset(final int pagesize, final PageAddress pageaddress) throws IOException{
		final Quadruple<String, RandomAccessFile, Integer, String> fileData = this.getFile(pagesize, pageaddress);
		final MappedByteBuffer mbb = fileData.getSecond().getChannel().map(FileChannel.MapMode.READ_WRITE, fileData.getThird(), pagesize);
		return new Tuple<MappedByteBuffer, Integer>(mbb, fileData.getThird());
	}

	@Override
	public byte[] getPage(final int pagesize, final PageAddress pageaddress) throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			final Tuple<MappedByteBuffer, Integer> bbao = this.getByteBufferAndOffset(pagesize, pageaddress);
			final byte result[] = new byte[pagesize];
			bbao.getFirst().get(result);
			return result;
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	@Override
	public void modifyPage(final int pagesize, final PageAddress pageaddress, final byte[] pageContent) throws IOException {
		BufferManager_CachedFiles.lock.lock();
		try {
			final Tuple<MappedByteBuffer, Integer> bbao = this.getByteBufferAndOffset(pagesize, pageaddress);
			bbao.getFirst().put(pageContent);
		} finally {
			BufferManager_CachedFiles.lock.unlock();
		}
	}

	@Override
	public void releasePage(final PageAddress pageaddress) {
		// do nothing, buffering is done by os for memory mapped files...
	}

	@Override
	public void releaseAllPages() {
		// do nothing, buffering is done by os for memory mapped files...
	}

	@Override
	public void writeAllModifiedPages(final String filename) throws IOException {
		// do nothing, buffering is done by os for memory mapped files...
	}

	@Override
	public void writeAllModifiedPages() throws IOException {
		// do nothing, buffering is done by os for memory mapped files...
	}

	@Override
	public void releaseAllPages(final String filename) {
		// do nothing, buffering is done by os for memory mapped files...
	}
}
