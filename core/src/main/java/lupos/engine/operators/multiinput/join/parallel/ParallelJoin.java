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
/**
 * 
 */
package lupos.engine.operators.multiinput.join.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.join.HashFunction;
import lupos.engine.operators.multiinput.join.Join;
import lupos.misc.debug.DebugStep;

public class ParallelJoin extends Join {

	// contains all ParallelJoiner-threads
	private static ThreadGroup toJoinBeforeNewQuery = new ThreadGroup(
			"toJoinBeforeNewQuery");
	// the number of ParallelJoiner-threads started
	private static int number = 0;

	private static final long serialVersionUID = 1L;

	protected static int DEFAULT_NUMBER_THREADS = 8;

	protected static int MAXBUFFER = 50;

	private static final int LEFT = 0;

	private static final int RIGHT = 1;

	protected ParallelIteratorMultipleQueryResults[] operands = {	new ParallelIteratorMultipleQueryResults(),
																	new ParallelIteratorMultipleQueryResults()};

	protected final Collection<? extends MultiInputOperator> operators;

	protected ResultCollector col;

	private EndOfEvaluationMessage endOfStreamMsg;

	private final HashFunction hashFun;

	private final List<ParallelJoiner> threadList;

	private final boolean optional;

	public ParallelJoin(
			final Collection<? extends MultiInputOperator> operators,
			final boolean optional) {
		this(operators, new ResultCollector(), optional);
	}

	public ParallelJoin(
			final Collection<? extends MultiInputOperator> operators,
			final ResultCollector col, final boolean optional) {
		this.operators = operators;
		this.col = col;
		for (final MultiInputOperator join : operators) {
			join.setSucceedingOperator(new OperatorIDTuple(col, 0));
		}
		this.hashFun = new HashFunction();
		this.threadList = new ArrayList<ParallelJoiner>(operators.size());
		this.optional = optional;
	}

	public static void waitForJoinThreads() {
		final Thread[] threads = new Thread[ParallelJoin.toJoinBeforeNewQuery
				.activeCount()];
		ParallelJoin.toJoinBeforeNewQuery.enumerate(threads, false);
		for (final Thread t : threads) {
			try {
				t.join();
			} catch (final InterruptedException e) {
				System.err.println();
				e.printStackTrace();
			}
		}

	}

