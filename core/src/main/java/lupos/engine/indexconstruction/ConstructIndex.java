package lupos.engine.indexconstruction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.items.IntArrayComparator;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.lsmtree.LSMTree;
import lupos.datastructures.lsmtree.level.Container;
import lupos.datastructures.lsmtree.level.disk.store.IStoreKeyValue;
import lupos.datastructures.lsmtree.level.disk.store.StoreIntTriple;
import lupos.datastructures.lsmtree.level.disk.store.StoreIntTriple.IntTripleComparator;
import lupos.datastructures.lsmtree.level.disk.store.StoreKeyValue;
import lupos.datastructures.lsmtree.level.disk.store.StoreUncompressedIntTriple;
import lupos.datastructures.lsmtree.level.factory.DiskLevelFactory;
import lupos.datastructures.lsmtree.level.factory.ILevelFactory;
import lupos.datastructures.lsmtree.level.factory.IMemoryLevelFactory;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.IntArrayNodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.NodeDeSerializer;
import lupos.datastructures.paged_dbbptree.node.nodedeserializer.StringIntegerNodeDeSerializer;
import lupos.datastructures.patriciatrie.diskseq.DBSeqTrieSet;
import lupos.datastructures.stringarray.StringArray;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.indexconstruction.implementation.GlobalIDsGenerator;
import lupos.engine.indexconstruction.implementation.IntegerStringDictionaryGenerator;
import lupos.engine.indexconstruction.implementation.LocalIDsGenerator;
import lupos.engine.indexconstruction.implementation.ReadTriples;
import lupos.engine.indexconstruction.implementation.SavePatTrie;
import lupos.engine.indexconstruction.implementation.StringShortener;
import lupos.engine.indexconstruction.implementation.TemporaryIDsGenerator;
import lupos.engine.indexconstruction.implementation.TrieMerger;
import lupos.engine.indexconstruction.implementation.dbbptree.DBBPTreeIndicesGenerator;
import lupos.engine.indexconstruction.implementation.dbbptree.IntArrayDBBPTreeIndicesGenerator;
import lupos.engine.indexconstruction.implementation.dbbptree.StringIntegerDBBPTreeDictionaryGenerator;
import lupos.engine.indexconstruction.implementation.incremental.AllIndicesWriter;
import lupos.engine.indexconstruction.implementation.incremental.EvaluationIndicesInserter;
import lupos.engine.indexconstruction.implementation.incremental.GlobalIDsGeneratorForIncrementalInsertion;
import lupos.engine.indexconstruction.implementation.indices.DBBPTreeContainer;
import lupos.engine.indexconstruction.implementation.indices.LSMTreeContainer;
import lupos.engine.indexconstruction.implementation.lsmtree.LSMTreeIndicesGenerator;
import lupos.engine.indexconstruction.implementation.lsmtree.StringIntegerLSMTreeDictionaryGenerator;
import lupos.engine.indexconstruction.implementation.sorter.InitialRunsMerger;
import lupos.engine.indexconstruction.implementation.sorter.TripleBlockSorter;
import lupos.engine.indexconstruction.interfaces.ICreateReadTriplesFactory;
import lupos.engine.indexconstruction.interfaces.ICreateTripleConsumers;
import lupos.engine.indexconstruction.interfaces.IDictionaryGenerator;
import lupos.engine.indexconstruction.interfaces.IEndOfProcessingNotification;
import lupos.engine.indexconstruction.interfaces.IIndexContainer;
import lupos.engine.indexconstruction.interfaces.IIndicesGenerator;
import lupos.engine.indexconstruction.interfaces.IParseConfiguration;
import lupos.engine.indexconstruction.interfaces.IReadTriples;
import lupos.engine.indexconstruction.interfaces.ITripleConsumerWithEndNotification;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Indices.DATA_STRUCT;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan.CollationOrder;
import lupos.misc.FileHelper;
import lupos.misc.TimeInterval;
import lupos.misc.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructIndex {

	public static boolean lazySortingMemoryLevel = true;
	public static boolean compressed = true;

	public static<K, V> IMemoryLevelFactory<K, V, Iterator<Map.Entry<K,Container<V>>>> createMemoryLevelFactory(){
		if(ConstructIndex.lazySortingMemoryLevel){
			return IMemoryLevelFactory.createMemoryLevelLazySortingFactory();
		} else {
			return IMemoryLevelFactory.createMemoryLevelTreeMapFactory();
		}
	}

	public static IStoreKeyValue<int[], int[]> createStoreIntArray(final CollationOrder collationOrder){
		if(ConstructIndex.compressed){
			return new StoreIntTriple(collationOrder);
		} else {
			return new StoreUncompressedIntTriple(collationOrder);
		}
	}

	public static ICreateTripleConsumers createTripleConsumer = new ICreateTripleConsumers(){
		@Override
		public Tuple<ITripleConsumerWithEndNotification[], IEndOfProcessingNotification> createTripleConsumers(final Map<String, Object> configuration, final List<Tuple<String, Long>> times) {
			final int size = (int) configuration.get("NUMBER_OF_PARALLEL_DICTIONARY_CONSTRUCTORS");
			final ITripleConsumerWithEndNotification[] consumers = new ITripleConsumerWithEndNotification[size];
			final List<DBSeqTrieSet> listOfTries = new LinkedList<DBSeqTrieSet>(); // this list collects the single tries...
			// first phase
			for(int i=0; i<size; i++){
				consumers[i] = new TemporaryIDsGenerator(configuration, new LocalIDsGenerator(new SavePatTrie(configuration, listOfTries, new TripleBlockSorter(configuration, times), times), times), times);
				// DBBPTree can deal with String spanning over two or more pages, but not LSM Tree!
				if(((String)configuration.get("TypeOfIndices")).compareTo("LSMTree")==0){
					consumers[i] = new StringShortener(consumers[i]);
				}
			}
			// second phase
			final IIndicesGenerator indicesGenerator;
			switch((String)configuration.get("TypeOfIndices")){
				case "LSMTree":
					indicesGenerator = new LSMTreeIndicesGenerator(configuration, times);
					break;
				case "IntArrayDBBPTree":
					indicesGenerator = new IntArrayDBBPTreeIndicesGenerator(configuration, times);
					break;
					default:
				case "DBBPTree":
					indicesGenerator = new DBBPTreeIndicesGenerator(configuration, times);
					break;
			}
			final GlobalIDsGenerator globalIDsGenerator = new GlobalIDsGenerator(configuration, listOfTries, new InitialRunsMerger(configuration, indicesGenerator, times), times);
			final IDictionaryGenerator StringIntegerDictionary =
					(((String)configuration.get("TypeOfIndices")).compareTo("LSMTree")==0)?
							new StringIntegerLSMTreeDictionaryGenerator(configuration, globalIDsGenerator, times)
							: new StringIntegerDBBPTreeDictionaryGenerator(globalIDsGenerator, times);
			final IEndOfProcessingNotification trieMerger = new TrieMerger(configuration, listOfTries, new IDictionaryGenerator[]{StringIntegerDictionary, new IntegerStringDictionaryGenerator(indicesGenerator, times)}, times);

			return new Tuple<ITripleConsumerWithEndNotification[], IEndOfProcessingNotification>(consumers, trieMerger);
		}
	};

	public static ICreateTripleConsumers createTripleConsumerForIncrementalInsertion = new ICreateTripleConsumers(){

		@Override
		public Tuple<ITripleConsumerWithEndNotification[], IEndOfProcessingNotification> createTripleConsumers(final Map<String, Object> configuration, final List<Tuple<String, Long>> times) throws IOException {
			final int size = (int) configuration.get("NUMBER_OF_PARALLEL_DICTIONARY_CONSTRUCTORS");
			final String dir = (String) configuration.get("dir");
			final ITripleConsumerWithEndNotification[] consumers = new ITripleConsumerWithEndNotification[size];
			final IIndexContainer<String, Integer> dictionary;
			@SuppressWarnings("unchecked")
			final IIndexContainer<int[], int[]>[] evaluationIndices = new IIndexContainer[CollationOrder.values().length];
			switch((String)configuration.get("TypeOfIndices")){
				case "LSMTreeIncremental":
					dictionary = new LSMTreeContainer<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>>(new LSMTree<String, Integer, Iterator<Map.Entry<String,Container<Integer>>>>("Dictionary", new DiskLevelFactory<String, Integer>(Comparator.<String>naturalOrder(), new StoreKeyValue<String, Integer>(String.class, Integer.class), dir + File.separator + "lsm-tree" + File.separator + "dict" + File.separator, StringIntegerLSMTreeDictionaryGenerator.m, StringIntegerLSMTreeDictionaryGenerator.k, ConstructIndex.createMemoryLevelFactory())));
					for(final CollationOrder collationOrder: CollationOrder.values()){
						final ILevelFactory<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>> lsmtreefactory = new DiskLevelFactory<int[], int[]>(new IntTripleComparator(collationOrder), ConstructIndex.createStoreIntArray(collationOrder), dir + File.separator + "lsm-tree" + File.separator + collationOrder.toString() + File.separator, StringIntegerLSMTreeDictionaryGenerator.m, StringIntegerLSMTreeDictionaryGenerator.k, ConstructIndex.createMemoryLevelFactory());
						final LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>> lsmtree=new LSMTree<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>(collationOrder.name(), lsmtreefactory);
						evaluationIndices[collationOrder.ordinal()] = new LSMTreeContainer<int[], int[], Iterator<Map.Entry<int[],Container<int[]>>>>(lsmtree);
					}
					break;
				default:
				case "IntArrayDBBPTreeIncremental":
					dictionary = new DBBPTreeContainer<String, Integer>(new lupos.datastructures.paged_dbbptree.DBBPTree<String, Integer>(StringIntegerDBBPTreeDictionaryGenerator.k, StringIntegerDBBPTreeDictionaryGenerator.k_, new StringIntegerNodeDeSerializer()));
					for(final CollationOrder collationOrder: CollationOrder.values()){
						final NodeDeSerializer<int[], int[]> nodeDeSerializer = new IntArrayNodeDeSerializer(collationOrder);
				     	final DBBPTree<int[], int[]> tree = new DBBPTree<int[], int[]>(new IntArrayComparator(collationOrder), StringIntegerDBBPTreeDictionaryGenerator.k, StringIntegerDBBPTreeDictionaryGenerator.k_, nodeDeSerializer, int[].class, int[].class);
				     	evaluationIndices[collationOrder.ordinal()] = new DBBPTreeContainer<int[], int[]>(tree);
					}
					break;
			}
			final StringArray stringArray = new StringArray();
			final GlobalIDsGeneratorForIncrementalInsertion globalIDsGenerator = new GlobalIDsGeneratorForIncrementalInsertion(dictionary, stringArray, new EvaluationIndicesInserter(evaluationIndices, times), times);
			// first phase
			for(int i=0; i<size; i++){
				consumers[i] = new TemporaryIDsGenerator(configuration, globalIDsGenerator, times);
				// DBBPTree can deal with String spanning over two or more pages, but not LSM Tree!
				if(((String)configuration.get("TypeOfIndices")).compareTo("LSMTreeIncremental")==0){
					consumers[i] = new StringShortener(consumers[i]);
				}
			}
			// second phase
			final IEndOfProcessingNotification indicesWriter = new AllIndicesWriter(configuration, dictionary, stringArray, evaluationIndices, times);
			return new Tuple<ITripleConsumerWithEndNotification[], IEndOfProcessingNotification>(consumers, indicesWriter);
		}

	};


	public static ICreateReadTriplesFactory createReadTriplesFactory = new ICreateReadTriplesFactory(){
		@Override
		public IReadTriples createReadTriples(final Map<String, Object> configuration, final ITripleConsumerWithEndNotification[] tripleConsumers, final IEndOfProcessingNotification secondPhase) throws URISyntaxException {
			return new ReadTriples(tripleConsumers, secondPhase, (int)configuration.get("NUMBER_OF_PARALLEL_INPUT"), (int)configuration.get("NUMBER_OF_PARALLEL_DICTIONARY_CONSTRUCTORS"), (String)configuration.get("compressor"), (String)configuration.get("dataFormat"), (String[])configuration.get("files"));
		}
	};

	public static IParseConfiguration parseConfiguration = new IParseConfiguration(){

		@Override
		public Map<String, Object> parseConfiguration(final String[] args, int argsoffset, final Map<String, Object> configuration) {
			if(args.length>argsoffset+1) {
				String result_dir = args[argsoffset].toLowerCase();
				if(result_dir.equals("-noresults")) {
					argsoffset++;
				} else if(result_dir.equals("-resultsdir")) {
					if(!result_dir.endsWith("\\") && !result_dir.endsWith("/")) {
						result_dir += File.separator;
					}
					configuration.put("resultsdir", args[argsoffset+1]);
					argsoffset+=2;
				} else if(result_dir.equals("-results")) {
					configuration.put("resultsfile", args[argsoffset+1]);
					argsoffset+=2;
				}
			}

			if(args.length<argsoffset+5){
				final String errortext = "Wrong number of command line arguments! Must be at least "+(argsoffset+5)+"!";
				log.error(errortext);
				log.error("Usage: [-noresults|-results file|-resultsdir dir] <datafile> <dataformat> <encoding> <NONE|BZIP2|HUFFMAN|GZIP> <directory for indices> [LIMIT_TRIPLES_IN_MEMORY [PARALLEL_TRIES [PARALLEL_INPUT [<datafile2> [<datafile3> ...]]]]]");
				log.error("Example: data.n3 N3 UTF-8 NONE /luposdateindex 10000000 1 2");
				throw new RuntimeException(errortext);
			}
			LiteralFactory.setType(LiteralFactory.MapType.NOCODEMAP);
			Indices.setUsedDatastructure(DATA_STRUCT.DBBPTREE);

			final String dataFormat = args[argsoffset+1];
			configuration.put("dataFormat", dataFormat);
			CommonCoreQueryEvaluator.encoding = args[argsoffset+2];
			configuration.put("compressor", args[argsoffset+3]);

			String dir = args[argsoffset+4];

			if(!dir.endsWith("\\") && !dir.endsWith("/")) {
				dir += File.separator;
			}
			// make directory such that we can store something inside!
			final File f_dir = new File(dir);
			f_dir.mkdirs();

			final String[] dirArray = new String[] { dir };
			DBMergeSortedBag.setTmpDir(dirArray);
			DiskCollection.setTmpDir(dirArray);
			lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(dir, true);

			configuration.put("dir", dir);

			configuration.put("writeindexinfo", dir+File.separator+"indices.info");

			if(args.length>argsoffset+5) {
				configuration.put("LIMIT_TRIPLES_IN_MEMORY", Integer.parseInt(args[argsoffset+5]));
			} else {
				configuration.put("LIMIT_TRIPLES_IN_MEMORY", 10000000);
			}
			if(args.length>argsoffset+6 && !dataFormat.startsWith("MULTIPLE")) {
				configuration.put("NUMBER_OF_PARALLEL_DICTIONARY_CONSTRUCTORS", Integer.parseInt(args[argsoffset+6]));
			} else {
				configuration.put("NUMBER_OF_PARALLEL_DICTIONARY_CONSTRUCTORS", 1);
			}
			if(args.length>argsoffset+7) {
				configuration.put("NUMBER_OF_PARALLEL_INPUT", Integer.parseInt(args[argsoffset+7]));
			} else {
				configuration.put("NUMBER_OF_PARALLEL_INPUT", 2);
			}

			final int numberOfFiles = Math.max(0, args.length-argsoffset-8) + 1;
			final String[] files = new String[numberOfFiles];
			files[0] = args[argsoffset];
			for(int i=8+argsoffset; i<args.length; i++) {
				files[i-7-argsoffset] = args[i];
			}
			configuration.put("files", files);

			return configuration;
		}

	};

	private static final Logger log = LoggerFactory.getLogger(ConstructIndex.class);

	public static void main(final String[] args) throws Exception {
		if(args.length<6){
			log.error("Wrong number of command line arguments!");
			log.error("The first argument is (LSMTree | LSMTreeIncremental | IntArrayDBBPTree | DBBPTree)");
			log.error("For LSMTree and LSMTreeIncremental you may further use [-memory m] [-runs k] in order to set up the memory level to hold m elements and the disk based levels to hold k runs");
		}

		final Map<String, Object> configuration = new HashMap<String, Object>();
		int argsoffset = 1;
		switch(args[0]){
			case "LSMTree":
				configuration.put("TypeOfIndices", "LSMTree");
				argsoffset = lsmTreeParams(args, argsoffset, configuration);
				configuration.put("incremental", false);
				break;
			case "LSMTreeIncremental":
				configuration.put("TypeOfIndices", "LSMTreeIncremental");
				argsoffset = lsmTreeParams(args, argsoffset, configuration);
				configuration.put("incremental", true);
				break;
			case "IntArrayDBBPTree":
				configuration.put("TypeOfIndices", "IntArrayDBBPTree");
				configuration.put("incremental", false);
				break;
			case "IntArrayDBBPTreeIncremental":
				configuration.put("TypeOfIndices", "IntArrayDBBPTreeIncremental");
				configuration.put("incremental", true);
				break;
			default:
			case "DBBPTree":
				if(args[0].compareTo("DBBPTree")!=0){
					log.warn("First argument "+args[0]+" not recognized, assuming DBBPTree!");
				}
				configuration.put("TypeOfIndices", "DBBPTree");
				configuration.put("incremental", false);
				break;
		}
		ConstructIndex.main(args, argsoffset, configuration);
	}

	private static int lsmTreeParams(final String[] args, int argsoffset, final Map<String, Object> configuration){
		boolean flag;
		do {
			flag = false;
			switch(args[argsoffset]){
				case "-memory":
					configuration.put("memory", args[argsoffset+1]);
					StringIntegerLSMTreeDictionaryGenerator.m = Integer.parseInt(args[argsoffset+1]);
					argsoffset += 2;
					flag = true;
					break;
				case "-runs":
					configuration.put("runs", args[argsoffset+1]);
					StringIntegerLSMTreeDictionaryGenerator.k = Integer.parseInt(args[argsoffset+1]);
					argsoffset += 2;
					flag = true;
					break;
				case "-lazysorting":
					configuration.put("sorting", "lazy");
					ConstructIndex.lazySortingMemoryLevel = true;
					argsoffset++;
					flag = true;
					break;
				case "-treemap":
					configuration.put("sorting", "treemap");
					ConstructIndex.lazySortingMemoryLevel = false;
					argsoffset++;
					flag = true;
					break;
				case "-uncompressed":
					configuration.put("compression", "uncompressed");
					ConstructIndex.compressed = false;
					argsoffset++;
					flag = true;
					break;
				case "-compressed":
					configuration.put("compression", "compressed");
					ConstructIndex.compressed = true;
					argsoffset++;
					flag = true;
					break;
				case "-space":
					configuration.put("space", "all");
					argsoffset++;
					flag = true;
					break;
			}
		} while(flag);
		return argsoffset;
	}

	public static void main(final String[] args, final int argsoffset, Map<String, Object> configuration) throws Exception {
		org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.INFO); // to switch off logging of debug messages
		// org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.ALL); // to switch on logging of all messages
		// org.apache.log4j.LogManager.getRootLogger().setLevel(org.apache.log4j.Level.OFF); // to switch off logging
		log.info("Starting program to construct an RDF3X Index for LUPOSDATE...");
		log.info("[help is printed when using less than 5 command line arguments]");
		log.info("_______________________________________________________________");
		log.info("Parsing command line arguments...");
		configuration = ConstructIndex.parseConfiguration.parseConfiguration(args, argsoffset, configuration);
		log.info("Deleting and creating directory...");
		final String dir = (String) configuration.get("dir");
		final File filedir = new File(dir);
		FileHelper.deleteDirectory(filedir);
		filedir.mkdirs();
		final List<Tuple<String, Long>> times = Collections.synchronizedList(new ArrayList<Tuple<String, Long>>(16));
		final long start = System.currentTimeMillis();
		log.info("Start time: {}", (new Date(start)).toString() + " ("+start+")");
		log.info("...");
		final Tuple<ITripleConsumerWithEndNotification[], IEndOfProcessingNotification> firstAndSecondPhase =
				(boolean)configuration.get("incremental")?
				ConstructIndex.createTripleConsumerForIncrementalInsertion.createTripleConsumers(configuration, times)
				: ConstructIndex.createTripleConsumer.createTripleConsumers(configuration, times);
		final IReadTriples readTriples = ConstructIndex.createReadTriplesFactory.createReadTriples(configuration, firstAndSecondPhase.getFirst(), firstAndSecondPhase.getSecond());
		readTriples.readTriples();
		final long end = System.currentTimeMillis();
		log.info("_______________________________________________________________");
		log.info("Done, RDF3X index constructed!");
		log.info("End time: {}", (new Date(end)).toString() + " ("+end+")");
		log.info("Statistics:");
		times.add(new Tuple<String, Long>("Total time (including glue code and sometimes clean up, which may not be included in the other times)", end-start));

		if (configuration.get("resultsdir") != null || configuration.get("resultsfile") != null) {
			//Create File + Dir
			final File file = (configuration.get("resultsdir") != null)?
					new File(configuration.get("resultsdir").toString()
					+ configuration.get("TypeOfIndices") + "_"
					+ configuration.get("memory") + "_"
					+ configuration.get("runs") + "_"
					+ configuration.get("sorting") + "_"
					+ configuration.get("compression") + ".dat") :
					new File(configuration.get("resultsfile").toString());
			file.getParentFile().mkdirs();
			FileWriter fstream;
			boolean first = false;
			if (file.exists()) {
				fstream = new FileWriter(file, true);//if file exists append to file.
			} else {
				file.createNewFile();
				fstream = new FileWriter(file);
				first = true;
			}
			final BufferedWriter out = new BufferedWriter(fstream);
			if (first) {
				int i = 1;
				for (final Tuple<String, Long> entry : times) {
					out.write("# " + i + ": ");
					out.write(entry.getFirst() + " ");
					out.newLine();
					i++;
				}

			}
			for(final Tuple<String, Long> entry: times){
				out.write(entry.getSecond().toString()+" ");
			}
			out.newLine();
			out.close();
		}
		for(final Tuple<String, Long> entry: times){
			if(entry.getFirst().startsWith("No time: ")){
				log.info("{}: {}", entry.getFirst().substring(9), entry.getSecond());
			} else {
				log.info("Used time: {}: {} ({} msec)", entry.getFirst(), new TimeInterval(entry.getSecond()), entry.getSecond());
			}
		}
	}
}
