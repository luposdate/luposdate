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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
public class SetImplementation<E extends Serializable> implements Set<E>, Serializable {

	private final Set<E> memorySet;
	private SortedSet<E> diskSet;

	/** Constant <code>MAXMEMORYMAPENTRIES=30000</code> */
	protected final static int MAXMEMORYMAPENTRIES = 30000;

	/**
	 * <p>Constructor for SetImplementation.</p>
	 */
	public SetImplementation() {
		this.memorySet = new HashSet<E>();
	}

	/**
	 * <p>Constructor for SetImplementation.</p>
	 *
	 * @param memorySet a {@link java.util.Set} object.
	 */
	public SetImplementation(final Set<E> memorySet) {
		this.memorySet = memorySet;
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean add(final E arg0) {
		if (this.memorySet.size() < MAXMEMORYMAPENTRIES)
			return this.memorySet.add(arg0);
		if (this.memorySet.contains(arg0))
			return false;
		if (this.diskSet == null){
			this.diskSet = new DBMergeSortedSet<E>(new SortConfiguration(), (Class<E>) arg0.getClass());
		}
		return this.diskSet.add(arg0);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag = false;
		for (final E e : arg0)
			flag = add(e) || flag;
		return flag;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.memorySet.clear();
		if (this.diskSet != null){
			this.diskSet.clear();
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final Object arg0) {
		if (this.memorySet.contains(arg0)){
			return true;
		}
		if (this.diskSet != null && this.diskSet.contains(arg0)){
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(final Collection<?> arg0) {
		for (final Object o : arg0){
			if (!contains(o)){
				return false;
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		if (!this.memorySet.isEmpty()){
			return false;
		}
		if (this.memorySet == null || this.memorySet.isEmpty()){
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Iterator<E> memoryIterator = SetImplementation.this.memorySet.iterator();
			Iterator<E> diskIterator = (SetImplementation.this.diskSet == null) ? null : SetImplementation.this.diskSet.iterator();

			@Override
			public boolean hasNext() {
				if (this.memoryIterator.hasNext()){
					return true;
				}
				if (this.diskIterator != null){
					return this.diskIterator.hasNext();
				}
				return false;
			}

			@Override
			public E next() {
				if (this.memoryIterator.hasNext()){
					return this.memoryIterator.next();
				}
				if (this.diskIterator != null && this.diskIterator.hasNext()){
					return this.diskIterator.next();
				}
				return null;
			}

			@Override
			public void remove() {
				throw (new UnsupportedOperationException(
						"This iterator does not support remove."));
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final Object arg0) {
		final boolean flag = this.memorySet.remove(arg0);
		if (flag){
			return true;
		}
		if (this.diskSet != null){
			return this.diskSet.remove(arg0);
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(final Collection<?> arg0) {
		boolean flag = false;
		for (final Object o : arg0){
			flag = remove(o) || flag;
		}
		return flag;
	}

	/** {@inheritDoc} */
	@Override
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

	/** {@inheritDoc} */
	@Override
	public int size() {
		int size = this.memorySet.size();
		if (this.diskSet != null){
			size += this.diskSet.size();
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"This set does not support toArray."));
	}

	/** {@inheritDoc} */
	@Override
	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support toArray."));
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString(){
		String result = "Set in memory: " + this.memorySet;
		if(this.diskSet!=null){
			result += "Set on disk: " + this.diskSet;
		}
		return result;
	}
}
