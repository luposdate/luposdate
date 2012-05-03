package lupos.engine.operators.multiinput.join.parallel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIterator;
import lupos.datastructures.queryresult.QueryResult;
import lupos.engine.operators.Operator;

public class ResultCollector extends Operator implements
		ParallelIterator<Bindings> {

	private static final long serialVersionUID = 1L;

	protected final Lock lock = new ReentrantLock();

	protected final Condition insertCondition = lock.newCondition();

	protected int numberOfThreads;

	protected int countThreads;

	// protected QueryResult[] resultArray;
	protected List<QueryResult> resultList = new ArrayList<QueryResult>();

	private Iterator<Bindings> currentIter;

	// /**
	// * fuer die Liste
	// */
	// private Iterator<QueryResult> listIter;

	@Override
	public QueryResult process(final QueryResult res, final int arg1) {
		lock.lock();
		try {
			resultList.add(res);
			insertCondition.signalAll();
		} finally {
			lock.unlock();
		}
		return null;
	}

	public QueryResult getResult() {
		return QueryResult.createInstance(this);
	}

	public void incNumberOfThreads() {
		lock.lock();
		try {
			countThreads++;
			insertCondition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public void setNumberOfThreads(final int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
		// does not work for optional: resultList.clear();
		currentIter = null;
		countThreads = 0;
	}

	/**
	 * If the list is empty and not all threads are ready, wait. If all threads
	 * are done, move to the last element.
	 */
	public boolean hasNext() {
		if (currentIter != null && currentIter.hasNext()) {
			// if the iterator from the current QS is not empty
			return true;
		}

		// if the iterator from the current QS is empty and jet QS are
		// available renew the currentIter

		lock.lock();
		try {
			// list is empty look for new threads
			while (countThreads < numberOfThreads || !resultList.isEmpty()) {
				// list is empty => wait and let the Threads proceed
				while (resultList.isEmpty() && countThreads < numberOfThreads) {
					try {
						insertCondition.await();
					} catch (final InterruptedException ignore) {
					}
				}

				// now another result is available
				while (!resultList.isEmpty()) {
					// take the first element
					final QueryResult res = resultList.remove(0);
					if (res != null) {
						currentIter = res.oneTimeIterator();
						if (currentIter.hasNext()) {
							return true;
						}
					}
				}
			}
		} finally {
			lock.unlock();
		}
		return false;
	}

	public Bindings next() {
		if (hasNext()) {
			return currentIter.next();
		} else {
			return null;
		}
	}

	public void remove() {
		throw new UnsupportedOperationException(
				"can not remove from QueryResults");
	}

	protected void waitForAllThreads() {
		lock.lock();
		try {
			while (countThreads < numberOfThreads) {
				try {
					insertCondition.await();
				} catch (final InterruptedException e1) {
				}

			}
		} finally {
			lock.unlock();
		}
	}

	public void close() {
		waitForAllThreads();
	}

	@Override
	public void finalize() {
		close();
	}
}
