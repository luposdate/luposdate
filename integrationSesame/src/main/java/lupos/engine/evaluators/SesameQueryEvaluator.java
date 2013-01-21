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

import java.util.Collection;
import java.util.Date;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsCollection;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.AnonymousLiteral;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.datastructures.items.literal.TypedLiteralOriginalContent;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.BooleanResult;
import lupos.datastructures.queryresult.GraphResult;
import lupos.datastructures.queryresult.QueryResult;
import lupos.misc.Tuple;
import lupos.rdf.parser.SesameturtleParser;
import lupos.sparql1_1.Node;

import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class SesameQueryEvaluator extends QueryEvaluator<Node> {
	private Repository repo;
	private RepositoryConnection con;
	private org.openrdf.query.Query q;
	private Object result;
	static final String baseURI = "http://example.org/example/local";

	public enum ONTOLOGY {
		NONE, RDFS
	};

	public enum FORMAT {
		N3, Trig, Trix, Turtle, NTriples
	}

	private ONTOLOGY ontology;
	private FORMAT type;

	public SesameQueryEvaluator() throws Exception {
	}

	public SesameQueryEvaluator(final String[] arguments) throws Exception {
		super(arguments);
	}
	
	public SesameQueryEvaluator(DEBUG debug, boolean multiplequeries, compareEvaluator compare, String compareoptions, int times, String dataset, ONTOLOGY ontology, FORMAT type) throws Exception{
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
	public long compileQuery(final String query) throws Exception {
		final Date a = new Date();
		q = con.prepareQuery(QueryLanguage.SPARQL, query);
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public long evaluateQuery() throws Exception {
		final Date a = new Date();
		if (q instanceof TupleQuery) {
			result = ((TupleQuery) q).evaluate();
		} else if (q instanceof BooleanQuery) {
			result = ((BooleanQuery) q).evaluate();
		} else if (q instanceof GraphQuery) {
			result = ((GraphQuery) q).evaluate();
		}
		return ((new Date()).getTime() - a.getTime());
	}

	@Override
	public QueryResult getResult() throws Exception {
		evaluateQuery();
		QueryResult qr = null;
		if (result instanceof Boolean) {
			qr = new BooleanResult();
			if ((Boolean) result)
				qr.add(new BindingsCollection());
		} else if (result instanceof TupleQueryResult) {
			qr = QueryResult.createInstance();
			while (((TupleQueryResult) result).hasNext()) {
				final BindingSet bs = ((TupleQueryResult) result).next();
				final Bindings binding = new BindingsMap();
				for (final Binding b : bs) {
					// Bindings bb = Bindings.createNewInstance();
					final Value v = b.getValue();
					if (v instanceof org.openrdf.model.Literal) {
						final org.openrdf.model.Literal lit = (org.openrdf.model.Literal) v;
						if (lit.getDatatype() != null) {
							binding.add(new Variable(b.getName()),
									TypedLiteralOriginalContent
											.createTypedLiteral("\""
													+ lit.getLabel() + "\"",
													"<" + lit.getDatatype()
															+ ">"));
						} else {
							binding.add(new Variable(b.getName()),
									LiteralFactory.createLiteral("\""
											+ lit.getLabel() + "\""));
						}
					} else if (v instanceof BNode) {
						binding.add(new Variable(b.getName()),
								new AnonymousLiteral(((BNode) v).toString()));
					} else if (v instanceof URI) {
						binding.add(new Variable(b.getName()), LiteralFactory
								.createURILiteral("<" + (v) + ">"));
					}
				}
				qr.add(binding);
			}
		} else if (result instanceof GraphQueryResultImpl) {
			final GraphQueryResult gqr = (GraphQueryResultImpl) result;
			final GraphResult graphResult = new GraphResult();
			while (gqr.hasNext()) {
				graphResult.addGraphResultTriple(SesameturtleParser
						.transformSesameStatementToTriple(gqr.next()));
			}
			return graphResult;
		} else {
			System.out
					.println("Query type currently not supported by SesameQueryEvaluator!");
		}
		return qr;
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
		NotifyingSail sailStack = new MemoryStore();

		if (ontology == ONTOLOGY.RDFS)
			sailStack = new ForwardChainingRDFSInferencer(sailStack);

		repo = new SailRepository(sailStack);
		try {
			repo.initialize();
			con = repo.getConnection();
		} catch (final RepositoryException e) {
			e.printStackTrace();
		}
		RDFFormat format = RDFFormat.RDFXML;
		if (type == FORMAT.N3)
			format = RDFFormat.N3;
		else if (type == FORMAT.NTriples)
			format = RDFFormat.N3;
		else if (type == FORMAT.Turtle)
			format = RDFFormat.TURTLE;
		else if (type == FORMAT.Trig)
			format = RDFFormat.TRIG;
		else if (type == FORMAT.Trix)
			format = RDFFormat.TRIX;

		con.add(defaultGraphs.iterator().next().openStream(), baseURI, format);
		return ((new Date()).getTime() - a.getTime());
	}
	
	@Override
	public long prepareInputDataWithSourcesOfNamedGraphs(
			Collection<URILiteral> defaultGraphs,
			Collection<Tuple<URILiteral, URILiteral>> namedGraphs)
			throws Exception {
		return this.prepareInputData(defaultGraphs, null);
	}

	public static void main(final String[] args) {
		_main(args, SesameQueryEvaluator.class);
	}
}
