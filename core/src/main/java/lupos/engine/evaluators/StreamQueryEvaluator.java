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
package lupos.engine.evaluators;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.application.CountResult;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Dataset.ONTOLOGY;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.adaptedRDF3X.SixIndices;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.multiinput.Union;
import lupos.engine.operators.rdfs.AlternativeRDFSchemaInference;
import lupos.engine.operators.rdfs.RDFSchemaInference;
import lupos.engine.operators.rdfs.RudimentaryRDFSchemaInference;
import lupos.engine.operators.rdfs.index.RDFSRoot;
import lupos.engine.operators.singleinput.AddBinding;
import lupos.engine.operators.stream.Stream;
import lupos.engine.operators.tripleoperator.TriggerOneTime;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.Tuple;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.misc.debug.DebugStep;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.optimizations.logical.rules.externalontology.ExternalOntologyRuleEngine;
import lupos.optimizations.logical.rules.generated.CorrectOperatorgraphRulePackage;
import lupos.optimizations.logical.rules.generated.LogicalOptimizationForStreamEngineRulePackage;
import lupos.optimizations.logical.rules.rdfs.RDFSRuleEngine0;
import lupos.optimizations.logical.rules.rdfs.RDFSRuleEngine1;
import lupos.optimizations.logical.rules.rdfs.RDFSRuleEngine2;
import lupos.optimizations.physical.PhysicalOptimizations;
import lupos.optimizations.sparql2core_sparql.SPARQL2CoreSPARQLParserVisitorImplementationDumper;
import lupos.rdf.Prefix;
import lupos.sparql1_1.ASTPrefixDecl;
import lupos.sparql1_1.ASTQuery;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.ParseException;
import lupos.sparql1_1.SimpleNode;
import lupos.sparql1_1.StreamSPARQL1_1Parser;
import lupos.sparql1_1.operatorgraph.StreamOperatorGraphGenerator;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_Stream;

public class StreamQueryEvaluator extends CommonCoreQueryEvaluator<Node> {

	private enum Optimizations {
		NONE
	}

	protected enum MATCHER {
		DEFAULT, SIMPLE, HASH, RDFSSIMPLE;
	}

	public StreamQueryEvaluator(final DEBUG debug, final boolean multiplequeries, final compareEvaluator compare, final String compareoptions, final int times, final String dataset,
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
			final QueryResult.TYPE resulttype, final STORAGE storage,
			final JOIN join, final JOIN optional, final SORT sort,
			final DISTINCT distinct,
			final MERGE_JOIN_OPTIONAL merge_join_optional, final String encoding,
			final MATCHER matcher) throws Exception{
		super(debug, multiplequeries, compare, compareoptions, times, dataset,
				type, externalontology,inmemoryexternalontologyinference, rdfs, codemap, tmpDirs, loadindexinfo,
				parallelOperands,blockwise,
				limit,jointhreads,joinbuffer,
				heap, tosort, indexheap, mergeheapheight, mergeheaptype, chunk, mergethreads, yagomax,
				resulttype, storage, join, optional, sort, distinct,
				merge_join_optional, encoding);
		this.init(matcher);
	}

	@Override
	public void setupArguments() {
		this.defaultOptimization = Optimizations.NONE;
		this.defaultRDFS = RDFS.NONE;
		super.setupArguments();
		this.args.addEnumOption("matcher",
				"Specify the pattern matcher algorithm to be used",
				MATCHER.DEFAULT);
	}

	protected SixIndices externalOntology;
	protected MATCHER matcher;

	private ONTOLOGY getONTOLOGYRDFS(final RDFS rdfs) {
		switch (rdfs) {
		case RDFS:
			return ONTOLOGY.RDFS;
		case OPTIMIZEDRDFS:
			return ONTOLOGY.ONTOLOGYRDFS;
		case RUDIMENTARYRDFS:
			return ONTOLOGY.RUDIMENTARYRDFS;
		case OPTIMIZEDRUDIMENTARYRDFS:
			return ONTOLOGY.ONTOLOGYRUDIMENTARYRDFS;
		case ALTERNATIVERDFS:
			return ONTOLOGY.ALTERNATIVERDFS;
		case OPTIMIZEDALTERNATIVERDFS:
			return ONTOLOGY.ONTOLOGYALTERNATIVERDFS;
		default:
		case NONE:
			return ONTOLOGY.NONE;
		}
	}

