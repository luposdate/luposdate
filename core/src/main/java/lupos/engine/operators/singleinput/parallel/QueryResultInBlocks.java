package lupos.engine.operators.singleinput.parallel;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.parallel.BoundedBuffer;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;

public class QueryResultInBlocks extends Operator {

	protected final static int MAXBUFFER = 100;
	protected static int BLOCKSIZE;

	protected Runner runner = null;
	protected BoundedBuffer<QueryResult> queryresultbuffer = new BoundedBuffer<QueryResult>(
			MAXBUFFER);

	public QueryResultInBlocks() {
		BLOCKSIZE = ParallelOperand.getQueueLimit();
	}

	@Override
	public QueryResult process(final QueryResult queryResult,
			final int operandID) {
		try {
			queryresultbuffer.put(queryResult);
		} catch (final InterruptedException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		if (runner == null) {
			runner = new Runner();
			runner.start();
		}
		return null;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		queryresultbuffer.endOfData();
		// wait until thread finished!
		if (runner != null)
			try {
				runner.join();
			} catch (final InterruptedException e) {
				System.err.println();
				e.printStackTrace();
			}
		return msg;
	}

	public class Runner extends Thread {

		@Override
		public void run() {
			try {
				while (queryresultbuffer.hasNext()) {
					final QueryResult queryResult = queryresultbuffer.get();
					final Iterator<Bindings> ib = queryResult.oneTimeIterator();
					while (ib.hasNext()) {
						final QueryResult queryresult_new = QueryResult
								.createInstance();
						for (int i = 0; i < BLOCKSIZE && ib.hasNext(); i++) {
							queryresult_new.add(ib.next());
						}
						for (final OperatorIDTuple opId : succeedingOperators) {
							opId.processAll(queryresult_new);
						}
					}
				}
			} catch (final InterruptedException e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

}
