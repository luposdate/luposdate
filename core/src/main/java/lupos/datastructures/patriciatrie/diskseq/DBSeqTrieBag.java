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
package lupos.datastructures.patriciatrie.diskseq;

import java.util.List;

import lupos.datastructures.patriciatrie.TrieBag;
import lupos.datastructures.patriciatrie.TrieWithValue;
import lupos.datastructures.patriciatrie.diskseq.nodemanager.SeqNodeManager;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.patriciatrie.exception.TrieNotMergeableException;
import lupos.datastructures.patriciatrie.node.Node;
import lupos.datastructures.patriciatrie.node.NodeHelper;
import lupos.datastructures.patriciatrie.node.NodeWithValue;

/**
 * This class implements a special disk based behavior for a trie for bag of strings.
 *
 * It can be used to store intermediary results on disk, that can be merged into
 * a DBTrie or a RBTrie later.
 *
 * However, because of its underlying data structure, it is not suited for the
 * basic operations add/remove.
 *
 * Merging is possible, but it breaks to possibility to convert this trie back
 * into a RBTrie or DBTrie, because the values of numberOfEntries for the
 * children will be wrong.
 */
public class DBSeqTrieBag extends TrieBag {

	/**
	 * Name of the file, that holds this trie.
	 */
	protected String fileName;

	/**
	 * Node Manager that manages write and read operations.
	 */
	protected SeqNodeManager nodeManager;

	/**
	 * Constructor
	 *
	 * @param fileName
	 *            Name of the file for this Trie
	 *
	 */
	public DBSeqTrieBag(final String fileName) {
		super();
		this.fileName = fileName;
		this.nodeManager = new SeqNodeManager(fileName);
	}

	@Override
	public void copy(final TrieWithValue<Integer> trie) throws TrieNotCopyableException {
		if (!trie.hasCompleteMetadata()){
			throw new TrieNotCopyableException();
		}

		this.nodeManager.clear();
		this.nodeManager.writeNextNodeRecursive(DBSeqNodeWithValue.deSerializer, trie.getRootNode());

		this.prepareForReading();
	}

	/**
	 * Resets the InputStream, that is responsible for the retrieval of the nodes from the underlying filesystem.
	 *
	 * This method must be called before trying to merge this trie into another.
	 */
	@Override
	protected void prepareForReading() {
		this.nodeManager.readAgain();

		this.setRootNode(this.nodeManager.readNextNode(DBSeqNodeWithValue.deSerializer));
	}

	@Override
	public void release() {
		this.nodeManager.close();
		this.nodeManager.release();
		this.nodeManager = null;
		super.release();
	}

	@Override
	protected NodeWithValue<Integer> createNodeInstance() {
		return new DBSeqNodeWithValue<Integer>(this.nodeManager);
	}

	@Override
	protected NodeWithValue<Integer> createRootNodeInstance() {
		throw new UnsupportedOperationException();	}

	@Override
	protected void changeRootNode(final Node rootNodeParam) {
		this.nodeManager.writeRootNodeAgain(DBSeqNode.deSerializer, rootNodeParam);
	}

	@Override
	public void clear() {
		// TODO Implementieren
	}

	@Override
	public boolean add(final String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String get(final int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getIndex(final String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(final String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void merge(final List<TrieBag> tries) throws TrieNotMergeableException {
		/*
		 * This trie must be empty, because the file, that contains the original
		 * elements, will be overwritten during the merge process. If this trie
		 * was not empty, there would be unpredictable side effects because the
		 * Input- and OutputStreams will be continuously reset.
		 */
		if (this.size() > 0){
			System.err.println("Warning: Calling DBSeqTrie.merge() on a non-empty DBSeqTrie does not work. All entries from this DBSeqTrie will be removed before merging.");
		}
		this.setRootNode(null);

		this.nodeManager.setCompleteMetadata(false);
		this.nodeManager.clear();
		this.merge(tries, false);
	}

	@Override
	public boolean hasCompleteMetadata() {
		return this.nodeManager.hasCompleteMetadata();
	}

	@Override
	public int getNodeCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void mergeAfterCheck(final NodeWithValue<Integer> root, final List<NodeWithValue<Integer>> nodesToMerge){
		NodeHelper.mergeSeqBag((DBSeqNodeWithValue<Integer>)root, nodesToMerge);
	}
}
