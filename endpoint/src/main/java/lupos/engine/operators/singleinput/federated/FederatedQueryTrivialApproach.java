package lupos.engine.operators.singleinput.federated;

import lupos.datastructures.queryresult.QueryResult;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.sparql1_1.Node;

public class FederatedQueryTrivialApproach extends FederatedQueryWithSucceedingJoin {

	public FederatedQueryTrivialApproach(Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public String toStringQuery(QueryResult bindings) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper();
		return "SELECT * " + this.federatedQuery.jjtGetChild(1).accept(dumper);
	}
}
