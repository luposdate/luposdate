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

import java.util.HashSet;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.items.BindingsComparator;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.messages.StartOfEvaluationMessage;
import lupos.engine.operators.multiinput.MergeIterator;

public class MergeJoinWithoutSortingSeveralIterations extends Join {

	/**
	 * 
	 */
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

	@SuppressWarnings("unchecked")
	@Override
	public QueryResult process(final QueryResult bindings, final int operandID) {
		bindings.materialize();
		this.comp.setVariables(this.intersectionVariables);
		final QueryResult oldLeft = this.left;
		final QueryResult oldRight = this.right;
		if (operandID == 0) {
			if (this.left == null)
				this.left = bindings;
			else
				this.left = QueryResult.createInstance(new MergeIterator<Bindings>(this.comp, this.left.iterator(), bindings.iterator()));
		} else if (operandID == 1) {
			if (this.right == null)
				this.right = bindings;
			else
				this.right = QueryResult.createInstance(new MergeIterator<Bindings>(this.comp, this.right.iterator(), bindings.iterator()));
		} else
			System.err.println("MergeJoin is a binary operator, but received the operand number "
							+ operandID);
		if (this.left != null && this.right != null) {
			this.left.materialize();
			this.right.materialize();

			final Iterator<Bindings> leftIterator = (operandID == 0 && oldLeft != null) ? new MinusIterator(
					bindings.iterator(), oldLeft.iterator())
					: this.left.iterator();
			final QueryResult rightLocal = (operandID == 1 && oldRight != null) ? QueryResult
					.createInstance(new MinusIterator(bindings.iterator(),
							oldRight.iterator()))
					: this.right;


			final ParallelIterator<Bindings> currentResult = (this.intersectionVariables
					.size() == 0) ? MergeJoin.cartesianProductIterator(
					leftIterator, rightLocal) : MergeJoin.mergeJoinIterator(
					leftIterator, rightLocal.iterator(), this.comp,
					this.intersectionVariables);
			if (currentResult != null && currentResult.hasNext()) {
				final QueryResult result = QueryResult
						.createInstance(new ParallelIterator<Bindings>() {

							int number = 0;

							@Override
							public void close() {
								currentResult.close();
							}

							@Override
							public boolean hasNext() {
								if (!currentResult.hasNext()) {
									MergeJoinWithoutSortingSeveralIterations.this.realCardinality = this.number;
									close();
								}
								return currentResult.hasNext();
							}

							@Override
							public Bindings next() {
								final Bindings b = currentResult.next();
								if (!currentResult.hasNext()) {
									MergeJoinWithoutSortingSeveralIterations.this.realCardinality = this.number;
									close();
								}
								if (b != null)
									this.number++;
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
						});
				return result;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public class MinusIterator implements ParallelIterator<Bindings> {

		final Iterator<Bindings> it;
		final Iterator<Bindings> minus;
		final HashSet<Bindings> currentMinusMap = new HashSet<Bindings>();
		Bindings currentMinus = null;
		Bindings nextMinus = null;
		Bindings next = null;

		public MinusIterator(final Iterator<Bindings> it,
				final Iterator<Bindings> minus) {
			this.it = it;
			this.minus = minus;
			nextMap();
		}

		@Override
		public void close() { // nothing to close...
		}

		@Override
		public boolean hasNext() {
			if (this.next == null)
				this.next = computeNext();
			return (this.next != null);
		}

		@Override
		public Bindings next() {
			if (this.next != null) {
				final Bindings znext = this.next;
				this.next = null;
				return znext;
			} else
				return computeNext();
		}

		private Bindings computeNext() {
			while (true) {
				if (!this.it.hasNext())
					return null;
				final Bindings next_local = this.it.next();
				if (next_local == null)
					return null;
				while (true) {
					if (this.currentMinus == null)
						return next_local;
					final int compare = MergeJoinWithoutSortingSeveralIterations.this.comp.compare(next_local, this.currentMinus);
					if (compare == 0) {
						if (this.currentMinusMap.contains(next_local)) {
							break;
						} else
							return next_local;
					}
					if (compare < 0)
						return next_local;
					nextMap();
				}
			}
		}

		private void nextMap() {
			this.currentMinusMap.clear();
			if (this.nextMinus == null && this.minus.hasNext())
				this.nextMinus = this.minus.next();
			if (this.nextMinus != null) {
				this.currentMinus = this.nextMinus;
				this.currentMinusMap.add(this.nextMinus);
				this.nextMinus = null;
				while (this.minus.hasNext()) {
					this.nextMinus = this.minus.next();
					if (MergeJoinWithoutSortingSeveralIterations.this.comp.compare(this.nextMinus, this.currentMinus) == 0)
						this.currentMinusMap.add(this.nextMinus);
					else
						break;
					this.nextMinus = null;
				}
			} else
				this.currentMinus = null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
