/**
 * Copyright (c) 2012, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.engine.operators.multiinput.optional;

import java.util.Comparator;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.join.MergeJoin;
import lupos.misc.debug.DebugStep;

public class MergeWithoutSortingOptional extends Optional {

	protected QueryResult left = null;
	protected QueryResult right = null;

	protected Comparator<Bindings> comp = new Comparator<Bindings>() {

		@Override
		public int compare(final Bindings o1, final Bindings o2) {
			for (final Variable var : MergeWithoutSortingOptional.this.intersectionVariables) {
				final Literal l1 = o1.get(var);
				final Literal l2 = o2.get(var);
				if (l1 != null && l2 != null) {
					final int compare = l1
							.compareToNotNecessarilySPARQLSpecificationConform(l2);
					if (compare != 0)
						return compare;
				} else if (l1 != null)
					return -1;
				else if (l2 != null)
					return 1;
			}
			return 0;
		}
	};

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (this.precedingOperators.size() == 1) {
			return bindings;
		} else {
			if (operandID == 0) {
				this.left = bindings;
			} else if (operandID == 1) {
				this.right = bindings;
			} else
				System.err.println("MergeWithoutSortingOptional is a binary operator, but received the operand number " + operandID);
			if (this.left != null && this.right != null) {				

				final Iterator<Bindings> currentResult = MergeJoin.mergeOptionalIterator(this.left.oneTimeIterator(), this.right.oneTimeIterator(), this.comp);

				if (currentResult != null && currentResult.hasNext()) {
					final QueryResult result = QueryResult.createInstance(currentResult);
					return result;					
				} else
					return null;
			} else
				return null;
		}
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		if (this.left != null && this.right == null) {
			if (this.succeedingOperators.size() > 1)
				this.left.materialize();
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				opId.processAll(this.left);
			}
		}
		return msg;
	}
	
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		if (this.left != null && this.right == null) {
			if (this.succeedingOperators.size() > 1)
				this.left.materialize();
			for (final OperatorIDTuple opId : this.succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(this.left,
						debugstep, this, opId.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
		}
		return msg;
	}
}
