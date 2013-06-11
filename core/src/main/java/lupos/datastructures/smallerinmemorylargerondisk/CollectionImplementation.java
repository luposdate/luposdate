/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.misc.util.ImmutableIterator;

public class CollectionImplementation<E> implements
		Collection<E>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5226267348532809078L;

	private int memoryLimit = 20000;

	private final Collection<E> memoryCollection = new LinkedList<E>();
	private PagedCollection<E> diskCollection = null;

	public CollectionImplementation() {

	}

	public CollectionImplementation(final int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	@Override
	public boolean add(final E arg0) {
		if (this.memoryCollection.size() + 1 < this.memoryLimit) {
			return this.memoryCollection.add(arg0);
		} else {
			if (this.diskCollection == null) {
				try {
					this.diskCollection = new PagedCollection<E>((Class<E>) arg0.getClass());
				} catch (final IOException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			return this.diskCollection.add(arg0);
		}
	}

	@Override
	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag = true;
		for (final E e : arg0) {
			flag = flag && this.add(e);
		}
		return flag;
	}

	@Override
	public void clear() {
		this.memoryCollection.clear();
		if (this.diskCollection != null) {
			try {
				this.diskCollection.release();
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			this.diskCollection = null;
		}
	}

	@Override
	public boolean contains(final Object arg0) {
		if (this.memoryCollection.contains(arg0)) {
			return true;
		}
		if (this.diskCollection != null) {
			return this.diskCollection.contains(arg0);
		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> arg0) {
		for (final Object o : arg0) {
			if (!this.contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return (this.memoryCollection.size() == 0);
	}

	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>() {
			Iterator<E> memoryIterator = CollectionImplementation.this.memoryCollection.iterator();
			Iterator<E> diskIterator = (CollectionImplementation.this.diskCollection == null) ? null
					: CollectionImplementation.this.diskCollection.iterator();

			@Override
			public boolean hasNext() {
				if (this.memoryIterator.hasNext()) {
					return true;
				}
				if (this.diskIterator != null) {
					return this.diskIterator.hasNext();
				}
				return false;
			}

			@Override
			public E next() {
				if (this.memoryIterator.hasNext()) {
					return this.memoryIterator.next();
				}
				if (this.diskIterator != null) {
					return this.diskIterator.next();
				}
				return null;
			}
		};
	}

	@Override
	public boolean remove(final Object arg0) {
		boolean flag = this.memoryCollection.remove(arg0);
		if (this.diskCollection != null) {
			flag = flag || this.diskCollection.remove(arg0);
		}
		return flag;
	}

	@Override
	public boolean removeAll(final Collection<?> arg0) {
		boolean flag = true;
		for (final Object o : arg0) {
			flag = flag && this.remove(o);
		}
		return flag;
	}

	@Override
	public boolean retainAll(final Collection<?> arg0) {
		final boolean flag = this.memoryCollection.retainAll(arg0);
		if (this.diskCollection != null) {
			return flag || this.diskCollection.retainAll(arg0);
		}
		return flag;
	}

	@Override
	public int size() {
		if (this.diskCollection != null) {
			return this.memoryCollection.size() + this.diskCollection.size();
		} else {
			return this.memoryCollection.size();
		}
	}

	@Override
	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	@Override
	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	@Override
	public String toString() {
		String s = "";
		for (final E e : this) {
			if (s.compareTo("") != 0) {
				s += ", ";
			}
			if (e != null) {
				s += e.toString();
			}
		}
		return "[ " + s + " ]";
	}

	public void release() {
		this.clear();
	}
}
