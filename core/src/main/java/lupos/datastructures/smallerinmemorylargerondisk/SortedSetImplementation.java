package lupos.datastructures.smallerinmemorylargerondisk;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import lupos.datastructures.dbmergesortedds.DBMergeSortedSet;

public class SortedSetImplementation<E extends Serializable> extends
		SetImplementation<E> implements SortedSet<E> {

	private final SortedSet<E> memorySet;
	private SortedSet<E> diskSet;

	private final static int HEAPHEIGHT = 5;

	protected final static int MAXMEMORYMAPENTRIES = 30000;

	public SortedSetImplementation(final SortedSet<E> memorySet) {
		this.memorySet = memorySet;
	}

	public Comparator<? super E> comparator() {
		return memorySet.comparator();
	}

	public E first() {
		final E firstMemory = memorySet.first();
		if (diskSet == null)
			return firstMemory;
		final E firstDisk = diskSet.first();
		if (firstMemory == null)
			return firstDisk;
		if (firstDisk == null)
			return firstMemory;
		return (memorySet.comparator().compare(firstMemory, firstDisk) <= 0) ? firstMemory
				: firstDisk;
	}

	public SortedSet<E> headSet(final E arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support headSet."));
	}

	public E last() {
		final E lastMemory = memorySet.last();
		if (diskSet == null)
			return lastMemory;
		final E lastDisk = diskSet.last();
		if (lastMemory == null)
			return lastDisk;
		if (lastDisk == null)
			return lastMemory;
		return (memorySet.comparator().compare(lastMemory, lastDisk) > 0) ? lastMemory
				: lastDisk;
	}

	public SortedSet<E> subSet(final E arg0, final E arg1) {
		throw (new UnsupportedOperationException(
				"This set does not support subSet."));
	}

	public SortedSet<E> tailSet(final E arg0) {
		throw (new UnsupportedOperationException(
				"This set does not support tailSet."));
	}

	@Override
	public boolean add(final E arg0) {
		if (memorySet.size() < MAXMEMORYMAPENTRIES)
			return memorySet.add(arg0);
		if (memorySet.contains(arg0))
			return false;
		if (diskSet == null)
				diskSet = new DBMergeSortedSet<E>(HEAPHEIGHT, memorySet
						.comparator(), (Class<E>) arg0.getClass());
		return diskSet.add(arg0);
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Iterator<E> memoryIterator = memorySet.iterator();
			Iterator<E> diskIterator = (diskSet == null) ? null : diskSet
					.iterator();
			E nextMemory = null;
			E nextDisk = null;

			public boolean hasNext() {
				if (nextMemory != null || nextDisk != null)
					return true;
				if (memoryIterator.hasNext())
					return true;
				if (diskIterator != null)
					return diskIterator.hasNext();
				return false;
			}

			public E next() {
				if (nextMemory == null && memoryIterator.hasNext())
					nextMemory = memoryIterator.next();
				if (diskIterator != null) {
					if (nextDisk == null && diskIterator.hasNext())
						nextDisk = diskIterator.next();
				}
				if (nextMemory == null)
					return nextDisk;
				if (nextDisk == null)
					return nextMemory;
				if (SortedSetImplementation.this.memorySet.comparator()
						.compare(nextMemory, nextDisk) <= 0) {
					final E result = nextMemory;
					nextMemory = null;
					return result;
				} else {
					final E result = nextDisk;
					nextDisk = null;
					return result;
				}
			}

			public void remove() {
				throw (new UnsupportedOperationException(
						"This iterator does not support remove."));
			}
		};
	}
}
