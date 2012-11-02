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

	protected final Condition insertCondition = this.lock.newCondition();

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
		this.lock.lock();
		try {
			this.resultList.add(res);
			this.insertCondition.signalAll();
		} finally {
			this.lock.unlock();
		}
		return null;
	}

	public QueryResult getResult() {
		return QueryResult.createInstance(this);
	}

	public void incNumberOfThreads() {
		this.lock.lock();
		try {
			this.countThreads++;
			this.insertCondition.signalAll();
		} finally {
			this.lock.unlock();
		}
	}

	public void setNumberOfThreads(final int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
		// does not work for optional: resultList.clear();
		this.currentIter = null;
		this.countThreads = 0;
	}

	/**
	 * If the list is empty and not all threads are ready, wait. If all threads
	 * are done, move to the last element.
	 */
	@Override
	public boolean hasNext() {
		if (this.currentIter != null && this.currentIter.hasNext()) {
			// if the iterator from the current QS is not empty
			return true;
		}

		// if the iterator from the current QS is empty and jet QS are
		// available renew the currentIter

		this.lock.lock();
		try {
			// list is empty look for new threads
			while (this.countThreads < this.numberOfThreads || !this.resultList.isEmpty()) {
				// list is empty => wait and let the Threads proceed
				while (this.resultList.isEmpty() && this.countThreads < this.numberOfThreads) {
					try {
						this.insertCondition.await();
					} catch (final InterruptedException ignore) { // ignore exception
					}
				}

				// now another result is available
				while (!this.resultList.isEmpty()) {
					// take the first element
					final QueryResult res = this.resultList.remove(0);
					if (res != null) {
						this.currentIter = res.oneTimeIterator();
						if (this.currentIter.hasNext()) {
							return true;
						}
					}
				}
			}
		} finally {
			this.lock.unlock();
		}
		return false;
	}

	@Override
	public Bindings next() {
		if (hasNext()) {
			return this.currentIter.next();
		} else {
			return null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"can not remove from QueryResults");
	}

	protected void waitForAllThreads() {
		this.lock.lock();
		try {
			while (this.countThreads < this.numberOfThreads) {
				try {
					this.insertCondition.await();
				} catch (final InterruptedException e1) { // ignore exception
				}

			}
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public void close() {
		waitForAllThreads();
	}

	@Override
	public void finalize() {
		close();
	}
}
