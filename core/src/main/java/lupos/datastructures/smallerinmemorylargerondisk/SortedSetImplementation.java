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
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;

public class SortedSetImplementation<E extends Serializable> extends
		SetImplementation<E> implements SortedSet<E> {

	private final SortedSet<E> memorySet;
	private SortedSet<E> diskSet;

	private final static int HEAPHEIGHT = 5;

	protected final static int MAXMEMORYMAPENTRIES = 30000;

	public SortedSetImplementation(final SortedSet<E> memorySet) {
		this.memorySet = memorySet;
	}

	public Comparator<? super E> comparator() {
		return memorySet.comparator();
	}

	public E first() {
		final E firstMemory = memorySet.first();
		if (diskSet == null)
			return firstMemory;
		final E firstDisk = diskSet.first();
		if (firstMemory == null)
			return firstDisk;
		if (firstDisk == null)
			return firstMemory;
		return (memorySet.comparator().compare(firstMemory, firstDisk) <= 0) ? firstMemory
				: firstDisk;
	}

	public SortedSet<E> headSet(final E arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support headSet."));
	}

	public E last() {
		final E lastMemory = memorySet.last();
		if (diskSet == null)
			return lastMemory;
		final E lastDisk = diskSet.last();
		if (lastMemory == null)
			return lastDisk;
		if (lastDisk == null)
			return lastMemory;
		return (memorySet.comparator().compare(lastMemory, lastDisk) > 0) ? lastMemory
				: lastDisk;
	}

	public SortedSet<E> subSet(final E arg0, final E arg1) {
		throw (new UnsupportedOperationException(
				"This set does not support subSet."));
	}

	public SortedSet<E> tailSet(final E arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support tailSet."));
	}

	@Override
	public boolean add(final E arg0) {
		if (memorySet.size() < MAXMEMORYMAPENTRIES)
			return memorySet.add(arg0);
		if (memorySet.contains(arg0))
			return false;
		if (diskSet == null)
				diskSet = new DBMergeSortedSet<E>(HEAPHEIGHT, memorySet
						.comparator(), (Class<E>) arg0.getClass());
		return diskSet.add(arg0);
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Iterator<E> memoryIterator = memorySet.iterator();
			Iterator<E> diskIterator = (diskSet == null) ? null : diskSet
					.iterator();
			E nextMemory = null;
			E nextDisk = null;

			public boolean hasNext() {
				if (nextMemory != null || nextDisk != null)
					return true;
				if (memoryIterator.hasNext())
					return true;
				if (diskIterator != null)
					return diskIterator.hasNext();
				return false;
			}

			public E next() {
				if (nextMemory == null && memoryIterator.hasNext())
					nextMemory = memoryIterator.next();
				if (diskIterator != null) {
					if (nextDisk == null && diskIterator.hasNext())
						nextDisk = diskIterator.next();
				}
				if (nextMemory == null)
					return nextDisk;
				if (nextDisk == null)
					return nextMemory;
				if (SortedSetImplementation.this.memorySet.comparator()
						.compare(nextMemory, nextDisk) <= 0) {
					final E result = nextMemory;
					nextMemory = null;
					return result;
				} else {
					final E result = nextDisk;
					nextDisk = null;
					return result;
				}
			}

			public void remove() {
				throw (new UnsupportedOperationException(
						"This iterator does not support remove."));
			}
		};
	}
}
