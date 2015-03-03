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
package lupos.misc.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.SortedSet;

/**
 * This class provides a skeletal implementation of the <tt>Set</tt>
 * interface to minimize the effort required to implement this
 * interface. <p>
 *
 * @param <T> the type of elements maintained by this sorted set
 * @author groppe
 * @version $Id: $Id
 */
public abstract class AbstractSortedSet<T> extends AbstractSet<T> implements SortedSet<T>{

	/** {@inheritDoc} */
	@Override
	public SortedSet<T> subSet(T fromElement, T toElement) {
		return this.subSet(fromElement, toElement, false);
	}

	/**
	 * Returns a view of the portion of this set whose elements range from fromElement, inclusive, to toElement (inclusive if inclusiveLastElement is true, otherwise exclusive).
	 * (If fromElement and toElement are equal and inclusiveLastElement is false, the returned set is empty.)
	 * The returned set is backed by this set, so changes in the returned set are reflected in this set, and vice-versa.
	 * The returned set supports all optional set operations that this set supports.
	 *
	 * The returned set will throw an IllegalArgumentException on an attempt to insert an element outside its range.
	 *
	 * @param fromElement low endpoint (inclusive) of the returned set
	 * @param toElement high endpoint of the returned set
	 * @param inclusiveLastElement true for an inclusive high endpoint, otherwise (for an exclusive high endpoint) false
	 * @return the backed set
	 */
	public abstract SortedSet<T> subSet(T fromElement, T toElement, boolean inclusiveLastElement);

	/** {@inheritDoc} */
	@Override
	public SortedSet<T> headSet(T toElement) {
		return this.subSet(this.first(), toElement);
	}

	/** {@inheritDoc} */
	@Override
	public SortedSet<T> tailSet(T fromElement) {
		return this.subSet(fromElement, this.last(), true);
	}

	/** {@inheritDoc} */
	@Override
	public T first() {
		Iterator<T> it = this.iterator();
		if(!it.hasNext()){
			return null;
		}
		return it.next();	
	}

	/** {@inheritDoc} */
	@Override
	public T last() {
		// This implementation is not efficient!
		// Please override this method if
		// determining the last key can be done in a more efficient way than iterating
		// through all keys...
		T last = null;
		Iterator<T> it = this.iterator();
		while(it.hasNext()){
			last = it.next();
		}
		return last;
	}
}
