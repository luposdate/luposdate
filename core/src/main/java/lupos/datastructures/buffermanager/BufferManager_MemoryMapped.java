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
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import lupos.misc.Quadruple;
import lupos.misc.Tuple;

/**
 * This class is a buffer manager, which uses memory mapped files.
 * However, it seems to be that there is no advantage in comparison to BufferManager_RandomAccess
 *
 * @author groppe
 * @version $Id: $Id
 */
public class BufferManager_MemoryMapped extends BufferManager_CachedFiles {

	private Tuple<MappedByteBuffer, Integer> getByteBufferAndOffset(final int pagesize, final PageAddress pageaddress) throws IOException{
		final Quadruple<String, RandomAccessFile, Integer, String> fileData = this.getFile(pagesize, pageaddress);
		final MappedByteBuffer mbb = fileData.getSecond().getChannel().map(FileChannel.MapMode.READ_WRITE, fileData.getThird(), pagesize);
		return new Tuple<MappedByteBuffer, Integer>(mbb, fileData.getThird());
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public void releasePage(final PageAddress pageaddress) {
		// do nothing, buffering is done by os for memory mapped files...
	}

	/** {@inheritDoc} */
	@Override
	public void releaseAllPages() {
		// do nothing, buffering is done by os for memory mapped files...
	}

	/** {@inheritDoc} */
	@Override
	public void writeAllModifiedPages(final String filename) throws IOException {
		// do nothing, buffering is done by os for memory mapped files...
	}

	/** {@inheritDoc} */
	@Override
	public void writeAllModifiedPages() throws IOException {
		// do nothing, buffering is done by os for memory mapped files...
	}

	/** {@inheritDoc} */
	@Override
	public void releaseAllPages(final String filename) {
		// do nothing, buffering is done by os for memory mapped files...
	}
}
