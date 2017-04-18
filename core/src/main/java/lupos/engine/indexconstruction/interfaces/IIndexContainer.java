package lupos.engine.indexconstruction.interfaces;

import java.io.OutputStream;
import java.util.List;

import lupos.datastructures.sorteddata.MapIteratorProvider;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.misc.Tuple;

public interface IIndexContainer<K, V> extends MapIteratorProvider<K, V> {
	public void put(final K key, final V value) throws Exception;
	public V get(final K key) throws Exception;
	public int size();
	public void writeHeader(final OutputStream loos) throws Exception;
	public void writeLuposObject(final OutputStream loos) throws Exception;
	public IIndexContainer<int[], int[]> createHistogramIndex(final CollationOrder collationOrder) throws Exception;
	public boolean createsHistogramIndex();
	public default void logProperties(final List<Tuple<String, Long>> times){
		throw new UnsupportedOperationException();
	};
	/**
	 * only for debug purposes
	 */
	public void print();
}
