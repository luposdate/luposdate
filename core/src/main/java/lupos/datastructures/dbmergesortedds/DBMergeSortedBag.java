/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
 */
package lupos.datastructures.dbmergesortedds;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.SortAndMergeHeap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.sorteddata.SortedBag;
import lupos.io.LuposObjectInputStream;
import lupos.misc.FileHelper;

@SuppressWarnings("unchecked")
public class DBMergeSortedBag<E extends Serializable> implements SortedBag<E> {

	protected volatile int id = 1;

	protected ReentrantLock mergeLock = new ReentrantLock();
	protected ReentrantLock newRunsLock = new ReentrantLock();

	protected static String[] mainFolder = new String[] { "tmp//DBMergeSortedBag//" };
	protected static volatile int folderId = 0;
	protected final String[] folder;
	protected int unsortedID = 1;
	protected static boolean parallelMerging = false;
	protected static int numberOfThreads = 1;

	static {
		DBMergeSortedBag.removeBagsFromDisk();
	}
	
	public static void removeBagsFromDisk(){
		for (final String mf : mainFolder) {
			FileHelper.deleteDirectory(new File(mf));
		}
	}

	public int getNewId() {
		return this.id++;
	}

	public static void setTmpDir(final String[] dir) {
		if (dir == null || dir.length == 0
				|| (dir.length == 1 && dir[0].compareTo("") == 0))
			return;
		mainFolder = new String[dir.length];

		for (int i = 0; i < dir.length; i++) {
			if (!(dir[i].endsWith("//") || dir[i].endsWith("/") || dir[i]
					.endsWith("\"")))
				dir[i] = dir[i] + "//";
			mainFolder[i] = dir[i] + "tmp//DBMergeSortedBag//";
			FileHelper.deleteDirectory(new File(mainFolder[i]));
		}
	}

	public static boolean isParallelMerging() {
		return parallelMerging;
	}

	public static void setParallelMerging(final boolean parallelMerging) {
		DBMergeSortedBag.parallelMerging = parallelMerging;
	}

	public static int getNumberOfThreads() {
		return numberOfThreads;
	}

	public static void setNumberOfThreads(final int numberOfThreads) {
		DBMergeSortedBag.numberOfThreads = numberOfThreads;
	}

	protected Run<E> currentRun = null;
	protected Comparator<? super E> comp;
	protected ToSort<Entry<E>> tosort;
	protected Heap<Entry<E>> heap;
	protected int size = 0;
	protected int n = 0;
	protected Class<? extends E> classOfElements;
	
	protected final SortConfiguration<Entry<E>> sortConfiguration;

	protected static ReentrantLock lock = new ReentrantLock();

	public Class<? extends E> getClassOfElements() {
		return classOfElements;
	}

