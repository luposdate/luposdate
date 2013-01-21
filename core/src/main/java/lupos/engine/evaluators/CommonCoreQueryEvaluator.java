/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.engine.evaluators;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lupos.compression.Compression;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.dbmergesortedds.DBMergeSortedBag;
import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.dbmergesortedds.SortConfiguration;
import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.heap.SortAndMergeHeap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.trie.SuperTrie;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.application.CollectAllResults;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.multiinput.join.parallel.ParallelJoin;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.parallel.ParallelOperand;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.misc.FileHelper;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.optimizations.logical.rules.parallel.RuleEngineForParallelOperator;
import lupos.optimizations.logical.rules.parallel.RuleJoinWithParallelOperands;
import lupos.rdf.Prefix;
import lupos.rdf.parser.Parser;
import lupos.rdf.parser.YagoParser;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;

public abstract class CommonCoreQueryEvaluator<A> extends QueryEvaluator<A> {
	/**
	 * Set this to the root node of the operator graph. Used by
	 * CommonCoreQueryEvaluator.getOperatorGraph()
	 */
	protected BasicOperator rootNode;

	/**
	 * Set this to specify which optimizations should be applied if none are
	 * specified on the command line. This should be an instance of an enum type
	 * defining all the optimizations possible with the given QueryEvaluator and
	 * should be set before super is called in setupArguments.
	 */
	@SuppressWarnings("rawtypes")
	protected Enum defaultOptimization;

	public enum JOIN {
		DEFAULT, NESTEDLOOP, HASHMAPINDEX, DBBPTREEINDEX, SMALLERINHASHMAPLARGERINDBBPTREEINDEX, MERGE, DBMERGE, DBMERGEITERATIONS, HASH, PARALLELNESTEDLOOP, PARALLELHASHMAPINDEX, PARALLELDBBPTREEINDEX, PARALLELSMALLERINHASHMAPLARGERINDBBPTREEINDEX, PARALLELMERGE, PARALLELDBMERGE, PARALLELHASH;
	}

	protected enum SORT {
		DEFAULT, DBMERGESORT, TREEMAP, QUICKSORT, INSERTIONSORT, SMALLERINTREEMAPLARGERINDBMERGE;
	}

	public enum DISTINCT {
		DEFAULT, DBSETBLOCKING, HASHSETBLOCKING, HASHSET, SMALLERINHASHSETLARGERINDBSET, LAZYHASHSETBLOCKING;
	}

	public enum STORAGE {
		MEMORY, DISK, HYBRID
	}

	protected enum MERGE_JOIN_OPTIONAL {
		SEQUENTIAL, PARALLEL, SORTSEQUENTIAL
	}

	protected enum FORMAT {
		N3, Detect, RDFXML, Turtle, Yago, ParallelN3, SesameNTriples, SesameRDFXML, SesameTurtle, NQUADS, BZIP2NQUADS
	}

	public static String encoding = "UTF-8";

	protected Result result;
	protected STORAGE storage;
	public JOIN join;
	public JOIN optional;
	public SORT sort;
	public DISTINCT distinct;
	public MERGE_JOIN_OPTIONAL merge_join_optional;
	public RDFS rdfs;
	protected boolean inmemoryexternalontologyinference;
	protected String externalontology;
	protected String type;

	public enum RDFS {
		NONE, RDFS, RUDIMENTARYRDFS, ALTERNATIVERDFS, OPTIMIZEDRDFS, OPTIMIZEDRUDIMENTARYRDFS, OPTIMIZEDALTERNATIVERDFS
	}

	public static Collection<String> supportedCharSets() {
		return Charset.availableCharsets().keySet();
	}
	
	private PARALLELOPERANDS parallelOperands;

	public void parallelOperator(final BasicOperator bo) {
		switch (this.parallelOperands) {
		case LAST:
		case ALL:
			final RuleEngineForParallelOperator refpo = new RuleEngineForParallelOperator();
			refpo.applyRules(bo);
			break;
		}
	}

	public List<DebugContainer<BasicOperatorByteArray>> parallelOperatorDebugByteArray(
			final BasicOperator bo, final Prefix prefixInstance) {
		switch (this.parallelOperands) {
		case ALL:
		case LAST:
			final RuleEngineForParallelOperator refpo = new RuleEngineForParallelOperator();
			return refpo.applyRulesDebugByteArray(bo, prefixInstance);
		default:
			return null;
		}
	}

