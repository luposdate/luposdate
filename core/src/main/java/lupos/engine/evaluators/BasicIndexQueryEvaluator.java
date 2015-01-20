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
package lupos.engine.evaluators;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.bindings.BindingsArrayPresortingNumbers;
import lupos.datastructures.bindings.BindingsArrayReadTriples;
import lupos.datastructures.bindings.BindingsArrayVarMinMax;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.dbmergesortedds.heap.Heap;
import lupos.datastructures.dbmergesortedds.tosort.ToSort;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.items.literal.string.StringURILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.index.BasicIndexScan;
import lupos.engine.operators.index.Dataset;
import lupos.engine.operators.index.Dataset.ONTOLOGY;
import lupos.engine.operators.index.Indices;
import lupos.engine.operators.index.Root;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.singleinput.sort.fastsort.FastSort;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.misc.Tuple;
import lupos.misc.debug.BasicOperatorByteArray;
import lupos.misc.debug.DebugStep;
import lupos.optimizations.logical.rules.DebugContainer;
import lupos.optimizations.logical.rules.generated.AfterPhysicalOptimizationRulePackage;
import lupos.optimizations.logical.rules.generated.CorrectOperatorgraphRulePackage;
import lupos.optimizations.logical.rules.generated.LogicalOptimizationRulePackage;
import lupos.optimizations.physical.PhysicalOptimizations;
import lupos.optimizations.sparql2core_sparql.SPARQL2CoreSPARQLParserVisitorImplementationDumper;
import lupos.rdf.Prefix;
import lupos.sparql1_1.ASTPrefixDecl;
import lupos.sparql1_1.ASTQuery;
import lupos.sparql1_1.ASTQuotedURIRef;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.SPARQL1_1Parser;
import lupos.sparql1_1.SimpleNode;
import lupos.sparql1_1.operatorgraph.IndexOperatorGraphGenerator;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreator_BasicIndex;

public abstract class BasicIndexQueryEvaluator extends CommonCoreQueryEvaluator<Node> {

	protected Root root;
	protected int opt;
	protected Dataset dataset;
	protected lupos.engine.operators.index.Indices.DATA_STRUCT datastructure;

	public BasicIndexQueryEvaluator() throws Exception {
		super();
	}

	public BasicIndexQueryEvaluator(final String[] args) throws Exception {
		super(args);
	}

	public BasicIndexQueryEvaluator(final DEBUG debug, final boolean multiplequeries, final compareEvaluator compare, final String compareoptions, final int times, final String dataset,
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
			final lupos.engine.operators.index.Indices.DATA_STRUCT datastructure,
			final Dataset.SORT datasetsort){
		super(debug, multiplequeries, compare, compareoptions, times, dataset,
				type, externalontology,inmemoryexternalontologyinference, rdfs, codemap, tmpDirs, loadindexinfo,
				parallelOperands,blockwise,
				limit,jointhreads,joinbuffer,
				heap, tosort, indexheap, mergeheapheight, mergeheaptype, chunk, mergethreads, yagomax,
				resulttype, storage, join, optional, sort, distinct,
				merge_join_optional, encoding);
		this.init(datastructure, datasetsort);
	}

	private void init(
			final lupos.engine.operators.index.Indices.DATA_STRUCT datastructure,
			final Dataset.SORT sort) {
		this.datastructure = datastructure;
		Indices.setUsedDatastructure(datastructure);
		Dataset.setSortingApproach(sort);
	}

	@Override
	public void setupArguments() {
		this.defaultRDFS = RDFS.NONE;
		super.setupArguments();
		this.args
		.addEnumOption(
				"datastructure",
				"Decides whether to use a Hashmap, B+-Tree in memory, or on harddrive as the internal data structure",
				lupos.engine.operators.index.Indices.DATA_STRUCT.DEFAULT);
		this.args
		.addEnumOption(
				"sortduringindexconstruction",
				"specifies whether distributed sorting should be used, normal sorting or a string search tree!",
				Dataset.SORT.NORMAL);
	}

