package lupos.datastructures.paged_dbbptree;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lupos.io.LuposObjectInputStream;

public abstract class Node<K extends Comparable<K> & Serializable, V extends Serializable> {
	protected LuposObjectInputStream<V> in;
	protected int filename;
	protected List<K> readKeys;

	protected Class<? super K> keyClass;
	protected Class<? super V> valueClass;

	protected Node(final Class<? super K> keyClass,
			final Class<? super V> valueClass, final int size) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
		readKeys = new ArrayList<K>(size);
	}

	public LuposObjectInputStream<V> getIn() {
		return in;
	}

	public void setIn(final LuposObjectInputStream<V> in) {
		this.in = in;
	}

	public int getFilename() {
		return filename;
	}

	public void setFilename(final int filename) {
		this.filename = filename;
	}

	public List<K> getKeys() {
		return readKeys;
	}

	public void setKeys(final List<K> readKeys) {
		this.readKeys = readKeys;
	}

	@Override
	public void finalize() {
		try {
			in.close();
		} catch (final IOException e1) {
		}
	}
}