/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
			sso.addSucceedingOperator(join, 0);
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
