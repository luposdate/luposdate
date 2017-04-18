package lupos.datastructures.lsmtree.debug;

public interface IKeyValuePrinter<K, V> {
	/**
	 * This method is to enable a user-defined printing method for key values to be used in printLevels()...
	 *
	 * @param k key to be printed
	 * @return the string representation of the given key
	 */
	public default String toStringKey(final K k){
		return k.toString();
	}

	/**
	 * This method is to enable a user-defined printing method for values to be used in printLevels()...
	 *
	 * @param v value to be printed
	 * @return the string representation of the given value
	 */
	public default String toStringValue(final V v){
		return v.toString();
	}
}
