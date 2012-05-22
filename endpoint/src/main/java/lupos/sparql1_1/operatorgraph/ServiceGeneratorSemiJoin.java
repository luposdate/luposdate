package lupos.sparql1_1.operatorgraph;

import lupos.engine.operators.singleinput.federated.FederatedQuery;
import lupos.engine.operators.singleinput.federated.FederatedQuerySemiJoin;
import lupos.sparql1_1.ASTService;

public class ServiceGeneratorSemiJoin extends ServiceGeneratorToJoinWithOriginal {

	@Override
	protected FederatedQuery getFederatedQuery(ASTService node) {		
		return new FederatedQuerySemiJoin(node);
	}
}
