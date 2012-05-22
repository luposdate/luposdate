package lupos.engine.operators.singleinput.federated;

import java.util.HashSet;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;

public class FederatedQueryJoinAtEndpoint extends FederatedQueryWithoutSucceedingJoin {

	public FederatedQueryJoinAtEndpoint(Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public QueryResult process(final QueryResult queryResult, final int operandID) {
		return FederatedQueryWithSucceedingJoin.process(queryResult, this.endpoint, this.toStringQuery(queryResult));
	}
	
	public String toStringQuery(final QueryResult queryResult) {
		String query = "SELECT * " + this.federatedQuery.jjtGetChild(1).accept(new SPARQLParserVisitorImplementationDumper());
		if(queryResult.isEmpty()){
			return query;
		}
		HashSet<Variable> vars = new HashSet<Variable>();
		for(Bindings bindings: queryResult){
			vars.addAll(bindings.getVariableSet());
		}
		query += "\nBINDINGS ";
		for(Variable var: vars){
			query += var.toString()+" ";
		}
		query+="{";
		for(Bindings bindings: queryResult){
			query+="\n(";
			boolean firstTime=true;
			for(Variable var: vars){
				if(firstTime){
					firstTime = false;
				} else {
					query += " ";
				}
				Literal literal = bindings.get(var);
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
