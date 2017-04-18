package lupos.datastructures.lsmtree.level.factory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.lsmtree.LSMTree;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.ILevel;
import lupos.datastructures.lsmtree.level.collector.RunCollectorIterator;
import lupos.datastructures.lsmtree.level.disk.DiskRun;
import lupos.datastructures.lsmtree.level.disk.store.IStoreKeyValue;
import lupos.datastructures.lsmtree.level.disk.store.StoreIntTriple;
import lupos.datastructures.lsmtree.level.disk.store.StoreKeyValue;
import lupos.datastructures.lsmtree.level.disk.store.StoreUncompressedIntTriple;
import lupos.datastructures.lsmtree.level.memory.IMemoryLevel;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;

/**
 * Creates the levels for memory and disk, as well as the runs
 *
 * @author Maike Herting
 *
 */
public class DiskLevelFactory<K,V> implements ILevelFactory<K, V, Iterator<Map.Entry<K,Container<V>>>> {

	/**
	 * Comparator used to compare keys
	 */
	protected final Comparator<K> comp;

	/**
	 * stores key-value-pairs
	 */
	protected final IStoreKeyValue<K,V> storeKeyValue;

	/**
	 * the factory for generating memory levels
	 */
	protected final IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> memoryLevelFactory;

	/**
	 * The path in which the disk runs are stored for this lsm-tree.
	 */
	protected final String path;

	/**
	 * contains the maximum number of entries stored into the memory level
	 */
	protected final int memorysize;

	/**
	 * contains the maximum number of disk runs for each level
	 */
	protected final int numberOfDiskRunsForEachLevel;

	/**
	 * This counter contains the number of instances of DiskLevelFactory.
	 * The counter is used to determine a unique path for the disk runs for this lsm-tree, if no path is already given.
	 */
	private static int instancecounter = 0;

	/**
	 * contains the default value of the maximum number of entries stored into the memory level
	 */
	public static int MEMORYSIZE_DEFAULT = 2000000;

	/**
	 * contains the default value of the maximum number of disk runs for each level
	 */
	public static int NUMBEROFDISKRUNSFOREACHLEVEL_DEFAULT = 64;

	/**
	 * Constructor
	 * if no comparator is specified it sets it to a natural Order comparator
	 *
	 * @param storeKeyValue
	 */
	@SuppressWarnings("unchecked")
	public<K2 extends Comparable<? super K2>> DiskLevelFactory(final IStoreKeyValue<K2,V> storeKeyValue){
		this((Comparator<K>) (Comparator.<K2>naturalOrder()), (IStoreKeyValue<K,V>) storeKeyValue);
	}

	/**
	 * Constructor
	 * if no comparator is specified it sets it to a natural Order comparator
	 *
	 * @param storeKeyValue
	 * @param memorysize the maximum number of entries stored into the memory level
	 * @param numberOfDiskRunsForEachLevel the maximum number of disk runs for each level
	 */
	@SuppressWarnings("unchecked")
	public<K2 extends Comparable<? super K2>> DiskLevelFactory(final IStoreKeyValue<K2,V> storeKeyValue, final int memorysize, final int numberOfDiskRunsForEachLevel){
		this((Comparator<K>) (Comparator.<K2>naturalOrder()), (IStoreKeyValue<K,V>) storeKeyValue, memorysize, numberOfDiskRunsForEachLevel);
	}

	/**
	 * Constructor
	 * if no comparator is specified it sets it to a natural Order comparator
	 *
	 * @param storeKeyValue
	 * @param path the path in which the disk runs are stored for this lsm-tree
	 */
	@SuppressWarnings("unchecked")
	public<K2 extends Comparable<? super K2>> DiskLevelFactory(final IStoreKeyValue<K2,V> storeKeyValue, final String path){
		this((Comparator<K>) (Comparator.<K2>naturalOrder()), (IStoreKeyValue<K,V>) storeKeyValue, path);
	}

