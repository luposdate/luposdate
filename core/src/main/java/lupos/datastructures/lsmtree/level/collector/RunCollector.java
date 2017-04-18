package lupos.datastructures.lsmtree.level.collector;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Comparator;

import lupos.datastructures.lsmtree.debug.IKeyValuePrinter;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.ILevel;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.lsmtree.sip.ISIPIterator;
import lupos.datastructures.lsmtree.sip.MergeSIPIterator;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;

/**
 *Run collector that stores the runs in a level
 *
 * @author Maike Herting
 *
 */
public abstract class RunCollector<K,V,R> implements ILevel<K,V,R>{

	/**
	 * Array to store the runs
	 */
	protected final ILevel<K,V,R>[] runs;

	/**
	 * The level factory that creates the next level and runs
	 */
	protected final ILevelFactory<K,V,R> levelFactory;

	/**
	 * The next level is initially set to null and only created if needed
	 */
	protected ILevel<K,V,R> nextLevel = null;

	/**
	 * The levelnumber of the run collector
	 */
	protected final int level;

	/**
	 * The maximum number of runs that can be stored in the run collector
	 */
	private final int k;

	/**
	 * Helps to count the number of runs that are stored in the run collector
	 */
	protected int counter=0;

	/**
	 * Comparator that is used to compare keys
	 */
	protected Comparator<K> comp;

	/**
	 * the maximum length of one run
	 */
	protected final long maximumRunLength;

	/**
	 * Constructor sets parameters and creates a new array of the size k
	 *
	 * @param levelFactory a level factory which creates level
	 * @param level number of the level
	 * @param k the maximum number of runs to be stored
	 * @param comp a comparator that is used to compare keys
	 */
	@SuppressWarnings("unchecked")
	public RunCollector(final ILevelFactory<K,V,R> levelFactory, final int level, final int k, final Comparator<K> comp, final long maximumRunLength){
		this.runs = new ILevel[k];
		this.k = k;
		this.level = level;
		this.levelFactory = levelFactory;
		this.comp = comp;
		this.maximumRunLength = maximumRunLength;
	}

	/**
	 * {@inheritDoc}
	 *
	 * new run is created by level factory and run iterator passed on to it, as well as counter increased
	 * if run doesn't fit into this run collector, next level is created if it doesn't exit yet and run collector is rolled out to it
	 * run collector is cleared and run is inserted
	 */
	@Override
	public boolean receiveRunFromLowerLevel(final R run) throws ClassNotFoundException, IOException, URISyntaxException{
		if(this.counter==this.k) {
			try {
					if(this.nextLevel==null){
						this.nextLevel=this.levelFactory.createLevel(this.level+1);
					}
					this.nextLevel.receiveRunFromLowerLevel(this.rollOut());
					this.release();
			} catch (final Exception e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}

		try {
			this.runs[this.counter] = this.levelFactory.createRun(this.level, this.counter);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		this.runs[this.counter].receiveRunFromLowerLevel(run);
		this.counter++;

		return true;
	}

	/**
	 * releases the higher levels, and releases all runs in run collector and sets the array runs to null and counter to zero
	 */
	@Override
	public void clear() {
		this.release();
		if(this.nextLevel!=null){
			this.nextLevel.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * starts looking for the key from the end of runs array and returns the container unless it's null
	 * if it can't be found in this run collector, the next level is searched for it on condition that it exists
	 */
	@Override
	public Container<V> get(final K key) throws ClassNotFoundException, IOException, URISyntaxException {
		Container<V> container=null;
		for(int i=this.counter-1;i>-1;i--){
			container=this.runs[i].get(key);
			if(container!=null){
				return container;
			}
		}
		if(this.nextLevel!=null){
			return this.nextLevel.get(key);
		} else {
			return null;
		}
	}


	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void printLevels(final IKeyValuePrinter<K, V> printer) {
		System.out.println("RunCollector: "+this.level);
		for(int i=0;i<this.counter;i++){
			this.runs[i].printLevels(printer);
		}
		if(this.nextLevel!=null){
			this.nextLevel.printLevels(printer);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void release() {
		for(int i=0; i<this.counter; i++){
			this.runs[i].release();
			this.runs[i]=null;
		}
		this.counter = 0;
	}

	@Override
	public ISIPIterator<K, Container<V>> prefixSearch(final Comparator<K> prefixComparator, final K prefixkey) {
		@SuppressWarnings("unchecked")
		final ISIPIterator<K, Container<V>>[] iterators = new ISIPIterator[this.counter + (this.nextLevel==null? 0 : 1)];
		for(int i=0;i<this.counter;i++){
			// reverse order as the younger runs must come first!
			iterators[this.counter-i-1] = this.runs[i].prefixSearch(prefixComparator, prefixkey);
		}
		if(this.nextLevel!=null){
			iterators[iterators.length-1] = this.nextLevel.prefixSearch(prefixComparator, prefixkey);
		}
		return new MergeSIPIterator<K, Container<V>>(this.comp, iterators);
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException, ClassNotFoundException, URISyntaxException {
		OutHelper.writeLuposIntVariableBytes(this.counter, loos);
		for(int i=0; i<this.counter; i++){
			this.runs[i].writeLuposObject(loos);
		}
		if(this.nextLevel!=null){
			OutHelper.writeLuposByte((byte)1, loos);
			this.nextLevel.writeLuposObject(loos);
		} else {
			OutHelper.writeLuposByte((byte)0, loos);
		}
	}

	@Override
	public void readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		this.counter = InputHelper.readLuposIntVariableBytes(lois);
		for(int i=0; i<this.counter; i++){
			this.runs[i] = this.levelFactory.createRun(this.level, i);
			this.runs[i].readLuposObject(lois);
		}
		final byte end = InputHelper.readLuposByte(lois);
		if(end==1){
			if(this.nextLevel==null){
				this.nextLevel = this.levelFactory.createLevel(this.level+1);
			}
			this.nextLevel.readLuposObject(lois);
		}
	}

	@Override
	public long numberOfUsedBytesOnDisk() throws IOException {
		long sum=0;
		if(this.nextLevel!=null){
			sum+=this.nextLevel.numberOfUsedBytesOnDisk();
		}
		for(int i=0; i<this.counter; i++){
			sum+=this.runs[i].numberOfUsedBytesOnDisk();
		}
		return sum;
	}

	@Override
	public long numberOfBytesOnDisk() {
		long sum=0;
		if(this.nextLevel!=null){
			sum+=this.nextLevel.numberOfBytesOnDisk();
		}
		for(int i=0; i<this.counter; i++){
			sum+=this.runs[i].numberOfBytesOnDisk();
		}
		return sum;
	}

	@Override
	public String getStructureInfo() throws IOException {
		String result = "Level "+this.level+" #runs="+this.counter+" (";
		for(int i=0; i<this.counter; i++){
			if(i>0){
				result+=", ";
			}
			result+=this.runs[i].getStructureInfo();
		}
		result+=")";
		if(this.nextLevel!=null){
			result += ", " + this.nextLevel.getStructureInfo();
		}
		return result;
	}
}