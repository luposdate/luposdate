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
import lupos.engine.operators.singleinput.ExpressionEvaluation.Helper;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;

public class FederatedQueryBitVectorJoin extends FederatedQueryWithSucceedingJoin {
	
	private IToStringHelper toStringHelper;
	
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

	public static int substringSize = 8;
	public static HASHFUNCTION hashFunction = HASHFUNCTION.MD5;
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
		MD5, SHA1, SHA256, SHA384, SHA512, Value;
		public void setup(){
			if(this==Value){
				FederatedQueryBitVectorJoin.approachClass = SemiJoinToStringHelper.class;
			} else {
				FederatedQueryBitVectorJoin.approachClass = BitVectorJoinToStringHelper.class;
				FederatedQueryBitVectorJoin.hashFunction = HASHFUNCTION.values()[this.ordinal()];
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
