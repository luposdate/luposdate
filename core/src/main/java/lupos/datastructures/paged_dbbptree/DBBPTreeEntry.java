package lupos.datastructures.paged_dbbptree;

public class DBBPTreeEntry<K, V> {
	public K key;
	public V value;
	public int filenameOfNextLeafNode;

	public DBBPTreeEntry(final K key, final V value) {
		this.key = key;
		this.value = value;
	}

	public DBBPTreeEntry(final K key, final V value,
			final int filenameOfNextLeafNode) {
		this.key = key;
		this.value = value;
		this.filenameOfNextLeafNode = filenameOfNextLeafNode;
	}

	@Override
	public String toString() {
		if (key != null)
			return key.toString() + " -> " + value.toString();
		else
			return "Next leaf node:" + filenameOfNextLeafNode;
	}
}