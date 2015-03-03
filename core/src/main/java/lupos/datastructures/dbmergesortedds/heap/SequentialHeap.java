
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
package lupos.datastructures.dbmergesortedds.heap;

import java.util.Arrays;
public class SequentialHeap<E extends Comparable<E>> extends Heap<E> {
	protected int length = 0;
	protected Object[] arr;
	protected int height;

	/**
	 * <p>Constructor for SequentialHeap.</p>
	 *
	 * @param height a int.
	 */
	public SequentialHeap(final int height) {
		this.arr = new Object[(1 << (height + 1)) - 1];
		this.height = height;
	}

	/**
	 * <p>Constructor for SequentialHeap.</p>
	 *
	 * @param arr an array of {@link java.lang.Object} objects.
	 * @param length a int.
	 */
	public SequentialHeap(final Object[] arr, final int length) {
		this.arr = new Object[arr.length];
		this.length = (length < arr.length) ? length : arr.length;
		System.arraycopy(arr, 0, this.arr, 0, this.length);
		determineHeight(arr.length);
	}

	/**
	 * <p>Constructor for SequentialHeap.</p>
	 *
	 * @param length_or_height a int.
	 * @param length a boolean.
	 */
	public SequentialHeap(final int length_or_height, final boolean length) {
		if (length) {
			this.arr = new Object[length_or_height];
			determineHeight(length_or_height);
		} else {
			this.arr = new Object[(1 << (length_or_height + 1)) - 1];
			this.height = length_or_height;
		}
	}

