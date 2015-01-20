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
package lupos.datastructures.patriciatrie.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import lupos.datastructures.patriciatrie.diskseq.DBSeqNode;
import lupos.datastructures.patriciatrie.diskseq.DBSeqNodeWithValue;
import lupos.datastructures.patriciatrie.ram.RBNode;
import lupos.datastructures.patriciatrie.ram.RBNodeWithValue;
import lupos.misc.Triple;
import lupos.misc.Tuple;

public final class NodeHelper {

	/**
	 * Adds a key to the node.
	 *
	 * @param node
	 *            the node to which the key is added
	 * @param key
	 *            Key to add
	 * @return <strong>false</strong> if the key could not be added (if the node already contained that key) <br />
	 *         <strong>true</strong> otherwise.
	 */
	public final static boolean addToSet(final Node node, final String key) {
		final int keyLength = key.length();

		// Search for a merge partner
		final Tuple<Boolean, Integer> idx = NodeHelper.searchMergePartner(node, key);

		if (!idx.getFirst()) {
			node.increaseArraySizes(idx.getSecond(), 1);
			node.modifyNumberOfEntries(1);
			node.setContent(idx.getSecond(), key);

			return true;
		}

		// The empty string already exists
		if (keyLength == 0) {
			return false;
		}

		final String prefixKey = node.getContent(idx.getSecond());
		final int prefixKeyLength = prefixKey.length();
		final int commonPrefixLength = NodeHelper.findLongestCommonPrefix(0, key, prefixKey);

		if (commonPrefixLength == prefixKeyLength) {
			if (node.hasChild(idx.getSecond())) {
				node.setIsOnRecursionStack(true);
				final boolean added = NodeHelper.addToSet(node.getChild(idx.getSecond()),key.substring(commonPrefixLength));
				node.setIsOnRecursionStack(false);
				if (added){
					node.modifyNumberOfEntries(1);
				}

				return added;
			}
			else {
				// There is more to add
				if (keyLength > prefixKeyLength) {
					final Node newChild = node.createNode();
					newChild.increaseArraySizes(0, 2);
					newChild.modifyNumberOfEntries(2);
					newChild.setContent(0, "");
					newChild.setContent(1, key.substring(commonPrefixLength));
					node.setChild(idx.getSecond(), newChild);
					node.modifyNumberOfEntries(1);

					return true;
				}
				else {
					// string already exists!
					return false;
				}
			}
		}

		final String keySuffix = key.substring(commonPrefixLength);
		final String prefixKeySuffix = prefixKey.substring(commonPrefixLength);
		final int compare = keySuffix.compareTo(prefixKeySuffix);

		final Node newChild = node.createNode();
		final Node childChild = node.getChild(idx.getSecond());

		newChild.increaseArraySizes(0, 2);
		newChild.modifyNumberOfEntries(childChild == null ? 2 : childChild.numberOfEntries + 1);

		if (compare < 0) {
			newChild.setContent(0, keySuffix);
			newChild.setContent(1, prefixKeySuffix);
			newChild.setChild(1, node.getChild(idx.getSecond()));
		}
		else {
			newChild.setContent(0, prefixKeySuffix);
			newChild.setChild(0, node.getChild(idx.getSecond()));
			newChild.setContent(1, keySuffix);
		}

		node.setContent(idx.getSecond(), node.getContent(idx.getSecond()).substring(0, commonPrefixLength));
		node.setChild(idx.getSecond(), newChild);
		node.modifyNumberOfEntries(1);

		return true;
	}

	/**
	 * Adds a key to the node.
	 *
	 * @param node
	 *            the node to which the key is added
	 * @param key
	 *            Key to add
	 * @return <strong>false</strong> if the key could not be added (if the node already contained that key) <br />
	 *         <strong>true</strong> otherwise.
	 */
	public final static boolean addToBag(final NodeWithValue<Integer> node, final String key) {
		final int keyLength = key.length();

		// Search for a merge partner
		final Tuple<Boolean, Integer> idx = NodeHelper.searchMergePartner(node, key);

		if (!idx.getFirst()) {
			node.increaseArraySizes(idx.getSecond(), 1);
			node.modifyNumberOfEntries(1);
			node.setContent(idx.getSecond(), key);

			// introduce counter!
			node.setValue(idx.getSecond(), new Integer(1));

			return true;
		}

		// The empty string already exists
		if (keyLength == 0) {
			// increase counter!
			final Integer current = 1 + node.getValue(idx.getSecond());
			node.setValue(idx.getSecond(), current);
			return false;
		}

		final String prefixKey = node.getContent(idx.getSecond());
		final int prefixKeyLength = prefixKey.length();
		final int commonPrefixLength = NodeHelper.findLongestCommonPrefix(0, key, prefixKey);

		if (commonPrefixLength == prefixKeyLength) {
			if (node.hasChild(idx.getSecond())) {
				node.setIsOnRecursionStack(true);
				final boolean added = NodeHelper.addToBag(node.getChild(idx.getSecond()),key.substring(commonPrefixLength));
				node.setIsOnRecursionStack(false);
				if (added){
					node.modifyNumberOfEntries(1);
				}

				return added;
			}
			else {
				// There is more to add
				if (keyLength > prefixKeyLength) {
					final NodeWithValue<Integer> newChild = node.createNode();
					newChild.increaseArraySizes(0, 2);
					newChild.modifyNumberOfEntries(2);
					newChild.setContent(0, "");
					newChild.setContent(1, key.substring(commonPrefixLength));
					node.setChild(idx.getSecond(), newChild);
					node.modifyNumberOfEntries(1);

					// introduce counter!
					newChild.setValue(1, new Integer(1));
					// copy counter for the other
					newChild.setValue(0, node.getValue(idx.getSecond()));
					// delete counter at the old place...
					node.setValue(idx.getSecond(), null);

					return true;
				}
				else {
					// string already exists!
					// increase counter!
					final Integer current = 1 + node.getValue(idx.getSecond());
					node.setValue(idx.getSecond(), current);

					return false;
				}
			}
		}

		final String keySuffix = key.substring(commonPrefixLength);
		final String prefixKeySuffix = prefixKey.substring(commonPrefixLength);
		final int compare = keySuffix.compareTo(prefixKeySuffix);

		final NodeWithValue<Integer> newChild = node.createNode();
		final Node childChild = node.getChild(idx.getSecond());

		newChild.increaseArraySizes(0, 2);
		newChild.modifyNumberOfEntries(childChild == null ? 2 : childChild.numberOfEntries + 1);

		if (compare < 0) {
			newChild.setContent(0, keySuffix);
			newChild.setContent(1, prefixKeySuffix);
			newChild.setChild(1, node.getChild(idx.getSecond()));
			// introduce counter!
			newChild.setValue(0, new Integer(1));
			// copy counter!
			newChild.setValue(1, node.getValue(idx.getSecond()));
		}
		else {
			newChild.setContent(0, prefixKeySuffix);
			newChild.setChild(0, node.getChild(idx.getSecond()));
			newChild.setContent(1, keySuffix);
			// introduce counter!
			newChild.setValue(1, new Integer(1));
			// copy counter!
			newChild.setValue(0, node.getValue(idx.getSecond()));
		}

		node.setValue(idx.getSecond(), null); // remove counter (no leaf node any longer!)

		node.setContent(idx.getSecond(), node.getContent(idx.getSecond()).substring(0, commonPrefixLength));
		node.setChild(idx.getSecond(), newChild);
		node.modifyNumberOfEntries(1);

		return true;
	}

