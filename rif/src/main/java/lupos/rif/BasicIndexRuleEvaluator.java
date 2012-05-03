package lupos.rif;

import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JProgressBar;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.evaluators.DebugContainerQuery;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.engine.evaluators.StreamQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.application.CollectAllResults;
import lupos.engine.operators.application.CollectRIFResult;
import lupos.engine.operators.index.BasicIndex;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.IndexCollection;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Construct;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.stream.Window;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.ArgumentParser;
import lupos.misc.Tuple;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.misc.debug.DebugStep;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.optimizations.logical.rules.generated.RIFRulesRulePackage;
import lupos.rdf.Prefix;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;
import lupos.rif.generated.parser.RIFParser;
import lupos.rif.generated.syntaxtree.CompilationUnit;
import lupos.rif.model.Document;
import lupos.rif.operator.ConstructPredicate;
import lupos.rif.visitor.BuildOperatorGraphRuleVisitor;
import lupos.rif.visitor.NormalizeRuleVisitor;
import lupos.rif.visitor.ParseSyntaxTreeVisitor;
import lupos.rif.visitor.ResolveListsRuleVisitor;
import lupos.rif.visitor.RuleDependencyGraphVisitor;
import lupos.rif.visitor.RuleFilteringVisitor;
import lupos.rif.visitor.SubstituteFunctionCallsVisitor;
import lupos.rif.visitor.ValidateRuleVisitor;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;

public class BasicIndexRuleEvaluator extends QueryEvaluator<Node> {
	protected final CommonCoreQueryEvaluator<Node> evaluator;
	private CompilationUnit compilationUnit;
	private Document rifDocument;

	public BasicIndexRuleEvaluator(final CommonCoreQueryEvaluator<Node> evaluator)
			throws Exception {
		super();
		this.evaluator = evaluator;
	}

	@Override
	public long compileQuery(String query) throws Exception {
		return compileQuery(query, this.evaluator.createIndexScanCreator());
	}

	public long compileQuery(
			String query, IndexScanCreatorInterface indexScanCreator) throws Exception {
		Date start = new Date();

		final RIFParser parser = new RIFParser(new StringReader(query));
		this.compilationUnit = parser.CompilationUnit();
		this.rifDocument = (Document) this.compilationUnit.accept(new ParseSyntaxTreeVisitor(), null);
		final BuildOperatorGraphRuleVisitor forward = new BuildOperatorGraphRuleVisitor(indexScanCreator);
		final ValidateRuleVisitor valVisitor = new ValidateRuleVisitor();
		final NormalizeRuleVisitor normVisitor = new NormalizeRuleVisitor();
		final SubstituteFunctionCallsVisitor subVisitor = new SubstituteFunctionCallsVisitor();
		final ResolveListsRuleVisitor listVisitor = new ResolveListsRuleVisitor();
		final RuleDependencyGraphVisitor dependencyVisitor = new RuleDependencyGraphVisitor();
		final RuleFilteringVisitor filteringVisitor = new RuleFilteringVisitor();

		this.rifDocument = (Document) this.rifDocument.accept(subVisitor, null);
		this.rifDocument = (Document) this.rifDocument.accept(listVisitor, null);
		this.rifDocument = (Document) this.rifDocument.accept(normVisitor, null);
		this.rifDocument.accept(valVisitor, null);
		this.rifDocument.accept(dependencyVisitor, null);
		this.rifDocument.accept(filteringVisitor, null);

		Class<?> clazz = Bindings.instanceClass;
		Bindings.instanceClass = BindingsMap.class;
		final Result res = (Result) rifDocument.accept(forward, null);
		this.evaluator.setResult(res);
		Bindings.instanceClass = (Class<? extends Bindings>) clazz;

		final BasicOperator root = indexScanCreator.getRoot();
		this.evaluator.setRootNode(root);
		
		root.setParents();
		root.detectCycles();
		root.sendMessage(new BoundVariablesMessage());
		this.evaluator.setBindingsVariablesBasedOnOperatorgraph();

		return new Date().getTime() - start.getTime();
	}
	