	/**
	 * <p>determineHeight.</p>
	 *
	 * @param length a int.
	 */
	protected void determineHeight(final int length) {
		this.height = 0;
		while (((1 << (this.height + 1)) - 1) <= length) {
			this.height++;
		}
		this.height++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#maxLength()
	 */
	/** {@inheritDoc} */
	@Override
	public int maxLength() {
		return arr.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#clear()
	 */
	/** {@inheritDoc} */
	@Override
	public void clear() {
		length = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#isFull()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isFull() {
		return size() == arr.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#peek()
	 */
	/** {@inheritDoc} */
	@Override
	public E peek() {
		return get(0);
	}

	/**
	 * <p>peekObject.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object peekObject() {
		return arr[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#pop()
	 */
	/** {@inheritDoc} */
	@Override
	public E pop() {
		if (length == 0)
			return null;
		else
			return deleteIndex(0);
	}

	/**
	 * <p>deleteIndex.</p>
	 *
	 * @param i a int.
	 * @return a E object.
	 */
	protected E deleteIndex(final int i) {
		if (i < 0)
			return null;
		final E result = get(i);
		arr[i] = arr[--length];
		arr[length] = null;
		bubbleDown(i);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#delete(E)
	 */
	/**
	 * <p>delete.</p>
	 *
	 * @param e a E object.
	 * @return a E object.
	 */
	public E delete(final E e) {
		return deleteIndex(indexOf(e, 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#add(E)
	 */
	/** {@inheritDoc} */
	@Override
	public void add(final E elem) {
		arr[length] = elem;
		bubbleUp(length++);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#contains(E)
	 */
	/**
	 * <p>contains.</p>
	 *
	 * @param ele a E object.
	 * @return a boolean.
	 */
	public boolean contains(final E ele) {
		if (length == 0)
			return false;
		else
			return contains(ele, 0);
	}

	/**
	 * <p>contains.</p>
	 *
	 * @param ele a E object.
	 * @param i a int.
	 * @return a boolean.
	 */
	protected boolean contains(final E ele, final int i) {
		if (ele.equals(get(i))) {
			return true;
		} else if (ele.compareTo(get(i)) < 0) {
			return false;
		} else
			return leftChild(i) > -1 && contains(ele, leftChild(i))
					|| rightChild(i) > -1 && contains(ele, rightChild(i));
	}

	/**
	 * <p>indexOf.</p>
	 *
	 * @param ele a E object.
	 * @param i a int.
	 * @return a int.
	 */
	protected int indexOf(final E ele, final int i) {
		if (ele.compareTo(get(i)) == 0)
			return i;
		else if (ele.compareTo(get(i)) < 0)
			return -1;
		else {
			if (leftChild(i) > -1) {
				final int j = indexOf(ele, leftChild(i));
				if (j >= 0)
					return j;
			}
			if (rightChild(i) > -1) {
				final int j = indexOf(ele, rightChild(i));
				if (j >= 0)
					return j;
			}
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#isEmpty()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return length == 0;
	}

	/**
	 * <p>buildHeap.</p>
	 */
	protected void buildHeap() {
		for (int i = length / 2 - 1; i >= 0; i--) {
			bubbleDown(i);
		}
	}

	/**
	 * <p>bubbleDown.</p>
	 *
	 * @param i a int.
	 */
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
			if (get(i).compareTo(get(minchild)) > 0) {
				swap(i, minchild);
				i = minchild;
			} else
				return;
		}
	}

	/**
	 * <p>bubbleUp.</p>
	 *
	 * @param i a int.
	 */
	protected void bubbleUp(int i) {
		while (true) {
			if (i == 0)
				return;
			final int p = parent(i);
			if (get(i).compareTo(get(p)) < 0) {
				swap(i, p);
				i = p;
			} else
				return;
		}
	}

	/**
	 * <p>swap.</p>
	 *
	 * @param i a int.
	 * @param j a int.
	 */
	protected void swap(final int i, final int j) {
		final E tmp = get(i);
		arr[i] = arr[j];
		arr[j] = tmp;
	}

	/**
	 * <p>get.</p>
	 *
	 * @param i a int.
	 * @return a E object.
	 */
	@SuppressWarnings("unchecked")
	protected E get(final int i) {
		return (E) arr[i];
	}

	/**
	 * <p>parent.</p>
	 *
	 * @param i a int.
	 * @return a int.
	 */
	protected int parent(final int i) {
		if (i <= 0)
			return -1;
		return (i - 1) / 2;
	}

	/**
	 * <p>leftChild.</p>
	 *
	 * @param i a int.
	 * @return a int.
	 */
	protected int leftChild(final int i) {
		final int c = 2 * i + 1;
		if (c < length)
			return c;
		else
			return -1;
	}

	/**
	 * <p>rightChild.</p>
	 *
	 * @param i a int.
	 * @return a int.
	 */
	protected int rightChild(final int i) {
		final int c = 2 * i + 2;
		if (c < length)
			return c;
		else
			return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lupos.datastructures.dbmergesortedds.HeapInterface#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Arrays.toString(arr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lupos.datastructures.dbmergesortedds.HeapInterface#checkHeapProperty()
	 */
	/**
	 * <p>checkHeapProperty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean checkHeapProperty() {
		for (int i = 0; i < length; i++) {
			final int leftChild = leftChild(i);
			if (leftChild >= 0 && get(i).compareTo(get(leftChild)) > 0) {
				System.err.println("Heap property violated!");
				return false;
			}
			final int rightChild = rightChild(i);
			if (rightChild >= 0 && get(i).compareTo(get(rightChild)) > 0) {
				System.err.println("Heap property violated!");
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected Object[] getContent() {
		return arr;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return length;
	}

	/**
	 * <p>addByUpdating.</p>
	 *
	 * @param elem a E object.
	 */
	protected void addByUpdating(E elem) {
		final int[] ia = getPathToFreeSpace();
		for (int index = ia[0]; index > 0; index--) {
			final int i = ia[index];
			if (arr[i] == null)
				break;
			final E zelem = get(i);
			if (elem.compareTo(zelem) < 0) {
				arr[i] = elem;
				elem = zelem;
			}
		}
		arr[length++] = elem;
	}

	/**
	 * <p>getPathToFreeSpace.</p>
	 *
	 * @return an array of int.
	 */
	protected int[] getPathToFreeSpace() {
		return getPathToElement(length);
	}

	/**
	 * <p>getPathToElement.</p>
	 *
	 * @param i a int.
	 * @return an array of int.
	 */
	protected int[] getPathToElement(int i) {
		final int[] path = new int[height + 2];
		for (int index = 1; index < path.length; index++) {
			path[index] = i;
			i = parent(i);
			if (i < 0) {
				path[0] = index;
				return path;
			}
		}
		path[0] = height;
		return path;
	}

	/**
	 * <p>updateAfterBecomingSmaller.</p>
	 *
	 * @param elem a E object.
	 */
	public void updateAfterBecomingSmaller(final E elem) {
		int i = 0;
		for (; i < length; i++) {
			if (arr[i].equals(elem))
				break;
		}
		if (i < length)
			this.bubbleUp(i);
	}
}