	/**
	 * puts a value of a key to the node.
	 *
	 * @param Node
	 *            the node in which the value is putted
	 * @param key
	 *            Key for the value
	 * @param value
	 *            Value of the key
	 * @return <strong>null</strong> if the key could not be removed (if the trie did not contain that key),
	 *         the old value otherwise (inside a Tuple-object to distinguish case that the the value itself is a null value).
	 */
	public final static<T> Tuple<T, Boolean> put(final NodeWithValue<T> node, final String key, final T value) {
		final int keyLength = key.length();

		// Search for a merge partner
		final Tuple<Boolean, Integer> idx = NodeHelper.searchMergePartner(node, key);

		if (!idx.getFirst()) {
			node.increaseArraySizes(idx.getSecond(), 1);
			node.modifyNumberOfEntries(1);
			node.setContent(idx.getSecond(), key);

			// !!!!!!!!!!!!!!!!!!
			node.setValue(idx.getSecond(), value);
			// !!!!!!!!!!!!!!!!!!

			return null;
		}

		// The empty string already exists
		if (keyLength == 0) {
			final T oldValue = node.getValue(idx.getSecond());
			// !!!!!!!!!!!!!!!!!!
			// overwrite existing value
			node.setValue(idx.getSecond(), value);
			// !!!!!!!!!!!!!!!!!!
			return new Tuple<T, Boolean>(oldValue, Boolean.TRUE);
		}

		final String prefixKey = node.getContent(idx.getSecond());
		final int prefixKeyLength = prefixKey.length();
		final int commonPrefixLength = findLongestCommonPrefix(0, key, prefixKey);

		if (commonPrefixLength == prefixKeyLength) {
			if (node.hasChild(idx.getSecond())) {
				node.setIsOnRecursionStack(true);
				final Tuple<T, Boolean> oldValue = NodeHelper.put(node.getChild(idx.getSecond()),key.substring(commonPrefixLength), value);
				node.setIsOnRecursionStack(false);
				if (oldValue==null){
					node.modifyNumberOfEntries(1);
				}

				return oldValue;
			}
			else {
				// There is more to add
				if (keyLength > prefixKeyLength) {
					final NodeWithValue<T> newChild = node.createNode();
					newChild.increaseArraySizes(0, 2);
					newChild.modifyNumberOfEntries(2);
					newChild.setContent(0, "");
					newChild.setContent(1, key.substring(commonPrefixLength));
					node.setChild(idx.getSecond(), newChild);
					node.modifyNumberOfEntries(1);

					// !!!!!!!!!!!!!!!!!!
					// set value
					newChild.setValue(1, value);
					// copy value for the other
					newChild.setValue(0, node.getValue(idx.getSecond()));
					// delete value at the old place...
					node.setValue(idx.getSecond(), null);
					// !!!!!!!!!!!!!!!!!!

					return null;
				}
				else {
					// string already exists!
					final T oldValue = node.getValue(idx.getSecond());
					// !!!!!!!!!!!!!!!!!!
					// overwrite existing value
					node.setValue(idx.getSecond(), value);
					// !!!!!!!!!!!!!!!!!!

					return new Tuple<T, Boolean>(oldValue, Boolean.TRUE);
				}
			}
		}

		final String keySuffix = key.substring(commonPrefixLength);
		final String prefixKeySuffix = prefixKey.substring(commonPrefixLength);
		final int compare = keySuffix.compareTo(prefixKeySuffix);

		final NodeWithValue<T> newChild = node.createNode();
		final Node childChild = node.getChild(idx.getSecond());

		newChild.increaseArraySizes(0, 2);
		newChild.modifyNumberOfEntries(childChild == null ? 2 : childChild.numberOfEntries + 1);

		if (compare < 0) {
			newChild.setContent(0, keySuffix);
			newChild.setContent(1, prefixKeySuffix);
			newChild.setChild(1, node.getChild(idx.getSecond()));
			// !!!!!!!!!!!!!!!!!!
			// put value
			newChild.setValue(0, value);
			// copy value
			newChild.setValue(1, node.getValue(idx.getSecond()));
			// !!!!!!!!!!!!!!!!!!
		}
		else {
			newChild.setContent(0, prefixKeySuffix);
			newChild.setChild(0, node.getChild(idx.getSecond()));
			newChild.setContent(1, keySuffix);
			// !!!!!!!!!!!!!!!!!!
			// put value
			newChild.setValue(1, value);
			// copy value
			newChild.setValue(0, node.getValue(idx.getSecond()));
			// !!!!!!!!!!!!!!!!!!
		}

		// !!!!!!!!!!!!!!!!!!
		node.setValue(idx.getSecond(), null); // remove value (no leaf node any longer!)
		// !!!!!!!!!!!!!!!!!!
		node.setContent(idx.getSecond(), node.getContent(idx.getSecond()).substring(0, commonPrefixLength));
		node.setChild(idx.getSecond(), newChild);
		node.modifyNumberOfEntries(1);

		return null;
	}

	/**
	 * Returns the <i>index</i>-th key in lexicographical order.
	 *
	 * @param node
	 *            The node from which we want to get the i-th key
	 * @param index
	 *            Index of the key
	 * @return The key
	 */
	public final static String get(final Node node, final int index) {
		int localIndex = 0;
		Node currentNode = null;

		for (int i = 0, j = node.getContentLength(); i < j; i++) {
			if (node.hasChild(i)) {
				currentNode = node.getChild(i);
				if (localIndex <= index && localIndex + currentNode.numberOfEntries > index){
					return node.getContent(i) + NodeHelper.get(currentNode, index - localIndex);
				}

				localIndex += currentNode.numberOfEntries;
			}
			else {
				if (localIndex == index){
					return node.getContent(i);
				}
				localIndex++;
			}
		}

		System.err.println("lupos.datastructures.patriciatrie.Node.get(): This should not happen");
		return null;
	}

	/**
	 * Returns the index of the key
	 *
	 * @param node
	 *            the node
	 * @param key
	 *            Key to get index for
	 * @return Index of the key
	 */
	public final static int getIndex(final Node node, final String key) {
		final Tuple<Boolean, Integer> mergePartner = NodeHelper.searchMergePartner(node, key);

		if (!mergePartner.getFirst()){
			return -1;
		} else {
			int offset = 0;

			for (int i = 0; i < mergePartner.getSecond(); i++){
				if (node.hasChild(i)){
					offset += node.getChild(i).numberOfEntries;
				} else {
					offset += 1;
				}
			}

			final String prefix = node.getContent(mergePartner.getSecond());

			if (node.hasChild(mergePartner.getSecond())) {
				final int commonPrefixLength = findLongestCommonPrefix(0, key, prefix);

				if (commonPrefixLength < prefix.length()){
					return -1;
				} else {
					final int idx = NodeHelper.getIndex(node.getChild(mergePartner.getSecond()), key.substring(commonPrefixLength));
					return idx == -1 ? -1 : offset + idx;
				}
			}
			else if (prefix.equals(key)) {
				return offset;
			}
		}

		return -1;
	}