	public CommonCoreQueryEvaluator() throws Exception {
		super();
	}

	public CommonCoreQueryEvaluator(final String[] args) throws Exception {
		super(args);
	}

	public CommonCoreQueryEvaluator(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset,
			final String type, final String externalontology,
			final boolean inmemoryexternalontologyinference, final RDFS rdfs,
			final LiteralFactory.MapType codemap, final String[] tmpDirs,
			final boolean loadindexinfo,
			final PARALLELOPERANDS parallelOperands, final boolean blockwise,
			final int limit, final int jointhreads, final int joinbuffer,
			final Heap.HEAPTYPE heap, final ToSort.TOSORT tosort,
			final int indexheap, final int mergeheapheight,
			final Heap.HEAPTYPE mergeheaptype, final int chunk,
			final int mergethreads, final int yagomax,
			final SuperTrie.TRIETYPE stringsearch,
			final QueryResult.TYPE resulttype, final STORAGE storage,
			final JOIN join, final JOIN optional, final SORT sort,
			final DISTINCT distinct,
			final MERGE_JOIN_OPTIONAL merge_join_optional, final String encoding){
		super(debug, multiplequeries, compare, compareoptions, times, dataset);
		init(type, externalontology,inmemoryexternalontologyinference, rdfs, codemap, tmpDirs, loadindexinfo,
				parallelOperands, blockwise,
				limit,jointhreads,joinbuffer,
				heap, tosort, indexheap, mergeheapheight, mergeheaptype, chunk, mergethreads, yagomax,
				stringsearch, resulttype, storage, join, optional, sort, distinct,
				merge_join_optional, encoding);
	}

	
	private void init(final String type, final String externalontology,
			final boolean inmemoryexternalontologyinference, final RDFS rdfs,
			final LiteralFactory.MapType codemap, final String[] tmpDirs,
			final boolean loadindexinfo,
			final PARALLELOPERANDS parallelOperands, final boolean blockwise,
			final int limit, final int jointhreads, final int joinbuffer,
			final Heap.HEAPTYPE heap, final ToSort.TOSORT tosort,
			final int indexheap, final int mergeheapheight,
			final Heap.HEAPTYPE mergeheaptype, final int chunk,
			final int mergethreads, final int yagomax,
			final SuperTrie.TRIETYPE stringsearch,
			final QueryResult.TYPE resulttype, final STORAGE storage,
			final JOIN join, final JOIN optional, final SORT sort,
			final DISTINCT distinct,
			final MERGE_JOIN_OPTIONAL merge_join_optional, final String encoding) {
		this.type = type;
		this.externalontology = externalontology;
		this.inmemoryexternalontologyinference = inmemoryexternalontologyinference;

		this.rdfs = rdfs;
		LiteralFactory.setType(codemap);
		DBMergeSortedBag.setTmpDir(tmpDirs);
		DiskCollection.setTmpDir(tmpDirs);
		if (loadindexinfo) {
			lupos.datastructures.paged_dbbptree.DBBPTree.setTmpDir(tmpDirs[0],
					!loadindexinfo);
		}

		this.parallelOperands = parallelOperands;
		RuleJoinWithParallelOperands.setBLOCKWISE(blockwise);
		if (parallelOperands == PARALLELOPERANDS.LAST) {
			RuleEngineForParallelOperator.setLastJoin(true);
		} else if (parallelOperands == PARALLELOPERANDS.ALL) {
			RuleEngineForParallelOperator.setLastJoin(false);
		}
		if (limit > 0)
			ParallelOperand.setQueueLimit(limit);
		ParallelJoin.setDEFAULT_NUMBER_THREADS(jointhreads);
		ParallelJoin.setMAXBUFFER(joinbuffer);
		
		SortConfiguration.setDEFAULT_HEAP_TYPE(heap);
		SortConfiguration.setDEFAULT_TOSORT(tosort);

		SortConfiguration.setDEFAULT_TOSORT_SIZE(indexheap);
		SortConfiguration.setDEFAULT_HEIGHT(indexheap);
		
		SortConfiguration.setDEFAULT_MERGE_HEAP_HEIGHT(mergeheapheight);
		SortConfiguration.setDEFAULT_MERGEHEAP_TYPE(mergeheaptype);
		
		SortConfiguration.setDEFAULT_K(chunk);
		
		if (mergethreads <= 1) {
			DBMergeSortedBag.setParallelMerging(false);
		} else {
			DBMergeSortedBag.setParallelMerging(true);
			DBMergeSortedBag.setNumberOfThreads(mergethreads);
		}
		Parser.setMaxTriples(yagomax);

		SuperTrie.TYPE = stringsearch;

		QueryResult.type=resulttype;
		this.storage = storage;

		this.join = join;
		this.optional = optional;
		this.sort = sort;
		this.distinct = distinct;
		this.merge_join_optional = merge_join_optional;
		CommonCoreQueryEvaluator.encoding = encoding;
	}

