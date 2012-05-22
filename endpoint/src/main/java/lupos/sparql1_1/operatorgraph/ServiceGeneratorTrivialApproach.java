package lupos.sparql1_1.operatorgraph;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Projection;
import lupos.engine.operators.singleinput.SeveralSucceedingOperators;
import lupos.engine.operators.singleinput.federated.FederatedQueryTrivialApproach;
import lupos.engine.operators.singleinput.modifiers.distinct.Distinct;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public class ServiceGeneratorTrivialApproach extends ServiceGenerator {
	
	@Override
	public boolean countsAsJoinPartner(final ASTService node){
		return !(node.jjtGetChild(0) instanceof ASTVar);
	}
	
	@Override
	public void insertIndependantFederatedQueryOperator(final ASTService node, final OperatorConnection connection, final IndexScanCreatorInterface indexScanCreator){
		FederatedQueryTrivialApproach federatedQuery = new FederatedQueryTrivialApproach(node);
		federatedQuery.setSucceedingOperator(connection.getOperatorIDTuple());
		indexScanCreator.createEmptyIndexScanAndConnectWithRoot(new OperatorIDTuple(federatedQuery, 0));
	}

	
	@Override
	public void insertFederatedQueryOperator(final ASTService node, final OperatorConnection connection){
		Node child0 = node.jjtGetChild(0);
		if (child0 instanceof ASTVar) {
			Join join = new Join();
			connection.connect(join);
			SeveralSucceedingOperators sso = new SeveralSucceedingOperators();
			sso.addSucceedingOperator(join);
			FederatedQueryTrivialApproach federatedQuery = new FederatedQueryTrivialApproach(node);
			Projection projection = new Projection();
			projection.addProjectionElement(new Variable(((ASTVar)child0).getName()));
			Distinct distinct = new Distinct();
			projection.addSucceedingOperator(distinct);
			distinct.addSucceedingOperator(federatedQuery);
			sso.addSucceedingOperator(projection, 0);
			federatedQuery.addSucceedingOperator(join, 1);
			connection.setOperatorConnection(sso);
		}		
	}
}
