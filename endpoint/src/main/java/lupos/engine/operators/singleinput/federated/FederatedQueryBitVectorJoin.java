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
	public FederatedQueryBitVectorJoin(final Node federatedQuery) {
		super(federatedQuery);
		try {
			this.toStringHelper = approachClass.newInstance();
		} catch (final Exception e) {
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
			return this.toString();
		}
	}

	public static enum APPROACH {
		// the hashfunctions must be declared in the same order as in HASHFUNCTION!
		MD5, SHA1, SHA256, SHA384, SHA512, Value, ValueSubstring, NonStandardSPARQL;
		public void setup(){
			if(this==NonStandardSPARQL){
				ServiceApproaches.setNonStandardSPARQLBitVectorJoin(true);
			} else {
				ServiceApproaches.setNonStandardSPARQLBitVectorJoin(false);
				if(this==Value){
					FederatedQueryBitVectorJoin.approachClass = SemiJoinToStringHelper.class;
				} else if(this==ValueSubstring){
					FederatedQueryBitVectorJoin.approachClass = SubstringValueToStringHelper.class;
				} else {
					FederatedQueryBitVectorJoin.approachClass = BitVectorJoinToStringHelper.class;
					FederatedQueryBitVectorJoin.hashFunction = HASHFUNCTION.values()[this.ordinal()];
				}
			}
		}
	}

	public static interface IToStringHelper{
		public String functionCall(Variable variable);
		public String valueConverter(final Literal value);
	}

	public static class BitVectorJoinToStringHelper implements IToStringHelper {

		@Override
		public String functionCall(final Variable variable) {
			return "substr("+hashFunction.toString()+"(str(" + variable + ")),1,"+FederatedQueryBitVectorJoin.substringSize+")";
		}

		@Override
		public String valueConverter(final Literal value){
			final Object o = Helper.unlazy(value);
			final String parameter;
			if (o instanceof TypedLiteral) {
				parameter = ((TypedLiteral) o).getOriginalContent().toString();
			} else if (o instanceof LanguageTaggedLiteral) {
				parameter = ((LanguageTaggedLiteral) o).getContentLiteral().toString();
			} else {
				parameter = o.toString();
			}
			return "\""+Helper.applyHashFunction(hashFunction.getName(), Helper.unquote(parameter)).substring(1,FederatedQueryBitVectorJoin.substringSize+1)+"\"";
		}
	}

	public static class SemiJoinToStringHelper implements IToStringHelper {

		@Override
		public String functionCall(final Variable variable) {
			return variable.toString();
		}


		@Override
		public String valueConverter(final Literal value) {
			return value.toString();
		}
	}

	public static class SubstringValueToStringHelper implements IToStringHelper {

		@Override
		public String functionCall(final Variable variable) { // begin at the end of the string as there are most likely the most significant characters (e.g. in the case of iris)
			return "substr(str(" + variable + "),strlen(str(" + variable + "))-"+(FederatedQueryBitVectorJoin.substringSize-1)+","+FederatedQueryBitVectorJoin.substringSize+")";
		}

		@Override
		public String valueConverter(final Literal value){
			final Object o = Helper.unlazy(value);
			final String parameter;
			if (o instanceof TypedLiteral) {
				parameter = ((TypedLiteral) o).getOriginalContent().toString();
			} else if (o instanceof LanguageTaggedLiteral) {
				parameter = ((LanguageTaggedLiteral) o).getContentLiteral().toString();
			} else {
				parameter = o.toString();
			}
			final String unquotetString = Helper.unquote(parameter);
			int start = unquotetString.length() - FederatedQueryBitVectorJoin.substringSize;
			if(start<0){
				start = 0;
			}
			return "\""+unquotetString.substring(start, unquotetString.length())+"\"";
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
			final Set<Variable> allVars = new HashSet<Variable>();
			for(final Bindings b: queryResult){
				allVars.addAll(b.getVariableSet());
			}
			allVars.retainAll(this.variablesInServiceCall);
			final Iterator <Variable> it = allVars.iterator();
			boolean oneOrMoreResults=false;
			if(it.hasNext()){
				result += "(";
				while (it.hasNext()) {
					final Variable variable = it.next();
					if(!this.surelyBoundVariablesInServiceCall.contains(variable)){
						result += "((bound("+variable+") && ";
					}
					result +="("+this.toStringHelper.functionCall(variable);
					result +=" IN (";
					final HashSet<String> results = new HashSet<String>();
					while (bindingsIterator.hasNext()) {
						final Bindings b = bindingsIterator.next();
						final Literal literal = variable.getLiteral(b);
						if(literal!=null){
							oneOrMoreResults=true;
							final String hashResult = this.toStringHelper.valueConverter(literal);
							results.add(hashResult);
						}
					}
					final Iterator<String > resultsIterator = results.iterator();
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
					if(it.hasNext()) {
						result+=" )&&";
					} else {
						result +=" ))";
					}
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
