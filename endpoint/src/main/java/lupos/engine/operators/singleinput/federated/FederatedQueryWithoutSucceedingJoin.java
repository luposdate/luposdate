package lupos.engine.operators.singleinput.federated;

import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.sparql1_1.Node;

public abstract class FederatedQueryWithoutSucceedingJoin extends FederatedQuery {

	public FederatedQueryWithoutSucceedingJoin(Node federatedQuery) {
		super(federatedQuery);
	}

	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		this.intersectionVariables = new HashSet<Variable>();
		this.unionVariables = new HashSet<Variable>();
		// fetch-as-needed and join-at-endpoint compute a join with the previous result, i.e. the previous bound variables are also bound afterwards:
		this.unionVariables.addAll(msg.getVariables());
		checkVariables(this.federatedQuery, this.unionVariables);
		this.intersectionVariables.addAll(this.unionVariables);
		final BoundVariablesMessage result = new BoundVariablesMessage(msg);
		result.getVariables().addAll(this.unionVariables);
		return result;
	}
}
