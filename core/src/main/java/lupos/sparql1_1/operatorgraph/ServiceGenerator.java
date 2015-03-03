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
package lupos.sparql1_1.operatorgraph;

import lupos.sparql1_1.ASTService;
import lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface;
import lupos.sparql1_1.operatorgraph.helper.OperatorConnection;

/**
 * This class is overridden by classes of the module endpoint, which really implement the operatorgraph generation for the service calls...
 *
 * @author groppe
 * @version $Id: $Id
 */
public class ServiceGenerator { 	

	/**
	 * <p>insertIndependantFederatedQueryOperator.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTService} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 * @param indexScanCreator a {@link lupos.sparql1_1.operatorgraph.helper.IndexScanCreatorInterface} object.
	 */
	@SuppressWarnings("unused")
	public void insertIndependantFederatedQueryOperator(final ASTService node, final OperatorConnection connection, final IndexScanCreatorInterface indexScanCreator){
		throw new UnsupportedOperationException("To enable service calls in SPARQL queries, please use the endpoint module!");
	}
	
	/**
	 * <p>countsAsJoinPartner.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTService} object.
	 * @return a boolean.
	 */
	@SuppressWarnings("unused")
	public boolean countsAsJoinPartner(final ASTService node){
		return false;
	}
		
	/**
	 * <p>insertFederatedQueryOperator.</p>
	 *
	 * @param node a {@link lupos.sparql1_1.ASTService} object.
	 * @param connection a {@link lupos.sparql1_1.operatorgraph.helper.OperatorConnection} object.
	 */
	@SuppressWarnings("unused")
	public void insertFederatedQueryOperator(final ASTService node, final OperatorConnection connection){
		throw new UnsupportedOperationException("To enable service calls in SPARQL queries, please use the endpoint module!");
	}
}
