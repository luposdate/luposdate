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
		if (threadsList == null) {
			threadsList = new LinkedList<Thread>();
		}
		threadsList.add(starterThread);

		final Thread thread = new ParallelOperandThread(queue, queryResult);

		thread.start();

		return null;
	}

	protected Semaphore sem = new Semaphore(0);

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		// wait until all threads are finished!
		if (threadsList != null)
			for (final Thread t : threadsList) {
				try {
					t.join();
				} catch (final InterruptedException e) {
					System.err.println(e);
					e.printStackTrace();
				}
			}
		return super.preProcessMessage(msg);
	}

	// @Override
	// public Message preProcessMessage(final EndOfStreamMessage msg) {
	// final Message msg2 = super.preProcessMessage(msg);
	// // wait until all threads are finished!
	// for (final Thread t : threadsList) {
	// try {
	// t.join();
	// } catch (final InterruptedException e) {
	// System.err.println(e);
	// e.printStackTrace();
	// }
	// }
	// return msg2;
	// }

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
					queue.put(bindings);
				} catch (final InterruptedException e) {
					System.err
							.println("ParallelOperandThread.run: interrupted");
					queue.endOfData();
					return;
				}
			}

			System.out.println("thread  stopped!");

			queue.endOfData();
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
			sem.release();
		}

		@Override
		public void run() {
			final QueryResult qr = QueryResult.createInstance(queue);
			if (succeedingOperators.size() > 1) {
				System.err
						.println("Error ParallelOperandStarter: More than one succeeding operator!");
			}
			for (final OperatorIDTuple oid : succeedingOperators) {
				oid.processAll(qr);
			}
			try {
				sem.acquire();
			} catch (final InterruptedException e1) {
				System.err.println(e1);
				e1.printStackTrace();
			}
		}
	}
}
