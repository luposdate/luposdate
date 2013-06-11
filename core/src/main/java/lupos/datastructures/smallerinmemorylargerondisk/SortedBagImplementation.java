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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.sorteddata.SortedBag;
import lupos.misc.util.ImmutableIterator;

public class SortedBagImplementation<E extends Serializable> implements SortedBag<E>{

	private final SortedBag<E> memoryBag;
	private SortedBag<E> diskBag;

	protected final static int MAXMEMORYMAPENTRIES=30000;

	public SortedBagImplementation(final SortedBag<E> memoryBag){
		this.memoryBag=memoryBag;
	}

	@Override
	public Comparator<? super E> comparator() {
		return this.memoryBag.comparator();
	}

	@Override
	public E first() {
		final E firstMemory=this.memoryBag.first();
		if(this.diskBag==null) {
			return firstMemory;
		}
		final E firstDisk=this.diskBag.first();
		if(firstMemory==null) {
			return firstDisk;
		}
		if(firstDisk==null) {
			return firstMemory;
		}
		return (this.memoryBag.comparator().compare(firstMemory,firstDisk)<=0)?firstMemory:firstDisk;
	}

	@Override
	public SortedBag<E> headBag(final E arg0) {
		throw(new UnsupportedOperationException("This Bag does not support headBag."));
	}

	@Override
	public E last() {
		final E lastMemory=this.memoryBag.last();
		if(this.diskBag==null) {
			return lastMemory;
		}
		final E lastDisk=this.diskBag.last();
		if(lastMemory==null) {
			return lastDisk;
		}
		if(lastDisk==null) {
			return lastMemory;
		}
		return (this.memoryBag.comparator().compare(lastMemory,lastDisk)>0)?lastMemory:lastDisk;
	}

	@Override
	public SortedBag<E> subBag(final E arg0, final E arg1) {
		throw(new UnsupportedOperationException("This Bag does not support subBag."));
	}

	@Override
	public SortedBag<E> tailBag(final E arg0) {
		throw(new UnsupportedOperationException("This Bag does not support tailBag."));
	}

	@Override
	public boolean add(final E arg0) {
		if(this.memoryBag.size()<MAXMEMORYMAPENTRIES) {
			return this.memoryBag.add(arg0);
		}
		if(this.memoryBag.contains(arg0)) {
			return false;
		}
		if(this.diskBag==null) {
			this.diskBag=new DBMergeSortedBag<E>(new SortConfiguration(), this.memoryBag.comparator(),(Class<E>)arg0.getClass());
		}
		return this.diskBag.add(arg0);
	}

	@Override
	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag=false;
		for(final E e:arg0) {
			flag = flag || this.add(e);
		}
		return flag;
	}

	@Override
	public void clear() {
		this.memoryBag.clear();
		if(this.diskBag!=null) {
			this.diskBag.clear();
		}
	}

	@Override
	public boolean contains(final Object arg0) {
		if(this.memoryBag.contains(arg0)) {
			return true;
		}
		if(this.diskBag!=null && this.diskBag.contains(arg0)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> arg0) {
		for(final Object o:arg0) {
			if(!this.contains(o)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		if(!this.memoryBag.isEmpty()) {
			return false;
		}
		if(this.memoryBag==null || this.memoryBag.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>(){
			Iterator<E> memoryIterator = SortedBagImplementation.this.memoryBag.iterator();
			Iterator<E> diskIterator = (SortedBagImplementation.this.diskBag==null)? null:SortedBagImplementation.this.diskBag.iterator();
			E nextMemory=null;
			E nextDisk=null;
			@Override
			public boolean hasNext() {
				if(this.nextMemory!=null || this.nextDisk!=null) {
					return true;
				}
				if(this.memoryIterator.hasNext()) {
					return true;
				}
				if(this.diskIterator!=null) {
					return this.diskIterator.hasNext();
				}
				return false;
			}
			@Override
			public E next() {
				if(this.nextMemory==null && this.memoryIterator.hasNext()) {
					this.nextMemory=this.memoryIterator.next();
				}
				if(this.diskIterator!=null){
					if(this.nextDisk==null && this.diskIterator.hasNext()) {
						this.nextDisk=this.diskIterator.next();
					}
				}
				if(this.nextMemory==null) {
					return this.nextDisk;
				}
				if(this.nextDisk==null) {
					return this.nextMemory;
				}
				if(SortedBagImplementation.this.memoryBag.comparator().compare(this.nextMemory, this.nextDisk)<=0){
					final E result=this.nextMemory;
					this.nextMemory=null;
					return result;
				}
				else {
					final E result=this.nextDisk;
					this.nextDisk=null;
					return result;
				}
			}
		};
	}

	@Override
	public boolean remove(final Object arg0) {
		final boolean flag=this.memoryBag.remove(arg0);
		if(flag) {
			return true;
		}
		if(this.diskBag!=null) {
			return this.diskBag.remove(arg0);
		}
		return false;
	}

	@Override
	public boolean removeAll(final Collection<?> arg0) {
		boolean flag=false;
		for(final Object o:arg0) {
			flag=flag || this.remove(o);
		}
		return flag;
	}

	@Override
	public boolean retainAll(final Collection<?> arg0) {
		boolean flag=false;
		for(final E e:this){
			if(!arg0.contains(e)){
				flag=true;
				this.remove(e);
			}
		}
		return flag;
	}

	@Override
	public int size() {
		int size=this.memoryBag.size();
		if(this.diskBag!=null) {
			size+=this.diskBag.size();
		}
		return size;
	}

	@Override
	public Object[] toArray() {
		throw(new UnsupportedOperationException("This Bag does not support toArray."));
	}

	@Override
	public <T> T[] toArray(final T[] arg0) {
		throw(new UnsupportedOperationException("This Bag does not support toArray."));
	}

}