	public void setResult(final Result result) {
		this.result = result;
	}

	public Result getResultOperator() {
		return this.result;
	}

	public enum PARALLELOPERANDS {
		NONE, ALL, LAST
	};

	/**
	 * Specifies arguments accepted by all descendants from
	 * CommonCoreQueryEvaluator. When overriding this, make sure to call
	 * super.setupArguments() and that defaultOptimization is set before you do.
	 */
	@Override
	public void setupArguments() {
		super.setupArguments();
		args.addEnumOption("stringsearch",
				"The type of trie used if a trie is applied...", SuperTrie.TYPE);
		args.addEnumOption("optimization",
				"Specify the optimization that should be applied",
				defaultOptimization);
		args.addEnumOption("join", "Specify the join algorithm to be used",
				JOIN.DEFAULT);
		args.addEnumOption(
				"optional",
				"Specify the join algorithm to be used in the optional operator",
				JOIN.DEFAULT);
		args.addEnumOption("sort", "Specify the sort algorithm to be used",
				SORT.DEFAULT);
		args.addEnumOption("distinct",
				"Specify the distinct algorithm to be used", DISTINCT.DEFAULT);
		args.addEnumOption("result", "Specify the storage type of the result",
				QueryResult.type);
		args.addEnumOption(
				"codemap",
				"The type of map used for administrating the codes for values.",
				LiteralFactory.MapType.PREFIXCODEMAP);
		args.addEnumOption(
				"storage",
				"The type of storage used for all operators, indices and (intermediate) results.",
				STORAGE.MEMORY);
		args.addStringOption(
				"tmpdir",
				"The type of storage used for all operators, indices and (intermediate) results. You can also specify several temporary directories (separated by commas) in order to e.g. use several hard disks for performance reasons.",
				"");
		args.addEnumOption(
				"merge_join_optional",
				"Specifies if MergeJoinWithoutSorting, MergeJoinSort, MergeWithoutSortingOptional and MergeJoinOptional should be replaced by their parallel versions.",
				MERGE_JOIN_OPTIONAL.SEQUENTIAL);
		args.addStringOption(
				"paralleloperands",
				"specifies whether or not ParallelOperands are added as operands of joins in order to compute operand results in parallel. The string option can be \"NONE\" for do not add ParallelOperators, \"N\", where N is a number, which is used as size for the Bounded Queue (the default is used when N<=0), and \"lastN\" if only the last join should get ParallelOperators (N like before). Furthermore, if the the string starts with BLOCK, then the computed operand results are transmitted blockwise to the join/optional operator.",
				"NONE");
		args.addIntegerOption(
				"jointhreads",
				"specifies the number of threads to start for the parallel join operators...",
				ParallelJoin.getDEFAULT_NUMBER_THREADS());
		args.addIntegerOption(
				"joinbuffer",
				"specifies the maximum size for the buffer for the parallel join operators...",
				ParallelJoin.getMAXBUFFER());		
		args.addEnumOption("heap", "specifies the heap type to be used",
				SortConfiguration.getDEFAULT_HEAP_TYPE());
		args.addIntegerOption("indexheap",
				"specifies the heap height used for index construction",
				SortConfiguration.getDEFAULT_HEIGHT());
		args.addEnumOption(
				"tosort",
				"specifies the tosort type to be used in heaps for the initial runs (if no heaps are used, i.e. tosort!=NONE)",
				SortConfiguration.getDEFAULT_TOSORT());
		args.addEnumOption(
				"mergeheaptype",
				"The heap type to be used for merging the initial runs for external merge sort.",
				SortConfiguration.getDEFAULT_MERGEHEAP_TYPE());
		args.addIntegerOption(
				"mergeheapheight",
				"The heap size to be used for merging the initial runs for external merge sort.",
				SortConfiguration.getDEFAULT_MERGE_HEAP_HEIGHT());

		args.addIntegerOption(
				"mergethreads",
				"specifies the number of threads to start for the merging phase of merge sort (=1 means sequential merging).",
				DBMergeSortedBag.getNumberOfThreads());
		args.addStringOption(
				"externalontology",
				"specifies an external ontology, which is used to optimize RDFS inference by replacing triple patterns adressing RDFS triple by concrete ones regarding the external ontology...",
				"");
		args.addIntegerOption(
				"chunk",
				"defines the chunk fraction of the data for SortedChunksHeap...",
				SortConfiguration.getDEFAULT_K());
		args.addIntegerOption(
				"yagomax",
				"specifies the maximum triples read by the YAGO parser (<=0 for all triples)...",
				YagoParser.getMaxTriples());
		args.addBooleanOption(
				"inmemoryexternalontologyinference",
				"specifies if the inference computations in external ontologies are done in memory (or disks are used for temporary data)",
				false);
		args.addStringOption("encoding",
				"specifies the used encoding for reading in data files...",
				encoding);
	}

