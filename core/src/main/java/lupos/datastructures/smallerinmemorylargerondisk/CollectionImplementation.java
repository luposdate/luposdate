package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import lupos.datastructures.dbmergesortedds.DiskCollection;

public class CollectionImplementation<E extends Serializable> implements
		Collection<E>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5226267348532809078L;

	private int memoryLimit = 20000;

	private final Collection<E> memoryCollection = new LinkedList<E>();
	private DiskCollection<E> diskCollection = null;

	public CollectionImplementation() {

	}

	public CollectionImplementation(final int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	public boolean add(final E arg0) {
		if (memoryCollection.size() + 1 < memoryLimit)
			return memoryCollection.add(arg0);
		else {
			if (diskCollection == null)
				diskCollection = new DiskCollection<E>((Class<E>) arg0
						.getClass());
			return diskCollection.add(arg0);
		}
	}

	public boolean addAll(final Collection<? extends E> arg0) {
		boolean flag = true;
		for (final E e : arg0) {
			flag = flag && add(e);
		}
		return flag;
	}

	public void clear() {
		memoryCollection.clear();
		if (diskCollection != null) {
			diskCollection.release();
			diskCollection = null;
		}
	}

	public boolean contains(final Object arg0) {
		if (memoryCollection.contains(arg0))
			return true;
		if (diskCollection != null)
			return diskCollection.contains(arg0);
		return false;
	}

	public boolean containsAll(final Collection<?> arg0) {
		for (final Object o : arg0) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return (memoryCollection.size() == 0);
	}

	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Iterator<E> memoryIterator = memoryCollection.iterator();
			Iterator<E> diskIterator = (diskCollection == null) ? null
					: diskCollection.iterator();

			public boolean hasNext() {
				if (memoryIterator.hasNext())
					return true;
				if (diskIterator != null)
					return diskIterator.hasNext();
				return false;
			}

			public E next() {
				if (memoryIterator.hasNext())
					return memoryIterator.next();
				if (diskIterator != null)
					return diskIterator.next();
				return null;
			}

			public void remove() {
				throw (new UnsupportedOperationException(
						"This iterator does not support remove."));
			}
		};
	}

	public boolean remove(final Object arg0) {
		boolean flag = memoryCollection.remove(arg0);
		if (diskCollection != null)
			flag = flag || diskCollection.remove(arg0);
		return flag;
	}

	public boolean removeAll(final Collection<?> arg0) {
		boolean flag = true;
		for (final Object o : arg0) {
			flag = flag && remove(o);
		}
		return flag;
	}

	public boolean retainAll(final Collection<?> arg0) {
		final boolean flag = memoryCollection.retainAll(arg0);
		if (diskCollection != null)
			return flag || diskCollection.retainAll(arg0);
		return flag;
	}

	public int size() {
		if (diskCollection != null)
			return memoryCollection.size() + diskCollection.size();
		else
			return memoryCollection.size();
	}

	public Object[] toArray() {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	public <T> T[] toArray(final T[] arg0) {
		throw (new UnsupportedOperationException(
				"This Collection does not support toArray."));
	}

	@Override
	public String toString() {
		String s = "";
		for (final E e : this) {
			if (s.compareTo("") != 0)
				s += ", ";
			if (e != null)
				s += e.toString();
		}
		return "[ " + s + " ]";
	}

	public void release() {
		if (diskCollection != null) {
			((DiskCollection) diskCollection).release();
			diskCollection = null;
		}
	}
}
