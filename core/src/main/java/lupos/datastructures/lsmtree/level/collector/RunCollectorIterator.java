package lupos.datastructures.lsmtree.level.collector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;

public class RunCollectorIterator<K,V> extends RunCollector<K,V,Iterator<Map.Entry<K,Container<V>>>> {

	public RunCollectorIterator(final ILevelFactory<K, V, Iterator<Entry<K, Container<V>>>> levelFactory, final int level, final int k, final Comparator<K> comp, final long maximumRunLength) {
		super(levelFactory, level, k, comp, maximumRunLength);
	}

	/**
	 * {@inheritDoc}
	 *
	 * returns a MergeIterator to merge the entries of all runs with merge sort into one new run
	 *
	 * @see MergeIterator<K,V>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Map.Entry<K, Container<V>>> rollOut() throws ClassNotFoundException, IOException, URISyntaxException {
		final Iterator<Map.Entry<K,Container<V>>>[] iterators = new Iterator[this.runs.length];
		for(int i=0;i<this.counter;i++){
			iterators[i] = this.runs[i].rollOut();
		}

		return new MergeIterator<K, Container<V>>(iterators, this.comp);
	}

	@Override
	public void addRun(final Generator<K, V> generator) throws ClassNotFoundException, IOException, URISyntaxException {
		if(this.maximumRunLength < generator.size()){
			if(this.nextLevel==null){
				this.nextLevel = this.levelFactory.createLevel(this.level+1);
			}
			this.nextLevel.addRun(generator);
		} else {
			final Iterator<Entry<K, Container<V>>> iterator = new Iterator<Entry<K, Container<V>>>(){

				private final Iterator<Entry<K, V>> iterator = generator.iterator();
				@Override
				public boolean hasNext() {
					return this.iterator.hasNext();
				}

				@Override
				public Entry<K, Container<V>> next() {
					final Entry<K, V> next = this.iterator.next();
					if(next==null){
						return null;
					}
					return new MapEntry<K, Container<V>>(next.getKey(), new Container<V>(next.getValue(), false));
				}
			};
			this.receiveRunFromLowerLevel(iterator);
		}
	}
}