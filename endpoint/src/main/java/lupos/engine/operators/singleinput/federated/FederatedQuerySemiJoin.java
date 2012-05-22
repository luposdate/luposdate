package lupos.engine.operators.singleinput.federated;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.queryresult.QueryResult;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;

public class FederatedQuerySemiJoin extends FederatedQueryWithSucceedingJoin {

	public FederatedQuerySemiJoin(Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public String toStringQuery(final QueryResult queryResult) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper() ;
		String result = "SELECT * " + this.federatedQuery.jjtGetChild(1).accept(dumper);
		Iterator<Bindings> bindingsIterator = queryResult.oneTimeIterator();
		if(bindingsIterator.hasNext()){
			result = result.substring(0, result.length()-2);
			result += "Filter(";
			boolean oneOrMoreResults=false;
			while (bindingsIterator.hasNext()) {
				Bindings b = bindingsIterator.next();
				Iterator<Variable> it = this.getVariablesInIntersectionOfServiceCallAndBindings(b).iterator();
				if(it.hasNext()){
					result += "(";
					while (it.hasNext()) {
						oneOrMoreResults=true;
						Variable variable = it.next();
						result+="( ";
						if(!this.surelyBoundVariablesInServiceCall.contains(variable)){
							result += "(bound("+variable+") && ";							
						}
						result +=variable+"="+variable.getLiteral(b);
						if(!this.surelyBoundVariablesInServiceCall.contains(variable)){
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
