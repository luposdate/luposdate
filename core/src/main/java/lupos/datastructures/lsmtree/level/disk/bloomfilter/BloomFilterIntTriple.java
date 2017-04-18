package lupos.datastructures.lsmtree.level.disk.bloomfilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.misc.BitVector;
/**
* Bloomfilter used to speed up finding keys in Run
*
* @see BitVector
* @author Maike Herting
*
*/
public class BloomFilterIntTriple implements IBloomFilter<int[]>{

	/**
	* The Bitvector used for the Bloomfilter (considering all components of the int-triples)
	*/
	protected final BitVector bitvectorAll;

	/**
	* The Bitvector used for the Bloomfilter (considering the first component of the int-triples according to the given collation order)
	*/
	protected final BitVector bitvectorFirst;

	/**
	* The Bitvector used for the Bloomfilter (considering the first and second components of the int-triples according to the given collation order)
	*/
	protected final BitVector bitvectorFirstSecond;

	/**
	 * the collation order of the triples according to which the bloom filter is created
	 */
	protected final CollationOrder collationOrder;

	/**
	 * the first sort criterion according to the given collation order
	 */
	protected final int pos0;

	/**
	 * the second sort criterion according to the given collation order
	 */
	protected final int pos1;

	/**
	 * the third sort criterion according to the given collation order
	 */
	protected final int pos2;

	/**
	 * the size of the bit vector
	 */
	protected final int size;

	/**
	 * Constructor
	 *
	 * @param collationOrder the collation order of the triples according to which the bloom filter is created
	 */
	public BloomFilterIntTriple(final CollationOrder collationOrder, final long maximumRunLength){
		this.collationOrder = collationOrder;
		this.pos0 = this.collationOrder.getSortCriterium(0);
		this.pos1 = this.collationOrder.getSortCriterium(1);
		this.pos2 = this.collationOrder.getSortCriterium(2);
		this.size = (int) Math.min(8*maximumRunLength, BloomFilter.MAXNUMBEROFBITSFORBLOOMFILTER);
		this.bitvectorAll = new BitVector(this.size);
		this.bitvectorFirst = new BitVector(this.size);
		this.bitvectorFirstSecond = new BitVector(this.size);
	}

	/**
	 *{@inheritDoc}
	 *
	 * Sets hashcode of key in bitvector
	 *
	 */
	@Override
	public void set(final int[] k){
		this.bitvectorAll.set(Math.abs(BloomFilterIntTriple.getHashCode(k) % this.size));
		this.bitvectorFirst.set(Math.abs(k[this.pos0] % this.size));
		this.bitvectorFirstSecond.set(Math.abs((k[this.pos0]+k[this.pos1]) % this.size));
	}

	/**
	 *{@inheritDoc}
	 *
	 *returns true if key is set in bitvector
	 *
	 */
	@Override
	public boolean get(final int[] k){
		return this.bitvectorAll.get(Math.abs(BloomFilterIntTriple.getHashCode(k) % this.size));
	}

	/**
	 * Calculates and returns the hash code of the given int-triple
	 *
	 * @param k the int-triple the hash code of which is to be determined
	 * @return hash code of the given int-triple
	 */
	public final static int getHashCode(final int[] k){
		return k[0]+k[1]+k[2];
	}

	@Override
	public boolean getPrefix(final int[] prefixKey) {
		if(prefixKey==null){
			return true;
		}
		if(prefixKey[this.pos0]<0){
			// the first position is not set in the prefix key, i.e., all triples match to the prefix key...
			return true;
		}
		if(prefixKey[this.pos1]<0){
			// only first position is fixed in the prefix key...
			return this.bitvectorFirst.get(Math.abs(prefixKey[this.pos0] % this.size));
		}
		if(prefixKey[this.pos2]<0){
			// first and second positions are fixed in the prefix key...
			return this.bitvectorFirstSecond.get(Math.abs((prefixKey[this.pos0] + prefixKey[this.pos1]) % this.size));
		}
		// all positions in the prefix key are fixed: Hence do a "normal" lookup in the bloom filter
		return this.get(prefixKey);
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
		this.bitvectorAll.writeWithoutSize(loos);
		this.bitvectorFirst.writeWithoutSize(loos);
		this.bitvectorFirstSecond.writeWithoutSize(loos);
	}

	@Override
	public void readLuposObject(final InputStream lois) throws IOException {
		this.bitvectorAll.readWithoutSize(lois, this.size);
		this.bitvectorFirst.readWithoutSize(lois, this.size);
		this.bitvectorFirstSecond.readWithoutSize(lois, this.size);
	}
}
