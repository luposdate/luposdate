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
package lupos.datastructures.dbmergesortedds.heap;

import java.util.Iterator;

import lupos.datastructures.parallel.BoundedBuffer;

public class ParallelHeap<E extends Comparable<E>> extends
		OptimizedSequentialHeap<E> {

	protected static final int firstHeapHeight = 2;
	protected int localFirstHeapHeight = firstHeapHeight;

	public ParallelHeap(final int height) {
		super(firstHeapHeight);
		this.height = height;
		initParallelHeaps();
	}

	public ParallelHeap(final int height, final int localFirstHeapHeight) {
		super(localFirstHeapHeight);
		this.localFirstHeapHeight = localFirstHeapHeight;
		this.height = height;
		initParallelHeaps();
	}

	public ParallelHeap(final Object[] content, final int size) {
		super(content, size);
		this.length = (content[0] == null && size > 0) ? size + 1 : size;
		cloneParallelHeaps(content);
		determineHeight(maxLength());
	}

	public ParallelHeap(final Object[] content, final int size,
			final int localFirstHeapHeight) {
		super(content, size);
		this.localFirstHeapHeight = localFirstHeapHeight;
		this.length = (content[0] == null && size > 0) ? size + 1 : size;
		cloneParallelHeaps(content);
		determineHeight(maxLength());
	}

	public ParallelHeap(final int length_or_height, final boolean length) {
		super(firstHeapHeight);
		if (length)
			determineHeight(length_or_height);
		else {
			this.height = length_or_height < 3 ? 3 : length_or_height;
		}
		initParallelHeaps();
	}

	public ParallelHeap(final int length_or_height, final boolean length,
			final int localFirstHeapHeight) {
		super(localFirstHeapHeight);
		this.localFirstHeapHeight = localFirstHeapHeight;
		if (length)
			determineHeight(length_or_height);
		else {
			this.height = length_or_height < 3 ? 3 : length_or_height;
		}
		initParallelHeaps();
	}

	protected void initParallelHeaps() {
		for (int i = (1 << localFirstHeapHeight) - 1; i < arr.length; i++) {
			final InnerParallelHeap<E> ihe_new = new InnerParallelHeap<E>(
					new SequentialHeap<E>(this.height - localFirstHeapHeight));
			this.arr[i] = ihe_new;
			ihe_new.start();
		}
	}

	protected void cloneParallelHeaps(final Object[] content) {
		for (int i = (1 << localFirstHeapHeight) - 1; i < arr.length; i++) {
			final InnerParallelHeap<E> ihe = ((InnerParallelHeap<E>) content[i]);
			// force ihe to finish all its instructions of its instruction
			// queue...
			ihe.waitForEmptyInstructionQueue();
			final InnerParallelHeap<E> ihe_new = new InnerParallelHeap<E>(
					new SequentialHeap<E>(ihe.sh.getContent(), ihe.sh.size()));
			this.arr[i] = ihe_new;
			ihe_new.start();
		}
	}

	@Override
	public void release() {
		for (int i = (1 << localFirstHeapHeight) - 1; i < arr.length; i++) {
			final InnerParallelHeap<E> ihe = ((InnerParallelHeap<E>) this.arr[i]);
			ihe.stopIt();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#maxLength()
	 */
	@Override
	public int maxLength() {
		int maxLength = (1 << localFirstHeapHeight) - 1;
		for (int i = (1 << localFirstHeapHeight) - 1; i < arr.length; i++) {
			maxLength += ((InnerParallelHeap) this.arr[i]).sh.maxLength();
		}
		return maxLength;
	}

	@Override
	public void clear() {
		super.clear();
		for (int i = (1 << localFirstHeapHeight) - 1; i < arr.length; i++) {
			((InnerParallelHeap) this.arr[i]).waitForEmptyInstructionQueue();
			((InnerParallelHeap) this.arr[i]).sh.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#isFull()
	 */
	@Override
	public boolean isFull() {
		return arr[0] != null && size() >= (1 << (height + 1)) - 1;
	}

	@Override
	protected boolean contains(final E ele, final int i) {
		if (ele.equals(get(i))) {
			return true;
		} else if (ele.compareTo(get(i)) < 0) {
			return false;
		} else {
			if (leftChild(i) > -1) {
				if (arr[leftChild(i)] instanceof InnerParallelHeap) {
					((InnerParallelHeap) arr[leftChild(i)])
							.waitForEmptyInstructionQueue();
					if (((InnerParallelHeap) arr[leftChild(i)]).sh
							.contains(ele))
						return true;
				} else if (contains(ele, leftChild(i)))
					return true;
			}
			if (rightChild(i) > -1) {
				if (arr[rightChild(i)] instanceof InnerParallelHeap) {
					((InnerParallelHeap) arr[rightChild(i)])
							.waitForEmptyInstructionQueue();
					if (((InnerParallelHeap) arr[rightChild(i)]).sh
							.contains(ele))
						return true;
				} else if (contains(ele, rightChild(i)))
					return true;
			}
			return false;
		}
	}

	@Override
	protected void bubbleDown(int i) {
		while (true) {
			final int left = leftChild(i);
			final int right = rightChild(i);
			int minchild;
			if (right > -1) {
				minchild = get(left).compareTo(get(right)) < 0 ? left : right;
			} else if (left > -1) {
				minchild = left;
			} else
				return;
			final E ie = get(i);
			if (ie.compareTo(get(minchild)) > 0) {
				if (arr[minchild] instanceof InnerParallelHeap) {
					arr[i] = ((InnerParallelHeap<E>) arr[minchild]).peek();
					((InnerParallelHeap<E>) arr[minchild]).bubbledown(ie);
					return;
				} else {
					swap(i, minchild);
					i = minchild;
				}
			} else
				return;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected E get(final int i) {
		if (arr[i] instanceof InnerParallelHeap)
			return ((InnerParallelHeap<E>) arr[i]).peek();
		else
			return (E) arr[i];
	}

	@Override
	protected void addByUpdating(E elem) {
		final int[] ia = getPathToFreeSpace();
		for (int index = ia[0]; index > 0; index--) {
			final int i = ia[index];
			if (arr[i] == null)
				break;
			else if (arr[i] instanceof InnerParallelHeap) {
				((InnerParallelHeap<E>) arr[i]).addByUpdating(elem);
				length++;
				return;
			} else {
				final E zelem = get(i);
				if (elem.compareTo(zelem) < 0) {
					arr[i] = elem;
					elem = zelem;
				}
			}
		}
		arr[length++] = elem;
	}

	@Override
	public void add(final E elem) {
		if (arr[0] == null) {
			if (length == 0)
				length++;
			arr[0] = elem;
			bubbleDown(0);
		} else {
			addByUpdating(elem);
		}
	}

	private class Entry implements Comparable<Entry> {
		public E e;
		public BoundedBuffer<E> bbe;

		public Entry(final E e, final BoundedBuffer<E> bbe) {
			this.e = e;
			this.bbe = bbe;
		}

		public int compareTo(final Entry o) {
			return e.compareTo(o.e);
		}

	}

	public static boolean HEAPREPLACEMENTS = false;

	private class MakeEmptyRunner extends Thread {
		private final SequentialHeap<E> she;
		private final BoundedBuffer<E> bbe;

		public MakeEmptyRunner(final SequentialHeap<E> she,
				final BoundedBuffer<E> bbe) {
			this.she = she;
			this.bbe = bbe;
		}

		@Override
		public void run() {
			if (HEAPREPLACEMENTS) {
				final SequentialHeap<E> tmpHeapReplacements = new SequentialHeap<E>(
						1024, true);
				while (she.size() > 2 * 1024) {
					for (int i = 0; i < 1024; i++) {
						tmpHeapReplacements.add((E) she.arr[--she.length]);
						she.arr[she.length] = null;
					}
					while (!tmpHeapReplacements.isEmpty()) {
						final E replacement = tmpHeapReplacements.pop();
						final E e = (E) she.arr[0];
						try {
							bbe.put(e);
						} catch (final InterruptedException e1) {
							System.err.println(e1);
							e1.printStackTrace();
						}
						she.arr[0] = replacement;
						she.bubbleDown(0);
					}
				}
			}
			E e = she.pop();
			while (e != null) {
				try {
					bbe.put(e);
				} catch (final InterruptedException e1) {
					System.err.println(e1);
					e1.printStackTrace();
				}
				e = she.pop();
			}
			bbe.endOfData();
		}

	}

	private final static int MAXBUFFER = 100;

	@Override
	public Iterator<E> emptyDatastructure() {
		if (length <= (1 << localFirstHeapHeight) - 1) {
			return new Iterator<E>() {

				public boolean hasNext() {
					return size() > 0;
				}

				public E next() {
					return pop();
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

		final int startInnerHeaps = (1 << localFirstHeapHeight) - 1;
		final int size = Math.min(arr.length - startInnerHeaps, length
				- startInnerHeaps);
		final SequentialHeap<Entry> she = new SequentialHeap<Entry>(size, true);
		final Thread[] threads = new Thread[size];
		for (int i = 0; i < size; i++) {
			final BoundedBuffer<E> bbe = new BoundedBuffer<E>(MAXBUFFER);
			threads[i] = new MakeEmptyRunner(
					((InnerParallelHeap<E>) arr[startInnerHeaps + i]).sh, bbe);
			threads[i].start();
			try {
				if (bbe.hasNext())
					she.add(new Entry(bbe.get(), bbe));
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		length = startInnerHeaps;

		return new Iterator<E>() {

			public boolean hasNext() {
				return size() > 0 || she.length > 0;
			}

			public E next() {
				if (size() > 0)
					return pop();
				else {
					if (she.length > 0) {
						final Entry entry = she.pop();
						final E e = entry.e;
						try {
							if (entry.bbe.hasNext()) {
								entry.e = entry.bbe.get();
								she.add(entry);
							}
						} catch (final InterruptedException e1) {
							System.out.println(e1);
							e1.printStackTrace();
						}
						return e;
					} else
						return null;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	@Override
	public E pop() {
		if (arr[0] == null) {
			if (length == 0 || length == 1) {
				length = 0;
				return null;
			}
			arr[0] = getLastElement();
			bubbleDown(0);
		}
		final E e = get(0);
		arr[0] = null;
		if (length == 1)
			length = 0;
		return e;
	}

	private Object getLastElement() {
		if (length - 1 < (1 << localFirstHeapHeight) - 1) {
			final Object o = arr[--length];
			arr[length] = null;
			return o;
		} else {
			final int[] ia = getPathToElement(length - 1);
			for (int index = ia[0]; index > 0; index--) {
				final int i = ia[index];
				if (arr[i] instanceof InnerParallelHeap) {
					length--;
					return ((InnerParallelHeap<E>) arr[i]).getLastElement();
				}
			}
			return null;
		}
	}
}