	@Override
	public long logicalOptimization() {
		Date start = new Date();
		this.evaluator.logicalOptimization();
		final RIFRulesRulePackage rules = new RIFRulesRulePackage();
		rules.applyRules(this.evaluator.getRootNode());
		this.evaluator.getRootNode().sendMessage(new BoundVariablesMessage());
		this.evaluator.logicalOptimization();
		return new Date().getTime() - start.getTime();
	}
	
	@Override
	public List<DebugContainer<BasicOperatorByteArray>> logicalOptimizationDebugByteArray(
			final Prefix prefixInstance) {
		final List<DebugContainer<BasicOperatorByteArray>> result = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
		result.add(new DebugContainer<BasicOperatorByteArray>(
				"Before logical optimization...",
				"logicaloptimizationPackageDescription", BasicOperatorByteArray
				.getBasicOperatorByteArray(this.evaluator.getRootNode().deepClone(),
						prefixInstance)));
		result.addAll(this.evaluator.logicalOptimizationDebugByteArray(prefixInstance));
		final RIFRulesRulePackage rules = new RIFRulesRulePackage();
		result.addAll(rules.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		this.evaluator.getRootNode().sendMessage(new BoundVariablesMessage());
		result.addAll(this.evaluator.logicalOptimizationDebugByteArray(prefixInstance));
		return result;
	}
	
	@Override
	public long physicalOptimization() {
		return this.evaluator.physicalOptimization();
	}

	@Override
	public List<DebugContainer<BasicOperatorByteArray>> physicalOptimizationDebugByteArray(final Prefix prefix) {
		return this.evaluator.physicalOptimizationDebugByteArray(prefix);
	}

	@Override
	public long evaluateQuery() throws Exception {
		return this.evaluator.evaluateQuery();
	}
	
	@Override
	public long evaluateQueryDebugSteps(final DebugStep debugstep, Application application) throws Exception {
		return this.evaluator.evaluateQueryDebugSteps(debugstep, application);
	}

	@Override
	public QueryResult getResult() throws Exception {
		final CollectRIFResult cr = new CollectRIFResult();
		this.evaluator.getResultOperator().addApplication(cr);
		this.evaluator.evaluateQuery();
		return cr.getResult();	
	}
	
	public CollectRIFResult getCollectedResults() throws Exception {
		final CollectRIFResult cr = new CollectRIFResult();
		this.evaluator.getResultOperator().addApplication(cr);
		this.evaluator.evaluateQuery();
		return cr;
	}

//	public Dataset getDataset() {
//		return evaluator.getDataset();
//	}

	
	public Result getResultOperator(){
		return this.evaluator.getResultOperator();
	}
	
	public BasicOperator getRootNode(){
		return this.evaluator.getRootNode();
	}

	@Override
	public ArgumentParser getArgs() {
		return this.evaluator.getArgs();
	}


	public CommonCoreQueryEvaluator<Node> getEvaluator() {
		return this.evaluator;
	}

//	public IndexCollection createIndexCollection() {
//		return evaluator.createIndexCollection();
//	}

	@Override
	public long prepareInputData(Collection<URILiteral> defaultGraphs,
			Collection<URILiteral> namedGraphs) throws Exception {
		return this.evaluator.prepareInputData(defaultGraphs, namedGraphs);
	}
	
	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		return this.evaluator.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
	}

	public CompilationUnit getCompilationUnit() {
		return this.compilationUnit;
	}

	public Document getDocument() {
		return this.rifDocument;
	}
	
	public IndexCollection getIndexCollection() {
		return (IndexCollection) evaluator.getRootNode();
	}

	public void setupArguments() {
		if(this.evaluator != null) {
			this.evaluator.setupArguments();
		}
	}

