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
	 * @param heapHeight
	 *            The height of the heap used to presort the elements in memory.
	 *            (The maximum number of elements that are held in memory at any
	 *            given time will be 2**heapHeight-1)
	 * @throws RemoteException
	 */
	public DBMergeSortedSet(final int heapHeight,
			final Class<? extends E> classOfElements) {
		super(heapHeight, classOfElements);
	}

	/**
	 * This constructor is just there to make the class serializable and should
	 * not be used in other cases (than the Java serialization)!
	 * 
	 * @throws RemoteException
	 */
	public DBMergeSortedSet() {
		super(0, null);
	}

	/**
	 * Standard constructor: The HeapHeight is set to 11.
	 * 
	 * @throws RemoteException
	 */
	public DBMergeSortedSet(final Class<? extends E> classOfElements){
		this(16, classOfElements);
	}

	/**
	 * Create a new DBMergeSortedBag that sorts using the specified Comparator.
	 * 
	 * @param heapHeight
	 *            The height of the heap used to presort the elements in memory.
	 *            (The maximum number of elements that are held in memory at any
	 *            given time will be 2**heapHeight-1)
	 * @param comp
	 *            The Comparator to use for sorting.
	 * @throws RemoteException
	 */
	public DBMergeSortedSet(final int heapHeight,
			final Comparator<? super E> comp,
			final Class<? extends E> classOfElements) {
		super(heapHeight, comp, classOfElements);
	}

	public DBMergeSortedSet(final Comparator<? super E> comp,
			final Class<? extends E> classOfElements) {
		this(11, comp, classOfElements);
	}

	private DBMergeSortedSet(final DBMergeSortedBag<E> bag,
			final Class<? extends E> classOfElements) {
		super(bag.heapHeight, bag.comp, classOfElements);
	}

	public E get(final E e) {
		return subSet(e, e).last();
	}

	@Override
	public boolean remove(final Object o) {
		final List<Object> list = new LinkedList<Object>();
		list.add(o);
		return removeAll(list);
	}

	@Override
	public int size() {
		if (currentRun == null) {
			final ParallelIterator<E> it = iterator();
			int counter = 0;
			while (it.hasNext()) {
				counter++;
				it.next();
			}
			it.close();
			return counter;
		}
		sort();
		return super.size();
	}

	@Override
	protected Entry<E> getNext(final Iterator<Entry<E>>[] iters,
			final Map<Integer, Integer> hm, final Heap<Entry<E>> mergeheap) {
		Entry<E> res = mergeheap.pop();
		res.run = (res.run == 0) ? 1 : res.run;
		if (iters[hm.get(res.run)].hasNext()) {
			final Entry<E> e = iters[hm.get(res.run)].next();
			e.runMatters = false;
			mergeheap.add(e);
		} else
			for (final Iterator<Entry<E>> it : iters) {
				if (it.hasNext()) {
					final Entry<E> e = it.next();
					e.runMatters = false;
					mergeheap.add(e);
					break;
				}
			}
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
		} else
			for (final Iterator<Entry<E>> it : iters) {
				if (it.hasNext()) {
					final Entry<E> elem = it.next();
					elem.runMatters = false;
					mergeheap.add(elem);
					break;
				}
			}
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

	public DBMergeSortedSet<E> subSet(final E arg0, final E arg1) {
			return new DBMergeSortedSet<E>(super.subBag(arg0, arg1),
					classOfElements);
	}

	public DBMergeSortedSet<E> tailSet(final E arg0) {
			return new DBMergeSortedSet<E>(super.tailBag(arg0), classOfElements);
	}

	@Override
	public String toString() {
		final Iterator<E> iter = iterator();
		String result = "[";
		while (iter.hasNext()) {
			result += iter.next();
			if (iter.hasNext())
				result += ", ";
		}
		result += "]";
		return result;
	}

	public DBMergeSortedSet<E> headSet(final E toElement) {
			return new DBMergeSortedSet<E>(super.headBag(toElement),
					classOfElements);
	}

	@Override
	public ParallelIterator<E> iterator() {
		// Do we have a small sorted bag? In other words:
		// Did we already write entries to disk or is all still stored in main
		// memory? In the latter case, we do not need to store it on disk and
		// just "sort" in memory!
		if (currentRun == null) {
			final ToSort<Entry<E>> zheap = ToSort.cloneInstance(tosort);
			return new ParallelIterator<E>() {
				Iterator<Entry<E>> it = zheap.emptyDatastructure();
				E next = (it.hasNext()) ? it.next().e : null;

				public boolean hasNext() {
					return next != null;
				}

				public E next() {
					E znext = next;
					Entry<E> ee = it.next();
					next = (ee == null) ? null : ee.e;

					while (znext != null && next != null && next.equals(znext)) {
						znext = next;
						ee = it.next();
						next = (ee == null) ? null : ee.e;
					}

					return znext;
				}

				public void remove() {
					throw new UnsupportedOperationException(
							"This operation is unsupported!");
				}

				@Override
				public void finalize() {
					close();
				}

				public void close() {
					zheap.release();
				}
			};
		}
		// if (currentRun == null) {
		// final Heap<Entry<E>> zheap = Heap.cloneInstance(tosort);
		// return new ParallelIterator<E>() {
		// public boolean hasNext() {
		// return !zheap.isEmpty();
		// }
		//
		// public E next() {
		// if (!hasNext())
		// return null;
		// Entry<E> e = zheap.pop();
		// while (!zheap.isEmpty() && e.equals(zheap.peek()))
		// e = zheap.pop();
		// return e.e;
		// }
		//
		// public void remove() {
		// throw new UnsupportedOperationException(
		// "This operation is unsupported!");
		// }
		//
		// @Override
		// public void finalize() {
		// close();
		// }
		//
		// public void close() {
		// zheap.release();
		// }
		// };
		// }
		// disk based
		sort();
		if (currentRun == null || currentRun.size == 0)
			return new ParallelIterator<E>() {
				public boolean hasNext() {
					return false;
				}

				public E next() {
					return null;
				}

				public void remove() {
				}

				public void close() {
				}
			};
		final ParallelIterator<Entry<E>> iter = currentRun.iterator();
		return new ParallelIterator<E>() {
			E next = (iter.hasNext()) ? iter.next().e : null;

			public boolean hasNext() {
				return (next != null);
			}

			public E next() {
				E result = next;
				next = (iter.hasNext()) ? iter.next().e : null;
				// remove any remaining duplicates!
				while (next != null && result.equals(next)) {
					result = next;
					next = (iter.hasNext()) ? iter.next().e : null;
				}
				return result;
			}

			public void remove() {
				iter.remove();
			}

			public void close() {
				iter.close();
			}
		};
	}
}
