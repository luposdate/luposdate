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
		// nothing to init...
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