	@Override
	public void init() throws Exception {
		super.init();
		this.init((lupos.engine.operators.index.Indices.DATA_STRUCT) this.args
				.getEnum("datastructure"), (Dataset.SORT) this.args
				.getEnum("sortduringindexconstruction"));
	}

	public void writeOutAllModifiedPagesInRDFDataIndices(final String dir) throws IOException {
		this.dataset.writeOutAllModifiedPages();
		if(dir!=null){
			if(this instanceof RDF3XQueryEvaluator){
				((RDF3XQueryEvaluator)this).writeOutIndexFile(dir);
			}
		}
	}

	public void buildCompletelyAllIndices() {
		this.dataset.buildCompletelyAllIndices();
	}

	protected ONTOLOGY getMaterializeOntology() {
		switch (this.rdfs) {
		case RDFS:
			return ONTOLOGY.RDFS;
		case RUDIMENTARYRDFS:
			return ONTOLOGY.RUDIMENTARYRDFS;
		case ALTERNATIVERDFS:
			return ONTOLOGY.ALTERNATIVERDFS;
		default:
			return ONTOLOGY.NONE;
		}
	}

	protected boolean isNonOptimizedRDFS() {
		switch (this.rdfs) {
		case RDFS:
		case RUDIMENTARYRDFS:
		case ALTERNATIVERDFS:
			return true;
		default:
			return false;
		}
	}

	private String getRDFSStringForStreamEngine(final RDFS rdfs) {
		switch (rdfs) {
		case OPTIMIZEDRDFS:
			return "RDFS";
		case OPTIMIZEDRUDIMENTARYRDFS:
			return "RUDIMENTARYRDFS";
		case OPTIMIZEDALTERNATIVERDFS:
			return "ALTERNATIVERDFS";
		}
		return "Error";
	}

	private static BasicIndexScan createBasicIndex(final Root ic,
			final List<OperatorIDTuple> oids, final TriplePattern tp) {
		final List<TriplePattern> tps = new LinkedList<TriplePattern>();
		tps.add(tp);
		final BasicIndexScan bi = ic.newIndexScan(null, tps, null);
		bi.setSucceedingOperators(oids);
		bi.setIntersectionVariables(tp.getIntersectionVariables());
		bi.setUnionVariables(tp.getUnionVariables());
		return bi;
	}

	private String[] getArguments() {
		if (this.externalontology.compareTo("") == 0) {
			return new String[] {
					"--rdfs",
					this.rdfs.toString(),
					"--debug",
					this.debug.toString(),
					this.inmemoryexternalontologyinference ? "--inmemoryexternalontologyinference"
							: "--no-inmemoryexternalontologyinference" };
		} else {
			return new String[] {
					"--rdfs",
					this.rdfs.toString(),
					"--externalontology",
					this.externalontology,
					"--debug",
					this.debug.toString(),
					this.inmemoryexternalontologyinference ? "--inmemoryexternalontologyinference"
							: "--no-inmemoryexternalontologyinference" };
		}
	}

	private void rdfsStreamQueryToIndexQuery(final String query,
			final Root ic) throws Exception {
		final StreamQueryEvaluator sqe = new StreamQueryEvaluator(
				this.getArguments());

		sqe.compileQuery(query);
		sqe.logicalOptimization();

		this.transformStreamToIndexOperatorGraph(sqe, ic);
	}

	private DebugContainerQuery<BasicOperatorByteArray, Node> rdfsStreamQueryToIndexQueryDebugByteArray(
			final String query, final Root ic,
			final Prefix prefixInstance)
			throws Exception {
		final StreamQueryEvaluator sqe = new StreamQueryEvaluator(
				this.getArguments());

		final DebugContainerQuery<BasicOperatorByteArray, Node> dcq = sqe
		.compileQueryDebugByteArray(query, prefixInstance);
		dcq.getCorrectOperatorGraphRules().addAll(
				sqe.logicalOptimizationDebugByteArray(prefixInstance));

		this.transformStreamToIndexOperatorGraph(sqe, ic);

		dcq
		.getCorrectOperatorGraphRules()
		.add(
				new DebugContainer<BasicOperatorByteArray>(
						"After transforming into operatorgraph using index-approaches...",
						"transformingIntoIndex", BasicOperatorByteArray
						.getBasicOperatorByteArray(ic
								.deepClone(), prefixInstance)));

		return dcq;
	}


