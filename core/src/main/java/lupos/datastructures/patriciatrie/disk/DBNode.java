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
package lupos.datastructures.patriciatrie.disk;

import java.io.IOException;

import lupos.datastructures.patriciatrie.disk.nodemanager.NodeInputStream;
import lupos.datastructures.patriciatrie.disk.nodemanager.NodeManager;
import lupos.datastructures.patriciatrie.disk.nodemanager.NodeOutputStream;
import lupos.datastructures.patriciatrie.node.Node;


/**
 * This class extends the abstract Node class and implements a disk based
 * behavior.
 *
 * @author groppe
 * @version $Id: $Id
 */
public class DBNode extends Node implements IDBNode {


	/** Constant <code>deserializer</code> */
	public final static Deserializer deserializer = new Deserializer(){
		/**
		 * Deserializes a node from an InputStream.
		 *
		 * @param idx
		 *            Node index in NodeManager
		 * @param nodeInputStream
		 *            InputStream from where the serialized data is read
		 * @return Deserialized instance
		 * @throws IOException
		 */
		@Override
		public DBNode deserialize(final NodeManager nodeManager, final int idx, final NodeInputStream nodeInputStream) throws IOException {
	//		logger.info("Deserializing node with index " + idx);
			final DBNode node = new DBNode();

			node.nodeManager = nodeManager;
			node.nodeIndex = idx;

			final int contentLength = nodeInputStream.readInt();

			if(contentLength<0){
				return node;
			}

			node.increaseArraySizes(0, contentLength);

			for (int i = 0; i < contentLength; i++) {
				final int len = nodeInputStream.readInt();
				final byte[] content_local = new byte[len];
				for (int j = 0; j < len; j++){
					content_local[j] = (byte) nodeInputStream.read();
				}

				node.setContent(i, new String(content_local, Deserializer.OUTPUT_ENCODING));
			}

			final int childrenLength = nodeInputStream.readInt();

			for (int i = 0; i < childrenLength; i++) {
				node.setChildId(i, nodeInputStream.readInt());
			}

			node.numberOfEntries = nodeInputStream.readInt();

			node.setChanged(false);

			return node;
		}
	};

	/** Index in NodeManager */
	private int nodeIndex;

	/** Responsible NodeManager */
	private NodeManager nodeManager;

	/** Indices of the children (must be stored in the same NodeManager) */
	protected int[] children;

