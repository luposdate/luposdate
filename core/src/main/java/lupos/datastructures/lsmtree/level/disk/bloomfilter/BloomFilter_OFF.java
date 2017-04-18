package lupos.datastructures.lsmtree.level.disk.bloomfilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class turns off Bloomfilter
 *
 * @author Maike Herting
 *
 */
public class BloomFilter_OFF<K> implements IBloomFilter<K> {

	/**
	 *{@inheritDoc}
	 *
	 * Here this method doesn't do anything
	 */
	@Override
	public void set(final K k) {

	}

	/**
	 *{@inheritDoc}
	 *
	 * Here it always returns true
	 */
	@Override
	public boolean get(final K k) {
		return true;
	}

	/**
	 *{@inheritDoc}
	 *
	 * Here it always returns true
	 */
	@Override
	public boolean getPrefix(final K prefixKey) {
		return true;
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
	}

	@Override
	public void readLuposObject(final InputStream lois) throws IOException {
	}
}