	private void transformStreamToIndexOperatorGraph(
			final StreamQueryEvaluator sqe, final Root ic) {
		Bindings.instanceClass = BindingsArrayReadTriples.class;
		this.result = sqe.getResultOperator();
		transformStreamToIndexOperatorGraph((PatternMatcher) sqe.getRootNode(), ic);
	}

	public static Root transformStreamToIndexOperatorGraph(
			final PatternMatcher pm, final Root ic) {
		final Set<TriplePattern> visited = new HashSet<TriplePattern>();
		for (final TriplePattern tp : pm.getTriplePatterns()) {
			final List<OperatorIDTuple> succ = tp.getSucceedingOperators();
			if (!visited.contains(tp)) {
				BasicIndexScan idx;
				if (succ.size() == 1) {
					if (succ.size() == 1
							&& succ.get(0).getOperator() instanceof Join) {
						final Join tj = findTopJoin((Join) succ.get(0)
								.getOperator());
						if (tj != null) {
							// final List<Join> bottomJoins = new
							// LinkedList<Join>();
							// final boolean complete = findBottomJoins(tj,
							// null,
							// bottomJoins);

							// final List<TriplePattern> tps =
							// collectPredTPs(tj,
							// bottomJoins, visited);

							final List<TriplePattern> tps = new LinkedList<TriplePattern>();
							final boolean complete = collectPredTPs(tj, tps);

							visited.addAll(tps);

							idx = ic.newIndexScan(null, tps, null);

							final HashSet<Variable> hsv = new HashSet<Variable>();
							for (final TriplePattern stp : tps) {
								hsv.addAll(stp.getUnionVariables());
							}

							idx.setIntersectionVariables(hsv);
							idx.setUnionVariables(hsv);

							if (complete) {
								idx.setSucceedingOperators(tj
										.getSucceedingOperators());
							} else {
								for (final BasicOperator prec : tj
										.getPrecedingOperators()) {
									if (collectPredTPs(prec,
											new LinkedList<TriplePattern>())) {
										final int operandID = prec
										.getOperatorIDTuple(tj).getId();
										idx
										.setSucceedingOperator(new OperatorIDTuple(
												tj, operandID));
									}
								}
								// delete joins which otherwise would have one
								// operand
								for (final TriplePattern tp2 : tps) {
									for (final OperatorIDTuple oidtuple : tp2
											.getSucceedingOperators()) {
										final BasicOperator bo = oidtuple
										.getOperator();
										if (bo instanceof Join) {
											bo.removePrecedingOperator(tp2);
											if (!bo.equals(tj)) {
												bo.removeFromOperatorGraph();
											}
										}
									}
								}
							}
						} else {
							idx = createBasicIndex(ic, succ, tp);
						}
					} else {
						idx = createBasicIndex(ic, succ, tp);
					}
				} else {
					idx = createBasicIndex(ic, succ, tp);
					// throw new IllegalArgumentException(
					// "TP had more than one succeeding operator.");
				}
				visited.add(tp);
				ic.getSucceedingOperators().add(new OperatorIDTuple(idx, 0));
			} else {

			}
		}
		ic.deleteParents();
		ic.setParents();
		ic.detectCycles();
		return ic;
		// final ReadTriplesDistinct rtd = new ReadTriplesDistinct();
		// for (final BasicOperator bo : q.getPrecedingOperators()) {
		// rtd.addPrecedingOperator(bo);
		// bo.addSucceedingOperator(new OperatorIDTuple(rtd, 0));
		// }
		// for (final BasicOperator bo : q.getPrecedingOperators()) {
		// bo.removeSucceedingOperator(q);
		// }
		// rtd.addSucceedingOperator(new OperatorIDTuple(q, 0));
		// q.setPrecedingOperator(rtd);
		// should have been done before!
		// ic.sendMessage(new BoundVariablesMessage());
	}

