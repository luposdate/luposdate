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
		readValues = new ArrayList<V>(k_);
		this.pageManager = pageManager;
		this.nodeDeSerializer = nodeDeSerializer;
	}

	@Override
	public String toString() {
		final String result = "Node stored in " + filename + "\n";
		final Iterator<K> it = readKeys.iterator();
		String s = "";
		for (final V v : readValues) {
			if (s.compareTo("") != 0)
				s += ",";
			s += "(";
			if (it.hasNext())
				s += it.next() + ",";
			s += v + ")";
		}
		return result + s + "\n-> " + nextLeafNode;
	}

	public void writeLeafNode(final boolean overwrite) {
		try {
			final OutputStream fos = new PageOutputStream(filename,
					pageManager, !overwrite);
			final LuposObjectOutputStream out = new LuposObjectOutputStreamWithoutWritingHeader(fos);
			out.writeLuposBoolean(true);
			final Iterator<V> it = readValues.iterator();
			V lastValue = null;
			K lastKey = null;
			for (final K k : readKeys) {
				final V v = it.next();
				writeLeafEntry(k, v, lastKey, lastValue, out);
				lastKey = k;
				lastValue = v;
			}
			if (nextLeafNode != null) {
				nodeDeSerializer.writeLeafEntryNextFileName(nextLeafNode, out);
			}
			out.close();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	public List<V> getValues() {
		return readValues;
	}

	public void setValues(final List<V> readValues) {
		this.readValues = readValues;
	}

	public int getNextLeafNode() {
		return nextLeafNode;
	}

	public void setNextLeafNode(final int nextLeafNode) {
		this.nextLeafNode = nextLeafNode;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(final boolean found) {
		this.found = found;
	}

	public DBBPTreeEntry<K, V> getNextLeafEntry(final int index) {
		if (index >= readValues.size()) {
			if (nextLeafNode != null) {
				return new DBBPTreeEntry<K, V>(null, null, nextLeafNode);
			} else {
				final DBBPTreeEntry<K, V> e = getNextLeafEntry(in,
						readKeys.size() == 0 ? null : readKeys.get(readKeys
								.size() - 1), readValues.size() == 0 ? null
								: readValues.get(readValues.size() - 1));
				if (e != null) {
					if (e.key != null)
						readKeys.add(e.key);
					if (e.value != null)
						readValues.add(e.value);
					if (e.filenameOfNextLeafNode >= 0)
						this.nextLeafNode = e.filenameOfNextLeafNode;
				}
				return e;
			}
		} else {
			return new DBBPTreeEntry<K, V>(readKeys.get(index), readValues
					.get(index));
		}
	}

	public void readFullLeafNode() {
		int posKey=readKeys.size();
		K lastKey=(posKey == 0) ? null : readKeys.get(posKey - 1);
		int posValue=readValues.size();
		V lastValue=(posValue == 0) ? null : readValues.get(posValue - 1);
		while (true) {
			final DBBPTreeEntry<K, V> e = nodeDeSerializer.getNextLeafEntry(in,lastKey,lastValue);
			if (e == null)
				return;
			if (e.filenameOfNextLeafNode >= 0) {
				nextLeafNode = e.filenameOfNextLeafNode;
				return;
			}
			if (e.key != null)
				readKeys.add(e.key);
			if (e.value != null)
				readValues.add(e.value);
			lastKey=e.key;
			lastValue=e.value;
		}
	}

	protected void writeLeafEntry(final K k, final V v, final K lastKey,
			final V lastValue, final LuposObjectOutputStream out)
			throws IOException {
		nodeDeSerializer.writeLeafEntry(k, v, out, lastKey, lastValue);
	}

	protected DBBPTreeEntry<K, V> getNextLeafEntry(
			final LuposObjectInputStream<V> in, final K lastKey,
			final V lastValue) {
		return nodeDeSerializer.getNextLeafEntry(in, lastKey, lastValue);
	}
}