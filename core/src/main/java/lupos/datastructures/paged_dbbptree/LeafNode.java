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
package lupos.datastructures.paged_dbbptree;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.buffermanager.PageOutputStream;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.io.LuposObjectOutputStreamWithoutWritingHeader;
import lupos.io.helper.OutHelper;

public class LeafNode<K extends Comparable<K> & Serializable, V extends Serializable>
		extends Node<K, V> {

	protected List<V> readValues = new LinkedList<V>();
	protected Integer nextLeafNode = null;
	protected boolean found;
	protected final PageManager pageManager;
	protected final NodeDeSerializer<K, V> nodeDeSerializer;

	public LeafNode(final Class<? super K> keyClass,
			final Class<? super V> valueClass, final int k_,
			final PageManager pageManager,
			final NodeDeSerializer<K, V> nodeDeSerializer) {
		super(keyClass, valueClass, k_);
		this.readValues = new ArrayList<V>(k_);
		this.pageManager = pageManager;
		this.nodeDeSerializer = nodeDeSerializer;
	}

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

	public void writeLeafNode(final boolean overwrite) {
		try {
			final OutputStream fos = new PageOutputStream(this.filename,
					this.pageManager, !overwrite);
			final LuposObjectOutputStream out = new LuposObjectOutputStreamWithoutWritingHeader(fos);
			OutHelper.writeLuposBoolean(true, out.os);
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

	public List<V> getValues() {
		return this.readValues;
	}

	public void setValues(final List<V> readValues) {
		this.readValues = readValues;
	}

	public int getNextLeafNode() {
		return this.nextLeafNode;
	}

	public void setNextLeafNode(final int nextLeafNode) {
		this.nextLeafNode = nextLeafNode;
	}

	public boolean isFound() {
		return this.found;
	}

	public void setFound(final boolean found) {
		this.found = found;
	}

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

	protected void writeLeafEntry(final K k, final V v, final K lastKey,
			final V lastValue, final LuposObjectOutputStream out)
			throws IOException {
		this.nodeDeSerializer.writeLeafEntry(k, v, out, lastKey, lastValue);
	}

	protected DBBPTreeEntry<K, V> getNextLeafEntry(
			final LuposObjectInputStream<V> in, final K lastKey,
			final V lastValue) {
		return this.nodeDeSerializer.getNextLeafEntry(in, lastKey, lastValue);
	}
}