	private static Join findTopJoin(final Join j) {
		// Join lastJoin = null;
		Join curJoin = j;
		while (true) {
			final List<OperatorIDTuple> succ = curJoin.getSucceedingOperators();
			// for (final BasicOperator op : curJoin.getPrecedingOperators()) {
			// if (!(op instanceof Join || op instanceof TriplePattern))
			// return lastJoin;
			// }
			if (succ.size() != 1
					|| !(succ.get(0).getOperator() instanceof Join)) {
				return curJoin;
			}
			// lastJoin = curJoin;
			curJoin = (Join) succ.get(0).getOperator();
		}
	}

	// private static boolean findBottomJoins(final Join j, final Join lastJoin,
	// final List<Join> result) {
	// boolean complete = true;
	// boolean allTriplePatterns = true;
	// for (final BasicOperator op : j.getPrecedingOperators()) {
	// if (!(op instanceof Join || op instanceof TriplePattern)) {
	// allTriplePatterns = false;
	// if (lastJoin != null)
	// result.add(lastJoin);
	// complete = false;
	// } else if (!(op instanceof TriplePattern))
	// allTriplePatterns = false;
	// }
	// boolean joinIsThere = false;
	// for (final BasicOperator op : j.getPrecedingOperators()) {
	// if (op instanceof Join) {
	// joinIsThere = true;
	// final List<Join> result2 = new LinkedList<Join>();
	// final boolean zComplete = findBottomJoins((Join) op, j, result2);
	// if (zComplete)
	// result.addAll(result2);
	// complete = complete && zComplete;
	// }
	// }
	// if (!joinIsThere) {
	// result.add(j);
	// }
	// return allTriplePatterns ? true : complete;
	// }

	private static boolean collectPredTPs(final BasicOperator bo,
			final List<TriplePattern> result) {
		if (bo instanceof TriplePattern) {
			result.add((TriplePattern) bo);
			return true;
		}
		if (bo instanceof Join) {
			boolean complete = true;
			for (final BasicOperator prec : bo.getPrecedingOperators()) {
				final List<TriplePattern> zresult = new LinkedList<TriplePattern>();
				final boolean zComplete = collectPredTPs(prec, zresult);
				// if (zComplete)
				result.addAll(zresult);
				complete = complete && zComplete;
			}
			return complete;
		} else {
			return false;
		}
	}

	// private static List<TriplePattern> collectPredTPs(final Join tj,
	// final List<Join> bottomJoins, final Set<TriplePattern> visited) {
	// final List<TriplePattern> res = new LinkedList<TriplePattern>();
	// final LinkedList<Join> next = new LinkedList<Join>();
	// for (final Join join : bottomJoins) {
	// if (!tj.equals(join))
	// next.add(join);
	// }
	// Join current = tj;
	// while (current != null) {
	// for (final BasicOperator op : current.getPrecedingOperators()) {
	// if (op instanceof Join) {
	// if (!bottomJoins.contains(current))
	// next.add((Join) op);
	// } else if (op instanceof TriplePattern) {
	// res.add((TriplePattern) op);
	// visited.add((TriplePattern) op);
	// } else {
	// throw new RuntimeException(
	// "Found an operator that does not belong here: "
	// + op);
	// }
	// }
	// current = next.poll();
	// }
	// return res;
	// }

