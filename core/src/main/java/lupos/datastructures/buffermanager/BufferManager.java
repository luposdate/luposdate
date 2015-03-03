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

/**
 * This abstract super class specifies the basic methods to be implemented by concrete buffer manager implementations.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class BufferManager {

	/**
	 * This class stores all the information necessary to access a page (pagenumber plus filename of basis file)...
	 */
	public static class PageAddress{
		public int pagenumber;
		public String filename;

		public PageAddress(final int pagenumber, final String filename){
			this.pagenumber = pagenumber;
			this.filename = filename;
		}

		@Override
		public boolean equals(final Object o){
			if(o instanceof PageAddress){
				final PageAddress other = (PageAddress)o;
				return (other.pagenumber == this.pagenumber) && (other.filename.compareTo(this.filename)==0);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode(){
			return (int)((long)this.pagenumber + this.filename.hashCode());
		}

		@Override
		public String toString(){
			return this.pagenumber + " in " + this.filename;
		}
	}

	/**
	 * BufferManager is a singleton!
	 */
	private static BufferManager bufferManager = null;

	/**
	 * the only way to get the singleton buffer manager
	 *
	 * @return the singleton buffer manager
	 */
	public static synchronized BufferManager getBufferManager(){
		if(BufferManager.bufferManager==null){
			bufferManager = new BufferManager_RandomAccess();
		}
		return bufferManager;
	}

	/**
	 * Sets the (singleton) buffer manager. This method should be called before the buffer manager is being used!
	 *
	 * @param bufferManager the buffer manager to be set
	 */
	public static synchronized void setBufferManager(final BufferManager bufferManager){
		if(BufferManager.bufferManager!=null){
			throw new UnsupportedOperationException("Tried to set buffer manager, but buffer manager is already set!");
		}
		BufferManager.bufferManager = bufferManager;
	}

	/**
	 * the protected constructor
	 */
	protected BufferManager() {
	}

	/**
	 * This method returns a page. If the page is not in the buffer, it is
	 * loaded from disk and added to the buffer.
	 *
	 * @param pagesize the size of the page
	 * @param pageaddress
	 *            The address of the page to be retrieved.
	 * @return The content of the page
	 * @throws java.io.IOException if any.
	 */
	public abstract byte[] getPage(final int pagesize, final PageAddress pageaddress) throws IOException;

	/**
	 * This method modifies a page in the buffer. If the page does not exist so
	 * far in the buffer it is added to the buffer and marked as modified.
	 *
	 * @param pagesize the size of the page
	 * @param pageaddress
	 *            The address of the modified page
	 * @param pageContent
	 *            The modified page
	 * @throws java.io.IOException if any.
	 */
	public abstract void modifyPage(final int pagesize, final PageAddress pageaddress, final byte[] pageContent) throws IOException;

	/**
	 * This method releases a page, i.e., its content does not need to be stored
	 * on disk.
	 *
	 * @param pageaddress the address of the page
	 */
	public abstract void releasePage(final PageAddress pageaddress);

	/**
	 * This method releases all pages, i.e., their contents do not need to be stored
	 * on disk.
	 */
	public abstract void releaseAllPages();

	/**
	 * This method writes all modified pages (in the buffer) to disk for a specific basis filename
	 *
	 * @param filename the basis filename of the pages to be written
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeAllModifiedPages(final String filename) throws IOException;


	/**
	 * This method writes all modified pages (in the buffer) to disk
	 *
	 * @throws java.io.IOException if any.
	 */
	public abstract void writeAllModifiedPages() throws IOException;
	/**
	 * This method closes the underlying files. This method should only be called
	 * if the buffer manager is not used any more...
	 *
	 * @throws java.io.IOException if any.
	 */
	public abstract void close() throws IOException;

	/**
	 * This method releases all pages of a basis filename, i.e., their contents do not need to be stored
	 * on disk.
	 *
	 * @param filename the filename of the basis file
	 */
	public abstract void releaseAllPages(final String filename);

	/**
	 * This method closes the underlying files of basis filename.
	 *
	 * @param filename the filename of the basis file
	 * @throws java.io.IOException if any.
	 */
	public abstract void close(final String filename) throws IOException;

	/**
	 * This method releases all pages, deletes the buffered file from disk and starts with a new file,
	 * i.e. all content of the buffered file is deleted.
	 *
	 * @param filename the filename of the basis file
	 * @throws java.io.IOException if any.
	 */
	public abstract void reset(final String filename) throws IOException;
}
