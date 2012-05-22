package lupos.sparql1_1.operatorgraph;

import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

/**
 * This class is overridden by classes of the module endpoint, which really implement the operatorgraph generation for the service calls...  
 */
public class ServiceGenerator { 	

	@SuppressWarnings("unused")
	public void insertIndependantFederatedQueryOperator(final ASTService node, final OperatorConnection connection, final IndexScanCreatorInterface indexScanCreator){
		throw new UnsupportedOperationException("To enable service calls in SPARQL queries, please use the endpoint module!");
	}
	
	@SuppressWarnings("unused")
	public boolean countsAsJoinPartner(final ASTService node){
		return false;
	}
		
	@SuppressWarnings("unused")
	public void insertFederatedQueryOperator(final ASTService node, final OperatorConnection connection){
		throw new UnsupportedOperationException("To enable service calls in SPARQL queries, please use the endpoint module!");
	}
}
