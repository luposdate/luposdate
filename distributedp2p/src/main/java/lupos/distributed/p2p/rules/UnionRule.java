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
package lupos.distributed.p2p.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;

/**
 * {@link lupos.optimizations.logical.rules.Rule} for LuposDate, that removes the operator "union", if only
 * one single input is set! So the optimization is it, to remove the "union" operator.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class UnionRule extends Rule {

	private lupos.engine.operators.multiinput.Union u = null;
	private lupos.distributed.operator.SubgraphContainer<?> sgc = null;

	private boolean _checkPrivate0(BasicOperator _op) {
		/*
		 * allow only SubgraphContainers as root entry
		 */
		if (_op.getClass() != lupos.distributed.operator.SubgraphContainer.class) {
			return false;
		}
		/*
		 * store the SubgraphContainer
		 */
		this.sgc = (lupos.distributed.operator.SubgraphContainer<?>) _op;

		/*
		 * Now get any "union" as succeeding operator of SubgraphContainer
		 */
		List<OperatorIDTuple> _succedingOperators_1_0 = _op
				.getSucceedingOperators();
		for (OperatorIDTuple _sucOpIDTup_1_0 : _succedingOperators_1_0) {
			if (!(_sucOpIDTup_1_0.getOperator() instanceof lupos.engine.operators.multiinput.Union)) {
				continue;
			}
			/*
			 * store this "union", but only if this operator has only one
			 * preceding operator (has to be the stored SubgraphContainer!)
			 */
			this.u = (lupos.engine.operators.multiinput.Union) _sucOpIDTup_1_0
					.getOperator();
			if (this.u.getPrecedingOperators().size() > 1)
				continue;
			return true;
		}
		return false;
	}

	/**
	 * Constructor for this rule
	 */
	public UnionRule() {
		this.startOpClass = lupos.distributed.operator.SubgraphContainer.class;
		this.ruleName = "Remove single input union operator";
	}

	/** {@inheritDoc} */
	@Override
	protected boolean check(BasicOperator _op) {
		return this._checkPrivate0(_op);
	}

	/** {@inheritDoc} */
	@Override
	protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		this.u.removeFromOperatorGraph();
		this.sgc.removeSucceedingOperator(this.u);
		for (OperatorIDTuple operator : this.u.getSucceedingOperators()) {
			operator.getOperator().removePrecedingOperator(this.u);
			operator.getOperator().addPrecedingOperator(this.sgc);
		}
		Logger.getLogger(getClass()).debug(String.format("Rule: %s: - Removed %s",this.ruleName,this.u));
	}
}
