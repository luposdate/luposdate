package lupos.engine.operators.singleinput.federated;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumperShort;
import lupos.sparql1_1.ASTTripleSet;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;

/**
 * Superclass of all operators for the service calls to sparql endpoints. 
 */
public abstract class FederatedQuery  extends SingleInputOperator {
	protected final Node federatedQuery;
	protected final Item endpoint;
	protected final Set<Variable> variablesInServiceCall;
	protected final Set<Variable> surelyBoundVariablesInServiceCall;
	
	
	public FederatedQuery(Node federatedQuery) {		
		this.federatedQuery = federatedQuery;				
		Node child0 = this.federatedQuery.jjtGetChild(0);
		if (child0 instanceof ASTVar) {
			this.endpoint = new Variable(((ASTVar) child0).getName());
		} else {
			this.endpoint = LiteralFactory.createURILiteralWithoutLazyLiteralWithoutException("<" + child0.toString() + ">");
		}
		this.variablesInServiceCall = new HashSet<Variable>();
		this.checkVariables(this.federatedQuery.jjtGetChild(1), this.variablesInServiceCall);
		this.surelyBoundVariablesInServiceCall = this.getSurelyBoundVariables();
	}
	
	private String getApproachName(){
		return "Approach "+this.getClass().getSimpleName();
	}
	
	@Override
	public String toString() {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper();
		return this.getApproachName()+"\n"+this.federatedQuery.accept(dumper);
	}

	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumperShort(prefixInstance);
		return this.getApproachName()+"\n"+this.federatedQuery.accept(dumper);
	}
		
	public void checkVariables(Node node, Collection<Variable> vars) {
		if (node instanceof ASTVar) {
			Variable v = new Variable(((ASTVar) node).getName());
			vars.add(v);
		} else {
			if (node.getChildren() != null) {
				for (Node child : node.getChildren()) {
					checkVariables(child, vars);
				}
			}
		}
	}
	
	public Set<Variable> getVariablesInIntersectionOfServiceCallAndBindings(final Bindings bindings){
		Set<Variable> result = bindings.getVariableSet();
		result.retainAll(this.variablesInServiceCall);
		return result;
	}
	
	/**
	 * This method uses a simple static analysis and considers only those variables, which are in triple patterns at top level.
	 * Most queries (in service calls) contain only triple patterns at top level, such that this simple static analysis is usually enough. 
	 * @return the variables, which are surely bound (i.e., in any case) by the sparql endpoint
	 */
	private Set<Variable> getSurelyBoundVariables(){
		Set<Variable> result = new HashSet<Variable>();
		// this.federatedQuery.jjtGetChild(1) contains the ASTGroupConstraint (i.e., the { ... } part) of the service call query
		for(Node node: this.federatedQuery.jjtGetChild(1).getChildren()){
			if(node instanceof ASTTripleSet){
				checkVariables(node, result);
			}
		}
		return result;
	}
}
