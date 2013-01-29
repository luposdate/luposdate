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
package lupos.datastructures.patriciatrie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import lupos.datastructures.patriciatrie.disk.DBTrieSet;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.patriciatrie.exception.TrieNotMergeableException;
import lupos.datastructures.patriciatrie.node.Node;
import lupos.datastructures.patriciatrie.node.NodeHelper;
import lupos.datastructures.patriciatrie.ram.RBTrieSet;
import lupos.misc.Tuple;

public abstract class TrieSet extends Trie implements Iterable<String> {

	/**
	 * Create a new main memory based trie
	 * @return the newly created main memory based trie set
	 */
	public static RBTrieSet createRamBasedTrieSet(){
		return new RBTrieSet();
	}
	
	/**
	 * Creates a new disk based trie with the default buffer size
	 * 
	 * @param fileName
	 *            Base filename for the trie
	 * @return the newly created disk based trie set
	 * @throws IOException
	 */
	public static DBTrieSet createDiskBasedTrieSet(final String fileName) throws IOException {
		return new DBTrieSet(fileName);
	}
	
	/**
	 * Creates a new trie
	 * 
	 * @param fileName
	 *            Base filename for the trie
	 * @param bufferSize
	 *            Amount of nodes that are simultaneously kept in memory
	 * @param pageSize
	 *            The size of a page to be stored on disk
	 * @return the newly created disk based trie set
	 * @throws IOException
	 */
	public static DBTrieSet createDiskBasedTrieSet(final String fileName, final int bufferSize, final int pageSize) throws IOException {
		return new DBTrieSet(fileName, bufferSize, pageSize);
	}
	
	/**
	 * Deletes all nodes contained within this trie and copies all nodes from
	 * the other trie.
	 * 
	 * The main purpose of this method is to convert one trie type into another
	 * (e.g. RAM-based to disk-based)
	 * 
	 * @param trie
	 *            Trie to copy
	 * @throws TrieNotCopyableException 
	 */
	public void copy(final Trie trie) throws TrieNotCopyableException {
		if (!trie.hasCompleteMetadata())
			throw new TrieNotCopyableException();
		
		this.clear();
		
		trie.prepareForReading();
		
		if (trie.getRootNode() != null) {
			this.setRootNode(this.createRootNodeInstance());
			
			this.getRootNode().setContent(new String[trie.getRootNode().getContentLength()]);
			
			this.getRootNode().setNumberOfEntries(trie.getRootNode().getNumberOfEntries());
			
			for (int j = 0; j < trie.getRootNode().getContentLength(); j++) {
				this.getRootNode().setContent(j, trie.getRootNode().getContent(j));
			}
			
			for (int j = 0; j < trie.getRootNode().getChildrenLength(); j++) {
				NodeHelper.setChildCopy(this.getRootNode(), j, trie.getRootNode().getChild(j));
			}
			
			this.getRootNode().setChanged(true);
		}
	}
	
	/**
	 * Adds a key to the trie.
	 * 
	 * @param key
	 *            Key to add
	 * @return <strong>false</strong> if the key could not be added (if the trie already contained that key),
	 *         <strong>true</strong> otherwise.
	 */
	public boolean add(final String key) {
		
		if (this.getRootNode() == null) {
			this.setRootNode(createNodeInstance());
		}
		
		return NodeHelper.addToSet(this.getRootNode(), key);
	}
	
