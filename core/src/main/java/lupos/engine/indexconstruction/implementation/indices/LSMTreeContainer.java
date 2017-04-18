package lupos.engine.indexconstruction.implementation.indices;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

import lupos.datastructures.lsmtree.LSMTree;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.misc.Tuple;

public class LSMTreeContainer<K extends Serializable, V extends Serializable, R> implements IIndexContainer<K, V> {

	private final LSMTree<K, V, R> index;
	private int size = 0;

	public LSMTreeContainer(final LSMTree<K, V, R> index){
		this.index = index;
	}

	@Override
	public final SIPParallelIterator<Entry<K, V>, K> iterator() {
		return this.index.iterator();
	}

	@Override
	public final void put(final K key, final V value) throws Exception {
		this.index.put(key, value);
		this.size++;
	}

	@Override
	public V get(final K key) throws Exception {
		return this.index.get(key);
	}

	@Override
	public final int size() {
		return this.size;
	}

	@Override
	public void writeHeader(final OutputStream loos) throws Exception {
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws Exception {
		this.index.writeLuposObject(loos);
	}

	@Override
	public IIndexContainer<int[], int[]> createHistogramIndex(final CollationOrder collationOrder) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean createsHistogramIndex() {
		return false;
	}

	@Override
	public void logProperties(final List<Tuple<String, Long>> times){
		final long start = System.currentTimeMillis();
		times.add(new Tuple<String, Long>("No time: "+this.index.getName()+": Number of bytes stored on disk (with wasted bytes to complete page)", this.index.numberOfBytesOnDisk()));
		try {
			times.add(new Tuple<String, Long>("No time: "+this.index.getName()+": Number of used bytes stored on disk (without wasted bytes to complete page)", this.index.numberOfUsedBytesOnDisk()));
			times.add(new Tuple<String, Long>("No time: "+this.index.getName()+": Structure Information: "+this.index.getStructureInfo(), -1L));
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		times.add(new Tuple<String, Long>("Used time to log properties of LSM Tree "+this.index.getName(), System.currentTimeMillis()-start));
	}

	@Override
	public void print() {
		this.index.printLevels();
	}
}
