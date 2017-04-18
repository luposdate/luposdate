package lupos.datastructures.lsmtree.level.disk;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.buffermanager.BufferManager.PageAddress;
import lupos.datastructures.buffermanager.PageManager;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.lsmtree.debug.IKeyValuePrinter;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.ILevel;
import lupos.datastructures.lsmtree.level.disk.bloomfilter.IBloomFilter;
import lupos.datastructures.lsmtree.level.disk.store.IKeyValueIterator;
import lupos.datastructures.lsmtree.level.disk.store.IStoreKeyValue;
import lupos.datastructures.lsmtree.level.disk.store.StoreKeyValue;
import lupos.datastructures.lsmtree.level.disk.summary.ISummary;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.lsmtree.sip.ISIPIterator;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.FileHelper;
import lupos.misc.Triple;
import lupos.misc.Tuple;

/**
 *Disk run that stores key-value-pairs on disk using the buffer manager from LuposDate
 *
 * @see BufferManager
 * @see PageManager
 * @author Maike Herting
 *
 */
public class DiskRun<K, V> implements ILevel<K, V, Iterator<Map.Entry<K,Container<V>>>> {

	/**
	* BufferManger from LuposDate
	*/
	protected BufferManager bufferManager = BufferManager.getBufferManager();

	/**
	* Filename of disk run
	*/
	protected final String filename;

	/**
	* Serializes and Deserializes the key-value-pairs
	*/
	protected final IStoreKeyValue<K, V> storeKeyValue;

	/**
	* The maximum number of Entries to be stored at one time
	*/
	protected final int maxNumberOfEntriesToStore = Integer.MAX_VALUE;

	/**
	* Comparator that is used to compare keys
	*/
	protected Comparator<K> comp;

	/**
	 * The level factory that create level
	 */
	protected final ILevelFactory<K, V, Iterator<Map.Entry<K,Container<V>>>> levelFactory;

	/**
	* Summary for the disk run
	*/
	protected ISummary<K> summary;

	/**
	* The maximum level of the summary that exists
	*/
	protected int maxSummaryLevel;

	/**
	* Level where run is stored
	*/
	protected int level;

	/**
	* Number of run in the level
	*/
	protected int number;

	/**
	* Bloomfilter that is used
	*/
	protected IBloomFilter<K> bloomFilter;


	/**
	 * Constructor sets parameters and the filename according to the level and number
	 *
	 * @param levelFactory a level factory which creates level
	 * @param level number of the level
	 * @param number number of run in level
	 * @param comp a comparator that is used to compare keys
	 * @param storeKeyValue used to serialize and deserialize key-value-pairs
	 * @throws java.io.IOException if any.
	 */
	public DiskRun(final ILevelFactory<K, V, Iterator<Map.Entry<K,Container<V>>>> levelFactory, final int level, final int number, final Comparator<K> comp, final IStoreKeyValue<K, V> storeKeyValue, final long maximumRunLength) throws IOException {
		this.levelFactory = levelFactory;
		this.level=level;
		this.number=number;
		this.filename = levelFactory.getPathToDiskRuns()+"Run_" + this.level + "_" + this.number;
		this.storeKeyValue = storeKeyValue;
		this.comp = comp;
		this.bloomFilter = this.storeKeyValue.createBloomFilter(maximumRunLength);
	}

