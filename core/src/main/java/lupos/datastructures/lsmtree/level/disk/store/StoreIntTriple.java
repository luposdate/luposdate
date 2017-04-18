package lupos.datastructures.lsmtree.level.disk.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.lsmtree.LSMTree;
import lupos.datastructures.lsmtree.debug.IKeyValuePrinter;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.BloomFilterIntTriple;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.BloomFilterIterator;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.IBloomFilter;
import lupos.datastructures.lsmtree.level.factory.DiskLevelFactory;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.lsmtree.sip.ISIPIterator;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.LengthHelper;
import lupos.io.helper.OutHelper;
import lupos.io.serializer.COMPARATOR;
import lupos.misc.Triple;
import lupos.misc.Tuple;

/**
 * Serializes and deserializes int-triples as well as the summary entries. It is assumed that the key and the value are the same.
 */
public class StoreIntTriple implements IStoreKeyValue<int[], int[]> {

	/**
	 * the collation order after which the triples are ordered in this lsm-tree
	 */
	protected final CollationOrder collationorder;

	/**
	 * The position in the int-triple of the primary sort criterion.
	 * It is only once determined because of performance reasons...
	 */
	protected final int pos0;
	/**
	 * The position in the int-triple of the secondary sort criterion.
	 * It is only once determined because of performance reasons...
	 */
	protected final int pos1;
	/**
	 * The position in the int-triple of the tertiary sort criterion.
	 * It is only once determined because of performance reasons...
	 */
	protected final int pos2;

	/**
	 * Constructor setting the collation order
	 *
	 */
	public StoreIntTriple(final CollationOrder collationorder) {
		this.collationorder = collationorder;
		this.pos0 = this.collationorder.getSortCriterium(0);
		this.pos1 = this.collationorder.getSortCriterium(1);
		this.pos2 = this.collationorder.getSortCriterium(2);
	}

	/**
	 * Determines the number of bytes necessary to store the given integer value
	 *
	 * @param value the integer value to be stored
	 * @return the number of bytes necessary to store the given integer value
	 */
	public final static int length(int value){
		int result = 0;
		do {
			result++;
			value >>>= 8;
		} while (value > 0);
		return result;
	}

	/**
	 * Determine the number of bytes, which are necessary to store the given int-triple...
	 *
	 * @param triple the int-triple to be stored
	 * @return the number of bytes, which are necessary to store the given int-triple
	 */
	public final static int length(final int[] triple){
		return 1 + StoreIntTriple.length(triple[0]) + StoreIntTriple.length(triple[1]) + StoreIntTriple.length(triple[2]);
	}

	/**
	 * Determine the number of bytes, which are necessary to store the given int-triple by using difference encoding compared to the previously stored triple...
	 *
	 * @param triple the int-triple to be stored
	 * @param previous the previous int-triple to be stored
	 * @param offsetbit the current bit in a remaining bit vector of size one byte
	 * @return the number of bytes, which are necessary to store the given int-triple
	 */
	public final int length(final int[] triple, final int[] previous, final int offsetbit){
		if(triple[this.pos0]==previous[this.pos0]){
			if(triple[this.pos1]==previous[this.pos1]){
				return ((offsetbit + 5) >= 8 ? 1 : 0) + length(triple[this.pos2]-previous[this.pos2]);
			} else {
				return ((offsetbit + 7) >= 8 ? 1 : 0) + length(triple[this.pos1]-previous[this.pos1]) + length(triple[this.pos2]);
			}
		} else {
			return 1 + length(triple[this.pos0]-previous[this.pos0]) + length(triple[this.pos1]) + length(triple[this.pos2]);
		}
	}

	/**
	 * Serializes one integer in the given page at the given offset.
	 * Only as much bytes are used as needed.
	 *
	 * @param i_par the integer to serialize
	 * @param page the page in which the integer is to serialize
	 * @param offset the position where the integer is to serialize
	 * @return the new offset where the next data can be written
	 */
	public final static int writeInt(int i_par, final byte[] page, int offset) {
		do {
			page[offset++]=(byte) i_par;
			i_par >>>= 8;
		} while (i_par > 0);
		return offset;
	}

