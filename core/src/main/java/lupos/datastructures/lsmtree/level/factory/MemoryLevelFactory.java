package lupos.datastructures.lsmtree.level.factory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.ILevel;
import lupos.datastructures.lsmtree.level.collector.RunCollectorIterator;
import lupos.datastructures.lsmtree.level.memory.IMemoryLevel;
import lupos.datastructures.lsmtree.level.memory.MemoryLevelTreeMap;
import lupos.io.Registration;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;

/**
 * Creates only memory levels, also for runs
 *
 * @author Maike Herting
 *
 */
public class MemoryLevelFactory<K,V> implements ILevelFactory<K,V,Iterator<Map.Entry<K,Container<V>>>> {

	public int memorysize = 512;
	public static int defaultmemorysize = 512;

	/**
	 * Comparator used to compare keys
	 */
	protected Comparator<K> comp;
	/**
	 * Constructor
	 * if no comparator is specified it sets it to a natural Order comparator
	 *
	 */
	public MemoryLevelFactory(){
		this(defaultmemorysize);
	}

	public<K2 extends Comparable<? super K2>> MemoryLevelFactory(final Comparator<K> comp){
		this(comp, defaultmemorysize);
	}

	@SuppressWarnings("unchecked")
	public<K2 extends Comparable<? super K2>> MemoryLevelFactory(final int memorysize){
		this((Comparator<K>) (Comparator.<K2>naturalOrder()), memorysize);
	}


	/**
	 * Constructor specifying the comparator
	 *
	 * @param comp a Comparator
	 *
	 */
	public MemoryLevelFactory(final Comparator<K> comp, final int memorysize){
		this.comp = comp;
		this.memorysize = memorysize;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public IMemoryLevel<K,V,Iterator<Map.Entry<K,Container<V>>>> createLevel0() {
		return new MemoryLevelTreeMap<K,V>(this, 0, this.memorysize, this.comp);
	}

	/**
	 * {@inheritDoc}
	 *
	 * if level equals 0, a memory level is created otherwise a run collector
	 * their size can be specified here
	 *
	 */
	@Override
	public ILevel<K,V,Iterator<Map.Entry<K,Container<V>>>> createLevel(final int level) {
		if (level==0){
			return new MemoryLevelTreeMap<K,V>(this, level, this.memorysize, this.comp);
		}else{
			return new RunCollectorIterator<K,V>(this, level, this.memorysize, this.comp, (long) Math.pow(this.memorysize, level));
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Run is a memory level
	 *
	 */
	@Override
	public ILevel<K,V,Iterator<Map.Entry<K,Container<V>>>> createRun(final int level, final int number) {
		return new MemoryLevelTreeMap<K,V>(this, level, (int) Math.pow(this.memorysize, level), this.comp);
	}

	@Override
	public String getPathToDiskRuns() {
		return "default";
	}

	@Override
	public Comparator<K> getComparator() {
		return this.comp;
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException {
		OutHelper.writeLuposIntVariableBytes(this.memorysize, loos);
		Registration.serializeWithoutId(this.comp, loos);
	}

	public static<K, V> MemoryLevelFactory<K, V> readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		final int memorysize = InputHelper.readLuposIntVariableBytes(lois);
		@SuppressWarnings("unchecked")
		final Comparator<K> comp = Registration.deserializeWithoutId(Comparator.class, lois);
		return new MemoryLevelFactory<K, V>(comp, memorysize);
	}
}