	public long compileQuery(final String query,
			final Root root_param) throws Exception {
		final Date a = new Date();
		this.root = root_param;
		this.rootNode = root_param;
		if (this.rdfs == RDFS.OPTIMIZEDRDFS || this.rdfs == RDFS.OPTIMIZEDRUDIMENTARYRDFS
				|| this.rdfs == RDFS.OPTIMIZEDALTERNATIVERDFS) {
			this.rdfsStreamQueryToIndexQuery(query, root_param);
		} else {
			SimpleNode root = SPARQL1_1Parser.parse(query);

			final SPARQL2CoreSPARQLParserVisitorImplementationDumper spvid = SPARQL2CoreSPARQLParserVisitorImplementationDumper.createInstance();
			final String corequery = (root==null)?"":(String) spvid.visit(root);

			root = SPARQL1_1Parser.parse(corequery);

			if(root==null){
				this.result = new Result();
				root_param.setSucceedingOperator(new OperatorIDTuple(this.result,0));
			} else {
				final IndexOperatorGraphGenerator spvi = IndexOperatorGraphGenerator.createOperatorGraphGenerator(root_param, this);
				spvi.visit((ASTQuery)root);

				this.result = spvi.getResult();
			}

			root_param.deleteParents();
			root_param.setParents();
			final CorrectOperatorgraphRulePackage recog = new CorrectOperatorgraphRulePackage();
			root_param.detectCycles();
			root_param.sendMessage(new BoundVariablesMessage());
			recog.applyRules(root_param);
		}
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public DebugContainerQuery<BasicOperatorByteArray, Node> compileQueryDebugByteArray(
			final String query, final Prefix prefixInstance) throws Exception {
		return this.compileQueryDebugByteArray(query, this.createRoot(),
				prefixInstance);
	}

	public DebugContainerQuery<BasicOperatorByteArray, Node> compileQueryDebugByteArray(
			final String query, final Root root_param,
			final Prefix prefixInstance)
			throws Exception {

		this.root = root_param;
		this.rootNode = root_param;

		final DebugContainerQuery<BasicOperatorByteArray, Node> dcq;
		if (this.rdfs == RDFS.OPTIMIZEDRDFS || this.rdfs == RDFS.OPTIMIZEDRUDIMENTARYRDFS || this.rdfs == RDFS.OPTIMIZEDALTERNATIVERDFS) {
			dcq = this.rdfsStreamQueryToIndexQueryDebugByteArray(query, root_param, prefixInstance);
		} else {
			final SimpleNode root = SPARQL1_1Parser.parse(query);

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
						final String namespace = ((ASTQuotedURIRef) prefixDeclChild)
						.toQueryString();

						// add namespace and prefix to predefined list
						// of prefix instance...
						prefixInstance.getPredefinedList().put(namespace,
								prefix);
					}
				}
			}

			final SPARQL2CoreSPARQLParserVisitorImplementationDumper spvid = SPARQL2CoreSPARQLParserVisitorImplementationDumper.createInstance();

			final String corequery = (root==null)?"":(String) spvid.visit(root);

			final SimpleNode root_CoreSPARQL = SPARQL1_1Parser.parse(corequery);

			if(root_CoreSPARQL==null){
				this.result = new Result();
				root_param.setSucceedingOperator(new OperatorIDTuple(this.result,0));
			} else {
				final IndexOperatorGraphGenerator spvi = IndexOperatorGraphGenerator.createOperatorGraphGenerator(root_param, this);
				spvi.visit((ASTQuery)root_CoreSPARQL);

				this.result = spvi.getResult();
			}

			root_param.deleteParents();
			root_param.setParents();
			final CorrectOperatorgraphRulePackage recog = new CorrectOperatorgraphRulePackage();
			root_param.detectCycles();
			root_param.sendMessage(new BoundVariablesMessage());

			final List<DebugContainer<BasicOperatorByteArray>> ldc = new LinkedList<DebugContainer<BasicOperatorByteArray>>();

