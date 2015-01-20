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
package lupos.datastructures.patriciatrie;

import lupos.datastructures.patriciatrie.node.Node;
import lupos.datastructures.patriciatrie.node.NodeHelper;

/**
 * This class implements some of the base algorithms that can be executed on Patricia Tries.
 */
public abstract class Trie {
		
	/**
	 * Root node of the trie.
	 */
	private Node rootNode;
	
	/**
	 * Constructor
	 * @param mode the mode of this trie
	 */
	public Trie() {
		this.setRootNode(null);
	}
	
	/**
	 * Creates a new node instance, depending on what kind of trie instance this is.
	 * 
	 * @return An instance of either RBNode, RBNodeWithValue, DBNode, DBNodeWithValue, DBSeqNode or DBSeqNodeWithValue
	 */
	protected abstract Node createNodeInstance();
	
	/**
	 * Creates a new root node instance, depending on what kind of trie instance this is.
	 * @return An instance of either RBNode, RBNodeWithValue, DBNode, DBNodeWithValue, DBSeqNode or DBSeqNodeWithValue
	 */
	protected abstract Node createRootNodeInstance();
	
	/**
	 * Changes the current root node.
	 * 
	 * @param rootNode_param
	 *            The new root node
	 */
	protected abstract void changeRootNode(Node rootNode_param);
	
	/**
	 * Deletes all nodes contained within this trie.
	 */
	public void clear() {
		if (this.getRootNode() != null) {
			this.getRootNode().destroyNode(true);
			this.setRootNode(null);			
		}
	}
	
	/**
	 * Releases the trie. In most cases it should not be used anymore after
	 * calling this method.
	 */
	public void release() {
		this.setRootNode(null);
	}
	
	/**
	 * @return <strong>true</strong> if this trie contains all metadata like
	 *         numberOfEntries for each inner node.<br />
	 *         <strong>false</strong> if this trie is missing important
	 *         metadata, which prevents this trie from being copied or merged
	 */
	public boolean hasCompleteMetadata() {
		return true;
	}
	
	/**
	 * Retrieves the <i>index</i>-th key in lexicographical order from the trie
	 * 
	 * @param index
	 *            Index of the key
	 * @return The key
	 */
	public String get(final int index) {
		if (this.getRootNode() == null){
			return null;
		}
		
		if (this.getRootNode().getNumberOfEntries() < index - 1){
			return null;
		}

		return NodeHelper.get(this.getRootNode(), index);
	}
		
	/**
	 * Returns the index of the key
	 * 
	 * @param key
	 *            Key to get index for
	 * @return Index of the key
	 */
	public int getIndex(final String key) {
		if (this.getRootNode() == null){
			return -1;
		}

		return NodeHelper.getIndex(this.getRootNode(), key);
	}
	
	/**
	 * Removes a key from the trie.
	 * 
	 * @param key
	 *            Key to remove
	 * @return <strong>false</strong> if the key could not be removed (if the trie did not contain that key),
	 *         <strong>true</strong> otherwise.
	 */
	public boolean remove(final String key) {
		if (this.getRootNode() == null) {
			return false;
		}
		
		return NodeHelper.remove(this.getRootNode(), key);
	}
		
	/**
	 * Prepares this trie for reading. In most cases, there is nothing to do,
	 * but certain Tries may require a stream reset.
	 */
	protected void prepareForReading() {
		// nothing to do per default
	}
		
	/**
	 * @return Number of entries in this trie
	 */
	public int size() {
		if (this.getRootNode() == null){
			return 0;
		} else {
			return this.getRootNode().getNumberOfEntries();
		}
	}
	
	/**
	 * @return Number of nodes in this trie
	 */
	public int getNodeCount() {
		if (this.getRootNode() == null){
			return 0;
		} else {
			return this.getRootNode().getNodeCount();
		}
	}
	
	@Override
	public String toString() {
		if (this.getRootNode() != null){
			return this.getRootNode().toString();
		} else {
			return "";
		}
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Trie)){
			return false;
		}
		
		final Trie trie = (Trie) obj;
		
		if(!((this instanceof TrieSet && trie instanceof TrieSet)
				|| (this instanceof TrieBag && trie instanceof TrieBag)
				|| (this instanceof TrieMap && trie instanceof TrieMap))){
			return false;
		}
		
		if ((this.getRootNode() == null) ^ (trie.getRootNode() == null)){ // ^ = OR Exclusive (XOR)!
			return false;
		}
		
		if (this.getRootNode() != null){
			return this.getRootNode().equals(trie.getRootNode());
		} else {
			return true;
		}
	}

	/**
	 * @param rootNode the rootNode to set
	 */
	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}

	/**
	 * @return the rootNode
	 */
	public Node getRootNode() {
		return this.rootNode;
	}
}
