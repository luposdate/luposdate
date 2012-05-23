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
package lupos.engine.operators.singleinput;

import java.util.Iterator;

import lupos.datastructures.bindings.Bindings;
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

	private ComparatorBindings comp;

	/**
	 * Constructor sets the node, the parent and the comparator
	 * 
	 * @param node
	 */
	public Group(final lupos.sparql1_1.Node node) {
		this.comp = new ComparatorAST(node);
	}
	
	public Group(final ComparatorBindings comp) {
		this.comp = comp;
	}

	protected QueryResult queryResult;

	/**
	 * saving the QueryResult
	 * 
	 * @param QueryResult
	 * @param int
	 * @return QueryResult
	 */
	public synchronized QueryResult process(final QueryResult bindings,
			final int operandID) {

		if (queryResult == null)
			queryResult = bindings;
		else
			queryResult.addAll(bindings);
		return null;
	}
	
	/**
	 * Bindings are compared and split in separate QueryResults
	 *
	 */
	private void computeResult(){
		if (queryResult != null) {
			QueryResult newQueryResult = QueryResult.createInstance();
			Iterator<Bindings> it = queryResult.oneTimeIterator();
			Bindings oldBinding = null;
			boolean firstRun = true;
			while (it.hasNext()) {
				Bindings b = it.next();
				if (!firstRun) {
					int compareValue = comp.compare(oldBinding, b);
					if (compareValue == 0) {
						newQueryResult.add(b);
					} else {
						// Send queryResult
						for (final OperatorIDTuple opId : succeedingOperators) {
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
			for (final OperatorIDTuple opId : succeedingOperators) {
				opId.processAll(newQueryResult);
			}
		}
	}

	/**
	 * Bindings are compared and split in separate QueryResults
	 * 
	 * @param EndOfEvaluationMessage
	 * @return Message
	 */
	@Override
	public Message preProcessMessage(final EndOfEvaluationMessage msg) {
		computeResult();
		return msg;
	}

	/**
	 * Bindings are compared and split in separate QueryResults
	 * 
	 * @param ComputeIntermediateResultMessage
	 * @return Message
	 */
	@Override
	public Message preProcessMessage(final ComputeIntermediateResultMessage msg) {
		computeResult();
		return msg;
	}
	
	private void computeResultDebug(final DebugStep debugstep){
		if (queryResult != null) {
			QueryResult newQueryResult = QueryResult.createInstance();
			Iterator<Bindings> it = queryResult.oneTimeIterator();
			Bindings oldBinding = null;
			boolean firstRun = true;
			while (it.hasNext()) {
				Bindings b = it.next();
				if (!firstRun) {
					int compareValue = comp.compare(oldBinding, b);
					if (compareValue == 0) {
						newQueryResult.add(b);
					} else {
						// Send queryResult
						for (final OperatorIDTuple opId : succeedingOperators) {
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
			for (final OperatorIDTuple opId : succeedingOperators) {
				final QueryResultDebug qrDebug = new QueryResultDebug(newQueryResult, debugstep, this, opId.getOperator(), true);
				((Operator) opId.getOperator()).processAllDebug(qrDebug, opId.getId(), debugstep);
			}
		}
	}

	
	public Message preProcessMessageDebug(
			final ComputeIntermediateResultMessage msg,
			final DebugStep debugstep) {
		computeResultDebug(debugstep);
		return msg;
	}
	
	public Message preProcessMessageDebug(final EndOfEvaluationMessage msg,
			final DebugStep debugstep) {
		computeResultDebug(debugstep);
		return msg;
	}
}
