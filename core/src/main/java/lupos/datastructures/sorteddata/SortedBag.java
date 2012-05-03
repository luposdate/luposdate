/**
 * 
 */
package lupos.datastructures.sorteddata;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * @author groppe
 *
 */
public interface SortedBag<E> extends Collection<E> { 

	public Comparator<? super E> comparator();
	public E first();
	public SortedBag<E> headBag(E toElement);
	public E last();
	public SortedBag<E> subBag(E fromElement, E toElement);
	public SortedBag<E> tailBag(E fromElement);
	public boolean add(E e);
	public boolean addAll(Collection<? extends E> c);
	public void clear();
	public boolean contains(Object o);
	public boolean containsAll(Collection<?> c);
	public boolean isEmpty();
	public Iterator<E> iterator();
	public boolean remove(Object o);
	public boolean removeAll(Collection<?> c);
	public boolean retainAll(Collection<?> c);
	public int size();
	public Object[] toArray();
	public <T> T[] toArray(T[] a);	
}
