package lupos.datastructures.lsmtree.level.disk.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.BloomFilter;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.BloomFilterIterator;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.IBloomFilter;
import lupos.io.Registration;
import lupos.misc.Triple;
import lupos.misc.Tuple;

/**
 * Serializes and deserializes key-value-pairs as well as the summary entries
 *
 * @author Maike  Herting
 *
 */
public class StoreKeyValue<K, V> implements IStoreKeyValue<K, V> {

	/**
	 * The Class that is used for keys
	 *
	 */
	protected final Class<K> keyClass;

	/**
	 * The Class that is used for value
	 *
	 */
	protected final Class<V> valueClass;

	/**
	 * Constructor setting the key and value Class
	 *
	 */
	public StoreKeyValue(final Class<K> keyClass, final Class<V> valueClass) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Serializes the key-value-pairs, the first entry is stored uncompressed, the next ones compressed
	 * First a removed byte is stored for the next 8 entries
	 *
	 * @see Registration
	 */
	@Override
	public Tuple<Integer, Entry<K, Container<V>>> store(Entry<K, Container<V>> entryToBeStored, final Iterator<Entry<K, Container<V>>> it, final int maxNumberOfEntriesToStore, final byte[] page, int offset) throws IOException {
		Entry<K, Container<V>> previous = null;
		boolean first = true;
		int offset_removed = offset;
		for (int i = 0; i < maxNumberOfEntriesToStore; i++) {
			if (entryToBeStored != null || it.hasNext()) {
				final Entry<K, Container<V>> entry;
				if (entryToBeStored != null) {
					entry = entryToBeStored;
					entryToBeStored = null;
				} else {
					entry = it.next();
				}
				// first entry uncompressed
				if (first) {
					final int length = Registration.lengthSerializeWithoutId(entry.getKey())
							+ ((entry.getValue().removed) ? 0
									: Registration.lengthSerializeWithoutId(entry.getValue().getValue()));
					if (offset + length + 1 >= page.length) {
						return new Tuple<Integer, Entry<K, Container<V>>>(offset, entry);
					}
					offset_removed = offset;
					offset++;
					page[offset_removed] = 0;
					if (entry.getValue().removed) {
						page[offset_removed] = (byte) (page[offset_removed] | 1);
					}
					offset = Registration.serializeWithoutId(entry.getKey(), page, offset);
					if (!entry.getValue().removed) {
						offset = Registration.serializeWithoutId(entry.getValue().getValue(), page, offset);
					}
					first = false;
				} else {
					// next entries compressed
					if (previous != null) {
						final int length = Registration.lengthSerializeWithoutId(entry.getKey(), previous.getKey())
								+ ((entry.getValue().removed) ? 0
										: Registration.lengthSerializeWithoutId(entry.getValue().getValue()));
						if (((i % 8 == 0) && (offset + length + 1) >= page.length)
								|| ((i % 8 != 0) && (offset + length) >= page.length)) {
							return new Tuple<Integer, Entry<K, Container<V>>>(offset, entry);
						}
						if (i % 8 == 0) {
							offset_removed = offset;
							offset++;
							page[offset_removed] = 0;
						}
						if (entry.getValue().removed) {
							page[offset_removed] = (byte) (page[offset_removed] | (1 << (i % 8)));
						}
						offset = Registration.serializeWithoutId(entry.getKey(), previous.getKey(), page, offset);
						if (!entry.getValue().removed) {
							offset = Registration.serializeWithoutId(entry.getValue().getValue(), page, offset);
						}
					}
				}
				previous = entry;
			} else {
				break;
			}
		}
		return new Tuple<Integer, Entry<K, Container<V>>>(offset, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Serializes the key-pagenumber-pairs, only the first entry is stored with the pagenumber, for the following ones only the keys are stored
	 *
	 * @see Registration
	 *
	 */
	@Override
	public Triple<Integer, Entry<K, Integer>, Object> storeSummary(final Entry<K, Integer> entryToBeStored, final K previouslyStoredKey, final Object storageInfo, final int maxNumberOfEntriesToStore, final byte[] page, int offset) throws IOException {

		if (entryToBeStored != null) {
			// first entry stored with pagenumber
			if (offset == 2) {
				offset = Registration.serializeWithoutId(entryToBeStored.getKey(), page, offset);
				offset = Registration.serializeWithoutId(entryToBeStored.getValue(), page, offset);
			} else {
				final int length = (previouslyStoredKey!=null)? Registration.lengthSerializeWithoutId(entryToBeStored.getKey(), previouslyStoredKey): Registration.lengthSerializeWithoutId(entryToBeStored.getKey());
				if (offset + length >= page.length) {
					return new Triple<Integer, Entry<K, Integer>, Object>(offset, entryToBeStored, null);
				}
				if(previouslyStoredKey!=null){
					offset = Registration.serializeWithoutId(entryToBeStored.getKey(), previouslyStoredKey, page, offset);
				} else {
					offset = Registration.serializeWithoutId(entryToBeStored.getKey(), page, offset);
				}
			}
		}
		return new Triple<Integer, Entry<K, Integer>, Object>(offset, null, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Deserializes the key-value-pairs
	 *
	 * @see Registration
	 */
	@Override
	public IKeyValueIterator<Integer,Entry<K, Container<V>>> getNextEntries(final int maxNumberOfCompressedEntries,
			final byte[] page, final int offset, final int maxBytesOnPage)
					throws ClassNotFoundException, IOException, URISyntaxException {

		return new IKeyValueIterator<Integer,Entry<K, Container<V>>>() {
			protected boolean flag = true;
			protected int counter = 0;
			protected K previousValue;
			protected int offset_local = offset;
			protected byte removed_Byte;

			@Override
			public boolean hasNext() {
				return this.flag;
			}

			@Override
			public Tuple<Integer, Entry<K, Container<V>>> next(final Tuple<Integer, Entry<K, Container<V>>> resultObject) {
				if (!this.flag) {
					return null;
				}
				MapEntry<K, Container<V>> entry = null;
				if (this.counter + 1 == maxNumberOfCompressedEntries) {
					this.flag = false;
				}
				try {
					final Tuple<K, Integer> key;
					if (this.counter == 0) {
						this.removed_Byte = page[this.offset_local];
						this.offset_local++;
						// uncompressed
						key = Registration.deserializeWithoutIdAndNewOffset(StoreKeyValue.this.keyClass, page, this.offset_local);
					} else {
						if (this.counter % 8 == 0) {
							this.removed_Byte = page[this.offset_local];
							this.offset_local++;
						}
						// compressed
						key = Registration.deserializeWithoutIdAndNewOffset(StoreKeyValue.this.keyClass, this.previousValue, page, this.offset_local);
					}
					this.previousValue = key.getFirst();
					final boolean removed = ((this.removed_Byte & (1 << (this.counter % 8))) != 0);
					final Tuple<V, Integer> value = (removed) ? null
							: Registration.deserializeWithoutIdAndNewOffset(StoreKeyValue.this.valueClass, page,
									key.getSecond());
					entry = new MapEntry<K, Container<V>>(key.getFirst(),
							new Container<V>((value == null) ? null : value.getFirst(), removed));
					this.offset_local = (value == null) ? key.getSecond() : value.getSecond();
				} catch (ClassNotFoundException | IOException | URISyntaxException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				if (this.offset_local >= maxBytesOnPage) {
					this.flag = false;
				}
				this.counter++;
				resultObject.setFirst(this.offset_local);
				resultObject.setSecond(entry);
				return resultObject;
			}
		};
	}

	/**
	 * {@inheritDoc}
	 *
	 * Deserializes the key-pagenumber-pairs, generating the pagenumber for all entries from the first entry
	 *
	 * @see Registration
	 */
	@Override
	public IKeyValueIterator<Integer, Entry<K, Integer>> getNextSumEntries(final int maxNumberOfCompressedEntries, final byte[] page, final int offset, final int maxBytesOnPage) throws ClassNotFoundException, IOException, URISyntaxException {

		return new IKeyValueIterator<Integer, Entry<K, Integer>>() {

			protected boolean flag = true;
			protected int counter = 0;
			protected int offset_local = offset;
			protected int pagenumber=0;
			protected K previousKey = null;

			@Override
			public boolean hasNext() {
				return this.flag;
			}

			@Override
			public Tuple<Integer, Entry<K, Integer>> next(final Tuple<Integer, Entry<K, Integer>> resultObject) {
				if (!this.flag) {
					return null;
				}
				MapEntry<K, Integer> entry = null;
				if (this.counter + 1 == maxNumberOfCompressedEntries) {
					this.flag = false;
				}
				try {
					final Tuple<K, Integer> key = (this.previousKey==null)?
							Registration.deserializeWithoutIdAndNewOffset(StoreKeyValue.this.keyClass, page, this.offset_local)
							:Registration.deserializeWithoutIdAndNewOffset(StoreKeyValue.this.keyClass, this.previousKey, page, this.offset_local);
					this.previousKey = key.getFirst();
					// only first key stored with pagenumber
					if (this.offset_local == 2) {
						final Tuple<Integer, Integer> value = Registration.deserializeWithoutIdAndNewOffset(Integer.class, page, key.getSecond());
						this.pagenumber=value.getFirst();
						entry = new MapEntry<K, Integer>(key.getFirst(), this.pagenumber);
						this.pagenumber++;
						this.offset_local = value.getSecond();
					} else {
						entry = new MapEntry<K, Integer>(key.getFirst(), this.pagenumber);
						this.pagenumber++;
						this.offset_local = key.getSecond();
					}
				} catch (ClassNotFoundException | IOException | URISyntaxException e) {
					System.err.println(e);
					e.printStackTrace();
				}
				if (this.offset_local >= maxBytesOnPage) {
					this.flag = false;
					this.previousKey = null;
				}
				this.counter++;
				resultObject.setFirst(this.offset_local);
				resultObject.setSecond(entry);
				return resultObject;
			}
		};
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public Iterator<Entry<K, Container<V>>> getBloomFilterIterator(final Iterator<Entry<K, Container<V>>> it,
			final IBloomFilter<K> iBloomFilter) {
		return new BloomFilterIterator<K, Container<V>>(it, iBloomFilter);
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public IBloomFilter<K> createBloomFilter(final long maximumRunLength) {
		return new BloomFilter<K>(maximumRunLength);
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
		Registration.serializeClass(this.keyClass, loos);
		Registration.serializeClass(this.valueClass, loos);
	}

	@SuppressWarnings("unchecked")
	public static<K, V> StoreKeyValue<K, V> readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		return new StoreKeyValue<K, V>((Class<K>)Registration.deserializeId(lois)[0], (Class<V>)Registration.deserializeId(lois)[0]);
	}
}
