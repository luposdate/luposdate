package lupos.sparql1_1.operatorgraph;

import lupos.engine.operators.singleinput.federated.FederatedQuery;
import lupos.engine.operators.singleinput.federated.FederatedQueryBitVectorJoin;
import lupos.sparql1_1.ASTService;

public class ServiceGeneratorBitVectorJoin extends ServiceGeneratorToJoinWithOriginal {

	@Override
	protected FederatedQuery getFederatedQuery(ASTService node) {
		return new FederatedQueryBitVectorJoin(node);
	}

}