	public void init() throws Exception {
		if(this.evaluator != null) {
			this.evaluator.init();
		}
	}
	
	private final static URILiteral rif_error = LiteralFactory.createStringURILiteralWithoutException("<http://www.w3.org/2007/rif#error>");
	
	/**
	 * 
	 * @return return a rule result with all predicates rif:error, which are detected errors in the ontology!
	 * @throws Exception
	 */
	public RuleResult inferTriplesAndStoreInDataset() throws Exception {
		CollectRIFResult cr = this.getCollectedResults();
		RuleResult result = new RuleResult();
		if(this.evaluator instanceof BasicIndexQueryEvaluator){
			for(QueryResult qr: cr.getQueryResults()){
				if(qr instanceof GraphResult){
					GraphResult gr = (GraphResult) qr;
					for(Triple t: gr.getGraphResultTriples()){
						final Collection<Indices> ci = ((BasicIndexQueryEvaluator)this.evaluator).getDataset().getDefaultGraphIndices();
						for (final Indices indices : ci) {
							if (!indices.contains(t)) {
								indices.add(t);
							}					
						}
					}
				} else if(qr instanceof RuleResult){
					RuleResult rr = (RuleResult) qr;
					for(Predicate predicate: rr.getPredicateResults()){
						if(rif_error.equals(predicate.getName())){
							result.getPredicateResults().add(predicate);
						}
					}
				}
			}
		} else if (this.evaluator instanceof StreamQueryEvaluator) {
			String s = "";
			for(QueryResult qr: cr.getQueryResults()){
				if(qr instanceof GraphResult){
					GraphResult gr = (GraphResult) qr;
					// TODO duplicated triple elimination!
					for(Triple t: gr.getGraphResultTriples()){
						if(!t.getSubject().isBlank() && !t.getSubject().isURI()){
							System.out.println("Warning: The subject of the inferred triple "+t+" is neither an uri nor a blank node and thus the triple will be ignored!");
						} else if(!t.getPredicate().isURI()){
							System.out.println("Warning: The predicate of the inferred triple "+t+" is not an uri and thus the triple will be ignored!");
						} else {
							s += t.getSubject() + " " + t.getPredicate() + " " +t.getObject() + " .\n";
						}
					}
				} else if(qr instanceof RuleResult){
					RuleResult rr = (RuleResult) qr;
					for(Predicate predicate: rr.getPredicateResults()){
						if(rif_error.equals(predicate.getName())){
							result.getPredicateResults().add(predicate);
						}
					}
				}
			}
			URILiteral in = LiteralFactory.createStringURILiteral("<inlinedata:"+s+">");
			((StreamQueryEvaluator)this.evaluator).addToDefaultGraphs(in);
		} else throw new Exception("Unkwon QueryEvaluator Type: " + this.evaluator.getClass());
		if(result.getPredicateResults().size()==0){
			return null;
		} else {
			return result;
		}
	}
	
	public QueryResult materializeInferredTriplesOfRifEngineAndGetResultOfSPARQLQuery(String query) throws Exception {
			this.inferTriplesAndStoreInDataset();
			return this.evaluator.getResult(query);
	}
	
	public long compileQueryAndInferenceIntoOneOperatorgraph(final String inferenceRuleset, final String query) throws Exception {
		Date a = new Date();
		this.compileQuery(inferenceRuleset);
		final BasicOperator rootInference = this.getRootNode();
		final Result resultInference = this.getResultOperator();
		this.evaluator.compileQuery(query);
		integrateInferenceOperatorgraphIntoQueryOperatorgraph(rootInference, resultInference, this.evaluator.getRootNode(), this.evaluator.getResultOperator());
		this.evaluator.setBindingsVariablesBasedOnOperatorgraph();
		return ((new Date()).getTime() - a.getTime());
	}
	
