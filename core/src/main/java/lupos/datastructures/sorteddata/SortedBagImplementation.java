/**
 * 
 */
package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author groppe
 * 
 */
public class SortedBagImplementation<E> implements SortedBag<E>, Collection<E> {

	protected SortedMap<E, ElementCounter<E>> sortedMap;

	public SortedBagImplementation(
			final SortedMap<E, ElementCounter<E>> sortedMap) {
		this.sortedMap = sortedMap;
	}

	/**
	 * Constructor
	 * 
	 * @param comparator
	 *            the comparator to use
	 * 
	 */
	public SortedBagImplementation(final Comparator<E> comparator) {
		this.sortedMap = new TreeMap<E, ElementCounter<E>>(comparator);
	}

	public Comparator<? super E> comparator() {
		return sortedMap.comparator();
	}

	public E first() {
		return sortedMap.firstKey();
	}

	public SortedBag<E> headBag(final E toElement) {
		return new SortedBagImplementation<E>(sortedMap.headMap(toElement));
	}

	public E last() {
		return sortedMap.lastKey();
	}

	public SortedBag<E> subBag(final E fromElement, final E toElement) {
		return new SortedBagImplementation<E>(sortedMap.subMap(fromElement,
				toElement));
	}

	public SortedBag<E> tailBag(final E fromElement) {
		return new SortedBagImplementation<E>(sortedMap.tailMap(fromElement));
	}

	public boolean add(final E e) {
		ElementCounter<E> elementCounter = sortedMap.get(e);
		if (elementCounter == null)
			elementCounter = new ElementCounter<E>(e);
		else
			elementCounter.add(e);
		sortedMap.put(e, elementCounter);
		return true;
	}

	public boolean addAll(final Collection<? extends E> c) {
		boolean flag = true;
		for (final E o : c) {
			flag = flag && add(o);
		}
		return flag;
	}

	public void clear() {
		sortedMap.clear();
	}

	public boolean contains(final Object o) {
		return sortedMap.containsKey(o);
	}

	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!sortedMap.containsKey(o))
				return false;
		}
		return true;
	}

	public boolean isEmpty() {
		return sortedMap.isEmpty();
	}

	public Iterator<E> iterator() {
		return new SortedIterator();
	}

	public boolean remove(final Object o) {
		if (sortedMap.remove(o) != null)
			return true;
		else
			return false;
	}

	public boolean removeAll(final Collection<?> c) {
		boolean flag = true;
		for (final Object o : c) {
			flag = flag && (sortedMap.remove(o) != null);
		}
		return flag;
	}

	public boolean retainAll(final Collection<?> c) {
		boolean flag = false;
		for (final Object o : this) {
			if (!c.contains(o)) {
				sortedMap.remove(o);
				flag = true;
			}
		}
		return flag;
	}

	public int size() {
		int size = 0;
		for (final ElementCounter<E> element : sortedMap.values()) {
			size += element.getElements().size();
		}
		return size;
	}

	public Object[] toArray() {
		final Object[] oa = new Object[size()];
		int i = 0;
		for (final ElementCounter<E> element : sortedMap.values()) {
			for (final E ele : element.getElements()) {
				oa[i] = ele;
				i++;
			}
		}
		return oa;
	}

	public <T> T[] toArray(final T[] a) {
		if (a.length != size()) {
			System.err.println("Size of given array is different from the number of elements!");
			return a;
		}
		int i = 0;
		for (final ElementCounter<E> element : sortedMap.values()) {
			for (final E ele : element.getElements()) {
				a[i] = (T) ele;
				i++;
			}
		}
		return a;
	}

	private class SortedIterator implements Iterator<E> {

		private final Iterator<E> keyIterator = sortedMap.keySet().iterator();
		private Iterator<E> currentIterator = null;

		public boolean hasNext() {
			if (currentIterator != null && currentIterator.hasNext())
				return true;
			return keyIterator.hasNext();
		}

		public E next() {
			if (currentIterator != null && currentIterator.hasNext()) {
				return currentIterator.next();
			}
			if (keyIterator.hasNext()) {
				currentIterator = sortedMap.get(keyIterator.next())
						.getElements().iterator();
				return currentIterator.next();
			} else
				return null;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"This iterator does not support to remove elements!");
		}

	}

}
