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
		N3, RDFXML
	}

	private ONTOLOGY ontology;
	private FORMAT type;

	public JenaQueryEvaluator() throws Exception {
	}

	public JenaQueryEvaluator(final String[] arguments) throws Exception {
		super(arguments);
	}
	
	public JenaQueryEvaluator(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset, ONTOLOGY ontology, FORMAT type) throws Exception{
		super.init(debug, multiplequeries, compare, compareoptions, times, dataset);
		this.ontology = ontology;
		this.type = type;
	}

	@Override
	public void setupArguments() {
		defaultRDFS = ONTOLOGY.NONE;
		super.setupArguments();
	}

	@Override
	public void init() throws Exception {
		super.init();
		ontology = (ONTOLOGY) args.getEnum("rdfs");
		type = FORMAT.valueOf(FORMAT.class, args.getString("type"));
	}
	
	public void setOntology(final ONTOLOGY ontology){
		this.ontology = ontology;
	}

	@Override
	public long compileQuery(final String queryString) throws Exception {
		final Date a = new Date();
		query = QueryFactory.create(queryString);
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public long logicalOptimization() {
		return 0;
	}

	@Override
	public long physicalOptimization() {
		return 0;
	}

	@Override
	public long prepareInputData(final Collection<URILiteral> defaultGraphs,
			final Collection<URILiteral> namedGraphs) throws Exception {
		final Date a = new Date();
		model = ModelFactory.createDefaultModel();
		// // TODO: consider all default graphs and named graphs!
		// System.out.println("used base dir: "+"file:"+System.getProperty("user.dir"));
		// model.read(defaultGraphs.iterator().next().openStream(), "file:"+System.getProperty("user.dir"), type.toString());
		model.read(defaultGraphs.iterator().next().openStream(), null, type.toString());

		if (ontology == ONTOLOGY.RDFS) {
			// TODO: consider all default graphs and named graphs!
			model = ModelFactory.createRDFSModel(model);
		} else if (ontology == ONTOLOGY.OWL) {
			final Reasoner owlReasoner = ReasonerRegistry.getOWLReasoner();
			model = ModelFactory.createInfModel(owlReasoner, model);
		}

		return ((new Date()).getTime() - a.getTime());
	}
	
	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		return this.prepareInputData(defaultGraphs, null);
	}

	@Override
	public long evaluateQuery() throws Exception {
		final Date a = new Date();
		final QueryExecution qe = QueryExecutionFactory.create(query, model);
		if (query.isAskType()) {
			qe.execAsk();
		} else if (query.isSelectType()) {
			final ResultSet results = qe.execSelect();
			ResultSetFormatter.consume(results);
		}
		qe.close();
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public QueryResult getResult() throws Exception {
		final QueryExecution qe = QueryExecutionFactory.create(query, model);
		if (query.isAskType()) {
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
		} else if (query.isSelectType()) {
			final ResultSet results = qe.execSelect();
			final QueryResult list = resultSetToBindingsList(results);
			qe.close();
			return list;
		} else if (query.isConstructType()) {
			final Model model = qe.execConstruct();
			qe.close();
			final GraphResult gr = new GraphResult();
			TurtleParser.triplesFromModel(model,
					new TripleConsumer() {
						public void consume(final Triple triple) {
							gr.addGraphResultTriple(triple);
						}
					}, TurtleParser.readFileNumber++);
			return gr;
		} else if (query.isDescribeType()) {
			final Model model = qe.execDescribe();
			qe.close();
			final GraphResult gr = new GraphResult();
			TurtleParser.triplesFromModel(model,
					new TripleConsumer() {
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
							if (sol.getLiteral(varname).getDatatypeURI() != null)
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
							else {
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
								} else
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
						} else {
							if (sol.get(varname).isAnon())
								binding
										.add(
												new Variable(varname),
												new lupos.datastructures.items.literal.AnonymousLiteral(
														sol
																.getResource(
																		varname)
																.toString()));
							else 
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
				result.add(binding);
			}
			return result;
		} catch (final Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return null;
		}
	}

	public static void main(final String[] args) {
		_main(args, JenaQueryEvaluator.class);
	}

}