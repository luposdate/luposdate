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
