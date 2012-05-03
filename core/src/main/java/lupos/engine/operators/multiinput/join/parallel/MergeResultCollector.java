package lupos.engine.operators.multiinput.join.parallel;

import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.multiinput.MergeUnionIterator;

public class MergeResultCollector extends ResultCollector {

	/**
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public QueryResult process(final QueryResult res, final int arg1) {
		lock.lock();
		try {
			resultList.add(res);
		} finally {
			lock.unlock();
		}
		return null;
	}

	@Override
	public QueryResult getResult() {
		waitForAllThreads();
		return QueryResult.createInstance(new MergeUnionIterator(resultList,
				false, intersectionVariables));
	}

	@Override
	public void incNumberOfThreads() {
		lock.lock();
		try {
			countThreads++;
			if (countThreads == numberOfThreads) {
				insertCondition.signalAll();
			}
		} finally {
			lock.unlock();
		}

	}

}
