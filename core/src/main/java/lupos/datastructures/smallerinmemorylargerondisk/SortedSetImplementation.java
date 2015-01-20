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
package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.misc.util.ImmutableIterator;

public class SortedSetImplementation<E extends Serializable> extends
		SetImplementation<E> implements SortedSet<E> {

	private final SortedSet<E> memorySet;
	private SortedSet<E> diskSet;

	protected final static int MAXMEMORYMAPENTRIES = 30000;

	public SortedSetImplementation(final SortedSet<E> memorySet) {
		this.memorySet = memorySet;
	}

	@Override
	public Comparator<? super E> comparator() {
		return this.memorySet.comparator();
	}

	@Override
	public E first() {
		final E firstMemory = this.memorySet.first();
		if (this.diskSet == null) {
			return firstMemory;
		}
		final E firstDisk = this.diskSet.first();
		if (firstMemory == null) {
			return firstDisk;
		}
		if (firstDisk == null) {
			return firstMemory;
		}
		return (this.memorySet.comparator().compare(firstMemory, firstDisk) <= 0) ? firstMemory
				: firstDisk;
	}

	@Override
	public SortedSet<E> headSet(final E arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support headSet."));
	}

	@Override
	public E last() {
		final E lastMemory = this.memorySet.last();
		if (this.diskSet == null) {
			return lastMemory;
		}
		final E lastDisk = this.diskSet.last();
		if (lastMemory == null) {
			return lastDisk;
		}
		if (lastDisk == null) {
			return lastMemory;
		}
		return (this.memorySet.comparator().compare(lastMemory, lastDisk) > 0) ? lastMemory
				: lastDisk;
	}

	@Override
	public SortedSet<E> subSet(final E arg0, final E arg1) {
		throw (new UnsupportedOperationException(
				"This set does not support subSet."));
	}

	@Override
	public SortedSet<E> tailSet(final E arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support tailSet."));
	}

	@Override
	public boolean add(final E arg0) {
		if (this.memorySet.size() < MAXMEMORYMAPENTRIES) {
			return this.memorySet.add(arg0);
		}
		if (this.memorySet.contains(arg0)) {
			return false;
		}
		if (this.diskSet == null) {
			this.diskSet = new DBMergeSortedSet<E>(new SortConfiguration(), this.memorySet
					.comparator(), (Class<E>) arg0.getClass());
		}
		return this.diskSet.add(arg0);
	}

	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>() {
			Iterator<E> memoryIterator = SortedSetImplementation.this.memorySet.iterator();
			Iterator<E> diskIterator = (SortedSetImplementation.this.diskSet == null) ? null : SortedSetImplementation.this.diskSet
					.iterator();
			E nextMemory = null;
			E nextDisk = null;

			@Override
			public boolean hasNext() {
				if (this.nextMemory != null || this.nextDisk != null) {
					return true;
				}
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
				if (this.nextMemory == null && this.memoryIterator.hasNext()) {
					this.nextMemory = this.memoryIterator.next();
				}
				if (this.diskIterator != null) {
					if (this.nextDisk == null && this.diskIterator.hasNext()) {
						this.nextDisk = this.diskIterator.next();
					}
				}
				if (this.nextMemory == null) {
					return this.nextDisk;
				}
				if (this.nextDisk == null) {
					return this.nextMemory;
				}
				if (SortedSetImplementation.this.memorySet.comparator()
						.compare(this.nextMemory, this.nextDisk) <= 0) {
					final E result = this.nextMemory;
					this.nextMemory = null;
					return result;
				} else {
					final E result = this.nextDisk;
					this.nextDisk = null;
					return result;
				}
			}
		};
	}
}