	public static void integrateInferenceOperatorgraphIntoQueryOperatorgraph(final BasicOperator rootInference, final Result resultInference, final BasicOperator rootQuery, final Result resultQuery){
		// first determine those operations, which generate triples of the result (and delete the other ones (and their preceding operators...))
		// also replace Construct operators with Generate operators!
		LinkedList<Generate> listOfConstructedTripel = new LinkedList<Generate>();
		for(BasicOperator bo: new LinkedList<BasicOperator>(resultInference.getPrecedingOperators())){
			if(bo instanceof Construct){
				Construct construct = (Construct)bo;
				// split construct and replace them with Generate operators!
				for(TriplePattern tp: construct.getTemplates()){
					Generate generate = new Generate(tp);
					for(BasicOperator father: construct.getPrecedingOperators()){
						father.addSucceedingOperator(generate);
						generate.addPrecedingOperator(father);
					}
					listOfConstructedTripel.add(generate);
					// remove old construct
					for(BasicOperator father: new HashSet<BasicOperator>(construct.getPrecedingOperators())){
						father.removeSucceedingOperator(construct);
						construct.removePrecedingOperator(father);
					}
				} 				
			} else if(bo instanceof ConstructPredicate){
				ConstructPredicate cp = (ConstructPredicate) bo;
				boolean toDelete = true;
				for(Tuple<URILiteral, List<Item>> tuple: cp.getPredicatePattern()){
					if(BasicIndexRuleEvaluator.rif_error.equals(tuple.getFirst())){
						// predicates as result of detecting errors in the ontology should remain! 
						toDelete = false;
						break;
					}
				}
				if(toDelete){
					deletePrecedingOperators(bo);
				} else {
					cp.setSucceedingOperator(new OperatorIDTuple(resultQuery, 0));
					resultQuery.addPrecedingOperator(cp);
				}
			} else {
				deletePrecedingOperators(bo);
			}
		}		
		// now connect generate operations with triple patterns/index scans of the query operator graph
		LinkedList<BasicOperator> toBeConnectedTo = determine1stLevelTriplePatternOrIndexScans(rootQuery);
		for(Generate generate: listOfConstructedTripel){
			generate.getSucceedingOperators().clear();			
			Item[] generateItems = generate.getValueOrVariable();
			for(BasicOperator tpOrIndexScan: new LinkedList<BasicOperator>(toBeConnectedTo)){
				if(tpOrIndexScan instanceof TriplePattern){
					TriplePattern tpi = (TriplePattern) tpOrIndexScan;
					if(isMatching(tpi, generateItems)){
						generate.addSucceedingOperator(tpOrIndexScan);
					}
				} else {
					BasicIndex bi = (BasicIndex) tpOrIndexScan;
					if(bi.getTriplePattern()!=null && bi.getTriplePattern().size()>0){
						LinkedList<TriplePattern> matchingTPs = new LinkedList<TriplePattern>();
						for(TriplePattern inIndexScan: bi.getTriplePattern()){
							if(isMatching(inIndexScan, generateItems)){
								matchingTPs.add(inIndexScan);
								break;
							}
						}
						if(matchingTPs.size()>0){
							// modify BasicIndex in toBeConnectedTo! (delete tp in current bi, add new BasicIndex with tp, join both operators and additionally add tp for generate operator!)
							for(TriplePattern tp: matchingTPs){
								TriplePattern newTP = new TriplePattern(tp.getPos(0), tp.getPos(1), tp.getPos(2));
								newTP.recomputeVariables();
								generate.addSucceedingOperator(newTP);
								newTP.addPrecedingOperator(generate);

								if(bi.getTriplePattern().size()==1){
									newTP.addSucceedingOperators(new LinkedList<OperatorIDTuple>(bi.getSucceedingOperators()));
									for(OperatorIDTuple opID: bi.getSucceedingOperators()){
										opID.getOperator().addPrecedingOperator(newTP);
									}
								} else {
									bi.getTriplePattern().remove(tp);
									Join join = new Join();
									join.setUnionVariables(bi.getUnionVariables());
									bi.recomputeVariables();
									tp.recomputeVariables();
									HashSet<Variable> joinVars = new HashSet<Variable>(tp.getUnionVariables());
									joinVars.retainAll(bi.getUnionVariables());
									join.setIntersectionVariables(joinVars);
									for(OperatorIDTuple opID: bi.getSucceedingOperators()){
										BasicOperator suc = opID.getOperator();
										suc.removePrecedingOperator(bi);
										suc.addPrecedingOperator(join);
									}
									join.setSucceedingOperators(bi.getSucceedingOperators());
									bi.setSucceedingOperator(new OperatorIDTuple(join, 0));
									join.addPrecedingOperator(bi);

									LinkedList<TriplePattern> tpList = new LinkedList<TriplePattern>();
									tpList.add(tp);
									BasicIndex newIndex = ((IndexCollection)rootQuery).newIndex(new OperatorIDTuple(join, 1), tpList, bi.getGraphConstraint());
									newIndex.recomputeVariables();
									join.addPrecedingOperator(newIndex);
									rootQuery.addSucceedingOperator(newIndex);
									newIndex.addPrecedingOperator(rootQuery);
									newTP.addSucceedingOperator(new OperatorIDTuple(join, 1));
									join.addPrecedingOperator(newTP);
									
									toBeConnectedTo.add(newIndex);
								}
							}
						}
					}
				}
			}
			if(generate.getSucceedingOperators().size()==0){
				// this generate operator is not connected to any other operator and thus can be deleted!
				deletePrecedingOperators(generate);
			}
		}		
		// add the first operations of the inference operator graph to the operator graph of the query 
		for(OperatorIDTuple rootChild: new LinkedList<OperatorIDTuple>(rootInference.getSucceedingOperators())){
			rootInference.removeSucceedingOperator(rootChild);
			BasicOperator rootChildOperator = rootChild.getOperator();
			rootChildOperator.removePrecedingOperator(rootInference);
			rootChildOperator.addPrecedingOperator(rootQuery);
			rootQuery.addSucceedingOperator(rootChild);
		}
		
	}
	
