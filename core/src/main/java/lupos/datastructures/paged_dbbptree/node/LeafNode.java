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
package lupos.datastructures.paged_dbbptree.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.buffermanager.PageOutputStream;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.NodeDeSerializer;
import lupos.io.helper.OutHelper;
public class LeafNode<K extends Serializable, V extends Serializable> extends Node<K, V> {

	public List<V> readValues = new LinkedList<V>();
	public Integer nextLeafNode = null;
	public boolean found;
	protected final PageManager pageManager;
	protected final NodeDeSerializer<K, V> nodeDeSerializer;

	/**
	 * <p>Constructor for LeafNode.</p>
	 *
	 * @param keyClass a {@link java.lang.Class} object.
	 * @param valueClass a {@link java.lang.Class} object.
	 * @param k_ a int.
	 * @param pageManager a {@link lupos.datastructures.buffermanager.PageManager} object.
	 * @param nodeDeSerializer a {@link lupos.datastructures.paged_dbbptree.node.nodedeserializer.NodeDeSerializer} object.
	 */
	public LeafNode(final Class<? super K> keyClass,
			final Class<? super V> valueClass, final int k_,
			final PageManager pageManager,
			final NodeDeSerializer<K, V> nodeDeSerializer) {
		super(keyClass, valueClass, k_);
		this.readValues = new ArrayList<V>(k_);
		this.pageManager = pageManager;
		this.nodeDeSerializer = nodeDeSerializer;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final String result = "Node stored in " + this.filename + "\n";
		final Iterator<K> it = this.readKeys.iterator();
		String s = "";
		for (final V v : this.readValues) {
			if (s.compareTo("") != 0) {
				s += ",";
			}
			s += "(";
			if (it.hasNext()) {
				s += it.next() + ",";
			}
			s += v + ")";
		}
		return result + s + "\n-> " + this.nextLeafNode;
	}

	/**
	 * <p>writeLeafNode.</p>
	 *
	 * @param overwrite a boolean.
	 */
	public void writeLeafNode(final boolean overwrite) {
		try {
			final OutputStream out = new PageOutputStream(this.filename, this.pageManager, !overwrite);
			OutHelper.writeLuposBoolean(true, out);
			final Iterator<V> it = this.readValues.iterator();
			V lastValue = null;
			K lastKey = null;
			for (final K k : this.readKeys) {
				final V v = it.next();
				this.writeLeafEntry(k, v, lastKey, lastValue, out);
				lastKey = k;
				lastValue = v;
			}
			if (this.nextLeafNode != null) {
				this.nodeDeSerializer.writeLeafEntryNextFileName(this.nextLeafNode, out);
			}
			out.close();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	/**
	 * <p>getValues.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<V> getValues() {
		return this.readValues;
	}

	/**
	 * <p>setValues.</p>
	 *
	 * @param readValues a {@link java.util.List} object.
	 */
	public void setValues(final List<V> readValues) {
		this.readValues = readValues;
	}

	/**
	 * <p>Getter for the field <code>nextLeafNode</code>.</p>
	 *
	 * @return a int.
	 */
	public int getNextLeafNode() {
		return this.nextLeafNode;
	}

	/**
	 * <p>Setter for the field <code>nextLeafNode</code>.</p>
	 *
	 * @param nextLeafNode a int.
	 */
	public void setNextLeafNode(final int nextLeafNode) {
		this.nextLeafNode = nextLeafNode;
	}

	/**
	 * <p>isFound.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFound() {
		return this.found;
	}

	/**
	 * <p>Setter for the field <code>found</code>.</p>
	 *
	 * @param found a boolean.
	 */
	public void setFound(final boolean found) {
		this.found = found;
	}

	/**
	 * <p>getNextLeafEntry.</p>
	 *
	 * @param index a int.
	 * @return a {@link lupos.datastructures.paged_dbbptree.node.DBBPTreeEntry} object.
	 */
	public DBBPTreeEntry<K, V> getNextLeafEntry(final int index) {
		if (index >= this.readValues.size()) {
			if (this.nextLeafNode != null) {
				return new DBBPTreeEntry<K, V>(null, null, this.nextLeafNode);
			} else {
				final DBBPTreeEntry<K, V> e = this.getNextLeafEntry(this.in,
						this.readKeys.size() == 0 ? null : this.readKeys.get(this.readKeys
								.size() - 1), this.readValues.size() == 0 ? null
								: this.readValues.get(this.readValues.size() - 1));
				if (e != null) {
					if (e.key != null) {
						this.readKeys.add(e.key);
					}
					if (e.value != null) {
						this.readValues.add(e.value);
					}
					if (e.filenameOfNextLeafNode >= 0) {
						this.nextLeafNode = e.filenameOfNextLeafNode;
					}
				}
				return e;
			}
		} else {
			return new DBBPTreeEntry<K, V>(this.readKeys.get(index), this.readValues
					.get(index));
		}
	}

	/**
	 * <p>readFullLeafNode.</p>
	 */
	public void readFullLeafNode() {
		final int posKey=this.readKeys.size();
		K lastKey=(posKey == 0) ? null : this.readKeys.get(posKey - 1);
		final int posValue=this.readValues.size();
		V lastValue=(posValue == 0) ? null : this.readValues.get(posValue - 1);
		while (true) {
			final DBBPTreeEntry<K, V> e = this.nodeDeSerializer.getNextLeafEntry(this.in,lastKey,lastValue);
			if (e == null) {
				return;
			}
			if (e.filenameOfNextLeafNode >= 0) {
				this.nextLeafNode = e.filenameOfNextLeafNode;
				return;
			}
			if (e.key != null) {
				this.readKeys.add(e.key);
			}
			if (e.value != null) {
				this.readValues.add(e.value);
			}
			lastKey=e.key;
			lastValue=e.value;
		}
	}

	/**
	 * <p>writeLeafEntry.</p>
	 *
	 * @param k a K object.
	 * @param v a V object.
	 * @param lastKey a K object.
	 * @param lastValue a V object.
	 * @param out a {@link java.io.OutputStream} object.
	 * @throws java.io.IOException if any.
	 */
	protected void writeLeafEntry(final K k, final V v, final K lastKey,
			final V lastValue, final OutputStream out)
			throws IOException {
		this.nodeDeSerializer.writeLeafEntry(k, v, out, lastKey, lastValue);
	}

	/**
	 * <p>getNextLeafEntry.</p>
	 *
	 * @param in a {@link java.io.InputStream} object.
	 * @param lastKey a K object.
	 * @param lastValue a V object.
	 * @return a {@link lupos.datastructures.paged_dbbptree.node.DBBPTreeEntry} object.
	 */
	protected DBBPTreeEntry<K, V> getNextLeafEntry(
			final InputStream in, final K lastKey,
			final V lastValue) {
		return this.nodeDeSerializer.getNextLeafEntry(in, lastKey, lastValue);
	}
}
