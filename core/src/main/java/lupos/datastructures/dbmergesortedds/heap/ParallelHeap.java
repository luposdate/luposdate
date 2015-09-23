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
package lupos.datastructures.dbmergesortedds.heap;

import java.util.Iterator;

import lupos.datastructures.parallel.BoundedBuffer;
import lupos.misc.util.ImmutableIterator;
public class ParallelHeap<E extends Comparable<E>> extends
		OptimizedSequentialHeap<E> {

	/** Constant <code>firstHeapHeight=2</code> */
	protected static final int firstHeapHeight = 2;
	protected int localFirstHeapHeight = firstHeapHeight;

	/**
	 * <p>Constructor for ParallelHeap.</p>
	 *
	 * @param height a int.
	 */
	public ParallelHeap(final int height) {
		super(firstHeapHeight);
		this.height = height;
		this.initParallelHeaps();
	}

	/**
	 * <p>Constructor for ParallelHeap.</p>
	 *
	 * @param height a int.
	 * @param localFirstHeapHeight a int.
	 */
	public ParallelHeap(final int height, final int localFirstHeapHeight) {
		super(localFirstHeapHeight);
		this.localFirstHeapHeight = localFirstHeapHeight;
		this.height = height;
		this.initParallelHeaps();
	}

	/**
	 * <p>Constructor for ParallelHeap.</p>
	 *
	 * @param content an array of {@link java.lang.Object} objects.
	 * @param size a int.
	 */
	public ParallelHeap(final Object[] content, final int size) {
		super(content, size);
		this.length = (content[0] == null && size > 0) ? size + 1 : size;
		this.cloneParallelHeaps(content);
		this.determineHeight(this.maxLength());
	}

	/**
	 * <p>Constructor for ParallelHeap.</p>
	 *
	 * @param content an array of {@link java.lang.Object} objects.
	 * @param size a int.
	 * @param localFirstHeapHeight a int.
	 */
	public ParallelHeap(final Object[] content, final int size,
			final int localFirstHeapHeight) {
		super(content, size);
		this.localFirstHeapHeight = localFirstHeapHeight;
		this.length = (content[0] == null && size > 0) ? size + 1 : size;
		this.cloneParallelHeaps(content);
		this.determineHeight(this.maxLength());
	}

	/**
	 * <p>Constructor for ParallelHeap.</p>
	 *
	 * @param length_or_height a int.
	 * @param length a boolean.
	 */
	public ParallelHeap(final int length_or_height, final boolean length) {
		super(firstHeapHeight);
		if (length) {
			this.determineHeight(length_or_height);
		} else {
			this.height = length_or_height < 3 ? 3 : length_or_height;
		}
		this.initParallelHeaps();
	}

	/**
	 * <p>Constructor for ParallelHeap.</p>
	 *
	 * @param length_or_height a int.
	 * @param length a boolean.
	 * @param localFirstHeapHeight a int.
	 */
	public ParallelHeap(final int length_or_height, final boolean length,
			final int localFirstHeapHeight) {
		super(localFirstHeapHeight);
		this.localFirstHeapHeight = localFirstHeapHeight;
		if (length) {
			this.determineHeight(length_or_height);
		} else {
			this.height = length_or_height < 3 ? 3 : length_or_height;
		}
		this.initParallelHeaps();
	}

	/**
	 * <p>initParallelHeaps.</p>
	 */
	protected void initParallelHeaps() {
		for (int i = (1 << this.localFirstHeapHeight) - 1; i < this.arr.length; i++) {
			final InnerParallelHeap<E> ihe_new = new InnerParallelHeap<E>(
					new SequentialHeap<E>(this.height - this.localFirstHeapHeight));
			this.arr[i] = ihe_new;
			ihe_new.start();
		}
	}

	/**
	 * <p>cloneParallelHeaps.</p>
	 *
	 * @param content an array of {@link java.lang.Object} objects.
	 */
	protected void cloneParallelHeaps(final Object[] content) {
		for (int i = (1 << this.localFirstHeapHeight) - 1; i < this.arr.length; i++) {
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

	/** {@inheritDoc} */
	@Override
	public void release() {
		for (int i = (1 << this.localFirstHeapHeight) - 1; i < this.arr.length; i++) {
			final InnerParallelHeap<E> ihe = ((InnerParallelHeap<E>) this.arr[i]);
			ihe.stopIt();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#maxLength()
	 */
	/** {@inheritDoc} */
	@Override
	public int maxLength() {
		int maxLength = (1 << this.localFirstHeapHeight) - 1;
		for (int i = (1 << this.localFirstHeapHeight) - 1; i < this.arr.length; i++) {
			maxLength += ((InnerParallelHeap) this.arr[i]).sh.maxLength();
		}
		return maxLength;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		super.clear();
		for (int i = (1 << this.localFirstHeapHeight) - 1; i < this.arr.length; i++) {
			((InnerParallelHeap) this.arr[i]).waitForEmptyInstructionQueue();
			((InnerParallelHeap) this.arr[i]).sh.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#isFull()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFull() {
		return this.arr[0] != null && this.size() >= (1 << (this.height + 1)) - 1;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean contains(final E ele, final int i) {
		if (ele.equals(this.get(i))) {
			return true;
		} else if (ele.compareTo(this.get(i)) < 0) {
			return false;
		} else {
			if (this.leftChild(i) > -1) {
				if (this.arr[this.leftChild(i)] instanceof InnerParallelHeap) {
					((InnerParallelHeap) this.arr[this.leftChild(i)])
							.waitForEmptyInstructionQueue();
					if (((InnerParallelHeap) this.arr[this.leftChild(i)]).sh
							.contains(ele)) {
						return true;
					}
				} else if (this.contains(ele, this.leftChild(i))) {
					return true;
				}
			}
			if (this.rightChild(i) > -1) {
				if (this.arr[this.rightChild(i)] instanceof InnerParallelHeap) {
					((InnerParallelHeap) this.arr[this.rightChild(i)])
							.waitForEmptyInstructionQueue();
					if (((InnerParallelHeap) this.arr[this.rightChild(i)]).sh
							.contains(ele)) {
						return true;
					}
				} else if (this.contains(ele, this.rightChild(i))) {
					return true;
				}
			}
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void bubbleDown(int i) {
		while (true) {
			final int left = this.leftChild(i);
			final int right = this.rightChild(i);
			int minchild;
			if (right > -1) {
				minchild = this.get(left).compareTo(this.get(right)) < 0 ? left : right;
			} else if (left > -1) {
				minchild = left;
			} else {
				return;
			}
			final E ie = this.get(i);
			if (ie.compareTo(this.get(minchild)) > 0) {
				if (this.arr[minchild] instanceof InnerParallelHeap) {
					this.arr[i] = ((InnerParallelHeap<E>) this.arr[minchild]).peek();
					((InnerParallelHeap<E>) this.arr[minchild]).bubbledown(ie);
					return;
				} else {
					this.swap(i, minchild);
					i = minchild;
				}
			} else {
				return;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected E get(final int i) {
		if (this.arr[i] instanceof InnerParallelHeap) {
			return ((InnerParallelHeap<E>) this.arr[i]).peek();
		} else {
			return (E) this.arr[i];
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void addByUpdating(E elem) {
		final int[] ia = this.getPathToFreeSpace();
		for (int index = ia[0]; index > 0; index--) {
			final int i = ia[index];
			if (this.arr[i] == null) {
				break;
			} else if (this.arr[i] instanceof InnerParallelHeap) {
				((InnerParallelHeap<E>) this.arr[i]).addByUpdating(elem);
				this.length++;
				return;
			} else {
				final E zelem = this.get(i);
				if (elem.compareTo(zelem) < 0) {
					this.arr[i] = elem;
					elem = zelem;
				}
			}
		}
		this.arr[this.length++] = elem;
	}

	/** {@inheritDoc} */
	@Override
	public void add(final E elem) {
		if (this.arr[0] == null) {
			if (this.length == 0) {
				this.length++;
			}
			this.arr[0] = elem;
			this.bubbleDown(0);
		} else {
			this.addByUpdating(elem);
		}
	}

	private class Entry implements Comparable<Entry> {
		public E e;
		public BoundedBuffer<E> bbe;

		public Entry(final E e, final BoundedBuffer<E> bbe) {
			this.e = e;
			this.bbe = bbe;
		}

		@Override
		public int compareTo(final Entry o) {
			return this.e.compareTo(o.e);
		}

	}

	/** Constant <code>HEAPREPLACEMENTS=false</code> */
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
				while (this.she.size() > 2 * 1024) {
					for (int i = 0; i < 1024; i++) {
						tmpHeapReplacements.add((E) this.she.arr[--this.she.length]);
						this.she.arr[this.she.length] = null;
					}
					while (!tmpHeapReplacements.isEmpty()) {
						final E replacement = tmpHeapReplacements.pop();
						final E e = (E) this.she.arr[0];
						try {
							this.bbe.put(e);
						} catch (final InterruptedException e1) {
							System.err.println(e1);
							e1.printStackTrace();
						}
						this.she.arr[0] = replacement;
						this.she.bubbleDown(0);
					}
				}
			}
			E e = this.she.pop();
			while (e != null) {
				try {
					this.bbe.put(e);
				} catch (final InterruptedException e1) {
					System.err.println(e1);
					e1.printStackTrace();
				}
				e = this.she.pop();
			}
			this.bbe.endOfData();
		}

	}

	private final static int MAXBUFFER = 100;

	/** {@inheritDoc} */
	@Override
	public Iterator<E> emptyDatastructure() {
		if (this.length <= (1 << this.localFirstHeapHeight) - 1) {
			return new ImmutableIterator<E>() {

				@Override
				public boolean hasNext() {
					return ParallelHeap.this.size() > 0;
				}

				@Override
				public E next() {
					return ParallelHeap.this.pop();
				}
			};
		}

		final int startInnerHeaps = (1 << this.localFirstHeapHeight) - 1;
		final int size = Math.min(this.arr.length - startInnerHeaps, this.length
				- startInnerHeaps);
		final SequentialHeap<Entry> she = new SequentialHeap<Entry>(size, true);
		final Thread[] threads = new Thread[size];
		for (int i = 0; i < size; i++) {
			final BoundedBuffer<E> bbe = new BoundedBuffer<E>(MAXBUFFER);
			threads[i] = new MakeEmptyRunner(
					((InnerParallelHeap<E>) this.arr[startInnerHeaps + i]).sh, bbe);
			threads[i].start();
			try {
				if (bbe.hasNext()) {
					she.add(new Entry(bbe.get(), bbe));
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		this.length = startInnerHeaps;

		return new ImmutableIterator<E>() {

			@Override
			public boolean hasNext() {
				return ParallelHeap.this.size() > 0 || she.length > 0;
			}

			@Override
			public E next() {
				if (ParallelHeap.this.size() > 0) {
					return ParallelHeap.this.pop();
				} else {
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
					} else {
						return null;
					}
				}
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public E pop() {
		if (this.arr[0] == null) {
			if (this.length == 0 || this.length == 1) {
				this.length = 0;
				return null;
			}
			this.arr[0] = this.getLastElement();
			this.bubbleDown(0);
		}
		final E e = this.get(0);
		this.arr[0] = null;
		if (this.length == 1) {
			this.length = 0;
		}
		return e;
	}

	private Object getLastElement() {
		if (this.length - 1 < (1 << this.localFirstHeapHeight) - 1) {
			final Object o = this.arr[--this.length];
			this.arr[this.length] = null;
			return o;
		} else {
			final int[] ia = this.getPathToElement(this.length - 1);
			for (int index = ia[0]; index > 0; index--) {
				final int i = ia[index];
				if (this.arr[i] instanceof InnerParallelHeap) {
					this.length--;
					return ((InnerParallelHeap<E>) this.arr[i]).getLastElement();
				}
			}
			return null;
		}
	}
}
