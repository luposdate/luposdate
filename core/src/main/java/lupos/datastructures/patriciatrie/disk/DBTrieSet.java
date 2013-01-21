/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.datastructures.patriciatrie.disk;

import java.io.IOException;

import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.patriciatrie.disk.nodemanager.NodeManager;
import lupos.datastructures.patriciatrie.node.Node;

/**
 * This class implements the disk based behavior for a trie for a set of strings.
 */
public class DBTrieSet extends TrieSet {

	/** This encoding will be used when Strings are serialized */
	public static final String OUTPUT_ENCODING = "UTF-8";
	
	/** NodeManager responsible for all the nodes of this trie */
	private NodeManager nodeManager;
	
	/**
	 * Creates a new trie
	 * 
	 * @param fileName
	 *            Base filename for the trie
	 * @param bufferSize
	 *            Amount of nodes that are simultaneously kept in memory
	 * @param pageSize
	 *            The size of a page to be stored on disk
	 * @param mode the mode of this trie
	 * @throws IOException
	 */
	public DBTrieSet(final String fileName, final int bufferSize, final int pageSize) throws IOException {
		super();
		this.nodeManager = new NodeManager(this, fileName, bufferSize, pageSize);
		
		if (this.nodeManager.isEmpty(1)) {
			this.setRootNode(null);
		}
		else {
			this.setRootNode((Node) this.nodeManager.loadDBNode(1, DBNode.deserializer));
		}		
	}
	
	/**
	 * Creates a new trie with the default buffer size
	 * 
	 * @param fileName
	 *            Base filename for the trie
	 * @throws IOException
	 */
	public DBTrieSet(final String fileName) throws IOException {
		this(fileName, NodeManager.NODES_TO_BUFFER, NodeManager.DEFAULT_PAGESIZE);
	}
	
	/**
	 * Saves all unsaved changes to disk and closes the underlying node manager.
	 * After this method has been called, the trie should not be accessed
	 * anymore.
	 */
	@Override
	public void release() {
		if (this.getRootNode() != null) {
			this.nodeManager.saveDBNode((IDBNode) this.getRootNode());
		}
		this.nodeManager.writeBufferToDisk();
		try {
			this.nodeManager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		super.release();
	}

	@Override
	protected Node createNodeInstance() {
		return new DBNode(this.nodeManager, -1);
	}
	
	@Override
	protected Node createRootNodeInstance() {
		return new DBNode(this.nodeManager, 1);
	}
	
	@Override
	protected void changeRootNode(final Node rootNode_local) {
		this.setRootNode(rootNode_local);
		
		((DBNode) this.getRootNode()).setNodeIndex(1);
		
		this.nodeManager.saveDBNode((IDBNode) this.getRootNode());
	}

}
