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
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.Client;
import lupos.misc.util.ImmutableIterator;
import lupos.sparql1_1.Node;

public class FederatedQueryFetchAsNeededWithCache extends FederatedQueryFetchAsNeeded {

	public HashMap<Bindings, QueryResult> cache = new HashMap<Bindings, QueryResult>();

	public FederatedQueryFetchAsNeededWithCache(final Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		return QueryResult.createInstance(new ImmutableIterator<Bindings>(){

			private final Iterator<Bindings> bindingsIterator = bindings.iterator();
			private Iterator<Bindings> currentIteratorQueryResult = this.nextIteratorQueryResult();

			@Override
			public boolean hasNext() {
				if(this.currentIteratorQueryResult==null){
					return false;
				}
				while(!this.currentIteratorQueryResult.hasNext()){
					this.currentIteratorQueryResult = this.nextIteratorQueryResult();
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

			private Iterator<Bindings> nextIteratorQueryResult(){
				try {
					if(!this.bindingsIterator.hasNext()){
						return null;
					}
					final Bindings bindingsTemp = this.bindingsIterator.next();

					final Bindings bindingsKey = bindingsTemp.clone();
					final Set<Variable> otherVars = bindingsKey.getVariableSet();
					otherVars.removeAll(FederatedQueryFetchAsNeededWithCache.this.variablesInServiceCall);
					otherVars.remove(FederatedQueryFetchAsNeededWithCache.this.endpoint);
					for(final Variable var: otherVars){
						bindingsKey.add(var, null);
					}

					final QueryResult cached = FederatedQueryFetchAsNeededWithCache.this.cache.get(bindingsKey);
					if(cached!=null){
						return cached.iterator();
					}

					final String fQuery = FederatedQueryFetchAsNeededWithCache.this.toStringQuery(bindingsTemp);
					if (!FederatedQueryFetchAsNeededWithCache.this.endpoint.isVariable()) {
						final QueryResult queryResult = QueryResult.createInstance(new IteratorQueryResultAndOneBindings(Client.submitQuery(((URILiteral)FederatedQueryFetchAsNeededWithCache.this.endpoint).getString(), fQuery, FederatedQueryFetchAsNeededWithCache.this.bindingsFactory), bindingsTemp));
						queryResult.materialize();
						FederatedQueryFetchAsNeededWithCache.this.cache.put(bindingsKey, queryResult);
						return queryResult.iterator();
					} else {
						Literal endpointURI = bindingsTemp.get((Variable) FederatedQueryFetchAsNeededWithCache.this.endpoint);
						if (endpointURI instanceof LazyLiteral) {
							endpointURI = ((LazyLiteral) endpointURI).getLiteral();
						}
						if (endpointURI instanceof URILiteral) {
							final QueryResult queryResult = QueryResult.createInstance(new IteratorQueryResultAndOneBindings(Client.submitQuery(((URILiteral) endpointURI).getString(), fQuery, FederatedQueryFetchAsNeededWithCache.this.bindingsFactory), bindingsTemp));
							queryResult.materialize();
							FederatedQueryFetchAsNeededWithCache.this.cache.put(bindingsKey, queryResult);
							return queryResult.iterator();
						} else {
							// ignore or error message?
						}
					}
				} catch(final IOException e){
					System.err.println(e);
					e.printStackTrace();
				}
				// in case of error try next one:
				return this.nextIteratorQueryResult();
			}
		});
	}


}
