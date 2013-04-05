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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.patriciatrie.node.NodeHelper;
import lupos.datastructures.patriciatrie.node.NodeWithValue;
import lupos.misc.Tuple;

public abstract class TrieWithValue<T> extends Trie implements Iterable<Entry<String, T>> {
	
	@Override
	protected abstract NodeWithValue<T> createNodeInstance();

	@Override
	protected abstract NodeWithValue<T> createRootNodeInstance();

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
	@SuppressWarnings("unchecked")
	public void copy(final TrieWithValue<T> trie) throws TrieNotCopyableException {
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
			
			NodeWithValue<T> rootCasted = (NodeWithValue<T>) this.getRootNode();
			NodeWithValue<T> otherRootCasted = (NodeWithValue<T>) trie.getRootNode();
			
			for (int j = 0; j < trie.getRootNode().getChildrenLength(); j++) {
				NodeHelper.setChildCopy(rootCasted, j, otherRootCasted.getChild(j));
			}
			
			rootCasted.setValues((T[]) new Object[otherRootCasted.getValuesLength()]);
				
			for (int j = 0; j < otherRootCasted.getValuesLength(); j++) {
				rootCasted.setValue(j, otherRootCasted.getValue(j));
			}
			
			this.getRootNode().setChanged(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Entry<String, T>> iterator() {
		this.prepareForReading();
		if (this.getRootNode() == null) {
			return new Iterator<Entry<String, T>>() {
				
				@Override
				public boolean hasNext() {
					return false;
				}
	
				@Override
				public Entry<String, T> next() {
					return null;
				}
	
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		else {
			return new Iterator<Entry<String, T>>() {
				
				private Stack<Tuple<NodeWithValue<T>, Integer>> nodeIndexStack;
				private String currentPrefix;
				private NodeWithValue<T> currentNode;
				private int currentNodeIndex;
				private int indexCounter;
				
				// Anonymous constructor
				{
					this.nodeIndexStack = new Stack<Tuple<NodeWithValue<T>, Integer>>();
					this.nodeIndexStack.push(new Tuple<NodeWithValue<T>, Integer>((NodeWithValue<T>)TrieWithValue.this.getRootNode(), 0));
					this.currentNode = (NodeWithValue<T>)TrieWithValue.this.getRootNode();
					this.currentNodeIndex = 0;
					this.indexCounter = 0;
					this.currentPrefix = "";
				}

				@Override
				public boolean hasNext() {
					return this.indexCounter < size();
				}

				@Override
				public Entry<String, T> next() {
					String result = null;
					
					// No more entries left
					if (this.indexCounter < size()) {
						// Use the current node
						if (this.currentNodeIndex < this.currentNode.getContentLength()) {
							while (this.currentNode.hasChild(this.currentNodeIndex)) {
								this.currentPrefix += this.currentNode.getContent(this.currentNodeIndex);
								this.nodeIndexStack.push(new Tuple<NodeWithValue<T>, Integer>(this.currentNode, this.currentNodeIndex));
								this.currentNode = this.currentNode.getChild(this.currentNodeIndex);
								this.currentNodeIndex = 0;
							}
							
							result = this.currentPrefix + this.currentNode.getContent(this.currentNodeIndex);
							Entry<String, T> entry = new MapEntry<String, T>(result, this.currentNode.getValue(this.currentNodeIndex));
							
							this.currentNodeIndex++;
							this.indexCounter++;
							
							return entry;
						} else {
							final Tuple<NodeWithValue<T>, Integer> entry = this.nodeIndexStack.pop();
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
