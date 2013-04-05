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
package lupos.engine.operators.multiinput.join;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.BindingsComparator;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;

public class MergeJoinWithoutSorting extends Join {

	private static final long serialVersionUID = 5051512203278340771L;

	protected QueryResult left = null;
	protected QueryResult right = null;

	protected BindingsComparator comp = new BindingsComparator();

	/**
	 * This method pre-processes the StartOfStreamMessage
	 * 
	 * @param msg
	 *            the message to be pre-processed
	 * @return the pre-processed message
	 */
	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		if (this.left != null)
			this.left.release();
		if (this.right != null)
			this.right.release();
		this.left = null;
		this.right = null;
		return super.preProcessMessage(msg);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (operandID == 0) {
			this.left = bindings;
		} else if (operandID == 1) {
			this.right = bindings;
		} else
			System.err.println("MergeJoin is a binary operator, but received the operand number "
							+ operandID);
		if (this.left != null && this.right != null) {

			this.comp.setVariables(this.intersectionVariables);
			final ParallelIterator<Bindings> currentResult = (this.intersectionVariables
					.size() == 0) ? MergeJoin.cartesianProductIterator(this.left, this.right) : 
						MergeJoin.mergeJoinIterator(this.left.oneTimeIterator(), this.right.oneTimeIterator(), this.comp, this.intersectionVariables);
			if (currentResult != null && currentResult.hasNext()) {
				final QueryResult result = QueryResult
						.createInstance(new SIPParallelIterator<Bindings, Bindings>() {

							int number = 0;

							@Override
							public void close() {
								currentResult.close();
							}

							@Override
							public boolean hasNext() {
								if (!currentResult.hasNext()) {
									MergeJoinWithoutSorting.this.realCardinality = this.number;
									close();
								}
								return currentResult.hasNext();
							}

							@Override
							public Bindings next() {
								final Bindings b = currentResult.next();
								if (b != null)
									this.number++;
								if (!currentResult.hasNext()) {
									MergeJoinWithoutSorting.this.realCardinality = this.number;
									close();
								}
								return b;
							}

							public Bindings getNext(final Bindings k) {
								@SuppressWarnings("unchecked")
								final Bindings b = ((SIPParallelIterator<Bindings, Bindings>) currentResult)
										.next(k);
								if (b != null)
									this.number++;
								if (!currentResult.hasNext()) {
									MergeJoinWithoutSorting.this.realCardinality = this.number;
									close();
								}
								return b;
							}

							@Override
							public void remove() {
								currentResult.remove();
							}

							@Override
							public void finalize() {
								close();
							}

							@Override
							public Bindings next(final Bindings k) {
								if (currentResult instanceof SIPParallelIterator)
									return getNext(k);
								else
									return next();
							}
						});
				return result;
			} else {
				this.left.release();
				this.right.release();
				return null;
			}
		} else {
			return null;
		}
	}
}
