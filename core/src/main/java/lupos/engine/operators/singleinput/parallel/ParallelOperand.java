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
package lupos.engine.operators.singleinput.parallel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;

/**
 * A parallel operator implementation which distributes the computation of
 * previous to a helper thread. Uses a {@link BlockingQueue} to communicate with
 * it.
 * 
 **/
public class ParallelOperand extends Operator {

	protected List<Thread> threadsList = null;

	/**
	 * Maximum number of elements in the queue.
	 */
	private static int queueLimit = 1000;

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

	/**
	 * Processes a QueryResult; in our case, gets values from the {@link #queue}
	 * and returns them. Its enclosed in an iterator, so we actually transfer it
	 * only when necessary (using {@link Iterator#next}).
	 * 
	 * Since we call a {@link BlockingQueue}, we might experience
	 * {@link InterruptedException}s, but that's not visible from the signature.
	 * Actually we circumvent this by rethrowing using {@link Thread#interrupt}
	 * on the current thread.
	 * 
	 * @see QueryResult
	 * @see Iterator
	 * @see BlockingQueue
	 * @see Thread
	 */
	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {
		final BoundedBuffer<Bindings> queue = new BoundedBuffer<Bindings>(
				queueLimit);
		final Thread starterThread = new ParallelOperandStarter(queue);
		starterThread.start();
		if (this.threadsList == null) {
			this.threadsList = new LinkedList<Thread>();
		}
		this.threadsList.add(starterThread);

		final Thread thread = new ParallelOperandThread(queue, queryResult);

		thread.start();

		return null;
	}

	protected Semaphore sem = new Semaphore(0);

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		// wait until all threads are finished!
		if (this.threadsList != null)
			for (final Thread t : this.threadsList) {
				try {
					t.join();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		return super.preProcessMessage(msg);
	}

	/**
	 * The helper thread for {@link ParallelOperand}. Uses a
	 * {@link BlockingQueue} to communicate with the main thread.
	 * 
	 * @see ParallelOperand
	 * @see BlockingQueue
	 * 
	 * @author Olof-Joachim Frahm, Yu Huang, Christian Wolters
	 */
	public class ParallelOperandThread extends Thread {
		/**
		 * @see ParallelOperand#queue
		 */
		private final BoundedBuffer<Bindings> queue;

		/**
		 * The result we evaluate in this thread.
		 * 
		 * @see ParallelOperand#process
		 */
		private final QueryResult result;

		/**
		 * Constructs a new thread object.
		 * 
		 * @see ParallelOperand#process
		 */
		ParallelOperandThread(final BoundedBuffer<Bindings> queue,
				final QueryResult queryResult) {
			super("ParallelOperandThread");
			this.queue = queue;
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
					this.queue.put(bindings);
				} catch (final InterruptedException e) {
					System.err
							.println("ParallelOperandThread.run: interrupted");
					this.queue.endOfData();
					return;
				}
			}

			System.out.println("thread  stopped!");

			this.queue.endOfData();
		}
	}

	public class ParallelOperandStarter extends Thread {
		/**
		 * @see ParallelOperand#queue
		 */
		private final BoundedBuffer<Bindings> queue;

		/**
		 * Constructs a new thread object.
		 * 
		 * @see ParallelOperand#process
		 */
		ParallelOperandStarter(final BoundedBuffer<Bindings> queue) {
			super("ParallelOperandStarter");
			this.queue = queue;
			ParallelOperand.this.sem.release();
		}

		@Override
		public void run() {
			final QueryResult qr = QueryResult.createInstance(this.queue);
			if (ParallelOperand.this.succeedingOperators.size() > 1) {
				System.err.println("Error ParallelOperandStarter: More than one succeeding operator!");
			}
			for (final OperatorIDTuple oid : ParallelOperand.this.succeedingOperators) {
				oid.processAll(qr);
			}
			try {
				ParallelOperand.this.sem.acquire();
			} catch (final InterruptedException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}
	}
}
