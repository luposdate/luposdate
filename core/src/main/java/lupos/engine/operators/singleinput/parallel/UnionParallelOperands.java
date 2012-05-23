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
package lupos.engine.operators.singleinput.parallel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;

public class UnionParallelOperands extends Operator {

	private static final long serialVersionUID = 7906232457715819851L;

	private static ThreadGroup toJoinBeforeNewQuery = new ThreadGroup(
			"toJoinBeforeNewQuery");

	protected List<Thread> threadsList = null;

	/**
	 * Maximum number of elements in the queue.
	 */
	private static int queueLimit = 1000;

	final BoundedBuffer<Bindings> queue = new BoundedBuffer<Bindings>(
			queueLimit);

	/**
	 * Queries the current global queue fill limit.
	 * 
	 * @return the current queue fill limit
	 */
	public static int getQueueLimit() {
		return queueLimit;
	}

	/**
	 * Sets the current global queue limit.
	 * 
	 * @param maximum
	 *            number of elements in the queue
	 */
	public static void setQueueLimit(final int limit) {
		queueLimit = limit;
	}

	public static void waitForJoinThreads() {
		final Thread[] threads = new Thread[UnionParallelOperands.toJoinBeforeNewQuery
				.activeCount()];
		UnionParallelOperands.toJoinBeforeNewQuery.enumerate(threads, false);
		for (final Thread t : threads) {
			// try {
			t.interrupt();
			// t.join();
			// } catch (final InterruptedException e) {
			// System.err.println();
			// e.printStackTrace();
			// }
		}

	}

	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {

		final Thread thread = new ParallelOperandThread(queryResult);

		if (threadsList == null) {
			threadsList = new LinkedList<Thread>();
		}
		threadsList.add(thread);
		thread.start();

		return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		final QueryResult qr = QueryResult.createInstance(queue);
		final Thread endOfDataThread = new Thread(toJoinBeforeNewQuery,
				"joinThread") {
			@Override
			public void run() {
				// wait until all threads are finished!
				if (threadsList != null)
					for (final Thread t : threadsList) {
						try {
							t.join();
						} catch (final InterruptedException e) {
							System.out.println("thread interrrupted");
							// System.err.println(e);
							// e.printStackTrace();
						}
					}
				queue.endOfData();
			}
		};
		endOfDataThread.start();
		if (succeedingOperators.size() > 1) {
			qr.materialize();
		}
		for (final OperatorIDTuple oid : succeedingOperators) {
			oid.processAll(qr);
		}
		return super.preProcessMessage(msg);
	}

	public class ParallelOperandThread extends Thread {

		/**
		 * The result we evaluate in this thread.
		 * 
		 * @see UnionParallelOperands#process
		 */
		private final QueryResult result;

		/**
		 * Constructs a new thread object.
		 * 
		 * @see UnionParallelOperands#process
		 */
		ParallelOperandThread(final QueryResult queryResult) {
			super(toJoinBeforeNewQuery,
					"ParallelOperandThread of UnionParallelOperands");
			this.result = queryResult;
		}

		/**
		 * Method run when this thread is started. Evaluates the bindings in the
		 * {@link #result} and puts them on the {@link #queue}.
		 * 
		 * If it's interrupted, we don't do much cleaning up, so better not use
		 * the {@link #queue} afterwards (there might not be an end marker, so
		 * you might wait forever to get an element from it.
		 */
		@Override
		public void run() {
			System.out.println("thread started...");

			final Iterator<Bindings> it = this.result.oneTimeIterator();
			Bindings bindings;

			while (it.hasNext()) {
				bindings = it.next();
				try {
					queue.put(bindings);
				} catch (final InterruptedException e) {
					System.err
							.println("ParallelOperandThread.run: interrupted");
					queue.endOfData();
					return;
				}
			}

			System.out.println("thread  stopped!");
		}
	}
}
