
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.engine.operators.singleinput.federated;

import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;
public class FederatedQuerySemiJoin extends FederatedQueryWithSucceedingJoin {

	/**
	 * <p>Constructor for FederatedQuerySemiJoin.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQuerySemiJoin(Node federatedQuery) {
		super(federatedQuery);
	}

	/** {@inheritDoc} */
	@Override
	public String toStringQuery(final QueryResult queryResult) {
		return FederatedQuerySemiJoin.toStringQuery(this.surelyBoundVariablesInServiceCall, this.variablesInServiceCall, this.federatedQuery, queryResult);
	}

	/**
	 * <p>toStringQuery.</p>
	 *
	 * @param surelyBoundVariablesInServiceCall a {@link java.util.Set} object.
	 * @param variablesInServiceCall a {@link java.util.Set} object.
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 * @param queryResult a {@link lupos.datastructures.queryresult.QueryResult} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String toStringQuery(final Set<Variable> surelyBoundVariablesInServiceCall, final Set<Variable> variablesInServiceCall, final Node federatedQuery, final QueryResult queryResult) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper() ;
		String result = "SELECT * {" + federatedQuery.jjtGetChild(1).accept(dumper);
		Iterator<Bindings> bindingsIterator = queryResult.oneTimeIterator();
		if(bindingsIterator.hasNext()){
			result = result.substring(0, result.length()-2);
			result += "}Filter(";
			boolean oneOrMoreResults=false;
			while (bindingsIterator.hasNext()) {
				Bindings b = bindingsIterator.next();
				Iterator<Variable> it = FederatedQuery.getVariablesInIntersectionOfSetOfVariablesAndBindings(variablesInServiceCall, b).iterator();
				if(it.hasNext()){
					result += "(";
					while (it.hasNext()) {
						oneOrMoreResults=true;
						Variable variable = it.next();
						result+="( ";
						if(!surelyBoundVariablesInServiceCall.contains(variable)){
							result += "(bound("+variable+") && ";							
						}
						result +=variable+"="+variable.getLiteral(b);
						if(!surelyBoundVariablesInServiceCall.contains(variable)){
							result += " ) || !bound("+variable+") ";
						}
						if(it.hasNext()){
							result+=" ) && ";
						} else {
							result +=" ))";
						}
					}
					if(bindingsIterator.hasNext())
						result +=" || ";
				} 
			}
			if(!oneOrMoreResults){
				result+=" true ";
			}
			result += "). }";
		}
		return result;
	}
}
