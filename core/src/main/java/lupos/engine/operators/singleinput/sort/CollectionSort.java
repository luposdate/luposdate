/**
 *
 */
package lupos.engine.operators.singleinput.sort;

import lupos.datastructures.queryresult.QueryResult;

/**
 * This is almost an abstract class, but as it needs to be
 * instaciated in operatorPipe it is not. Nevertheless it should not be
 * instaniated, as no useful results will be created. 
 * DO ONLY USE EXTENDING CLASSES
 */
public abstract class CollectionSort extends Sort {

	public CollectionSort() {
	}

	/**
	 * Contructor
	 * 
	 * @param node
	 *            the current sort node. From this node all other informations
	 *            like variables to sort after will be extracted.
	 */
	public CollectionSort(final lupos.sparql1_1.Node node) {
		super(node);
	}

	/**
	 * Processes ( => Sorts ) a given QueryResult by forwarding it to post
	 * process, which will be overritten by almost every extending class. If qr
	 * is already sorted, qr will imediatly returned or merged with an existing
	 * list.
	 * 
	 * @param qr
	 *            the QueryResult to sort
	 * @return the sorted and propably merged QueryResult
	 */
	@Override
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {
		/*
		 * if( bindings.getSorted() ) return merge( bindings ); else
		 */return postProcess(bindings, operandID);
	}

	/**
	 * in this method every subclass will have its sorting-algorithms, here
	 * sorting will be done
	 * 
	 * @param qr
	 * @return
	 */
	protected QueryResult postProcess(final QueryResult qr, final int id) {
		return qr;
	}

	/**
	 * @return a QueryResult working correctly for each subclass purposes.
	 *         Therefor ervery subclass needing another type of QueryResult (so
	 *         far only DiskBasedQueryResult is an alternative) will overwrite
	 *         this method.
	 */
	protected QueryResult getQR() {
		return QueryResult.createInstance();
	}

	/**
	 * Do only use in InsertionSort and DiskBasedInsertionSort
	 * 
	 * @param bind
	 * @return
	 */
	protected QueryResult merge(final QueryResult bind) {
		return bind;
	}
}