	/**
	 * Write 2 bits for values 1 to 4. It is assumed that the two bits can be stored in the same byte.
	 *
	 * @param value the value in the domain 1 to 4
	 * @param page the page on which the bit vector is stored
	 * @param offset the offset in the page of the bit vector
	 * @param offsetbit the current bit
	 */
	public final static void write2Bits(final int value, final byte[] page, final int offset, final int offsetbit){
		if(value==1 || value==3){
			page[offset] = (byte) (page[offset] & ~(1 << offsetbit));
		} else {
			page[offset] = (byte) (page[offset] | (1 << offsetbit));
		}
		// write next bit
		if(value==1 || value==2){
			page[offset] = (byte) (page[offset] & ~(1 << (offsetbit+1)));
		} else {
			page[offset] = (byte) (page[offset] | (1 << (offsetbit+1)));
		}
	}

	/**
	 * Write 2 bits for values 1 to 4. In case of an overflow the byte at position offset2 is used for storing the bit(s).
	 *
	 * @param value the value in the domain 1 to 4
	 * @param page the page on which the bit vector is stored
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!) and returned back):
	 * 			position 0: the offset in the page of the bit vector in case the second bit must be stored in another byte
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit
	 * @return the updated offsets-array
	 */
	public final static int[] write2Bits(final int value, final byte[] page, int[] offsets){
		if(value==1 || value==3){
			offsets = write1Bit(false, page, offsets);
		} else {
			offsets = write1Bit(true, page, offsets);
		}
		if(value==1 || value==2){
			offsets = write1Bit(false, page, offsets);
		} else {
			offsets = write1Bit(true, page, offsets);
		}
		return offsets;
	}

	/**
	 * Write 1 bit for values false or true. In case of an overflow the byte at position offset2 is used for storing the bit.
	 *
	 * @param value the value in the boolean domain
	 * @param page the page on which the bit vector is stored
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!) and returned back):
	 * 			position 0: the offset in the page of the bit vector in case the second bit must be stored in another byte
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit
	 * @return the updated offsets-array
	 */
	public final static int[] write1Bit(final boolean value, final byte[] page, final int[] offsets){
		// is it necessary to start a new byte (which is stored at offset2)?
		if(offsets[2]>=8){
			offsets[1] = offsets[0];
			offsets[0]++;
			offsets[2] = 0;
		}
		if(value){
			page[offsets[1]] = (byte) (page[offsets[1]] | (1 << offsets[2]));
		} else {
			page[offsets[1]] = (byte) (page[offsets[1]] & ~(1 << offsets[2]));
		}
		offsets[2]++;
		return offsets;
	}

	/**
	 * Write 1 bit for values 0 or 1.
	 *
	 * @param value the value in the domain 0 to 1
	 * @param page the page on which the bit vector is stored
	 * @param offset the offset in the page of the bit vector
	 * @param offsetbit the current bit
	 */
	public final static void write1Bit(final boolean value, final byte[] page, final int offset, final int offsetbit){
		if(value){
			page[offset] = (byte) (page[offset] | (1 << offsetbit));
		} else {
			page[offset] = (byte) (page[offset] & ~(1 << offsetbit));
		}
	}

	/**
	 * Serializes the given entry containing the int-triple and the  in the given page at the position offset and returns the new offset
	 *
	 * @param entry the int-triple to be stored
	 * @param page the page in which the int-triple is stored
	 * @param offset the position in the page where the int-triple is stored
	 * @return the new offset for the next data to be stored in the given page
	 */
	public final int serialize(final Entry<int[], Container<int[]>> entry, final byte[] page, final int offset){
		final int[] triple = entry.getKey();
		write1Bit(entry.getValue().removed, page, offset, 0);
		return this.serialize(triple, page, offset, 1);
	}

