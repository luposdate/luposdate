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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.index.Root;
import lupos.optimizations.logical.rules.generated.runtime.Rule;

/**
 * This {@link Rule} is an optional rule, that checks if all proceeding and succeeding operators are right connected,
 * so that the graph can be accessed in both order.
 */
public class CheckRule extends Rule {
	private Logger l = Logger.getLogger(getClass().getName());
	private BasicOperator root;

	public CheckRule() {
		this.ruleName = "CheckSucceedingPreceding-Connections";
	}
	
	@Override
	protected boolean check(BasicOperator _rootOp) {
		if (root == null && _rootOp instanceof Root) {
			this.root = _rootOp;
			return true;
		}
		return false;
	}

	private Set<BasicOperator> alreadyVisited = new HashSet<BasicOperator>();
	
	private void checkSucceeding(BasicOperator root) {
		if (alreadyVisited.contains(root)) return;
		alreadyVisited.add(root);
		List<OperatorIDTuple> succs = root.getSucceedingOperators();
		firstLoop:
		for (OperatorIDTuple op : succs) {
			for (BasicOperator pred : op.getOperator().getPrecedingOperators()) {
				if (root.equals(pred)) {
					continue firstLoop;
				}
			}
			l.log(Level.FINEST , String.format("Rule %s: Missing preceeding added: %s should have preceeding %s",this.ruleName,op,root));
			op.getOperator().addPrecedingOperator(root);
		}
		for (OperatorIDTuple op : root.getSucceedingOperators()) checkSucceeding(op.getOperator());
	}
	
	@Override
	protected void replace(HashMap<Class<?>, HashSet<BasicOperator>> _startNodes) {
		for (OperatorIDTuple o : this.root.getSucceedingOperators()) checkSucceeding(o.getOperator());
	}

}
