/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.BindingsComparator;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;

public class NAryMergeJoinWithoutSorting extends Join {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5051512203278340771L;

	protected QueryResult[] operandResults;
	protected final Bindings minimum;
	protected final Bindings maximum;

	public NAryMergeJoinWithoutSorting(@SuppressWarnings("unused") final int numberOfOperands, final Bindings minimum, final Bindings maximum) {
		super();
		this.operandResults = new QueryResult[this.getNumberOfOperands()];
		this.minimum = minimum;
		this.maximum = maximum;
	}

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
		for (int i = 0; i < this.operandResults.length; i++) {
			if (this.operandResults[i] != null) {
				this.operandResults[i].release();
				this.operandResults[i] = null;
			}
		}
		return super.preProcessMessage(msg);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (operandID < this.operandResults.length) {
			this.operandResults[operandID] = bindings;
		} else
			System.err.println("NAryMergeJoin is a " + this.operandResults.length
					+ "-ary operator, but received the operand number "
					+ operandID);
		boolean go = true;
		for (int i = 0; i < this.operandResults.length; i++) {
			if (this.operandResults[i] == null) {
				go = false;
				break;
			}
		}
		if (go) {

			this.comp.setVariables(this.intersectionVariables);
			
			if (this.minimum == null)
				return null;

			@SuppressWarnings("unchecked")
			final Iterator<Bindings>[] itb = new Iterator[this.operandResults.length];
			for (int i = 0; i < this.operandResults.length; i++) {
				itb[i] = this.operandResults[i].oneTimeIterator();
			}

			final ParallelIterator<Bindings> currentResult = (this.intersectionVariables
					.size() == 0) ? MergeJoin
					.cartesianProductIterator(this.operandResults) : MergeJoin
					.mergeJoinIterator(itb, this.comp, this.intersectionVariables,
							this.minimum, this.maximum);
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
									NAryMergeJoinWithoutSorting.this.realCardinality = this.number;
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
									NAryMergeJoinWithoutSorting.this.realCardinality = this.number;
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
									NAryMergeJoinWithoutSorting.this.realCardinality = this.number;
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
				for (int i = 0; i < this.operandResults.length; i++) {
					if (this.operandResults[i] != null) {
						this.operandResults[i].release();
						this.operandResults[i] = null;
					}
				}
				return null;
			}
		} else {
			return null;
		}
	}
}
