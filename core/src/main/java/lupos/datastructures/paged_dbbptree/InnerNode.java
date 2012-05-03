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
import lupos.io.LuposObjectOutputStream;
import lupos.io.LuposObjectOutputStreamWithoutWritingHeader;
import lupos.misc.Tuple;

public class InnerNode<K extends Comparable<K> & Serializable, V extends Serializable>
		extends Node<K, V> {
	protected List<Integer> readReferences = new LinkedList<Integer>();
	protected final PageManager pageManager;
	protected final NodeDeSerializer<K, V> nodeDeSerializer;

	public InnerNode(final Class<? super K> keyClass,
			final Class<? super V> valueClass, final int k,
			final PageManager pageManager,
			final NodeDeSerializer<K, V> nodeDeSerializer) {
		super(keyClass, valueClass, k);
		readReferences = new ArrayList<Integer>(k + 1);
		this.pageManager = pageManager;
		this.nodeDeSerializer = nodeDeSerializer;
	}

	@Override
	public String toString() {
		String result = "Node stored in " + filename + "\n";
		final Iterator<K> it = readKeys.iterator();
		for (final Integer s : readReferences) {
			result += "-" + s + "- ";
			if (it.hasNext())
				result += "[" + it.next() + "]";
		}
		return result;
	}

	public void readNextEntry() {
		final Tuple<K, Integer> result = nodeDeSerializer
				.getNextInnerNodeEntry(readKeys.size() == 0 ? null : readKeys
						.get(readKeys.size() - 1), in);
		if (result == null) {
			try {
				in.close();
			} catch (final IOException e1) {
			}
			return;
		}

		final int nextFilename = result.getSecond();
		readReferences.add(nextFilename);

		final K nextKey = result.getFirst();

		if (nextKey == null) {
			try {
				in.close();
			} catch (final IOException e1) {
			}
			return;
		}

		readKeys.add(nextKey);
	}

	public void readFullInnerNode() {
		int pos=this.readKeys.size();
		K lastKey=(pos==0)?null:this.readKeys.get(pos-1);
		while (true) {
			final Tuple<K, Integer> nextEntry = nodeDeSerializer
					.getNextInnerNodeEntry(lastKey, in);
			if (nextEntry == null || nextEntry.getSecond() == null
					|| nextEntry.getSecond() < 0)
				return;
			readReferences.add(nextEntry.getSecond());
			if (nextEntry.getFirst() == null)
				return;
			lastKey = nextEntry.getFirst();
			readKeys.add(nextEntry.getFirst());
		}
	}

	public List<Integer> getReferences() {
		return readReferences;
	}

	public void setReferences(final List<Integer> readReferences) {
		this.readReferences = readReferences;
	}

	public void writeInnerNode(final boolean overwrite) {
		try {
			final OutputStream fos = new PageOutputStream(filename,
					pageManager, !overwrite);
			final LuposObjectOutputStream out = new LuposObjectOutputStreamWithoutWritingHeader(fos);
			out.writeLuposBoolean(false);

			K lastKey = null;
			final Iterator<Integer> it = readReferences.iterator();
			for (final K k : readKeys) {
				nodeDeSerializer
						.writeInnerNodeEntry(it.next(), k, out, lastKey);
				lastKey = k;
			}
			if (it.hasNext())
				nodeDeSerializer.writeInnerNodeEntry(it.next(), out);
			out.close();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
}