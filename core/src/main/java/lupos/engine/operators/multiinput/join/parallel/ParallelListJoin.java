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
/**
 *
 */
package lupos.engine.operators.multiinput.join.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.join.HashFunction;
import lupos.engine.operators.multiinput.join.Join;
import lupos.misc.debug.DebugStep;

public class ParallelListJoin extends Join {

	private static final long serialVersionUID = 1L;

	private static final int LEFT = 0;

	private static final int RIGHT = 1;

	/**
	 * left results: this list is never null!
	 */
	private final List<QueryResult> left;

	/**
	 * right results: this list is never null!
	 */
	private final List<QueryResult> right;

	private final Collection<? extends Join> operators;

	private final ResultCollector col;

	private EndOfEvaluationMessage endOfStreamMsg;

	private final HashFunction hashFun;

	private final List<ParallelJoiner> threadList;

	public ParallelListJoin(final Collection<? extends Join> operators) {
		this.operators = operators;
		col = new ResultCollector();
		for (final Join join : operators) {
			join.setSucceedingOperator(new OperatorIDTuple(col, 0));
		}
		hashFun = new HashFunction();
		left = new LinkedList<QueryResult>();
		right = new LinkedList<QueryResult>();
		threadList = new ArrayList<ParallelJoiner>(operators.size());
	}

	@Override
	public QueryResult process(final QueryResult input, final int operatorID) {
		switch (operatorID) {
		case LEFT:
			left.add(input);
			break;
		case RIGHT:
			right.add(input);
			break;
		default:
			throw new UnsupportedOperationException(
					"Only two operators are supported!");
		}
		return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		endOfStreamMsg = msg;

		final QueryResult qr = join(left, right);
		if (qr != null) {
			// System.out
			// .println("ParallelJoin.preprocessMessage() qr ist  nicht null");
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(qr);
			}
		}
		left.clear();
		right.clear();
		return msg;
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		endOfStreamMsg = msg;

		final QueryResult qr = join(left, right);
		if (qr != null) {
			// System.out
			// .println("ParallelJoin.preprocessMessage() qr ist  nicht null");
			for (final OperatorIDTuple opId : succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(qr,
						debugstep, this, opId.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
						.getId(), debugstep);
			}
		}
		left.clear();
		right.clear();
		return msg;
	}

	/**
	 * The actual join.
	 * 
	 * @param left
	 *            a list with query results for the left table
	 * @param right
	 *            a list with query results for the right table
	 * @return the result
	 */
	private QueryResult join(final List<QueryResult> left,
			final List<QueryResult> right) {
		// compute intersection variables
		// number of threads is minimum of the number of operators or left or
		// right size

		final int numberOfThreads = operators.size();
		threadList.clear();
		// create Queryresults
		final QueryResult[] leftResults = new QueryResult[numberOfThreads];
		final QueryResult[] rightResults = new QueryResult[numberOfThreads];
		for (int i = 0; i < rightResults.length; i++) {
			leftResults[i] = QueryResult.createInstance();
			rightResults[i] = QueryResult.createInstance();
		}
		// relate each binding to one of the right results
		// the key of the hashfunktion should be in the bound of the
		// Threads number
		// every array cell has a list of bindings
		fillResultArray(right, numberOfThreads, rightResults);
		fillResultArray(left, numberOfThreads, leftResults);

		int countThreads = numberOfThreads;
		final Iterator<? extends Join> iterator = operators.iterator();
		for (int i = 0; i < rightResults.length; i++) {
			final Join nextOperator = iterator.next();
			if (!(rightResults[i].isEmpty() || leftResults[i].isEmpty())) {
				// if the list of the binding is not empty
				// count the number of threads
				final ParallelJoiner parallelJoin = new ParallelJoiner(
						leftResults[i], rightResults[i], nextOperator);
				// add the Therads to the list
				threadList.add(parallelJoin);
			} else {
				countThreads--;
			}
		}
		// System.out.println("ParallelJoin.join()nr Threads " + numberOfThreads
		// + " left" + Arrays.toString(leftResults) + " right "
		// + Arrays.toString(rightResults));

		// before the Threads start, deliver the number of threads to the
		// resultCollector
		col.setNumberOfThreads(countThreads);
		// start all Threads
		for (int i = 0; i < countThreads; i++) {
			threadList.get(i).start();
		}

		return col.getResult();
	}

	/**
	 * 
	 * Fill the result array with the bindings out of the lists.
	 * 
	 * @param source
	 *            a list with query results
	 * @param numberOfThreads
	 *            how many threads do we use
	 * @param destResults
	 *            the array with results
	 */
	private void fillResultArray(final List<QueryResult> source,
			final int numberOfThreads, final QueryResult[] destResults) {
		for (final QueryResult queryResult : source) {
			for (final Bindings b : queryResult) {
				if (b != null) {
					final long key = hashFun.hash(hashFun.getKey(b,
							intersectionVariables));
					final int resultNumber = (int) (key % numberOfThreads);
					destResults[resultNumber].add(b);
				}
			}
		}
	}

	/**
	 * A wrapper for an join operator what processes parallel the join between
	 * two tables.
	 * 
	 * @see Thread
	 */
	private class ParallelJoiner extends Thread {

		/**
		 * the left table
		 */
		private final QueryResult left;

		/**
		 * the right table
		 */
		private final QueryResult right;

		/**
		 * an join operator what processes parallel the join between two tables.
		 */
		private final Join joiner;

		/**
		 * Constructs a wrapper for an join operator what processes parallel the
		 * join between two tables.
		 * 
		 * @param left
		 *            the left table
		 * @param right
		 *            the right table
		 * @param joinOperator
		 */
		public ParallelJoiner(final QueryResult left, final QueryResult right,
				final Join joinOperator) {
			this.left = left;
			this.right = right;
			this.joiner = joinOperator;
		}

		@Override
		public void run() {

			joiner.setIntersectionVariables(intersectionVariables);
			joiner.process(left, LEFT);
			// System.out.println("ParallelJoiner.run()leftsize: " + left.size()
			// + " rightsize: " + right.size());
			final QueryResult res = joiner.process(right, RIGHT);
			if (res == null) {// force
				endOfStreamMsg = (EndOfEvaluationMessage) joiner
						.preProcessMessage(endOfStreamMsg);
			} else {//
				col.process(res, LEFT);
			}
			col.incNumberOfThreads();
		}
	}

}
