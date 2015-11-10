/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.engine.operators.singleinput.federated;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.bindings.BindingsFactory;
import lupos.datastructures.items.Item;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.messages.BindingsFactoryMessage;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.SingleInputOperator;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumper;
import lupos.optimizations.sparql2core_sparql.SPARQLParserVisitorImplementationDumperShort;
import lupos.sparql1_1.ASTGroupConstraint;
import lupos.sparql1_1.ASTSelectQuery;
import lupos.sparql1_1.ASTTripleSet;
import lupos.sparql1_1.ASTUnionConstraint;
import lupos.sparql1_1.ASTVar;
import lupos.sparql1_1.Node;

/**
 * Superclass of all operators for the service calls to sparql endpoints.
 *
 * @author groppe
 * @version $Id: $Id
 */
public abstract class FederatedQuery  extends SingleInputOperator {
	protected final Node federatedQuery;
	protected final Item endpoint;
	protected final Set<Variable> variablesInServiceCall;
	protected final Set<Variable> surelyBoundVariablesInServiceCall;
	protected Set<Variable> variablesBoundFromOutside;

	protected BindingsFactory bindingsFactory;

	public static final int MAX_BINDINGS_IN_ENDPOINT_REQUEST = 300;

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final BindingsFactoryMessage msg){
		this.bindingsFactory = msg.getBindingsFactory();
		return msg;
	}


	@Override
	public Message preProcessMessage(final BoundVariablesMessage msg) {
		this.variablesBoundFromOutside = new HashSet<Variable>(msg.getVariables());
		final Set<Variable> variables = new HashSet<Variable>(msg.getVariables());
		variables.addAll(this.surelyBoundVariablesInServiceCall);
		return new BoundVariablesMessage();
	}

	/**
	 * <p>Constructor for FederatedQuery.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQuery(final Node federatedQuery) {
		this.federatedQuery = federatedQuery;
		final Node child0 = this.federatedQuery.jjtGetChild(0);
		if (child0 instanceof ASTVar) {
			this.endpoint = new Variable(((ASTVar) child0).getName());
		} else {
			this.endpoint = LiteralFactory.createURILiteralWithoutLazyLiteralWithoutException("<" + child0.toString() + ">");
		}
		this.variablesInServiceCall = new HashSet<Variable>();
		FederatedQuery.checkVariables(this.federatedQuery.jjtGetChild(1), this.variablesInServiceCall);
		this.surelyBoundVariablesInServiceCall = this.getSurelyBoundVariables();
	}

	private String getApproachName(){
		return "Approach "+this.getClass().getSimpleName();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumper();
		return this.getApproachName()+"\n"+this.federatedQuery.accept(dumper);
	}

	/** {@inheritDoc} */
	@Override
	public String toString(final lupos.rdf.Prefix prefixInstance) {
		final SPARQLParserVisitorImplementationDumper dumper = new SPARQLParserVisitorImplementationDumperShort(prefixInstance);
		return this.getApproachName()+"\n"+this.federatedQuery.accept(dumper);
	}

	/**
	 * <p>checkVariables.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.Node} object.
	 * @param vars a {@link java.util.Collection} object.
	 */
	public static void checkVariables(final Node node, final Collection<Variable> vars) {
		if (node instanceof ASTVar) {
			final Variable v = new Variable(((ASTVar) node).getName());
			vars.add(v);
		} else {
			if (node.getChildren() != null) {
				for (final Node child : node.getChildren()) {
					FederatedQuery.checkVariables(child, vars);
				}
			}
		}
	}

	/**
	 * <p>getVariablesInIntersectionOfServiceCallAndBindings.</p>
	 *
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Variable> getVariablesInIntersectionOfServiceCallAndBindings(final Bindings bindings){
		return FederatedQuery.getVariablesInIntersectionOfSetOfVariablesAndBindings(this.variablesInServiceCall, bindings);
	}

	/**
	 * <p>getVariablesInIntersectionOfSetOfVariablesAndBindings.</p>
	 *
	 * @param variablesInServiceCall a {@link java.util.Set} object.
	 * @param bindings a {@link lupos.datastructures.bindings.Bindings} object.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Variable> getVariablesInIntersectionOfSetOfVariablesAndBindings(final Set<Variable> variablesInServiceCall, final Bindings bindings){
		final Set<Variable> result = bindings.getVariableSet();
		result.retainAll(variablesInServiceCall);
		return result;
	}

	/**
	 * This method uses a simple static analysis to determine the variables, which are surely bound.
	 * This simple static analysis is usually enough for most queries.
	 * @return the variables, which are surely bound (i.e., in any case) by the sparql endpoint
	 */
	private Set<Variable> getSurelyBoundVariables(){
		// this.federatedQuery.jjtGetChild(1) contains the ASTGroupConstraint (i.e., the { ... } part) of the service call query
		return FederatedQuery.getSurelyBoundVariables(this.federatedQuery.jjtGetChild(1));
	}

	/**
	 * This method uses a simple static analysis to determine the variables, which are surely bound.
	 * This simple static analysis is usually enough for most queries.
	 * @param node the node in the abstract syntax tree to be checked...
	 * @return the variables, which are surely bound (i.e., in any case) by the sparql endpoint
	 */
	private static Set<Variable> getSurelyBoundVariables(final Node node){
		final Set<Variable> result = new HashSet<Variable>();

		if(node instanceof ASTGroupConstraint){
			final Node[] children = node.getChildren();
			if(children!=null){
				for(final Node child: node.getChildren()){
					result.addAll(FederatedQuery.getSurelyBoundVariables(child));
				}
			}
		} else if(node instanceof ASTTripleSet){
			FederatedQuery.checkVariables(node, result);
		} else if(node instanceof ASTSelectQuery){
			// check embedded select queries recursively
			final ASTSelectQuery select = (ASTSelectQuery) node;
			final Set<Variable> innerQueryVariables = new HashSet<Variable>();
			final Set<Variable> innerQueryProjectedVariables = new HashSet<Variable>();
			for(final Node innerQueryChild: select.getChildren()){
				if(innerQueryChild instanceof ASTVar){
					final Variable v = new Variable(((ASTVar) innerQueryChild).getName());
					innerQueryProjectedVariables.add(v);
				} else if(innerQueryChild instanceof ASTGroupConstraint){
					innerQueryVariables.addAll(FederatedQuery.getSurelyBoundVariables(innerQueryChild));
				}
			}

			if(!select.isSelectAll()){
				innerQueryVariables.retainAll(innerQueryProjectedVariables);
			}
			result.addAll(innerQueryVariables);
		} else if(node instanceof ASTUnionConstraint){
			// Check union...
			// Compute intersection of variables of left and right operand:
			final Set<Variable> resultUnion = FederatedQuery.getSurelyBoundVariables(node.jjtGetChild(0));
			resultUnion.retainAll(FederatedQuery.getSurelyBoundVariables(node.jjtGetChild(1)));
			result.addAll(resultUnion);
		}
		return result;
	}


}