	/**
	 * Returns the value of the key
	 *
	 * @param node
	 *            The node
	 * @param key
	 *            Key to get value for
	 * @return Value of the key (or null if the key does not exist)
	 */
	public final static<T> T get(final NodeWithValue<T> node, final String key) {
		final Tuple<Boolean, Integer> mergePartner = NodeHelper.searchMergePartner(node, key);

		if (!mergePartner.getFirst()){
			return null;
		} else {

			final String prefix = node.getContent(mergePartner.getSecond());

			if (node.hasChild(mergePartner.getSecond())) {
				final int commonPrefixLength = findLongestCommonPrefix(0, key, prefix);

				if (commonPrefixLength < prefix.length()){
					return null;
				} else {
					return NodeHelper.get(node.getChild(mergePartner.getSecond()), key.substring(commonPrefixLength));
				}
			} else if (prefix.equals(key)) {
				return node.getValue(mergePartner.getSecond());
			}
		}
		return null;
	}


	/**
	 * Removes a key from the node.
	 *
	 * @param Node
	 *            the node
	 * @param key
	 *            Key to remove
	 * @return <strong>false</strong> if the key could not be removed (if the node did not contain that key)<br />
	 *         <strong>true</strong> otherwise.
	 */
	public final static boolean remove(final Node node, final String key) {
		final Tuple<Boolean, Integer> idx = NodeHelper.searchMergePartner(node, key);

		if (!idx.getFirst()) {
			return false;
		}

		final String prefixKey = node.getContent(idx.getSecond());
		final int prefixKeyLength = prefixKey.length();
		final int commonPrefixLength = NodeHelper.findLongestCommonPrefix(1, key, prefixKey) + 1;

		if (commonPrefixLength < prefixKeyLength) {
			return false;
		}

		if (node.hasChild(idx.getSecond())) {
			final Node childNode = node.getChild(idx.getSecond());
			node.setIsOnRecursionStack(true);
			if (NodeHelper.remove(childNode, key.substring(commonPrefixLength))) {
				if (childNode.getContentLength() == 1) {
					node.setContent(idx.getSecond(), prefixKey + childNode.getContent(0));
					node.setChild(idx.getSecond(), childNode.getChild(0));
					childNode.destroyNode(false);
				}

				node.modifyNumberOfEntries(-1);
				node.setIsOnRecursionStack(false);
				return true;
			} else {
				node.setIsOnRecursionStack(false);
				return false;
			}
		} else {
			if (key.length() == prefixKeyLength) {
				node.decreaseArraySizes(idx.getSecond());
				node.modifyNumberOfEntries(-1);
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Removes a key from the node.
	 *
	 * @param Node
	 *            the node
	 * @param key
	 *            Key to remove
	 * @return <strong>null</strong> if the key could not be removed (if the trie did not contain that key),
	 *         the old value otherwise (inside a Tuple-object to distinguish case that the the value itself is a null value).
	 */
	public final static<T> Tuple<T, Boolean> removeKey(final NodeWithValue<T> node, final String key) {
		final Tuple<Boolean, Integer> idx = NodeHelper.searchMergePartner(node, key);

		if (!idx.getFirst()) {
			return null;
		}

		final String prefixKey = node.getContent(idx.getSecond());
		final int prefixKeyLength = prefixKey.length();
		final int commonPrefixLength = NodeHelper.findLongestCommonPrefix(1, key, prefixKey) + 1;

		if (commonPrefixLength < prefixKeyLength) {
			return null;
		}

		if (node.hasChild(idx.getSecond())) {
			final NodeWithValue<T> childNode = node.getChild(idx.getSecond());
			node.setIsOnRecursionStack(true);
			final Tuple<T, Boolean> oldValue = NodeHelper.removeKey(childNode, key.substring(commonPrefixLength));
			if (oldValue!=null) {
				if (childNode.getContentLength() == 1) {
					node.setContent(idx.getSecond(), prefixKey + childNode.getContent(0));
					node.setChild(idx.getSecond(), childNode.getChild(0));
					childNode.destroyNode(false);
				}

				node.modifyNumberOfEntries(-1);
				node.setIsOnRecursionStack(false);
				return oldValue;
			} else {
				node.setIsOnRecursionStack(false);
				return null;
			}
		} else {
			if (key.length() == prefixKeyLength) {
				final T oldValue = node.getValue(idx.getSecond());
				node.decreaseArraySizes(idx.getSecond());
				node.modifyNumberOfEntries(-1);
				return new Tuple<T, Boolean>(oldValue, Boolean.TRUE);
			} else {
				return null;
			}
		}
	}

	/**
	 * Searches for a possible merge partner in the content array
	 *
	 * @param node
	 *            Node to be processed...
	 * @param key
	 *            Key to find a merge partner for
	 * @return A tuple consisting of a boolean and an integer value: <br />
	 *         <strong>true</strong> and the index of the merge partner if a merge partner was found<br />
	 *         <strong>false</strong> and the index of the insertion index if no merge partner was found
	 */
	protected static Tuple<Boolean, Integer> searchMergePartner(final Node node, final String key) {
		final int keyLength = key.length();

		// If this.content contains an empty string, it must be the first element
		if (keyLength == 0){
			return new Tuple<Boolean, Integer>(node.getContent()!=null && node.getContent(0).length() == 0, 0);
		}

		for (int contentIdx = 0, contentLength = node.getContentLength(); contentIdx < contentLength; contentIdx++) {
			final String prefixKey = node.getContent(contentIdx);

			if (prefixKey.length() != 0) {
				if (key.charAt(0) == prefixKey.charAt(0)){
					return new Tuple<Boolean, Integer>(true, contentIdx);
				} else if (key.charAt(0) < prefixKey.charAt(0)){
					return new Tuple<Boolean, Integer>(false, contentIdx);
				}
			}
		}

		return new Tuple<Boolean, Integer>(false, node.getContentLength());
	}

	/**
	 * Finds the longest common prefix
	 *
	 * @param startAt
	 *            Chars to skip before comparison
	 * @param str1
	 *            First string
	 * @param str2
	 *            Second string
	 * @return Number of equal chars from startAt to last equal char
	 */
	protected static int findLongestCommonPrefix(final int startAt, final String str1, final String str2) {
		if (str1.length() <= startAt || str2.length() <= startAt){
			return 0;
		}
		final int endAt = Math.min(str1.length(), str2.length());
		for (int i = startAt; i < endAt; i++){
			if (str1.charAt(i) != str2.charAt(i)){
				return i - startAt;
			}
		}
		return endAt - startAt;
	}

	/**
	 * Compares all mergeable nodes and finds the longest common prefix
	 *
	 * @param mergeableNodes
	 *            List of mergeable nodes. <strong>Caution:</strong> Must
	 *            contain at least one element.
	 * @param startAt
	 *            String pos to start at
	 * @return Number of equal chars from startAt to last equal char
	 */
	protected final static int getCommonPrefixLength(final List<Triple<Integer, String, Node>> mergeableNodes, final int startAt) {
		int prefixLength = startAt;
		boolean prefixFinished = false;

		while (!prefixFinished) {
			final char currentChar = mergeableNodes.get(0).getSecond().length() > prefixLength ? mergeableNodes.get(0).getSecond().charAt(prefixLength) : 0;

			for (int i = 0, j = mergeableNodes.size(); i < j && !prefixFinished; i++) {
				final String s = mergeableNodes.get(i).getSecond();
				prefixFinished = (currentChar - (s.length() > prefixLength ? s.charAt(prefixLength) : 0) != 0) || (prefixLength >= s.length());
			}

			if (!prefixFinished){
				prefixLength++;
			}
		}

		return prefixLength - startAt;
	}

	/**
	 * Returns a sorted Map of Lists of nodes that share a common prefix. The
	 * key for each value is the integer representation of the first char.
	 *
	 * @param nodesToMerge
	 *            Array of nodes to use for calculation
	 * @return Map of List of Triplets that contains for each mergeable node the
	 *         index, the content string and the node reference
	 */
	protected final static SortedMap<Integer, List<Triple<Integer, String, Node>>> getMergeableNodes(final List<? extends Node> nodesToMerge) {
		final SortedMap<Integer, List<Triple<Integer, String, Node>>> result = new TreeMap<Integer, List<Triple<Integer,String,Node>>>();
		List<Triple<Integer, String, Node>> tmp = null;

		for (final Node currentNode : nodesToMerge) {
			for (int i = 0, j = currentNode.getContentLength(); i < j; i++) {
				final String currentContent = currentNode.getContent(i);
				final char firstChar = (currentContent.length() == 0 ? 0 : currentContent.charAt(0));

				tmp = result.get(firstChar + 0);
				if (tmp == null) {
					tmp = new LinkedList<Triple<Integer, String, Node>>();
					result.put(firstChar + 0, tmp);
				}

				tmp.add(new Triple<Integer, String, Node>(i, currentContent, currentNode));
			}
		}

		return result;
	}

	/**
	 * Merges n trie nodes into one trie node.<br />
	 * <br />
	 * <strong>Caution:</strong> This merge function <strong>must</strong> be called from an empty root node,
	 * not from a node that should actually be merged with another!
	 *
	 * @param node The node in which the other nodes will be merged...
	 * @param nodesToMerge List of nodes that will be merged.
	 */
	public final static void mergeSet(final Node node, final List<Node> nodesToMerge) {

		boolean finished = false;

		node.setIsOnRecursionStack(true);

		final Iterator<List<Triple<Integer, String, Node>>> mergeableNodesIterator = getMergeableNodes(nodesToMerge).values().iterator();

		while (!finished) {

			// Array to store the references to the mergeable nodes for current idx
			final List<Triple<Integer, String, Node>> mergeableNodes = (mergeableNodesIterator.hasNext() ? mergeableNodesIterator.next() : null);

			// Nothing left to merge
			if (mergeableNodes == null || mergeableNodes.isEmpty()){
				finished = true;
			} else {
				// Now we have a list of nodes, with all nodes having at least one char in common (at their respective content index)
				// Next step: Find longest common prefix to build the new node

				if (mergeableNodes.size() == 1) {
					// This is an only child, the node can be directly inserted

					final int index = node.getContentLength();
					final Triple<Integer, String, Node> nodeData = mergeableNodes.get(0);
					final Node n = nodeData.getThird();

					node.increaseArraySizes(index, 1);
					node.setContent(index, nodeData.getSecond());

					if (node.isFromSameTrie(n)) {
						node.setChild(index, n.getChild(nodeData.getFirst()));
					} else {
						NodeHelper.setChildCopy(node, index, n.getChild(nodeData.getFirst()));
					}

					node.modifyNumberOfEntries(node.hasChild(index) ? node.getChild(index).numberOfEntries : 1);
				} else {

					// Find the common prefix length
					final int prefixLength = getCommonPrefixLength(mergeableNodes, 0);

					// Now it's time to create the new entry in the resulting trie

					// Steps to perform:
					// 1. Create empty node
					Node tmpNode = null;

					// 2. Prepare recursion on empty node with truncated strings of mergeable nodes
					final List<Node> nodeList = new ArrayList<Node>(mergeableNodes.size());

					for (final Triple<Integer, String, Node> entry : mergeableNodes) {
						// 2.1. a) Empty suffix, check for children
						if (entry.getSecond().length() == prefixLength) {
							if (entry.getThird().hasChild(entry.getFirst())) {
								nodeList.add(entry.getThird().getChild(entry.getFirst()));
							} else if (tmpNode==null) {
								tmpNode = NodeHelper.createTemporaryNode("", null);
								nodeList.add(tmpNode);
							} else {
								// nothing to do for sets
							}
						} else { // 2.1. b) Non-empty suffix, truncate
							nodeList.add(NodeHelper.createTemporaryNode(entry.getSecond().substring(prefixLength),
									entry.getThird().getChild(entry.getFirst())));
						}
					}

					final int index = node.getContentLength();

					node.increaseArraySizes(index, 1);
					node.setContent(index, mergeableNodes.get(0).getSecond().substring(0, prefixLength));

					/*
					 * TODO Optimierungsbedarf
					 *
					 * Hier wird in jedem Fall ein neuer Knoten angelegt, auch wenn dies in manchen Faellen nicht noetig ist.
					 * Sinnvoller waere hier wahrscheinlich, irgendwie den Knoten aus dem Basis-Trie wiederzuverwerten, um nicht
					 * alles bei jedem Mergen neu zu erstellen.
					 */

					// If more than one non-empty suffix exists, it is possible that some of them still share a common prefix => recursion
					if (nodeList.size() > 1) {
						final Node inode = node.createNode();
						NodeHelper.mergeSet(inode, nodeList);
						node.modifyNumberOfEntries(inode.numberOfEntries);
						node.setChild(index, inode);
					} else {
						node.modifyNumberOfEntries(1);
					}
				}
			}
		}
		node.setIsOnRecursionStack(false);
	}

	/**
	 * Merges n trie nodes into one trie node.<br />
	 * <br />
	 *
	 * @param node The node in which the other nodes will be merged...
	 * @param nodesToMerge List of nodes that will be merged.
	 */
	public static void mergeSeqSet(final DBSeqNode node, final List<Node> nodesToMerge) {
		/*
		 * Because of the underlying data structure, the merge method had to be
		 * reimplemented for this kind of node: Every node has to be written to
		 * disk, before going into recursion.
		 *
		 * First this method determines, which operations have to be performed
		 * for this node and stores them in different lists.
		 *
		 * Once the contents of this node and the hasChild-flag are available,
		 * this node can be written to disk. After writing this node to disk,
		 * all pending operations can be performed in the same order as they were
		 * added to the list of operations.
		 *
		 * It is especially necessary, that all getChild-operations are
		 * performed in depth-first-search-order, because it is possible, that a
		 * DBSeqNode will be merged.
		 */
		boolean finished = false;

		final Iterator<List<Triple<Integer, String, Node>>> mergeableNodesIterator = NodeHelper.getMergeableNodes(nodesToMerge).values().iterator();

		/*
		 * These lists will collect all data, that is needed to write this node to the nodeManager
		 * before going into recursion
		 */
		final List<ChildFlag> hasChildren = new LinkedList<ChildFlag>();
		final List<String> contents = new LinkedList<String>();
		final List<Tuple<Node, Integer>> children_local = new LinkedList<Tuple<Node, Integer>>();
		final List<List<Object>> nodeLists = new LinkedList<List<Object>>();


		while (!finished) {

			// Array to store the references to the mergeable nodes for current idx
			final List<Triple<Integer, String, Node>> mergeableNodes = (mergeableNodesIterator.hasNext() ? mergeableNodesIterator.next() : null);

			// Nothing left to merge
			if (mergeableNodes == null || mergeableNodes.isEmpty()) {
				finished = true;
			} else {
				// Now we have a list of nodes, with all nodes having at least one char in common (at their respective content index)
				// Next step: Find longest common prefix to build the new node

				if (mergeableNodes.size() == 1) {
					// This is an only child, the node can be directly inserted

					final Triple<Integer, String, Node> nodeData = mergeableNodes.get(0);
					final Node n = nodeData.getThird();

					contents.add(nodeData.getSecond());

					if (n.hasChild(nodeData.getFirst())) {
						// The child node can be copied recursively after writing this node
						hasChildren.add(ChildFlag.ONE_CHILD);
						children_local.add(new Tuple<Node, Integer>(n, nodeData.getFirst()));
					}
					else {
						hasChildren.add(ChildFlag.NO_CHILD);
					}
				}
				else {

					// Find the common prefix length
					final int prefixLength = getCommonPrefixLength(mergeableNodes, 0);

					// Now it's time to create the new entry in the resulting trie

					// Steps to perform:
					// 1. Create empty node
					Node tmpNode = null;

					// 2. Prepare recursion on empty node with truncated strings of mergeable nodes
					final List<Object> nodeList = new ArrayList<Object>(mergeableNodes.size());

					for (final Triple<Integer, String, Node> entry : mergeableNodes) {
						// 2.1. a) Empty suffix, check for children
						if (entry.getSecond().length() == prefixLength) {
							if (entry.getThird().hasChild(entry.getFirst())) {
								// The child node can be copied recursively after writing this node
								nodeList.add(new Tuple<Node, Integer>(entry.getThird(), entry.getFirst()));
							}
							else if (tmpNode == null) {
								tmpNode = NodeHelper.createTemporaryNode("", null);
								nodeList.add(tmpNode);
							}
						}
						// 2.1. b) Non-empty suffix, truncate
						else {
							// A temporary node has to be created with the remaining suffix of this entry and its child node after writing this node
							nodeList.add(new Triple<String, Node, Integer>(entry.getSecond().substring(prefixLength), entry.getThird(), entry.getFirst()));
// from Node:							nodeList.add(this.createTemporaryNode(entry.getSecond().substring(prefixLength),
//									(this.mode == TRIE_MODE.SET)? null: entry.getThird().getValue(entry.getFirst()),
//									entry.getThird().getChild(entry.getFirst())));
//							if(this.mode != TRIE_MODE.SET){
//								values.add(entry.getThird().getValue(entry.getFirst()));
//							}
						}
					}

					contents.add(mergeableNodes.get(0).getSecond().substring(0, prefixLength));

					// If more than one non-empty suffix exists, it is possible that some of them still share a common prefix => recursion
					if (nodeList.size() > 1) {
						hasChildren.add(ChildFlag.CHILDREN);
						nodeLists.add(nodeList);
					} else {
						hasChildren.add(ChildFlag.NO_CHILD);
					}
				}

				// After all this is done, the content indices of the merged nodes have to be increased
			}
		}

		Iterator<ChildFlag> itHasChildren = hasChildren.iterator();
		final Iterator<String> itContents = contents.iterator();
		final Iterator<Tuple<Node, Integer>> itChildren = children_local.iterator();
		final Iterator<List<Object>> itNodeLists = nodeLists.iterator();
		int i = 0;

		// Prepare content and children arrays
		node.setContent(new String[contents.size()]);
		node.setChildren(new boolean[hasChildren.size()]);

		// Copy the contents
		while (itContents.hasNext()){
			node.getContent()[i++] = itContents.next();
		}

		i = 0;
		// Set the hasChild-flags
		while (itHasChildren.hasNext()){
			final boolean hasChild = !itHasChildren.next().equals(ChildFlag.NO_CHILD);
			node.getChildren()[i++] = hasChild;
		}

		// Write this node to disk (Caution: Wrong numberOfEntries, this breaks the possibility to convert it back into a RBTrie or DBTrie)
		node.getNodeManager().writeNextNode(DBSeqNode.deSerializer, node);

		itHasChildren = hasChildren.iterator();

		/*
		 * Write all children in the depth-first-search-order to disk.
		 *
		 * Even though the numberOfEntries will not be written to disk, it is
		 * still necessary to update the value. The rootNode will be written
		 * twice, such that at least the total entry count will be correct.
		 */
		while (itHasChildren.hasNext()) {
			final ChildFlag currentFlag = itHasChildren.next();

			switch (currentFlag) {
				case NO_CHILD:
					// Nothing more to do
					node.modifyNumberOfEntries(1);
					break;
				case ONE_CHILD:
					// Recursive copy of the node
					final Tuple<Node, Integer> nodeOneTuple = itChildren.next();
					final Node nodeOne = nodeOneTuple.getFirst();
					final Node nodeOneChild = nodeOne.getChild(nodeOneTuple.getSecond());
					node.modifyNumberOfEntries(node.getNodeManager().writeNextNodeRecursive(DBSeqNode.deSerializer, nodeOneChild));

					break;
				case CHILDREN:
					// Recursion into merge
					final List<Object> oL = itNodeLists.next();
					final List<Node> nL = new ArrayList<Node>(oL.size());
					final Node nodeMore = node.createNode();

					for (final Object o : oL) {
						if (o instanceof Node) {
							// Temporary node (should be the empty string with no children)
							nL.add((Node) o);
						} else if (o instanceof Triple<?, ?, ?>) {
							// Temporary node with remaining suffix
							final Triple<?, ?, ?> t = (Triple<?, ?, ?>) o;
							final String s = (String) t.getFirst();
							final Node n = (Node) t.getSecond();
							final Integer nIdx = (Integer) t.getThird();
							final Node newNode = NodeHelper.createTemporaryNode(s, n.getChild(nIdx));
							nL.add(newNode);
						} else if (o instanceof Tuple<?, ?>) {
							// Child node
							final Tuple<?, ?> t = (Tuple<?, ?>) o;
							final Node n = (Node) t.getFirst();
							final Integer nIdx = (Integer) t.getSecond();
							nL.add(n.getChild(nIdx));
						}
					}
					NodeHelper.mergeSeqSet((DBSeqNode)nodeMore, nL);

					node.modifyNumberOfEntries(nodeMore.getNumberOfEntries());

					break;
			}
		}
	}

	/**
	 * Merges n trie nodes into one trie node.<br />
	 * <br />
	 * <strong>Caution:</strong> This merge function <strong>must</strong> be called from an empty root node,
	 * not from a node that should actually be merged with another!
	 *
	 * @param node The node in which the other nodes will be merged...
	 * @param nodesToMerge List of nodes that will be merged.
	 */
	@SuppressWarnings({ "unchecked", "null" })
	public final static void mergeBag(final NodeWithValue<Integer> node, final List<NodeWithValue<Integer>> nodesToMerge) {

		boolean finished = false;

		node.setIsOnRecursionStack(true);

		final Iterator<List<Triple<Integer, String, Node>>> mergeableNodesIterator = getMergeableNodes(nodesToMerge).values().iterator();

		while (!finished) {

			// Array to store the references to the mergeable nodes for current idx
			final List<Triple<Integer, String, Node>> mergeableNodes = (mergeableNodesIterator.hasNext() ? mergeableNodesIterator.next() : null);

			// Nothing left to merge
			if (mergeableNodes == null || mergeableNodes.isEmpty()){
				finished = true;
			} else {
				// Now we have a list of nodes, with all nodes having at least one char in common (at their respective content index)
				// Next step: Find longest common prefix to build the new node

				if (mergeableNodes.size() == 1) {
					// This is an only child, the node can be directly inserted

					final int index = node.getContentLength();
					final Triple<Integer, String, Node> nodeData = mergeableNodes.get(0);
					final NodeWithValue<Integer> n = (NodeWithValue<Integer>) nodeData.getThird();

					node.increaseArraySizes(index, 1);
					node.setContent(index, nodeData.getSecond());

					if (node.isFromSameTrie(n)) {
						node.setChild(index, n.getChild(nodeData.getFirst()));
					} else {
						NodeHelper.setChildCopy(node, index, n.getChild(nodeData.getFirst()));
					}

					node.setValue(index, n.getValue(nodeData.getFirst()));

					node.modifyNumberOfEntries(node.hasChild(index) ? node.getChild(index).numberOfEntries : 1);
				} else {

					// Find the common prefix length
					final int prefixLength = getCommonPrefixLength(mergeableNodes, 0);

					// Now it's time to create the new entry in the resulting trie

					// Steps to perform:
					// 1. Create empty node
					NodeWithValue<Integer> tmpNode = null;

					// 2. Prepare recursion on empty node with truncated strings of mergeable nodes
					final List<NodeWithValue<Integer>> nodeList = new ArrayList<NodeWithValue<Integer>>(mergeableNodes.size());

					for (final Triple<Integer, String, Node> entry : mergeableNodes) {
						// 2.1. a) Empty suffix, check for children
						if (entry.getSecond().length() == prefixLength) {
							if (entry.getThird().hasChild(entry.getFirst())) {
								nodeList.add((NodeWithValue<Integer>)entry.getThird().getChild(entry.getFirst()));
							} else if (tmpNode==null) {
								tmpNode = NodeHelper.createTemporaryNode("", ((NodeWithValue<Integer>)entry.getThird()).getValue(entry.getFirst()), null);
								nodeList.add(tmpNode);
							} else {
								// it is a bag!
								// calculate number of occurrences!
								tmpNode.setValue(0, tmpNode.getValue(0) + ((NodeWithValue<Integer>) entry.getThird()).getValue(entry.getFirst()));
							}
						} else { // 2.1. b) Non-empty suffix, truncate
							nodeList.add(NodeHelper.createTemporaryNode(entry.getSecond().substring(prefixLength),
									(((NodeWithValue<Integer>) entry.getThird()).getValue(entry.getFirst())),
									(NodeWithValue<Integer>) entry.getThird().getChild(entry.getFirst())));
						}
					}

					final int index = node.getContentLength();

					node.increaseArraySizes(index, 1);
					node.setContent(index, mergeableNodes.get(0).getSecond().substring(0, prefixLength));

					/*
					 * TODO Optimierungsbedarf
					 *
					 * Hier wird in jedem Fall ein neuer Knoten angelegt, auch wenn dies in manchen Faellen nicht noetig ist.
					 * Sinnvoller waere hier wahrscheinlich, irgendwie den Knoten aus dem Basis-Trie wiederzuverwerten, um nicht
					 * alles bei jedem Mergen neu zu erstellen.
					 */

					// If more than one non-empty suffix exists, it is possible that some of them still share a common prefix => recursion
					if (nodeList.size() > 1) {
						final NodeWithValue<Integer> inode = node.createNode();
						NodeHelper.mergeBag(inode, nodeList);
						node.modifyNumberOfEntries(inode.numberOfEntries);
						node.setChild(index, inode);
					} else {
						node.modifyNumberOfEntries(1);
						node.setValue(index, tmpNode.getValue(0));
					}
				}
			}
		}
		node.setIsOnRecursionStack(false);
	}

	/**
	 * Operations that can be performed in the merge algorithm.
	 */
	protected static enum ChildFlag {
		/** Node has no child */
		NO_CHILD,

		/** Node has one child, that can be copied */
		ONE_CHILD,

		/** Node has one child, but recursion is needed */
		CHILDREN
	}


	/**
	 * Merges n trie nodes into one trie node.<br />
	 * <br />
	 *
	 * @param node The node in which the other nodes will be merged...
	 * @param nodesToMerge List of nodes that will be merged.
	 */
	public static void mergeSeqBag(final DBSeqNodeWithValue<Integer> node, final List<NodeWithValue<Integer>> nodesToMerge) {
		/*
		 * Because of the underlying data structure, the merge method had to be
		 * reimplemented for this kind of node: Every node has to be written to
		 * disk, before going into recursion.
		 *
		 * First this method determines, which operations have to be performed
		 * for this node and stores them in different lists.
		 *
		 * Once the contents of this node and the hasChild-flag are available,
		 * this node can be written to disk. After writing this node to disk,
		 * all pending operations can be performed in the same order as they were
		 * added to the list of operations.
		 *
		 * It is especially necessary, that all getChild-operations are
		 * performed in depth-first-search-order, because it is possible, that a
		 * DBSeqNode will be merged.
		 */
		boolean finished = false;

		final Iterator<List<Triple<Integer, String, Node>>> mergeableNodesIterator = NodeHelper.getMergeableNodes(nodesToMerge).values().iterator();

		/*
		 * These lists will collect all data, that is needed to write this node to the nodeManager
		 * before going into recursion
		 */
		final List<ChildFlag> hasChildren = new LinkedList<ChildFlag>();
		final List<String> contents = new LinkedList<String>();
		final List<Integer> values = new LinkedList<Integer>();
		final List<Tuple<Node, Integer>> children_local = new LinkedList<Tuple<Node, Integer>>();
		final List<List<Object>> nodeLists = new LinkedList<List<Object>>();


		while (!finished) {

			// Array to store the references to the mergeable nodes for current idx
			final List<Triple<Integer, String, Node>> mergeableNodes = (mergeableNodesIterator.hasNext() ? mergeableNodesIterator.next() : null);

			// Nothing left to merge
			if (mergeableNodes == null || mergeableNodes.isEmpty()) {
				finished = true;
			} else {
				// Now we have a list of nodes, with all nodes having at least one char in common (at their respective content index)
				// Next step: Find longest common prefix to build the new node

				if (mergeableNodes.size() == 1) {
					// This is an only child, the node can be directly inserted

					final Triple<Integer, String, Node> nodeData = mergeableNodes.get(0);
					final Node n = nodeData.getThird();

					contents.add(nodeData.getSecond());
					// TODO: Implement for map
					final Integer t = ((NodeWithValue<Integer>) n).getValue(nodeData.getFirst());
					if(t!=null){
						values.add(t);
					}

					if (n.hasChild(nodeData.getFirst())) {
						// The child node can be copied recursively after writing this node
						hasChildren.add(ChildFlag.ONE_CHILD);
						children_local.add(new Tuple<Node, Integer>(n, nodeData.getFirst()));
					}
					else {
						hasChildren.add(ChildFlag.NO_CHILD);
					}
				}
				else {

					// Find the common prefix length
					final int prefixLength = getCommonPrefixLength(mergeableNodes, 0);

					// Now it's time to create the new entry in the resulting trie

					// Steps to perform:
					// 1. Create empty node
					NodeWithValue<Integer> tmpNode = null;

					// 2. Prepare recursion on empty node with truncated strings of mergeable nodes
					final List<Object> nodeList = new ArrayList<Object>(mergeableNodes.size());

					for (final Triple<Integer, String, Node> entry : mergeableNodes) {
						// 2.1. a) Empty suffix, check for children
						if (entry.getSecond().length() == prefixLength) {
							if (entry.getThird().hasChild(entry.getFirst())) {
								// The child node can be copied recursively after writing this node
								nodeList.add(new Tuple<Node, Integer>(entry.getThird(), entry.getFirst()));
							}
							else if (tmpNode == null) {
								tmpNode = NodeHelper.createTemporaryNode("", ((NodeWithValue<Integer>)entry.getThird()).getValue(entry.getFirst()), null);
								nodeList.add(tmpNode);
							} else {
								// TODO implement for map
								// it is a bag!
								// calculate number of occurrences!
								tmpNode.setValue(0, tmpNode.getValue(0) + ((NodeWithValue<Integer>) entry.getThird()).getValue(entry.getFirst()));
							}
						}
						// 2.1. b) Non-empty suffix, truncate
						else {
							// A temporary node has to be created with the remaining suffix of this entry and its child node after writing this node
							nodeList.add(new Triple<String, Node, Integer>(entry.getSecond().substring(prefixLength), entry.getThird(), entry.getFirst()));
// from Node:							nodeList.add(this.createTemporaryNode(entry.getSecond().substring(prefixLength),
//									(this.mode == TRIE_MODE.SET)? null: entry.getThird().getValue(entry.getFirst()),
//									entry.getThird().getChild(entry.getFirst())));
//							if(this.mode != TRIE_MODE.SET){
//								values.add(entry.getThird().getValue(entry.getFirst()));
//							}
						}
					}

					contents.add(mergeableNodes.get(0).getSecond().substring(0, prefixLength));

					// If more than one non-empty suffix exists, it is possible that some of them still share a common prefix => recursion
					if (nodeList.size() > 1) {
						hasChildren.add(ChildFlag.CHILDREN);
						nodeLists.add(nodeList);
					}
					else {
						hasChildren.add(ChildFlag.NO_CHILD);
						// TODO implement for map
							// TODO: check: maybe wrong order to write tmpNode here!
						values.add(tmpNode.getValue(0));
					}
				}

				// After all this is done, the content indices of the merged nodes have to be increased
			}
		}

		Iterator<ChildFlag> itHasChildren = hasChildren.iterator();
		final Iterator<String> itContents = contents.iterator();
		final Iterator<Integer> itValues = values.iterator();
		final Iterator<Tuple<Node, Integer>> itChildren = children_local.iterator();
		final Iterator<List<Object>> itNodeLists = nodeLists.iterator();
		int i = 0;

		// Prepare content and children arrays
		node.setContent(new String[contents.size()]);
		node.setChildren(new boolean[hasChildren.size()]);
		// TODO implement for map
		node.setValues(new Integer[hasChildren.size()]);

		// Copy the contents
		while (itContents.hasNext()){
			node.getContent()[i++] = itContents.next();
		}

		i = 0;
		// Set the hasChild-flags
		while (itHasChildren.hasNext()){
			final boolean hasChild = !itHasChildren.next().equals(ChildFlag.NO_CHILD);
			// TODO implement for map
			if(!hasChild){
				node.getValues()[i] = itValues.next();
			}
			node.getChildren()[i++] = hasChild;
		}

		// Write this node to disk (Caution: Wrong numberOfEntries, this breaks the possibility to convert it back into a RBTrie or DBTrie)
		node.getNodeManager().writeNextNode(DBSeqNodeWithValue.deSerializer, node);

		itHasChildren = hasChildren.iterator();

		/*
		 * Write all children in the depth-first-search-order to disk.
		 *
		 * Even though the numberOfEntries will not be written to disk, it is
		 * still necessary to update the value. The rootNode will be written
		 * twice, such that at least the total entry count will be correct.
		 */
		while (itHasChildren.hasNext()) {
			final ChildFlag currentFlag = itHasChildren.next();

			switch (currentFlag) {
				case NO_CHILD:
					// Nothing more to do
					node.modifyNumberOfEntries(1);
					break;
				case ONE_CHILD:
					// Recursive copy of the node
					final Tuple<Node, Integer> nodeOneTuple = itChildren.next();
					final Node nodeOne = nodeOneTuple.getFirst();
					final Node nodeOneChild = nodeOne.getChild(nodeOneTuple.getSecond());
					node.modifyNumberOfEntries(node.getNodeManager().writeNextNodeRecursive(DBSeqNodeWithValue.deSerializer, nodeOneChild));

					break;
				case CHILDREN:
					// Recursion into merge
					final List<Object> oL = itNodeLists.next();
					final List<NodeWithValue<Integer>> nL = new ArrayList<NodeWithValue<Integer>>(oL.size());
					final Node nodeMore = node.createNode();

					for (final Object o : oL) {
						if (o instanceof NodeWithValue) {
							// Temporary node (should be the empty string with no children)
							nL.add((NodeWithValue<Integer>) o);
						} else if (o instanceof Triple<?, ?, ?>) {
							// Temporary node with remaining suffix
							final Triple<?, ?, ?> t = (Triple<?, ?, ?>) o;
							final String s = (String) t.getFirst();
							final Node n = (Node) t.getSecond();
							final Integer nIdx = (Integer) t.getThird();
							final NodeWithValue<Integer> newNode = NodeHelper.createTemporaryNode(s,
									((NodeWithValue<Integer>)n).getValue(nIdx), // TODO: check correctness!
									(NodeWithValue<Integer>) n.getChild(nIdx));

							nL.add(newNode);
						} else if (o instanceof Tuple<?, ?>) {
							// Child node
							final Tuple<?, ?> t = (Tuple<?, ?>) o;
							final Node n = (Node) t.getFirst();
							final Integer nIdx = (Integer) t.getSecond();
							nL.add((NodeWithValue<Integer>) n.getChild(nIdx));
						}
					}
					NodeHelper.mergeSeqBag((DBSeqNodeWithValue<Integer>)nodeMore, nL);

					node.modifyNumberOfEntries(nodeMore.getNumberOfEntries());

					break;
			}
		}
	}

	/**
	 * Creates a clone of the node (if not null) and sets this clone as the i-th
	 * element of the children array. If necessary, the children array will be
	 * initialized first. It will not be initialized, if node is null.
	 *
	 * @param parentnode
	 *            The node to which the cloned node is added
	 * @param i
	 *            Array index of the node
	 * @param node
	 *            Node instance to be cloned and stored
	 */
	public final static void setChildCopy(final Node parentNode, final int i, final Node node) {
		if (node != null) {
			final boolean wasOnRecursionStack = parentNode.isOnRecursionStack();
			parentNode.setIsOnRecursionStack(true);
			final Node nodeCopy = parentNode.createNode();

			nodeCopy.setContent(new String[node.getContentLength()]);
			nodeCopy.numberOfEntries = node.numberOfEntries;

			for (int j = 0; j < node.getContentLength(); j++) {
				nodeCopy.setContent(j, node.getContent(j));
			}

			for (int j = 0; j < node.getChildrenLength(); j++) {
				NodeHelper.setChildCopy(nodeCopy, j, node.getChild(j));
			}

			parentNode.setChild(i, nodeCopy);
			parentNode.setIsOnRecursionStack(wasOnRecursionStack);
		}
	}

	/**
	 * Creates a clone of the node (if not null) and sets this clone as the i-th
	 * element of the children array. If necessary, the children array will be
	 * initialized first. It will not be initialized, if node is null.
	 *
	 * @param parentnode
	 *            The node to which the cloned node is added
	 * @param i
	 *            Array index of the node
	 * @param node
	 *            Node instance to be cloned and stored
	 */
	@SuppressWarnings("unchecked")
	protected final static<T> void setChildCopy(final NodeWithValue<T> parentNode, final int i, final NodeWithValue<T> node) {
		if (node != null) {
			final boolean wasOnRecursionStack = parentNode.isOnRecursionStack();
			parentNode.setIsOnRecursionStack(true);
			final NodeWithValue<T> nodeCopy = parentNode.createNode();

			nodeCopy.setContent(new String[node.getContentLength()]);
			nodeCopy.numberOfEntries = node.numberOfEntries;

			for (int j = 0; j < node.getContentLength(); j++) {
				nodeCopy.setContent(j, node.getContent(j));
			}

			for (int j = 0; j < node.getChildrenLength(); j++) {
				NodeHelper.setChildCopy(nodeCopy, j, node.getChild(j));
			}

			nodeCopy.setValues((T[]) new Object[node.getValuesLength()]);

			for (int j = 0; j < node.getValuesLength(); j++) {
				nodeCopy.setValue(j, node.getValue(j));
			}

			// TODO: Number of entries kopieren, size neu berechnen (eventuell einfach aus Kindern klauen?)
			parentNode.setChild(i, nodeCopy);
			parentNode.setIsOnRecursionStack(wasOnRecursionStack);
		}
	}

	/**
	 * Creates a temporary node instance with one entry and one child. This will
	 * always be ram-based.
	 *
	 * @param key
	 *            Key to add
	 * @param child
	 *            Child node that belongs to this key
	 * @return Temporary node
	 */
	protected final static<T> NodeWithValue<T> createTemporaryNode(final String key, final T value, final NodeWithValue<T> child) {
		final RBNodeWithValue<T> temporaryNode = new RBNodeWithValue<T>();
		temporaryNode.increaseArraySizes(0, 1);
		temporaryNode.setContent(0, key);
		temporaryNode.setChild(0, child);
		temporaryNode.setValue(0, value);

		if (child != null){
			temporaryNode.numberOfEntries = child.getNumberOfEntries();
		} else {
			temporaryNode.numberOfEntries = 1;
		}

		return temporaryNode;
	}

	/**
	 * Creates a temporary node instance with one entry and one child. This will
	 * always be ram-based.
	 *
	 * @param key
	 *            Key to add
	 * @param child
	 *            Child node that belongs to this key
	 * @return Temporary node
	 */
	protected final static<T> Node createTemporaryNode(final String key, final Node child) {
		final RBNode temporaryNode = new RBNode();
		temporaryNode.increaseArraySizes(0, 1);
		temporaryNode.setContent(0, key);
		temporaryNode.setChild(0, child);

		if (child != null){
			temporaryNode.numberOfEntries = child.getNumberOfEntries();
		} else {
			temporaryNode.numberOfEntries = 1;
		}

		return temporaryNode;
	}

	/**
	 * Helper method for a clearly arranged toString output.
	 *
	 * @param node
	 *            the node to be printed...
	 * @param indent
	 *            Current indention level
	 * @return Clearly arranged string
	 */
	protected final static String toString(final Node node, final String indent) {
		String s = indent + "[";
		for (int i = 0; i < node.getContentLength(); i++) {
			if (i > 0){
				s += ",\n " + indent;
			}
			s += "\"" + node.getContent(i) + "\"";
			if (node.hasChild(i)){
				s += " ->\n" + NodeHelper.toString(node.getChild(i), indent + "  ");
			}
		}
		return s + "]";
	}

	/**
	 * Helper method for a clearly arranged toString output.
	 *
	 * @param node
	 *            the node to be printed...
	 * @param indent
	 *            Current indention level
	 * @return Clearly arranged string
	 */
	protected final static<T> String toString(final NodeWithValue<T> node, final String indent) {
		String s = indent + "[";
		for (int i = 0; i < node.getContentLength(); i++) {
			if (i > 0){
				s += ",\n " + indent;
			}
			s += "\"" + node.getContent(i) + "\"";
			if (node.hasChild(i)){
				s += " ->\n" + NodeHelper.toString(node.getChild(i), indent + "  ");
			} else {
					s += " => " + node.getValue(i);
			}
		}
		return s + "]";
	}
}
