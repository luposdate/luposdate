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
package lupos.optimizations.logical.rules.parallel;

import java.util.Map;

import lupos.engine.operators.BasicOperator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.SimpleOperatorGraphVisitor;
import lupos.engine.operators.multiinput.join.Join;
import lupos.engine.operators.multiinput.optional.Optional;
import lupos.engine.operators.singleinput.parallel.ParallelOperand;

/**
 * Implements a graph transformation which inserts a {@link ParallelOperand}
 * between each {@link Join} operator and its arguments, effectively evaluating
 * in a separate thread and thus distributing it across possibly multiple
 * processors.
 * 
 * @see ParallelOperand
 */
public class RuleJoinLastParallelOperands extends RuleJoinWithParallelOperands {

	private boolean checkRecursiveForJoinOrOptional(final BasicOperator op) {
		for (final OperatorIDTuple sop : op.getSucceedingOperators()) {
			final FindJoinOrOptionalOperatorVisitor frov = new FindJoinOrOptionalOperatorVisitor();
			sop.getOperator().visit(frov);
			if (frov.found())
				return true;
		}
		return false;
	}

	@Override
	protected boolean checkPrecondition(final Map<String, BasicOperator> mso) {
		final BasicOperator join = mso.get("join");

		return !checkRecursiveForJoinOrOptional(join)
				&& super.checkPrecondition(mso);
	}

	@Override
	public String getName() {
		return "JoinLastParallelOperands";
	}

	private class FindJoinOrOptionalOperatorVisitor implements SimpleOperatorGraphVisitor {

		private boolean found = false;

		public Object visit(final BasicOperator basicOperator) {
			if (basicOperator instanceof Join
					|| basicOperator instanceof Optional)
				found = true;
			return null;
		}

		public boolean found() {
			return found;
		}

	}
}
