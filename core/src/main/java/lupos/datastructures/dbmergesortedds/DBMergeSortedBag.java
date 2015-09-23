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
import lupos.misc.IOCostsInputStream;

@SuppressWarnings("unchecked")
public class DBMergeSortedBag<E extends Serializable> implements SortedBag<E> {

	protected volatile int id = 1;

	protected ReentrantLock mergeLock = new ReentrantLock();
	protected ReentrantLock newRunsLock = new ReentrantLock();

	/** Constant <code>mainFolder="new String[] { tmp//DBMergeSortedBag// "{trunked}</code> */
	protected static String[] mainFolder = new String[] { "tmp//DBMergeSortedBag//" };
	/** Constant <code>folderId=0</code> */
	protected static volatile int folderId = 0;
	protected final String[] folder;
	protected int unsortedID = 1;
	/** Constant <code>parallelMerging=false</code> */
	protected static boolean parallelMerging = false;
	/** Constant <code>numberOfThreads=1</code> */
	protected static int numberOfThreads = 1;

	static {
		DBMergeSortedBag.removeBagsFromDisk();
	}

	/**
	 * <p>removeBagsFromDisk.</p>
	 */
	public static void removeBagsFromDisk(){
		for (final String mf : mainFolder) {
			FileHelper.deleteDirectory(new File(mf));
		}
	}

	/**
	 * <p>getNewId.</p>
	 *
	 * @return a int.
	 */
	public int getNewId() {
		return this.id++;
	}

	/**
	 * <p>setTmpDir.</p>
	 *
	 * @param dir an array of {@link java.lang.String} objects.
	 */
	public static void setTmpDir(final String[] dir) {
		if (dir == null || dir.length == 0
				|| (dir.length == 1 && dir[0].compareTo("") == 0)) {
			return;
		}
		mainFolder = new String[dir.length];

		for (int i = 0; i < dir.length; i++) {
			if (!(dir[i].endsWith("//") || dir[i].endsWith("/") || dir[i]
					.endsWith("\""))) {
				dir[i] = dir[i] + "//";
			}
			mainFolder[i] = dir[i] + "tmp//DBMergeSortedBag//";
			FileHelper.deleteDirectory(new File(mainFolder[i]));
		}
	}

	/**
	 * <p>isParallelMerging.</p>
	 *
	 * @return a boolean.
	 */
	public static boolean isParallelMerging() {
		return parallelMerging;
	}

	/**
	 * <p>Setter for the field <code>parallelMerging</code>.</p>
	 *
	 * @param parallelMerging a boolean.
	 */
	public static void setParallelMerging(final boolean parallelMerging) {
		DBMergeSortedBag.parallelMerging = parallelMerging;
	}

	/**
	 * <p>Getter for the field <code>numberOfThreads</code>.</p>
	 *
	 * @return a int.
	 */
	public static int getNumberOfThreads() {
		return numberOfThreads;
	}

	/**
	 * <p>Setter for the field <code>numberOfThreads</code>.</p>
	 *
	 * @param numberOfThreads a int.
	 */
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

	/** Constant <code>lock</code> */
	protected static ReentrantLock lock = new ReentrantLock();

