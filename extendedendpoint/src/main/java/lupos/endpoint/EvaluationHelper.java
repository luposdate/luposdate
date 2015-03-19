package lupos.endpoint;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.DebugContainerQuery;
import lupos.engine.evaluators.JenaQueryEvaluator;
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.engine.evaluators.QueryEvaluator.DEBUG;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.evaluators.SesameQueryEvaluator;
import lupos.engine.evaluators.StreamQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.application.CollectRIFResult;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoin;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoinNonStandardSPARQL;
import lupos.gui.InferenceHelper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapper;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperAST;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperASTRIF;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperBasicOperatorByteArray;
import lupos.gui.operatorgraph.graphwrapper.GraphWrapperRules;
import lupos.misc.FileHelper;
import lupos.misc.Triple;
import lupos.misc.Tuple;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.rdf.Prefix;
import lupos.rif.BasicIndexRuleEvaluator;
import lupos.rif.datatypes.RuleResult;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.TokenMgrError;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.rio.RDFParseException;

import com.hp.hpl.jena.n3.turtle.TurtleParseException;
import com.hp.hpl.jena.query.QueryParseException;

public class EvaluationHelper {

	/**
	 * list of registered query evaluators
	 */
	protected static List<lupos.misc.Tuple<String, Class<? extends QueryEvaluator<Node>>>> registeredEvaluators = new LinkedList<lupos.misc.Tuple<String, Class<? extends QueryEvaluator<Node>>>>();

	/**
	 * just helper instance for dealing with ontology inference
	 */
	protected final static RuleSets rulesets = new RuleSets("/rif/");

	/**
	 * for initialization purposes...
	 * to be called only once...
	 * @param serviceApproach the service approach to be used in a service call...
	 * @param bitVectorApproach the bit vector approach to be used in a service call...
	 * @param semanticInterpretationOfLiterals if literals are semantically interpreted
	 * @param substringSize the substring size used in bit vector joins using SPARQL 1.1 features
	 * @param bitvectorSize the bitvector size used in bit vector joins with proprietary extensions
	 */
	public static void init(final ServiceApproaches serviceApproach, final FederatedQueryBitVectorJoin.APPROACH bitVectorApproach, final boolean semanticInterpretationOfLiterals, final int substringSize, final int bitvectorSize){
		bitVectorApproach.setup();
		serviceApproach.setup();
		FederatedQueryBitVectorJoin.substringSize = substringSize;
		FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize = bitvectorSize;
		LiteralFactory.semanticInterpretationOfLiterals = semanticInterpretationOfLiterals;
	}

	/**
	 * register an evaluator
	 * @param evaluatorName the name to be displayed of the evaluator
	 * @param evaluatorClass the evaluator class from which the evaluator will be instantiated...
	 */
	public static void registerEvaluator(final String evaluatorName, final Class<? extends QueryEvaluator<Node>> evaluatorClass){
		EvaluationHelper.registeredEvaluators.add(new lupos.misc.Tuple<String, Class<? extends QueryEvaluator<Node>>>(evaluatorName, evaluatorClass));
	}

	/**
	 * just for registering the standard luposdate evaluators...
	 */
	public static void registerEvaluators(){
		EvaluationHelper.registerEvaluator("MemoryIndex", MemoryIndexQueryEvaluator.class);
		EvaluationHelper.registerEvaluator("RDF3X", RDF3XQueryEvaluator.class);
		EvaluationHelper.registerEvaluator("Stream", StreamQueryEvaluator.class);
		EvaluationHelper.registerEvaluator("Jena", JenaQueryEvaluator.class);
		EvaluationHelper.registerEvaluator("Sesame", SesameQueryEvaluator.class);
	}
	
