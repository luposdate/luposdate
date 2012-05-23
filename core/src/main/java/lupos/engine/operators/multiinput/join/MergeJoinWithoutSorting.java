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
package lupos.engine.operators.multiinput.join;

import java.util.Comparator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.Variable;
import lupos.datastructures.items.literal.Literal;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.SIPParallelIterator;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;

public class MergeJoinWithoutSorting extends Join {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5051512203278340771L;

	protected QueryResult left = null;
	protected QueryResult right = null;

	protected Comparator<Bindings> comp = new Comparator<Bindings>() {

		public int compare(final Bindings o1, final Bindings o2) {
			for (final Variable var : intersectionVariables) {
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

	/**
	 * This method pre-processes the StartOfStreamMessage
	 * 
	 * @param msg
	 *            the message to be pre-processed
	 * @return the pre-processed message
	 */
	@Override
	public Message preProcessMessage(final StartOfEvaluationMessage msg) {
		if (left != null)
			left.release();
		if (right != null)
			right.release();
		left = null;
		right = null;
		return super.preProcessMessage(msg);
	}

	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		if (operandID == 0) {
			left = bindings;
		} else if (operandID == 1) {
			right = bindings;
		} else
			System.err.println("MergeJoin is a binary operator, but received the operand number "
							+ operandID);
		if (left != null && right != null) {

			// System.out.println(">>>>>>>>>>left " + intersectionVariables
			// + " size:" + left.size());
			// Bindings last = null;
			// int i = 0;
			// for (final Bindings b : left) {
			// i++;
			// if (last != null) {
			// if (comp.compare(last, b) > 0) {
			// System.out.println("Not correctly sorted:" + last
			// + "<---------->" + b);
			// }
			// }
			// last = b;
			// }
			// System.out.println(">>>>>>>>>>right " + intersectionVariables
			// + " size:" + right.size());
			// last = null;
			// for (final Bindings b : right) {
			// if (last != null) {
			// if (comp.compare(last, b) > 0) {
			// System.out.println("Not correctly sorted:" + last
			// + "<---------->" + b);
			// }
			// }
			// last = b;
			// }

			final ParallelIterator<Bindings> currentResult = (intersectionVariables
					.size() == 0) ? MergeJoin.cartesianProductIterator(left,
					right) : MergeJoin.mergeJoinIterator(
					left.oneTimeIterator(), right.oneTimeIterator(), comp,
					intersectionVariables);
			if (currentResult != null && currentResult.hasNext()) {
				final QueryResult result = QueryResult
						.createInstance(new SIPParallelIterator<Bindings, Bindings>() {

							int number = 0;

							public void close() {
								currentResult.close();
							}

							public boolean hasNext() {
								if (!currentResult.hasNext()) {
									realCardinality = number;
									close();
								}
								return currentResult.hasNext();
							}

							public Bindings next() {
								final Bindings b = currentResult.next();
								if (b != null)
									number++;
								if (!currentResult.hasNext()) {
									realCardinality = number;
									close();
								}
								return b;
							}

							public Bindings getNext(final Bindings k) {
								final Bindings b = ((SIPParallelIterator<Bindings, Bindings>) currentResult)
										.next(k);
								if (b != null)
									number++;
								if (!currentResult.hasNext()) {
									realCardinality = number;
									close();
								}
								return b;
							}

							public void remove() {
								currentResult.remove();
							}

							@Override
							public void finalize() {
								close();
							}

							public Bindings next(final Bindings k) {
								if (currentResult instanceof SIPParallelIterator)
									return getNext(k);
								else
									return next();
							}
						});
				// System.out.println(this.toString());
				// System.out.println("!!!!!!!!!!Preceding operators:"
				// + this.getPrecedingOperators());
				// System.out.println("!!!!!!!!!!Results: Left:"+left.size()+
				// "\n!!!!!!!!!! Right:"
				// +right.size()+"\n!!!!!!!!!! Result:"+((result
				// ==null)?"null":result.size()));*/
				// System.out.println("!!!!!!!!!!Results: Left:" + left
				// + "\n!!!!!!!!!! Right:" + right
				// + "\n!!!!!!!!!! Result:"
				// + ((result == null) ? "null" : result));
				// System.out.println("Result:" + result);
				// System.out.println("Result size:" + result.size());
				return result;
			} else {
				left.release();
				right.release();
				return null;
			}
		} else {
			return null;
		}
	}
}