	@Override
	public synchronized QueryResult process(final QueryResult queryResult,
			final int operatorID) {
		this.operands[operatorID].addQueryResult(queryResult);
		return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		this.endOfStreamMsg = msg;
		if (!this.operands[0].isEmpty() && !this.operands[1].isEmpty()) {
			final QueryResult qr = join();
			if (qr != null) {
				if (this.succeedingOperators.size() > 1)
					qr.materialize();
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					opId.processAll(qr);
				}
			}
		}
		this.operands[0].release();
		this.operands[1].release();
		this.operands[0] = new ParallelIteratorMultipleQueryResults();
		this.operands[1] = new ParallelIteratorMultipleQueryResults();
		return msg;
	}

	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg, final DebugStep debugstep) {
		this.endOfStreamMsg = msg;
		if (!this.operands[0].isEmpty() && !this.operands[1].isEmpty()) {
			final QueryResult qr = join();
			if (qr != null) {
				if (this.succeedingOperators.size() > 1)
					qr.materialize();
				for (final OperatorIDTuple opId : this.succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(qr,
							debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug,
							opId.getId(), debugstep);
				}
			}
		}
		this.operands[0].release();
		this.operands[1].release();
		this.operands[0] = new ParallelIteratorMultipleQueryResults();
		this.operands[1] = new ParallelIteratorMultipleQueryResults();
		return msg;
	}

	/**
	 * the actual join
	 */
	protected QueryResult join() {
		// compute intersection variables
		// number of threads is minimum of the number of operators or left or
		// right size

		final int numberOfThreads = /* Math.min( */
		this.operators.size()
		/*
		 * , Math.min(left // this forces the query results to materialize => do
		 * not do this! .size(), right.size()))
		 */;
		this.threadList.clear();

		// if nothing to join
		if (numberOfThreads == 0) {
			return null;
		}
		// create Queryresults

		// relate each binding to one of the right results
		// the key of the hashfunktion should be in the bound of the
		// Threadsnumber
		// every arraycell has a list of bindings
		final QueryResult[] leftResults = prepareResultArray(numberOfThreads, this.operands[0].getQueryResult());
		final QueryResult[] rightResults = prepareResultArray(numberOfThreads, this.operands[1].getQueryResult());

		int countThreads = 0;
		final Iterator<? extends MultiInputOperator> iterator = this.operators.iterator();
		if (this.optional) {
			// optional anyway materializes its queryresults
			// so do this before, because we want to know the number of results!
			final QueryResultMaterializer[] qrm_right = new QueryResultMaterializer[rightResults.length];
			final QueryResultMaterializer[] qrm_left = new QueryResultMaterializer[leftResults.length];
			for (int i = 0; i < rightResults.length; i++) {
				qrm_right[i] = new QueryResultMaterializer(rightResults[i]);
				qrm_right[i].start();
				qrm_left[i] = new QueryResultMaterializer(leftResults[i]);
				qrm_left[i].start();
			}
			for (int i = 0; i < rightResults.length; i++) {
				try {
					qrm_right[i].join();
					qrm_left[i].join();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		}
		for (int i = 0; i < rightResults.length; i++) {
			final MultiInputOperator nextOperator = iterator.next();
			if (this.optional) {
				if (rightResults[i].size() == 0) {
					if (leftResults[i].size() > 0)
						this.col.process(leftResults[i], LEFT);
					continue;
				}
			}
			// if the list of the binding is not empty
			// count the number of threads
			countThreads++;
			final ParallelJoiner parallelJoin = new ParallelJoiner(
					leftResults[i], rightResults[i], nextOperator);
			// add the Therads to the list
			this.threadList.add(parallelJoin);
		}
		// before the Threads start, deliver the number of threads to the
		// resultCollector
		this.col.setNumberOfThreads(countThreads);
		// start all Threads
		// System.out.println("Paralleljoin().preprocessMs countThreads: "
		// + countThreads);
		for (int i = 0; i < countThreads; i++) {
			this.threadList.get(i).start();
		}
		return this.col.getResult();
	}

	private QueryResult[] prepareResultArray(final int numberOfThreads,
			final QueryResult source) {

		@SuppressWarnings("unchecked")
		final BoundedBuffer<Bindings>[] buffer = new BoundedBuffer[numberOfThreads];

		final QueryResult[] res = new QueryResult[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++) {
			buffer[i] = new BoundedBuffer<Bindings>(MAXBUFFER);
			res[i] = QueryResult.createInstance(buffer[i]);
		}

		new BindingsSharerThread(buffer, source).start();
		return res;
	}

	public static int getDEFAULT_NUMBER_THREADS() {
		return DEFAULT_NUMBER_THREADS;
	}

	public static void setDEFAULT_NUMBER_THREADS(
			final int default_number_threads) {
		DEFAULT_NUMBER_THREADS = default_number_threads;
	}

	public static int getMAXBUFFER() {
		return MAXBUFFER;
	}

	public static void setMAXBUFFER(final int maxbuffer) {
		MAXBUFFER = maxbuffer;
	}

	protected class ParallelJoiner extends Thread {

		private final QueryResult left;

		private final QueryResult right;

		private final MultiInputOperator joiner;

		public ParallelJoiner(final QueryResult left, final QueryResult right,
				final MultiInputOperator joinOperator) {
			super(toJoinBeforeNewQuery, "ParallelJoiner" + (number++));
			this.left = left;
			this.right = right;
			this.joiner = joinOperator;
		}

		public MultiInputOperator getJoiner() {
			return this.joiner;
		}

		@Override
		public void run() {
			this.joiner.setIntersectionVariables(ParallelJoin.this.intersectionVariables);
			this.joiner.process(this.left, LEFT);
			// System.out.println("ParallelJoiner.run()leftsize: " + left.size()
			// + " rightsize: " + right.size());
			final QueryResult res = this.joiner.process(this.right, RIGHT);
			if (res != null) {
				ParallelJoin.this.col.process(res, LEFT);
			}
			ParallelJoin.this.endOfStreamMsg = (EndOfEvaluationMessage) this.joiner.preProcessMessage(ParallelJoin.this.endOfStreamMsg);
			ParallelJoin.this.col.incNumberOfThreads();
			// System.out.println("ParallelJoiner.run() end");

		}
	}

	/**
	 * Shares the bindings to the threads
	 * 
	 */
	public class BindingsSharerThread extends Thread {

		private final BoundedBuffer<Bindings>[] b;

		private final QueryResult qres;

		/**
             * 
             */
		public BindingsSharerThread(final BoundedBuffer<Bindings>[] b,
				final QueryResult qres) {
			this.b = b;
			this.qres = qres;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				shareOut();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}

		}

		/**
		 * @throws InterruptedException
		 * 
		 */
		private void shareOut() throws InterruptedException {
			final long numberOfThreads = this.b.length;
			final BoundedBuffer<Bindings>[] leftBuff = this.b;
			final Iterator<Bindings> leftIter = this.qres.oneTimeIterator();
			// share the rest of the bindings
			while (leftIter.hasNext()) {// share left
				final Bindings bindings = leftIter.next();
				if (bindings != null) {
					final long key = ParallelJoin.this.hashFun.hash(HashFunction.getKey(bindings, ParallelJoin.this.intersectionVariables));
					final int resultNumber = (int) (key % numberOfThreads);
					leftBuff[resultNumber].put(bindings);
				}
			}
			// now all bindings are shared out!
			for (int i = 0; i < numberOfThreads; i++) {
				leftBuff[i].endOfData();
			}
		}
	}

	public class QueryResultMaterializer extends Thread {

		private final QueryResult qres;

		/**
             * 
             */
		public QueryResultMaterializer(final QueryResult qres) {
			this.qres = qres;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			this.qres.materialize();
		}
	}
}
