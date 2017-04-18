package lupos.engine.indexconstruction.implementation.indices;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.IntArrayDBBPTreeStatistics;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.indexconstruction.implementation.dbbptree.IntArrayDBBPTreeIndicesGenerator;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.io.helper.OutHelper;

public class DBBPTreeContainer<K extends Serializable, V extends Serializable> implements IIndexContainer<K, V> {

	private final lupos.datastructures.paged_dbbptree.DBBPTree<K, V> index;

	public DBBPTreeContainer(final lupos.datastructures.paged_dbbptree.DBBPTree<K, V> dictionary){
		this.index = dictionary;
	}

	@Override
	public final SIPParallelIterator<Entry<K, V>, K> iterator() {
		return this.index.iterator();
	}

	@Override
	public final void put(final K key, final V value) throws Exception {
		this.index.put(key, value);
	}

	@Override
	public V get(final K key) throws Exception {
		return this.index.get(key);
	}

	@Override
	public final int size() {
		return this.index.size();
	}

	@Override
	public void writeHeader(final OutputStream loos) throws Exception {
		OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), loos);
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws Exception {
		this.index.writeLuposObject(loos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IIndexContainer<int[], int[]> createHistogramIndex(final CollationOrder collationOrder) throws Exception {
		final IntArrayDBBPTreeStatistics histogramIndex = IntArrayDBBPTreeIndicesGenerator.generateHistogramIndex((Comparator<int[]>)this.index.comparator(), collationOrder, (DBBPTree<int[], int[]>) this.index);
		return new DBBPTreeContainer<int[], int[]>(histogramIndex);
	}

	@Override
	public boolean createsHistogramIndex() {
		return true;
	}

	@Override
	public void print() {
		final SIPParallelIterator<Entry<K, V>, K> iterator = this.index.iterator();
		while(iterator.hasNext()){
			System.out.println(iterator.next());
		}
		iterator.close();
	}
}
