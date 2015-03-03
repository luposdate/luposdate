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

/**
 * This class implements some of the base algorithms that can be executed on Patricia Tries.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class Node {

	/** Prefix strings contained within this node */
	private String[] content;

	/** Number of entries contained in this node and all its children */
	protected int numberOfEntries;

	/** Indicates, if this node has been changed since it has been loaded */
	private boolean changed;

	/** Indicates, if this node is currently on the recursion stack and must therefore not be written to disk */
	protected boolean onRecursionStack;

	/**
	 * Creates a new node instance
	 *
	 * @return A new node instance
	 */
	protected abstract Node createNode();

	/**
	 * Removes this node from the underlying data structure if necessary.
	 *
	 * @param recursive
	 *            If set, all children will also be destroyed.
	 */
	public void destroyNode(final boolean recursive) {
		// nothing to do
	}

	/**
	 * <p>hasChild.</p>
	 *
	 * @param i
	 *            Array index of the child
	 * @return <strong>true</strong> if there is a child at index i<br />
	 *         <strong>false</strong> otherwise
	 */
	public abstract boolean hasChild(int i);

	/**
	 * <p>getChild.</p>
	 *
	 * @param i
	 *            Array index of the child
	 * @return The node instance if available, <strong>null</strong> otherwise
	 */
	public abstract Node getChild(int i);

	/**
	 * Sets the i-th element of the children array to the node. If necessary, the children array will be
	 * initialized first. It will not be initialized, if node is null.
	 *
	 * @param i
	 *            Array index of the node
	 * @param node
	 *            Node instance to be stored
	 */
	protected abstract void setChild(int i, Node node);

	/**
	 * Checks if this node and the given node are inside the same trie, thus the
	 * reference pointer can be reused instead of copying the whole subtrie.
	 *
	 * @param node
	 *            The other node
	 * @return <b>true</b> if both nodes are inside the same trie,<br>
	 *         <b>false</b> otherwise
	 */
	protected abstract boolean isFromSameTrie(Node node);

	/**
	 * Returns the value of the flag "onRecursionStack" for this node.
	 *
	 * @return The value of the flag "onRecursionStack"
	 */
	public final boolean isOnRecursionStack() {
		return this.onRecursionStack;
	}

	/**
	 * Sets the flag "onRecursionStack" for this node. The flag is used by the
	 * NodeManager. Nodes with flag set will not be written to disk.
	 *
	 * @param isOnStack
	 *            Value to set
	 */
	protected final void setIsOnRecursionStack(final boolean isOnStack) {
		this.onRecursionStack = isOnStack;
	}

	/**
	 * Increases the size of the children array by inserting <i>amount</i> null elements (or something similar, depending
	 * on the node type) starting at the given index, but only if the array has already been initialized. Otherwise it
	 * remains unchanged.
	 *
	 * @param idx
	 *            Index for the new element
	 *            @param amount Amount of entries to add
	 */
	protected abstract void increaseChildrenArraySize(int idx, int amount);

	/**
	 * Decreases the size of the children array by removing the <i>idx</i>-th element, but only if the array
	 * has already been initialized. Otherwise it remains unchanged
	 *
	 * @param idx
	 *            Index of element to remove
	 */
	protected abstract void removeChildrenArrayElement(final int idx);

	/**
	 * Increases or decreases the numberOfEntries value and marks this node as
	 * changed
	 *
	 * @param amount
	 *            Amount to be added
	 */
	protected final void modifyNumberOfEntries(final int amount) {
		this.numberOfEntries += amount;

		this.setChanged(true);
	}

	/**
	 * <p>Getter for the field <code>content</code>.</p>
	 *
	 * @param i
	 *            Array index of the content
	 * @return The key if available, <strong>null</strong> otherwise
	 */
	public final String getContent(final int i) {
		if (this.content != null){
			return this.content[i];
		} else {
			return null;
		}
	}

	/**
	 * Sets the i-th element of the content array to the key. If necessary, the content array will be
	 * initialized first.
	 *
	 * @param i
	 *            Array index of the key
	 * @param key
	 *            Key to be stored
	 */
	public final void setContent(final int i, final String key) {
		if (this.content == null){
			this.content = new String[1];
		}

		this.content[i] = key;

		this.setChanged(true);
	}

	/**
	 * Increases the size of the content array by inserting <i>amount</i> null elements, starting at the given index
	 *
	 * @param idx
	 *            Index for the new element
	 * @param amount
	 *            Amount of entries to add
	 */
	protected final void increaseContentArraySize(final int idx, final int amount) {
		final String[] newContent = new String[this.getContentLength() + amount];

		if (this.getContentLength() > 0) {
			System.arraycopy(this.getContent(), 0, newContent, 0, idx);
			System.arraycopy(this.getContent(), idx, newContent, idx + amount, this.getContentLength() - idx - amount + 1);
		}

		for (int i = idx; i < idx + amount; i++){
			newContent[i] = null;
		}

		this.setContent(newContent);
	}

	/**
	 * Decreases the size of the content array by removing the <i>idx</i>-th element.
	 *
	 * @param idx
	 *            Index of the element to remove
	 */
	protected final void removeContentArrayElement(final int idx) {
		if (this.getContentLength() > 1) {
			final String[] newContent = new String[this.getContentLength() - 1];

			System.arraycopy(this.getContent(), 0, newContent, 0, idx);
			System.arraycopy(this.getContent(), idx + 1, newContent, idx, this.getContentLength() - idx - 1);

			this.setContent(newContent);
		} else {
			this.setContent(null);
		}
	}


	/**
	 * Increases the size of the content and the children array by <i>amount</i>.
	 *
	 * @param idx
	 *            Index for the new element
	 * @param amount
	 *            Amount of entries to add
	 */
	protected void increaseArraySizes(final int idx, final int amount) {
		this.increaseContentArraySize(idx, amount);
		this.increaseChildrenArraySize(idx, amount);
		this.setChanged(true);
	}

	/**
	 * Decreases the size of the content and the children array by 1.
	 *
	 * @param idx
	 *            Index of the element to remove
	 */
	protected void decreaseArraySizes(final int idx) {
		this.removeContentArrayElement(idx);
		this.removeChildrenArrayElement(idx);
		this.setChanged(true);
	}

	/**
	 * <p>getContentLength.</p>
	 *
	 * @return The length of the content array if it is initialized, 0 otherwise
	 */
	public final int getContentLength() {
		return (this.getContent() == null ? 0 : this.getContent().length);
	}

	/**
	 * <p>getChildrenLength.</p>
	 *
	 * @return The length of the children array if it is initialized, 0 otherwise
	 */
	public abstract int getChildrenLength();

	/**
	 * <p>Getter for the field <code>numberOfEntries</code>.</p>
	 *
	 * @return The number of entries stored in this sub-trie
	 */
	public final int getNumberOfEntries() {
		return this.numberOfEntries;
	}

	/**
	 * Sets the number of entries
	 *
	 * @param numberOfEntries the number of entries to be set
	 */
	public final void setNumberOfEntries(final int numberOfEntries){
		this.numberOfEntries = numberOfEntries;
	}

	/**
	 * Base constructor. No array initialized, numberOfEntries set to 0.
	 * Mode stored.
	 */
	public Node() {
		this.setContent(null);
		this.numberOfEntries = 0;

		// New nodes are automatically marked as changed, because they have not been stored yet
		this.setChanged(true);

		this.onRecursionStack = false;
	}

	/**
	 * Returns the number of nodes in the trie below and including this node.
	 *
	 * @return Number of nodes
	 */
	public final int getNodeCount() {
		int result = 1; // This node

		for (int i = 0; i < this.getChildrenLength(); i++) {
			if (this.hasChild(i)) {
				result += this.getChild(i).getNodeCount();
			}
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return NodeHelper.toString(this, "");
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Node)){
			return false;
		}

		@SuppressWarnings("unchecked")
		final Node node = (Node) obj;

		if (this.numberOfEntries != node.numberOfEntries){
			return false;
		}

		// Size cannot be compared because different types of tries have
		// different "pointer" sizes

		// Same content length=
		if (this.getContentLength() != node.getContentLength()){
			return false;
		}

		// Same amount of children?
		if (this.getChildrenLength() != node.getChildrenLength()){
			return false;
		}

		// Contents all equal?
		for (int i = 0; i < this.getContentLength(); i++){
			if (!this.getContent(i).equals(node.getContent(i))){
				return false;
			}
		}

		for (int i = 0; i < this.getChildrenLength(); i++) {

			// Does one have a child, the other does not have?
			if (this.hasChild(i) != node.hasChild(i)){
				return false;
			} else {
				// If there is a child, is it equal to the other?
				if (this.hasChild(i) && !this.getChild(i).equals(node.getChild(i))){
					return false;
				}
			}
		}

		// No differences found
		return true;
	}

	/**
	 * <p>Setter for the field <code>changed</code>.</p>
	 *
	 * @param changed the changed to set
	 */
	public final void setChanged(final boolean changed) {
		this.changed = changed;
	}

	/**
	 * <p>isChanged.</p>
	 *
	 * @return the changed
	 */
	public final boolean isChanged() {
		return this.changed;
	}

	/**
	 * <p>Setter for the field <code>content</code>.</p>
	 *
	 * @param content the content to set
	 */
	public final void setContent(final String[] content) {
		this.content = content;
	}

	/**
	 * <p>Getter for the field <code>content</code>.</p>
	 *
	 * @return the content
	 */
	public final String[] getContent() {
		return this.content;
	}
}