	@Override
	public void init() throws Exception {
		super.init();

		PARALLELOPERANDS parallelOperands;
		boolean blockwise = false;
		int limit = 0;

		String s = args.getString("paralleloperands").toUpperCase();
		if (s.compareTo("NONE") == 0)
			parallelOperands = PARALLELOPERANDS.NONE;
		else {
			if (s.startsWith("BLOCK")) {
				s = s.substring(5);
				blockwise = true;
			}
			if (s.startsWith("LAST")) {
				s = s.substring(4);
				parallelOperands = PARALLELOPERANDS.LAST;
			} else {
				parallelOperands = PARALLELOPERANDS.ALL;
			}
			try {
				limit = Integer.parseInt(s);
			} catch (final NumberFormatException nfe) {
				System.err
						.println("An integer was expected as parameter of --paralleloperands instead of "
								+ s + "!");
				System.err.println("The parameter will be ignored...");
			}
		}

		init(args.getString("type"), args.getString("externalontology"),
				args.getBool("inmemoryexternalontologyinference"),
				(RDFS) args.getEnum("rdfs"),
				(LiteralFactory.MapType) args.getEnum("codemap"), args
						.getString("tmpdir").split(","),
				args.get("loadindexinfo") != null,
				parallelOperands, blockwise,
				limit, args.getInt("jointhreads"), args.getInt("joinbuffer"),
				(Heap.HEAPTYPE) args.getEnum("heap"),
				(ToSort.TOSORT) args.getEnum("tosort"),
				args.getInt("indexheap"), args.getInt("mergeheapheight"),
				(Heap.HEAPTYPE) args.getEnum("mergeheaptype"),
				args.getInt("chunk"), args.getInt("mergethreads"),
				args.getInt("yagomax"),
				(SuperTrie.TRIETYPE) args.getEnum("stringsearch"),
				(QueryResult.TYPE) args.getEnum("result"),
				(STORAGE) args.getEnum("storage"), (JOIN) args.getEnum("join"),
				(JOIN) args.getEnum("optional"), (SORT) args.getEnum("sort"),
				(DISTINCT) args.getEnum("distinct"),
				(MERGE_JOIN_OPTIONAL) args.getEnum("merge_join_optional"),
				args.getString("encoding"));
	}
	
	public abstract IndexScanCreatorInterface createIndexScanCreator();
	
	@SuppressWarnings("deprecation")
	public void setBindingsVariablesBasedOnOperatorgraph(){
		BindingsArray.forceVariables(getAllVariablesOfQuery());
	}

	/**
	 * 
	 * @return all used variables in the current query (also those, which are not projected)
	 * @see getVariablesOfQuery()
	 */
	@SuppressWarnings("serial")
	public Set<Variable> getAllVariablesOfQuery(){
		final Set<Variable> maxVariables = new TreeSet<Variable>();
		this.rootNode.visit(new SimpleOperatorGraphVisitor() {
			public Object visit(final BasicOperator basicOperator) {
				if (basicOperator.getUnionVariables() != null)
					maxVariables.addAll(basicOperator.getUnionVariables());
				return null;
			}

		});
		return maxVariables;
	}

	/**
	 * 
	 * @return the variables which may occur in the result of the current query, but not all used variables in the query (e.g., variables are left away, which are not projected)
	 * @see getAllVariablesOfQuery()
	 */
	public Set<Variable> getVariablesOfQuery(){
		return new HashSet<Variable>(this.getResultOperator().getUnionVariables());
	}
	
