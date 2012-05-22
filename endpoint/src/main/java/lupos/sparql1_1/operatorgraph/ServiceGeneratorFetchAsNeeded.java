package lupos.sparql1_1.operatorgraph;

import lupos.engine.operators.singleinput.federated.FederatedQueryFetchAsNeeded;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public class ServiceGeneratorFetchAsNeeded extends ServiceGenerator {
	
	@Override
	public void insertFederatedQueryOperator(final ASTService node, final OperatorConnection connection){
		connection.connectAndSetAsNewOperatorConnection(new FederatedQueryFetchAsNeeded(node));
	}
}