			ldc.add(new DebugContainer<BasicOperatorByteArray>(
					"Before a possible correction of the operator graph...",
					"correctoperatorgraphPackageDescription",
					BasicOperatorByteArray.getBasicOperatorByteArray(root_param.deepClone(), prefixInstance)));
			ldc.addAll(recog.applyRulesDebugByteArray(root_param,
					prefixInstance));
			dcq = new DebugContainerQuery<BasicOperatorByteArray, Node>(
					SPARQL1_1Parser.parse(query), corequery, root_CoreSPARQL,
					ldc);
		}
		return dcq;
	}

	@Override
	public long evaluateQuery() throws Exception {
		if(this.dataset==null){
			this.prepareInputData(new LinkedList<URILiteral>(), new LinkedList<URILiteral>());
		}
		this.buildCompletelyAllIndices();
		// new OperatorGraphNew(root.deepClone(), -1, false)
		// .displayOperatorGraph("lala", null);

		Class<? extends Bindings> instanceClass = null;
		if (Bindings.instanceClass == BindingsArrayVarMinMax.class
				|| Bindings.instanceClass == BindingsArrayPresortingNumbers.class) {
			// is BindingsArrayVarMinMax or BindingsArrayPresortingNumbers
			// necessary? Or is only BindingsArray
			// sufficient?
			final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
				public boolean found = false;

				@Override
				public Object visit(final BasicOperator basicOperator) {
					if (basicOperator instanceof FastSort) {
						this.found = true;
					}
					return null;
				}

				@Override
				public boolean equals(final Object o) {
					if (o instanceof Boolean) {
						return this.found == (Boolean) o;
					} else {
						return super.equals(o);
					}
				}
			};
			this.root.visit(sogv);
			if (sogv.equals(false)) {
				instanceClass = Bindings.instanceClass;
				Bindings.instanceClass = BindingsArray.class;
				this.bindingsFactory = BindingsFactory.createBindingsFactory();
				this.setBindingsVariablesBasedOnOperatorgraph();
			}
		}

		final Date a = new Date();
		this.root.sendMessage(new StartOfEvaluationMessage());
		this.root.startProcessing();
		this.root.sendMessage(new EndOfEvaluationMessage());
		final long time = ((new Date()).getTime() - a.getTime());

