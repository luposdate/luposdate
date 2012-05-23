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
package lupos.datastructures.dbmergesortedds;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import lupos.datastructures.queryresult.ParallelIterator;

public class LazySortedSet<Internal, External> implements SortedSet<External> {
	public interface Converter<Internal, External> {
		public Internal extToInt(External obj);

		public External intToExt(Internal obj);
	}

	private final SortedSet<Internal> sourceSet;
	private final Converter<Internal, External> conv;

	public LazySortedSet(final SortedSet<Internal> sourceSet,
			final Converter<Internal, External> conv) {
		this.sourceSet = sourceSet;
		this.conv = conv;
	}

	public boolean add(final External o) {
		return sourceSet.add(conv.extToInt(o));
	}

	public boolean addAll(final Collection<? extends External> c) {
		boolean result = true;
		for (final External o : c)
			result = add(o) && result;
		return result;
	}

	public void clear() {
		sourceSet.clear();
	}

	public Comparator<? super External> comparator() {
		final Comparator<? super Internal> comp = sourceSet.comparator();
		if (comp == null)
			return null;
		return new Comparator<External>() {
			public int compare(final External o1, final External o2) {
				return comp.compare(conv.extToInt(o1), conv.extToInt(o2));
			}
		};
	}

	public boolean contains(final Object o) {
		return sourceSet.contains(o);
	}

	public boolean containsAll(final Collection<?> c) {
		return sourceSet.containsAll(c);
	}

	@Override
	public boolean equals(final Object o) {
		return sourceSet.equals(o);
	}

	public External first() {
		return conv.intToExt(sourceSet.first());
	}

	@Override
	public int hashCode() {
		return sourceSet.hashCode();
	}

	public SortedSet<External> headSet(final External toElement) {
		return new LazySortedSet<Internal, External>(sourceSet.headSet(conv
				.extToInt(toElement)), conv);
	}

	public boolean isEmpty() {
		return sourceSet.isEmpty();
	}

	public ParallelIterator<External> iterator() {
		final Iterator<Internal> iter = sourceSet.iterator();
		return new ParallelIterator<External>() {
			public boolean hasNext() {
				return iter.hasNext();
			}

			public External next() {
				return conv.intToExt(iter.next());
			}

			public void remove() {
				iter.remove();
			}

			@Override
			public void finalize() {
				close();
			}

			public void close() {
				if (iter instanceof ParallelIterator) {
					((ParallelIterator) iter).close();
				}
			}
		};
	}

	public External last() {
		return conv.intToExt(sourceSet.last());
	}

	@SuppressWarnings("unchecked")
	public boolean remove(final Object o) {
		return sourceSet.remove(conv.extToInt((External) o));
	}

	public boolean removeAll(final Collection<?> c) {
		return sourceSet.removeAll(c);
	}

	public boolean retainAll(final Collection<?> c) {
		return sourceSet.retainAll(c);
	}

	public int size() {
		return sourceSet.size();
	}

	public SortedSet<External> subSet(final External fromElement,
			final External toElement) {
		return new LazySortedSet<Internal, External>(sourceSet.subSet(conv
				.extToInt(fromElement), conv.extToInt(toElement)), conv);
	}

	public SortedSet<External> tailSet(final External fromElement) {
		return new LazySortedSet<Internal, External>(sourceSet.tailSet(conv
				.extToInt(fromElement)), conv);
	}

	public Object[] toArray() {
		return sourceSet.toArray();
	}

	public <T> T[] toArray(final T[] a) {
		return sourceSet.toArray(a);
	}

}
