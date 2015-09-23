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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsCollection;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Triple;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.TypedLiteralOriginalContent;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.tripleoperator.TripleConsumer;
import lupos.misc.Tuple;
import lupos.rdf.parser.TurtleParser;
import lupos.sparql1_1.Node;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
public class JenaQueryEvaluator extends QueryEvaluator<Node> {

	private Model model;
	private com.hp.hpl.jena.query.Query query;

	public enum ONTOLOGY {
		NONE, RDFS, OWL
	};

	public enum FORMAT {
		N3, RDFXML {
			@Override
			public String getReaderName(){
				return "RDF/XML";
			}
		};
		public String getReaderName(){
			return this.toString();
		}
	}

	private ONTOLOGY ontology;
	private FORMAT type;

	/**
	 * <p>Constructor for JenaQueryEvaluator.</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public JenaQueryEvaluator() throws Exception {
	}

	/**
	 * <p>Constructor for JenaQueryEvaluator.</p>
	 *
	 * @param arguments an array of {@link java.lang.String} objects.
	 * @throws java.lang.Exception if any.
	 */
	public JenaQueryEvaluator(final String[] arguments) throws Exception {
		super(arguments);
	}

	/**
	 * <p>Constructor for JenaQueryEvaluator.</p>
	 *
	 * @param debug a DEBUG object.
	 * @param multiplequeries a boolean.
	 * @param compare a compareEvaluator object.
	 * @param compareoptions a {@link java.lang.String} object.
	 * @param times a int.
	 * @param dataset a {@link java.lang.String} object.
	 * @param ontology a {@link lupos.engine.evaluators.JenaQueryEvaluator.ONTOLOGY} object.
	 * @param type a {@link lupos.engine.evaluators.JenaQueryEvaluator.FORMAT} object.
	 * @throws java.lang.Exception if any.
	 */
	public JenaQueryEvaluator(final DEBUG debug, final boolean multiplequeries, final compareEvaluator compare, final String compareoptions, final int times, final String dataset, final ONTOLOGY ontology, final FORMAT type) throws Exception{
		super.init(debug, multiplequeries, compare, compareoptions, times, dataset);
		this.ontology = ontology;
		this.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public void setupArguments() {
		this.defaultRDFS = ONTOLOGY.NONE;
		super.setupArguments();
	}

	/** {@inheritDoc} */
	@Override
	public void init() throws Exception {
		super.init();
		this.ontology = (ONTOLOGY) this.args.getEnum("rdfs");
		this.type = FORMAT.valueOf(FORMAT.class, this.args.getString("type"));
	}

	/**
	 * <p>Setter for the field <code>ontology</code>.</p>
	 *
	 * @param ontology a {@link lupos.engine.evaluators.JenaQueryEvaluator.ONTOLOGY} object.
	 */
	public void setOntology(final ONTOLOGY ontology){
		this.ontology = ontology;
	}

	/** {@inheritDoc} */
	@Override
	public long compileQuery(final String queryString) throws Exception {
		final Date a = new Date();
		this.query = QueryFactory.create(queryString);
		return ((new Date()).getTime() - a.getTime());
	}

	/** {@inheritDoc} */
	@Override
	public long logicalOptimization() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public long physicalOptimization() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		this.model = ModelFactory.createDefaultModel();
		// // TODO: consider all default graphs and named graphs!
		// System.out.println("used base dir: "+"file:"+System.getProperty("user.dir"));
		// model.read(defaultGraphs.iterator().next().openStream(), "file:"+System.getProperty("user.dir"), type.getReaderName());
		this.model.read(defaultGraphs.iterator().next().openStream(), null, this.type.getReaderName());

		if (this.ontology == ONTOLOGY.RDFS) {
			// TODO: consider all default graphs and named graphs!
			this.model = ModelFactory.createRDFSModel(this.model);
		} else if (this.ontology == ONTOLOGY.OWL) {
			final Reasoner owlReasoner = ReasonerRegistry.getOWLReasoner();
			this.model = ModelFactory.createInfModel(owlReasoner, this.model);
			// if Pellet would be used: But currently problem with newer Jena api!
			// this.model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, this.model);
		}

		return ((new Date()).getTime() - a.getTime());
	}

