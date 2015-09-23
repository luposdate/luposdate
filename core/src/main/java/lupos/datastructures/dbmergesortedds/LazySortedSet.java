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

	/**
	 * <p>Constructor for LazySortedSet.</p>
	 *
	 * @param sourceSet a {@link java.util.SortedSet} object.
	 * @param conv a {@link lupos.datastructures.dbmergesortedds.LazySortedSet.Converter} object.
	 */
	public LazySortedSet(final SortedSet<Internal> sourceSet,
			final Converter<Internal, External> conv) {
		this.sourceSet = sourceSet;
		this.conv = conv;
	}

	/**
	 * <p>add.</p>
	 *
	 * @param o a External object.
	 * @return a boolean.
	 */
	public boolean add(final External o) {
		return sourceSet.add(conv.extToInt(o));
	}

	/** {@inheritDoc} */
	public boolean addAll(final Collection<? extends External> c) {
		boolean result = true;
		for (final External o : c)
			result = add(o) && result;
		return result;
	}

	/**
	 * <p>clear.</p>
	 */
	public void clear() {
		sourceSet.clear();
	}

	/**
	 * <p>comparator.</p>
	 *
	 * @return a {@link java.util.Comparator} object.
	 */
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

	/** {@inheritDoc} */
	public boolean contains(final Object o) {
		return sourceSet.contains(o);
	}

	/** {@inheritDoc} */
	public boolean containsAll(final Collection<?> c) {
		return sourceSet.containsAll(c);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		return sourceSet.equals(o);
	}

	/**
	 * <p>first.</p>
	 *
	 * @return a External object.
	 */
	public External first() {
		return conv.intToExt(sourceSet.first());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return sourceSet.hashCode();
	}

	/**
	 * <p>headSet.</p>
	 *
	 * @param toElement a External object.
	 * @return a {@link java.util.SortedSet} object.
	 */
	public SortedSet<External> headSet(final External toElement) {
		return new LazySortedSet<Internal, External>(sourceSet.headSet(conv
				.extToInt(toElement)), conv);
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty() {
		return sourceSet.isEmpty();
	}

	/**
	 * <p>iterator.</p>
	 *
	 * @return a {@link lupos.datastructures.queryresult.ParallelIterator} object.
	 */
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

	/**
	 * <p>last.</p>
	 *
	 * @return a External object.
	 */
	public External last() {
		return conv.intToExt(sourceSet.last());
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public boolean remove(final Object o) {
		return sourceSet.remove(conv.extToInt((External) o));
	}

	/** {@inheritDoc} */
	public boolean removeAll(final Collection<?> c) {
		return sourceSet.removeAll(c);
	}

	/** {@inheritDoc} */
	public boolean retainAll(final Collection<?> c) {
		return sourceSet.retainAll(c);
	}

	/**
	 * <p>size.</p>
	 *
	 * @return a int.
	 */
	public int size() {
		return sourceSet.size();
	}

	/**
	 * <p>subSet.</p>
	 *
	 * @param fromElement a External object.
	 * @param toElement a External object.
	 * @return a {@link java.util.SortedSet} object.
	 */
	public SortedSet<External> subSet(final External fromElement,
			final External toElement) {
		return new LazySortedSet<Internal, External>(sourceSet.subSet(conv
				.extToInt(fromElement), conv.extToInt(toElement)), conv);
	}

	/**
	 * <p>tailSet.</p>
	 *
	 * @param fromElement a External object.
	 * @return a {@link java.util.SortedSet} object.
	 */
	public SortedSet<External> tailSet(final External fromElement) {
		return new LazySortedSet<Internal, External>(sourceSet.tailSet(conv
				.extToInt(fromElement)), conv);
	}

	/**
	 * <p>toArray.</p>
	 *
	 * @return an array of {@link java.lang.Object} objects.
	 */
	public Object[] toArray() {
		return sourceSet.toArray();
	}

	/**
	 * <p>toArray.</p>
	 *
	 * @param a an array of T objects.
	 * @param <T> a T object.
	 * @return an array of T objects.
	 */
	public <T> T[] toArray(final T[] a) {
		return sourceSet.toArray(a);
	}

}
