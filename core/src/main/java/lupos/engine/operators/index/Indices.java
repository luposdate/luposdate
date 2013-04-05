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
package lupos.engine.operators.index;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsArray;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.evaluators.BasicIndexQueryEvaluator;
import lupos.engine.evaluators.CommonCoreQueryEvaluator;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.application.Application;
import lupos.engine.operators.index.Dataset.ONTOLOGY;
import lupos.engine.operators.index.adaptedRDF3X.RDF3XIndexScan;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.rdfs.AlternativeRDFSchemaInference;
import lupos.engine.operators.rdfs.RDFSchemaInference;
import lupos.engine.operators.rdfs.RudimentaryRDFSchemaInference;
import lupos.engine.operators.rdfs.index.RDFSPutIntoIndices;
import lupos.engine.operators.rdfs.index.RDFSPutIntoIndicesCyclicComputation;
import lupos.engine.operators.singleinput.Result;
import lupos.engine.operators.tripleoperator.TripleOperator;
import lupos.engine.operators.tripleoperator.TriplePattern;
import lupos.engine.operators.tripleoperator.patternmatcher.PatternMatcher;
import lupos.io.LuposObjectInputStream;
import lupos.io.LuposObjectOutputStream;
import lupos.optimizations.logical.rules.externalontology.ExternalOntologyRuleEngine;
import lupos.optimizations.logical.rules.rdfs.RDFSRuleEngine0;
import lupos.optimizations.logical.rules.rdfs.RDFSRuleEngine1;
import lupos.optimizations.logical.rules.rdfs.RDFSRuleEngine2;
import lupos.optimizations.physical.PhysicalOptimizations;

/**
 * Instances of this class are used as data structure for storing triples
 * indexed by various criteria.
 * 
 */
public abstract class Indices extends TripleOperator {

	protected static int HEAPHEIGHT = 16;

	/**
	 * The pattern used to indicate which of the various indexing maps is used.<br>
	 * The e.g. subject groups all Triples which share the same subject in a
	 * collection. This same subject is used as key in the map. E.g: SPOMAP
	 * stands for SubjectPredicateObjectMap The order of the patterns is the
	 * result of a numbering schema:
	 * <ul>
	 * <li>1: Subject</li>
	 * <li>2: Predicate</li>
	 * <li>4: Object</li>
	 * </ul>
	 * The SubjectPredicateMap can be found at position 3 (1+2).<br>
	 * The SubjectObjectMap can be found at position 3 (1+2) and so on.
	 * 
	 */
	public enum MAP_PATTERN {
		/**
		 * No special groping necessary
		 */
		NONE,
		/**
		 * The triples are grouped by their subject.
		 */
		SMAP,
		/**
		 * The triples are grouped by their predicate.
		 */
		PMAP,
		/**
		 * The triples are grouped by their subject (primarily) and their
		 * predicate (secondarily).
		 */
		SPMAP,
		/**
		 * The triples are grouped by their object.
		 */
		OMAP,
		/**
		 * The triples are grouped by their subject (primarily) and their object
		 * (secondarily).
		 */
		SOMAP,
		/**
		 * The triples are grouped by their predicate (primarily) and their
		 * object (secondarily).
		 */
		POMAP,
		/**
		 * The triples are grouped by their subject (primarily), their predicate
		 * (secondarily) and their object (tertiarily).
		 */
		SPOMAP
	}

	protected URILiteral rdfName = null;

	public URILiteral getRdfName() {
		return this.rdfName;
	}

	public void setRdfName(final URILiteral rdfName) {
		this.rdfName = rdfName;
	}

	/**
	 * Returns whether a triple was successfully put into the index structure
	 * 
	 * @param t
	 *            the triple
	 */
	public abstract void add(Triple t);

	@Override
	public void consume(final Triple e) {
		add(e);
	}

	/**
	 * Returns whether a triple was successfully removed from the index
	 * structure.
	 * 
	 * @param t
	 *            the triple to be removed
	 */
	public abstract void remove(Triple t);

	/**
	 * Returns whether the index structure contains a certain triple
	 * 
	 * @param t
	 *            the triple to be found
	 * @return <code>true</code> if the index structure contains a certain
	 *         triple
	 */
	public abstract boolean contains(Triple t);

	/**
	 * An enumeration containing the available data structures for the indexing
	 * maps
	 */
	public enum DATA_STRUCT {
		DBBPTREE, HASHMAP, BPTREE, DEFAULT
	}

	/**
	 * HASHMAP is defined as the default data structure of the indexing maps
	 */
	public static DATA_STRUCT usedDatastructure = DATA_STRUCT.HASHMAP;

	/**
	 * Sets the data structure used for the index structure
	 * 
	 * @param ds
	 *            the data structure used for the index structure
	 */
	public static void setUsedDatastructure(final DATA_STRUCT ds) {
		Indices.usedDatastructure = ds;
	}

