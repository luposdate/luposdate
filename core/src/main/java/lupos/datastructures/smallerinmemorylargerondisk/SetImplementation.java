/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.SortConfiguration;

public class SetImplementation<E extends Serializable> implements Set<E> {

	private final Set<E> memorySet;
	private SortedSet<E> diskSet;

	protected final static int MAXMEMORYMAPENTRIES = 30000;

	public SetImplementation() {
		this.memorySet = new HashSet<E>();
	}

	public SetImplementation(final Set<E> memorySet) {
		this.memorySet = memorySet;
	}

	public boolean add(final E arg0) {
		if (memorySet.size() < MAXMEMORYMAPENTRIES)
			return memorySet.add(arg0);
		if (memorySet.contains(arg0))
			return false;
		if (diskSet == null)
				diskSet = new DBMergeSortedSet<E>(new SortConfiguration(), (Class<E>) arg0.getClass());
		return diskSet.add(arg0);
	}

	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag = false;
		for (final E e : arg0)
			flag = flag || add(e);
		return flag;
	}

	public void clear() {
		memorySet.clear();
		if (diskSet != null)
			diskSet.clear();
	}

	public boolean contains(final Object arg0) {
		if (memorySet.contains(arg0))
			return true;
		if (diskSet != null && diskSet.contains(arg0))
			return true;
		return false;
	}

	public boolean containsAll(final Collection<?> arg0) {
		for (final Object o : arg0)
			if (!contains(o))
				return false;
		return true;
	}

	public boolean isEmpty() {
		if (!memorySet.isEmpty())
			return false;
		if (memorySet == null || memorySet.isEmpty())
			return true;
		return false;
	}

	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Iterator<E> memoryIterator = memorySet.iterator();
			Iterator<E> diskIterator = (diskSet == null) ? null : diskSet
					.iterator();

			public boolean hasNext() {
				if (memoryIterator.hasNext())
					return true;
				if (diskIterator != null)
					return diskIterator.hasNext();
				return false;
			}

			public E next() {
				if (memoryIterator.hasNext())
					return memoryIterator.next();
				if (diskIterator != null && diskIterator.hasNext())
					return diskIterator.next();
				return null;
			}

			public void remove() {
				throw (new UnsupportedOperationException(
						"This iterator does not support remove."));
			}
		};
	}

	public boolean remove(final Object arg0) {
		final boolean flag = memorySet.remove(arg0);
		if (flag)
			return true;
		if (diskSet != null)
			return diskSet.remove(arg0);
		return false;
	}

	public boolean removeAll(final Collection<?> arg0) {
		boolean flag = false;
		for (final Object o : arg0)
			flag = flag || remove(o);
		return flag;
	}

	public boolean retainAll(final Collection<?> arg0) {
		boolean flag = false;
		for (final E e : this) {
			if (!arg0.contains(e)) {
				flag = true;
				remove(e);
			}
		}
		return flag;
	}

	public int size() {
		int size = memorySet.size();
		if (diskSet != null)
			size += diskSet.size();
		return size;
	}

	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"This set does not support toArray."));
	}

	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support toArray."));
	}
}
