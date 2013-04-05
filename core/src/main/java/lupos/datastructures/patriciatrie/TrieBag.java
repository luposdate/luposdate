/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
import java.util.Map.Entry;

import lupos.datastructures.patriciatrie.disk.DBTrieBag;
import lupos.datastructures.patriciatrie.exception.TrieNotMergeableException;
import lupos.datastructures.patriciatrie.node.NodeHelper;
import lupos.datastructures.patriciatrie.node.NodeWithValue;
import lupos.datastructures.patriciatrie.ram.RBTrieBag;

public abstract class TrieBag extends TrieWithValue<Integer> {

	/**
	 * Create a new main memory based trie
	 * @return the newly created main memory based trie set
	 */
	public static RBTrieBag createRamBasedTrieBag(){
		return new RBTrieBag();
	}
	
	/**
	 * Creates a new disk based trie with the default buffer size
	 * 
	 * @param fileName
	 *            Base filename for the trie
	 * @return the newly created disk based trie set
	 * @throws IOException
	 */
	public static DBTrieBag createDiskBasedTrieBag(final String fileName) throws IOException {
		return new DBTrieBag(fileName);
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
	public static DBTrieBag createDiskBasedTrieSet(final String fileName, final int bufferSize, final int pageSize) throws IOException {
		return new DBTrieBag(fileName, bufferSize, pageSize);
	}	
	
	/**
	 * Adds a key to the trie.
	 * 
	 * @param key
	 *            Key to add
	 * @return <strong>false</strong> if the key could not be added (if the trie already contained that key),
	 *         <strong>true</strong> otherwise.
	 */
	@SuppressWarnings("unchecked")
	public boolean add(final String key) {
		
		if (this.getRootNode() == null) {
			this.setRootNode(createNodeInstance());
		}
		
		return NodeHelper.addToBag((NodeWithValue<Integer>)this.getRootNode(), key);
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
	@SuppressWarnings("unchecked")
	protected void merge(final List<TrieBag> tries, final boolean checkMetadata) throws TrieNotMergeableException {
		final List<NodeWithValue<Integer>> nodesToMerge = new ArrayList<NodeWithValue<Integer>>(tries.size());
		
		// Only add valid root nodes
		for (final TrieBag t : tries) {
			if (checkMetadata && !t.hasCompleteMetadata()){
				throw new TrieNotMergeableException();
			} else {
				t.prepareForReading();
				if (t.getRootNode() != null){
					nodesToMerge.add((NodeWithValue<Integer>)t.getRootNode());
				}
			}
		}
		
		// Only do something, if anything mergeable is available
		if (nodesToMerge.size() > 0) {
			
			// Add our own root node
			if (this.getRootNode() != null){
				nodesToMerge.add((NodeWithValue<Integer>)this.getRootNode());
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
				final NodeWithValue<Integer> root = this.createNodeInstance();
	
				this.mergeAfterCheck(root, nodesToMerge);
				
				this.changeRootNode(root);
			}
		}
	}
	
	/**
	 * This method is overwritten by DBSeqTrieBag
	 * @param root the new root
	 * @param nodesToMerge the nodes to be merged
	 */
	protected void mergeAfterCheck(final NodeWithValue<Integer> root, final List<NodeWithValue<Integer>> nodesToMerge){
		NodeHelper.mergeBag(root, nodesToMerge);
	}
	
	/**
	 * Merges a list of tries into this trie.
	 * 
	 * @param tries
	 *            List of tries
	 * @throws TrieNotMergeableException
	 */
	public void merge(final List<TrieBag> tries) throws TrieNotMergeableException {
		this.merge(tries, true);
	}
	
	/**
	 * @return an iterator to iterate trough the keys inside the patricia trie (returning also duplicates)
	 */
	public Iterator<String> keyIterator() {
		final Iterator<Entry<String, Integer>> entryIterator = this.iterator();
		return new Iterator<String>(){
			
			int duplicatesLeft = 0;
			String currentKey = null;

			@Override
			public boolean hasNext() {				
				return this.duplicatesLeft>0 || entryIterator.hasNext();
			}

			@Override
			public String next() {
				if(this.duplicatesLeft==0){
					Entry<String, Integer> entry = entryIterator.next();
					if(entry==null){
						return null;
					}
					this.currentKey = entry.getKey();
					this.duplicatesLeft = entry.getValue();
				}				
				this.duplicatesLeft--;
				return this.currentKey;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}			
		};
	}

}