	/**
	 * Returns the data structure used for the index structure
	 * 
	 * @return the data structure used for the index structure
	 */
	public static DATA_STRUCT getUsedDatastructure() {
		return Indices.usedDatastructure;
	}

	public void clear() {
		init(usedDatastructure);
	}

	public abstract void init(DATA_STRUCT ds);

	public void build() {
		// may be overridden...
	}
	
	public void loadData(
			final URILiteral graphURI, final String dataFormat,
			final ONTOLOGY materialize,
			final Dataset.IndicesFactory indicesFactory, final int opt,
			final Dataset dataset, final boolean debug,
			final boolean inMemoryExternalOntologyComputation) throws Exception {
		this.loadDataWithoutConsideringOntoloy(graphURI, dataFormat, dataset);
		if (materialize != ONTOLOGY.NONE) {
			final Map<Variable, Integer> vars = BindingsArray.getPosVariables();
			final Root ic = indicesFactory.createRoot();
			final HashSet<Triple> newTriples = new HashSet<Triple>();
			final TripleOperator rpiim = inMemoryExternalOntologyComputation ? new RDFSPutIntoIndicesCyclicComputation(
					newTriples, this)
					: new RDFSPutIntoIndices(this);
			RDFSchemaInference.evaluateAxiomaticAndRDFSValidTriples(this);
			switch (materialize) {
			case RDFS:
				RDFSchemaInference.addInferenceRules(ic, rpiim, graphURI);
				break;
			case RUDIMENTARYRDFS:
				RudimentaryRDFSchemaInference.addInferenceRules(ic, rpiim,
						graphURI);
				break;
			case ALTERNATIVERDFS:
				AlternativeRDFSchemaInference.addInferenceRules(ic, rpiim,
						graphURI);
				break;
			case ONTOLOGYRDFS:
				RDFSchemaInference.addInferenceRulesForExternalOntology(ic,
						rpiim, graphURI);
				break;
			case ONTOLOGYALTERNATIVERDFS:
				AlternativeRDFSchemaInference
						.addInferenceRulesForExternalOntology(ic, rpiim,
								graphURI);
				break;
			case ONTOLOGYRUDIMENTARYRDFS:
				RudimentaryRDFSchemaInference
						.addInferenceRulesForExternalOntology(ic, rpiim,
								graphURI);
				break;
			}
			ic.deleteParents();
			ic.setParents();
			ic.detectCycles();
			ic.sendMessage(new BoundVariablesMessage());
			final Set<Variable> maxVariables = new TreeSet<Variable>();
			for (final OperatorIDTuple oit : ic.getSucceedingOperators()) {
				final BasicOperator op = oit.getOperator();
				for (final Variable v : op.getUnionVariables())
					maxVariables.add(v);
			}
			if (inMemoryExternalOntologyComputation) {
				BindingsArray.forceVariables(maxVariables);
				ic.optimizeJoinOrder(opt);
				PhysicalOptimizations.memoryReplacements();
				ic.physicalOptimization();
									do {
						for (final Triple t : newTriples) {
							if (debug)
								System.out
										.println(">>>>>>>>>>>>>> Inferred Triple in memory:"
												+ t);
							this.add(t);
						}
						this.build();
						((RDFSPutIntoIndicesCyclicComputation) rpiim)
								.newTripleProcessing();
						ic.sendMessage(new StartOfEvaluationMessage());
						ic.startProcessing();
						ic.sendMessage(new EndOfEvaluationMessage());
					} while (((RDFSPutIntoIndicesCyclicComputation) rpiim)
							.getNewTriples());
					this.build();
			} else {
				maxVariables.add(new Variable("s"));
				maxVariables.add(new Variable("p"));
				maxVariables.add(new Variable("o"));
				BindingsArray.forceVariables(maxVariables);
				int size = 0;
				for (final OperatorIDTuple oit : ic.getSucceedingOperators()) {
					final BasicOperator op = oit.getOperator();
					size += ((BasicIndexScan) op).triplePatterns.size();
				}
				// first transform into StreamQueryEvaluator graph!
				final TripleOperator[] to = new TripleOperator[size];
				int i = 0;
				for (final OperatorIDTuple oit : ic.getSucceedingOperators()) {
					final BasicOperator op = oit.getOperator();
					if (((BasicIndexScan) op).triplePatterns.size() == 1) {
						to[i] = ((BasicIndexScan) op).triplePatterns.iterator()
								.next();
						to[i].setSucceedingOperators(op
								.getSucceedingOperators());
						for (final OperatorIDTuple oit2 : op
								.getSucceedingOperators()) {
							oit2.getOperator().removePrecedingOperator(op);
							oit2.getOperator().addPrecedingOperator(to[i]);
						}
						i++;
					} else {
						// insert join!
						final Join join = new Join();
						join
								.setSucceedingOperators(op
										.getSucceedingOperators());
						for (final OperatorIDTuple oit2 : op
								.getSucceedingOperators()) {
							oit2.getOperator().removePrecedingOperator(op);
							oit2.getOperator().addPrecedingOperator(join);
						}
						int j = 0;
						for (final TriplePattern tp : ((BasicIndexScan) op).triplePatterns) {
							to[i] = tp;
							tp.setSucceedingOperator(new OperatorIDTuple(join,
									j));
							join.addPrecedingOperator(tp);
							i++;
							j++;
						}
					}
				}
				final PatternMatcher pm = new PatternMatcher(to);
				for (final TripleOperator top : to) {
					top.setPrecedingOperator(pm);
				}
				// connect all generate operators to the pattern matcher and
				// disconnect to RDFSPutIntoIndices!
				for (final BasicOperator bo : rpiim.getPrecedingOperators()) {
					bo.setSucceedingOperator(new OperatorIDTuple(pm, 0));
					pm.addPrecedingOperator(bo);
				}
				// create new TriplePattern ?s ?p ?o. for getting the results!
				final Result result = new Result();
				result.addApplication(new Application() {

					public void call(final QueryResult res) {
						final Iterator<Bindings> itb = res.oneTimeIterator();
						while (itb.hasNext()) {
							final Bindings b = itb.next();
							final Triple t = new Triple(b
									.get(new Variable("s")), b
									.get(new Variable("p")), b
									.get(new Variable("o")));
							if (debug)
								System.out
										.println(">>>>>>>>>>>>>> Inferred Triple using disk-based approach:"
												+ t);
							rpiim.consume(t);
						}
					}

					public void start(final Type type) {
					}

					public void stop() {
					}

					public void deleteResult(final QueryResult res) {
					}

					public void deleteResult() {
					}

				});
				final TriplePattern tp = new TriplePattern(new Variable("s"),
						new Variable("p"), new Variable("o"));
				tp.addSucceedingOperator(new OperatorIDTuple(result, 0));
				result.addPrecedingOperator(tp);
				tp.addPrecedingOperator(pm);
				pm.add(tp);
					final ExternalOntologyRuleEngine eore = new ExternalOntologyRuleEngine();
					eore.applyRules(pm);
					final RDFSRuleEngine0 rdfsRuleEngine0 = new RDFSRuleEngine0(
							false);
					rdfsRuleEngine0.applyRules(pm);
					final RDFSRuleEngine1 rdfsRuleEngine1 = new RDFSRuleEngine1();
					rdfsRuleEngine1.applyRules(pm);
					final RDFSRuleEngine2 rdfsRuleEngine2 = new RDFSRuleEngine2();
					rdfsRuleEngine2.applyRules(pm);
					// remove tp=?s ?p ?o. from the pattern matcher such that
					// not all data is retrieved and only new inferred triples
					tp.removePrecedingOperator(pm);
					pm.removeSucceedingOperator(tp);
					ic
							.setSucceedingOperators(new LinkedList<OperatorIDTuple>());
					BasicIndexQueryEvaluator
							.transformStreamToIndexOperatorGraph(pm, ic);
					for (final OperatorIDTuple oit : ic
							.getSucceedingOperators()) {
						if (oit.getOperator() instanceof RDF3XIndexScan)
							((RDF3XIndexScan) oit.getOperator())
									.setCollationOrder(new LinkedList<Variable>());
					}
					PhysicalOptimizations.addReplacement("multiinput.join.",
							"Join", "MergeJoinWithoutSortingSeveralIterations");
					ic.physicalOptimization();
				this.build();
					ic.startProcessing();
					ic.sendMessage(new EndOfEvaluationMessage());
					this.build();
			}
			if (vars != null)
				BindingsArray.forceVariables(vars.keySet());
			return;
		}
		this.build();
	}


	protected void loadDataWithoutConsideringOntoloy(final URILiteral graphURI,
			final String dataFormat, final Dataset dataset) throws Exception {
		dataset.waitForCodeMapConstruction();
		CommonCoreQueryEvaluator.readTriples(dataFormat, graphURI.openStream(),
				this);
	}

	public abstract void constructCompletely();
	
	public abstract void writeOutAllModifiedPages() throws IOException;

	public void writeIndexInfo(final LuposObjectOutputStream out)
			throws IOException {
		System.err
				.println("Writing index info is not supported by this type of index!");
	}

	public void readIndexInfo(final LuposObjectInputStream in)
			throws IOException, ClassNotFoundException {
		System.err
				.println("Reading index info is not supported by this type of index!");
	}
}
