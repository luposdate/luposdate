package lupos.datastructures.lsmtree.level.disk.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.BloomFilterIntTriple;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.BloomFilterIterator;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.IBloomFilter;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.Triple;
import lupos.misc.Tuple;

/**
 * Serializes and deserializes uncompressed int triples as well as the summary entries. It is assumed that the key and value are the same for each entry.
 */
public class StoreUncompressedIntTriple implements IStoreKeyValue<int[], int[]> {

	private final CollationOrder collationorder;

	public StoreUncompressedIntTriple(final CollationOrder collationorder){
		this.collationorder = collationorder;
	}

	/**
	 * Serializes one integer in the given page at the given offset.
	 *
	 * @param i_par the integer to serialize
	 * @param page the page in which the integer is to serialize
	 * @param offset the position where the integer is to serialize
	 * @return the new offset where the next data can be written
	 */
	public final static int writeInt(int i_par, final byte[] page, int offset) {
		for(int i=0; i<4; i++){
			page[offset++]=(byte) i_par;
			i_par >>>= 8;
		}
		return offset;
	}

	/**
	 * reads one integer
	 *
	 * @param page the page from which the integer is deserialized
	 * @param offset the offset in the page of the integer to be read
	 * @param length the length of the integer to be serialized (1 to 4 bytes)
	 * @return the deserialized integer
	 */
	public final static int readInteger(final byte[] page, final int offset){
		int result = 0;
		int lengthi = 3;
		while(lengthi>=0){
			result <<= 8;
			result |= (0xFF & page[offset+lengthi]);
			lengthi--;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Tuple<Integer, Entry<int[], Container<int[]>>> store(Entry<int[], Container<int[]>> entryToBeStored, final Iterator<Entry<int[], Container<int[]>>> it, final int maxNumberOfEntriesToStore, final byte[] page, int offset) throws IOException {
		int offset_removed = offset;
		for (int i = 0; i < maxNumberOfEntriesToStore; i++) {
			if (entryToBeStored != null || it.hasNext()) {
				final Entry<int[], Container<int[]>> entry;
				if (entryToBeStored != null) {
					entry = entryToBeStored;
					entryToBeStored = null;
				} else {
					entry = it.next();
				}
				final int length = 3*LengthHelper.lengthLuposInt();
				if (((i % 8 == 0) && (offset + length + 1) >= page.length)
						|| ((i % 8 != 0) && (offset + length) >= page.length)) {
					return new Tuple<Integer, Entry<int[], Container<int[]>>>(offset, entry);
				}
				if (i % 8 == 0) {
					offset_removed = offset;
					offset++;
					page[offset_removed] = 0;
				}
				if (entry.getValue().removed) {
					page[offset_removed] = (byte) (page[offset_removed] | (1 << (i % 8)));
				}
				final int[] key = entry.getKey();
				offset = StoreUncompressedIntTriple.writeInt(key[0], page, offset);
				offset = StoreUncompressedIntTriple.writeInt(key[1], page, offset);
				offset = StoreUncompressedIntTriple.writeInt(key[2], page, offset);
			} else {
				break;
			}
		}
		return new Tuple<Integer, Entry<int[], Container<int[]>>>(offset, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Triple<Integer, Entry<int[], Integer>, Object> storeSummary(final Entry<int[], Integer> entryToBeStored, final int[] previouslyStoredKey, final Object storageInfo, final int maxNumberOfEntriesToStore, final byte[] page, int offset) throws IOException {

		if (entryToBeStored != null) {
			// first entry stored with pagenumber
			if (offset == 2) {
				final int[] key = entryToBeStored.getKey();
				offset = StoreUncompressedIntTriple.writeInt(key[0], page, offset);
				offset = StoreUncompressedIntTriple.writeInt(key[1], page, offset);
				offset = StoreUncompressedIntTriple.writeInt(key[2], page, offset);
				offset = Registration.serializeWithoutId(entryToBeStored.getValue(), page, offset);
			} else {
				final int length = 3*LengthHelper.lengthLuposInt();
				if (offset + length >= page.length) {
					return new Triple<Integer, Entry<int[], Integer>, Object>(offset, entryToBeStored, null);
				}
				final int[] key = entryToBeStored.getKey();
				offset = StoreUncompressedIntTriple.writeInt(key[0], page, offset);
				offset = StoreUncompressedIntTriple.writeInt(key[1], page, offset);
				offset = StoreUncompressedIntTriple.writeInt(key[2], page, offset);
			}
		}
		return new Triple<Integer, Entry<int[], Integer>, Object>(offset, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IKeyValueIterator<Integer,Entry<int[], Container<int[]>>> getNextEntries(final int maxNumberOfCompressedEntries,
			final byte[] page, final int offset, final int maxBytesOnPage)
					throws ClassNotFoundException, IOException, URISyntaxException {

		return new IKeyValueIterator<Integer,Entry<int[], Container<int[]>>>() {
			protected boolean flag = true;
			protected int counter = 0;
			protected int offset_local = offset;
			protected byte removed_Byte;

			@Override
			public boolean hasNext() {
				return this.flag;
			}

			@Override
			public Tuple<Integer, Entry<int[], Container<int[]>>> next(final Tuple<Integer, Entry<int[], Container<int[]>>> resultObject) {
				if (!this.flag) {
					return null;
				}
				MapEntry<int[], Container<int[]>> entry = null;
				if (this.counter + 1 == maxNumberOfCompressedEntries) {
					this.flag = false;
				}
				if (this.counter % 8 == 0) {
					this.removed_Byte = page[this.offset_local];
					this.offset_local++;
				}
				final int[] triple = new int[3];
				triple[0] = StoreUncompressedIntTriple.readInteger(page, this.offset_local);
				this.offset_local+=4;
				triple[1] = StoreUncompressedIntTriple.readInteger(page, this.offset_local);
				this.offset_local+=4;
				triple[2] = StoreUncompressedIntTriple.readInteger(page, this.offset_local);
				this.offset_local+=4;
				final boolean removed = ((this.removed_Byte & (1 << (this.counter % 8))) != 0);
				entry = new MapEntry<int[], Container<int[]>>(triple,
						new Container<int[]>(triple, removed));

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
	 */
	@Override
	public IKeyValueIterator<Integer, Entry<int[], Integer>> getNextSumEntries(final int maxNumberOfCompressedEntries, final byte[] page, final int offset, final int maxBytesOnPage) throws ClassNotFoundException, IOException, URISyntaxException {

		return new IKeyValueIterator<Integer, Entry<int[], Integer>>() {

			protected boolean flag = true;
			protected int counter = 0;
			protected int offset_local = offset;
			protected int pagenumber=0;

			@Override
			public boolean hasNext() {
				return this.flag;
			}

			@Override
			public Tuple<Integer, Entry<int[], Integer>> next(final Tuple<Integer, Entry<int[], Integer>> resultObject) {
				if (!this.flag) {
					return null;
				}
				MapEntry<int[], Integer> entry = null;
				if (this.counter + 1 == maxNumberOfCompressedEntries) {
					this.flag = false;
				}
				try {
					final int[] triple = new int[3];
					triple[0] = StoreUncompressedIntTriple.readInteger(page, this.offset_local);
					this.offset_local+=4;
					triple[1] = StoreUncompressedIntTriple.readInteger(page, this.offset_local);
					this.offset_local+=4;
					triple[2] = StoreUncompressedIntTriple.readInteger(page, this.offset_local);
					this.offset_local+=4;

					// only first key stored with pagenumber
					if (this.offset_local == 2) {
						final Tuple<Integer, Integer> value = Registration.deserializeWithoutIdAndNewOffset(Integer.class, page, this.offset_local);
						this.pagenumber=value.getFirst();
						entry = new MapEntry<int[], Integer>(triple, this.pagenumber);
						this.pagenumber++;
						this.offset_local = value.getSecond();
					} else {
						entry = new MapEntry<int[], Integer>(triple, this.pagenumber);
						this.pagenumber++;
					}
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
	 */
	@Override
	public Iterator<Entry<int[], Container<int[]>>> getBloomFilterIterator(final Iterator<Entry<int[], Container<int[]>>> it, final IBloomFilter<int[]> iBloomFilter) {
		return new BloomFilterIterator<int[], Container<int[]>>(it, iBloomFilter);
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public IBloomFilter<int[]> createBloomFilter(final long maximumRunLength) {
		return new BloomFilterIntTriple(this.collationorder, maximumRunLength);
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
		OutHelper.writeLuposByte((byte) this.collationorder.ordinal(), loos);
	}


	public static StoreUncompressedIntTriple readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		return new StoreUncompressedIntTriple(CollationOrder.values()[InputHelper.readLuposByte(lois)]);
	}
}