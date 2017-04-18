package lupos.datastructures.lsmtree;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.lsmtree.debug.IKeyValuePrinter;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.factory.DiskLevelFactory;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.lsmtree.level.factory.MemoryLevelFactory;
import lupos.datastructures.lsmtree.level.memory.IMemoryLevel;
import lupos.datastructures.lsmtree.sip.ISIPIterator;
import lupos.datastructures.lsmtree.sip.RemoveDeletedEntriesSIPIterator;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.sorteddata.MapIteratorProvider;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;

/**
 * LSM tree that can insert, find and remove key-value-pairs in a level structure, specified by the level factory
 *
 * @author Maike Herting
 *
 */
public class LSMTree<K, V, R> implements MapIteratorProvider<K, V> {

	/**
	 * The default name of created LSM trees in the case that no name is given
	 */
	private static final String DEFAULT_NAME = "LSM Tree";

	/**
	 * the name of this LSM tree
	 */
	protected final String name;

	/**
	 * the first level, which is a memory level
	 *
	 */
	protected IMemoryLevel<K,V,R> level0;

	/**
	 * level factory creating new levels
	 *
	 */
	protected final ILevelFactory<K,V,R> levelFactory;

	/**
	 * Constructor setting the level factory and creating the first level
	 *
	 * @param levelFactory the level factory that is used to create level
	 */
	public LSMTree(final ILevelFactory<K,V,R> levelFactory){
		this(LSMTree.DEFAULT_NAME, levelFactory);

	}

	/**
	 * Constructor setting the level factory and creating the first level
	 *
	 * @param name the name of this LSM tree
	 * @param levelFactory the level factory that is used to create level
	 */
	public LSMTree(final String name, final ILevelFactory<K,V,R> levelFactory){
		this.levelFactory = levelFactory;
		this.level0 = levelFactory.createLevel0();
		this.name = name;
	}

	/**
	 * Returns the name of this LSM tree
	 *
	 * @return the name of this LSM tree
	 */
	public String getName(){
		return this.name;
	}

	/**
	* Inserts the key and its value in the memory level
	* value is stored in Container with a removed marker which is set to false
	*
	* @param K the key
	* @param V the value
	* @return boolean if insert was successful
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public boolean put(final K key, final V value) throws ClassNotFoundException, IOException, URISyntaxException{
		return this.level0.put(key,new Container<V>(value,false));
	}

	/**
	* Returns the value that belongs to the key
	* Checks if value was removed in which case it will return null
	*
	* @param key the key that is looked for
	* @return V the value
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public V get(final K key) throws ClassNotFoundException, IOException, URISyntaxException {
		final Container<V> container = this.level0.get(key);
		if(container==null || container.isDeleted()){
			return null;
		} else {
			return container.getValue();
		}
	}

	/**
	 * Returns the result of a prefix search in form of a ISIPIterator
	 *
	 * @param prefixComparator the comparator used during the prefix search
	 * @param prefixkey the prefix key
	 * @return the result of the prefix search in form of a ISIPIterator
	 */
	public ISIPIterator<K, V> prefixSearch(final Comparator<K> prefixComparator, final K prefixkey){
		return new RemoveDeletedEntriesSIPIterator<K, V>(this.levelFactory.getComparator(), this.level0.prefixSearch(prefixComparator, prefixkey));
	}

	/**
	 * Returns the result of a prefix search in form of a SIPParallelIterator
	 *
	 * @param prefixComparator the comparator used during the prefix search
	 * @param prefixkey the prefix key
	 * @return the result of the prefix search in form of a SIPParallelIterator
	 */
	public SIPParallelIterator<java.util.Map.Entry<K, V>, K> prefixSearchSIPParallelIterator(final Comparator<K> prefixComparator, final K prefixkey){
		return new RemoveDeletedEntriesSIPIterator<K, V>(this.levelFactory.getComparator(), this.level0.prefixSearch(prefixComparator, prefixkey));
	}

	/**
	 * Returns all entries in form of a ISIPIterator
	 *
	 * @return iterator for iterating through all entries in form of a ISIPIterator
	 */
	public ISIPIterator<K, V> isipIterator(){
		final Comparator<K> ordinaryComparator = this.levelFactory.getComparator();
		return new RemoveDeletedEntriesSIPIterator<K, V>(ordinaryComparator, this.level0.prefixSearch(getPrefixComparatorForNullPrefixKeys(ordinaryComparator), null));
	}

	/**
	 * Returns all entries in form of a SIPParallelIterator
	 *
	 * @return iterator for iterating through all entries in form of a SIPParallelIterator
	 */
	@Override
	public SIPParallelIterator<java.util.Map.Entry<K, V>, K> iterator(){
		final Comparator<K> ordinaryComparator = this.levelFactory.getComparator();
		return new RemoveDeletedEntriesSIPIterator<K, V>(ordinaryComparator, this.level0.prefixSearch(getPrefixComparatorForNullPrefixKeys(ordinaryComparator), null));
	}

