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
/**
 * 
 */
package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * <p>SortedBag interface.</p>
 *
 * @author groppe
 * @version $Id: $Id
 */
public interface SortedBag<E> extends Collection<E> { 

	/**
	 * <p>comparator.</p>
	 *
	 * @return a {@link java.util.Comparator} object.
	 */
	public Comparator<? super E> comparator();
	/**
	 * <p>first.</p>
	 *
	 * @return a E object.
	 */
	public E first();
	/**
	 * <p>headBag.</p>
	 *
	 * @param toElement a E object.
	 * @return a {@link lupos.datastructures.sorteddata.SortedBag} object.
	 */
	public SortedBag<E> headBag(E toElement);
	/**
	 * <p>last.</p>
	 *
	 * @return a E object.
	 */
	public E last();
	/**
	 * <p>subBag.</p>
	 *
	 * @param fromElement a E object.
	 * @param toElement a E object.
	 * @return a {@link lupos.datastructures.sorteddata.SortedBag} object.
	 */
	public SortedBag<E> subBag(E fromElement, E toElement);
	/**
	 * <p>tailBag.</p>
	 *
	 * @param fromElement a E object.
	 * @return a {@link lupos.datastructures.sorteddata.SortedBag} object.
	 */
	public SortedBag<E> tailBag(E fromElement);
	/**
	 * <p>add.</p>
	 *
	 * @param e a E object.
	 * @return a boolean.
	 */
	public boolean add(E e);
	/** {@inheritDoc} */
	public boolean addAll(Collection<? extends E> c);
	/**
	 * <p>clear.</p>
	 */
	public void clear();
	/** {@inheritDoc} */
	public boolean contains(Object o);
	/** {@inheritDoc} */
	public boolean containsAll(Collection<?> c);
	/**
	 * <p>isEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isEmpty();
	/**
	 * <p>iterator.</p>
	 *
	 * @return a {@link java.util.Iterator} object.
	 */
	public Iterator<E> iterator();
	/** {@inheritDoc} */
	public boolean remove(Object o);
	/** {@inheritDoc} */
	public boolean removeAll(Collection<?> c);
	/** {@inheritDoc} */
	public boolean retainAll(Collection<?> c);
	/**
	 * <p>size.</p>
	 *
	 * @return a int.
	 */
	public int size();
	/**
	 * <p>toArray.</p>
	 *
	 * @return an array of {@link java.lang.Object} objects.
	 */
	public Object[] toArray();
	/**
	 * <p>toArray.</p>
	 *
	 * @param a an array of T objects.
	 * @param <T> a T object.
	 * @return an array of T objects.
	 */
	public <T> T[] toArray(T[] a);	
}