	/**
	 * Serializes the given int-triple in the given page at the position offset and returns the new offset. Note that it is assumed that never an overflow happens in the bit vector (must be the case if you start with bit 0 or 1).
	 *
	 * @param triple the int-triple to be stored
	 * @param page the page in which the int-triple is stored
	 * @param offset the position in the page where the int-triple is stored
	 * @param offsetbitvector the current position in the bit vector (must be 0 or 1)
	 * @return the new offset for the next data to be stored in the given page
	 */
	public final int serialize(final int[] triple, final byte[] page, final int offset, final int offsetbitvector){
		write2Bits(length(triple[this.pos0]), page, offset, offsetbitvector);
		write2Bits(length(triple[this.pos1]), page, offset, offsetbitvector+2);
		write2Bits(length(triple[this.pos2]), page, offset, offsetbitvector+4);
		int result = StoreIntTriple.writeInt(triple[this.pos0], page, offset + 1);
		result = StoreIntTriple.writeInt(triple[this.pos1], page, result);
		result = StoreIntTriple.writeInt(triple[this.pos2], page, result);
		return result;
	}

	/**
	 * Serializes the given int-triple in the given page at the position offset and returns the new offset
	 *
	 * @param triple the int-triple to be stored
	 * @param page the page in which the int-triple is stored
	 * @param offset the position in the page where the int-triple is stored
	 * @return the new offset for the next data to be stored in the given page
	 */
	public final int serialize(final int[] triple, final byte[] page, final int offset){
		return this.serialize(triple, page, offset, 0);
	}

	/**
	 * Serializes the given entry (consisting of int-triple and marker for insertion/deletion).
	 *
	 * @param entry the entry to be serialized
	 * @param previous the previous serialized int-triple (such that the given entry can be serialized using difference encoding)
	 * @param page the page in which the given entry is serialized
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!) and returned back):
	 * 			position 0: the offset in the page of the bit vector in case the second bit must be stored in another byte
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit
	 * @return the updated offsets-array
	 */
	public final int[] serialize(final Entry<int[], Container<int[]>> entry, final int[] previous, final byte[] page, int[] offsets){
		offsets = write1Bit(entry.getValue().removed, page, offsets);
		return this.serialize(entry.getKey(), previous, page, offsets);
	}

	/**
	 * Serializes the given int-triple
	 *
	 * @param triple the int-triple to be serialized
	 * @param previous the previous serialized int-triple (such that the given int-triple can be serialized using difference encoding)
	 * @param page the page in which the given int-triple is serialized
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!) and returned back):
	 * 			position 0: the offset in the page of the bit vector in case the second bit must be stored in another byte
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit
	 * @return the updated offsets-array
	 */
	public final int[] serialize(final int[] triple, final int[] previous, final byte[] page, int[] offsets){
		if(triple[this.pos0]==previous[this.pos0]){
			if(triple[this.pos1]==previous[this.pos1]){
				final int diff = triple[this.pos2]-previous[this.pos2];
				offsets = write2Bits(4, page, offsets);
				offsets = write2Bits(StoreIntTriple.length(diff), page, offsets);
				offsets[0] = StoreIntTriple.writeInt(diff, page, offsets[0]);
				return offsets;
			} else {
				final int diff = triple[this.pos1]-previous[this.pos1];
				offsets = write2Bits(2, page, offsets);
				offsets = write2Bits(StoreIntTriple.length(diff), page, offsets);
				offsets = write2Bits(StoreIntTriple.length(triple[this.pos2]), page, offsets);
				offsets[0] = StoreIntTriple.writeInt(diff, page, offsets[0]);
				offsets[0] = StoreIntTriple.writeInt(triple[this.pos2], page, offsets[0]);
				return offsets;
			}
		} else {
			final int diff = triple[this.pos0]-previous[this.pos0];
			offsets = write1Bit(false, page, offsets);
			offsets = write2Bits(StoreIntTriple.length(diff), page, offsets);
			offsets = write2Bits(StoreIntTriple.length(triple[this.pos1]), page, offsets);
			offsets = write2Bits(StoreIntTriple.length(triple[this.pos2]), page, offsets);
			offsets[0] = StoreIntTriple.writeInt(diff, page, offsets[0]);
			offsets[0] = StoreIntTriple.writeInt(triple[this.pos1], page, offsets[0]);
			offsets[0] = StoreIntTriple.writeInt(triple[this.pos2], page, offsets[0]);
			return offsets;
		}
	}

