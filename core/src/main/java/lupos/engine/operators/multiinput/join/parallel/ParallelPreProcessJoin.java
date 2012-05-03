package lupos.engine.operators.multiinput.join.parallel;

import java.util.Collection;
import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.multiinput.MultiInputOperator;
import lupos.engine.operators.multiinput.join.HashFunction;
import lupos.misc.debug.DebugStep;

public class ParallelPreProcessJoin extends MultiInputOperator {

	private static final long serialVersionUID = 1L;

	protected static final int DEFAULT_NUMBER_THREADS = 8;

	private static final int LEFT = 0;

	private static final int RIGHT = 1;

	private QueryResult left;

	private QueryResult right;

	protected final Collection<? extends MultiInputOperator> operators;

	protected ResultCollector col;

	private EndOfEvaluationMessage endOfStreamMsg;

	private final HashFunction hashFun;

	private QueryResult[] leftResults = new QueryResult[0];

	private QueryResult[] rightResults = new QueryResult[0];

	public ParallelPreProcessJoin(
			final Collection<? extends MultiInputOperator> operators) {
		this(operators, new ResultCollector());
	}

	public ParallelPreProcessJoin(
			final Collection<? extends MultiInputOperator> operators,
			final ResultCollector col) {
		this.operators = operators;
		this.col = col;
		for (final MultiInputOperator join : operators) {
			join.setSucceedingOperator(new OperatorIDTuple(col, 0));
		}
		hashFun = new HashFunction();
		left = QueryResult.createInstance();
		right = QueryResult.createInstance();
	}

	@Override
	public QueryResult process(final QueryResult input, final int operatorID) {
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
				for (final OperatorIDTuple opId : succeedingOperators) {
					opId.processAll(qr);
				}
			}
		}
		if (left != null)
			left.release();
		if (right != null)
			right.release();
		left = QueryResult.createInstance();
		right = QueryResult.createInstance();
		return msg;
	}

	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		endOfStreamMsg = msg;
		if (left != null && right != null) {
			final QueryResult qr = join();
			if (qr != null) {
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
		left = QueryResult.createInstance();
		right = QueryResult.createInstance();
		return msg;
	}

	/**
	 * the actual join
	 */
	private QueryResult join() {
		// compute intersection variables
		// number of threads is minimum of the number of operators or left or
		// right size

		final int numberOfThreads = Math.min(operators.size(), Math.min(left
				.size(), right.size()));

		final Iterator<? extends MultiInputOperator> joinOperIter = operators
				.iterator();

		for (int id = 0; id < numberOfThreads; id++) {
			final MultiInputOperator joinOper = joinOperIter.next();
			final ParallelJoiner j = new ParallelJoiner(id, joinOper);
			j.start();
		}
		// if nothing to join
		if (numberOfThreads == 0) {
			return null;
		}
		col.setNumberOfThreads(numberOfThreads);
		// create Queryresults

		// relate each binding to one of the right results
		// the key of the hashfunktion should be in the bound of the
		// Threadsnumber
		// every arraycell has a list of bindings
		leftResults = prepareResultArray(numberOfThreads, left);
		rightResults = prepareResultArray(numberOfThreads, right);

		int countThreads = 0;
		final Iterator<? extends MultiInputOperator> iterator = operators
				.iterator();
		for (int i = 0; i < rightResults.length; i++) {
			final MultiInputOperator nextOperator = iterator.next();
			if (!(rightResults[i].isEmpty() || leftResults[i].isEmpty())) {
				// if the list of the binding is not empty
				// count the number of threads
				countThreads++;
				// add the Therads to the list
			}
		}
		// before the Threads start, deliver the number of threads to the
		// resultCollector

		// start all Threads
		// System.out.println("Paralleljoin().preprocessMs countThreads: "
		// + countThreads);
		// for (int i = 0; i < countThreads; i++) {
		// threadList.get(i).start();
		// }
		return col.getResult();
	}

	private QueryResult[] prepareResultArray(final int numberOfThreads,
			final QueryResult source) {
		final QueryResult[] res = new QueryResult[numberOfThreads];

		for (int i = 0; i < res.length; i++) {
			res[i] = QueryResult.createInstance();
		}

		for (final Bindings b : source.getCollection()) {
			if (b != null) {
				final long key = hashFun.hash(hashFun.getKey(b,
						intersectionVariables));
				final int resultNumber = (int) (key % numberOfThreads);
				res[resultNumber].add(b);
			}
		}
		return res;
	}

	protected class ParallelJoiner extends Thread {

		private final MultiInputOperator joiner;

		private final int id;

		public ParallelJoiner(final int id, final MultiInputOperator joinOper) {
			this.id = id;
			joiner = joinOper;
		}

		public MultiInputOperator getJoiner() {
			return joiner;
		}

		@Override
		public void run() {
			// ueberpruefen, ob neue QS links oder recht da sind und dann
			// process(..) aufrufen
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

}
