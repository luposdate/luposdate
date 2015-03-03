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
import org.apache.log4j.Logger;

import lupos.optimizations.logical.rules.generated.runtime.Rule;
import lupos.engine.operators.BasicOperator;

/**
 * {@link lupos.optimizations.logical.rules.Rule} for LuposDate, that removes operators, that need multi input, but only have single input data.
 * The optimization would be, to remove the operator.
 *
 * @author Bjoern
 * @version $Id: $Id
 */
public class MultiInputRule extends Rule {

	private lupos.engine.operators.multiinput.MultiInputOperator mi = null;

	private boolean _checkPrivate0(BasicOperator _op) {
		/*
		 * allow only MultiInputOperator that have single input
		 */
		if (!(_op instanceof lupos.engine.operators.multiinput.MultiInputOperator)) {
			return false;
		}
		mi = (lupos.engine.operators.multiinput.MultiInputOperator) _op;
		return (mi.getPrecedingOperators().size() <= 1);
	}

	/**
	 * Constructor for this rule
	 */
	public MultiInputRule() {
		this.startOpClass = lupos.engine.operators.multiinput.MultiInputOperator.class;
		this.ruleName = "Remove multi input operators with single input";
	}

	/** {@inheritDoc} */
	@Override
	protected boolean check(BasicOperator _op) {
		return this._checkPrivate0(_op);
	}

	/** {@inheritDoc} */
	@Override
	protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		
		this.mi.removeFromOperatorGraph();
		this.deleteOperatorWithoutParentsRecursive(this.mi, _startNodes);
		Logger.getLogger(getClass()).debug(String.format("Rule: %s:  - Removed single input operator: %s",this.ruleName,this.mi));
	}
}
