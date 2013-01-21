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
package lupos.engine.operators.singleinput.federated;

import java.io.IOException;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LazyLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.URILiteral;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.endpoint.client.Client;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;

public class FederatedQueryFetchAsNeeded extends FederatedQueryWithoutSucceedingJoin {

	public FederatedQueryFetchAsNeeded(Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		return QueryResult.createInstance(new Iterator<Bindings>(){

			private Iterator<Bindings> bindingsIterator = bindings.iterator();
			private Iterator<Bindings> currentIteratorQueryResult = nextIteratorQueryResult();
			
			@Override
			public boolean hasNext() {
				if(this.currentIteratorQueryResult==null){
					return false;
				}
				while(!this.currentIteratorQueryResult.hasNext()){
					this.currentIteratorQueryResult = nextIteratorQueryResult();
					if(this.currentIteratorQueryResult==null){
						return false;
					}
				}
				return true;					
			}

			@Override
			public Bindings next() {
				if(hasNext()){
					return this.currentIteratorQueryResult.next();
				} else {
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			private Iterator<Bindings> nextIteratorQueryResult(){
				try {
					if(!this.bindingsIterator.hasNext()){
						return null;
					}
					Bindings bindingsTemp = this.bindingsIterator.next();
					final String fQuery = toStringQuery(bindingsTemp);
					if (!FederatedQueryFetchAsNeeded.this.endpoint.isVariable()) {
						return new IteratorQueryResultAndOneBindings(Client.submitQuery(((URILiteral)FederatedQueryFetchAsNeeded.this.endpoint).getString(), fQuery), bindingsTemp);
					} else {
						Literal endpointURI = bindingsTemp.get((Variable) FederatedQueryFetchAsNeeded.this.endpoint);
						if (endpointURI instanceof LazyLiteral)
							endpointURI = ((LazyLiteral) endpointURI).getLiteral();
						if (endpointURI instanceof URILiteral) {
							return new IteratorQueryResultAndOneBindings(Client.submitQuery(((URILiteral) endpointURI).getString(), fQuery), bindingsTemp);
						} else {
							// ignore or error message?
						}
					}
				} catch(IOException e){
					System.err.println(e);
					e.printStackTrace();
				}
				// in case of error try next one:
				return nextIteratorQueryResult();
			}
		});	
	}

	public String toStringQuery(final Bindings bindings) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper() {
			@Override
			public String visit(final ASTVar node) {
				Variable var = new Variable(node.getName());
				if ((bindings.get(var) != null)) {
					return bindings.get(var).toString();
				} else
					return "?" + node.getName();
			}
		};

		return "SELECT * " + this.federatedQuery.jjtGetChild(1).accept(dumper);
	}
	
	public static class IteratorQueryResultAndOneBindings implements ParallelIterator<Bindings>{
		
		private final Iterator<Bindings> it;
		private final Bindings bindings;
		
		public IteratorQueryResultAndOneBindings(final QueryResult queryResult, final Bindings bindings){
			this.it = queryResult.oneTimeIterator();
			this.bindings = bindings;
		}

		@Override
		public boolean hasNext() {
			return this.it.hasNext();
		}

		@Override
		public Bindings next() {
			if(!hasNext()){
				return null;
			}
			Bindings result = this.it.next();
			result.addAll(this.bindings);
			return result;
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
		
	}
}