	@Override
	public List<DebugContainer<BasicOperatorByteArray>> physicalOptimizationDebugByteArray(final Prefix prefixInstance) {
		physicalOptimization();
		final LinkedList<DebugContainer<BasicOperatorByteArray>> debug = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
		debug.add(new DebugContainer<BasicOperatorByteArray>(
				"After physical optimization...", "physicaloptimizationRule",
				BasicOperatorByteArray.getBasicOperatorByteArray(
						this.rootNode.deepClone(), prefixInstance)));
		return debug;
	}

	@Override
	public QueryResult getResult() throws Exception {
		return this.getResult(false);
	}

	public QueryResult getResult(final boolean oneTime) throws Exception {
		final CollectAllResults cr = new CollectAllResults(oneTime);
		this.result.addApplication(cr);
		evaluateQuery();
		return cr.getResult();
	}

	public QueryResult[] getResults() throws Exception {
		return this.getResults(false);
	}

	public QueryResult[] getResults(final boolean oneTime) throws Exception {
		final CollectAllResults cr = new CollectAllResults(oneTime);
		this.result.addApplication(cr);
		evaluateQuery();
		return cr.getQueryResults();
	}
	
	public QueryResult getResult(final String query, final boolean oneTime) throws Exception {
		compileQuery(query);
		logicalOptimization();
		physicalOptimization();
		return getResult(oneTime);
	}
	
	public QueryResult[] getResults(final String query, final boolean oneTime) throws Exception {
		compileQuery(query);
		logicalOptimization();
		physicalOptimization();
		return getResults(oneTime);
	}	

	public CollectAllResults getCollectedResults(final boolean oneTime) throws Exception {
		final CollectAllResults cr = new CollectAllResults(oneTime);
		this.result.addApplication(cr);
		evaluateQuery();
		return cr;
	}

	public BasicOperator getRootNode() {
		return this.rootNode;
	}

	public void setRootNode(BasicOperator rootNode) {
		this.rootNode = rootNode;
	}

	private static int MULTIPLEDATATHREADS = 8;

	/**
	 * @return the multipledatathreads
	 */
	public static int getMultipleDataThreads() {
		return MULTIPLEDATATHREADS;
	}

	/**
	 * @param MULTIPLEDATATHREADS
	 */
	public static void setMultipleDataThreads(final int MULTIPLEDATATHREADS) {
		CommonCoreQueryEvaluator.MULTIPLEDATATHREADS = MULTIPLEDATATHREADS;
	}

	public final static int MAX_TRIPLES_IN_BUFFER = 50;

