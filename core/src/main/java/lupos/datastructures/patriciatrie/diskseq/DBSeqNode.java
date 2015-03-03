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

import java.io.IOException;

import lupos.datastructures.patriciatrie.disk.Deserializer;
import lupos.datastructures.patriciatrie.disk.nodemanager.NodeInputStream;
import lupos.datastructures.patriciatrie.diskseq.nodemanager.SeqNodeManager;
import lupos.datastructures.patriciatrie.node.Node;


/**
 * This class extends the abstract Node class and implements a disk based behavior for sequential serialization of the whole trie.
 */
public class DBSeqNode extends Node {


	public final static DeSerializer deSerializer = new DeSerializer(){
		/**
		 * Reads all relevant information for a DBSeqNode from a given InputStream
		 * and returns a DBSeqNode instance.
		 *
		 * @param nodeManager
		 *            The NodeManager, that handles all read and write requests for
		 *            this trie.
		 * @param inputStream
		 *            The InputStream to read from
		 * @return DBSeqNode instance
		 * @throws IOException
		 */
		@Override
		public Node deserialize(final SeqNodeManager nodeManager, final NodeInputStream inputStream) throws IOException {
			final DBSeqNode node = new DBSeqNode(nodeManager);
			final int contentLength = inputStream.readInt();

			node.setContent(new String[contentLength]);

			for (int i = 0; i < contentLength; i++) {
				final int len = inputStream.readInt();
				final byte[] content = new byte[len];
				for (int j = 0; j < len; j++) {
					content[j] = (byte) inputStream.read();
				}

				node.getContent()[i] = new String(content, Deserializer.OUTPUT_ENCODING);
			}

			final int childrenLength = inputStream.readInt();

			node.children = new boolean[childrenLength];

			for (int i = 0; i < childrenLength; i++) {
				node.children[i] = (inputStream.read() == 1);
			}

			node.numberOfEntries = inputStream.readInt();

			node.setChanged(false);

			return node;
		}

		/**
		 * Serializes this node and writes the output to the OutputStream
		 *
		 * @param node
		 *            node to be serialized...
		 * @param writer
		 *            Stream to write the serialized data to
		 * @throws IOException
		 */
		@Override
		public final void serialize(final Node node, final Writer writer) throws IOException {
			final int contentLength = node.getContentLength();
			final int childrenLength = node.getChildrenLength();

			// Output the content
			writer.writeInt(contentLength);

			for (int i = 0; i < contentLength; i++) {
				final String content_local = node.getContent(i);
				final byte[] output = content_local.getBytes(Deserializer.OUTPUT_ENCODING);
				writer.writeInt(output.length);
				writer.write(output);
			}

			writer.writeInt(childrenLength);

			for (int i = 0; i < childrenLength; i++) {
				writer.write(node.hasChild(i) ? 1 : 0);
			}

			writer.writeInt(node.getNumberOfEntries());

			node.setChanged(false);
		}
	};

	/** The NodeManager, that handles all read and write requests for this trie. */
	protected SeqNodeManager nodeManager;

	/** hasChild flags */
	protected boolean[] children;

	/**
	 * Constructor
	 *
	 * @param nodeManager
	 *            The NodeManager, that handles all read and write requests for this trie.
	 */
	public DBSeqNode(final SeqNodeManager nodeManager) {
		super();
		this.nodeManager = nodeManager;
	}

	@Override
	public final Node createNode() {
		return new DBSeqNode(this.nodeManager);
	}

	@Override
	public final boolean hasChild(final int i) {
		return this.children != null && this.children.length > i && this.children[i];
	}

	@Override
	public final Node getChild(final int i) {
		if (this.hasChild(i)) {
			return this.nodeManager.readNextNode(DBSeqNode.deSerializer);
		} else {
			return null;
		}
	}

	@Override
	protected final void setChild(final int i, final Node node) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final boolean isFromSameTrie(final Node node) {
		return false;
	}

	@Override
	protected final void increaseChildrenArraySize(final int idx, final int amount) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected final void removeChildrenArrayElement(final int idx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final int getChildrenLength() {
		return this.children == null ? 0 : this.children.length;
	}

	/**
	 * @param children the children to set
	 */
	public final void setChildren(final boolean[] children) {
		this.children = children;
	}

	/**
	 * @return the children
	 */
	public final boolean[] getChildren() {
		return this.children;
	}

	/**
	 * @param nodeManager the nodeManager to set
	 */
	public final void setNodeManager(final SeqNodeManager nodeManager) {
		this.nodeManager = nodeManager;
	}

	/**
	 * @return the nodeManager
	 */
	public final SeqNodeManager getNodeManager() {
		return this.nodeManager;
	}
}
