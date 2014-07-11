/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.rif;

import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
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
import lupos.engine.evaluators.MemoryIndexQueryEvaluator;
import lupos.engine.evaluators.QueryEvaluator;
import lupos.engine.evaluators.RDF3XQueryEvaluator;
import lupos.engine.evaluators.StreamQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.application.CollectRIFResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Construct;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.generate.Generate;
import lupos.engine.operators.singleinput.sparul.Insert;
import lupos.engine.operators.stream.Window;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.ArgumentParser;
import lupos.misc.Tuple;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.misc.debug.DebugStep;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.optimizations.logical.rules.generated.RIFRules0RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules10RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules11RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules12RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules13RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules14RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules15RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules16RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules1RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules2RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules3RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules4RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules5RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules6RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules7RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules8RulePackage;
import lupos.optimizations.logical.rules.generated.RIFRules9RulePackage;
import lupos.optimizations.physical.PhysicalOptimizations;
import lupos.rdf.Prefix;
import lupos.rif.datatypes.Predicate;
import lupos.rif.datatypes.RuleResult;
import lupos.rif.generated.parser.RIFParser;
import lupos.rif.generated.syntaxtree.CompilationUnit;
import lupos.rif.model.Document;
import lupos.rif.operator.ConstructPredicate;
import lupos.rif.operator.InsertIndexScan;
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

	/**
	 * This constructor initializes the rule evaluator using the given query evaluator as underlying query evaluator
	 * @param evaluator the underlying query evaluator
	 * @throws Exception
	 */
	public BasicIndexRuleEvaluator(final CommonCoreQueryEvaluator<Node> evaluator)
			throws Exception {
		super();
		this.evaluator = evaluator;
	}

	/**
	 * this constructor setups a rule evaluator, which works in main memory
	 * @throws Exception
	 */
	public BasicIndexRuleEvaluator() throws Exception{
		this(false);
	}

	/**
	 * this constructor setups a rule evaluator, which works in main memory
	 * @param stream if stream is true the rule evaluator for streams is used, otherwise the main memory evaluator
	 * @throws Exception
	 */
	public BasicIndexRuleEvaluator(final boolean stream) throws Exception{
		super();
		this.evaluator = (stream)? new StreamQueryEvaluator(): new MemoryIndexQueryEvaluator();
		this.evaluator.setupArguments();
		this.evaluator.getArgs().set("debug", DEBUG.ALL);
		this.evaluator.getArgs().set("result", QueryResult.TYPE.MEMORY);
		this.evaluator.getArgs().set("codemap", LiteralFactory.MapType.HASHMAP);
		this.evaluator.getArgs().set("distinct", CommonCoreQueryEvaluator.DISTINCT.HASHSET);
		this.evaluator.getArgs().set("optional", CommonCoreQueryEvaluator.JOIN.HASHMAPINDEX);
		this.evaluator.getArgs().set("type", "Turtle");
		this.evaluator.getArgs().set("datastructure", Indices.DATA_STRUCT.HASHMAP);
		this.evaluator.init();
	}

	/**
	 * this constructor setups a rule evaluator, which uses disk-based indices
	 * (it uses a RDF3XQueryEvaluator as underlying query evaluator)
	 * @param directoryOfIndices the directory, in which the indices have been constructed
	 * @throws Exception
	 */
	public BasicIndexRuleEvaluator(final String directoryOfIndices) throws Exception{
		super();
		final RDF3XQueryEvaluator rdf3xEvaluator = new RDF3XQueryEvaluator();
		rdf3xEvaluator.loadLargeScaleIndices(directoryOfIndices);
		this.evaluator = rdf3xEvaluator;
	}

	@Override
	public long compileQuery(final String query) throws Exception {
		return this.compileQuery(query, this.evaluator.createIndexScanCreator());
	}

	public long compileQuery(
			final String query, final IndexScanCreatorInterface indexScanCreator) throws Exception {
		final Date start = new Date();

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

		final Class<? extends Bindings> clazz = Bindings.instanceClass;
		Bindings.instanceClass = BindingsMap.class;
		final Result res = (Result) this.rifDocument.accept(forward, null);
		this.evaluator.setResult(res);
		Bindings.instanceClass = clazz;

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
		final Date start = new Date();
		this.evaluator.logicalOptimization();
		this.evaluator.setBindingsVariablesBasedOnOperatorgraph();
		final RIFRules0RulePackage rules0 = new RIFRules0RulePackage();
		rules0.applyRules(this.evaluator.getRootNode());
		final RIFRules1RulePackage rules1 = new RIFRules1RulePackage();
		rules1.applyRules(this.evaluator.getRootNode());
		final RIFRules2RulePackage rules2 = new RIFRules2RulePackage();
		rules2.applyRules(this.evaluator.getRootNode());
		final RIFRules3RulePackage rules3 = new RIFRules3RulePackage();
		rules3.applyRules(this.evaluator.getRootNode());
		final RIFRules4RulePackage rules4 = new RIFRules4RulePackage();
		rules4.applyRules(this.evaluator.getRootNode());
		final RIFRules5RulePackage rules5 = new RIFRules5RulePackage();
		rules5.applyRules(this.evaluator.getRootNode());
		final RIFRules6RulePackage rules6 = new RIFRules6RulePackage();
		rules6.applyRules(this.evaluator.getRootNode());
		final RIFRules7RulePackage rules7 = new RIFRules7RulePackage();
		rules7.applyRules(this.evaluator.getRootNode());
		final RIFRules8RulePackage rules8 = new RIFRules8RulePackage();
		rules8.applyRules(this.evaluator.getRootNode());
		final RIFRules9RulePackage rules9 = new RIFRules9RulePackage();
		rules9.applyRules(this.evaluator.getRootNode());
		final RIFRules10RulePackage rules10 = new RIFRules10RulePackage();
		rules10.applyRules(this.evaluator.getRootNode());
		final RIFRules11RulePackage rules11 = new RIFRules11RulePackage();
		rules11.applyRules(this.evaluator.getRootNode());
		final RIFRules12RulePackage rules12 = new RIFRules12RulePackage();
		rules12.applyRules(this.evaluator.getRootNode());
		final RIFRules13RulePackage rules13 = new RIFRules13RulePackage();
		rules13.applyRules(this.evaluator.getRootNode());
		final RIFRules14RulePackage rules14 = new RIFRules14RulePackage();
		rules14.applyRules(this.evaluator.getRootNode());
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
		this.evaluator.setBindingsVariablesBasedOnOperatorgraph();
		final RIFRules0RulePackage rules0 = new RIFRules0RulePackage();
		result.addAll(rules0.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules1RulePackage rules1 = new RIFRules1RulePackage();
		result.addAll(rules1.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules2RulePackage rules2 = new RIFRules2RulePackage();
		result.addAll(rules2.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules3RulePackage rules3 = new RIFRules3RulePackage();
		result.addAll(rules3.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules4RulePackage rules4 = new RIFRules4RulePackage();
		result.addAll(rules4.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules5RulePackage rules5 = new RIFRules5RulePackage();
		result.addAll(rules5.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules6RulePackage rules6 = new RIFRules6RulePackage();
		result.addAll(rules6.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules7RulePackage rules7 = new RIFRules7RulePackage();
		result.addAll(rules7.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules8RulePackage rules8 = new RIFRules8RulePackage();
		result.addAll(rules8.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules9RulePackage rules9 = new RIFRules9RulePackage();
		result.addAll(rules9.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules10RulePackage rules10 = new RIFRules10RulePackage();
		result.addAll(rules10.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules11RulePackage rules11 = new RIFRules11RulePackage();
		result.addAll(rules11.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules12RulePackage rules12 = new RIFRules12RulePackage();
		result.addAll(rules12.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules13RulePackage rules13 = new RIFRules13RulePackage();
		result.addAll(rules13.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		final RIFRules14RulePackage rules14 = new RIFRules14RulePackage();
		result.addAll(rules14.applyRulesDebugByteArray(this.evaluator.getRootNode(), prefixInstance));
		this.evaluator.getRootNode().sendMessage(new BoundVariablesMessage());
		result.addAll(this.evaluator.logicalOptimizationDebugByteArray(prefixInstance));
		return result;
	}

	@Override
	public long physicalOptimization() {
		final long start = (new Date()).getTime();
		PhysicalOptimizations.addReplacement("multiinput.join.", "IndexJoinWithDuplicateElimination", "HashMapIndexJoinWithDuplicateElimination");
		this.evaluator.physicalOptimization();
		final RIFRules15RulePackage rules15 = new RIFRules15RulePackage();
		rules15.applyRules(this.evaluator.getRootNode());
		final RIFRules16RulePackage rules16 = new RIFRules16RulePackage();
		rules16.applyRules(this.evaluator.getRootNode());
		this.getRootNode().deleteParents();
		this.getRootNode().setParents();
		this.getRootNode().detectCycles();
//		this.getRootNode().sendMessage(new BoundVariablesMessage());
		return (new Date()).getTime() - start;
	}

	@Override
	public List<DebugContainer<BasicOperatorByteArray>> physicalOptimizationDebugByteArray(final Prefix prefixInstance) {
		this.physicalOptimization();
		final LinkedList<DebugContainer<BasicOperatorByteArray>> debugResult = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
		debugResult.add(new DebugContainer<BasicOperatorByteArray>(
				"After physical optimization...", "physicaloptimizationRule",
				BasicOperatorByteArray.getBasicOperatorByteArray(
						this.getRootNode().deepClone(), prefixInstance)));
		return debugResult;
	}

	@Override
	public long evaluateQuery() throws Exception {
		return this.evaluator.evaluateQuery();
	}

	@Override
	public long evaluateQueryDebugSteps(final DebugStep debugstep, final Application application) throws Exception {
		return this.evaluator.evaluateQueryDebugSteps(debugstep, application);
	}

	public QueryResult getResultWithOnDemandInference(final String inferenceRuleset, final String query) throws Exception{
		return this.getResultWithOnDemandInference(inferenceRuleset, query, false);
	}

	public QueryResult getResultWithOnDemandInference(final String inferenceRuleset, final String query, final boolean oneTime) throws Exception{
		this.compileQueryAndInferenceIntoOneOperatorgraph(inferenceRuleset, query);
		this.logicalOptimization();
		this.physicalOptimization();
		return this.getResult(oneTime);
	}

	@Override
	public QueryResult getResult() throws Exception {
		return this.getResult(false);
	}

	public QueryResult getResult(final boolean oneTime) throws Exception {
		final CollectRIFResult cr = new CollectRIFResult(oneTime);
		this.evaluator.getResultOperator().addApplication(cr);
		this.evaluator.evaluateQuery();
		return cr.getResult();
	}

	public QueryResult[] getResults() throws Exception {
		return this.getResults(false);
	}

	public QueryResult[] getResults(final boolean oneTime) throws Exception {
		final CollectRIFResult cr = new CollectRIFResult(oneTime);
		this.evaluator.getResultOperator().addApplication(cr);
		this.evaluator.evaluateQuery();
		return cr.getQueryResults();
	}

	public CollectRIFResult getCollectedResults(final boolean oneTime) throws Exception {
		final CollectRIFResult cr = new CollectRIFResult(oneTime);
		this.evaluator.getResultOperator().addApplication(cr);
		this.evaluator.evaluateQuery();
		return cr;
	}

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

	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		return this.evaluator.prepareInputData(defaultGraphs, namedGraphs);
	}

	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		return this.evaluator.prepareInputDataWithSourcesOfNamedGraphs(defaultGraphs, namedGraphs);
	}

	public CompilationUnit getCompilationUnit() {
		return this.compilationUnit;
	}

	public Document getDocument() {
		return this.rifDocument;
	}

	public Root getRoot() {
		return (Root) this.evaluator.getRootNode();
	}

	@Override
	public void setupArguments() {
		if(this.evaluator != null) {
			this.evaluator.setupArguments();
		}
	}

	@Override
	public void init() throws Exception {
		if(this.evaluator != null) {
			this.evaluator.init();
		}
	}

	@Override
	public void prepareForQueryDebugSteps(final DebugStep debugstep) {
		if(this.evaluator != null) {
			this.evaluator.prepareForQueryDebugSteps(debugstep);
		}
	}

	private final static URILiteral rif_error = LiteralFactory.createStringURILiteralWithoutException("<http://www.w3.org/2007/rif#error>");

	/**
	 *
	 * @return return a rule result with all predicates rif:error, which are detected errors in the ontology!
	 * @throws Exception
	 */
	public RuleResult inferTriplesAndStoreInDataset() throws Exception {
		final CollectRIFResult cr = this.getCollectedResults(true);
		final RuleResult result = new RuleResult();
		if(this.evaluator instanceof BasicIndexQueryEvaluator){
			for(final QueryResult qr: cr.getQueryResults()){
				if(qr instanceof GraphResult){
					final GraphResult gr = (GraphResult) qr;
					for(final Triple t: gr.getGraphResultTriples()){
						final Collection<Indices> ci = ((BasicIndexQueryEvaluator)this.evaluator).getDataset().getDefaultGraphIndices();
						for (final Indices indices : ci) {
							indices.add(t);
						}
					}
				} else if(qr instanceof RuleResult){
					final RuleResult rr = (RuleResult) qr;
					for(final Predicate predicate: rr.getPredicateResults()){
						if(rif_error.equals(predicate.getName())){
							result.getPredicateResults().add(predicate);
						}
					}
				}
			}
		} else if (this.evaluator instanceof StreamQueryEvaluator) {
			String s = "";
			for(final QueryResult qr: cr.getQueryResults()){
				if(qr instanceof GraphResult){
					final GraphResult gr = (GraphResult) qr;
					// TODO duplicated triple elimination!
					for(final Triple t: gr.getGraphResultTriples()){
						if(!t.getSubject().isBlank() && !t.getSubject().isURI()){
							System.out.println("Warning: The subject of the inferred triple "+t+" is neither an uri nor a blank node and thus the triple will be ignored!");
						} else if(!t.getPredicate().isURI()){
							System.out.println("Warning: The predicate of the inferred triple "+t+" is not an uri and thus the triple will be ignored!");
						} else {
							s += t.getSubject() + " " + t.getPredicate() + " " +t.getObject() + " .\n";
						}
					}
				} else if(qr instanceof RuleResult){
					final RuleResult rr = (RuleResult) qr;
					for(final Predicate predicate: rr.getPredicateResults()){
						if(rif_error.equals(predicate.getName())){
							result.getPredicateResults().add(predicate);
						}
					}
				}
			}
			final URILiteral in = LiteralFactory.createStringURILiteral("<inlinedata:"+s+">");
			((StreamQueryEvaluator)this.evaluator).addToDefaultGraphs(in);
		} else {
			throw new Exception("Unkwon QueryEvaluator Type: " + this.evaluator.getClass());
		}
		if(result.getPredicateResults().size()==0){
			return null;
		} else {
			return result;
		}
	}

	public QueryResult materializeInferredTriplesOfRifEngineAndGetResultOfSPARQLQuery(final String query) throws Exception {
			this.inferTriplesAndStoreInDataset();
			return this.evaluator.getResult(query);
	}

	public long compileQueryAndInferenceIntoOneOperatorgraph(final String inferenceRuleset, final String query) throws Exception {
		final Date a = new Date();
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
		final LinkedList<Generate> listOfConstructedTripel = new LinkedList<Generate>();
		for(final BasicOperator bo: new LinkedList<BasicOperator>(resultInference.getPrecedingOperators())){
			if(bo instanceof Construct){
				final Construct construct = (Construct)bo;
				// split construct and replace them with Generate operators!
				for(final TriplePattern tp: construct.getTemplates()){
					final Generate generate = new Generate(tp);
					for(final BasicOperator father: construct.getPrecedingOperators()){
						father.addSucceedingOperator(generate);
						generate.addPrecedingOperator(father);
					}
					listOfConstructedTripel.add(generate);
					// remove old construct
					for(final BasicOperator father: new HashSet<BasicOperator>(construct.getPrecedingOperators())){
						father.removeSucceedingOperator(construct);
						construct.removePrecedingOperator(father);
					}
				}
			} else if(bo instanceof ConstructPredicate){
				final ConstructPredicate cp = (ConstructPredicate) bo;
				boolean toDelete = true;
				for(final Tuple<URILiteral, List<Item>> tuple: cp.getPredicatePattern()){
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
		final LinkedList<BasicOperator> toBeConnectedTo = determine1stLevelTriplePatternOrIndexScans(rootQuery);
		for(final Generate generate: listOfConstructedTripel){
			generate.getSucceedingOperators().clear();
			final Item[] generateItems = generate.getValueOrVariable();
			for(final BasicOperator tpOrIndexScan: new LinkedList<BasicOperator>(toBeConnectedTo)){
				if(tpOrIndexScan instanceof TriplePattern){
					final TriplePattern tpi = (TriplePattern) tpOrIndexScan;
					if(BasicIndexRuleEvaluator.isMatching(tpi, generateItems)){
						generate.addSucceedingOperator(tpOrIndexScan);
					}
				} else {
					final BasicIndexScan bi = (BasicIndexScan) tpOrIndexScan;
					if(bi.getTriplePattern()!=null && bi.getTriplePattern().size()>0){
						final LinkedList<TriplePattern> matchingTPs = new LinkedList<TriplePattern>();
						for(final TriplePattern inIndexScan: bi.getTriplePattern()){
							if(BasicIndexRuleEvaluator.isMatching(inIndexScan, generateItems)){
								matchingTPs.add(inIndexScan);
								break;
							}
						}
						if(matchingTPs.size()>0){
							// modify BasicIndex in toBeConnectedTo! (delete tp in current bi, add new BasicIndex with tp, join both operators and additionally add tp for generate operator!)
							for(final TriplePattern tp: matchingTPs){
								final TriplePattern newTP = new TriplePattern(tp.getPos(0), tp.getPos(1), tp.getPos(2));
								newTP.recomputeVariables();
								generate.addSucceedingOperator(newTP);
								newTP.addPrecedingOperator(generate);

								if(bi.getTriplePattern().size()==1){
									newTP.addSucceedingOperators(new LinkedList<OperatorIDTuple>(bi.getSucceedingOperators()));
									for(final OperatorIDTuple opID: bi.getSucceedingOperators()){
										opID.getOperator().addPrecedingOperator(newTP);
									}
								} else {
									bi.getTriplePattern().remove(tp);
									final Join join = new Join();
									join.setUnionVariables(bi.getUnionVariables());
									bi.recomputeVariables();
									tp.recomputeVariables();
									final HashSet<Variable> joinVars = new HashSet<Variable>(tp.getUnionVariables());
									joinVars.retainAll(bi.getUnionVariables());
									join.setIntersectionVariables(joinVars);
									for(final OperatorIDTuple opID: bi.getSucceedingOperators()){
										final BasicOperator suc = opID.getOperator();
										suc.removePrecedingOperator(bi);
										suc.addPrecedingOperator(join);
									}
									join.setSucceedingOperators(bi.getSucceedingOperators());
									bi.setSucceedingOperator(new OperatorIDTuple(join, 0));
									join.addPrecedingOperator(bi);

									final LinkedList<TriplePattern> tpList = new LinkedList<TriplePattern>();
									tpList.add(tp);
									final BasicIndexScan newIndex = ((Root)rootQuery).newIndexScan(new OperatorIDTuple(join, 1), tpList, bi.getGraphConstraint());
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

		rootInference.deleteParents();
		rootInference.setParents();
		rootInference.detectCycles();
		// determine those operators, which are not connected to the result operator (or an insert operator), and delete them
		final HashSet<BasicOperator> visited = new HashSet<BasicOperator>();
		final HashSet<BasicOperator> connectedOperators = new HashSet<BasicOperator>();
		final LinkedList<BasicOperator> path = new LinkedList<BasicOperator>();
		path.add(rootInference);
		BasicIndexRuleEvaluator.determineOperatorsConnectedWithResultOrInsert(rootInference, path, visited, connectedOperators);

		for(final BasicOperator bo: visited){
			if(!connectedOperators.contains(bo)){
				bo.removeFromOperatorGraphWithoutConnectingPrecedingWithSucceedingOperators();
			}
		}

		// add the first operations of the inference operator graph to the operator graph of the query
		for(final OperatorIDTuple rootChild: new LinkedList<OperatorIDTuple>(rootInference.getSucceedingOperators())){
			rootInference.removeSucceedingOperator(rootChild);
			final BasicOperator rootChildOperator = rootChild.getOperator();
			rootChildOperator.removePrecedingOperator(rootInference);
			rootChildOperator.addPrecedingOperator(rootQuery);
			if(rootChildOperator instanceof InsertIndexScan) {
				// these operators are used to insert facts/triples => should occur as leftmost operators after the root such that they are evaluated first!
				final LinkedList<OperatorIDTuple> list = new LinkedList<OperatorIDTuple>(rootQuery.getSucceedingOperators());
				list.addFirst(rootChild);
				rootQuery.setSucceedingOperators(list);
			} else {
				rootQuery.addSucceedingOperator(rootChild);
			}
		}
		rootQuery.deleteParents();
		rootQuery.setParents();
		rootQuery.detectCycles();
	}

	public static void determineOperatorsConnectedWithResultOrInsert(final BasicOperator currentOperator, final LinkedList<BasicOperator> currentPath, final HashSet<BasicOperator> visited, final HashSet<BasicOperator> connectedOperators){
		if(currentOperator instanceof Result || currentOperator instanceof Insert){
			BasicIndexRuleEvaluator.addToConnectedOperators(currentPath, connectedOperators);
		} else {
			if(visited.contains(currentOperator)){
				if(connectedOperators.contains(currentOperator)){
					BasicIndexRuleEvaluator.addToConnectedOperators(currentPath, connectedOperators);
				}
				return;
			} else {
				visited.add(currentOperator);
				for(final OperatorIDTuple opIDTuple: currentOperator.getSucceedingOperators()){
					final BasicOperator suc = opIDTuple.getOperator();
					currentPath.addLast(suc);
					BasicIndexRuleEvaluator.determineOperatorsConnectedWithResultOrInsert(suc, currentPath, visited, connectedOperators);
					currentPath.removeLast();
				}
			}
		}
	}

	private static void addToConnectedOperators(final Collection<BasicOperator> toBeAdded, final HashSet<BasicOperator> connectedOperators){
		for(final BasicOperator inPath: toBeAdded){
			if(!connectedOperators.contains(inPath)){
				connectedOperators.add(inPath);
				BasicIndexRuleEvaluator.addToConnectedOperators(inPath.getCycleOperands(), connectedOperators);
			} else {
				connectedOperators.add(inPath);
			}
		}
	}

	private static boolean isMatching(final TriplePattern tp, final Item[] generateItems){
		boolean flag=true;
		for(int i=0; i<3; i++){
			final Item a = generateItems[i];
			final Item b = tp.getPos(i);
			if(!a.isVariable() && !b.isVariable() && !a.equals(b)){
				flag=false;
				break;
			}
		}
		return flag;
	}

	public static void deletePrecedingOperators(final BasicOperator toDelete){
		if(toDelete.getSucceedingOperators().size()>0){
			// maybe result of this operation is somewhere else used => end of recursion
			return;
		}
		for(final BasicOperator parent: new LinkedList<BasicOperator>(toDelete.getPrecedingOperators())){
			parent.removeSucceedingOperator(toDelete);
			toDelete.removePrecedingOperator(parent);
			deletePrecedingOperators(parent);
		}
	}

	public static LinkedList<BasicOperator> determine1stLevelTriplePatternOrIndexScans(final BasicOperator rootQuery){
		final LinkedList<BasicOperator> resultlist = new LinkedList<BasicOperator>();
		determine1stLevelTriplePatternOrIndexScans(rootQuery, resultlist);
		return resultlist;
	}

	private static void determine1stLevelTriplePatternOrIndexScans(final BasicOperator rootQuery, final LinkedList<BasicOperator> resultlist) {
		for(final OperatorIDTuple child: rootQuery.getSucceedingOperators()){
			final BasicOperator childOperator = child.getOperator();
			if(childOperator instanceof TriplePattern || childOperator instanceof BasicIndexScan){
				resultlist.add(childOperator);
			} else if(childOperator instanceof PatternMatcher || childOperator instanceof Window){
				determine1stLevelTriplePatternOrIndexScans(childOperator, resultlist);
			}
		}
	}
}
