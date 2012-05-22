package lupos.engine.evaluators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.dbmergesortedds.DiskCollection;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.codemap.IntegerStringMapJava;
import lupos.datastructures.items.literal.codemap.StringIntegerMapJava;
import lupos.datastructures.paged_dbbptree.DBBPTree;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.application.Application;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.misc.debug.DebugStep;
import lupos.misc.ArgumentParser;
import lupos.misc.FileHelper;
import lupos.misc.Tuple;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.rdf.Prefix;

public abstract class QueryEvaluator<A> {

	private String queryString;
	protected ArgumentParser args = new ArgumentParser();

	protected Enum defaultRDFS;

	public enum compareEvaluator {
		NONE, RDF3X, MEMORYINDEX, STREAM
	};

	public QueryEvaluator() throws Exception {
		setupArguments();
		init();
	}

	public QueryEvaluator(final String[] arguments) throws Exception {
		setupArguments();
		args.parse(arguments, false);
		init();
	}
	
	public QueryEvaluator(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset){
		init(debug, multiplequeries, compare, compareoptions, times, dataset);
	}

	protected DEBUG debug;
	private boolean multiplequeries;
	private compareEvaluator compare;
	private String compareoptions;
	private int times;
	private String dataset;

	/**
	 * Code that should execute after the arguments have been parsed
	 * 
	 * @throws Exception
	 * 
	 */
	public void init() throws Exception {
		init((DEBUG) args.getEnum("debug"),args.getBool("multiplequeries"),(compareEvaluator) args.getEnum("compare"),args.getString("compareoptions"),args.getInt("times"),args.getString("dataset"));
	}
	
	public void init(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset){
		this.debug = debug;
		this.multiplequeries = multiplequeries;
		this.compare = compare;
		this.compareoptions = compareoptions;
		this.times = times;
		this.dataset = dataset;
	}

	public ArgumentParser getArgs() {
		return args;
	}

	public enum DEBUG {
		NONE, ALL, WITHOUTRESULT, ONLYFINAL
	};

	/**
	 * Specifies arguments accepted by all query evaluators. Override this
	 * method to specify any additional arguments that the given QueryEvaluator
	 * accepts. (Make sure to call super if you do).
	 */
	public void setupArguments() {
		args.addEnumOption("rdfs",
				"Usage of ontology reasoning based on RDF Schema", defaultRDFS);
		args.addEnumOption("debug",
				"Displays additional information for debugging purposes.",
				DEBUG.NONE);
		args
				.addBooleanOption(
						"multiplequeries",
						"Whether the queryfile contains the query itself (no-multiplequeries) or a filename of a query in each line of the queryfile.",
						false);
		args.addEnumOption("compare",
				"For comparing the result of this engine with another.",
				compareEvaluator.NONE);
		args
				.addStringOption(
						"compareoptions",
						"The options of the evaluator specified in compare with which the results of this evaluator will be compared to.");
		args
				.addIntegerOption("times",
						"For measuring the execution times x-times and present the average in seconds.");
		args
				.addStringOption(
						"type",
						"Specify the type of input data, e.g. \"N3\" (default) or \"RDFXML\". If the string starts with \"Multiple\", then the given file contains the filenames of several datafiles (separated by line breaks).",
						"N3");
		args.addStringOption("dataset", "Specify a dataset", "");
	}

	public long compileQueryFromFile(final String queryFile) throws Exception {
		final Date a = new Date();
		queryString = FileHelper.fastReadFile(queryFile);
		compileQuery(queryString);
		return ((new Date()).getTime() - a.getTime());
	}

	public String getQueryString() {
		return queryString;
	}

	public abstract long compileQuery(String query) throws Exception;
	
	public DebugContainerQuery<BasicOperatorByteArray, A> compileQueryDebugByteArray(
			final String query, final Prefix prefixInstance) throws Exception{
		compileQuery(query);
		return null;
	}

	public DebugContainerQuery<BasicOperator, A> compileQueryDebug(
			final String query) throws Exception {
		compileQuery(query);
		return null;
	}

	public long prepareInputData(final String inputFile) throws Exception {
		final Collection<URILiteral> cu = new LinkedList<URILiteral>();
		cu.add(LiteralFactory.createURILiteralWithoutLazyLiteral("<file:"
				+ inputFile + ">"));
		return prepareInputData(cu, new LinkedList<URILiteral>());
	}

	public abstract long logicalOptimization();
	
