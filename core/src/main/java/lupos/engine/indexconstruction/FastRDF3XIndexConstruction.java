/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.engine.indexconstruction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lupos.compression.Compression;
import lupos.datastructures.buffermanager.BufferManager;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.MapEntry;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.Heap.HEAPTYPE;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.TripleKey;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree.Generator;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringIntegerNodeDeSerializer;
import lupos.datastructures.patriciatrie.TrieSet;
import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieSet;
import lupos.datastructures.patriciatrie.exception.TrieNotCopyableException;
import lupos.datastructures.patriciatrie.ram.RBTrieMap;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.DATA_STRUCT;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.io.helper.InputHelper;
import lupos.io.helper.OutHelper;
import lupos.misc.FileHelper;
import lupos.misc.TimeInterval;
import lupos.misc.util.ImmutableIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class constructs the RDF3X indices on disk using a dictionary, which is
 * also constructed on disk...
 *
 * @author groppe
 * @version $Id: $Id
 */
public class FastRDF3XIndexConstruction {

	private static final Logger log = LoggerFactory.getLogger(FastRDF3XIndexConstruction.class);

	// the parameters are used for the B+-trees
	private static final int k = 1000;
	private static final int k_ = 1000;

	private static final int NUMBER_OF_THREADS = 8;

	// how many triples are loaded into main memory to be sorted in the initial runs?
	/** Constant <code>LIMIT_TRIPLES_IN_MEMORY=50000000</code> */
	public static int LIMIT_TRIPLES_IN_MEMORY = 50000000;

	// just for mapping from 0 to 2 to S, P and O
	/** Constant <code>map="new String[]{S, P, O}"</code> */
	protected final static String[] map = new String[]{"S", "P", "O"};