	/**
	 * Merges a list of tries into this trie.
	 * 
	 * @param tries
	 *            List of tries
	 * @param checkMetadata
	 *            When set, a TrieNotMergeableException is thrown, when one or
	 *            more tries have missing metadata.
	 * @throws TrieNotMergeableException
	 */
	protected void merge(final List<Trie> tries, final boolean checkMetadata) throws TrieNotMergeableException {
		final List<Node> nodesToMerge = new ArrayList<Node>(tries.size());
		
		// Only add valid root nodes
		for (final Trie t : tries) {
			if (checkMetadata && !t.hasCompleteMetadata()){
				throw new TrieNotMergeableException();
			} else {
				t.prepareForReading();
				if (t.getRootNode() != null){
					nodesToMerge.add(t.getRootNode());
				}
			}
		}
		
		// Only do something, if anything mergeable is available
		if (nodesToMerge.size() > 0) {
			
			// Add our own root node
			if (this.getRootNode() != null){
				nodesToMerge.add(this.getRootNode());
			}
			
			// If there is only one valid root node to merge, skip merging and use
			// this as result
			if (nodesToMerge.size() == 1) {
				// 
				if (nodesToMerge.get(0) != this.getRootNode()) {
					System.err.println("Please do not use merge for one trie, use copy instead!");
					this.setRootNode(nodesToMerge.get(0)); // TODO Hier muss kopiert werden, nicht einfach der rootNode uebernommen
				}
			}
			
			// Only merge if there are at least 2 valid root nodes
			else if (nodesToMerge.size() > 1) {
				final Node root = this.createNodeInstance();
	
				this.mergeAfterCheck(root, nodesToMerge);
				
				this.changeRootNode(root);
			}
		}
	}
	
	/**
	 * This method is overwritten by DBSeqTrieSet
	 * @param root the new root node
	 * @param nodesToMerge the nodes to be merged
	 */
	protected void mergeAfterCheck(final Node root, final List<Node> nodesToMerge){
		NodeHelper.mergeSet(root, nodesToMerge);
	}
	
	/**
	 * Merges a list of tries into this trie.
	 * 
	 * @param tries
	 *            List of tries
	 * @throws TrieNotMergeableException
	 */
	public void merge(final List<Trie> tries) throws TrieNotMergeableException {
		this.merge(tries, true);
	}
	
	@Override
	public final Iterator<String> iterator() {
		this.prepareForReading();
		if (this.getRootNode() == null) {
			return new Iterator<String>() {
				
				@Override
				public boolean hasNext() {
					return false;
				}
	
				@Override
				public String next() {
					return null;
				}
	
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		else {
			return new Iterator<String>() {
				
				private Stack<Tuple<Node, Integer>> nodeIndexStack;
				private String currentPrefix;
				private Node currentNode;
				private int currentNodeIndex;
				private int indexCounter;
				
				// Anonymous constructor
				{
					this.nodeIndexStack = new Stack<Tuple<Node, Integer>>();
					this.nodeIndexStack.push(new Tuple<Node, Integer>(TrieSet.this.getRootNode(), 0));
					this.currentNode = TrieSet.this.getRootNode();
					this.currentNodeIndex = 0;
					this.indexCounter = 0;
					this.currentPrefix = "";
				}

				@Override
				public boolean hasNext() {
					return this.indexCounter < size();
				}

				@Override
				public String next() {
					String result = null;
					
					// No more entries left
					if (this.indexCounter < size()) {
						// Use the current node
						if (this.currentNodeIndex < this.currentNode.getContentLength()) {
							while (this.currentNode.hasChild(this.currentNodeIndex)) {
								this.currentPrefix += this.currentNode.getContent(this.currentNodeIndex);
								this.nodeIndexStack.push(new Tuple<Node, Integer>(this.currentNode, this.currentNodeIndex));
								this.currentNode = this.currentNode.getChild(this.currentNodeIndex);
								this.currentNodeIndex = 0;
							}
							
							result = this.currentPrefix + this.currentNode.getContent(this.currentNodeIndex);
							
							this.currentNodeIndex++;
							this.indexCounter++;
							
							return result;
						} else {
							final Tuple<Node, Integer> entry = this.nodeIndexStack.pop();
							this.currentPrefix = this.currentPrefix.substring(0, this.currentPrefix.length() - entry.getFirst().getContent(entry.getSecond()).length());
							this.currentNode = entry.getFirst();
							this.currentNodeIndex = entry.getSecond() + 1;
							
							return next();
						}
					} else {
						return null;
					}					
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
		}
	}
}
