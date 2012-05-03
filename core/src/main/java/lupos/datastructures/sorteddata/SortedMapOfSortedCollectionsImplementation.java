/**
 * 
 */
package lupos.datastructures.sorteddata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedMap;

/**
 * A {@link Map} that further provides a <i>total ordering</i> on its keys and
 * the values of the collections which are used as values of the map.<br>
 * The map and the collection are ordered according to the
 * {@linkplain Comparable natural ordering} of its keys, or by a
 * {@link Comparator} typically provided at sorted map and sorted collection
 * creation time, respectively.
 * 
 * @author Sebastian Ebers
 * @param <K>
 *            the type of keys maintained by this map
 * @param <V>
 *            the type of mapped values
 * 
 */
public class SortedMapOfSortedCollectionsImplementation<K, V, CV extends Collection<V>>
		extends SortedMapOfCollectionsImplementation<K, V, CV> {

	protected Comparator<? super V> comparator;

	/**
	 * Constructor setting up the map to use and the class of the values of the
	 * collections which are used as values of the map
	 * 
	 * @param sortedMap
	 *            the map to use
	 * @param clazz
	 *            the class of the collection to use
	 * @param comparator
	 *            the comparator to use for the collection
	 */
	public SortedMapOfSortedCollectionsImplementation(
			final SortedMap<K, CV> sortedMap, final Class<? extends CV> clazz,
			final Comparator<? super V> comparator) {
		super(sortedMap, clazz);
		this.comparator = comparator;
	}

	/**
	 * Creates a new instance of the collection used as value of the map. Note
	 * that the comparator will be set if there was one explicitly provided
	 * during the instantiation of this class.
	 * 
	 * @return a newly allocated instance of the class represented by this
	 *         object.
	 * @throws IllegalAccessException
	 *             if the class or its nullary constructor is not accessible.
	 * @throws InstantiationException
	 *             if this <code>Class</code> represents an abstract class, an
	 *             interface, an array class, a primitive type, or void; or if
	 *             the class has no nullary constructor; or if the instantiation
	 *             fails for some other reason.
	 */
	@Override
	protected CV getNewCollection() throws InstantiationException,
			IllegalAccessException {
		if (comparator != null) {			
				try {
					return klass.getConstructor(Comparator.class).newInstance(
							comparator);
				} catch (final IllegalArgumentException e1) {
					System.err.println(e1);
					e1.printStackTrace();
				} catch (final SecurityException e1) {
					System.err.println(e1);
					e1.printStackTrace();
				} catch (final InvocationTargetException e1) {
					System.err.println(e1);
					e1.printStackTrace();
				} catch (final NoSuchMethodException e1) {
					System.err.println(e1);
					e1.printStackTrace();
				}
		}
		return klass.newInstance();
	}
}
