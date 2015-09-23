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
package lupos.engine.operators.singleinput.federated;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.bindings.BindingsMap;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.Client;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.federated.FederatedQueryFetchAsNeeded.FetchAsNeededDumper;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
public class FederatedQueryVectoredFetchAsNeeded extends FederatedQueryWithoutSucceedingJoin {

	/**
	 * <p>Constructor for FederatedQueryFetchAsNeeded.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQueryVectoredFetchAsNeeded(final Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public QueryResult process(final QueryResult queryresult, final int operandID) {
		if(queryresult.isEmpty()){
			return null;
		}

		if (!this.endpoint.isVariable()) {
			try {
				return this.getQueryResultFromEndpoint((URILiteral)FederatedQueryVectoredFetchAsNeeded.this.endpoint, queryresult);
			} catch (final IOException e) {
				System.err.println(e);
				e.printStackTrace();
				return null;
			}
		} else {

			// first collect the requests to one sparql endpoint
			final HashMap<URILiteral, QueryResult> requestToOneSPARQLEndpoint = new HashMap<URILiteral, QueryResult>();
			final Iterator<Bindings> it = queryresult.iterator();
			while(it.hasNext()){
				final Bindings bindings = it.next();
				Literal endpointURILit = bindings.get((Variable) FederatedQueryVectoredFetchAsNeeded.this.endpoint);
				if (endpointURILit instanceof LazyLiteral) {
					endpointURILit = ((LazyLiteral) endpointURILit).getLiteral();
				}
				if (endpointURILit instanceof URILiteral) {
					final URILiteral endpointURI = (URILiteral) endpointURILit;
					QueryResult qr = requestToOneSPARQLEndpoint.get(endpointURI);
					if(qr==null){
						qr = QueryResult.createInstance();
					}
					qr.add(bindings);
					requestToOneSPARQLEndpoint.put(endpointURI, qr);
				} else {
					// ignore or error message?
				}
			}
			// for each sparql endpoint submit query...
			final ParallelIteratorMultipleQueryResults pimqr = new ParallelIteratorMultipleQueryResults();
			for(final Entry<URILiteral, QueryResult> entry: requestToOneSPARQLEndpoint.entrySet()){
				try {
					pimqr.addQueryResult(this.getQueryResultFromEndpoint(entry.getKey(), entry.getValue()));
				} catch (final IOException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
			return QueryResult.createInstance(pimqr);
		}
	}

	public QueryResult getQueryResultFromEndpoint(final URILiteral endpointURI, final QueryResult queryresult) throws IOException{
		final QueryResult result = QueryResult.createInstance();

		final Class<? extends Bindings> iBindingsInstanceClass = BindingsMap.class;
		Bindings.instanceClass = BindingsMap.class;
		final BindingsFactory iBindingsFactory = BindingsFactory.createBindingsFactory();

		final QueryResult resultFromEndpoint = Client.submitQuery(endpointURI.getString(), this.toStringQuery(queryresult), iBindingsFactory);
		// prepare retrieved result to quickly access the result concerning the index
		final HashMap<Integer, QueryResult> mapBindingsToIndex = new HashMap<Integer, QueryResult>();
		final Iterator<Bindings> resultFromEndpoint_it = resultFromEndpoint.iterator();

		// Because iterator() and not oneTimeItreator() is used, all bindings should already be created...
		Bindings.instanceClass = iBindingsInstanceClass;

		while(resultFromEndpoint_it.hasNext()){
			final Bindings bindingsFromEndpoint = resultFromEndpoint_it.next();
			final Set<Variable> vs = bindingsFromEndpoint.getVariableSet();
			if(vs.size()>0){
				final String vname = vs.iterator().next().getName();
				final Integer index = Integer.parseInt(vname.substring(vname.lastIndexOf('_')+1));
				QueryResult qr = mapBindingsToIndex.get(index);
				if(qr==null){
					qr = QueryResult.createInstance();
				}
				qr.add(this.removeIndex(bindingsFromEndpoint, index));
				mapBindingsToIndex.put(index, qr);
			}
		}
		// prepare to join
		final Iterator<Bindings> it = queryresult.iterator();
		int index = 0;
		while(it.hasNext()){
			final Bindings bindings = it.next();
			final QueryResult qrToJoin = this.getQueryResultToJoin(mapBindingsToIndex, index);
			FederatedQueryVectoredFetchAsNeeded.joinBindingsWithQueryResult(result, bindings, qrToJoin);
			index++;
		}
		return result;
	}

	public QueryResult getQueryResultToJoin(final HashMap<Integer, QueryResult> mapBindingsToIndex, final int index){ // to be overwritten by variant with using cache...
		return mapBindingsToIndex.get(index);
	}

	public Bindings removeIndex(final Bindings bindings, final int index){
		final int fromRight = ("_" + index).length();
		final Bindings result = this.bindingsFactory.createInstance();
		for(final Variable v: bindings.getVariableSet()){
			final String vname = v.getName();
			result.add(new Variable(vname.substring(0, vname.length()-fromRight)), bindings.get(v));
		}
		return result;
	}

	public static void joinBindingsWithQueryResult(final QueryResult result, final Bindings bindings1, final QueryResult qr){
		if(qr != null){
			final Iterator<Bindings> it = qr.iterator();
			while(it.hasNext()){
				final Bindings bindings2 = it.next();
				Join.joinBindings(result, bindings1.clone(), bindings2);
			}
		}
	}

	/**
	 * <p>toStringQuery.</p>
	 *
	 * @param queryresult contains the query result with which the result needs to be joined.
	 * @return a {@link java.lang.String} object.
	 */
	public String toStringQuery(final QueryResult queryresult) {
		final StringBuilder query = new StringBuilder();
		final Iterator<Bindings> it = queryresult.iterator();
		int index = 0;
		while(it.hasNext()){
			final Bindings bindings = it.next();
			if(this.bindingsToBeConsidered(bindings, index)){
				final SPARQLParserVisitorImplementationDumper dumper = new VectoredFetchAsNeededDumper(bindings, index);
				if(query.length()>0){
					query.append("\nUNION\n");
				}
				query.append(this.federatedQuery.jjtGetChild(1).accept(dumper));
			}
			index++;
		}
		query.insert(0, "SELECT * WHERE {");
		query.append("}");
		return query.toString();
	}

	public boolean bindingsToBeConsidered(final Bindings bindings, final int index){ // to be overwritten by the variant with using cache...
		return true;
	}

	public static class VectoredFetchAsNeededDumper extends FetchAsNeededDumper {

		protected final int index;

		public VectoredFetchAsNeededDumper(final Bindings bindings, final int index){
			super(bindings);
			this.index = index;
		}

		@Override
		public String visit(final ASTVar node) {
			final Variable var = new Variable(node.getName());
			if ((this.bindings.get(var) != null)) {
				return this.bindings.get(var).toString();
			} else {
				return "?" + node.getName()+"_"+this.index;
			}
		}
	}
}
