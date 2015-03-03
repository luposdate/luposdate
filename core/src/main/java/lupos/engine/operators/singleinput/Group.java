
/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
 *
 * @author groppe
 * @version $Id: $Id
 */
package lupos.engine.operators.singleinput;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
import lupos.datastructures.queryresult.ParallelIteratorMultipleQueryResults;
import lupos.datastructures.queryresult.QueryResult;
import lupos.datastructures.queryresult.QueryResultDebug;
import lupos.engine.operators.Operator;
import lupos.engine.operators.OperatorIDTuple;
import lupos.engine.operators.messages.ComputeIntermediateResultMessage;
import lupos.engine.operators.messages.EndOfEvaluationMessage;
import lupos.engine.operators.messages.Message;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorAST;
import lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings;
import lupos.misc.debug.DebugStep;
public class Group extends SingleInputOperator {
	/**
	 *
	 */
	private static final long serialVersionUID = -3636219378615890918L;

	private final ComparatorBindings comp;

	/**
	 * Constructor sets the node, the parent and the comparator
	 *
	 * @param node a lupos$sparql1_1$Node object.
	 */
	public Group(final lupos.sparql1_1.Node node) {
		this.comp = new ComparatorAST(node);
	}

	/**
	 * <p>Constructor for Group.</p>
	 *
	 * @param comp a {@link lupos.engine.operators.singleinput.sort.comparator.ComparatorBindings} object.
	 */
	public Group(final ComparatorBindings comp) {
		this.comp = comp;
	}

	protected ParallelIteratorMultipleQueryResults queryResults = new ParallelIteratorMultipleQueryResults();

	/**
	 * {@inheritDoc}
	 *
	 * saving the QueryResult
	 */
	@Override
	public synchronized QueryResult process(final QueryResult queryResult, final int operandID) {
		this.queryResults.addQueryResult(queryResult);
		return null;
	}

	/**
	 * Bindings are compared and split in separate QueryResults
	 *
	 */
	private void computeResult(){
		if (!this.queryResults.isEmpty()) {
			QueryResult newQueryResult = QueryResult.createInstance();
			final Iterator<Bindings> it = this.queryResults.getQueryResult().oneTimeIterator();
			Bindings oldBinding = null;
			boolean firstRun = true;
			while (it.hasNext()) {
				final Bindings b = it.next();
				if (!firstRun) {
					final int compareValue = this.comp.compare(oldBinding, b);
					if (compareValue == 0) {
						newQueryResult.add(b);
					} else {
						// Send queryResult
						for (final OperatorIDTuple opId: this.succeedingOperators) {
							opId.processAll(newQueryResult);
						}
						newQueryResult = QueryResult.createInstance();
						newQueryResult.add(b);
					}
				} else {
					firstRun = false;
					newQueryResult.add(b);
				}
				oldBinding = b;
			}
			// Send queryResult
			for (final OperatorIDTuple opId: this.succeedingOperators) {
				opId.processAll(newQueryResult);
			}
			this.queryResults.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Bindings are compared and split in separate QueryResults
	 */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		this.computeResult();
		return msg;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Bindings are compared and split in separate QueryResults
	 */
	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		this.deleteAllAtSucceedingOperators();
		this.computeResult();
		this.queryResults.reset();
		return msg;
	}

	private void computeResultDebug(final DebugStep debugstep){
		if (!this.queryResults.isEmpty()) {
			QueryResult newQueryResult = QueryResult.createInstance();
			final Iterator<Bindings> it = this.queryResults.getQueryResult().oneTimeIterator();
			Bindings oldBinding = null;
			boolean firstRun = true;
			while (it.hasNext()) {
				final Bindings b = it.next();
				if (!firstRun) {
					final int compareValue = this.comp.compare(oldBinding, b);
					if (compareValue == 0) {
						newQueryResult.add(b);
					} else {
						// Send queryResult
						for (final OperatorIDTuple opId: this.succeedingOperators) {
							final QueryResultDebug qrDebug = new QueryResultDebug(newQueryResult, debugstep, this, opId.getOperator(), true);
							((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
						}
						newQueryResult = QueryResult.createInstance();
						newQueryResult.add(b);
					}
				} else {
					firstRun = false;
					newQueryResult.add(b);
				}
				oldBinding = b;
			}
			// Send queryResult
			for (final OperatorIDTuple opId: this.succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(newQueryResult, debugstep, this, opId.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
			}
			this.queryResults.clear();
		}
	}


	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final ComputeIntermediateResultMessage msg, final DebugStep debugstep) {
		this.computeResultDebug(debugstep);
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg, final DebugStep debugstep) {
		this.computeResultDebug(debugstep);
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public QueryResult deleteQueryResult(final QueryResult queryResult, final int operandID) {
		this.queryResults.removeAll(queryResult);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void deleteQueryResult(final int operandID) {
		this.queryResults.removeAll();
	}
}
