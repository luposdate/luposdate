package lupos.datastructures.lsmtree.level.memory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.lsmtree.debug.IKeyValuePrinter;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.ILevel;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.lsmtree.sip.ISIPIterator;
import lupos.datastructures.lsmtree.sip.MergeSIPIterator;
import lupos.io.helper.InputHelper;

public abstract class MemoryLevelIterator<K,V> extends MemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>> implements IMemoryLevelIterator<K,V> {

	public MemoryLevelIterator(final ILevelFactory<K, V, Iterator<Entry<K, Container<V>>>> levelFactory, final int level, final int THRESHOLD) {
		super(levelFactory, level, THRESHOLD);
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void printLevels(final IKeyValuePrinter<K, V> printer){
		System.out.println("Level: "+this.level);
		boolean first = true;
		try {
			final Iterator<Map.Entry<K, Container<V>>> iterator = this.rollOut();
			while(iterator.hasNext()){
				final Entry<K, Container<V>> entry = iterator.next();
				if(first){
					first = false;
				} else {
					System.out.print(", ");
				}
				System.out.print(printer.toStringKey(entry.getKey()) + " : " +
						"("+(entry.getValue().value==null?null:printer.toStringValue(entry.getValue().value))+", "+
						(entry.getValue().isDeleted()?"removed":"inserted")+")");
			}
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		System.out.println(" # = " + this.size());
		if(this.nextLevel!=null){
			this.nextLevel.printLevels(printer);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public ISIPIterator<K, Container<V>> prefixSearch(final Comparator<K> prefixComparator, final K prefixkey) {
		try {
			final Iterator<Entry<K, Container<V>>> originaliterator = this.rollOut();
			final ISIPIterator<K, Container<V>> iterator = new ISIPIterator<K, Container<V>>(){

				Entry<K, Container<V>> currentEntry = null;

				@Override
				public boolean hasNext() {
					if(this.currentEntry!=null){
						return true;
					}
					this.currentEntry = this.next();
					return (this.currentEntry!=null);
				}

				@Override
				public Entry<K, Container<V>> next() {
					if(this.currentEntry!=null){
						final Entry<K, Container<V>> result = this.currentEntry;
						this.currentEntry = null;
						return result;
					}
					Entry<K, Container<V>> next;
					do {
						if(!originaliterator.hasNext()){
							return null;
						}
						next = originaliterator.next();
					} while(prefixComparator.compare(next.getKey(), prefixkey)<0);
					if(prefixComparator.compare(next.getKey(), prefixkey)>0){
						return null;
					}
					return next;
				}

				@Override
				public Entry<K, Container<V>> next(final K k) {
					Entry<K, Container<V>> next = this.next();
					while(next!=null && prefixComparator.compare(next.getKey(), k)<0){
						next = this.next();
					}
					return next;
				}
			};
			if(this.nextLevel==null){
				return iterator;
			} else {
				return new MergeSIPIterator<K, Container<V>>(this.levelFactory.getComparator(), iterator, this.nextLevel.prefixSearch(prefixComparator, prefixkey));
			}
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return new ISIPIterator<K, Container<V>>(){
				@Override
				public boolean hasNext() {
					return false;
				}
				@Override
				public Entry<K, Container<V>> next() {
					return null;
				}
				@Override
				public Entry<K, Container<V>> next(final K k) {
					return null;
				}
			};
		}
	}
	@Override
	public void readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		final byte empty = InputHelper.readLuposByte(lois);
		if(empty!=0){
			final ILevel<K,V,Iterator<Map.Entry<K,Container<V>>>> run = this.levelFactory.createRun(this.level, 0);
			run.readLuposObject(lois);
			final Iterator<Entry<K, Container<V>>> iterator = run.rollOut();
			while(iterator.hasNext()){
				final Entry<K, Container<V>> entry = iterator.next();
				this.put(entry.getKey(), entry.getValue());
			}
		}
		final byte end = InputHelper.readLuposByte(lois);
		if(end==1){
			if(this.nextLevel==null){
				this.nextLevel = this.levelFactory.createLevel(this.level+1);
			}
			this.nextLevel.readLuposObject(lois);
		}
	}
}