	/**
	 * reads one bit
	 *
	 * @param page the current page
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!)):
	 * 			position 0: the offset in the page of the bit vector in case the  bit must be read from the next byte
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit to be read
	 * @return true if the bit is set and otherwise false...
	 */
	public final static boolean read1Bit(final byte[] page, final int[] offsets){
		if(offsets[2]>=8){
			offsets[1] = offsets[0];
			offsets[0]++;
			offsets[2]=0;
		}
		final boolean result = (page[offsets[1]] & (1 << offsets[2])) != 0;
		offsets[2]++;
		return result;
	}

	/**
	 * reads two bits and returns the length of the next integer to be read according to these two bits
	 *
	 * @param page the current page
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!)):
	 * 			position 0: the offset in the page of the bit vector in case the bit must be read from the next byte
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit to be read
	 * @return the length of the next integer to be read
	 */
	public final static int readLengthOfInteger(final byte[] page, final int[] offsets){
		final boolean bit0 = StoreIntTriple.read1Bit(page, offsets); // side-effect: offsets is modified within the call!
		final boolean bit1 = StoreIntTriple.read1Bit(page, offsets); // side-effect: offsets is modified within the call!
		if(bit0){
			if(bit1){
				return 4;
			} else {
				return 2;
			}
		} else {
			if(bit1){
				return 3;
			} else {
				return 1;
			}
		}
	}

	/**
	 * reads one integer of given length
	 *
	 * @param page the page from which the integer is deserialized
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!)):
	 * 			position 0: the offset in the page of the integer to be read
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit to be read
	 * @param length the length of the integer to be serialized (1 to 4 bytes)
	 * @return the deserialized integer
	 */
	public final static int readInteger(final byte[] page, final int[] offsets, final int length){
		int result = 0;
		int lengthi = length - 1;
		while(lengthi>=0){
			result <<= 8;
			result |= (0xFF & page[offsets[0]+lengthi]);
			lengthi--;
		}
		offsets[0] += length;
		return result;
	}

	/**
	 * Deserializes an entry (consisiting of int-triple and marker for insertion/deletion)
	 *
	 * @param page the page from which is deserialized
	 * @param previous the previous deserialized int-triple (such that difference encoding can be used)
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!)):
	 * 			position 0: the offset in the page of the entry to be deserialized
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit to be read
	 * @return the deserialized entry
	 */
	public final Entry<int[], Container<int[]>> deserializeEntry(final byte[] page, final int[] previous, final int[] offsets){
		final boolean removed = read1Bit(page, offsets); // side-effect: offsets is modified within the call!
		final int[] triple = this.deserializeTriple(page, previous, offsets); // side-effect: offsets is modified within the call!
		return new MapEntry<int[], Container<int[]>>(triple, new Container<int[]>(triple, removed));
	}

	/**
	 * Deserializes an int-triple
	 *
	 * @param page the page from which is deserialized
	 * @param previous the previous deserialized int-triple (such that difference encoding can be used)
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!)):
	 * 			position 0: the offset in the page of the int-triple to be deserialized
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit to be read
	 * @return the deserialized int-triple
	 */
	public final int[] deserializeTriple(final byte[] page, final int[] previous, final int[] offsets){
		final int[] triple = new int[3];
		final boolean bit0 = StoreIntTriple.read1Bit(page, offsets); // side-effect: offsets is modified within the call!
		if(bit0){
			final boolean bit1 = StoreIntTriple.read1Bit(page, offsets); // side-effect: offsets is modified within the call!
			if(bit1){
				final int length2 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
				triple[this.pos0] = previous[this.pos0]; // side-effect: offsets is modified within the call!
				triple[this.pos1] = previous[this.pos1]; // side-effect: offsets is modified within the call!
				triple[this.pos2] = readInteger(page, offsets, length2) + previous[this.pos2]; // side-effect: offsets is modified within the call!
			} else {
				final int length1 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
				final int length2 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
				triple[this.pos0] = previous[this.pos0]; // side-effect: offsets is modified within the call!
				triple[this.pos1] = readInteger(page, offsets, length1) + previous[this.pos1]; // side-effect: offsets is modified within the call!
				triple[this.pos2] = readInteger(page, offsets, length2); // side-effect: offsets is modified within the call!
			}
		} else {
			final int length0 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
			final int length1 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
			final int length2 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
			triple[this.pos0] = readInteger(page, offsets, length0) + previous[this.pos0]; // side-effect: offsets is modified within the call!
			triple[this.pos1] = readInteger(page, offsets, length1); // side-effect: offsets is modified within the call!
			triple[this.pos2] = readInteger(page, offsets, length2); // side-effect: offsets is modified within the call!
		}
		return triple;
	}