	public List<DebugContainer<BasicOperatorByteArray>> logicalOptimizationDebugByteArray(
			final Prefix prefixInstance) {
		logicalOptimization();
		return null;
	}

	public abstract long physicalOptimization();

	public List<DebugContainer<BasicOperatorByteArray>> physicalOptimizationDebugByteArray(final Prefix prefixInstance) {
		physicalOptimization();
		return null;
	}

	public abstract long prepareInputData(Collection<URILiteral> defaultGraphs,
			Collection<URILiteral> namedGraphs) throws Exception;
	
	public abstract long prepareInputDataWithSourcesOfNamedGraphs(Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs) throws Exception;

	public abstract long evaluateQuery() throws Exception;

	public long evaluateQueryDebugSteps(final DebugStep debugstep, Application application) throws Exception {
		return evaluateQuery();
	}

	public void prepareForQueryDebugSteps(final DebugStep debugstep) {
	}
	public abstract QueryResult getResult() throws Exception;

	public QueryResult getResult(final String query) throws Exception {
		compileQuery(query);
		logicalOptimization();
		physicalOptimization();
		return getResult();
	}

	public QueryResult getResult(final String inputFile, final String query)
			throws Exception {
		prepareInputData(inputFile);
		compileQuery(query);
		logicalOptimization();
		physicalOptimization();
		return getResult();
	}

	public void displayOperatorGraph(final String title) {
		System.out.println("Printing the operator graph is not supported!");
	}

	/**
	 * @deprecated Use {@link FileHelper#readFileToCollection(String)} instead
	 */
	@Deprecated
	public static Collection<String> readFileToCollection(final String filename) {
		return FileHelper.readFileToCollection(filename);
	}

	public static QueryEvaluator getQueryEvaluator(final compareEvaluator ce,
			final String[] arguments) throws Exception {
		if (ce == compareEvaluator.MEMORYINDEX)
			return new MemoryIndexQueryEvaluator(arguments);
		else if (ce == compareEvaluator.STREAM)
			return new StreamQueryEvaluator(arguments);
		else if (ce == compareEvaluator.RDF3X)
			return new RDF3XQueryEvaluator(arguments);
		else {
			System.out.println("Unknown QueryEvaluator-type " + ce);
			return null;
		}
	}

	/**
	 * Code to be executed in the main method of any QueryEvaluator
	 * 
	 * @param arguments
	 *            The arguments to the query evaluator
	 * @param klass
	 *            The QueryEvaluator class which should be used
	 **/
	public static void _main(final String[] arguments,
			final Class<? extends QueryEvaluator> klass) {

		try {
			final QueryEvaluator evaluator = klass.newInstance();
			evaluator.args.setAppDescription("Evaluate a query using the "
					+ klass.getSimpleName().replace("QueryEvaluator", "")
					+ " Engine\nUsage: java " + klass.getName()
					+ " data-file query-file [arguments]");
			final List<String> rest = evaluator.args.parse(arguments, true);
			evaluator.init();

			if (rest.size() != 2) {
				System.err.print("\nUnknown arguments:");
				for (int i = 2; i < rest.size(); i++)
					System.err.print(rest.get(i) + " ");
				System.err.println("\n\n" + evaluator.args.helptext());
				return;
			}

			final String datafile = rest.get(0);
			final String queryfile = rest.get(1);
			System.out.println("Configuration:" + evaluator.args.toString()
					+ "\n\n");
			_main(evaluator, datafile, queryfile);
		} catch (final Exception e) {
			System.err.println(e);
			e.printStackTrace();
			return;
		}
	}

