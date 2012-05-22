package lupos.sparql1_1.operatorgraph;

import lupos.engine.operators.singleinput.federated.FederatedQueryJoinAtEndpoint;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public class ServiceGeneratorJoinAtEndpoint extends ServiceGenerator {
	
	@Override
	public void insertFederatedQueryOperator(final ASTService node, final OperatorConnection connection){
		connection.connectAndSetAsNewOperatorConnection(new FederatedQueryJoinAtEndpoint(node));
	}
}
