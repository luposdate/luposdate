package lupos.sparql1_1.operatorgraph;

import java.util.LinkedList;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.singleinput.Group;
import lupos.engine.operators.singleinput.SeveralSucceedingOperators;
import lupos.engine.operators.singleinput.federated.FederatedQuery;
import lupos.engine.operators.singleinput.sort.Sort;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorVariables;
import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

public abstract class ServiceGeneratorToJoinWithOriginal extends ServiceGenerator {
	@Override
	public void insertFederatedQueryOperator(final ASTService node, final OperatorConnection connection){
		SeveralSucceedingOperators sso = new SeveralSucceedingOperators();
		BasicOperator federatedQuery = this.getFederatedQuery(node);
		Node child0 = node.jjtGetChild(0);
		if(child0 instanceof ASTVar){
			Sort sort = new Sort();
			LinkedList<Variable> listOfVars = new LinkedList<Variable>();
			listOfVars.add(new Variable(((ASTVar)child0).getName()));
			ComparatorVariables comparator = new ComparatorVariables(listOfVars);
			sort.setComparator(comparator);
			Group group = new Group(comparator);
			sort.addSucceedingOperator(group);
			group.addSucceedingOperator(federatedQuery);
			sso.addSucceedingOperator(sort);
		} else {
			sso.addSucceedingOperator(federatedQuery);
		}
		Join join = new Join();
		federatedQuery.addSucceedingOperator(join);
		sso.addSucceedingOperator(join, 1);
		connection.connect(join);
		connection.setOperatorConnection(sso);
	}

	protected abstract FederatedQuery getFederatedQuery(ASTService node);
}