	public static void _main(final QueryEvaluator evaluator,
			final String datafile, final String queryfile) {
		try {
			final URILiteral rdfURL = LiteralFactory
					.createURILiteralWithoutLazyLiteral("<file:" + datafile
							+ ">");
			final Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
			final Collection<URILiteral> namedGraphs = new LinkedList<URILiteral>();
			defaultGraphs.add(rdfURL);

			Collection<String> queryFiles;
			if (evaluator.multiplequeries) {
				queryFiles = FileHelper.readFileToCollection(queryfile);
			} else {
				queryFiles = new LinkedList<String>();
				queryFiles.add(queryfile);
			}

			// dataset is given from command line
			if (evaluator.dataset.compareTo("") != 0) {
				// copy RDF name in dataset to the static variable dataset
				final BufferedReader br = new BufferedReader(new FileReader(
						evaluator.dataset));
				while (br.ready()) {
					String rdf = br.readLine();
					// System.out.println(rdf);

					while (br.ready() && (rdf.length() > 0)
							&& rdf.substring(0, 1).compareTo("*") == 0) {
						rdf = br.readLine();
						// System.out.println(query);
					}

					if ((rdf.length() > 0)
							&& (rdf.substring(0, 1).compareTo("*") != 0)) {
						final String[] s = rdf.split(" ");
						if (s.length > 0) {
							boolean defaultGraph;
							String url;
							if (s.length == 1) {
								defaultGraph = true;
								url = s[0];
							} else {
								url = s[1];
								if (s[0].compareTo("named") == 0
										|| s[0].compareTo("named:") == 0)
									defaultGraph = false;
								else
									defaultGraph = true;
							}
							final URILiteral rdfURL2 = LiteralFactory
									.createURILiteralWithoutLazyLiteral("<file:"
											+ url + ">");

							if (defaultGraph)
								defaultGraphs.add(rdfURL2);
							else
								namedGraphs.add(rdfURL2);
						}
					}

				}
			}

			if (evaluator.compare != compareEvaluator.NONE) {
				String[] argsCompareEngine = evaluator.compareoptions
						.split("[\b]|[\t]|[\f]|[\n]|[\r]| ");
				for (int i = 0; i < argsCompareEngine.length; i++) {
					if (argsCompareEngine[i].compareTo("--compare") == 0
							|| argsCompareEngine[i]
									.compareTo("--compareoptions") == 0
							|| argsCompareEngine[i].compareTo("--times") == 0) {
						System.out.println("The option " + argsCompareEngine[i]
								+ " must not be nested in a compare option!");
						return;
					}
				}

				String compareDataFile = datafile;

				Collection<URILiteral> compareDefaultGraphs = defaultGraphs;
				Collection<URILiteral> compareNamedGraphs = namedGraphs;
				// final Collection<String> compareQueryFiles = queryFiles;
				if (!argsCompareEngine[0].startsWith("--")) {
					compareDataFile = argsCompareEngine[0];
					final URILiteral compareRdfURL = LiteralFactory
							.createURILiteralWithoutLazyLiteral("<file:"
									+ compareDataFile + ">");
					compareDefaultGraphs = new LinkedList<URILiteral>();
					compareNamedGraphs = new LinkedList<URILiteral>();
					compareDefaultGraphs.add(compareRdfURL);

					final String[] newArgs = new String[argsCompareEngine.length - 1];
					System.arraycopy(argsCompareEngine, 1, newArgs, 0,
							newArgs.length);
					argsCompareEngine = newArgs;
				}

				final QueryEvaluator qe = getQueryEvaluator(evaluator.compare,
						argsCompareEngine);
				System.out.println("Engine 1: "
						+ qe.getClass().getSimpleName().replace(
								"QueryEvaluator", "") + " Engine\n");
				System.out.println("Reading data for engine 1 from:"
						+ compareDataFile);
				System.out.println(qe.args.toString() + "\n\n");
				System.out.println("Engine 2: "
						+ evaluator.getClass().getSimpleName().replace(
								"QueryEvaluator", "") + " Engine\n");
				System.out
						.println("Reading data for engine 2 from:" + datafile);

				qe.prepareInputData(compareDefaultGraphs, compareNamedGraphs);
				evaluator.prepareInputData(defaultGraphs, namedGraphs);

				for (final String localqueryfile : queryFiles) {

					System.out
							.println("___________________________________________________________________________\n\nQuery in file:"
									+ localqueryfile);

					final String query = FileHelper
							.fastReadFile(localqueryfile);

					final QueryResult result2 = ((evaluator.debug) == DEBUG.ALL || (evaluator.debug) == DEBUG.WITHOUTRESULT) ? evaluator
							.getResult(query, localqueryfile)
							: evaluator.getResult(query);
					final QueryResult result1 = ((evaluator.debug) == DEBUG.ALL || (evaluator.debug) == DEBUG.WITHOUTRESULT) ? qe
							.getResult(query, localqueryfile)
							: qe.getResult(query);
					if ((evaluator.debug) == DEBUG.ONLYFINAL) {
						evaluator
								.displayOperatorGraph("Final Operatorgraph for query "
										+ localqueryfile
										+ " of engine "
										+ evaluator.getClass().getSimpleName());
						qe
								.displayOperatorGraph("Final Operatorgraph for query "
										+ localqueryfile
										+ " of engine "
										+ qe.getClass().getSimpleName());
					}

					int resultSize1;
					int resultSize2;
					if (result1 == null) {
						resultSize1 = 0;
					} else {
						resultSize1 = result1.size();
					}
					if (result2 == null) {
						resultSize2 = 0;
					} else {
						resultSize2 = result2.size();
					}
					System.out
							.println("Number of results engine 1 versus number of results of engine 2:"
									+ resultSize1
									+ (resultSize1 == resultSize2 ? "=" : "!=")
									+ resultSize2);
					if ((resultSize1 == 0 && resultSize2 == 0)
							|| (result1 != null && result2 != null && result1
									.equals(result2))) {
							System.out.println("The results match exactly!");

						if (resultSize1 == 0)
							System.out
									.println("However, the results are empty. Please check more carefully!");
						else {
							if (result1.sameOrder(result2))
								System.out
										.println("\nThe results are in the same order!");
						}
					} else {
							System.out
									.println("Results mismatch!\n\nPlease set the logger's level to DEBUG to see more detailed information");

						if (result1 != null)
							System.out
									.println("Size of query result of engine 1:"
											+ result1.size());
						if (result2 != null)
							System.out
									.println("Size of query result of engine 2:"
											+ result2.size());

						if (result1 != null
								&& result2 != null
								&& result1
										.containsAllExceptAnonymousLiterals(result2)
								&& result2
										.containsAllExceptAnonymousLiterals(result1)) {
							System.out
									.println("\n\nIt seems to be that the results are equivalent except of anonymous literals.");
							System.out
									.println("It must be more carefully checked!");

							if (result1
									.sameOrderExceptAnonymousLiterals(result2))
								System.out
										.println("\nThe results are in the same order ignoring anonymous literals!");
						}
					}
				}
			} else {
				final int times = evaluator.times;
				if (times == 0) {
					System.out.println("Evaluate a query in "
							+ queryfile
							+ " on data in "
							+ datafile
							+ " using the "
							+ evaluator.getClass().getSimpleName().replace(
									"QueryEvaluator", "") + " Engine\n");
					System.out.println("Read input data from file " + datafile
							+ " ...");
					evaluator.prepareInputData(defaultGraphs, namedGraphs);

						for (final String localqueryfile : queryFiles) {
							System.out.println("\nQuery in file:"
									+ localqueryfile);
							System.out.println("Compile query...");
							evaluator.compileQueryFromFile(localqueryfile);

							System.out.println("Logical optimization...");
							evaluator.logicalOptimization();
							System.out.println("Physical optimization...");
							evaluator.physicalOptimization();

							System.out.println("Evaluate query ...");
							final QueryResult resultQueryEvaluator = evaluator
									.getResult();
							System.out.println("\nQuery Result:");
							System.out.println(resultQueryEvaluator);
							System.out.println("Number of results: "
									+ resultQueryEvaluator.size());
							if ((evaluator.debug) == DEBUG.ONLYFINAL)
								evaluator
										.displayOperatorGraph("Final Operatorgraph for query "
												+ localqueryfile);
							System.out.println("----------------Done.");
							if (!(evaluator instanceof MemoryIndexQueryEvaluator))
								DiskCollection.removeCollectionsFromDisk();
							try {
								Thread.sleep(10000);
							} catch (final InterruptedException e) {
								e.printStackTrace();
							}
					}
				} else {
					System.out.println("Prepare input data in "
							+ datafile
							+ " using the "
							+ evaluator.getClass().getSimpleName().replace(
									"QueryEvaluator", "") + " Engine\n");
					long readInputDataTime = 0;
					final long constructIndices = 0;
					// construct index only one time:
					readInputDataTime += evaluator.prepareInputData(
							defaultGraphs, namedGraphs);
					System.out
							.println("\n-------------------------------Times for preparing input:\n");
					System.out
							.println("(a) Time in seconds to read input data:"
									+ ((double) readInputDataTime) / 1000);

					if (evaluator instanceof BasicIndexQueryEvaluator) {
						System.out
								.println("(b) Time in seconds to construct completely all indices:"
										+ ((double) constructIndices) / 1000);
						System.out
								.println("(a) + (b):"
										+ ((double) readInputDataTime + constructIndices)
										/ 1000);
					}

					System.out.println("\n**************************Done.\n");

					for (final String localqueryfile : queryFiles) {
						System.out.println("\nEvaluate query in file:"
								+ localqueryfile + "...");
						final String query = FileHelper
								.fastReadFile(localqueryfile);
						long compileQueryTime = 0;
						long logicalOptimizationTime = 0;
						long physicalOptimizationTime = 0;
						long evaluateQueryTime = 0;
						long totalTime = 0;
						final long[] compileQueryTimeArray = new long[times];
						final long[] logicalOptimizationTimeArray = new long[times];
						final long[] physicalOptimizationTimeArray = new long[times];
						final long[] evaluateQueryTimeArray = new long[times];
						final long[] totalTimeArray = new long[times];
						for (int i = 0; i < times; i++) {
							compileQueryTimeArray[i] = evaluator
									.compileQuery(query);
							compileQueryTime += compileQueryTimeArray[i];
							logicalOptimizationTimeArray[i] = evaluator
									.logicalOptimization();
							logicalOptimizationTime += logicalOptimizationTimeArray[i];
							physicalOptimizationTimeArray[i] = evaluator
									.physicalOptimization();
							physicalOptimizationTime += physicalOptimizationTimeArray[i];
							evaluateQueryTimeArray[i] = evaluator
									.evaluateQuery();
							evaluateQueryTime += evaluateQueryTimeArray[i];
							totalTimeArray[i] = compileQueryTimeArray[i]
									+ logicalOptimizationTimeArray[i]
									+ physicalOptimizationTimeArray[i]
									+ evaluateQueryTimeArray[i];
							totalTime += totalTimeArray[i];
							if (!(evaluator instanceof MemoryIndexQueryEvaluator))
								DiskCollection.removeCollectionsFromDisk();
						}
						if (!((totalTime * 0.95) / times <= totalTimeArray[0] && totalTimeArray[0] <= (totalTime * 1.05)
								/ times)) {
							System.out
									.println("The total time of the first experiment differs more than 5% from the average. We run this experiment again.\nThe old times are:"
											+ compileQueryTimeArray[0]
											+ " (comp.) + "
											+ logicalOptimizationTimeArray[0]
											+ " (log. opt.) + "
											+ physicalOptimizationTimeArray[0]
											+ " (phys. opt.) + "
											+ evaluateQueryTimeArray[0]
											+ " (eval.) = "
											+ totalTimeArray[0]
											+ "\n");
							compileQueryTime -= compileQueryTimeArray[0];
							logicalOptimizationTime -= logicalOptimizationTimeArray[0];
							physicalOptimizationTime -= physicalOptimizationTimeArray[0];
							evaluateQueryTime -= evaluateQueryTimeArray[0];
							totalTime -= totalTimeArray[0];

							compileQueryTimeArray[0] = evaluator
									.compileQuery(query);
							compileQueryTime += compileQueryTimeArray[0];
							logicalOptimizationTimeArray[0] = evaluator
									.logicalOptimization();
							logicalOptimizationTime += logicalOptimizationTimeArray[0];
							physicalOptimizationTimeArray[0] = evaluator
									.physicalOptimization();
							physicalOptimizationTime += physicalOptimizationTimeArray[0];
							evaluateQueryTimeArray[0] = evaluator
									.evaluateQuery();
							evaluateQueryTime += evaluateQueryTimeArray[0];
							totalTimeArray[0] = compileQueryTimeArray[0]
									+ logicalOptimizationTimeArray[0]
									+ physicalOptimizationTimeArray[0]
									+ evaluateQueryTimeArray[0];
							totalTime += totalTimeArray[0];
							if (!(evaluator instanceof MemoryIndexQueryEvaluator))
								DiskCollection.removeCollectionsFromDisk();
						}
						System.out
								.println("\n-------------------------------Times:\n");
						System.out
								.println("(I) Time in seconds to compile query: Avg"
										+ toString(compileQueryTimeArray)
										+ "/1000 = "
										+ (((double) compileQueryTime) / times)
										/ 1000);
						System.out
								.println("Standard deviation of the sample:"
										+ computeStandardDeviationOfTheSample(compileQueryTimeArray)
										/ 1000);
						System.out
								.println("Sample standard deviation:"
										+ computeSampleStandardDeviation(compileQueryTimeArray)
										/ 1000);
						System.out
								.println("\n(II) Time in seconds used for logical optimization: Avg"
										+ toString(logicalOptimizationTimeArray)
										+ "/1000 = "
										+ (((double) logicalOptimizationTime) / times)
										/ 1000);
						System.out
								.println("Standard deviation of the sample:"
										+ computeStandardDeviationOfTheSample(logicalOptimizationTimeArray)
										/ 1000);
						System.out
								.println("Sample standard deviation:"
										+ computeSampleStandardDeviation(logicalOptimizationTimeArray)
										/ 1000);
						System.out
								.println("\n(III) Time in seconds used for physical optimization: Avg"
										+ toString(physicalOptimizationTimeArray)
										+ "/1000 = "
										+ (((double) physicalOptimizationTime) / times)
										/ 1000);
						System.out
								.println("Standard deviation of the sample:"
										+ computeStandardDeviationOfTheSample(physicalOptimizationTimeArray)
										/ 1000);
						System.out
								.println("Sample standard deviation:"
										+ computeSampleStandardDeviation(physicalOptimizationTimeArray)
										/ 1000);
						System.out
								.println("\n(IV) Time in seconds to evaluate query: Avg"
										+ toString(evaluateQueryTimeArray)
										+ "/1000 = "
										+ (((double) evaluateQueryTime) / times)
										/ 1000);
						System.out
								.println("Standard deviation of the sample:"
										+ computeStandardDeviationOfTheSample(evaluateQueryTimeArray)
										/ 1000);
						System.out
								.println("Sample standard deviation:"
										+ computeSampleStandardDeviation(evaluateQueryTimeArray)
										/ 1000);
						System.out
								.println("\nTotal time in seconds (I)+(II)+(III)+(IV): Avg"
										+ toString(totalTimeArray)
										+ "/1000 = "
										+ (((double) totalTime) / times) / 1000);
						System.out
								.println("Standard deviation of the sample:"
										+ computeStandardDeviationOfTheSample(totalTimeArray)
										/ 1000);
						System.out
								.println("Sample standard deviation:"
										+ computeSampleStandardDeviation(totalTimeArray)
										/ 1000);

						System.out.println("\n**************************Done.");
					}
					writeOutModifiedPagesOfDictionary();
					return;
				}
			}
			writeOutModifiedPagesOfDictionary();
		} catch (final Exception ex) {
			System.err.println(ex);
			ex.printStackTrace();
			return;
		}
	}