//		System.out.println("Number of results:" + cr.getNumberResults());
		if (instanceClass != null) {
			Bindings.instanceClass = instanceClass;
		}
		this.buildCompletelyAllIndices();
		return time;
	}

	@Override
	public long evaluateQueryDebugSteps(final DebugStep debugstep, final Application application)
	throws Exception {
		if(this.dataset==null){
			this.prepareInputData(new LinkedList<URILiteral>(), new LinkedList<URILiteral>());
		}
		this.buildCompletelyAllIndices();
		// new OperatorGraphNew(root.deepClone(), -1, false)
		// .displayOperatorGraph("lala", null);

		this.result.addApplication(application);
		Class<? extends Bindings> instanceClass = null;
		if (Bindings.instanceClass == BindingsArrayVarMinMax.class
				|| Bindings.instanceClass == BindingsArrayPresortingNumbers.class) {
			// is BindingsArrayVarMinMax or BindingsArrayPresortingNumbers
			// necessary? Or is only BindingsArray
			// sufficient?
			final SimpleOperatorGraphVisitor sogv = new SimpleOperatorGraphVisitor() {
				public boolean found = false;

				@Override
				public Object visit(final BasicOperator basicOperator) {
					if (basicOperator instanceof FastSort) {
						this.found = true;
					}
					return null;
				}

				@Override
				public boolean equals(final Object o) {
					if (o instanceof Boolean) {
						return this.found == (Boolean) o;
					} else {
						return super.equals(o);
					}
				}
			};
			this.root.visit(sogv);
			if (sogv.equals(false)) {
				instanceClass = Bindings.instanceClass;
				Bindings.instanceClass = BindingsArray.class;
				this.bindingsFactory = BindingsFactory.createBindingsFactory();
				this.setBindingsVariablesBasedOnOperatorgraph();
			}
		}

		final Date a = new Date();
		this.root.sendMessageDebug(new StartOfEvaluationMessage(),
				debugstep);
		this.root.startProcessingDebug(debugstep);
		this.root.sendMessageDebug(new EndOfEvaluationMessage(),
				debugstep);
		final long time = ((new Date()).getTime() - a.getTime());

		if (instanceClass != null) {
			Bindings.instanceClass = instanceClass;
		}

		this.buildCompletelyAllIndices();
		return time;
	}


	@Override
	public long logicalOptimization() {
		final Date a = new Date();
		this.setBindingsVariablesBasedOnOperatorgraph();
		final LogicalOptimizationRulePackage refie = new LogicalOptimizationRulePackage();
		refie.applyRules(this.root);
		this.rootNode.sendMessage(new BindingsFactoryMessage(this.bindingsFactory));
		this.root.optimizeJoinOrder(this.opt);
		final LogicalOptimizationRulePackage refie2 = new LogicalOptimizationRulePackage();
		refie2.applyRules(this.root);
		this.parallelOperator(this.root);
		this.rootNode.sendMessage(new BindingsFactoryMessage(this.bindingsFactory));
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public List<DebugContainer<BasicOperatorByteArray>> logicalOptimizationDebugByteArray(
			final Prefix prefixInstance) {
		final List<DebugContainer<BasicOperatorByteArray>> result = new LinkedList<DebugContainer<BasicOperatorByteArray>>();
		this.setBindingsVariablesBasedOnOperatorgraph();
		result.add(new DebugContainer<BasicOperatorByteArray>(
				"Before logical optimization...",
				"logicaloptimizationPackageDescription", BasicOperatorByteArray
				.getBasicOperatorByteArray(this.root.deepClone(),
						prefixInstance)));
		final LogicalOptimizationRulePackage refie = new LogicalOptimizationRulePackage();
		result.addAll(refie.applyRulesDebugByteArray(this.root,
				prefixInstance));
		this.rootNode.sendMessage(new BindingsFactoryMessage(this.bindingsFactory));
		this.root.optimizeJoinOrder(this.opt);
		result.add(new DebugContainer<BasicOperatorByteArray>(
				"After optimizing the join order...",
				"optimizingjoinord;erRule", BasicOperatorByteArray
				.getBasicOperatorByteArray(this.root.deepClone(),
						prefixInstance)));
		final LogicalOptimizationRulePackage refie2 = new LogicalOptimizationRulePackage();
		result.addAll(refie2.applyRulesDebugByteArray(this.root,
				prefixInstance));
		final List<DebugContainer<BasicOperatorByteArray>> ldc = this.parallelOperatorDebugByteArray(
				this.root, prefixInstance);
		if (ldc != null) {
			result.addAll(ldc);
		}
		this.rootNode.sendMessage(new BindingsFactoryMessage(this.bindingsFactory));
		return result;
	}


	@Override
	public long physicalOptimization() {
		final Date a = new Date();
		if (this.rdfs != RDFS.NONE) {
			PhysicalOptimizations.rdfsReplacements();
		}
		if (this.storage == STORAGE.DISK) {
			PhysicalOptimizations.diskbasedReplacements();
		} else if (this.storage == STORAGE.MEMORY) {
			PhysicalOptimizations.memoryReplacements();
		} else if (this.storage == STORAGE.HYBRID) {
			PhysicalOptimizations.hybridReplacements();
		}
		if (this.datastructure != Indices.DATA_STRUCT.DEFAULT) {
			Indices.usedDatastructure = this.datastructure;
		}
		if (this.join != JOIN.DEFAULT) {
			String to = "";
			switch (this.join) {
			case NESTEDLOOP:
				to = "NestedLoopJoin";
				break;
			default:
			case HASHMAPINDEX:
				to = "HashMapIndexJoin";
				break;
			case PAGEDMAPINDEX:
				to = "PagedMapIndexJoin";
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
			case PAGEDMAPINDEX:
				to = "PagedMapIndexOptional";
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
			default:
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
			default:
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
			if (this.merge_join_optional == MERGE_JOIN_OPTIONAL.PARALLEL) {
				PhysicalOptimizations.addReplacementMergeJoinAndMergeOptional(
						"multiinput.join.", "MergeJoinSort",
				"parallel.MergeParallelJoinSort");
				PhysicalOptimizations.addReplacementMergeJoinAndMergeOptional(
						"multiinput.optional.", "MergeOptionalSort",
				"parallel.MergeParallelOptionalSort");
			}
		}

		this.root.physicalOptimization();
		this.root.deleteParents();
		this.root.setParents();
		this.root.detectCycles();
		final AfterPhysicalOptimizationRulePackage refie = new AfterPhysicalOptimizationRulePackage();
		refie.applyRules(this.root);
		this.setBindingsVariablesBasedOnOperatorgraph();
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a=new Date();
		// there should be at least one default graphs (especially for update operations...)
		if(defaultGraphs.size()==0) {
			defaultGraphs.add(new StringURILiteral("<inlinedata:>"));
		}
		return (new Date().getTime())-a.getTime();
	}

	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		final Date a=new Date();
		// there should be at least one default graphs (especially for update operations...)
		if(defaultGraphs.size()==0){
			defaultGraphs.add(new StringURILiteral("<inlinedata:>"));
		}
		return (new Date().getTime())-a.getTime();
	}


	public abstract Root createRoot();

	@Override
	public long compileQuery(final String query) throws Exception {
		return this.compileQuery(query, this.createRoot());
	}

	public void setRoot(final Root root_param) {
		this.root = root_param;
		this.rootNode = root_param;
	}

	@Override
	public void setRootNode(final BasicOperator rootNode) {
		this.setRoot((Root)rootNode);
	}

	public Dataset getDataset() {
		return this.dataset;
	}

	/**
	 * Processes a database dump of the default graph by storing all its content into
	 * one or several files.
	 *
	 * @param filename The basic filename (will be extended with X.n3, where X is a run number)
	 * @param triplesInOneFile The maximum number of triples to be stored in one file (<=0 for all triples in one file)
	 * @throws Exception
	 */
	public void dump(final String filename, final int triplesInOneFile) throws Exception{
		int currentRunNumber=0;
		PrintStream printer = new PrintStream(new
				BufferedOutputStream(
						new FileOutputStream(
								filename+((triplesInOneFile<=0)?"":currentRunNumber)+".n3")));
		final QueryResult queryResult=this.getResult("SELECT * WHERE {?s ?p ?o.}");

		final Variable s=new Variable("s");
		final Variable p=new Variable("p");
		final Variable o=new Variable("o");

		int tripleNumber=0;

		final Iterator<Bindings> it=queryResult.oneTimeIterator();
		Bindings lastBindings=null;
		while(it.hasNext()){
			final Bindings b=it.next();

			if(triplesInOneFile>0 && tripleNumber>=triplesInOneFile){
				printer.print(".");
				printer.close();
				currentRunNumber++;
				printer = new PrintStream(new
						BufferedOutputStream(
								new FileOutputStream(filename+currentRunNumber+".n3")));
				tripleNumber=1;
				lastBindings=null;
			} else {
				tripleNumber++;
			}

			if(lastBindings==null || lastBindings.get(s).compareToNotNecessarilySPARQLSpecificationConform(b.get(s))!=0){
				if(lastBindings!=null) {
					printer.println(".");
				}
				printer.print(b.get(s)+" "+b.get(p)+" "+b.get(o));
			} else if(lastBindings.get(p).compareToNotNecessarilySPARQLSpecificationConform(b.get(p))!=0){
				printer.println(";");
				printer.print("  "+b.get(p)+" "+b.get(o));
			} else if(lastBindings.get(o).compareToNotNecessarilySPARQLSpecificationConform(b.get(o))!=0){
				printer.println(",");
				printer.print("    "+b.get(o));
			} else {
				System.err.println("Duplicate triple in index. Should never occur! Triple: "+b);
			}
			lastBindings=b;
		}
		if(lastBindings!=null) {
			printer.print(".");
		}
		printer.close();
	}

	@Override
	public IndexScanCreatorInterface createIndexScanCreator() {
		return new IndexScanCreator_BasicIndex(this.createRoot());
	}
}
