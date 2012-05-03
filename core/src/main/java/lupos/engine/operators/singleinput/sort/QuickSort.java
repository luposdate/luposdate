package lupos.engine.operators.singleinput.sort;

/**
 * This class is a subclass of Sort. It realsies sorting by using the quicksort algorithm
 */

import java.util.Iterator;
import java.util.Random;

import lupos.datastructures.bindings.Bindings;
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
	protected QueryResult _bindings = QueryResult.createInstance();

	public QuickSort(final lupos.sparql1_1.Node node) {
		super(node);
	}

	public QuickSort() {
	}

	/**
	 * simply adds bindings to storage (_bindings)
	 * 
	 * @return always null
	 */
	protected QueryResult postProcess(final QueryResult bindings, final int id) {
		_bindings.addAll(bindings);
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
			if (comparator.compare(bindings.get(i), bindings.get(i + 1)) > 0) {
				b = false;
				break;
			}
		}
		if (b) {
			return bindings;
		} else {
			return out_postProcess(bindings);
		}
	}

	/**
	 * Implementation of the quicksort algorithm
	 * 
	 * @param bindings
	 * @return the given QueryResult in correct order
	 */
	protected QueryResult out_postProcess(final QueryResult bindings) {

		if (bindings.size() == 1) {
			return bindings;
		} else if (bindings.size() == 2) {
			final QueryResult ret = getQR();
			final Iterator<Bindings> iter = bindings.iterator();
			final Bindings b1 = iter.next();
			final Bindings b2 = iter.next();
			if (comparator.compare(b1, b2) <= 0) {
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

			if (comparator.compare(pivot, nextB) <= 0) {
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
			if (_LowEnd.size() == _bindings.size()) {
				if (checkIfSorted(_LowEnd)) {
					return _LowEnd;
				}
			}
			_LowEnd = out_postProcess(_LowEnd);
		}
		if (_HighEnd.size() != 0) {
			if (_HighEnd.size() == _bindings.size()) {
				if (checkIfSorted(_HighEnd)) {
					return _HighEnd;
				}
			}
			_HighEnd = out_postProcess(_HighEnd);
		}

		_LowEnd.addAll(_HighEnd);
		// System.out.println( checkIfSorted( _LowEnd ) );
		// System.out.println( _LowEnd );
		return _LowEnd;
	}

	protected boolean checkIfSorted(final QueryResult bindings) {
		boolean b = true;
		for (int i = 0; i < bindings.size() - 1; i++) {
			if (comparator.compare(bindings.get(i), bindings.get(i + 1)) > 0) {
				b = false;
				break;
			}
		}
		return b;
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		_bindings = checkIfAlreadySorted(_bindings);
		for (final Bindings b : _bindings) {
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(b);
			}
		}
		_bindings = QueryResult.createInstance();
		return msg;
	}

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

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		// problem: it does not count the number of occurences of a binding
		// i.e. { ?a=<a> }, { ?a=<a> } and delete { ?a=<a> } will result in
		// {} instead of { ?a=<a> }!!!!!!
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext())
			_bindings.remove(itb.next());
		return null;
	}

	public void deleteAll(final int operandID) {
		_bindings.release();
		_bindings = QueryResult.createInstance();
	}

	protected boolean isPipelineBreaker() {
		return true;
	}
	
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		preProcessMessageDebug(new EndOfEvaluationMessage(), debugstep);
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		_bindings = checkIfAlreadySorted(_bindings);
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(_bindings,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		_bindings = QueryResult.createInstance();
		return msg;
	}
}
