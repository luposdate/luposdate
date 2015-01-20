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
package lupos.datastructures.patriciatrie.disk.nodemanager;

import java.io.IOException;

import lupos.datastructures.patriciatrie.Trie;
import lupos.datastructures.patriciatrie.disk.Deserializer;
import lupos.datastructures.patriciatrie.disk.IDBNode;
import lupos.datastructures.patriciatrie.node.Node;

import lupos.datastructures.buffermanager.PageInputStream;
import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.buffermanager.PageOutputStream;

/**
 * This class provides methods for the disk based management of nodes.
 */
public class NodeManager implements HashBuffer.OverflowHandler<IDBNode> {
	
	/**
	 * Amount of nodes that are simultaneously stored in memory.
	 * 
	 * <strong>Caution:</strong> This value must currently be greater than the
	 * maximum recursion depth.
	 */
	public static final int NODES_TO_BUFFER = 255;

	/**
	 * The default number of bytes in a page to be stored on disk.
	 */
	public static final int DEFAULT_PAGESIZE = 128;
	
	/** Underlying pageManager */
	private PageManager pageManager;
	
	/** Underlying nodeBuffer */
	private HashBuffer<IDBNode> nodeBuffer;
	
	/**
	 * Checks if there is any data stored on a given page.
	 * 
	 * @param pageIdx
	 *            Page index to check for data
	 * @return <strong>true</strong> if no byte of that page differs from 0,
	 *         <strong>false</strong> otherwise
	 */
	public boolean isEmpty(final int pageIdx) {
		try {
			final byte[] pageContent  = this.pageManager.getPage(pageIdx);
			
			for (int i = 0; i < pageContent.length; i++)
				if (pageContent[i] != 0)
					return false;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	/**
	 * Stores a given node to its assigned page
	 * 
	 * @param node
	 *            DBNode to store
	 */
	protected void storeDBNode(final IDBNode node) {
		final NodeOutputStream nodeOutputStream = this.getOutputStream(node.getNodeIndex());
		
		if (nodeOutputStream != null) {
			try {
				node.serialize(nodeOutputStream);
				nodeOutputStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Adds a node to the node buffer. If the node buffer is full, the oldest
	 * entry will be actually written to disk
	 * 
	 * @param node
	 *            DBNode to save
	 */
	public void saveDBNode(final IDBNode node) {
		this.nodeBuffer.add(node.getNodeIndex(), node);
	}
	
	/**
	 * Retrieves a node with the given index from either the node buffer or the
	 * page with the given index.
	 * 
	 * @param idx
	 *            Index of the DBNode
	 * @return The DBNode with the index idx
	 */
	public IDBNode loadDBNode(final int idx, Deserializer deserializer) {
		IDBNode node = this.nodeBuffer.get(idx);
				
		if (node == null) {
			final NodeInputStream nodeInputStream = this.getInputStream(idx);
			
			if (nodeInputStream != null) {
				try {
					node = deserializer.deserialize(this, idx, nodeInputStream);
					nodeInputStream.close();
					this.nodeBuffer.add(node.getNodeIndex(), node);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return node;
	}
	
	/**
	 * @param trie
	 *            Trie, to which this NodeManager belongs
	 * @param fileName
	 *            Name of the file, that contains all the pages for this trie
	 * @param bufferSize
	 *            Number of DBNodes to hold in the buffer
	 * @param pageSize
	 *            The number of bytes in a page to be stored on disk
	 * @throws IOException
	 */
	public NodeManager(final Trie trie, final String fileName, final int bufferSize, final int pageSize) throws IOException {
		this.pageManager = new PageManager(fileName, pageSize);

		// If the given file contains data at page 1, it can be assumed that the
		// root node of a trie is stored on that page
		if (!this.isEmpty(1)){
			this.pageManager.initAfterLoading();
		}
		
		this.nodeBuffer = new HashBuffer<IDBNode>(bufferSize);
		this.nodeBuffer.setOverflowHandler(this);		
	}
	
	/**
	 * @param trie
	 *            Trie, to which this NodeManager belongs
	 * @param fileName
	 *            Name of the file, that contains all the pages for this trie
	 * @throws IOException
	 */
	public NodeManager(final Trie trie, final String fileName) throws IOException {
		this(trie, fileName, NODES_TO_BUFFER, DEFAULT_PAGESIZE);
	}
	
	/**
	 * @return Next free index to use for a DBNode
	 */
	public int getIndexForNewNode() {
		return this.pageManager.getNumberOfNewPage(); 
	}
		
	/**
	 * Removes a node from the node manager
	 * 
	 * @param idx
	 *            Index of the node to be removed
	 */
	public void removeNode(final int idx) {
		try {
			this.nodeBuffer.remove(idx);
			this.pageManager.releaseSequenceOfPages(idx);
			
			if (idx == 1) {
				this.pageManager.modifyPage(1, this.pageManager.getEmptyPage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return PageManager for this NodeManager
	 */
	public PageManager getPageManager() {
		return this.pageManager;
	}

	/**
	 * Returns an InputStream for the node with the index idx to load from.
	 * 
	 * @param idx
	 *            Index of the DBNode
	 * @return NodeInputStream to read from
	 */
	protected NodeInputStream getInputStream(final int idx) {
		try {
			return new NodeInputStream(new PageInputStream(idx, this.pageManager));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Returns an OutputStream for the node with the index idx to write to.
	 * 
	 * @param idx
	 *            Index of the DBNode
	 * @return NodeOutputStream to write to
	 */
	protected NodeOutputStream getOutputStream(final int idx) {
		try {
			return new NodeOutputStream(new PageOutputStream(idx, this.pageManager));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Writes all buffered (and therefore not yet written) nodes to disk and
	 * clears the node buffer.
	 */
	public void writeBufferToDisk() {
		for (final IDBNode node : this.nodeBuffer.values()) {
			if (node.isChanged())
				this.storeDBNode(node);
		}
		
		try {
			this.pageManager.writeAllModifiedPages();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the NodeManager. Afterwards the NodeManager should not be used
	 * anymore.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		this.pageManager.close();
	}
	
	@Override
	public void onOverflow(final IDBNode obj) {
		if (obj.isChanged() && !obj.isOnRecursionStack()) {
			this.storeDBNode(obj);
		}
	}
}