	/**
	 * Returns the index (needed for evaluation methods) of a registered evaluator specified by name
	 * @param evaluator name of the evaluator
	 * @return index if it's registered
	 * @throws Runtime Exception if specified evaluator is not registered
	 */
	public static int getEvaluatorIndexByName(String evaluator) {
		for (lupos.misc.Tuple<String, Class<? extends QueryEvaluator<Node>>> registeredEvaluator : registeredEvaluators) {
			if (registeredEvaluator.getFirst().equalsIgnoreCase(evaluator)) {
				return registeredEvaluators.indexOf(registeredEvaluator);
			}
		}
		throw new RuntimeException(String.format("Evaluator %s not registered", evaluator));
	}

	/**
	 * This method instantiates a (main memory) query evaluator to be used in the endpoint for proprietary entensions...
	 * @param evaluatorIndex the index of the evaluator to be instantiated
	 * @return the instantiated (main memory) query evaluator
	 * @throws Exception in case of any error during instantiation...
	 */
	@SuppressWarnings("unchecked")
	public static QueryEvaluator<Node> setupEvaluator(final int evaluatorIndex) throws Exception {
		// use static method "newInstance()" for instantiation if available
		QueryEvaluator<Node> evaluator = null;
		final Class<? extends QueryEvaluator<Node>> evalClass = EvaluationHelper.registeredEvaluators.get(evaluatorIndex).getSecond();
		try {
			Method m;
			if ((m  = evalClass.getDeclaredMethod("newInstance")) != null && (m.getModifiers() & Modifier.STATIC) != 0) {
				final Object instance = m.invoke(evalClass);
				if (instance instanceof QueryEvaluator) {
					evaluator = (QueryEvaluator<Node>) instance;
				}
			}
		} catch (NoSuchMethodException | SecurityException e) {
			evaluator = null;
		}
		if (evaluator == null) {
			// otherwise use standard constructor
			evaluator = evalClass.newInstance();
		}
		evaluator.setupArguments();
		evaluator.getArgs().set("debug", DEBUG.ALL);
		evaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
		evaluator.getArgs().set("codemap", LiteralFactory.MapType.TRIEMAP);
		evaluator.getArgs().set("distinct", CommonCoreQueryEvaluator.DISTINCT.HASHSET);
		evaluator.getArgs().set("optional", CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);

		if (evaluator instanceof JenaQueryEvaluator) {
				evaluator.getArgs().set("RDFS", JenaQueryEvaluator.ONTOLOGY.NONE);
		} else if (evaluator instanceof SesameQueryEvaluator) {
				evaluator.getArgs().set("RDFS", SesameQueryEvaluator.ONTOLOGY.NONE);
		} else {
			evaluator.getArgs().set("RDFS", CommonCoreQueryEvaluator.RDFS.NONE);
		}

		if (evaluator instanceof JenaQueryEvaluator
				|| evaluator instanceof SesameQueryEvaluator) {
			evaluator.getArgs().set("type", "N3");
		} else {
			evaluator.getArgs().set("type", "Turtle");
			evaluator.getArgs().set("core", true);
		}

		if (evaluator instanceof RDF3XQueryEvaluator) {
			evaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.BPTREE);
		} else {
			evaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.HASHMAP);
		}
		evaluator.init();
		return evaluator;
	}

	/**
	 * This method returns the abstract syntax trees of a given query, the core sparql query and also its abstract syntax tree,
	 * @param evaluatorIndex which evaluator to use
	 * @param rifEvaluation if a rif rule set is to be processed (instead of a sparql query)
	 * @param query the query to be compiled
	 * @return null if the evaluator does not provide additional compile information, otherwise a triple container with the abstract syntax trees of "query", the core sparql query and also its abstract syntax tree
	 * @throws Exception in case of any errors
	 */
	public static Triple<GraphWrapper, String, GraphWrapper> getCompileInfo(final int evaluatorIndex, final boolean rifEvaluation, final String query) throws Exception {
		QueryEvaluator<Node> evaluator = EvaluationHelper.setupEvaluator(evaluatorIndex);
		if(rifEvaluation){
			evaluator = new BasicIndexRuleEvaluator((CommonCoreQueryEvaluator<Node>) evaluator);
		}
		final Prefix prefixInstance = new Prefix(true);
		final DebugContainerQuery<BasicOperatorByteArray, Node> dcq = evaluator.compileQueryDebugByteArray(query, prefixInstance);
		if(dcq==null && !rifEvaluation){
			// this operator does not support returning the abstract syntax trees and the core sparql query
			return null;
		}
		if(rifEvaluation) {
			return new Triple<GraphWrapper, String, GraphWrapper>(new GraphWrapperASTRIF(((BasicIndexRuleEvaluator) evaluator).getCompilationUnit()), null, new GraphWrapperRules(((BasicIndexRuleEvaluator) evaluator).getDocument()));
		} else {
			return new Triple<GraphWrapper, String, GraphWrapper>(new GraphWrapperAST(dcq.getAst()), dcq.getCoreSPARQLQuery(), new GraphWrapperAST(dcq.getAstCoreSPARQLQuery()));
		}
	}

	/**
	 * returns the line, column and error message of an exception occurring during compiling a sparql query or RIF ruleset
	 * @param e the error throwable
	 * @return a triple containing the line, column and error message. If the line and column is not detected, -1 is returned for both
	 */
	public static Triple<Integer, Integer, String> dealWithThrowableFromQueryParser(final Throwable e) {
		int line = -1;
		int column = -1;
		if (e instanceof TokenMgrError) {
			final TokenMgrError tme = (TokenMgrError) e;

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern.compile("line (\\d+), column (\\d+)");
			final Matcher matcher = pattern.matcher(tme.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				line = Integer.parseInt(matcher.group(1));
				column = Integer.parseInt(matcher.group(2));
			}
			return new Triple<Integer, Integer, String>(line, column, tme.getMessage());
		} else	if (e instanceof lupos.rif.generated.parser.TokenMgrError) {
			final lupos.rif.generated.parser.TokenMgrError tme = (lupos.rif.generated.parser.TokenMgrError) e;

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern
					.compile("line (\\d+), column (\\d+)");
			final Matcher matcher = pattern.matcher(tme.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				line = Integer.parseInt(matcher.group(1));
				column = Integer.parseInt(matcher.group(2));
			}
			return new Triple<Integer, Integer, String>(line, column, tme.getMessage());
		} else if (e instanceof ParseException) {
			final ParseException pe = (ParseException) e;

			// get precise line and column...
			if (pe.currentToken.next == null) {
				line = pe.currentToken.beginLine;
				column = pe.currentToken.beginColumn;
			} else {
				line = pe.currentToken.next.beginLine;
				column = pe.currentToken.next.beginColumn;
			}

			return new Triple<Integer, Integer, String>(line, column, pe.getMessage());
		} else if(e instanceof lupos.rif.generated.parser.ParseException){
			final lupos.rif.generated.parser.ParseException pe = (lupos.rif.generated.parser.ParseException) e;

			// get precise line and column...
			if (pe.currentToken.next == null) {
				line = pe.currentToken.beginLine;
				column = pe.currentToken.beginColumn;
			} else {
				line = pe.currentToken.next.beginLine;
				column = pe.currentToken.next.beginColumn;
			}

			return new Triple<Integer, Integer, String>(line, column, pe.getMessage());
		} else if (e instanceof QueryParseException) {
			final QueryParseException qpe = (QueryParseException) e;

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern.compile("line (\\d+), column (\\d+)", Pattern.CASE_INSENSITIVE);
			final Matcher matcher = pattern.matcher(qpe.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				line = Integer.parseInt(matcher.group(1));
				column = Integer.parseInt(matcher.group(2));
			}
			return new Triple<Integer, Integer, String>(line, column, qpe.getMessage());
		} else if (e instanceof MalformedQueryException) {
			final MalformedQueryException mqe = (MalformedQueryException) e;

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern.compile("line (\\d+), column (\\d+)");
			final Matcher matcher = pattern.matcher(mqe.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				line = Integer.parseInt(matcher.group(1));
				column = Integer.parseInt(matcher.group(2));
			}
			return new Triple<Integer, Integer, String>(line, column, mqe.getMessage());
		} else {
			// default: just return the error message!
			return new Triple<Integer, Integer, String>(-1, -1, e.toString());
		}
	}

	/**
	 * Returns the line, column and error text of an error from the rdf parser
	 * @param e the error throwable
	 * @return a triple containing the line, column and error message. If the line and column is not detected, -1 is returned for both
	 */
	public static Triple<Integer, Integer, String> dealWithThrowableFromRDFParser(final Throwable e) {
		int line = -1;
		int column = -1;
		if (e instanceof TurtleParseException) {
			final TurtleParseException n3e = (TurtleParseException) e;

			// create the pattern to match
			// and create a matcher against the string
			final Pattern pattern = Pattern.compile("line (\\d+), column (\\d+)");
			final Matcher matcher = pattern.matcher(n3e.getMessage());

			final Pattern pattern2 = Pattern.compile("Line (\\d+): ");
			final Matcher matcher2 = pattern2.matcher(n3e.getMessage());

			// try to find the pattern in the message...
			if (matcher.find() == true) {
				// get matches...
				line = Integer.parseInt(matcher.group(1));
				column = Integer.parseInt(matcher.group(2));
			} else if (matcher2.find() == true) {
				// get matches....
				line = Integer.parseInt(matcher2.group(1));
				column = 1;
			}

			return new Triple<Integer, Integer, String>(line, column, n3e.getMessage());
		} else if (e instanceof RDFParseException) {
			final RDFParseException rdfpe = (RDFParseException) e;

			// get precise line and column...
			line = rdfpe.getLineNumber();
			column = rdfpe.getColumnNumber();

			if (column == -1) {
				column = 1;
			}

			return new Triple<Integer, Integer, String>(line, column, rdfpe.getMessage());
		} else {
			// default: just return the error message!
			return new Triple<Integer, Integer, String>(-1, -1, e.toString());
		}
	}

	/**
	 * returns a sequence of operator graphs for each intermediate step after compiling the query including logical and physical optimization steps
	 * @param evaluatorIndex the evaluator to be used...
	 * @param rifEvaluation if a rif rule set is to be processed (instead of a sparql query)
	 * @param sparqlinference which type of inference should be considered
	 * @param mode the mode according to which the rif rule set is generated (only relevant when considering RDFS or OWL2RL inference)
	 * @param sparqlinferencematerialization if the inferred triples are materialized in the database (and afterwards the query is evaluated) or if inference is done on demand by integrating the operator graphs of the sparql query and the rif rule set (maybe generated from an ontology) (not relevant in case of no inference nor if a given rif rule set is evaluated)
	 * @param checkInconsistencies if rules to check the inconsistency of an owl2rl ontology should be incorporated (only relevant for OWL2RL inference)
	 * @param data the data in N3 format
	 * @param rif the rif rule set (only relevant for RIF inference)
	 * @param query the query to be compiled (or a given RIF rule set in the case that its result should be displayed)
	 * @return the container holding the list of prefixes and a list of triple containers: Each triple container represents one step in the optimization phase having a rule name (that has been applied) and its description as well as the operator graph
	 * @throws Exception in case of any errors...
	 */
	public static Tuple<Prefix, List<Triple<String, String, GraphWrapperBasicOperatorByteArray>>> getOperatorGraphs(final int evaluatorIndex, final boolean rifEvaluation, final SPARQLINFERENCE sparqlinference, final GENERATION mode, final SPARQLINFERENCEMATERIALIZATION sparqlinferencematerialization, final boolean checkInconsistencies, final String data, final String rif, final String query) throws Exception {
		QueryEvaluator<Node> evaluator = EvaluationHelper.setupEvaluator(evaluatorIndex);
		if(rifEvaluation){
			evaluator = new BasicIndexRuleEvaluator((CommonCoreQueryEvaluator<Node>) evaluator);
		}
		final Prefix prefixInstance = new Prefix(true);
		final DebugContainerQuery<BasicOperatorByteArray, Node> dcq = evaluator.compileQueryDebugByteArray(query, prefixInstance);
		if(dcq==null){
			// this operator does not support returning the operator graphs
			return null;
		}
		final List<DebugContainer<BasicOperatorByteArray>>  ruleApplications = dcq.getCorrectOperatorGraphRules();

		// do inference for materialization strategy
		if(!(evaluator instanceof JenaQueryEvaluator || evaluator instanceof SesameQueryEvaluator)){

			final String inferenceRules = sparqlinference.getRuleSet(EvaluationHelper.rulesets, mode, checkInconsistencies, data, rif);
			if(inferenceRules != null){
				final BasicIndexRuleEvaluator birqe = new BasicIndexRuleEvaluator((CommonCoreQueryEvaluator<Node>) evaluator);
				birqe.compileQuery(inferenceRules);

				// TODO improve RIF logical optimization such that it is fast enough for large operator graphs!
				// as workaround here only use the logical optimization of the underlying evaluator!
				ruleApplications.addAll(evaluator.logicalOptimizationDebugByteArray(prefixInstance));
				ruleApplications.addAll(birqe.physicalOptimizationDebugByteArray(prefixInstance));

				if(sparqlinferencematerialization == SPARQLINFERENCEMATERIALIZATION.COMBINEDQUERYOPTIMIZATION){
					final BasicOperator rootInference = birqe.getRootNode();
					final Result resultInference = birqe.getResultOperator();
					final CommonCoreQueryEvaluator<Node> commonCoreQueryEvaluator = (CommonCoreQueryEvaluator<Node>) evaluator;
					BasicIndexRuleEvaluator.integrateInferenceOperatorgraphIntoQueryOperatorgraph(rootInference, resultInference, commonCoreQueryEvaluator.getRootNode(), commonCoreQueryEvaluator.getResultOperator());
					commonCoreQueryEvaluator.setBindingsVariablesBasedOnOperatorgraph();
				}
			}
		}

		ruleApplications.addAll(evaluator.logicalOptimizationDebugByteArray(prefixInstance));
		ruleApplications.addAll(evaluator.physicalOptimizationDebugByteArray(prefixInstance));
		final List<Triple<String, String, GraphWrapperBasicOperatorByteArray>> resultList = new ArrayList<Triple<String, String, GraphWrapperBasicOperatorByteArray>>(ruleApplications.size());
		for(final DebugContainer<BasicOperatorByteArray> dc: ruleApplications){
			resultList.add(new Triple<String, String, GraphWrapperBasicOperatorByteArray>(dc.getRuleName(), dc.getDescription(), new GraphWrapperBasicOperatorByteArray(dc.getRoot())));
		}
		return new Tuple<Prefix, List<Triple<String, String, GraphWrapperBasicOperatorByteArray>>>(prefixInstance, resultList);
	}

	/**
	 *
	 * @param evaluatorIndex the evaluator to be used...
	 * @param rifEvaluation if a rif rule set is to be processed (instead of a sparql query)
	 * @param sparqlinference which type of inference should be considered
	 * @param mode the mode according to which the rif rule set is generated (only relevant when considering RDFS or OWL2RL inference)
	 * @param sparqlinferencematerialization if the inferred triples are materialized in the database (and afterwards the query is evaluated) or if inference is done on demand by integrating the operator graphs of the sparql query and the rif rule set (maybe generated from an ontology) (not relevant in case of no inference nor if a given rif rule set is evaluated)
	 * @param checkInconsistencies if rules to check the inconsistency of an owl2rl ontology should be incorporated (only relevant for OWL2RL inference)
	 * @param data the data in N3 format
	 * @param rif the rif rule set (only relevant for RIF inference)
	 * @param query the query to be compiled (or a given RIF rule set in the case that its result should be displayed)
	 * @return the container holding a warning text (e.g. for warning different ontology inference models for third-party evaluators) and an array of different query results (not only sparql results, but also e.g. GraphResult, RuleResult (for rif results or in the case of errors in the ontology))
	 * @throws Exception in case of any errors...
	 */
	public static Tuple<String, QueryResult[]> getQueryResult(final int evaluatorIndex, final boolean rifEvaluation, final SPARQLINFERENCE sparqlinference, final GENERATION mode, final SPARQLINFERENCEMATERIALIZATION sparqlinferencematerialization, final boolean checkInconsistencies, final String data, final String rif, final String query) throws Exception {
		String warning = "";
		QueryEvaluator<Node> evaluator = EvaluationHelper.setupEvaluator(evaluatorIndex);
		if(rifEvaluation){
			evaluator = new BasicIndexRuleEvaluator((CommonCoreQueryEvaluator<Node>) evaluator);
		}

		if(!rifEvaluation){
			// setup inference for third-party evaluators
			if(sparqlinference != SPARQLINFERENCE.NONE && (evaluator instanceof JenaQueryEvaluator || evaluator instanceof SesameQueryEvaluator)){
				boolean flag = false;
				if(evaluator instanceof JenaQueryEvaluator && (sparqlinference == SPARQLINFERENCE.OWL2RL)
				   || sparqlinference == SPARQLINFERENCE.RDFS) {
					warning += "Jena and Sesame evaluators do not support different rulesets and materialization strategies!\nUsing their standard inference for "+sparqlinference+"...";
					if(evaluator instanceof JenaQueryEvaluator){
						if(sparqlinference == SPARQLINFERENCE.OWL2RL){
							((JenaQueryEvaluator) evaluator).setOntology(JenaQueryEvaluator.ONTOLOGY.OWL);
						} else {
							((JenaQueryEvaluator) evaluator).setOntology(JenaQueryEvaluator.ONTOLOGY.RDFS);
						}
						flag = true;
					} else if(evaluator instanceof SesameQueryEvaluator){
						((SesameQueryEvaluator)evaluator).setOntology(SesameQueryEvaluator.ONTOLOGY.RDFS);
						flag = true;
					}
				}
				if(!flag) {
					warning += "The "+((evaluator instanceof JenaQueryEvaluator)?"Jena":"Sesame")+" evaluator does not support this type of inference ("+sparqlinference+")...\nEvaluate query without considering inference!";
				}
			}
		}
		// set up data preparation
		final URILiteral rdfURL = LiteralFactory.createStringURILiteral("<inlinedata:" + data + ">");
		final LinkedList<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
		defaultGraphs.add(rdfURL);
		evaluator.prepareInputData(defaultGraphs, new LinkedList<URILiteral>());
		// do inference for materialization strategy
		RuleResult errorsInOntology = null;
		if(!(evaluator instanceof JenaQueryEvaluator || evaluator instanceof SesameQueryEvaluator)){

			final String inferenceRules = sparqlinference.getRuleSet(EvaluationHelper.rulesets, mode, checkInconsistencies, data, rif);
			if(inferenceRules != null){
				final BasicIndexRuleEvaluator birqe = new BasicIndexRuleEvaluator((CommonCoreQueryEvaluator<Node>) evaluator);
				birqe.compileQuery(inferenceRules);

				// TODO improve RIF logical optimization such that it is fast enough for large operator graphs!
				// as workaround here only use the logical optimization of the underlying evaluator!
				evaluator.logicalOptimization();
				birqe.physicalOptimization();

				if(sparqlinferencematerialization == SPARQLINFERENCEMATERIALIZATION.MATERIALIZEALL){
					errorsInOntology = birqe.inferTriplesAndStoreInDataset();
				} else { // case SPARQLINFERENCEMATERIALIZATION.COMBINEDQUERYOPTIMIZATION:
					final BasicOperator rootInference = birqe.getRootNode();
					final Result resultInference = birqe.getResultOperator();
					final CommonCoreQueryEvaluator<Node> commonCoreQueryEvaluator = (CommonCoreQueryEvaluator<Node>) evaluator;
					BasicIndexRuleEvaluator.integrateInferenceOperatorgraphIntoQueryOperatorgraph(rootInference, resultInference, commonCoreQueryEvaluator.getRootNode(), commonCoreQueryEvaluator.getResultOperator());
					commonCoreQueryEvaluator.setBindingsVariablesBasedOnOperatorgraph();
				}
			}
		}

		// compile and optimize query/ruleset
		evaluator.compileQuery(query);
		evaluator.logicalOptimization();
		evaluator.physicalOptimization();
		QueryResult[] result;
		// start evaluation...
		if (evaluator instanceof CommonCoreQueryEvaluator || evaluator instanceof BasicIndexRuleEvaluator) {
			final CollectRIFResult crr = new CollectRIFResult(false);
			final Result resultOperator = (evaluator instanceof CommonCoreQueryEvaluator)?((CommonCoreQueryEvaluator<Node>)evaluator).getResultOperator(): ((BasicIndexRuleEvaluator)evaluator).getResultOperator();
			resultOperator.addApplication(crr);
			evaluator.evaluateQuery();
			result = crr.getQueryResults();
		} else {
			result = new QueryResult[1];
			result[0] = evaluator.getResult();
		}
		if(errorsInOntology!=null) {
			final QueryResult[] iresult = result;
			result = new QueryResult[result.length+1];
			System.arraycopy(iresult, 0, result, 0, iresult.length);
			result[result.length-1] = errorsInOntology;
		}
		return new Tuple<String, QueryResult[]>(warning, result);
	}

	/**
	 * Just to differ between the different modes for generating the rif rule set from the ontology data
	 */
	public static enum GENERATION {
		GENERATEDOPT(){
			@Override
			public String toString(){
				return "Gen. Alt.";
			}
		},
		GENERATED(){
			@Override
			public String toString(){
				return "Generated";
			}
		},
		FIXED(){
			@Override
			public String toString(){
				return "Fixed";
			}
		}
	}

	/**
	 * Helper class to get the RIF rule set dependent on the type of ontology, the generation mode and if inconsistency check rules should be incorporated into the rule set
	 */
	public static class RuleSets {
		private final String path;
		public RuleSets(final String path){
			this.path=path;
		}
		public String getRDFS(final GENERATION mode, final String data){
			switch(mode){
			case FIXED:
				return EvaluationHelper.getResourceAsString(this.path + "rule_rdfs.rif");
			case GENERATED:
				return InferenceHelper.getRIFInferenceRulesForRDFSOntology(data);
			default:
			case GENERATEDOPT:
				return InferenceHelper.getRIFInferenceRulesForRDFSOntologyAlternative(data);
			}
		}
		public String getOWL2RL(final GENERATION mode, final boolean checkInconsistencies, final String data){
			switch(mode){
			case FIXED:
				if(checkInconsistencies){
					return EvaluationHelper.getResourceAsString(this.path + "rule_owl2rl.rif");
				} else {
					return EvaluationHelper.getResourceAsString(this.path + "rule_owl2rlNoInconsistencyRules.rif");
				}
			case GENERATED:
				if(checkInconsistencies){
					return InferenceHelper.getRIFInferenceRulesForOWL2Ontology(data);
				} else {
					return InferenceHelper.getRIFInferenceRulesForOWL2OntologyWithoutCheckingInconsistencies(data);
				}
			default:
			case GENERATEDOPT:
				if(checkInconsistencies){
					return InferenceHelper.getRIFInferenceRulesForOWL2OntologyAlternative(data);
				} else {
					return InferenceHelper.getRIFInferenceRulesForOWL2OntologyAlternativeWithoutCheckingInconsistencies(data);
				}
			}
		}
	}

	/**
	 * just for the different types of inference to be considered during sparql processing
	 */
	public enum SPARQLINFERENCE {
		NONE(){
			@Override
			public String toString(){
				return "None";
			}
			@Override
			public boolean isMaterializationChoice() {
				return false;
			}
			@Override
			public boolean isGeneratedChoice() {
				return false;
			}
			@Override
			public String getRuleSet(final RuleSets rulesets, final GENERATION mode, final boolean checkInconsistencies, final String data, final String rif) {
				return null;
			}
		},
		RIF(){
			@Override
			public boolean isMaterializationChoice() {
				return true;
			}
			@Override
			public boolean isGeneratedChoice() {
				return false;
			}
			@Override
			public String getRuleSet(final RuleSets rulesets, final GENERATION mode, final boolean checkInconsistencies, final String data, final String rif) {
				return rif;
			}
		},
		RDFS(){
			@Override
			public boolean isMaterializationChoice() {
				return true;
			}
			@Override
			public boolean isGeneratedChoice() {
				return true;
			}
			@Override
			public String getRuleSet(final RuleSets rulesets, final GENERATION mode, final boolean checkInconsistencies, final String data, final String rif) {
				return rulesets.getRDFS(mode, data);
			}
		},
		OWL2RL{
			@Override
			public String toString(){
				return "OWL2 RL";
			}
			@Override
			public boolean isMaterializationChoice() {
				return true;
			}
			@Override
			public boolean isGeneratedChoice() {
				return true;
			}
			@Override
			public String getRuleSet(final RuleSets rulesets, final GENERATION mode, final boolean checkInconsistencies, final String data, final String rif) {
				return rulesets.getOWL2RL(mode, checkInconsistencies, data);
			}

			@Override
			public boolean isCheckInconsistenciesChoice() {
				return true;
			}
		};

		/**
		 * @return whether or not materialization of inferred triples can be applied
		 */
		public abstract boolean isMaterializationChoice();

		/**
		 * @return whether or not RIF rules can be generated from a
		 */
		public abstract boolean isGeneratedChoice();

		/**
		 * retrieves the RIF rule set to be considered during sparql query processing
		 * @param rulesets a given RIF rule set (is returned back for RIF mode, otherwise it is not needed)
		 * @param mode the mode after which the RIF rule set is generated (only relevant for ontology inference)
		 * @param checkInconsistencies whether or not rules are generated to check inconsistencies (only relevant for OWL2RL)
		 * @param data a string containing the data in which the ontology is contained (only for RDFS and OWL2RL relevant)
		 * @param rif a string containing the RIF rule set (only for RIF relevant)
		 * @return the RIF rule set to be considered during sparql evaluation
		 */
		public abstract String getRuleSet(RuleSets rulesets, final GENERATION mode, final boolean checkInconsistencies, final String data, final String rif);

		/**
		 * @return whether or or not it is supported to check inconsistencies during ontology inference
		 */
		public boolean isCheckInconsistenciesChoice() {
			return false;
		}
	}

	/**
	 * Just to differ between Materialization- and On Demand-Strategy
	 */
	public static enum SPARQLINFERENCEMATERIALIZATION {
		COMBINEDQUERYOPTIMIZATION(){
			@Override
			public String toString(){
				return "On Demand";
			}
		},
		MATERIALIZEALL(){
			@Override
			public String toString(){
				return "Materialize";
			}
		}
	}

	/**
	 * Just for reading a file from jar or if it is not contained in the jar from filesystem and return the content as string
	 * @param resource the resource to be loaded
	 * @return the file content as string
	 */
	public static String getResourceAsString(final String resource){
		final URL url = EvaluationHelper.class.getResource(resource);

		return FileHelper.readFile(resource, new FileHelper.GetReader() {

				@Override
				public Reader getReader(final String filename) throws FileNotFoundException {
					try {
						InputStream stream = null;
						stream = this.getClass().getResourceAsStream(filename);
						return new java.io.InputStreamReader(stream);
					} catch(final Exception e){
						return new FileReader(url.getFile());
					}
				}
			});
	}
}
