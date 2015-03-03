
/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.datastructures.dbmergesortedds;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.queryresult.ParallelIterator;
public class DBMergeSortedSet<E extends Serializable> extends
		DBMergeSortedBag<E> implements SortedSet<E>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8310384191213115085L;

	/**
	 * Create a new DBMergeSortedBag that sorts according to the elements'
	 * natural order.
	 *
	 * @param sortConfiguration a {@link lupos.datastructures.dbmergesortedds.SortConfiguration} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedSet(final SortConfiguration sortConfiguration,
			final Class<? extends E> classOfElements) {
		super(sortConfiguration, classOfElements);
	}

	/**
	 * This constructor is just there to make the class serializable and should
	 * not be used in other cases (than the Java serialization)!
	 */
	public DBMergeSortedSet() {
		super(new SortConfiguration(), null);
	}

	/**
	 * Standard constructor
	 *
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedSet(final Class<? extends E> classOfElements){
		this(new SortConfiguration(), classOfElements);
	}

	/**
	 * Create a new DBMergeSortedBag that sorts using the specified Comparator.
	 *
	 * @param comp
	 *            The Comparator to use for sorting.
	 * @param sortConfiguration a {@link lupos.datastructures.dbmergesortedds.SortConfiguration} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedSet(final SortConfiguration sortConfiguration,
			final Comparator<? super E> comp,
			final Class<? extends E> classOfElements) {
		super(sortConfiguration, comp, classOfElements);
	}

	/**
	 * <p>Constructor for DBMergeSortedSet.</p>
	 *
	 * @param comp a {@link java.util.Comparator} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedSet(final Comparator<? super E> comp,
			final Class<? extends E> classOfElements) {
		this(new SortConfiguration(), comp, classOfElements);
	}

	private DBMergeSortedSet(final DBMergeSortedBag<E> bag,
			final Class<? extends E> classOfElements) {
		super(bag.sortConfiguration, bag.comp, classOfElements);
	}

	/**
	 * <p>get.</p>
	 *
	 * @param e a E object.
	 * @return a E object.
	 */
	public E get(final E e) {
		return this.subSet(e, e).last();
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final Object o) {
		final List<Object> list = new LinkedList<Object>();
		list.add(o);
		return this.removeAll(list);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		if (this.currentRun == null) {
			final ParallelIterator<E> it = this.iterator();
			int counter = 0;
			while (it.hasNext()) {
				counter++;
				it.next();
			}
			it.close();
			return counter;
		}
		this.sort();
		return super.size();
	}

	/** {@inheritDoc} */
	@Override
	protected Entry<E> getNext(final Iterator<Entry<E>>[] iters,
			final Map<Integer, Integer> hm, final Heap<Entry<E>> mergeheap) {
		Entry<E> res = mergeheap.pop();
		res.run = (res.run == 0) ? 1 : res.run;
		if (iters[hm.get(res.run)].hasNext()) {
			final Entry<E> e = iters[hm.get(res.run)].next();
			e.runMatters = false;
			mergeheap.add(e);
		}
		// remove duplicates during merging...
		while (mergeheap.peek() != null && res.equals(mergeheap.peek())) {
			res = mergeheap.pop();
			res.run = (res.run == 0) ? 1 : res.run;
			if (iters[hm.get(res.run)].hasNext()) {
				final Entry<E> e = iters[hm.get(res.run)].next();
				e.runMatters = false;
				mergeheap.add(e);
			}
		}
		return res;
	}

	/** {@inheritDoc} */
	@Override
	protected Entry<E> getNext(final Iterator<Entry<E>>[] iters,
			final int basisID, final Heap<Entry<E>> mergeheap) {
		// size++;
		Entry<E> res = mergeheap.pop();
		res.run = (res.run == 0) ? 1 : res.run;
		if (iters[res.run - basisID].hasNext()) {
			final Entry<E> elem = iters[res.run - basisID].next();
			elem.runMatters = false;
			mergeheap.add(elem);
		}
		// remove duplicates during merging...
		while (mergeheap.peek() != null && res.equals(mergeheap.peek())) {
			res = mergeheap.pop();
			res.run = (res.run == 0) ? 1 : res.run;
			if (iters[res.run - basisID].hasNext()) {
				final Entry<E> elem = iters[res.run - basisID].next();
				elem.runMatters = false;
				mergeheap.add(elem);
			}
		}
		return res;
	}

	/** {@inheritDoc} */
	@Override
	protected void addToRun(final Entry<E> e){
		// already eliminate duplicates when adding the entry to the run
		if(this.currentRun.max==null || !this.currentRun.max.equals(e.e)){
			this.currentRun.add(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public DBMergeSortedSet<E> subSet(final E arg0, final E arg1) {
			return new DBMergeSortedSet<E>(super.subBag(arg0, arg1),
					this.classOfElements);
	}

	/** {@inheritDoc} */
	@Override
	public DBMergeSortedSet<E> tailSet(final E arg0) {
			return new DBMergeSortedSet<E>(super.tailBag(arg0), this.classOfElements);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final Iterator<E> iter = this.iterator();
		String result = "[";
		while (iter.hasNext()) {
			result += iter.next();
			if (iter.hasNext()) {
				result += ", ";
			}
		}
		result += "]";
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public DBMergeSortedSet<E> headSet(final E toElement) {
			return new DBMergeSortedSet<E>(super.headBag(toElement),
					this.classOfElements);
	}

	/** {@inheritDoc} */
	@Override
	public ParallelIterator<E> iterator() {
		// Do we have a small sorted bag? In other words:
		// Did we already write entries to disk or is all still stored in main
		// memory? In the latter case, we do not need to store it on disk and
		// just "sort" in memory!
		if (this.currentRun == null) {
			final ToSort<Entry<E>> zheap = ToSort.cloneInstance(this.tosort);
			return new ParallelIterator<E>() {
				Iterator<Entry<E>> it = zheap.emptyDatastructure();
				E next = (this.it.hasNext()) ? this.it.next().e : null;

				@Override
				public boolean hasNext() {
					return this.next != null;
				}

				@Override
				public E next() {
					E znext = this.next;
					Entry<E> ee = this.it.next();
					this.next = (ee == null) ? null : ee.e;

					while (znext != null && this.next != null && this.next.equals(znext)) {
						znext = this.next;
						ee = this.it.next();
						this.next = (ee == null) ? null : ee.e;
					}

					return znext;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException(
							"This operation is unsupported!");
				}

				@Override
				public void finalize() {
					this.close();
				}

				@Override
				public void close() {
					zheap.release();
				}
			};
		}
		// disk based
		this.sort();
		if (this.currentRun == null || this.currentRun.size == 0) {
			return new ParallelIterator<E>() {
				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public E next() {
					return null;
				}

				@Override
				public void remove() {
				}

				@Override
				public void close() {
				}
			};
		}
		final ParallelIterator<Entry<E>> iter = this.currentRun.iterator();
		return new ParallelIterator<E>() {
			E next = (iter.hasNext()) ? iter.next().e : null;

			@Override
			public boolean hasNext() {
				return (this.next != null);
			}

			@Override
			public E next() {
				E result = this.next;
				this.next = (iter.hasNext()) ? iter.next().e : null;
				// remove any remaining duplicates!
				while (this.next != null && result.equals(this.next)) {
					result = this.next;
					this.next = (iter.hasNext()) ? iter.next().e : null;
				}
				return result;
			}

			@Override
			public void remove() {
				iter.remove();
			}

			@Override
			public void close() {
				iter.close();
			}
		};
	}

	/**
	 * <p>main.</p>
	 *
	 * @param arg an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] arg){
		final SortConfiguration sortConfig = new SortConfiguration();
		sortConfig.useChunksMergeSort();
		sortConfig.setHuffmanCompression();
		final DBMergeSortedSet<String> set = new DBMergeSortedSet<String>(sortConfig, String.class);
		final String[] elems = { "aaab", "ab", "aaaaaab", "aaaaaaaaaaaaaaaaz", "aaaaaaajll", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" };
		// add to set
		for(int i=0; i<100000; i++){
			set.add(elems[i % elems.length]+(i % 100));
		}
		// print out sorted set
		for(final String s: set){
			System.out.println(s);
		}
	}
}
