package lupos.engine.operators.singleinput.sort;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.misc.debug.DebugStep;
import lupos.sparql1_1.Node;

/**
 * This class represents a static List which is used to sort every incoming
 * binding into a List given by this class.
 * 
 * @author Jan Kolbaum
 */

public class InsertionSort extends CollectionSort {

	// List to fill
	protected QueryResult list = getQR();

	public InsertionSort(final Node node) {
		super(node);
	}

	public InsertionSort() {
	}

	/**
	 * this method checks if the static List has already been created,
	 * 
	 * @return true if the List had already bin created, false if not.
	 */

	public boolean exists() {
		return !(list == null);
	}

	/**
	 * Adds the given binding in correct order to the List (String comparison)
	 * 
	 * @param binding
	 *            the binding to add
	 * @return true if successfully added
	 */
	@Override
	protected QueryResult postProcess(final QueryResult bindings, final int id) {
		for (final Iterator<Bindings> iter = bindings.iterator(); iter
				.hasNext();) {
			/* if( bindings.getSorted() ){merge( bindings ); return null; } */
			final Bindings binding = iter.next();
			try {

				if (list.isEmpty()) {
					list.add(binding);
					break;
				}

				if (list.size() == 1) {
					if (comparator.compare(binding, list.get(0)) > 0) {
						list.addLast(binding);
						break;
					} else {
						list.addFirst(binding);
						break;
					}
				} else {

					int min = 0;
					int max = list.size() - 1;
					while (min != max) {

						int pos = (max + min) / 2;
						if (comparator.compare(list.get(pos), binding) >= 0) {
							if (comparator.compare(list.get(pos), binding) == 0) {
								list.add(pos, binding);
								getSortedQueryResult();
							}
							if (max == pos) {
								max = max--;
							} else {
								max = pos;
							}
						} else {
							if (min == pos) {
								min = ++pos;
							} else {
								min = pos;
							}
						}
					}
					if (max == list.size() - 1
							&& comparator.compare(list.get(max), binding) < 0) {
						list.add(binding);
						break;
					}
					list.add(max, binding);
				}
			} catch (final Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		// return getSortedQueryResult( );
		return null;
	}

	@Override
	protected QueryResult merge(final QueryResult bind) {
		final Iterator<Bindings> iterBind = bind.iterator();
		Bindings binding = iterBind.next();
		for (int i = 0; i < list.size(); i++) {
			if (comparator.compare(binding, list.get(i)) <= 0) {
				list.add(i, binding);
				i++;
				if (iterBind.hasNext())
					binding = iterBind.next();
				else
					break;
			}
		}
		while (iterBind.hasNext()) {
			list.add(iterBind.next());
		}
		return getSortedQueryResult();
	}

	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		for (final Bindings b : list) {
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(b);
			}
		}
		list = QueryResult.createInstance();
		return msg;
	}

	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		preProcessMessage(new EndOfEvaluationMessage());
		return msg;
	}

	/**
	 * @return the List in descending order
	 */
	public QueryResult getSortedQueryResult() {
		return list;
	}

	public QueryResult deleteQueryResult(final QueryResult queryResult,
			final int operandID) {
		// problem: it does not count the number of occurences of a binding
		// i.e. { ?a=<a> }, { ?a=<a> } and delete { ?a=<a> } will result in
		// {} instead of { ?a=<a> }!!!!!!
		final Iterator<Bindings> itb = queryResult.oneTimeIterator();
		while (itb.hasNext())
			list.remove(itb.next());
		return null;
	}

	public void deleteAll(final int operandID) {
		list.release();
		list = QueryResult.createInstance();
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
		for (final OperatorIDTuple opId : succeedingOperators) {
			final QueryResultDebug qrDebug = new QueryResultDebug(list,
					debugstep, this, opId.getOperator(), true);
			((Operator) opId.getOperator()).processAllDebug(qrDebug, opId
					.getId(), debugstep);
		}
		list = QueryResult.createInstance();
		return msg;
	}
}
