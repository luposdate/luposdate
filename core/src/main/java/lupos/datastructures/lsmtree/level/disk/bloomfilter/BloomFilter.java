package lupos.datastructures.lsmtree.level.disk.bloomfilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lupos.misc.BitVector;
/**
* Bloomfilter used to speed up finding keys in Run
*
* @see BitVector
* @author Maike Herting
*
*/

public class BloomFilter<K> implements IBloomFilter<K>{

	/**
	* The size of the Bloomfilter for the bitvector
	*/
	public static final int MAXNUMBEROFBITSFORBLOOMFILTER = 4882 * 1024;

	/**
	* The bit vector used for the Bloomfilter
	*/
	protected final BitVector bitvector;

	/**
	 * the size of the bit vector
	 */
	protected final int size;

	public BloomFilter(final long maximumRunLength){
		this.size = (int) Math.min(8*maximumRunLength, MAXNUMBEROFBITSFORBLOOMFILTER);
		this.bitvector = new BitVector(this.size);
	}

	/**
	 *{@inheritDoc}
	 *
	 * Sets hashcode of key in bitvector
	 *
	 */
	@Override
	public void set(final K k){
		this.bitvector.set(Math.abs(k.hashCode() % this.size));
	}

	/**
	 *{@inheritDoc}
	 *
	 *returns true if key is set in bitvector
	 *
	 */
	@Override
	public boolean get(final K k){
		return this.bitvector.get(Math.abs(k.hashCode() % this.size));
	}

	/**
	 *{@inheritDoc}
	 *
	 * This method is not implemented for general keys:
	 * It throws an UnsupportedOperationException (except for the null prefix key, where always true is returned).
	 *
	 * For specific keys, you should override this method...
	 *
	 * @throws UnsupportedOperationException
	 */
	@Override
	public boolean getPrefix(final K prefixKey) {
		if(prefixKey==null){
			return true;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
		this.bitvector.writeWithoutSize(loos);
	}

	@Override
	public void readLuposObject(final InputStream lois) throws IOException {
		this.bitvector.readWithoutSize(lois, this.size);
	}
}
