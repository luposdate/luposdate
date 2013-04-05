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
package lupos.engine.operators.singleinput.sort;

/**
 * This class is a subclass of Sort. It realsies sorting by using the quicksort algorithm
 */

import java.util.Iterator;
import java.util.Random;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;

public class QuickSort extends CollectionSort {

	// Here all incoming bindings are stored
	protected ParallelIteratorMultipleQueryResults _bindings = new ParallelIteratorMultipleQueryResults();

	public QuickSort(final lupos.sparql1_1.Node node) {
		super(node);
	}

	public QuickSort() {
		// nothing to init...
	}

	/**
	 * simply adds bindings to storage (_bindings)
	 * 
	 * @return always null
	 */
	@Override
	protected QueryResult postProcess(final QueryResult queryResult, final int id) {
		this._bindings.addQueryResult(queryResult);
		return null;
	}

	/**
	 * This Method checks if the incoming QueryResult is already sorted. If not
	 * out_postProcess will be called to order the given QueryResult and its
	 * results will be returned
	 * 
	 * @param bindings
	 *            , QueryResult to check
	 * @return the correctly sorted QueryResult
	 */
	protected QueryResult checkIfAlreadySorted(final QueryResult bindings) {
		// check if already sorted
		boolean b = true;
		for (int i = 0; i < bindings.size() - 1; i++) {
			if (this.comparator.compare(bindings.get(i), bindings.get(i + 1)) > 0) {
				b = false;
				break;
			}
		}
		if (b) {
			return bindings;
		} else {
			return out_postProcess(bindings, bindings.size());
		}
	}

	/**
	 * Implementation of the quicksort algorithm
	 * 
	 * @param bindings
	 * @return the given QueryResult in correct order
	 */
	protected QueryResult out_postProcess(final QueryResult bindings, int size) {

		if (bindings.size() == 1) {
			return bindings;
		} else if (bindings.size() == 2) {
			final QueryResult ret = getQR();
			final Iterator<Bindings> iter = bindings.iterator();
			final Bindings b1 = iter.next();
			final Bindings b2 = iter.next();
			if (this.comparator.compare(b1, b2) <= 0) {
				ret.add(b1);
				ret.add(b2);
				return ret;
			} else {
				ret.add(b2);
				ret.add(b1);
				return ret;
			}
		}

		QueryResult _LowEnd = getQR();
		QueryResult _HighEnd = getQR();
		final Bindings pivot = bindings.get(new Random().nextInt(bindings
				.size()));

		final Iterator<Bindings> it = bindings.iterator();
		while (it.hasNext()) {

			final Bindings nextB = it.next();

			if (this.comparator.compare(pivot, nextB) <= 0) {
				_HighEnd.add(nextB);
			} else {
				_LowEnd.add(nextB);
			}
		}

		if (checkOnDoubleEntry(_LowEnd, _HighEnd)) {
			System.out.println(_LowEnd);
			System.out.println(_HighEnd);
		}

		if (_LowEnd.size() != 0) {
			if (_LowEnd.size() == size) {
				if (checkIfSorted(_LowEnd)) {
					return _LowEnd;
				}
			}
			_LowEnd = out_postProcess(_LowEnd, size);
		}
		if (_HighEnd.size() != 0) {
			if (_HighEnd.size() == size) {
				if (checkIfSorted(_HighEnd)) {
					return _HighEnd;
				}
			}
			_HighEnd = out_postProcess(_HighEnd, size);
		}

		_LowEnd.addAll(_HighEnd);
		// System.out.println( checkIfSorted( _LowEnd ) );
		// System.out.println( _LowEnd );
		return _LowEnd;
	}

	protected boolean checkIfSorted(final QueryResult bindings) {
		boolean b = true;
		for (int i = 0; i < bindings.size() - 1; i++) {
			if (this.comparator.compare(bindings.get(i), bindings.get(i + 1)) > 0) {
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		QueryResult result = checkIfAlreadySorted(this._bindings.getQueryResult());
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			opId.processAll(result);
		}
		this._bindings = new ParallelIteratorMultipleQueryResults();
		return msg;
	}

	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		preProcessMessage(new EndOfEvaluationMessage());
		return msg;
	}

	private boolean checkOnDoubleEntry(final QueryResult bindings0,
			final QueryResult bindings1) {
		for (int i = 0; i < bindings1.size(); i++) {
			if (bindings0.contains(bindings1.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
		this._bindings.removeAll(queryResult);
		return null;
	}

	@Override
	public void deleteAll(final int operandID) {
		this._bindings.release();
		this._bindings = new ParallelIteratorMultipleQueryResults();	
	}

	@Override
	protected boolean isPipelineBreaker() {
		return true;
	}
	
	@Override
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return msg;
	}
	
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		QueryResult result = checkIfAlreadySorted(this._bindings.getQueryResult());
		for (final OperatorIDTuple opId : this.succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(result,debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
		}
		this._bindings = new ParallelIteratorMultipleQueryResults();
		return msg;
	}
}
