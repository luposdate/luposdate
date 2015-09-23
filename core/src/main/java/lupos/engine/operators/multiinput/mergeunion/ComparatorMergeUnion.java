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
package lupos.engine.operators.multiinput.mergeunion;

import java.util.Comparator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.Union;
import lupos.misc.debug.DebugStep;
public class ComparatorMergeUnion extends Union {

	protected QueryResult[] operandResults = null;

	protected final Comparator<Bindings> comparator;

	/**
	 * <p>Constructor for ComparatorMergeUnion.</p>
	 *
	 * @param comparator a {@link java.util.Comparator} object.
	 */
	public ComparatorMergeUnion(final Comparator<Bindings> comparator) {
		super();
		this.comparator = comparator;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		if (this.operandResults == null) {
			this.operandResults = new QueryResult[this.precedingOperators.size()];
		}

		this.operandResults[operandID] = bindings;

		boolean flag = true;
		for (final QueryResult qr : this.operandResults) {
			if (qr == null) {
				flag = false;
				break;
			}
		}
		if (flag) {
			return QueryResult
					.createInstance(new MergeUnionIterator(this.operandResults,
							this.succeedingOperators.size() > 1, this.comparator));
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		// do we already process the operand results (then all operandResults
		// are null)?
		// otherwise process it now!
		if (this.operandResults != null) {
			boolean flag = true;
			for (final QueryResult qr : this.operandResults) {
				if (qr == null) {
					flag = false;
					break;
				}
			}
			if (!flag) {
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					opId
							.processAll(QueryResult
									.createInstance(new MergeUnionIterator(
											this.operandResults, this.succeedingOperators
													.size() > 1, this.comparator)));
				}
			}
		}
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		// do we already process the operand results (then all operandResults
		// are null)?
		// otherwise process it now!
		if (this.operandResults != null) {
			boolean flag = true;
			for (final QueryResult qr : this.operandResults) {
				if (qr == null) {
					flag = false;
					break;
				}
			}
			if (!flag) {
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(
							QueryResult
									.createInstance(new MergeUnionIterator(
											this.operandResults, this.succeedingOperators
													.size() > 1, this.comparator)),
							debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug,
							opId.getId(), debugstep);
				}
			}
		}
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return super.toString() + ": " + this.comparator.toString();
	}

}
