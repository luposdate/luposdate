package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
public class MapEntry<K, V> implements Entry<K, V>, Serializable,
		Comparable<Entry<K, V>> {
	private static final long serialVersionUID = -1799734392680458236L;

	public MapEntry(final K key) {
		this.k = key;
	}

	public MapEntry(final K key, final V value) {
		this.k = key;
		this.v = value;
	}

	private final K k;
	private V v;

	public K getKey() {
		return k;
	}

	public V getValue() {
		return v;
	}

	public V setValue(final V value) {
		final V res = v;
		v = value;
		return res;
	}

	@Override
	public boolean equals(final Object other) {
		return k.equals(((MapEntry<K, V>) other).k);
	}

	public int compareTo(final Entry<K, V> other) {
		return ((Comparable<K>) k).compareTo(other.getKey());
	}

	@Override
	public String toString() {
		return k + " => " + v;
	}
}
