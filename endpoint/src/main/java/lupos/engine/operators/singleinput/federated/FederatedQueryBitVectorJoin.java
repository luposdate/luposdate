
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LanguageTaggedLiteral;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.items.literal.TypedLiteral;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.ServiceApproaches;
public class FederatedQueryBitVectorJoin extends FederatedQueryWithSucceedingJoin {
	
	private IToStringHelper toStringHelper;
	
	/**
	 * <p>Constructor for FederatedQueryBitVectorJoin.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQueryBitVectorJoin(Node federatedQuery) {
		super(federatedQuery);
		try {
			this.toStringHelper = approachClass.newInstance();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.err.println("Use BitVectorJoinToStringHelper as default!");
			this.toStringHelper = new BitVectorJoinToStringHelper();
		}
	}

	/** Constant <code>substringSize=8</code> */
	public static int substringSize = 8;
	/** Constant <code>hashFunction</code> */
	public static HASHFUNCTION hashFunction = HASHFUNCTION.MD5;
	/** Constant <code>approachClass</code> */
	public static Class<? extends IToStringHelper> approachClass = BitVectorJoinToStringHelper.class;
	
	public static enum HASHFUNCTION {
		MD5, SHA1, SHA256 {
			@Override
			public String getName() {
				return "SHA-256";
			}
		}, SHA384 {
			@Override
			public String getName() {
				return "SHA-384";
			}
		}, SHA512 {
			@Override
			public String getName() {
				return "SHA-512";
			}
		};
		public String getName(){
			return toString();
		}
	}
	
	public static enum APPROACH {
		// the hashfunctions must be declared in the same order as in HASHFUNCTION! 
		MD5, SHA1, SHA256, SHA384, SHA512, Value, NonStandardSPARQL;
		public void setup(){
			if(this==NonStandardSPARQL){
				ServiceApproaches.setNonStandardSPARQLBitVectorJoin(true);
			} else {
				ServiceApproaches.setNonStandardSPARQLBitVectorJoin(false);
				if(this==Value){
					FederatedQueryBitVectorJoin.approachClass = SemiJoinToStringHelper.class;
				} else {
					FederatedQueryBitVectorJoin.approachClass = BitVectorJoinToStringHelper.class;
					FederatedQueryBitVectorJoin.hashFunction = HASHFUNCTION.values()[this.ordinal()];
				}
			}
		}
	}
	
	public static interface IToStringHelper{
		public String functionCallBeginning();
		public String functionCallEnd();
		public String valueConverter(final Literal value);
	}
	
	public static class BitVectorJoinToStringHelper implements IToStringHelper {
		
		@Override
		public String functionCallBeginning() {
			return "substr("+hashFunction.toString()+"(str(";
		}

		@Override
		public String functionCallEnd() {
			return ")),1,"+FederatedQueryBitVectorJoin.substringSize+")";
		}

		@Override
		public String valueConverter(final Literal value){
			Object o = Helper.unlazy(value);
			final String parameter;
			if (o instanceof TypedLiteral) {
				parameter = ((TypedLiteral) o).getOriginalContent().toString();
			} else if (o instanceof LanguageTaggedLiteral) {
				parameter = ((LanguageTaggedLiteral) o).getContentLiteral().toString();
			} else parameter = o.toString();
			return "\""+Helper.applyHashFunction(hashFunction.getName(), Helper.unquote(parameter)).substring(1,FederatedQueryBitVectorJoin.substringSize+1)+"\"";
		}
		
		public int getStartOfSubstring(final int substringSizeOfHashValue){
			final int result = Helper.getLengthOfHashFunction(hashFunction.getName()) - substringSizeOfHashValue + 1; // + 1 for character " in the beginning of the string!
			return (result>0)? result : 0; 
		}
	}
	
	public static class SemiJoinToStringHelper implements IToStringHelper {

		@Override
		public String functionCallBeginning() {
			return "";
		}

		@Override
		public String functionCallEnd() {
			return "";
		}

		@Override
		public String valueConverter(final Literal value) {
			return value.toString();
		}		
	}
	
	/** {@inheritDoc} */
	@Override
	public String toStringQuery(final QueryResult queryResult) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper() ;
		String result = "SELECT * {" + this.federatedQuery.jjtGetChild(1).accept(dumper);
		Iterator<Bindings> bindingsIterator = queryResult.iterator();
		if(bindingsIterator.hasNext()){
			result = result.substring(0, result.length()-2);
			result += "}Filter(";
			// determine all variables in the bindings
			Set<Variable> allVars = new HashSet<Variable>();
			for(Bindings b: queryResult){
				allVars.addAll(b.getVariableSet());
			}
			allVars.retainAll(this.variablesInServiceCall);
			Iterator <Variable> it = allVars.iterator();
			boolean oneOrMoreResults=false;
			if(it.hasNext()){
				result += "(";
				while (it.hasNext()) {
					Variable variable = it.next();
					if(!this.surelyBoundVariablesInServiceCall.contains(variable)){
						result += "((bound("+variable+") && ";
					}
					result +="("+this.toStringHelper.functionCallBeginning()+variable+this.toStringHelper.functionCallEnd();
					result +=" IN (";
					HashSet<String> results = new HashSet<String>();
					while (bindingsIterator.hasNext()) {
						Bindings b = bindingsIterator.next();
						Literal literal = variable.getLiteral(b);
						if(literal!=null){
							oneOrMoreResults=true;
							String hashResult = this.toStringHelper.valueConverter(literal);
							results.add(hashResult);
						}
					}
					Iterator<String > resultsIterator = results.iterator();
					while (resultsIterator.hasNext()) {
						result+=resultsIterator.next();
						if (resultsIterator.hasNext()) {
							result+=",";	
						}
					}
					if(this.surelyBoundVariablesInServiceCall.contains(variable)) {
						result+=")";
					} else {
						result += " )))||";
						result += " !bound("+variable+") ";
					}
					if(it.hasNext())
						result+=" )&&";
					else
						result +=" ))";
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
