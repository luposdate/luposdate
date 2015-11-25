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

import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResult.TYPE;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;
public class FederatedQueryJoinAtEndpoint extends FederatedQueryWithoutSucceedingJoin {

	/**
	 * <p>Constructor for FederatedQueryJoinAtEndpoint.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQueryJoinAtEndpoint(final Node federatedQuery) {
		super(federatedQuery);
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult process(final QueryResult queryResult, final int operandID) {
		final ParallelIteratorMultipleQueryResults pimqr = new ParallelIteratorMultipleQueryResults();
		final Iterator<Bindings> it = queryResult.oneTimeIterator();
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
	 * <p>toStringQuery.</p>
	 *
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String toStringQuery(final QueryResult queryResult) {
		queryResult.materialize();
		if(queryResult.isEmpty()){
			return "SELECT * WHERE {}";
		}
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper();
		String query = "SELECT * " + this.federatedQuery.jjtGetChild(1).accept(dumper);
		final HashSet<Variable> vars = new HashSet<Variable>();
		for(final Bindings bindings: queryResult){
			vars.addAll(bindings.getVariableSet());
		}
		query += "\nVALUES ( ";
		for(final Variable var: vars){
			query += var.toString()+" ";
		}
		query+=") {";
		for(final Bindings bindings: queryResult){
			query+="\n(";
			boolean firstTime=true;
			for(final Variable var: vars){
				if(firstTime){
					firstTime = false;
				} else {
					query += " ";
				}
				final Literal literal = bindings.get(var);
				if(literal==null){
					query += "UNDEF";
				} else {
					query += literal.toString();
				}
			}
			query+=")";
		}

		return query+" }";
	}
}
