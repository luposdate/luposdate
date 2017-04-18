package lupos.datastructures.lsmtree.level.memory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.misc.util.ImmutableIterator;

/**
 * A memory level implementation that stores key-value-pairs in memory
 * and transfers its entries if the memory is full to the next level of the LSM-Tree.
 */
public class MemoryLevelLazySorting<K,V> extends MemoryLevelIterator<K, V> implements IMemoryLevelIterator<K ,V>, IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>> {

	/**
	 * length of start sequences sorted with insertion sort
	 */
	public static int M = 25;

	/**
	 * the number of threads of the threads pool for sorting
	 */
	protected final static int NUMBEROFTHREADS = 8;

	/**
	 * the threads pool for the sorting threads
	 */
	protected Semaphore threadsPoolRemaining = new Semaphore(NUMBEROFTHREADS);;

	/**
	 * for storing the keys...
	 */
	protected final K[] keys;
	/**
	 * for storing the values...
	 */
	protected final Container<V>[] values;

	/**
	 * the number of inserted key-value-pairs
	 */
	protected int size;

	/**
	 * the number of already sorted key-value-pairs
	 */
	protected int alreadySorted;

	/**
	 * the comparator used to compare the keys
	 */
	protected final Comparator<K> comparator;

	/**
	 * Constructor sets parameters
	 *
	 * @param levelFactory a level factory which creates level
	 * @param level number of the level
	 * @param THRESHOLD the maximum number of entries to be stored in the memory
	 * @param comp a comparator that is used to compare keys
	 */
	@SuppressWarnings("unchecked")
	public MemoryLevelLazySorting(final ILevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> levelFactory, final int level, final int THRESHOLD, final Comparator<K> comp){
		super(levelFactory, level, THRESHOLD);
		this.comparator = comp;
		this.keys = (K[]) new Object[THRESHOLD];
		this.values = new Container[THRESHOLD];
		this.size = 0;
		this.alreadySorted = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean putIntoMemory(final K key, final Container<V> value) {
		this.keys[this.size] = key;
		this.values[this.size] = value;
		this.size++;
		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * if key can't be found in the memory, the next level is searched for it on condition that it exists
	 */
	@Override
	public Container<V> get(final K key) throws ClassNotFoundException, IOException, URISyntaxException {
		// only sort if there are at least M unsorted entries
		if(this.size-this.alreadySorted>MemoryLevelLazySorting.M){
			this.sort();
		}
		// search in the sorted fragment
		final Container<V> result = this.binarySearch(0, this.alreadySorted-1, key);
		if(result!=null){
			return result;
		}
		// search sequentially in the remaining unsorted fragment
		for(int i=this.alreadySorted; i<this.size; i++){
			if(this.comparator.compare(this.keys[i], key)==0){
				return this.values[i];
			}
		}
		// search in the upper levels
		if(this.nextLevel!=null){
			return this.nextLevel.get(key);
		}
		return null;
	}

	/**
	 * Do a binary search...
	 *
	 * @param start left side of the interval to be searched in
	 * @param end right side of the interval to be searched in
	 * @param key key to be searched for
	 * @return the value of the key or null if the key has not been found
	 */
	private Container<V> binarySearch(final int start, final int end, final K key){
		if(end<start){
			return null;
		}
		if(end==start){
			if(this.comparator.compare(this.keys[start], key)==0){
				return this.values[start];
			}
			return null;
		}
		final int middle = (start + end) / 2;
		final int compare = this.comparator.compare(this.keys[start], key);
		if(compare==0){
			// check if there are younger entries with the same key...
			int index = middle;
			while(index-1>=start && this.comparator.compare(this.keys[index-1], key)==0){
				index--;
			}
			return this.values[index];
		} else if(compare<0){
			return this.binarySearch(middle+1, end, key);
		} else {
			return this.binarySearch(start, middle-1, key);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Map.Entry<K,Container<V>>> rollOut(){
		this.sort();
		return new ImmutableIterator<Map.Entry<K,Container<V>>>(){

			private int index = 0;

			@Override
			public final boolean hasNext() {
				return this.index<MemoryLevelLazySorting.this.size;
			}

			@Override
			public final Entry<K, Container<V>> next() {
				if(this.hasNext()){
					final Entry<K, Container<V>> result = new MapEntry<K, Container<V>>(MemoryLevelLazySorting.this.keys[this.index], MemoryLevelLazySorting.this.values[this.index]);
					this.index++;
					return result;
				}
				return null;
			}
		};
	}

	/**
	 * sorts the entries...
	 */
	private void sort(){
		if(this.alreadySorted!=this.size){
			final Sorter sorter = new Sorter(this.alreadySorted, this.size-this.alreadySorted);
			sorter.run();
			if(this.alreadySorted>0){
				this.merge(0, this.alreadySorted, this.size-this.alreadySorted);
			}
			this.alreadySorted = this.size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		this.size = 0;
		this.alreadySorted = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size() {
		return this.size;
	}

	/**
	 * merges two sorted parts of the keys and values arrays
	 *
	 * @param start0 the start index of the first sorted part
	 * @param length0 the length of the first sorted part
	 * @param length1 the length of the second sorted part (placed directly after the first sorted parted)
	 */
	@SuppressWarnings("unchecked")
	private void merge(final int start0, final int length0, final int length1) {
		final int start1 = start0 + length0;
		final int totallength = length0+length1;
		final K[] s = (K[])new Object[totallength];
		final Container<V>[] sv = new Container[totallength];
		int index_0 = 0;
		int index_1 = 0;
		int index_s = 0;
		while (index_s < s.length) {
			if (index_0 >= length0) {
				// rest from b
				System.arraycopy(this.keys, start1+index_1, s, index_s, length1 - index_1);
				System.arraycopy(this.values, start1+index_1, sv, index_s, length1 - index_1);
				break;
			} else if (index_1 >= length1) {
				// rest from a
				System.arraycopy(this.keys, start0+index_0, s, index_s, length0 - index_0);
				System.arraycopy(this.values, start0+index_0, sv, index_s, length0 - index_0);
				break;
			} else {
				if((this.comparator.compare(MemoryLevelLazySorting.this.keys[start0+index_0], MemoryLevelLazySorting.this.keys[start1+index_1]) <= 0)){
					s[index_s] = this.keys[start0+index_0];
					sv[index_s] = this.values[start0+index_0];
					index_0++;
				} else {
					s[index_s] = this.keys[start1+index_1];
					sv[index_s] = this.values[start1+index_1];
					index_1++;
				}
				index_s++;
			}
		}
		System.arraycopy(s,0,this.keys,start0,totallength);
		System.arraycopy(sv,0,this.values,start0,totallength);
	}

	/**
	 * Sorting thread, which implements a parallel merge sort
	 */
	public class Sorter extends Thread {
		private final int start;
		private final int length;

		public Sorter(final int start, final int length) {
			this.start = start;
			this.length = length;
		}

		@Override
		public void run() {
			if (this.length <= M) {
				this.insertionSort(this.start, this.length);
			} else {
				final int m = this.length / 2;
				final Sorter sorter1 = new Sorter(this.start, m);
				if (MemoryLevelLazySorting.this.threadsPoolRemaining.tryAcquire()) {
					sorter1.start();
				} else {
					sorter1.run();
				}
				final Sorter sorter2 = new Sorter(this.start + m, this.length - m);
				sorter2.run();
				try {
					sorter1.join();
					MemoryLevelLazySorting.this.merge(this.start, m, this.length - m);
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}

		/**
		 * Insertion sort
		 *
		 * @param min index for sorting
		 * @param length the length of the part to be sorted
		 */
		private void insertionSort(final int min, final int length) {
			// insertion sort on the original sequence!
			for (int i = 0 + 1; i < length; ++i) {
				final K tmp2 = MemoryLevelLazySorting.this.keys[i];
				final Container<V> tmp2_v = MemoryLevelLazySorting.this.values[i];
				int j;
				for (j = i - 1; j >= 0
						&& MemoryLevelLazySorting.this.comparator.compare(tmp2, MemoryLevelLazySorting.this.keys[j]) < 0; --j) {
					MemoryLevelLazySorting.this.keys[j + 1] = MemoryLevelLazySorting.this.keys[j];
					MemoryLevelLazySorting.this.values[j + 1] = MemoryLevelLazySorting.this.values[j];
				}
				MemoryLevelLazySorting.this.keys[j + 1] = tmp2;
				MemoryLevelLazySorting.this.values[j + 1] = tmp2_v;
			}
		}
	}
}
