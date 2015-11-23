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
import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResult.TYPE;
import lupos.endpoint.client.Client;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.sparql1_1.Node;

/**
 * For all those approaches, which must join the result of the sparql endpoint afterwards with the original queryresult (computed from the local data (and/or other service calls))
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class FederatedQueryWithSucceedingJoin extends FederatedQuery {

	/**
	 * <p>Constructor for FederatedQueryWithSucceedingJoin.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQueryWithSucceedingJoin(final Node federatedQuery) {
		super(federatedQuery);
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		this.intersectionVariables = new HashSet<Variable>();
		this.unionVariables = new HashSet<Variable>();
		FederatedQuery.checkVariables(this.federatedQuery, this.unionVariables);
		this.intersectionVariables.addAll(this.unionVariables);
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().addAll(this.unionVariables);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		final ParallelIteratorMultipleQueryResults pimqr = new ParallelIteratorMultipleQueryResults();
		final Iterator<Bindings> it = bindings.oneTimeIterator();
		while(it.hasNext()){
			final QueryResult qr = QueryResult.createInstance(TYPE.MEMORY);
			int i=0;
			while(i<FederatedQuery.MAX_BINDINGS_IN_ENDPOINT_REQUEST && it.hasNext()){
				qr.add(it.next());
				i++;
			}
			final QueryResult resultOfEndpoint = FederatedQueryWithSucceedingJoin.process(qr, this.endpoint, this.toStringQuery(qr), this.bindingsFactory);
			resultOfEndpoint.materialize(); // otherwise it may be blocking!
			pimqr.addQueryResult(resultOfEndpoint);
		}
		return QueryResult.createInstance(pimqr);
	}

	/**
	 * <p>process.</p>
	 *
	 * @param bindings a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @param endpoint a {@link lupos.datastructures.items.Item} object.
	 * @param fQuery a {@link java.lang.String} object.
	 * @param bindingsFactory a {@link lupos.datastructures.bindings.BindingsFactory} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult process(final QueryResult bindings, final Item endpoint, final String fQuery, final BindingsFactory bindingsFactory){
		if (!endpoint.isVariable()) {
			try {
				return Client.submitQuery(((URILiteral)endpoint).getString(), fQuery, bindingsFactory);
			} catch (final Exception e) {
				System.err.println(e);
				e.printStackTrace();
			}
		} else {
			// service call with variable
			// it could be that several endpoints are asked, which we succeedingly ask for results...
			// the same endpoints are not asked again!
			return QueryResult.createInstance(new ParallelIterator<Bindings>(){

				private final Iterator<Bindings> it = bindings.oneTimeIterator();
				private final HashSet<URILiteral> alreadyAskedEndpoints = new HashSet<URILiteral>();
				private Iterator<Bindings> currentIteratorQueryResult = this.getNextIteratorQueryResult();

				@Override
				public boolean hasNext() {
					if(this.currentIteratorQueryResult==null){
						return false;
					}
					while(!this.currentIteratorQueryResult.hasNext()){
						this.currentIteratorQueryResult = this.getNextIteratorQueryResult();
						if(this.currentIteratorQueryResult==null){
							return false;
						}
					}
					return true;
				}

				@Override
				public Bindings next() {
					if(this.hasNext()){
						return this.currentIteratorQueryResult.next();
					} else {
						return null;
					}
				}

				@Override
				public void remove() {
					this.it.remove();
				}

				@Override
				public void close() {
					if(this.it instanceof ParallelIterator){
						((ParallelIterator<Bindings>)this.it).close();
					}
				}

				private Iterator<Bindings> getNextIteratorQueryResult(){
					final URILiteral endpointURI = this.nextEndpoint();
					if(endpointURI==null){
						return null;
					}
					final String uri = endpointURI.getString();
					try {
						return FederatedQueryWithSucceedingJoin.addEndpointVariable((Variable)endpoint, endpointURI, Client.submitQuery(uri, fQuery, bindingsFactory)).oneTimeIterator();
					} catch (final IOException e) {
						System.err.println(e);
						e.printStackTrace();
					}
					return null;
				}

				private URILiteral nextEndpoint(){
					while(this.it.hasNext()){
						final Bindings service = this.it.next();
						Literal endpointURI = service.get((Variable) endpoint);
						if (endpointURI instanceof LazyLiteral) {
							endpointURI = ((LazyLiteral) endpointURI).getLiteral();
						}
						if (endpointURI instanceof URILiteral) {
							final URILiteral result = (URILiteral) endpointURI;
							if(!this.alreadyAskedEndpoints.contains(result)){ // do not ask endpoints several times!
								this.alreadyAskedEndpoints.add(result);
								return result;
							}
						} else {
							// error message or maybe ignore?
						}
					}
					return null;
				}
			});
		}
		return null;
	}

	/**
	 * <p>toStringQuery.</p>
	 *
	 * @param bindings a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected abstract String toStringQuery(QueryResult bindings);

	/**
	 * <p>addEndpointVariable.</p>
	 *
	 * @param endpointVariable a {@link lupos.datastructures.items.Variable} object.
	 * @param endpointURI a {@link lupos.datastructures.items.literal.Literal} object.
	 * @param resultSetToBindingsList a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a {@link lupos.datastructures.queryresult.QueryResult} object.
	 */
	public static QueryResult addEndpointVariable(final Variable endpointVariable, final Literal endpointURI, final QueryResult resultSetToBindingsList) {
		return QueryResult.createInstance(new ParallelIterator<Bindings>(){

			private final Iterator<Bindings> it = resultSetToBindingsList.oneTimeIterator();

			@Override
			public boolean hasNext() {
				return this.it.hasNext();
			}

			@Override
			public Bindings next() {
				final Bindings bindings = this.it.next();
				if(bindings==null){
					return null;
				}
				bindings.add(endpointVariable, endpointURI);
				return bindings;
			}

			@Override
			public void remove() {
				this.it.remove();
			}

			@Override
			public void close() {
				if(this.it instanceof ParallelIterator){
					((ParallelIterator<Bindings>)this.it).close();
				}
			}
		});
	}
}
