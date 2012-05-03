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

	private QueryResult left = null;

	private QueryResult right = null;

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
		hashFun = new HashFunction();
		threadList = new ArrayList<ParallelJoiner>(operators.size());
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
	public synchronized QueryResult process(final QueryResult input,
			final int operatorID) {
		switch (operatorID) {
		case LEFT:
			if (left != null)
				left.add(input);
			else
				left = input;
			break;
		case RIGHT:
			if (right != null)
				right.add(input);
			else
				right = input;
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
		if (left != null && right != null) {
			final QueryResult qr = join();
			if (qr != null) {
				if (succeedingOperators.size() > 1)
					qr.materialize();
				for (final OperatorIDTuple opId : succeedingOperators) {
					opId.processAll(qr);
				}
			}
		}
		if (left != null)
			left.release();
		if (right != null)
			right.release();
		left = null;
		right = null;
		return msg;
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		endOfStreamMsg = msg;
		if (left != null && right != null) {
			final QueryResult qr = join();
			if (qr != null) {
				if (succeedingOperators.size() > 1)
					qr.materialize();
				for (final OperatorIDTuple opId : succeedingOperators) {
					final QueryResultDebug qrDebug = new QueryResultDebug(qr,
							debugstep, this, opId.getOperator(), true);
					((Operator) opId.getOperator()).processAllDebug(qrDebug,
							opId.getId(), debugstep);
				}
			}
		}
		if (left != null)
			left.release();
		if (right != null)
			right.release();
		left = null;
		right = null;
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
		operators.size()
		/*
		 * , Math.min(left // this forces the query results to materialize => do
		 * not do this! .size(), right.size()))
		 */;
		threadList.clear();

		// if nothing to join
		if (numberOfThreads == 0) {
			return null;
		}
		// create Queryresults

		// relate each binding to one of the right results
		// the key of the hashfunktion should be in the bound of the
		// Threadsnumber
		// every arraycell has a list of bindings
		final QueryResult[] leftResults = prepareResultArray(numberOfThreads,
				left);
		final QueryResult[] rightResults = prepareResultArray(numberOfThreads,
				right);

		int countThreads = 0;
		final Iterator<? extends MultiInputOperator> iterator = operators
				.iterator();
		if (optional) {
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
			if (optional) {
				if (rightResults[i].size() == 0) {
					if (leftResults[i].size() > 0)
						col.process(leftResults[i], LEFT);
					continue;
				}
			}
			// if the list of the binding is not empty
			// count the number of threads
			countThreads++;
			final ParallelJoiner parallelJoin = new ParallelJoiner(
					leftResults[i], rightResults[i], nextOperator);
			// add the Therads to the list
			threadList.add(parallelJoin);
		}
		// before the Threads start, deliver the number of threads to the
		// resultCollector
		col.setNumberOfThreads(countThreads);
		// start all Threads
		// System.out.println("Paralleljoin().preprocessMs countThreads: "
		// + countThreads);
		for (int i = 0; i < countThreads; i++) {
			threadList.get(i).start();
		}
		return col.getResult();
	}

	// private QueryResult[] prepareResultArray(final int numberOfThreads,
	// final QueryResult source) {
	// final QueryResult[] res = new QueryResult[numberOfThreads];
	//
	// final Iterator<Bindings> itb = source.oneTimeIterator();
	// while (itb.hasNext()) {
	// final Bindings b = itb.next();
	// final long key = hashFun.hash(hashFun.getKey(b,
	// intersectionVariables));
	// final int resultNumber = (int) (key % numberOfThreads);
	// if (res[resultNumber] == null)
	// res[resultNumber] = QueryResult.createInstance();
	// res[resultNumber].add(b);
	// }
	// return res;
	// }

	private QueryResult[] prepareResultArray(final int numberOfThreads,
			final QueryResult source) {

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
			return joiner;
		}

		@Override
		public void run() {
			joiner.setIntersectionVariables(intersectionVariables);
			joiner.process(left, LEFT);
			// System.out.println("ParallelJoiner.run()leftsize: " + left.size()
			// + " rightsize: " + right.size());
			final QueryResult res = joiner.process(right, RIGHT);
			if (res != null) {
				col.process(res, LEFT);
			}
			endOfStreamMsg = (EndOfEvaluationMessage) joiner
					.preProcessMessage(endOfStreamMsg);
			col.incNumberOfThreads();
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
			final long numberOfThreads = b.length;
			final BoundedBuffer<Bindings>[] leftBuff = b;
			final Iterator<Bindings> leftIter = qres.oneTimeIterator();
			// share the rest of the bindings
			while (leftIter.hasNext()) {// share left
				final Bindings b = leftIter.next();
				if (b != null) {
					final long key = hashFun.hash(hashFun.getKey(b,
							intersectionVariables));
					final int resultNumber = (int) (key % numberOfThreads);
					leftBuff[resultNumber].put(b);
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
			qres.materialize();
		}
	}
}