	/**
	 * Returns an iterator for prefix search where the prefix key will be always a null value in order to iterate through all entries of the lsm tree
	 *
	 * @param ordinaryComparator the ordinary comparator, which is used in the case that the values to be compared all both non-null
	 * @return the prefix comparator (comparing keys with a null prefix key)
	 */
	private final static<K> Comparator<K> getPrefixComparatorForNullPrefixKeys(final Comparator<K> ordinaryComparator){
		return new Comparator<K>(){
			@Override
			public int compare(final K o1, final K o2) {
				if(o1==null || o2==null){ // a prefix key of null always fits!
					return 0;
				}
				return ordinaryComparator.compare(o1, o2);
			}
		};
	}


	/**
	* Removes the key by inserting it with the value null and the removed marker in the container is set to true
	*
	* @param key the key that is looked for
	* @return Container<V> including the value and removed marker
	* @throws java.io.IOException
	* @throws java.lang.ClassNotFoundException
	* @throws java.net.URISyntaxException
	*/
	public boolean remove(final K key) throws ClassNotFoundException, IOException, URISyntaxException{
		return this.level0.put(key,new Container<V>(null, true));
	}

	/**
	* only used for debugging
	* displays the entries of the LSM tree
	*
	* @param printer the printer to be used to print the keys and values
	*/
	public void printLevels(final IKeyValuePrinter<K, V> printer){
		this.level0.printLevels(printer);
	}

	/**
	* only used for debugging
	* displays the entries of the LSM tree (by using a standard printer for the keys and values)
	*
	*/
	public void printLevels(){
		this.level0.printLevels(new IKeyValuePrinter<K, V>(){});
	}

	/**
	 * clears the LSM tree
	 */
	public void clear() {
		this.level0.clear();
	}

	/**
	 * Adds a complete run to this LSM tree
	 *
	 * @param generator the run
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void addRun(final Generator<K, V> generator) throws ClassNotFoundException, IOException, URISyntaxException {
		this.level0.addRun(generator);
	}

	/**
	 * Writes all information of this LSM tree to disk such that it can be loaded from disk after exiting the program
	 *
	 * @param loos the output stream to which the LSM tree is written...
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeLuposObject(final OutputStream loos) throws IOException, ClassNotFoundException, URISyntaxException {
		LSMTree.writeTypeOfObject(loos, levelFactoryClasses, (Class<ILevelFactory>) this.levelFactory.getClass());
		this.levelFactory.writeLuposObject(loos);
		this.level0.writeLuposObject(loos);
	}

	/**
	 * @return the number of bytes, which are used on disk (without wasted bytes to complete pages)
	 */
	public long numberOfUsedBytesOnDisk() throws IOException {
		return this.level0.numberOfUsedBytesOnDisk();
	}

	/**
	 * @return the number of bytes, which are used on disk (with wasted bytes to complete pages)
	 */
	public long numberOfBytesOnDisk(){
		return this.level0.numberOfBytesOnDisk();
	}

	/**
	 * @return information about structure (number of entries/runs) in this and succeeding levels as string
	 */
	public String getStructureInfo() throws IOException {
		return this.level0.getStructureInfo();
	}

	@SuppressWarnings({ "unchecked" })
	public static<K, V, R> LSMTree<K, V, R> readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		try {
			final ILevelFactory<K, V, R> levelFactory = readTypeOfObject(lois, levelFactoryClasses);
			final LSMTree<K, V, R> result = new LSMTree<K, V, R>(levelFactory);
			result.level0.readLuposObject(lois);
			return result;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	public static<C> void writeTypeOfObject(final OutputStream loos, final List<Class<? extends C>> classes, final Class<C> c) throws IOException {
		int i=0;
		for(final Class<? extends C> current: classes){
			if(c.equals(current)){
				OutHelper.writeLuposIntVariableBytes(i, loos);
				break;
			}
			i++;
		}
	}

	public static<C> C readTypeOfObject(final InputStream lois, final List<Class<? extends C>> classes) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		final Class<? extends C> lfc = classes.get(InputHelper.readLuposIntVariableBytes(lois));
		final Method method = lfc.getMethod("readLuposObject", InputStream.class);
		@SuppressWarnings("unchecked")
		final C c = (C) method.invoke(null, lois);
		return c;
	}

	@SuppressWarnings("rawtypes")
	private static List<Class<? extends ILevelFactory>> levelFactoryClasses = new LinkedList<Class<? extends ILevelFactory>>();

	public static void registerFactoryClass(@SuppressWarnings("rawtypes") final Class<? extends ILevelFactory> factoryClass){
		levelFactoryClasses.add(factoryClass);
	}

	static {
		LSMTree.registerFactoryClass(MemoryLevelFactory.class);
		LSMTree.registerFactoryClass(DiskLevelFactory.class);
	}
}