	/**
	 * Constructor for deserialization.
	 */
	private DBNode() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param nodeManager
	 *            Responsible NodeManager
	 * @param nodeIndex
	 *            If this is a node, that already exists, the nodeIndex has to
	 *            be a value greater or equal 1. If this is a new node, the value must be -1
	 */
	public DBNode(final NodeManager nodeManager, final int nodeIndex) {
		super();
		this.nodeManager = nodeManager;
		if (nodeIndex == -1) {
			this.nodeIndex = this.nodeManager.getIndexForNewNode();
		} else {
			// Workaround for root node
			if (nodeIndex == 1 && this.nodeManager.isEmpty(1)) {
				this.nodeManager.getIndexForNewNode();
			}

			this.nodeIndex = nodeIndex; // TODO Vorher pruefen, ob nodeIndex < getIndexForNewNode ist, aber der Index darf dabei NICHT erhoeht werden.
		}

		this.children = null;
		this.nodeManager.saveDBNode(this);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Serializes this node and writes the output to the OutputStream
	 */
	@Override
	public final void serialize(final NodeOutputStream nodeOutputStream) throws IOException {
		final int contentLength = this.getContentLength();
		final int childrenLength = this.getChildrenLength();

		// Output the content
		nodeOutputStream.writeInt(contentLength);

		for (int i = 0; i < contentLength; i++) {
			final String content_local = this.getContent(i);
			final byte[] output = content_local.getBytes(Deserializer.OUTPUT_ENCODING);
			nodeOutputStream.writeInt(output.length);
			nodeOutputStream.write(output);
		}

		nodeOutputStream.writeInt(childrenLength);

		for (int i = 0; i < childrenLength; i++) {
			nodeOutputStream.writeInt(this.getChildId(i));
		}

		nodeOutputStream.writeInt(this.numberOfEntries);

		this.setChanged(false);
	}

	/** {@inheritDoc} */
	@Override
	public final int getNodeIndex() {
		return this.nodeIndex;
	}

	/**
	 * Overwrites the index for the NodeManager.
	 *
	 * @param idx
	 *            New index for NodeManager
	 */
	protected final void setNodeIndex(final int idx) {
		this.nodeIndex = idx;
	}

	/** {@inheritDoc} */
	@Override
	protected final Node createNode() {
		return new DBNode(this.nodeManager, -1);
	}

	/** {@inheritDoc} */
	@Override
	public final void destroyNode(final boolean recursive) {
//		logger.debug("Destroying node with index " + this.getNodeIndex());
		if (recursive) {
			final int childrenLength = this.getChildrenLength();

			for (int i = 0; i < childrenLength; i++) {
				if (this.hasChild(i)){
					((DBNode)this.getChild(i)).destroyNode(true);
				}
			}
		}

		this.nodeManager.removeNode(this.getNodeIndex());
	}

	/** {@inheritDoc} */
	@Override
	public final boolean hasChild(final int i) {
		return this.children != null && i < this.children.length && this.children[i] != -1;
	}

	/** {@inheritDoc} */
	@Override
	public final Node getChild(final int i) {
		if (this.children != null && this.children[i] != -1){
			return (Node) this.nodeManager.loadDBNode(this.children[i], DBNode.deserializer);
		} else {
			return null;
		}
	}

	/**
	 * Sets the internal node index for a child.
	 *
	 * @param i
	 *            Array index
	 * @param idx
	 *            Node index
	 * @param idx
	 *            Node index
	 */
	protected final void setChildId(final int i, final int idx) {
		if (idx == 1) {
			System.err.println("RootNode kann kein Kind sein!");
			System.exit(0);
		}
//		logger.debug("Setting children[" + i + "] to " + idx);
		if (this.children == null && idx != -1) {
			this.children = new int[this.getContentLength()];

			for (int j = 0; j < this.children.length; j++) {
				this.children[j] = -1;
			}
		}

		if (this.children != null) {
			this.children[i] = idx;
		}

		this.setChanged(true);
	}

	/**
	 * <p>getChildId.</p>
	 *
	 * @param i
	 *            Array index
	 * @return Node index of the child if it is set, -1 otherwise
	 */
	protected final int getChildId(final int i) {
		if (this.children != null) {
			return this.children[i];
		} else {
			return -1;
		}
	}

	/** {@inheritDoc} */
	@Override
	protected final void setChild(final int i, final Node node) {
		if (node != null){
			this.setChildId(i, ((DBNode) node).getNodeIndex());
		} else {
			this.setChildId(i, -1);
		}

		this.nodeManager.saveDBNode(this);
	}

	/** {@inheritDoc} */
	@Override
	protected final boolean isFromSameTrie(final Node node) {
		if (node instanceof DBNode) {

			// If both nodes share the same node manager, they must be inside the same trie.
			return this.nodeManager == ((DBNode) node).nodeManager;
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	protected final void increaseChildrenArraySize(final int idx, final int amount) {
		if (this.children != null) {
			final int[] newChildren = new int[this.getChildrenLength() + 1];

			if (this.getChildrenLength() > 0) {
				System.arraycopy(this.children, 0, newChildren, 0, idx);
				System.arraycopy(this.children, idx, newChildren, idx + amount, this.getChildrenLength() - idx - amount + 1);

				for (int i = idx; i < idx + amount; i++) {
					newChildren[i] = -1;
				}
			}

			this.children = newChildren;
			this.setChanged(true);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected final void removeChildrenArrayElement(final int idx) {
		if (this.children != null) {
			if (this.getChildrenLength() > 1) {
				final int[] newContent = new int[this.getChildrenLength() - 1];

				System.arraycopy(this.children, 0, newContent, 0, idx);
				System.arraycopy(this.children, idx + 1, newContent, idx, this.getChildrenLength() - idx - 1);

				this.children = newContent;
			} else {
				this.children = null;
			}

			this.setChanged(true);
		}
	}

	/** {@inheritDoc} */
	@Override
	public final int getChildrenLength() {
		return (this.children == null ? 0 : this.children.length);
	}
}