	/**
	 * Deserializes the first entry
	 *
	 * @param page the page from which is deserialized
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!)):
	 * 			position 0: the offset in the page of the entry to be deserialized
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit to be read
	 * @return the deserialized entry
	 */
	public final Entry<int[], Container<int[]>> deserializeEntry(final byte[] page, final int[] offsets){
		final boolean removed = read1Bit(page, offsets); // side-effect: offsets is modified within the call!
		final int[] triple = this.deserializeTriple(page, offsets); // side-effect: offsets is modified within the call!
		return new MapEntry<int[], Container<int[]>>(triple, new Container<int[]>(triple, removed));
	}

	/**
	 * Deserializes the first int-triple
	 *
	 * @param page the page from which is deserialized
	 * @param offsets int array (Because of performance reasons this int-array is modified during the call (be aware of side effects, i.e. clone offsets if you need the old one before calling this method!)):
	 * 			position 0: the offset in the page of the int-triple to be deserialized
	 * 			position 1: the offset in the page of the current bit vector
	 * 			position 2: the current bit to be read
	 * @return the deserialized int-triple
	 */
	public final int[] deserializeTriple(final byte[] page, final int[] offsets){
		final int[] triple = new int[3];
		final int length0 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
		final int length1 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
		final int length2 = StoreIntTriple.readLengthOfInteger(page, offsets); // side-effect: offsets is modified within the call!
		triple[this.pos0] = readInteger(page, offsets, length0); // side-effect: offsets is modified within the call!
		triple[this.pos1] = readInteger(page, offsets, length1); // side-effect: offsets is modified within the call!
		triple[this.pos2] = readInteger(page, offsets, length2); // side-effect: offsets is modified within the call!
		return triple;
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
	public Tuple<Integer, Entry<int[], Container<int[]>>> store(Entry<int[], Container<int[]>> entryToBeStored, final Iterator<Entry<int[], Container<int[]>>> it, final int maxNumberOfEntriesToStore, final byte[] page, final int offset) throws IOException {
		Entry<int[], Container<int[]>> previous = null;
		boolean first = true;
		// in array position 0 is current offset, then follows the offset of the current bitvector and in the third position is the current bit in the bitvector!
		int[] offsets = new int[]{offset, offset, 7};
		for (int i = 0; i < maxNumberOfEntriesToStore; i++) {
			if (entryToBeStored != null || it.hasNext()) {
				final Entry<int[], Container<int[]>> entry;
				if (entryToBeStored != null) {
					entry = entryToBeStored;
					entryToBeStored = null;
				} else {
					entry = it.next();
				}
				// first entry uncompressed
				if(first) {
					final int length = StoreIntTriple.length(entry.getKey());
					if (offsets[0] + length >= page.length) {
						return new Tuple<Integer, Entry<int[], Container<int[]>>>(offsets[0], entry);
					}
					offsets[0] = this.serialize(entry, page, offsets[0]);
					first = false;
				} else {
					// next entries compressed
					if (previous != null) {
						final int length = this.length(entry.getKey(), previous.getKey(), offsets[2]);
						if ((offsets[0] + length) >= page.length) {
							return new Tuple<Integer, Entry<int[], Container<int[]>>>(offsets[0], entry);
						}
						offsets = this.serialize(entry, previous.getKey(), page, offsets);
					}
				}
				previous = entry;
			} else {
				break;
			}
		}
		return new Tuple<Integer, Entry<int[], Container<int[]>>>(offsets[0], null);
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
	public Triple<Integer, Entry<int[], Integer>, Object> storeSummary(final Entry<int[], Integer> entryToBeStored, final int[] previouslyStoredKey, final Object storageInfo, final int maxNumberOfEntriesToStore, final byte[] page, final int offset) throws IOException {
		// in array position 0 is current offset, then follows the offset of the current bitvector and in the third position is the current bit in the bitvector!
		int[] offsets = (int[]) storageInfo;
		if (entryToBeStored != null) {
			// first entry stored with pagenumber
			if(offsets==null){
				offsets = new int[]{offset, offset, 6};
				offsets[0] = this.serialize(entryToBeStored.getKey(), page, offsets[0]);
				final int value = entryToBeStored.getValue();
				offsets = StoreIntTriple.write2Bits(StoreIntTriple.length(value), page, offsets);
				offsets[0] = StoreIntTriple.writeInt(value, page, offsets[0]);
			} else {
				final int length = this.length(entryToBeStored.getKey(), previouslyStoredKey, offsets[2]);
				if (offsets[0] + length >= page.length) {
					return new Triple<Integer, Entry<int[], Integer>, Object>(offsets[0], entryToBeStored, null);
				}
				offsets = this.serialize(entryToBeStored.getKey(), previouslyStoredKey, page, offsets);
			}
		}
		return new Triple<Integer, Entry<int[], Integer>, Object>(offsets[0], null, offsets);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Deserializes the key-value-pairs
	 *
	 * @see Registration
	 */
	@Override
	public IKeyValueIterator<Integer, Entry<int[], Container<int[]>>> getNextEntries(final int maxNumberOfCompressedEntries, final byte[] page, final int offset, final int maxBytesOnPage) throws ClassNotFoundException, IOException, URISyntaxException {

		return new IKeyValueIterator<Integer, Entry<int[], Container<int[]>>>() {
			protected boolean flag = true;
			protected int counter = 0;
			protected int[] previousValue;
			protected int[] offsets = new int[]{offset+1, offset, 0};

			@Override
			public boolean hasNext() {
				return this.flag;
			}

			@Override
			public Tuple<Integer, Entry<int[], Container<int[]>>> next(final Tuple<Integer, Entry<int[], Container<int[]>>> resultObject) {
				if (!this.flag) {
					return null;
				}
				Entry<int[], Container<int[]>> result;
				if (this.counter == 0) {
					result = StoreIntTriple.this.deserializeEntry(page, this.offsets); // side-effect: offsets is modified within the call!
				} else {
					result = StoreIntTriple.this.deserializeEntry(page, this.previousValue, this.offsets); // side-effect: offsets is modified within the call!
				}
				this.previousValue = result.getKey();
				if (this.offsets[0] >= maxBytesOnPage) {
					this.flag = false;
				}
				this.counter++;
				if (this.counter == maxNumberOfCompressedEntries) {
					this.flag = false;
				}
				resultObject.setFirst(this.offsets[0]);
				resultObject.setSecond(result);
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
	public IKeyValueIterator<Integer, Entry<int[], Integer>> getNextSumEntries(final int maxNumberOfCompressedEntries, final byte[] page, final int offset, final int maxBytesOnPage) throws ClassNotFoundException, IOException, URISyntaxException {

		return new IKeyValueIterator<Integer, Entry<int[], Integer>>() {

			protected boolean flag = true;
			protected int counter = 0;
			protected int[] offsets = null;
			protected int pagenumber = 0;
			protected int[] previousKey = null;

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
				// only first key stored with pagenumber
				if (this.offsets == null) {
					this.offsets = new int[]{offset+1, offset, 0};
					final int[] key = StoreIntTriple.this.deserializeTriple(page, this.offsets);
					this.previousKey = key;
					final int length = StoreIntTriple.readLengthOfInteger(page, this.offsets);
					this.pagenumber = StoreIntTriple.readInteger(page, this.offsets, length);
					entry = new MapEntry<int[], Integer>(key, this.pagenumber);
					this.pagenumber++;
				} else {
					final int[] key = StoreIntTriple.this.deserializeTriple(page, this.previousKey, this.offsets);
					this.previousKey = key;
					entry = new MapEntry<int[], Integer>(key, this.pagenumber);
					this.pagenumber++;
				}
				if (this.offsets[0] >= maxBytesOnPage) {
					this.flag = false;
				}
				this.counter++;
				resultObject.setFirst(this.offsets[0]);
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

	/**
	 * This class is for printing int-triples (and in general int-arrays) keys and values
	 */
	public static class IntArrayPrinter implements IKeyValuePrinter<int[], int[]>{
		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public String toStringKey(final int[] k){
			return Arrays.toString(k);
		}

		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public String toStringValue(final int[] v){
			return Arrays.toString(v);
		}
	}

	/**
	 * This class is for comparing two int-triples according to a given collation order.
	 * It can also server as prefix comparator to compare prefix keys with other keys.
	 * Prefix keys must have a int value <0 for triple positions not to be compared...
	 */
	public static class IntTripleComparator implements Comparator<int[]>{

		/**
		 * the order according to to which the int-triples are compared
		 */
		protected final CollationOrder collationOrder;

		/**
		 * Constructor
		 *
		 * @param collationOrder the order according to to which the int-triples are compared
		 */
		public IntTripleComparator(final CollationOrder collationOrder){
			this.collationOrder = collationOrder;
		}

		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public int compare(final int[] o1, final int[] o2) {
			for(int i=0; i<3; i++){
				final int pos = this.collationOrder.getSortCriterium(i);
				if(o1[pos]<0 || o2[pos]<0){ // in case of prefix keys...
					break;
				}
				final int diff = o1[pos]-o2[pos];
				if(diff!=0){
					return diff;
				}
			}
			return 0;
		}

		private static boolean alreadyRegistered = false;

		public static void register(){
			if(IntTripleComparator.alreadyRegistered){
				return;
			}
			IntTripleComparator.alreadyRegistered = true;
			COMPARATOR.registerDeSerializer(new COMPARATOR.ComparatorDeSerializer<int[]>(){

				@Override
				public int length(final Comparator<int[]> t) {
					return LengthHelper.lengthLuposByte();
				}

				@Override
				public void serialize(final Comparator<int[]> t, final OutputStream out) throws IOException {
					final IntTripleComparator itc = (IntTripleComparator) t;
					OutHelper.writeLuposByte((byte) itc.collationOrder.ordinal(), out);
				}

				@Override
				public IntTripleComparator deserialize(final InputStream in) throws IOException, URISyntaxException, ClassNotFoundException {
					final byte order = InputHelper.readLuposByte(in);
					return new IntTripleComparator(CollationOrder.values()[order]);
				}

				@SuppressWarnings("unchecked")
				@Override
				public Class<? extends IntTripleComparator>[] getRegisteredClasses() {
					return new Class[] { IntTripleComparator.class };
				}
			});
		}

		static {
			register();
		}
	}

	/**
	 * Just for testing purposes...
	 *
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(final String[] args) throws ClassNotFoundException, IOException, URISyntaxException{
		// this method is for testing purposes
		final ILevelFactory<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>> testfactory = new DiskLevelFactory<int[], int[]>(new IntTripleComparator(CollationOrder.SPO), new StoreIntTriple(CollationOrder.SPO), 5, 5);
		final LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>> testtree=new LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>(testfactory);
		for(int s=0; s<10; s++){
			for(int p=0; p<100; p++){
				for(int o=0; o<100; o++){
					final int[] triple = new int[]{s, p, o};
					testtree.put(triple, triple);
				}
			}
		}
		testtree.printLevels(new IntArrayPrinter());
		System.out.println(Arrays.toString(testtree.get(new int[]{7, 5, 2})));
		System.out.println("Prefix Search Result:");
		final ISIPIterator<int[], int[]> it = testtree.prefixSearch(new IntTripleComparator(CollationOrder.SPO), new int[]{2, 8, -1});
		while(it.hasNext()){
			System.out.println(Arrays.toString(it.next().getKey()));
		}
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
		OutHelper.writeLuposByte((byte) this.collationorder.ordinal(), loos);
	}

	public static StoreIntTriple readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		return new StoreIntTriple(CollationOrder.values()[InputHelper.readLuposByte(lois)]);
	}
}