	/**
	 * Create a new DBMergeSortedBag that sorts according to the elements'
	 * natural order.
	 */
	public DBMergeSortedBag(final SortConfiguration<Entry<E>> sortConfiguration,
			final Class<? extends E> classOfElements) {
		this(sortConfiguration, null, classOfElements);
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
	 */
	public DBMergeSortedBag(
			final SortConfiguration<Entry<E>> sortConfiguration,
			final Comparator<? super E> comp,
			final Class<? extends E> classOfElements) {
		this.sortConfiguration = sortConfiguration;
		lock.lock();
		try {
			if (comp != null)
				this.comp = comp;
			else
				this.comp = new StandardComparator();
			
			tosort = this.sortConfiguration.createToSort();
			if (tosort == null) {
				heap = this.sortConfiguration.createHeap();
				tosort = heap;
			}
			this.classOfElements = classOfElements;

			folder = new String[mainFolder.length];

			final int currentFolderId = folderId++;
			final LinkedList<String> lls = new LinkedList<String>();
			for (int i = 0; i < mainFolder.length; i++) {
				lls.add(mainFolder[i]);
			}

			for (int i = 0; i < mainFolder.length; i++) {
				// choose randomly the directories, such that the runs are well
				// distributed
				// over the different directories...
				final int index = (int) (Math.floor(Math.random() * lls.size()));
				folder[i] = lls.remove(index) + (currentFolderId) + "//";
			}
		} finally {
			lock.unlock();
		}
	}

	public Comparator<? super E> comparator() {
		return comp;
	}

	public E first() {
		sort();
		if (currentRun == null)
			return null;
		return currentRun.getIndex(0).e;
	}

	public DBMergeSortedBag<E> headBag(final E arg0) {
		final DBMergeSortedBag<E> headBag = new DBMergeSortedBag<E>(this.sortConfiguration, comp, classOfElements);
		for (final E e : this) {
			if (comp.compare(e, arg0) >= 0)
				break;
			headBag.add(e);
		}
		return headBag;
	}

	public E last() {
		sort();
		if (currentRun == null)
			return null;
		E result = null;
		for (final Entry<E> e : currentRun)
			result = e.e;
		return result;
	}

	public DBMergeSortedBag<E> subBag(final E arg0, final E arg1) {
		final DBMergeSortedBag<E> subBag = new DBMergeSortedBag<E>(this.sortConfiguration, comp, classOfElements);
		for (final E e : this) {
			if (comp.compare(e, arg1) >= 0)
				break;
			if (comp.compare(e, arg0) >= 0)
				subBag.add(e);
		}
		return subBag;
	}

	public DBMergeSortedBag<E> tailBag(final E arg0) {
		final DBMergeSortedBag<E> tailBag = new DBMergeSortedBag<E>(this.sortConfiguration, comp, classOfElements);
		for (final E e : this) {
			if (comp.compare(e, arg0) >= 0)
				tailBag.add(e);
		}
		return tailBag;
	}

	public boolean add(final E ele) {
		if (tosort.isFull()) {
			popHeap();
		}

		final Entry<E> e = new Entry<E>(ele, comp, n++);
		if (currentRun == null)
			e.run = 1;
		else if (currentRun.max == null
				|| comp.compare(e.e, currentRun.max) >= 0) {
			e.run = currentRun.runID;
		} else
			e.run = currentRun.runID + 1;

		tosort.add(e);
		size++;
		return true;
	}

	protected void closeAndNewCurrentRun() {
		try {
			currentRun.close();
		} catch (final IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
		currentRun = Run.createInstance(this);
	}
	
	/**
	 * This method adds an entry to the current run.
	 * This method is overridden by DBMergeSortedSet to eliminate duplicates already in the initial runs...
	 * @param e the entry to write into the current run!
	 */
	protected void addToRun(final Entry<E> e){
		this.currentRun.add(e);
	}

	private void popHeap() {
		if (heap != null) {
			int numberPopped = 0;
			while (!heap.isEmpty()
					&& (numberPopped == 0 || (numberPopped < this.sortConfiguration.getElementsToPopWhenHeapIsFull() && (currentRun == null || heap.peek().run <= currentRun.runID)))) {
				final Entry<E> e = heap.pop();
				e.runMatters = false;
				if (currentRun == null)
					currentRun = Run.createInstance(this);
				else if (e.run > currentRun.runID) {
					closeAndNewCurrentRun();
				}
				e.run = currentRun.runID;
				this.addToRun(e);
				numberPopped++;
			}
		} else {
			// sort this run completely!
			if (currentRun == null)
				currentRun = Run.createInstance(this);
			// maybe some few elements can be written into the previous run =>
			// do not close the old run now!

			// System.out.println("Sorting for " + currentRun.file.toString()
			// + " " + tosort.isFull());
			final Iterator<Entry<E>> it = tosort.emptyDatastructure();
			while (it.hasNext()) {
				final Entry<E> e = it.next();
				if (e.run > currentRun.runID) {
					// now no elements must be written to the previous run any
					// more!
					closeAndNewCurrentRun();
				}
				e.run = currentRun.runID;
				this.addToRun(e);
			}
			tosort.clear();
			// System.out.println("Sorting for " + currentRun.file.toString()
			// + " " + tosort.isFull());
		}
	}

	public boolean addAll(final Collection<? extends E> c) {
		for (final E e : c) {
			add(e);
		}
		return true;
	}

	public void clear() {
		tosort.clear();
		try {
			if (currentRun != null)
				currentRun.close();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		try {
			for (final String f : folder) {
				final File dir = new File(f);
				FileHelper.deleteDirectory(new File(f));
				dir.mkdirs();
			}
			if (currentRun != null) {
				currentRun.clear();
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		size = 0;
	}

	public boolean sorted() {
		return tosort.isEmpty()
				&& (currentRun == null || unsortedID == currentRun.runID);
	}

	public void sort() {
		if (sorted() || currentRun == null
				|| (unsortedID == currentRun.runID && currentRun.size == 0))
			return;
		if (parallelMerging)
			parallelSort();
		else
			sequentialSort();
		System.out.println("Run ID after sorting:" + currentRun.runID);
	}

	protected void parallelSort() {
		while (!tosort.isEmpty())
			popHeap();

		if (sorted()) {
			try {
				currentRun.flush();
			} catch (final IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			size = currentRun.size;
			return;
		}

		try {
			currentRun.close();
		} catch (final IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}

		try {
			final BoundedBuffer<Integer> bbi = new BoundedBuffer<Integer>(
					currentRun.runID - unsortedID + 1);

			for (int i = unsortedID; i <= currentRun.runID; i++) {
				bbi.put(i);
			}

			final Thread[] threads = new Thread[numberOfThreads];

			newRuns = new boolean[numberOfThreads];

			for (int i = 0; i < numberOfThreads; i++) {
				newRuns[i] = false;
			}

			for (int i = 0; i < numberOfThreads; i++) {
				threads[i] = new MergeThread(bbi, i);
				threads[i].start();
			}

			// wait until all is merged:
			for (final Thread t : threads) {
				t.join();
				if (((MergeThread) t).getFinalRun() != null)
					currentRun = ((MergeThread) t).getFinalRun();
			}

			unsortedID = currentRun.runID;

			size = currentRun.size;

		} catch (final InterruptedException e) {
			System.err.println();
			e.printStackTrace();
		}
	}

	protected void sequentialSort() {
		while (!tosort.isEmpty())
			popHeap();

		final Heap<Entry<E>> sequentialMergeHeap = this.sortConfiguration.createMergeHeap();
		if(sequentialMergeHeap instanceof SortAndMergeHeap){
			System.err.println("The k-chunks merge heap is not ideal for merging, please set e.g. the SEQUENTIAL heap as type for the merge heap via DBMergeSortedBag.setMegeHeypType(...)");
		}
		if (!sorted()) {

			// loop until all is sorted!
			while (unsortedID < currentRun.runID) {
				this.closeAndNewCurrentRun();
				n = 0;
				final Iterator<Entry<E>>[] iters = new Iterator[Math.min(
						sequentialMergeHeap.maxLength(),
						(currentRun.runID - unsortedID))];
				for (int i = 0; i < iters.length; i++) {
					iters[i] = iteratorFromRun(unsortedID + i);
				}

				for (final Iterator<Entry<E>> it : iters) {
					final Entry e = it.next();

					if (e != null) {
						e.runMatters = false;
						sequentialMergeHeap.add(e);
					}
				}

				while (!sequentialMergeHeap.isEmpty()) {
					final Entry e = getNext(iters, unsortedID, sequentialMergeHeap);
					e.run = currentRun.runID;
					e.n = n++;
					currentRun.add(e);
				}

				// delete previous already merged files
				for (int i = 0; i < iters.length; i++) {
					FileHelper.deleteFile(folder[(unsortedID + i)
							% folder.length]
							+ (unsortedID + i));
				}

				unsortedID += iters.length;
			}
		}
		sequentialMergeHeap.release();
		try {
			currentRun.flush();
		} catch (final IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		size = currentRun.size;
		if (heap == null)
			sequentialMergeHeap.release();
	}

	private Iterator<Entry<E>> iteratorFromRun(final int runID) {
		try {
			final File file = new File(folder[runID % folder.length] + runID);
			if (!file.exists())
				return new Iterator<Entry<E>>() {
					public boolean hasNext() {
						return false;
					}

					public Entry<E> next() {
						return null;
					}

					public void remove() {
					}
				};

			return new Iterator<Entry<E>>() {
				boolean isClosed = false;
				Entry<E> next = null;
				int currentFile = 0;
				File fileLocal = file;
				LuposObjectInputStream is = new LuposObjectInputStream<E>(
						DBMergeSortedBag.this.sortConfiguration.createInputStream(new BufferedInputStream(new FileInputStream(file))),
						classOfElements);
				int n = 0;

				public boolean hasNext() {
					if (next == null && !isClosed)
						next = next();
					return next != null;
				}

				public Entry<E> next() {
					if (next != null) {
						final Entry<E> res = next;
						next = null;
						return res;
					}
					if (isClosed)
						return null;
					try {
						Entry<E> e = null;
						try {
							e = is.readLuposEntry();
						} catch (final EOFException e1) {
						}
						if (e == null) {
							if (fileLocal.length() > Run.STORAGELIMIT) {
								currentFile++;
								fileLocal = new File(folder[runID
										% folder.length]
										+ runID + "_" + currentFile);
								if (fileLocal.exists()) {
									try {
										is.close();
									} catch (final IOException ee) {
									}
									is = new LuposObjectInputStream<E>(
										DBMergeSortedBag.this.sortConfiguration.createInputStream(
											new BufferedInputStream(
													new FileInputStream(
															fileLocal))),
											classOfElements);
									e = is.readLuposEntry();
								}
							}
						}
						if (e != null) {
							e.comp = comp;
							e.runMatters = false;
							e.n = n++;
							e.run = runID;
						} else {
							try {
								is.close();
								isClosed = true;
							} catch (final IOException ee) {
							}
						}
						return e;
					} catch (final EOFException e) {

					} catch (final IOException e) {
						e.printStackTrace();
					} catch (final ClassNotFoundException e) {
						e.printStackTrace();
					}
					try {
						is.close();
						isClosed = true;
					} catch (final IOException e) {
					}
					return null;
				}

				public void remove() {
					throw (new UnsupportedOperationException());
				}
			};
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected Entry<E> getNext(final Iterator<Entry<E>>[] iters,
			final int basisID, final Heap<Entry<E>> mergeheap) {
		final Entry<E> res = mergeheap.pop();
		if (iters[res.run - basisID].hasNext()) {
			final Entry<E> e = iters[res.run - basisID].next();
			e.runMatters = false;
			mergeheap.add(e);
		}
		return res;
	}

	// this method has the same functionality to the previous one, except that a
	// map defines which iterator id is the right one for which run ID
	protected Entry<E> getNext(final Iterator<Entry<E>>[] iters,
			final Map<Integer, Integer> hm, final Heap<Entry<E>> mergeheap) {
		final Entry<E> res = mergeheap.pop();
		if (iters[hm.get(res.run)].hasNext()) {
			final Entry<E> e = iters[hm.get(res.run)].next();
			e.runMatters = false;
			mergeheap.add(e);
		}
		return res;
	}

	public boolean contains(final Object o) {
		sort();
		if (currentRun == null) {
			final Iterator<E> it = this.iterator();
			while (it.hasNext()) {
				if (it.next().equals(o))
					return true;
			}
			return false;
		}
		if (currentRun.contains((E) o))
			return true;
		return false;
	}

	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c)
			if (!contains(o))
				return false;
		return true;
	}

	public boolean isEmpty() {
		if (!tosort.isEmpty())
			return false;
		sort();
		if (currentRun == null)
			return true;
		if (!currentRun.isEmpty())
			return false;
		return true;
	}

	public ParallelIterator<E> iterator() {
		// Do we have a small sorted bag? In other words:
		// Did we already write entries to disk or is all still stored in main
		// memory? In the latter case, we do not need to store it on disk and
		// just "sort" in memory!
		if (currentRun == null
				|| (unsortedID == currentRun.runID && currentRun.size == 0)) {
			final ToSort<Entry<E>> zheap = ToSort.cloneInstance(tosort);
			return new ParallelIterator<E>() {
				Iterator<Entry<E>> it = zheap.emptyDatastructure();

				public boolean hasNext() {
					return it.hasNext();
				}

				public E next() {
					final Entry<E> next = it.next();
					return (next == null) ? null : next.e;
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
		// disk based
		sort();
		final ParallelIterator<Entry<E>> iter = currentRun.iterator();
		return new ParallelIterator<E>() {
			public boolean hasNext() {
				return iter.hasNext();
			}

			public E next() {
				if (iter.hasNext())
					return iter.next().e;
				else
					return null;
			}

			public void remove() {
				iter.remove();
			}

			public void close() {
				iter.close();
			}
		};
	}

	public boolean remove(final Object arg0) {
		sort();
		if (currentRun == null) {
			ToSort<Entry<E>> zheap = this.sortConfiguration.createToSort();
			if (zheap == null) {
				zheap = this.sortConfiguration.createHeap();
			}
			final Iterator<Entry<E>> it = tosort.emptyDatastructure();
			boolean flag = false;
			while (it.hasNext()) {
				final Entry<E> next = it.next();
				if (next.e.equals(arg0))
					flag = true;
				else
					zheap.add(next);
			}
			tosort = zheap;
			if (zheap instanceof Heap)
				heap = (Heap<Entry<E>>) zheap;
			return flag;
		}
		if (currentRun.contains((E) arg0)) {
			currentRun.remove((E) arg0);
			size--;
			return true;
		}
		return false;
	}

	public E removeAndReturn(final E e) {
		sort();
		if (currentRun == null) {
			ToSort<Entry<E>> zheap = this.sortConfiguration.createToSort();
			if (zheap == null) {
				zheap = this.sortConfiguration.createHeap();
			}
			final Iterator<Entry<E>> it = tosort.emptyDatastructure();
			E removedEntry = null;
			while (it.hasNext()) {
				final Entry<E> next = it.next();
				if (removedEntry == null && next.e.equals(e))
					removedEntry = next.e;
				else
					zheap.add(next);
			}
			tosort = zheap;
			if (zheap instanceof Heap)
				heap = (Heap<Entry<E>>) zheap;
			return removedEntry;
		}
		if (currentRun.contains(e)) {
			size--;
			return currentRun.remove(e).e;
		}
		return null;
	}

	public boolean removeAll(final Collection<?> arg0) {
		sort();
		if (currentRun == null) {
			ToSort<Entry<E>> zheap = this.sortConfiguration.createToSort();
			if (zheap == null) {
				zheap = this.sortConfiguration.createHeap();
			}
			final Iterator<Entry<E>> it = tosort.emptyDatastructure();
			boolean flag = false;
			while (it.hasNext()) {
				final Entry<E> next = it.next();
				boolean flag2 = false;
				for (final Object o : arg0) {
					if (next.e.equals(o))
						flag2 = true;
				}
				if (flag2)
					flag = true;
				else
					zheap.add(next);
			}
			tosort = zheap;
			if (zheap instanceof Heap)
				heap = (Heap<Entry<E>>) zheap;
			return flag;
		}
		if (currentRun.containsAny((Collection<E>) arg0)) {
			currentRun.removeAll((Collection<E>) arg0);
			size = currentRun.size;
			return true;
		}
		return false;
	}

	public boolean retainAll(final Collection<?> arg0) {
		throw (new UnsupportedOperationException(
				"We don't do that kind of thing around here - a.k.a. ProgrammerWasTooLazyToImplementThisException."));
	}

	public int size() {
		return size;
	}

	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
	}

	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
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

	public void release() {
		tosort.release();
		if (currentRun != null) {
			try {
				currentRun.close();
			} catch (final IOException e) {
				// e.printStackTrace();
			}
			for (final String f : folder) {
				FileHelper.deleteDirectory(new File(f));
			}
		}
	}

	protected boolean[] newRuns;

	public class MergeThread extends Thread {

		private final BoundedBuffer<Integer> bbi;
		private final int number;
		private Run<E> mergeRun;

		public MergeThread(final BoundedBuffer<Integer> bbi, final int number) {
			this.bbi = bbi;
			this.number = number;
		}

		public Run getFinalRun() {
			return mergeRun;
		}

		@Override
		public void run() {

			try {
				while (true) {
					Object[] o;
					Heap<Entry<E>> mergeheap;
					boolean lastRound = false;
					mergeLock.lock();
					try {
						// other values than 1 as minimum can cause deadlocks!
						o = bbi.get(1, 1 << DBMergeSortedBag.this.sortConfiguration.getMergeHeapHeight());

						if (o == null) {
							mergeRun = null;
							return;
						}

						mergeheap = DBMergeSortedBag.this.sortConfiguration.createMergeHeap();

						if(mergeheap instanceof SortAndMergeHeap){
							System.err.println("The k-chunks merge heap is not ideal for merging, please set e.g. the SEQUENTIAL heap as type for the merge heap via DBMergeSortedBag.setMegeHeypType(...)");
						}
						
						mergeRun = Run.createInstance(DBMergeSortedBag.this);

						// check if last merge iteration...
						newRunsLock.lock();
						try {
							if (bbi.isCurrentlyEmpty() && noneInPipe()) {
								lastRound = true;
								bbi.endOfData();
							} else
								newRuns[number] = true;
						} finally {
							newRunsLock.unlock();
						}
					} finally {
						mergeLock.unlock();
					}

					final Iterator<Entry<E>>[] iters = new Iterator[o.length];
					final HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
					for (int i = 0; i < iters.length; i++) {
						iters[i] = iteratorFromRun((Integer) o[i]);
						hm.put((Integer) o[i], i);
					}

					for (final Iterator<Entry<E>> it : iters) {
						final Entry e = it.next();
						if (e != null) {
							e.runMatters = false;
							mergeheap.add(e);
						}
					}
					int n_local = 0;
					while (!mergeheap.isEmpty()) {
						final Entry e = getNext(iters, hm, mergeheap);
						e.run = mergeRun.runID;
						e.n = n_local++;
						mergeRun.add(e);
					}

					mergeheap.release();
					// delete previous already merged files
					for (int i = 0; i < iters.length; i++) {
						FileHelper.deleteFile(folder[((Integer) o[i])
								% folder.length]
								+ (o[i]));
					}

					if (lastRound) {
						try {
							mergeRun.flush();
						} catch (final IOException e1) {
							System.out.println(e1);
							e1.printStackTrace();
						}
						return;
					}

					try {
						mergeRun.close();
					} catch (final IOException e1) {
						System.out.println(e1);
						e1.printStackTrace();
					}

					newRunsLock.lock();
					try {
						newRuns[number] = false;
						bbi.put(mergeRun.runID);
					} finally {
						newRunsLock.unlock();
					}
				}

			} catch (final InterruptedException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}

		private boolean noneInPipe() {
			for (final boolean b : newRuns) {
				// are the other still generating a new run?
				if (b)
					return false;
			}
			return true;
		}
	}
	
	public static void main(String[] arg){
		SortConfiguration sortConfig = new SortConfiguration();
		sortConfig.setHuffmanCompression();
		DBMergeSortedBag<String> set = new DBMergeSortedBag<String>(sortConfig, String.class);
		String[] elems = { "aaab", "ab", "aaaaaab", "aaaaaaaaaaaaaaaaz", "aaaaaaajll" };
		// add to set
		for(int i=0; i<100000; i++){
			for(int j=0; j<elems.length; j++){
				set.add(elems[j]+(i % 100));
			}
		}
		// print out sorted set
		for(String s: set){
			System.out.println(s);
		}
	}
}