	public static void readTriples(String type, final InputStream input,
			final TripleConsumer tc) throws Exception {
		type = type.toUpperCase();
		if (type.startsWith("MULTIPLE")) {
			final String typeWithoutMultiple = type.substring("MULTIPLE"
					.length());
			if (MULTIPLEDATATHREADS == 1) {
				final BoundedBuffer<Triple> bbTriples = new BoundedBuffer<Triple>(
						MAX_TRIPLES_IN_BUFFER);

				final Thread thread = new Thread() {
					@Override
					public void run() {
						for (final String filename : FileHelper
								.readInputStreamToCollection(input)) {
							System.out.println("Reading data from file: "
									+ filename);
							try {
								String type2;
								if (typeWithoutMultiple.compareTo("DETECT") == 0) {
									final int index = filename.lastIndexOf('.');
									if (index == -1)
										System.err
												.println("Type of "
														+ filename
														+ " could not be automatically detected!");
									type2 = filename.substring(index + 1)
											.toUpperCase();
								} else
									type2 = typeWithoutMultiple;
								readTriplesWithoutMultipleFiles(type2,
										new FileInputStream(filename),
										new TripleConsumer() {
											public void consume(
													final Triple triple) {
												try {
													bbTriples.put(triple);
												} catch (final InterruptedException e) {
													System.err.println(e);
													e.printStackTrace();
												}
											}
										});
							} catch (final Throwable e) {
								System.err.println(e);
								e.printStackTrace();
							}
						}
						bbTriples.endOfData();
					}
				};
				thread.start();
				Triple t;
				try {
					while ((t = bbTriples.get()) != null) {
						tc.consume(t);
					}
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			} else {
				final TripleConsumer synchronizedTC = new TripleConsumer() {
					public synchronized void consume(final Triple triple) {
						tc.consume(triple);
					}
				};

				final Collection<String> filenames = FileHelper
						.readInputStreamToCollection(input);
				final BoundedBuffer<String> filenamesBB = new BoundedBuffer<String>(
						filenames.size());
				for (final String filename : filenames)
					try {
						filenamesBB.put(filename);
					} catch (final InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				filenamesBB.endOfData();
				final Thread[] threads = new Thread[MULTIPLEDATATHREADS];
				for (int i = 0; i < MULTIPLEDATATHREADS; i++) {
					threads[i] = new Thread() {
						@Override
						public void run() {
							try {
								while (filenamesBB.hasNext()) {
									final String filename = filenamesBB.get();
									if (filename == null)
										break;
									System.out
											.println("Reading data from file: "
													+ filename);
									String type2;
									if (typeWithoutMultiple.compareTo("DETECT") == 0) {
										final int index = filename
												.lastIndexOf('.');
										if (index == -1)
											System.err
													.println("Type of "
															+ filename
															+ " could not be automatically detected!");
										type2 = filename.substring(index + 1)
												.toUpperCase();
									} else
										type2 = typeWithoutMultiple;
									try {
										readTriplesWithoutMultipleFiles(type2,
												new FileInputStream(filename),
												synchronizedTC);
									} catch (final Throwable e) {
										System.err.println(e);
										e.printStackTrace();
									}
								}
							} catch (final InterruptedException e) {
								System.err.println(e);
								e.printStackTrace();
							}
						}
					};
					threads[i].start();
				}
				for (int i = 0; i < MULTIPLEDATATHREADS; i++) {
					try {
						threads[i].join();
					} catch (final InterruptedException e) {
						System.err.println(e);
						e.printStackTrace();
					}
				}
			}
		} else
			readTriplesWithoutMultipleFiles(type, input, tc);
	}

	public static void readTriplesWithoutMultipleFiles(final String type,
			final InputStream input, final TripleConsumer tc)
			throws Exception {
		if (type.startsWith("BZIP2")) {			
			InputStream uncompressed = Compression.BZIP2.createInputStream(input);			
			readTriplesWithoutMultipleFilesUncompressed(type.substring(5), uncompressed, tc);
		} else if (type.startsWith("GZIP")) {			
			InputStream uncompressed = Compression.GZIP.createInputStream(input);			
			readTriplesWithoutMultipleFilesUncompressed(type.substring(4), uncompressed, tc);
		} else if (type.startsWith("HUFFMAN")) {			
			InputStream uncompressed = Compression.HUFFMAN.createInputStream(input);			
			readTriplesWithoutMultipleFilesUncompressed(type.substring(7), uncompressed, tc);
		} else {
			readTriplesWithoutMultipleFilesUncompressed(type, input, tc);
		}
	}

	public static void readTriplesWithoutMultipleFilesUncompressed(
			final String type,
			final InputStream input, final TripleConsumer tc) throws Exception {
		final int length=type.length();
		if(length>0){
			String className=type.substring(0, 1).toUpperCase();
			if(length>1)
				className+=type.substring(1).toLowerCase();
			className+="Parser";
			try {
				Class<?> c = Class.forName("lupos.rdf.parser."+className);
				Method m = c.getMethod("parseRDFData", InputStream.class, TripleConsumer.class, String.class);
				int number= (Integer) m.invoke(c, input, tc, encoding);
				System.out.println("Number of read triples:"+ number);				
			} catch (ClassNotFoundException e) {
				System.err.println("No parser for RDF data format "+type+" found!");
			} catch (SecurityException e) {
				System.err.println("No parser for RDF data format "+type+" found!");
			} catch (NoSuchMethodException e) {
				System.err.println("No parser for RDF data format "+type+" found!");
			} catch (IllegalArgumentException e) {
				System.err.println("No parser for RDF data format "+type+" found!");
			} catch (IllegalAccessException e) {
				System.err.println("No parser for RDF data format "+type+" found!");
			} catch (InvocationTargetException e) {
				Throwable t=e.getCause();
				if(t!=null && t instanceof Exception)
					throw (Exception)t;
				if(t!=null && t instanceof Error)
					throw (Error)t;
				System.err.println("No parser for RDF data format "+type+" found!");
			}
		} else System.err.println("No input type for RDF data given!");		
	}
}