	private void init(final MATCHER matcher) throws Exception {
		this.matcher = matcher;
		if (this.externalontology != null && this.externalontology.compareTo("") != 0) {
			final String[] inp = new String[2];
			final int index = this.externalontology.indexOf(',');
			if (index < 0) {
				System.err
						.println("Usage: '--externalOntology Format,File', where Format can be e.g. N3...");
			} else {
				inp[0] = this.externalontology.substring(0, index);
				inp[1] = this.externalontology.substring(index + 1);
				final Collection<URILiteral> defaultGraphs = new LinkedList<URILiteral>();
				try {
					if (inp[1].startsWith("<")) {
						defaultGraphs.add(LiteralFactory
								.createURILiteralWithoutLazyLiteral(inp[1]));
					} else {
						defaultGraphs.add(LiteralFactory
								.createURILiteralWithoutLazyLiteral("<file:"
										+ inp[1] + ">"));
					}
				} catch (final URISyntaxException e1) {
					System.err.println(e1);
					e1.printStackTrace();
				}
				final Dataset dataset = new Dataset(
						defaultGraphs,
						new LinkedList<URILiteral>(),
						inp[0],
						this.getONTOLOGYRDFS(this.rdfs),
						BasicIndexScan.MERGEJOIN,
						new Dataset.IndicesFactory() {
							@Override
							public Indices createIndices(
									final URILiteral uriLiteral) {
								return new SixIndices(uriLiteral);
							}

							@Override
							public lupos.engine.operators.index.Root createRoot() {
								return new lupos.engine.operators.index.adaptedRDF3X.RDF3XRoot();
							}
						}, this.debug == DEBUG.ALL || this.debug == DEBUG.WITHOUTRESULT,
						this.inmemoryexternalontologyinference);
				// get now the data plus inferred triples of the external
				// ontology...
				this.externalOntology = (SixIndices) dataset
						.getDefaultGraphIndices().iterator().next();
			}
		} else {
			this.externalOntology = null;
		}
	}

	@Override
	public void init() throws Exception {
		super.init();
		this.init((MATCHER) this.args.getEnum("matcher"));
	}

	private Collection<PatternMatcher> patternMatchers;
	private Collection<URILiteral> defaultGraphs;

	public StreamQueryEvaluator() throws Exception {
	}

	public StreamQueryEvaluator(final String[] arguments) throws Exception {
		super(arguments);
	}

	private void determinePatternMatchers() {
		if (this.rootNode instanceof PatternMatcher) {
			this.patternMatchers = new LinkedList<PatternMatcher>();
			this.patternMatchers.add((PatternMatcher) this.rootNode);
		} else if (this.rootNode instanceof Stream) {
			this.patternMatchers = ((Stream) this.rootNode).getPatternMatchers();
		}
	}

	private void checkForTimeFunc(final Node root) {
		//		if (root instanceof ASTTimeFuncNode) {
		//			Bindings.instanceClass = BindingsMap.class;
		//		} else {
		final Node[] na = root.getChildren();
		if (na != null)
		 {
			for (final Node sn : na) {
				this.checkForTimeFunc(sn);
			}
		//		}
		}
	}