	/** {@inheritDoc} */
	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			final Collection<URILiteral> defaultGraphs,
			final Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		return this.prepareInputData(defaultGraphs, null);
	}

	/** {@inheritDoc} */
	@Override
	public long evaluateQuery() throws Exception {
		final Date a = new Date();
		final QueryExecution qe = QueryExecutionFactory.create(this.query, this.model);
		if (this.query.isAskType()) {
			qe.execAsk();
		} else if (this.query.isSelectType()) {
			final ResultSet results = qe.execSelect();
			ResultSetFormatter.consume(results);
		}
		qe.close();
		return ((new Date()).getTime() - a.getTime());
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult getResult() throws Exception {
		final QueryExecution qe = QueryExecutionFactory.create(this.query, this.model);
		if (this.query.isAskType()) {
			if (qe.execAsk()) {
				// unempty => true
				final QueryResult qr = new BooleanResult();
				qr.add(new BindingsCollection());
				return qr;
			} else {
				final QueryResult qr = new BooleanResult();
				qe.close();
				return qr;
			}
		} else if (this.query.isSelectType()) {
			final ResultSet results = qe.execSelect();
			final QueryResult list = resultSetToBindingsList(results);
			qe.close();
			return list;
		} else if (this.query.isConstructType()) {
			final Model model = qe.execConstruct();
			qe.close();
			final GraphResult gr = new GraphResult();
			TurtleParser.triplesFromModel(model,
					new TripleConsumer() {
						@Override
						public void consume(final Triple triple) {
							gr.addGraphResultTriple(triple);
						}
					}, TurtleParser.readFileNumber++);
			return gr;
		} else if (this.query.isDescribeType()) {
			final Model model = qe.execDescribe();
			qe.close();
			final GraphResult gr = new GraphResult();
			TurtleParser.triplesFromModel(model,
					new TripleConsumer() {
						@Override
						public void consume(final Triple triple) {
							gr.addGraphResultTriple(triple);
						}
					}, TurtleParser.readFileNumber++);
			return gr;
		} else {
			System.out
					.println("Query type currently not supported by JenaQueryEvaluator!");
			qe.close();
			return null;
		}
	}

	/**
	 * <p>resultSetToBindingsList.</p>
	 *
	 * @param solutions a {@link com.hp.hpl.jena.query.ResultSet} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult resultSetToBindingsList(final ResultSet solutions) {
		// System.out.println(solutions);
		try {
			String varname;
			QuerySolution sol;
			final QueryResult result = QueryResult.createInstance();
			Bindings binding;
			// System.out.println(solutions.hasNext());
			while (solutions.hasNext()) {
				sol = solutions.nextSolution();
				binding = new BindingsMap(); // Bindings.createNewInstance();
				final Iterator<String> it = sol.varNames();
				while (it.hasNext()) {
					varname = it.next();
					if (sol.contains(varname)) {
						if (sol.get(varname).isLiteral()) {
							if (sol.getLiteral(varname).getDatatypeURI() != null) {
								binding
										.add(
												new Variable(varname),
												TypedLiteralOriginalContent
														.createTypedLiteral(
																"\""
																		+ sol
																				.getLiteral(
																						varname)
																				.getLexicalForm()
																		+ "\"",
																"<"
																		+ sol
																				.getLiteral(
																						varname)
																				.getDatatypeURI()
																		+ ">"));
							} else {
								if (sol.getLiteral(varname).getLanguage() != null
										&& sol.getLiteral(varname)
												.getLanguage().length() > 0) {
									binding
											.add(
													new Variable(varname),
													lupos.datastructures.items.literal.LiteralFactory
															.createLanguageTaggedLiteral(
																	"\""
																			+ sol
																					.getLiteral(
																							varname)
																					.getLexicalForm()
																			+ "\"",
																	sol
																			.getLiteral(
																					varname)
																			.getLanguage()));
								} else {
									binding
											.add(
													new Variable(varname),
													lupos.datastructures.items.literal.LiteralFactory
															.createLiteral("\""
																	+ sol
																			.getLiteral(
																					varname)
																			.getLexicalForm()
																	+ "\""));
								}
							}
						} else {
							if (sol.get(varname).isAnon()) {
								binding
										.add(
												new Variable(varname),
												new lupos.datastructures.items.literal.AnonymousLiteral(
														sol
																.getResource(
																		varname)
																.toString()));
							} else {
								binding
										.add(
												new Variable(varname),
												lupos.datastructures.items.literal.LiteralFactory
														.createURILiteral("<"
																+ sol
																		.getResource(varname)
																+ ">"));
							}
						}
					}
				}
				result.add(binding);
			}
			return result;
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		_main(args, JenaQueryEvaluator.class);
	}

}