	private static boolean isMatching(final TriplePattern tp, final Item[] generateItems){
		boolean flag=true;
		for(int i=0; i<3; i++){
			Item a = generateItems[i];
			Item b = tp.getPos(i);
			if(!a.isVariable() && !b.isVariable() && !a.equals(b)){
				flag=false;
				break;
			}
		}
		return flag;
	}
	
	public static void deletePrecedingOperators(BasicOperator toDelete){
		if(toDelete.getSucceedingOperators().size()>0){
			// maybe result of this operation is somewhere else used => end of recursion
			return;
		}
		for(BasicOperator parent: new LinkedList<BasicOperator>(toDelete.getPrecedingOperators())){
			parent.removeSucceedingOperator(toDelete);
			toDelete.removePrecedingOperator(parent);
			deletePrecedingOperators(parent);
		}
	}
	
	public static LinkedList<BasicOperator> determine1stLevelTriplePatternOrIndexScans(BasicOperator rootQuery){
		LinkedList<BasicOperator> resultlist = new LinkedList<BasicOperator>();
		determine1stLevelTriplePatternOrIndexScans(rootQuery, resultlist);
		return resultlist;
	}
	
	private static void determine1stLevelTriplePatternOrIndexScans(BasicOperator rootQuery, LinkedList<BasicOperator> resultlist) {
		for(OperatorIDTuple child: rootQuery.getSucceedingOperators()){
			BasicOperator childOperator = child.getOperator();
			if(childOperator instanceof TriplePattern || childOperator instanceof BasicIndex){
				resultlist.add(childOperator);
			} else if(childOperator instanceof PatternMatcher || childOperator instanceof Window){
				determine1stLevelTriplePatternOrIndexScans(childOperator, resultlist);
			}
		}
	}
}
