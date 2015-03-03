
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.engine.operators.singleinput.federated;

import java.util.HashSet;

import lupos.datastructures.items.Variable;
import lupos.engine.operators.messages.BoundVariablesMessage;
import lupos.engine.operators.messages.Message;
import lupos.sparql1_1.Node;
public abstract class FederatedQueryWithoutSucceedingJoin extends FederatedQuery {

	/**
	 * <p>Constructor for FederatedQueryWithoutSucceedingJoin.</p>
	 *
	 * @param federatedQuery a {@link lupos.sparql1_1.Node} object.
	 */
	public FederatedQueryWithoutSucceedingJoin(Node federatedQuery) {
		super(federatedQuery);
	}

	/** {@inheritDoc} */
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
