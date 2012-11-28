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
import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.sorteddata.SortedBag;

public class SortedBagImplementation<E extends Serializable> implements SortedBag<E>{

	private final SortedBag<E> memoryBag;
	private SortedBag<E> diskBag;
	
	protected final static int MAXMEMORYMAPENTRIES=30000;
	
	public SortedBagImplementation(final SortedBag<E> memoryBag){
		this.memoryBag=memoryBag;
	}
	
	public Comparator<? super E> comparator() {
		return memoryBag.comparator();
	}

	public E first() {
		final E firstMemory=memoryBag.first();
		if(diskBag==null) 
			return firstMemory;
		final E firstDisk=diskBag.first();
		if(firstMemory==null) return firstDisk;
		if(firstDisk==null) return firstMemory;
		return (memoryBag.comparator().compare(firstMemory,firstDisk)<=0)?firstMemory:firstDisk;
	}

	public SortedBag<E> headBag(final E arg0) {
		throw(new UnsupportedOperationException("This Bag does not support headBag."));
	}

	public E last() {
		final E lastMemory=memoryBag.last();
		if(diskBag==null) 
			return lastMemory;
		final E lastDisk=diskBag.last();
		if(lastMemory==null) return lastDisk;
		if(lastDisk==null) return lastMemory;
		return (memoryBag.comparator().compare(lastMemory,lastDisk)>0)?lastMemory:lastDisk;
	}

	public SortedBag<E> subBag(final E arg0, final E arg1) {
		throw(new UnsupportedOperationException("This Bag does not support subBag."));
	}

	public SortedBag<E> tailBag(final E arg0) {
		throw(new UnsupportedOperationException("This Bag does not support tailBag."));
	}

	public boolean add(final E arg0) {
		if(memoryBag.size()<MAXMEMORYMAPENTRIES) return memoryBag.add(arg0);
		if(memoryBag.contains(arg0)) return false;
		if(diskBag==null) diskBag=new DBMergeSortedBag<E>(new SortConfiguration(), memoryBag.comparator(),(Class<E>)arg0.getClass());
		return diskBag.add(arg0);
	}

	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag=false;
		for(final E e:arg0)
			flag = flag || add(e);
		return flag;
	}

	public void clear() {
		memoryBag.clear();
		if(diskBag!=null) diskBag.clear();
	}

	public boolean contains(final Object arg0) {
		if(memoryBag.contains(arg0)) return true;
		if(diskBag!=null && diskBag.contains(arg0)) return true;
		return false;
	}

	public boolean containsAll(final Collection<?> arg0) {
		for(final Object o:arg0)
			if(!contains(o)) return false;
		return true;
	}

	public boolean isEmpty() {
		if(!memoryBag.isEmpty()) return false;
		if(memoryBag==null || memoryBag.isEmpty()) return true;
		return false;
	}

	public Iterator<E> iterator() {
		return new Iterator<E>(){
			Iterator<E> memoryIterator = memoryBag.iterator();
			Iterator<E> diskIterator = (diskBag==null)? null:diskBag.iterator();
			E nextMemory=null;
			E nextDisk=null;
			public boolean hasNext() {
				if(nextMemory!=null || nextDisk!=null) return true;
				if(memoryIterator.hasNext()) return true;
				if(diskIterator!=null) return diskIterator.hasNext();
				return false;
			}
			public E next() {						
				if(nextMemory==null && memoryIterator.hasNext()) nextMemory=memoryIterator.next();
				if(diskIterator!=null){
					if(nextDisk==null && diskIterator.hasNext()) nextDisk=diskIterator.next();
				}
				if(nextMemory==null) return nextDisk;
				if(nextDisk==null) return nextMemory;
				if(SortedBagImplementation.this.memoryBag.comparator().compare(nextMemory, nextDisk)<=0){
					final E result=nextMemory;
					nextMemory=null;
					return result;
				}
				else {
					final E result=nextDisk;
					nextDisk=null;
					return result;							
				}
			}
			public void remove() {
				throw(new UnsupportedOperationException("This iterator does not support remove."));
			}
		};
	}

	public boolean remove(final Object arg0) {
		final boolean flag=memoryBag.remove(arg0);
		if(flag) return true;
		if(diskBag!=null) return diskBag.remove(arg0);
		return false;
	}

	public boolean removeAll(final Collection<?> arg0) {
		boolean flag=false;
		for(final Object o:arg0)
			flag=flag || remove(o);
		return flag;
	}

	public boolean retainAll(final Collection<?> arg0) {
		boolean flag=false;
		for(final E e:this){
			if(!arg0.contains(e)){
				flag=true;
				remove(e);
			}
		}
		return flag;
	}

	public int size() {
		int size=memoryBag.size();
		if(diskBag!=null) size+=diskBag.size();
		return size;
	}

	public Object[] toArray() {
		throw(new UnsupportedOperationException("This Bag does not support toArray."));
	}

	public <T> T[] toArray(final T[] arg0) {
		throw(new UnsupportedOperationException("This Bag does not support toArray."));
	}

}
