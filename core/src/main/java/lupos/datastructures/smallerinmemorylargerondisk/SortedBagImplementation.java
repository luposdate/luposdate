package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.sorteddata.SortedBag;

public class SortedBagImplementation<E extends Serializable> implements SortedBag<E>{

	private final SortedBag<E> memoryBag;
	private SortedBag<E> diskBag;
	
	private final static int HEAPHEIGHT=5;

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
		if(diskBag==null) diskBag=new DBMergeSortedBag<E>(HEAPHEIGHT, memoryBag.comparator(),(Class<E>)arg0.getClass());
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
