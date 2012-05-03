package lupos.datastructures.paged_dbbptree;

import java.io.IOException;
import java.io.Serializable;

import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.misc.Tuple;

public interface NodeDeSerializer<K, V> extends Serializable {

	public Tuple<K, Integer> getNextInnerNodeEntry(final K lastKey2,
			final LuposObjectInputStream<V> in2);

	public DBBPTreeEntry<K, V> getNextLeafEntry(
			final LuposObjectInputStream<V> in, final K lastKey,
			final V lastValue);

	public void writeInnerNodeEntry(final int fileName, final K key,
			final LuposObjectOutputStream out, final K lastKey)
			throws IOException;

	public void writeInnerNodeEntry(final int fileName,
			final LuposObjectOutputStream out) throws IOException;

	public void writeLeafEntry(final K k, final V v,
			final LuposObjectOutputStream out, final K lastKey,
			final V lastValue) throws IOException;

	public void writeLeafEntryNextFileName(final int filename,
			final LuposObjectOutputStream out) throws IOException;

}