	/**
	 * write out all modified pages of the dictionary in the buffer manager
	 * @throws IOException
	 */
	public static void writeOutModifiedPagesOfDictionary()
			throws IOException {
		if (LazyLiteral.getHm() != null) {
			if (LazyLiteral.getHm() instanceof StringIntegerMapJava) {
				if (((StringIntegerMapJava) LazyLiteral.getHm())
						.getOriginalMap() instanceof DBBPTree) {
					((DBBPTree<String, Integer>) ((StringIntegerMapJava) LazyLiteral
							.getHm()).getOriginalMap()).writeAllModifiedPages();
				}
			}
		}
		if (LazyLiteral.getV() != null) {
			if (LazyLiteral.getV() instanceof IntegerStringMapJava) {
				if (((IntegerStringMapJava) LazyLiteral.getV())
						.getOriginalMap() instanceof DBBPTree) {
					((DBBPTree<Integer, String>) ((IntegerStringMapJava) LazyLiteral
							.getV()).getOriginalMap()).writeAllModifiedPages();
				}
			}
		}
	}
	
	/**
	 * write out all pages in buffer managers including dictionary and RDF data indices
	 * @throws IOException
	 */
	public static void writeOutModifiedPages(BasicIndexQueryEvaluator evaluator, String dir) throws IOException{
		QueryEvaluator.writeOutModifiedPagesOfDictionary();
		evaluator.writeOutAllModifiedPagesInRDFDataIndices(dir);
	}

	public static String toString(final long[] oa) {
		String s = "(";
		boolean first = true;
		for (final long o : oa) {
			if (first)
				first = false;
			else
				s += ", ";
			s += o;
		}
		return s + ")";
	}

	public static double computeStandardDeviationOfTheSample(final long[] array) {
		return Math.sqrt(computeInnerTerm(array) / (array.length));
	}

	public static double computeSampleStandardDeviation(final long[] array) {
		return Math.sqrt(computeInnerTerm(array) / ((double) array.length - 1));
	}

	private static double computeInnerTerm(final long[] array) {
		long sum = 0;
		for (final long l : array) {
			sum += l;
		}
		final double mean = ((double) sum) / array.length;
		double innerTerm = 0;
		for (final long l : array) {
			final double diff = (l) - mean;
			innerTerm += diff * diff;
		}
		return innerTerm;
	}
}