	/**
	 * {@inheritDoc}
	 *
	 * First bloom filter is checked, then the summaries are searched for the key to find the pagenumber where it is stored
	 * afterwards a DiskRunIterator is created starting from that pagenumber or from 0 if no summary exists to compare their entries to the key that is searched for
	 */
	@Override
	public Container<V> get(final K key) throws ClassNotFoundException, IOException, URISyntaxException {

		if(!this.bloomFilter.get(key)){
			return null;
		}

		int pagenumber = 0;
		int maxLevel = this.maxSummaryLevel;

		while (maxLevel>=0 && this.summary!=null){
			pagenumber = this.summary.getPagenumber(key, pagenumber, maxLevel);
			maxLevel--;
		}

		if (pagenumber < 0) {
			return null;

		}
		final Iterator<Entry<K, Container<V>>> it = new DiskRunIterator(pagenumber);
		while (it.hasNext()) {
			final Entry<K, Container<V>> entry = it.next();
			if (this.comp.compare(key, entry.getKey()) < 0){
				break;
			}
			if (this.comp.compare(key, entry.getKey()) == 0) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public K getFirstSummaryEntry() throws ClassNotFoundException, URISyntaxException {
		try {
			final int pagesize = PageManager.getDefaultPageSize();
			final int pagenumber = 0;
			final int offset = 2;
			final PageAddress pageAddress = new PageAddress(pagenumber, this.summary.getFilename());
			final byte[] page = this.bufferManager.getPage(pagesize, pageAddress);

			final IKeyValueIterator<Integer, Entry<K, Integer>> it = this.storeKeyValue.getNextSumEntries(this.maxNumberOfEntriesToStore, page, offset, pagesize);
			final Tuple<Integer, Entry<K, Integer>> entry = it.next(new Tuple<Integer, Entry<K, Integer>>(null, null));
			return entry.getSecond().getKey();

		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see DiskRunIterator
	 */
	@Override
	public Iterator<Entry<K, Container<V>>> rollOut() throws IOException, ClassNotFoundException, URISyntaxException {
		return new DiskRunIterator(0);

	}

	/**
	 * {@inheritDoc}
	 *
	 * First the keys are set in bloom filter, then entries of originalrun are stored on page via storeKeyValue
	 * if necessary a new page is allocated and its first key added to summary
	 * at the end page is modified and a flag is set to indicate the last page of the disk run
	 */
	@Override
	public boolean receiveRunFromLowerLevel(final Iterator<Entry<K, Container<V>>> originalrun) throws ClassNotFoundException, URISyntaxException {
		final Iterator<Entry<K, Container<V>>> run = this.storeKeyValue.getBloomFilterIterator(originalrun, this.bloomFilter);
		try {
			final int pagesize = PageManager.getDefaultPageSize();
			int pagenumber = 0;
			int offset = 2;
			PageAddress pageAddress = new PageAddress(pagenumber, this.filename);
			byte[] page = this.bufferManager.getPage(pagesize, pageAddress);
			Entry<K, Container<V>> entry = null;
			while(entry!=null || run.hasNext()) {

				// returns entry that doesn't fit to page or null if all
				// elements of run were stored
				final Tuple<Integer, Entry<K, Container<V>>> result = this.storeKeyValue.store(entry, run, this.maxNumberOfEntriesToStore, page, offset);

				entry = null;

				if (result.getSecond() != null) {
					// page is full!
					// write maxbytes!
					offset = result.getFirst();
					page[0] = (byte) offset;
					page[1] = (byte) (offset >>> 8); // last bit not set as this is not the last page!
					this.bufferManager.modifyPage(pagesize, pageAddress, page);
					pagenumber++;
					offset = 2;
					pageAddress = new PageAddress(pagenumber, this.filename);
					page = this.bufferManager.getPage(pagesize, pageAddress);
					entry = result.getSecond();
					// first key of next page for summary
					if(pagenumber==1){
						this.summary = new Summary(this.comp, this.storeKeyValue, this.level, this.number, 0);
					}
					if(this.summary!=null){
						this.summary.addEntry(entry.getKey(), pagenumber);
					}
				} else {
					offset = result.getFirst();
				}
			}
			if(this.summary!=null){
				this.summary.lastEntry(true);
			}
			page[1] = (byte) 0x80; // last bit set as this is the last page!
			page[0] = (byte) offset;
			page[1] |= (byte) (offset >>> 8);
			this.bufferManager.modifyPage(pagesize, pageAddress, page);
			// to write all modified pages to disk:
			// this.bufferManager.writeAllModifiedPages();
		} catch (final IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * also releases the summaries that belong to disk run
	 */
	@Override
	public void release() {
		this.bufferManager.releaseAllPages(this.filename);
		DiskRun.deleteFiles(this.filename);
		if(this.summary!=null){
			this.summary.release();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 */
	@Override
	public void printLevels(final IKeyValuePrinter<K, V> printer) {
		Iterator<Entry<K, Container<V>>> it;
		try {
			it = new DiskRunIterator(0);
			boolean first = true;
			int count = 0;
			while (it.hasNext()) {
				if(first){
					first = false;
				} else {
					System.out.print(", ");
				}
				final Entry<K, Container<V>> entry = it.next();
				System.out.print(printer.toStringKey(entry.getKey()) + " : " +
						"("+(entry.getValue().value==null?null:printer.toStringValue(entry.getValue().value))+", "+
						(entry.getValue().isDeleted()?"removed":"inserted")+")");
				count++;
			}
			System.out.println(" # = " + count);
			if(this.summary!=null){
				this.summary.printSummary(printer);
			}
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
		}

	}

	/**
	 * Iterator to get next entries that are stored in the disk run
	 */
	public class DiskRunIterator implements Iterator<Entry<K, Container<V>>> {

		/**
		 * size of the page
		 */
		protected final int pagesize;

		/**
		 * number of the page
		 */
		protected int pagenumber;

		/**
		 * address to read from in page
		 */
		protected int offset;

		/**
		 * pageadress on disk where disk run is stored
		 */
		protected PageAddress pageAddress;

		/**
		 * page where entries are stored
		 */
		protected byte[] page;

		/**
		 * last offset where an entry is stored on page
		 */
		protected int maxNumberInPage;

		/**
		 * iterator with entries and their offsets
		 */
		protected IKeyValueIterator<Integer,Entry<K, Container<V>>> it;

		/**
		 * This object is just for storing the results of the it iterator. This is done because of performance reasons in order to avoid the new generation of tuples for each it.next()-call and just having only one tuple object for this purpose...
		 */
		protected final Tuple<Integer, Entry<K, Container<V>>> resultObject = new Tuple<Integer, Entry<K, Container<V>>>(null, null);

		/**
		 * Whether or not the iterator of the next page will be prefetched
		 */
		protected final boolean prefetchIterator;

		/**
		 * Constructor to set parameters
		 * pagesize is set to 8KB, offset is set to 2 and entries for iterator are deserialized
		 *
		 * @see StoreKeyValue
		 * @param pagenumber the page where entries should be read from
		 * @throws java.io.IOException
		 * @throws java.lang.ClassNotFoundException
		 * @throws java.net.URISyntaxException
		 */
		public DiskRunIterator(final int pagenumber) throws IOException, ClassNotFoundException, URISyntaxException {
			this(true, pagenumber);
		}
		/**
		 * Constructor to set parameters
		 * pagesize is set to 8KB, offset is set to 2 and entries for iterator are deserialized
		 *
		 * @see StoreKeyValue
		 * @param prefetchIterator if the iterators are initialized with the next page whenever one page is fully read
		 * @param pagenumber the page where entries should be read from
		 * @throws java.io.IOException
		 * @throws java.lang.ClassNotFoundException
		 * @throws java.net.URISyntaxException
		 */
		public DiskRunIterator(final boolean prefetchIterator, final int pagenumber) throws IOException, ClassNotFoundException, URISyntaxException {
			this.prefetchIterator = prefetchIterator;
			this.pagesize = PageManager.getDefaultPageSize();
			this.pagenumber = pagenumber;
			this.offset = 2;
			this.pageAddress = new PageAddress(this.pagenumber, DiskRun.this.filename);
			this.page = DiskRun.this.bufferManager.getPage(this.pagesize, this.pageAddress);
			this.maxNumberInPage = (0xFF & this.page[0]) | (0x7F & this.page[1]) << 8;
			this.it = DiskRun.this.storeKeyValue.getNextEntries(DiskRun.this.maxNumberOfEntriesToStore, this.page, this.offset, this.maxNumberInPage);
		}

		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public boolean hasNext() {
			return this.it.hasNext();
		}

		/**
		 * {@inheritDoc}
		 *
		 * if iterator doesn't have next entries, this method will try to get the next iterator
		 */
		@Override
		public Entry<K, Container<V>> next() {
			if(this.it.hasNext()) {
				final Tuple<Integer, Entry<K, Container<V>>> entry = this.it.next(this.resultObject);
				this.offset = entry.getFirst();
				if (this.prefetchIterator && !this.it.hasNext()) {
					try {
						this.getNextIterator();
					} catch (ClassNotFoundException | IOException | URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
				return entry.getSecond();
			}
			return null;
		}

		/**
		 * either next page of disk run is loaded or next iterator is generated with new offset
		 */
		public void getNextIterator() throws ClassNotFoundException, IOException, URISyntaxException {
			if (this.offset >= this.maxNumberInPage) {
				if ((this.page[1] & 0x80) != 0) { // last bit is set => last page!
					return;
				}
				this.pagenumber++;
				this.offset = 2;
				this.pageAddress = new PageAddress(this.pagenumber, DiskRun.this.filename);
				this.page = DiskRun.this.bufferManager.getPage(this.pagesize, this.pageAddress);
				this.maxNumberInPage = (0xFF & this.page[0]) | (0x7F & this.page[1]) << 8;
			}
			this.it = DiskRun.this.storeKeyValue.getNextEntries(DiskRun.this.maxNumberOfEntriesToStore, this.page, this.offset, this.maxNumberInPage);
		}
	}

	/**
	 * The summary stores the first keys of the pages as well as the pagenumber from the disk run to help finding entries
	 *
	 */
	public class Summary implements ISummary<K> {

		/**
		 * size of the page
		 */
		protected final int pagesize = PageManager.getDefaultPageSize();

		/**
		 * number of the page
		 */
		protected int pagenumber = 0;

		/**
		 * address to read from in page
		 */
		protected int offset;

		/**
		 * pageadress on disk where summary is stored
		 */
		protected PageAddress pageAddress;

		/**
		 * page where entries are stored
		 */
		protected byte[] page;

		/**
		 * last offset where an entry is stored on page
		 */
		protected int maxNumberInPage;

		/**
		* The maximum number of Entries to be stored at one time
		*/
		protected final int maxNumberOfEntriesToStore = Integer.MAX_VALUE;

		/**
		* Filename of summary
		*/
		protected final String filename;

		/**
		* Serializes and Deserializes the key-pagenumber-pairs
		*/
		protected final IStoreKeyValue<K, V> storeKeyValue;

		/**
		* Comparator that is used to compare keys
		*/
		protected Comparator<K> comp;

		/**
		* level of the summary
		*/
		protected int summaryLevel;

		/**
		* level of disk run where summary belongs to
		*/
		protected final int level;

		/**
		* number of disk run in the level where summary belongs to
		*/
		protected final int number;

		/**
		 * the previously stored key in the summary (just to use difference encoding for storing the keys...)
		 */
		protected K previouslyStoredKey = null;

		/**
		 * this object is for storing additional information where to store the next summary entry (used e.g. by StoreIntTriples)
		 */
		protected Object storageInfo = null;

		/**
		* optional a summary of the summary one summarylevel higher
		*/
		protected ISummary<K> sumsummary;

		/**
		 * Constructor sets parameters and the filename according to the level, number and summarylevel
		 *
		 * @param comp a comparator that is used to compare keys
		 * @param storeKeyValue used to serialize and deserialize key-pagenumber-pairs
		 * @param level number of the level of disk run
		 * @param number number of run in level
		 * @param summarylevel the level of the summary
		 * @throws java.io.IOException if any.
		 */
		public Summary(final Comparator<K> comp, final IStoreKeyValue<K, V> storeKeyValue, final int level,
				final int number, final int summaryLevel) throws IOException {
			this.level = level;
			this.number = number;
			this.summaryLevel = summaryLevel;
			this.filename = DiskRun.this.levelFactory.getPathToDiskRuns()+"Summary_" + this.level + "_" + this.number + "_" +this.summaryLevel;
			this.storeKeyValue = storeKeyValue;
			this.comp = comp;
		}

		/**
		 * {@inheritDoc}
		 *
		 * as long as summary iterator has next entries it compares key to entries in summary and returns current pagenumber or the pagenumber before
		 */
		@Override
		public int getPagenumber(final K key, final int summaryPage, final int summaryLevel) throws ClassNotFoundException, IOException, URISyntaxException {
			final Iterator<Entry<K, Integer>> it = new SummaryIterator(summaryPage, summaryLevel);
			Entry<K, Integer> entry = null;
			while (it.hasNext()) {
				entry = it.next();
				final int comparison = this.comp.compare(key, entry.getKey());
				if(comparison < 0) {
					return entry.getValue() - 1;
					// entspricht genau gesuchtem key, aktuelle seite laden
				} else if(comparison == 0) {
					return entry.getValue();
				}
			}
			if (entry == null) {
				return -1;
			}
			if (this.comp.compare(key, entry.getKey()) >= 0) {
				return entry.getValue();
			}
			return -1;
		}

		/**
		 * {@inheritDoc}
		 *
		 * if a second page for summary is needed, a summary of the summary is created
		 */
		@Override
		public void addEntry(final K key, final int pagenumber) throws IOException {
			if (pagenumber == 1) {
				this.offset = 2;
			}
			this.pageAddress = new PageAddress(this.pagenumber, this.filename);
			this.page = DiskRun.this.bufferManager.getPage(this.pagesize, this.pageAddress);
			this.maxNumberInPage = (0xFF & this.page[0]) | (0x7F & this.page[1]) << 8;
			Entry<K, Integer> entry = new MapEntry<K, Integer>(key, pagenumber);
			Triple<Integer, Entry<K, Integer>, Object> result = this.storeKeyValue.storeSummary(entry, this.previouslyStoredKey, this.storageInfo, this.maxNumberOfEntriesToStore, this.page, this.offset);
			this.storageInfo = result.getThird();
			if (result.getSecond() != null) {
				// page is full!
				this.previouslyStoredKey = null;
				this.storageInfo = null;
				// write maxbytes!
				this.offset = result.getFirst();
				this.lastEntry(false);
				this.pagenumber++;
				this.offset = 2;
				this.pageAddress = new PageAddress(this.pagenumber, this.filename);
				this.page = DiskRun.this.bufferManager.getPage(this.pagesize, this.pageAddress);
				entry = result.getSecond();
				// store this entry on just loaded new page
				result = this.storeKeyValue.storeSummary(entry, null, null, this.maxNumberOfEntriesToStore, this.page, this.offset);
				this.storageInfo = result.getThird();
				// summary consists of 2 pages => create new summary of upper level
				if (this.pagenumber == 1) {
					this.sumsummary = new Summary(this.comp, this.storeKeyValue, this.level, this.number, this.summaryLevel+1);
					DiskRun.this.maxSummaryLevel++;
				}
				if (this.pagenumber >= 1) {
					// store entry in summary of upper level
					this.sumsummary.addEntry(entry.getKey(), this.pagenumber);
				}
			}
			this.offset = result.getFirst();
			this.previouslyStoredKey = key;
		}

		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public void lastEntry(final boolean last) throws IOException {
			if(this.sumsummary!=null){
				this.sumsummary.lastEntry(last);
			}
			if (last) {
				this.page[1] = (byte) 0x80; // this is the last page!
			} else {
				this.page[1] = 0; // this is not the last page!
			}
			this.page[0] = (byte) this.offset;
			this.page[1] |= (byte) (this.offset >>> 8);
			DiskRun.this.bufferManager.modifyPage(this.pagesize, this.pageAddress, this.page);
			if(last){
				// free up resources...
				this.page = null;
			}
		}

		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public void release() {
			DiskRun.this.bufferManager.releaseAllPages(this.filename);
			DiskRun.deleteFiles(this.filename);
			if(this.sumsummary!=null){
				this.sumsummary.release();
			}
		}

		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public String getFilename() {
			return DiskRun.this.levelFactory.getPathToDiskRuns()+"Summary_" + this.level + "_" + this.number + "_";
		}

		@Override
		public<V1> void printSummary(final IKeyValuePrinter<K, V1> printer) {
			System.out.print("Summary (Level "+this.summaryLevel+"): ");
			for(int page=0; page<=this.pagenumber; page++){ // pagenumber contains the last page of the summary...
				Iterator<Entry<K, Integer>> it;
				try {
					System.out.print("Page: "+page+" ");
					it = new SummaryIterator(page, this.summaryLevel);
					while (it.hasNext()) {
						final Entry<K, Integer> entry = it.next();
						System.out.print(entry.getValue()+" "+printer.toStringKey(entry.getKey())+" ");
					}
				} catch (ClassNotFoundException | IOException
						| URISyntaxException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			System.out.println();
			if(this.sumsummary!=null){
				this.sumsummary.printSummary(printer);
			}
		}

		@Override
		public Triple<Integer, Entry<K, Integer>, Iterator<Entry<K, Integer>>> prefixSearch(final Comparator<K> prefixComparator, final K prefixkey, final int summaryPage, final int summaryLevel) throws ClassNotFoundException, IOException, URISyntaxException {
			final Iterator<Entry<K, Integer>> it = new SummaryIterator(summaryPage, summaryLevel);
			Entry<K, Integer> entry = null;
			while (it.hasNext()) {
				entry = it.next();
				if (prefixComparator.compare(prefixkey, entry.getKey()) <= 0) {
					return new Triple<Integer, Entry<K, Integer>, Iterator<Entry<K, Integer>>>(entry.getValue()-1, entry, it);
				}
			}
			if (entry == null) {
				return null;
			}
			if (prefixComparator.compare(prefixkey, entry.getKey()) >= 0) {
				return new Triple<Integer, Entry<K, Integer>, Iterator<Entry<K, Integer>>>(entry.getValue(), entry, null);
			}
			return null;
		}

		@Override
		public ISummary<K> createSummaryOfTheSummary() throws IOException {
			this.sumsummary = new Summary(this.comp, this.storeKeyValue, this.level, this.number, this.summaryLevel+1);
			return this.sumsummary;
		}

		@Override
		public long numberOfBytesOnDisk() {
			final long sum;
			if(this.sumsummary!=null){
				sum = this.sumsummary.numberOfBytesOnDisk();
			} else {
				sum = 0;
			}
			return sum + DiskRun.numberOfBytesOnDisk(this.filename);
		}

		@Override
		public long numberOfUsedBytesOnDisk() throws IOException {
			final long sum;
			if(this.sumsummary!=null){
				sum = this.sumsummary.numberOfUsedBytesOnDisk();
			} else {
				sum = 0;
			}
			return sum + DiskRun.numberOfUsedBytesOnDisk(this.filename, DiskRun.this.bufferManager, this.pagesize);
		}
	}

	/**
	 * helps to find pagenumber
	 */
	public class SummaryIterator implements Iterator<Entry<K, Integer>> {

		/**
		 * size of the page
		 */
		protected final int pagesize;

		/**
		 * number of the page
		 */
		protected int pagenumber;

		/**
		 * address to read from in page
		 */
		protected int offset;

		/**
		 * pageadress on disk where summary is stored
		 */
		protected PageAddress pageAddress;

		/**
		 * page where entries are stored
		 */
		protected byte[] page;

		/**
		 * last offset where an entry is stored on page
		 */
		protected int maxNumberInPage;

		/**
		 * iterator with entries and their offsets
		 */
		protected IKeyValueIterator<Integer, Entry<K, Integer>> it;

		/**
		 * For performance reasons this object stores the results of the iterator (just one creation of a tuple objects instead for each call of it.next()...)
		 */
		protected final Tuple<Integer, Entry<K, Integer>> resultObject = new Tuple<Integer, Entry<K, Integer>>(null, null);

		/**
		 * Constructor to set parameters
		 * pagesize is set to 8KB, offset is set to 2 and entries for iterator are deserialized
		 *
		 * @see StoreKeyValue
		 * @param pagenumber the page where entries should be read from
		 * @throws java.io.IOException
		 * @throws java.lang.ClassNotFoundException
		 * @throws java.net.URISyntaxException
		 */
		public SummaryIterator(final int summaryPage, final int summaryLevel) throws IOException, ClassNotFoundException, URISyntaxException { //
			this.pagesize = PageManager.getDefaultPageSize();
			this.pagenumber = summaryPage;
			this.offset = 2;
			this.pageAddress = new PageAddress(this.pagenumber, DiskRun.this.summary.getFilename()+summaryLevel);
			this.page = DiskRun.this.bufferManager.getPage(this.pagesize, this.pageAddress);
			this.maxNumberInPage = (0xFF & this.page[0]) | (0x7F & this.page[1]) << 8;
			this.it = DiskRun.this.storeKeyValue.getNextSumEntries(DiskRun.this.maxNumberOfEntriesToStore, this.page, this.offset, this.maxNumberInPage);
		}

		/**
		 * {@inheritDoc}
		 *
		 */
		@Override
		public boolean hasNext() {
			return this.it.hasNext();
		}

		/**
		 * {@inheritDoc}
		 *
		 * if iterator doesn't have next entries, this method will try to get the next iterator
		 */
		@Override
		public Entry<K, Integer> next() {
			if (this.it.hasNext()) {
				final Tuple<Integer, Entry<K, Integer>> entry = this.it.next(this.resultObject);
				this.offset = entry.getFirst();
				if (!this.it.hasNext()) {
					try {
						this.getNextIterator();
					} catch (ClassNotFoundException | IOException | URISyntaxException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
				return entry.getSecond();
			}
			return null;
		}

		/**
		 * next iterator is generated with new offset
		 */
		protected void getNextIterator() throws ClassNotFoundException, IOException, URISyntaxException {
			if (this.offset >= this.maxNumberInPage) {
				return;
			}
			this.it = DiskRun.this.storeKeyValue.getNextSumEntries(DiskRun.this.maxNumberOfEntriesToStore, this.page, this.offset, this.maxNumberInPage);
		}

	}

	@Override
	public ISIPIterator<K, Container<V>> prefixSearch(final Comparator<K> prefixComparator, final K prefixkey) {
		try {
			if(!this.bloomFilter.getPrefix(prefixkey)){
				return null;
			}
			Integer pagenumber = 0;
			int maxLevel = this.maxSummaryLevel;
			// the following currentSummaries structure is used to optimize a SIP search by logging the current path through the summary, which can be reused for succeeding searches (i.e., a following search path can be only right to this logged path through the summary)
			@SuppressWarnings("unchecked")
			final Triple<Integer, Entry<K, Integer>, Iterator<Entry<K, Integer>>>[] currentSummaries = new Triple[maxLevel+1];
			// search through the summary to find the correct leaf node...
			while (maxLevel>=0 && this.summary!=null){
				currentSummaries[maxLevel] = this.summary.prefixSearch(prefixComparator, prefixkey, pagenumber, maxLevel);
				pagenumber = currentSummaries[maxLevel].getFirst();
				maxLevel--;
			}
			if (pagenumber == null) {
				return null;
			}
			final int finalpagenumber = pagenumber;
			return new ISIPIterator<K, Container<V>>(){

				/**
				 * whether or not the iterator is already finished (i.e., is false if the iterator definitely does not have any entries any more)
				 */
				boolean finished;

				/**
				 * A current entry to be returned next.
				 * Is only not null if hasNext() has been called...
				 */
				Entry<K, Container<V>> currentEntry;

				DiskRunIterator it;
				{
					this.it = new DiskRunIterator(false, finalpagenumber);
					while(this.it.hasNext()) {
						this.currentEntry = this.it.next();
						final int comparison = prefixComparator.compare(prefixkey, this.currentEntry.getKey());
						if (comparison < 0){
							this.finished = true;
							break;
						}
						if (comparison == 0) {
							this.finished = false;
							break;
						}
					}
					if(!this.it.hasNext()){
						this.finished = true;
					}
				}

				@Override
				public boolean hasNext() {
					if(this.finished){
						return false;
					}
					if(this.currentEntry!=null){
						return true;
					}
					this.currentEntry = this.next();
					return (this.currentEntry!=null);
				}

				@Override
				public Entry<K, Container<V>> next() {
					if(this.finished){
						return null;
					}
					if(this.currentEntry!=null){
						final Entry<K, Container<V>> result = this.currentEntry;
						this.currentEntry = null;
						return result;
					}
					Entry<K, Container<V>> result = this.it.next();
					if(result==null){
						try {
							this.it.getNextIterator();
							if(this.it.hasNext()){
								result = this.it.next();
							} else {
								this.finished = true;
								return null;
							}
						} catch (ClassNotFoundException | IOException | URISyntaxException e) {
							System.err.println(e);
							e.printStackTrace();
							this.finished = true;
							return null;
						}
					}
					final int comparison = prefixComparator.compare(prefixkey, result.getKey());
					if(comparison<0){
						this.finished = true;
						return null;
					} else if(comparison>0){
						throw new RuntimeException("This should never happen: The keys are stored in the wrong order in "+DiskRun.this.filename);
					}
					return result;
				}

				@Override
				public Entry<K, Container<V>> next(final K k) {
					if(this.finished){
						return null;
					}
					Entry<K, Container<V>> result;
					if(this.currentEntry!=null){
						result = this.currentEntry;
						this.currentEntry = null;
						if(DiskRun.this.comp.compare(result.getKey(), k)>=0){
							return result;
						}
					}
					boolean tookNextIterator = false;
					boolean wentOverSummary = false;
					do {
						result = this.it.next();
						if(result==null){
							try {
								if(tookNextIterator){
									if(wentOverSummary){
										// We already went over the summary one time => just go to the right until entry is found or end is reached!
										this.it.getNextIterator();
										if(this.it.hasNext()){
											result = this.it.next();
										} else {
											this.finished = true;
											return null;
										}
									} else {
										wentOverSummary = true;
										// a whole page is read without getting a key >= k
										// go along the summary to go directly to a page, which contains keys >= k
										final int pagenumber = this.getPageNumberForSIP(k);
										if(pagenumber<0){
											this.finished = true;
											return null;
										}
										if(this.it.pagenumber==pagenumber){
											this.it.getNextIterator();
										} else {
											this.it = new DiskRunIterator(false, pagenumber);
										}
										if(this.it.hasNext()){
											result = this.it.next();
										} else {
											this.finished = true;
											return null;
										}
									}
								} else {
									tookNextIterator = true;
									this.it.getNextIterator();
									if(this.it.hasNext()){
										result = this.it.next();
									} else {
										this.finished = true;
										return null;
									}
								}
							} catch (ClassNotFoundException | IOException | URISyntaxException e) {
								System.err.println(e);
								e.printStackTrace();
								this.finished = true;
								return null;
							}
						}
					} while(DiskRun.this.comp.compare(result.getKey(), k)<0);
					return result;
				}

				/**
				 * Find the leaf node for a given key (or the leaf node with the next-closest larger key).
				 * First the summary is going up (in order to avoid every time to go to the whole summary from the top level to the lowest level)...
				 * After finding the right summary level, the summary is traversed to the lower ones to finally find the correct leaf node.
				 *
				 * @param k
				 * @return
				 * @throws ClassNotFoundException
				 * @throws IOException
				 * @throws URISyntaxException
				 */
				private final int getPageNumberForSIP(final K k) throws ClassNotFoundException, IOException, URISyntaxException{
					int level = 0;
					while(level<currentSummaries.length){
						final Iterator<Entry<K, Integer>> currentIterator = currentSummaries[level].getThird();
						if(currentIterator!=null){
							while(currentIterator.hasNext()){
								final Entry<K, Integer> sumEntry = currentIterator.next();
								currentSummaries[level].setFirst(sumEntry.getValue());
								currentSummaries[level].setSecond(sumEntry);
								final int comparison = DiskRun.this.comp.compare(sumEntry.getKey(), k);
								if(comparison>0){
									final int pagenumber = sumEntry.getValue() - 1;
									currentSummaries[level].setFirst(pagenumber);
									if(level==0){
										return pagenumber;
									} else {
										// continue to search in lower levels of summary
										return this.getPagenumberGoingTheSummaryDown(level-1, pagenumber, k);
									}
								} else if(comparison==0){
									final int pagenumber = sumEntry.getValue();
									if(currentIterator.hasNext()){
										final Entry<K, Integer> sumEntry2 = currentIterator.next(); // just to make this case symmetric to comparison>0!
										currentSummaries[level].setFirst(sumEntry2.getValue()-1);
										currentSummaries[level].setSecond(sumEntry2);
									} else {
										currentSummaries[level].setThird(null); // to mark that the right-most child has been visited
									}
									if(level==0){
										return pagenumber;
									} else {
										// continue to search in lower levels of summary
										return this.getPagenumberGoingTheSummaryDown(level-1, pagenumber, k);
									}
								}
							}
							// check if last pointer in node must be visited:
							int upperLevel = level+1;
							while(upperLevel<currentSummaries.length){
								if(currentSummaries[upperLevel].getSecond()!=null){
									final K upperKey = currentSummaries[upperLevel].getSecond().getKey();
									if(DiskRun.this.comp.compare(k, upperKey)<0){
										return this.getPagenumberGoingTheSummaryDown(level, currentSummaries[upperLevel].getSecond().getValue(), k);
									} else {
										break;
									}
								}
								upperLevel++;
							}
							if(upperLevel>=currentSummaries.length){
								// special case top level: just go to the right!
								return this.getPagenumberGoingTheSummaryDown(level-1, currentSummaries[level].getSecond().getValue(), k);
							}
						} // else: already went down according to last pointer in node!
						level++;
					}
					// top level reached
					return -1;
				}

				/**
				 * Iterate through the given summary-level to find the leaf node which could contain the key (or the next-closest larger key)
				 *
				 * @param key
				 * @param summaryPage
				 * @param summaryLevel
				 * @return
				 * @throws ClassNotFoundException
				 * @throws IOException
				 * @throws URISyntaxException
				 */
				public int getPagenumberIteratingOneSummaryLevel(final K key, final int summaryPage, final int summaryLevel) throws ClassNotFoundException, IOException, URISyntaxException {
					final SummaryIterator it = new SummaryIterator(summaryPage, summaryLevel);
					currentSummaries[summaryLevel].setThird(it);
					Entry<K, Integer> entry = null;
					while (it.hasNext()) {
						entry = it.next();
						currentSummaries[summaryLevel].setFirst(entry.getValue());
						currentSummaries[summaryLevel].setSecond(entry);
						final int comparison = DiskRun.this.comp.compare(key, entry.getKey());
						if(comparison < 0) {
							final int result = entry.getValue() - 1;
							entry.setValue(result);
							return result;
						} else if(comparison == 0) { // load current page...
							if(it.hasNext()){
								final Entry<K, Integer> sumEntry2 = it.next(); // just to make this case symmetric to comparison<0!
								currentSummaries[summaryLevel].setFirst(sumEntry2.getValue()-1);
								currentSummaries[summaryLevel].setSecond(sumEntry2);
							} else {
								currentSummaries[summaryLevel].setThird(null); // to mark that the right-most child has been visited
							}
							return entry.getValue();
						}
					}
					if (entry == null) {
						return -1;
					}
					if (DiskRun.this.comp.compare(key, entry.getKey()) >= 0) {
						return entry.getValue();
					}
					return -1;
				}

				/**
				 * Go down in the summary hierarchy to find the key (or the next-closest larger key)
				 *
				 * @param currentLevel
				 * @param pagenumber
				 * @param key
				 * @return
				 * @throws ClassNotFoundException
				 * @throws IOException
				 * @throws URISyntaxException
				 */
				public int getPagenumberGoingTheSummaryDown(int currentLevel, int pagenumber, final K key) throws ClassNotFoundException, IOException, URISyntaxException {
					while (currentLevel>=0 && DiskRun.this.summary!=null){
						pagenumber = this.getPagenumberIteratingOneSummaryLevel(key, pagenumber, currentLevel);
						currentLevel--;
					}
					return pagenumber;
				}
			};
		} catch (ClassNotFoundException | IOException | URISyntaxException e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void clear() {
		this.release();
	}

	@Override
	public void addRun(final Generator<K, V> generator) throws ClassNotFoundException, IOException, URISyntaxException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeLuposObject(final OutputStream loos) throws IOException, ClassNotFoundException, URISyntaxException {
		if(this.summary==null){
			OutHelper.writeLuposIntVariableBytes(0, loos);
		} else {
			OutHelper.writeLuposIntVariableBytes(this.maxSummaryLevel+1, loos);
		}
		this.bloomFilter.writeLuposObject(loos);
	}

	@Override
	public void readLuposObject(final InputStream lois) throws IOException, ClassNotFoundException, URISyntaxException {
		this.maxSummaryLevel = InputHelper.readLuposIntVariableBytes(lois) - 1;
		if(this.maxSummaryLevel>=0){
			this.summary = new Summary(this.comp, this.storeKeyValue, this.level, this.number, 0);
			ISummary<K> currentSummary = this.summary;
			for(int i=1; i<this.maxSummaryLevel; i++){
				currentSummary = currentSummary.createSummaryOfTheSummary();
			}
		}
		this.bloomFilter.readLuposObject(lois);
	}

	/**
	 * deletes all files starting with the given filename and ending with _X (where X is a number), as generated by the buffer manager
	 *
	 * @param filename the given prefix filename
	 */
	public static void deleteFiles(final String filename){
		int i = 0;
		boolean deleted;
		do {
			deleted = FileHelper.deleteFile(filename+"_"+i);
			i++;
		} while(deleted);
	}

	/**
	 * Determines the number of used bytes on disk (without wasted bytes to complete pages)
	 * It is assumed that the first two bytes of a page of the file to be investigated contain the number of bytes used in this page,
	 * and that the highest bit of the second byte is set if this is the last page in the file.
	 * This format is used for lsm tree runs and summary files.
	 *
	 * @param filename the file to be investigated
	 * @return the number of used bytes on disk (without wasted bytes to complete pages)
	 * @throws IOException
	 */
	public static long numberOfUsedBytesOnDisk(final String filename, final BufferManager bufferManager, final int pagesize) throws IOException{
		long result = 0;
		int pagenumber = 0;
		byte[] page;
		do{
			final PageAddress pageAddress = new PageAddress(pagenumber, filename);
			page = bufferManager.getPage(pagesize, pageAddress);
			result += (0xFF & page[0]) | (0x7F & page[1]) << 8;
			pagenumber++;
		} while((page[1] & 0x80) != 0);
		return result;
	}

	/**
	 * Determines the number of used bytes on disk of files handled by the buffer manager.
	 * Please make sure that all pages in the buffer manager are written out.
	 *
	 * @param filename the name of the file
	 * @return the number of used bytes on disk (with wasted bytes to complete pages)
	 */
	public static long numberOfBytesOnDisk(final String filename){
		long sum = 0;
		int i = 0;
		long filelength;
		do{
			filelength = new File(filename+"_"+i).length();
			sum += filelength;
			i++;
		} while(filelength>0);
		return sum;
	}

	@Override
	public long numberOfUsedBytesOnDisk() throws IOException {
		final long sum;
		if(this.summary!=null){
			sum = this.summary.numberOfUsedBytesOnDisk();
		} else {
			sum = 0;
		}
		return sum + DiskRun.numberOfUsedBytesOnDisk(this.filename, this.bufferManager, PageManager.getDefaultPageSize());
	}

	@Override
	public long numberOfBytesOnDisk() {
		final long sum;
		if(this.summary!=null){
			sum = this.summary.numberOfBytesOnDisk();
		} else {
			sum = 0;
		}
		return sum + DiskRun.numberOfBytesOnDisk(this.filename);
	}

	@Override
	public String getStructureInfo() throws IOException {
		final long numberOfBytes = this.numberOfBytesOnDisk();
		return "Bytes used/wasted (#pages): " + this.numberOfUsedBytesOnDisk() + "/" + numberOfBytes + " ("+(numberOfBytes/PageManager.getDefaultPageSize())+")";
	}
}
