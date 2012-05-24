/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.misc.BitVector;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;

public class FederatedQueryBitVectorJoinNonStandardSPARQL extends FederatedQueryWithSucceedingJoin {
	
	
	public FederatedQueryBitVectorJoinNonStandardSPARQL(Node federatedQuery) {
		super(federatedQuery);
	}

	public static int bitvectorSize = 8;
	
	@Override
	public String toStringQuery(final QueryResult queryResult) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper() ;
		String result = "SELECT * " + this.federatedQuery.jjtGetChild(1).accept(dumper);
		Iterator<Bindings> bindingsIterator = queryResult.iterator();
		if(bindingsIterator.hasNext()){
			result = result.substring(0, result.length()-2);
			result += "Filter(";
			// determine all variables in the bindings
			Set<Variable> allVars = new HashSet<Variable>();
			for(Bindings b: queryResult){
				allVars.addAll(b.getVariableSet());
			}
			allVars.retainAll(this.variablesInServiceCall);
			Iterator <Variable> it = allVars.iterator();
			boolean oneOrMoreResults=false;
			if(it.hasNext()){
				while (it.hasNext()) {
					result += "(";
					Variable variable = it.next();
					if(!this.surelyBoundVariablesInServiceCall.contains(variable)){
						result += "((bound("+variable+") && ";
					}
					result +="<http://www.ifis.uni-luebeck.de/functions/BitVectorFilter>("+variable;
					result +=",";
					BitVector bv = new BitVector(FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize);
					while (bindingsIterator.hasNext()) {
						Bindings b = bindingsIterator.next();
						Literal literal = variable.getLiteral(b);
						if(literal!=null){
							oneOrMoreResults=true;
							bv.set(Math.abs(literal.toString().hashCode() % FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize));
						}
					}
					result += bv.getBigInteger().toString()+","+FederatedQueryBitVectorJoinNonStandardSPARQL.bitvectorSize+")";
					if(!this.surelyBoundVariablesInServiceCall.contains(variable)) {
						result += " ))||";
						result += " !bound("+variable+") ";
					}
					if(it.hasNext())
						result+=" )&&";
					else
						result +=" )";
					bindingsIterator = queryResult.iterator();
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