	@Override
	public long compileQuery(final String query) throws ParseException {
		final Date a = new Date();
		SimpleNode root = StreamSPARQL1_1Parser.parse(query);
		try {
			final SPARQL2CoreSPARQLParserVisitorImplementationDumper spvid = SPARQL2CoreSPARQLParserVisitorImplementationDumper.createInstance();
			final String corequery = (root==null)?"":(String) spvid.visit(root);
			root = StreamSPARQL1_1Parser.parse(corequery);
			// checkForTimeFunc(root);
			final StreamOperatorGraphGenerator spvi = StreamOperatorGraphGenerator.createOperatorGraphGenerator(this);
			spvi.visit((ASTQuery)root);
			this.rootNode = spvi.getOperatorgraphRoot();
			this.result = spvi.getResult();
			this.determinePatternMatchers();
			this.rootNode.deleteParents();
			this.rootNode.setParents();
			this.rootNode.detectCycles();
			this.rootNode.sendMessage(new BoundVariablesMessage());
			final CorrectOperatorgraphRulePackage recog = new CorrectOperatorgraphRulePackage();
			recog.applyRules(this.rootNode);
			if (this.rdfs != RDFS.NONE) {
				for (final PatternMatcher zpm : this.patternMatchers) {
					final RDFSRoot ic = new RDFSRoot();
					if (this.rdfs == RDFS.RDFS || this.rdfs == RDFS.OPTIMIZEDRDFS) {
						if (this.externalOntology != null) {
							RDFSchemaInference.addInferenceRulesForInstanceData(ic,
									zpm);
						} else {
							RDFSchemaInference.addInferenceRules(ic, zpm);
						}
					} else if (this.rdfs == RDFS.RUDIMENTARYRDFS
							|| this.rdfs == RDFS.OPTIMIZEDRUDIMENTARYRDFS) {
						if (this.externalOntology != null) {
							RudimentaryRDFSchemaInference
							.addInferenceRulesForInstanceData(ic, zpm);
						} else {
							RudimentaryRDFSchemaInference
							.addInferenceRules(ic, zpm);
						}
					} else if (this.rdfs == RDFS.ALTERNATIVERDFS
							|| this.rdfs == RDFS.OPTIMIZEDALTERNATIVERDFS) {
						if (this.externalOntology != null) {
							AlternativeRDFSchemaInference
							.addInferenceRulesForInstanceData(ic, zpm);
						} else {
							AlternativeRDFSchemaInference
							.addInferenceRules(ic, zpm);
						}
					}
					ic.addToPatternMatcher(zpm);
					zpm.deleteParents();
					zpm.setParents();
					zpm.detectCycles();
					zpm.sendMessage(new BoundVariablesMessage());
					if (this.externalOntology != null) {
						this.precompileExternalOntology(zpm);
						zpm.deleteParents();
						zpm.setParents();
						zpm.detectCycles();
						zpm.sendMessage(new BoundVariablesMessage());
						final ExternalOntologyRuleEngine eore = new ExternalOntologyRuleEngine();
						eore.applyRules(zpm);
					}
				}
			}
			this.setBindingsVariablesBasedOnOperatorgraph();
		} catch (final InstantiationException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public DebugContainerQuery<BasicOperatorByteArray, Node> compileQueryDebugByteArray(
			final String query, final Prefix prefixInstance) throws ParseException {
		final SimpleNode root = StreamSPARQL1_1Parser.parse(query);
		if(root!=null){
			for (int i = 0; i < root.jjtGetNumChildren(); ++i) {
				final Node child = root.jjtGetChild(i); // get current child

				if (child instanceof ASTPrefixDecl) {
					// get prefix...
					final String prefix = ((ASTPrefixDecl) child).getPrefix();

					// get child of PrefixDecl to get the namespace...
					final Node prefixDeclChild = child.jjtGetChild(0);

					// if child of PrefixDecl is QuotedURIRef...
					if (prefixDeclChild instanceof ASTQuotedURIRef) {
						// get namespace...
						final String namespace = ((ASTQuotedURIRef) prefixDeclChild).toQueryString();

						// add namespace and prefix to predefined list
						// of prefix instance...
						prefixInstance.getPredefinedList().put(namespace, prefix);
					}
				}
			}
		}

		SPARQL2CoreSPARQLParserVisitorImplementationDumper spvid;
		try {
			spvid = SPARQL2CoreSPARQLParserVisitorImplementationDumper.createInstance();
			final String corequery = (root==null)?"":(String) spvid.visit(root);

			final SimpleNode rootCoreSPARQL = StreamSPARQL1_1Parser.parse(corequery);

			// checkForTimeFunc(rootCoreSPARQL);

			final StreamOperatorGraphGenerator spvi = StreamOperatorGraphGenerator.createOperatorGraphGenerator(this);
			spvi.visit((ASTQuery)rootCoreSPARQL);
			this.rootNode = spvi.getOperatorgraphRoot();
			this.determinePatternMatchers();
			this.result = spvi.getResult();

			this.rootNode.deleteParents();
			this.rootNode.setParents();
			this.rootNode.detectCycles();
			this.rootNode.sendMessage(new BoundVariablesMessage());
			final DebugContainer<BasicOperatorByteArray> dc = new DebugContainer<BasicOperatorByteArray>(
					"Before a possible correction of the operator graph...",
					"correctoperatorgraphPackageDescription",
					BasicOperatorByteArray.getBasicOperatorByteArray(this.rootNode.deepClone(), prefixInstance));
			final CorrectOperatorgraphRulePackage recog = new CorrectOperatorgraphRulePackage();
			final List<DebugContainer<BasicOperatorByteArray>> correctOperatorGraphRules = recog
			.applyRulesDebugByteArray(this.rootNode, prefixInstance);
			correctOperatorGraphRules.add(0, dc);
			if (this.rdfs != RDFS.NONE) {
				for (final PatternMatcher zpm : this.patternMatchers) {
					final RDFSRoot ic = new RDFSRoot();
					if (this.rdfs == RDFS.RDFS || this.rdfs == RDFS.OPTIMIZEDRDFS) {
						if (this.externalOntology != null) {
							RDFSchemaInference.addInferenceRulesForInstanceData(ic,
									zpm);
						} else {
							RDFSchemaInference.addInferenceRules(ic, zpm);
						}
					} else if (this.rdfs == RDFS.RUDIMENTARYRDFS
							|| this.rdfs == RDFS.OPTIMIZEDRUDIMENTARYRDFS) {
						if (this.externalOntology != null) {
							RudimentaryRDFSchemaInference
							.addInferenceRulesForInstanceData(ic, zpm);
						} else {
							RudimentaryRDFSchemaInference
							.addInferenceRules(ic, zpm);
						}
					} else if (this.rdfs == RDFS.ALTERNATIVERDFS
							|| this.rdfs == RDFS.OPTIMIZEDALTERNATIVERDFS) {
						if (this.externalOntology != null) {
							AlternativeRDFSchemaInference
							.addInferenceRulesForInstanceData(ic, zpm);
						} else {
							AlternativeRDFSchemaInference
							.addInferenceRules(ic, zpm);
						}
					}
					ic.addToPatternMatcher(zpm);
					zpm.deleteParents();
					zpm.setParents();
					zpm.detectCycles();
					zpm.sendMessage(new BoundVariablesMessage());
					correctOperatorGraphRules
					.add(new DebugContainer<BasicOperatorByteArray>(
							"After adding RDFS inference rules to the operator graph...",
							"AddRDFSInferenceRules", BasicOperatorByteArray
							.getBasicOperatorByteArray(this.rootNode.deepClone(), prefixInstance)));
					if (this.externalOntology != null) {
						this.precompileExternalOntology(zpm);
						correctOperatorGraphRules
						.add(new DebugContainer<BasicOperatorByteArray>(
								"After the precomilation of RDFS inference rules to the operator graph...",
								"PrecompileRDFSInferenceRules",
								BasicOperatorByteArray
								.getBasicOperatorByteArray(this.rootNode.deepClone(), prefixInstance)));
						zpm.deleteParents();
						zpm.setParents();
						zpm.detectCycles();
						zpm.sendMessage(new BoundVariablesMessage());
						final ExternalOntologyRuleEngine eore = new ExternalOntologyRuleEngine();
						correctOperatorGraphRules
						.addAll(eore.applyRulesDebugByteArray(zpm,
								prefixInstance));
					}
				}
			}
			this.setBindingsVariablesBasedOnOperatorgraph();
			return new DebugContainerQuery<BasicOperatorByteArray, Node>(
					StreamSPARQL1_1Parser.parse(query), corequery, rootCoreSPARQL,
					correctOperatorGraphRules);
		} catch (final InstantiationException e) {
			System.err.println(e);
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<DebugContainer<BasicOperatorByteArray>> logicalOptimizationDebugByteArray(
			final Prefix prefixInstance) {
		this.rootNode.deleteParents();
		this.rootNode.setParents();
		final List<DebugContainer<BasicOperatorByteArray>> debug = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
		debug.add(new DebugContainer<BasicOperatorByteArray>(
				"Before logical optimization...",
				"logicaloptimizationPackageDescription", BasicOperatorByteArray
				.getBasicOperatorByteArray(this.rootNode.deepClone(),
						prefixInstance)));
		final LogicalOptimizationForStreamEngineRulePackage refse = new LogicalOptimizationForStreamEngineRulePackage();
		this.rootNode.detectCycles();
		this.rootNode.sendMessage(new BoundVariablesMessage());
		debug.addAll(refse.applyRulesDebugByteArray(this.rootNode, prefixInstance));
		if (this.rdfs == RDFS.OPTIMIZEDRDFS || this.rdfs == RDFS.OPTIMIZEDRUDIMENTARYRDFS
				|| this.rdfs == RDFS.OPTIMIZEDALTERNATIVERDFS) {
			// should have been done before
			// rootNode.deleteParents();
			// rootNode.setParents();
			// rootNode.detectCycles();
			// rootNode.sendMessage(new BoundVariablesMessage());
			final RDFSRuleEngine0 rdfsRuleEngine0 = new RDFSRuleEngine0(
					this.externalOntology != null);
			debug.addAll(rdfsRuleEngine0.applyRulesDebugByteArray(this.rootNode,
					prefixInstance));
			final RDFSRuleEngine1 rdfsRuleEngine1 = new RDFSRuleEngine1();
			debug.addAll(rdfsRuleEngine1.applyRulesDebugByteArray(this.rootNode,
					prefixInstance));
			final RDFSRuleEngine2 rdfsRuleEngine2 = new RDFSRuleEngine2();
			debug.addAll(rdfsRuleEngine2.applyRulesDebugByteArray(this.rootNode,
					prefixInstance));
		}
		final List<DebugContainer<BasicOperatorByteArray>> ldc = this.parallelOperatorDebugByteArray(
				this.rootNode, prefixInstance);
		if (ldc != null) {
			debug.addAll(ldc);
		}
		return debug;
	}


	@Override
	public long logicalOptimization() {
		final Date a = new Date();
		this.rootNode.deleteParents();
		this.rootNode.setParents();
		final LogicalOptimizationForStreamEngineRulePackage refse = new LogicalOptimizationForStreamEngineRulePackage();
		this.rootNode.detectCycles();
		this.rootNode.sendMessage(new BoundVariablesMessage());
		refse.applyRules(this.rootNode);
		if (this.rdfs == RDFS.OPTIMIZEDRDFS || this.rdfs == RDFS.OPTIMIZEDRUDIMENTARYRDFS
				|| this.rdfs == RDFS.OPTIMIZEDALTERNATIVERDFS) {
			// should have been done before
			// rootNode.deleteParents();
			// rootNode.setParents();
			// rootNode.detectCycles();
			// rootNode.sendMessage(new BoundVariablesMessage());
			final RDFSRuleEngine0 rdfsRuleEngine0 = new RDFSRuleEngine0(
					this.externalOntology != null);
			rdfsRuleEngine0.applyRules(this.rootNode);
			final RDFSRuleEngine1 rdfsRuleEngine1 = new RDFSRuleEngine1();
			rdfsRuleEngine1.applyRules(this.rootNode);
			final RDFSRuleEngine2 rdfsRuleEngine2 = new RDFSRuleEngine2();
			rdfsRuleEngine2.applyRules(this.rootNode);
		}
		this.parallelOperator(this.rootNode);
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public long physicalOptimization() {
		final Date a = new Date();
		if (this.storage == STORAGE.DISK) {
			PhysicalOptimizations.diskbasedReplacements();
		} else if (this.storage == STORAGE.MEMORY) {
			PhysicalOptimizations.memoryReplacements();
		} else if (this.storage == STORAGE.HYBRID) {
			PhysicalOptimizations.hybridReplacements();
		}
		PhysicalOptimizations.streamReplacements();
		if (this.rdfs != RDFS.NONE) {
			PhysicalOptimizations.rdfsReplacements();
		}
		if (this.join != JOIN.DEFAULT) {
			String to = "";
			switch (this.join) {
			case NESTEDLOOP:
				to = "NestedLoopJoin";
				break;
			case HASHMAPINDEX:
				to = "HashMapIndexJoin";
				break;
			case DBBPTREEINDEX:
				to = "DBBPTreeIndexJoin";
				break;
			case SMALLERINHASHMAPLARGERINDBBPTREEINDEX:
				to = "HybridIndexJoin";
				break;
			case MERGE:
				to = "TreeBagMergeJoin";
				break;
			case DBMERGE:
				to = "DBMergeSortedBagMergeJoin";
				break;
			case DBMERGEITERATIONS:
				to = "MergeJoinWithoutSortingSeveralIterations";
				break;
			case HASH:
				to = "HashJoin";
				break;
			case PARALLELNESTEDLOOP:
				to = "parallel.NestedLoopParallelJoin";
				break;
			case PARALLELHASHMAPINDEX:
				to = "parallel.HashMapIndexParallelJoin";
				break;
			case PARALLELDBBPTREEINDEX:
				to = "parallel.DBBPTreeIndexParallelJoin";
				break;
			case PARALLELSMALLERINHASHMAPLARGERINDBBPTREEINDEX:
				to = "parallel.HybridIndexParallelJoin";
				break;
			case PARALLELMERGE:
				to = "parallel.TreeBagMergeParallelJoin";
				break;
			case PARALLELDBMERGE:
				to = "parallel.DBMergeSortedBagMergeParallelJoin";
				break;
			case PARALLELHASH:
				to = "parallel.HashParallelJoin";
				break;
			}
			PhysicalOptimizations
					.addReplacement("multiinput.join.", "Join", to);
		}
		if (this.optional != JOIN.DEFAULT) {
			String to = "";
			switch (this.optional) {
			case NESTEDLOOP:
				to = "NaiveOptional";
				break;
			case HASHMAPINDEX:
				to = "HashMapIndexOptional";
				break;
			case DBBPTREEINDEX:
				to = "DBBPTreeIndexOptional";
				break;
			case SMALLERINHASHMAPLARGERINDBBPTREEINDEX:
				to = "HybridIndexOptional";
				break;
			case MERGE:
				to = "TreeBagOptional";
				break;
			case DBMERGE:
				to = "DBMergeSortedBagOptional";
				break;
			default:
			case HASH:
				to = "HashOptional";
				break;
			case PARALLELNESTEDLOOP:
				to = "parallel.NaiveParallelJoin";
				break;
			case PARALLELHASHMAPINDEX:
				to = "parallel.HashMapIndexParallelOptional";
				break;
			case PARALLELDBBPTREEINDEX:
				to = "parallel.DBBPTreeIndexParallelOptional";
				break;
			case PARALLELSMALLERINHASHMAPLARGERINDBBPTREEINDEX:
				to = "parallel.HybridIndexParallelOptional";
				break;
			case PARALLELMERGE:
				to = "parallel.TreeBagMergeParallelOptional";
				break;
			case PARALLELDBMERGE:
				to = "parallel.DBMergeSortedBagParallelOptional";
				break;
			case PARALLELHASH:
				to = "parallel.HashParallelOptional";
				break;
			}
			PhysicalOptimizations.addReplacement("multiinput.optional.",
					"Optional", to);
		}
		if (this.sort != SORT.DEFAULT) {
			String to = "";
			switch (this.sort) {
			case QUICKSORT:
				to = "QuickSort";
				break;
			case INSERTIONSORT:
				to = "InsertionSort";
				break;
			case DBMERGESORT:
				to = "DBMergeSortedBagSort";
				break;
			case TREEMAP:
				to = "TreeMapSort";
				break;
			case SMALLERINTREEMAPLARGERINDBMERGE:
				to = "HybridSortedBagSort";
				break;
			}
			PhysicalOptimizations.addReplacement("singleinput.sort.", "Sort",
					to);
		}
		if (this.matcher != MATCHER.DEFAULT) {
			String to = "";
			switch (this.matcher) {
			case SIMPLE:
				to = "SimplePatternMatcher";
				break;
			case HASH:
				to = "HashPatternMatcher";
				break;
			case RDFSSIMPLE:
				to = "RDFSSimplePatternMatcher";
			}
			PhysicalOptimizations.addReplacement(
					"tripleoperator.patternmatcher.", "PatternMatcher", to);
		}
		if (this.distinct != DISTINCT.DEFAULT) {
			String to = "";
			switch (this.distinct) {
			case DBSETBLOCKING:
				to = "DBSetBlockingDistinct";
				break;
			case HASHSETBLOCKING:
				to = "HashBlockingDistinct";
				break;
			case LAZYHASHSETBLOCKING:
				to = "HashBlockingDistinct";
				break;
			case HASHSET:
				to = "InMemoryDistinct";
				break;
			case SMALLERINHASHSETLARGERINDBSET:
				to = "HybridBlockingDistinct";
				break;
			case FASTPAGEDHASHSET:
				to = "NonBlockingFastDistinct";
				break;
			}
			PhysicalOptimizations.addReplacement(
					"singleinput.modifiers.distinct.", "Distinct", to);
		}
		if (this.merge_join_optional != MERGE_JOIN_OPTIONAL.SEQUENTIAL) {
			PhysicalOptimizations.addReplacementMergeJoinAndMergeOptional(
					"multiinput.join.", "MergeJoinWithoutSorting",
					"parallel.MergeParallelJoinWithoutSorting");
			PhysicalOptimizations.addReplacementMergeJoinAndMergeOptional(
					"multiinput.optional.", "MergeWithoutSortingOptional",
					"parallel.MergeWithoutSortingParallelOptional");
			switch (this.merge_join_optional) {
			case PARALLEL:
				PhysicalOptimizations.addReplacementMergeJoinAndMergeOptional(
						"multiinput.join.", "MergeJoinSort",
						"parallel.MergeParallelJoinSort");
				PhysicalOptimizations.addReplacementMergeJoinAndMergeOptional(
						"multiinput.optional.", "MergeOptionalSort",
						"parallel.MergeParallelOptionalSort");
			}
		}

		this.rootNode = PhysicalOptimizations.replaceOperators(this.rootNode, this.rootNode);
		this.patternMatchers.clear();
		this.determinePatternMatchers();
		final Set<Variable> maxVariables = new TreeSet<Variable>();
		this.rootNode.visit(new SimpleOperatorGraphVisitor() {
			@Override
			public Object visit(final BasicOperator basicOperator) {
				if (basicOperator.getUnionVariables() != null) {
					maxVariables.addAll(basicOperator.getUnionVariables());
				}
				return null;
			}

		});
		BindingsArray.forceVariables(maxVariables);
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		if (defaultGraphs.size() > 0 && namedGraphs.size() == 0) {
			this.defaultGraphs = defaultGraphs;
		} else {
			throw new Exception(
					"The StreamQueryEvaluator currently supports only one default graph and no named graphs!");
		}
		return 0;
	}

	public void addToDefaultGraphs(final URILiteral in){
		if(this.defaultGraphs == null){
			this.defaultGraphs = new LinkedList<URILiteral>();
		}
		this.defaultGraphs.add(in);
	}

	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		if (defaultGraphs.size() > 0 && namedGraphs.size() == 0) {
			this.defaultGraphs = defaultGraphs;
		} else {
			throw new Exception(
					"The StreamQueryEvaluator currently supports only one default graph and no named graphs!");
		}
		return 0;
	}

	@Override
	public long evaluateQuery() throws Exception {
		final CountResult cr = new CountResult();
		this.result.addApplication(cr);
		final Date a = new Date();
		this.rootNode.sendMessage(new StartOfEvaluationMessage());
		for(final URILiteral in: this.defaultGraphs) {
			CommonCoreQueryEvaluator.readTriples(this.type, in.openStream(), (TripleConsumer) this.rootNode);
		}
		this.rootNode.sendMessage(new EndOfEvaluationMessage());
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public void prepareForQueryDebugSteps(final DebugStep debugstep) {
		super.prepareForQueryDebugSteps(debugstep);
		if (this.rootNode instanceof PatternMatcher) {
			final PatternMatcher new_pm = PatternMatcher.createDebugInstance(
					(PatternMatcher) this.rootNode, debugstep);
			this.rootNode.replaceWith(new_pm);
			this.rootNode = new_pm;
		} else {
			for (final PatternMatcher zpm : this.patternMatchers) {
				zpm.replaceWith(PatternMatcher.createDebugInstance(zpm,
						debugstep));
			}
			final Stream new_stream = Stream.createDebugInstance(
					(Stream) this.rootNode, debugstep);
			this.rootNode.replaceWith(new_stream);
			this.rootNode = new_stream;
		}
	}

	@Override
	public long evaluateQueryDebugSteps(final DebugStep debugstep, final Application application)
	throws Exception {
		this.result.addApplication(application);
		final Date a = new Date();
		this.rootNode.sendMessageDebug(new StartOfEvaluationMessage(), debugstep);
		for(final URILiteral in: this.defaultGraphs) {
			CommonCoreQueryEvaluator.readTriples(this.type, in.openStream(), (TripleConsumer) this.rootNode);
		}
		this.rootNode.sendMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return ((new Date()).getTime() - a.getTime());
	}

	public static void main(final String[] args) {
		_main(args, StreamQueryEvaluator.class);
	}

	protected void precompileExternalOntology(final PatternMatcher zpm) {
		if (this.externalOntology != null) {
			boolean change = true;
			while (change) {
				change = false;
				for (final TriplePattern tp : zpm.getTriplePatterns()) {

					final ISONTOLOGYTRIPLEPATTERN isOntologyTriplePattern = isOntologyTriplePattern(
							tp.getItems(), this.rdfs);
					if (isOntologyTriplePattern != ISONTOLOGYTRIPLEPATTERN.NO) {
						// yeah, is an rdfs triple pattern!
						final Iterator<Triple> it = this.externalOntology
								.evaluateTriplePattern(tp);

						// first generate the Add(...)-operators for
						// the evaluated triple patterns and add them
						// under an UNION operator
						final Union union = new Union();
						final TriggerOneTime trigger = new TriggerOneTime();

						while (it.hasNext()) {
							final Triple t = it.next();
							BasicOperator lastOperator = union;
							for (int i = 0; i < 3; i++) {
								if (tp.getPos(i).isVariable()) {
									final AddBinding add = new AddBinding(
											(Variable) tp.getPos(i), t
													.getPos(i));
									add
											.addSucceedingOperator(new OperatorIDTuple(
													lastOperator, 0));
									// Not completely correct, but for
									// the succeeding optimization steps
									// enough!
									add
											.setUnionVariables((HashSet<Variable>) tp
													.getVariables().clone());
									add.setIntersectionVariables(add
											.getUnionVariables());
									lastOperator.addPrecedingOperator(add);
									lastOperator = add;
								}
							}
							trigger.addSucceedingOperator(new OperatorIDTuple(
									lastOperator, 0));
						}

						if (trigger.getSucceedingOperators().size() > 0) {
							trigger.setPrecedingOperators(tp
									.getPrecedingOperators());
							for (final BasicOperator po : tp
									.getPrecedingOperators()) {
								po.addSucceedingOperator(new OperatorIDTuple(
										trigger, 0));
							}
							BasicOperator lo = union;
							if (union.getPrecedingOperators().size() == 1
									&& isOntologyTriplePattern == ISONTOLOGYTRIPLEPATTERN.YES) {
								lo = union.getPrecedingOperators().get(0);
							}
							lo.setSucceedingOperators(tp
									.getSucceedingOperators());
							for (final OperatorIDTuple so : tp
									.getSucceedingOperators()) {
								so.getOperator().addPrecedingOperator(lo);
							}
						}

						if (tp.getPrecedingOperators().size() != 1) {
							System.err
									.println("Something is wrong! Triple pattern has not exactly one preceding operator!");
						}

						for (final OperatorIDTuple so : tp
								.getSucceedingOperators()) {
							so.getOperator().removePrecedingOperator(tp);
						}

						// if (isOntologyTriplePattern ==
						// ISONTOLOGYTRIPLEPATTERN.YES) {
						// then delete the old triple pattern
						final BasicOperator po = tp.getPrecedingOperators()
								.get(0);
						po.removeSucceedingOperator(tp);
						// } else {
						// // otherwise do not delete old triple pattern and
						// // make the union operator its succeeding operator!
						// tp.setSucceedingOperator(new OperatorIDTuple(union,
						// union.getPrecedingOperators().size()));
						// union.addPrecedingOperator(tp);
						// }
					}
				}
			}
		}
	}

	public enum ISONTOLOGYTRIPLEPATTERN {
		YES, NO, GENERALTRIPLEPATTERN
	};

	public static ISONTOLOGYTRIPLEPATTERN isOntologyTriplePattern(
			final Item[] items, final RDFS rdfs) {
		if (rdfs == RDFS.NONE) {
			return ISONTOLOGYTRIPLEPATTERN.NO;
		}
		if (items[1].isVariable()) {
			// TODO: make again to GENERALTRIPLEPATTERN and inprve logical
			// optimization!
			// return ISONTOLOGYTRIPLEPATTERN.GENERALTRIPLEPATTERN;
			return ISONTOLOGYTRIPLEPATTERN.NO;
		}
		final String predicate = items[1].toString();
		if (predicate
				.compareTo("<http://www.w3.org/2000/01/rdf-schema#subPropertyOf>") == 0
				|| predicate
						.compareTo("<http://www.w3.org/2000/01/rdf-schema#subClassOf>") == 0) {
			return ISONTOLOGYTRIPLEPATTERN.YES;
		}
		if (rdfs == RDFS.RUDIMENTARYRDFS
				|| rdfs == RDFS.OPTIMIZEDRUDIMENTARYRDFS) {
			return ISONTOLOGYTRIPLEPATTERN.NO;
		}
		if (predicate
				.compareTo("<http://www.w3.org/2000/01/rdf-schema#domain>") == 0
				|| predicate
						.compareTo("<http://www.w3.org/2000/01/rdf-schema#range>") == 0) {
			return ISONTOLOGYTRIPLEPATTERN.YES;
		}
		if (rdfs == RDFS.ALTERNATIVERDFS
				|| rdfs == RDFS.OPTIMIZEDALTERNATIVERDFS) {
			return ISONTOLOGYTRIPLEPATTERN.NO;
		}
		if (predicate
				.compareTo("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>") != 0) {
			return ISONTOLOGYTRIPLEPATTERN.NO;
		}
		if (items[2].isVariable()) {
			// TODO: make again to GENERALTRIPLEPATTERN and inprve logical
			// optimization!
			// return ISONTOLOGYTRIPLEPATTERN.GENERALTRIPLEPATTERN;
			return ISONTOLOGYTRIPLEPATTERN.NO;
		}
		final String object = items[2].toString();
		if (object
				.compareTo("<http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>") == 0
				|| object
						.compareTo("<http://www.w3.org/2000/01/rdf-schema#Class>") == 0
				|| object
						.compareTo("<http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty>") == 0
				|| object
						.compareTo("<http://www.w3.org/2000/01/rdf-schema#Datatype>") == 0) {
			return ISONTOLOGYTRIPLEPATTERN.YES;
		}
		return ISONTOLOGYTRIPLEPATTERN.NO;
	}

	@Override
	public void setRootNode(final BasicOperator rootNode) {
		super.setRootNode(rootNode);
		this.determinePatternMatchers();
	}

	@Override
	public IndexScanCreatorInterface createIndexScanCreator() {
		return new IndexScanCreator_Stream();
	}
}