	/**
	 * Constructor
	 * if no comparator is specified it sets it to a natural Order comparator
	 *
	 * @param storeKeyValue
	 * @param path the path in which the disk runs are stored for this lsm-tree
	 * @param memorysize the maximum number of entries stored into the memory level
	 * @param numberOfDiskRunsForEachLevel the maximum number of disk runs for each level
	 */
	@SuppressWarnings("unchecked")
	public<K2 extends Comparable<? super K2>> DiskLevelFactory(final IStoreKeyValue<K2,V> storeKeyValue, final String path, final int memorysize, final int numberOfDiskRunsForEachLevel){
		this((Comparator<K>) (Comparator.<K2>naturalOrder()), (IStoreKeyValue<K,V>) storeKeyValue, path, memorysize, numberOfDiskRunsForEachLevel);
	}

	/**
	 * Constructor specifying the comparator and the StoreKeyValue interface
	 *
	 * @param comp a Comparator
	 * @param storeKeyValue
	 */
	public DiskLevelFactory(final Comparator<K> comp, final IStoreKeyValue<K,V> storeKeyValue){
		this(comp, storeKeyValue, "lsm-tree/"+instancecounter+"/");
	}

	/**
	 * Constructor specifying the comparator and the StoreKeyValue interface
	 *
	 * @param comp a Comparator
	 * @param storeKeyValue
	 * @param memorysize the maximum number of entries stored into the memory level
	 * @param numberOfDiskRunsForEachLevel the maximum number of disk runs for each level
	 */
	public DiskLevelFactory(final Comparator<K> comp, final IStoreKeyValue<K,V> storeKeyValue, final int memorysize, final int numberOfDiskRunsForEachLevel){
		this(comp, storeKeyValue, "lsm-tree/"+instancecounter+"/", memorysize, numberOfDiskRunsForEachLevel);
	}

	/**
	 * Constructor specifying the comparator, the StoreKeyValue interface and the path for disk runs
	 *
	 * @param comp a Comparator
	 * @param storeKeyValue
	 * @param path the path in which the disk runs are stored for this lsm-tree
	 */
	public DiskLevelFactory(final Comparator<K> comp, final IStoreKeyValue<K,V> storeKeyValue, final String path){
		this(comp, storeKeyValue, path, DiskLevelFactory.MEMORYSIZE_DEFAULT, DiskLevelFactory.NUMBEROFDISKRUNSFOREACHLEVEL_DEFAULT);
	}

	/**
	 * Constructor specifying the comparator, the StoreKeyValue interface and the path for disk runs
	 *
	 * @param comp a Comparator
	 * @param storeKeyValue
	 * @param path the path in which the disk runs are stored for this lsm-tree
	 * @param memorysize the maximum number of entries stored into the memory level
	 * @param numberOfDiskRunsForEachLevel the maximum number of disk runs for each level
	 */
	public DiskLevelFactory(final Comparator<K> comp, final IStoreKeyValue<K,V> storeKeyValue, final String path, final int memorysize, final int numberOfDiskRunsForEachLevel){
		this(comp, storeKeyValue, path, memorysize, numberOfDiskRunsForEachLevel, IMemoryLevelFactory.createMemoryLevelLazySortingFactory());
	}

	/**
	 * Constructor specifying the comparator, the StoreKeyValue interface and the path for disk runs
	 *
	 * @param comp a Comparator
	 * @param storeKeyValue
	 * @param path the path in which the disk runs are stored for this lsm-tree
	 * @param memorysize the maximum number of entries stored into the memory level
	 * @param numberOfDiskRunsForEachLevel the maximum number of disk runs for each level
	 * @param memoryLevelFactory the memory level factory for creating memory levels
	 */
	public DiskLevelFactory(final Comparator<K> comp, final IStoreKeyValue<K,V> storeKeyValue, final String path, final int memorysize, final int numberOfDiskRunsForEachLevel, final IMemoryLevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> memoryLevelFactory){
		this.comp = comp;
		this.storeKeyValue = storeKeyValue;
		this.memoryLevelFactory = memoryLevelFactory;
		this.path = path;
		final File f = new File(this.path);
		f.mkdirs();
		this.memorysize = memorysize;
		this.numberOfDiskRunsForEachLevel = numberOfDiskRunsForEachLevel;
		DiskLevelFactory.instancecounter++;
	}