	/**
	 * <p>Getter for the field <code>classOfElements</code>.</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<? extends E> getClassOfElements() {
		return this.classOfElements;
	}

	/**
	 * Create a new DBMergeSortedBag that sorts according to the elements'
	 * natural order.
	 *
	 * @param sortConfiguration a {@link lupos.datastructures.dbmergesortedds.SortConfiguration} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedBag(final SortConfiguration<Entry<E>> sortConfiguration,
			final Class<? extends E> classOfElements) {
		this(sortConfiguration, null, classOfElements);
	}

	/**
	 * Create a new DBMergeSortedBag that sorts using the specified Comparator.
	 *
	 * @param comp
	 *            The Comparator to use for sorting.
	 * @param sortConfiguration a {@link lupos.datastructures.dbmergesortedds.SortConfiguration} object.
	 * @param classOfElements a {@link java.lang.Class} object.
	 */
	public DBMergeSortedBag(
			final SortConfiguration<Entry<E>> sortConfiguration,
			final Comparator<? super E> comp,
			final Class<? extends E> classOfElements) {
		this.sortConfiguration = sortConfiguration;
		lock.lock();
		try {
			if (comp != null) {
				this.comp = comp;
			} else {
				this.comp = new StandardComparator();
			}

			this.tosort = this.sortConfiguration.createToSort();
			if (this.tosort == null) {
				this.heap = this.sortConfiguration.createHeap();
				this.tosort = this.heap;
			}
			this.classOfElements = classOfElements;

			this.folder = new String[mainFolder.length];

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
				this.folder[i] = lls.remove(index) + (currentFolderId) + "//";
			}
		} finally {
			lock.unlock();
		}
	}

	/** {@inheritDoc} */
	@Override
	public Comparator<? super E> comparator() {
		return this.comp;
	}

	/** {@inheritDoc} */
	@Override
	public E first() {
		this.sort();
		if (this.currentRun == null) {
			return null;
		}
		return this.currentRun.getIndex(0).e;
	}

	/** {@inheritDoc} */
	@Override
	public DBMergeSortedBag<E> headBag(final E arg0) {
		final DBMergeSortedBag<E> headBag = new DBMergeSortedBag<E>(this.sortConfiguration, this.comp, this.classOfElements);
		for (final E e : this) {
			if (this.comp.compare(e, arg0) >= 0) {
				break;
			}
			headBag.add(e);
		}
		return headBag;
	}

	/** {@inheritDoc} */
	@Override
	public E last() {
		this.sort();
		if (this.currentRun == null) {
			return null;
		}
		E result = null;
		for (final Entry<E> e : this.currentRun) {
			result = e.e;
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public DBMergeSortedBag<E> subBag(final E arg0, final E arg1) {
		final DBMergeSortedBag<E> subBag = new DBMergeSortedBag<E>(this.sortConfiguration, this.comp, this.classOfElements);
		for (final E e : this) {
			if (this.comp.compare(e, arg1) >= 0) {
				break;
			}
			if (this.comp.compare(e, arg0) >= 0) {
				subBag.add(e);
			}
		}
		return subBag;
	}

	/** {@inheritDoc} */
	@Override
	public DBMergeSortedBag<E> tailBag(final E arg0) {
		final DBMergeSortedBag<E> tailBag = new DBMergeSortedBag<E>(this.sortConfiguration, this.comp, this.classOfElements);
		for (final E e : this) {
			if (this.comp.compare(e, arg0) >= 0) {
				tailBag.add(e);
			}
		}
		return tailBag;
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(final E ele) {
		if (this.tosort.isFull()) {
			this.popHeap();
		}

		final Entry<E> e = new Entry<E>(ele, this.comp, this.n++);
		if (this.currentRun == null) {
			e.run = 1;
		} else if (this.currentRun.max == null
				|| this.comp.compare(e.e, this.currentRun.max) >= 0) {
			e.run = this.currentRun.runID;
		} else {
			e.run = this.currentRun.runID + 1;
		}

		this.tosort.add(e);
		this.size++;
		return true;
	}

	/**
	 * <p>closeAndNewCurrentRun.</p>
	 */
	protected void closeAndNewCurrentRun() {
		try {
			this.currentRun.close();
		} catch (final IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
		this.currentRun = Run.createInstance(this);
	}

	/**
	 * This method adds an entry to the current run.
	 * This method is overridden by DBMergeSortedSet to eliminate duplicates already in the initial runs...
	 *
	 * @param e the entry to write into the current run!
	 */
	protected void addToRun(final Entry<E> e){
		this.currentRun.add(e);
	}

	private void popHeap() {
		if (this.heap != null) {
			int numberPopped = 0;
			while (!this.heap.isEmpty()
					&& (numberPopped == 0 || (numberPopped < this.sortConfiguration.getElementsToPopWhenHeapIsFull() && (this.currentRun == null || this.heap.peek().run <= this.currentRun.runID)))) {
				final Entry<E> e = this.heap.pop();
				e.runMatters = false;
				if (this.currentRun == null) {
					this.currentRun = Run.createInstance(this);
				} else if (e.run > this.currentRun.runID) {
					this.closeAndNewCurrentRun();
				}
				e.run = this.currentRun.runID;
				this.addToRun(e);
				numberPopped++;
			}
		} else {
			// sort this run completely!
			if (this.currentRun == null)
			 {
				this.currentRun = Run.createInstance(this);
			// maybe some few elements can be written into the previous run =>
			// do not close the old run now!
			}

			// System.out.println("Sorting for " + currentRun.file.toString()
			// + " " + tosort.isFull());
			final Iterator<Entry<E>> it = this.tosort.emptyDatastructure();
			while (it.hasNext()) {
				final Entry<E> e = it.next();
				if (e.run > this.currentRun.runID) {
					// now no elements must be written to the previous run any
					// more!
					this.closeAndNewCurrentRun();
				}
				e.run = this.currentRun.runID;
				this.addToRun(e);
			}
			this.tosort.clear();
			// System.out.println("Sorting for " + currentRun.file.toString()
			// + " " + tosort.isFull());
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		for (final E e : c) {
			this.add(e);
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.tosort.clear();
		try {
			if (this.currentRun != null) {
				this.currentRun.close();
			}
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		try {
			for (final String f : this.folder) {
				final File dir = new File(f);
				FileHelper.deleteDirectory(new File(f));
				dir.mkdirs();
			}
			if (this.currentRun != null) {
				this.currentRun.clear();
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		this.size = 0;
	}

	/**
	 * <p>sorted.</p>
	 *
	 * @return a boolean.
	 */
	public boolean sorted() {
		return this.tosort.isEmpty()
				&& (this.currentRun == null || this.unsortedID == this.currentRun.runID);
	}

	/**
	 * <p>sort.</p>
	 */
	public void sort() {
		if (this.sorted() || this.currentRun == null
				|| (this.unsortedID == this.currentRun.runID && this.currentRun.size == 0)) {
			return;
		}
		if (parallelMerging) {
			this.parallelSort();
		} else {
			this.sequentialSort();
		}
		System.out.println("Run ID after sorting:" + this.currentRun.runID);
	}

	/**
	 * <p>parallelSort.</p>
	 */
	protected void parallelSort() {
		while (!this.tosort.isEmpty()) {
			this.popHeap();
		}

		if (this.sorted()) {
			try {
				this.currentRun.flush();
			} catch (final IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			this.size = this.currentRun.size;
			return;
		}

		try {
			this.currentRun.close();
		} catch (final IOException e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}

		try {
			final BoundedBuffer<Integer> bbi = new BoundedBuffer<Integer>(
					this.currentRun.runID - this.unsortedID + 1);

			for (int i = this.unsortedID; i <= this.currentRun.runID; i++) {
				bbi.put(i);
			}

			final Thread[] threads = new Thread[numberOfThreads];

			this.newRuns = new boolean[numberOfThreads];

			for (int i = 0; i < numberOfThreads; i++) {
				this.newRuns[i] = false;
			}

			for (int i = 0; i < numberOfThreads; i++) {
				threads[i] = new MergeThread(bbi, i);
				threads[i].start();
			}

			// wait until all is merged:
			for (final Thread t : threads) {
				t.join();
				if (((MergeThread) t).getFinalRun() != null) {
					this.currentRun = ((MergeThread) t).getFinalRun();
				}
			}

			this.unsortedID = this.currentRun.runID;

			this.size = this.currentRun.size;

		} catch (final InterruptedException e) {
			System.err.println();
			e.printStackTrace();
		}
	}

	/**
	 * <p>sequentialSort.</p>
	 */
	protected void sequentialSort() {
		while (!this.tosort.isEmpty()) {
			this.popHeap();
		}

		final Heap<Entry<E>> sequentialMergeHeap = this.sortConfiguration.createMergeHeap();
		if(sequentialMergeHeap instanceof SortAndMergeHeap){
			System.err.println("The k-chunks merge heap is not ideal for merging, please set e.g. the SEQUENTIAL heap as type for the merge heap via DBMergeSortedBag.setMegeHeypType(...)");
		}
		if (!this.sorted()) {

			// loop until all is sorted!
			while (this.unsortedID < this.currentRun.runID) {
				this.closeAndNewCurrentRun();
				this.n = 0;
				final Iterator<Entry<E>>[] iters = new Iterator[Math.min(
						sequentialMergeHeap.maxLength(),
						(this.currentRun.runID - this.unsortedID))];
				for (int i = 0; i < iters.length; i++) {
					iters[i] = this.iteratorFromRun(this.unsortedID + i);
				}

				for (final Iterator<Entry<E>> it : iters) {
					final Entry e = it.next();

					if (e != null) {
						e.runMatters = false;
						sequentialMergeHeap.add(e);
					}
				}

				while (!sequentialMergeHeap.isEmpty()) {
					final Entry e = this.getNext(iters, this.unsortedID, sequentialMergeHeap);
					e.run = this.currentRun.runID;
					e.n = this.n++;
					this.currentRun.add(e);
				}

				// delete previous already merged files
				for (int i = 0; i < iters.length; i++) {
					FileHelper.deleteFile(this.folder[(this.unsortedID + i)
							% this.folder.length]
							+ (this.unsortedID + i));
				}

				this.unsortedID += iters.length;
			}
		}
		sequentialMergeHeap.release();
		try {
			this.currentRun.flush();
		} catch (final IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		this.size = this.currentRun.size;
		if (this.heap == null) {
			sequentialMergeHeap.release();
		}
	}

	private Iterator<Entry<E>> iteratorFromRun(final int runID) {
		try {
			final File file = new File(this.folder[runID % this.folder.length] + runID);
			if (!file.exists()) {
				return new Iterator<Entry<E>>() {
					@Override
					public boolean hasNext() {
						return false;
					}

					@Override
					public Entry<E> next() {
						return null;
					}

					@Override
					public void remove() {
					}
				};
			}

			return new Iterator<Entry<E>>() {
				boolean isClosed = false;
				Entry<E> next = null;
				int currentFile = 0;
				File fileLocal = file;
				LuposObjectInputStream is = new LuposObjectInputStream<E>(
						DBMergeSortedBag.this.sortConfiguration.createInputStream(IOCostsInputStream.createIOCostsInputStream(new BufferedInputStream(new FileInputStream(file)))),
						DBMergeSortedBag.this.classOfElements);
				int n = 0;

				@Override
				public boolean hasNext() {
					if (this.next == null && !this.isClosed) {
						this.next = this.next();
					}
					return this.next != null;
				}

				@Override
				public Entry<E> next() {
					if (this.next != null) {
						final Entry<E> res = this.next;
						this.next = null;
						return res;
					}
					if (this.isClosed) {
						return null;
					}
					try {
						Entry<E> e = null;
						try {
							e = this.is.readLuposEntry();
						} catch (final EOFException e1) {
						}
						if (e == null) {
							if (this.fileLocal.length() > Run.STORAGELIMIT) {
								this.currentFile++;
								this.fileLocal = new File(DBMergeSortedBag.this.folder[runID
										% DBMergeSortedBag.this.folder.length]
										+ runID + "_" + this.currentFile);
								if (this.fileLocal.exists()) {
									try {
										this.is.close();
									} catch (final IOException ee) {
									}
									this.is = new LuposObjectInputStream<E>(
										DBMergeSortedBag.this.sortConfiguration.createInputStream(IOCostsInputStream.createIOCostsInputStream(new BufferedInputStream(new FileInputStream(this.fileLocal)))),
											DBMergeSortedBag.this.classOfElements);
									e = this.is.readLuposEntry();
								}
							}
						}
						if (e != null) {
							e.comp = DBMergeSortedBag.this.comp;
							e.runMatters = false;
							e.n = this.n++;
							e.run = runID;
						} else {
							try {
								this.is.close();
								this.isClosed = true;
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
						this.is.close();
						this.isClosed = true;
					} catch (final IOException e) {
					}
					return null;
				}

				@Override
				public void remove() {
					throw (new UnsupportedOperationException());
				}
			};
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>getNext.</p>
	 *
	 * @param iters an array of {@link java.util.Iterator} objects.
	 * @param basisID a int.
	 * @param mergeheap a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 */
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
	/**
	 * <p>getNext.</p>
	 *
	 * @param iters an array of {@link java.util.Iterator} objects.
	 * @param hm a {@link java.util.Map} object.
	 * @param mergeheap a {@link lupos.datastructures.dbmergesortedds.heap.Heap} object.
	 * @return a {@link lupos.datastructures.dbmergesortedds.Entry} object.
	 */
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

	/** {@inheritDoc} */
	@Override
	public boolean contains(final Object o) {
		this.sort();
		if (this.currentRun == null) {
			final Iterator<E> it = this.iterator();
			while (it.hasNext()) {
				if (it.next().equals(o)) {
					return true;
				}
			}
			return false;
		}
		if (this.currentRun.contains((E) o)) {
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!this.contains(o)) {
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		if (!this.tosort.isEmpty()) {
			return false;
		}
		this.sort();
		if (this.currentRun == null) {
			return true;
		}
		if (!this.currentRun.isEmpty()) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public ParallelIterator<E> iterator() {
		// Do we have a small sorted bag? In other words:
		// Did we already write entries to disk or is all still stored in main
		// memory? In the latter case, we do not need to store it on disk and
		// just "sort" in memory!
		if (this.currentRun == null
				|| (this.unsortedID == this.currentRun.runID && this.currentRun.size == 0)) {
			final ToSort<Entry<E>> zheap = ToSort.cloneInstance(this.tosort);
			return new ParallelIterator<E>() {
				Iterator<Entry<E>> it = zheap.emptyDatastructure();

				@Override
				public boolean hasNext() {
					return this.it.hasNext();
				}

				@Override
				public E next() {
					final Entry<E> next = this.it.next();
					return (next == null) ? null : next.e;
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
		final ParallelIterator<Entry<E>> iter = this.currentRun.iterator();
		return new ParallelIterator<E>() {
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public E next() {
				if (iter.hasNext()) {
					return iter.next().e;
				} else {
					return null;
				}
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

	/** {@inheritDoc} */
	@Override
	public boolean remove(final Object arg0) {
		this.sort();
		if (this.currentRun == null) {
			ToSort<Entry<E>> zheap = this.sortConfiguration.createToSort();
			if (zheap == null) {
				zheap = this.sortConfiguration.createHeap();
			}
			final Iterator<Entry<E>> it = this.tosort.emptyDatastructure();
			boolean flag = false;
			while (it.hasNext()) {
				final Entry<E> next = it.next();
				if (next.e.equals(arg0)) {
					flag = true;
				} else {
					zheap.add(next);
				}
			}
			this.tosort = zheap;
			if (zheap instanceof Heap) {
				this.heap = (Heap<Entry<E>>) zheap;
			}
			return flag;
		}
		if (this.currentRun.contains((E) arg0)) {
			this.currentRun.remove((E) arg0);
			this.size--;
			return true;
		}
		return false;
	}

	/**
	 * <p>removeAndReturn.</p>
	 *
	 * @param e a E object.
	 * @return a E object.
	 */
	public E removeAndReturn(final E e) {
		this.sort();
		if (this.currentRun == null) {
			ToSort<Entry<E>> zheap = this.sortConfiguration.createToSort();
			if (zheap == null) {
				zheap = this.sortConfiguration.createHeap();
			}
			final Iterator<Entry<E>> it = this.tosort.emptyDatastructure();
			E removedEntry = null;
			while (it.hasNext()) {
				final Entry<E> next = it.next();
				if (removedEntry == null && next.e.equals(e)) {
					removedEntry = next.e;
				} else {
					zheap.add(next);
				}
			}
			this.tosort = zheap;
			if (zheap instanceof Heap) {
				this.heap = (Heap<Entry<E>>) zheap;
			}
			return removedEntry;
		}
		if (this.currentRun.contains(e)) {
			this.size--;
			return this.currentRun.remove(e).e;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(final Collection<?> arg0) {
		this.sort();
		if (this.currentRun == null) {
			ToSort<Entry<E>> zheap = this.sortConfiguration.createToSort();
			if (zheap == null) {
				zheap = this.sortConfiguration.createHeap();
			}
			final Iterator<Entry<E>> it = this.tosort.emptyDatastructure();
			boolean flag = false;
			while (it.hasNext()) {
				final Entry<E> next = it.next();
				boolean flag2 = false;
				for (final Object o : arg0) {
					if (next.e.equals(o)) {
						flag2 = true;
					}
				}
				if (flag2) {
					flag = true;
				} else {
					zheap.add(next);
				}
			}
			this.tosort = zheap;
			if (zheap instanceof Heap) {
				this.heap = (Heap<Entry<E>>) zheap;
			}
			return flag;
		}
		if (this.currentRun.containsAny((Collection<E>) arg0)) {
			this.currentRun.removeAll((Collection<E>) arg0);
			this.size = this.currentRun.size;
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		throw (new UnsupportedOperationException(
				"We don't do that kind of thing around here - a.k.a. ProgrammerWasTooLazyToImplementThisException."));
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.size;
	}

	/** {@inheritDoc} */
	@Override
	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
	}

	/** {@inheritDoc} */
	@Override
	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"If the contents of this datastructure were small enough to fit into RAM, it wouldn't be disk based."));
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

	/**
	 * <p>release.</p>
	 */
	public void release() {
		this.tosort.release();
		if (this.currentRun != null) {
			try {
				this.currentRun.close();
			} catch (final IOException e) {
				// e.printStackTrace();
			}
			for (final String f : this.folder) {
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
			return this.mergeRun;
		}

		@Override
		public void run() {

			try {
				while (true) {
					Object[] o;
					Heap<Entry<E>> mergeheap;
					boolean lastRound = false;
					DBMergeSortedBag.this.mergeLock.lock();
					try {
						// other values than 1 as minimum can cause deadlocks!
						o = this.bbi.get(1, 1 << DBMergeSortedBag.this.sortConfiguration.getMergeHeapHeight());

						if (o == null) {
							this.mergeRun = null;
							return;
						}

						mergeheap = DBMergeSortedBag.this.sortConfiguration.createMergeHeap();

						if(mergeheap instanceof SortAndMergeHeap){
							System.err.println("The k-chunks merge heap is not ideal for merging, please set e.g. the SEQUENTIAL heap as type for the merge heap via DBMergeSortedBag.setMegeHeypType(...)");
						}

						this.mergeRun = Run.createInstance(DBMergeSortedBag.this);

						// check if last merge iteration...
						DBMergeSortedBag.this.newRunsLock.lock();
						try {
							if (this.bbi.isCurrentlyEmpty() && this.noneInPipe()) {
								lastRound = true;
								this.bbi.endOfData();
							} else {
								DBMergeSortedBag.this.newRuns[this.number] = true;
							}
						} finally {
							DBMergeSortedBag.this.newRunsLock.unlock();
						}
					} finally {
						DBMergeSortedBag.this.mergeLock.unlock();
					}

					final Iterator<Entry<E>>[] iters = new Iterator[o.length];
					final HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
					for (int i = 0; i < iters.length; i++) {
						iters[i] = DBMergeSortedBag.this.iteratorFromRun((Integer) o[i]);
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
						final Entry e = DBMergeSortedBag.this.getNext(iters, hm, mergeheap);
						e.run = this.mergeRun.runID;
						e.n = n_local++;
						this.mergeRun.add(e);
					}

					mergeheap.release();
					// delete previous already merged files
					for (int i = 0; i < iters.length; i++) {
						FileHelper.deleteFile(DBMergeSortedBag.this.folder[((Integer) o[i])
								% DBMergeSortedBag.this.folder.length]
								+ (o[i]));
					}

					if (lastRound) {
						try {
							this.mergeRun.flush();
						} catch (final IOException e1) {
							System.out.println(e1);
							e1.printStackTrace();
						}
						return;
					}

					try {
						this.mergeRun.close();
					} catch (final IOException e1) {
						System.out.println(e1);
						e1.printStackTrace();
					}

					DBMergeSortedBag.this.newRunsLock.lock();
					try {
						DBMergeSortedBag.this.newRuns[this.number] = false;
						this.bbi.put(this.mergeRun.runID);
					} finally {
						DBMergeSortedBag.this.newRunsLock.unlock();
					}
				}

			} catch (final InterruptedException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}

		private boolean noneInPipe() {
			for (final boolean b : DBMergeSortedBag.this.newRuns) {
				// are the other still generating a new run?
				if (b) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * <p>main.</p>
	 *
	 * @param arg an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] arg){
		final SortConfiguration sortConfig = new SortConfiguration();
		sortConfig.setHuffmanCompression();
		final DBMergeSortedBag<String> set = new DBMergeSortedBag<String>(sortConfig, String.class);
		final String[] elems = { "aaab", "ab", "aaaaaab", "aaaaaaaaaaaaaaaaz", "aaaaaaajll" };
		// add to set
		for(int i=0; i<100000; i++){
			for(int j=0; j<elems.length; j++){
				set.add(elems[j]+(i % 100));
			}
		}
		// print out sorted set
		for(final String s: set){
			System.out.println(s);
		}
	}
}