	/**
	 * Constructs the large-scale indices for RDF3X.
	 * The command line arguments are
	 * datafile dataformat encoding NONE|BZIP2|HUFFMAN|GZIP directory_for_indices [LIMIT_TRIPLES_IN_MEMORY [datafile2 [datafile3 ...]]]
	 * If you want to import more than one file you can use the additional parameters datafilei!
	 *
	 * @param args
	 *            command line arguments
	 */
	public static void main(final String[] args) {
		try {

			log.info("Starting program to construct an RDF3X Index for LUPOSDATE...");
			log.debug("[help is printed when using less than 5 command line arguments]");
			log.debug("_______________________________________________________________");

			if (args.length < 5) {
				log.error("Usage: java -Xmx768M lupos.engine.indexconstruction.FastRDF3XIndexConstruction <datafile> <dataformat> <encoding> <NONE|BZIP2|HUFFMAN|GZIP> <directory for indices> [LIMIT_TRIPLES_IN_MEMORY [<datafile2> [<datafile3> ...]]]");
				log.error("Example: java -Xmx768M lupos.engine.indexconstruction.FastRDF3XIndexConstruction data.n3 N3 UTF-8 NONE /luposdateindex 500000");
				return;
			}

			// analyze command line parameters
			final Date start = new Date();
			log.debug("Starting time: {}", start);

			LiteralFactory.setType(LiteralFactory.MapType.NOCODEMAP);
			Indices.setUsedDatastructure(DATA_STRUCT.DBBPTREE);

			final String datafile = args[0];
			final String dataFormat = args[1];
			CommonCoreQueryEvaluator.encoding = args[2];

			final String compressor = args[3];
			if(compressor.compareTo("BZIP2")==0){
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.BZIP2);
			} else if(compressor.compareTo("HUFFMAN")==0){
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.HUFFMAN);
			} else if(compressor.compareTo("GZIP")==0){
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.GZIP);
			} else {
				SortConfiguration.setDEFAULT_COMPRESSION(Compression.NONE);
			}

			String dir = args[4];
			if(!dir.endsWith("\\") && !dir.endsWith("/")) {
				dir += "/";
			}
			// make directory such that we can store something inside!
			final File f_dir = new File(dir);
			f_dir.mkdirs();

			final String[] dirArray = new String[] { dir };
			final String writeindexinfo = dirArray[0]+File.separator+RDF3XQueryEvaluator.INDICESINFOFILE;
			DBMergeSortedBag.setTmpDir(dirArray);
			DiskCollection.setTmpDir(dirArray);
			lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(dir, true);

			final Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
			defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + datafile+ ">"));

			if(args.length>5) {
				FastRDF3XIndexConstruction.LIMIT_TRIPLES_IN_MEMORY = Integer.parseInt(args[5]);
			}
			for(int i=6; i<args.length; i++) {
				defaultGraphs.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:" + args[i]+ ">"));
			}

			// first generate block-wise dictionaries for each block and id-triples with local ids of the local dictionary
			final CreateLocalDictionaryAndLocalIds runGenerator = new CreateLocalDictionaryAndLocalIds(dir);
			for(final URILiteral uri: defaultGraphs) {
				try {
					CommonCoreQueryEvaluator.readTriples(dataFormat, uri.openStream(), runGenerator);
				} catch (final Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			runGenerator.endOfBlock();

			// merge local dictionaries
			final List<TrieSet> listOfTries = runGenerator.getTries();
			final TrieSet final_trie = new DBSeqTrieSet(dir + "FinalTrie");
			if(listOfTries.size()>1){
				final_trie.merge(listOfTries);
			} else {
				final_trie.copy(listOfTries.get(0));
			}

			// create real dictionary
			final Generator<String, Integer> smsi = new Generator<String, Integer>() {

				@Override
				public Iterator<java.util.Map.Entry<String, Integer>> iterator() {
					return new ImmutableIterator<java.util.Map.Entry<String, Integer>>() {

						Iterator<String> it = final_trie.iterator();
						int index = 1;

						@Override
						public boolean hasNext() {
							return this.it.hasNext();
						}

						@Override
						public Entry<String, Integer> next() {
							return new MapEntry<String, Integer>(this.it.next(), this.index++);
						}
					};
				}

				@Override
				public int size() {
					return final_trie.size();
				}
			};

			final lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer> simap = new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(
						k,
						k_,
						new StringIntegerNodeDeSerializer());

			final Thread thread0 = new Thread() {
				@Override
				public void run() {

					try {
						simap.generateDBBPTree(smsi);
						LazyLiteral.setHm(new StringIntegerMapJava(simap));
					} catch (final IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			};
			final Thread thread1 = new Thread() {
				@Override
				public void run() {
					StringArray ismap;
					try {
						ismap = new StringArray();
						ismap.generate(final_trie.iterator());
						LazyLiteral.setV(ismap);
					} catch (final IOException e) {
						log.error(e.getMessage(), e);
					}
				}
			};
			// TODO make thread-safe!
			LiteralFactory.setTypeWithoutInitializing(LiteralFactory.MapType.LAZYLITERALWITHOUTINITIALPREFIXCODEMAP);
			thread0.run();
			thread1.run();
			try {
				thread0.join();
				thread1.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
			final_trie.release();

			// map local ids to global ids of initial runs
			int runNumber = 0;
			for(final TrieSet trie: listOfTries) {
				// determine mapping
				final int[] mapping = new int[trie.size()];
				final SIPParallelIterator<java.util.Map.Entry<String, Integer>, String> iterator = (SIPParallelIterator<java.util.Map.Entry<String, Integer>, String>) simap.entrySet().iterator();

				int local_id =0;
				for(final String key: trie){
					final java.util.Map.Entry<String, Integer> entry = iterator.next(key);
					if(entry.getKey().compareTo(key)!=0){
						log.error("Local string not in global dictionary! Cannot be without any other error => Abort!");
						System.exit(0);
					}
					mapping[local_id] = entry.getValue();
					local_id++;
				}

				iterator.close();

				// local trie is not needed any more!
				trie.release();
				// map all 6 different initial runs of this trie
				final Thread[] threads = new Thread[6];
				for(int primaryPos=0; primaryPos<3; primaryPos++) {
					final int other_condition1 = (primaryPos==0)?1:0;
					final int other_condition2 = (primaryPos==2)?1:2;
					final String prefixFilename = dir + FastRDF3XIndexConstruction.map[primaryPos] + "_Run_"+runNumber+"_";
					threads[primaryPos*2] = new LocalToGlobalIdMapper(prefixFilename + FastRDF3XIndexConstruction.map[other_condition1] + FastRDF3XIndexConstruction.map[other_condition2], mapping);
					threads[primaryPos*2].start();
					threads[primaryPos*2 + 1] = new LocalToGlobalIdMapper(prefixFilename + FastRDF3XIndexConstruction.map[other_condition2] + FastRDF3XIndexConstruction.map[other_condition1], mapping);
					threads[primaryPos*2 + 1].start();
				}

				// wait for the six threads for finishing their job (otherwise maybe too much memory consumption)
				for(final Thread thread: threads){
					thread.join();
				}

				runNumber++;
			}

			// merge initial runs...
			int size = 0;
			for(int primaryPos=0; primaryPos<3; primaryPos++) {
				final int other_condition1 = (primaryPos==0)?1:0;
				final int other_condition2 = (primaryPos==2)?1:2;
				size = FastRDF3XIndexConstruction.mergeRuns(dir, listOfTries.size(), primaryPos, other_condition1, other_condition2);
				size = FastRDF3XIndexConstruction.mergeRuns(dir, listOfTries.size(), primaryPos, other_condition2, other_condition1);
			}

			// generate indices (evaluation indices plus histogram indices)
			final SixIndices indices = new SixIndices(defaultGraphs.iterator().next());

			for(int primaryPos=0; primaryPos<3; primaryPos++) {
				final int other_condition1 = (primaryPos==0)?1:0;
				final int other_condition2 = (primaryPos==2)?1:2;
				final CollationOrder order1 = CollationOrder.valueOf(FastRDF3XIndexConstruction.map[primaryPos] + FastRDF3XIndexConstruction.map[other_condition1] + FastRDF3XIndexConstruction.map[other_condition2]);
				final String prefixFilename = dir + FastRDF3XIndexConstruction.map[primaryPos] + "_Final_Run_";
				indices.generate(order1, new GeneratorFromFinalRun(prefixFilename + FastRDF3XIndexConstruction.map[other_condition1] + FastRDF3XIndexConstruction.map[other_condition2], size, primaryPos, other_condition1, other_condition2));
				indices.generateStatistics(order1);

				final CollationOrder order2 = CollationOrder.valueOf(FastRDF3XIndexConstruction.map[primaryPos] + FastRDF3XIndexConstruction.map[other_condition2] + FastRDF3XIndexConstruction.map[other_condition1]);
				indices.generate(order2, new GeneratorFromFinalRun(prefixFilename + FastRDF3XIndexConstruction.map[other_condition2] + FastRDF3XIndexConstruction.map[other_condition1], size, primaryPos, other_condition2, other_condition1));
				indices.generateStatistics(order2);
			}

			indices.constructCompletely();

			// write out index info

			final OutputStream out = new BufferedOutputStream(new FileOutputStream(writeindexinfo));

			BufferManager.getBufferManager().writeAllModifiedPages();

			OutHelper.writeLuposInt(lupos.datastructures.paged_dbbptree.DBBPTree.getCurrentFileID(), out);

			((lupos.datastructures.paged_dbbptree.DBBPTree) ((StringIntegerMapJava) LazyLiteral.getHm()).getOriginalMap()).writeLuposObject(out);
			((StringArray) LazyLiteral.getV()).writeLuposStringArray(out);
			OutHelper.writeLuposInt(1, out);
			LiteralFactory.writeLuposLiteral(defaultGraphs.iterator().next(), out);
			indices.writeIndexInfo(out);
			OutHelper.writeLuposInt(0, out);
			out.close();
			final Date end = new Date();
			log.debug("_______________________________________________________________");
			log.info("Done, RDF3X index constructed!");
			log.debug("End time: {}", end);

			log.debug("Used time: {}", new TimeInterval(start, end));
			log.debug("Number of imported triples: {}", indices.getIndex(CollationOrder.SPO).size());
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * merges the initial runs into a final run...
	 *
	 * @param prefixFilename a {@link java.lang.String} object.
	 * @param numberOfRuns a int.
	 * @param primaryPos a int.
	 * @param secondaryPos a int.
	 * @param tertiaryPos a int.
	 * @throws java.io.IOException if any.
	 * @return a int.
	 */
	public static int mergeRuns(final String prefixFilename, final int numberOfRuns, final int primaryPos, final int secondaryPos, final int tertiaryPos) throws IOException {
		int size = 0;
		// use a heap for merging the smallest elements of the runs
		final Heap<HeapElementContainer> mergeHeap = Heap.createInstance(numberOfRuns, true, HEAPTYPE.OPTIMIZEDSEQUENTIAL);
		final IteratorFromRun[] iterators = new IteratorFromRun[numberOfRuns];
		for(int i=0; i<numberOfRuns; i++) {
			iterators[i] = new IteratorFromRun(prefixFilename + FastRDF3XIndexConstruction.map[primaryPos] + "_Run_"+i+"_"+ FastRDF3XIndexConstruction.map[secondaryPos] + FastRDF3XIndexConstruction.map[tertiaryPos] + "_mapped");
			final int[] first = iterators[i].next();
			if(first!=null){
				mergeHeap.add(new HeapElementContainer(first, i));
			}
		}

		final FinalRunWriter out = new FinalRunWriter(prefixFilename + FastRDF3XIndexConstruction.map[primaryPos] + "_Final_Run_"+ FastRDF3XIndexConstruction.map[secondaryPos] + FastRDF3XIndexConstruction.map[tertiaryPos]);
		int[] lastTriple = null;
		while(!mergeHeap.isEmpty()) {
			final HeapElementContainer element = mergeHeap.pop();
			final int run = element.getRun();
			final int[] toBeAdded = iterators[run].next();
			if(toBeAdded!=null){
				mergeHeap.add(new HeapElementContainer(toBeAdded, run));
			}
			final int[] triple = element.getTriple();
			if(lastTriple == null || lastTriple[0]!=triple[0] || lastTriple[1]!=triple[1] || lastTriple[2]!=triple[2]) {
				// store triple in final run
				out.write(triple);
				lastTriple = triple;
				size++;
			}
		}
		out.close();

		// delete all intermediate runs
		for(int i=0; i<numberOfRuns; i++) {
			FileHelper.deleteFile(prefixFilename + FastRDF3XIndexConstruction.map[primaryPos] + "_Run_"+i+"_"+ FastRDF3XIndexConstruction.map[secondaryPos] + FastRDF3XIndexConstruction.map[tertiaryPos] + "_mapped");
		}

		return size;
	}

	/**
	 * for creating a 'local' dictionary for each initial run and creating id-triples of the initial runs according to the local dictionary
	 */
	public static class CreateLocalDictionaryAndLocalIds implements TripleConsumer{

		private final RBTrieMap<Integer> map = new RBTrieMap<Integer>();
		private final int[][] blockOfIdTriples = new int[FastRDF3XIndexConstruction.LIMIT_TRIPLES_IN_MEMORY][];
		private int index = 0;
		private int runNumber = 0;
		private final List<TrieSet> listOfTries = new LinkedList<TrieSet>();
		private final String dir;

		public CreateLocalDictionaryAndLocalIds(final String dir){
			this.dir = dir;
		}

		@Override
		public void consume(final Triple triple) {
			final int[] idtriple = new int[3];
			int i=0;
			for(final Literal literal: triple){
				idtriple[i] = this.insertIntoMap(literal.toString());
				if(literal.originalStringDiffers()){
					this.insertIntoMap(literal.originalString());
				}
				i++;
			}
			this.blockOfIdTriples[this.index] = idtriple;
			this.index++;
			if(this.index>=FastRDF3XIndexConstruction.LIMIT_TRIPLES_IN_MEMORY){
				this.endOfBlock();
			}
		}

		private int insertIntoMap(final String value) {
			Integer code = this.map.get(value);
			if(code==null) {
				code = this.map.size();
				this.map.put(value, code);
			}
			return code;
		}

		public void endOfBlock(){
			if(this.index==0){
				return;
			}
			// create mapping preliminary id of triples => local id of triples, which reflects the order
			final int[] mapping = new int[this.map.size()];
			int local_id = 0;
			for(final Entry<String, Integer> entry: this.map) {
				mapping[entry.getValue()] = local_id;
				local_id++;
			}
			// apply mapping to id triples
			for(int i=0; i<this.index; i++) {
				final int[] triple = this.blockOfIdTriples[i];
				for(int j=0; j<3; j++) {
					triple[j] = mapping[triple[j]];
				}
			}
			// write out patricia trie
			final DBSeqTrieSet disk_set = new DBSeqTrieSet(this.dir+"Set_"+this.runNumber);
			try {
				disk_set.copy(this.map);
				this.listOfTries.add(disk_set);
			} catch (final TrieNotCopyableException e) {
				log.error(e.getMessage(), e);
			}
			// free resources of map in main memory
			this.map.clear();

			// sort id triples according to six collation orders and write them out as runs (in parallel)...
			final Thread threadS = new CountingSorter(this.blockOfIdTriples, this.index, 0, this.dir + "S_Run_"+this.runNumber+"_", mapping.length);
			threadS.start();
			final Thread threadP = new CountingSorter(this.blockOfIdTriples, this.index, 1, this.dir + "P_Run_"+this.runNumber+"_", mapping.length);
			threadP.start();
			final Thread threadO = new CountingSorter(this.blockOfIdTriples, this.index, 2, this.dir + "O_Run_"+this.runNumber+"_", mapping.length);
			threadO.start();

			try {
				threadS.join();
				threadP.join();
				threadO.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}

			this.runNumber++;
			this.index = 0;
		}

		public List<TrieSet> getTries(){
			return this.listOfTries;
		}
	}

	/**
	 * Sort the id-triples of the initial runs with counting sort according to the primary sort condition,
	 * use quicksort to afterwards sort it according to the secondary and tertiary sort criteria
	 * (e.g. first sort according to S, P and O, afterwards sort according to SPO and SOP based on the
	 * sorting according to S, PSO and POS based on P, and OSP and OPS based on O.)
	 */
	public static class CountingSorter extends Thread {

		private final int[][] blockOfIdTriples;
		private final int end;
		private final int pos;
		private final String filePrefix;
		private final int max_code;

		public CountingSorter(final int[][] blockOfIdTriples, final int end, final int pos, final String filePrefix, final int max_code) {
			this.blockOfIdTriples = blockOfIdTriples;
			this.end = end;
			this.pos = pos;
			this.filePrefix = filePrefix;
			this.max_code = max_code;
		}

		@Override
		public void run(){
			// start counting sort
			final int[] numberOfOccurences = new int[this.max_code];
			int numberOfBorders = 0;
			for(int i=0; i<this.end; i++){
				final int index = this.blockOfIdTriples[i][this.pos];
				if(numberOfOccurences[index]==0){
					numberOfBorders++;
				}
				numberOfOccurences[index]++;
			}
			// calculate addresses and borders...
			final int[] borders = new int[numberOfBorders];
			int index_borders = 0;
			if(numberOfOccurences[0]!=0) {
				borders[0] = numberOfOccurences[0];
				index_borders = 1;
			}
			for(int i=0; i < this.max_code - 1; i++){
				final boolean flag = (numberOfOccurences[i+1]>0);
				numberOfOccurences[i+1] = numberOfOccurences[i] + numberOfOccurences[i+1];
				if(flag) {
					borders[index_borders] = numberOfOccurences[i+1];
					index_borders++;
				}
			}
			// do sorting
			final int[][] blockOfSortedIdTriples = new int[this.end][];
			for(int i=0; i<this.end; i++){
				final int key = this.blockOfIdTriples[i][this.pos];
				blockOfSortedIdTriples[numberOfOccurences[key] - 1]=this.blockOfIdTriples[i];
				numberOfOccurences[key]--;
			}
			// now we have to do sorting according to the secondary and tertiary condition
			final int other_condition1 = (this.pos==0)?1:0;
			final int other_condition2 = (this.pos==2)?1:2;

			final Thread thread1 = new SecondaryConditionSorter(blockOfSortedIdTriples, this.pos, other_condition1, other_condition2, borders, this.filePrefix);
			thread1.start();
			final Thread thread2 = new SecondaryConditionSorter(blockOfSortedIdTriples, this.pos, other_condition2, other_condition1, borders, this.filePrefix);
			thread2.start();
			try {
				thread1.join();
				thread2.join();
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Sort routines for sorting according to the secondary and tertiary sort criterium...
	 */
	public static class SecondaryConditionSorter extends Thread {

		private final int[][] blockOfSortedIdTriples;
		private final int primary_pos;
		private final int secondary_pos;
		private final int tertiary_pos;
		private final int[] borders;
		private final String fileName;

		public SecondaryConditionSorter(final int[][] blockOfSortedIdTriples, final int primary_pos, final int secondary_pos, final int tertiary_pos, final int[] borders, final String filePrefix) {
			this.blockOfSortedIdTriples = blockOfSortedIdTriples;
			this.primary_pos = primary_pos;
			this.secondary_pos = secondary_pos;
			this.tertiary_pos = tertiary_pos;
			this.borders = borders;
			this.fileName = filePrefix + FastRDF3XIndexConstruction.map[this.secondary_pos] + FastRDF3XIndexConstruction.map[this.tertiary_pos] ;
		}

		@Override
		public void run() {
			final int[][] blockOfFinallySortedIdTriples = new int[this.blockOfSortedIdTriples.length][];
			final ExecutorService executor = Executors.newFixedThreadPool(FastRDF3XIndexConstruction.NUMBER_OF_THREADS);
			int last_index = 0;
			for(int i=0; i<this.borders.length; i++){
				final int current_end = this.borders[i];
				executor.submit(new BasicSorter(this.blockOfSortedIdTriples, blockOfFinallySortedIdTriples, this.secondary_pos, this.tertiary_pos, last_index, current_end));
				last_index = current_end;
			}
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.DAYS);
			} catch (final InterruptedException e) {
				log.error(e.getMessage(), e);
			};

			// write out initial run:
			try {
				final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.fileName));
				// write run in a compressed way:
				int start = 0;
				int previousPrimaryCode = 0;
				for(int i=0; i<this.borders.length; i++) {
					final int end = this.borders[i];
					previousPrimaryCode = FastRDF3XIndexConstruction.writeBlock(blockOfFinallySortedIdTriples, start, end, this.primary_pos, this.secondary_pos, this.tertiary_pos, previousPrimaryCode, out);
					start = end;
				}
				out.close();
			} catch (final FileNotFoundException e) {
				log.error(e.getMessage(), e);
			} catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Write out a 'block'. A block has the same id at the primary position, and is sorted according to the secondary and tertiary sort criteria.
	 *
	 * @param block
	 * @param start
	 * @param end
	 * @param primaryPos
	 * @param secondaryPos
	 * @param tertiaryPos
	 * @param previousPrimaryCode
	 * @param out
	 * @return
	 * @throws IOException
	 */
	private static int writeBlock(final int[][] block, final int start, final int end, final int primaryPos, final int secondaryPos, final int tertiaryPos, final int previousPrimaryCode, final OutputStream out) throws IOException{
		final int primaryCode = block[start][primaryPos];
		// difference encoding for the primary position
		OutHelper.writeLuposIntVariableBytes(primaryCode - previousPrimaryCode, out);

		// how many times this primary code is repeated?
		OutHelper.writeLuposIntVariableBytes(end-start, out);

		int previousSecondaryCode = 0;
		int previousTertiaryCode = 0;
		for(int j=start; j<end; j++) {
			final int secondaryCode = block[j][secondaryPos];
			// use difference encoding also for the secondary position
			final int differenceSecondaryPosition = secondaryCode - previousSecondaryCode;

			OutHelper.writeLuposIntVariableBytes(differenceSecondaryPosition, out);
			if(differenceSecondaryPosition>0){
				// difference encoding cannot be used for the tertiary position, as the secondary position changed
				previousTertiaryCode = 0;
			}
			final int tertiaryCode = block[j][tertiaryPos];
			OutHelper.writeLuposIntVariableBytes(tertiaryCode - previousTertiaryCode, out);

			previousSecondaryCode = secondaryCode;
			previousTertiaryCode = tertiaryCode;
		}
		return primaryCode;
	}

	/**
	 * Quicksort for sorting a 'block' according to the secondary and tertiary sort criteria.
	 * A 'block' is a set of triples with the same id at the primary position.
	 */
	public static class BasicSorter extends Thread {

		private final int[][] blockOfSortedIdTriples;
		private final int secondary_pos;
		private final int tertiary_pos;
		private final int[][] blockOfFinallySortedIdTriples;
		private final int start;
		private final int end;

		public BasicSorter(final int[][] blockOfSortedIdTriples, final int[][] blockOfFinallySortedIdTriples, final int secondary_pos, final int tertiary_pos, final int start, final int end) {
			this.blockOfSortedIdTriples = blockOfSortedIdTriples;
			this.blockOfFinallySortedIdTriples = blockOfFinallySortedIdTriples;
			this.secondary_pos = secondary_pos;
			this.tertiary_pos = tertiary_pos;
			this.start = start;
			this.end = end;
		}

		@Override
		public void run(){
			System.arraycopy(this.blockOfSortedIdTriples, this.start, this.blockOfFinallySortedIdTriples, this.start, this.end - this.start);
			this.quicksort(this.start, this.end - 1);
		}

		private void quicksort(final int low, final int high) {
			int i = low, j = high;
			// Get the pivot element from the middle of the list
			final int[] pivot = this.blockOfFinallySortedIdTriples[low + (high-low)/2];

			// Divide into two lists
			while (i <= j) {
				// If the current value from the left list is smaller then the pivot
				// element then get the next element from the left list.
				// Check primary and tertiary sort condition!
				while (this.blockOfFinallySortedIdTriples[i][this.secondary_pos] < pivot[this.secondary_pos] ||
						(this.blockOfFinallySortedIdTriples[i][this.secondary_pos] == pivot[this.secondary_pos] &&
								this.blockOfFinallySortedIdTriples[i][this.tertiary_pos] < pivot[this.tertiary_pos])) {
					i++;
				}
				// If the current value from the right list is larger then the pivot
				// element then get the next element from the right list.
				// Check primary and tertiary sort condition!
				while (this.blockOfFinallySortedIdTriples[j][this.secondary_pos] > pivot[this.secondary_pos] ||
						(this.blockOfFinallySortedIdTriples[j][this.secondary_pos] == pivot[this.secondary_pos] &&
						this.blockOfFinallySortedIdTriples[j][this.tertiary_pos] > pivot[this.tertiary_pos])) {
					j--;
				}

				// If we have found a values in the left list which is larger then
				// the pivot element and if we have found a value in the right list
				// which is smaller then the pivot element then we exchange the
				// values.
				// As we are done we can increase i and j
				if (i <= j) {
					final int[] tmp = this.blockOfFinallySortedIdTriples[i];
					this.blockOfFinallySortedIdTriples[i] = this.blockOfFinallySortedIdTriples[j];
					this.blockOfFinallySortedIdTriples[j] =tmp;
					i++;
					j--;
				}
			}
			// Recursion
			if (low < j){
				this.quicksort(low, j);
			}
			if (i < high){
				this.quicksort(i, high);
			}
		}
	}

	/**
	 * Maps the initial runs with their local ids to the global ids of the global dictionary
	 */
	public static class LocalToGlobalIdMapper extends Thread {

		private final String filename;
		private final int[] mapping;

		public LocalToGlobalIdMapper(final String filename, final int[] mapping) {
			this.filename = filename;
			this.mapping = mapping;
		}

		@Override
		public void run() {
			try {
				final InputStream in = new BufferedInputStream(new FileInputStream(this.filename));
				final OutputStream out = new BufferedOutputStream(new FileOutputStream(this.filename + "_mapped"));

				int previousPrimaryCode = 0;
				int previousMappedPrimaryCode = 0;
				Integer primaryCode;
				while((primaryCode = InputHelper.readLuposIntVariableBytes(in)) != null){
					primaryCode+=previousPrimaryCode;
					final int primaryMappedCode = this.mapping[primaryCode];
					OutHelper.writeLuposIntVariableBytes(primaryMappedCode - previousMappedPrimaryCode, out);

					final int repetitions = InputHelper.readLuposIntVariableBytes(in);
					OutHelper.writeLuposIntVariableBytes(repetitions, out);

					int previousSecondaryCode = 0;
					int previousMappedSecondaryCode = 0;
					int previousTertiaryCode = 0;
					int previousMappedTertiaryCode = 0;

					for(int i=0; i<repetitions; i++) {
						final int secondaryCode = InputHelper.readLuposIntVariableBytes(in) + previousSecondaryCode;
						final int secondaryMappedCode = this.mapping[secondaryCode];
						OutHelper.writeLuposIntVariableBytes(secondaryMappedCode - previousMappedSecondaryCode, out);
						if(secondaryMappedCode != previousMappedSecondaryCode) {
							previousTertiaryCode = 0;
							previousMappedTertiaryCode = 0;
						}

						final int tertiaryCode = InputHelper.readLuposIntVariableBytes(in) + previousTertiaryCode;
						final int tertiaryMappedCode = this.mapping[tertiaryCode];
						OutHelper.writeLuposIntVariableBytes(tertiaryMappedCode - previousMappedTertiaryCode, out);

						previousMappedSecondaryCode = secondaryMappedCode;
						previousSecondaryCode = secondaryCode;
						previousMappedTertiaryCode = tertiaryMappedCode;
						previousTertiaryCode = tertiaryCode;
					}

					previousPrimaryCode = primaryCode;
					previousMappedPrimaryCode = primaryMappedCode;
				}

				out.close();
				in.close();
				FileHelper.deleteFile(this.filename);
			} catch (final FileNotFoundException e) {
				log.error(e.getMessage(), e);
			} catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Just for iterating through a run
	 */
	public static class IteratorFromRun {

		private final InputStream in;

		int previousPrimaryCode = 0;
		int previousSecondaryCode = 0;
		int previousTertiaryCode = 0;
		int leftWithSamePrimaryCode = 0;

		public IteratorFromRun(final String filename) throws EOFException, FileNotFoundException, IOException {
			this.in = new BufferedInputStream(new FileInputStream(filename));
		}

		public int[] next() throws IOException {
			if(this.leftWithSamePrimaryCode==0) {
				final Integer code = InputHelper.readLuposIntVariableBytes(this.in);
				if(code == null){
					this.in.close();
					return null;
				}
				this.previousPrimaryCode += code;
				this.previousSecondaryCode = 0;
				this.previousTertiaryCode = 0;
				this.leftWithSamePrimaryCode = InputHelper.readLuposIntVariableBytes(this.in);
			}
			final int code = InputHelper.readLuposIntVariableBytes(this.in);
			if(code > 0) {
				this.previousTertiaryCode = 0;
			}
			this.previousSecondaryCode += code;
			this.previousTertiaryCode += InputHelper.readLuposIntVariableBytes(this.in);
			this.leftWithSamePrimaryCode--;
			return new int[] { this.previousPrimaryCode, this.previousSecondaryCode, this.previousTertiaryCode};
		}
	}

	/**
	 * This is just for generating B+-trees from the final run
	 */
	public static class GeneratorFromFinalRun implements Generator<TripleKey, Triple>{

		private final String filename;
		private final int size;
		private int primaryMap;
		private int secondaryMap;
		private int tertiaryMap;
		private final CollationOrder order;

		public GeneratorFromFinalRun(final String filename, final int size, final int primaryPos, final int secondaryPos, final int tertiaryPos) {
			this.filename = filename;
			this.size = size;
			switch(primaryPos){
				default:
				case 0:
					this.primaryMap = 0;
					break;
				case 1:
					this.secondaryMap = 0;
					break;
				case 2:
					this.tertiaryMap = 0;
					break;
			}
			switch(secondaryPos){
				default:
				case 0:
					this.primaryMap = 1;
					break;
				case 1:
					this.secondaryMap = 1;
					break;
				case 2:
					this.tertiaryMap = 1;
					break;
			}
			switch(tertiaryPos){
				default:
				case 0:
					this.primaryMap = 2;
					break;
				case 1:
					this.secondaryMap = 2;
					break;
				case 2:
					this.tertiaryMap = 2;
					break;
			}
			this.order = CollationOrder.createCollationOrder(primaryPos, secondaryPos);
		}

		@Override
		public int size() {
			return this.size;
		}

		@Override
		public Iterator<Entry<TripleKey, Triple>> iterator() {
			try {
				return new ImmutableIterator<Entry<TripleKey, Triple>>(){

					IteratorFromRun it = new IteratorFromRun(GeneratorFromFinalRun.this.filename);
					int[] triple = this.it.next();

					@Override
					public boolean hasNext() {
						return this.triple!=null;
					}

					@Override
					public Entry<TripleKey, Triple> next() {
						if(this.triple==null){
							return null;
						}
						final Triple t = new Triple(new LazyLiteral(this.triple[GeneratorFromFinalRun.this.primaryMap]),
								new LazyLiteral(this.triple[GeneratorFromFinalRun.this.secondaryMap]),
								new LazyLiteral(this.triple[GeneratorFromFinalRun.this.tertiaryMap]));

						final TripleKey key = new TripleKey(t, GeneratorFromFinalRun.this.order);

						try {
							this.triple = this.it.next();

						} catch (final IOException e) {
							log.error(e.getMessage(), e);
							this.triple = null;
						}

						return new MapEntry<TripleKey, Triple>(key, t);
					}
				};
			} catch (final IOException e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}

	}

	/**
	 * This is a container for merging the initial runs...
	 */
	public static class HeapElementContainer implements Comparable<HeapElementContainer> {

		private final int[] triple;
		private final int run;

		public HeapElementContainer(final int[] triple, final int run){
			this.triple= triple;
			this.run = run;
		}

		@Override
		public final int compareTo(final HeapElementContainer o) {
			for(int pos=0; pos<3; pos++) {
				final int compare = this.triple[pos] - o.triple[pos];
				if(compare!=0){
					return compare;
				}
			}
			return 0;
		}

		public final int[] getTriple() {
			return this.triple;
		}

		public final int getRun() {
			return this.run;
		}
	}

	/**
	 * This is just for writing the final run
	 */
	public static class FinalRunWriter {

		private final static int MAX = 8 * 1024;
		private final int[][] block = new int[FinalRunWriter.MAX][];;
		private int current = 0;
		private int previousPrimaryCode = 0;
		private final OutputStream out;

		public FinalRunWriter(final String filename) throws FileNotFoundException, IOException {
			this.out = new BufferedOutputStream(new FileOutputStream(filename));
		}

		public void write(final int[] triple) {
			if(this.current==FinalRunWriter.MAX || (this.current>0 && this.block[this.current-1][0]!=triple[0])) {
				this.writeBlock();
			}
			this.block[this.current] = triple;
			this.current++;
		}

		private void writeBlock() {
			if(this.current>0) {
				try {
					this.previousPrimaryCode = FastRDF3XIndexConstruction.writeBlock(this.block, 0, this.current, 0, 1, 2, this.previousPrimaryCode, this.out);
				} catch (final IOException e) {
					log.error(e.getMessage(), e);
				}
				this.current = 0;
			}
		}

		public void close() throws IOException {
			this.writeBlock();
			this.out.close();
		}
	}
}