	/**
	 * {@inheritDoc}
	 *
	 * if level equals 0, a memory level is created otherwise a run collector
	 * their size can be specified here
	 */
	@Override
	public ILevel<K, V, Iterator<Map.Entry<K,Container<V>>>> createLevel(final int level) {
		if (level==0){
			return this.memoryLevelFactory.createMemoryLevel(this, level, this.memorysize, this.comp);
		}else{
			return new RunCollectorIterator<K,V>(this, level, this.numberOfDiskRunsForEachLevel, this.comp, (long) (this.memorysize*Math.pow(this.numberOfDiskRunsForEachLevel, level-1)));
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>> createLevel0() {
		return this.memoryLevelFactory.createMemoryLevel(this, 0, this.memorysize, this.comp);
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public ILevel<K, V, Iterator<Map.Entry<K,Container<V>>>> createRun(final int level, final int number) throws IOException {
		return new DiskRun<K,V>(this, level, number, this.comp, this.storeKeyValue, (long) (this.memorysize*Math.pow(this.numberOfDiskRunsForEachLevel, level-1)));
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public String getPathToDiskRuns() {
		return this.path;
	}

	@Override
	public Comparator<K> getComparator() {
		return this.comp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
		if(this.comp.equals(Comparator.naturalOrder())){
			OutHelper.writeLuposByte((byte)0, loos);
		} else {
			OutHelper.writeLuposByte((byte)1, loos);
			Registration.serializeWithoutId(this.comp, loos);
		}
		LSMTree.writeTypeOfObject(loos, storeKeyValuesClasses, (Class<IStoreKeyValue>) this.storeKeyValue.getClass());
		this.storeKeyValue.writeLuposObject(loos);
		OutHelper.writeLuposString(this.path, loos);
		OutHelper.writeLuposIntVariableBytes(this.memorysize, loos);
		OutHelper.writeLuposIntVariableBytes(this.numberOfDiskRunsForEachLevel, loos);
	}

	@SuppressWarnings("unchecked")
	public static<K, V> DiskLevelFactory<K, V> readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		try {
			final Comparator<K> comp;
			final byte compType = InputHelper.readLuposByte(lois);
			if(compType==0){
				comp = (Comparator<K>) (Comparator.naturalOrder());
			} else {
				comp = Registration.deserializeWithoutId(Comparator.class, lois);
			}
			final IStoreKeyValue<K, V> storeKeyValue = LSMTree.readTypeOfObject(lois, storeKeyValuesClasses);
			final String path = InputHelper.readLuposString(lois);
			final int memorysize = InputHelper.readLuposIntVariableBytes(lois);
			final int numberOfDiskRunsForEachLevel = InputHelper.readLuposIntVariableBytes(lois);
			return new DiskLevelFactory<K, V>(comp, storeKeyValue, path, memorysize, numberOfDiskRunsForEachLevel);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.println(e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private static List<Class<? extends IStoreKeyValue>> storeKeyValuesClasses = new LinkedList<Class<? extends IStoreKeyValue>>();

	public static void registerStoreKeyValuesClassesClass(@SuppressWarnings("rawtypes") final Class<? extends IStoreKeyValue> factoryClass){
		storeKeyValuesClasses.add(factoryClass);
	}

	static {
		DiskLevelFactory.registerStoreKeyValuesClassesClass(StoreKeyValue.class);
		DiskLevelFactory.registerStoreKeyValuesClassesClass(StoreIntTriple.class);
		DiskLevelFactory.registerStoreKeyValuesClassesClass(StoreUncompressedIntTriple.class);
	}
}
